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
import com.lekebilen.quasseldroid.qtcomm.QMetaType;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeRegistry;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeSerializer;
import com.lekebilen.quasseldroid.qtcomm.QVariant;

public class QMap<T, V> implements QMetaTypeSerializer<Map<T, V>> {
	String keyType;
	String valueType;
	
	public QMap (String element1Type, String element2Type){
		this.keyType = element1Type;
		this.valueType = element2Type;
	}
	
	@Override
	public void serialize(QDataOutputStream stream,
			Map<T, V> data, DataStreamVersion version)
			throws IOException {
		stream.writeUInt(data.size(), 32);
		for (T key : data.keySet()) {
			QMetaTypeRegistry.instance().getTypeForName(keyType).getSerializer().serialize(stream, key, version);
			QMetaTypeRegistry.instance().getTypeForName(valueType).getSerializer().serialize(stream, data.get(key), version);
		}
	}

	@Override
	public Map<T, V> unserialize(QDataInputStream stream,
			DataStreamVersion version) throws IOException {
		
		Map map = new HashMap<String, T>();
		
		int len = (int) stream.readUInt(32);
		for (int i=0; i<len; i++) {
			T key = (T)QMetaTypeRegistry.instance().getTypeForName(keyType).getSerializer().unserialize(stream, version);
			V value = (V)QMetaTypeRegistry.instance().getTypeForName(valueType).getSerializer().unserialize(stream, version);
			map.put(key, value);
		}
		return map;
	}

}
