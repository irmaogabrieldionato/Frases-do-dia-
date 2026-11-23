package com.orbis.meudia;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.CRC32;

/**
 * Portable offline backup section for task time sessions.
 * This is a staging format: the Android task backup importer must integrate it
 * transactionally before it can be exposed as a full-app backup.
 */
public final class TaskTimeBackupSection {
    private static final String HEADER = "ORBIS-TIME-BACKUP-1\n";
    private static final int MAX_BYTES = 2000100;

    private TaskTimeBackupSection() {}

    public static String exportSection(List<TaskTimeHistory.Session> sessions) {
        String history = TaskTimeHistoryCodec.encode(sessions);
        byte[] bytes = history.getBytes(StandardCharsets.UTF_8);
        CRC32 crc = new CRC32();
        crc.update(bytes, 0, bytes.length);
        return HEADER + bytes.length + "\n" + Long.toHexString(crc.getValue()) + "\n" + history;
    }

    /** Fully validate before returning; never mutate existing sessions. */
    public static List<TaskTimeHistory.Session> prepareRestore(
            List<TaskTimeHistory.Session> existing, String section) {
        if (section == null || section.length() > MAX_BYTES || !section.startsWith(HEADER))
            throw new IllegalArgumentException("Invalid backup section");
        int start = HEADER.length();
        int lengthEnd = section.indexOf('\n', start);
        int crcEnd = lengthEnd < 0 ? -1 : section.indexOf('\n', lengthEnd + 1);
        if (crcEnd < 0) throw new IllegalArgumentException("Missing backup metadata");
        String lengthText = section.substring(start, lengthEnd);
        String crcText = section.substring(lengthEnd + 1, crcEnd);
        if (!lengthText.matches("0|[1-9][0-9]{0,6}") || !crcText.matches("[0-9a-f]{1,8}"))
            throw new IllegalArgumentException("Invalid backup metadata");
        int expectedLength = Integer.parseInt(lengthText);
        if (expectedLength > 2000000) throw new IllegalArgumentException("Backup too large");
        String history = section.substring(crcEnd + 1);
        byte[] bytes = history.getBytes(StandardCharsets.UTF_8);
        CRC32 crc = new CRC32();
        crc.update(bytes, 0, bytes.length);
        if (bytes.length != expectedLength || !Long.toHexString(crc.getValue()).equals(crcText))
            throw new IllegalArgumentException("Backup integrity check failed");
        return TaskTimeHistoryCodec.mergeForRestore(existing, history);
    }
}
