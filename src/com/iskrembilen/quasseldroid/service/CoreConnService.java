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

package com.iskrembilen.quasseldroid.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.*;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.widget.Toast;

import com.iskrembilen.quasseldroid.*;
import com.iskrembilen.quasseldroid.IrcMessage.Flag;
import com.iskrembilen.quasseldroid.Network.ConnectionState;
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent;
import com.iskrembilen.quasseldroid.events.DisconnectCoreEvent;
import com.iskrembilen.quasseldroid.events.FilterMessagesEvent;
import com.iskrembilen.quasseldroid.events.GetBacklogEvent;
import com.iskrembilen.quasseldroid.events.InitProgressEvent;
import com.iskrembilen.quasseldroid.events.JoinChannelEvent;
import com.iskrembilen.quasseldroid.events.LatencyChangedEvent;
import com.iskrembilen.quasseldroid.events.ManageChannelEvent;
import com.iskrembilen.quasseldroid.events.ManageMessageEvent;
import com.iskrembilen.quasseldroid.events.ManageNetworkEvent;
import com.iskrembilen.quasseldroid.events.ManageMessageEvent.MessageAction;
import com.iskrembilen.quasseldroid.events.ManageNetworkEvent.NetworkAction;
import com.iskrembilen.quasseldroid.events.NetworksAvailableEvent;
import com.iskrembilen.quasseldroid.events.SendMessageEvent;
import com.iskrembilen.quasseldroid.events.UnsupportedProtocolEvent;
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent.Status;
import com.iskrembilen.quasseldroid.events.ManageChannelEvent.ChannelAction;
import com.iskrembilen.quasseldroid.events.NewCertificateEvent;
import com.iskrembilen.quasseldroid.io.CoreConnection;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.iskrembilen.quasseldroid.util.QuasseldroidNotificationManager;
import com.iskrembilen.quasseldroid.util.ThemeUtil;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This Service holds the connection to the core from the phone, it handles all
 * the communication with the core. It talks to CoreConnection
 */

public class CoreConnService extends Service {

	private static final String TAG = CoreConnService.class.getSimpleName();

	/**
	 * Id for result code in the resultReciver that is going to notify the
	 * activity currently on screen about the change
	 */
	public static final int CONNECTION_DISCONNECTED = 0;
	public static final int CONNECTION_CONNECTED = 1;
	public static final int NEW_CERTIFICATE = 2;
	public static final int UNSUPPORTED_PROTOCOL = 3;
	public static final int INIT_PROGRESS = 4;
	public static final int INIT_DONE = 5;
	public static final int CONNECTION_CONNECTING = 6;
	public static final int LATENCY_CORE = 7;

	public static final String STATUS_KEY = "status";
	public static final String CERT_KEY = "certificate";
	public static final String PROGRESS_KEY = "networkname";

	public static final String LATENCY_CORE_KEY = "latency";

	private CoreConnection coreConn;
	private final IBinder binder = new LocalBinder();
	private boolean requestedDisconnect;

	Handler incomingHandler;

	SharedPreferences preferences;

	private NetworkCollection networks;

	private QuasseldroidNotificationManager notificationManager;

	private boolean preferenceParseColors;

	private OnSharedPreferenceChangeListener preferenceListener;

	private int latency;
	private boolean initDone = false;
	private String initReason = "";
	
	private boolean preferenceUseWakeLock;
	private WakeLock wakeLock;

