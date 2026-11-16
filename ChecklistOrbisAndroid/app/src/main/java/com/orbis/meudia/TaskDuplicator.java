package com.orbis.meudia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.UUID;

/** Copies a task plan without copying completion records or occurrence exclusions. */
final class TaskDuplicator {
    private TaskDuplicator() {}

    static JSONObject duplicate(JSONObject source, String newStart) throws JSONException {
        JSONObject copy = new JSONObject(source.toString());
        String oldStart = source.optString("start", "");
        copy.put("id", UUID.randomUUID().toString());
        copy.put("start", newStart);
        copy.put("history", new JSONObject());
        copy.remove("skips");

        // Preserve the original series' calendar-day span when moving its start.
        // This prevents an expired end date from silently hiding the duplicate.
        String until = source.optString("until", "");
        if (!until.isEmpty() && !oldStart.isEmpty()) {
            Calendar oldFirst = Schedule.date(oldStart);
            Calendar oldLast = Schedule.date(until);
            int span = Schedule.dayDiff(oldFirst, oldLast);
            if (span >= 0) {
                Calendar shifted = Schedule.date(newStart);
                shifted.add(Calendar.DAY_OF_YEAR, span);
                copy.put("until", Schedule.key(shifted));
            } else {
                copy.remove("until"); // malformed legacy range must not hide the copy
            }
        }

        JSONArray subtasks = copy.optJSONArray("subtasks");
        if (subtasks != null) {
            for (int i = 0; i < subtasks.length(); i++) {
                JSONObject sub = subtasks.optJSONObject(i);
                if (sub != null) {
                    sub.put("id", UUID.randomUUID().toString());
                    sub.put("history", new JSONObject());
                }
            }
        }
        return copy;
    }
}
