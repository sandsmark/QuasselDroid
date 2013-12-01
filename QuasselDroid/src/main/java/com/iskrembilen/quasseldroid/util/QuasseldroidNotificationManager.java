package com.iskrembilen.quasseldroid.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.gui.LoginActivity;
import com.iskrembilen.quasseldroid.gui.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class QuasseldroidNotificationManager {

    //TODO: lots of duplicate code in this class, clean up

    private Context context;
    private SharedPreferences preferences;
    private List<Integer> highlightedBuffers;
    NotificationManager notifyManager;

    public QuasseldroidNotificationManager(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        highlightedBuffers = new ArrayList<Integer>();
        //Remove any disconnect notification since we are connecting again
        notifyManager.cancel(R.id.NOTIFICATION_DISCONNECTED);
    }

    public void notifyHighlightsRead(int bufferId) {
        if (highlightedBuffers.contains((Integer) bufferId)) {
            highlightedBuffers.remove((Integer) bufferId);
            if (highlightedBuffers.size() == 0) {
                notifyConnected(false);
            } else {
                notifyHighlight(null);
            }
        }
    }

    private void notifyConnected(boolean withPhysicalNotifications) {
        int defaults = 0;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.stat_normal)
                .setContentTitle(context.getText(R.string.app_name))
                .setContentText(context.getText(R.string.notification_connected))
                .setOngoing(true)
                .setWhen(System.currentTimeMillis());

        if (withPhysicalNotifications && preferences.getBoolean(context.getString(R.string.preference_notify_connect), false)) {
            if (preferences.getBoolean(context.getString(R.string.preference_notification_vibrate), true))
                defaults |= Notification.DEFAULT_VIBRATE;

            if (preferences.getBoolean(context.getString(R.string.preference_notification_sound_active), false) &&
                    preferences.getBoolean(context.getString(R.string.has_focus), true) == false &&
                    preferences.getBoolean(context.getString(R.string.preference_notification_sound), false)) {
                Uri ringtone = Uri.parse(preferences.getString(context.getString(R.string.preference_notification_connect_sound_file), ""));
                if (ringtone.equals(Uri.EMPTY)) defaults |= Notification.DEFAULT_SOUND;
                else builder.setSound(ringtone);
            } else if (preferences.getBoolean(context.getString(R.string.preference_notification_sound_active), true) == false &&
                    preferences.getBoolean(context.getString(R.string.preference_notification_sound), false)) {
                Uri ringtone = Uri.parse(preferences.getString(context.getString(R.string.preference_notification_connect_sound_file), ""));
                if (ringtone.equals(Uri.EMPTY)) defaults |= Notification.DEFAULT_SOUND;
                else builder.setSound(ringtone);
            }
        }
        if (defaults != 0) builder.setDefaults(defaults);

        Intent launch = new Intent(context, MainActivity.class);
        launch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, launch, 0);
        builder.setContentIntent(contentIntent);

        // Send the notification.
        notifyManager.notify(R.id.NOTIFICATION, builder.build());
    }

    public void notifyConnected() {
        notifyConnected(true);
    }

    public Notification getConnectingNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.stat_connecting)
                .setContentTitle(context.getText(R.string.app_name))
                .setContentText(context.getText(R.string.notification_connecting))
                .setOngoing(true)
                .setWhen(System.currentTimeMillis());

        Intent launch = new Intent(context, MainActivity.class);
        launch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, launch, 0);
        builder.setContentIntent(contentIntent);
        return builder.build();

    }

    public void notifyConnecting() {
        // Send the notification.
        notifyManager.notify(R.id.NOTIFICATION, getConnectingNotification());
    }

    public void notifyHighlight(Integer bufferId) {
        int defaults = 0;

        if (bufferId != null && !highlightedBuffers.contains(bufferId)) {
            highlightedBuffers.add(bufferId);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.stat_highlight)
                .setContentTitle(context.getText(R.string.app_name))
                .setContentText(context.getResources().getQuantityString(R.plurals.notification_highlighted_on_x_buffers, highlightedBuffers.size(), highlightedBuffers.size()))
                .setOngoing(true)
                .setTicker(context.getString(R.string.notification_you_have_been_highlighted))
                .setWhen(System.currentTimeMillis())
                .setNumber(highlightedBuffers.size());

        Intent launch = new Intent(context, MainActivity.class);
        launch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, launch, 0);
        builder.setContentIntent(contentIntent);

        if (bufferId != null) {
            if (preferences.getBoolean(context.getString(R.string.preference_notification_sound_active), false) &&
                    preferences.getBoolean(context.getString(R.string.has_focus), true) == false &&
                    preferences.getBoolean(context.getString(R.string.preference_notification_sound), false)) {

                Uri ringtone = Uri.parse(preferences.getString(context.getString(R.string.preference_notification_sound_file), ""));
                if (ringtone.equals(Uri.EMPTY)) defaults |= Notification.DEFAULT_SOUND;
                else builder.setSound(ringtone);
            } else if (preferences.getBoolean(context.getString(R.string.preference_notification_sound_active), true) == false &&
                    preferences.getBoolean(context.getString(R.string.preference_notification_sound), false)) {

                Uri ringtone = Uri.parse(preferences.getString(context.getString(R.string.preference_notification_sound_file), ""));
                if (ringtone.equals(Uri.EMPTY)) defaults |= Notification.DEFAULT_SOUND;
                else builder.setSound(ringtone);
            }
            if (preferences.getBoolean(context.getString(R.string.preference_notification_light), false))
                defaults |= Notification.DEFAULT_LIGHTS;
            if (preferences.getBoolean(context.getString(R.string.preference_notification_vibrate), false))
                defaults |= Notification.DEFAULT_VIBRATE;
        }
        if (defaults != 0) builder.setDefaults(defaults);

        // Send the notification.
        notifyManager.notify(R.id.NOTIFICATION, builder.build());
    }

    public Notification getDisconnectedNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.stat_disconnected)
                .setContentTitle(context.getText(R.string.app_name))
                .setContentText(context.getText(R.string.notification_disconnected))
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        Intent launch = new Intent(context, LoginActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, launch, 0);

        // Set the info for the views that show in the notification panel.
        builder.setContentIntent(contentIntent);
        //Send the notification.
        return builder.build();

    }

    public void notifyDisconnected() {
        //Send the notification.
        notifyManager.notify(R.id.NOTIFICATION, getDisconnectedNotification());
    }
}
