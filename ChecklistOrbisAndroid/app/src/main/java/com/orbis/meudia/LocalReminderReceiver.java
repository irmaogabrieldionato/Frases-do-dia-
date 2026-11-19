package com.orbis.meudia;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import android.os.Build;

/**
 * Offline notification delivery endpoint. Scheduling and runtime permission UI are separate
 * increments; no network service, calendar provider, or external API is used.
 */
public final class LocalReminderReceiver extends BroadcastReceiver {
    static final String ACTION_REMIND = "com.orbis.meudia.LOCAL_REMINDER";
    static final String EXTRA_TITLE = "reminder_title";
    static final String EXTRA_ID = "reminder_id";
    static final String EXTRA_TASK_ID = "reminder_task_id";
    private static final String CHANNEL_ID = "orbis_local_reminders";

    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_REMIND.equals(intent.getAction())) return;
        // Renewal must not depend on the notification permission: a denied
        // notification must not permanently stop the recurrence chain.
        try {
            showNotification(context, intent);
        } finally {
            renewRecurringReminder(context, intent);
        }
    }

    private static void showNotification(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= 33 &&
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) return;

        String title = intent.getStringExtra(EXTRA_TITLE);
        if (title == null || title.trim().isEmpty()) return;
        title = title.trim();
        if (title.length() > 160) title = title.substring(0, 160);
        int id = intent.getIntExtra(EXTRA_ID, 0);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        Intent launch = new Intent(context, MainActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // Every reminder opens the same home screen, without task-specific extras.
        // Reuse one launch PendingIntent instead of allocating hash-derived identities.
        PendingIntent open = PendingIntent.getActivity(context, 0, launch,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(context, CHANNEL_ID)
                : new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.ic_header_check)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(title)
                .setAutoCancel(true)
                .setContentIntent(open);
        // A stable tag distinguishes task IDs even when their Java hashes collide.
        // Legacy broadcasts without a task ID retain the original notification behavior.
        String taskId = intent.getStringExtra(EXTRA_TASK_ID);
        if (taskId != null && !taskId.isEmpty()) {
            manager.notify(taskId, id, builder.build());
        } else {
            manager.notify(id, builder.build());
        }
    }

    private static void renewRecurringReminder(Context context, Intent intent) {
        String taskId = intent.getStringExtra(EXTRA_TASK_ID);
        if (taskId == null || taskId.isEmpty()) return;
        SharedPreferences prefs = context.getSharedPreferences("meu-dia-dados-v1", Context.MODE_PRIVATE);
        JSONArray tasks;
        try { tasks = new JSONArray(prefs.getString("tasks", "[]")); }
        catch (Exception invalidData) { return; }
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject task = tasks.optJSONObject(i);
            if (task == null || !taskId.equals(task.optString("id"))) continue;
            if ("once".equals(task.optString("mode")) || TaskTime.getTime(task).isEmpty()) return;
            try {
                // Schedule strictly after delivery, not after the originally planned time.
                // This avoids a burst of stale notifications after device sleep.
                LocalReminderScheduler.scheduleNext(context, task, taskId,
                        System.currentTimeMillis());
            } catch (RuntimeException ignored) {
                // Alarm restrictions may prevent renewal; reboot/time recovery retries.
            }
            return;
        }
    }
}
