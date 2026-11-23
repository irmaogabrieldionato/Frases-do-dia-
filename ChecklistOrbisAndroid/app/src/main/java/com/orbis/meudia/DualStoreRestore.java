package com.orbis.meudia;

/**
 * Coordinates two independently persisted offline snapshots.
 * The caller must validate both new snapshots before calling restore.
 * A failed write may have modified its store before returning false or throwing.
 * This provides best-effort compensating rollback, NOT crash-safe atomicity.
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
        String oldTasks = tasks.snapshot();
        String oldHistory = history.snapshot();
        if (oldTasks == null || oldHistory == null)
            throw new IllegalStateException("Cannot snapshot existing data");

        // A false result or exception can occur after a partial persistence.
        final boolean taskWritten;
        try {
            taskWritten = tasks.write(newTasks);
        } catch (RuntimeException failure) {
            rollback(tasks, oldTasks, null, null, failure);
            throw failure;
        }
        if (!taskWritten) {
            rollback(tasks, oldTasks, null, null, null);
            return false;
        }

        final boolean historyWritten;
        try {
            historyWritten = history.write(newHistory);
        } catch (RuntimeException failure) {
            rollback(tasks, oldTasks, history, oldHistory, failure);
            throw failure;
        }
        if (!historyWritten) {
            rollback(tasks, oldTasks, history, oldHistory, null);
            return false;
        }
        return true;
    }

    private static void rollback(Store tasks, String oldTasks, Store history,
                                 String oldHistory, RuntimeException cause) {
        IllegalStateException recoveryFailure = null;
        // Reverse order: restore the last store attempted before the first.
        if (history != null) {
            try {
                if (!history.write(oldHistory))
                    throw new IllegalStateException("History rollback rejected");
            } catch (RuntimeException failure) {
                recoveryFailure = new IllegalStateException(
                        "Restore rollback incomplete; manual recovery required", failure);
            }
        }
        try {
            if (!tasks.write(oldTasks))
                throw new IllegalStateException("Task rollback rejected");
        } catch (RuntimeException failure) {
            if (recoveryFailure == null)
                recoveryFailure = new IllegalStateException(
                        "Restore rollback incomplete; manual recovery required", failure);
            else recoveryFailure.addSuppressed(failure);
        }
        if (recoveryFailure != null) {
            if (cause != null) recoveryFailure.addSuppressed(cause);
            throw recoveryFailure;
        }
    }
}
