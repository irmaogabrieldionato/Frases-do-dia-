package com.orbis.meudia;

import org.json.JSONObject;
import org.junit.Test;
import java.util.Calendar;
import java.util.TimeZone;
import static org.junit.Assert.*;

/** Offline reminder regression tests for calendar edge cases. */
public class LocalReminderCalendarEdgeTest {
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    @Test public void leapDayIsValidOnlyInLeapYears() throws Exception {
        JSONObject task = new JSONObject().put("time", "12:30");
        assertTrue(LocalReminderTime.occurrenceMillis(task, "2028-02-29", UTC, 0L) > 0L);
        assertEquals(-1L, LocalReminderTime.occurrenceMillis(task, "2027-02-29", UTC, 0L));
    }

    @Test public void invalidMonthAndDayAreRejected() throws Exception {
        JSONObject task = new JSONObject().put("time", "08:00");
        assertEquals(-1L, LocalReminderTime.occurrenceMillis(task, "2026-04-31", UTC, 0L));
        assertEquals(-1L, LocalReminderTime.occurrenceMillis(task, "2026-13-01", UTC, 0L));
        assertEquals(-1L, LocalReminderTime.occurrenceMillis(task, "2026-00-10", UTC, 0L));
    }

    @Test public void springForwardNonexistentLocalTimeIsRejected() throws Exception {
        JSONObject task = new JSONObject().put("time", "02:30");
        TimeZone ny = TimeZone.getTimeZone("America/New_York");
        assertEquals(-1L, LocalReminderTime.occurrenceMillis(task, "2026-03-08", ny, 0L));
        assertTrue(LocalReminderTime.occurrenceMillis(task, "2026-03-09", ny, 0L) > 0L);
    }

    @Test public void missingTimeOrInvalidDateNeverSchedules() throws Exception {
        JSONObject task = new JSONObject();
        assertEquals(-1L, LocalReminderTime.occurrenceMillis(task, "2026-11-20", UTC, 0L));
        task.put("time", "09:00");
        assertEquals(-1L, LocalReminderTime.occurrenceMillis(task, "20/11/2026", UTC, 0L));
        assertEquals(-1L, LocalReminderTime.occurrenceMillis(task, null, UTC, 0L));
    }
}
