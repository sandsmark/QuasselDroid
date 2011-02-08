package com.lekebilen.quasseldroid.gui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lekebilen.quasseldroid.Buffer;
import com.lekebilen.quasseldroid.IrcMessage;
import com.lekebilen.quasseldroid.IrcUser;
import com.lekebilen.quasseldroid.Network;
import com.lekebilen.quasseldroid.R;

public class ChatActivity extends Activity{


	public static final int MESSAGE_RECEIVED = 0;

	private int separatorLineNum = 3;
	private int curLineNum = 0;

	private BacklogAdapter adapter;
	private static final String TAG = ChatActivity.class.getSimpleName();
	private int bufferId;
	private String bufferName;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_layout);

		nicks = new ArrayList<String>();

		if (savedInstanceState!=null) {
			bufferId = savedInstanceState.getInt(BufferActivity.BUFFER_ID_EXTRA);
			bufferName = savedInstanceState.getString(BufferActivity.BUFFER_NAME_EXTRA);
		}else{
			//TODO: do something?
		}

		((TextView)findViewById(R.id.chatNameView)).setText(bufferName);
		mCallbackText = ((TextView)findViewById(R.id.chatNameView));

		adapter = new BacklogAdapter(this, null);
		ListView backlogList = ((ListView)findViewById(R.id.chatBacklogList)); 
		backlogList.setAdapter(adapter);
		backlogList.setDividerHeight(0);

		findViewById(R.id.ChatInputView).setOnKeyListener(inputfieldKeyListener);
	}

	private OnKeyListener inputfieldKeyListener =  new View.OnKeyListener() {
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction()==0 ) { //On key down as well
				EditText inputfield = (EditText)findViewById(R.id.ChatInputView);
				String inputText = inputfield.getText().toString();

				if ( ! "".equals(inputText) ) {
					//coreconnection.sendmessage(inputText);
					inputfield.setText("");
				}

				return true;
			}
			return false;
		}
	};

	private ArrayList<String> nicks;

	//Nick autocomplete when pressing the search-button
	@Override
	public boolean onSearchRequested() {
		EditText inputfield = (EditText)findViewById(R.id.ChatInputView); 
		String inputNick = inputfield.getText().toString();

		if ( "".equals(inputNick) ) {
			if ( nicks.size() > 0 ) {
				inputfield.setText(nicks.get(0)+ ": ");
				inputfield.setSelection(nicks.get(0).length() + 2);
			}
		} else {
			for (String nick : nicks) {
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
	protected void onStart() {
		doBindService();
		super.onStart();
	}

	@Override
	protected void onStop() {
		doUnbindService();
		super.onStop();
	}



	private class BacklogAdapter extends BaseAdapter {

		private ArrayList<BacklogEntry> backlog;
		private LayoutInflater inflater;


		public BacklogAdapter(Context context, ArrayList<BacklogEntry> backlog) {
			if (backlog==null) {
				this.backlog = new ArrayList<BacklogEntry>();
			}else {
				this.backlog = backlog;				
			}
			inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}

		public void addItem(BacklogEntry item) {
			Log.i(TAG, item.time);
			this.backlog.add(item);
			notifyDataSetChanged();
		}


		@Override
		public int getCount() {
			return backlog.size();
		}

		@Override
		public BacklogEntry getItem(int position) {
			return backlog.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (separatorLineNum == curLineNum ) { //Set separator line here
				convertView = inflater.inflate(R.layout.listseparator, null);
				curLineNum++;
				return convertView;
			}
			if (convertView==null) {
				convertView = inflater.inflate(R.layout.backlog_item, null);
				holder = new ViewHolder();
				holder.timeView = (TextView)convertView.findViewById(R.id.backlog_time_view);
				holder.nickView = (TextView)convertView.findViewById(R.id.backlog_nick_view);
				holder.msgView = (TextView)convertView.findViewById(R.id.backlog_msg_view);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			BacklogEntry entry = backlog.get(position);
			holder.timeView.setText(entry.time);
			holder.nickView.setText(entry.nick);
			int hashcode = entry.nick.hashCode() & 0x00FFFFFF;
			holder.nickView.setTextColor(Color.rgb(hashcode & 0xFF0000, hashcode & 0xFF00, hashcode & 0xFF));

			holder.msgView.setText(entry.msg);
			return convertView;
		}


	}	

	public static class ViewHolder {
		public TextView timeView;
		public TextView nickView;
		public TextView msgView;
	}

	public class BacklogEntry {
		public String time;
		public String nick;
		public String msg;

		public BacklogEntry(String time, String nick, String msg) {
			this.time = time;
			this.nick = nick;
			this.msg = msg;
		}
	}

	/**
	 * Code for service binding:
	 */
	/** Messenger for communicating with service. */
	Messenger mService = null;
	/** Flag indicating whether we have called bind on the service. */
	boolean mIsBound;
	/** Some text view we are using to show state information. */
	TextView mCallbackText;

	/**
	 * Handler of incoming messages from service.
	 */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			//	            case CoreConnection.MSG_CONNECT:
			//	                mCallbackText.setText("We have connection!");
			//	                break;
			//	            case CoreConnection.MSG_CONNECT_FAILED:
			//	            	mCallbackText.setText("Connection failed!");
			//	            	break;
			//	            case CoreConnection.MSG_NEW_BUFFER:
			//	            	mCallbackText.setText("Got new buffer!");
			//	            	Buffer buffer = (Buffer) msg.obj;
			////	            	break;
			//	            case CoreConnection.MSG_NEW_MESSAGE:
			//	            	IrcMessage message = (IrcMessage) msg.obj;
			//	            	if (message.bufferInfo.id == bufferId) // Check if the message belongs to the buffer we're displaying
			//	            		adapter.addItem(new BacklogEntry(message.timestamp.toString(), message.sender, message.content));
			//	            	break;
			////	            case CoreConnection.MSG_NEW_NETWORK:
			////	            	mCallbackText.setText("Got new network!");
			////	            	Network network = (Network) msg.obj;
			////	            	break;
			//	            case CoreConnection.MSG_NEW_USER:
			//	            	mCallbackText.setText("Got new user!");//TODO: handle me
			//	            	IrcUser user = (IrcUser) msg.obj; 
			//	            	if (user.channels.contains(bufferName)) // Make sure the user is in this channel
			//	            		nicks.add(user.nick);
			//	            	break;
			//	            default:
			//	                super.handleMessage(msg);
			}
		}
	}

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className,
				IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service.  We are communicating with our
			// service through an IDL interface, so get a client-side
			// representation of that from the raw service object.
			mService = new Messenger(service);
			mCallbackText.setText("Attached.");

			// We want to monitor the service for as long as we are
			//	        // connected to it.
			//	        try {
			//	            Message msg = Message.obtain(null,
			//	                    CoreConnection.MSG_REGISTER_CLIENT);
			//	            msg.replyTo = mMessenger;
			//	            mService.send(msg);
			//
			//	            // Get some sweet, sweet backlog
			//	            msg = Message.obtain(null, CoreConnection.MSG_REQUEST_BACKLOG, -1, -1, bufferId);
			//	            mService.send(msg);
			//	        } catch (RemoteException e) {
			//	            // In this case the service has crashed before we could even
			//	            // do anything with it; we can count on soon being
			//	            // disconnected (and then reconnected if it can be restarted)
			//	            // so there is no need to do anything here.
			//	        }

			// As part of the sample, tell the user what happened.
			Toast.makeText(ChatActivity.this, R.string.remote_service_connected,
					Toast.LENGTH_SHORT).show();
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mService = null;
			mCallbackText.setText("Disconnected.");

			// As part of the sample, tell the user what happened.
			Toast.makeText(ChatActivity.this, R.string.remote_service_disconnected,
					Toast.LENGTH_SHORT).show();
		}
	};

	void doBindService() {
		// Establish a connection with the service.  We use an explicit
		// class name because there is no reason to be able to let other
		// applications replace our component.
		//	    bindService(new Intent(ChatActivity.this, 
		//	            CoreConnection.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
		mCallbackText.setText("Binding.");
	}

	void doUnbindService() {
		//	    if (mIsBound) {
		//	        // If we have received the service, and hence registered with
		//	        // it, then now is the time to unregister.
		//	        if (mService != null) {
		//	            try {
		//	                Message msg = Message.obtain(null,
		//	                        CoreConnection.MSG_UNREGISTER_CLIENT);
		//	                msg.replyTo = mMessenger;
		//	                mService.send(msg);
		//	            } catch (RemoteException e) {
		//	                // There is nothing special we need to do if the service
		//	                // has crashed.
		//	            }
		//	        }
		//
		//	        // Detach our existing connection.
		//	        unbindService(mConnection);
		//	        mIsBound = false;
		//	        mCallbackText.setText("Unbinding.");
		//	    }
	}
}
