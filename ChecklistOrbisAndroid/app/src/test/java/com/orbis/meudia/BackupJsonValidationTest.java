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


    @Test public void rejectsEntireBackupWhenLaterTaskIsCorrupt() throws Exception {
        String first="{\"title\":\"Valid\",\"mode\":\"daily\",\"start\":\"2024-02-29\",\"history\":{\"2024-02-29\":75}}";
        String invalidSecond="{\"title\":\"Invalid\",\"mode\":\"daily\",\"start\":\"2026-02-30\"}";
        String root="{\"format\":\"checklist-orbis-backup\",\"version\":1,\"tasks\":["+first+","+invalidSecond+"]}";
        assertInvalid(root);
        assertEquals(1,validate("{\"format\":\"checklist-orbis-backup\",\"version\":1,\"tasks\":["+first+"]}").length());
    }

    @Test public void rejectsInvalidTopLevelAndTaskShapes() throws Exception {
        for(String json:new String[]{
            "[]",
            "{}",
            "{\"format\":\"checklist-orbis-backup\",\"version\":1,\"tasks\":{}}",
            "{\"format\":\"checklist-orbis-backup\",\"version\":1,\"tasks\":[null]}",
            "{\"format\":\"checklist-orbis-backup\",\"version\":1,\"tasks\":[42]}",
            "{\"format\":\"checklist-orbis-backup\",\"version\":1,\"tasks\":[{\"title\":\"x\",\"mode\":\"never\",\"start\":\"2024-01-01\"}]}"
        }) assertInvalid(json);
    }

    @Test public void preservesLegacyOptionalFieldsDuringValidation() throws Exception {
        String json="{\"format\":\"checklist-orbis-backup\",\"version\":1,\"tasks\":["
            +"{\"title\":\"Legacy\",\"mode\":\"weekly\",\"start\":\"2024-01-01\","
            +"\"description\":\"Keep this\",\"priority\":5,\"progress\":30}]}";
        JSONArray result=validate(json);
        assertEquals("Keep this",result.getJSONObject(0).getString("description"));
        assertEquals(5,result.getJSONObject(0).getInt("priority"));
        assertEquals(30,result.getJSONObject(0).getInt("progress"));
    }


    @Test public void retainsMultipleTasksAndExtendedFieldsAcrossValidation() throws Exception {
        String json="{\\\"format\\\":\\\"checklist-orbis-backup\\\",\\\"version\\\":1,\\\"tasks\\\":["
            +"{\\\"title\\\":\\\"Original\\\",\\\"mode\\\":\\\"daily\\\",\\\"start\\\":\\\"2024-02-29\\\","
            +"\\\"project\\\":\\\"Study\\\",\\\"section\\\":\\\"Reading\\\",\\\"tags\\\":[\\\"books\\\",\\\"home\\\"],"
            +"\\\"history\\\":{\\\"2024-02-29\\\":35}},"
            +"{\\\"title\\\":\\\"Legacy\\\",\\\"mode\\\":\\\"once\\\",\\\"start\\\":\\\"2020-01-01\\\"}]}";
        JSONArray result=validate(json);
        assertEquals(2,result.length());
        assertEquals("Study",result.getJSONObject(0).getString("project"));
        assertEquals("Reading",result.getJSONObject(0).getString("section"));
        assertEquals(2,result.getJSONObject(0).getJSONArray("tags").length());
        assertEquals(35,result.getJSONObject(0).getJSONObject("history").getInt("2024-02-29"));
        assertFalse(result.getJSONObject(1).has("history"));
    }

    private void assertInvalid(String json) throws Exception {
        try {validate(json);fail("Expected invalid backup: "+json);}
        catch(JSONException expected) { /* validation rejects before restoration */ }
    }
}
