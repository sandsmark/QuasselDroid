package com.iskrembilen.quasseldroid.gui.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.iskrembilen.quasseldroid.R;

public class LoginProgressDialog extends DialogFragment {
    private Callbacks callbacks;

    public static LoginProgressDialog newInstance() {
        return new LoginProgressDialog();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new ClassCastException("Activity must implement fragment's callbacks.");
        }

        callbacks = (Callbacks) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog prog = new ProgressDialog(getActivity());
        prog.setMessage(getResources().getString(R.string.notification_connecting));
        setCancelable(true);
        return prog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        callbacks.onCanceled();
    }

    public interface Callbacks {
        public void onCanceled();

    }
}
