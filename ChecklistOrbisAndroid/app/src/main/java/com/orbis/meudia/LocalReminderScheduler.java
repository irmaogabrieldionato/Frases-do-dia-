package com.orbis.meudia;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
                            int reminderId, long nowMillis) {
        if (context == null || task == null || reminderId <= 0) return false;
        long when = LocalReminderTime.occurrenceMillis(
                task, occurrenceDate, TimeZone.getDefault(), nowMillis);
        if (when < 0L) return false;
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms == null) return false;
        PendingIntent pending = reminderIntent(context, reminderId,
                task.optString("title", ""));
        if (Build.VERSION.SDK_INT >= 23) {
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pending);
        } else {
            alarms.set(AlarmManager.RTC_WAKEUP, when, pending);
        }
        return true;
    }

    static void cancel(Context context, int reminderId) {
        if (context == null || reminderId <= 0) return;
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms == null) return;
        Intent intent = new Intent(context, LocalReminderReceiver.class);
        intent.setAction(LocalReminderReceiver.ACTION_REMIND);
        PendingIntent pending = PendingIntent.getBroadcast(context, reminderId, intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pending != null) {
            alarms.cancel(pending);
            pending.cancel();
        }
    }

    private static PendingIntent reminderIntent(Context context, int id, String title) {
        Intent intent = new Intent(context, LocalReminderReceiver.class);
        intent.setAction(LocalReminderReceiver.ACTION_REMIND);
        intent.putExtra(LocalReminderReceiver.EXTRA_ID, id);
        intent.putExtra(LocalReminderReceiver.EXTRA_TITLE, title);
        return PendingIntent.getBroadcast(context, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
