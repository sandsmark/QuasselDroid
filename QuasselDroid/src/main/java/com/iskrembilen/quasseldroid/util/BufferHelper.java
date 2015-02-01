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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.iskrembilen.quasseldroid.protocol.state.NetworkCollection;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.ManageChannelEvent;
import com.iskrembilen.quasseldroid.events.ManageChannelEvent.ChannelAction;
import com.iskrembilen.quasseldroid.events.ManageNetworkEvent;
import com.iskrembilen.quasseldroid.events.ManageNetworkEvent.NetworkAction;
import com.iskrembilen.quasseldroid.events.SendMessageEvent;

public class BufferHelper {
    public static void joinChannel(int bufferId, NetworkCollection networks) {
        BusProvider.getInstance().post(new SendMessageEvent(bufferId, "/join " + networks.getBufferById(bufferId).getInfo().name));
    }

    public static void partChannel(int bufferId, NetworkCollection networks) {
        BusProvider.getInstance().post(new SendMessageEvent(bufferId, "/part " + networks.getBufferById(bufferId).getInfo().name));
    }

    public static void deleteChannel(int bufferId) {
        BusProvider.getInstance().post(new ManageChannelEvent(bufferId, ChannelAction.DELETE));
    }

    public static void tempHideChannel(int bufferId) {
        BusProvider.getInstance().post(new ManageChannelEvent(bufferId, ChannelAction.TEMP_HIDE));
    }

    public static void permHideChannel(int bufferId) {
        BusProvider.getInstance().post(new ManageChannelEvent(bufferId, ChannelAction.PERM_HIDE));
    }

    public static void unhideChannel(int bufferId) {
        BusProvider.getInstance().post(new ManageChannelEvent(bufferId, ChannelAction.UNHIDE));
    }

    public static void connectNetwork(int networkId) {
        BusProvider.getInstance().post(new ManageNetworkEvent(networkId, NetworkAction.CONNECT));
    }

    public static void disconnectNetwork(int networkId) {
        BusProvider.getInstance().post(new ManageNetworkEvent(networkId, NetworkAction.DISCONNECT));
    }

    public static void showDeleteConfirmDialog(Context context, final int bufferId) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_title_delete_buffer)
                .setMessage(R.string.dialog_message_delete_buffer)
                .setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BufferHelper.deleteChannel(bufferId);
                    }

                })
                .setNegativeButton(R.string.action_no, null)
                .show();
    }
}
