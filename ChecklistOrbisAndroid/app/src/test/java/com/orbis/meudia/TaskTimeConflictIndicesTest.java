package com.orbis.meudia;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.*;

public class TaskTimeConflictIndicesTest {
    private static JSONObject task(String time, int duration) throws Exception {
        return new JSONObject().put("time", time).put("durationMinutes", duration);
    }

    @Test public void reportsEachParticipantOnceInOriginalOrder() throws Exception {
        JSONArray tasks = new JSONArray()
            .put(task("09:00", 60))
            .put(task("09:30", 60))
            .put(task("11:00", 30))
            .put(task("09:45", 15));
        String original = tasks.toString();
        assertEquals(Arrays.asList(0, 1, 3), TaskTime.conflictingTaskIndices(tasks));
        assertEquals(3, TaskTime.countConflictingPairs(tasks));
        assertEquals(original, tasks.toString());
    }

    @Test public void excludesInvalidAndAdjacentIntervals() throws Exception {
        JSONArray tasks = new JSONArray().put(task("09:00", 60))
            .put(task("10:00", 30)).put(JSONObject.NULL)
            .put(task("25:00", 60)).put(task("09:30", 0));
        assertEquals(Collections.emptyList(), TaskTime.conflictingTaskIndices(tasks));
        assertEquals(Collections.emptyList(), TaskTime.conflictingTaskIndices(null));
    }

    @Test public void clipsAtMidnight() throws Exception {
        JSONArray tasks = new JSONArray().put(task("23:50", 30))
            .put(task("23:55", 5)).put(task("00:00", 10));
        assertEquals(Arrays.asList(0, 1), TaskTime.conflictingTaskIndices(tasks));
    }
}
