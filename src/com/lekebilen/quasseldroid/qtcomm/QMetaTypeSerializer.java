/**
 * Copyright Frederik M.J.V. 2010 - LGPL 2.1 / GPLv3
 */

package com.lekebilen.quasseldroid.qtcomm;

import java.io.IOException;


public interface QMetaTypeSerializer<T extends Object> {
	public void serialize(QDataOutputStream stream, T data, DataStreamVersion version) throws IOException;
	public T unserialize(QDataInputStream stream, DataStreamVersion version) throws IOException;
}
