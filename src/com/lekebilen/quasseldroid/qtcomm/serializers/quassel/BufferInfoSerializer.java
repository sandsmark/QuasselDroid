package com.lekebilen.quasseldroid.qtcomm.serializers.quassel;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.lekebilen.quasseldroid.BufferInfo;
import com.lekebilen.quasseldroid.qtcomm.DataStreamVersion;
import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QDataOutputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeRegistry;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeSerializer;

public class BufferInfoSerializer implements QMetaTypeSerializer<BufferInfo> {

	@Override
	public void serialize(QDataOutputStream stream, BufferInfo data,
			DataStreamVersion version) throws IOException {
		stream.writeInt(data.id);
		stream.writeInt(data.networkId);
		stream.writeShort(data.type);
		stream.writeUInt(data.groupId, 32);
		QMetaTypeRegistry.instance().getTypeForName("QByteArray").getSerializer().serialize(stream, data.name, version);
	}

	@Override
	public BufferInfo unserialize(QDataInputStream stream,
			DataStreamVersion version) throws IOException {
		BufferInfo ret = new BufferInfo();
		ret.id = stream.readInt();
		ret.networkId = stream.readInt();
		ret.type = stream.readShort();
		ret.groupId = stream.readUInt(32);
		ret.name = (ByteBuffer) QMetaTypeRegistry.instance().getTypeForName("QByteArray").getSerializer().unserialize(stream, version);
		return ret;
	}
}
