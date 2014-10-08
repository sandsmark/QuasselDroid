package com.iskrembilen.quasseldroid.gui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.JoinChannelEvent;
import com.iskrembilen.quasseldroid.util.BusProvider;

import org.w3c.dom.Text;

public class TopicDialog extends DialogFragment {

    private static final String TAG = TopicDialog.class.getSimpleName();

    private String topic;
    private String title;

    private TextView topicField;

    public static TopicDialog newInstance(String title, String topic) {
        TopicDialog fragment = new TopicDialog();
        Bundle args = new Bundle();
        args.putString("topic", topic);
        args.putString("title", title);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        topic = getArguments().getString("topic");
        title = getArguments().getString("title");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        topic = getArguments().getString("topic");
        title = getArguments().getString("title");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialog = getActivity().getLayoutInflater().inflate(R.layout.dialog_topic, null);
        TextView topicField = (TextView) dialog.findViewById(R.id.dialog_topic_text);
        topicField.setText(topic);

        Log.d(TAG,title+topic);

        builder.setView(dialog).setTitle(title);
        return builder.create();
    }
}
