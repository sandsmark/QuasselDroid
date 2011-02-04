package com.lekebilen.quasseldroid;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QDataOutputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaType;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeRegistry;
import com.lekebilen.quasseldroid.qtcomm.QVariant;


public class CoreConnection extends Observable {
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
	
	public static void main(String[] args) {
		try {
			CoreConnection conn = new CoreConnection("localhost", 4242, "test", "test");
		} catch (UnknownHostException e) {
			System.err.println("Unknown host!");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			System.err.println("Security error!");
			e.printStackTrace();
		}
	}
	
	public CoreConnection(String host, int port, String username, String password)
	throws UnknownHostException, IOException, GeneralSecurityException {
			// START CREATE SOCKETS
			SocketFactory factory = (SocketFactory)SocketFactory.getDefault();
			Socket socket = (Socket)factory.createSocket(host, port);
			outStream = new QDataOutputStream(socket.getOutputStream());
			// END CREATE SOCKETS 

			
			// START CLIENT INFO
			Map<String, QVariant<?>> initial = new HashMap<String, QVariant<?>>();
			
			DateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy HH:mm:ss");
			Date date = new Date();
			initial.put("ClientDate", new QVariant<String>(dateFormat.format(date), QVariant.Type.String));
			initial.put("UseSsl", new QVariant<Boolean>(true, QVariant.Type.Bool));
			initial.put("ClientVersion", new QVariant<String>("v0.6.1 (dist-<a href='http://git.quassel-irc.org/?p=quassel.git;a=commit;h=611ebccdb6a2a4a89cf1f565bee7e72bcad13ffb'>611ebcc</a>)", QVariant.Type.String));
			initial.put("UseCompression", new QVariant<Boolean>(false, QVariant.Type.Bool));
			initial.put("MsgType", new QVariant<String>("ClientInit", QVariant.Type.String));
			initial.put("ProtocolVersion", new QVariant<Integer>(10, QVariant.Type.Int));
			
			sendQVariantMap(initial);
			// END CLIENT INFO
			
			
			// START CORE INFO
			inStream = new QDataInputStream(socket.getInputStream());
			Map<String, QVariant<?>> reply = readQVariantMap();
			System.out.println("Got answer from server: ");
			for (String key : reply.keySet()) {
				System.out.println("\t" + key + " : " + reply.get(key));
			}
			// TODO: We should check that the core is new and dandy here. 
			// END CORE INFO

			
			// START SSL CONNECTION
			SSLContext sslContext = SSLContext.getInstance("TLS");
			TrustManager[] trustManagers = new TrustManager [] { new CustomTrustManager() };
			sslContext.init(null, trustManagers, null);
			SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
			SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(socket, host, port, true);
			
			sslSocket.setEnabledProtocols(new String[] {"SSLv3"});
			for (String protocol: sslSocket.getEnabledProtocols()) {
				System.out.println(protocol);
			}

			sslSocket.setUseClientMode(true);
			sslSocket.startHandshake();
			inStream = new QDataInputStream(sslSocket.getInputStream());
			outStream = new QDataOutputStream(sslSocket.getOutputStream());
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
			System.out.println("Got answer from server: ");
			for (String key : reply.keySet()) {
				System.out.println("\t" + key + " : " + reply.get(key));
			}
			// END LOGIN ACK

			
			// START SESSION INIT
			reply = readQVariantMap();
			System.out.println("Got session init from core: ");
			for (String key : reply.keySet()) {
				System.out.println("\t" + key + " : " + reply.get(key));
			}
			
			Map<String, QVariant<?>> sessionState = (Map<String, QVariant<?>>) reply.get("SessionState").getData();
			List<QVariant<?>> bufferInfos = (List<QVariant<?>>) sessionState.get("BufferInfos").getData();
			buffers = new HashMap<Integer, Buffer>();
			for (QVariant<?> bufferInfoQV: bufferInfos) {
				BufferInfo bufferInfo = (BufferInfo)bufferInfoQV.getData();
				buffers.put(bufferInfo.id, new Buffer(bufferInfo));
			}
			// END SESSION INIT
			
			// Now the fun part starts, where we play signal proxy
			
			// START SIGNAL PROXY INIT
			List<QVariant<?>> packedFunc = new LinkedList<QVariant<?>>();
			packedFunc.add(new QVariant<Integer>(RequestType.InitRequest.getValue(), QVariant.Type.Int));
			packedFunc.add(new QVariant<String>("Network", QVariant.Type.String));
			packedFunc.add(new QVariant<String>("1", QVariant.Type.String));
			sendQVariantList(packedFunc);
			

			packedFunc = new LinkedList<QVariant<?>>();
			packedFunc.add(new QVariant<Integer>(RequestType.InitRequest.getValue(), QVariant.Type.Int));
			packedFunc.add(new QVariant<String>("BufferSyncer", QVariant.Type.String));
			packedFunc.add(new QVariant<String>("", QVariant.Type.String));
			sendQVariantList(packedFunc);
			
			packedFunc = new LinkedList<QVariant<?>>();
			packedFunc.add(new QVariant<Integer>(RequestType.Sync.getValue(), QVariant.Type.Int));
			packedFunc.add(new QVariant<String>("BufferSyncer", QVariant.Type.String));
			packedFunc.add(new QVariant<ByteBuffer>((ByteBuffer.wrap("requestLastSeenMsg".getBytes())), QVariant.Type.ByteArray));
			packedFunc.add(new QVariant<Integer>(-1, "BufferId"));
			packedFunc.add(new QVariant<Integer>(-1, "MsgId"));
			sendQVariantList(packedFunc);
			
			
			
			ReadThread readThread = new ReadThread(this);
			readThread.start();
			
			
			// Apparently the client doesn't send heartbeats?
			/*TimerTask sendPingAction = new TimerTask() {
				public void run() {
					
				}
			};*/
			
//			dump("/home/sandsmark/projects/quasseldroid/corelol.dump");
			

			// END SIGNAL PROXY
	}
	
