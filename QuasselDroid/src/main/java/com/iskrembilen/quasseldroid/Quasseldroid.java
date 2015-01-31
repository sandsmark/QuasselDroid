package com.iskrembilen.quasseldroid;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent;
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent.Status;
import com.iskrembilen.quasseldroid.protocol.state.Client;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.iskrembilen.quasseldroid.util.ThemeUtil;
import com.squareup.otto.Subscribe;

public class Quasseldroid extends Application {
    public Bundle savedInstanceState;
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        //Populate the preferences with default values if this has not been done before
        PreferenceManager.setDefaultValues(this, R.xml.preference_appearance, true);
        PreferenceManager.setDefaultValues(this, R.xml.preference_connection, true);
        PreferenceManager.setDefaultValues(this, R.xml.preference_notification, true);

        //Load current theme
        ThemeUtil.initTheme(this);
<<<<<<< HEAD
        BusProvider.getInstance().register(this);
        context = this;
    }

    @Subscribe
    public void onConnectionChanged(ConnectionChangedEvent event) {
        Log.d(TAG, "Changing connection status to: " + event.status);
        status = event.status;
=======
>>>>>>> Reworked the protcol, added automated syncing
    }
}
