package com.orbis.meudia;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class DailySuggestionsTest {
    @Test public void suggestsOnlyPendingOccurrencesWithoutMutatingHistory() throws Exception {
        JSONObject pending = new JSONObject().put("title","A").put("start","2026-11-01");
        JSONObject finished = new JSONObject().put("title","B").put("start","2026-11-01")
                .put("history",new JSONObject().put("2026-11-21",100));
        JSONObject future = new JSONObject().put("title","C").put("start","2026-11-22");
        JSONArray all = new JSONArray().put(pending).put(finished).put(future);
        String before = all.toString();
        assertEquals(1, DailySuggestions.pending(all,"2026-11-21").size());
        assertSame(pending, DailySuggestions.pending(all,"2026-11-21").get(0));
        assertEquals(before, all.toString());
    }
    @Test public void invalidInputIsEmpty() {
        assertTrue(DailySuggestions.pending(null,"2026-11-21").isEmpty());
        assertTrue(DailySuggestions.pending(new JSONArray(),"invalid").isEmpty());
    }
}
