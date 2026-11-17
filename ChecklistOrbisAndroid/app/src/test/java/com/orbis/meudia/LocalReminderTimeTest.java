package com.orbis.meudia;

import org.json.JSONObject;
import org.junit.Test;
import java.util.Calendar;
import java.util.TimeZone;
import static org.junit.Assert.*;

public class LocalReminderTimeTest {
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private long utc(int year, int month, int day, int hour, int minute) {
        Calendar c = Calendar.getInstance(UTC);
        c.clear();
        c.set(year, month - 1, day, hour, minute);
        return c.getTimeInMillis();
    }

    @Test public void futureOccurrenceAndPastOccurrence() throws Exception {
        JSONObject task = new JSONObject().put("time", "09:30").put("history", new JSONObject().put("2026-11-17", 75));
        long expected = utc(2026, 11, 18, 9, 30);
        assertEquals(expected, LocalReminderTime.occurrenceMillis(task, "2026-11-18", UTC, expected - 1));
        assertEquals(-1L, LocalReminderTime.occurrenceMillis(task, "2026-11-18", UTC, expected));
        assertEquals(75, task.getJSONObject("history").getInt("2026-11-17"));
    }

    @Test public void rejectsInvalidAndLegacyValues() throws Exception {
        JSONObject task = new JSONObject();
        assertEquals(-1L, LocalReminderTime.occurrenceMillis(task, "2026-11-18", UTC, 0));
        task.put("time", "10:00");
        for (String invalid : new String[]{"2026-02-29", "2026-13-01", "2026-00-01", "not-a-date"}) {
            assertEquals(-1L, LocalReminderTime.occurrenceMillis(task, invalid, UTC, 0));
        }
        assertEquals(utc(2028, 2, 29, 10, 0),
                LocalReminderTime.occurrenceMillis(task, "2028-02-29", UTC, 0));
    }

    @Test public void rejectsDaylightSavingGap() throws Exception {
        JSONObject task = new JSONObject().put("time", "02:30");
        TimeZone ny = TimeZone.getTimeZone("America/New_York");
        assertEquals(-1L, LocalReminderTime.occurrenceMillis(task, "2026-03-08", ny, 0));
        assertTrue(LocalReminderTime.occurrenceMillis(task, "2026-03-09", ny, 0) > 0);
    }
}
