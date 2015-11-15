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

package com.iskrembilen.quasseldroid.gui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.iskrembilen.quasseldroid.protocol.state.Buffer;
import com.iskrembilen.quasseldroid.protocol.state.BufferInfo;
import com.iskrembilen.quasseldroid.protocol.state.Client;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.BufferDetailsChangedEvent;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.iskrembilen.quasseldroid.util.MessageUtil;
import com.squareup.otto.Subscribe;

public class TopicViewDialog extends DialogFragment {

    private static final String TAG = TopicViewDialog.class.getSimpleName();

    private int id;

    private AlertDialog mDialog;
    private Buffer mBuffer;

    public static TopicViewDialog newInstance(int id) {
        TopicViewDialog fragment = new TopicViewDialog();
        Bundle args = new Bundle();
        args.putInt("id", id);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        id = getArguments().getInt("id");
    }

    @Subscribe
    public void onBufferDetailsChanged(BufferDetailsChangedEvent event) {
        if (event.bufferId == id && mBuffer != null) {
            mDialog.setTitle(mBuffer.getInfo().name);
            setTopic(mBuffer.getTopic());
        }
        if (Client.getInstance() == null || Client.getInstance().getNetworks() == null || Client.getInstance().getNetworks().getBufferById(event.bufferId) == null) {
            dismiss();
        } else {
            Buffer buffer = Client.getInstance().getNetworks().getBufferById(event.bufferId);
            setTopic(buffer.getTopic());
        }
    }

    public void setTopic(CharSequence topic) {
        ((TextView) getDialog().findViewById(R.id.dialog_simple_text)).setText(topic);
    }

    @Override
    public Dialog getDialog() {
        return mDialog;
    }

    @Override
    public @NonNull Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        id = getArguments().getInt("id");
        mBuffer = Client.getInstance().getNetworks().getBufferById(id);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_simple_view, null);
        TextView topicField = (TextView) view.findViewById(R.id.dialog_simple_text);
        topicField.setText(MessageUtil.parseStyleCodes(getActivity(), mBuffer.getTopic(), preferences.getBoolean(getResources().getString(R.string.preference_colored_text),true)));
        if (preferences.getBoolean(getString(R.string.preference_monospace), false)) {
            topicField.setTypeface(Typeface.MONOSPACE);
        }

        String bufferName;
        if (mBuffer.getInfo().type== BufferInfo.Type.StatusBuffer)
            bufferName = Client.getInstance().getNetworks().getNetworkById(mBuffer.getInfo().networkId).getName();
        else
            bufferName = mBuffer.getInfo().name;

        builder.setView(view)
                .setTitle(bufferName)
                .setPositiveButton(getString(R.string.action_close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNeutralButton(getString(R.string.action_edit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TopicEditDialog.newInstance(mBuffer.getTopic(),mBuffer.getInfo().name,id).show(getFragmentManager(),TAG);
                    }
                });

        mDialog = builder.create();

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if (mBuffer.getInfo().type != BufferInfo.Type.ChannelBuffer) {
                    mDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);
                }
            }
        });

        BusProvider.getInstance().register(this);
        return mDialog;
    }
}
