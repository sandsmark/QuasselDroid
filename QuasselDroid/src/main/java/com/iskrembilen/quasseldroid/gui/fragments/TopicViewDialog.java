package com.iskrembilen.quasseldroid.gui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.SendMessageEvent;
import com.iskrembilen.quasseldroid.util.BusProvider;

public class TopicViewDialog extends DialogFragment {

    private static final String TAG = TopicViewDialog.class.getSimpleName();

    private String topic;
    private int id;

    protected TextView topicField;

    public static TopicViewDialog newInstance(String topic, int id) {
        TopicViewDialog fragment = new TopicViewDialog();
        Bundle args = new Bundle();
        args.putString("topic", topic);
        args.putInt("id", id);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        topic = getArguments().getString("topic");
        id = getArguments().getInt("id");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        topic = getArguments().getString("topic");
        id = getArguments().getInt("id");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialog = getActivity().getLayoutInflater().inflate(R.layout.dialog_topic_view, null);
        topicField = (TextView) dialog.findViewById(R.id.dialog_topic_text);
        topicField.setText(topic);

        builder.setView(dialog).setTitle("Channel Topic");
        builder.setPositiveButton(getString(R.string.dialog_action_close),new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNeutralButton(getString(R.string.dialog_action_edit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TopicEditDialog.newInstance(topic,id).show(getFragmentManager(),TAG);
            }
        });
        return builder.create();
    }
}
