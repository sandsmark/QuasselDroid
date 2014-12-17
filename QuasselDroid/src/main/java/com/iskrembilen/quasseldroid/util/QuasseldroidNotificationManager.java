package com.iskrembilen.quasseldroid.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.SparseArray;

import com.iskrembilen.quasseldroid.Buffer;
import com.iskrembilen.quasseldroid.BufferInfo;
import com.iskrembilen.quasseldroid.IrcMessage;
import com.iskrembilen.quasseldroid.NetworkCollection;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.gui.LoginActivity;
import com.iskrembilen.quasseldroid.gui.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QuasseldroidNotificationManager {

    //TODO: lots of duplicate code in this class, clean up

    private Context context;
    private SharedPreferences preferences;
    private final List<Integer> highlightedBuffers = new ArrayList<Integer>();
    private final SparseArray<List<IrcMessage>> highlightedMessages = new SparseArray<List<IrcMessage>>();
    private IrcMessage lastMessage;

    android.app.NotificationManager notifyManager;
    private boolean connected = false;
    private Notification pendingHighlightNotification;

    public QuasseldroidNotificationManager(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        notifyManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //Remove any disconnect notification since we are connecting again
        notifyManager.cancel(R.id.NOTIFICATION_DISCONNECTED);
    }

    public void notifyHighlightsRead(Integer bufferId) {
        synchronized (highlightedBuffers) {
            if (highlightedBuffers.contains(bufferId)) {
                highlightedMessages.remove(bufferId);
                highlightedBuffers.remove(bufferId);
                if (highlightedBuffers.size() == 0) {
                    notifyConnected(false);
                } else {
                    notifyHighlights();
                }
            }
        }
    }

    private void notifyConnected(boolean withPhysicalNotifications) {
        int defaults = 0;
        connected = true;
        if(pendingHighlightNotification != null) {
            notifyManager.notify(R.id.NOTIFICATION, pendingHighlightNotification);
            pendingHighlightNotification = null;
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.icon_flat)
                    .setContentTitle(context.getText(R.string.app_name))
                    .setContentText(context.getText(R.string.notification_connected))
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setWhen(System.currentTimeMillis());

            if (withPhysicalNotifications && preferences.getBoolean(context.getString(R.string.preference_notify_connect), false)) {
                if (preferences.getBoolean(context.getString(R.string.preference_notification_vibrate), true))
                    defaults |= Notification.DEFAULT_VIBRATE;

                if (preferences.getBoolean(context.getString(R.string.preference_notification_sound_active), false) &&
                        !preferences.getBoolean(context.getString(R.string.has_focus), true) &&
                        preferences.getBoolean(context.getString(R.string.preference_notification_sound), false)) {
                    Uri ringtone = Uri.parse(preferences.getString(context.getString(R.string.preference_notification_connect_sound_file), ""));
                    if (ringtone.equals(Uri.EMPTY)) defaults |= Notification.DEFAULT_SOUND;
                    else builder.setSound(ringtone);
                } else if (!preferences.getBoolean(context.getString(R.string.preference_notification_sound_active), true) &&
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

            builder.setColor(context.getResources().getColor(R.color.primary));

            // Send the notification.
            notifyManager.notify(R.id.NOTIFICATION, builder.build());
        }
    }

    public void notifyConnected() {
        notifyConnected(true);
    }

    public Notification getConnectingNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.icon_flat)
                .setContentTitle(context.getText(R.string.app_name))
                .setContentText(context.getText(R.string.notification_connecting))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setWhen(System.currentTimeMillis());

        Intent launch = new Intent(context, MainActivity.class);
        launch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, launch, 0);
        builder.setContentIntent(contentIntent);
        builder.setColor(context.getResources().getColor(R.color.primary));
        return builder.build();

    }

    public void notifyConnecting() {
        // Send the notification.
        notifyManager.notify(R.id.NOTIFICATION, getConnectingNotification());
    }

    public void addMessage(IrcMessage message) {
        if (!highlightedBuffers.contains(message.bufferInfo.id)) {
            highlightedBuffers.add(message.bufferInfo.id);
            if (highlightedMessages.get(message.bufferInfo.id)==null)
                highlightedMessages.put(message.bufferInfo.id, new ArrayList<IrcMessage>());
        }
        if (!highlightedMessages.get(message.bufferInfo.id).contains(message))
            highlightedMessages.get(message.bufferInfo.id).add(message);

        lastMessage = message;

        notifyHighlights();
    }

    int getHighlightedMessageCount() {
        int res = 0;
        for (Integer bufferId : highlightedBuffers) {
            res += highlightedMessages.get(bufferId).size();
        }
        return res;
    }

    public void notifyHighlights() {
        int defaults = 0;

        Resources res = context.getResources();

        int highlightedMessageCount = getHighlightedMessageCount();

        // Building the base notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.stat_highlight)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setWhen(System.currentTimeMillis())
                .setNumber(highlightedMessageCount);

        if (lastMessage!=null) {
            builder.setTicker(String.format("%s : %s",
                    lastMessage.getNick(),
                    lastMessage.content));
        }

        if (highlightedBuffers.size()==1 && highlightedMessages.get(highlightedBuffers.get(0)).size()==1) {
            IrcMessage message = highlightedMessages.get(highlightedBuffers.get(0)).get(0);

            builder.setContentTitle(message.bufferInfo.name)
                   .setContentText(String.format("%s: %s",message.getNick(),message.content));
        } else {
            builder.setContentTitle(context.getText(R.string.app_name))
                   .setContentText(
                           String.format(
                                   res.getString(R.string.notification_hightlights_on_buffers),
                                   res.getQuantityString(R.plurals.notification_x_highlights, highlightedMessageCount, highlightedMessageCount),
                                   res.getQuantityString(R.plurals.notification_on_x_buffers, highlightedBuffers.size(), highlightedBuffers.size())));

            NotificationCompat.InboxStyle inboxStyle =
                    new NotificationCompat.InboxStyle();

            // Sets a title for the Inbox in expanded layout
            inboxStyle.setBigContentTitle(
                    String.format(
                            res.getString(R.string.notification_hightlights_on_buffers),
                            res.getQuantityString(R.plurals.notification_x_highlights, highlightedMessageCount, highlightedMessageCount),
                            res.getQuantityString(R.plurals.notification_on_x_buffers, highlightedBuffers.size(), highlightedBuffers.size())));

            // Moves events into the expanded layout
            for (Integer bufferId : highlightedBuffers) {
                Buffer buffer = NetworkCollection.getInstance().getBufferById(bufferId);
                List<IrcMessage> messages = highlightedMessages.get(bufferId);

                if (messages.size()==1) {
                    IrcMessage m = messages.get(0);
                    if (m.bufferInfo.type== BufferInfo.Type.QueryBuffer)
                        inboxStyle.addLine(String.format("[%s] %s",m.bufferInfo.name,m.content));
                    else
                        inboxStyle.addLine(String.format("[%s] %s: %s",m.bufferInfo.name,m.getNick(),m.content));
                } else if (buffer.getInfo().type== BufferInfo.Type.QueryBuffer) {
                    SpannableStringBuilder s;

                    for (IrcMessage m : messages) {
                        s = new SpannableStringBuilder(String.format("%s: %s",m.getNick(),m.content));
                        s.setSpan(new StyleSpan(Typeface.BOLD), 0, m.getNick().length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                        inboxStyle.addLine(s);
                    }
                } else {
                    SpannableStringBuilder s = new SpannableStringBuilder(buffer.getInfo().name);
                    s.setSpan(new StyleSpan(Typeface.BOLD), 0, buffer.getInfo().name.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                    inboxStyle.addLine(s);
                    for (IrcMessage m : messages) {
                        inboxStyle.addLine(String.format("%s: %s",m.getNick(),m.content));
                    }
                }
            }
            // Moves the expanded layout object into the notification object.
            builder.setStyle(inboxStyle);
        }

        builder.setColor(ThemeUtil.color.chatHighlight);

        Intent launch = new Intent(context, MainActivity.class);
        launch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, launch, 0);
        builder.setContentIntent(contentIntent);

        if (preferences.getBoolean(context.getString(R.string.preference_notification_sound_active), false) &&
                !preferences.getBoolean(context.getString(R.string.has_focus), true) &&
                preferences.getBoolean(context.getString(R.string.preference_notification_sound), false)) {

            Uri ringtone = Uri.parse(preferences.getString(context.getString(R.string.preference_notification_sound_file), ""));
            if (ringtone.equals(Uri.EMPTY)) defaults |= Notification.DEFAULT_SOUND;
            else builder.setSound(ringtone);
        } else if (!preferences.getBoolean(context.getString(R.string.preference_notification_sound_active), true) &&
                preferences.getBoolean(context.getString(R.string.preference_notification_sound), false)) {

            Uri ringtone = Uri.parse(preferences.getString(context.getString(R.string.preference_notification_sound_file), ""));
            if (ringtone.equals(Uri.EMPTY)) defaults |= Notification.DEFAULT_SOUND;
            else builder.setSound(ringtone);
        }
        if (preferences.getBoolean(context.getString(R.string.preference_notification_light), false))
            defaults |= Notification.DEFAULT_LIGHTS;
        if (preferences.getBoolean(context.getString(R.string.preference_notification_vibrate), false))
            defaults |= Notification.DEFAULT_VIBRATE;

        if (defaults != 0) builder.setDefaults(defaults);

        // Send the notification.
        if(!connected) {
            pendingHighlightNotification = builder.build();
        } else {
            notifyManager.notify(R.id.NOTIFICATION, builder.build());
        }

        lastMessage = null;
    }

    public void notifyDisconnected() {
        connected = false;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.icon_flat)
                .setContentTitle(context.getText(R.string.app_name))
                .setContentText(context.getText(R.string.notification_disconnected))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setWhen(System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        Intent launch = new Intent(context, LoginActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, launch, 0);

        // Set the info for the views that show in the notification panel.
        builder.setContentIntent(contentIntent);

        builder.setColor(context.getResources().getColor(R.color.chat_line_error_dark));
        //Send the notification.
        notifyManager.notify(R.id.NOTIFICATION_DISCONNECTED, builder.build());
    }
}