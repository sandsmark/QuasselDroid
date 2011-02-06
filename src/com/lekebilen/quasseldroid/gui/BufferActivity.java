package com.lekebilen.quasseldroid.gui;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lekebilen.quasseldroid.Buffer;
import com.lekebilen.quasseldroid.BufferInfo;
import com.lekebilen.quasseldroid.CoreConnection;
import com.lekebilen.quasseldroid.Network;
import com.lekebilen.quasseldroid.R;

public class BufferActivity extends ListActivity {
	
	public static final String BUFFER_ID_EXTRA = "bufferid";
	public static final String BUFFER_NAME_EXTRA = "buffername";

	
	ArrayList<Buffer> bufferList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.buffer_list);
		bufferList = new ArrayList<Buffer>();
		
		Buffer test = new Buffer(new BufferInfo());
		test.getInfo().name = "#testlolol1";
		bufferList.add(test);
		test = new Buffer(new BufferInfo());
		test.getInfo().name = "#testlolol2";
		bufferList.add(test);
		test = new Buffer(new BufferInfo());
		test.getInfo().name = "#testlolol3";
		bufferList.add(test);
		test = new Buffer(new BufferInfo());
		test.getInfo().name = "#testlolol4";
		bufferList.add(test);

		ListAdapter adapter = new BufferListAdapter(this, bufferList);
		getListView().setDividerHeight(0);
		setListAdapter(adapter);
	}
	
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Intent i = new Intent(BufferActivity.this, ChatActivity.class);
		i.putExtra(BUFFER_ID_EXTRA, bufferList.get(position).getInfo().id);
		i.putExtra(BUFFER_NAME_EXTRA, bufferList.get(position).getInfo().name);
		
		startActivity(i);
	}
	

	private class BufferListAdapter extends BaseAdapter {
		private ArrayList<Buffer> list;
		private LayoutInflater inflater;

		public BufferListAdapter(Context context, ArrayList<Buffer> list) {
			if (list==null) {
				this.list = new ArrayList<Buffer>();
			}else {
				this.list = list;				
			}
			inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Buffer getItem(int position) {
			return list.get(position);
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
			Buffer entry = list.get(position);
			holder.bufferView.setText(entry.getInfo().name);
			
			//Check here if there are any unread messages in the buffer, and then set this color if there is
			holder.bufferView.setTextColor(getResources().getColor(R.color.unread_buffer_color));
			return convertView;
		}
	}
	
	public static class ViewHolder {
		public TextView bufferView;
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
	            case CoreConnection.MSG_NEW_BUFFER:
	            	mCallbackText.setText("Got new buffer!");
	            	Buffer buffer = (Buffer) msg.obj;
	            	bufferList.add(buffer);
	            	break;
	            case CoreConnection.MSG_NEW_NETWORK: //TODO: handle me
	            	mCallbackText.setText("Got new network!");
	            	Network network = (Network) msg.obj;
	            	break;
	            default:
	                super.handleMessage(msg);
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
	        // connected to it.
	        try {
	            Message msg = Message.obtain(null,
	                    CoreConnection.MSG_REGISTER_CLIENT);
	            msg.replyTo = mMessenger;
	            mService.send(msg);

	            // Get some sweet, sweet backlog 
	            //TODO: request buffer list
//	            msg = Message.obtain(null, CoreConnection.MSG_REQUEST_BACKLOG, -1, -1, bufferId);
	            mService.send(msg);
	        } catch (RemoteException e) {
	            // In this case the service has crashed before we could even
	            // do anything with it; we can count on soon being
	            // disconnected (and then reconnected if it can be restarted)
	            // so there is no need to do anything here.
	        }

	        // As part of the sample, tell the user what happened.
	        Toast.makeText(BufferActivity.this, R.string.remote_service_connected,
	                Toast.LENGTH_SHORT).show();
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        mService = null;
	        mCallbackText.setText("Disconnected.");

	        // As part of the sample, tell the user what happened.
	        Toast.makeText(BufferActivity.this, R.string.remote_service_disconnected,
	                Toast.LENGTH_SHORT).show();
	    }
	};

	void doBindService() {
	    // Establish a connection with the service.  We use an explicit
	    // class name because there is no reason to be able to let other
	    // applications replace our component.
	    bindService(new Intent(BufferActivity.this, 
	            CoreConnection.class), mConnection, Context.BIND_AUTO_CREATE);
	    mIsBound = true;
	    mCallbackText.setText("Binding.");
	}

	void doUnbindService() {
	    if (mIsBound) {
	        // If we have received the service, and hence registered with
	        // it, then now is the time to unregister.
	        if (mService != null) {
	            try {
	                Message msg = Message.obtain(null,
	                        CoreConnection.MSG_UNREGISTER_CLIENT);
	                msg.replyTo = mMessenger;
	                mService.send(msg);
	            } catch (RemoteException e) {
	                // There is nothing special we need to do if the service
	                // has crashed.
	            }
	        }

	        // Detach our existing connection.
	        unbindService(mConnection);
	        mIsBound = false;
	        mCallbackText.setText("Unbinding.");
	    }
	}


}
