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
}
