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
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.iskrembilen.quasseldroid.gui.dialogs.TopicViewDialog;
import com.iskrembilen.quasseldroid.service.InFocus;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.iskrembilen.quasseldroid.util.Helper;
import com.iskrembilen.quasseldroid.util.ThemeUtil;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String BUFFER_STATE = "buffer_state";
    private static final String DRAWER_SELECTION = "drawer_selection";
    private static final String DRAWER_STATE = "drawer_state";

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
        setTheme(ThemeUtil.theme_noactionbar);
        super.onCreate(savedInstanceState);
        currentTheme = ThemeUtil.theme_noactionbar;
        setContentView(R.layout.layout_main);

        actionbar = new ClickableActionBar(getApplicationContext(),(Toolbar) findViewById(R.id.action_bar));
        setSupportActionBar(actionbar.getWrappedToolbar());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionbar.setOnTitleClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                showDetailPopup();
            }
        });

        manager.preInit();

        drawer = (DrawerLayout) findViewById(R.id.drawer);

        extensibleDrawerToggle = new ExtensibleDrawerToggle(drawer, new ActionBarDrawerToggle(
                this,                   /* host Activity */
                drawer,                 /* DrawerLayout object */
                actionbar.wrappedToolbar,   /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,   /* "open drawer" description */
                R.string.drawer_close   /* "close drawer" description */
        ) {
            int openDrawers;

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View drawerView) {
                manager.setWindowProperties(manager.getOpenDrawers(drawer));
                if (drawerView.getId() == R.id.left_drawer && openedBuffer != -1) {
                    BusProvider.getInstance().post(new UpdateReadBufferEvent());
                }
                ((BufferFragment) manager.bufferFragment).finishActionMode();
                setTitleAndMenu();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                manager.closeDrawerOpposite(drawerView);
                manager.setWindowProperties(manager.getOpenDrawers(drawer));
                setTitleAndMenu();
            }
        });
        drawer.setDrawerListener((extensibleDrawerToggle.getDrawerListener()));

        manager.lockDrawer(Gravity.LEFT,true);
        manager.lockDrawer(Gravity.RIGHT, true);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        showLag = preferences.getBoolean(getString(R.string.preference_show_lag), false);

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

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (((Quasseldroid) getApplication()).savedInstanceState!=null) {
            getState(((Quasseldroid) getApplication()).savedInstanceState);

            Log.d(TAG,"Loaded state: BUFFER="+openedBuffer+"; DRAWER="+openedDrawer);

            ((Quasseldroid) getApplication()).savedInstanceState = null;
        }

        // Sync the toggle state after onRestoreInstanceState has occurred.
        extensibleDrawerToggle.drawerToggle.syncState();
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
                + ((ThemeUtil.theme_noactionbar==R.style.Theme_QuasselDroid_Material_Light_NoActionBar)?"LIGHT ":"DARK ")
                + ((ThemeUtil.theme_noactionbar == currentTheme) ? "== " : "!= ")
                + ((currentTheme==R.style.Theme_QuasselDroid_Material_Light_NoActionBar)?"LIGHT":"DARK")
        );

        if (ThemeUtil.theme_noactionbar != currentTheme) {
            Log.d(TAG, "Changing theme");
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
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
            return;
        } else if (Quasseldroid.status == Status.Connected) {
            loadBufferAndDrawerState();
        }
        hideKeyboard(drawer);
    }

    private void loadBufferAndDrawerState() {
        NetworkCollection networks = NetworkCollection.getInstance();
        if (networks != null) {
            if (openedBuffer == -1 || networks.getBufferById(openedBuffer) == null) {
                Log.d(TAG, "Loading state: Empty");
                openedBuffer = -1;
                BusProvider.getInstance().post(new BufferOpenedEvent(-1, false));
                drawer.closeDrawer(Gravity.END);
                drawer.openDrawer(Gravity.START);
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
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Stopping activity");
        unbindService(focusConnection);

        ((Quasseldroid) getApplication()).savedInstanceState = storeState(new Bundle());

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Destroying activity");
        preferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

        ((Quasseldroid) getApplication()).savedInstanceState = storeState(new Bundle());

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG,"Saving state: BUFFER="+openedBuffer+"; DRAWER="+manager.getOpenDrawers(extensibleDrawerToggle.drawer));
        super.onSaveInstanceState(outState);
        storeState(outState);
    }

    Bundle storeState(Bundle in) {
        in.putInt(BUFFER_STATE, openedBuffer);
        in.putInt(DRAWER_SELECTION, manager.getOpenDrawers(extensibleDrawerToggle.drawer));
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
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawer.closeDrawer(Gravity.END);
                if (drawer.isDrawerOpen(Gravity.START)) {
                    drawer.closeDrawer(Gravity.START);
                } else {
                    drawer.openDrawer(Gravity.START);
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

    @Subscribe
    public void onInitProgressed(InitProgressEvent event) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment currentFragment = fm.findFragmentById(R.id.main_content_container);
        if (event.done) {
            manager.init();

            loadBufferAndDrawerState();

            hideKeyboard(drawer);
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
        Log.d(TAG,"Setting subtitle "+(bufferHasTopic?"":"non ")+" clickable: "+topic);
        actionbar.setSubtitle(subtitle);
        actionbar.setTitleClickable(!bufferHasTopic);
        actionbar.setSubtitleVisibile(showLag || !emptyString(topic));
    }

    private boolean emptyString(CharSequence topic) {
        return topic==null || topic.toString().trim()=="";
    }

    private void showDetailPopup() {
        String topic = null;
        if (manager.nickFragment!=null)
            topic = ((NickListFragment) manager.nickFragment).topic;
        if (topic!=null)
            TopicViewDialog.newInstance(topic, openedBuffer).show(getSupportFragmentManager(),TAG);
    }

    @Subscribe
    public void onConnectionChanged(ConnectionChangedEvent event) {
        if (event.status == Status.Disconnected) {
            Log.d(TAG, "Connection status is disconnected");
            if (event.reason != "") {
                removeDialog(R.id.DIALOG_CONNECTING);
                Toast.makeText(MainActivity.this.getApplicationContext(), event.reason, Toast.LENGTH_LONG).show();
            }
            Log.e(TAG,"REASON: "+event.reason);
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
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        ConnectingFragment fragment = ConnectingFragment.newInstance();
        fragmentTransaction.replace(R.id.main_content_container, fragment, "init");
        if (manager.bufferFragment != null)
            fragmentTransaction.remove(manager.bufferFragment);
        if (manager.nickFragment != null)
            fragmentTransaction.remove(manager.nickFragment);
        drawer.closeDrawers();
        fragmentTransaction.commit();
    }

    @Subscribe
    public void onBufferOpened(BufferOpenedEvent event) {
        if (event.bufferId != -1) {
            openedBuffer = event.bufferId;
            if (event.switchToBuffer) {
                drawer.closeDrawers();
                ((BufferFragment)manager.bufferFragment).finishActionMode();

                setTitleAndMenu();
            }
        }
    }

    private void setTitleAndMenu() {
        NetworkCollection networks = NetworkCollection.getInstance();
        Buffer buffer = networks.getBufferById(openedBuffer);

        switch (manager.getOpenDrawers(drawer)) {
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
                Log.d(TAG, "Opened drawer: " + manager.getOpenDrawers(drawer));
        }

        manager.setWindowProperties(manager.getOpenDrawers(extensibleDrawerToggle.drawer));
        if (buffer==null || (manager.getOpenDrawers(extensibleDrawerToggle.drawer)==Gravity.START)) {
            bufferHasTopic = false;
            actionbar.setTitle("Quasseldroid");
            topic = null;
        } else {
            switch (buffer.getInfo().type) {
                case QueryBuffer:
                    bufferHasTopic = true;
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
                    bufferHasTopic = false;
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
            drawer.closeDrawer(Gravity.END);
            drawer.openDrawer(Gravity.START);
        }
    }
    class QuasselDroidFragmentManager {
        Fragment chatFragment;
        Fragment nickFragment;
        Fragment detailFragment;
        Fragment bufferFragment;

        HashMap<Integer,Fragment> drawers = new HashMap<Integer,Fragment>();

        void cleanup() {
            setMainFragment(null);
            setPanelFragment(Gravity.END, null);
            setPanelFragment(Gravity.START, null);

            chatFragment = null;
            nickFragment = null;
            detailFragment = null;
            bufferFragment = null;
        }

        void preInit() {
            if (chatFragment==null) chatFragment = ChatFragment.newInstance();
            if (nickFragment==null) nickFragment = NickListFragment.newInstance();
            if (detailFragment==null) detailFragment = DetailFragment.newInstance();
            if (bufferFragment==null) bufferFragment = BufferFragment.newInstance();
        }

        void init() {
            setPanelFragment(Gravity.END, nickFragment);
            setPanelFragment(Gravity.START, bufferFragment);

            initMainFragment();
        }

        void initMainFragment() {
            setMainFragment(chatFragment);
            manager.lockDrawer(Gravity.LEFT, false);
        }

        void setMainFragment(Fragment fragment) {
            FragmentManager fm = getSupportFragmentManager();
            Fragment currentFragment = fm.findFragmentById(R.id.main_content_container);
            if (currentFragment == null || currentFragment.getClass() != fragment.getClass()) {
                FragmentTransaction trans = fm.beginTransaction();
                if (currentFragment != null) {
                    trans.remove(currentFragment);
                }
                trans.add(R.id.main_content_container,fragment);
                trans.commit();
            }
        }

        void setPanelFragment(int side, Fragment fragment) {
            int id = (side == Gravity.START) ? R.id.left_drawer : R.id.right_drawer;
            String tag = (side==Gravity.START) ? "LEFT" : "RIGHT";

            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();
            Fragment currentFragment = (drawers.containsKey(id)) ? drawers.get(id) : null;

            String a = (currentFragment == null) ? "NULL" : currentFragment.getClass().getCanonicalName();
            String b = (fragment == null) ? "NULL" : fragment.getClass().getCanonicalName();

            if (currentFragment == fragment) {
            } else if (fragment == null && currentFragment != null) {
                ft.remove(currentFragment);
                drawers.put(id,null);
                lockDrawer(side, true);
                Log.d(this.getClass().getCanonicalName(), "Replace " + a + " with " + b);
            } else if (currentFragment == null) {
                ft.add(id, fragment, tag);
                drawers.put(id,fragment);
                lockDrawer(side, false);
                Log.d(this.getClass().getCanonicalName(), "Replace " + a + " with " + b);
            } else {
                ft.replace(id, fragment, tag);
                drawers.put(id,fragment);
                lockDrawer(side, false);
                Log.d(this.getClass().getCanonicalName(), "Replace " + a + " with " + b);
            }

            ft.commit();
        }

        void lockDrawer(int side, boolean locked) {
            if (locked)
                extensibleDrawerToggle.getDrawer().setDrawerLockMode(extensibleDrawerToggle.getDrawer().LOCK_MODE_LOCKED_CLOSED, side);
            else
                extensibleDrawerToggle.getDrawer().setDrawerLockMode(extensibleDrawerToggle.getDrawer().LOCK_MODE_UNLOCKED, side);
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

        public int getOpenDrawers(DrawerLayout drawer) {
            if (drawer.isDrawerVisible(Gravity.START)) {
                return Gravity.START;
            } else if (drawer.isDrawerVisible(Gravity.END)) {
                return Gravity.END;
            } else {
                return Gravity.NO_GRAVITY;
            }
        }

        public void closeDrawerOpposite(View drawerView) {
            if (drawerView.getTag()=="LEFT")
                drawer.closeDrawer(Gravity.START);
            else
                drawer.closeDrawer(Gravity.END);
        }

        public void openDrawer(int openedDrawer) {
            if (openedDrawer==Gravity.NO_GRAVITY)
                drawer.closeDrawers();
            else
                drawer.openDrawer(openedDrawer);
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
            wrappedToolbar.findViewById(R.id.actionTitleArea).setBackground(getSelectedItemDrawable());
        }

        public Toolbar getWrappedToolbar() {
            return wrappedToolbar;
        }

        public boolean isSubtitleVisibile() {
            return ((TextView) wrappedToolbar.findViewById(R.id.subtitle)).getVisibility() == View.VISIBLE;
        }

        public void setSubtitleVisibile(boolean subtitleVisibility) {
            ((TextView) wrappedToolbar.findViewById(R.id.subtitle)).setVisibility(
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

        public void setTitleClickable(boolean clickability) {
            wrappedToolbar.findViewById(R.id.actionTitleArea).setClickable(clickability);
        }

        Drawable getSelectedItemDrawable() {
            int[] attrs = new int[]{R.attr.selectableItemBackground};
            TypedArray ta = context.obtainStyledAttributes(attrs);
            Drawable selectedItemDrawable = ta.getDrawable(0);
            ta.recycle();
            return selectedItemDrawable;
        }
    }

    private ServiceConnection focusConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName cn, IBinder service) {
        }

        public void onServiceDisconnected(ComponentName cn) {
        }
    };

    private void hideKeyboard(View view) {
        if (view != null) {
            view.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        }
    }

}