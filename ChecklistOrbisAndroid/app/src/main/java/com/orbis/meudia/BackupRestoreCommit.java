package com.orbis.meudia;

/**
 * Small, platform-independent boundary for committing a validated backup.
 * The caller must validate the complete backup before invoking this method.
 */
final class BackupRestoreCommit {
    interface Writer {
        boolean write(String day, String tasksJson);
    }

    interface AfterCommit {
        void run();
    }

    private BackupRestoreCommit() {}

    static boolean restore(Writer writer, String day, String tasksJson, AfterCommit afterCommit) {
        if (writer == null || afterCommit == null || day == null || tasksJson == null) {
            throw new IllegalArgumentException("Missing restore argument");
        }
        if (!writer.write(day, tasksJson)) {
            return false;
        }
        afterCommit.run();
        return true;
    }
}
