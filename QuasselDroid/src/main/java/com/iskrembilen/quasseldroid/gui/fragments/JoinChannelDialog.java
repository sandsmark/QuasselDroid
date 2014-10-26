package com.iskrembilen.quasseldroid.gui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialog = getActivity().getLayoutInflater().inflate(R.layout.dialog_join_channel, null);

        networkSpinner = (Spinner) dialog.findViewById(R.id.dialog_join_channel_network_spinner);
        channelNameField = (EditText) dialog.findViewById(R.id.dialog_join_channel_channel_name_field);

        builder.setView(dialog)
                .setTitle(getResources().getString(R.string.dialog_title_channel))
                .setPositiveButton(getResources().getString(R.string.dialog_action_join), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String channelName = channelNameField.getText().toString().trim();
                        if (channelName != null && !channelName.trim().equalsIgnoreCase("")) {
                            String networkSelected = (String) networkSpinner.getSelectedItem();
                            BusProvider.getInstance().post(new JoinChannelEvent(networkSelected, channelName));
                            dismiss();
                        } else {
                            Toast.makeText(getActivity(),R.string.dialog_message_join_no_channel,Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton(getResources().getString(R.string.dialog_action_cancel), new DialogInterface.OnClickListener() {
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
