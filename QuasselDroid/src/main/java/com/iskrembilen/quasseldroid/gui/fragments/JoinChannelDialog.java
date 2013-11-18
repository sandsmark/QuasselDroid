package com.iskrembilen.quasseldroid.gui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_join_channel, container, false);

        networkSpinner = (Spinner) root.findViewById(R.id.dialog_join_channel_network_spinner);
        channelNameField = (EditText) root.findViewById(R.id.dialog_join_channel_channel_name_field);

        OnClickListener buttonListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.dialog_join_channel_cancel_button) {
                    getDialog().dismiss();

                } else if (v.getId() == R.id.dialog_join_channel_join_button && !channelNameField.getText().toString().equals("")) {
                    String channelName = channelNameField.getText().toString().trim();
                    String networkSelected = (String) networkSpinner.getSelectedItem();
                    BusProvider.getInstance().post(new JoinChannelEvent(networkSelected, channelName));
                    dismiss();
                }
            }
        };
        root.findViewById(R.id.dialog_join_channel_join_button).setOnClickListener(buttonListener);
        root.findViewById(R.id.dialog_join_channel_cancel_button).setOnClickListener(buttonListener);

        return root;
    }

    public String[] getNetworkNames() {
        return getArguments().getStringArray("networks");
    }
}
