package com.iskrembilen.quasseldroid.gui.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.iskrembilen.quasseldroid.protocol.state.Client;
import com.iskrembilen.quasseldroid.protocol.state.IgnoreListManager;

public class IgnoreListFragment extends PreferenceFragment {

    ListView list;
    ListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        for (IgnoreListManager.IgnoreListItem item : Client.getInstance().getIgnoreListManager().getIgnoreList()) {
            SwitchPreference pref = new SwitchPreference(this);
            pref.setTitle(item.getRule());
            pref.setChecked(item.isActive());
            screen.addPreference(pref);
        }

        setPreferenceScreen(screen);*/
    }


}
