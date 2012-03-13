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

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.ExpandableListActivity;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.iskrembilen.quasseldroid.Buffer;
import com.iskrembilen.quasseldroid.BufferCollection;
import com.iskrembilen.quasseldroid.BufferUtils;
import com.iskrembilen.quasseldroid.Network;
import com.iskrembilen.quasseldroid.NetworkCollection;
import com.iskrembilen.quasseldroid.service.CoreConnService;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.IrcUser;


public class BufferActivity extends ExpandableListActivity {

	private static final String TAG = BufferActivity.class.getSimpleName();

	public static final String BUFFER_ID_EXTRA = "bufferid";
	public static final String BUFFER_NAME_EXTRA = "buffername";

	private static final String ITEM_POSITION_KEY = "itempos";

	private static final String LIST_POSITION_KEY = "listpos";

	BufferListAdapter bufferListAdapter;

	ResultReceiver statusReciver;

	SharedPreferences preferences;
	OnSharedPreferenceChangeListener listener;

	private int restoreListPosition = 0;
	private int restoreItemPosition = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState != null) {
			restoreListPosition = savedInstanceState.getInt(LIST_POSITION_KEY);
			restoreItemPosition = savedInstanceState.getInt(ITEM_POSITION_KEY);
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.buffer_list);
		//bufferList = new ArrayList<Buffer>();

		bufferListAdapter = new BufferListAdapter(this);
		getExpandableListView().setDividerHeight(0);
		getExpandableListView().setCacheColorHint(0xffffffff);
		//		registerForContextMenu(getListView());

		statusReciver = new ResultReceiver(null) {

			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				if (resultCode==CoreConnService.CONNECTION_DISCONNECTED) finish();
				else if(resultCode==CoreConnService.INIT_PROGRESS) {
					((TextView)findViewById(R.id.buffer_list_progress_text)).setText(resultData.getString(CoreConnService.PROGRESS_KEY));
				}else if(resultCode==CoreConnService.INIT_DONE) {
					setListAdapter(bufferListAdapter);
					bufferListAdapter.setNetworks(boundConnService.getNetworkList(bufferListAdapter));
				}
				super.onReceiveResult(resultCode, resultData);
			}

		};

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		listener =new OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if(key.equals(getResources().getString(R.string.preference_fontsize_channel_list))){
					bufferListAdapter.notifyDataSetChanged();
				}

			}
		};
		preferences.registerOnSharedPreferenceChangeListener(listener); //To avoid GC issues
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (boundConnService == null) return;
	}

	@Override
	protected void onStart() {
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
		preferences.unregisterOnSharedPreferenceChangeListener(listener);
		super.onDestroy();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	    ExpandableListView listView = getExpandableListView();
	    // Save position of first visible item
	    restoreListPosition = listView.getFirstVisiblePosition();
	    outState.putInt(LIST_POSITION_KEY, restoreListPosition);

	    // Save scroll position of item
	    View itemView = listView.getChildAt(0);
	    restoreItemPosition = itemView == null ? 0 : itemView.getTop();
	    outState.putInt(ITEM_POSITION_KEY, restoreItemPosition);

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.standard_menu, menu);
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
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		int bufferId = (int) ((AdapterView.AdapterContextMenuInfo)menuInfo).id;
		if (bufferListAdapter.networks.getBufferById(bufferId).isActive()) {
			menu.add(Menu.NONE, R.id.CONTEXT_MENU_PART, Menu.NONE, "Part");			
		}else{
			menu.add(Menu.NONE, R.id.CONTEXT_MENU_JOIN, Menu.NONE, "Join");
		}
	}



	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.CONTEXT_MENU_JOIN:
			boundConnService.sendMessage((int)info.id, "/join "+bufferListAdapter.networks.getBufferById((int)info.id).getInfo().name);
			return true;
		case R.id.CONTEXT_MENU_PART:
			boundConnService.sendMessage((int)info.id, "/part "+bufferListAdapter.networks.getBufferById((int)info.id).getInfo().name);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}



	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		openBuffer(bufferListAdapter.getChild(groupPosition, childPosition));
		return true;
	}

	@Override
	public void onGroupExpand(int groupPosition) {
		bufferListAdapter.getGroup(groupPosition).setOpen(true);
	}

	@Override
	public void onGroupCollapse(int groupPosition) {
		bufferListAdapter.getGroup(groupPosition).setOpen(false);
	}

	private void openBuffer(Buffer buffer) {
		Intent i = new Intent(BufferActivity.this, ChatActivity.class);
		i.putExtra(BUFFER_ID_EXTRA, buffer.getInfo().id);
		i.putExtra(BUFFER_NAME_EXTRA, buffer.getInfo().name);
		startActivity(i);
	}

	public class BufferListAdapter extends BaseExpandableListAdapter implements Observer {
		private NetworkCollection networks;
		private LayoutInflater inflater;

		public BufferListAdapter(Context context) {
			inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}

		public void setNetworks(NetworkCollection networks){
			this.networks = networks;
			if (networks == null)
				return;
			networks.addObserver(this);
			notifyDataSetChanged();
			if(getExpandableListAdapter() != null) {
				for(int group = 0; group < getGroupCount(); group++) {
					if(getGroup(group).isOpen()) getExpandableListView().expandGroup(group);
					else getExpandableListView().collapseGroup(group);
				}
				getExpandableListView().setSelectionFromTop(restoreListPosition, restoreItemPosition);
			}
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
		}

		@Override
		public void update(Observable observable, Object data) {
			notifyDataSetChanged();
		}

		@Override
		public Buffer getChild(int groupPosition, int childPosition) {
			return networks.getNetwork(groupPosition).getBuffers().getPos(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return networks.getNetwork(groupPosition).getBuffers().getPos(childPosition).getInfo().id;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			ViewHolderChild holder = null;
			if (convertView==null) {
				convertView = inflater.inflate(R.layout.buffer_child_item, null);
				holder = new ViewHolderChild();
				holder.bufferView = (TextView)convertView.findViewById(R.id.buffer_list_item_name);
				holder.bufferImage = (ImageView)convertView.findViewById(R.id.buffer_list_item_image);
				holder.bufferView.setTextSize(TypedValue.COMPLEX_UNIT_DIP , Float.parseFloat(preferences.getString(getString(R.string.preference_fontsize_channel_list), ""+holder.bufferView.getTextSize())));
				convertView.setTag(holder);
			} else {
				holder = (ViewHolderChild)convertView.getTag();
			}
			Buffer entry = getChild(groupPosition, childPosition);
			switch (entry.getInfo().type) {
			case StatusBuffer:
			case ChannelBuffer:
				holder.bufferView.setText(entry.getInfo().name);
				if(entry.isActive()) holder.bufferImage.setImageResource(R.drawable.irc_channel_active);
				else holder.bufferImage.setImageResource(R.drawable.irc_channel_inactive);
				break;
			case QueryBuffer:
				String nick = entry.getInfo().name;
				//				if (boundConnService.hasUser(nick)){
				//					nick += boundConnService.getUser(nick).away ? " (Away)": "";
				//				}

				List<IrcUser> nickList = this.networks.getNetwork(groupPosition).getUserList();
				
				int nick_found = 0;                     
				         
				for (IrcUser ircuser : nickList) {
					if (ircuser.name.startsWith(nick)) {
						nick_found  = 1;
					    if (ircuser.away) {
					    	nick += " (away)";
					    }
					    break;
					}
				
				}
				if (nick_found == 0){
					nick += " (off)";
				}

				holder.bufferView.setText(nick);
				holder.bufferImage.setImageResource(R.drawable.im_user);
				
				break;
			case GroupBuffer:
			case InvalidBuffer:
				holder.bufferView.setText("XXXX " + entry.getInfo().name);
			}				

			BufferUtils.setBufferViewStatus(BufferActivity.this, entry, holder.bufferView);
			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			if (networks==null) {
				return 0;
			}else {
				return networks.getNetwork(groupPosition).getBuffers().getBufferCount();
			}
		}

		@Override
		public Network getGroup(int groupPosition) {
			return networks.getNetwork(groupPosition);
		}

		@Override
		public int getGroupCount() {
			if (networks==null) {
				return 0;
			}else {
				return networks.size();
			}
		}

		@Override
		public long getGroupId(int groupPosition) {
			return networks.getNetwork(groupPosition).getId();
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			ViewHolderGroup holder = null;
			if (convertView==null) {
				convertView = inflater.inflate(R.layout.buffer_group_item, null);
				holder = new ViewHolderGroup();
				holder.statusView = (TextView)convertView.findViewById(R.id.buffer_list_item_name);
				holder.statusView.setTextSize(TypedValue.COMPLEX_UNIT_DIP , Float.parseFloat(preferences.getString(getString(R.string.preference_fontsize_channel_list), ""+holder.statusView.getTextSize())));
				holder.statusView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if(getGroup((Integer) v.getTag()).getStatusBuffer() != null)
							openBuffer(getGroup((Integer) v.getTag()).getStatusBuffer());
					}
				});
				convertView.setTag(holder);
			} else {
				holder = (ViewHolderGroup)convertView.getTag();
			}
			Network entry = getGroup(groupPosition);
			holder.statusView.setText(entry.getName());
			holder.statusView.setTag(groupPosition); //Used in click listener to know what item this is
			BufferUtils.setBufferViewStatus(BufferActivity.this, entry.getStatusBuffer(), holder.statusView);
			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		public void clearBuffers() {
			networks = null;
			notifyDataSetChanged();
		}	

		public void stopObserving() {
			if (networks == null) return;
			for(Network network : networks.getNetworkList())
				network.deleteObserver(this);
		}

	}

	public static class ViewHolderChild {
		public ImageView bufferImage;
		public TextView bufferView;
	}
	public static class ViewHolderGroup {
		public TextView statusView;
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

			boundConnService.registerStatusReceiver(statusReciver);

			//Testing to see if i can add item to adapter in service
			if(boundConnService.isInitComplete()) { 
				setListAdapter(bufferListAdapter);
				bufferListAdapter.setNetworks(boundConnService.getNetworkList(bufferListAdapter));
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
			bufferListAdapter.stopObserving();
			if (boundConnService != null)
				boundConnService.unregisterStatusReceiver(statusReciver);
			// Detach our existing connection.
			unbindService(mConnection);
			isBound = false;
			bufferListAdapter.clearBuffers();
			setListAdapter(null);
		}
	}
}