	private WifiLock wifiLock;


	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
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
		Log.i(TAG, "Service created");
		incomingHandler = new IncomingHandler();
		notificationManager = new QuasseldroidNotificationManager(this);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferenceParseColors = preferences.getBoolean(getString(R.string.preference_colored_text), false);
		preferenceUseWakeLock = preferences.getBoolean(getString(R.string.preference_wake_lock), false);
		preferenceListener = new OnSharedPreferenceChangeListener() {
			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if(key.equals(getString(R.string.preference_colored_text))) {
					preferenceParseColors = preferences.getBoolean(getString(R.string.preference_colored_text), false);
				} else if(key.equals(getString(R.string.preference_wake_lock))) {
					preferenceUseWakeLock = preferences.getBoolean(getString(R.string.preference_wake_lock), true);
					if(!preferenceUseWakeLock) releaseWakeLockIfExists();
					else if(preferenceUseWakeLock && isConnected()) acquireWakeLockIfEnabled();
				} 
			}
		};
		preferences.registerOnSharedPreferenceChangeListener(preferenceListener);
		BusProvider.getInstance().register(this);
		startForeground(R.id.NOTIFICATION, notificationManager.getConnectingNotification());
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "Destroying service");
		this.disconnectFromCore();
		BusProvider.getInstance().unregister(this);
		stopForeground(true);

	}

	public Handler getHandler() {
		return incomingHandler;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			handleIntent(intent);
		}
		return START_STICKY;

	}

	/**
	 * Handle the data in the intent, and use it to connect with CoreConnect
	 * 
	 * @param intent
	 */
	private void handleIntent(Intent intent) {
		if (coreConn != null) {
			this.disconnectFromCore();
		}
		requestedDisconnect = false;
		Bundle connectData = intent.getExtras();
		String address = connectData.getString("address");
		int port = connectData.getInt("port");
		String username = connectData.getString("username");
		String password = connectData.getString("password");
		Boolean ssl = connectData.getBoolean("ssl");
		Log.i(TAG, "Connecting to core: " + address + ":" + port
				+ " with username " + username);
		networks = NetworkCollection.getInstance();
		networks.clear();
		
		acquireWakeLockIfEnabled();
		
		coreConn = new CoreConnection(address, port, username, password, ssl,
				this);
	}
	
	private void acquireWakeLockIfEnabled() {
		if (preferenceUseWakeLock) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "quasseldroid wakelock");
			wakeLock.acquire();
			Log.i(TAG, "WakeLock acquired");
			
			WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "quasseldroid wifilock");
			wifiLock.acquire();
			Log.i(TAG, "WifiLock acquired");
		}
	}

	private void releaseWakeLockIfExists() {
		if(wakeLock != null) {
			wakeLock.release();
			Log.i(TAG, "WakeLock released");
		}
		if(wifiLock != null) {
			wifiLock.release();
			Log.i(TAG, "WifiLock released");
		}
		wakeLock = null;
		wifiLock = null;
	}


	public void sendMessage(int bufferId, String message) {
		coreConn.sendMessage(bufferId, message);
	}
	
	public void unhideTempHiddenBuffer(int bufferId) {
		coreConn.requestUnhideTempHiddenBuffer(bufferId);
		networks.getBufferById(bufferId).setTemporarilyHidden(false);
	}

	public Buffer getBuffer(int bufferId, Observer obs) {
		Buffer buffer = networks.getBufferById(bufferId);
		if(obs != null && buffer != null)
			buffer.addObserver(obs);
		return buffer;
	}

	public NetworkCollection getNetworkList(Observer obs) {
		return networks;
	}

	/**
	 * Checks if there is a highlight in the message and then sets the flag of
	 * that message to highlight
	 * 
	 * @param buffer
	 *            the buffer the message belongs to
	 * @param message
	 *            the message to check
	 */
	public void checkMessageForHighlight(Buffer buffer, IrcMessage message) {
		if (message.type == IrcMessage.Type.Plain) {
			String nick = networks.getNetworkById(buffer.getInfo().networkId).getNick();
			if(nick == null) {
				Log.e(TAG, "Nick is null in check message for highlight");
				return;
			} else if(nick.equals("")) return;
			Pattern regexHighlight = Pattern.compile(".*(?<!(\\w|\\d))" + Pattern.quote(networks.getNetworkById(buffer.getInfo().networkId).getNick()) + "(?!(\\w|\\d)).*", Pattern.CASE_INSENSITIVE);
			Matcher matcher = regexHighlight.matcher(message.content);
			if (matcher.find()) {
				message.setFlag(IrcMessage.Flag.Highlight);
				// FIXME: move to somewhere proper
			}
		}
	}

	/**
	 * Parse mIRC style codes in IrcMessage
	 */
	public void parseStyleCodes(IrcMessage message) {
		if(!preferenceParseColors) return;
		
		final char boldIndicator = 2;
		final char normalIndicator = 15;
		final char italicIndicator = 29;
		final char underlineIndicator = 31;
		final char colorIndicator = 3;

		String content = message.content.toString();

		if (content.indexOf(boldIndicator) == -1 
				&& content.indexOf(italicIndicator) == -1
				&& content.indexOf(underlineIndicator) == -1
				&& content.indexOf(colorIndicator) == -1)
			return;

		SpannableStringBuilder newString = new SpannableStringBuilder(message.content);
		
		int start, end, endSearchOffset, startIndicatorLength, style, fg, bg;
		while (true) {
			content = newString.toString();
			end = -1;
			startIndicatorLength = 1;
			style = 0;
			fg = -1;
			bg = -1;
			
			start = content.indexOf(boldIndicator);
			if (start != -1) {
				end = content.indexOf(boldIndicator, start+1);
				style = Typeface.BOLD;
			}

			if (start == -1) {
				start = content.indexOf(italicIndicator);
				if (start != -1) {
					end = content.indexOf(italicIndicator, start+1);
					style = Typeface.ITALIC;
				}
			}

			if (start == -1) {
				start = content.indexOf(underlineIndicator);
				if (start != -1) {
					end = content.indexOf(underlineIndicator, start+1);
					style = -1;
				}
			}
			
			endSearchOffset = start + 1;
			
			// Colors?
			if (start == -1) {
				start = content.indexOf(colorIndicator);
				
				if (start != -1) {
					// Note that specifying colour codes here is optional, as the same indicator will cancel existing colours
					endSearchOffset = start + 1;
					if (endSearchOffset < content.length()) {
						if (Character.isDigit(content.charAt(endSearchOffset))) {
							if (endSearchOffset+1 < content.length() && Character.isDigit(content.charAt(endSearchOffset + 1))) {
								fg = Integer.parseInt(content.substring(endSearchOffset, endSearchOffset + 2));
								endSearchOffset += 2;
							} else {
								fg = Integer.parseInt(content.substring(endSearchOffset, endSearchOffset+1));
								endSearchOffset += 1;
							}
							
							if (endSearchOffset < content.length() && content.charAt(endSearchOffset) == ',') {
								if (endSearchOffset+1 < content.length() && Character.isDigit(content.charAt(endSearchOffset+1))) {
									endSearchOffset++;
									if (endSearchOffset+1 < content.length() && Character.isDigit(content.charAt(endSearchOffset + 1))) {
										bg = Integer.parseInt(content.substring(endSearchOffset, endSearchOffset + 2));
										endSearchOffset += 2;
									} else {
										bg = Integer.parseInt(content.substring(endSearchOffset, endSearchOffset + 1));
										endSearchOffset += 1;
									}
								}
							}
						}
					}
					startIndicatorLength = endSearchOffset - start;
					
					end = content.indexOf(colorIndicator, endSearchOffset);
				}
			}

			if (start == -1)
				break;

			int norm = content.indexOf(normalIndicator, start+1);
			if (norm != -1 && (end == -1 || norm < end))
				end = norm;

			if (end == -1)
				end = content.length();

			if (end - (start + startIndicatorLength) > 0) {
				// Only set spans if there's any text between start & end
				if (style == -1) {
					newString.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
				} else {
					newString.setSpan(new StyleSpan(style), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
				}
				
				if (fg != -1) {
					newString.setSpan(new ForegroundColorSpan(getResources()
							.getColor(mircCodeToColor(fg))), start, end,
							Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
				}
				if (bg != -1) {
					newString.setSpan(new BackgroundColorSpan(getResources()
							.getColor(mircCodeToColor(bg))), start, end,
							Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
				}
			}

			// Intentionally don't remove "normal" indicators or color here, as they are multi-purpose
			if (end < content.length() && (content.charAt(end) == boldIndicator
					|| content.charAt(end) == italicIndicator
					|| content.charAt(end) == underlineIndicator))
				newString.delete(end, end+1);

			newString.delete(start, start + startIndicatorLength);
		}
		
		// NOW we remove the "normal" and color indicator
		while (true) {
			content = newString.toString();
			int normPos = content.indexOf(normalIndicator);
			if (normPos != -1)
				newString.delete(normPos, normPos+1);
			
			int colorPos = content.indexOf(colorIndicator);
			if (colorPos != -1)
				newString.delete(colorPos, colorPos+1);
			
			if (normPos == -1 && colorPos == -1)
				break;
		}
		
		message.content = newString;
	}

	private int mircCodeToColor(int code) {
		int color;
		switch (code) {
		case 0: // white
			color = R.color.ircmessage_white;
			break;
		case 1: // black
			color = R.color.ircmessage_black;
			break;
		case 2: // blue (navy)
			color = R.color.ircmessage_blue;
			break;
		case 3: // green
			color = R.color.ircmessage_green;
			break;
		case 4: // red
			color = R.color.ircmessage_red;
			break;
		case 5: // brown (maroon)
			color = R.color.ircmessage_brown;
			break;
		case 6: // purple
			color = R.color.ircmessage_purple;
			break;
		case 7: // orange (olive)
			color = R.color.ircmessage_orange;
			break;
		case 8: // yellow
			color = R.color.ircmessage_yellow;
			break;
		case 9: // light green (lime)
			color = R.color.ircmessage_light_green;
			break;
		case 10: // teal (a green/blue cyan)
			color = R.color.ircmessage_teal;
			break;
		case 11: // light cyan (cyan) (aqua)
			color = R.color.ircmessage_light_cyan;
			break;
		case 12: // light blue (royal)
			color = R.color.ircmessage_light_blue;
			break;
		case 13: // pink (light purple) (fuchsia)
			color = R.color.ircmessage_pink;
			break;
		case 14: // grey
			color = R.color.ircmessage_gray;
			break;
		default:
			color = ThemeUtil.chatPlainResource;
		}
		return color;
	}

	public void disconnectFromCore() {
		releaseWakeLockIfExists();
		if (coreConn != null)
			coreConn.closeConnection();
		coreConn = null;
		notificationManager = null;
		stopSelf();
	}

	public boolean isConnected() {
		return  coreConn != null && coreConn.isConnected();
	}

	/**
	 * Handler of incoming messages from CoreConnection, since it's in another
	 * read thread.
	 */
	class IncomingHandler extends Handler {


		@Override
		public void handleMessage(Message msg) {
			if (msg == null || coreConn == null) {
				return;
			}

			Buffer buffer;
			IrcMessage message;
			Bundle bundle;
			IrcUser user;
			String bufferName;
			switch (msg.what) {
			case R.id.NEW_BACKLOGITEM_TO_SERVICE:
				/**
				 * New message on one buffer so update that buffer with the new
				 * message
				 */
				message = (IrcMessage) msg.obj;
				buffer = networks.getBufferById(message.bufferInfo.id);
				
				if (buffer == null) {
					Log.e(TAG, "A message buffer is null:" + message);
					return;
				}

				if (!buffer.hasMessage(message)) {
					/**
					 * Check if we are highlighted in the message, TODO: Add
					 * support for custom highlight masks
					 */
					checkMessageForHighlight(buffer, message);
					parseStyleCodes(message);
					buffer.addBacklogMessage(message);
				} else {
					Log.e(TAG, "Getting message buffer already have " + buffer.getInfo().name);
				}
				break;
			case R.id.NEW_MESSAGE_TO_SERVICE:
				/**
				 * New message on one buffer so update that buffer with the new
				 * message
				 */
				message = (IrcMessage) msg.obj;
				buffer = networks.getBufferById(message.bufferInfo.id);
				if (buffer == null) {
					Log.e(TAG, "A messages buffer is null: " + message);
					return;
				}

				if (!buffer.hasMessage(message)) {
					/**
					 * Check if we are highlighted in the message, TODO: Add
					 * support for custom highlight masks
					 */
					checkMessageForHighlight(buffer, message);
					parseStyleCodes(message);
					if ((message.isHighlighted() && !buffer.isDisplayed()) || (buffer.getInfo().type == BufferInfo.Type.QueryBuffer && ((message.flags & Flag.Self.getValue()) == 0))) {
						notificationManager.notifyHighlight(buffer.getInfo().id);
						
					}
					buffer.addMessage(message);
					
					if (buffer.isTemporarilyHidden()) {
						unhideTempHiddenBuffer(buffer.getInfo().id);
					}
				} else {
					Log.e(TAG, "Getting message buffer already have " + buffer.toString());
				}
				break;

			case R.id.NEW_BUFFER_TO_SERVICE:
				/**
				 * New buffer received, so update out channel holder with the
				 * new buffer
				 */
				networks.addBuffer((Buffer) msg.obj);
				break;
			case R.id.ADD_MULTIPLE_BUFFERS:
				/**
				 * Complete list of buffers received
				 */
				for (Buffer tmp : (Collection<Buffer>) msg.obj) {
					networks.addBuffer(tmp);
				}
				break;
			case R.id.ADD_NETWORK:
				networks.addNetwork((Network)msg.obj);
				break;
			case R.id.NETWORK_REMOVED:
				networks.removeNetwork(msg.arg1);
				break;
			case R.id.SET_CONNECTION_STATE:
				networks.getNetworkById(msg.arg1).setConnectionState((ConnectionState)msg.obj);
				break;
			case R.id.SET_STATUS_BUFFER:
				networks.getNetworkById(msg.arg1).setStatusBuffer((Buffer) msg.obj);
			case R.id.SET_LAST_SEEN_TO_SERVICE:
				/**
				 * Setting last seen message id in a buffer
				 */
				buffer = networks.getBufferById(msg.arg1);
				if (buffer != null) {
					buffer.setLastSeenMessage(msg.arg2);
					//if(buffer.hasUnseenHighlight()) {FIXME
						notificationManager.notifyHighlightsRead(buffer.getInfo().id);
					//}
				} else {
					Log.e(TAG, "Getting set last seen message on unknown buffer: " + msg.arg1);
				}
				break;
			case R.id.SET_MARKERLINE_TO_SERVICE:
				/**
				 * Setting marker line message id in a buffer
				 */
				buffer = networks.getBufferById(msg.arg1);
				if (buffer != null) {
					buffer.setMarkerLineMessage(msg.arg2);
				} else {
					Log.e(TAG, "Getting set marker line message on unknown buffer: " + msg.arg1);
				}
				break;


			case R.id.CONNECTING:
				/**
				 * CoreConn has connected to a core
				 */
				notificationManager.notifyConnecting();
				BusProvider.getInstance().post(new ConnectionChangedEvent(Status.Connecting));
				break;

				
			case R.id.LOST_CONNECTION:
				/**
				 * Lost connection with core, update notification
				 */
				if(coreConn != null) {
					if (msg.obj != null) { // Have description of what is wrong,
						// used only for login atm
						BusProvider.getInstance().post(new ConnectionChangedEvent(Status.Disconnected, (String)msg.obj));
					} else {
						BusProvider.getInstance().post(new ConnectionChangedEvent(Status.Disconnected));
					}
					notificationManager.notifyDisconnected();
				}
				disconnectFromCore();
				break;
			case R.id.NEW_USER_ADDED:
				/**
				 * New IrcUser added
				 */
				user = (IrcUser) msg.obj;
				networks.getNetworkById(msg.arg1).onUserJoined(user);
				break;
			case R.id.NEW_USER_INFO:
				bundle = (Bundle) msg.obj;
				user = networks.getNetworkById(msg.arg1).getUserByNick(bundle.getString("nick"));
				if(user != null) {
					user.away = bundle.getBoolean("away");
					user.awayMessage = bundle.getString("awayMessage");
					user.ircOperator = bundle.getString("ircOperator");
					user.channels = (ArrayList<String>) bundle.getSerializable("channels");
					user.notifyObservers();
				} else {
					Log.e(TAG, "User not found for new user info");
					//TODO: why is it not found...
				}
				break;
			case R.id.SET_BUFFER_ORDER:
				/**
				 * Buffer order changed so set the new one
				 */
				//---- start debug stuff
//				ArrayList<Integer> a = (((Bundle)msg.obj).getIntegerArrayList("keys"));
//				ArrayList<Integer> b = ((Bundle)msg.obj).getIntegerArrayList("buffers");
//				ArrayList<Integer> c = new ArrayList<Integer>();
//				for(Network net : networks.getNetworkList()) {
//					c.add(net.getStatusBuffer().getInfo().id);
//					for(Buffer buf : net.getBuffers().getRawBufferList()) {
//						c.add(buf.getInfo().id);
//					}
//				}
//				Collections.sort(a);
//				Collections.sort(b);
//				Collections.sort(c);
				//---- end debug stuff
				
				if(networks == null) throw new RuntimeException("Networks are null when setting buffer order");
				if(networks.getBufferById(msg.arg1) == null)
					return;
					//throw new RuntimeException("Buffer is null when setting buffer order, bufferid " + msg.arg1 + " order " + msg.arg2 + " for this buffers keys: " + a.toString() + " corecon buffers: " + b.toString() + " service buffers: " + c.toString());
				networks.getBufferById(msg.arg1).setOrder(msg.arg2);
				break;

			case R.id.SET_BUFFER_TEMP_HIDDEN:
				/**
				 * Buffer has been marked as temporary hidden, update buffer
				 */
				networks.getBufferById(msg.arg1).setTemporarilyHidden((Boolean) msg.obj);
				break;

			case R.id.SET_BUFFER_PERM_HIDDEN:
				/**
				 * Buffer has been marked as permanently hidden, update buffer
				 */
				networks.getBufferById(msg.arg1).setPermanentlyHidden((Boolean) msg.obj);
				break;

			case R.id.INVALID_CERTIFICATE:
				/**
				 * Received a mismatching certificate
				 */
			case R.id.NEW_CERTIFICATE:
				/**
				 * Received a new, unseen certificate
				 */
				bundle = new Bundle();
				bundle.putString(CERT_KEY, (String) msg.obj);
				BusProvider.getInstance().post(new NewCertificateEvent((String)msg.obj));
				break;
			case R.id.SET_BUFFER_ACTIVE:
				/**
				 * Set buffer as active or parted
				 */
				networks.getBufferById(msg.arg1).setActive((Boolean)msg.obj);
				break;
			case R.id.UNSUPPORTED_PROTOCOL:
				/**
				 * The protocol version of the core is not supported so tell user it is to old
				 */
				BusProvider.getInstance().post(new UnsupportedProtocolEvent());
				break;
			case R.id.INIT_PROGRESS:
				initDone = false;
				initReason = (String)msg.obj;
				BusProvider.getInstance().post(new InitProgressEvent(false, initReason));
				break;
			case R.id.INIT_DONE:
				/**
				 * CoreConn has connected to a core
				 */
				notificationManager.notifyConnected();
				initDone = true;
				BusProvider.getInstance().post(new InitProgressEvent(true, ""));
				BusProvider.getInstance().post(new NetworksAvailableEvent(networks));
				break;
			case R.id.USER_PARTED:
				bundle = (Bundle) msg.obj;
				if (networks.getNetworkById(msg.arg1) == null) { // sure why not
					Log.w(TAG, "Unable to find network for user that parted");
					return;
				}
				networks.getNetworkById(msg.arg1).onUserParted(bundle.getString("nick"), bundle.getString("buffer"));
				break;
			case R.id.USER_QUIT:
				if (networks.getNetworkById(msg.arg1) == null) {
					System.err.println("Unable to find buffer for message");
					return;
				}
				networks.getNetworkById(msg.arg1).onUserQuit((String)msg.obj);
				break;
			case R.id.USER_JOINED:
				if (networks.getNetworkById(msg.arg1) == null) {
					System.err.println("Unable to find buffer for message");
					return;
				}
				bundle = (Bundle) msg.obj;
				user = networks.getNetworkById(msg.arg1).getUserByNick(bundle.getString("nick"));
				String modes = (String)bundle.get("mode"); 
				bufferName = (String)bundle.get("buffername");
				for (Buffer buf : networks.getNetworkById(msg.arg1).getBuffers().getRawBufferList()) {
					if(buf.getInfo().name.equalsIgnoreCase(bufferName)) {
						buf.getUsers().addUser(user, modes);
						return;
					}
				}
				//Did not find buffer in the network, something is wrong
				Log.w(TAG, "joinIrcUser: Did not find buffer with name " + bufferName);
				throw new RuntimeException("joinIrcUser: Did not find buffer with name " + bufferName);
			case R.id.USER_CHANGEDNICK:
				if (networks.getNetworkById(msg.arg1) == null) {
					Log.e(TAG, "Could not find network with id " + msg.arg1 + " for changing a user nick");
					return;
				}
				bundle = (Bundle) msg.obj;
				user = networks.getNetworkById(msg.arg1).getUserByNick(bundle.getString("oldNick"));
				if (user == null) {
					Log.e(TAG, "Unable to find user " + bundle.getString("oldNick") + " for changing nick");
					return;
				}
				user.changeNick(bundle.getString("newNick"));
				break;
			case R.id.USER_ADD_MODE:
				if (networks.getNetworkById(msg.arg1) == null) {
					System.err.println("Unable to find buffer for message");
					return;
				}
				bundle = (Bundle) msg.obj;
				bufferName = bundle.getString("channel");
				user = networks.getNetworkById(msg.arg1).getUserByNick(bundle.getString("nick"));
				for(Buffer buf : networks.getNetworkById(msg.arg1).getBuffers().getRawBufferList()) {
					if(buf.getInfo().name.equals(bufferName)) {
						buf.getUsers().addModeToUser(user, bundle.getString("mode"));
						break;
					}
				}
				break;
			case R.id.USER_REMOVE_MODE:
				if (networks.getNetworkById(msg.arg1) == null) {
					System.err.println("Unable to find buffer for message");
					return;
				}
				bundle = (Bundle) msg.obj;
				bufferName = bundle.getString("channel");
				user = networks.getNetworkById(msg.arg1).getUserByNick(bundle.getString("nick"));
				for(Buffer buf : networks.getNetworkById(msg.arg1).getBuffers().getRawBufferList()) {
						if(buf.getInfo().name.equals(bufferName)) {
							buf.getUsers().removeModeFromUser(user, bundle.getString("mode"));
							break;
						}
				}
				break;
			case R.id.CHANNEL_TOPIC_CHANGED:
				networks.getNetworkById(msg.arg1).getBuffers().getBuffer(msg.arg2).setTopic((String)msg.obj);
				break;
			case R.id.SET_CONNECTED:
				networks.getNetworkById(msg.arg1).setConnected((Boolean)msg.obj);
				break;
			case R.id.SET_MY_NICK:
				networks.getNetworkById(msg.arg1).setNick((String)msg.obj);
				break;
			case R.id.REMOVE_BUFFER:
				networks.getNetworkById(msg.arg1).removeBuffer(msg.arg2);
				break;
			case R.id.SET_CORE_LATENCY:
                latency = msg.arg1;
                BusProvider.getInstance().post(new LatencyChangedEvent(latency));
                break;
			case R.id.SET_NETWORK_LATENCY:
                networks.getNetworkById(msg.arg1).setLatency(msg.arg2);
                break;
			case R.id.RENAME_BUFFER:
				networks.getBufferById(msg.arg1).setName((String) msg.obj);
				break;
			case R.id.SET_USER_SERVER:
				bundle = (Bundle) msg.obj;
				Network networkServer = networks.getNetworkById(msg.arg1);
				if (networkServer != null) {
					IrcUser userServer = networkServer.getUserByNick(bundle.getString("nick"));
					if (userServer != null) {
						userServer.server = bundle.getString("server");
					}
				}
				break;
			case R.id.SET_USER_REALNAME:
				bundle = (Bundle) msg.obj;
				Network networkRealName = networks.getNetworkById(msg.arg1);
				if (networkRealName != null) {
					IrcUser userRealName = networkRealName.getUserByNick(bundle.getString("nick"));
					if (userRealName != null) {
						userRealName.realName = bundle.getString("realname");
					}
				}
				break;
			case R.id.SET_USER_AWAY:
				bundle = (Bundle) msg.obj;
				Network networkAway = networks.getNetworkById(msg.arg1);
				if (networkAway != null) {
					IrcUser userAway = networkAway.getUserByNick(bundle.getString("nick"));
					if (userAway != null) {
						userAway.away = bundle.getBoolean("away");
					}
				}
				break;
			}
		}
	}

	public boolean isInitComplete() {
		if(coreConn == null) return false;
		return coreConn.isInitComplete();
	}

	public Network getNetworkById(int networkId) {
        return networks.getNetworkById(networkId);
    }
	
	@Produce
	public ConnectionChangedEvent produceConnectionStatus() {
		if(isConnected())
			return new ConnectionChangedEvent(Status.Connected);
		else 
			return new ConnectionChangedEvent(Status.Disconnected);
	}
	
	@Produce
	public LatencyChangedEvent produceLatency() {
		return new LatencyChangedEvent(latency);
	}
	
	@Produce
	public NetworksAvailableEvent produceNetworksAvailable() {
		return new NetworksAvailableEvent(networks);
	}
	
	@Subscribe
	public void doJoinChannel(JoinChannelEvent event) {
		int networksStatusBufferId = -1;
		for(Network network : networks.getNetworkList()) {
			if(network.getName().equals(event.networkName)) {
				networksStatusBufferId = network.getStatusBuffer().getInfo().id;
				break;
			}
		}
		if(networksStatusBufferId != -1) {
			sendMessage(networksStatusBufferId, "/join "+ event.channelName);
			Toast.makeText(getApplicationContext(), "Joining channel " + event.channelName, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(getApplicationContext(), "Error joining channel", Toast.LENGTH_LONG).show();
		}
	}
	
	@Subscribe
	public void doDisconnectCore(DisconnectCoreEvent event) {
		disconnectFromCore();
	}
	
	@Subscribe
	public void doSendMessage(SendMessageEvent event) {
		sendMessage(event.bufferId, event.message);
	}
	
	@Subscribe
	public void doManageChannel(ManageChannelEvent event) {
		if(event.action == ChannelAction.DELETE) {
			coreConn.requestRemoveBuffer(event.bufferId);
		} else if(event.action == ChannelAction.PERM_HIDE) {
			coreConn.requestPermHideBuffer(event.bufferId);
		} else if(event.action == ChannelAction.TEMP_HIDE) {
			coreConn.requestTempHideBuffer(event.bufferId);
		} else if(event.action == ChannelAction.MARK_AS_READ) {
			coreConn.requestMarkBufferAsRead(event.bufferId);
		} else if(event.action == ChannelAction.HIGHLIGHTS_READ) {
			notificationManager.notifyHighlightsRead(event.bufferId);
		}
	}
	
	@Subscribe
	public void doManageNetwork(ManageNetworkEvent event) {
		if(event.action == NetworkAction.CONNECT) {
			coreConn.requestConnectNetwork(event.networkId);
		} else if(event.action == NetworkAction.DISCONNECT) {
			coreConn.requestDisconnectNetwork(event.networkId);
		}
	}
	
	@Subscribe
	public void doManageMessage(ManageMessageEvent event) {
		if(event.action == MessageAction.LAST_SEEN) {
			notificationManager.notifyHighlightsRead(event.bufferId);
			coreConn.requestSetLastMsgRead(event.bufferId, event.messageId);
			networks.getBufferById(event.bufferId).setLastSeenMessage(event.messageId);
		} else if(event.action == MessageAction.MARKER_LINE) {
			coreConn.requestSetMarkerLine(event.bufferId, event.messageId);
			networks.getBufferById(event.bufferId).setMarkerLineMessage(event.messageId);
		}
	}
	
	@Subscribe
	public void getGetBacklog(GetBacklogEvent event) {
		Log.d(TAG, "Fetching more backlog");
		coreConn.requestMoreBacklog(event.bufferId, event.backlogAmount);
	}
	
	@Subscribe
	public void onFilterMessages(FilterMessagesEvent event) {
		if(event.filtered) 
			networks.getBufferById(event.bufferId).addFilterType(event.filterType);
		else 
			networks.getBufferById(event.bufferId).removeFilterType(event.filterType);
	}
	
	@Produce
	public InitProgressEvent produceInitDoneEvent() {
		return new InitProgressEvent(initDone, initReason);
	}
}
