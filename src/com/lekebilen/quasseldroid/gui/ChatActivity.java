package com.lekebilen.quasseldroid.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import com.lekebilen.quasseldroid.Buffer;
import com.lekebilen.quasseldroid.BufferInfo;
import com.lekebilen.quasseldroid.IrcMessage;
import com.lekebilen.quasseldroid.R;
import com.lekebilen.quasseldroid.service.CoreConnService;

public class ChatActivity extends Activity{


	public static final int MESSAGE_RECEIVED = 0;

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
			if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction()==0 ) { //On key down as well
				EditText inputfield = (EditText)findViewById(R.id.ChatInputView);
				String inputText = inputfield.getText().toString();

				if ( ! "".equals(inputText) ) {
					boundConnService.sendMessage(adapter.buffer.getInfo().id, inputText);
					inputfield.setText("");
				}

				return true;
			} else if (keyCode == KeyEvent.KEYCODE_TAB && event.getAction() == 0) {
				onSearchRequested(); // lawl
			}
			return false;
		}
	};


	//Nick autocomplete when pressing the search-button
	@Override
	public boolean onSearchRequested() {
		EditText inputfield = (EditText)findViewById(R.id.ChatInputView); 
		String inputNick = inputfield.getText().toString();

		if ( "".equals(inputNick) ) {
			if ( adapter.buffer.getNicks().size() > 0 ) {
				inputfield.setText(adapter.buffer.getNicks().get(0)+ ": ");
				inputfield.setSelection(adapter.buffer.getNicks().get(0).length() + 2);
			}
		} else {
			for (String nick : adapter.buffer.getNicks()) {
				if ( nick.matches("(?i)"+inputNick+".*")  ) { //Matches the start of the string
					inputfield.setText(nick+ ": ");
					inputfield.setSelection(nick.length() + 2);
					break;
				}
			}
		}
		return false;  // don't go ahead and show the search box
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
			Intent i = new Intent(ChatActivity.this, PreferenceView.class);
			startActivity(i);
			break;
		case R.id.menu_disconnect:
			this.boundConnService.disconnectFromCore();
			break;
		}
		return super.onOptionsItemSelected(item);
	}



	@Override
	protected void onStart() {
		doBindService();
		dynamicBacklogAmout = Integer.parseInt(preferences.getString(getString(R.string.preference_dynamic_backlog), "10"));
		super.onStart();
	}

	@Override
	protected void onStop() {
		//Dont save position if list is at bottom
		if (backlogList.getLastVisiblePosition()==adapter.getCount()-1) {
			adapter.buffer.setTopMessageShown(0);
		}else{
			adapter.buffer.setTopMessageShown(adapter.getListTopMessageId());
		}
		boundConnService.markBufferAsRead(adapter.getBufferId());
		doUnbindService();
		super.onStop();
	}



	public class BacklogAdapter extends BaseAdapter implements Observer {

		//private ArrayList<IrcMessage> backlog;
		private LayoutInflater inflater;
		private Buffer buffer;
		private ListView list = (ListView)findViewById(R.id.chatBacklogList);


		public BacklogAdapter(Context context, ArrayList<IrcMessage> backlog) {
			//			if (backlog==null) {
			//				this.backlog = new ArrayList<IrcMessage>();
			//			}else {
			//				this.backlog = backlog;				
			//			}
			inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}

		//		public void addItem(IrcMessage item) {
		//			Log.i(TAG, item.timestamp.toString());
		//			//this.backlog.add(item);
		//			notifyDataSetChanged();
		//		}

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
			if (buffer.getLastSeenMessage() == getItem(position).messageId && position != (getCount()-1)) { 
				holder.separatorView.getLayoutParams().height = 1;
			} else {
				holder.separatorView.getLayoutParams().height = 0;
			}

			IrcMessage entry = this.getItem(position);
			holder.messageID = entry.messageId;
			holder.timeView.setText(entry.getTime());
			
			
			switch (entry.type) {
			case Action:
			case Server:
				holder.nickView.setText("*");
				holder.msgView.setTypeface(Typeface.DEFAULT_BOLD);
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
			//Log.d(TAG, "loading: "+ Boolean.toString(loading) +"totalItemCount: "+totalItemCount+ "visibleItemCount: " +visibleItemCount+"firstVisibleItem: "+firstVisibleItem+ "visibleThreshold: "+visibleThreshold);
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
			adapter.setBuffer(boundConnService.getBuffer(intent.getIntExtra(BufferActivity.BUFFER_ID_EXTRA, 0), adapter));

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
			if (adapter.buffer.getSize()!= 0){
				adapter.buffer.setLastSeenMessage(adapter.buffer.getBacklogEntry(adapter.buffer.getSize()-1).messageId);
			}
			adapter.stopObserving();
			boundConnService.unregisterStatusReceiver(statusReceiver);
			unbindService(mConnection);
			isBound = false;
			adapter.clearBuffer();

		}
	}
}
