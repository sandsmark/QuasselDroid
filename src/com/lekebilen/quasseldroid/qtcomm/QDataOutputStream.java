/**
 * Copyright Frederik M.J.V. 2010 - LGPL 2.1 / GPLv3
 */

package com.lekebilen.quasseldroid.qtcomm;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class QDataOutputStream extends DataOutputStream{
	public QDataOutputStream(OutputStream base){
		super(base);
	}
	public void writeQVariant(QVariant<?> var){

	}
	public QVariant<?> readQVariant(){
		return null;
	}
	public void writeUInt(long num) throws IOException{
		writeByte((int)(num>>24 &0xFF));
		writeByte((int)(num>>16 &0xFF));
		writeByte((int)(num>>8 &0xFF));
		writeByte((int)(num &0xFF));
	}
}
