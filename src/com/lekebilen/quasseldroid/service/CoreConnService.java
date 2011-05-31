/**
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

package com.lekebilen.quasseldroid.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.lekebilen.quasseldroid.Buffer;
import com.lekebilen.quasseldroid.BufferCollection;
import com.lekebilen.quasseldroid.IrcMessage;
import com.lekebilen.quasseldroid.IrcUser;
import com.lekebilen.quasseldroid.R;
import com.lekebilen.quasseldroid.gui.BufferActivity;
import com.lekebilen.quasseldroid.gui.ChatActivity;
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
	
	private Pattern URLPattern= Pattern.compile("((mailto\\:|(news|(ht|f)tp(s?))\\://){1}\\S+)", Pattern.CASE_INSENSITIVE);


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
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
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
	
	public void setLastSeen(int bufferId, int msgId) {
		coreConn.requestSetLastMsgRead(bufferId, msgId);
		bufferCollection.getBuffer(bufferId).setLastSeenMessage(msgId);
	}
	
	public void setMarkerLine(int bufferId, int msgId) {
		coreConn.requestSetMarkerLine(bufferId, msgId);
		bufferCollection.getBuffer(bufferId).setMarkerLineMessage(msgId);
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
		if (bufferCollection == null)
			return null;
		
		bufferCollection.addObserver(obs);
		return bufferCollection;
	}

	/**
	 * Handler of incoming messages from CoreConnection, since it's in another read thread.
	 */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (msg == null) {
				Log.e(TAG, "Msg was null?");
				return;
			}
			
			Buffer buffer;
			IrcMessage message;
			switch (msg.what) {
			case R.id.CORECONNECTION_NEW_BACKLOGITEM_TO_SERVICE:
				/**
				 * New message on one buffer so update that buffer with the new message
				 */
				message = (IrcMessage)msg.obj;
				buffer = bufferCollection.getBuffer(message.bufferInfo.id);
				if (buffer == null) {
					Log.e(TAG, "A messages buffer is null:" + message);
					return;
				}
	
				if(!buffer.hasMessage(message)) {
					/**
					 * Check if we are highlighted in the message, 
					 * TODO: Add support for custom highlight masks
					 */
					checkMessageForHighlight(buffer, message);
					checkForURL(message);
					buffer.addBacklogMessage(message);	
				}else {
					Log.e(TAG, "Getting message buffer already have "+ buffer.getInfo().name);
				}
				break;
			case R.id.CORECONNECTION_NEW_MESSAGE_TO_SERVICE:
				/**
				 * New message on one buffer so update that buffer with the new message
				 */
				message = (IrcMessage)msg.obj;
				buffer = bufferCollection.getBuffer(message.bufferInfo.id);
				if (buffer == null) {
					Log.e(TAG, "A messages buffer is null: " + message);
					return;
				}

				if(!buffer.hasMessage(message)) {
					/**
					 * Check if we are highlighted in the message, 
					 * TODO: Add support for custom highlight masks					 */
					checkMessageForHighlight(buffer, message);
					if (message.isHighlighted()) {
						// Create a notification about the highlight
						String text = buffer.getInfo().name + ": <" + message.getNick() + "> " + message.content;
						Notification notification = new Notification(R.drawable.highlight, text, System.currentTimeMillis());
						Intent launch = new Intent(CoreConnService.this, ChatActivity.class);
						launch.putExtra(BufferActivity.BUFFER_ID_EXTRA, buffer.getInfo().id);
						launch.putExtra(BufferActivity.BUFFER_NAME_EXTRA, buffer.getInfo().name);
						PendingIntent contentIntent = PendingIntent.getActivity(CoreConnService.this, 0, launch, 0);
					
						// Set the info for the views that show in the notification panel.
						notification.setLatestEventInfo(CoreConnService.this, getText(R.string.app_name),
								text, contentIntent);
						// Send the notification.
						notifyManager.notify(R.id.NOTIFICATION, notification);
					}
					
					checkForURL(message);
					buffer.addMessage(message);					
					}else {
						Log.e(TAG, "Getting message buffer already have " + buffer.toString());
					}
				break;

			case R.id.CORECONNECTION_NEW_BUFFER_TO_SERVICE:
				/**
				 * New buffer received, so update out channel holder with the new buffer
				 */
				buffer = (Buffer)msg.obj;
				bufferCollection.addBuffer(buffer);
				
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
				if(bufferCollection.hasBuffer(msg.arg1)) {
					bufferCollection.getBuffer(msg.arg1).setLastSeenMessage(msg.arg2);
				} else{
					Log.e(TAG, "Getting set last seen message on unknown buffer: " + msg.arg1);
				}
				break;
			case R.id.CORECONNECTION_SET_MARKERLINE_TO_SERVICE:
				/**
				 * Setting marker line message id in a buffer
				 */
				if(bufferCollection.hasBuffer(msg.arg1)) {
					bufferCollection.getBuffer(msg.arg1).setMarkerLineMessage(msg.arg2);
				} else{
					Log.e(TAG, "Getting set marker line message on unknown buffer: " + msg.arg1);
				}
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
			
			case R.id.CORECONNECTION_SET_BUFFER_ORDER:
				/**
				 * Buffer order changed so set the new one
				 */
				bufferCollection.getBuffer(msg.arg1).setOrder(msg.arg2);
				break;
			
			case R.id.CORECONNECTION_SET_BUFFER_TEMP_HIDDEN:
				/**
				 * Buffer has been marked as temporary hidden, update buffer
				 */
				bufferCollection.getBuffer(msg.arg1).setTemporarilyHidden((Boolean)msg.obj);
				break;
				
			case R.id.CORECONNECTION_SET_BUFFER_PERM_HIDDEN:
				/**
				 * Buffer has been marked as permanently hidden, update buffer
				 */
				bufferCollection.getBuffer(msg.arg1).setPermanentlyHidden((Boolean)msg.obj);
				break;
				
			case R.id.CORECONNECTION_INVALID_CERTIFICATE:
				/**
				 * Received a mismatching certificate
				 */
			case R.id.CORECONNECTION_NEW_CERTIFICATE:
				/**
				 * Received a new, unseen certificate
				 */
				AlertDialog.Builder builder = new AlertDialog.Builder(CoreConnService.this);
				final String hashedCert = (String)msg.obj;
				builder.setMessage("Received a new certificate, do you trust it?\n" + hashedCert)
				       .setCancelable(false)
				       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
								preferences.edit().putString("certificate", hashedCert).commit();
				           }
				       })
				       .setNegativeButton("No", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				           }
				       });
				AlertDialog alert = builder.create();

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
				
				//FIXME: move to somewhere proper
			}
		}
	}
	
	/**
	 * Check if a message contains a URL that we need to support to open in a browser
	 * Set the url fields in the message so we can get it later
	 * @param message ircmessage to check
	 */
	public void checkForURL(IrcMessage message) {
		Matcher matcher = URLPattern.matcher(message.content);
		if (matcher.find()) {
			message.addURL(this, matcher.group(0));
		}
	}
	
	/**
	 * Parse mIRC color codes in IrcMessage
	 */
	public void parseColorCodes(IrcMessage message) {
		final char formattingIndicator = 3;
		
		/*if (message.content.toString().indexOf(formattingIndicator) == -1)
				return;*/
		String content = message.content.toString();
		
		SpannableStringBuilder newString = new SpannableStringBuilder(content);
		while (true) {
			content = newString.toString();
//			^C5,12colored text and background^C
			int start = content.indexOf(formattingIndicator);
			
			if (start == -1) {
				return;
			}
			
			int end = start + 1;
			int fg = -1;
			int bg = -1;
			if (Character.isDigit(content.charAt(end + 1))) {
				if (Character.isDigit(content.charAt(end + 2))) {
					fg = Integer.parseInt(content.substring(end, end + 2));
					end += 2;
				} else {
					fg = Integer.parseInt(content.substring(end, end + 1));
					end += 1;
				}
			}
			
			if (content.charAt(end + 1) == ',') {
				end++;
				
				if (Character.isDigit(content.charAt(end + 1))) {
					if (Character.isDigit(content.charAt(end + 2))) {
						bg = Integer.parseInt(content.substring(end, end + 2));
						end += 2;
					} else {
						bg = Integer.parseInt(content.substring(end, end + 1));
						end += 1;
					}
				}
			}
			int length = end - start;
			int endOfSpan = content.indexOf(formattingIndicator, end) - length;
			newString.delete(start, end);
			if (fg != -1) {
				newString.setSpan(new ForegroundColorSpan(getResources().getColor(mircCodeToColor(fg))), start, endOfSpan, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			}
			if (bg != -1) {
				newString.setSpan(new BackgroundColorSpan(getResources().getColor(mircCodeToColor(bg))), start, endOfSpan, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			}
		}
		//message.content = newString.; BURN IN HELL JAVA
	}
	
	private int mircCodeToColor(int code) {
		int color;
		switch (code) {
		case 0: //white
			color = R.color.ircmessage_white;
			break;
		case 1: //black
			color = R.color.ircmessage_black;
			break;
		case 2: //blue (navy)
			color = R.color.ircmessage_blue;
			break;
		case 3: //green
			color = R.color.ircmessage_green;
			break;
		case 4: //red
			color = R.color.ircmessage_red;
			break;
		case 5: //brown (maroon)
			color = R.color.ircmessage_brown;
			break;
		case 6: //purple
			color = R.color.ircmessage_purple;
			break;
		case 7: //orange (olive)
			color = R.color.ircmessage_orange;
			break;
		case 8: //yellow
			color = R.color.ircmessage_yellow;
			break;
		case 9: //light green (lime)
			color = R.color.ircmessage_light_green;
			break;
		case 10: //teal (a green/blue cyan)
			color = R.color.ircmessage_teal;
			break;
		case 11: //light cyan (cyan) (aqua)
			color = R.color.ircmessage_light_cyan;
			break;
		case 12: //light blue (royal)
			color = R.color.ircmessage_light_blue;
			break;
		case 13: //pink (light purple) (fuchsia)
			color = R.color.ircmessage_pink;
			break;
		case 14: //grey
			color = R.color.ircmessage_gray;
			break;
		default:
			color = R.color.ircmessage_normal_color;
		}
		return color;
	}

	public void disconnectFromCore() {
		notifyManager.cancel(R.id.NOTIFICATION);
		if (coreConn != null)
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
		if (coreConn!=null && coreConn.isConnected()) {
			resultReceiver.send(CoreConnService.CONNECTION_CONNECTED, null);
		}else{
			resultReceiver.send(CoreConnService.CONNECTION_DISCONNECTED, null);
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
