/**
 QuasselDroid - Quassel client for Android
 Copyright (C) 2010 Frederik M. J. Vestre
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

import com.iskrembilen.quasseldroid.qtcomm.DataStreamVersion;
import com.iskrembilen.quasseldroid.qtcomm.QDataInputStream;
import com.iskrembilen.quasseldroid.qtcomm.QDataOutputStream;
import com.iskrembilen.quasseldroid.qtcomm.QMetaTypeSerializer;
import com.iskrembilen.quasseldroid.util.StringReaderUtil;

import java.io.IOException;

public class QString implements QMetaTypeSerializer<String> {

    StringReaderUtil stringReader = new StringReaderUtil("UTF-16BE");

    @Override
    public void serialize(QDataOutputStream stream, String data,
                          DataStreamVersion version) throws IOException {
        if (data == null) {
            stream.writeUInt(0xFFFFFFFF, 32);
        } else {
            stream.writeUInt(data.getBytes("UTF-16BE").length, 32);
            stream.write(data.getBytes("UTF-16BE"));
        }
    }

    @Override
    public String unserialize(QDataInputStream stream, DataStreamVersion version)
            throws IOException {
        int len = (int) stream.readUInt(32);
        if (len == 0xFFFFFFFF)
            return "";

        return stringReader.readString(stream, len);
    }
}
