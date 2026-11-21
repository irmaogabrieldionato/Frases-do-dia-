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
}
