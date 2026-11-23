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
}
