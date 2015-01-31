package com.iskrembilen.quasseldroid.gui.settings;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.util.ThemeUtil;

import java.util.List;

import de.kuschku.uilib.preferences.ActionBarPreferenceActivity;

public class SettingsActivity extends ActionBarPreferenceActivity implements Toolbar.OnMenuItemClickListener{
    CharSequence parentTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeUtil.theme);
        parentTitle = getResources().getString(R.string.action_preferences);

        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean isValidFragment(String fragmentName){
        return true;
    }

    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }
}
