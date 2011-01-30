/**
 * Copyright Martin Sandsmark 2011 - LGPL 2.1 / GPLv3
 */

package com.lekebilen.quasseldroid.qtcomm.serializers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.lekebilen.quasseldroid.qtcomm.DataStreamVersion;
import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QDataOutputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeRegistry;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeSerializer;
import com.lekebilen.quasseldroid.qtcomm.QVariant;

public class QMap<T, V> implements QMetaTypeSerializer<Map<T, V>> {
	int element1Type = 0;
	int element2Type = 0;
	
	public QMap (int element1Type, int element2Type){
		this.element1Type = element1Type;
		this.element2Type = element2Type;
	}
	
	@Override
	public void serialize(QDataOutputStream stream,
			Map<T, V> data, DataStreamVersion version)
			throws IOException {
		stream.writeUInt(data.size(), 32);
		for (T key : data.keySet()) {
			QMetaTypeRegistry.instance().getTypeForId(element1Type).getSerializer().serialize(stream, key, version);
			QMetaTypeRegistry.instance().getTypeForId(element2Type).getSerializer().serialize(stream, data.get(key), version);
		}
	}

	@Override
	public Map<T, V> unserialize(QDataInputStream stream,
			DataStreamVersion version) throws IOException {
		
		Map map = new HashMap<String, T>();
		
		int len = (int) stream.readUInt(32);
		
		for (int i=0; i<len; i++) {
			T key = (T)QMetaTypeRegistry.instance().getTypeForId(element1Type).getSerializer().unserialize(stream, version);
			V value = (V)QMetaTypeRegistry.instance().getTypeForId(element2Type).getSerializer().unserialize(stream, version);
			map.put(key, value);
		}
		return map;
	}

}
