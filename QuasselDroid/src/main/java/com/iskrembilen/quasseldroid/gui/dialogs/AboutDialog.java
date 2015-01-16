package com.iskrembilen.quasseldroid.gui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iskrembilen.quasseldroid.BuildConfig;
import com.iskrembilen.quasseldroid.R;

public class AboutDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialog = inflater.inflate(R.layout.dialog_about, null);
        LinearLayout authorList = (LinearLayout) dialog.findViewById(R.id.authors);

        View github = dialog.findViewById(R.id.link_github);
        View community = dialog.findViewById(R.id.link_community);
        github.getBackground().setColorFilter(getResources().getColor(R.color.material_blue_grey_900), PorterDuff.Mode.SRC_IN);
        github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.app_link_github))));
            }
        });
        community.getBackground().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_IN);
        community.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.app_link_community))));
            }
        });

        String[] authorNames = getResources().getStringArray(R.array.author_names);
        String[] authorDescriptions = getResources().getStringArray(R.array.author_descriptions);

        View item;
        for (int i = 0; i < authorNames.length; i++ ) {
            item = inflater.inflate(R.layout.widget_author,null);
            ((TextView) item.findViewById(R.id.text1)).setText(authorNames[i]);
            ((TextView) item.findViewById(R.id.text2)).setText(authorDescriptions[i]);
            authorList.addView(item);
        }

        ((TextView) dialog.findViewById(R.id.version_field)).setText(BuildConfig.VERSION_NAME);

        builder.setView(dialog)
               .setCancelable(true);
        return builder.create();
    }
}
