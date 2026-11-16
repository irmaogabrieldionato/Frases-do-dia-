package com.orbis.meudia;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/** Moves a one-time task without touching its identity or completion history. */
final class TaskRescheduler {
    private TaskRescheduler() {}

    private static boolean validDay(String day) {
        if (day == null || !day.matches("\\d{4}-\\d{2}-\\d{2}")) return false;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        format.setLenient(false);
        try {
            return day.equals(format.format(format.parse(day)));
        } catch (ParseException exception) {
            return false;
        }
    }

    static boolean moveOnce(JSONObject task, String newDay) throws JSONException {
        if (!"once".equals(task.optString("mode", "once"))) return false;
        if (!validDay(newDay)) return false;
        String current = task.optString("start", "");
        if (newDay.equals(current)) return false;
        // Update only the scheduling field: historical percentages and subtasks remain intact.
        task.put("start", newDay);
        return true;
    }
}
