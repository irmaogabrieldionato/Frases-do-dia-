package com.orbis.meudia;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.net.Uri;
import org.json.JSONObject;
import java.util.TimeZone;

/**
 * Schedules a single offline, inexact reminder. No exact-alarm privilege, network,
 * external calendar provider or background service is required.
 *
 * The caller owns stable reminder IDs and must cancel/recreate alarms when a task
 * is edited, removed or its occurrence changes. Reboot recovery is a later increment.
 */
final class LocalReminderScheduler {
    private LocalReminderScheduler() {}

    static boolean schedule(Context context, JSONObject task, String occurrenceDate,
                            String taskId, long nowMillis) {
        if (context == null || task == null || taskId == null || taskId.isEmpty()) return false;
        long when = LocalReminderTime.occurrenceMillis(
                task, occurrenceDate, TimeZone.getDefault(), nowMillis);
        if (when < 0L) return false;
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms == null) return false;
        PendingIntent pending = reminderIntent(context, taskId,
                task.optString("title", ""));
        if (Build.VERSION.SDK_INT >= 23) {
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pending);
        } else {
            alarms.set(AlarmManager.RTC_WAKEUP, when, pending);
        }
        return true;
    }

    static void cancel(Context context, String taskId) {
        if (context == null || taskId == null || taskId.isEmpty()) return;
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms == null) return;
        Intent intent = new Intent(context, LocalReminderReceiver.class);
        intent.setAction(LocalReminderReceiver.ACTION_REMIND);
        intent.setData(Uri.parse("orbis-reminder://task/" + Uri.encode(taskId)));
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pending != null) { alarms.cancel(pending); pending.cancel(); }
    }

    static void cancelLegacy(Context context, String taskId) {
        if (context == null || taskId == null || taskId.isEmpty()) return;
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms == null) return;
        int id = taskId.hashCode() & 0x7fffffff;
        if (id == 0) id = 1;
        Intent intent = new Intent(context, LocalReminderReceiver.class);
        intent.setAction(LocalReminderReceiver.ACTION_REMIND);
        PendingIntent pending = PendingIntent.getBroadcast(context, id, intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pending != null) { alarms.cancel(pending); pending.cancel(); }
    }

    private static PendingIntent reminderIntent(Context context, String taskId, String title) {
        Intent intent = new Intent(context, LocalReminderReceiver.class);
        intent.setAction(LocalReminderReceiver.ACTION_REMIND);
        intent.setData(Uri.parse("orbis-reminder://task/" + Uri.encode(taskId)));
        int notificationId = taskId.hashCode() & 0x7fffffff;
        if (notificationId == 0) notificationId = 1;
        intent.putExtra(LocalReminderReceiver.EXTRA_ID, notificationId);
        intent.putExtra(LocalReminderReceiver.EXTRA_TITLE, title);
        return PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
