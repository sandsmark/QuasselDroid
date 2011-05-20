package com.lekebilen.quasseldroid.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;

/**
 * Utility class for reading a String from the QDataInputStream. 
 * Made to decrease the unserialization time for strings
 * Uses a different method to create the string then the old new String(byte[], encoding)
 * 
 */
public class StringReaderUtil {
	int buflen = -1;
	ByteBuffer buf;
	CharsetDecoder decoder;
	CharBuffer charBuffer;
	
	/**
	 * Create a new StringReader util
	 * @param charset name of the charset to decode
	 */
	public StringReaderUtil(String charset) {
		decoder = Charset.forName(charset).newDecoder();
	}
	
	public String readString(QDataInputStream stream, int len) throws IOException {
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
