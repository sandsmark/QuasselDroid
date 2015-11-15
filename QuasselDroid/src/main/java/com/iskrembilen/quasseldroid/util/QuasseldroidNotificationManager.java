/*
    QuasselDroid - Quassel client for Android
    Copyright (C) 2015 Ken Børge Viktil
    Copyright (C) 2015 Magnus Fjell
    Copyright (C) 2015 Martin Sandsmark <martin.sandsmark@kde.org>

    This program is free software: you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation, either version 3 of the License, or (at your option)
    any later version, or under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either version 2.1 of
    the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License and the
    GNU Lesser General Public License along with this program.  If not, see
    <http://www.gnu.org/licenses/>.
 */

package com.iskrembilen.quasseldroid.util;

import android.app.Notification;
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
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.SparseArray;

import com.google.common.base.Optional;
import com.iskrembilen.quasseldroid.protocol.state.Buffer;
import com.iskrembilen.quasseldroid.protocol.state.BufferInfo;
import com.iskrembilen.quasseldroid.protocol.state.Client;
import com.iskrembilen.quasseldroid.protocol.state.IrcMessage;
import com.iskrembilen.quasseldroid.protocol.state.NetworkCollection;
import com.iskrembilen.quasseldroid.Quasseldroid;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.InitProgressEvent;
import com.iskrembilen.quasseldroid.gui.LoginActivity;
import com.iskrembilen.quasseldroid.gui.MainActivity;
import com.iskrembilen.quasseldroid.service.CoreConnService;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class QuasseldroidNotificationManager {

    //TODO: lots of duplicate code in this class, clean up

    private Context context;
    private SharedPreferences preferences;
    private final List<Integer> highlightedBuffers = new ArrayList<>();
    private final SparseArray<List<IrcMessage>> highlightedMessages = new SparseArray<>();
    private List<Integer> buffers = new ArrayList<>();

    android.app.NotificationManager notifyManager;
    private boolean connected = false;
    private boolean pendingHighlightNotification;
    private PendingIntent contentIntent;

    public QuasseldroidNotificationManager(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        notifyManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //Remove any disconnect notification since we are connecting again
        notifyManager.cancel(R.id.NOTIFICATION_DISCONNECTED);
        BusProvider.getInstance().register(this);
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(sharedPreferenceListener);
    }

    public void notifyHighlightsRead(Integer bufferId) {
        Log.d(getClass().getSimpleName(), String.format("Intent. Setting highlights read for buffer %d", bufferId));
        synchronized (highlightedBuffers) {
            if (highlightedBuffers.contains(bufferId)) {
                highlightedMessages.remove(bufferId);
                highlightedBuffers.remove(bufferId);
                buffers.remove((Integer) bufferId);
                if (highlightedBuffers.size() == 0) {
                    notifyConnected(false);
                } else if (!connected) {
                    notifyConnected(false);
                    pendingHighlightNotification = true;
                } else {
                    notifyHighlights();
                }
            }
        }
    }

    private void notifyConnected(boolean withPhysicalNotifications) {
        int defaults = 0;
        connected = true;
        if (checkPending()) {
            notifyHighlights();
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.stat_normal)
                    .setContentTitle(context.getText(R.string.app_name))
                    .setContentText(context.getText(R.string.notification_connected))
                    .setAutoCancel(true)
                    .setPriority(preferences.getBoolean("notifypersistence", false) ? NotificationCompat.PRIORITY_MIN : NotificationCompat.PRIORITY_LOW)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
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
            if (contentIntent != null) contentIntent.cancel();
            contentIntent = PendingIntent.getActivity(context, 0, launch, 0);
            builder.setContentIntent(contentIntent);

            builder.setColor(context.getResources().getColor(R.color.primary));

            Intent disconnect = new Intent(context, CoreConnService.class);
            disconnect.putExtra("disconnect",true);

            PendingIntent actionIntent = PendingIntent.getService(context,0,disconnect, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.addAction(R.drawable.ic_disconnect,context.getString(R.string.action_disconnect),actionIntent);

            // Send the notification.
            notifyManager.notify(R.id.NOTIFICATION, builder.build());
        }
    }

    private boolean checkPending() {
        if (pendingHighlightNotification && getHighlightedMessageCount()==0)
            pendingHighlightNotification = false;

        return pendingHighlightNotification;
    }

    public void notifyConnected() {
        notifyConnected(true);
    }

    public Notification getConnectingNotification() {
        return getConnectingNotification(Optional.<String>absent());
    }

    public Notification getConnectingNotification(Optional<String> status) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.stat_connecting)
                .setContentTitle(context.getText(R.string.app_name))
                .setContentText(status.or(context.getText(R.string.notification_connecting).toString()))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setWhen(System.currentTimeMillis());

        Intent launch = new Intent(context, MainActivity.class);
        launch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (contentIntent != null) contentIntent.cancel();
        contentIntent = PendingIntent.getActivity(context, 0, launch, 0);
        builder.setContentIntent(contentIntent);
        builder.setColor(context.getResources().getColor(R.color.primary));
        return builder.build();

    }

    public void notifyConnecting() {
        notifyConnecting(Optional.<String>absent());
    }

    public void notifyConnecting(Optional<String> status) {
        // Send the notification.
        notifyManager.notify(R.id.NOTIFICATION, getConnectingNotification(status));
    }

    public void addMessage(IrcMessage message) {
        synchronized (highlightedBuffers) {
            // If the buffer in question isn’t in the list of highlighted buffers, add it
            if (!highlightedBuffers.contains(message.bufferInfo.id)) {
                highlightedBuffers.add(message.bufferInfo.id);
                // If the buffer has had no highlights yet, add a new list of highlights
                if (highlightedMessages.get(message.bufferInfo.id) == null)
                    highlightedMessages.put(message.bufferInfo.id, new ArrayList<IrcMessage>());
            }
            if (!highlightedMessages.get(message.bufferInfo.id).contains(message))
                highlightedMessages.get(message.bufferInfo.id).add(message);
        }

        if (buffers.contains(message.bufferInfo.id)) {
            buffers.remove((Integer) message.bufferInfo.id);
        }
        buffers.add(message.bufferInfo.id);

        pendingHighlightNotification = true;

        if (connected)
            notifyHighlights();
    }

    int getHighlightedMessageCount() {
        synchronized (highlightedBuffers) {
            int res = 0;
            for (Integer bufferId : highlightedBuffers) {
                res += highlightedMessages.get(bufferId).size();
            }
            return res;
        }
    }

    public void notifyHighlights() {
        System.out.printf("Connected: %b\n", connected);

        if (!connected) return;

        synchronized (highlightedBuffers) {
            boolean displayColors = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.preference_colored_text), true);

            int defaults = 0;

            Resources res = context.getResources();

            int highlightedMessageCount = getHighlightedMessageCount();

            // Building the base notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.drawable.stat_highlight)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setWhen(System.currentTimeMillis())
                    .setNumber(highlightedMessageCount);

            if (highlightedBuffers.size() == 1 && highlightedMessages.get(highlightedBuffers.get(0)).size() == 1) {
                IrcMessage message = highlightedMessages.get(highlightedBuffers.get(0)).get(0);

                builder.setContentTitle(message.bufferInfo.name)
                        .setContentText(MessageUtil.parseStyleCodes(context, String.format("%s: %s", message.getNick(), message.content), displayColors));
            } else if (highlightedBuffers.size() == 1) {
                NetworkCollection networks = Client.getInstance().getNetworks();
                Buffer buffer = networks.getBufferById(highlightedBuffers.get(0));
                List<IrcMessage> messages = highlightedMessages.get(highlightedBuffers.get(0));

                builder.setContentTitle(buffer.getInfo().name)
                        .setContentText(
                                String.format(
                                        res.getString(R.string.notification_hightlights_on_buffers),
                                        res.getQuantityString(R.plurals.notification_x_highlights, highlightedMessageCount, highlightedMessageCount),
                                        buffer.getInfo().name));

                NotificationCompat.InboxStyle inboxStyle =
                        new NotificationCompat.InboxStyle();

                // Sets a title for the Inbox in expanded layout
                inboxStyle.setBigContentTitle(
                        String.format(
                                res.getString(R.string.notification_hightlights_on_buffers),
                                res.getQuantityString(R.plurals.notification_x_highlights, highlightedMessageCount, highlightedMessageCount),
                                buffer.getInfo().name));

                // Moves events into the expanded layout

                if (buffer.getInfo().type == BufferInfo.Type.QueryBuffer) {
                    SpannableString s;

                    for (IrcMessage m : messages) {
                        s = MessageUtil.parseStyleCodes(context, String.format("%s: %s", m.getNick(), m.content), displayColors);
                        s.setSpan(new StyleSpan(Typeface.BOLD), 0, m.getNick().length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                        inboxStyle.addLine(s);
                    }
                } else {
                    SpannableStringBuilder s = new SpannableStringBuilder(buffer.getInfo().name);
                    s.append(":");
                    s.setSpan(new StyleSpan(Typeface.BOLD), 0, buffer.getInfo().name.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                    inboxStyle.addLine(s);
                    for (IrcMessage m : messages) {
                        inboxStyle.addLine(String.format("  %s: %s", m.getNick(), m.content));
                    }
                }

                // Moves the expanded layout object into the notification object.
                builder.setStyle(inboxStyle);
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

                synchronized (highlightedBuffers) {
                    // Moves events into the expanded layout
                    for (Integer bufferId : highlightedBuffers) {
                        Buffer buffer = Client.getInstance().getNetworks().getBufferById(bufferId);
                        List<IrcMessage> messages = highlightedMessages.get(bufferId);

                        if (messages.size() == 1) {
                            IrcMessage m = messages.get(0);
                            SpannableString s;
                            if (m.bufferInfo.type == BufferInfo.Type.QueryBuffer) {
                                s = MessageUtil.parseStyleCodes(context, String.format("%s: %s", m.getNick(), m.content), displayColors);
                                s.setSpan(new StyleSpan(Typeface.BOLD), 0, m.getNick().length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                            } else {
                                s = MessageUtil.parseStyleCodes(context, String.format("%s %s: %s", m.bufferInfo.name, m.getNick(), m.content), displayColors);
                                s.setSpan(new StyleSpan(Typeface.BOLD), 0, m.bufferInfo.name.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                            }
                            inboxStyle.addLine(s);
                        } else if (buffer.getInfo().type == BufferInfo.Type.QueryBuffer) {
                            SpannableString s;

                            for (IrcMessage m : messages) {
                                s = MessageUtil.parseStyleCodes(context, String.format("%s: %s", m.getNick(), m.content), displayColors);
                                s.setSpan(new StyleSpan(Typeface.BOLD), 0, m.getNick().length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                inboxStyle.addLine(s);
                            }
                        } else {
                            SpannableStringBuilder s = new SpannableStringBuilder(buffer.getInfo().name);
                            s.append(":");
                            s.setSpan(new StyleSpan(Typeface.BOLD), 0, buffer.getInfo().name.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                            inboxStyle.addLine(s);
                            for (IrcMessage m : messages) {
                                inboxStyle.addLine(String.format("  %s: %s", m.getNick(), m.content));
                            }
                        }
                    }
                }
                // Moves the expanded layout object into the notification object.
                builder.setStyle(inboxStyle);
            }

            builder.setColor(ThemeUtil.Color.chatHighlight);
            if (hasDirectMessage()) {
                builder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
            } else {
                builder.setCategory(NotificationCompat.CATEGORY_SOCIAL);
            }

            Intent launch = new Intent(context, MainActivity.class);
            if (!buffers.isEmpty()) launch.putExtra("extraBufferId", buffers.get(0));
            launch.putExtra("extraDrawer", false);

            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.scheme("content");
            uriBuilder.path(Quasseldroid.class.getCanonicalName());
            uriBuilder.appendPath("open-buffer");
            launch.setData(uriBuilder.build());

            launch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (contentIntent != null) contentIntent.cancel();
            contentIntent = PendingIntent.getActivity(context, highlightedMessages.hashCode(), launch, 0);
            builder.setContentIntent(contentIntent);



            if (pendingHighlightNotification) {
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
            }

            // Send the notification.
            notifyManager.notify(R.id.NOTIFICATION, builder.build());

            pendingHighlightNotification = false;
        }
    }

    private boolean hasDirectMessage() {
        NetworkCollection networks = Client.getInstance().getNetworks();
        synchronized (highlightedBuffers) {
            for (Integer bufferId : highlightedBuffers) {
                Buffer buffer = networks.getBufferById(bufferId);

                // TODO: Maybe add Groupbuffer here as well?
                if (buffer != null && buffer.getInfo() != null && buffer.getInfo().type == BufferInfo.Type.QueryBuffer)
                    return true;
            }
        }
        return false;
    }

    @Subscribe
    public void onInitProgressed(InitProgressEvent event) {
        if (event.done && getHighlightedMessageCount()>0) {
            notifyHighlights();
        } else if (!event.done) {
            notifyConnecting(Optional.of(event.progress));
        }
    }

    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(context.getResources().getString(R.string.preference_notify_hide_persistence))
                    && connected
                    && highlightedMessages.size()==0) {
                notifyConnected(false);
            }
        }
    };

    public void notifyDisconnected() {
        connected = false;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.stat_disconnected)
                .setContentTitle(context.getText(R.string.app_name))
                .setContentText(context.getText(R.string.notification_disconnected))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setWhen(System.currentTimeMillis());

        // Clean up existing messages

        // The PendingIntent to launch our activity if the user selects this notification
        Intent launch = new Intent(context, LoginActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (contentIntent != null) contentIntent.cancel();
        contentIntent = PendingIntent.getActivity(context, 0, launch, 0);

        // Set the info for the views that show in the notification panel.
        builder.setContentIntent(contentIntent);

        builder.setColor(context.getResources().getColor(R.color.chat_line_error_dark));
        //Send the notification.
        notifyManager.notify(R.id.NOTIFICATION_DISCONNECTED, builder.build());
    }

    public void clear() {
        buffers.clear();
        highlightedBuffers.clear();
        highlightedMessages.clear();
    }
}
