package com.orbis.meudia;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class TaskReschedulerTest {
    @Test public void movePreservesDailyHistoryAndIdentity() throws Exception {
        JSONObject item = new JSONObject().put("id", "original").put("start", "2026-11-01")
                .put("mode", "once").put("priority", 5)
                .put("history", new JSONObject().put("2026-11-01", 75))
                .put("subtasks", new JSONArray().put(new JSONObject().put("id", "sub1")
                        .put("history", new JSONObject().put("2026-11-01", true))));
        assertTrue(TaskRescheduler.moveOnce(item, "2026-11-20"));
        assertEquals("original", item.getString("id"));
        assertEquals(5, item.getInt("priority"));
        assertEquals("2026-11-20", item.getString("start"));
        assertEquals(75, item.getJSONObject("history").getInt("2026-11-01"));
        assertTrue(item.getJSONArray("subtasks").getJSONObject(0)
                .getJSONObject("history").getBoolean("2026-11-01"));
        assertTrue(Schedule.occurs(item, "2026-11-20"));
        assertFalse(Schedule.occurs(item, "2026-11-01"));
    }

    @Test public void noOpAndRecurringTasksRemainUnchanged() throws Exception {
        JSONObject old = new JSONObject().put("start", "2026-11-01");
        assertFalse(TaskRescheduler.moveOnce(old, "2026-11-01"));
        assertEquals("2026-11-01", old.getString("start"));
        assertFalse(TaskRescheduler.moveOnce(old, ""));
        JSONObject recurring = new JSONObject().put("mode", "weekly")
                .put("start", "2026-11-01").put("history", new JSONObject().put("2026-11-01", 30));
        assertFalse(TaskRescheduler.moveOnce(recurring, "2026-11-20"));
        assertEquals("2026-11-01", recurring.getString("start"));
        assertEquals(30, recurring.getJSONObject("history").getInt("2026-11-01"));
    }

    @Test public void legacyTaskWithoutHistoryCanMove() throws Exception {
        JSONObject old = new JSONObject().put("title", "legacy").put("start", "2026-01-01");
        assertTrue(TaskRescheduler.moveOnce(old, "2026-11-16"));
        assertEquals("legacy", old.getString("title"));
        assertFalse(old.has("history"));
    }
}
