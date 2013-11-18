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

package com.iskrembilen.quasseldroid.qtcomm;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class QDataInputStream extends DataInputStream {
    byte[] buf = new byte[4];

    public QDataInputStream(InputStream base) {
        super(base);
    }

    public QVariant readQVariant() {
        return null;
    }

    public long readUInt(int size) throws IOException {
        switch (size) {
            case 64:
                return readLong();
            case 32:
                readFully(buf);
                long ret = ((buf[0] & 255) << 24 | (buf[1] & 255) << 16 | (buf[2] & 255) << 8 | (buf[3] & 255)) & 0xFFFFFFFFL;
                return ret;
            case 16:
                return readUnsignedShort();
            case 8:
                return readUnsignedByte();
            default:
                System.err.println("No support for reading unsigned ints of size " + size);
                return 0; // fuck you too
        }
    }
}
