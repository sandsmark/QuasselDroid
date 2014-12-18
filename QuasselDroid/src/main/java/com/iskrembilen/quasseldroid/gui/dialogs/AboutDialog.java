package com.iskrembilen.quasseldroid.gui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import com.iskrembilen.quasseldroid.BuildConfig;
import com.iskrembilen.quasseldroid.R;

public class AboutDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialog = inflater.inflate(R.layout.dialog_about, null);
        LinearLayout authorList = (LinearLayout) dialog.findViewById(R.id.authors);

        String[] author_names = getResources().getStringArray(R.array.author_names);
        String[] author_descriptions = getResources().getStringArray(R.array.author_descriptions);

        View item;
        for (int i = 0; i < author_names.length; i++ ) {
            item = inflater.inflate(R.layout.widget_author,null);
            ((TextView) item.findViewById(R.id.text1)).setText(author_names[i]);
            ((TextView) item.findViewById(R.id.text2)).setText(author_descriptions[i]);
            authorList.addView(item);
        }

        ((TextView) dialog.findViewById(R.id.version_field)).setText(BuildConfig.VERSION_NAME);

        builder.setView(dialog)
               .setCancelable(true);
        return builder.create();
    }
}
