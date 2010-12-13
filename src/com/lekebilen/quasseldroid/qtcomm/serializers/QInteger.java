/**
 * Copyright Frederik M.J.V. 2010 - LGPL 2.1 / GPLv3
 */

package com.lekebilen.quasseldroid.qtcomm.serializers;

import java.io.IOException;

import com.lekebilen.quasseldroid.qtcomm.DataStreamVersion;
import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QDataOutputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeSerializer;

public class QInteger implements QMetaTypeSerializer<Integer> {
	@Override
	public void serialize(QDataOutputStream stream, Integer data,
			DataStreamVersion version) throws IOException {
		stream.writeInt(data);
	}

	@Override
	public Integer unserialize(QDataInputStream stream, DataStreamVersion version)
			throws IOException {
		return stream.readInt();
	}
}
