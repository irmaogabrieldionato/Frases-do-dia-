package com.orbis.meudia;

import org.junit.Test;
import java.util.Arrays;
import java.util.ArrayList;
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
    @Test public void restoreMergeKeepsBothDaysAndIsIdempotent() {
        TaskTimeHistory.Session first = session("a", "2026-11-11", TaskTimeHistory.Kind.STOPWATCH);
        TaskTimeHistory.Session second = session("b", "2026-11-12", TaskTimeHistory.Kind.TIMER);
        String payload = TaskTimeHistoryCodec.encode(Arrays.asList(first, second));
        List<TaskTimeHistory.Session> once = TaskTimeHistoryCodec.mergeForRestore(
                Collections.singletonList(first), payload);
        assertEquals(2, once.size());
        assertEquals(2, TaskTimeHistory.byDate(once, first.taskId).size());
        assertEquals(2, TaskTimeHistoryCodec.mergeForRestore(once, payload).size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void restoreRejectsConflictingIdsWithoutReplacingExisting() {
        TaskTimeHistoryCodec.mergeForRestore(
                Collections.singletonList(session("a", "2026-11-11", TaskTimeHistory.Kind.STOPWATCH)),
                TaskTimeHistoryCodec.encode(Collections.singletonList(
                        session("a", "2026-11-12", TaskTimeHistory.Kind.TIMER))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void restoreRejectsCorruptImport() {
        TaskTimeHistoryCodec.mergeForRestore(Collections.<TaskTimeHistory.Session>emptyList(),
                "ORBIS-TIME-1\\n1\\ncorrupt");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsUtf8PayloadLargerThanByteLimit() {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 400; i++) text.append('界');
        List<TaskTimeHistory.Session> sessions = new ArrayList<>();
        for (int i = 0; i < 1800; i++) {
            sessions.add(new TaskTimeHistory.Session("id-" + i,
                    text.toString(), "2026-11-11", TaskTimeHistory.Kind.STOPWATCH, 1000, 0));
        }
        TaskTimeHistoryCodec.encode(sessions);
    }

    @Test public void acceptsSmallUtf8Payload() {
        TaskTimeHistory.Session item = session("日本語", "2026-11-12", TaskTimeHistory.Kind.TIMER);
        assertEquals(item.taskId, TaskTimeHistoryCodec.decode(
                TaskTimeHistoryCodec.encode(Collections.singletonList(item))).get(0).taskId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsUnpairedHighSurrogateBeforePersisting() {
        TaskTimeHistoryCodec.encode(Collections.singletonList(
                session("broken\\uD83D", "2026-11-11", TaskTimeHistory.Kind.TIMER)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsUnpairedLowSurrogateBeforePersisting() {
        TaskTimeHistoryCodec.encode(Collections.singletonList(
                session("broken\\uDC00", "2026-11-11", TaskTimeHistory.Kind.STOPWATCH)));
    }

    @Test public void acceptsValidSurrogatePairWithoutLoss() {
        TaskTimeHistory.Session item = session("emoji-\\uD83D\\uDE80", "2026-11-11", TaskTimeHistory.Kind.TIMER);
        assertEquals(item.id, TaskTimeHistoryCodec.decode(TaskTimeHistoryCodec.encode(
                Collections.singletonList(item))).get(0).id);
    }

}
