package com.orbis.meudia;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class ScheduleInvalidDateTest {
    private JSONObject task(String start, String mode) throws Exception {
        return new JSONObject().put("start", start).put("mode", mode);
    }

    @Test public void invalidCalendarDatesAreRejected() {
        assertFalse(Schedule.validDateKey("2026-02-29"));
        assertFalse(Schedule.validDateKey("2026-13-01"));
        assertFalse(Schedule.validDateKey("2026-04-31"));
        assertFalse(Schedule.validDateKey("2026-1-01"));
        assertFalse(Schedule.validDateKey(""));
        assertTrue(Schedule.validDateKey("2024-02-29"));
    }

    @Test public void invalidStartAndSelectedDateNeverCreateOccurrences() throws Exception {
        assertFalse(Schedule.occurs(task("2026-02-29", "daily"), "2026-03-01"));
        assertFalse(Schedule.occurs(task("2026-02-28", "daily"), "2026-02-30"));
        assertFalse(Schedule.occurs(task("2026-02-28", "once"), "2026-02-30"));
    }

    @Test public void invalidEndDateCannotExpandRecurrence() throws Exception {
        JSONObject t = task("2026-01-01", "daily").put("until", "2026-02-30");
        assertFalse(Schedule.occurs(t, "2026-01-02"));
    }

    @Test public void validLeapDayRecurrenceRemainsAvailable() throws Exception {
        assertTrue(Schedule.occurs(task("2024-02-29", "yearly"), "2028-02-29"));
        assertTrue(Schedule.occurs(task("2026-01-31", "monthly"), "2026-02-28"));
    }
}
