package com.orbis.meudia;

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
