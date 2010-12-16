package com.lekebilen.quasseldroid.qtcomm.serializers;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.lekebilen.quasseldroid.qtcomm.DataStreamVersion;
import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QDataOutputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeSerializer;


public class QByteArray implements QMetaTypeSerializer<ByteBuffer>{

	@Override
	public ByteBuffer unserialize(QDataInputStream stream, DataStreamVersion version)
	throws IOException {
		int len = (int)stream.readUInt();
		if(len == 0xFFFFFFFF)
			return null;
		byte data[] = new byte[len];
		stream.readFully(data);
		return ByteBuffer.wrap(data);
	}

	@Override
	public void serialize(QDataOutputStream stream, ByteBuffer data,
			DataStreamVersion version) throws IOException {
		//OOPS: Requires a byte buffer with array for writing
		//FIXME: ^ Make it work without hasArray()
		if(data==null){
			stream.writeUInt(0xFFFFFFFF);
		}else{
			stream.writeUInt(data.array().length);
			stream.write(data.array());
		}


	}
}
