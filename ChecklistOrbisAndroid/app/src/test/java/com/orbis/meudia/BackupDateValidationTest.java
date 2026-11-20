package com.orbis.meudia;

import org.junit.Test;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

public class BackupDateValidationTest {
    private boolean valid(String value) throws Exception {
        Method method=MainActivity.class.getDeclaredMethod("validBackupDate", String.class);
        method.setAccessible(true);
        return (Boolean)method.invoke(null,value);
    }

    @Test public void acceptsRealDatesAndLeapDays() throws Exception {
        assertTrue(valid("2024-02-29"));
        assertTrue(valid("2026-11-20"));
        assertTrue(valid("2000-02-29"));
    }

    @Test public void checksGregorianCenturyAndBoundaryDates() throws Exception {
        assertTrue(valid("0001-01-01"));
        assertTrue(valid("9999-12-31"));
        assertTrue(valid("2400-02-29"));
        assertFalse(valid("2100-02-29"));
        assertFalse(valid("2026-11-31"));
        assertFalse(valid("2026-12-32"));
        assertFalse(valid("2026-01-01 "));
        assertFalse(valid(" 2026-01-01"));
    }

    @Test public void rejectsImpossibleOrMalformedDates() throws Exception {
        for(String date:new String[]{"2026-02-29","2026-02-31","2026-13-40",
                "1900-02-29","2026-04-31","0000-01-01","2026-00-10",
                "2026-01-00","2026-1-01","2026-01-01extra",""}) {
            assertFalse(date,valid(date));
        }
        assertFalse(valid(null));
    }
}
