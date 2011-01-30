package com.lekebilen.quasseldroid.qtcomm.serializers;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import com.lekebilen.quasseldroid.qtcomm.DataStreamVersion;
import com.lekebilen.quasseldroid.qtcomm.QDataInputStream;
import com.lekebilen.quasseldroid.qtcomm.QDataOutputStream;
import com.lekebilen.quasseldroid.qtcomm.QMetaTypeSerializer;

public class QDateTime implements QMetaTypeSerializer<Calendar> {

	@Override
	public void serialize(QDataOutputStream stream, Calendar data,
			DataStreamVersion version) throws IOException {
		int a = (14 - data.get(Calendar.MONTH)) / 12;
		int y = data.get(Calendar.YEAR) + 4800 - a;
		int m = data.get(Calendar.MONTH) + 12 * a - 3;
		int jdn = data.get(Calendar.DAY_OF_MONTH) + (153 * m + 2) / 5 + 365 * y + y / 4 - y/100 + y/400 - 32045;
		stream.writeUInt(jdn, 32);
		
		
		int secondsSinceMidnight = data.get(Calendar.HOUR) * 3600 + data.get(Calendar.MINUTE) * 60 + data.get(Calendar.SECOND);
		stream.writeUInt(secondsSinceMidnight, 32);
		
		if (data.getTimeZone().equals(TimeZone.getTimeZone("UTC")))
			stream.writeUInt(1, 8);
		else if (data.getTimeZone().equals(TimeZone.getDefault()))
			stream.writeUInt(0, 8);
		else {
			System.err.println("We can't serialize dates in other timezones! FIXME");
			stream.writeUInt(0, 8);
		}
		
	}

	@Override
	public Calendar unserialize(QDataInputStream stream,
			DataStreamVersion version) throws IOException {
		long julianDay = stream.readUInt(32);
		long secondsSinceMidnight = stream.readUInt(32);
		long isUTC = stream.readUInt(8);
		
		double J = (double)(julianDay) + 0.5f;
		long j = (int) (J + 32044);
		long g = j / 146097;
		long dg = j % 146097;
		long c = (((dg / 36524) + 1) * 3) / 4;
		long dc = dg - c * 36524;
		long b = dc / 1461;
		long db = dc % 1461;
		long a = (db / 365 + 1) * 3 / 4;
		long da = db - a * 365;
		long y = g * 400 + c * 100 + b * 4 + a;
		long m = (da * 5 + 308) / 153 - 2;
		long d = da - (m + 4) * 153 / 5 + 122;
		
		int year = (int) (y - 4800 + (m+2)/12);
		int month = (int) ((m+2) % 12 + 1);
		int day = (int) (d + 1);
		
		int hour = (int) (secondsSinceMidnight / 3600);
		int minute = (int)((secondsSinceMidnight - (hour*3600))/60);
		int second = (int)((secondsSinceMidnight - (hour*3600) - (minute*60)));
		
		TimeZone zone;
		if (isUTC == 1)
			zone = TimeZone.getTimeZone("UTC");
		else
			zone = TimeZone.getDefault();
		
		Calendar cal = Calendar.getInstance(zone);
		cal.set(year, month, day, hour, minute, second);
		System.out.println("Julian day:" + julianDay + " Seconds since midnight: " + secondsSinceMidnight);
		System.out.println("Deserialized date: " + day + "." + month + "." + year + "  " + hour + ":" + minute + ":" + second);
		return cal;
	}
}
