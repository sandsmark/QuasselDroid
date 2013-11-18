/*
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

package com.iskrembilen.quasseldroid;

public class NetworkServer {
    public String host, password, proxyHost, proxyUser, proxyPassword;
    public long port, sslVersion, proxyType, proxyPort;
    public boolean useSsl, useProxy;

    public NetworkServer(String host, long port, String password,
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

    public String toString() {
        return "" + this.host + ":" + this.port;
    }
}
