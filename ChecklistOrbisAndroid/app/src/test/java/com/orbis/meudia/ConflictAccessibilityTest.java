package com.orbis.meudia;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

/** JVM regression tests for TalkBack labels; no Android runtime required. */
public class ConflictAccessibilityTest {
    @Test public void allFifteenLanguagesHaveNonemptyLabels() {
        assertEquals(15, Languages.CODES.length);
        for (String code : Languages.CODES) {
            String label = ConflictAccessibility.label(code, new JSONObject());
            assertNotNull(code, label);
            assertFalse(code, label.trim().isEmpty());
            assertFalse(code, label.contains("time_conflict_warning"));
        }
    }

    @Test public void validTimeIsAnnouncedButNotSymbolOnly() throws Exception {
        JSONObject task = new JSONObject().put("time", "09:30");
        String label = ConflictAccessibility.label("pt-BR", task);
        assertTrue(label.contains("Conflito de horário"));
        assertTrue(label.contains("09:30"));
        assertFalse(label.startsWith("⚠"));
    }

    @Test public void missingOrInvalidTimeDoesNotSpeakAnInvalidValue() throws Exception {
        JSONObject task = new JSONObject().put("time", "25:99");
        String invalid = ConflictAccessibility.label("en", task);
        String missing = ConflictAccessibility.label("en", new JSONObject());
        assertEquals(missing, invalid);
        assertFalse(invalid.contains("25:99"));
    }

    @Test public void arabicAndUrduAreNotEnglishFallback() {
        String english = ConflictAccessibility.label("en", null);
        assertNotEquals(english, ConflictAccessibility.label("ar", null));
        assertNotEquals(english, ConflictAccessibility.label("ur", null));
    }

    @Test public void noTaskMutation() throws Exception {
        JSONObject task = new JSONObject().put("time", "23:50").put("durationMinutes", 20);
        String before = task.toString();
        ConflictAccessibility.label("pt-BR", task);
        assertEquals(before, task.toString());
    }
}
