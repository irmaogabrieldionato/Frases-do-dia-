package com.orbis.meudia;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class TaskAttachmentsTest {
    private JSONObject item(String uri, String mime, long bytes) throws Exception {
        return new JSONObject().put("uri", uri).put("mime", mime).put("bytes", bytes);
    }
    @Test public void acceptsLocalMediaAndCountsBytes() throws Exception {
        JSONObject task = new JSONObject().put("title", "Work");
        assertTrue(TaskAttachments.add(task, item("content://media/1", "image/jpeg", 120)));
        assertTrue(TaskAttachments.add(task, item("content://media/2", "video/mp4", 300)));
        assertEquals(420, TaskAttachments.totalBytes(task));
        assertEquals("Work", task.getString("title"));
    }
    @Test public void rejectsUnsafeOrInvalidInputsWithoutMutation() throws Exception {
        JSONObject task = new JSONObject();
        assertFalse(TaskAttachments.add(task, item("file:///tmp/a", "image/jpeg", 1)));
        assertFalse(TaskAttachments.add(task, item("content://x", "application/pdf", 1)));
        assertFalse(TaskAttachments.add(task, item("content://x", "video/mp4", -1)));
        assertFalse(TaskAttachments.add(task, item("content://x", "video/mp4", TaskAttachments.MAX_FILE_BYTES + 1)));
        assertFalse(task.has("attachments"));
    }
    @Test public void duplicateAndLimitAreEnforced() throws Exception {
        JSONObject task = new JSONObject();
        for (int i=0; i<TaskAttachments.MAX_PER_TASK; i++)
            assertTrue(TaskAttachments.add(task, item("content://media/" + i, "image/png", i)));
        assertFalse(TaskAttachments.add(task, item("content://media/0", "image/png", 0)));
        assertFalse(TaskAttachments.add(task, item("content://media/extra", "image/png", 0)));
        assertEquals(TaskAttachments.MAX_PER_TASK, task.getJSONArray("attachments").length());
    }
    @Test public void removingAttachmentKeepsOtherDataAndDoesNotTouchSource() throws Exception {
        JSONObject task = new JSONObject().put("title", "Work");
        assertTrue(TaskAttachments.add(task, item("content://media/1", "image/jpeg", 120)));
        assertTrue(TaskAttachments.add(task, item("content://media/2", "video/mp4", 300)));
        assertFalse(TaskAttachments.remove(task, "content://media/missing"));
        assertEquals(420, TaskAttachments.totalBytes(task));
        assertTrue(TaskAttachments.remove(task, "content://media/1"));
        assertFalse(TaskAttachments.remove(task, "content://media/1"));
        assertEquals(300, TaskAttachments.totalBytes(task));
        assertEquals("content://media/2", task.getJSONArray("attachments").getJSONObject(0).getString("uri"));
        assertEquals("Work", task.getString("title"));
    }
    @Test public void portableBackupOmitsDeviceOnlyUrisWithoutMutatingOriginal() throws Exception {
        JSONObject task = new JSONObject().put("title", "Task")
                .put("history", new JSONObject().put("2026-11-21", 55));
        assertTrue(TaskAttachments.add(task, item("content://media/42", "image/jpeg", 100)));
        org.json.JSONArray original = new org.json.JSONArray().put(task);
        org.json.JSONArray portable = TaskAttachments.portableTasks(original);
        JSONObject copy = portable.getJSONObject(0);
        assertFalse(copy.has("attachments"));
        assertEquals("Task", copy.getString("title"));
        assertEquals(55, copy.getJSONObject("history").getInt("2026-11-21"));
        assertEquals(1, original.getJSONObject(0).getJSONArray("attachments").length());
        assertEquals(0, TaskAttachments.portableTasks(null).length());
    }

    @Test public void countsOmittedMediaReferencesWithoutChangingTasks() throws Exception {
        JSONObject one = new JSONObject().put("title", "one");
        JSONObject two = new JSONObject().put("title", "two");
        assertTrue(TaskAttachments.add(one, item("content://media/1", "image/png", 100)));
        assertTrue(TaskAttachments.add(one, item("content://media/2", "video/mp4", 200)));
        org.json.JSONArray tasks = new org.json.JSONArray().put(one).put(two).put("legacy");
        assertEquals(2, TaskAttachments.omittedAttachmentCount(tasks));
        assertEquals(0, TaskAttachments.omittedAttachmentCount(null));
        assertEquals(2, one.getJSONArray("attachments").length());
        assertEquals(0, TaskAttachments.omittedAttachmentCount(TaskAttachments.portableTasks(tasks)));
    }
}
