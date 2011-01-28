import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.net.SocketFactory;

import com.lekebilen.quasseldroid.qtcomm.QDataOutputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaType;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeRegistry;
import com.lekebilen.quasseldroid.qtcomm.QVariant;


public class TestClient {
	public static void main(String[] args) {
		try {
			SocketFactory factory = (SocketFactory)SocketFactory.getDefault();
			Socket socket = (Socket)factory.createSocket("localhost", 4242);
			QDataOutputStream ss = new QDataOutputStream(socket.getOutputStream());
			
			Map<String, QVariant<?>> initial = new HashMap<String, QVariant<?>>();
			
			DateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy HH:mm:ss");
			Date date = new Date();
			initial.put("ClientDate", new QVariant<String>(dateFormat.format(date)));
			initial.put("UseSsl", new QVariant<Boolean>(false));
			initial.put("ClientVersion", new QVariant<String>("v0.6.1 (dist-<a href='http://git.quassel-irc.org/?p=quassel.git;a=commit;h=611ebccdb6a2a4a89cf1f565bee7e72bcad13ffb'>611ebcc</a>)"));
			initial.put("UseCompression", new QVariant<Boolean>(false));
			initial.put("MsgType", new QVariant<String>("ClientInit"));
			initial.put("ProtocolVersion", new QVariant<Integer>(10));
			
			QDataOutputStream bos = new QDataOutputStream(new ByteArrayOutputStream());
			QVariant<Map<String, QVariant<?>>> bufstruct = new QVariant<Map<String, QVariant<?>>>(initial);
			QMetaTypeRegistry.serialize(QMetaType.Type.QVariantMap, bos, bufstruct.getData());
			ss.writeUInt(bos.size(), 32);
			QMetaTypeRegistry.serialize(QMetaType.Type.QVariant, ss, bufstruct);

			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
