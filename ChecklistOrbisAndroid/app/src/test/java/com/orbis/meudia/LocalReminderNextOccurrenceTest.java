package com.orbis.meudia;

import org.json.JSONObject;
import org.junit.Test;
import java.util.Calendar;
import java.util.TimeZone;
import static org.junit.Assert.*;

public class LocalReminderNextOccurrenceTest {
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private long at(int year, int month, int day, int hour) {
        Calendar c = Calendar.getInstance(UTC);
        c.clear();
        c.set(year, month - 1, day, hour, 0);
        return c.getTimeInMillis();
    }

    @Test public void dailySkipsElapsedTime() throws Exception {
        JSONObject task = new JSONObject().put("start", "2026-11-18")
                .put("mode", "daily").put("time", "09:00");
        assertEquals(at(2026, 11, 20, 9),
                LocalReminderNextOccurrence.find(task, UTC, at(2026, 11, 19, 10)));
    }

    @Test public void skippedOccurrenceIsNotSelected() throws Exception {
        JSONObject task = new JSONObject().put("start", "2026-11-19")
                .put("mode", "daily").put("time", "09:00")
                .put("skips", new org.json.JSONArray().put("2026-11-20"));
        assertEquals(at(2026, 11, 21, 9),
                LocalReminderNextOccurrence.find(task, UTC, at(2026, 11, 19, 10)));
    }

    @Test public void oncePastOrFiniteRecurrenceReturnsNone() throws Exception {
        JSONObject once = new JSONObject().put("start", "2026-11-19")
                .put("mode", "once").put("time", "09:00");
        assertEquals(-1L, LocalReminderNextOccurrence.find(once, UTC, at(2026, 11, 19, 10)));
        JSONObject daily = new JSONObject().put("start", "2026-11-19")
                .put("mode", "daily").put("count", 1).put("time", "09:00");
        assertEquals(-1L, LocalReminderNextOccurrence.find(daily, UTC, at(2026, 11, 19, 10)));
    }

    @Test public void leapDayYearlyFindsNextLeapYear() throws Exception {
        JSONObject task = new JSONObject().put("start", "2024-02-29")
                .put("mode", "yearly").put("time", "09:00");
        // Existing Schedule rules clamp Feb 29 to Feb 28 in non-leap years.
        assertEquals(at(2027, 2, 28, 9),
                LocalReminderNextOccurrence.find(task, UTC, at(2026, 3, 1, 0)));
    }
}
