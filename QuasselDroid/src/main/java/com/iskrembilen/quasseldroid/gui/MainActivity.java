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

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.iskrembilen.quasseldroid.gui.fragments.DetailFragment;
import com.iskrembilen.quasseldroid.gui.fragments.NickListFragment;
import com.iskrembilen.quasseldroid.gui.fragments.TopicDialog;
import com.iskrembilen.quasseldroid.service.InFocus;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.iskrembilen.quasseldroid.util.CustomDrawerToggle;
import com.iskrembilen.quasseldroid.util.Helper;
import com.iskrembilen.quasseldroid.util.ThemeUtil;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import org.w3c.dom.Text;

import java.lang.reflect.Field;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String BUFFER_ID_EXTRA = "bufferid";
    public static final String BUFFER_NAME_EXTRA = "buffername";
    private static final long BACK_THRESHOLD = 4000;
    private static final String OPENED_DRAWER = "opened_drawer";

    SharedPreferences preferences;
    OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

    private DrawerLayout drawer;
    private CustomDrawerToggle drawerToggle;

    private int currentTheme;
    private Boolean showLag = false;

    private int lag = 0;

    private Fragment chatFragment;
    private Fragment bufferFragment;
    private Fragment nickFragment;
    private Fragment detailFragment;

    private TextView title;
    private TextView subTitle;
    private boolean showSubtitle = false;
    public CharSequence subTitleSpan;
    private View actionTitleArea;

    private int openedBuffer = -1;
    private boolean isDrawerOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MainActivity created");
        setTheme(ThemeUtil.theme);
        super.onCreate(savedInstanceState);
        currentTheme = ThemeUtil.theme;
        setContentView(R.layout.main_layout);

        drawer = (DrawerLayout) findViewById(R.id.drawer);

        if (savedInstanceState != null) {
            Log.d(TAG, "MainActivity has savedInstanceState");
            openedBuffer = savedInstanceState.getInt(BUFFER_ID_EXTRA);
            if (drawer != null) openDrawer(savedInstanceState.getInt(OPENED_DRAWER));

            FragmentManager manager = getFragmentManager();
            bufferFragment = manager.findFragmentById(R.id.left_drawer);
            nickFragment = manager.findFragmentById(R.id.right_drawer);
            Fragment fragment = manager.findFragmentById(R.id.main_content_container);
            if (fragment.getClass() == ChatFragment.class) {
                chatFragment = fragment;
            }
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        showLag = preferences.getBoolean(getString(R.string.preference_show_lag), false);

        getActionBar().setHomeButtonEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(true);
        View actionBar = getLayoutInflater().inflate(R.layout.actionbar_messageview, null);

        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.findViewById(R.id.actionTitleArea).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                showDetailPopup();
            }
        });
        actionTitleArea = actionBar.findViewById(R.id.actionTitleArea);
        actionBar.findViewById(R.id.actionButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (getOpenDrawers()!=1) {
                    openDrawer(Gravity.LEFT);
                } else {
                    closeDrawer(Gravity.LEFT);
                }
            }
        });
        actionBar.findViewById(R.id.actionButton).setClickable(true);
        this.title = ((TextView)actionBar.findViewById(R.id.title));
        this.subTitle = ((TextView)actionBar.findViewById(R.id.subtitle));

        final ImageView upIndicator = (ImageView) actionBar.findViewById(R.id.upIndicator);

        drawerToggle = new CustomDrawerToggle(
                this,                   /* host Activity */
                drawer,                 /* DrawerLayout object */
                R.drawable.ic_drawer,   /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,   /* "open drawer" description */
                R.string.drawer_close   /* "close drawer" description */
        ) {

            public Canvas canvas;

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View drawerView) {
                setTitleAndMenu();
                invalidateOptionsMenu();

                if (drawerView.getId() == R.id.left_drawer && openedBuffer != -1) {
                    BusProvider.getInstance().post(new UpdateReadBufferEvent());
                }
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                setTitleAndMenu();
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                super.mSlider.invalidateSelf();
                upIndicator.setImageDrawable(drawerToggle.mSlider.getCurrent());
            }
        };


        drawer.setDrawerListener(drawerToggle);

        getActionBar().setCustomView(actionBar);

        sharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(getResources().getString(R.string.preference_show_lag))) {
                    showLag = preferences.getBoolean(getString(R.string.preference_show_lag), false);
                    updateSubtitle();
                }

            }
        };
        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener); //To avoid GC issues
    }

    private void showDetailPopup() {
        String topic = ((NickListFragment)nickFragment).topic;
        if (topic!=null) {
            TopicDialog.newInstance(topic).show(getFragmentManager(),TAG);
        }
    }

    private void setActionBarSubtitle(CharSequence content) {
        //getActionBar().setSubtitle(subtitle);
        if (content==null || content.toString().trim().equalsIgnoreCase("")) {
            this.subTitle.setVisibility(View.GONE);
            this.subTitle.setText(null);
        } else {
            this.subTitle.setVisibility(View.VISIBLE);
            this.subTitle.setText(content);
        }
    }

    public void setActionBarTitle(CharSequence content) {
        this.title.setText(content);
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
        Log.d(TAG, "Starting activity");
        super.onStart();

        bindService(new Intent(this, InFocus.class), focusConnection, Context.BIND_AUTO_CREATE);
        if (ThemeUtil.theme != currentTheme) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "Resuming activity");
        super.onResume();
        BusProvider.getInstance().register(this);
        if (Quasseldroid.status == Status.Disconnected) {
            returnToLogin();
        } else if (Quasseldroid.status != Status.Connecting) {
            if (isDrawerOpen && bufferFragment != null) {
                drawer.openDrawer(Gravity.LEFT);
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
        } else drawer.closeDrawer(Gravity.LEFT);
        hideKeyboard(drawer);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Pausing activity");
        isDrawerOpen = drawer.isDrawerOpen(Gravity.LEFT);
        BusProvider.getInstance().unregister(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Stopping activity");
        unbindService(focusConnection);
        super.onStop();
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
        outState.putInt(OPENED_DRAWER, getOpenDrawers());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "Configuration changed");
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onSearchRequested() {
        BusProvider.getInstance().post(new CompleteNickEvent());
        return false; //Activity ate the request
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawer.closeDrawer(Gravity.RIGHT);
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
        FragmentManager manager = getFragmentManager();
        Fragment currentFragment = manager.findFragmentById(R.id.main_content_container);
        if (event.done) {
            if (currentFragment == null || currentFragment.getClass() != ChatFragment.class) {
                chatFragment = ChatFragment.newInstance();
                nickFragment = NickListFragment.newInstance();
                detailFragment = DetailFragment.newInstance();
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

            setTitleAndMenu();
            invalidateOptionsMenu();
        } else if (currentFragment == null || currentFragment.getClass() != ConnectingFragment.class) {
            Log.d(TAG, "Showing progress");
            showInitProgress();
        }
    }

    @Subscribe
    public void onLatencyChanged(LatencyChangedEvent event) {
        if (event.latency > 0) {
            lag = event.latency;
            updateSubtitle();
        }
    }

    public void updateSubtitle() {
        if (showLag && showSubtitle && subTitleSpan!=null && subTitleSpan.toString().trim()!="") {
            setActionBarSubtitle(TextUtils.concat(Helper.formatLatency(lag, getResources()), " — ", subTitleSpan));
        } else if (showLag) {
            setActionBarSubtitle(Helper.formatLatency(lag, getResources()));
        } else if (showSubtitle) {
            setActionBarSubtitle(subTitleSpan);
        } else {
            setActionBarSubtitle(null);
        }
    }

    @Subscribe
    public void onConnectionChanged(ConnectionChangedEvent event) {
        if (event.status == Status.Disconnected) {
            Log.d(TAG, "Connection status is disconnected");
            if (event.reason != "") {
                removeDialog(R.id.DIALOG_CONNECTING);
                Toast.makeText(MainActivity.this.getApplicationContext(), event.reason, Toast.LENGTH_LONG).show();

            }
            returnToLogin();
        }
    }

    private void returnToLogin() {
        Log.d(TAG, "Returning to login");
        finish();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void showInitProgress() {
        FragmentManager manager = getFragmentManager();
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

                FragmentManager manager = getFragmentManager();
                FragmentTransaction trans = manager.beginTransaction();
                NetworkCollection networks = NetworkCollection.getInstance();

                Fragment current = manager.findFragmentById(R.id.right_drawer);

                try {
                    if (current != null) {
                        if (networks.getBufferById(openedBuffer).getInfo().type == BufferInfo.Type.QueryBuffer) {
                            if (detailFragment!=null) trans.replace(R.id.right_drawer, detailFragment);
                            drawer.setDrawerLockMode(drawer.LOCK_MODE_UNLOCKED, Gravity.RIGHT);
                            closeDrawer(Gravity.RIGHT);
                        } else if (networks.getBufferById(openedBuffer).getInfo().type == BufferInfo.Type.ChannelBuffer) {
                            if (nickFragment!=null) trans.replace(R.id.right_drawer, nickFragment);
                            drawer.setDrawerLockMode(drawer.LOCK_MODE_UNLOCKED, Gravity.RIGHT);
                            closeDrawer(Gravity.RIGHT);
                        } else {
                            drawer.setDrawerLockMode(drawer.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
                            closeDrawer(Gravity.RIGHT);
                        }
                    }
                    trans.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                setTitleAndMenu();
                invalidateOptionsMenu();
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

    public void setTitleAndMenu() {
        int side = getOpenDrawers();
        switch (side) {
        case 0:
            showSubtitle=true;
            actionTitleArea.setClickable(true);
            if (openedBuffer != -1) {
                NetworkCollection networks = NetworkCollection.getInstance();
                Buffer buffer = networks.getBufferById(openedBuffer);
                if (buffer.getInfo().type == BufferInfo.Type.StatusBuffer) {
                    setActionBarTitle(networks.getNetworkById(buffer.getInfo().networkId).getName());
                    showSubtitle = false;
                } else {
                    setActionBarTitle(buffer.getInfo().name);
                    subTitleSpan=buffer.getTopic();
                }
            } else {
                setActionBarTitle(getResources().getString(R.string.app_name));
            }
            if (chatFragment != null) chatFragment.setUserVisibleHint(true);
            updateSubtitle();
            setMenuVisible(chatFragment);
            break;
        case 1:
            showSubtitle = false;
            actionTitleArea.setClickable(false);
            setMenuVisible(bufferFragment);
            if (chatFragment != null) chatFragment.setUserVisibleHint(false);
            setActionBarTitle(getResources().getString(R.string.app_name));
            updateSubtitle();
            if (bufferFragment != null && drawer != null) hideKeyboard(bufferFragment.getView());
            break;
        case 2:
            showSubtitle=true;
            actionTitleArea.setClickable(true);
            if (openedBuffer != -1) {
                NetworkCollection networks = NetworkCollection.getInstance();
                Buffer buffer = networks.getBufferById(openedBuffer);
                if (buffer.getInfo().type == BufferInfo.Type.StatusBuffer) {
                    setActionBarTitle(networks.getNetworkById(buffer.getInfo().networkId).getName());
                    showSubtitle = false;
                } else {
                    setActionBarTitle(buffer.getInfo().name);
                    subTitleSpan=buffer.getTopic();
                }
            } else {
                setActionBarTitle(getResources().getString(R.string.app_name));
            }
            if (chatFragment != null) chatFragment.setUserVisibleHint(true);
            updateSubtitle();
            setMenuVisible(nickFragment);
            if (bufferFragment != null && drawer != null) hideKeyboard(bufferFragment.getView());
            break;
        default:
            showSubtitle = false;
            actionTitleArea.setClickable(false);
            setMenuVisible(null);
            setActionBarTitle(getResources().getString(R.string.app_name));
            updateSubtitle();
            if (bufferFragment != null && drawer != null) hideKeyboard(bufferFragment.getView());
            break;
        }
    }

    private int getOpenDrawers() {
        if (drawer == null)
            return 0;
        int openDrawers = 0;
        if (drawer.isDrawerOpen(Gravity.LEFT)) openDrawers += 1;
        if (drawer.isDrawerOpen(Gravity.RIGHT)) openDrawers += 2;
        return openDrawers;
    }
    private void closeDrawer(int side) {
        switch (side) {
            case 1:
                drawer.closeDrawer(Gravity.LEFT);
                break;
            case 2:
                drawer.closeDrawer(Gravity.RIGHT);
                break;
            case 3:
                drawer.closeDrawer(Gravity.LEFT);
                drawer.closeDrawer(Gravity.RIGHT);
                break;
        }
    }
    private void openDrawer(int side) {
        switch (side) {
            case 1:
            case 3:
                drawer.openDrawer(Gravity.LEFT);
                closeDrawer(2);
                break;
            case 2:
                drawer.openDrawer(Gravity.RIGHT);
                closeDrawer(1);
                break;
        }
    }

    private void setMenuVisible(Fragment fragment) {
        if (chatFragment != null) chatFragment.setMenuVisibility(false);
        if (bufferFragment != null) bufferFragment.setMenuVisibility(false);
        if (nickFragment != null) nickFragment.setMenuVisibility(false);
        if (fragment != null) fragment.setMenuVisibility(true);
    }

}
