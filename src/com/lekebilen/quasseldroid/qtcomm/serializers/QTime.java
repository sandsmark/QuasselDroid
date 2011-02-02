package com.lekebilen.quasseldroid.qtcomm.serializers;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.lekebilen.quasseldroid.qtcomm.DataStreamVersion;
import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QDataOutputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeSerializer;

public class QTime implements QMetaTypeSerializer<Calendar> {

	@Override
	public void serialize(QDataOutputStream stream, Calendar data,
			DataStreamVersion version) throws IOException {
		long sum = data.get(Calendar.HOUR) * 3600000;
		sum += data.get(Calendar.MINUTE) * 60000;
		sum += data.get(Calendar.SECOND) * 1000;
		sum += data.get(Calendar.MILLISECOND);
		stream.writeUInt(sum, 32);
	}

	@Override
	public Calendar unserialize(QDataInputStream stream, DataStreamVersion version)
			throws IOException {
		long millisSinceMidnight = stream.readUInt(32);
		int hour = (int) (millisSinceMidnight / 3600000);
		int minute = (int)((millisSinceMidnight - (hour*3600000))/60000);
		int second = (int)((millisSinceMidnight - (hour*3600000) - (minute*60000))/1000);
		int millis = (int)((millisSinceMidnight - (hour*3600000) - (minute*60000) - (second * 1000)));

		Calendar ret = new GregorianCalendar();
		ret.set(Calendar.HOUR, hour);
		ret.set(Calendar.MINUTE, minute);
		ret.set(Calendar.SECOND, second);
		ret.set(Calendar.MILLISECOND, millis);

		return ret;
	}

}
