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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.iskrembilen.quasseldroid.Buffer;
import com.iskrembilen.quasseldroid.BufferInfo;
import com.iskrembilen.quasseldroid.IrcMessage;
import com.iskrembilen.quasseldroid.UserCollection;
import com.iskrembilen.quasseldroid.IrcMessage.Type;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.service.CoreConnService;

public class ChatActivity extends Activity{


	public static final int MESSAGE_RECEIVED = 0;
	private static final String BUFFER_ID_EXTRA = "bufferid";
	private static final String BUFFER_NAME_EXTRA = "buffername";

	private BacklogAdapter adapter;
	private ListView backlogList;


	private int dynamicBacklogAmout;

	SharedPreferences preferences;

	private ResultReceiver statusReceiver;

	private static final String TAG = ChatActivity.class.getSimpleName();



	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.chat_layout);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		adapter = new BacklogAdapter(this, null);
		backlogList = ((ListView)findViewById(R.id.chatBacklogList));
		backlogList.setCacheColorHint(0xffffff);
		backlogList.setAdapter(adapter);
		backlogList.setOnScrollListener(new BacklogScrollListener(5));
		backlogList.setDividerHeight(0);
		backlogList.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		//View v = backlogList.getChildAt(backlogList.getChildCount());
		backlogList.setSelection(backlogList.getChildCount());

		findViewById(R.id.ChatInputView).setOnKeyListener(inputfieldKeyListener);
		backlogList.setOnItemLongClickListener(itemLongClickListener);
		((ListView) findViewById(R.id.chatBacklogList)).setCacheColorHint(0xffffff);

		statusReceiver = new ResultReceiver(null) {

			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				if (resultCode==CoreConnService.CONNECTION_DISCONNECTED) finish();
				super.onReceiveResult(resultCode, resultData);
			}

		};
	}


	OnItemLongClickListener itemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			IrcMessage message = adapter.getItem(position);
			if (message.hasURLs()) {
				ArrayList<String> urls = (ArrayList<String>) message.getURLs();

				if (urls.size() == 1 ){ //Open the URL
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urls.get(0)));
					startActivity(browserIntent);
				} else if (urls.size() > 1 ){
					//Show list of urls, and make it possible to choose one
				}
			}
			return false;
		}
	};

	private OnKeyListener inputfieldKeyListener =  new View.OnKeyListener() {
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction()==KeyEvent.ACTION_DOWN ) { //On key down as well
				EditText inputfield = (EditText)findViewById(R.id.ChatInputView);
				String inputText = inputfield.getText().toString();

				if ( ! "".equals(inputText) ) {
					boundConnService.sendMessage(adapter.buffer.getInfo().id, inputText);
					inputfield.setText("");
				}

				return true;
			} else if (keyCode == KeyEvent.KEYCODE_TAB && event.getAction() == KeyEvent.ACTION_DOWN) {
				onSearchRequested(); // lawl
				return true;
			}
			return false;
		}
	};


	//TODO: fix this again after changing from string to ircusers
	//Nick autocomplete when pressing the search-button
	@Override
	public boolean onSearchRequested() {
		EditText inputfield = (EditText)findViewById(R.id.ChatInputView);
		String inputString = inputfield.getText().toString();
		String[] inputWords = inputString.split(" ");
		String inputNick = inputWords[inputWords.length-1];
		int inputLength = inputString.lastIndexOf(" ") == -1 ? 0: inputString.substring(0, inputString.lastIndexOf(" ")).length();
		UserCollection userColl = adapter.buffer.getUsers();
		
		if ( "".equals(inputNick) ) {
			if ( userColl.getOperators().size() > 0 ) {
				inputfield.setText(userColl.getOperators().get(0).getNick()+ ": ");
				inputfield.setSelection(userColl.getOperators().get(0).getNick().length() + 2);
			}
		} else {
			if (matchAndSetNick(inputNick, userColl.getOperators())){}
			else if (matchAndSetNick(inputNick, userColl.getVoiced())) {}
			else if (matchAndSetNick(inputNick, userColl.getUsers())) {}
		}
		return false;  // don't go ahead and show the search box
	}

	private boolean matchAndSetNick(String input, List<IrcUser> userList) {
		for (IrcUser user : userList) {
			if ( user.getNick().matches("(?i)"+inputNick+".*")  ) { //Matches the start of the string
				String additional = inputWords.length > 1 ? " ": ": ";
				inputfield.setText(inputString.substring(0, inputLength) + (inputLength >0 ? " ":"") + nick+  additional);
				inputfield.setSelection(inputLength + (inputLength >0 ? 1:0) + nick.length() + additional.length());
				return true;
			}
		}
		return false;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.chat_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_preferences:
			Intent i = new Intent(ChatActivity.this, PreferenceView.class);
			startActivity(i);
			break;
		case R.id.menu_disconnect:
			this.boundConnService.disconnectFromCore();
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
	protected void onStart() {
		super.onStart();
		dynamicBacklogAmout = Integer.parseInt(preferences.getString(getString(R.string.preference_dynamic_backlog), "10"));
		doBindService();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (adapter.buffer == null) return;
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
		doUnbindService();
	}



	@Override
	protected Dialog onCreateDialog(int id) {
		//TODO: wtf rewrite this dialog in code creator shit, if it is possible, mabye it is an alert builder for a reason
		Dialog dialog;
		switch (id) {
		case R.id.DIALOG_HIDE_EVENTS:
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
		i.putExtra(BUFFER_ID_EXTRA, buffer.getInfo().id);
		i.putExtra(BUFFER_NAME_EXTRA, buffer.getInfo().name);
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
				((TextView)findViewById(R.id.chatNameView)).setText(buffer.getInfo().name); //TODO: Add which server we are connected to
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
				holder.msgView.setTextColor(getResources().getColor(R.color.ircmessage_actionmessage_color));
				holder.nickView.setTextColor(getResources().getColor(R.color.ircmessage_actionmessage_color));
				holder.msgView.setText(entry.getNick()+" "+entry.content);
				break;
			case Server:
				holder.nickView.setText("*");
				holder.msgView.setTextColor(getResources().getColor(R.color.ircmessage_servermessage_color));
				holder.nickView.setTextColor(getResources().getColor(R.color.ircmessage_servermessage_color));
				holder.msgView.setText(entry.content);
				break;
			case Join:
				holder.nickView.setText("-->");
				holder.msgView.setText(entry.getNick() + " has joined " + entry.content);
				holder.msgView.setTextColor(getResources().getColor(R.color.ircmessage_commandmessages_color));
				holder.nickView.setTextColor(getResources().getColor(R.color.ircmessage_commandmessages_color));
				break;
			case Part:
				holder.nickView.setText("<--");
				holder.msgView.setText(entry.getNick() + " has left (" + entry.content + ")");
				holder.msgView.setTextColor(getResources().getColor(R.color.ircmessage_commandmessages_color));
				holder.nickView.setTextColor(getResources().getColor(R.color.ircmessage_commandmessages_color));
				break;
			case Quit:				
				holder.nickView.setText("<--");
				holder.msgView.setText(entry.getNick() + " has quit (" + entry.content + ")");
				holder.msgView.setTextColor(getResources().getColor(R.color.ircmessage_commandmessages_color));
				holder.nickView.setTextColor(getResources().getColor(R.color.ircmessage_commandmessages_color));
				break;
				//TODO: implement the rest
			case Kick:
				holder.nickView.setText("<-*");
				int nickEnd = entry.content.toString().indexOf(" ");
				String nick = entry.content.toString().substring(0, nickEnd);
				String reason = entry.content.toString().substring(nickEnd+1);
				holder.msgView.setText(entry.getNick() + " has kicked " + nick + " from " + entry.bufferInfo.name + " (" + reason + ")");
				holder.msgView.setTextColor(getResources().getColor(R.color.ircmessage_commandmessages_color));
				holder.nickView.setTextColor(getResources().getColor(R.color.ircmessage_commandmessages_color));
				break;

			case Mode:
				holder.nickView.setText("***");
				holder.msgView.setText("Mode " + entry.content.toString() + " by " + entry.getNick());
				holder.msgView.setTextColor(getResources().getColor(R.color.ircmessage_commandmessages_color));
				holder.nickView.setTextColor(getResources().getColor(R.color.ircmessage_commandmessages_color));
				break;
			case Nick:
				holder.nickView.setText("<->");
				holder.msgView.setText(entry.getNick()+" is now known as " + entry.content.toString());
				holder.msgView.setTextColor(getResources().getColor(R.color.ircmessage_commandmessages_color));
				holder.nickView.setTextColor(getResources().getColor(R.color.ircmessage_commandmessages_color));
				break;
			case Plain:
			default:
				if(entry.isSelf()) {
					holder.nickView.setTextColor(Color.BLACK); //TODO: probably move to color file, or somewhere else it needs to be, so user can select color them self
				}else{
					int hashcode = entry.getNick().hashCode() & 0x00FFFFFF;
					holder.nickView.setTextColor(Color.rgb(hashcode & 0xFF0000, hashcode & 0xFF00, hashcode & 0xFF));
				}
				holder.msgView.setTextColor(0xff000000);
				holder.msgView.setTypeface(Typeface.DEFAULT);

				holder.nickView.setText("<" + entry.getNick() + ">");
				holder.msgView.setText(entry.content);
				break;
			}
			if (entry.isHighlighted()) {
				holder.item_layout.setBackgroundResource(R.color.ircmessage_highlight_color);
			}else {
				holder.item_layout.setBackgroundResource(R.color.ircmessage_normal_color);
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
			buffer.deleteObserver(this);

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

			Intent intent = getIntent();
			//Testing to see if i can add item to adapter in service
			Buffer buffer = boundConnService.getBuffer(intent.getIntExtra(BufferActivity.BUFFER_ID_EXTRA, 0), adapter);
			adapter.setBuffer(buffer);
			buffer.setDisplayed(true);
			if(buffer.hasUnseenHighlight()) {
				boundConnService.onHighlightsRead(buffer.getInfo().id);
			}

			//Move list to correect position
			if (adapter.buffer.getTopMessageShown() == 0) {
				backlogList.setSelection(adapter.getCount()-1);
			}else{
				adapter.setListTopMessage(adapter.buffer.getTopMessageShown());
			}

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
		bindService(new Intent(ChatActivity.this, CoreConnService.class), mConnection, Context.BIND_AUTO_CREATE);
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
