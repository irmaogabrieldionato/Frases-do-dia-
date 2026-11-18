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
import android.os.Build;

/**
 * Offline notification delivery endpoint. Scheduling and runtime permission UI are separate
 * increments; no network service, calendar provider, or external API is used.
 */
public final class LocalReminderReceiver extends BroadcastReceiver {
    static final String ACTION_REMIND = "com.orbis.meudia.LOCAL_REMINDER";
    static final String EXTRA_TITLE = "reminder_title";
    static final String EXTRA_ID = "reminder_id";
    private static final String CHANNEL_ID = "orbis_local_reminders";

    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_REMIND.equals(intent.getAction())) return;
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
        PendingIntent open = PendingIntent.getActivity(context, id, launch,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(context, CHANNEL_ID)
                : new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.ic_header_check)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(title)
                .setAutoCancel(true)
                .setContentIntent(open);
        manager.notify(id, builder.build());
    }
}
