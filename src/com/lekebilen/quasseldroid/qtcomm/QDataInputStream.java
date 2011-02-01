/**
 * Copyright Frederik M.J.V. 2010
 * Copyright Martin Sandsmark 2011 
 * LGPL 2.1 / GPLv3
 */

package com.lekebilen.quasseldroid.qtcomm;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class QDataInputStream extends DataInputStream{
	public QDataInputStream(InputStream base){
		super(base);
	}
	public QVariant readQVariant(){
		return null;
	}
	public long readUInt(int size) throws IOException{
		switch (size) {
		case 64:
			return readLong();
		case 32:
			byte[] buf = new byte[4]; 
			readFully(buf);
			
			long ret =  ((buf[0]&255) << 24 | (buf[1]&255) << 16 | (buf[2]&255) << 8 | (buf[3]&255)) & 0xFFFFFFFFL;
			return ret;
		case 16:
			return readUnsignedShort();
		case 8:
			return readUnsignedByte();
		default:
			System.err.println("No support for reading unsigned ints of size " + size);
			return 0; // fuck you too
		}
	}
}
