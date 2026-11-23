package com.orbis.meudia;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Versioned, bounded, pure-Java codec for local task time history.
 * No network, permissions or Android framework dependencies.
 * Format: ORBIS-TIME-1\\n followed by length-prefixed fields.
 * The caller is responsible for atomic persistence and backup integration.
 */
public final class TaskTimeHistoryCodec {
    private static final String HEADER = "ORBIS-TIME-1\n";
    private static final int MAX_SESSIONS = 10000;
    private static final int MAX_TEXT = 512;
    private static final int MAX_BYTES = 2000000;

    private TaskTimeHistoryCodec() {}

    public static String encode(List<TaskTimeHistory.Session> sessions) {
        if (sessions == null || sessions.size() > MAX_SESSIONS)
            throw new IllegalArgumentException("Invalid session count");
        StringBuilder out = new StringBuilder(HEADER);
        out.append(sessions.size()).append('\n');
        HashSet<String> ids = new HashSet<>();
        for (TaskTimeHistory.Session session : sessions) {
            if (session == null || !ids.add(session.id))
                throw new IllegalArgumentException("Null or duplicate session");
            field(out, session.id);
            field(out, session.taskId);
            field(out, session.occurrenceDate);
            field(out, session.kind.name());
            field(out, Long.toString(session.elapsedMillis));
            field(out, Long.toString(session.targetMillis));
            if (out.length() > MAX_BYTES) throw new IllegalArgumentException("History too large");
        }
        String encoded = out.toString();
        if (encoded.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES)
            throw new IllegalArgumentException("History too large in UTF-8");
        return encoded;
    }

    /**
     * Prepare a non-destructive import from a versioned history payload.
     * Identical session IDs are idempotent; conflicting IDs abort the entire merge.
     * Caller must persist the result only after this method returns successfully.
     */
    public static List<TaskTimeHistory.Session> mergeForRestore(
            List<TaskTimeHistory.Session> existing, String incomingEncoded) {
        // Validate existing data before processing an import; never overwrite corruption.
        decode(encode(existing));
        List<TaskTimeHistory.Session> incoming = decode(incomingEncoded);
        java.util.LinkedHashMap<String, TaskTimeHistory.Session> merged =
                new java.util.LinkedHashMap<>();
        for (TaskTimeHistory.Session session : existing) merged.put(session.id, session);
        for (TaskTimeHistory.Session session : incoming) {
            TaskTimeHistory.Session old = merged.get(session.id);
            if (old != null) {
                if (!old.taskId.equals(session.taskId)
                        || !old.occurrenceDate.equals(session.occurrenceDate)
                        || old.kind != session.kind
                        || old.elapsedMillis != session.elapsedMillis
                        || old.targetMillis != session.targetMillis)
                    throw new IllegalArgumentException("Conflicting time session id");
            } else {
                merged.put(session.id, session);
            }
        }
        List<TaskTimeHistory.Session> result = new ArrayList<>(merged.values());
        // Enforce the same count and payload limits before persistence.
        encode(result);
        return Collections.unmodifiableList(result);
    }

    private static void field(StringBuilder out, String value) {
        if (value == null || value.length() > MAX_TEXT)
            throw new IllegalArgumentException("Invalid field length");
        out.append(value.length()).append(':').append(value);
    }

    public static List<TaskTimeHistory.Session> decode(String encoded) {
        if (encoded == null || encoded.length() > MAX_BYTES
                || encoded.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES
                || !encoded.startsWith(HEADER))
            throw new IllegalArgumentException("Invalid history header");
        int newline = encoded.indexOf('\n', HEADER.length());
        if (newline < 0) throw new IllegalArgumentException("Missing count");
        String countText = encoded.substring(HEADER.length(), newline);
        if (!countText.matches("0|[1-9][0-9]{0,4}"))
            throw new IllegalArgumentException("Invalid count");
        int count = Integer.parseInt(countText);
        if (count > MAX_SESSIONS) throw new IllegalArgumentException("Too many sessions");
        int[] cursor = {newline + 1};
        ArrayList<TaskTimeHistory.Session> sessions = new ArrayList<>();
        HashSet<String> ids = new HashSet<>();
        for (int i = 0; i < count; i++) {
            String id = read(encoded, cursor);
            String taskId = read(encoded, cursor);
            String date = read(encoded, cursor);
            String kindText = read(encoded, cursor);
            String elapsedText = read(encoded, cursor);
            String targetText = read(encoded, cursor);
            try {
                if (!elapsedText.matches("0|[1-9][0-9]*")
                        || !targetText.matches("0|[1-9][0-9]*"))
                    throw new IllegalArgumentException("Invalid duration");
                TaskTimeHistory.Kind kind = TaskTimeHistory.Kind.valueOf(kindText);
                TaskTimeHistory.Session session = new TaskTimeHistory.Session(id, taskId, date,
                        kind, Long.parseLong(elapsedText), Long.parseLong(targetText));
                if (!ids.add(id)) throw new IllegalArgumentException("Duplicate id");
                sessions.add(session);
            } catch (RuntimeException error) {
                throw new IllegalArgumentException("Invalid history session", error);
            }
        }
        if (cursor[0] != encoded.length())
            throw new IllegalArgumentException("Trailing history content");
        return Collections.unmodifiableList(sessions);
    }

    private static String read(String input, int[] cursor) {
        int start = cursor[0];
        int colon = input.indexOf(':', start);
        if (colon < 0 || colon - start > 3 || colon == start)
            throw new IllegalArgumentException("Malformed field");
        String lengthText = input.substring(start, colon);
        if (!lengthText.matches("0|[1-9][0-9]{0,2}"))
            throw new IllegalArgumentException("Malformed length");
        int length = Integer.parseInt(lengthText);
        if (length > MAX_TEXT || colon + 1 + length > input.length())
            throw new IllegalArgumentException("Truncated field");
        cursor[0] = colon + 1 + length;
        return input.substring(colon + 1, cursor[0]);
    }
}
