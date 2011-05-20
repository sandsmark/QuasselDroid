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
	int buflen = -1;
	ByteBuffer buf;
	CharsetDecoder decoder = Charset.forName("UTF-16BE").newDecoder();
	CharBuffer charBuffer;
	
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

		if (len > buflen) { // If the buffers we have are to small, make them bigger
			buf = ByteBuffer.allocate(len);
			charBuffer = CharBuffer.allocate(len);
			buflen = len;
		}
		
		//reset buffers, so we start from the beginning of them 
		buf.clear();
		charBuffer.clear();
		
		//mark the start so we can reset back after we have read in the string
		charBuffer.mark();
		
		// Set the limit of the byte buffer, so we know where to stop the string. 
		// Or else you get characters from old strings that was longer then this one
		buf.limit(len);
		
		//Read the string
		stream.readFully(buf.array(), 0, len);
		
		//Decode it with correct encoding
		decoder.decode(buf, charBuffer, false);
		
		//Set where the current string ends, it is the position we are at after decoding into the buffer
		charBuffer.limit(charBuffer.position());
		
		//Reset buffer back to the mark(the start of the buffer) so we can convert to string
		charBuffer.reset();
		
		return charBuffer.toString();
	}
}
