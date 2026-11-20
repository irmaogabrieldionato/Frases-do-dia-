package com.orbis.meudia;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

public class BackupJsonValidationTest {
    private JSONArray validate(String json) throws Exception {
        Method method=MainActivity.class.getDeclaredMethod("validateBackup", String.class);
        method.setAccessible(true);
        try {
            return (JSONArray)method.invoke(null,json);
        } catch(InvocationTargetException e) {
            if(e.getCause() instanceof JSONException)throw (JSONException)e.getCause();
            throw e;
        }
    }

    private String backup(String version,String title,String history) {
        return "{\"format\":\"checklist-orbis-backup\",\"version\":"+version+
            ",\"tasks\":[{\"title\":"+title+
            ",\"mode\":\"daily\",\"start\":\"2024-02-29\""+
            (history==null?"":",\"history\":"+history)+"}]}";
    }

    @Test public void acceptsLegacyTaskAndValidHistory() throws Exception {
        assertEquals(1,validate(backup("1","\"Read\"","{\"2024-02-29\":50,\"2024-03-01\":100}")).length());
        assertEquals(1,validate(backup("1","\"Read\"",null)).length());
    }

    @Test public void rejectsInvalidVersionTypesAndNullTitle() throws Exception {
        for(String json:new String[]{
            backup("1.5","\"Read\"",null),
            backup("\"1\"","\"Read\"",null),
            backup("true","\"Read\"",null),
            backup("null","\"Read\"",null),
            backup("1","null",null),
            backup("1","42",null)
        }) assertInvalid(json);
    }

    @Test public void rejectsCorruptHistoryWithoutReplacingData() throws Exception {
        for(String history:new String[]{
            "{\"2026-02-31\":50}",
            "{\"2024-02-29\":101}",
            "{\"2024-02-29\":-1}",
            "{\"2024-02-29\":10.5}",
            "{\"2024-02-29\":null}",
            "[]"
        }) assertInvalid(backup("1","\"Read\"",history));
        assertInvalid("{broken json");
    }

    private void assertInvalid(String json) throws Exception {
        try {validate(json);fail("Expected invalid backup: "+json);}
        catch(JSONException expected) { /* validation rejects before restoration */ }
    }
}
