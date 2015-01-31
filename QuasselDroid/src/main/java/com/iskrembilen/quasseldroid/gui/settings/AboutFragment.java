package com.iskrembilen.quasseldroid.gui.settings;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import de.kuschku.uilib.preferences.SimplePreferenceFragment;

public class AboutFragment extends SimplePreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            PackageInfo pinfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            getPreferenceScreen().getPreferenceManager().findPreference("version").setSummary(pinfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
