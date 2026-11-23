package com.orbis.meudia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import java.util.Arrays;
import org.junit.Test;

public class TaskTimeWorkedMillisTest {
    @Test public void actualWorkExcludesTimerTargetOnDayAndTotal() {
        TaskTimeHistory.Session timer = new TaskTimeHistory.Session("timer", "task", "2026-11-23",
                TaskTimeHistory.Kind.TIMER, 2000, 10000);
        TaskTimeHistory.Session watch = new TaskTimeHistory.Session("watch", "task", "2026-11-23",
                TaskTimeHistory.Kind.STOPWATCH, 3000, 0);
        assertEquals(5000, TaskTimeHistory.byDate(Arrays.asList(timer, watch), "task").get(0).workedMillis());
        assertEquals(5000, TaskTimeHistory.totalForTask(Arrays.asList(timer, watch), "task").workedMillis());
        assertEquals(5000, TaskTimeHistory.totalBetweenDates(Arrays.asList(timer, watch), "task",
                "2026-11-23", "2026-11-23").workedMillis());
    }

    @Test public void workedTimeOverflowIsRejectedRatherThanWrapped() {
        TaskTimeHistory.Session timer = new TaskTimeHistory.Session("timer", "task", "2026-11-23",
                TaskTimeHistory.Kind.TIMER, Long.MAX_VALUE, 1);
        TaskTimeHistory.Session watch = new TaskTimeHistory.Session("watch", "task", "2026-11-23",
                TaskTimeHistory.Kind.STOPWATCH, 1, 0);
        TaskTimeHistory.Day day = TaskTimeHistory.byDate(Arrays.asList(timer, watch), "task").get(0);
        assertThrows(ArithmeticException.class, day::workedMillis);
        TaskTimeHistory.Total total = TaskTimeHistory.totalForTask(Arrays.asList(timer, watch), "task");
        assertThrows(ArithmeticException.class, total::workedMillis);
    }
}
