package com.orbis.meudia;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

/** JVM regressions for the optional, offline deadline editor data contract. */
public class DeadlineEditorContractTest {
    @Test public void deadlineLabelsExistForAllLanguages() {
        assertEquals(15, Languages.CODES.length);
        for (String language : Languages.CODES) {
            for (String key : new String[]{"deadline", "no_deadline", "clear_deadline"}) {
                String translated = Languages.text(language, key);
                assertNotNull(translated);
                assertFalse(translated.trim().isEmpty());
                assertNotEquals(key, translated);
            }
        }
    }

    @Test public void deadlineSurvivesJsonBackupWithoutChangingHistoricalProgress() throws Exception {
        JSONObject old = new JSONObject("{\"id\":\"original\",\"start\":\"2026-11-17\",\"history\":{\"2026-11-16\":45}}");
        assertEquals("", TaskDeadline.get(old));
        assertTrue(TaskDeadline.set(old, "2026-12-31"));
        JSONObject restored = new JSONObject(old.toString());
        assertEquals("2026-12-31", TaskDeadline.get(restored));
        assertEquals("2026-11-17", restored.getString("start"));
        assertEquals(45, restored.getJSONObject("history").getInt("2026-11-16"));
        assertEquals("original", restored.getString("id"));
        assertTrue(TaskDeadline.clear(restored));
        assertEquals("", TaskDeadline.get(restored));
        assertEquals(45, restored.getJSONObject("history").getInt("2026-11-16"));
    }
}
