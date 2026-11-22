package com.orbis.meudia;

import org.junit.Test;
import static org.junit.Assert.*;

public class GuideTranslationsTest {
    @Test public void allFifteenLanguagesHaveLocalizedHelp() {
        assertEquals(15, Languages.CODES.length);
        for (String code : Languages.CODES) {
            assertNotNull(GuideTranslations.title(code));
            assertTrue(GuideTranslations.title(code).length() > 3);
            assertTrue(GuideTranslations.overview(code).length() > 100);
            assertTrue(GuideTranslations.overview(code).contains("⚠"));
            assertNotEquals(code, GuideTranslations.title(code));
            assertFalse(GuideTranslations.overview(code).equals(GuideTranslations.title(code)));
        }
    }

    @Test public void differentLanguagesHaveDifferentHelp() {
        assertNotEquals(GuideTranslations.overview("en"), GuideTranslations.overview("pt-BR"));
        assertNotEquals(GuideTranslations.overview("ar"), GuideTranslations.overview("ur"));
        assertNotEquals(GuideTranslations.title("ja"), GuideTranslations.title("zh-CN"));
    }

    @Test public void unknownLanguageUsesPortugueseFallback() {
        assertEquals(GuideTranslations.overview("pt-BR"), GuideTranslations.overview("unknown"));
    }

    @Test public void noGuideClaimsSyncOrMediaBackup() {
        for (String code : Languages.CODES) {
            String guide = GuideTranslations.overview(code);
            assertFalse(guide.contains("service_role"));
            assertFalse(guide.contains("https://"));
        }
    }
}
