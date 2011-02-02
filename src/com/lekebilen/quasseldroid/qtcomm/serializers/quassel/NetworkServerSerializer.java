package com.lekebilen.quasseldroid.qtcomm.serializers.quassel;

import java.io.IOException;
import java.util.Map;

import com.lekebilen.quasseldroid.NetworkServer;
import com.lekebilen.quasseldroid.qtcomm.DataStreamVersion;
import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QDataOutputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeRegistry;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeSerializer;
import com.lekebilen.quasseldroid.qtcomm.QVariant;

public class NetworkServerSerializer implements QMetaTypeSerializer<NetworkServer> {

	@Override
	public void serialize(QDataOutputStream stream, NetworkServer data,
			DataStreamVersion version) throws IOException {
		throw new IOException("IMPLEMENT ME! TODO DAWG");
	}

	@Override
	public NetworkServer unserialize(QDataInputStream stream,
			DataStreamVersion version) throws IOException {
		Map<String, QVariant<?>> map = (Map<String, QVariant<?>>)
			QMetaTypeRegistry.instance().getTypeForName("QVariantMap").getSerializer().unserialize(stream, version);
		
		return new NetworkServer((String)map.get("Host").getData(),
				(Long)map.get("Port").getData(),
				(String)map.get("Password").getData(),
				
				(Boolean)map.get("UseSSL").getData(),
				(Integer)map.get("sslVersion").getData(),
				
				(Boolean)map.get("UseProxy").getData(),
				(String)map.get("ProxyHost").getData(),
				(Long)map.get("ProxyPort").getData(),
				(Integer)map.get("ProxyType").getData(),
				(String)map.get("ProxyUser").getData(),
				(String)map.get("ProxyPass").getData()
				);
		
	}

}
