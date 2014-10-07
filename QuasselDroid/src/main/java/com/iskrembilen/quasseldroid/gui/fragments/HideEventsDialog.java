package com.iskrembilen.quasseldroid.gui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;

import java.util.ArrayList;

import com.iskrembilen.quasseldroid.Buffer;
import com.iskrembilen.quasseldroid.IrcMessage;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.FilterMessagesEvent;
import com.iskrembilen.quasseldroid.util.BusProvider;

public class HideEventsDialog extends DialogFragment {

    public static HideEventsDialog newInstance(Buffer buffer) {
        HideEventsDialog fragment = new HideEventsDialog();

        String[] filterList = IrcMessage.Type.getFilterList();
        boolean[] checked = new boolean[filterList.length];
        ArrayList<IrcMessage.Type> filters = buffer.getFilters();
        for (int i = 0; i < checked.length; i++) {
            if (filters.contains(IrcMessage.Type.valueOf(filterList[i]))) {
                checked[i] = true;
            } else {
                checked[i] = false;
            }
        }

        Bundle args = new Bundle();
        args.putStringArray("filterlist", filterList);
        args.putBooleanArray("checked", checked);
        args.putInt("bufferid", buffer.getInfo().id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.dialog_title_events));

        builder.setMultiChoiceItems(getFilterList(), getCheckedList(), new OnMultiChoiceClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                IrcMessage.Type type = IrcMessage.Type.valueOf(IrcMessage.Type.getFilterList()[which]);
                if (isChecked)
                    BusProvider.getInstance().post(new FilterMessagesEvent(getBufferId(), type, true));
                else
                    BusProvider.getInstance().post(new FilterMessagesEvent(getBufferId(), type, false));
            }
        });
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        return builder.create();
    }

    private boolean[] getCheckedList() {
        return getArguments().getBooleanArray("checked");
    }

    private String[] getFilterList() {
        return getArguments().getStringArray("filterlist");
    }

    private int getBufferId() {
        return getArguments().getInt("bufferid");
    }

}
