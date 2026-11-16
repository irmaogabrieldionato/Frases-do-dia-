package com.orbis.meudia;

import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.Test;
import java.util.TimeZone;
import java.util.Calendar;
import static org.junit.Assert.*;

/** Pure JVM regression checks: date arithmetic and existing recurring tasks. */
public class ScheduleTest {
    private JSONObject task(String start, String mode) throws Exception {
        return new JSONObject().put("start", start).put("mode", mode);
    }

    @Test public void monthlyClampsDay31ToShortMonths() throws Exception {
        JSONObject task = task("2024-01-31", "monthly");
        assertTrue(Schedule.occurs(task, "2024-02-29"));
        assertTrue(Schedule.occurs(task, "2024-04-30"));
        assertFalse(Schedule.occurs(task, "2024-02-28"));
        assertTrue(Schedule.occurs(task, "2024-03-31"));
    }

    @Test public void leapDayYearlyClampsThenReturnsToLeapDay() throws Exception {
        JSONObject task = task("2024-02-29", "yearly");
        assertTrue(Schedule.occurs(task, "2025-02-28"));
        assertFalse(Schedule.occurs(task, "2025-03-01"));
        assertTrue(Schedule.occurs(task, "2028-02-29"));
    }

    @Test public void weeklyMaskAndOccurrenceLimit() throws Exception {
        JSONObject task = task("2026-11-16", "weekly"); // Monday
        int monday = 1 << (Calendar.MONDAY - 1);
        int wednesday = 1 << (Calendar.WEDNESDAY - 1);
        task.put("weekdays", monday | wednesday).put("count", 3);
        assertTrue(Schedule.occurs(task, "2026-11-16"));
        assertTrue(Schedule.occurs(task, "2026-11-18"));
        assertFalse(Schedule.occurs(task, "2026-11-17"));
        assertTrue(Schedule.occurs(task, "2026-11-23"));
        assertFalse(Schedule.occurs(task, "2026-11-25"));
    }

    @Test public void skipsAndUntilAreHonoredWithoutMutatingTask() throws Exception {
        JSONObject task = task("2026-11-15", "daily");
        task.put("until", "2026-11-18");
        task.put("skips", new JSONArray().put("2026-11-16"));
        assertTrue(Schedule.occurs(task, "2026-11-15"));
        assertFalse(Schedule.occurs(task, "2026-11-16"));
        assertTrue(Schedule.occurs(task, "2026-11-18"));
        assertFalse(Schedule.occurs(task, "2026-11-19"));
        assertEquals("2026-11-15", task.getString("start"));
    }

    @Test public void datesAcrossDaylightSavingUseCalendarDays() {
        TimeZone old = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
            assertEquals(2, Schedule.dayDiff(Schedule.date("2026-03-07"), Schedule.date("2026-03-09")));
            assertEquals(2, Schedule.dayDiff(Schedule.date("2026-10-31"), Schedule.date("2026-11-02")));
        } finally {
            TimeZone.setDefault(old);
        }
    }
}
