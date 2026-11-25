package com.orbis.meudia;

import android.util.AtomicFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.json.JSONObject;
import org.json.JSONException;
import java.nio.charset.StandardCharsets;

/** Durable pre-write snapshots. Caller must serialize all store access and
 * recover before allowing normal reads or writes. Not yet wired into UI. */
final class RestoreRecoveryJournal {
    private final AtomicFile file;

    RestoreRecoveryJournal(File privateFile) {
        file = new AtomicFile(privateFile);
    }

    private boolean hasCommittedSnapshot() {
        return file.getBaseFile().exists() || sidecar(".bak").exists();
    }

    private File sidecar(String suffix) {
        return new File(file.getBaseFile().getPath() + suffix);
    }

    // A lone .new is an interrupted preparation, never a committed snapshot.
    // No store writes may begin until prepare returns successfully.
    private void discardUncommittedPreparation() throws IOException {
        if (!hasCommittedSnapshot() && sidecar(".new").exists()) {
            if (!sidecar(".new").delete() && sidecar(".new").exists())
                throw new IOException("Cannot discard interrupted preparation");
        }
    }

    void prepare(String tasks, String history) throws IOException {
        if (tasks == null || history == null) throw new IllegalArgumentException("Missing snapshot");
        if (hasCommittedSnapshot())
            throw new IOException("Recovery already pending");
        discardUncommittedPreparation();
        final byte[] bytes;
        try {
            bytes = new JSONObject().put("version", 1).put("tasks", tasks)
                    .put("history", history).toString().getBytes(StandardCharsets.UTF_8);
        } catch (JSONException e) {
            throw new IOException("Cannot encode recovery snapshots", e);
        }
        FileOutputStream out = file.startWrite();
        try {
            out.write(bytes);
            out.getFD().sync();
            file.finishWrite(out);
        } catch (IOException | RuntimeException e) {
            file.failWrite(out);
            throw e;
        }
        // AtomicFile may log rather than throw on rename failure.
        if (!java.util.Arrays.equals(bytes, file.readFully()))
            throw new IOException("Recovery journal verification failed");
    }

    /** Caller serializes access and validates both replacement snapshots first.
     * A pending journal is retained on any exception, including failed rollback.
     * Startup must recover before normal store access.
     */
    boolean restore(DualStoreRestore.Store tasks, String newTasks,
                    DualStoreRestore.Store history, String newHistory) throws IOException {
        if (tasks == null || history == null || tasks == history
                || newTasks == null || newHistory == null)
            throw new IllegalArgumentException("Missing restore argument");
        prepare(tasks.snapshot(), history.snapshot());
        boolean restored = DualStoreRestore.restore(tasks, newTasks, history, newHistory);
        // False means compensating rollback succeeded. Exceptions retain the journal.
        clear();
        return restored;
    }

    boolean recover(DualStoreRestore.Store tasks, DualStoreRestore.Store history) throws IOException {
        if (!hasCommittedSnapshot()) {
            discardUncommittedPreparation();
            return false;
        }
        final String oldTasks, oldHistory;
        try {
            JSONObject data = new JSONObject(new String(file.readFully(), StandardCharsets.UTF_8));
            if (data.getInt("version") != 1) throw new IOException("Unknown recovery version");
            oldTasks = data.getString("tasks");
            oldHistory = data.getString("history");
        } catch (JSONException e) {
            throw new IOException("Invalid recovery journal", e);
        }
        if (!history.write(oldHistory) || !tasks.write(oldTasks))
            throw new IOException("Recovery incomplete; journal retained");
        clear();
        return true;
    }

    void clear() throws IOException {
        file.delete();
        if (hasCommittedSnapshot() || sidecar(".new").exists())
            throw new IOException("Cannot clear recovery journal");
    }
}
