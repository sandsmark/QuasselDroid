package com.lekebilen.quasseldroid.service;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ViewDebug.IntToString;
import android.widget.Toast;

import com.lekebilen.quasseldroid.Buffer;
import com.lekebilen.quasseldroid.BufferCollection;
import com.lekebilen.quasseldroid.IrcMessage;
import com.lekebilen.quasseldroid.IrcUser;
import com.lekebilen.quasseldroid.R;
import com.lekebilen.quasseldroid.IrcMessage.Flag;
import com.lekebilen.quasseldroid.IrcMessage.Type;
import com.lekebilen.quasseldroid.R.drawable;
import com.lekebilen.quasseldroid.R.id;
import com.lekebilen.quasseldroid.R.string;
import com.lekebilen.quasseldroid.gui.BufferActivity;
import com.lekebilen.quasseldroid.gui.LoginActivity;
import com.lekebilen.quasseldroid.io.CoreConnection;

/**
 * This Service holds the connection to the core from the phone, 
 * it handles all the communication with the core. 
 * It talks to CoreConnection
 */

public class CoreConnService extends Service{

	private static final String TAG = CoreConnService.class.getSimpleName();
	
	/** Id for result code in the resultReciver that is going to notify the activity currently on screen about the change */
	public static final int CONNECTION_DISCONNECTED = 0;
	public static final int CONNECTION_CONNECTED = 1;
	public static final String STATUS_KEY = "status";

	private CoreConnection coreConn;
	private final IBinder binder = new LocalBinder();

	Handler incomingHandler;
	NotificationManager notifyManager;
	ArrayList<ResultReceiver> statusReceivers;

	SharedPreferences preferences;


