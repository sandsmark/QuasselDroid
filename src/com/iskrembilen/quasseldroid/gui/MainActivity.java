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
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.iskrembilen.quasseldroid.Buffer;
import com.iskrembilen.quasseldroid.BufferInfo;
import com.iskrembilen.quasseldroid.BufferUtils;
import com.iskrembilen.quasseldroid.Network;
import com.iskrembilen.quasseldroid.NetworkCollection;
import com.iskrembilen.quasseldroid.Quasseldroid;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.BufferOpenedEvent;
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent;
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent.Status;
import com.iskrembilen.quasseldroid.events.CompleteNickEvent;
import com.iskrembilen.quasseldroid.events.DisconnectCoreEvent;
import com.iskrembilen.quasseldroid.events.InitProgressEvent;
import com.iskrembilen.quasseldroid.events.LatencyChangedEvent;
import com.iskrembilen.quasseldroid.events.UpdateReadBufferEvent;
import com.iskrembilen.quasseldroid.gui.MainActivity.FragmentAdapter;
import com.iskrembilen.quasseldroid.gui.fragments.BufferFragment;
import com.iskrembilen.quasseldroid.gui.fragments.ChatFragment;
import com.iskrembilen.quasseldroid.gui.fragments.ConnectingFragment;
import com.iskrembilen.quasseldroid.gui.fragments.NickListFragment;
import com.iskrembilen.quasseldroid.service.CoreConnService;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.iskrembilen.quasseldroid.util.Helper;
import com.iskrembilen.quasseldroid.util.ThemeUtil;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.util.GregorianCalendar;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends SherlockFragmentActivity {

	private static final String TAG = MainActivity.class.getSimpleName();

	public static final String BUFFER_ID_EXTRA = "bufferid";
	public static final String BUFFER_NAME_EXTRA = "buffername";
	private static final long BACK_THRESHOLD = 4000;

	SharedPreferences preferences;
	OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

	private int currentTheme;
	private Boolean showLag = false;

	private ViewPager pager;

	private int openedBuffer = -1;

	private long lastBackPressed = 0;
	
	private FragmentAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(ThemeUtil.theme);
		super.onCreate(savedInstanceState);
		currentTheme = ThemeUtil.theme;
		setContentView(R.layout.main_layout);

		adapter = new FragmentAdapter(getSupportFragmentManager());
		pager = (ViewPager) findViewById(R.id.pager);
		pager.setOffscreenPageLimit(2);

		PagerTabStrip pagerIndicator = (PagerTabStrip) findViewById(R.id.pagerIndicator);
		pagerIndicator.setDrawFullUnderline(false);
		pagerIndicator.setTextColor(getResources().getColor(R.color.pager_indicator_text_color));
		pagerIndicator.setTabIndicatorColor(getResources().getColor(R.color.pager_indicator_color));

		pager.setOnPageChangeListener(adapter);
		pager.setAdapter(adapter);

		preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		showLag = preferences.getBoolean(getString(R.string.preference_show_lag), false);

		sharedPreferenceChangeListener =new OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if(key.equals(getResources().getString(R.string.preference_show_lag))){
					showLag = preferences.getBoolean(getString(R.string.preference_show_lag), false);
					if(!showLag) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
							setActionBarSubtitle("");
						} else {
							setTitle(getResources().getString(R.string.app_name));

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
	protected void onStart() {
		super.onStart();
		if(ThemeUtil.theme != currentTheme) {
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		BusProvider.getInstance().register(this);
		if(!Quasseldroid.connected) {
			returnToLogin();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		BusProvider.getInstance().unregister(this);

	}

	@Override
	protected void onDestroy() {
		preferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		long currentTime = System.currentTimeMillis();
		if(currentTime - lastBackPressed < BACK_THRESHOLD) super.onBackPressed(); 
		else {
			Toast.makeText(this, getString(R.string.pressed_back_toast), Toast.LENGTH_SHORT).show();
			lastBackPressed = currentTime;
		}
	}

	@Override
	public boolean onSearchRequested() {
		if(pager.getCurrentItem() == 1) {
			BusProvider.getInstance().post(new CompleteNickEvent());
			return false; //Activity ate the request
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.base_menu, menu);
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
			startActivity(new Intent(this, LoginActivity.class)); 
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public class FragmentAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {
		public static final int BUFFERS_POS = 0;
		public static final int CHAT_POS = 1;
		public static final int NICKS_POS = 2; 
		public static final int PAGE_COUNT = 3;
		public boolean chatShown = false;

		public FragmentAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case BUFFERS_POS:
				return BufferFragment.newInstance();
			case CHAT_POS:
				return ChatFragment.newInstance();
			case NICKS_POS:
				return NickListFragment.newInstance();
			default:
				return null;
			}
		}

		@Override
		public int getCount() {
			if (chatShown)
				return PAGE_COUNT;
			else
				return 1;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case BUFFERS_POS:
				return "Channels";
			case CHAT_POS:
				return "Chat";
			case NICKS_POS:
				return "Nicks";
			default:
				return super.getPageTitle(position);
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			// TODO Auto-generated method stub	
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			// TODO Auto-generated method stub	
		}

		@Override
		public void onPageSelected(int position) {
			if(position == BUFFERS_POS) {
				BusProvider.getInstance().post(new UpdateReadBufferEvent());
			}
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(pager.getWindowToken(), 0);
		}
	}

	@Subscribe
	public void onInitProgressed(InitProgressEvent event) {
		FragmentManager manager = getSupportFragmentManager();
		if(event.done) {
			if(manager.findFragmentById(R.id.connecting_fragment_container) != null) {
				FragmentTransaction trans = manager.beginTransaction();
				trans.remove(manager.findFragmentById(R.id.connecting_fragment_container));
				trans.commit();
				pager.setVisibility(View.VISIBLE);
				findViewById(R.id.connecting_fragment_container).setVisibility(View.GONE);

				//Doing this seems to fix a bug where menu items doesn't show up in the actionbar
				pager.setCurrentItem(FragmentAdapter.BUFFERS_POS);
			}
		} else {
			if(manager.findFragmentById(R.id.connecting_fragment_container) == null) {
				findViewById(R.id.connecting_fragment_container).setVisibility(View.VISIBLE);
				pager.setVisibility(View.GONE);
				FragmentManager fragmentManager = getSupportFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				ConnectingFragment fragment = ConnectingFragment.newInstance();
				fragmentTransaction.add(R.id.connecting_fragment_container, fragment, "connect");
				fragmentTransaction.commit();
			}

		}
	}

	@Subscribe
	public void onLatencyChanged(LatencyChangedEvent event) {
		if(showLag) {
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
		if(event.status == Status.Disconnected) {
			if(event.reason != "") {
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

	@Subscribe
	public void onBufferOpened(BufferOpenedEvent event) {
		if(event.bufferId != -1) {
			adapter.chatShown = true;
			openedBuffer = event.bufferId;
			setTitle(NetworkCollection.getInstance().getBufferById(event.bufferId).getInfo().name);
			pager.setCurrentItem(FragmentAdapter.CHAT_POS, true);
		}
	}
	
	 @Produce
	 public BufferOpenedEvent produceBufferOpenedEvent() {
		 return new BufferOpenedEvent(openedBuffer);
	 }
}
