package com.orbis.meudia;

import org.json.JSONObject;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Finds the next eligible local occurrence without network access or side effects.
 * This is a calculation primitive; it does not install or renew an Android alarm.
 */
final class LocalReminderNextOccurrence {
    private LocalReminderNextOccurrence() {}

    static long find(JSONObject task, TimeZone zone, long nowMillis) {
        if (task == null || zone == null || TaskTime.getTime(task).isEmpty()) return -1L;
        String start = task.optString("start", "");
        if (!start.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) return -1L;
        Calendar first = Calendar.getInstance(zone);
        first.setLenient(false);
        try {
            first.clear();
            first.set(Integer.parseInt(start.substring(0, 4)),
                    Integer.parseInt(start.substring(5, 7)) - 1,
                    Integer.parseInt(start.substring(8, 10)));
            first.getTimeInMillis();
        } catch (RuntimeException invalid) { return -1L; }

        Calendar day = Calendar.getInstance(zone);
        day.setTimeInMillis(nowMillis);
        day.set(Calendar.HOUR_OF_DAY, 12);
        day.set(Calendar.MINUTE, 0);
        day.set(Calendar.SECOND, 0);
        day.set(Calendar.MILLISECOND, 0);
        Calendar firstNoon = (Calendar) first.clone();
        firstNoon.set(Calendar.HOUR_OF_DAY, 12);
        if (day.before(firstNoon)) day = firstNoon;

        // A four-year cycle includes a leap day; cover one extra day for
        // yearly reminders and avoid unbounded iteration for malformed data.
        for (int i = 0; i <= 1462; i++) {
            String key = Schedule.key(day);
            String until = task.optString("until", "");
            if (!until.isEmpty() && key.compareTo(until) > 0) return -1L;
            if (Schedule.occurs(task, key)) {
                long millis = LocalReminderTime.occurrenceMillis(task, key, zone, nowMillis);
                if (millis > nowMillis) return millis;
            }
            day.add(Calendar.DAY_OF_YEAR, 1);
        }
        return -1L;
    }
}
