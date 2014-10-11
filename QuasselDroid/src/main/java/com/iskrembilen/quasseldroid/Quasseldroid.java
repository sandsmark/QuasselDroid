package com.iskrembilen.quasseldroid;

import android.app.Application;
import android.preference.PreferenceManager;
import android.util.Log;

import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent;
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent.Status;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.iskrembilen.quasseldroid.util.ThemeUtil;
import com.squareup.otto.Subscribe;

public class Quasseldroid extends Application {
    private static final String TAG = Quasseldroid.class.getSimpleName();
    public static Status status;

    @Override
    public void onCreate() {
        super.onCreate();
        status = Status.Disconnected;
        //Populate the preferences with default values if this has not been done before
        PreferenceManager.setDefaultValues(this, R.layout.data_preferences, true);
        //Load current theme
        ThemeUtil.initTheme(this);
        BusProvider.getInstance().register(this);
    }

    @Subscribe
    public void onConnectionChanged(ConnectionChangedEvent event) {
        Log.d(TAG, "Changing connection status to: " + event.status);
        status = event.status;
    }
}
