package com.iskrembilen.quasseldroid.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.iskrembilen.quasseldroid.R;

public class InFocus extends Service {

    protected Intent inFocusIntent;
    protected SharedPreferences pref;
    protected SharedPreferences.Editor prefEditor;

    @Override
    public void onCreate() {
        super.onCreate();

        inFocusIntent = new Intent(this.getString(R.string.has_focus) + "_changed");
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        prefEditor = pref.edit();

        onFocus();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(screenStateChanged, intentFilter);

    }

    private void onFocus() {
        prefEditor.putBoolean(this.getString(R.string.has_focus), true);
        prefEditor.commit();

        inFocusIntent.putExtra(this.getString(R.string.has_focus), true);
        sendBroadcast(inFocusIntent);
    }

    private void onUnfocus() {
        prefEditor.putBoolean(this.getString(R.string.has_focus), false);
        prefEditor.commit();

        inFocusIntent.putExtra(this.getString(R.string.has_focus), false);
        sendBroadcast(inFocusIntent);
    }

    private BroadcastReceiver screenStateChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                onFocus();
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                onUnfocus();
            }
        }
    };

    @Override
    public boolean onUnbind(Intent intent) {
        onUnfocus();
        unregisterReceiver(screenStateChanged);
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        InFocus getService() {
            return InFocus.this;
        }
    }

    private final IBinder binder = new LocalBinder();

}