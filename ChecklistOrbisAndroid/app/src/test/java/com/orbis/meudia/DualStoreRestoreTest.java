package com.orbis.meudia;

import org.junit.Test;
import static org.junit.Assert.*;

public class DualStoreRestoreTest {
    private static class MemoryStore implements DualStoreRestore.Store {
        String value;
        boolean rejectNext;
        MemoryStore(String value) { this.value = value; }
        public String snapshot() { return value; }
        public boolean write(String next) {
            if (rejectNext) { rejectNext = false; return false; }
            value = next;
            return true;
        }
    }

    @Test public void bothSnapshotsCommit() {
        MemoryStore tasks = new MemoryStore("old-tasks");
        MemoryStore time = new MemoryStore("old-time");
        assertTrue(DualStoreRestore.restore(tasks, "new-tasks", time, "new-time"));
        assertEquals("new-tasks", tasks.value);
        assertEquals("new-time", time.value);
    }

    @Test public void secondFailureRestoresFirst() {
        MemoryStore tasks = new MemoryStore("old-tasks");
        MemoryStore time = new MemoryStore("old-time");
        time.rejectNext = true;
        assertFalse(DualStoreRestore.restore(tasks, "new-tasks", time, "new-time"));
        assertEquals("old-tasks", tasks.value);
        assertEquals("old-time", time.value);
    }

    @Test public void firstFailureLeavesBothUntouched() {
        MemoryStore tasks = new MemoryStore("old-tasks");
        MemoryStore time = new MemoryStore("old-time");
        tasks.rejectNext = true;
        assertFalse(DualStoreRestore.restore(tasks, "new-tasks", time, "new-time"));
        assertEquals("old-tasks", tasks.value);
        assertEquals("old-time", time.value);
    }
    @Test public void secondStorePartialFalseRollsBackBoth() {
        MemoryStore tasks = new MemoryStore("before-tasks");
        MemoryStore history = new MemoryStore("before-history") {
            @Override public boolean write(String next) {
                value = next;
                return !next.equals("after-history");
            }
        };
        assertFalse(DualStoreRestore.restore(tasks, "after-tasks", history, "after-history"));
        assertEquals("before-tasks", tasks.value);
        assertEquals("before-history", history.value);
    }

    @Test public void firstStorePartialFalseRollsBackFirst() {
        MemoryStore tasks = new MemoryStore("before-tasks") {
            @Override public boolean write(String next) {
                value = next;
                return !next.equals("after-tasks");
            }
        };
        MemoryStore history = new MemoryStore("before-history");
        assertFalse(DualStoreRestore.restore(tasks, "after-tasks", history, "after-history"));
        assertEquals("before-tasks", tasks.value);
        assertEquals("before-history", history.value);
    }

    @Test public void secondStoreThrowAfterMutationRollsBackBothAndRethrows() {
        MemoryStore tasks = new MemoryStore("before-tasks");
        MemoryStore history = new MemoryStore("before-history") {
            @Override public boolean write(String next) {
                value = next;
                if (next.equals("after-history")) throw new IllegalArgumentException("disk failure");
                return true;
            }
        };
        try {
            DualStoreRestore.restore(tasks, "after-tasks", history, "after-history");
            fail("Expected write exception");
        } catch (IllegalArgumentException expected) {
            assertEquals("disk failure", expected.getMessage());
        }
        assertEquals("before-tasks", tasks.value);
        assertEquals("before-history", history.value);
    }

    @Test public void failedCompensationIsReportedNotSilentlyIgnored() {
        MemoryStore tasks = new MemoryStore("before-tasks") {
            @Override public boolean write(String next) {
                if (next.equals("before-tasks")) return false;
                value = next;
                return true;
            }
        };
        MemoryStore history = new MemoryStore("before-history");
        history.rejectNext = true;
        try {
            DualStoreRestore.restore(tasks, "after-tasks", history, "after-history");
            fail("Expected recovery error");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("rollback incomplete"));
        }
    }

}
