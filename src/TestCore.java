import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import javax.net.ServerSocketFactory;

import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaType;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeRegistry;
import com.lekebilen.quasseldroid.qtcomm.QVariant;

public
class TestCore {
    public static void main(String[] string) {
        try {
            /*SSLServerSocketFactory sslserversocketfactory =
                    (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket sslserversocket =
                    (SSLServerSocket) sslserversocketfactory.createServerSocket(4242);
            SSLSocket sslsocket = (SSLSocket) sslserversocket.accept();
            QDataInputStream is = new QDataInputStream(sslsocket.getInputStream());*/

            ServerSocketFactory serversocketfactory =
            	(ServerSocketFactory) ServerSocketFactory.getDefault();
            ServerSocket serversocket =
            	(ServerSocket) serversocketfactory.createServerSocket(4242);
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
			
			Map<String, QVariant<?>> reply;
			
			
			is.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
