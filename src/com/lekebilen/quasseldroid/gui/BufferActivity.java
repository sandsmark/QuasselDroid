package com.lekebilen.quasseldroid.gui;

import java.util.Observable;
import java.util.Observer;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.lekebilen.quasseldroid.Buffer;
import com.lekebilen.quasseldroid.BufferCollection;
import com.lekebilen.quasseldroid.R;
import com.lekebilen.quasseldroid.service.CoreConnService;

public class BufferActivity extends ListActivity {

	private static final String TAG = BufferActivity.class.getSimpleName();

	public static final String BUFFER_ID_EXTRA = "bufferid";
	public static final String BUFFER_NAME_EXTRA = "buffername";

	BufferListAdapter bufferListAdapter;
	
	ResultReceiver statusReciver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.buffer_list);
		//bufferList = new ArrayList<Buffer>();

		bufferListAdapter = new BufferListAdapter(this);
		getListView().setDividerHeight(0);
		getListView().setCacheColorHint(0xffffffff);
		setListAdapter(bufferListAdapter);
		
		statusReciver = new ResultReceiver(null) {

			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				if (resultCode==CoreConnService.CONNECTION_DISCONNECTED) finish();
				super.onReceiveResult(resultCode, resultData);
			}
			
		};
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
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Intent i = new Intent(BufferActivity.this, ChatActivity.class);
		i.putExtra(BUFFER_ID_EXTRA, bufferListAdapter.getItem(position).getInfo().id);
		i.putExtra(BUFFER_NAME_EXTRA, bufferListAdapter.getItem(position).getInfo().name);

		startActivity(i);
	}


	public class BufferListAdapter extends BaseAdapter implements Observer {
		private BufferCollection bufferCollection;
		private LayoutInflater inflater;

		public BufferListAdapter(Context context) {
			inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}

		public void setBuffers(BufferCollection buffers){
			this.bufferCollection = buffers;
			bufferCollection.addObserver(this);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			if (bufferCollection==null) {
				return 0;
			}else {
				return bufferCollection.getBufferCount();
			}
		}

		@Override
		public Buffer getItem(int position) {
			return bufferCollection.getPos(position);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView==null) {
				convertView = inflater.inflate(R.layout.buffer_list_item, null);
				holder = new ViewHolder();
				holder.bufferView = (TextView)convertView.findViewById(R.id.buffer_list_item_name);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			Buffer entry = this.getItem(position);
			switch (entry.getInfo().type) {
			case StatusBuffer:
				holder.bufferView.setText(entry.getInfo().name);
				break;
			case ChannelBuffer:
				holder.bufferView.setText("\t" + entry.getInfo().name);
				break;
			case QueryBuffer:
				String nick = entry.getInfo().name;
				if (boundConnService.hasUser(nick)){
					nick += boundConnService.getUser(nick).away ? " (Away)": "";
				}
				holder.bufferView.setText("\t" + nick);
				break;
			case GroupBuffer:
			case InvalidBuffer:
				holder.bufferView.setText("XXXX " + entry.getInfo().name);
			}

				

			//Check here if there are any unread messages in the buffer, and then set this color if there is
			if (entry.hasUnseenHighlight()){
				holder.bufferView.setTextColor(getResources().getColor(R.color.buffer_highlight_color));
			} else if (entry.hasUnreadMessage()){
				holder.bufferView.setTextColor(getResources().getColor(R.color.buffer_unread_color));
			} else if (entry.hasUnreadActivity()) {
				holder.bufferView.setTextColor(getResources().getColor(R.color.buffer_activity_color));
			}else {
				holder.bufferView.setTextColor(getResources().getColor(R.color.buffer_read_color));
			}
			return convertView;
		}

		@Override
		public void update(Observable observable, Object data) {
			notifyDataSetChanged();

		}

		public void clearBuffers() {
			bufferCollection = null;
		}

		public void stopObserving() {
			bufferCollection.deleteObserver(this);
			
		}
	}

	public static class ViewHolder {
		public TextView bufferView;
	}

	//	/**
	//	 * Code for service binding:
	//	 */
	//	/** Messenger for communicating with service. */
	//	Messenger mService = null;
	//	/** Flag indicating whether we have called bind on the service. */
	//	boolean mIsBound;
	//	/** Some text view we are using to show state information. */
	//	TextView mCallbackText;
	//
	//	/**
	//	 * Handler of incoming messages from service.
	//	 */
	//	class IncomingHandler extends Handler {
	//		@Override
	//		public void handleMessage(Message msg) {
	//			switch (msg.what) {
	//			case R.id.BUFFER_LIST_UPDATED:
	//				BufferActivity.this.listAdapter.addBuffer((Buffer) msg.obj);
	//				break;
	//				////	            case CoreConnection.MSG_CONNECT_FAILED:
	//				////	            	mCallbackText.setText("Connection failed!");
	//				////	            	break;
	//				////	            case CoreConnection.MSG_NEW_BUFFER:
	//				////	            	mCallbackText.setText("Got new buffer!");
	//				////	            	Buffer buffer = (Buffer) msg.obj;
	//				////	            	bufferList.add(buffer);
	//				////	            	break;
	//				////	            case CoreConnection.MSG_NEW_NETWORK: //TODO: handle me
	//				////	            	mCallbackText.setText("Got new network!");
	//				////	            	Network network = (Network) msg.obj;
	//				////	            	break;
	//				////	            default:
	//				////	                super.handleMessage(msg);
	//				//	        }
	//			}
	//		}
	//	}
	//	/**
	//	 * Target we publish for clients to send messages to IncomingHandler.
	//	 */
	//	final Messenger mMessenger = new Messenger(new IncomingHandler());
	//
	//	/**
	//	 * Class for interacting with the main interface of the service.
	//	 */
	//	private ServiceConnection mConnection = new ServiceConnection() {
	//		public void onServiceConnected(ComponentName className,
	//	            IBinder service) {
	//	        // This is called when the connection with the service has been
	//	        // established, giving us the service object we can use to
	//	        // interact with the service.  We are communicating with our
	//	        // service through an IDL interface, so get a client-side
	//	        // representation of that from the raw service object.
	//	        mService = new Messenger(service);
	//	        mCallbackText.setText("Attached.");
	//
	//	        // We want to monitor the service for as long as we are
	//	        // connected to it.
	////	        try {
	////	            Message msg = Message.obtain(null,
	////	                    CoreConnection.MSG_REGISTER_CLIENT);
	////	            msg.replyTo = mMessenger;
	////	            mService.send(msg);
	////
	////	            // Get some sweet, sweet backlog 
	////	            //TODO: request buffer list
	////	            msg = Message.obtain(null, CoreConnection.MSG_REQUEST_BUFFERS, 1, 0, null);
	////	            mService.send(msg);
	////	        } catch (RemoteException e) {
	////	            // In this case the service has crashed before we could even
	////	            // do anything with it; we can count on soon being
	////	            // disconnected (and then reconnected if it can be restarted)
	////	            // so there is no need to do anything here.
	////	        }
	//
	//	        // As part of the sample, tell the user what happened.
	//	        Toast.makeText(BufferActivity.this, R.string.remote_service_connected,
	//	                Toast.LENGTH_SHORT).show();
	//	    }
	//
	//	    public void onServiceDisconnected(ComponentName className) {
	//	        // This is called when the connection with the service has been
	//	        // unexpectedly disconnected -- that is, its process crashed.
	//	        mService = null;
	//	        mCallbackText.setText("Disconnected.");
	//
	//	        // As part of the sample, tell the user what happened.
	//	        Toast.makeText(BufferActivity.this, R.string.remote_service_disconnected,
	//	                Toast.LENGTH_SHORT).show();
	//	    }
	//	};
	//
	//	void doBindService() {
	//	    // Establish a connection with the service.  We use an explicit
	//	    // class name because there is no reason to be able to let other
	//	    // applications replace our component.
	//	    bindService(new Intent(BufferActivity.this, 
	//	            CoreConnection.class), mConnection, Context.BIND_AUTO_CREATE);
	//	    mIsBound = true;
	//	    mCallbackText.setText("Binding.");
	//	}
	//
	//	void doUnbindService() {
	//	    if (mIsBound) {
	//	        // If we have received the service, and hence registered with
	//	        // it, then now is the time to unregister.
	////	        if (mService != null) {
	////	            try {
	////	                Message msg = Message.obtain(null,
	////	                        CoreConnection.MSG_UNREGISTER_CLIENT);
	////	                msg.replyTo = mMessenger;
	////	                mService.send(msg);
	////	            } catch (RemoteException e) {
	////	                // There is nothing special we need to do if the service
	////	                // has crashed.
	////	            }
	////	        }
	//
	//	        // Detach our existing connection.
	//	        unbindService(mConnection);
	//	        mIsBound = false;
	//	        mCallbackText.setText("Unbinding.");
	//	    }
	//	}

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
			bufferListAdapter.setBuffers(boundConnService.getBufferList(bufferListAdapter));


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
			boundConnService.unregisterStatusReceiver(statusReciver);
			// Detach our existing connection.
			unbindService(mConnection);
			isBound = false;
			bufferListAdapter.clearBuffers();
		}
	}



}
