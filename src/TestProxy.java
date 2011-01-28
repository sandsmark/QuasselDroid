import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QDataOutputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaType;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeRegistry;
import com.lekebilen.quasseldroid.qtcomm.QVariant;


public class TestProxy {
	
	public static void main(String[] args) {
		try {
			QDataInputStream stream = new QDataInputStream(new FileInputStream("/home/sandsmark/tmp/qvariant/file.dat"));
			//List<QVariant<?>> variantlist;
			//variantlist = (List<QVariant<?>>)QMetaTypeRegistry.unserialize(QMetaType.Type.QVariantList, stream);
			//for(QVariant<?> variant: variantlist){
			//	System.out.println(variant.getType().name());
			//	if(variant.getType()==Type.Int)
			//		System.out.print((Integer)variant.getData());
			//	if(variant.getType()==Type.String)
			//		System.out.print((String)variant.getData());
			//}
			//System.out.println(stream.readUInt(32)); // wtf garbage data? should be 0

			Map<String, QVariant<?>> init;
			init = (Map<String, QVariant<?>>)QMetaTypeRegistry.unserialize(QMetaType.Type.QVariantMap, stream);

			for (String key : init.keySet()) {
				System.out.println(key + " : " + init.get(key));
			}
			stream.close();
			QDataOutputStream outstream = new QDataOutputStream(new FileOutputStream("/home/sandsmark/tmp/qvariant/file2.dat"));
			QMetaTypeRegistry.serialize(QMetaType.Type.QVariantMap, outstream, init);
			outstream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
