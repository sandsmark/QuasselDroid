package com.iskrembilen.quasseldroid.gui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.iskrembilen.quasseldroid.Buffer;
import com.iskrembilen.quasseldroid.BufferInfo;
import com.iskrembilen.quasseldroid.NetworkCollection;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.BufferDetailsChangedEvent;
import com.iskrembilen.quasseldroid.util.BusProvider;
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
        if (event.bufferId == id) {
            mDialog.setTitle(mBuffer.getInfo().name);
            setTopic(mBuffer.getTopic());
        }
    }

    public void setTopic(CharSequence topic) {
        ((TextView) getDialog().findViewById(R.id.dialog_topic_text)).setText(topic);
    }

    @Override
    public Dialog getDialog() {
        return mDialog;
    }

    @Override
    public @NonNull Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        id = getArguments().getInt("id");
        mBuffer = NetworkCollection.getInstance().getBufferById(id);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_topic_view, null);
        TextView topicField = (TextView) view.findViewById(R.id.dialog_topic_text);
        topicField.setText(mBuffer.getTopic());

        builder.setView(view)
                .setTitle(mBuffer.getInfo().name)
                .setPositiveButton(getString(R.string.dialog_action_close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNeutralButton(getString(R.string.dialog_action_edit), new DialogInterface.OnClickListener() {
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
