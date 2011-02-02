package com.lekebilen.quasseldroid;

public class NetworkServer {
	public String host, password, proxyHost, proxyUser, proxyPassword;
	public long port, sslVersion, proxyType, proxyPort;
	public boolean useSsl, useProxy;
	
	public NetworkServer (String host, long port, String password,
			boolean useSsl, long sslVersion,
			boolean useProxy, String proxyHost, long proxyPort, long proxyType, String proxyUser, String proxyPassword) {
		this.host = host;
		this.port = port;
		this.password = password;
		this.useSsl = useSsl;
		this.sslVersion = sslVersion;
		this.useProxy = useProxy;
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.proxyType = proxyType;
		this.proxyUser = proxyUser;
		this.proxyPassword = proxyPassword;
	}
}
