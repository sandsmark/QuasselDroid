package com.lekebilen.quasseldroid.qtcomm.serializers;

import java.io.IOException;

import com.lekebilen.quasseldroid.qtcomm.DataStreamVersion;
import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QDataOutputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeSerializer;

public class Void implements QMetaTypeSerializer<java.lang.Void> {

	@Override
	public void serialize(QDataOutputStream stream, java.lang.Void data,
			DataStreamVersion version) throws IOException {
		stream.write(0);
		
	}

	@Override
	public java.lang.Void unserialize(QDataInputStream stream,
			DataStreamVersion version) throws IOException {
		System.out.println("Unserialized a void.");
		return null;
	}
	

}
