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
import android.widget.EditText;

import com.iskrembilen.quasseldroid.Identity;
import com.iskrembilen.quasseldroid.IdentityCollection;
import com.iskrembilen.quasseldroid.R;

import java.util.List;

public class EditNickDialog extends DialogFragment {

    private static final String TAG = EditNickDialog.class.getSimpleName();

    private int identityId;
    private int pos;
    private boolean add;

    private OnResultListener<String> listener;

    protected EditText editText;

    public static EditNickDialog newInstance(int pos, int identityId) {
        EditNickDialog fragment = new EditNickDialog();
        Bundle args = new Bundle();
        args.putInt("pos", pos);
        args.putInt("identityId", identityId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
    }

    @Override
    public @NonNull Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        pos = getArguments().getInt("pos");
        identityId = getArguments().getInt("identityId");
        add = pos==-1;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialog = inflater.inflate(R.layout.dialog_simple_edit, null);
        final EditText editText = (EditText) dialog.findViewById(R.id.dialog_simple_text);
        editText.setHint(R.string.hint_nick_edit);
        this.editText = editText;

        if (!add)
            editText.setText(IdentityCollection.getInstance().getIdentity(identityId).getNicks().get(pos));

        if (add)
            builder.setTitle(R.string.dialog_title_nick_add);
        else
            builder.setTitle(R.string.dialog_title_nick_edit);

        builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton(getString(R.string.dialog_action_save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener!=null)
                    listener.onClick(editText.getText().toString());
                dialog.dismiss();
            }
        });
        builder.setView(dialog);
        return builder.create();
    }

    public void setOnResultListener(OnResultListener<String> listener) {
        this.listener = listener;
    }

    public static interface OnResultListener<T> {
        public void onClick(T result);
    }
}
