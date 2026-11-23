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

    @Test public void totalAcrossDatesDoesNotMixTargetWithWorkedTime() {
        TaskTimeHistory.Total total = TaskTimeHistory.totalForTask(Arrays.asList(
                s("a", "study", "2026-11-11", TaskTimeHistory.Kind.STOPWATCH, 2700000, 0),
                s("b", "study", "2026-11-11", TaskTimeHistory.Kind.TIMER, 900000, 1800000),
                s("c", "study", "2026-11-12", TaskTimeHistory.Kind.STOPWATCH, 4200000, 0),
                s("d", "study", "2026-11-12", TaskTimeHistory.Kind.TIMER, 1200000, 1500000),
                s("e", "other", "2026-11-12", TaskTimeHistory.Kind.TIMER, 9999, 9999)
        ), "study");
        assertEquals(2, total.activeDays);
        assertEquals(6900000, total.stopwatchMillis);
        assertEquals(2100000, total.timerElapsedMillis);
        assertEquals(3300000, total.timerTargetMillis);
        assertEquals(2, total.stopwatchSessions);
        assertEquals(2, total.timerSessions);
    }

    @Test public void totalEmptyTaskHasZeroDaysAndDurations() {
        TaskTimeHistory.Total total = TaskTimeHistory.totalForTask(
                Collections.<TaskTimeHistory.Session>emptyList(), "task");
        assertEquals(0, total.activeDays);
        assertEquals(0, total.stopwatchMillis);
        assertEquals(0, total.timerElapsedMillis);
        assertEquals(0, total.timerTargetMillis);
    }

    @Test(expected = ArithmeticException.class)
    public void totalRejectsOverflowAcrossDays() {
        TaskTimeHistory.totalForTask(Arrays.asList(
                s("a", "task", "2026-11-11", TaskTimeHistory.Kind.STOPWATCH, Long.MAX_VALUE, 0),
                s("b", "task", "2026-11-12", TaskTimeHistory.Kind.STOPWATCH, 1, 0)
        ), "task");
    }
    @Test public void occurrenceFilterKeepsIndividualSessionsWithoutMixingDatesOrTasks() {
        TaskTimeHistory.Session first = s("a", "study", "2026-11-11",
                TaskTimeHistory.Kind.STOPWATCH, 2700000, 0);
        TaskTimeHistory.Session second = s("b", "study", "2026-11-11",
                TaskTimeHistory.Kind.TIMER, 1200000, 1800000);
        List<TaskTimeHistory.Session> result = TaskTimeHistory.forOccurrence(Arrays.asList(
                first, s("other-date", "study", "2026-11-12",
                        TaskTimeHistory.Kind.TIMER, 2000, 3000),
                second, s("other-task", "work", "2026-11-11",
                        TaskTimeHistory.Kind.STOPWATCH, 1000, 0)
        ), "study", "2026-11-11");
        assertEquals(2, result.size());
        assertSame(first, result.get(0));
        assertSame(second, result.get(1));
        assertEquals(1800000, result.get(1).targetMillis);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void occurrenceFilterResultIsImmutable() {
        TaskTimeHistory.forOccurrence(Collections.singletonList(
                s("a", "study", "2026-11-11", TaskTimeHistory.Kind.TIMER, 1, 2)),
                "study", "2026-11-11").clear();
    }

    @Test(expected = IllegalArgumentException.class)
    public void occurrenceFilterRejectsMissingDate() {
        TaskTimeHistory.forOccurrence(Collections.<TaskTimeHistory.Session>emptyList(),
                "study", " ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void occurrenceFilterRejectsImpossibleCalendarDate() {
        TaskTimeHistory.forOccurrence(Collections.<TaskTimeHistory.Session>emptyList(),
                "study", "2026-02-30");
    }

    @Test public void occurrenceFilterAcceptsValidLeapDay() {
        assertTrue(TaskTimeHistory.forOccurrence(Collections.<TaskTimeHistory.Session>emptyList(),
                "study", "2028-02-29").isEmpty());
    }

}
