package com.orbis.meudia;

import org.json.JSONArray;
import org.json.JSONObject;

/** Offline-only metadata for task attachments. No file upload or cloud dependency. */
final class TaskAttachments {
    static final int MAX_PER_TASK = 20;
    static final long MAX_FILE_BYTES = 1024L * 1024L * 1024L;

    static boolean supportedMime(String mime) {
        return mime != null && (mime.matches("image/[a-zA-Z0-9.+-]+")
                || mime.matches("video/[a-zA-Z0-9.+-]+"));
    }

    static boolean valid(JSONObject item) {
        if (item == null) return false;
        String uri = item.optString("uri", "");
        String mime = item.optString("mime", "");
        long size = item.optLong("bytes", -1);
        return uri.startsWith("content://") && uri.length() > "content://".length()
                && supportedMime(mime) && size >= 0 && size <= MAX_FILE_BYTES;
    }

    static boolean add(JSONObject task, JSONObject item) {
        if (task == null || !valid(item)) return false;
        JSONArray existing = task.optJSONArray("attachments");
        if (existing != null && existing.length() >= MAX_PER_TASK) return false;
        if (existing != null) for (int i = 0; i < existing.length(); i++) {
            JSONObject previous = existing.optJSONObject(i);
            if (previous != null && previous.optString("uri").equals(item.optString("uri"))) return false;
        }
        JSONArray attachments = existing == null ? new JSONArray() : existing;
        try {
            JSONObject copy = new JSONObject(item.toString());
            attachments.put(copy);
            task.put("attachments", attachments);
            return true;
        } catch (org.json.JSONException exception) {
            return false;
        }
    }

    /** Removes only the selected local attachment reference; never deletes the source media. */
    static boolean remove(JSONObject task, String uri) {
        if (task == null || uri == null || uri.isEmpty()) return false;
        JSONArray existing = task.optJSONArray("attachments");
        if (existing == null) return false;
        JSONArray kept = new JSONArray();
        boolean removed = false;
        for (int i = 0; i < existing.length(); i++) {
            JSONObject item = existing.optJSONObject(i);
            if (item != null && uri.equals(item.optString("uri", ""))) {
                removed = true;
            } else {
                kept.put(existing.opt(i));
            }
        }
        if (!removed) return false;
        try {
            task.put("attachments", kept);
            return true;
        } catch (org.json.JSONException exception) {
            return false;
        }
    }

    /**
     * Produces a portable backup copy. Android content URIs are device-specific
     * access grants, not media bytes; exporting them would create broken links
     * and potentially expose local document-provider identifiers.
     */
    static JSONArray portableTasks(JSONArray source) throws org.json.JSONException {
        JSONArray copy = new JSONArray(source == null ? "[]" : source.toString());
        for (int i = 0; i < copy.length(); i++) {
            JSONObject task = copy.optJSONObject(i);
            if (task != null) task.remove("attachments");
        }
        return copy;
    }

    /** Number of device-only references excluded from a portable JSON backup. */
    static int omittedAttachmentCount(JSONArray tasks) {
        if (tasks == null) return 0;
        int count = 0;
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject task = tasks.optJSONObject(i);
            JSONArray items = task == null ? null : task.optJSONArray("attachments");
            if (items != null) count += items.length();
        }
        return count;
    }

    static long totalBytes(JSONObject task) {
        JSONArray items = task == null ? null : task.optJSONArray("attachments");
        if (items == null) return 0;
        long sum = 0;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (valid(item)) {
                long bytes = item.optLong("bytes");
                if (Long.MAX_VALUE - sum < bytes) return Long.MAX_VALUE;
                sum += bytes;
            }
        }
        return sum;
    }
}
