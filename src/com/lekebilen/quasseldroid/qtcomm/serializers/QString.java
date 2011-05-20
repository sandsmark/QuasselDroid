/**
 * Copyright Frederik M.J.V. 2010 - LGPL 2.1 / GPLv3
 */

package com.lekebilen.quasseldroid.qtcomm.serializers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import android.util.Log;

import com.lekebilen.quasseldroid.qtcomm.DataStreamVersion;
import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QDataOutputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeSerializer;

public class QString implements QMetaTypeSerializer<String> {
	static int buflen = -1;
	ByteBuffer buf;
	Charset c = Charset.forName("UTF-16BE");
	CharsetDecoder decoder = c.newDecoder();
	CharBuffer a;
	@Override
	public void serialize(QDataOutputStream stream, String data,
			DataStreamVersion version) throws IOException {
		if(data==null){
			stream.writeUInt(0xFFFFFFFF, 32);
		}else{
			stream.writeUInt(data.getBytes("UTF-16BE").length, 32);
			stream.write(data.getBytes("UTF-16BE"));
		}
	}

	@Override
	public String unserialize(QDataInputStream stream, DataStreamVersion version)
			throws IOException {
		int len = (int)stream.readUInt(32);
		if(len == 0xFFFFFFFF)
			return "";

		if (len > buflen) {
			buf = ByteBuffer.allocate(len);
			a = CharBuffer.allocate(len);
			buflen = len;
		}
		buf.clear();
		a.clear();
		a.mark();
		buf.limit(len);
		stream.readFully(buf.array(), 0, len);
		decoder.decode(buf, a, false);
		a.limit(a.position());
		a.reset();
		//Log.e("AAAA", a.toString());
		return a.toString();
	}
}
