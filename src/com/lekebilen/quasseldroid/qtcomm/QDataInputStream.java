/**
 * Copyright Frederik M.J.V. 2010 - LGPL 2.1 / GPLv3
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
			long ret =  ((long) (buf[0] << 24 | buf[1] << 16 | buf[2] << 8 | buf[3])) & 0xFFFFFFFFL;
			return ret;
		case 16:
			return readUnsignedShort();
		case 8:
			return readUnsignedByte();
		default:
			return 0; // fuck you too
		}
	}
}
