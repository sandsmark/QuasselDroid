/*
    QuasselDroid - Quassel client for Android
    Copyright (C) 2015 Ken Børge Viktil
    Copyright (C) 2015 Magnus Fjell
    Copyright (C) 2015 Martin Sandsmark <martin.sandsmark@kde.org>

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

package com.iskrembilen.quasseldroid.protocol.qtcomm;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class QDataOutputStream extends DataOutputStream {
    public QDataOutputStream(OutputStream base) {
        super(base);
    }

    public void writeQVariant(QVariant<?> var) {

    }

    public QVariant<?> readQVariant() {
        return null;
    }

    public void writeUInt(long num, int size) throws IOException {
        switch (size) {
            case 64:
                writeByte((int) (num >> 56 & 0xFF));
                writeByte((int) (num >> 48 & 0xFF));
                writeByte((int) (num >> 40 & 0xFF));
                writeByte((int) (num >> 32 & 0xFF));
            case 32:
                writeByte((int) (num >> 24 & 0xFF));
                writeByte((int) (num >> 16 & 0xFF));
            case 16:
                writeByte((int) (num >> 8 & 0xFF));
            case 8:
                writeByte((int) (num & 0xFF));
        }
    }
}
