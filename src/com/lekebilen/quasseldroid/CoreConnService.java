package com.lekebilen.quasseldroid;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PatternMatcher;
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
	
	public void markBufferAsRead(int bufferId){
		coreConn.requestMarkBufferAsRead(bufferId);
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
				
				if(!buffer.hasMessage(message)) {
					/**
					 * Check if we are highlighted in the message, 
					 * TODO: Add support for custom highlight masks
					 */
					Pattern regexHighlight = Pattern.compile(".*(?<!(\\w|\\d))"+coreConn.getNick(buffer.getInfo().networkId)+"(?!(\\w|\\d)).*", Pattern.CASE_INSENSITIVE);
					Matcher matcher = regexHighlight.matcher(message.content);
					if (matcher.find()) {
						message.setFlag(IrcMessage.Flag.Highlight);
					}
					buffer.addBacklog(message);					
				}else {
					Log.e(TAG, "Getting message buffer already have");
				}

				break;

			case R.id.CORECONNECTION_NEW_BUFFER_TO_SERVICE:
				/**
				 * New buffer received, so update out channel holder with the new buffer
				 */
				buffer = (Buffer)msg.obj;
				Log.i(TAG, "GETTING BUFFER: " + buffer.getInfo().name);
				if (!bufferCollection.hasBuffer(buffer.getInfo().id)) {
					bufferCollection.addBuffer(buffer);
				} else {
					Log.e(TAG, "Getting already gotten buffer");
				}
				break;
			case R.id.CORECONNECTION_SET_LAST_SEEN_TO_SERVICE:
				/**
				 * Setting last seen message id in a buffer
				 */
				Log.d(TAG, "service lastseenset buffer");
				Buffer buf = (Buffer) msg.obj;
				buf.setLastSeenMessage(msg.arg1);
				break;
			case R.id.CORECONNECTION_SET_MARKERLINE_TO_SERVICE:
				/**
				 * Setting marker line message id in a buffer
				 */
				Log.d(TAG, "service markerlineset buffer");
				Buffer buf2 = (Buffer) msg.obj;
				buf2.setMarkerLineMessage(msg.arg1);
				break;
			}
		}
	}

	public void disconnectFromCore() {
		coreConn.disconnect();
		
	}

}