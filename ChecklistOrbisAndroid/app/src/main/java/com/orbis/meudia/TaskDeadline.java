package com.orbis.meudia;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/** Optional deadline independent of the scheduled occurrence date. */
final class TaskDeadline {
    private TaskDeadline() {}

    static boolean isValid(String day) {
        if (day == null || !day.matches("\\d{4}-\\d{2}-\\d{2}")) return false;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        format.setLenient(false);
        try {
            return day.equals(format.format(format.parse(day)));
        } catch (ParseException exception) {
            return false;
        }
    }

    static boolean set(JSONObject task, String deadline) throws JSONException {
        if (task == null || !isValid(deadline)) return false;
        if (deadline.equals(task.optString("deadline", ""))) return false;
        task.put("deadline", deadline);
        return true;
    }

    static boolean clear(JSONObject task) {
        return task != null && task.remove("deadline") != null;
    }

    static String get(JSONObject task) {
        if (task == null) return "";
        String value = task.optString("deadline", "");
        return isValid(value) ? value : "";
    }
}
