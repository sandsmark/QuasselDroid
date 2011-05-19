/**
 * Copyright Martin Sandsmark 2011 
 * LGPL 2.1 / GPLv3
 */

package com.lekebilen.quasseldroid.io;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.lekebilen.quasseldroid.Buffer;
import com.lekebilen.quasseldroid.BufferInfo;
import com.lekebilen.quasseldroid.IrcMessage;
import com.lekebilen.quasseldroid.IrcUser;
import com.lekebilen.quasseldroid.R;
import com.lekebilen.quasseldroid.R.id;
import com.lekebilen.quasseldroid.R.string;
import com.lekebilen.quasseldroid.qtcomm.DataStreamVersion;
import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QDataOutputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaType;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeRegistry;
import com.lekebilen.quasseldroid.qtcomm.QVariant;
import com.lekebilen.quasseldroid.qtcomm.QVariantType;
import com.lekebilen.quasseldroid.service.CoreConnService;

public class CoreConnection {
	@SuppressWarnings("unused")

	private static final String TAG = CoreConnection.class.getSimpleName();

	private Socket socket;
	private QDataOutputStream outStream;
	private QDataInputStream inStream;

	private Map<Integer, Buffer> buffers;
	private Map<Integer, String> nicks;
	private List<Integer> networks;

	private String address;
	private int port;
	private String username;
	private String password;
	private boolean ssl;

	private CoreConnService service;
	private Timer heartbeatTimer;
	private ReadThread readThread;

	private boolean connected;


