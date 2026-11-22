package com.orbis.meudia;

import org.json.JSONObject;

/** Pure, localized spoken label for a time-conflict warning. */
final class ConflictAccessibility {
    private ConflictAccessibility() {}

    static String label(String language, JSONObject task) {
        String description = Languages.text(language, "time_conflict_warning");
        String time = TaskTime.getTime(task);
        if (time.isEmpty()) return description;
        return description + ". " + Languages.text(language, "time_conflict_at") + " " + time;
    }
}
