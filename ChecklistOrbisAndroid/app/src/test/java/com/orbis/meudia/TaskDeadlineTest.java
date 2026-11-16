package com.orbis.meudia;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class TaskDeadlineTest {
    @Test public void independentOfStartAndHistory() throws Exception {
        JSONObject task = new JSONObject("{\"id\":\"old\",\"start\":\"2026-11-16\",\"history\":{\"2026-11-15\":75}}");
        assertTrue(TaskDeadline.set(task, "2026-12-31"));
        assertEquals("2026-12-31", TaskDeadline.get(task));
        assertEquals("2026-11-16", task.getString("start"));
        assertEquals(75, task.getJSONObject("history").getInt("2026-11-15"));
        assertEquals("old", task.getString("id"));
        assertFalse(TaskDeadline.set(task, "2026-12-31"));
    }

    @Test public void rejectsImpossibleDatesWithoutMutation() throws Exception {
        JSONObject task = new JSONObject("{\"start\":\"2026-11-16\"}");
        assertFalse(TaskDeadline.set(task, "2026-02-29"));
        assertFalse(TaskDeadline.set(task, "2026-04-31"));
        assertFalse(TaskDeadline.set(task, "tomorrow"));
        assertFalse(task.has("deadline"));
        assertTrue(TaskDeadline.set(task, "2028-02-29"));
    }

    @Test public void legacyAndClear() throws Exception {
        JSONObject task = new JSONObject("{\"start\":\"2026-11-16\"}");
        assertEquals("", TaskDeadline.get(task));
        assertFalse(TaskDeadline.clear(task));
        assertTrue(TaskDeadline.set(task, "2026-11-20"));
        assertTrue(TaskDeadline.clear(task));
        assertEquals("", TaskDeadline.get(task));
        assertEquals("2026-11-16", task.getString("start"));
    }
}
