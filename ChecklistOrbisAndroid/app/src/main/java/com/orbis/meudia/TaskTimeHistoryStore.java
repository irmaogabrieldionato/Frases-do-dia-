package com.orbis.meudia;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Local, opt-in storage for completed time sessions. This store is not yet
 * connected to the timer UI or to the app's global backup/restore flow.
 */
public final class TaskTimeHistoryStore {
    private static final String PREFS = "orbis_task_time_history_v1";
    private static final String KEY = "sessions";
    private final SharedPreferences preferences;

    public TaskTimeHistoryStore(Context context) {
        if (context == null) throw new IllegalArgumentException("Missing context");
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public synchronized List<TaskTimeHistory.Session> load() {
        String raw = preferences.getString(KEY, null);
        if (raw == null) return Collections.emptyList();
        // Deliberately propagate corruption; never silently erase existing history.
        return TaskTimeHistoryCodec.decode(raw);
    }

    public synchronized boolean save(List<TaskTimeHistory.Session> sessions) {
        // Fully validate and serialize before touching persisted data.
        String encoded = TaskTimeHistoryCodec.encode(sessions);
        return preferences.edit().putString(KEY, encoded).commit();
    }

    /**
     * Import a versioned time-history payload without replacing existing sessions.
     * Invalid or conflicting imports fail before any SharedPreferences write.
     * A failed commit is reported to the caller; existing data is not cleared.
     */
    public synchronized boolean mergeRestore(String incomingEncoded) {
        List<TaskTimeHistory.Session> merged =
                TaskTimeHistoryCodec.mergeForRestore(load(), incomingEncoded);
        return save(merged);
    }

    /** Read-only export for a future user-controlled backup flow. */
    public synchronized String exportSnapshot() {
        return TaskTimeHistoryCodec.encode(load());
    }

    /** Chronological per-day history for a single task; never includes other tasks. */
    public synchronized List<TaskTimeHistory.Day> daysForTask(String taskId) {
        return TaskTimeHistory.byDate(load(), taskId);
    }

    /** Aggregate only the recorded sessions of this task. */
    public synchronized TaskTimeHistory.Total totalForTask(String taskId) {
        return TaskTimeHistory.totalForTask(load(), taskId);
    }

    /**
     * Individual sessions for one task occurrence, preserving insertion order.
     * Returns a detached, immutable snapshot, never the mutable persisted list.
     */
    public synchronized List<TaskTimeHistory.Session> sessionsForOccurrence(
            String taskId, String occurrenceDate) {
        return TaskTimeHistory.forOccurrence(load(), taskId, occurrenceDate);
    }

    public synchronized boolean append(TaskTimeHistory.Session session) {
        if (session == null) throw new IllegalArgumentException("Missing session");
        List<TaskTimeHistory.Session> all = new ArrayList<>(load());
        all.add(session);
        return save(all);
    }
}
