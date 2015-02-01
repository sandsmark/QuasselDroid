/*
    QuasselDroid - Quassel client for Android
    Copyright (C) 2015 Ken Børge Viktil
    Copyright (C) 2015 Magnus Fjell
    Copyright (C) 2015 Martin Sandsmark <martin.sandsmark@kde.org>

    This program is free software: you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation, either version 3 of the License, or (at your option)
    any later version, or under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either version 2.1 of
    the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License and the
    GNU Lesser General Public License along with this program.  If not, see
    <http://www.gnu.org/licenses/>.
 */

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