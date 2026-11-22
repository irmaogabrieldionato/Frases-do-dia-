package com.orbis.meudia;

import org.junit.Test;
import static org.junit.Assert.*;

public class GuideTopicsTest {
    @Test public void allFifteenLanguagesHaveFiveTopics() {
        assertEquals(15, Languages.CODES.length);
        for (String code : Languages.CODES) {
            String[] topics = GuideTopics.headings(code);
            assertEquals(5, topics.length);
            for (String topic : topics) {
                assertNotNull(topic);
                assertTrue(topic.length() >= 5);
                assertFalse(topic.equals(code));
            }
        }
    }

    @Test public void translatedHeadingsDifferFromEnglish() {
        for (String code : Languages.CODES) {
            if (!"en".equals(code)) {
                assertNotEquals(GuideTopics.headings("en")[0], GuideTopics.headings(code)[0]);
            }
        }
    }

    @Test public void fallbackAndDefensiveCopy() {
        assertArrayEquals(GuideTopics.headings("pt-BR"), GuideTopics.headings("invalid"));
        String[] topics = GuideTopics.headings("en");
        topics[0] = "mutated";
        assertEquals("Calendar and tasks", GuideTopics.headings("en")[0]);
    }
    @Test public void allLanguagesHaveFiveUsefulInstructions() {
        for (String code : Languages.CODES) {
            String[] headings = GuideTopics.headings(code);
            String[] details = GuideTopics.details(code);
            assertEquals(headings.length, details.length);
            for (String detail : details) {
                assertNotNull(detail);
                assertTrue("Missing instructions for " + code, detail.length() >= 35);
            }
        }
    }

    @Test public void localizedInstructionsAndDefensiveCopy() {
        for (String code : Languages.CODES) {
            if (!"en".equals(code)) {
                assertNotEquals("Untranslated first instruction for " + code,
                        GuideTopics.details("en")[0], GuideTopics.details(code)[0]);
            }
        }
        assertArrayEquals(GuideTopics.details("pt-BR"), GuideTopics.details("unknown"));
        String[] details = GuideTopics.details("pt-BR");
        details[0] = "changed";
        assertNotEquals("changed", GuideTopics.details("pt-BR")[0]);
    }

}
