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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.samples.apps.iosched.ui.widget.ScrimInsetsFrameLayout;
import com.iskrembilen.quasseldroid.Quasseldroid;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.BufferDetailsChangedEvent;
import com.iskrembilen.quasseldroid.events.BufferOpenedEvent;
import com.iskrembilen.quasseldroid.events.BufferRemovedEvent;
import com.iskrembilen.quasseldroid.events.CompleteNickEvent;
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent;
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent.Status;
import com.iskrembilen.quasseldroid.events.DisconnectCoreEvent;
import com.iskrembilen.quasseldroid.events.InitProgressEvent;
import com.iskrembilen.quasseldroid.events.JoinChannelEvent;
import com.iskrembilen.quasseldroid.events.LatencyChangedEvent;
import com.iskrembilen.quasseldroid.events.ManageChannelEvent;
import com.iskrembilen.quasseldroid.events.UpdateReadBufferEvent;
import com.iskrembilen.quasseldroid.gui.dialogs.TopicViewDialog;
import com.iskrembilen.quasseldroid.gui.fragments.BufferFragment;
import com.iskrembilen.quasseldroid.gui.fragments.ChatFragment;
import com.iskrembilen.quasseldroid.gui.fragments.ConnectingFragment;
import com.iskrembilen.quasseldroid.gui.fragments.DetailFragment;
import com.iskrembilen.quasseldroid.gui.fragments.NickListFragment;
import com.iskrembilen.quasseldroid.gui.settings.SettingsActivity;
import com.iskrembilen.quasseldroid.protocol.state.Buffer;
import com.iskrembilen.quasseldroid.protocol.state.Client;
import com.iskrembilen.quasseldroid.protocol.state.NetworkCollection;
import com.iskrembilen.quasseldroid.service.InFocus;
import com.iskrembilen.quasseldroid.util.*;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String BUFFER_STATE = "buffer_state";
    private static final String DRAWER_SELECTION = "drawer_selection";

    public static MainActivity instance;

    SharedPreferences preferences;
    OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

    public ClickableActionBar getActionbar() {
        return actionbar;
    }

    private ClickableActionBar actionbar;

    private QuasselDroidFragmentManager manager = new QuasselDroidFragmentManager();

    private int currentTheme;
    private Boolean showLag = false;

    private int lag = 0;

    private int openedBuffer = -1;
    private Side openedDrawer = Side.NONE;

    private CharSequence topic;
    private boolean bufferHasTopic;

    private boolean connectionEstablished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MainActivity created");
        setTheme(ThemeUtil.themeNoActionBarDrawStatusBar);
        supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY);
        super.onCreate(savedInstanceState);
        currentTheme = ThemeUtil.themeNoActionBarDrawStatusBar;

        instance = this;

        setContentView(R.layout.layout_main);

        actionbar = new ClickableActionBar(getApplicationContext(),(Toolbar) findViewById(R.id.action_bar));
        setSupportActionBar(actionbar.getWrappedToolbar());

        actionbar.setOnTitleClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                showDetailPopup();
            }
        });

        manager.preInit();

        if (savedInstanceState==null) {
            manager.init();
        } else {
            manager.initMainFragment();
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        showLag = preferences.getBoolean(getString(R.string.preference_lag), false);

        sharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(getResources().getString(R.string.preference_lag))) {
                    showLag = preferences.getBoolean(getString(R.string.preference_lag), false);
                    updateSubtitle();
                }
            }
        };

        ScrimInsetsFrameLayout insetLayout = (ScrimInsetsFrameLayout) findViewById(R.id.scrimInsetsFrameLayout);
        if (insetLayout != null) insetLayout.setOnInsetsCallback(new ScrimInsetsFrameLayout.OnInsetsCallback() {
            @Override
            public void onInsetsChanged(Rect insets) {
                ((BufferFragment) manager.bufferFragment).setStatusBarHeight(insets.top);
            }
        });

        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener); //To avoid GC issues
    }

    @Subscribe
    public void onBufferDetailsChanged(BufferDetailsChangedEvent event) {
        if (event.bufferId==openedBuffer) {
            topic = Client.getInstance().getNetworks().getBufferById(openedBuffer).getTopic();
            setTitleAndMenu();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (((Quasseldroid) getApplication()).savedInstanceState!=null) {
            getState(((Quasseldroid) getApplication()).savedInstanceState);

            Log.d(TAG,"Loaded state: BUFFER="+openedBuffer+"; DRAWER="+openedDrawer);

            ((Quasseldroid) getApplication()).savedInstanceState = null;
        }
    }

    void getState(Bundle in) {
        if (in.containsKey(BUFFER_STATE))
            openedBuffer = in.getInt(BUFFER_STATE);
        if (in.containsKey(DRAWER_SELECTION))
            openedDrawer = Side.valueOf(in.getString(DRAWER_SELECTION));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent!=null) {
            int requestOpenBuffer = intent.getIntExtra("extraBufferId", -1);
            boolean requestOpenDrawer = intent.getBooleanExtra("extraDrawer", false);
            if (requestOpenBuffer != -1) {
                openedBuffer = requestOpenBuffer;
            }

            if (requestOpenDrawer) {
                openedDrawer = Side.LEFT;
            }
            loadBufferAndDrawerState();
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "Starting activity");
        super.onStart();

        bindService(new Intent(this, InFocus.class), focusConnection, Context.BIND_AUTO_CREATE);

        Log.d(TAG, "Current themes: "
                        + ((ThemeUtil.themeNoActionBar==R.style.Theme_QuasselDroid_Material_Light_NoActionBar)?"LIGHT ":"DARK ")
                        + ((ThemeUtil.themeNoActionBar == currentTheme) ? "== " : "!= ")
                        + ((currentTheme==R.style.Theme_QuasselDroid_Material_Light_NoActionBar)?"LIGHT":"DARK")
        );
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "Resuming activity");
        super.onResume();

        manager.setupDrawer();

        BusProvider.getInstance().register(this);

        if (ThemeUtil.themeNoActionBarDrawStatusBar != currentTheme) {
            Log.d(TAG, "Changing theme");
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recreate();
                }
            }, 1);
            return;
        }

        if (Client.getInstance().status == Status.Disconnected) {
            Log.d(TAG, "Status is disconnected when resuming activity");
            returnToLogin(true);
            return;
        } else if (Client.getInstance().status == Status.Connected) {
            loadBufferAndDrawerState();
            connectionEstablished = true;
        }

        setTitleAndMenu();
        manager.hideKeyboard();
        Client.getInstance().setActivity(this);
    }

    private void loadBufferAndDrawerState() {
        NetworkCollection networks = Client.getInstance().getNetworks();
        if (networks != null) {
            if (openedBuffer == -1 || networks.getBufferById(openedBuffer) == null) {
                Log.d(TAG, "Loading state: Empty");
                openedBuffer = -1;
                BusProvider.getInstance().post(new BufferOpenedEvent(-1, false));
                manager.openDrawer(Side.LEFT);
            } else {
                Log.d(TAG, "Loading state: "+openedBuffer);
                manager.openDrawer(openedDrawer);
                BusProvider.getInstance().post(new BufferOpenedEvent(openedBuffer, true));
            }
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Pausing activity");
        BusProvider.getInstance().unregister(this);
        manager.cleanupMenus();
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

        ((Quasseldroid) getApplication()).savedInstanceState = storeState(new Bundle());
        manager.cleanupMenus();

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG,"Saving state: BUFFER="+openedBuffer+"; DRAWER="+manager.getOpenDrawer());
        super.onSaveInstanceState(outState);
        storeState(outState);
    }

    Bundle storeState(Bundle in) {
        in.putInt(BUFFER_STATE, openedBuffer);
        in.putString(DRAWER_SELECTION, manager.getOpenDrawer().name());
        return in;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "Configuration changed");
        super.onConfigurationChanged(newConfig);
        manager.extensibleDrawerToggle.drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onSearchRequested() {
        BusProvider.getInstance().post(new CompleteNickEvent());
        return false; //Activity ate the request
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_preferences:
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
                return true;
            case R.id.menu_disconnect:
                BusProvider.getInstance().post(new DisconnectCoreEvent());
                return true;
            case R.id.menu_nick_list:
                manager.onDrawerButtonClicked();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onInitProgressed(InitProgressEvent event) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment currentFragment = fm.findFragmentById(R.id.main_content_container);
        if (event.done) {
            manager.initMainFragment();

            loadBufferAndDrawerState();
            connectionEstablished = true;

            manager.hideKeyboard();
            setTitleAndMenu();

            ((BufferFragment) manager.bufferFragment).init();
        } else if (currentFragment == null || !connectionEstablished && currentFragment.getClass()!=ConnectingFragment.class) {
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
        CharSequence subtitle;
        if (showLag && emptyString(topic)) {
            subtitle = Helper.formatLatency(lag, getResources());
        } else if (showLag) {
            subtitle = TextUtils.concat(Helper.formatLatency(lag, getResources()), " — ", topic);
        } else {
            subtitle = topic;
        }

        actionbar.setSubtitle(subtitle);
        actionbar.setTitleClickable(bufferHasTopic);
        actionbar.setSubtitleVisible(showLag || !emptyString(topic));
    }

    private boolean emptyString(CharSequence topic) {
        return topic==null || topic.toString().trim().equals("");
    }

    private void showDetailPopup() {
        TopicViewDialog.newInstance(openedBuffer).show(getSupportFragmentManager(), TAG);
    }

    @Subscribe
    public void onConnectionChanged(ConnectionChangedEvent event) {
        if (event.status == Status.Disconnected) {
            Log.d(TAG, "Connection status is disconnected");
            if (event.reason != "") {
                removeDialog(R.id.DIALOG_CONNECTING);
                Toast.makeText(MainActivity.this.getApplicationContext(), event.reason, Toast.LENGTH_LONG).show();
            }
            returnToLogin(false);
        }
    }

    private void returnToLogin(boolean skipAnimation) {
        Log.d(TAG, "Returning to login");
        finish();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (skipAnimation) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        }
        startActivity(intent);
    }

    private void showInitProgress() {
        manager.setFragment(R.id.main_content_container, new ConnectingFragment());
        manager.closeDrawer(Side.BOTH);
    }

    @Subscribe
    public void onBufferOpened(BufferOpenedEvent event) {
        NetworkCollection networks = Client.getInstance().getNetworks();

        if (event.bufferId != -1
                && networks.getBufferById(event.bufferId)!=null
                && networks.getNetworkById(networks.getBufferById(event.bufferId).getInfo().networkId)!=null) {
            openedBuffer = event.bufferId;
            ((ChatFragment) manager.chatFragment).setBuffer(event.bufferId);
            if (event.switchToBuffer) {
                manager.closeDrawer(Side.BOTH);
                ((BufferFragment)manager.bufferFragment).finishActionMode();
                updateBufferRead();
                setTitleAndMenu();
            }
        } else {
            Log.d(TAG, "Buffer Opened Event was faulty");
        }
    }
    private void setTitleAndMenu() {
        NetworkCollection networks = Client.getInstance().getNetworks();
        Buffer buffer = networks.getBufferById(openedBuffer);

        manager.chatFragment.setMenuVisibility(true);

        if (buffer==null) {
            bufferHasTopic = false;
            actionbar.setTitle(getResources().getString(R.string.app_name));
            topic = null;
        } else {
            switch (buffer.getInfo().type) {
                case QueryBuffer:
                    bufferHasTopic = false;
                    manager.setFragment(R.id.right_drawer, manager.detailFragment);
                    manager.lockDrawer(Side.RIGHT,DrawerLayout.LOCK_MODE_UNLOCKED);
                    actionbar.setTitle(buffer.getInfo().name);
                    topic = buffer.getTopic();
                    break;
                case StatusBuffer:
                    bufferHasTopic = true;
                    manager.lockDrawer(Side.RIGHT,DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    actionbar.setTitle(networks.getNetworkById(buffer.getInfo().networkId).getName());
                    topic = buffer.getTopic();
                    break;
                case ChannelBuffer:
                    bufferHasTopic = true;
                    manager.setFragment(R.id.right_drawer, manager.nickFragment);
                    manager.lockDrawer(Side.RIGHT,DrawerLayout.LOCK_MODE_UNLOCKED);
                    actionbar.setTitle(buffer.getInfo().name);
                    topic = buffer.getTopic();
                    break;
                default:
                    bufferHasTopic = false;
                    actionbar.setTitle(buffer.getInfo().name);
                    manager.lockDrawer(Side.RIGHT,DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    topic = buffer.getTopic();
            }
            if (topic != null) topic = MessageUtil.parseStyleCodes(this, topic.toString(), preferences.getBoolean(getResources().getString(R.string.preference_colored_text), true));
        }
        updateSubtitle();
        invalidateOptionsMenu();
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
            manager.openDrawer(Side.LEFT);
        }
    }

    class QuasselDroidFragmentManager {
        private Fragment chatFragment;
        private Fragment nickFragment;
        private Fragment detailFragment;
        private Fragment bufferFragment;

        private ExtensibleDrawerToggle extensibleDrawerToggle;

        private SparseArray<Fragment> drawers = new SparseArray<>();
        private DrawerLayout leftDrawer;
        private DrawerLayout rightDrawer;

        private Side openDrawer;

        void preInit() {
            Log.d(getClass().getSimpleName(), "Setting up fragments");

            FragmentManager manager = getSupportFragmentManager();

            if (chatFragment==null) {
                if (manager.findFragmentById(R.id.main_content_container) instanceof ChatFragment)
                    chatFragment = manager.findFragmentById(R.id.main_content_container);
                else
                    chatFragment = ChatFragment.newInstance();
                ((ChatFragment) chatFragment).setNetworks(Client.getInstance().getNetworks());
            }
            if (nickFragment==null) {
                if (manager.findFragmentById(R.id.right_drawer) instanceof NickListFragment)
                    nickFragment = manager.findFragmentById(R.id.right_drawer);
                else
                    nickFragment = NickListFragment.newInstance();
                ((NickListFragment) nickFragment).setNetworks(Client.getInstance().getNetworks());
            }
            if (detailFragment==null) {
                if (manager.findFragmentById(R.id.right_drawer) instanceof DetailFragment)
                    detailFragment = manager.findFragmentById(R.id.right_drawer);
                else
                    detailFragment = DetailFragment.newInstance();
                ((DetailFragment) detailFragment).setNetworks(Client.getInstance().getNetworks());
            }
            if (bufferFragment==null) {
                if (manager.findFragmentById(R.id.left_drawer) instanceof BufferFragment)
                    bufferFragment = manager.findFragmentById(R.id.left_drawer);
                else
                    bufferFragment = BufferFragment.newInstance();
                ((BufferFragment) bufferFragment).setNetworks(Client.getInstance().getNetworks());
            }
        }

        void init() {
            Log.d(getClass().getSimpleName(),"Initializing Side and Main panels");

            setFragment(R.id.right_drawer, nickFragment);

            initMainFragment();
        }

        void initMainFragment() {
            setFragment(R.id.main_content_container, chatFragment);
        }

        void setFragment(int id, Fragment fragment) {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();
            Fragment currentFragment = (drawers.get(id)==null) ? manager.findFragmentById(id) : drawers.get(id);

            boolean menuVisbility = currentFragment!=null && currentFragment.isMenuVisible();
            if (currentFragment!=null) currentFragment.setMenuVisibility(false);

            String from = (currentFragment == null) ? "NULL" : currentFragment.getClass().getSimpleName();
            String to = (fragment == null) ? "NULL" : fragment.getClass().getSimpleName();

            if (fragment==currentFragment) {
                // Replace stuff with itself: Do nothing!
            } else if (fragment == null) {
                ft.remove(currentFragment);
            } else if (currentFragment == null) {
                ft.add(id, fragment);
            } else {
                ft.replace(id, fragment);
            }

            drawers.put(id, fragment);

            ft.commitAllowingStateLoss();
            if (fragment != null) fragment.setMenuVisibility(menuVisbility);
        }

        public void cleanupMenus() {
            nickFragment.setMenuVisibility(false);
            chatFragment.setMenuVisibility(false);
            detailFragment.setMenuVisibility(false);
        }

        public void setupDrawer() {
            leftDrawer = (DrawerLayout) findViewById(R.id.main_drawer);
            rightDrawer = (DrawerLayout) findViewById(R.id.nick_drawer);

            if (leftDrawer!=null) {
                leftDrawer.setStatusBarBackgroundColor(getResources().getColor(R.color.primary_dark));

                if (extensibleDrawerToggle==null) {
                    extensibleDrawerToggle = new ExtensibleDrawerToggle(leftDrawer, new ActionBarDrawerToggle(
                            MainActivity.this, /* host Activity */
                            leftDrawer, /* DrawerLayout object */
                            actionbar.getWrappedToolbar(), /* nav drawer icon to replace 'Up' caret */
                            R.string.hint_drawer_open, /* "open drawer" description */
                            R.string.hint_drawer_close /* "close drawer" description */
                    ) {
                        /**
                         * Called when a drawer has settled in a completely closed state.
                         */
                        @Override
                        public void onDrawerClosed(View drawerView) {
                            updateBufferRead();
                            ((BufferFragment) manager.bufferFragment).finishActionMode();
                            setTitleAndMenu();
                        }

                        /**
                         * Called when a drawer has settled in a completely open state.
                         */
                        @Override
                        public void onDrawerOpened(View drawerView) {
                            manager.closeDrawer(Side.RIGHT);
                            setTitleAndMenu();
                            manager.hideKeyboard();
                        }

                        @Override
                        public void onDrawerSlide(View drawerView, float slideOffset) {
                            // We set the offset here to zero to disable the animation in the toggle
                            super.onDrawerSlide(drawerView, 0);
                        }
                    });
                    leftDrawer.setDrawerListener((extensibleDrawerToggle.getDrawerListener()));
                }
            }
        }

        private void onDrawerButtonClicked() {
            if (rightDrawer.isDrawerVisible(GravityCompat.END)) {
                closeDrawer(Side.RIGHT);
            } else {
                openDrawer(Side.RIGHT);
            }
        }

        private void closeDrawer(Side side) {
            if (side==Side.RIGHT||side==Side.BOTH) {
                rightDrawer.closeDrawers();
            }

            if (leftDrawer!=null&&(side==Side.LEFT||side==Side.BOTH)) {
                leftDrawer.closeDrawers();
            }
        }


        public void openDrawer(Side side) {
            if (side==Side.RIGHT) {
                rightDrawer.openDrawer(GravityCompat.END);
            } else if (side==Side.LEFT&&leftDrawer!=null) {
                if (rightDrawer.isDrawerVisible(GravityCompat.END))
                    rightDrawer.closeDrawers();

                leftDrawer.openDrawer(GravityCompat.START);
            }
        }

        public void lockDrawer(Side side, int lockMode) {
            if (side==Side.RIGHT) {
                rightDrawer.setDrawerLockMode(lockMode);
            } else if (side==Side.LEFT&&leftDrawer!=null) {
                leftDrawer.setDrawerLockMode(lockMode);
            }
        }

        public Side getOpenDrawer() {
            if (leftDrawer!=null && leftDrawer.isDrawerVisible(GravityCompat.START))
                return Side.LEFT;
            else if (rightDrawer.isDrawerVisible(GravityCompat.END))
                return Side.RIGHT;
            else
                return Side.NONE;
        }

        public void hideKeyboard() {
            hideKeyboard(actionbar.getWrappedToolbar());
        }

        public void hideKeyboard(View view) {
            view.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        }
    }

    private void updateBufferRead() {
        if (openedBuffer != -1) {
            BusProvider.getInstance().post(new UpdateReadBufferEvent());
        }
    }

    class ExtensibleDrawerToggle {
        ActionBarDrawerToggle drawerToggle;
        DrawerLayout drawer;

        ExtensibleDrawerToggle(DrawerLayout drawer, final ActionBarDrawerToggle drawerToggle) {
            this.drawerToggle = drawerToggle;
            this.drawer = drawer;
            drawer.post(new Runnable() {
                @Override
                public void run() {
                    drawerToggle.syncState();
                }
            });
        }

        DrawerLayout.DrawerListener getDrawerListener() {
            return drawerToggle;
        }

        DrawerLayout getDrawer() {
            return drawer;
        }
    }

    public class ClickableActionBar {
        Toolbar wrappedToolbar;
        Context context;

        ClickableActionBar(Context context, Toolbar toolbar) {
            this.context = context;
            this.wrappedToolbar = toolbar;
        }

        public Toolbar getWrappedToolbar() {
            return wrappedToolbar;
        }

        public boolean isSubtitleVisible() {
            return wrappedToolbar.findViewById(R.id.action_bar_subtitle).getVisibility() == View.VISIBLE;
        }

        public void setSubtitleVisible(boolean subtitleVisibility) {
            wrappedToolbar.findViewById(R.id.action_bar_subtitle).setVisibility(
                    subtitleVisibility ? View.VISIBLE
                            : View.GONE
            );
        }

        public CharSequence getTitle() {
            return ((TextView) wrappedToolbar.findViewById(R.id.action_bar_title)).getText();
        }

        public void setOnTitleClickListener(View.OnClickListener listener) {
            wrappedToolbar.findViewById(R.id.actionTitleArea).setOnClickListener(listener);
        }

        public void setTitle(CharSequence subtitle) {
            ((TextView) wrappedToolbar.findViewById(R.id.action_bar_title)).setText(subtitle);
        }

        public CharSequence getSubtitle() {
            return ((TextView) wrappedToolbar.findViewById(R.id.action_bar_subtitle)).getText();
        }

        public void setSubtitle(CharSequence subtitle) {
            ((TextView) wrappedToolbar.findViewById(R.id.action_bar_subtitle)).setText(subtitle);
        }

        public boolean isTitleClickable() {
            return wrappedToolbar.findViewById(R.id.actionTitleArea).isClickable();
        }

        public void setTitleClickable(boolean clickable) {
            wrappedToolbar.findViewById(R.id.actionTitleArea).setClickable(clickable);
        }
    }

    private ServiceConnection focusConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName cn, IBinder service) {
        }

        public void onServiceDisconnected(ComponentName cn) {
        }
    };

    public enum Side {
        LEFT,
        RIGHT,
        BOTH,
        NONE
    }
}
