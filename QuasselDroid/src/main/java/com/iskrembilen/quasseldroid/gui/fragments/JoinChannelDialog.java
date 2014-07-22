package com.iskrembilen.quasseldroid.gui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.JoinChannelEvent;
import com.iskrembilen.quasseldroid.util.BusProvider;

public class JoinChannelDialog extends SherlockDialogFragment {

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
        getDialog().setTitle("Join Channel");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getSherlockActivity(), android.R.layout.simple_spinner_item, getNetworkNames());
        networkSpinner.setAdapter(adapter);

    }

    @Override
    public void onStart() {
        super.onStart();
       AlertDialog dialog = (AlertDialog)getDialog();
        if(dialog != null) {
            Button positiveButton = (Button) dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!channelNameField.getText().toString().equals("")) {
                        String channelName = channelNameField.getText().toString().trim();
                        String networkSelected = (String) networkSpinner.getSelectedItem();
                        BusProvider.getInstance().post(new JoinChannelEvent(networkSelected, channelName));
                        dismiss();
                    }
                }
            });
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View customLayout = inflater.inflate(R.layout.dialog_join_channel, null);

        networkSpinner = (Spinner) customLayout.findViewById(R.id.dialog_join_channel_network_spinner);
        channelNameField = (EditText) customLayout.findViewById(R.id.dialog_join_channel_channel_name_field);

        builder.setView(customLayout);
        builder.setPositiveButton(R.string.dialog_join_channel_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Needed for backwards compatibility
            }
        });
        builder.setNegativeButton(R.string.dialog_join_channel_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                getDialog().dismiss();
            }
        });

        return builder.create();
    }

    public String[] getNetworkNames() {
        return getArguments().getStringArray("networks");
    }
}
