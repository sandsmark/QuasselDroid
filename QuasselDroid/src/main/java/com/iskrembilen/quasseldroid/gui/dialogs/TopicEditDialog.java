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
import android.widget.EditText;

import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.SendMessageEvent;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.iskrembilen.quasseldroid.util.MessageUtil;

public class TopicEditDialog extends DialogFragment {

    private static final String TAG = TopicEditDialog.class.getSimpleName();

    private CharSequence topic;
    private String name;
    private int id;

    protected EditText topicField;

    public static TopicEditDialog newInstance(CharSequence topic, String name, int id) {
        TopicEditDialog fragment = new TopicEditDialog();
        Bundle args = new Bundle();
        args.putCharSequence("topic", topic);
        args.putString("name", name);
        args.putInt("id", id);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        topic = getArguments().getCharSequence("topic");
        name = getArguments().getString("name");
        id = getArguments().getInt("id");
    }

    @Override
    public @NonNull Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        topic = getArguments().getCharSequence("topic");
        name = getArguments().getString("name");
        id = getArguments().getInt("id");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        View dialog = getActivity().getLayoutInflater().inflate(R.layout.dialog_simple_edit, null);
        final EditText topicField = (EditText) dialog.findViewById(R.id.dialog_simple_text);
        this.topicField = topicField;
        topicField.setHint(R.string.hint_topic_edit);
        topicField.setText(MessageUtil.parseStyleCodes(getActivity(), topic.toString(), preferences.getBoolean(getResources().getString(R.string.preference_colored_text), true)));
        if (preferences.getBoolean(getString(R.string.preference_monospace), false)) {
            topicField.setTypeface(Typeface.MONOSPACE);
        }

        builder.setView(dialog).setTitle(name);
        builder.setPositiveButton(getString(R.string.action_close),new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNeutralButton(getString(R.string.action_save),new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BusProvider.getInstance().post(new SendMessageEvent(id, "/topic "+topicField.getText().toString()));
            }
        });
        return builder.create();
    }
}
