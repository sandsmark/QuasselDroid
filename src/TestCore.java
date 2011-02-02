import java.io.File;
import java.io.FileInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QDataOutputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaType;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeRegistry;
import com.lekebilen.quasseldroid.qtcomm.QVariant;

public
class TestCore {
    public static void main(String[] string) {
        try {
            ServerSocketFactory serversocketfactory =
            	(ServerSocketFactory) ServerSocketFactory.getDefault();
            ServerSocket serversocket =
            	(ServerSocket) serversocketfactory.createServerSocket(4242);
            System.out.println("Waiting for connection...");
            Socket socket = (Socket) serversocket.accept();

            
            QDataInputStream is = new QDataInputStream(socket.getInputStream());
            int len = is.readInt();
            System.out.println("We are getting this many bytesis: " + len);
            
			Map<String, QVariant<?>> init;
			QVariant v = (QVariant)QMetaTypeRegistry.unserialize(QMetaType.Type.QVariant, is);

			init = (Map<String, QVariant<?>>)v.getData();
			System.out.println("Got connection: ");
			for (String key : init.keySet()) {
				System.out.println("\t" + key + " : " + init.get(key));
			}
			
            File file = new File("/home/sandsmark/projects/quasseldroid/info-core.dump");
            byte [] buf = new byte[(int) file.length()];
            FileInputStream in = new FileInputStream(file);
            in.read(buf);
			
			QDataOutputStream outstream = new QDataOutputStream(socket.getOutputStream());
			outstream.writeInt(buf.length);
			outstream.write(buf);

//			SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
//			SSLServerSocket sslserversocket = (SSLServerSocket) sslserversocketfactory.createServerSocket(socket, 4242);
//			SSLSocket sslsocket = (SSLSocket) sslserversocket.accept();
			SSLContext sslContext = SSLContext.getDefault();

			SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
			
			SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(socket, "localhost", 4242, true);
			//sslSocket.setEnabledProtocols(new String[] {"SSLv3"});
			for (String protocol: sslSocket.getEnabledProtocols()) {
				System.out.println(protocol);
			}
			sslSocket.setUseClientMode(false);
			sslSocket.startHandshake();

			is = new QDataInputStream(sslSocket.getInputStream());
            len = is.readInt();
            System.out.println("We are getting this many bytesis: " + len);
            
			v = (QVariant)QMetaTypeRegistry.unserialize(QMetaType.Type.QVariant, is);

			init = (Map<String, QVariant<?>>)v.getData();
			System.out.println("Got connection: ");
			for (String key : init.keySet()) {
				System.out.println("\t" + key + " : " + init.get(key));
			}

			
			is.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
