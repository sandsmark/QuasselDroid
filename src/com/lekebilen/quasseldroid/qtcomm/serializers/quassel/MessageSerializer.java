package com.lekebilen.quasseldroid.qtcomm.serializers.quassel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

import com.lekebilen.quasseldroid.BufferInfo;
import com.lekebilen.quasseldroid.Message;
import com.lekebilen.quasseldroid.qtcomm.DataStreamVersion;
import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QDataOutputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeRegistry;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeSerializer;

public class MessageSerializer implements QMetaTypeSerializer<Message> {

	@Override
	public void serialize(QDataOutputStream stream, Message data,
			DataStreamVersion version) throws IOException {
		stream.writeInt(data.messageId);
		stream.writeUInt(data.timestamp.getTime() / 1000, 32);
		stream.writeUInt(data.type.getValue(), 32);
		stream.writeByte(data.flags);
		stream.writeBytes(data.sender);// TODO FIXME
		stream.writeBytes(data.content);//ditto
	}

	@Override
	public Message unserialize(QDataInputStream stream,
			DataStreamVersion version) throws IOException {
		Message ret = new Message();
		ret.messageId = stream.readInt();
		ret.timestamp = new Date(stream.readUInt(32) * 1000);
		ret.type = Message.Type.getForValue((int) stream.readUInt(32));
		ret.flags = stream.readByte();
		ret.bufferInfo = (BufferInfo) QMetaTypeRegistry.instance().getTypeForName("BufferInfo").getSerializer().unserialize(stream, version);
		ByteBuffer b = (ByteBuffer) QMetaTypeRegistry.instance().getTypeForName("QByteArray").getSerializer().unserialize(stream, version); 
		ret.sender = new String(b.array(), "UTF-8BE");
		b = (ByteBuffer) QMetaTypeRegistry.instance().getTypeForName("QByteArray").getSerializer().unserialize(stream, version); 
		ret.content = new String(b.array(), "UTF-8BE");
		
		return ret;
	}

}
