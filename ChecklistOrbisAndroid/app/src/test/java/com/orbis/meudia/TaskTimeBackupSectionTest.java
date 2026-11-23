package com.orbis.meudia;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class TaskTimeBackupSectionTest {
    private TaskTimeHistory.Session session(String id, long elapsed) {
        return new TaskTimeHistory.Session(id, "task", "2026-11-23",
                TaskTimeHistory.Kind.STOPWATCH, elapsed, 0);
    }

    @Test public void roundTripAndIdempotentRestore() {
        String section = TaskTimeBackupSection.exportSection(Arrays.asList(session("one", 100)));
        List<TaskTimeHistory.Session> restored =
                TaskTimeBackupSection.prepareRestore(Collections.emptyList(), section);
        assertEquals(1, restored.size());
        assertEquals(100, restored.get(0).elapsedMillis);
        assertEquals(1, TaskTimeBackupSection.prepareRestore(restored, section).size());
    }

    @Test public void corruptedPayloadCannotChangeExistingData() {
        List<TaskTimeHistory.Session> original = Arrays.asList(session("existing", 20));
        String section = TaskTimeBackupSection.exportSection(Arrays.asList(session("new", 30)));
        String corrupted = section.substring(0, section.length() - 1) + "9";
        assertThrows(IllegalArgumentException.class,
                () -> TaskTimeBackupSection.prepareRestore(original, corrupted));
        assertEquals(1, original.size());
        assertEquals("existing", original.get(0).id);
    }

    @Test public void conflictingIdAbortsMerge() {
        List<TaskTimeHistory.Session> original = Arrays.asList(session("same", 20));
        String section = TaskTimeBackupSection.exportSection(Arrays.asList(session("same", 30)));
        assertThrows(IllegalArgumentException.class,
                () -> TaskTimeBackupSection.prepareRestore(original, section));
        assertEquals(20, original.get(0).elapsedMillis);
    }
    @Test public void emptyHistoryRoundTripsWithoutErasingExistingSessions() {
        String section = TaskTimeBackupSection.exportSection(Collections.emptyList());
        List<TaskTimeHistory.Session> existing = Arrays.asList(session("kept", 42));
        List<TaskTimeHistory.Session> restored =
                TaskTimeBackupSection.prepareRestore(existing, section);
        assertEquals(1, restored.size());
        assertEquals("kept", restored.get(0).id);
    }

    @Test public void malformedLengthAndChecksumMetadataAreRejected() {
        String section = TaskTimeBackupSection.exportSection(Arrays.asList(session("one", 100)));
        int first = section.indexOf('\n') + 1;
        int second = section.indexOf('\n', first);
        int third = section.indexOf('\n', second + 1);
        String badLength = section.substring(0, first) + "0001" + section.substring(second);
        String badChecksum = section.substring(0, second + 1) + "NOT_HEX" + section.substring(third);
        assertThrows(IllegalArgumentException.class,
                () -> TaskTimeBackupSection.prepareRestore(Collections.emptyList(), badLength));
        assertThrows(IllegalArgumentException.class,
                () -> TaskTimeBackupSection.prepareRestore(Collections.emptyList(), badChecksum));
    }

}
