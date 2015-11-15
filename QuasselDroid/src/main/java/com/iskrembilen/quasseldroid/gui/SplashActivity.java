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

import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent;
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent.Status;
import com.iskrembilen.quasseldroid.service.InFocus;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.iskrembilen.quasseldroid.util.ThemeUtil;
import com.squareup.otto.Subscribe;

public class SplashActivity extends Activity {
    // Set the display time, in milliseconds (or extract it out as a configurable parameter)
    private final int SPLASH_DISPLAY_LENGTH = 0;
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
        setTheme(ThemeUtil.themeNoActionBar);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_splash);
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
