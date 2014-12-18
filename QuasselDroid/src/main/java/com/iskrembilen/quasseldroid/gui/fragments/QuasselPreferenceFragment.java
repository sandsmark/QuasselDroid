package com.iskrembilen.quasseldroid.gui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.util.ThemeUtil;

public class QuasselPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.data_preferences);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
