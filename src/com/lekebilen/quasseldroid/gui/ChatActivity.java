package com.lekebilen.quasseldroid.gui;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
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
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lekebilen.quasseldroid.Buffer;
import com.lekebilen.quasseldroid.CoreConnService;
import com.lekebilen.quasseldroid.IrcMessage;
import com.lekebilen.quasseldroid.R;

public class ChatActivity extends Activity{


	public static final int MESSAGE_RECEIVED = 0;

	private BacklogAdapter adapter;
	private static final String TAG = ChatActivity.class.getSimpleName();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.chat_layout);

		adapter = new BacklogAdapter(this, null);
		ListView backlogList = ((ListView)findViewById(R.id.chatBacklogList));
		backlogList.setCacheColorHint(0xffffff);
		backlogList.setAdapter(adapter);
		backlogList.setOnScrollListener(new BacklogScrollListener(5));
		backlogList.setDividerHeight(0);
		backlogList.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		
		findViewById(R.id.ChatInputView).setOnKeyListener(inputfieldKeyListener);
		
		((ListView) findViewById(R.id.chatBacklogList)).setCacheColorHint(0xffffff);
	}

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
			if ( adapter.buffer.nicks().size() > 0 ) {
				inputfield.setText(adapter.buffer.nicks().get(0)+ ": ");
				inputfield.setSelection(adapter.buffer.nicks().get(0).length() + 2);
			}
		} else {
			for (String nick : adapter.buffer.nicks()) {
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
		super.onStart();
	}

	@Override
	protected void onStop() {
		boundConnService.markBufferAsRead(adapter.getBufferId());
		doUnbindService();
		super.onStop();
	}
	


	public class BacklogAdapter extends BaseAdapter implements Observer {

		//private ArrayList<IrcMessage> backlog;
		private LayoutInflater inflater;
		private Buffer buffer;


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
			((TextView)findViewById(R.id.chatNameView)).setText(buffer.getInfo().name + ": " + buffer.topic());
			notifyDataSetChanged();
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
			return position;
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

			//Log.i(TAG, position + "   "+ getCount());

			if (buffer.getLastSeenMessage() == getItem(position).messageId && position != (getCount()-1)) { //Set separator line here
				holder.separatorView.getLayoutParams().height = 1;
			} else {
				holder.separatorView.getLayoutParams().height = 0;
			}

			IrcMessage entry = this.getItem(position);
			holder.timeView.setText(entry.getTime());
			int hashcode = entry.getNick().hashCode() & 0x00FFFFFF;

			holder.nickView.setTextColor(Color.rgb(hashcode & 0xFF0000, hashcode & 0xFF00, hashcode & 0xFF));
			holder.msgView.setTextColor(0xff000000);
			holder.msgView.setTypeface(Typeface.DEFAULT);
			
			switch (entry.type) {
			case Action:
				holder.nickView.setText("*");
				holder.msgView.setTypeface(Typeface.DEFAULT_BOLD);
				holder.msgView.setText(entry.getNick() + " " + entry.content);
				break;
			case Join:
				holder.nickView.setText("â†’");
				holder.msgView.setText(entry.getNick() + " has joined " + entry.content);
				break;
			case Part:
				holder.nickView.setText("â†�");
				holder.msgView.setText(entry.getNick() + " has left (" + entry.content + ")");
				break;
			case Quit:				
				holder.nickView.setText("â†�");
				holder.msgView.setText(entry.getNick() + " has quit (" + entry.content + ")");
				break;
			//TODO: implement the rest
			case Plain:
			default:
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
			Log.i(TAG, "BACKLOG CHANGED");
			notifyDataSetChanged();
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
			boundConnService.getMoreBacklog(this.getBufferId());
			
		}


	}	

	public static class ViewHolder {
		public TextView timeView;
		public TextView nickView;
		public TextView msgView;
		public TextView separatorView;
		public LinearLayout item_layout;
	}




	private class BacklogScrollListener implements OnScrollListener {
		
		private int visibleThreshold;
	    private int previousTotal = 0;
	    private boolean loading = true;
	    
	    public BacklogScrollListener(int visibleThreshold) {
	        this.visibleThreshold = visibleThreshold;
	    }

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		    if (loading) {
	            if (totalItemCount > previousTotal) { //This isn't 100% correct, since we can get msg on the bottom of the buffer while we wait for backlog, but it's good enough for now
	                loading = false;
	            }
	        }
		    Log.d(TAG, "# totalItemCount: "+totalItemCount+ "visibleItemCount: " +visibleItemCount+"firstVisibleItem: "+firstVisibleItem+ "visibleThreshold: "+visibleThreshold);
	        if (!loading && (firstVisibleItem <= visibleThreshold)) {
	        	previousTotal = totalItemCount;
	        	loading = true;
	            ChatActivity.this.adapter.getMoreBacklog();
	            
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
			Log.d(TAG, "Buffer gotten, nr or msg on it in the start is: "+adapter.getCount());

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
			unbindService(mConnection);
			isBound = false;
			adapter.clearBuffer();
			
		}
	}
}
