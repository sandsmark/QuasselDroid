/**
    QuasselDroid - Quassel client for Android
 	Copyright (C) 2011 Martin Sandsmark <martin.sandsmark@kde.org>

    This program is free software: you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation, either version 3 of the License, or (at your option)
    any later version, or under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either version 2.1 of
    the License, or (at your option) any later version.

 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.

    You should have received a copy of the GNU General Public License and the
    GNU Lesser General Public License along with this program.  If not, see
    <http://www.gnu.org/licenses/>.
 */


package com.iskrembilen.quasseldroid.qtcomm.serializers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.iskrembilen.quasseldroid.qtcomm.DataStreamVersion;
import com.iskrembilen.quasseldroid.qtcomm.EmptyQVariantException;
import com.iskrembilen.quasseldroid.qtcomm.QDataInputStream;
import com.iskrembilen.quasseldroid.qtcomm.QDataOutputStream;
import com.iskrembilen.quasseldroid.qtcomm.QMetaTypeRegistry;
import com.iskrembilen.quasseldroid.qtcomm.QMetaTypeSerializer;

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

		for (Map.Entry<T, V> element : data.entrySet()) {
			keySerializer.serialize(stream, element.getKey(), version);
			valueSerializer.serialize(stream, element.getValue(), version);
		}
	}

	@Override
	public Map<T, V> unserialize(QDataInputStream stream,
			DataStreamVersion version) throws IOException, EmptyQVariantException {
		
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
