package com.iskrembilen.quasseldroid.gui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.iskrembilen.quasseldroid.Buffer;
import com.iskrembilen.quasseldroid.IrcMessage;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.FilterMessagesEvent;
import com.iskrembilen.quasseldroid.util.BusProvider;

import java.util.ArrayList;

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
                
                // If the type is a join or quit, additionally apply the same action to the Netsplit versions
                if (type == IrcMessage.Type.Join)
                    BusProvider.getInstance().post(new FilterMessagesEvent(getBufferId(), IrcMessage.Type.NetsplitJoin, isChecked));
                else if (type == IrcMessage.Type.Quit)
                    BusProvider.getInstance().post(new FilterMessagesEvent(getBufferId(), IrcMessage.Type.NetsplitQuit, isChecked));

                BusProvider.getInstance().post(new FilterMessagesEvent(getBufferId(), type, isChecked));
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