	BufferCollection bufferCollection;
	HashMap<String, IrcUser> ircUsers = new HashMap<String, IrcUser>();

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
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		incomingHandler = new IncomingHandler();
		notifyManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		statusReceivers = new ArrayList<ResultReceiver>();
	}

	@Override
	public void onDestroy() {
		this.disconnectFromCore();

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
	 * Show a notification while this service is running.
	 * @param connected are we connected to a core or not 
	 */
	private void showNotification(boolean connected) {
		//TODO: Remove when "leaving" the application
		CharSequence text =  "";
		int icon;
		if (connected){
			text = getText(R.string.notification_connected);
			icon = R.drawable.icon;
		} else {
			text = getText(R.string.notification_disconnected);
			icon = R.drawable.inactive;
		}
		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(icon, text, System.currentTimeMillis());
		// The PendingIntent to launch our activity if the user selects this notification
		PendingIntent contentIntent;

		//TODO: Fix so that if a chat is currently on top, launch that one, instead of the BufferActivity
		if (connected){ //Launch the Buffer Activity.
			Intent launch = new Intent(this, BufferActivity.class);
			launch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			contentIntent = PendingIntent.getActivity(this, 0, launch, 0);
		} else {
			Intent launch = new Intent(this, LoginActivity.class);
			contentIntent = PendingIntent.getActivity(this, 0, launch, 0);
		}
		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.app_name),
				text, contentIntent);
		// Send the notification.
		notifyManager.notify(R.id.NOTIFICATION, notification);
	}

	/**
	 * Handle the data in the intent, and use it to connect with CoreConnect
	 * @param intent
	 */
	private void handleIntent(Intent intent) {
		if (coreConn!=null) {
			this.disconnectFromCore();
		}
		Bundle connectData = intent.getExtras();
		String address = connectData.getString("address");
		int port = connectData.getInt("port");
		String username = connectData.getString("username");
		String password = connectData.getString("password");
		Boolean ssl = connectData.getBoolean("ssl");
		Log.i(TAG, "Connecting to core: "+address+":"+port+" with username " +username);
		bufferCollection = new BufferCollection();
		coreConn = new CoreConnection(address, port, username, password, ssl, this);
//		try {
//			coreConn.connect();
//			// ↓↓↓↓ FIXME TODO HANDLE THESE YOU DICKWEEDS! ↓↓↓↓
//			showNotification(true);
//		} catch (UnknownHostException e) {
//			Toast.makeText(getApplicationContext(), "Unknown host!", Toast.LENGTH_LONG).show();
//		} catch (IOException e) {
//			Toast.makeText(getApplicationContext(), "IO error while connecting!", Toast.LENGTH_LONG).show();
//			e.printStackTrace();
//		} catch (GeneralSecurityException e) {
//			Toast.makeText(getApplicationContext(), "Invalid username/password combination.", Toast.LENGTH_LONG).show();
//		}
	}

	public void newUser(IrcUser user) {
		ircUsers.put(user.nick, user);
	}

	public IrcUser getUser(String nick){
		return ircUsers.get(nick);
	}
	public boolean hasUser(String nick){
		return ircUsers.containsKey(nick);
	}

	public void sendMessage(int bufferId, String message){
		coreConn.sendMessage(bufferId, message);
	}

	public void markBufferAsRead(int bufferId){
		coreConn.requestMarkBufferAsRead(bufferId);
	}

	public Buffer getBuffer(int bufferId, Observer obs){
		bufferCollection.getBuffer(bufferId).addObserver(obs);
		//coreConn.requestBacklog(bufferId);
		return bufferCollection.getBuffer(bufferId);
	}

	public void getMoreBacklog(int bufferId, int amount){
		Log.d(TAG, "GETING MORE BACKLOG");
		coreConn.requestMoreBacklog(bufferId, amount);
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
			IrcMessage message;
			switch (msg.what) {
			case R.id.CORECONNECTION_NEW_BACKLOGITEM_TO_SERVICE:
				/**
				 * New message on one buffer so update that buffer with the new message
				 */
				message = (IrcMessage)msg.obj;
				buffer = bufferCollection.getBuffer(message.bufferInfo.id);
	
				if(buffer != null && !buffer.hasMessage(message)) {
					/**
					 * Check if we are highlighted in the message, 
					 * TODO: Add support for custom highlight masks
					 */
					checkMessageForHighlight(buffer, message);
					buffer.addBacklogMessage(message);	
				}else {
					Log.e(TAG, "Getting message buffer already have"+ buffer.toString());
				}
				break;
			case R.id.CORECONNECTION_NEW_MESSAGE_TO_SERVICE:
				/**
				 * New message on one buffer so update that buffer with the new message
				 */
				message = (IrcMessage)msg.obj;
				buffer = bufferCollection.getBuffer(message.bufferInfo.id);

				if(buffer != null) {
					if (!buffer.hasMessage(message)) {
					/**
					 * Check if we are highlighted in the message, 
					 * TODO: Add support for custom highlight masks					 */
					checkMessageForHighlight(buffer, message);
					buffer.addMessage(message);					
					}else {
						Log.e(TAG, "Getting message buffer already have " + buffer.toString());
					}
				}else{
					Log.e(TAG, "Getting message but don't have buffer: " + message);
				}
				break;

			case R.id.CORECONNECTION_NEW_BUFFER_TO_SERVICE:
				/**
				 * New buffer received, so update out channel holder with the new buffer
				 */
				buffer = (Buffer)msg.obj;
				if (!bufferCollection.hasBuffer(buffer.getInfo().id)) {
					bufferCollection.addBuffer(buffer);
				} else {
					Log.e(TAG, "Getting already gotten buffer");
				}
				break;
			case R.id.CORECONNECTION_ADD_MULTIPLE_BUFFERS:
				/**
				 * Complete list of buffers received
				 */
				bufferCollection.addBuffers((Collection<Buffer>) msg.obj);
				break;

			case R.id.CORECONNECTION_SET_LAST_SEEN_TO_SERVICE:
				/**
				 * Setting last seen message id in a buffer
				 */
				//Log.d(TAG, "service lastseenset buffer");
				Buffer buf = (Buffer) msg.obj;
				buf.setLastSeenMessage(msg.arg1);
				break;
			case R.id.CORECONNECTION_SET_MARKERLINE_TO_SERVICE:
				/**
				 * Setting marker line message id in a buffer
				 */
				//Log.d(TAG, "service markerlineset buffer");
				Buffer buf2 = (Buffer) msg.obj;
				buf2.setMarkerLineMessage(msg.arg1);
				break;

			case R.id.CORECONNECTION_CONNECTED:
				/**
				 * CoreConn has connected to a core
				 */
				showNotification(true);
				for (ResultReceiver statusReceiver:statusReceivers) {
					statusReceiver.send(CoreConnService.CONNECTION_CONNECTED, null);
				}
				break;
				
			case R.id.CORECONNECTION_LOST_CONNECTION:
				/**
				 * Lost connection with core, update notification
				 */
				for (ResultReceiver statusReceiver:statusReceivers) {
					if(msg.obj!=null) { //Have description of what is wrong, used only for login atm
						Bundle bundle = new Bundle();
						bundle.putString(CoreConnService.STATUS_KEY, (String)msg.obj);
						statusReceiver.send(CoreConnService.CONNECTION_DISCONNECTED,bundle);
					}else{
						statusReceiver.send(CoreConnService.CONNECTION_DISCONNECTED, null);
					}
				}
				showNotification(false);
				break;

			case R.id.CORECONNECTION_NEW_USERLIST_ADDED:
				/**
				 * Initial list of users
				 */
				ArrayList<IrcUser> users = (ArrayList<IrcUser>) msg.obj;
				for (IrcUser user : users) {
					newUser(user);
				}
				break;

			case R.id.CORECONNECTION_NEW_USER_ADDED:
				/**
				 * New IrcUser added
				 */
				IrcUser user = (IrcUser) msg.obj;
				newUser(user);
				break;
			}
			
		}
	}
	
	
	/**
	 * Checks if there is a highlight in the message and then sets the flag of that message to highlight
	 * @param buffer the buffer the message belongs to
	 * @param message the message to check
	 */
	public void checkMessageForHighlight(Buffer buffer, IrcMessage message) {
		if (message.type==IrcMessage.Type.Plain) {
			Pattern regexHighlight = Pattern.compile(".*(?<!(\\w|\\d))"+coreConn.getNick(buffer.getInfo().networkId)+"(?!(\\w|\\d)).*", Pattern.CASE_INSENSITIVE);
			Matcher matcher = regexHighlight.matcher(message.content);
			if (matcher.find()) {
				message.setFlag(IrcMessage.Flag.Highlight);
			}
		}
	}

	public void disconnectFromCore() {
		notifyManager.cancel(R.id.NOTIFICATION);
		coreConn.disconnect();
	}

	public boolean isConnected() {
		return coreConn.isConnected();
	}

	/**
	 * Register a resultReceiver with this server, that will get notification when a change happens with the connection to the core
	 * Like when the connection is lost.
	 * @param resultReceiver - Receiver that will get the status updates
	 */
	public void registerStatusReceiver(ResultReceiver resultReceiver) {
		statusReceivers.add(resultReceiver);
		if (!coreConn.isConnected()) {
			resultReceiver.send(CoreConnService.CONNECTION_DISCONNECTED, null);
		}else{
			resultReceiver.send(CoreConnService.CONNECTION_CONNECTED, null);
		}
		
	}
	
	/**
	 * Unregister a receiver when you don't want any more updates.
	 * @param resultReceiver
	 */
	public void unregisterStatusReceiver(ResultReceiver resultReceiver) {
		statusReceivers.remove(resultReceiver);
	}


}
