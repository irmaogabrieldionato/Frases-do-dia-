package com.orbis.meudia;

import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.*;

public class TaskTimeHistoryCodecTest {
    private TaskTimeHistory.Session session(String id, String date, TaskTimeHistory.Kind kind) {
        return new TaskTimeHistory.Session(id, "task:estudo\n📖", date, kind,
                120000, kind == TaskTimeHistory.Kind.TIMER ? 180000 : 0);
    }

    @Test public void roundTripPreservesDifferentDatesKindsAndUnicode() {
        List<TaskTimeHistory.Session> input = Arrays.asList(
                session("one", "2026-11-11", TaskTimeHistory.Kind.STOPWATCH),
                session("two", "2026-11-12", TaskTimeHistory.Kind.TIMER));
        List<TaskTimeHistory.Session> restored = TaskTimeHistoryCodec.decode(TaskTimeHistoryCodec.encode(input));
        assertEquals(2, restored.size());
        assertEquals(input.get(0).taskId, restored.get(0).taskId);
        assertEquals("2026-11-12", restored.get(1).occurrenceDate);
        assertEquals(TaskTimeHistory.Kind.TIMER, restored.get(1).kind);
        assertEquals(180000, restored.get(1).targetMillis);
        assertEquals(2, TaskTimeHistory.byDate(restored, input.get(0).taskId).size());
    }

    @Test public void emptyHistoryRoundTrips() {
        assertTrue(TaskTimeHistoryCodec.decode(
                TaskTimeHistoryCodec.encode(Collections.<TaskTimeHistory.Session>emptyList())).isEmpty());
    }

    @Test(expected = IllegalArgumentException.class) public void rejectsTruncation() {
        String encoded = TaskTimeHistoryCodec.encode(Collections.singletonList(
                session("a", "2026-11-11", TaskTimeHistory.Kind.TIMER)));
        TaskTimeHistoryCodec.decode(encoded.substring(0, encoded.length() - 1));
    }

    @Test(expected = IllegalArgumentException.class) public void rejectsTrailingData() {
        TaskTimeHistoryCodec.decode(TaskTimeHistoryCodec.encode(
                Collections.<TaskTimeHistory.Session>emptyList()) + "extra");
    }

    @Test(expected = IllegalArgumentException.class) public void rejectsUnknownVersion() {
        TaskTimeHistoryCodec.decode("ORBIS-TIME-2\n0\n");
    }

    @Test(expected = IllegalArgumentException.class) public void rejectsDuplicateIds() {
        TaskTimeHistoryCodec.encode(Arrays.asList(
                session("same", "2026-11-11", TaskTimeHistory.Kind.TIMER),
                session("same", "2026-11-12", TaskTimeHistory.Kind.STOPWATCH)));
    }

    @Test(expected = IllegalArgumentException.class) public void rejectsCorruptDuration() {
        String encoded = TaskTimeHistoryCodec.encode(Collections.singletonList(
                session("a", "2026-11-11", TaskTimeHistory.Kind.STOPWATCH)));
        TaskTimeHistoryCodec.decode(encoded.replace("6:120000", "6:-20000"));
    }
}
