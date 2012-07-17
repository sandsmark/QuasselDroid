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
import android.support.v4.app.FragmentActivity;
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
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent;
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent.Status;
import com.iskrembilen.quasseldroid.events.InitProgressEvent;
import com.iskrembilen.quasseldroid.events.LatencyChangedEvent;
import com.iskrembilen.quasseldroid.gui.fragments.ConnectingFragment;
import com.iskrembilen.quasseldroid.service.CoreConnService;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.iskrembilen.quasseldroid.util.Helper;
import com.iskrembilen.quasseldroid.util.ThemeUtil;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Observable;
import java.util.Observer;

public class BufferActivity extends FragmentActivity {

	private static final String TAG = BufferActivity.class.getSimpleName();

	public static final String BUFFER_ID_EXTRA = "bufferid";
	public static final String BUFFER_NAME_EXTRA = "buffername";

	private static final String ITEM_POSITION_KEY = "itempos";

	private static final String LIST_POSITION_KEY = "listpos";

	SharedPreferences preferences;
	OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

	private int currentTheme;

	private Boolean showLag = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(ThemeUtil.theme);
		super.onCreate(savedInstanceState);
		currentTheme = ThemeUtil.theme;
		setContentView(R.layout.buffer_list);
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
		if(ThemeUtil.theme != currentTheme) {
			Intent intent = new Intent(this, BufferActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
		doBindService();
		super.onStart();
	}

	@Override
	protected void onStop() {
		doUnbindService();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		preferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.buffer_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_preferences:
			Intent i = new Intent(BufferActivity.this, PreferenceView.class);
			startActivity(i);
			break;
		case R.id.menu_disconnect:
			this.boundConnService.disconnectFromCore();
			startActivity(new Intent(this, LoginActivity.class));
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Code for service binding:
	 */
	private CoreConnService boundConnService;
	private Boolean isBound;

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. Because we have bound to a explicit
			// service that we know is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			Log.i(TAG, "BINDING ON SERVICE DONE");
			boundConnService = ((CoreConnService.LocalBinder)service).getService();

			//Testing to see if i can add item to adapter in service
			if(boundConnService.isInitComplete()) { 
				//bufferListAdapter.setNetworks(boundConnService.getNetworkList(bufferListAdapter));
			}


		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			// Because it is running in our same process, we should never
			// see this happen.
			boundConnService = null;

		}
	};

	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).

		// Send a ResultReciver with the intent to the service, so that we can 
		// get a notification if the connection status changes like we disconnect. 

		bindService(new Intent(BufferActivity.this, CoreConnService.class), mConnection, Context.BIND_AUTO_CREATE);
		isBound = true;
		Log.i(TAG, "BINDING");
	}

	void doUnbindService() {
		if (isBound) {
			Log.i(TAG, "Unbinding service");
			// Detach our existing connection.
			unbindService(mConnection);
			isBound = false;;
		}
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
				Toast.makeText(BufferActivity.this.getApplicationContext(), event.reason, Toast.LENGTH_LONG).show();

			}
			finish();
			startActivity(new Intent(BufferActivity.this, LoginActivity.class));
		}
	}

	@Subscribe
	public void onInitProgressed(InitProgressEvent event) {
		if(event.done) {
			//TODO: add fragment transaction				
		} else {
			((ConnectingFragment)getSupportFragmentManager().findFragmentById(1)).updateProgress(event.progress); //TODO TEMP ID

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
}
