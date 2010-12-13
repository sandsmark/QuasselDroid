/**
 * Copyright Frederik M.J.V. 2010 - LGPL 2.1 / GPLv3
 */

package com.lekebilen.quasseldroid.qtcomm.serializers;

import java.io.IOException;

import com.lekebilen.quasseldroid.qtcomm.DataStreamVersion;
import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QDataOutputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeSerializer;

public class UnsignedInteger implements QMetaTypeSerializer<Long> {
	@Override
	public void serialize(QDataOutputStream stream, Long data,
			DataStreamVersion version) throws IOException {
		stream.writeUInt(data);
	}

	@Override
	public Long unserialize(QDataInputStream stream, DataStreamVersion version)
			throws IOException {
		return stream.readUInt();
	}
}
