package com.orbis.meudia;

import org.json.JSONException;
import org.json.JSONObject;

/** Moves a one-time task without touching its identity or completion history. */
final class TaskRescheduler {
    private TaskRescheduler() {}

    static boolean moveOnce(JSONObject task, String newDay) throws JSONException {
        if (!"once".equals(task.optString("mode", "once"))) return false;
        if (newDay == null || !newDay.matches("\\d{4}-\\d{2}-\\d{2}")) return false;
        String current = task.optString("start", "");
        if (newDay.equals(current)) return false;
        // Update only the scheduling field: historical percentages and subtasks remain intact.
        task.put("start", newDay);
        return true;
    }
}
