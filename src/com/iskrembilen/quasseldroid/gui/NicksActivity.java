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

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

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
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.iskrembilen.quasseldroid.Buffer;
import com.iskrembilen.quasseldroid.BufferInfo;
import com.iskrembilen.quasseldroid.IrcMessage;
import com.iskrembilen.quasseldroid.IrcUser;
import com.iskrembilen.quasseldroid.IrcMessage.Type;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.UserCollection;
import com.iskrembilen.quasseldroid.service.CoreConnService;

public class NicksActivity extends Activity{

	private static final String TAG = NicksActivity.class.getSimpleName();
	private ResultReceiver statusReceiver;
	private NicksAdapter adapter;
	private ExpandableListView list;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.nick_layout);

		adapter = new NicksAdapter(this);
		list = (ExpandableListView)findViewById(R.id.userList);
		list.setAdapter(adapter);
		statusReceiver = new ResultReceiver(null) {

			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				if (resultCode==CoreConnService.CONNECTION_DISCONNECTED) finish();
				super.onReceiveResult(resultCode, resultData);
			}

		};
	}

	@Override
	protected void onStart() {
		super.onStart();
		doBindService();
	}

	@Override
	protected void onStop() {
		super.onStop();
		doUnbindService();
	}

	public class NicksAdapter extends BaseExpandableListAdapter implements Observer{

		private LayoutInflater inflater;
		private UserCollection users;
		public NicksAdapter(Context context) {
			inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.users = null;

		}

		public void setUsers(UserCollection users) {
			users.addObserver(this);
			this.users = users;
			notifyDataSetChanged();
			for(int i=0; i<adapter.getGroupCount();i++) {
				list.expandGroup(i);
			}
		}

		@Override
		public void update(Observable observable, Object data) {
			if(data == null) {
				return;
			}
			switch ((Integer)data) {
			case R.id.BUFFERUPDATE_USERSCHANGED:
				notifyDataSetChanged();				
				break;			
			}
		}

		public void stopObserving() {
			users.deleteObserver(this);

		}

		@Override
		public IrcUser getChild(int groupPosition, int childPosition) {
			return getGroup(groupPosition).get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return groupPosition*10 + childPosition;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {

			ViewHolderChild holder = null;

			if (convertView==null) {
				convertView = inflater.inflate(R.layout.nicklist_item, null);
				holder = new ViewHolderChild();
				holder.nickView = (TextView)convertView.findViewById(R.id.nicklist_nick_view);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolderChild)convertView.getTag();
			}
			IrcUser entry = getChild(groupPosition, childPosition);
			holder.nickView.setText(entry.nick);
			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			if (this.users==null) return 0;
			return getGroup(groupPosition).size();
		}

		@Override
		public List<IrcUser> getGroup(int groupPosition) {
			switch (groupPosition) {
			case 0:
				return users.getOperators();
			case 1:
				return users.getVoiced();
			case 2:
				return users.getUsers();
			}
			return null;
		}

		@Override
		public int getGroupCount() {
			return 3;
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			ViewHolderGroup holder = null;

			if (convertView==null) {
				convertView = inflater.inflate(R.layout.nicklist_group_item, null);
				holder = new ViewHolderGroup();
				holder.nameView = (TextView)convertView.findViewById(R.id.nicklist_group_name_view);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolderGroup)convertView.getTag();
			}
			switch (groupPosition) {
			case 0:
				holder.nameView.setText("Operators");
				break;
			case 1:
				holder.nameView.setText("Voiced");
				break;
			case 2:
				holder.nameView.setText("Users");
				break;
			}
			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}
	}


	public static class ViewHolderChild {
		public TextView nickView;
	}
	public static class ViewHolderGroup {
		public TextView nameView;
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
			Log.d(TAG, "BINDING ON SERVICE DONE");
			boundConnService = ((CoreConnService.LocalBinder)service).getService();

			Intent intent = getIntent();
			Buffer buffer = boundConnService.getBuffer(intent.getIntExtra(BufferActivity.BUFFER_ID_EXTRA, 0), null);
			adapter.setUsers(buffer.getUsers());

			boundConnService.registerStatusReceiver(statusReceiver);
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
		bindService(new Intent(NicksActivity.this, CoreConnService.class), mConnection, Context.BIND_AUTO_CREATE);
		isBound = true;
		Log.i(TAG, "BINDING");
	}

	void doUnbindService() {
		if (isBound) {
			Log.i(TAG, "Unbinding service");
			// Detach our existing connection.
			adapter.stopObserving();
			boundConnService.unregisterStatusReceiver(statusReceiver);
			unbindService(mConnection);
			isBound = false;

		}
	}
}
