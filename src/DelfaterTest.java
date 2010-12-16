import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaType;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeRegistry;
import com.lekebilen.quasseldroid.qtcomm.QVariant;
import com.lekebilen.quasseldroid.qtcomm.QVariant.Type;


public class DelfaterTest {
	public static void main(String[] args) {
		//byte[] data = {-105, -125, -128, -128, -80, 90, 17, 119, -124, 73, -116, 63, 61, 29, -23, -43, -98, -16, 77, 52, 106, 27, 97, -55, -70, 43, -96, 33, -127, -2, 9, -50, 92, 0, -121, -26, -118, -110, -85, -12, 12, 32, 23, 2, 40, 104, -40, -20, -23, 35, -110, -77, 91};
		try {
			InputStream file = new FileInputStream("/tmp/t");
			byte[] datalength = new byte[4];
			System.out.println(file.read(datalength));
			byte[] balength = new byte[4];
			System.out.println(file.read(balength));
			byte[] data = new byte[(int)readUInt(balength)];
			System.out.println(file.read(data));
			/*Inflater inf = new Inflater(true);
			inf.inflate(data);*/
			
			byte[] uncompressed = new byte[(int)readUInt(data)]; 
			InflaterInputStream stream = new InflaterInputStream(new ByteArrayInputStream(data,4,data.length-4), new Inflater(false));
		
			stream.read(uncompressed);
			//System.out.println(new String(uncompressed));
			for(int i=0;i<uncompressed.length;i++){
				byte[] dcompr = new byte[1];
				dcompr[0] = uncompressed[i];
				//System.out.println("Got:"+uncompressed[i]+(new String(dcompr)));
			}
			OutputStream ofile = new FileOutputStream("/tmp/t2");
			ofile.write(uncompressed);
			ofile.close();
			System.out.println("---------");
			QDataInputStream dstream = new QDataInputStream(new ByteArrayInputStream(uncompressed));
			QVariant<ArrayList<QVariant<?>>> variantlist;
			variantlist = (QVariant<ArrayList<QVariant<?>>>)QMetaTypeRegistry.unserialize(QMetaType.Type.QVariant, dstream);
			for(QVariant<?> variant: variantlist.getData()){
				System.out.print(variant.getType().name());
				if(variant.getType()==Type.Int){
					System.out.println(":"+(Integer)variant.getData());
				}else if(variant.getType()==Type.String){
					System.out.println(":"+(String)variant.getData());
				} else{
					System.out.print('\n');
				}
					
			}
			stream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static long readUInt(byte[] buf){ 
		long ret =  ((long) (buf[0] << 24 | buf[1] << 16 | buf[2] << 8 | buf[3])) & 0xFFFFFFFFL;
		System.out.println("Read uint:"+ ret);
		return ret;
	}

}
