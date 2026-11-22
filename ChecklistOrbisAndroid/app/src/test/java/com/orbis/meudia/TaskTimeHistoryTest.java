package com.orbis.meudia;

import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.*;

public class TaskTimeHistoryTest {
    private TaskTimeHistory.Session s(String id, String task, String date,
                                      TaskTimeHistory.Kind kind, long elapsed, long target) {
        return new TaskTimeHistory.Session(id, task, date, kind, elapsed, target);
    }

    @Test public void sameTaskDifferentDatesRemainSeparate() {
        List<TaskTimeHistory.Day> days = TaskTimeHistory.byDate(Arrays.asList(
                s("a", "study", "2026-11-11", TaskTimeHistory.Kind.STOPWATCH, 2700000, 0),
                s("b", "study", "2026-11-11", TaskTimeHistory.Kind.TIMER, 1800000, 1800000),
                s("c", "study", "2026-11-12", TaskTimeHistory.Kind.STOPWATCH, 4200000, 0),
                s("d", "study", "2026-11-12", TaskTimeHistory.Kind.TIMER, 1500000, 1500000),
                s("e", "other", "2026-11-11", TaskTimeHistory.Kind.STOPWATCH, 999, 0)
        ), "study");
        assertEquals(2, days.size());
        assertEquals("2026-11-11", days.get(0).date);
        assertEquals(2700000, days.get(0).stopwatchMillis);
        assertEquals(1800000, days.get(0).timerElapsedMillis);
        assertEquals("2026-11-12", days.get(1).date);
        assertEquals(4200000, days.get(1).stopwatchMillis);
    }

    @Test public void multipleSessionsAggregateWithoutMixingTargetAndElapsed() {
        List<TaskTimeHistory.Day> days = TaskTimeHistory.byDate(Arrays.asList(
                s("a", "x", "2026-11-11", TaskTimeHistory.Kind.TIMER, 2000, 5000),
                s("b", "x", "2026-11-11", TaskTimeHistory.Kind.TIMER, 3000, 6000)
        ), "x");
        assertEquals(5000, days.get(0).timerElapsedMillis);
        assertEquals(11000, days.get(0).timerTargetMillis);
        assertEquals(2, days.get(0).timerSessions);
    }

    @Test public void emptyTaskHistoryIsEmpty() {
        assertTrue(TaskTimeHistory.byDate(Collections.<TaskTimeHistory.Session>emptyList(), "x").isEmpty());
    }

    @Test(expected = IllegalArgumentException.class) public void rejectImpossibleDate() {
        s("x", "task", "2026-02-30", TaskTimeHistory.Kind.TIMER, 1000, 1000);
    }

    @Test(expected = IllegalArgumentException.class) public void rejectNegativeTime() {
        s("x", "task", "2026-11-11", TaskTimeHistory.Kind.STOPWATCH, -1, 0);
    }

    @Test(expected = IllegalArgumentException.class) public void rejectDuplicateIds() {
        TaskTimeHistory.byDate(Arrays.asList(
                s("x", "task", "2026-11-11", TaskTimeHistory.Kind.TIMER, 100, 100),
                s("x", "task", "2026-11-12", TaskTimeHistory.Kind.TIMER, 100, 100)
        ), "task");
    }
}
