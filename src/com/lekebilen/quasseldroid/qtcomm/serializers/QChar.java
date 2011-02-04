package com.lekebilen.quasseldroid.qtcomm.serializers;

import java.io.IOException;

import com.lekebilen.quasseldroid.qtcomm.DataStreamVersion;
import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QDataOutputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeSerializer;

public class QChar implements QMetaTypeSerializer<Character> {

	@Override
	public void serialize(QDataOutputStream stream, Character data,
			DataStreamVersion version) throws IOException {
		stream.writeUInt(data.charValue(), 16);
		
	}

	@Override
	public Character unserialize(QDataInputStream stream,
			DataStreamVersion version) throws IOException {
		return new Character((char) stream.readUInt(16));
	}

}
