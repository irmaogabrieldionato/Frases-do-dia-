package com.orbis.meudia;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class TaskTimeOverlapTest {
    private JSONObject task(String time, int minutes) throws Exception {
        JSONObject task = new JSONObject();
        task.put("time", time);
        task.put("durationMinutes", minutes);
        return task;
    }

    @Test public void overlappingAndSymmetric() throws Exception {
        JSONObject a = task("09:00", 60);
        JSONObject b = task("09:30", 60);
        assertTrue(TaskTime.overlaps(a, b));
        assertTrue(TaskTime.overlaps(b, a));
    }

    @Test public void touchingEndpointsDoNotOverlap() throws Exception {
        assertFalse(TaskTime.overlaps(task("09:00", 60), task("10:00", 30)));
        assertFalse(TaskTime.overlaps(task("10:00", 30), task("09:00", 60)));
    }

    @Test public void midnightClipsAtEndOfDay() throws Exception {
        assertTrue(TaskTime.overlaps(task("23:50", 120), task("23:55", 5)));
        assertFalse(TaskTime.overlaps(task("23:50", 120), task("00:00", 30)));
    }

    @Test public void missingOrInvalidIntervalsNeverConflict() throws Exception {
        assertFalse(TaskTime.overlaps(task("25:00", 60), task("09:00", 60)));
        assertFalse(TaskTime.overlaps(task("09:00", 0), task("09:00", 60)));
        assertFalse(TaskTime.overlaps(new JSONObject(), task("09:00", 60)));
        assertFalse(TaskTime.overlaps(null, task("09:00", 60)));
    }

    @Test public void doesNotMutateTaskData() throws Exception {
        JSONObject a = new JSONObject("{\"time\":\"09:00\",\"durationMinutes\":60,\"history\":{\"2026-11-21\":40}}");
        String original = a.toString();
        TaskTime.overlaps(a, task("09:20", 20));
        assertEquals(original, a.toString());
    }
}
