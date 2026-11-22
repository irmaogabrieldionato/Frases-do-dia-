package com.orbis.meudia;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.*;

public class ConflictPeersTest {
    private static JSONObject task(String title, String time, int duration) throws Exception {
        return new JSONObject().put("title", title).put("time", time).put("durationMinutes", duration);
    }

    @Test public void peersOnlyIncludeActualOverlapsAndPreserveDisplayOrder() throws Exception {
        JSONArray visible = new JSONArray()
                .put(task("A", "09:00", 60))
                .put(task("B", "09:30", 60))
                .put(task("C", "09:45", 15))
                .put(task("D", "10:30", 30));
        assertEquals(Arrays.asList(1, 2), TaskTime.conflictingPeers(visible, 0));
        assertEquals(Arrays.asList(0, 2), TaskTime.conflictingPeers(visible, 1));
        assertEquals(Collections.emptyList(), TaskTime.conflictingPeers(visible, 3));
    }

    @Test public void sameTitleAndIdAreNotUsedAsIdentity() throws Exception {
        JSONArray visible = new JSONArray()
                .put(task("same", "08:00", 60).put("id", "same"))
                .put(task("same", "08:30", 60).put("id", "same"));
        assertEquals(Arrays.asList(1), TaskTime.conflictingPeers(visible, 0));
        assertEquals(Arrays.asList(0), TaskTime.conflictingPeers(visible, 1));
    }

    @Test public void malformedAndMissingEntriesAreIgnored() throws Exception {
        JSONArray visible = new JSONArray()
                .put(task("A", "09:00", 60))
                .put(JSONObject.NULL)
                .put("not-a-task")
                .put(task("invalid", "25:00", 60))
                .put(task("adjacent", "10:00", 30));
        assertTrue(TaskTime.conflictingPeers(visible, 0).isEmpty());
        assertTrue(TaskTime.conflictingPeers(visible, 1).isEmpty());
        assertTrue(TaskTime.conflictingPeers(visible, -1).isEmpty());
        assertTrue(TaskTime.conflictingPeers(visible, 999).isEmpty());
        assertTrue(TaskTime.conflictingPeers(null, 0).isEmpty());
    }

    @Test public void doesNotMutateJsonOrHistories() throws Exception {
        JSONObject a = task("A", "09:00", 60).put("history", new JSONObject().put("2026-11-22", 65));
        JSONObject b = task("B", "09:30", 60);
        JSONArray visible = new JSONArray().put(a).put(b);
        String before = visible.toString();
        assertEquals(Arrays.asList(1), TaskTime.conflictingPeers(visible, 0));
        assertEquals(before, visible.toString());
        assertEquals(65, a.getJSONObject("history").getInt("2026-11-22"));
    }

    @Test public void hiddenTasksDoNotLeakIntoVisiblePeerList() throws Exception {
        JSONObject a = task("Visible", "09:00", 60);
        JSONObject hidden = task("Hidden by priority filter", "09:30", 60);
        JSONArray all = new JSONArray().put(a).put(hidden);
        JSONArray filtered = new JSONArray().put(a);
        assertEquals(Arrays.asList(1), TaskTime.conflictingPeers(all, 0));
        assertTrue(TaskTime.conflictingPeers(filtered, 0).isEmpty());
    }
}
