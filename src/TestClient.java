import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.net.SocketFactory;

import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QDataOutputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaType;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeRegistry;
import com.lekebilen.quasseldroid.qtcomm.QVariant;
import com.lekebilen.quasseldroid.qtcomm.serializers.QMap;


public class TestClient {
	public static void main(String[] args) {
		try {
			SocketFactory factory = (SocketFactory)SocketFactory.getDefault();
			Socket socket = (Socket)factory.createSocket("mts.ms", 4242);
			QDataOutputStream ss = new QDataOutputStream(socket.getOutputStream());
			
			Map<String, QVariant<?>> initial = new HashMap<String, QVariant<?>>();
			
			DateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy HH:mm:ss");
			Date date = new Date();
			initial.put("ClientDate", new QVariant<String>(dateFormat.format(date), QVariant.Type.String));
			initial.put("UseSsl", new QVariant<Boolean>(true, QVariant.Type.Bool));
			initial.put("ClientVersion", new QVariant<String>("v0.6.1 (dist-<a href='http://git.quassel-irc.org/?p=quassel.git;a=commit;h=611ebccdb6a2a4a89cf1f565bee7e72bcad13ffb'>611ebcc</a>)", QVariant.Type.String));
			initial.put("UseCompression", new QVariant<Boolean>(false, QVariant.Type.Bool));
			initial.put("MsgType", new QVariant<String>("ClientInit", QVariant.Type.String));
			initial.put("ProtocolVersion", new QVariant<Integer>(10, QVariant.Type.Int));
			
			QDataOutputStream bos = new QDataOutputStream(new ByteArrayOutputStream());
			QVariant<Map<String, QVariant<?>>> bufstruct = new QVariant<Map<String, QVariant<?>>>(initial, QVariant.Type.Map);
			QMetaTypeRegistry.serialize(QMetaType.Type.QVariant, bos, bufstruct);
			
			// Tell the other end how much data to expect
			ss.writeUInt(bos.size(), 32);
			// Send data 
			QMetaTypeRegistry.serialize(QMetaType.Type.QVariant, ss, bufstruct);
			
			// Time to read from the core
			QDataInputStream is = new QDataInputStream(socket.getInputStream());
			//QDataInputStream is = new QDataInputStream(new FileInputStream("/home/sandsmark/tmp/qvariant/file2.dat"));
			int len = is.readInt();
			System.out.println("We're getting this many bytesies from the core: " + len);
            
/*			QDataOutputStream outstream = new QDataOutputStream(new FileOutputStream("c:\\users\\sandsmark\\kek"));
			byte [] buffer = new byte[len];
			is.read(buffer);
			outstream.write(buffer);
			return;*/
				
			Map<String, QVariant<?>> init;
			QVariant <Map<String, QVariant<?>>> v = (QVariant <Map<String, QVariant<?>>>)QMetaTypeRegistry.unserialize(QMetaType.Type.QVariant, is);

			init = (Map<String, QVariant<?>>)v.getData();
			System.out.println("Got answer from server: ");
			for (String key : init.keySet()) {
				System.out.println("\t" + key + " : " + init.get(key));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
