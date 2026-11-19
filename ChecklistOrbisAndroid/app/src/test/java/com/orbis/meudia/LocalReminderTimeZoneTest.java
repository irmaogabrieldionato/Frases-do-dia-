package com.orbis.meudia;

import org.json.JSONObject;
import org.junit.Test;
import java.util.Calendar;
import java.util.TimeZone;
import static org.junit.Assert.*;

/** Regression coverage for offline alarm recalculation after time-zone changes. */
public class LocalReminderTimeZoneTest {
    private static long at(TimeZone zone, int year, int month, int day, int hour, int minute) {
        Calendar calendar = Calendar.getInstance(zone);
        calendar.clear();
        calendar.setLenient(false);
        calendar.set(year, month - 1, day, hour, minute);
        return calendar.getTimeInMillis();
    }

    @Test public void sameLocalClockIsRecomputedForNewTimeZone() throws Exception {
        JSONObject task = new JSONObject().put("time", "09:15");
        TimeZone utc = TimeZone.getTimeZone("UTC");
        TimeZone saoPaulo = TimeZone.getTimeZone("America/Sao_Paulo");
        long inUtc = LocalReminderTime.occurrenceMillis(task, "2026-11-20", utc, 0L);
        long inBrazil = LocalReminderTime.occurrenceMillis(task, "2026-11-20", saoPaulo, 0L);
        assertEquals(at(utc, 2026, 11, 20, 9, 15), inUtc);
        assertEquals(at(saoPaulo, 2026, 11, 20, 9, 15), inBrazil);
        assertEquals(3L * 60L * 60L * 1000L, inBrazil - inUtc);
    }

    @Test public void oldOccurrenceIsNotRescheduledAfterClockChange() throws Exception {
        JSONObject task = new JSONObject().put("time", "09:15");
        TimeZone zone = TimeZone.getTimeZone("UTC");
        long due = at(zone, 2026, 11, 20, 9, 15);
        assertEquals(-1L, LocalReminderTime.occurrenceMillis(task, "2026-11-20", zone, due));
        assertEquals(-1L, LocalReminderTime.occurrenceMillis(task, "2026-11-20", zone, due + 1000L));
    }

    @Test public void repeatedDstHourIsAValidLocalTime() throws Exception {
        JSONObject task = new JSONObject().put("time", "01:30");
        TimeZone zone = TimeZone.getTimeZone("America/New_York");
        assertTrue(LocalReminderTime.occurrenceMillis(task, "2026-11-01", zone, 0L) > 0L);
    }
}
