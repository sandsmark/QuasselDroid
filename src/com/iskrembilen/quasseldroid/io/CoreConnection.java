/**
    QuasselDroid - Quassel client for Android
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

package com.iskrembilen.quasseldroid.io;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.iskrembilen.quasseldroid.Buffer;
import com.iskrembilen.quasseldroid.BufferCollection;
import com.iskrembilen.quasseldroid.BufferInfo;
import com.iskrembilen.quasseldroid.CoreInfo;
import com.iskrembilen.quasseldroid.IrcMessage;
import com.iskrembilen.quasseldroid.IrcUser;
import com.iskrembilen.quasseldroid.Network;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.exceptions.UnsupportedProtocolException;
import com.iskrembilen.quasseldroid.io.CustomTrustManager.NewCertificateException;
import com.iskrembilen.quasseldroid.qtcomm.EmptyQVariantException;
import com.iskrembilen.quasseldroid.qtcomm.QDataInputStream;
import com.iskrembilen.quasseldroid.qtcomm.QDataOutputStream;
import com.iskrembilen.quasseldroid.qtcomm.QMetaType;
import com.iskrembilen.quasseldroid.qtcomm.QMetaTypeRegistry;
import com.iskrembilen.quasseldroid.qtcomm.QVariant;
import com.iskrembilen.quasseldroid.qtcomm.QVariantType;
import com.iskrembilen.quasseldroid.service.CoreConnService;

public class CoreConnection {

	private static final String TAG = CoreConnection.class.getSimpleName();

	private Socket socket;
	private QDataOutputStream outStream;
	private QDataInputStream inStream;

	private Map<Integer, Buffer> buffers;
	private CoreInfo coreInfo;
	private Map<Integer, Network> networks;

	private String address;
	private int port;
	private String username;
	private String password;
	private boolean ssl;

	CoreConnService service;
	private Timer heartbeatTimer;
	private ReadThread readThread;

	private boolean connected;
	private boolean initComplete;
	private int initBacklogBuffers;


	public CoreConnection(String address, int port, String username,
			String password, Boolean ssl, CoreConnService parent) {
		this.address = address;
		this.port = port;
		this.username = username;
		this.password = password;
		this.ssl = ssl;
		this.service = parent;

		this.connected = false;

		readThread = new ReadThread();
		readThread.start();
	}

	/**
	 * Checks whether the core is available. 
	 */	
	public boolean isConnected() {
		if (connected && 
				socket != null && socket.isConnected() &&
				readThread != null && readThread.isAlive())
			return true;
		else {
			return false;
		}
	}

	/**
	 * requests the core to set a given buffer as read
	 * @param buffer the buffer id to set as read
	 */
	public void requestMarkBufferAsRead(int buffer) {
		List<QVariant<?>> retFunc = new LinkedList<QVariant<?>>();
		retFunc.add(new QVariant<Integer>(RequestType.Sync.getValue(), QVariantType.Int));
		retFunc.add(new QVariant<String>("BufferSyncer", QVariantType.String));
		retFunc.add(new QVariant<String>("", QVariantType.String));
		retFunc.add(new QVariant<String>("requestMarkBufferAsRead", QVariantType.ByteArray));
		retFunc.add(new QVariant<Integer>(buffer, "BufferId"));

		try {
			sendQVariantList(retFunc);
		} catch (IOException e) {
			e.printStackTrace();
			connected = false;
		}
	}

	public void requestSetLastMsgRead(int buffer, int msgid) {
		List<QVariant<?>> retFunc = new LinkedList<QVariant<?>>();
		retFunc.add(new QVariant<Integer>(RequestType.Sync.getValue(), QVariantType.Int));
		retFunc.add(new QVariant<String>("BufferSyncer", QVariantType.String));
		retFunc.add(new QVariant<String>("", QVariantType.String));
		retFunc.add(new QVariant<String>("requestSetLastSeenMsg", QVariantType.ByteArray));
		retFunc.add(new QVariant<Integer>(buffer, "BufferId"));
		retFunc.add(new QVariant<Integer>(msgid, "MsgId"));

		try {
			sendQVariantList(retFunc);
		} catch (IOException e) {
			e.printStackTrace();
			connected = false;
		}
	}

	public void requestSetMarkerLine(int buffer, int msgid) {
		List<QVariant<?>> retFunc = new LinkedList<QVariant<?>>();
		retFunc.add(new QVariant<Integer>(RequestType.Sync.getValue(), QVariantType.Int));
		retFunc.add(new QVariant<String>("BufferSyncer", QVariantType.String));
		retFunc.add(new QVariant<String>("", QVariantType.String));
		retFunc.add(new QVariant<String>("requestSetMarkerLine", QVariantType.ByteArray));
		retFunc.add(new QVariant<Integer>(buffer, "BufferId"));
		retFunc.add(new QVariant<Integer>(msgid, "MsgId"));

		try {
			sendQVariantList(retFunc);
		} catch (IOException e) {
			e.printStackTrace();
			connected = false;
		}
	}


	/**
	 * Requests all buffers.
	 */

	/**
	 * Requests the unread backlog for a given buffer.
	 */
	public void requestUnreadBacklog(int buffer) {
		requestBacklog(buffer, buffers.get(buffer).getLastSeenMessage());
	}

	/**
	 * Requests moar backlog for a give buffer
	 * @param buffer Buffer id to request moar for
	 */
	public void requestMoreBacklog(int buffer, int amount) {
		if (buffers.get(buffer).getUnfilteredSize()==0) {
			requestBacklog(buffer, -1, -1, amount);			
		}else {
//			Log.e(TAG, "GETTING: "+buffers.get(buffer).getUnfilteredBacklogEntry(0).messageId);
			requestBacklog(buffer, -1, buffers.get(buffer).getUnfilteredBacklogEntry(0).messageId, amount);			
		}
	}

	/**
	 * Requests all backlog from a given message ID until the current. 
	 */
	private void requestBacklog(int buffer, int firstMsgId) {
		requestBacklog(buffer, firstMsgId, -1);
	}

	/**
	 * Requests backlog between two given message IDs.
	 */
	private void requestBacklog(int buffer, int firstMsgId, int lastMsgId) {
		requestBacklog(buffer, firstMsgId, lastMsgId, 10); //TODO: get the number from the shared preferences 
	}

	private void requestBacklog(int buffer, int firstMsgId, int lastMsgId, int maxAmount) {
		List<QVariant<?>> retFunc = new LinkedList<QVariant<?>>();
		retFunc.add(new QVariant<Integer>(RequestType.Sync.getValue(), QVariantType.Int));
		retFunc.add(new QVariant<String>("BacklogManager", QVariantType.String));
		retFunc.add(new QVariant<String>("", QVariantType.String));
		retFunc.add(new QVariant<String>("requestBacklog", QVariantType.String));
		retFunc.add(new QVariant<Integer>(buffer, "BufferId"));
		retFunc.add(new QVariant<Integer>(firstMsgId, "MsgId"));
		retFunc.add(new QVariant<Integer>(lastMsgId, "MsgId"));
		retFunc.add(new QVariant<Integer>(maxAmount, QVariantType.Int));
		retFunc.add(new QVariant<Integer>(0, QVariantType.Int));

		try {
			sendQVariantList(retFunc);
		} catch (IOException e) {
			e.printStackTrace();
			connected = false;
		}
	}

	/**
	 * Sends an IRC message to a given buffer
	 * @param buffer buffer to send to
	 * @param message content of message
	 */
	public void sendMessage(int buffer, String message) {
		if (message.charAt(0) == '/') {
			String t[] = message.split(" ");

			message = t[0].toUpperCase();
			if (t.length > 1){
				for (int i=1; i<t.length; i++) {
					message += ' ' + t[i];
				}
			}
		} else {
			message = "/SAY " + message;
		}

		List<QVariant<?>> retFunc = new LinkedList<QVariant<?>>();
		retFunc.add(new QVariant<Integer>(RequestType.RpcCall.getValue(), QVariantType.Int));
		retFunc.add(new QVariant<String>("2sendInput(BufferInfo,QString)", QVariantType.String));
		retFunc.add(new QVariant<BufferInfo>(buffers.get(buffer).getInfo(), "BufferInfo"));
		retFunc.add(new QVariant<String>(message, QVariantType.String));	
		try {
			sendQVariantList(retFunc);
		} catch (IOException e) {
			e.printStackTrace();
			connected = false;
		}
	}


	/**
	 * Initiates a connection.
	 * @throws EmptyQVariantException 
	 * @throws UnsupportedProtocolException 
	 */
	public void connect() throws UnknownHostException, IOException, GeneralSecurityException, CertificateException, NewCertificateException, EmptyQVariantException, UnsupportedProtocolException {	
		// START CREATE SOCKETS
		SocketFactory factory = (SocketFactory)SocketFactory.getDefault();
		socket = (Socket)factory.createSocket(address, port);
		socket.setKeepAlive(true);
		outStream = new QDataOutputStream(socket.getOutputStream());
		// END CREATE SOCKETS 


		// START CLIENT INFO
		Map<String, QVariant<?>> initial = new HashMap<String, QVariant<?>>();

		DateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy HH:mm:ss");
		Date date = new Date();
		initial.put("ClientDate", new QVariant<String>(dateFormat.format(date), QVariantType.String));
		initial.put("UseSsl", new QVariant<Boolean>(ssl, QVariantType.Bool));
		initial.put("ClientVersion", new QVariant<String>("v0.6.1 (dist-<a href='http://git.quassel-irc.org/?p=quassel.git;a=commit;h=611ebccdb6a2a4a89cf1f565bee7e72bcad13ffb'>611ebcc</a>)", QVariantType.String));
		initial.put("UseCompression", new QVariant<Boolean>(false, QVariantType.Bool));
		initial.put("MsgType", new QVariant<String>("ClientInit", QVariantType.String));
		initial.put("ProtocolVersion", new QVariant<Integer>(10, QVariantType.Int));

		sendQVariantMap(initial);
		// END CLIENT INFO

		// START CORE INFO
		inStream = new QDataInputStream(socket.getInputStream());
		Map<String, QVariant<?>> reply = readQVariantMap();
		System.out.println("CORE INFO: ");
		coreInfo = new CoreInfo();
		coreInfo.setCoreFeatures((Integer)reply.get("CoreFeatures").getData());
		coreInfo.setCoreInfo((String)reply.get("CoreInfo").getData());
		coreInfo.setSupportSsl((Boolean)reply.get("SupportSsl").getData());
		coreInfo.setCoreDate(new Date((String)reply.get("CoreDate").getData()));
		coreInfo.setCoreStartTime((GregorianCalendar)reply.get("CoreStartTime").getData());
		String coreVersion = (String)reply.get("CoreVersion").getData(); //CoreVersion : v0.7.1 (git-<a href="http://git.quassel-irc.org/?p=quassel.git;a=commit;h=aa285964d0e486a681f56254dc123857c15c66fa">aa28596</a>)
		coreVersion = coreVersion.substring(coreVersion.indexOf("v")+1, coreVersion.indexOf(" "));
		coreInfo.setCoreVersion(coreVersion);
		coreInfo.setConfigured((Boolean)reply.get("Configured").getData());
		coreInfo.setLoginEnabled((Boolean)reply.get("LoginEnabled").getData());
		coreInfo.setMsgType((String)reply.get("MsgType").getData());
		coreInfo.setProtocolVersion(((Long)reply.get("ProtocolVersion").getData()).intValue());
		coreInfo.setSupportsCompression((Boolean)reply.get("SupportsCompression").getData());
		
		Matcher matcher = Pattern.compile("(\\d+)\\W(\\d+)\\W", Pattern.CASE_INSENSITIVE).matcher(coreInfo.getCoreVersion());
		System.out.println(coreInfo.getCoreVersion());
		int version, release;
		if (matcher.find()) {
			version = Integer.parseInt(matcher.group(1));
			release = Integer.parseInt(matcher.group(2));
		} else {
			throw new IOException("Can't match core version, illegal version?");
		}
		
		//Check that the protocol version is atleast 10 and the version is above 0.6.0
		if(coreInfo.getProtocolVersion()<10 || !(version>0 || (version==0 && release>=6)))
			throw new UnsupportedProtocolException("Protocol version is old: "+coreInfo.getProtocolVersion());
		/*for (String key : reply.keySet()) {
			System.out.println("\t" + key + " : " + reply.get(key));
		}*/
		// END CORE INFO

		// START SSL CONNECTION
		if (ssl) {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			TrustManager[] trustManagers = new TrustManager [] { new CustomTrustManager(this) };
			sslContext.init(null, trustManagers, null);
			SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
			SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(socket, address, port, true);
			sslSocket.setEnabledProtocols(new String[] {"SSLv3"});

			sslSocket.setUseClientMode(true);
			sslSocket.startHandshake();
			inStream = new QDataInputStream(sslSocket.getInputStream());
			outStream = new QDataOutputStream(sslSocket.getOutputStream());
			socket = sslSocket;
		} else {
			System.err.println("SSL DISABLED!");
		}
		// FINISHED SSL CONNECTION


		// START LOGIN
		Map<String, QVariant<?>> login = new HashMap<String, QVariant<?>>();
		login.put("MsgType", new QVariant<String>("ClientLogin", QVariantType.String));
		login.put("User", new QVariant<String>(username, QVariantType.String));
		login.put("Password", new QVariant<String>(password, QVariantType.String));
		sendQVariantMap(login);
		// FINISH LOGIN


		// START LOGIN ACK 
		reply = readQVariantMap();
		if (!reply.get("MsgType").toString().equals("ClientLoginAck"))
			throw new GeneralSecurityException("Invalid password?");
		// END LOGIN ACK


		// START SESSION INIT
		reply = readQVariantMap();
		/*System.out.println("SESSION INIT: ");
		for (String key : reply.keySet()) {
			System.out.println("\t" + key + " : " + reply.get(key));
		}*/

		Map<String, QVariant<?>> sessionState = (Map<String, QVariant<?>>) reply.get("SessionState").getData();
		System.out.println(sessionState);
		List<QVariant<?>> networkIds = (List<QVariant<?>>) sessionState.get("NetworkIds").getData();
		networks = new HashMap<Integer, Network>(networkIds.size());
		for (QVariant<?> networkId: networkIds) {
			Integer id = (Integer) networkId.getData();
			networks.put(id, new Network(id));
		}
		
		List<QVariant<?>> bufferInfos = (List<QVariant<?>>) sessionState.get("BufferInfos").getData();
		buffers = new HashMap<Integer, Buffer>(bufferInfos.size());
		QuasselDbHelper dbHelper = new QuasselDbHelper(service.getApplicationContext());
		ArrayList<Integer> bufferIds = new ArrayList<Integer>();
		for (QVariant<?> bufferInfoQV: bufferInfos) {
			BufferInfo bufferInfo = (BufferInfo)bufferInfoQV.getData();
			Buffer buffer = new Buffer(bufferInfo, dbHelper);
			buffers.put(bufferInfo.id, buffer);
			if(bufferInfo.type==BufferInfo.Type.StatusBuffer){
				networks.get(bufferInfo.networkId).setStatusBuffer(buffer);
			}else{
				networks.get(bufferInfo.networkId).addBuffer(buffer);
			}
			bufferIds.add(bufferInfo.id);
		}
		dbHelper.open();
		dbHelper.cleanupEvents(bufferIds.toArray(new Integer[bufferIds.size()]));
		dbHelper.close();

		// END SESSION INIT

		// Now the fun part starts, where we play signal proxy

		// START SIGNAL PROXY INIT

		// We must do this here, to get network names early enough
		for(Network network: networks.values()) {
			sendInitRequest("Network", Integer.toString(network.getId()));
		}
		sendInitRequest("BufferSyncer", "");
		//sendInitRequest("BufferViewManager", ""); this is about where this should be, but don't know what it does
		sendInitRequest("BufferViewConfig", "0");

		int backlogAmout = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(service).getString(service.getString(R.string.preference_initial_backlog_limit), "1"));
		initBacklogBuffers = 0;
		for (Buffer buffer:buffers.values()) {
			initBacklogBuffers += 1;
			requestMoreBacklog(buffer.getInfo().id, backlogAmout);
		}

		TimerTask sendPingAction = new TimerTask() {
			public void run() {
				List<QVariant<?>> packedFunc = new LinkedList<QVariant<?>>();
				packedFunc.add(new QVariant<Integer>(RequestType.HeartBeat.getValue(), QVariantType.Int));
				packedFunc.add(new QVariant<Calendar>(Calendar.getInstance(), QVariantType.Time));
				try {
					sendQVariantList(packedFunc);
				} catch (IOException e) {
					e.printStackTrace();
					disconnect();
				}
			}
		};
		heartbeatTimer = new Timer();
		heartbeatTimer.schedule(sendPingAction, 30000, 30000); // Send heartbeats every 30 seconds

		// END SIGNAL PROXY
		Log.i(TAG, "Connected!");
		connected = true;

		Message msg = service.getHandler().obtainMessage(R.id.CONNECTED);
		msg.sendToTarget();
		initComplete = false;
	}

	/**
	 * Attempts to disconnect from the core, as best as we can.
	 * Java sucks.
	 */
	public void disconnect() {
		if (heartbeatTimer!=null) {
			heartbeatTimer.cancel(); // Has this stopped executing now? Nobody knows.
		}
		try {
			if (outStream != null)
				outStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		readThread.running = false;

		connected = false;
	}

	/****************************
	 * Private internal communication stuff.
	 * Please don't look below this line.
	 * @author sandsmark
	 */

	/**
	 * Type of a given request (should be pretty self-explanatory).
	 */
	private enum RequestType {
		Invalid(0),
		Sync(1),
		RpcCall(2),
		InitRequest(3),
		InitData(4),
		HeartBeat(5),
		HeartBeatReply(6);

		// Below this line; java sucks. Hard.
		int value;
		RequestType(int value){
			this.value = value;
		}
		public int getValue(){
			return value;
		}

		public static RequestType getForVal(int val) {
			for (RequestType type: values()) {
				if (type.value == val)
					return type;
			}
			return Invalid;
		}
	}

	/**
	 * Convenience function to send a given QVariant.
	 * @param data QVariant to send.
	 */
	private synchronized void sendQVariant(QVariant<?> data) throws IOException {
		// See how much data we're going to send
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		QDataOutputStream bos = new QDataOutputStream(baos);
		QMetaTypeRegistry.serialize(QMetaType.Type.QVariant, bos, data);

		// Tell the other end how much data to expect
		outStream.writeUInt(bos.size(), 32);

		// Sanity check, check that we can decode our own stuff before sending it off
		//QDataInputStream bis = new QDataInputStream(new ByteArrayInputStream(baos.toByteArray()));
		//QMetaTypeRegistry.instance().getTypeForId(QMetaType.Type.QVariant.getValue()).getSerializer().unserialize(bis, DataStreamVersion.Qt_4_2);

		// Send data 
		QMetaTypeRegistry.serialize(QMetaType.Type.QVariant, outStream, data);
	}

	/**
	 * Convenience function to send a given QVariantMap.
	 * @param data the given QVariantMap to send.
	 */
	private void sendQVariantMap(Map<String, QVariant<?>> data) throws IOException {
		QVariant<Map<String, QVariant<?>>> bufstruct = new QVariant<Map<String, QVariant<?>>>(data, QVariantType.Map);
		sendQVariant(bufstruct);
	}

	/**
	 * A convenience function to send a given QVariantList.
	 * @param data The QVariantList to send.
	 */
	private void sendQVariantList(List<QVariant<?>> data) throws IOException {
		QVariant<List<QVariant<?>>> bufstruct = new QVariant<List<QVariant<?>>>(data, QVariantType.List);
		sendQVariant(bufstruct);
	}

	/**
	 * A convenience function to read a QVariantMap.
	 * @throws EmptyQVariantException 
	 */
	private Map<String, QVariant<?>> readQVariantMap() throws IOException, EmptyQVariantException {
		// Length of this packet (why do they send this? noone knows!).
		inStream.readUInt(32);
		QVariant <Map<String, QVariant<?>>> v = (QVariant <Map<String, QVariant<?>>>)QMetaTypeRegistry.unserialize(QMetaType.Type.QVariant, inStream);

		Map<String, QVariant<?>>ret = (Map<String, QVariant<?>>)v.getData();
		//		System.out.println(ret.toString());
		return ret;
	}

	/**
	 * A convenience function to read a QVariantList.
	 * @throws EmptyQVariantException 
	 */	
	private List<QVariant<?>> readQVariantList() throws IOException, EmptyQVariantException {	
		inStream.readUInt(32); // Length
		QVariant <List<QVariant<?>>> v = (QVariant <List<QVariant<?>>>)QMetaTypeRegistry.unserialize(QMetaType.Type.QVariant, inStream);

		List<QVariant<?>>ret = (List<QVariant<?>>)v.getData();
		//		System.out.println(ret.toString());
		return ret;
	}

	/**
	 * Convenience function to request an init of a given object.
	 * @param className The class name of the object we want.
	 * @param objectName The name of the object we want.
	 */
	private void sendInitRequest(String className, String objectName) throws IOException {
		List<QVariant<?>> packedFunc = new LinkedList<QVariant<?>>();
		packedFunc.add(new QVariant<Integer>(RequestType.InitRequest.getValue(), QVariantType.Int));
		packedFunc.add(new QVariant<String>(className, QVariantType.String));
		packedFunc.add(new QVariant<String>(objectName, QVariantType.String));
		sendQVariantList(packedFunc);
	}
	
	private void updateInitProgress(String message) {
		service.getHandler().obtainMessage(R.id.INIT_PROGRESS, message).sendToTarget();
	}
	
	private void updateInitDone() {
		initComplete = true;
		service.getHandler().obtainMessage(R.id.INIT_DONE).sendToTarget();
	}

	private class ReadThread extends Thread {
		boolean running = false;

		CountDownTimer checkAlive = new CountDownTimer(180000, 180000) {
			@Override
			public void onTick(long millisUntilFinished) {
				//Do nothing, no use
			}
			@Override
			public void onFinish() {
				Log.i(TAG, "Timer finished, disconnection from core");
				CoreConnection.this.disconnect(); 
				Message msg = service.getHandler().obtainMessage(R.id.LOST_CONNECTION);
				msg.sendToTarget();
			}
		};

		public void run() {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(CoreConnection.this.service);
			boolean doWakeLock = preferences.getBoolean(CoreConnection.this.service.getString(R.string.preference_wake_lock), true);
			
			PowerManager pm = (PowerManager) CoreConnection.this.service.getSystemService(Context.POWER_SERVICE);
			WakeLock lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Heute ist mein tag");
			if (doWakeLock)
				 lock.acquire();
			
			try {
				doRun();
			} catch (EmptyQVariantException e) {
				service.getHandler().obtainMessage(R.id.LOST_CONNECTION, "Protocol error!").sendToTarget();
				e.printStackTrace();
				disconnect();
				return;
			} finally {
				if (doWakeLock)
					lock.release();
			}
		}

		public void doRun() throws EmptyQVariantException {
			this.running = true;

			try {
				connect();
				// ↓↓↓↓ FIXME TODO HANDLE THESE YOU DICKWEEDS! ↓↓↓↓
			} catch (UnknownHostException e) {
				service.getHandler().obtainMessage(R.id.LOST_CONNECTION, "Unknown host!").sendToTarget();
				return;
			} catch (UnsupportedProtocolException e) {
				service.getHandler().obtainMessage(R.id.UNSUPPORTED_PROTOCOL).sendToTarget();
				Log.w(TAG, e);
				disconnect();
				return;
			} catch (IOException e) {
				if(e.getCause() instanceof NewCertificateException) {
					service.getHandler().obtainMessage(R.id.INVALID_CERTIFICATE, ((NewCertificateException)e.getCause()).hashedCert()).sendToTarget();					
					disconnect();
				}else{
					service.getHandler().obtainMessage(R.id.LOST_CONNECTION, "IO error while connecting! " + e.getMessage()).sendToTarget();
					e.printStackTrace();
					disconnect();
				}
				return;
			} catch (CertificateException e) {
				service.getHandler().obtainMessage(R.id.INVALID_CERTIFICATE, "Invalid SSL certificate from core!").sendToTarget();
				disconnect();
				return;
			} catch (GeneralSecurityException e) {
				service.getHandler().obtainMessage(R.id.LOST_CONNECTION, "Invalid username/password combination.").sendToTarget();
				disconnect();
				return;
			} catch (EmptyQVariantException e) {
				service.getHandler().obtainMessage(R.id.LOST_CONNECTION, "IO error while connecting!").sendToTarget();
				e.printStackTrace();
				disconnect();
				return;
			}

			List<QVariant<?>> packedFunc;
			final long startWait = System.currentTimeMillis();
			while (running) {

				try {
					packedFunc = readQVariantList();
					//Log.i(TAG, "Slow core is slow: " + (System.currentTimeMillis() - startWait) + "ms");
				} catch (IOException e) {
					//TODO: not sure if this is really the best way to check if we are connected, by just waiting until it fails, but will have to do for now
					CoreConnection.this.disconnect(); 
					Message msg = service.getHandler().obtainMessage(R.id.LOST_CONNECTION);
					msg.sendToTarget();

					System.err.println("IO error!");	
					e.printStackTrace();
					this.running = false;
					CoreConnection.this.connected = false;
					return;
				}
				//We received a package, aka we are not disconnected, restart timer
				//Log.i(TAG, "Package reviced, reseting countdown");
				checkAlive.cancel();
				checkAlive.start();

				long start = System.currentTimeMillis();
				RequestType type = RequestType.getForVal((Integer)packedFunc.remove(0).getData());
				String className = "", objectName;


				/*
				 * Here we handle different calls from the core.
				 */
				switch (type) {
				/*
				 * A heartbeat is a simple request sent with fixed intervals,
				 * to make sure that both ends are still connected (apparently, TCP isn't good enough).
				 * TODO: We should use this, and disconnect automatically when the core has gone away.
				 */
				case HeartBeat:
					Log.i(TAG, "Got heartbeat");
					List<QVariant<?>> packet = new LinkedList<QVariant<?>>();
					packet.add(new QVariant<Integer>(RequestType.HeartBeatReply.getValue(), QVariantType.Int));
					packet.add(new QVariant<Calendar>(Calendar.getInstance(), QVariantType.Time));
					try {
						sendQVariantList(packet);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case HeartBeatReply:
					Log.i(TAG, "Got heartbeat reply");
					break;
					/*
					 * This is when the core send us a new object to create.
					 * Since we don't actually create objects, we parse out the fields
					 * in the objects manually.
					 */
				case InitData:
					// The class name and name of the object we are about to create
					className = (String) packedFunc.remove(0).getData();
					objectName = (String) packedFunc.remove(0).getData();

					/*
					 * An object representing an IRC network, containing users and channels ("buffers"). 
					 */
					if (className.equals("Network")) {
						int networkId = Integer.parseInt(objectName);
						Network network = networks.get(networkId);

						Map<String, QVariant<?>> initMap = (Map<String, QVariant<?>>) packedFunc.remove(0).getData();
						// Store the network name and associated nick for "our" user
						network.setNick((String) initMap.get("myNick").getData());
						network.setName((String) initMap.get("networkName").getData());
						boolean isConnected = (Boolean)initMap.get("isConnected").getData();
						network.setConnected(isConnected);
						if(network.getStatusBuffer() != null)
							network.getStatusBuffer().setActive(isConnected);
						
						//we got enough info to tell service we are parsing network
						Log.i(TAG, "Started parsing network " + network.getName());
						updateInitProgress("Receiving network: " +network.getName());

						// Horribly nested maps
						Map<String, QVariant<?>> usersAndChans = (Map<String, QVariant<?>>) initMap.get("IrcUsersAndChannels").getData();
						Map<String, QVariant<?>> channels = (Map<String, QVariant<?>>) usersAndChans.get("channels").getData();

						// Parse out the list of nicks in all channels, and topics
						for (QVariant<?> channel:  channels.values()) {
							Map<String, QVariant<?>> chan = (Map<String, QVariant<?>>) channel.getData();
							String chanName = (String)chan.get("name").getData();
							Map<String, QVariant<?>> userModes = (Map<String, QVariant<?>>) chan.get("UserModes").getData();
							List<String> users = new ArrayList<String>(userModes.keySet());
							String topic = (String)chan.get("topic").getData();
							// Horribly inefficient search for the right buffer, Java sucks.

							Map<String, QVariant<?>> userObjs = (Map<String, QVariant<?>>) usersAndChans.get("users").getData();

							ArrayList<IrcUser> ircUsers = new ArrayList<IrcUser>();

							for (String nick : userObjs.keySet()) {
								IrcUser user = new IrcUser();
								user.name = nick;
								Map<String, QVariant<?>> map = (Map<String, QVariant<?>>) userObjs.get(nick).getData();
								user.away = (Boolean) map.get("away").getData();
								user.awayMessage = (String) map.get("awayMessage").getData();
								user.ircOperator = (String) map.get("ircOperator").getData();
								user.nick = (String) map.get("nick").getData();
								user.channels = (List<String>) map.get("channels").getData();

								ircUsers.add(user);
							}
							network.setUserList(ircUsers);

							for (Buffer buffer: network.getBuffers().getRawBufferList()) {
								if (buffer.getInfo().name.equals(chanName)) {
									buffer.setTopic(topic);
									buffer.setNicks(users);
									buffer.setActive(true);
									break;
								}
							}

							service.getHandler().obtainMessage(R.id.NEW_USERLIST_ADDED, ircUsers).sendToTarget();

						}
						
						Log.i(TAG, "Sending network " + network.getName() + " to service");
						service.getHandler().obtainMessage(R.id.ADD_NETWORK, network).sendToTarget();
						
						try {
							//sendInitRequest("BufferSyncer", "");
							/*sendInitRequest("BufferViewManager", "");
								sendInitRequest("AliasManager", "");
								sendInitRequest("NetworkConfig", "GlobalNetworkConfig");
								sendInitRequest("IgnoreListManager", "");*/

							List<QVariant<?>> reqPackedFunc = new LinkedList<QVariant<?>>();
							reqPackedFunc.add(new QVariant<Integer>(RequestType.Sync.getValue(), QVariantType.Int));
							reqPackedFunc.add(new QVariant<String>("BufferSyncer", QVariantType.String));
							reqPackedFunc.add(new QVariant<String>("", QVariantType.String));
							reqPackedFunc.add(new QVariant<String>("requestPurgeBufferIds", QVariantType.String));

							sendQVariantList(reqPackedFunc);
						} catch (IOException e) {
							e.printStackTrace();
							running = false;
						}



						long endWait = System.currentTimeMillis();
						Log.w(TAG, "Network parsed, took: "+(endWait-startWait));
						/*
						 * An object that is used to synchronize metadata about buffers,
						 * like the last seen message, marker lines, etc.
						 */
					} else if (className.equals("BufferSyncer")) {
						// Parse out the last seen messages
						updateInitProgress("Receiving last seen and marker lines");

						
						List<QVariant<?>> lastSeen = (List<QVariant<?>>) ((Map<String, QVariant<?>>)packedFunc.get(0).getData()).get("LastSeenMsg").getData();
						for (int i=0; i<lastSeen.size(); i+=2) {
							int bufferId = (Integer)lastSeen.get(i).getData();
							int msgId = (Integer)lastSeen.get(i+1).getData();
							if (buffers.containsKey(bufferId)){ // We only care for buffers we have open
								Message msg = service.getHandler().obtainMessage(R.id.SET_LAST_SEEN_TO_SERVICE);
								msg.arg1 = bufferId;
								msg.arg2 = msgId;
								msg.sendToTarget();
							}else{
								Log.e(TAG, "Getting last seen message for buffer we dont have " +bufferId);
							}
						}
						// Parse out the marker lines for buffers if the core supports them
						QVariant<?> rawMarkerLines = ((Map<String, QVariant<?>>)packedFunc.get(0).getData()).get("MarkerLines");
						if(rawMarkerLines != null) {
							List<QVariant<?>> markerLines = (List<QVariant<?>>) rawMarkerLines.getData();
							for (int i=0; i<markerLines.size(); i+=2) {
								int bufferId = (Integer)markerLines.get(i).getData();
								int msgId = (Integer)markerLines.get(i+1).getData();
								if (buffers.containsKey(bufferId)){
									Message msg = service.getHandler().obtainMessage(R.id.SET_MARKERLINE_TO_SERVICE);
									msg.arg1 = bufferId;
									msg.arg2 = msgId;
									msg.sendToTarget();
								}else{
									Log.e(TAG, "Getting markerlinemessage for buffer we dont have " +bufferId);
								}
							}
						}else{
							Log.e(TAG, "Marker lines are null in BufferSyncer, should not happen");
						}

						/*
						 * A class representing another user on a given IRC network.
						 */
					} else if (className.equals("IrcUser")) {
						IrcUser user = new IrcUser();
						user.name = className;
						Map<String, QVariant<?>> map = (Map<String, QVariant<?>>) packedFunc.remove(0).getData();
						user.away = (Boolean) map.get("away").getData();
						user.awayMessage = (String) map.get("awayMessage").getData();
						user.ircOperator = (String) map.get("ircOperator").getData();
						user.nick = (String) map.get("nick").getData();
						user.channels = (List<String>) map.get("channels").getData();

						Message msg = service.getHandler().obtainMessage(R.id.NEW_USER_ADDED);
						msg.obj = (IrcUser) user;
						msg.sendToTarget();
					}
					//TODO: after making network object come back and fix this. Needs that shit
//					else if (className.equals("IrcChannel")) {
//						System.out.println(packedFunc.toString() + " Object: "+objectName);
//						// topic, UserModes, password, ChanModes, name
//						Map<String, QVariant<?>> map = (Map<String, QVariant<?>>) packedFunc.remove(0).getData();
////						String bufferName = map.get("name");
//						Buffer buffer;
////						if(buffers.containsKey(bufferName) {
////							buffer = buffers.get(bufferName);
////						}
//						
//						
//					} 
					else if (className.equals("BufferViewConfig")) {
						Map<String, QVariant<?>> map = (Map<String, QVariant<?>>) packedFunc.remove(0).getData();
						List<QVariant<?>> tempList = (List<QVariant<?>>) map.get("TemporarilyRemovedBuffers").getData();
						List<QVariant<?>> permList = (List<QVariant<?>>) map.get("RemovedBuffers").getData();
						List<QVariant<?>> orderList = (List<QVariant<?>>) map.get("BufferList").getData();
						updateInitProgress("Receiving buffer list information");
						BufferCollection.orderAlphabetical = (Boolean) map.get("sortAlphabetically").getData();


						//TODO: mabye send this in a bulk to the service so it wont sort and shit every time
						for (QVariant bufferId: tempList) {
							if (!buffers.containsKey(bufferId.getData())) {
								Log.e(TAG, "TempList, dont't have buffer: " +bufferId.getData());
								continue;
							}
							Message msg = service.getHandler().obtainMessage(R.id.SET_BUFFER_TEMP_HIDDEN);
							msg.arg1 = ((Integer) bufferId.getData());
							msg.obj = true;
							msg.sendToTarget();
						}

						for (QVariant bufferId: permList) {
							if (!buffers.containsKey(bufferId.getData())) {
								Log.e(TAG, "TempList, dont't have buffer: " +bufferId.getData());
								continue;
							}
							Message msg = service.getHandler().obtainMessage(R.id.SET_BUFFER_PERM_HIDDEN);
							msg.arg1 = ((Integer) bufferId.getData());
							msg.obj = true;
							msg.sendToTarget();
						}

						int order = 0;
						for (QVariant bufferId: orderList) {
							if (!buffers.containsKey(bufferId.getData())) {
								System.err.println("got buffer info for non-existant buffer id: " + bufferId.getData());
								continue;
							}
							//buffers.get(bufferId.getData()).setOrder(order);
							Message msg = service.getHandler().obtainMessage(R.id.SET_BUFFER_ORDER);
							msg.arg1 = (Integer) bufferId.getData();
							msg.arg2 = order;
							msg.sendToTarget();

							order++;
						}
						updateInitProgress("Receiving backlog");

						/*
						 * There are several objects that we don't care about (at the moment).
						 */
					} else {
						System.out.println("Unparsed InitData: " + className + "(" + objectName + ").");
					}
					break;
					/*
					 * Sync requests are sent by the core whenever an object needs to be updated.
					 * Again, we just parse out whatever we need manually
					 */
				case Sync:
					/* See above; parse out information about object, 
					 * and additionally a sync function name.
					 */
					Object foo = packedFunc.remove(0).getData();
					//System.out.println("FUCK" + foo.toString() + " balle " + foo.getClass().getName());
					/*if (foo.getClass().getName().equals("java.nio.ReadWriteHeapByteBuffer")) {
						try {
							System.out.println("faen i helvete: " + new String(((ByteBuffer)foo).array(), "UTF-8"));
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}						
					}*/
					className = (String)foo; // This is either a byte buffer or a string
					objectName = (String) packedFunc.remove(0).getData();
					String function = packedFunc.remove(0).toString();

					/*
					 * The BacklogManager object is responsible for synchronizing backlog
					 * between the core and the client.
					 * 
					 * The receiveBacklog function is called in the client with a given (requested)
					 * amount of messages.
					 */
					if (className.equals("BacklogManager") && function.equals("receiveBacklog")) {
						/* Here we first just dump some unused data;
						 * the buffer id is embedded in the message itself (in a bufferinfo object),
						 * the rest of the arguments aren't used at all, apparently.
						 */
						packedFunc.remove(0); // Buffer ID (Integer)
						packedFunc.remove(0); // first message
						packedFunc.remove(0); // last message
						packedFunc.remove(0); // limit to how many messages to fetch
						packedFunc.remove(0); // additional messages to fetch
						List<QVariant<?>> data = (List<QVariant<?>>)(packedFunc.remove(0).getData());
						Collections.reverse(data); // Apparently, we receive them in the wrong order
					
						// Send our the backlog messages to our listeners
						for (QVariant<?> message: data) {
							Message msg = service.getHandler().obtainMessage(R.id.NEW_BACKLOGITEM_TO_SERVICE);
							msg.obj = (IrcMessage) message.getData();
							msg.sendToTarget();
						}
						if(!initComplete) { //We are still initializing backlog for the first time
							initBacklogBuffers -= 1;
							if(initBacklogBuffers<=0) {
								updateInitDone();
							}
						}
						/* 
						 * The addIrcUser function in the Network class is called whenever a new
						 * IRC user appears on a given network. 
						 */
					} else if (className.equals("Network") && function.equals("addIrcUser")) {
						String nick = (String) packedFunc.remove(0).getData();
						try {
							sendInitRequest("IrcUser", objectName+"/" + nick);
						} catch (IOException e) {
							e.printStackTrace();
							running = false; // We have obviously lost our connection, just stop this thread.

						}
					} else if (className.equals("Network") && function.equals("addIrcChannel")) {
						String bufferName = (String) packedFunc.remove(0).getData();
						try {
							sendInitRequest("IrcChannel", objectName+"/" + bufferName);
						} catch (IOException e) {
							e.printStackTrace();
							running = false; // We have obviously lost our connection, just stop this thread.
						}

					} 
					//TODO: need network objects to lookup buffers in given networks
//					else if (className.equals("IrcUser") && function.equals("partChannel")) {
//						System.out.println(packedFunc.toString()+" objectname: "+ objectName);
//						String[] tmp = objectName.split("/");
//						int networkId = Integer.parseInt(tmp[1]);
//						String userName = tmp[1];
//						
//					} 
					else if (className.equals("BufferSyncer") && function.equals("setLastSeenMsg")) {
						int bufferId = (Integer) packedFunc.remove(0).getData();
						int msgId = (Integer) packedFunc.remove(0).getData();
						Message msg = service.getHandler().obtainMessage(R.id.SET_LAST_SEEN_TO_SERVICE);
						msg.arg1 = bufferId;
						msg.arg2 = msgId;
						msg.sendToTarget();

					} else if (className.equals("BufferSyncer") && function.equals("setMarkerLine")) {
						int bufferId = (Integer) packedFunc.remove(0).getData();
						int msgId = (Integer) packedFunc.remove(0).getData();
						Message msg = service.getHandler().obtainMessage(R.id.SET_MARKERLINE_TO_SERVICE);
						msg.arg1 = bufferId;
						msg.arg2 = msgId;
						msg.sendToTarget();			

						/*
						 * markBufferAsRead is called whenever a given buffer is set as read by the core. 
						 */
					} else if (className.equals("BufferSyncer") && function.equals("markBufferAsRead")) {
						//TODO: this basicly does shit. So find out if it effects anything and what it should do
						//int buffer = (Integer) packedFunc.remove(0).getData();
						//buffers.get(buffer).setRead();
					} else {
						System.out.println("Unparsed Sync request: " + className + "::" + function);
					}

					break;

					/*
					 * Remote procedure calls are direct calls that are not associated with any objects.
					 */
				case RpcCall:
					// Contains a normalized function signature; see QMetaObject::normalizedSignature, I guess.
					String functionName = packedFunc.remove(0).toString();

					/*
					 * This is called by the core when a new message should be displayed.
					 */
					if (functionName.equals("2displayMsg(Message)")) {
						IrcMessage message = (IrcMessage) packedFunc.remove(0).getData();
						Message msg = service.getHandler().obtainMessage(R.id.NEW_MESSAGE_TO_SERVICE);
						msg.obj = message;
						msg.sendToTarget();

					} else {
						System.out.println("Unhandled RpcCall: " + functionName + " (" + packedFunc + ").");
					}
					break;
				default:
					System.out.println("Unhandled request type: " + type.name());
				}
				long end = System.currentTimeMillis();
				if (end-start > 500) {
					System.err.println("Slow parsing (" + (end-start) + "ms)!: Request type: " + type.name() + " Class name:" + className);
				}
			}
			try {
				inStream.close();
			} catch (IOException e) {
				System.out.println("WARNING: Unable to close input stream (already closed?).");
			}
		}
	}

	public boolean isInitComplete() {
		return initComplete;
	}
}

