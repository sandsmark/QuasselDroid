package com.lekebilen.quasseldroid;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Observer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Adapter;

import com.lekebilen.quasseldroid.gui.BufferActivity;
import com.lekebilen.quasseldroid.gui.ChatActivity;

/**
 * This Service holdes the connection to the core from the phone, it handles all the communication with the core. It talks to CoreConnection
 * 
 */

public class CoreConnService extends Service{
	
	private static final String TAG = CoreConnService.class.getSimpleName();
	
	private CoreConnection coreConn;
	private final IBinder binder = new LocalBinder();
	
	BufferActivity.BufferListAdapter adapter;
	Handler notifyHandler;
	Handler incomingHandler;
	
	HashMap<Integer, Buffer> buffers = new HashMap<Integer, Buffer>();
	
	/**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
	public class LocalBinder extends Binder {
		public CoreConnService getService() {
            return CoreConnService.this;
        }
    }

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return binder;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		incomingHandler = new IncomingHandler();
	}

	public Handler getHandler() {
		return incomingHandler;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent!=null) {
			handleIntent(intent);
		}
		return START_STICKY;

	}
	
	/**
	 * Handle the data in the intent, and use it to connect with CoreConnect
	 * @param intent
	 */
	private void handleIntent(Intent intent) {
		Bundle connectData = intent.getExtras();
		String address = connectData.getString("address");
		int port = connectData.getInt("port");
		String username = connectData.getString("username");
		String password = connectData.getString("password");
		Boolean ssl = connectData.getBoolean("ssl");
		Log.i(TAG, "Connecting to core: "+address+":"+port+" with username " +username);
		coreConn = new CoreConnection(address, port, username, password, ssl, this);
		try {
			coreConn.connect();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void newMessage(IrcMessage message) {
		Log.i(TAG, "MESSAGE: " + message.content.toString() );
		
		Buffer buffer = buffers.get(message.bufferInfo.id);
		buffer.addBacklog(message);
		//Message msg = notifyHandler.obtainMessage(R.id.CHAT_MESSAGES_UPDATED);
		//msg.obj = message;
		//msg.sendToTarget();
		
		
	}
	
	public void newBuffer(Buffer buffer) {
		Log.i(TAG, "GETTING BUFFER: " + buffer.getInfo().name);
		Message msg = notifyHandler.obtainMessage(R.id.BUFFER_LIST_UPDATED);
		if (buffers.get(buffer.getInfo().id) == null ) {
			buffers.put(buffer.getInfo().id, buffer);
		} else {
			Log.e(TAG, "Getting already gotten buffer");
		}
		
		msg.obj = buffer;
		msg.sendToTarget();
		//adapter.notifyDataSetChanged();
		
	}
	
	public void newUser(IrcUser user) {
		
	}
	
	public void sendMessage(Buffer buffer, String message){
		//TODO
	}
	
	public Buffer getBuffer(int bufferId, Observer obs){
		//this.notifyHandler = notifyHandler;
		buffers.get(bufferId).addObserver(obs);
		coreConn.requestBacklog(bufferId);
		return buffers.get(bufferId);
	}
	
	public void getBufferList(Handler notifyHandler) {
		this.notifyHandler = notifyHandler;
		//Buffer buffer = new Buffer(new BufferInfo());
		//buffer.getInfo().name = "#MTDT12";
		//adapter.addBuffer(buffer);
		//adapter.notifyDataSetChanged();
		coreConn.requestBuffers();
	}
	
	/**
	 * Handler of incoming messages from CoreConnection, since it's in another read thread.
	 */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case R.id.CORECONNECTION_NEW_MESSAGE_TO_SERVICE:
					newMessage((IrcMessage)msg.obj);
					break;
			
			}
		}
	}

}