package com.orbis.meudia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Optional local wall-clock start time and duration for a task.
 * These fields do not modify the occurrence date, recurrence, or progress history.
 * No timezone conversion is performed: time is a local HH:mm value.
 */
final class TaskTime {
    private TaskTime() {}

    static boolean isValidTime(String time) {
        if (time == null || !time.matches("(?:[01][0-9]|2[0-3]):[0-5][0-9]")) return false;
        return true;
    }

    static String getTime(JSONObject task) {
        if (task == null) return "";
        String value = task.optString("time", "");
        return isValidTime(value) ? value : "";
    }

    static int getDurationMinutes(JSONObject task) {
        if (task == null) return 0;
        Object value = task.opt("durationMinutes");
        if (!(value instanceof Number)) return 0;
        Number number = (Number) value;
        double decimal = number.doubleValue();
        if (!Double.isFinite(decimal) || decimal != Math.rint(decimal) || decimal < 1 || decimal > 1440) return 0;
        int minutes = number.intValue();
        return minutes >= 1 && minutes <= 1440 ? minutes : 0;
    }

    /** Detects an overlap between two tasks on the same local calendar day.
     * Missing/invalid time or duration means the interval is unknown, not overlapping.
     * Endpoints touching exactly are not considered overlapping. Intervals do not wrap
     * to the following day; a duration crossing midnight is clipped at 24:00.
     */
    static boolean overlaps(JSONObject first, JSONObject second) {
        String a = getTime(first);
        String b = getTime(second);
        int aDuration = getDurationMinutes(first);
        int bDuration = getDurationMinutes(second);
        if (a.isEmpty() || b.isEmpty() || aDuration == 0 || bDuration == 0) return false;
        int aStart = Integer.parseInt(a.substring(0, 2)) * 60 + Integer.parseInt(a.substring(3));
        int bStart = Integer.parseInt(b.substring(0, 2)) * 60 + Integer.parseInt(b.substring(3));
        int aEnd = Math.min(1440, aStart + aDuration);
        int bEnd = Math.min(1440, bStart + bDuration);
        return aStart < bEnd && bStart < aEnd;
    }

    /** Counts conflicting task pairs without changing task JSON or persisted history.
     * A null or malformed array entry is ignored. The caller must supply tasks
     * for one occurrence date; this method deliberately does not infer recurrence.
     */
    static int countConflictingPairs(JSONArray tasks) {
        if (tasks == null) return 0;
        int conflicts = 0;
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject first = tasks.optJSONObject(i);
            if (first == null) continue;
            for (int j = i + 1; j < tasks.length(); j++) {
                JSONObject second = tasks.optJSONObject(j);
                if (second != null && overlaps(first, second)) conflicts++;
            }
        }
        return conflicts;
    }

    /** Indices of tasks participating in at least one overlap, in input order.
     * The caller supplies tasks for a single occurrence date. No persisted state
     * is modified and invalid entries never appear in the result.
     */
    static java.util.List<Integer> conflictingTaskIndices(JSONArray tasks) {
        java.util.List<Integer> indices = new java.util.ArrayList<>();
        if (tasks == null) return indices;
        boolean[] conflicting = new boolean[tasks.length()];
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject first = tasks.optJSONObject(i);
            if (first == null) continue;
            for (int j = i + 1; j < tasks.length(); j++) {
                JSONObject second = tasks.optJSONObject(j);
                if (second != null && overlaps(first, second)) {
                    conflicting[i] = true;
                    conflicting[j] = true;
                }
            }
        }
        for (int i = 0; i < conflicting.length; i++) {
            if (conflicting[i]) indices.add(i);
        }
        return indices;
    }

    /** Visible peers overlapping the selected task, in display order.
     * Uses the same filtered list as the UI, without reading or writing stored tasks.
     * A task is excluded by its display index, not by title or persistent ID.
     */
    static java.util.List<Integer> conflictingPeers(JSONArray visibleTasks, int selectedIndex) {
        java.util.List<Integer> peers = new java.util.ArrayList<>();
        if (visibleTasks == null || selectedIndex < 0 || selectedIndex >= visibleTasks.length()) return peers;
        JSONObject selected = visibleTasks.optJSONObject(selectedIndex);
        if (selected == null) return peers;
        for (int i = 0; i < visibleTasks.length(); i++) {
            if (i == selectedIndex) continue;
            JSONObject candidate = visibleTasks.optJSONObject(i);
            if (candidate != null && overlaps(selected, candidate)) peers.add(i);
        }
        return peers;
    }

    static boolean setTime(JSONObject task, String time) throws JSONException {
        if (task == null || !isValidTime(time)) return false;
        if (time.equals(getTime(task))) return false;
        task.put("time", time);
        return true;
    }

    static boolean clearTime(JSONObject task) {
        return task != null && task.remove("time") != null;
    }

    static boolean setDurationMinutes(JSONObject task, int minutes) throws JSONException {
        if (task == null || minutes < 1 || minutes > 1440) return false;
        if (minutes == getDurationMinutes(task)) return false;
        task.put("durationMinutes", minutes);
        return true;
    }

    static boolean clearDuration(JSONObject task) {
        return task != null && task.remove("durationMinutes") != null;
    }
}