	public Buffer [] getBuffers() {
		return (Buffer[]) buffers.values().toArray();
	}
	
	private class ReadThread extends Thread {
		boolean running = false;
		CoreConnection parent;
		
		public ReadThread(CoreConnection parent) {
			this.parent = parent;
		}
		
		public void run() {
			this.running = true;
			
			List<QVariant<?>> packedFunc;
			while (running) {
				try {
					packedFunc = readQVariantList();
				} catch (IOException e) {
					running = false;//FIXME: handle this properly?
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
					System.out.println("Init data:");
					name = new String(((ByteBuffer)packedFunc.remove(0).getData()).array());
					if (name.equals("Network")) {
						// Do nothing, for now
					} else if (name.equals("BufferSyncer")) {
						packedFunc.remove(0); // Object name, not used
						List<QVariant<?>> lastSeen = (List<QVariant<?>>) ((Map<String, QVariant<?>>)packedFunc.get(0).getData()).get("LastSeenMsg").getData();
						for (int i=0; i<lastSeen.size()/2; i++) {
							int bufferId = (Integer)lastSeen.remove(0).getData();
							int msgId = (Integer)lastSeen.remove(0).getData();
							buffers.get(bufferId).setLastSeenMessage(msgId);
						}
						List<QVariant<?>> markerLines = (List<QVariant<?>>) ((Map<String, QVariant<?>>)packedFunc.get(0).getData()).get("MarkerLines").getData();
						for (int i=0; i<lastSeen.size()/2; i++) {
							int bufferId = (Integer)lastSeen.remove(0).getData();
							int msgId = (Integer)lastSeen.remove(0).getData();
							buffers.get(bufferId).setMarkerLineMessage(msgId);
						}
						
						System.out.println(packedFunc);
					}
					break;
				case Sync:
					System.out.println("Sync request:");
//					name = new String(((ByteBuffer)packedFunc.remove(0).getData()).array());
					
//					System.out.println(name);
					break;
				case RpcCall:
					System.out.println("RPC call:");
					name = new String(((ByteBuffer)packedFunc.remove(0).getData()).array());
					System.out.println(name);
					break;
				}
			}
		}
	}

	
	private void sendQVariant(QVariant<?> data) throws IOException {
		// See how much data we're going to send
		QDataOutputStream bos = new QDataOutputStream(new ByteArrayOutputStream());
		QMetaTypeRegistry.serialize(QMetaType.Type.QVariant, bos, data);
		
		// Tell the other end how much data to expect
		outStream.writeUInt(bos.size(), 32);
		
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
		System.out.println("We're getting this many bytesies from the core: " + len);
		QVariant <Map<String, QVariant<?>>> v = (QVariant <Map<String, QVariant<?>>>)QMetaTypeRegistry.unserialize(QMetaType.Type.QVariant, inStream);

		Map<String, QVariant<?>>ret = (Map<String, QVariant<?>>)v.getData();
		
		return ret;
	}
	
