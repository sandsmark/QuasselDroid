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

import android.annotation.TargetApi;
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
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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

import com.iskrembilen.quasseldroid.Buffer;
import com.iskrembilen.quasseldroid.BufferInfo;
import com.iskrembilen.quasseldroid.BufferUtils;
import com.iskrembilen.quasseldroid.Network;
import com.iskrembilen.quasseldroid.NetworkCollection;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.BufferOpenedEvent;
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent;
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent.Status;
import com.iskrembilen.quasseldroid.events.DisconnectCoreEvent;
import com.iskrembilen.quasseldroid.events.InitProgressEvent;
import com.iskrembilen.quasseldroid.events.LatencyChangedEvent;
import com.iskrembilen.quasseldroid.events.UpdateReadBufferEvent;
import com.iskrembilen.quasseldroid.gui.MainActivity.FragmentAdapter;
import com.iskrembilen.quasseldroid.gui.fragments.BufferFragment;
import com.iskrembilen.quasseldroid.gui.fragments.ChatFragment;
import com.iskrembilen.quasseldroid.gui.fragments.ConnectingFragment;
import com.iskrembilen.quasseldroid.service.CoreConnService;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.iskrembilen.quasseldroid.util.Helper;
import com.iskrembilen.quasseldroid.util.ThemeUtil;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Observable;
import java.util.Observer;

public class MainActivity extends FragmentActivity {

	private static final String TAG = MainActivity.class.getSimpleName();

	public static final String BUFFER_ID_EXTRA = "bufferid";
	public static final String BUFFER_NAME_EXTRA = "buffername";

	SharedPreferences preferences;
	OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

	private int currentTheme;
	private Boolean showLag = false;

	private ViewPager pager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(ThemeUtil.theme);
		super.onCreate(savedInstanceState);
		currentTheme = ThemeUtil.theme;
		setContentView(R.layout.main_layout);

		FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(adapter);
		pager.setOnPageChangeListener(adapter);

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

	@TargetApi(11)
	private void setActionBarSubtitle(String subtitle) {
		getActionBar().setSubtitle(subtitle);
	}

	@Override
	protected void onStart() {
		if(ThemeUtil.theme != currentTheme) {
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
		BusProvider.getInstance().register(this);
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		BusProvider.getInstance().unregister(this);

	}

	@Override
	protected void onDestroy() {
		preferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
		super.onDestroy();
	}

	@Override
	public boolean onSearchRequested() {
		if(pager.getCurrentItem() == 1) {
			getSupportFragmentManager().findFragmentById(R.id.chat_fragment_container);
			return false; //Activity ate the request
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.base_menu, menu);
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

	class ActionModeData {
		public int id;
		public View listItem;
		public ActionMode actionMode;
		public ActionMode.Callback actionModeCallbackNetwork;
		public ActionMode.Callback actionModeCallbackBuffer;
	}

	@Subscribe
	public void onConnectionChanged(ConnectionChangedEvent event) {
		if(event.status == Status.Disconnected) {
			if(event.reason != "") {
				removeDialog(R.id.DIALOG_CONNECTING);
				Toast.makeText(MainActivity.this.getApplicationContext(), event.reason, Toast.LENGTH_LONG).show();

			}
			finish();
			startActivity(new Intent(MainActivity.this, LoginActivity.class));
		}
	}

	public class FragmentAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {

		public FragmentAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if(position == 0) {
				return BufferFragment.newInstance();
			} else if(position == 1) {
				return ChatFragment.newInstance();
			}
			return null;
		}

		@Override
		public int getCount() {
			return 2;
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
			if(position == 0) {
				BusProvider.getInstance().post(new UpdateReadBufferEvent());
			}
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
	public void onBufferOpened(BufferOpenedEvent event) {
		pager.setCurrentItem(1);
	}
}
