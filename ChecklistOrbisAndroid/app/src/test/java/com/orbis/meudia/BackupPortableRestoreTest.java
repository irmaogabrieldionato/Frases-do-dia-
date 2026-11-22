package com.orbis.meudia;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

/** Version-1 JSON backups must never import another device's content URI grants. */
public class BackupPortableRestoreTest {
    @Test public void dropsUntrustedDeviceUriAndPreservesTaskHistory() throws Exception {
        JSONArray incoming = new JSONArray("[{\"title\":\"Reading\",\"description\":\"Notes\","
                + "\"history\":{\"2024-02-29\":75},"
                + "\"attachments\":[{\"uri\":\"content://other-device/private\","
                + "\"mime\":\"image/jpeg\",\"bytes\":123}]}]");
        String original = incoming.toString();
        JSONArray safe = TaskAttachments.portableTasks(incoming);
        JSONObject restored = safe.getJSONObject(0);
        assertFalse(restored.has("attachments"));
        assertEquals("Reading", restored.getString("title"));
        assertEquals("Notes", restored.getString("description"));
        assertEquals(75, restored.getJSONObject("history").getInt("2024-02-29"));
        assertEquals(original, incoming.toString());
    }

    @Test public void stripsAllAttachmentFieldsButPreservesMultipleTasks() throws Exception {
        JSONArray incoming = new JSONArray("[{\"title\":\"A\",\"attachments\":[]},"
                + "{\"title\":\"B\",\"attachments\":\"malformed\"},"
                + "{\"title\":\"C\",\"priority\":4}]");
        JSONArray safe = TaskAttachments.portableTasks(incoming);
        assertEquals(3, safe.length());
        assertFalse(safe.getJSONObject(0).has("attachments"));
        assertFalse(safe.getJSONObject(1).has("attachments"));
        assertEquals(4, safe.getJSONObject(2).getInt("priority"));
    }
}
