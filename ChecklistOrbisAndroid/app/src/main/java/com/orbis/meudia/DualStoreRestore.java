package com.orbis.meudia;

/**
 * Coordinates two independently persisted offline snapshots.
 * The caller must validate both new snapshots before calling restore.
 * If the second write fails, the first store is restored from its prior snapshot.
 * This is compensating rollback, not a crash-safe database transaction.
 */
final class DualStoreRestore {
    interface Store {
        String snapshot();
        boolean write(String value);
    }

    private DualStoreRestore() {}

    static boolean restore(Store tasks, String newTasks, Store history, String newHistory) {
        if (tasks == null || history == null || tasks == history
                || newTasks == null || newHistory == null)
            throw new IllegalArgumentException("Missing restore argument");
        // Capture both originals before any mutation.
        String oldTasks = tasks.snapshot();
        String oldHistory = history.snapshot();
        if (oldTasks == null || oldHistory == null)
            throw new IllegalStateException("Cannot snapshot existing data");
        if (!tasks.write(newTasks)) return false;
        try {
            if (history.write(newHistory)) return true;
        } catch (RuntimeException failure) {
            rollback(tasks, oldTasks, failure);
            throw failure;
        }
        rollback(tasks, oldTasks, null);
        return false;
    }

    private static void rollback(Store tasks, String oldTasks, RuntimeException cause) {
        try {
            if (!tasks.write(oldTasks))
                throw new IllegalStateException("Task rollback failed; manual recovery required");
        } catch (RuntimeException rollbackFailure) {
            if (cause != null) rollbackFailure.addSuppressed(cause);
            throw rollbackFailure;
        }
    }
}
