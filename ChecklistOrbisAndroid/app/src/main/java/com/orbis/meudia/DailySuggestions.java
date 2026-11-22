package com.orbis.meudia;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/** Read-only, offline suggestions for unfinished occurrences on a selected day. */
final class DailySuggestions {
    static List<JSONObject> pending(JSONArray tasks, String day) {
        ArrayList<JSONObject> result = new ArrayList<>();
        if (tasks == null || !TaskDeadline.isValid(day)) return result;
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject task = tasks.optJSONObject(i);
            if (task == null || !Schedule.occurs(task, day)) continue;
            JSONObject history = task.optJSONObject("history");
            int value = history == null ? 0 : history.optInt(day, 0);
            if (value < 100) result.add(task);
        }
        return result;
    }
}
