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

	BufferCollection bufferCollection;

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
		bufferCollection = new BufferCollection();
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

	public void newUser(IrcUser user) {

	}

	public void sendMessage(int bufferId, String message){
		coreConn.sendMessage(bufferId, message);
	}

	public Buffer getBuffer(int bufferId, Observer obs){
		bufferCollection.getBuffer(bufferId).addObserver(obs);
		coreConn.requestBacklog(bufferId);
		return bufferCollection.getBuffer(bufferId);
	}


	public BufferCollection getBufferList(Observer obs) {
		bufferCollection.addObserver(obs);
		coreConn.requestBuffers();
		return bufferCollection;
	}

	/**
	 * Handler of incoming messages from CoreConnection, since it's in another read thread.
	 */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			Buffer buffer;
			switch (msg.what) {
			case R.id.CORECONNECTION_NEW_MESSAGE_TO_SERVICE:
				/**
				 * New message on one buffer so update that buffer with the new message
				 */
				IrcMessage message = (IrcMessage)msg.obj;
				Log.i(TAG, "MESSAGE: " + message.content.toString() );

				buffer = bufferCollection.getBuffer(message.bufferInfo.id);
				buffer.addBacklog(message);
				break;

			case R.id.CORECONNECTION_NEW_BUFFER_TO_SERVICE:
				/**
				 * New buffer recived, so update out channel holder with the new buffer
				 */
				buffer = (Buffer)msg.obj;
				Log.i(TAG, "GETTING BUFFER: " + buffer.getInfo().name);
				if (!bufferCollection.hasBuffer(buffer.getInfo().id)) {
					bufferCollection.addBuffer(buffer);
				} else {
					Log.e(TAG, "Getting already gotten buffer");
				}
				break;
			}
		}
	}

}