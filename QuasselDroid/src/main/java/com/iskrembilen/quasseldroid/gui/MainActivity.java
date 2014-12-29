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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.iskrembilen.quasseldroid.Buffer;
import com.iskrembilen.quasseldroid.NetworkCollection;
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
import com.iskrembilen.quasseldroid.events.LatencyChangedEvent;
import com.iskrembilen.quasseldroid.events.UpdateReadBufferEvent;
import com.iskrembilen.quasseldroid.gui.dialogs.TopicViewDialog;
import com.iskrembilen.quasseldroid.gui.fragments.BufferFragment;
import com.iskrembilen.quasseldroid.gui.fragments.ChatFragment;
import com.iskrembilen.quasseldroid.gui.fragments.ConnectingFragment;
import com.iskrembilen.quasseldroid.gui.fragments.DetailFragment;
import com.iskrembilen.quasseldroid.gui.fragments.NickListFragment;
import com.iskrembilen.quasseldroid.service.InFocus;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.iskrembilen.quasseldroid.util.Helper;
import com.iskrembilen.quasseldroid.util.ThemeUtil;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.lang.reflect.Field;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String BUFFER_STATE = "buffer_state";
    private static final String DRAWER_SELECTION = "drawer_selection";

    SharedPreferences preferences;
    OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

    private DrawerLayout drawer;
    private ExtensibleDrawerToggle extensibleDrawerToggle;
    private ClickableActionBar actionbar;

    private QuasselDroidFragmentManager manager = new QuasselDroidFragmentManager();

    private int currentTheme;
    private Boolean showLag = false;

    private int lag = 0;

    private int openedBuffer = -1;
    private int openedDrawer = Gravity.NO_GRAVITY;

    private CharSequence topic;
    private boolean bufferHasTopic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "MainActivity created");
        setTheme(ThemeUtil.themeNoActionBar);
        super.onCreate(savedInstanceState);
        currentTheme = ThemeUtil.themeNoActionBar;

        setContentView(R.layout.layout_main);

        actionbar = new ClickableActionBar(getApplicationContext(),(Toolbar) findViewById(R.id.action_bar));
        setSupportActionBar(actionbar.getWrappedToolbar());

        actionbar.setOnTitleClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                showDetailPopup();
            }
        });

        manager.setupDrawer();

        manager.preInit();

        if (savedInstanceState==null) {
            manager.init();

            manager.lockDrawer(Gravity.START, true);
            manager.lockDrawer(Gravity.END, true);
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

        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener); //To avoid GC issues
    }

    @Subscribe
    public void onBufferDetailsChanged(BufferDetailsChangedEvent event) {
        if (event.bufferId==openedBuffer) {
            topic = NetworkCollection.getInstance().getBufferById(openedBuffer).getTopic();
            updateSubtitle();
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

        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (manager.hasDrawer) extensibleDrawerToggle.drawerToggle.syncState();
    }

    void getState(Bundle in) {
        if (in.containsKey(BUFFER_STATE))
            openedBuffer = in.getInt(BUFFER_STATE);
        if (in.containsKey(DRAWER_SELECTION))
            openedDrawer = in.getInt(DRAWER_SELECTION);
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

        BusProvider.getInstance().register(this);

        if (ThemeUtil.themeNoActionBar != currentTheme) {
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

        if (Quasseldroid.status == Status.Disconnected) {
            returnToLogin();
            return;
        } else if (Quasseldroid.status == Status.Connected) {
            loadBufferAndDrawerState();
        }

        setTitleAndMenu();
        manager.hideKeyboard();
    }

    private void loadBufferAndDrawerState() {
        NetworkCollection networks = NetworkCollection.getInstance();
        if (networks != null) {
            if (openedBuffer == -1 || networks.getBufferById(openedBuffer) == null) {
                Log.d(TAG, "Loading state: Empty");
                openedBuffer = -1;
                BusProvider.getInstance().post(new BufferOpenedEvent(-1, false));
                manager.openDrawer(Gravity.START);
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
        Log.d(TAG,"Saving state: BUFFER="+openedBuffer+"; DRAWER="+manager.getOpenDrawers());
        super.onSaveInstanceState(outState);
        storeState(outState);
    }

    Bundle storeState(Bundle in) {
        in.putInt(BUFFER_STATE, openedBuffer);
        if (manager.hasDrawer) in.putInt(DRAWER_SELECTION, manager.getOpenDrawers());
        return in;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "Configuration changed");
        super.onConfigurationChanged(newConfig);
        extensibleDrawerToggle.drawerToggle.onConfigurationChanged(newConfig);
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
                Intent i = new Intent(MainActivity.this, PreferenceView.class);
                startActivity(i);
                return true;
            case R.id.menu_disconnect:
                BusProvider.getInstance().post(new DisconnectCoreEvent());
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

            manager.hideKeyboard();
            setTitleAndMenu();
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
        TopicViewDialog.newInstance(topic, NetworkCollection.getInstance().getBufferById(openedBuffer).getInfo().name, openedBuffer).show(getSupportFragmentManager(), TAG);
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
        manager.setPanelFragment(Gravity.NO_GRAVITY, new ConnectingFragment());
        manager.openDrawer(Gravity.NO_GRAVITY);
    }

    @Subscribe
    public void onBufferOpened(BufferOpenedEvent event) {
        NetworkCollection networks = NetworkCollection.getInstance();

        if (event.bufferId != -1
                && networks.getBufferById(event.bufferId)!=null
                && networks.getNetworkById(networks.getBufferById(event.bufferId).getInfo().networkId)!=null) {
            openedBuffer = event.bufferId;
            if (event.switchToBuffer) {
                manager.openDrawer(Gravity.NO_GRAVITY);
                ((BufferFragment)manager.bufferFragment).finishActionMode();
                updateBufferRead();
                setTitleAndMenu();
            }
        }
    }
    private void setTitleAndMenu() {
        NetworkCollection networks = NetworkCollection.getInstance();
        Buffer buffer = networks.getBufferById(openedBuffer);

        switch (manager.getOpenDrawers()) {
            case Gravity.START:
                Log.d(TAG, "Opened drawer: START");
                break;
            case Gravity.END:
                Log.d(TAG, "Opened drawer: END");
                break;
            case Gravity.NO_GRAVITY:
                Log.d(TAG, "Opened drawer: NONE");
                break;
            default:
                Log.d(TAG, "Opened drawer: " + manager.getOpenDrawers());
        }

        if (manager.hasDrawer) manager.setWindowProperties(manager.getOpenDrawers());
        if (buffer==null || manager.hasDrawer && (manager.getOpenDrawers()==Gravity.START)) {
            bufferHasTopic = false;
            actionbar.setTitle(getResources().getString(R.string.app_name));
            topic = null;
        } else {
            switch (buffer.getInfo().type) {
                case QueryBuffer:
                    bufferHasTopic = false;
                    manager.setPanelFragment(Gravity.END,manager.detailFragment);
                    manager.lockDrawer(Gravity.END, false);
                    actionbar.setTitle(buffer.getInfo().name);
                    topic = buffer.getTopic();
                    break;
                case StatusBuffer:
                    bufferHasTopic = false;
                    manager.lockDrawer(Gravity.END, true);
                    actionbar.setTitle(networks.getNetworkById(buffer.getInfo().networkId).getName());
                    break;
                case ChannelBuffer:
                    bufferHasTopic = true;
                    manager.setPanelFragment(Gravity.END,manager.nickFragment);
                    manager.lockDrawer(Gravity.END, false);
                    actionbar.setTitle(buffer.getInfo().name);
                    topic = buffer.getTopic();
                    break;
                default:
                    bufferHasTopic = false;
                    actionbar.setTitle(buffer.getInfo().name);
                    topic = buffer.getTopic();
                    manager.lockDrawer(Gravity.END, true);
            }
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
            manager.openDrawer(Gravity.START);
        }
    }

    class QuasselDroidFragmentManager {
        Fragment chatFragment;
        Fragment nickFragment;
        Fragment detailFragment;
        Fragment bufferFragment;

        SparseArray<Fragment> drawers = new SparseArray<Fragment>();
        private boolean hasDrawer;

        void preInit() {
            Log.d(getClass().getSimpleName(),"Setting up fragments");

            FragmentManager manager = getSupportFragmentManager();

            if (chatFragment==null) {
                if (manager.findFragmentById(R.id.main_content_container) instanceof ChatFragment)
                    chatFragment = manager.findFragmentById(R.id.main_content_container);
                else
                    chatFragment = ChatFragment.newInstance();
                ((ChatFragment) chatFragment).setNetworks(NetworkCollection.getInstance());
            }
            if (nickFragment==null) {
                if (manager.findFragmentById(R.id.right_drawer) instanceof NickListFragment)
                    nickFragment = manager.findFragmentById(R.id.right_drawer);
                else
                    nickFragment = NickListFragment.newInstance();
                ((NickListFragment) nickFragment).setNetworks(NetworkCollection.getInstance());
            }
            if (detailFragment==null) {
                if (manager.findFragmentById(R.id.right_drawer) instanceof DetailFragment)
                    detailFragment = manager.findFragmentById(R.id.right_drawer);
                else
                    detailFragment = DetailFragment.newInstance();
                ((DetailFragment) detailFragment).setNetworks(NetworkCollection.getInstance());
            }
            if (bufferFragment==null) {
                if (manager.findFragmentById(R.id.left_drawer) instanceof BufferFragment)
                    bufferFragment = manager.findFragmentById(R.id.left_drawer);
                else
                    bufferFragment = BufferFragment.newInstance();
                ((BufferFragment) bufferFragment).setNetworks(NetworkCollection.getInstance());
            }
        }

        void init() {
            Log.d(getClass().getSimpleName(),"Initializing Side and Main panels");

            setPanelFragment(Gravity.END, nickFragment);
            setPanelFragment(Gravity.START, bufferFragment);

            initMainFragment();
        }

        void initMainFragment() {
            setPanelFragment(Gravity.NO_GRAVITY, chatFragment);
            manager.lockDrawer(Gravity.START, false);
        }

        void setPanelFragment(int side, Fragment fragment) {
            int id;
            String tag;
            boolean isDrawer;
            switch (side) {
                case Gravity.START:
                    id = R.id.left_drawer;
                    tag = "LEFT";
                    isDrawer = true;
                    break;
                case Gravity.END:
                    id = R.id.right_drawer;
                    tag = "RIGHT";
                    isDrawer = true;
                    break;
                case Gravity.NO_GRAVITY:
                    id = R.id.main_content_container;
                    tag = "MAIN";
                    isDrawer = false;
                    break;
                default:
                    throw new IllegalArgumentException("Error replacing fragments: Unknown side");
            }

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
                ft.add(id, fragment, tag);
            } else {
                ft.replace(id, fragment, tag);
            }

            Log.d(this.getClass().getSimpleName(), String.format("Add on side %s the fragment %s, replacing  %s",tag,to,from));

            drawers.put(id, fragment);

            if (isDrawer)
                lockDrawer(side, (fragment == null));

            ft.commitAllowingStateLoss();
            if (fragment != null) fragment.setMenuVisibility(menuVisbility);
        }

        void lockDrawer(int side, boolean locked) {
            if (hasDrawer && locked)
                extensibleDrawerToggle.getDrawer().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, side);
            else if (hasDrawer)
                extensibleDrawerToggle.getDrawer().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, side);
        }

        public void setWindowProperties(int side) {
            switch (side) {
                case Gravity.START:
                    bufferFragment.setMenuVisibility(true);
                    chatFragment.setMenuVisibility(false);
                    break;
                case Gravity.END:
                case Gravity.NO_GRAVITY:
                default:
                    bufferFragment.setMenuVisibility(false);
                    chatFragment.setMenuVisibility(true);
                    break;
            }
        }

        public int getOpenDrawers() {
            if (hasDrawer)
                return getOpenDrawers(extensibleDrawerToggle.drawer);
            else
                return Gravity.NO_GRAVITY;
        }

        public int getOpenDrawers(DrawerLayout drawer) {
            if (!hasDrawer || drawer.isDrawerVisible(Gravity.START)) {
                return Gravity.START;
            } else if (drawer.isDrawerVisible(Gravity.END)) {
                return Gravity.END;
            } else {
                return Gravity.NO_GRAVITY;
            }
        }

        public void closeDrawerOpposite(View drawerView) {
            if (hasDrawer && drawerView.getTag()=="LEFT")
                closeDrawer(Gravity.START);
            else if (hasDrawer)
                closeDrawer(Gravity.END);
        }

        public void openDrawer(int openedDrawer) {
            if (hasDrawer) {
                closeDrawer(Gravity.CENTER);
                if (openedDrawer != Gravity.NO_GRAVITY) {
                    drawer.openDrawer(openedDrawer);
                }
            }
        }

        public void cleanup() {
            setPanelFragment(Gravity.START, null);
            setPanelFragment(Gravity.END, null);
            setPanelFragment(Gravity.NO_GRAVITY, null);

            bufferFragment = null;
            nickFragment = null;
            chatFragment = null;
            detailFragment = null;
        }

        public void cleanupMenus() {
            bufferFragment.setMenuVisibility(false);
            nickFragment.setMenuVisibility(false);
            chatFragment.setMenuVisibility(false);
            detailFragment.setMenuVisibility(false);
        }

        public void setupDrawer() {
            if (null==findViewById(R.id.drawer)) {
                hasDrawer = false;
                return;
            } else {
                hasDrawer = true;
            }

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

            extensibleDrawerToggle = new ExtensibleDrawerToggle(drawer, new ActionBarDrawerToggle(
                    MainActivity.this,                   /* host Activity */
                    drawer,                 /* DrawerLayout object */
                    actionbar.wrappedToolbar,   /* nav drawer icon to replace 'Up' caret */
                    R.string.hint_drawer_open,   /* "open drawer" description */
                    R.string.hint_drawer_close   /* "close drawer" description */
            ) {
                int openDrawers;

                /** Called when a drawer has settled in a completely closed state. */
                public void onDrawerClosed(View drawerView) {
                    manager.setWindowProperties(manager.getOpenDrawers());
                    updateBufferRead();
                    if (drawerView.getId()==R.id.left_drawer) updateBufferRead();
                    ((BufferFragment) manager.bufferFragment).finishActionMode();
                    setTitleAndMenu();
                }

                /** Called when a drawer has settled in a completely open state. */
                public void onDrawerOpened(View drawerView) {
                    manager.closeDrawerOpposite(drawerView);
                    manager.setWindowProperties(manager.getOpenDrawers());
                    setTitleAndMenu();
                }
            });
            drawer.setDrawerListener((extensibleDrawerToggle.getDrawerListener()));
        }

        public void hideKeyboard() {
            hideKeyboard(actionbar.getWrappedToolbar());
        }

        public void hideKeyboard(View view) {
            view.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        }

        public void closeDrawer(int side) {
            if (hasDrawer && side==Gravity.CENTER)
                drawer.closeDrawers();
            else if (hasDrawer)
                drawer.closeDrawer(side);
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

    class ClickableActionBar {
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
            return wrappedToolbar.findViewById(R.id.subtitle).getVisibility() == View.VISIBLE;
        }

        public void setSubtitleVisible(boolean subtitleVisibility) {
            wrappedToolbar.findViewById(R.id.subtitle).setVisibility(
                    subtitleVisibility ? View.VISIBLE
                            : View.GONE
            );
        }

        public CharSequence getTitle() {
            return ((TextView) wrappedToolbar.findViewById(R.id.title)).getText();
        }

        public void setOnTitleClickListener(View.OnClickListener listener) {
            wrappedToolbar.findViewById(R.id.actionTitleArea).setOnClickListener(listener);
        }

        public void setTitle(CharSequence subtitle) {
            ((TextView) wrappedToolbar.findViewById(R.id.title)).setText(subtitle);
        }

        public CharSequence getSubtitle() {
            return ((TextView) wrappedToolbar.findViewById(R.id.subtitle)).getText();
        }

        public void setSubtitle(CharSequence subtitle) {
            ((TextView) wrappedToolbar.findViewById(R.id.subtitle)).setText(subtitle);
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

}