package com.orbis.meudia;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

/** Restores local one-time alarms after reboot or a device time-zone change. */
public final class LocalReminderRecoveryReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;
        String action = intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action)
                && !Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                && !Intent.ACTION_TIME_CHANGED.equals(action)) return;
        SharedPreferences prefs = context.getSharedPreferences("meu-dia-dados-v1", Context.MODE_PRIVATE);
        JSONArray tasks;
        try { tasks = new JSONArray(prefs.getString("tasks", "[]")); }
        catch (Exception ignored) { return; }
        long now = System.currentTimeMillis();
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject task = tasks.optJSONObject(i);
            if (task == null || !"once".equals(task.optString("mode"))) continue;
            String id = task.optString("id");
            if (id.isEmpty()) continue;
            // Recompute against the device's current local time zone.
            LocalReminderScheduler.cancel(context, id);
            LocalReminderScheduler.schedule(context, task, task.optString("start"), id, now);
        }
    }
}
