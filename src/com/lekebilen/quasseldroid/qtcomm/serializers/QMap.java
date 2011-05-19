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
	QMetaTypeSerializer<T> keySerializer;
	QMetaTypeSerializer<V> valueSerializer;
	
	public QMap (String element1Type, String element2Type){
		this.keyType = element1Type;
		this.valueType = element2Type;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void serialize(QDataOutputStream stream,
			Map<T, V> data, DataStreamVersion version)
			throws IOException {
		stream.writeUInt(data.size(), 32);
		keySerializer = QMetaTypeRegistry.instance().getTypeForName(keyType).getSerializer();
		valueSerializer = QMetaTypeRegistry.instance().getTypeForName(valueType).getSerializer();

		for (T key : data.keySet()) {
			keySerializer.serialize(stream, key, version);
			valueSerializer.serialize(stream, data.get(key), version);
		}
	}

	@Override
	public Map<T, V> unserialize(QDataInputStream stream,
			DataStreamVersion version) throws IOException {
		
		Map map = new HashMap<String, T>();
		keySerializer = QMetaTypeRegistry.instance().getTypeForName(keyType).getSerializer();
		valueSerializer = QMetaTypeRegistry.instance().getTypeForName(valueType).getSerializer();		
		int len = (int) stream.readUInt(32);
		for (int i=0; i<len; i++) {
			map.put((T)keySerializer.unserialize(stream, version), (V)valueSerializer.unserialize(stream, version));
		}
		return map;
	}

}
