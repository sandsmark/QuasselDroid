package com.iskrembilen.quasseldroid.gui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.iskrembilen.quasseldroid.R;

public class TopicViewDialog extends DialogFragment {

    private static final String TAG = TopicViewDialog.class.getSimpleName();

    private CharSequence topic;
    private String name;
    private int id;

    protected TextView topicField;

    public static TopicViewDialog newInstance(CharSequence topic, String name, int id) {
        TopicViewDialog fragment = new TopicViewDialog();
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
    public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
        topic = getArguments().getCharSequence("topic");
        name = getArguments().getString("name");
        id = getArguments().getInt("id");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialog = getActivity().getLayoutInflater().inflate(R.layout.dialog_topic_view, null);
        topicField = (TextView) dialog.findViewById(R.id.dialog_topic_text);
        topicField.setText(topic);

        builder.setView(dialog).setTitle(name);
        builder.setPositiveButton(getString(R.string.dialog_action_close),new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNeutralButton(getString(R.string.dialog_action_edit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TopicEditDialog.newInstance(topic,name,id).show(getFragmentManager(),TAG);
            }
        });
        return builder.create();
    }
}
