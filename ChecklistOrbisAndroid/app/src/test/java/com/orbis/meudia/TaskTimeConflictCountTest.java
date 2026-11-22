package com.orbis.meudia;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class TaskTimeConflictCountTest {
    private JSONObject task(String time, int duration) throws Exception {
        return new JSONObject().put("time", time).put("durationMinutes", duration);
    }

    @Test public void countsEachPairExactlyOnce() throws Exception {
        JSONArray tasks = new JSONArray()
            .put(task("09:00", 60))
            .put(task("09:30", 60))
            .put(task("09:45", 15))
            .put(task("10:30", 30));
        assertEquals(3, TaskTime.countConflictingPairs(tasks));
    }

    @Test public void adjacentAndUnknownIntervalsAreIgnored() throws Exception {
        JSONArray tasks = new JSONArray()
            .put(task("09:00", 60))
            .put(task("10:00", 30))
            .put(task("25:00", 60))
            .put(new JSONObject())
            .put(JSONObject.NULL);
        assertEquals(0, TaskTime.countConflictingPairs(tasks));
        assertEquals(0, TaskTime.countConflictingPairs(null));
    }

    @Test public void doesNotModifySourceOrHistory() throws Exception {
        JSONArray tasks = new JSONArray()
            .put(new JSONObject("{\"time\":\"08:00\",\"durationMinutes\":120,\"history\":{\"2026-11-22\":80}}"))
            .put(task("09:00", 30));
        String before = tasks.toString();
        assertEquals(1, TaskTime.countConflictingPairs(tasks));
        assertEquals(before, tasks.toString());
    }
}
