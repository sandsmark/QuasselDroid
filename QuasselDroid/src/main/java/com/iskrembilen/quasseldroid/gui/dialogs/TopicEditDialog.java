package com.iskrembilen.quasseldroid.gui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.SendMessageEvent;
import com.iskrembilen.quasseldroid.util.BusProvider;

public class TopicEditDialog extends DialogFragment {

    private static final String TAG = TopicEditDialog.class.getSimpleName();

    private String topic;
    private int id;

    protected EditText topicField;

    public static TopicEditDialog newInstance(String topic, int id) {
        TopicEditDialog fragment = new TopicEditDialog();
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

        View dialog = getActivity().getLayoutInflater().inflate(R.layout.dialog_topic_edit, null);
        final EditText topicField = (EditText) dialog.findViewById(R.id.dialog_topic_text);
        this.topicField = topicField;
        topicField.setText(topic);

        builder.setView(dialog).setTitle("Channel Topic");
        builder.setPositiveButton(getString(R.string.dialog_action_close),new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNeutralButton(getString(R.string.dialog_action_save),new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BusProvider.getInstance().post(new SendMessageEvent(id, "/topic "+topicField.getText().toString()));
            }
        });
        return builder.create();
    }
}
