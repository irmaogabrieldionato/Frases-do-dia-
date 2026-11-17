package com.orbis.meudia;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class TaskTimeTest {
    @Test public void validatesClockAndDuration() throws Exception {
        JSONObject task = new JSONObject();
        assertFalse(TaskTime.setTime(task, "24:00"));
        assertFalse(TaskTime.setTime(task, "9:30"));
        assertFalse(TaskTime.setTime(task, "12:60"));
        assertFalse(TaskTime.setDurationMinutes(task, 0));
        assertFalse(TaskTime.setDurationMinutes(task, 1441));
        assertFalse(task.has("time"));
        assertFalse(task.has("durationMinutes"));
        assertTrue(TaskTime.setTime(task, "09:30"));
        assertTrue(TaskTime.setDurationMinutes(task, 90));
        assertEquals("09:30", TaskTime.getTime(task));
        assertEquals(90, TaskTime.getDurationMinutes(task));
        assertFalse(TaskTime.setTime(task, "09:30"));
        assertFalse(TaskTime.setDurationMinutes(task, 90));
    }

    @Test public void preservesHistoryRecurrenceAndDeadline() throws Exception {
        JSONObject task = new JSONObject("{\"id\":\"stable\",\"start\":\"2026-11-17\",\"mode\":\"weekly\",\"deadline\":\"2026-12-01\",\"history\":{\"2026-11-17\":45}}");
        assertTrue(TaskTime.setTime(task, "23:45"));
        assertTrue(TaskTime.setDurationMinutes(task, 1440));
        JSONObject restored = new JSONObject(task.toString());
        assertEquals("23:45", TaskTime.getTime(restored));
        assertEquals(1440, TaskTime.getDurationMinutes(restored));
        assertEquals("stable", restored.getString("id"));
        assertEquals("2026-11-17", restored.getString("start"));
        assertEquals("weekly", restored.getString("mode"));
        assertEquals("2026-12-01", restored.getString("deadline"));
        assertEquals(45, restored.getJSONObject("history").getInt("2026-11-17"));
    }

    @Test public void legacyAndMalformedDataAreSafe() throws Exception {
        JSONObject legacy = new JSONObject("{\"start\":\"2026-11-17\"}");
        assertEquals("", TaskTime.getTime(legacy));
        assertEquals(0, TaskTime.getDurationMinutes(legacy));
        assertFalse(TaskTime.clearTime(legacy));
        assertFalse(TaskTime.clearDuration(legacy));
        legacy.put("time", "bad");
        legacy.put("durationMinutes", "invalid");
        assertEquals("", TaskTime.getTime(legacy));
        assertEquals(0, TaskTime.getDurationMinutes(legacy));
        assertTrue(TaskTime.clearTime(legacy));
        assertTrue(TaskTime.clearDuration(legacy));
    }
}
