package com.iskrembilen.quasseldroid;

import android.app.Application;
import android.preference.PreferenceManager;

import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent;
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent.Status;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.iskrembilen.quasseldroid.util.ThemeUtil;
import com.squareup.otto.Subscribe;

public class Quasseldroid extends Application {
    public static boolean connected;

    @Override
    public void onCreate() {
        super.onCreate();
        connected = false;
        //Populate the preferences with default vaules if this has not been done before
        PreferenceManager.setDefaultValues(this, R.layout.preferences, true);
        //Load current theme
        ThemeUtil.initTheme(this);
        BusProvider.getInstance().register(this);
    }

    @Subscribe
    public void onConnectionChanged(ConnectionChangedEvent event) {
        if (event.status == Status.Disconnected) {
            connected = false;
        } else {
            connected = true;
        }
    }
}
