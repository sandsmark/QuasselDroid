package com.iskrembilen.quasseldroid.gui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.iskrembilen.quasseldroid.BuildConfig;
import com.iskrembilen.quasseldroid.R;

public class AboutDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialog = getActivity().getLayoutInflater().inflate(R.layout.dialog_about, null);

        ((TextView) dialog.findViewById(R.id.version_field)).setText(BuildConfig.VERSION_NAME);

        builder.setView(dialog)
               .setCancelable(true);
        return builder.create();
    }
}
