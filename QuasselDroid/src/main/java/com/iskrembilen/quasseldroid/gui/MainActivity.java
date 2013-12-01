/*
    QuasselDroid - Quassel client for Android
 	Copyright (C) 2011 Ken Børge Viktil
 	Copyright (C) 2011 Magnus Fjell
 	Copyright (C) 2011 Martin Sandsmark <martin.sandsmark@kde.org>

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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.iskrembilen.quasseldroid.Buffer;
import com.iskrembilen.quasseldroid.BufferInfo;
import com.iskrembilen.quasseldroid.NetworkCollection;
import com.iskrembilen.quasseldroid.Quasseldroid;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.BufferOpenedEvent;
import com.iskrembilen.quasseldroid.events.BufferRemovedEvent;
import com.iskrembilen.quasseldroid.events.CompleteNickEvent;
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent;
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent.Status;
import com.iskrembilen.quasseldroid.events.DisconnectCoreEvent;
import com.iskrembilen.quasseldroid.events.InitProgressEvent;
import com.iskrembilen.quasseldroid.events.LatencyChangedEvent;
import com.iskrembilen.quasseldroid.events.UpdateReadBufferEvent;
import com.iskrembilen.quasseldroid.gui.fragments.BufferFragment;
import com.iskrembilen.quasseldroid.gui.fragments.ChatFragment;
import com.iskrembilen.quasseldroid.gui.fragments.ConnectingFragment;
import com.iskrembilen.quasseldroid.gui.fragments.NickListFragment;
import com.iskrembilen.quasseldroid.service.InFocus;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.iskrembilen.quasseldroid.util.Helper;
import com.iskrembilen.quasseldroid.util.ThemeUtil;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.lang.reflect.Field;

public class MainActivity extends SherlockFragmentActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String BUFFER_ID_EXTRA = "bufferid";
    public static final String BUFFER_NAME_EXTRA = "buffername";
    private static final long BACK_THRESHOLD = 4000;
    private static final String DRAWER_BUFFER_OPEN = "bufferdrawer";
    private static final String DRAWER_NICKS_OPEN = "nicksdrawer";

    SharedPreferences preferences;
    OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

    private DrawerLayout drawer;
    private ActionBarDrawerToggle drawerToggle;

    private int currentTheme;
    private Boolean showLag = false;

    private Fragment chatFragment;
    private Fragment bufferFragment;
    private Fragment nickFragment;

    private int openedBuffer = -1;
    private boolean isDrawerOpen = false;

    private long lastBackPressed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MainActivity created");
        setTheme(ThemeUtil.theme);
        super.onCreate(savedInstanceState);
        currentTheme = ThemeUtil.theme;
        setContentView(R.layout.main_layout);

        drawer = (DrawerLayout) findViewById(R.id.drawer);

        try {
            Field mLeftDragger = drawer.getClass().getDeclaredField("mLeftDragger");
            mLeftDragger.setAccessible(true);
            ViewDragHelper leftDraggerObj = (ViewDragHelper) mLeftDragger.get(drawer);
            Field mLeftEdgeSize = leftDraggerObj.getClass().getDeclaredField("mEdgeSize");
            mLeftEdgeSize.setAccessible(true);
            int leftEdge = mLeftEdgeSize.getInt(leftDraggerObj);
            mLeftEdgeSize.setInt(leftDraggerObj, leftEdge * 3);

            Field mRightDragger = drawer.getClass().getDeclaredField("mRightDragger");
            mRightDragger.setAccessible(true);
            ViewDragHelper rightDraggerObj = (ViewDragHelper) mRightDragger.get(drawer);
            Field mRightEdgeSize = rightDraggerObj.getClass().getDeclaredField("mEdgeSize");
            mRightEdgeSize.setAccessible(true);
            int rightEdge = mRightEdgeSize.getInt(rightDraggerObj);
            mRightEdgeSize.setInt(rightDraggerObj, rightEdge * 3);
        } catch (Exception e) {
            Log.e(TAG, "Setting the draggable zone for the drawers failed!", e);
        }

        if (savedInstanceState != null) {
            Log.d(TAG, "MainActivity has savedInstanceState");
            openedBuffer = savedInstanceState.getInt(BUFFER_ID_EXTRA);
            isDrawerOpen = savedInstanceState.getBoolean(DRAWER_BUFFER_OPEN);
            if (savedInstanceState.getBoolean(DRAWER_NICKS_OPEN)) drawer.openDrawer(Gravity.RIGHT);
            FragmentManager manager = getSupportFragmentManager();
            bufferFragment = manager.findFragmentById(R.id.left_drawer);
            nickFragment = manager.findFragmentById(R.id.right_drawer);
            Fragment fragment = manager.findFragmentById(R.id.main_content_container);
            if (fragment.getClass() == ChatFragment.class) {
                chatFragment = fragment;
            }
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        showLag = preferences.getBoolean(getString(R.string.preference_show_lag), false);

        drawerToggle = new ActionBarDrawerToggle(
                this,                   /* host Activity */
                drawer,                 /* DrawerLayout object */
                R.drawable.ic_drawer,   /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,   /* "open drawer" description */
                R.string.drawer_close   /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View drawerView) {
                Log.d(TAG, "drawer closed");
                Fragment drawerFragment = getSupportFragmentManager().findFragmentById(drawerView.getId());
                if (drawerFragment != null) drawerFragment.setMenuVisibility(false);
                if (chatFragment != null) chatFragment.setMenuVisibility(true);

                if (openedBuffer != -1) {
                    NetworkCollection networks = NetworkCollection.getInstance();
                    Buffer buffer = networks.getBufferById(openedBuffer);
                    if (buffer != null) {
                        if (buffer.getInfo().type == BufferInfo.Type.StatusBuffer)
                            getSupportActionBar().setTitle(networks.getNetworkById(buffer.getInfo().networkId).getName());
                        else
                            getSupportActionBar().setTitle(buffer.getInfo().name);
                    }
                } else {
                    getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
                    invalidateOptionsMenu();
                }
                if (drawerView.getId() == R.id.left_drawer && openedBuffer != -1) {
                    chatFragment.setUserVisibleHint(true);
                    BusProvider.getInstance().post(new UpdateReadBufferEvent());
                }
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                if (drawerView.getId() == R.id.left_drawer && openedBuffer != -1) {
                    if (chatFragment != null) chatFragment.setUserVisibleHint(false);
                }
                Fragment drawerFragment = getSupportFragmentManager().findFragmentById(drawerView.getId());
                if (drawerFragment != null) drawerFragment.setMenuVisibility(true);
                if (chatFragment != null) chatFragment.setMenuVisibility(false);

                if (bufferFragment != null) {
                    hideKeyboard(bufferFragment.getView());
                }

                getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
                invalidateOptionsMenu();
            }
        };

        // Set the drawer toggle as the DrawerListener
        drawer.setDrawerListener(drawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        sharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(getResources().getString(R.string.preference_show_lag))) {
                    showLag = preferences.getBoolean(getString(R.string.preference_show_lag), false);
                    if (!showLag) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            setActionBarSubtitle("");
                        } else {
                            getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

                        }
                    }
                }

            }
        };
        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener); //To avoid GC issues
    }

    private void setActionBarSubtitle(String subtitle) {
        getSupportActionBar().setSubtitle(subtitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
        isDrawerOpen = drawer.isDrawerOpen(Gravity.LEFT);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Activity onStart");
        bindService(new Intent(this, InFocus.class), focusConnection, Context.BIND_AUTO_CREATE);
        if (ThemeUtil.theme != currentTheme) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        if (Quasseldroid.status == Status.Disconnected) {
            returnToLogin();
        } else if (Quasseldroid.status == Status.Connecting) {
            showInitProgress();
        } else {
            if (isDrawerOpen && bufferFragment != null) {
                drawer.openDrawer(Gravity.LEFT);
                if (chatFragment != null) {
                    chatFragment.setUserVisibleHint(false);
                }
            } else {
                drawer.closeDrawer(Gravity.LEFT);
            }
        }

        NetworkCollection networks = NetworkCollection.getInstance();
        if (openedBuffer != -1 && networks != null && networks.getBufferById(openedBuffer) == null) {
            openedBuffer = -1;
            BusProvider.getInstance().post(new BufferOpenedEvent(-1, false));
            drawer.closeDrawer(Gravity.RIGHT);
            drawer.openDrawer(Gravity.LEFT);
            return;
        }
        if (isDrawerOpen && bufferFragment != null) {
            drawer.openDrawer(Gravity.LEFT);
            if (chatFragment != null) chatFragment.setUserVisibleHint(false);
        } else drawer.closeDrawer(Gravity.LEFT);
        hideKeyboard(drawer);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isDrawerOpen = drawer.isDrawerOpen(Gravity.LEFT);
        BusProvider.getInstance().unregister(this);

    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Stopping activity");
        super.onStop();
        unbindService(focusConnection);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Destroying activity");
        preferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "Saving instance state");
        outState.putInt(BUFFER_ID_EXTRA, openedBuffer);
        outState.putBoolean(DRAWER_BUFFER_OPEN, drawer.isDrawerOpen(Gravity.LEFT));
        outState.putBoolean(DRAWER_NICKS_OPEN, drawer.isDrawerOpen(Gravity.RIGHT));
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "Configuration changed");
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBackPressed < BACK_THRESHOLD) super.onBackPressed();
        else {
            Toast.makeText(this, getString(R.string.pressed_back_toast), Toast.LENGTH_SHORT).show();
            lastBackPressed = currentTime;
        }
    }

    @Override
    public boolean onSearchRequested() {
        BusProvider.getInstance().post(new CompleteNickEvent());
        return false; //Activity ate the request
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.base_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawer.isDrawerOpen(Gravity.LEFT)) {
                    drawer.closeDrawer(Gravity.LEFT);
                } else {
                    drawer.openDrawer(Gravity.LEFT);
                }
                return true;
            case R.id.menu_preferences:
                Intent i = new Intent(MainActivity.this, PreferenceView.class);
                startActivity(i);
                return true;
            case R.id.menu_disconnect:
                BusProvider.getInstance().post(new DisconnectCoreEvent());
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideKeyboard(View view) {
        if (view != null) {
            view.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        }
    }

    @Subscribe
    public void onInitProgressed(InitProgressEvent event) {
        FragmentManager manager = getSupportFragmentManager();
        Fragment currentFragment = manager.findFragmentById(R.id.main_content_container);
        if (event.done) {
            if (currentFragment == null || currentFragment.getClass() != ChatFragment.class) {
                chatFragment = ChatFragment.newInstance();
                nickFragment = NickListFragment.newInstance();
                bufferFragment = BufferFragment.newInstance();

                FragmentTransaction trans = manager.beginTransaction();
                if (currentFragment != null) {
                    trans.remove(currentFragment);
                }
                trans.add(R.id.main_content_container, chatFragment);

                //Initialize the buffer drawer
                trans.add(R.id.left_drawer, bufferFragment);
                drawer.openDrawer(Gravity.LEFT);
                isDrawerOpen = true;

                //Initialize the nick drawer
                trans.add(R.id.right_drawer, nickFragment);
                trans.commit();
            }
        } else if (currentFragment == null || currentFragment.getClass() != ConnectingFragment.class) {
            Log.d(TAG, "Showing progress");
            showInitProgress();
        }
    }

    @Subscribe
    public void onLatencyChanged(LatencyChangedEvent event) {
        if (showLag) {
            if (event.latency > 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    setActionBarSubtitle(Helper.formatLatency(event.latency, getResources()));
                } else {
                    setTitle(getResources().getString(R.string.app_name) + " - "
                            + Helper.formatLatency(event.latency, getResources()));
                }
            }
        }
    }

    @Subscribe
    public void onConnectionChanged(ConnectionChangedEvent event) {
        if (event.status == Status.Disconnected) {
            if (event.reason != "") {
                removeDialog(R.id.DIALOG_CONNECTING);
                Toast.makeText(MainActivity.this.getApplicationContext(), event.reason, Toast.LENGTH_LONG).show();

            }
            returnToLogin();
        }
    }

    private void returnToLogin() {
        finish();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void showInitProgress() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        ConnectingFragment fragment = ConnectingFragment.newInstance();
        fragmentTransaction.replace(R.id.main_content_container, fragment, "init");
        if (bufferFragment != null)
            fragmentTransaction.remove(bufferFragment);
        if (nickFragment != null)
            fragmentTransaction.remove(nickFragment);
        drawer.closeDrawers();
        fragmentTransaction.commit();
    }

    @Subscribe
    public void onBufferOpened(BufferOpenedEvent event) {
        if (event.bufferId != -1) {
            openedBuffer = event.bufferId;
            if (event.switchToBuffer) {
                drawer.closeDrawers();
            }
        }
    }

    @Produce
    public BufferOpenedEvent produceBufferOpenedEvent() {
        return new BufferOpenedEvent(openedBuffer);
    }

    @Subscribe
    public void onBufferRemoved(BufferRemovedEvent event) {
        if (event.bufferId == openedBuffer) {
            openedBuffer = -1;
            BusProvider.getInstance().post(new BufferOpenedEvent(-1, false));
            drawer.closeDrawer(Gravity.RIGHT);
            drawer.openDrawer(Gravity.LEFT);
        }
    }

    private ServiceConnection focusConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName cn, IBinder service) {
        }

        public void onServiceDisconnected(ComponentName cn) {
        }
    };
}
