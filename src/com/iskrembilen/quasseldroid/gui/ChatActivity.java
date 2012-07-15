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
import com.iskrembilen.quasseldroid.events.LatencyChangedEvent;
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


	public static final int MESSAGE_RECEIVED = 0;
	public static final String BUFFER_ID = "bufferid";
	private static final String BUFFER_NAME = "buffername";

	private BacklogAdapter adapter;
	private ListView backlogList;


	private int dynamicBacklogAmout;

	SharedPreferences preferences;
	OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

	private NickCompletionHelper nickCompletionHelper;

	private int bufferId;
	private int currentTheme;

	private static final String TAG = ChatActivity.class.getSimpleName();

	private Boolean showLag = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(ThemeUtil.theme);
		super.onCreate(savedInstanceState);
		currentTheme = ThemeUtil.theme;
		setContentView(R.layout.chat_layout);
		Intent intent = getIntent();

		if(intent.hasExtra(BUFFER_ID)) {
			bufferId = intent.getIntExtra(BUFFER_ID, 0);
		}

		initActionBar();

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		showLag = preferences.getBoolean(getString(R.string.preference_show_lag), false);

		adapter = new BacklogAdapter(this, null);
		backlogList = ((ListView)findViewById(R.id.chatBacklogList));
		backlogList.setAdapter(adapter);
		backlogList.setOnScrollListener(new BacklogScrollListener(5));
		backlogList.setSelection(backlogList.getChildCount());

		((ImageButton)findViewById(R.id.chat_auto_complete_button)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				EditText inputfield = (EditText)findViewById(R.id.ChatInputView);
				nickCompletionHelper.completeNick(inputfield);
			}
		});

		((EditText)findViewById(R.id.ChatInputView)).setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (event != null && event.getAction() == KeyEvent.ACTION_DOWN && 
						((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) || (event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER))) {
					EditText inputfield = (EditText)findViewById(R.id.ChatInputView);
					String inputText = inputfield.getText().toString();

					if (!"".equals(inputText)) {
						boundConnService.sendMessage(adapter.buffer.getInfo().id, inputText);
						InputHistoryHelper.addHistoryEntry(inputText);
						inputfield.setText("");
						InputHistoryHelper.tempStoreCurrentEntry("");
					}

					return true;
				}
				return false;
			}
		});

		((EditText)findViewById(R.id.ChatInputView)).setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_TAB && event.getAction() == KeyEvent.ACTION_DOWN) {
					onSearchRequested();
					return true;
				}
				if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
					EditText text = (EditText)v;
					InputHistoryHelper.tempStoreCurrentEntry(text.getText().toString());
					text.setText(InputHistoryHelper.getNextHistoryEntry());
					text.setSelection(text.getText().length());
					return true;
				}
				if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
					EditText text = (EditText)v;
					text.setText(InputHistoryHelper.getPreviousHistoryEntry());
					text.setSelection(text.getText().length());
					return true;
				}
				return false;
			}
		});

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
		EditText inputfield = (EditText)findViewById(R.id.ChatInputView);
		nickCompletionHelper.completeNick(inputfield);
		return false; //Activity ate the request
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.chat_menu, menu);
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
			break;
		case R.id.menu_disconnect:
			this.boundConnService.disconnectFromCore();
			finish();
			break;
		case R.id.menu_hide_events:
			showDialog(R.id.DIALOG_HIDE_EVENTS);
			break;
		case R.id.menu_users_list:
			openNickList(adapter.buffer);
			break;
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
			intent.putExtra(BUFFER_ID, bufferId);
			startActivity(intent);
		}
		dynamicBacklogAmout = Integer.parseInt(preferences.getString(getString(R.string.preference_dynamic_backlog), "10"));
		findViewById(R.id.chat_auto_complete_button).setEnabled(false);
		findViewById(R.id.ChatInputView).setEnabled(false);
		doBindService();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (adapter.buffer != null && boundConnService.isConnected()) {
			adapter.buffer.setDisplayed(false);

			//Dont save position if list is at bottom
			if (backlogList.getLastVisiblePosition()==adapter.getCount()-1) {
				adapter.buffer.setTopMessageShown(0);
			}else{
				adapter.buffer.setTopMessageShown(adapter.getListTopMessageId());
			}
			if (adapter.buffer.getUnfilteredSize()!= 0){
				boundConnService.setLastSeen(adapter.getBufferId(), adapter.buffer.getUnfilteredBacklogEntry(adapter.buffer.getUnfilteredSize()-1).messageId);
				boundConnService.markBufferAsRead(adapter.getBufferId());
				boundConnService.setMarkerLine(adapter.getBufferId(), adapter.buffer.getUnfilteredBacklogEntry(adapter.buffer.getUnfilteredSize()-1).messageId);
			}
		}
		doUnbindService();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(BUFFER_ID, bufferId);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		bufferId = savedInstanceState.getInt(BUFFER_ID);
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		//TODO: wtf rewrite this dialog in code creator shit, if it is possible, mabye it is an alert builder for a reason
		Dialog dialog;
		switch (id) {
		case R.id.DIALOG_HIDE_EVENTS:
			if(adapter.buffer == null) return null;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Hide Events");
			String[] filterList = IrcMessage.Type.getFilterList();
			boolean[] checked = new boolean[filterList.length];
			ArrayList<IrcMessage.Type> filters = adapter.buffer.getFilters();
			for (int i=0;i<checked.length;i++) {
				if(filters.contains(IrcMessage.Type.valueOf(filterList[i]))) {
					checked[i]=true;
				}else{
					checked[i]=false;
				}
			}
			builder.setMultiChoiceItems(filterList, checked, new OnMultiChoiceClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					IrcMessage.Type type = IrcMessage.Type.valueOf(IrcMessage.Type.getFilterList()[which]);
					if(isChecked)
						adapter.addFilter(type);
					else
						adapter.removeFilter(type);
				}
			});
			dialog = builder.create();
			break;

		default:
			dialog = null;
			break;
		}
		return dialog;
	}

	private void openNickList(Buffer buffer) {
		Intent i = new Intent(ChatActivity.this, NicksActivity.class);
		i.putExtra(BUFFER_ID, buffer.getInfo().id);
		i.putExtra(BUFFER_NAME, buffer.getInfo().name);
		startActivity(i);
	}

	public class BacklogAdapter extends BaseAdapter implements Observer {

		//private ArrayList<IrcMessage> backlog;
		private LayoutInflater inflater;
		private Buffer buffer;
		private ListView list = (ListView)findViewById(R.id.chatBacklogList);


		public BacklogAdapter(Context context, ArrayList<IrcMessage> backlog) {
			inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}

		public void setBuffer(Buffer buffer) {
			this.buffer = buffer;
			if ( buffer.getInfo().type == BufferInfo.Type.QueryBuffer ){
				((TextView)findViewById(R.id.chatNameView)).setText(buffer.getInfo().name);
			} else if ( buffer.getInfo().type == BufferInfo.Type.StatusBuffer ){
				((TextView)findViewById(R.id.chatNameView)).setText(buffer.getInfo().name + " ("
						+ boundConnService.getNetworkById(buffer.getInfo().networkId).getServer() + ") | "
						+ getResources().getString(R.string.users) + ": "
						+ boundConnService.getNetworkById(buffer.getInfo().networkId).getCountUsers() + " | "
						+ Helper.formatLatency(boundConnService.getNetworkById(buffer.getInfo().networkId).getLatency(), getResources()));
			} else{
				((TextView)findViewById(R.id.chatNameView)).setText(buffer.getInfo().name + ": " + buffer.getTopic());
			}
			notifyDataSetChanged();
			list.scrollTo(list.getScrollX(), list.getScrollY());
		}


		@Override
		public int getCount() {
			if (this.buffer==null) return 0;
			return buffer.getSize();
		}

		@Override
		public IrcMessage getItem(int position) {
			//TODO: QriorityQueue is fucked, we dont want to convert to array here, so change later
			return (IrcMessage) buffer.getBacklogEntry(position);
		}

		@Override
		public long getItemId(int position) {
			return buffer.getBacklogEntry(position).messageId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;

			if (convertView==null) {
				convertView = inflater.inflate(R.layout.backlog_item, null);
				holder = new ViewHolder();
				holder.timeView = (TextView)convertView.findViewById(R.id.backlog_time_view);
				holder.timeView.setTextColor(ThemeUtil.chatTimestampColor);
				holder.nickView = (TextView)convertView.findViewById(R.id.backlog_nick_view);
				holder.msgView = (TextView)convertView.findViewById(R.id.backlog_msg_view);
				holder.separatorView = (TextView)convertView.findViewById(R.id.backlog_list_separator);
				holder.item_layout = (LinearLayout)convertView.findViewById(R.id.backlog_item_linearlayout);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}

			//Set separator line here
			if (position != (getCount()-1) && (buffer.getMarkerLineMessage() == getItem(position).messageId || (buffer.isMarkerLineFiltered() && getItem(position).messageId<buffer.getMarkerLineMessage() && getItem(position+1).messageId>buffer.getMarkerLineMessage()))) { 
				holder.separatorView.getLayoutParams().height = 1;
			} else {
				holder.separatorView.getLayoutParams().height = 0;
			}

			IrcMessage entry = this.getItem(position);
			holder.messageID = entry.messageId;
			holder.timeView.setText(entry.getTime());

			switch (entry.type) {
			case Action:
				holder.nickView.setText("-*-");
				holder.msgView.setTextColor(ThemeUtil.chatActionColor);
				holder.nickView.setTextColor(ThemeUtil.chatActionColor);
				holder.msgView.setText(entry.getNick()+" "+entry.content);
				break;
			case Error:
				holder.nickView.setText("*");
				holder.msgView.setTextColor(ThemeUtil.chatErrorColor);
				holder.nickView.setTextColor(ThemeUtil.chatErrorColor);
				holder.msgView.setText(entry.content);
				break;
			case Server:
				holder.nickView.setText("*");
				holder.msgView.setTextColor(ThemeUtil.chatServerColor);
				holder.nickView.setTextColor(ThemeUtil.chatServerColor);
				holder.msgView.setText(entry.content);
				break;
			case Notice:
				holder.nickView.setText(entry.getNick());
				holder.msgView.setTextColor(ThemeUtil.chatNoticeColor);
				holder.nickView.setTextColor(ThemeUtil.chatNoticeColor);
				holder.msgView.setText(entry.content);
				break;
			case Join:
				holder.nickView.setText("-->");
				holder.msgView.setText(entry.getNick() + " has joined " + entry.content);
				holder.msgView.setTextColor(ThemeUtil.chatJoinColor);
				holder.nickView.setTextColor(ThemeUtil.chatJoinColor);
				break;
			case Part:
				holder.nickView.setText("<--");
				holder.msgView.setText(entry.getNick() + " has left (" + entry.content + ")");
				holder.msgView.setTextColor(ThemeUtil.chatPartColor);
				holder.nickView.setTextColor(ThemeUtil.chatPartColor);
				break;
			case Quit:				
				holder.nickView.setText("<--");
				holder.msgView.setText(entry.getNick() + " has quit (" + entry.content + ")");
				holder.msgView.setTextColor(ThemeUtil.chatQuitColor);
				holder.nickView.setTextColor(ThemeUtil.chatPartColor);
				break;
			case Kill:
				holder.nickView.setText("<--");
				holder.msgView.setText(entry.getNick() + " was killed (" + entry.content + ")");
				holder.msgView.setTextColor(ThemeUtil.chatKillColor);
				holder.nickView.setTextColor(ThemeUtil.chatKillColor);
				break;
			case Kick:
				holder.nickView.setText("<-*");
				int nickEnd = entry.content.toString().indexOf(" ");
				String nick = entry.content.toString().substring(0, nickEnd);
				String reason = entry.content.toString().substring(nickEnd+1);
				holder.msgView.setText(entry.getNick() + " has kicked " + nick + " from " + entry.bufferInfo.name + " (" + reason + ")");
				holder.msgView.setTextColor(ThemeUtil.chatKickColor);
				holder.nickView.setTextColor(ThemeUtil.chatKickColor);
				break;
			case Mode:
				holder.nickView.setText("***");
				holder.msgView.setText("Mode " + entry.content.toString() + " by " + entry.getNick());
				holder.msgView.setTextColor(ThemeUtil.chatModeColor);
				holder.nickView.setTextColor(ThemeUtil.chatModeColor);
				break;
			case Nick:
				holder.nickView.setText("<->");
				holder.msgView.setText(entry.getNick()+" is now known as " + entry.content.toString());
				holder.msgView.setTextColor(ThemeUtil.chatNickColor);
				holder.nickView.setTextColor(ThemeUtil.chatNickColor);
				break;
			case DayChange:
				holder.nickView.setText("-");
				holder.msgView.setText("{Day changed to " + entry.content.toString() + "}");
				holder.msgView.setTextColor(ThemeUtil.chatDayChangeColor);
				holder.nickView.setTextColor(ThemeUtil.chatDayChangeColor);
			case Plain:
			default:
				if(entry.isSelf()) {
					holder.nickView.setTextColor(ThemeUtil.chatSelfColor);
				}else{
					holder.nickView.setTextColor(entry.senderColor);
				}
				holder.msgView.setTextColor(ThemeUtil.chatPlainColor);
				holder.msgView.setTypeface(Typeface.DEFAULT);

				holder.nickView.setText("<" + entry.getNick() + ">");
				holder.msgView.setText(entry.content);
				break;
			}
			if (entry.isHighlighted()) {
				holder.item_layout.setBackgroundColor(ThemeUtil.chatHighlightColor);
			}else {
				holder.item_layout.setBackgroundResource(0);
			}
			//Log.i(TAG, "CONTENT:" + entry.content);
			return convertView;
		}

		@Override
		public void update(Observable observable, Object data) {
			if (data==null) {
				notifyDataSetChanged();
				return;
			}
			switch ((Integer)data) {
			case R.id.BUFFERUPDATE_NEWMESSAGE:
				notifyDataSetChanged();				
				break;
			case R.id.BUFFERUPDATE_BACKLOG:
				int topId = getListTopMessageId();
				notifyDataSetChanged();
				setListTopMessage(topId);
				break;
			default:
				notifyDataSetChanged();
			}

		}

		/*
		 * Returns the messageid for the ircmessage that is currently at the top of the screen
		 */
		public int getListTopMessageId() {
			int topId;
			if (list.getChildCount()==0) {
				topId = 0;
			}else {
				topId = ((ViewHolder)list.getChildAt(0).getTag()).messageID;
			}
			return topId;
		}

		/*
		 * Sets what message from the adapter will be at the top of the visible screen
		 */
		public void setListTopMessage(int messageid) {
			for(int i=0;i<adapter.getCount();i++){
				if (adapter.getItemId(i)==messageid){
					list.setSelectionFromTop(i,5);
					break;
				}
			}
		}

		public void stopObserving() {
			if(buffer != null) buffer.deleteObserver(this);

		}

		public void clearBuffer() {
			buffer = null;

		}

		public int getBufferId() {
			return buffer.getInfo().id;
		}

		public void getMoreBacklog() {
			adapter.buffer.setBacklogPending(ChatActivity.this.dynamicBacklogAmout);
			boundConnService.getMoreBacklog(adapter.getBufferId(),ChatActivity.this.dynamicBacklogAmout);
		}

		public void removeFilter(Type type) {
			buffer.removeFilterType(type);

		}

		public void addFilter(Type type) {
			buffer.addFilterType(type);

		}
	}	


	public static class ViewHolder {
		public TextView timeView;
		public TextView nickView;
		public TextView msgView;
		public TextView separatorView;
		public LinearLayout item_layout;

		public int messageID;
	}




	private class BacklogScrollListener implements OnScrollListener {

		private int visibleThreshold;
		private boolean loading = false;

		public BacklogScrollListener(int visibleThreshold) {
			this.visibleThreshold = visibleThreshold;
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			if (loading) {
				if (!adapter.buffer.hasPendingBacklog()) {
					loading = false;
				}
			}
			//			Log.d(TAG, "loading: "+ Boolean.toString(loading) +"totalItemCount: "+totalItemCount+ "visibleItemCount: " +visibleItemCount+"firstVisibleItem: "+firstVisibleItem+ "visibleThreshold: "+visibleThreshold);
			if (!loading && (firstVisibleItem <= visibleThreshold)) {
				if (adapter.buffer!=null) {
					loading = true;
					ChatActivity.this.adapter.getMoreBacklog();
				}else {
					Log.w(TAG, "Can't get backlog on null buffer");
				}

			}	

		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// Not interesting for us to use

		}

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
			Buffer buffer = boundConnService.getBuffer(bufferId, adapter);
			adapter.setBuffer(buffer);
			nickCompletionHelper = new NickCompletionHelper(buffer.getUsers().getUniqueUsers());
			findViewById(R.id.chat_auto_complete_button).setEnabled(true);
			findViewById(R.id.ChatInputView).setEnabled(true);

			buffer.setDisplayed(true);

			boundConnService.onHighlightsRead(buffer.getInfo().id);

			//Move list to correect position
			if (adapter.buffer.getTopMessageShown() == 0) {
				backlogList.setSelection(adapter.getCount()-1);
			}else{
				adapter.setListTopMessage(adapter.buffer.getTopMessageShown());
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
		bindService(new Intent(ChatActivity.this, CoreConnService.class), mConnection, Context.BIND_AUTO_CREATE);
		isBound = true;
		Log.i(TAG, "BINDING");
	}

	void doUnbindService() {
		if (isBound) {
			Log.i(TAG, "Unbinding service");
			// Detach our existing connection.
			adapter.stopObserving();
			unbindService(mConnection);
			isBound = false;

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
