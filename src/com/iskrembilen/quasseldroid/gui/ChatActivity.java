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
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.iskrembilen.quasseldroid.Buffer;
import com.iskrembilen.quasseldroid.BufferInfo;
import com.iskrembilen.quasseldroid.IrcMessage;
import com.iskrembilen.quasseldroid.IrcMessage.Type;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent;
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent.Status;
import com.iskrembilen.quasseldroid.events.DisconnectCoreEvent;
import com.iskrembilen.quasseldroid.events.LatencyChangedEvent;
import com.iskrembilen.quasseldroid.gui.fragments.ChatFragment;
import com.iskrembilen.quasseldroid.service.CoreConnService;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.iskrembilen.quasseldroid.util.Helper;
import com.iskrembilen.quasseldroid.util.InputHistoryHelper;
import com.iskrembilen.quasseldroid.util.NickCompletionHelper;
import com.iskrembilen.quasseldroid.util.SenderColorHelper;
import com.iskrembilen.quasseldroid.util.ThemeUtil;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class ChatActivity extends FragmentActivity {

	private static final String TAG = ChatActivity.class.getSimpleName();

	public static final int MESSAGE_RECEIVED = 0;

	
	SharedPreferences preferences;
	OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

	private int currentTheme;
	private Boolean showLag = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(ThemeUtil.theme);
		super.onCreate(savedInstanceState);
		currentTheme = ThemeUtil.theme;
		setContentView(R.layout.chat_layout);
		Intent intent = getIntent();
		if(intent.hasExtra(ChatFragment.BUFFER_ID)) {
			int bufferId = intent.getIntExtra(ChatFragment.BUFFER_ID, 0);
			((ChatFragment)getSupportFragmentManager().findFragmentById(R.id.chat_fragment)).setBuffer(bufferId);
		}

		initActionBar();

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		showLag = preferences.getBoolean(getString(R.string.preference_show_lag), false);



		sharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {

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

	@TargetApi(14)
	private void initActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onSearchRequested() {
		getSupportFragmentManager().findFragmentById(R.id.chat_fragment_container);
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
			Intent intent = new Intent(this, BufferActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		case R.id.menu_preferences:
			Intent i = new Intent(ChatActivity.this, PreferenceView.class);
			startActivity(i);
			return true;
		case R.id.menu_disconnect:
			BusProvider.getInstance().post(new DisconnectCoreEvent());
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		BusProvider.getInstance().register(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		BusProvider.getInstance().unregister(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if(ThemeUtil.theme != currentTheme) {
			Intent intent = new Intent(this, ChatActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			intent.putExtra(ChatFragment.BUFFER_ID, bufferId); //FIXME: do we need this with fragments?
			startActivity(intent);
		}
	}
	
	@Subscribe
	public void onConnectionChanged(ConnectionChangedEvent event) {
		if(event.status == Status.Disconnected) {
			Log.d(TAG, "Getting result disconnected");
			finish();				
		}
	}
	@Subscribe
	public void onLatencyChanged(LatencyChangedEvent event) {
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
