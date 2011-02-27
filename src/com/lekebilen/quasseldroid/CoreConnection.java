/**
 * Copyright Martin Sandsmark 2011 
 * LGPL 2.1 / GPLv3
 */

package com.lekebilen.quasseldroid;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

import android.os.Message;

import com.lekebilen.quasseldroid.qtcomm.DataStreamVersion;
import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QDataOutputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaType;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeRegistry;
import com.lekebilen.quasseldroid.qtcomm.QVariant;

public class CoreConnection {
	private static final String TAG = CoreConnection.class.getSimpleName();
	public CoreConnection(String address, int port, String username,
			String password, Boolean ssl, CoreConnService parent) {
		this.address = address;
		this.port = port;
		this.username = username;
		this.password = password;
		this.ssl = ssl;
		this.service = parent;
		this.nicks = new HashMap<Integer, String>();
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
		retFunc.add(new QVariant<Integer>(RequestType.Sync.getValue(), QVariant.Type.Int));
		retFunc.add(new QVariant<String>("BacklogManager", QVariant.Type.String));
		retFunc.add(new QVariant<String>("", QVariant.Type.String));
		retFunc.add(new QVariant<String>("requestMarkBufferAsRead", QVariant.Type.String));
		retFunc.add(new QVariant<Integer>(buffer, "BufferId"));
		
		try {
			sendQVariantList(retFunc);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Requests all buffers.
	 */
	public void requestBuffers() {
		try {
			sendInitRequest("BufferSyncer", "");
			for(int network: networks) {
				sendInitRequest("Network", Integer.toString(network));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Requests the unread backlog for a given buffer.
	 */
	public void requestBacklog(int buffer) {
		requestBacklog(buffer, buffers.get(buffer).getLastSeenMessage());
	}
	
	/**
	 * Requests moar backlog for a give buffer
	 * @param buffer Buffer id to request moar for
	 */
	public void requestMoreBacklog(int buffer) {
		requestBacklog(buffer, buffers.get(buffer).getBacklogEntry(buffers.get(buffer).getSize()).messageId);
	}
	
	/**
	 * Requests all backlog from a given message ID until the current. 
	 */
	public void requestBacklog(int buffer, int firstMsgId) {
		requestBacklog(buffer, firstMsgId, -1);
	}
	
	/**
	 * Requests backlog between two given message IDs.
	 */
	public void requestBacklog(int buffer, int firstMsgId, int lastMsgId) {
		List<QVariant<?>> retFunc = new LinkedList<QVariant<?>>();
		retFunc.add(new QVariant<Integer>(RequestType.Sync.getValue(), QVariant.Type.Int));
		retFunc.add(new QVariant<String>("BacklogManager", QVariant.Type.String));
		retFunc.add(new QVariant<String>("", QVariant.Type.String));
		retFunc.add(new QVariant<String>("requestBacklog", QVariant.Type.String));
		retFunc.add(new QVariant<Integer>(buffer, "BufferId"));
		retFunc.add(new QVariant<Integer>(firstMsgId, "MsgId"));
		retFunc.add(new QVariant<Integer>(lastMsgId, "MsgId"));
		retFunc.add(new QVariant<Integer>(Config.backlogLimit, QVariant.Type.Int));
		retFunc.add(new QVariant<Integer>(Config.backlogAdditional, QVariant.Type.Int));
		
		try {
			sendQVariantList(retFunc);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends an IRC message to a given buffer
	 * @param buffer buffer to send to
	 * @param message content of message
	 */
	public void sendMessage(int buffer, String message) {
		List<QVariant<?>> retFunc = new LinkedList<QVariant<?>>();
		retFunc.add(new QVariant<Integer>(RequestType.RpcCall.getValue(), QVariant.Type.Int));
		retFunc.add(new QVariant<String>("2sendInput(BufferInfo,QString)", QVariant.Type.String));
		retFunc.add(new QVariant<BufferInfo>(buffers.get(buffer).getInfo(), "BufferInfo"));
		retFunc.add(new QVariant<String>("/SAY " + message, QVariant.Type.String));	
		try {
			sendQVariantList(retFunc);
		} catch (IOException e) {
			e.printStackTrace();
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
			initial.put("ClientDate", new QVariant<String>(dateFormat.format(date), QVariant.Type.String));
			initial.put("UseSsl", new QVariant<Boolean>(ssl, QVariant.Type.Bool));
			initial.put("ClientVersion", new QVariant<String>("v0.6.1 (dist-<a href='http://git.quassel-irc.org/?p=quassel.git;a=commit;h=611ebccdb6a2a4a89cf1f565bee7e72bcad13ffb'>611ebcc</a>)", QVariant.Type.String));
			initial.put("UseCompression", new QVariant<Boolean>(false, QVariant.Type.Bool));
			initial.put("MsgType", new QVariant<String>("ClientInit", QVariant.Type.String));
			initial.put("ProtocolVersion", new QVariant<Integer>(10, QVariant.Type.Int));
			
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
			login.put("MsgType", new QVariant<String>("ClientLogin", QVariant.Type.String));
			login.put("User", new QVariant<String>(username, QVariant.Type.String));
			login.put("Password", new QVariant<String>(password, QVariant.Type.String));
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
			sendInitRequest("BacklogManager", "");
			
			List<QVariant<?>> packedFunc = new LinkedList<QVariant<?>>();
			packedFunc.add(new QVariant<Integer>(RequestType.Sync.getValue(), QVariant.Type.Int));
			packedFunc.add(new QVariant<String>("BufferSyncer", QVariant.Type.String));
			packedFunc.add(new QVariant<String>("", QVariant.Type.String));
			packedFunc.add(new QVariant<String>("requestSetLastSeenMsg", QVariant.Type.String));
			packedFunc.add(new QVariant<Integer>(1, "BufferId"));
			packedFunc.add(new QVariant<Integer>(1, "MsgId"));
			sendQVariantList(packedFunc);
			
			
			readThread = new ReadThread();
			readThread.start();
			
			
			TimerTask sendPingAction = new TimerTask() {
				public void run() {
					List<QVariant<?>> packedFunc = new LinkedList<QVariant<?>>();
					packedFunc.add(new QVariant<Integer>(RequestType.HeartBeat.getValue(), QVariant.Type.Int));
					packedFunc.add(new QVariant<Calendar>(Calendar.getInstance(), QVariant.Type.Time));
					try {
						sendQVariantList(packedFunc);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			heartbeatTimer = new Timer();
			heartbeatTimer.schedule(sendPingAction, 0, 30000); // Send heartbeats every 30 seconds
			
			// END SIGNAL PROXY
	}
	
	/**
	 * Attempts to disconnect from the core, as best as we can.
	 * Java sucks.
	 */
	public void disconnect() {
		heartbeatTimer.cancel(); // Has this stopped executing now? Nobody knows.
		try {
			outStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		readThread.running = false;
		//try {
			//readThread.join(); //Cant joine a thread when its run from the UI thread, makes everything hang
		//} catch (InterruptedException e) {
		//	e.printStackTrace();
		//}
		
	}

	/****************************
	 * Private internal communication stuff.
	 * Please don't look below this line.
	 * @author sandsmark
	 */

	private enum RequestType {
		Invalid(0),
	    Sync(1),
	    RpcCall(2),
	    InitRequest(3),
	    InitData(4),
	    HeartBeat(5),
	    HeartBeatReply(6);
	    
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
	private int backlogFetchAmount = 50;
	private Socket socket;

	private class ReadThread extends Thread {
		boolean running = false;

		public void run() {
			this.running = true;
			
			List<QVariant<?>> packedFunc;
			while (running) {
				try {
					packedFunc = readQVariantList();
				} catch (IOException e) {
					//TODO: not sure if this is really the best way to check if we are connected, by just waiting untill it fails, but will have to do for now
					CoreConnection.this.disconnect(); 
					Message msg = service.getHandler().obtainMessage(R.id.CORECONNECTION_LOST_CONNECTION);
					msg.sendToTarget();
					
					System.err.println("IO error!");	
					e.printStackTrace();
					return;
				}
				RequestType type = RequestType.getForVal((Integer)packedFunc.remove(0).getData());
				String name;
				switch (type) {
				case HeartBeat:
					System.out.println("Got heartbeat");
					break;
				case InitData:
					name = new String(((ByteBuffer)packedFunc.remove(0).getData()).array());
					if (name.equals("Network")) {
						int networkId = Integer.parseInt((String) packedFunc.remove(0).getData()); // Object name, not used
						Map<String, QVariant<?>> initMap = (Map<String, QVariant<?>>) packedFunc.remove(0).getData();
						Map<String, QVariant<?>> usersAndChans = (Map<String, QVariant<?>>) initMap.get("IrcUsersAndChannels").getData();
						Map<String, QVariant<?>> channels = (Map<String, QVariant<?>>) usersAndChans.get("channels").getData();
						for (QVariant<?> channel:  channels.values()) {
							Map<String, QVariant<?>> chan = (Map<String, QVariant<?>>) channel.getData();
							String chanName = (String)chan.get("name").getData();
							Map<String, QVariant<?>> userModes = (Map<String, QVariant<?>>) chan.get("UserModes").getData();
							List<String> users = new ArrayList<String>(userModes.keySet());
							for (Buffer buffer: buffers.values()) {
								if (buffer.getInfo().name.equals(chanName) && buffer.getInfo().networkId == networkId) {
									buffer.setNicks(users);
								}
							}
							nicks.put(networkId, (String) initMap.get("myNick").getData());
						}
					} else if (name.equals("BufferSyncer")) {
						packedFunc.remove(0); // Object name, not used
						List<QVariant<?>> lastSeen = (List<QVariant<?>>) ((Map<String, QVariant<?>>)packedFunc.get(0).getData()).get("LastSeenMsg").getData();
						for (int i=0; i<lastSeen.size()/2; i++) {
							int bufferId = (Integer)lastSeen.remove(0).getData();
							int msgId = (Integer)lastSeen.remove(0).getData();
							if (buffers.containsKey(bufferId)){ // We only care for buffers we have open
								
								Message msg = service.getHandler().obtainMessage(R.id.CORECONNECTION_SET_LAST_SEEN_TO_SERVICE);
								msg.obj = buffers.get(bufferId);
								msg.arg1 = msgId;
								msg.sendToTarget();
							}
						}
						List<QVariant<?>> markerLines = (List<QVariant<?>>) ((Map<String, QVariant<?>>)packedFunc.get(0).getData()).get("MarkerLines").getData();
						for (int i=0; i<markerLines.size()/2; i++) {
							int bufferId = (Integer)markerLines.remove(0).getData();
							int msgId = (Integer)markerLines.remove(0).getData();
							if (buffers.containsKey(bufferId)){
								Message msg = service.getHandler().obtainMessage(R.id.CORECONNECTION_SET_MARKERLINE_TO_SERVICE);
								msg.obj = buffers.get(bufferId);
								msg.arg1 = msgId;
								msg.sendToTarget();
							}
							
							
						}
						// We don't fetch backlog automatically
						for (int buffer: buffers.keySet()) {
							//System.out.println("Bufferlol: " + buffer);
							Message msg = service.getHandler().obtainMessage(R.id.CORECONNECTION_NEW_BUFFER_TO_SERVICE);
							msg.obj = buffers.get(buffer);// We have now received everything we need to know about this buffer
							msg.sendToTarget();
//							requestBacklog(buffer, buffers.get(buffer).getLastSeenMessage());
						}
//						sendMessage(MSG_NEW_BUFFER, buffers.get(buffers)); 
						
					} else if (name.equals("IrcUser")) {
						IrcUser user = new IrcUser();
						user.name = (String) packedFunc.remove(0).getData();
						Map<String, QVariant<?>> map = (Map<String, QVariant<?>>) packedFunc.remove(0).getData();
						user.away = (Boolean) map.get("away").getData();
						user.awayMessage = (String) map.get("awayMessage").getData();
						user.ircOperator = (String) map.get("ircOperator").getData();
						user.nick = (String) map.get("nick").getData();
						user.channels = (List<String>) map.get("channels").getData();
						service.newUser(user);
					} else {
						System.out.println("InitData: " + name);
					}
					break;
				case Sync:
					String className = packedFunc.remove(0).toString();
					packedFunc.remove(0); // object name, we don't really care
					String function = packedFunc.remove(0).toString();
					
					if (className.equals("BacklogManager") && function.equals("receiveBacklog")) {
						int buffer = (Integer) packedFunc.remove(0).getData();
						packedFunc.remove(0); // first
						packedFunc.remove(0); // last
						packedFunc.remove(0); // limit
						packedFunc.remove(0); // additional
						List<QVariant<?>> data = (List<QVariant<?>>)(packedFunc.remove(0).getData());
						Collections.reverse(data);
						for (QVariant<?> message: data) {
							//buffers.get(buffer).addBacklog((IrcMessage) message.getData());
							//service.newMessage((IrcMessage) message.getData());
							Message msg = service.getHandler().obtainMessage(R.id.CORECONNECTION_NEW_MESSAGE_TO_SERVICE);
							msg.obj = (IrcMessage) message.getData();
							msg.sendToTarget();
						}						
					} else if (className.equals("Network") && function.equals("addIrcUser")) {
						String nick = (String) packedFunc.remove(0).getData();
						try {
							sendInitRequest("IrcUser", "1/" + nick);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (className.equals("BufferSyncer") && function.equals("markBufferAsRead")) {
						int buffer = (Integer) packedFunc.remove(0).getData();
						buffers.get(buffer).setRead();
					} else {
						System.out.println("Sync request: " + className + "::" + function);
					}

					break;
				case RpcCall:
					String functionName = packedFunc.remove(0).toString();
//					int buffer = functionName.charAt(0);
//					functionName = functionName.substring(1);
					if (functionName.equals("2displayMsg(Message)")) {
						IrcMessage message = (IrcMessage) packedFunc.remove(0).getData();
//						buffers.get(message.bufferInfo.id).addBacklog(message);
						Message msg = service.getHandler().obtainMessage(R.id.CORECONNECTION_NEW_MESSAGE_TO_SERVICE);
						msg.obj = message;
						msg.sendToTarget();
						
					} else {
						System.out.println("RpcCall: " + functionName + " (" + packedFunc + ").");
					}
					break;
				default:
					System.out.println(type);
				}
			}
			try {
				inStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	
	private void sendQVariant(QVariant<?> data) throws IOException {
		// See how much data we're going to send
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		QDataOutputStream bos = new QDataOutputStream(baos);
		QMetaTypeRegistry.serialize(QMetaType.Type.QVariant, bos, data);
		
		// Tell the other end how much data to expect
		outStream.writeUInt(bos.size(), 32);
		
		// Sanity check, check that we can decode our own stuff before sending it off
		QDataInputStream bis = new QDataInputStream(new ByteArrayInputStream(baos.toByteArray()));
		QMetaTypeRegistry.instance().getTypeForId(QMetaType.Type.QVariant.getValue()).getSerializer().unserialize(bis, DataStreamVersion.Qt_4_2);
		
		// Send data 
		QMetaTypeRegistry.serialize(QMetaType.Type.QVariant, outStream, data);
	}
	
	private void sendQVariantMap(Map<String, QVariant<?>> data) throws IOException {
		QVariant<Map<String, QVariant<?>>> bufstruct = new QVariant<Map<String, QVariant<?>>>(data, QVariant.Type.Map);
		sendQVariant(bufstruct);
	}
	
	private void sendQVariantList(List<QVariant<?>> data) throws IOException {
		QVariant<List<QVariant<?>>> bufstruct = new QVariant<List<QVariant<?>>>(data, QVariant.Type.List);
		sendQVariant(bufstruct);
	}
	
	private Map<String, QVariant<?>> readQVariantMap() throws IOException {
		long len = inStream.readUInt(32);
		QVariant <Map<String, QVariant<?>>> v = (QVariant <Map<String, QVariant<?>>>)QMetaTypeRegistry.unserialize(QMetaType.Type.QVariant, inStream);

		Map<String, QVariant<?>>ret = (Map<String, QVariant<?>>)v.getData();
		
		return ret;
	}
	
	private List<QVariant<?>> readQVariantList() throws IOException {	
		long len = inStream.readUInt(32);
		QVariant <List<QVariant<?>>> v = (QVariant <List<QVariant<?>>>)QMetaTypeRegistry.unserialize(QMetaType.Type.QVariant, inStream);

		List<QVariant<?>>ret = (List<QVariant<?>>)v.getData();
		
		return ret;
	}
	
	private void sendInitRequest(String className, String objectName) throws IOException {
		List<QVariant<?>> packedFunc = new LinkedList<QVariant<?>>();
		packedFunc.add(new QVariant<Integer>(RequestType.InitRequest.getValue(), QVariant.Type.Int));
		packedFunc.add(new QVariant<String>(className, QVariant.Type.String));
		packedFunc.add(new QVariant<String>(objectName, QVariant.Type.String));
		sendQVariantList(packedFunc);
	}
	
	public boolean isConnected() {
		return (socket != null && socket.isConnected() && readThread != null && readThread.isAlive());
	}
	
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

	         throw new GeneralSecurityException("Couldn't initialize certificate management");
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
	        	 String hashedCert = hash(chain[0].getEncoded());
	        	 //TODO: Had to comment out this because we no longer have a shared preferences here, fix somehow?
//	        	 if (CoreConnection.this.settings.contains("certificate")) {
//	        		 if (!CoreConnection.this.settings.getString("certificate", "lol").equals(hashedCert)) {
//	        			 throw new CertificateException();
//	        		 }
//	        	 } else {
//	        		 System.out.println("Storing new certificate: " + hashedCert);
//	        		 CoreConnection.this.settings.edit().putString("certificate", hashedCert).commit();
//	        	 }
	         }
	     }
	     
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
}

