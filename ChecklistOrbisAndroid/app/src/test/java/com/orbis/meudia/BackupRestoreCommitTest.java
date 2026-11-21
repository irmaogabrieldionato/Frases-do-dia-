package com.orbis.meudia;

import org.junit.Test;
import static org.junit.Assert.*;

public class BackupRestoreCommitTest {
    @Test public void failedWriteDoesNotPublishRestoredState() {
        final String[] active = {"old"};
        final int[] alarms = {0};
        boolean ok = BackupRestoreCommit.restore((day, json) -> false,
                "2026-11-21", "[{\"title\":\"new\"}]", () -> {
                    active[0] = "new";
                    alarms[0]++;
                });
        assertFalse(ok);
        assertEquals("old", active[0]);
        assertEquals(0, alarms[0]);
    }

    @Test public void successfulWritePublishesOnlyAfterCommit() {
        final String[] active = {"old"};
        final int[] alarms = {0};
        boolean ok = BackupRestoreCommit.restore((day, json) -> {
                    assertEquals("old", active[0]);
                    assertEquals(0, alarms[0]);
                    assertEquals("2026-11-21", day);
                    assertEquals("[]", json);
                    return true;
                }, "2026-11-21", "[]", () -> {
                    active[0] = "new";
                    alarms[0]++;
                });
        assertTrue(ok);
        assertEquals("new", active[0]);
        assertEquals(1, alarms[0]);
    }

    @Test public void thrownWriteDoesNotPublishRestoredState() {
        final int[] callbacks = {0};
        try {
            BackupRestoreCommit.restore((day, json) -> {
                throw new IllegalStateException("simulated storage error");
            }, "2026-11-21", "[]", () -> callbacks[0]++);
            fail("Expected simulated storage error");
        } catch (IllegalStateException expected) {
            assertEquals("simulated storage error", expected.getMessage());
        }
        assertEquals(0, callbacks[0]);
    }

    @Test public void invalidArgumentsNeverInvokeWriterOrCallbacks() {
        final int[] writes = {0};
        final int[] callbacks = {0};
        BackupRestoreCommit.Writer writer = (day, json) -> {
            writes[0]++;
            return true;
        };
        BackupRestoreCommit.AfterCommit callback = () -> callbacks[0]++;
        try {
            BackupRestoreCommit.restore(writer, null, "[]", callback);
            fail("Expected missing day rejection");
        } catch (IllegalArgumentException expected) {
            assertEquals("Missing restore argument", expected.getMessage());
        }
        try {
            BackupRestoreCommit.restore(writer, "2026-11-21", null, callback);
            fail("Expected missing JSON rejection");
        } catch (IllegalArgumentException expected) {
            assertEquals("Missing restore argument", expected.getMessage());
        }
        assertEquals(0, writes[0]);
        assertEquals(0, callbacks[0]);
    }
}
