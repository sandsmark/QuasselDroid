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

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.iskrembilen.quasseldroid.qtcomm.DataStreamVersion;
import com.iskrembilen.quasseldroid.qtcomm.QDataInputStream;
import com.iskrembilen.quasseldroid.qtcomm.QDataOutputStream;
import com.iskrembilen.quasseldroid.qtcomm.QMetaTypeSerializer;

public class QTime implements QMetaTypeSerializer<Calendar> {

	@Override
	public void serialize(QDataOutputStream stream, Calendar data,
			DataStreamVersion version) throws IOException {
		long sum = data.get(Calendar.HOUR) * 3600000L;
		sum += data.get(Calendar.MINUTE) * 60000L;
		sum += data.get(Calendar.SECOND) * 1000L;
		sum += data.get(Calendar.MILLISECOND);
		stream.writeUInt(sum, 32);
	}

	@Override
	public Calendar unserialize(QDataInputStream stream, DataStreamVersion version)
			throws IOException {
		long millisSinceMidnight = stream.readUInt(32);
		int hour = (int) (millisSinceMidnight / 3600000L);
		int minute = (int)((millisSinceMidnight - (hour*3600000L))/60000L);
		int second = (int)((millisSinceMidnight - (hour*3600000L) - (minute*60000L))/1000L);
		int millis = (int)((millisSinceMidnight - (hour*3600000L) - (minute*60000L) - (second * 1000L)));

		Calendar ret = new GregorianCalendar();
		ret.set(Calendar.HOUR, hour);
		ret.set(Calendar.MINUTE, minute);
		ret.set(Calendar.SECOND, second);
		ret.set(Calendar.MILLISECOND, millis);

		return ret;
	}

}
