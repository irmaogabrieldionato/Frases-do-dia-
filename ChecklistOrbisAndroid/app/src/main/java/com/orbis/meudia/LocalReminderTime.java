package com.orbis.meudia;

import org.json.JSONObject;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Pure offline reminder-time calculation. Does not request permissions or schedule alarms.
 * Returns -1 for missing/invalid dates, missing time, past occurrences and DST gaps.
 */
final class LocalReminderTime {
    private LocalReminderTime() {}

    static long occurrenceMillis(JSONObject task, String occurrenceDate, TimeZone zone, long nowMillis) {
        String clock = TaskTime.getTime(task);
        if (clock.isEmpty() || occurrenceDate == null || zone == null ||
                !occurrenceDate.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) return -1L;
        try {
            int year = Integer.parseInt(occurrenceDate.substring(0, 4));
            int month = Integer.parseInt(occurrenceDate.substring(5, 7));
            int day = Integer.parseInt(occurrenceDate.substring(8, 10));
            int hour = Integer.parseInt(clock.substring(0, 2));
            int minute = Integer.parseInt(clock.substring(3, 5));
            Calendar cal = Calendar.getInstance(zone);
            cal.clear();
            cal.setLenient(false);
            cal.set(year, month - 1, day, hour, minute, 0);
            long millis = cal.getTimeInMillis();
            // Reject nonexistent local wall-clock times during daylight-saving jumps.
            Calendar roundTrip = Calendar.getInstance(zone);
            roundTrip.setTimeInMillis(millis);
            if (roundTrip.get(Calendar.YEAR) != year ||
                roundTrip.get(Calendar.MONTH) != month - 1 ||
                roundTrip.get(Calendar.DAY_OF_MONTH) != day ||
                roundTrip.get(Calendar.HOUR_OF_DAY) != hour ||
                roundTrip.get(Calendar.MINUTE) != minute) return -1L;
            return millis > nowMillis ? millis : -1L;
        } catch (RuntimeException invalid) {
            return -1L;
        }
    }
}