	public CoreConnection(String address, int port, String username,
			String password, Boolean ssl, CoreConnService parent) {
		this.address = address;
		this.port = port;
		this.username = username;
		this.password = password;
		this.ssl = ssl;
		this.service = parent;

		this.nicks = new HashMap<Integer, String>();

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
			// No fucking clue wth the following is suppose to achieve so just commenting it out for now.
			//			try {
			//				connect();
			//				return true;
			//			} catch (Exception e) {
			//				Message msg = service.getHandler().obtainMessage(R.id.CORECONNECTION_DISCONNECTED);
			//
			//				// Do not crash on start up if we don't have buffer (will output invalid username/password combination)
			//				if(buffers != null) {
			//					msg.obj = buffers.values();
			//					msg.sendToTarget();
			//				}

			return false;
			//}
		}
	}

	/**
	 * Gets the users own nick on a given network.
	 * @param networkId the network to get the nick for
	 * @return the nick of the user
	 */
	public String getNick(int networkId) {
		return nicks.get(networkId);
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
		retFunc.add(new QVariant<String>("requestMarkBufferAsRead", QVariantType.String));
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
		retFunc.add(new QVariant<String>("requestSetLastSeenMsg", QVariantType.String));
		retFunc.add(new QVariant<Integer>(buffer, "BufferId"));
		retFunc.add(new QVariant<Integer>(msgid, "BufferId"));

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
		retFunc.add(new QVariant<String>("requestSetMarkerLine", QVariantType.String));
		retFunc.add(new QVariant<Integer>(buffer, "BufferId"));
		retFunc.add(new QVariant<Integer>(msgid, "BufferId"));

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
		if (buffers.get(buffer).getSize()==0) {
			requestBacklog(buffer, -1, -1, amount);			
		}else {
			requestBacklog(buffer, -1, buffers.get(buffer).getBacklogEntry(0).messageId, amount);			
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
	 */
	public void connect() throws UnknownHostException, IOException, GeneralSecurityException {	
		// START CREATE SOCKETS
		SocketFactory factory = (SocketFactory)SocketFactory.getDefault();
		socket = (Socket)factory.createSocket(address, port);
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
		for (String key : reply.keySet()) {
			System.out.println("\t" + key + " : " + reply.get(key));
		}
		// TODO: We should check that the core is new and dandy here. 
		// END CORE INFO

		// START SSL CONNECTION
		if (ssl) {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			TrustManager[] trustManagers = new TrustManager [] { new CustomTrustManager() };
			sslContext.init(null, trustManagers, null);
			SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
			SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(socket, address, port, true);
			sslSocket.setEnabledProtocols(new String[] {"SSLv3"});

			sslSocket.setUseClientMode(true);
			sslSocket.startHandshake();
			inStream = new QDataInputStream(sslSocket.getInputStream());
			outStream = new QDataOutputStream(sslSocket.getOutputStream());
			socket = sslSocket;
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
		System.out.println("SESSION INIT: ");
		for (String key : reply.keySet()) {
			System.out.println("\t" + key + " : " + reply.get(key));
		}

		Map<String, QVariant<?>> sessionState = (Map<String, QVariant<?>>) reply.get("SessionState").getData();
		List<QVariant<?>> bufferInfos = (List<QVariant<?>>) sessionState.get("BufferInfos").getData();
		buffers = new HashMap<Integer, Buffer>(bufferInfos.size());
		for (QVariant<?> bufferInfoQV: bufferInfos) {
			BufferInfo bufferInfo = (BufferInfo)bufferInfoQV.getData();
			buffers.put(bufferInfo.id, new Buffer(bufferInfo));
		}
		List<QVariant<?>> networkIds = (List<QVariant<?>>) sessionState.get("NetworkIds").getData();
		networks = new ArrayList<Integer>(networkIds.size());
		for (QVariant<?> networkId: networkIds) {
			networks.add((Integer) networkId.getData());
		}
		// END SESSION INIT

		// Now the fun part starts, where we play signal proxy

		// START SIGNAL PROXY INIT
	
		// We must do this here, to get network names early enough
		for(int network: networks) {
			sendInitRequest("Network", Integer.toString(network));
		}
		sendInitRequest("BufferSyncer", "");
		sendInitRequest("BufferViewConfig", "0");

		for (Buffer buffer:buffers.values()) {
			int backlogAmout = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(service).getString(service.getString(R.string.preference_initial_backlog_limit), "1"));
			requestMoreBacklog(buffer.getInfo().id, backlogAmout);
		}

		//Send buffers to CoreConService
		Message msg = service.getHandler().obtainMessage(R.id.CORECONNECTION_ADD_MULTIPLE_BUFFERS);
		msg.obj = buffers.values();
		msg.sendToTarget();




		TimerTask sendPingAction = new TimerTask() {
			public void run() {
				List<QVariant<?>> packedFunc = new LinkedList<QVariant<?>>();
				packedFunc.add(new QVariant<Integer>(RequestType.HeartBeat.getValue(), QVariantType.Int));
				packedFunc.add(new QVariant<Calendar>(Calendar.getInstance(), QVariantType.Time));
				try {
					sendQVariantList(packedFunc);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		heartbeatTimer = new Timer();
		heartbeatTimer.schedule(sendPingAction, 30000, 30000); // Send heartbeats every 30 seconds

		// END SIGNAL PROXY
		Log.i(TAG, "Connected!");
		connected = true;

		msg = service.getHandler().obtainMessage(R.id.CORECONNECTION_CONNECTED);
		msg.sendToTarget();
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
	 */
	private Map<String, QVariant<?>> readQVariantMap() throws IOException {
		// Length of this packet (why do they send this? noone knows!).
		inStream.readUInt(32);
		QVariant <Map<String, QVariant<?>>> v = (QVariant <Map<String, QVariant<?>>>)QMetaTypeRegistry.unserialize(QMetaType.Type.QVariant, inStream);

		Map<String, QVariant<?>>ret = (Map<String, QVariant<?>>)v.getData();

		return ret;
	}

	/**
	 * A convenience function to read a QVariantList.
	 */	
	private List<QVariant<?>> readQVariantList() throws IOException {	
		inStream.readUInt(32); // Length
		QVariant <List<QVariant<?>>> v = (QVariant <List<QVariant<?>>>)QMetaTypeRegistry.unserialize(QMetaType.Type.QVariant, inStream);

		List<QVariant<?>>ret = (List<QVariant<?>>)v.getData();

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

	/**
	 * A custom trust manager for the SSL socket so we can
	 * let the user manually verify certificates.
	 * @author sandsmark
	 */
	private class CustomTrustManager implements javax.net.ssl.X509TrustManager {
		/*
		 * The default X509TrustManager returned by SunX509.  We'll delegate
		 * decisions to it, and fall back to the logic in this class if the
		 * default X509TrustManager doesn't trust it.
		 */
		X509TrustManager defaultTrustManager;

		CustomTrustManager() throws GeneralSecurityException {
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			tmf.init(ks);

			TrustManager tms [] = tmf.getTrustManagers();

			/*
			 * Iterate over the returned trustmanagers, look
			 * for an instance of X509TrustManager.  If found,
			 * use that as our "default" trust manager.
			 */
			for (int i = 0; i < tms.length; i++) {
				if (tms[i] instanceof X509TrustManager) {
					defaultTrustManager = (X509TrustManager) tms[i];
					return;
				}
			}

			throw new GeneralSecurityException("Couldn't initialize certificate management!");
		}

		/*
		 * Delegate to the default trust manager.
		 */
		public void checkClientTrusted(X509Certificate[] chain, String authType)
		throws CertificateException {
			try {
				defaultTrustManager.checkClientTrusted(chain, authType);
			} catch (CertificateException excep) {

			}
		}

		/*
		 * Delegate to the default trust manager.
		 */
		public void checkServerTrusted(X509Certificate[] chain, String authType)
		throws CertificateException {
			try {
				defaultTrustManager.checkServerTrusted(chain, authType);
			} catch (CertificateException excep) {
				/* Here we either check the certificate against the last stored one,
				 * or throw a security exception to let the user know that something is wrong.
				 */
				String hashedCert = hash(chain[0].getEncoded());
				SharedPreferences preferences = CoreConnection.this.service.getSharedPreferences("CertificateStorage", Context.MODE_PRIVATE);
				if (preferences.contains("certificate")) { 
					if (!preferences.getString("certificate", "lol").equals(hashedCert)) {
						throw new CertificateException();
					}
					// We haven't seen a certificate from this core before, just store it
					// TODO: let the user decide whether to trust it or not.
				} else {
					System.out.println("Storing new certificate: " + hashedCert);
					preferences.edit().putString("certificate", hashedCert).commit();
				}
			}
		}

		/**
		 * Java sucks.
		 * @param s The bytes to hash
		 * @return a hash representing the input bytes.
		 */
		private String hash(byte[] s) {
			try {
				MessageDigest digest = java.security.MessageDigest.getInstance("SHA1");
				digest.update(s);
				byte messageDigest[] = digest.digest();
				StringBuffer hexString = new StringBuffer();
				for (int i=0; i<messageDigest.length; i++)
					hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
				return hexString.toString();	    	        
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			return "";
		}


		/*
		 * Merely pass this through.
		 */
		public X509Certificate[] getAcceptedIssuers() {
			return defaultTrustManager.getAcceptedIssuers();
		}
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
				Message msg = service.getHandler().obtainMessage(R.id.CORECONNECTION_LOST_CONNECTION);
				msg.sendToTarget();
			}
		};

		public void run() {
			this.running = true;

			try {
				connect();
				// ↓↓↓↓ FIXME TODO HANDLE THESE YOU DICKWEEDS! ↓↓↓↓
			} catch (UnknownHostException e) {
				service.getHandler().obtainMessage(R.id.CORECONNECTION_LOST_CONNECTION, "Unknown host!").sendToTarget();
				//Toast.makeText(getApplicationContext(), "Unknown host!", Toast.LENGTH_LONG).show();
			} catch (IOException e) {
				service.getHandler().obtainMessage(R.id.CORECONNECTION_LOST_CONNECTION, "IO error while connecting!").sendToTarget();
				//Toast.makeText(getApplicationContext(), "IO error while connecting!", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			} catch (GeneralSecurityException e) {
				service.getHandler().obtainMessage(R.id.CORECONNECTION_LOST_CONNECTION, "Invalid username/password combination.").sendToTarget();
				//Toast.makeText(getApplicationContext(), "Invalid username/password combination.", Toast.LENGTH_LONG).show();
			}

			List<QVariant<?>> packedFunc;
			while (running) {
				try {
					//long startWait = System.currentTimeMillis();
					packedFunc = readQVariantList();
					//Log.i(TAG, "Slow core is slow: " + (System.currentTimeMillis() - startWait) + "ms");
				} catch (IOException e) {
					//TODO: not sure if this is really the best way to check if we are connected, by just waiting until it fails, but will have to do for now
					CoreConnection.this.disconnect(); 
					Message msg = service.getHandler().obtainMessage(R.id.CORECONNECTION_LOST_CONNECTION);
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

						Map<String, QVariant<?>> initMap = (Map<String, QVariant<?>>) packedFunc.remove(0).getData();
						// Store the network name and associated nick for "our" user
						nicks.put(networkId, (String) initMap.get("myNick").getData());

						for (Buffer buffer: buffers.values()) {
							if (buffer.getInfo().networkId == networkId && buffer.getInfo().name.equals("")) {
								buffer.setName((String) initMap.get("networkName").getData());
								break;
							}
						}

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

							for (Buffer buffer: buffers.values()) {
								if (buffer.getInfo().name.equals(chanName) && buffer.getInfo().networkId == networkId) {
									buffer.setTopic(topic);
									buffer.setNicks(users);
									break;
								}
							}
							
							Message msg = service.getHandler().obtainMessage(R.id.CORECONNECTION_NEW_USERLIST_ADDED);
							msg.obj = ircUsers;
							msg.sendToTarget();

						}

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




						/*
						 * An object that is used to synchronize metadata about buffers,
						 * like the last seen message, marker lines, etc.
						 */
					} else if (className.equals("BufferSyncer")) {
						// Parse out the last seen messages
						List<QVariant<?>> lastSeen = (List<QVariant<?>>) ((Map<String, QVariant<?>>)packedFunc.get(0).getData()).get("LastSeenMsg").getData();
						for (int i=0; i<lastSeen.size()/2; i++) {
							int bufferId = (Integer)lastSeen.remove(0).getData();
							int msgId = (Integer)lastSeen.remove(0).getData();
							if (buffers.containsKey(bufferId)){ // We only care for buffers we have open

								Message msg = service.getHandler().obtainMessage(R.id.CORECONNECTION_SET_LAST_SEEN_TO_SERVICE);
								msg.arg1 = bufferId;
								msg.arg2 = msgId;
								msg.sendToTarget();
							}
						}
						// Parse out the marker lines for buffers
						List<QVariant<?>> markerLines = (List<QVariant<?>>) ((Map<String, QVariant<?>>)packedFunc.get(0).getData()).get("MarkerLines").getData();
						for (int i=0; i<markerLines.size()/2; i++) {
							int bufferId = (Integer)markerLines.remove(0).getData();
							int msgId = (Integer)markerLines.remove(0).getData();
							if (buffers.containsKey(bufferId)){
								Message msg = service.getHandler().obtainMessage(R.id.CORECONNECTION_SET_MARKERLINE_TO_SERVICE);
								msg.arg1 = bufferId;
								msg.arg2 = msgId;
								msg.sendToTarget();
							}
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

						Message msg = service.getHandler().obtainMessage(R.id.CORECONNECTION_NEW_USER_ADDED);
						msg.obj = (IrcUser) user;
						msg.sendToTarget();
					} else if (className.equals("BufferViewConfig")) {
						Map<String, QVariant<?>> map = (Map<String, QVariant<?>>) packedFunc.remove(0).getData();
						List<QVariant<?>> tempList = (List<QVariant<?>>) map.get("TemporarilyRemovedBuffers").getData();
						List<QVariant<?>> permList = (List<QVariant<?>>) map.get("RemovedBuffers").getData();
						List<QVariant<?>> orderList = (List<QVariant<?>>) map.get("BufferList").getData();
						int networkId = (Integer) map.get("networkId").getData(); // let's hope we don't need this, LAWLERZ!!1
						boolean autoSort = (Boolean) map.get("sortAlphabetically").getData();
						
						for (QVariant bufferId: tempList) {
							buffers.get(bufferId.getData()).setTemporarilyHidden(true);
						}
						
						for (QVariant bufferId: permList) {
							buffers.get(bufferId.getData()).setPermanentlyHidden(true);
						}
						
						int order = 0;
						for (QVariant bufferId: orderList) {
							if (!buffers.containsKey(bufferId.getData())) {
								System.err.println("got buffer info for non-existant buffer id: " + bufferId.getData());
								continue;
							}
							buffers.get(bufferId.getData()).setAutoSort(autoSort);
							//buffers.get(bufferId.getData()).setOrder(order);
							Message msg = service.getHandler().obtainMessage(R.id.CORECONNECTION_SET_BUFFER_ORDER);
							msg.arg1 = (Integer) bufferId.getData();
							msg.arg2 = order;
							msg.sendToTarget();
							
							order++;
						}
						
						
						
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
					if (foo.getClass().getName().equals("java.nio.ReadWriteHeapByteBuffer")) {
						try {
							System.out.println("faen i helvete: " + new String(((ByteBuffer)foo).array(), "UTF-8"));
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}						
					}
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
							Message msg = service.getHandler().obtainMessage(R.id.CORECONNECTION_NEW_BACKLOGITEM_TO_SERVICE);
							msg.obj = (IrcMessage) message.getData();
							msg.sendToTarget();
						}
						/* 
						 * The addIrcUser function in the Network class is called whenever a new
						 * IRC user appears on a given network. 
						 */
					} else if (className.equals("Network") && function.equals("addIrcUser")) {
						String nick = (String) packedFunc.remove(0).getData();
						try {
							sendInitRequest("IrcUser", "1/" + nick);
						} catch (IOException e) {
							e.printStackTrace();
							running = false; // We have obviously lost our connection, just stop this thread.

						}
					} else if (className.equals("BufferSyncer") && function.equals("setLastSeenMsg")) {
						int bufferId = (Integer) packedFunc.remove(0).getData();
						int msgId = (Integer) packedFunc.remove(0).getData();
						Message msg = service.getHandler().obtainMessage(R.id.CORECONNECTION_SET_LAST_SEEN_TO_SERVICE);
						msg.arg1 = bufferId;
						msg.arg2 = msgId;
						msg.sendToTarget();
						
					} else if (className.equals("BufferSyncer") && function.equals("setMarkerLine")) {
						int bufferId = (Integer) packedFunc.remove(0).getData();
						int msgId = (Integer) packedFunc.remove(0).getData();
						Message msg = service.getHandler().obtainMessage(R.id.CORECONNECTION_SET_MARKERLINE_TO_SERVICE);
						msg.arg1 = bufferId;
						msg.arg2 = msgId;
						msg.sendToTarget();			
						
						/*
						 * markBufferAsRead is called whenever a given buffer is set as read by the core. 
						 */
					} else if (className.equals("BufferSyncer") && function.equals("markBufferAsRead")) {
						//TODO: this basicly does shit. So find out if it effects anything and what it should do
						int buffer = (Integer) packedFunc.remove(0).getData();
						buffers.get(buffer).setRead();
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
						Message msg = service.getHandler().obtainMessage(R.id.CORECONNECTION_NEW_MESSAGE_TO_SERVICE);
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
}