	private List<QVariant<?>> readQVariantList() throws IOException {	
		long len = inStream.readUInt(32);
		System.out.println("We're getting this many bytesies from the core: " + len);
		QVariant <List<QVariant<?>>> v = (QVariant <List<QVariant<?>>>)QMetaTypeRegistry.unserialize(QMetaType.Type.QVariant, inStream);

		List<QVariant<?>>ret = (List<QVariant<?>>)v.getData();
		
		return ret;
	}
	
	private void dump(String file) throws IOException {
		long len = inStream.readUInt(32);
		QDataOutputStream outstream = new QDataOutputStream(new FileOutputStream(file));
		byte [] buffer = new byte[(int)len];
		inStream.read(buffer);
		outstream.write(buffer);
		System.exit(0);			 	
	}
	
	private static class CustomTrustManager implements javax.net.ssl.X509TrustManager {
	     /*
	      * The default X509TrustManager returned by SunX509.  We'll delegate
	      * decisions to it, and fall back to the logic in this class if the
	      * default X509TrustManager doesn't trust it.
	      */
	     X509TrustManager sunJSSEX509TrustManager;
	 
	     CustomTrustManager() throws GeneralSecurityException {
	         // create a "default" JSSE X509TrustManager.
	 
	         KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
	         //ks.load(new FileInputStream("trustedCerts"),
	         //    "passphrase".toCharArray());
	 
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
	                 sunJSSEX509TrustManager = (X509TrustManager) tms[i];
	                 return;
	             }
	         }
	 
	         /*
	          * Find some other way to initialize, or else we have to fail the
	          * constructor.
	          */
	         throw new GeneralSecurityException("Couldn't initialize");
	     }
	 
	     /*
	      * Delegate to the default trust manager.
	      */
	     public void checkClientTrusted(X509Certificate[] chain, String authType)
	                 throws CertificateException {
	         try {
	             sunJSSEX509TrustManager.checkClientTrusted(chain, authType);
	         } catch (CertificateException excep) {

	         }
	     }
	 
	     /*
	      * Delegate to the default trust manager.
	      */
	     public void checkServerTrusted(X509Certificate[] chain, String authType)
	                 throws CertificateException {
	         try {
	             sunJSSEX509TrustManager.checkServerTrusted(chain, authType);
	         } catch (CertificateException excep) {
	             for (X509Certificate cert : chain) {
	            	 System.out.println(cert.getEncoded());
	             }
	         }
	     }
	 
	     /*
	      * Merely pass this through.
	      */
	     public X509Certificate[] getAcceptedIssuers() {
	         return sunJSSEX509TrustManager.getAcceptedIssuers();
	     }
		
	}

}
