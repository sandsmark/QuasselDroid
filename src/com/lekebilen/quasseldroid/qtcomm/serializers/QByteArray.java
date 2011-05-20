package com.lekebilen.quasseldroid.qtcomm.serializers;

import java.io.IOException;

import com.lekebilen.quasseldroid.qtcomm.DataStreamVersion;
import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QDataOutputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeSerializer;
import com.lekebilen.quasseldroid.util.StringReaderUtil;

public class QByteArray implements QMetaTypeSerializer<String> {
	
	StringReaderUtil stringReader = new StringReaderUtil("UTF-8");
	
	@Override
	public String unserialize(QDataInputStream stream, DataStreamVersion version)
	throws IOException {
		int len = (int)stream.readUInt(32);
		if(len == 0xFFFFFFFF)
			return "";
			
		return stringReader.readString(stream, len);
	}

	@Override
	public void serialize(QDataOutputStream stream, String data,
			DataStreamVersion version) throws IOException {
		//OOPS: Requires a byte buffer with array for writing
		//FIXME: ^ Make it work without hasArray()
		if(data==null){
			stream.writeUInt(0xFFFFFFFF, 32);
		}else{
			byte [] wbuf = data.getBytes("UTF-8");
			stream.writeUInt(wbuf.length, 32);
			stream.write(wbuf);
		}
	}
}
