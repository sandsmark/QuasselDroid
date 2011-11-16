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

import com.iskrembilen.quasseldroid.qtcomm.DataStreamVersion;
import com.iskrembilen.quasseldroid.qtcomm.QDataInputStream;
import com.iskrembilen.quasseldroid.qtcomm.QDataOutputStream;
import com.iskrembilen.quasseldroid.qtcomm.QMetaTypeSerializer;

public class QChar implements QMetaTypeSerializer<Character> {

	@Override
	public void serialize(QDataOutputStream stream, Character data,
			DataStreamVersion version) throws IOException {
		stream.writeUInt(data.charValue(), 16);
		
	}

	@Override
	public Character unserialize(QDataInputStream stream,
			DataStreamVersion version) throws IOException {
		return Character.valueOf((char) stream.readUInt(16));
	}

}
