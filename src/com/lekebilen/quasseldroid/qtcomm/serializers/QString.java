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
import com.lekebilen.quasseldroid.util.StringReaderUtil;

public class QString implements QMetaTypeSerializer<String> {
	
	StringReaderUtil stringReader = new StringReaderUtil("UTF-16BE");
	
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
		
		return stringReader.readString(stream, len);
	}
}
