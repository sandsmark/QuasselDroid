package com.iskrembilen.quasseldroid.gui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.squareup.otto.Subscribe;

import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent;
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent.Status;
import com.iskrembilen.quasseldroid.service.InFocus;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.iskrembilen.quasseldroid.util.ThemeUtil;

public class SplashActivity extends Activity {
    // Set the display time, in milliseconds (or extract it out as a configurable parameter)
    private final int SPLASH_DISPLAY_LENGTH = 500;
    private final String TAG = SplashActivity.class.getSimpleName();
    private Class<?> activityToStart;
    private ServiceConnection focusConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName cn, IBinder service) {
        }

        public void onServiceDisconnected(ComponentName cn) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeUtil.theme);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash);
        getActionBar().hide();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, InFocus.class), focusConnection, Context.BIND_AUTO_CREATE);
        BusProvider.getInstance().register(this);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (activityToStart != null)
                    startActivity(activityToStart);
                else startActivity(LoginActivity.class);
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(focusConnection);
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onBackPressed() {
        //Eat back press not allowed here
    }

    private void startActivity(Class<?> activity) {
        Intent mainIntent = new Intent(SplashActivity.this, activity);
        startActivity(mainIntent);
        finish();
    }

    @Subscribe
    public void onConnectionChanged(ConnectionChangedEvent event) {
        Log.i(TAG, "COOOn");
        if (event.status == Status.Connected || event.status == Status.Connecting) {
            activityToStart = MainActivity.class;
        } else {
            activityToStart = LoginActivity.class;
        }
    }
}
