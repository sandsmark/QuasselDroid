/**
    QuasselDroid - Quassel client for Android
 	Copyright (C) 2011 Ken Børge Viktil
 	Copyright (C) 2011 Magnus Fjell
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

package com.iskrembilen.quasseldroid.io;

import java.util.Arrays;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

import com.iskrembilen.quasseldroid.IrcMessage;

public class QuasselDbHelper {
	public static final String KEY_ID = "_id";
	public static final String KEY_NAME = "name";
	public static final String KEY_ADDRESS = "server";
	public static final String KEY_PORT = "port";
	public static final String KEY_SSL = "ssl";
	public static final String KEY_CERTIFICATE = "certificates";
	public static final String KEY_BUFFERID = "bufferid";
	public static final String KEY_EVENT = "event";

	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;

	private static final String DATABASE_NAME = "data";
	private static final String CORE_TABLE = "cores";
	private static final String CERTIFICATE_TABLE = "certificates";
	private static final String HIDDENEVENTS_TABLE = "hiddenevents";
	private static final String DATABASE_CREATE_TABLE1 = 
		"create table cores (_id integer primary key autoincrement, name text not null, server text not null, port integer not null, ssl integer not null);";
	private static final String DATABASE_CREATE_TABLE2 =  "create table certificates (content text);";
	private static final String DATABASE_CREATE_TABLE3 = "create table hiddenevents (bufferid integer not null, event text not null);";
	private static final int DATABASE_VERSION = 1;

	private static final String TAG = "DbHelper";
	private final Context context;

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE_TABLE1);
			db.execSQL(DATABASE_CREATE_TABLE2);
			db.execSQL(DATABASE_CREATE_TABLE3);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + CORE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + CERTIFICATE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + HIDDENEVENTS_TABLE);
			onCreate(db);
		}
	}

	public QuasselDbHelper(Context ctx) {
		this.context = ctx;
	}

	public void open() throws SQLException {
		dbHelper = new DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
	}

	public void close() {
		db.close();
		db = null;
		dbHelper.close();
		dbHelper = null;	
	}

	public void addCore(String name, String address, int port, boolean useSSL) {
		try {
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_NAME, name);
			initialValues.put(KEY_ADDRESS, address);
			initialValues.put(KEY_PORT, port);
			initialValues.put(KEY_SSL, useSSL ? 1:0);
			db.insert(CORE_TABLE, null, initialValues);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}


	public void deleteCore(long rowId) {
		db.delete(CORE_TABLE, KEY_ID + "=" + rowId, null);
	}

	public boolean hasCores() {
		Cursor c = db.query(CORE_TABLE, new String[] {KEY_ID,KEY_NAME}, null, null, null, null, null);
		boolean hasCores;
		if (c != null && c.getCount()>0) {
			hasCores = true;
		}else{
			hasCores = false;
		}

		if(c != null) c.close();
		return hasCores;
	}

	public Cursor getAllCores() {
		return db.query(CORE_TABLE, new String[] {KEY_ID,KEY_NAME}, null, null, null, null, null);
	}

	public Bundle getCore(long rowId) throws SQLException {
		Cursor cursor = db.query(true, CORE_TABLE, new String[] {KEY_ADDRESS, KEY_PORT, KEY_NAME, KEY_SSL}, KEY_ID + "=" + rowId, null, null, null, null, null);
		Bundle b = new Bundle();
		if (cursor != null) {
			cursor.moveToFirst();
			b.putString(KEY_NAME, cursor.getString(cursor.getColumnIndex(KEY_NAME)));
			b.putInt(KEY_PORT, cursor.getInt(cursor.getColumnIndex(KEY_PORT)));
			b.putString(KEY_ADDRESS, cursor.getString(cursor.getColumnIndex(KEY_ADDRESS)));
			b.putBoolean(KEY_SSL, cursor.getInt(cursor.getColumnIndex(KEY_SSL))>0);
			cursor.close(); 
		}
		return b;
	}

	public void updateCore(long rowId, String name, String address, int port, boolean useSSL) {
		ContentValues args = new ContentValues();
		args.put(KEY_NAME, name);
		args.put(KEY_ADDRESS, address);
		args.put(KEY_PORT, port);
		args.put(KEY_SSL, useSSL);
		db.update(CORE_TABLE, args, KEY_ID + "=" + rowId, null);
		//TODO: need to make sure that core names are unique, and send back som error to the user if its not, or we  get problems if names are the same
	}
	public void storeCertificate(byte[] certificate) {
		try {
			ContentValues value = new ContentValues();
			value.put(KEY_CERTIFICATE, certificate);
			db.insert(CERTIFICATE_TABLE, null, value);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean hasCertificate(String certificate) {
		Cursor c = db.query(CERTIFICATE_TABLE, new String[] {KEY_CERTIFICATE}, null, null, null, null, null);
		boolean ret = false;
		if (c != null) { // This is retarded, fuck Android.
			if (Arrays.equals(c.getBlob(c.getColumnIndex(KEY_CERTIFICATE)), certificate.getBytes())) {
				ret = true;
			}
			c.close();
		}
		return ret;
	}

	public void addHiddenEvent(IrcMessage.Type event, int bufferId) {
		try {
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_EVENT, event.name());
			initialValues.put(KEY_BUFFERID, bufferId);
			db.insert(HIDDENEVENTS_TABLE, null, initialValues);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public void cleanupEvents(Integer[] bufferids) {
		if(bufferids.length==0)
			return;
		StringBuilder list = new StringBuilder("(");
		for (int id : bufferids) {
			list.append(id + ",");
		}
		list.deleteCharAt(list.length()-1);
		list.append(")");
		db.delete(HIDDENEVENTS_TABLE, KEY_BUFFERID +" NOT IN "+list.toString(), null);
	}

	public void deleteHiddenEvent(IrcMessage.Type event, int bufferId) {
		db.delete(HIDDENEVENTS_TABLE, KEY_EVENT + "='" + event.name() + "' AND "+KEY_BUFFERID + "=" +bufferId, null);
	}

	public IrcMessage.Type[] getHiddenEvents(int bufferId) throws SQLException {
		Cursor cursor = db.query(true, HIDDENEVENTS_TABLE, new String[] {KEY_EVENT}, KEY_BUFFERID + "=" + bufferId, null, null, null, null, null);
		IrcMessage.Type[] events = null;
		if (cursor != null) {
			if(cursor.getCount() > 0) {
				events =  new IrcMessage.Type[cursor.getCount()];
				cursor.moveToFirst();
				int i = 0;
				while (!cursor.isAfterLast()) {
					events[i] = IrcMessage.Type.valueOf(cursor.getString(0));
					i++;
					cursor.moveToNext();
				}
			}
			cursor.close(); 
		}
		return events;
	}
}

