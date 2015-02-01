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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.JoinChannelEvent;
import com.iskrembilen.quasseldroid.util.BusProvider;

public class JoinChannelDialog extends DialogFragment {

    private Spinner networkSpinner;
    private EditText channelNameField;

    public static JoinChannelDialog newInstance(String[] networkNames) {
        JoinChannelDialog fragment = new JoinChannelDialog();
        Bundle args = new Bundle();
        args.putStringArray("networks", networkNames);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, getNetworkNames());
        networkSpinner.setAdapter(adapter);
    }

    @Override
    public @NonNull Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialog = getActivity().getLayoutInflater().inflate(R.layout.dialog_join_channel, null);

        networkSpinner = (Spinner) dialog.findViewById(R.id.dialog_join_channel_network_spinner);
        channelNameField = (EditText) dialog.findViewById(R.id.dialog_join_channel_channel_name_field);

        builder.setView(dialog)
                .setTitle(getResources().getString(R.string.dialog_title_channel))
                .setPositiveButton(getResources().getString(R.string.action_join), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String channelName = channelNameField.getText().toString().trim();
                        if (!channelName.trim().equalsIgnoreCase("")) {
                            String networkSelected = (String) networkSpinner.getSelectedItem();
                            BusProvider.getInstance().post(new JoinChannelEvent(networkSelected, channelName));
                            dismiss();
                        } else {
                            Toast.makeText(getActivity(),R.string.dialog_message_join_no_channel,Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton(getResources().getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getDialog().dismiss();
            }
        });
        return builder.create();
    }

    public String[] getNetworkNames() {
        return getArguments().getStringArray("networks");
    }
}
