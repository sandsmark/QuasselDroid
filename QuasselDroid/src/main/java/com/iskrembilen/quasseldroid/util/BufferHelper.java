package com.iskrembilen.quasseldroid.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.iskrembilen.quasseldroid.NetworkCollection;
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

    public static void connectNetwork(int networkId) {
        BusProvider.getInstance().post(new ManageNetworkEvent(networkId, NetworkAction.CONNECT));
    }

    public static void disconnectNetwork(int networkId) {
        BusProvider.getInstance().post(new ManageNetworkEvent(networkId, NetworkAction.DISCONNECT));
    }

    public static void showDeleteConfirmDialog(Context context, final int bufferId) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_delete_buffer_title)
                .setMessage(R.string.dialog_delete_buffer_message)
                .setPositiveButton(R.string.dialog_delete_buffer_yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BufferHelper.deleteChannel(bufferId);
                    }

                })
                .setNegativeButton(R.string.dialog_delete_buffer_no, null)
                .show();
    }
}
