package com.lekebilen.quasseldroid.qtcomm.serializers.quassel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

import com.lekebilen.quasseldroid.BufferInfo;
import com.lekebilen.quasseldroid.IrcMessage;
import com.lekebilen.quasseldroid.qtcomm.DataStreamVersion;
import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QDataOutputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeRegistry;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeSerializer;

public class MessageSerializer implements QMetaTypeSerializer<IrcMessage> {

	@Override
	public void serialize(QDataOutputStream stream, IrcMessage data,
			DataStreamVersion version) throws IOException {
		stream.writeInt(data.messageId);
		stream.writeUInt(data.timestamp.getTime() / 1000, 32);
		stream.writeUInt(data.type.getValue(), 32);
		stream.writeByte(data.flags);
		stream.writeBytes(data.sender);// TODO FIXME
		stream.writeBytes(data.content);//ditto
	}

	@Override
	public IrcMessage unserialize(QDataInputStream stream,
			DataStreamVersion version) throws IOException {
		IrcMessage ret = new IrcMessage();
		ret.messageId = stream.readInt();
		ret.timestamp = new Date(stream.readUInt(32) * 1000);
		ret.type = IrcMessage.Type.getForValue((int) stream.readUInt(32));
		ret.flags = stream.readByte();
		ret.bufferInfo = (BufferInfo) QMetaTypeRegistry.instance().getTypeForName("BufferInfo").getSerializer().unserialize(stream, version);
		ret.sender = (String) QMetaTypeRegistry.instance().getTypeForName("QByteArray").getSerializer().unserialize(stream, version);
		ret.content =  (String) QMetaTypeRegistry.instance().getTypeForName("QByteArray").getSerializer().unserialize(stream, version);
		
		return ret;
	}

}
