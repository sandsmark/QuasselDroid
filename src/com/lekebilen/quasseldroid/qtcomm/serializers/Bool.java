package com.lekebilen.quasseldroid.qtcomm.serializers;

import java.io.IOException;

import com.lekebilen.quasseldroid.qtcomm.DataStreamVersion;
import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QDataOutputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeSerializer;

public class Bool implements QMetaTypeSerializer<Boolean> {

	@Override
	public void serialize(QDataOutputStream stream, Boolean data,
			DataStreamVersion version) throws IOException {
		stream.writeBoolean(data);
	}

	@Override
	public Boolean unserialize(QDataInputStream stream, DataStreamVersion version)
			throws IOException {
		return stream.readBoolean();
	}

}
