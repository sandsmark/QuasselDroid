import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QDataOutputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaType;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeRegistry;
import com.lekebilen.quasseldroid.qtcomm.QVariant;
import com.lekebilen.quasseldroid.qtcomm.QVariant.Type;
import com.lekebilen.quasseldroid.qtcomm.serializers.QList;


public class TestProxy {
	
	public static void main(String[] args) {
		try {
			QDataInputStream stream = new QDataInputStream(new FileInputStream("/home/freqmod/tmp/qvarianttest"));
			List<QVariant<?>> variantlist;
			variantlist = (List<QVariant<?>>)QMetaTypeRegistry.unserialize(QMetaType.Type.QVariantList, stream);
			for(QVariant<?> variant: variantlist){
				System.out.println(variant.getType().name());
				if(variant.getType()==Type.Int)
					System.out.println((Integer)variant.getData());
				if(variant.getType()==Type.String)
					System.out.println((String)variant.getData());
			}
			stream.close();
			QDataOutputStream outstream = new QDataOutputStream(new FileOutputStream("/home/freqmod/tmp/qvarianttestwrtite"));
			QMetaTypeRegistry.serialize(QMetaType.Type.QVariantList, outstream, variantlist);
			outstream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
