package com.orbis.meudia;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.util.Collections;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class RestoreStartupTest {
    @Test public void startupRecoversInterruptedFirstWriteAndDoesNotDuplicateHistory() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences tasks = context.getSharedPreferences("meu-dia-dados-v1", Context.MODE_PRIVATE);
        TaskTimeHistoryStore history = new TaskTimeHistoryStore(context);
        File journalFile = new File(context.getNoBackupFilesDir(), "restore-journal.json");
        // This test requires a fresh, disposable emulator, never personal data.
        assertFalse(journalFile.exists());
        assertFalse(tasks.contains("tasks"));
        assertTrue(history.load().isEmpty());
        String date = "2026-11-27";
        String before = "[]";
        String changed = "[{\"id\":\"fixture\",\"title\":\"Interrupted import\",\"mode\":\"once\",\"start\":\"2026-11-27\"}]";
        try {
            assertTrue(tasks.edit().putString("day", date).putString("tasks", before).commit());
            assertTrue(history.save(Collections.singletonList(new TaskTimeHistory.Session(
                    "fixture-session", "fixture-task", date, TaskTimeHistory.Kind.STOPWATCH, 1234, 0))));
            String originalHistory = history.exportSnapshot();
            RestoreRecoveryJournal journal = new RestoreRecoveryJournal(journalFile);
            journal.prepare(new JSONObject().put("day", date).put("tasks", before).toString(), originalHistory);
            // Persist exactly the intermediate state left after the first write.
            assertTrue(tasks.edit().putString("tasks", changed).commit());
            assertEquals(changed, tasks.getString("tasks", null));
            try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
                assertEquals(0, new org.json.JSONArray(tasks.getString("tasks", "[]")).length());
                assertEquals(originalHistory, new TaskTimeHistoryStore(context).exportSnapshot());
                assertFalse(journalFile.exists());
            }
            try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
                assertEquals(0, new org.json.JSONArray(tasks.getString("tasks", "[]")).length());
                assertEquals(originalHistory, new TaskTimeHistoryStore(context).exportSnapshot());
                assertEquals(1, history.load().size());
            }
        } finally {
            new RestoreRecoveryJournal(journalFile).clear();
            tasks.edit().clear().commit();
            history.save(Collections.emptyList());
        }
    }
}
