package com.orbis.meudia;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class TaskDuplicatorTest {
    @Test public void copyHasIndependentIdsAndHistories() throws Exception {
        JSONObject original = new JSONObject()
                .put("id", "original").put("title", "Read")
                .put("start", "2026-11-01").put("mode", "daily")
                .put("history", new JSONObject().put("2026-11-01", 80))
                .put("skips", new JSONArray().put("2026-11-02"))
                .put("subtasks", new JSONArray().put(new JSONObject()
                        .put("id", "sub-original").put("title", "Chapter")
                        .put("history", new JSONObject().put("2026-11-01", true))));
        JSONObject copy = TaskDuplicator.duplicate(original, "2026-11-16");
        assertNotEquals(original.getString("id"), copy.getString("id"));
        assertEquals("Read", copy.getString("title"));
        assertEquals("2026-11-16", copy.getString("start"));
        assertEquals(0, copy.getJSONObject("history").length());
        assertFalse(copy.has("skips"));
        JSONObject subCopy = copy.getJSONArray("subtasks").getJSONObject(0);
        assertNotEquals("sub-original", subCopy.getString("id"));
        assertEquals(0, subCopy.getJSONObject("history").length());
        assertEquals(80, original.getJSONObject("history").getInt("2026-11-01"));
        assertEquals("sub-original", original.getJSONArray("subtasks").getJSONObject(0).getString("id"));
        copy.getJSONObject("history").put("2026-11-16", 100);
        assertFalse(original.getJSONObject("history").has("2026-11-16"));
    }

    @Test public void expiredSeriesEndMovesWithStart() throws Exception {
        JSONObject source = new JSONObject().put("id", "a")
                .put("start", "2026-01-01").put("until", "2026-01-10")
                .put("mode", "daily");
        JSONObject copy = TaskDuplicator.duplicate(source, "2026-11-16");
        assertEquals("2026-11-25", copy.getString("until"));
        assertTrue(Schedule.occurs(copy, "2026-11-16"));
        assertTrue(Schedule.occurs(copy, "2026-11-25"));
        assertFalse(Schedule.occurs(copy, "2026-11-26"));
        assertEquals("2026-01-10", source.getString("until"));
    }

    @Test public void leapDaySpanUsesCalendarDays() throws Exception {
        JSONObject source = new JSONObject().put("id", "a")
                .put("start", "2024-02-28").put("until", "2024-03-01")
                .put("mode", "daily");
        JSONObject copy = TaskDuplicator.duplicate(source, "2026-02-28");
        assertEquals("2026-03-02", copy.getString("until"));
    }

    @Test public void oneOffCopyHasNoEndDateAndOldDataRemainsCompatible() throws Exception {
        JSONObject source = new JSONObject().put("id", "old")
                .put("start", "2026-01-01").put("mode", "once");
        JSONObject copy = TaskDuplicator.duplicate(source, "2026-11-16");
        assertFalse(copy.has("until"));
        assertTrue(Schedule.occurs(copy, "2026-11-16"));
        assertFalse(Schedule.occurs(copy, "2026-11-17"));
    }
}
