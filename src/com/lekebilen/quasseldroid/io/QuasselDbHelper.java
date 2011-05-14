package com.lekebilen.quasseldroid.io;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

public class QuasselDbHelper {
	public static final String KEY_ID = "_id";
	public static final String KEY_NAME = "name";
	public static final String KEY_ADDRESS = "server";
	public static final String KEY_PORT = "port";
	public static final String KEY_CERTIFICATE = "certificates";

	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;

	private static final String DATABASE_NAME = "data";
	private static final String CORE_TABLE = "cores";
	private static final String CERTIFICATE_TABLE = "certificates";
	private static final String DATABASE_CREATE = 
		"create table cores (_id integer primary key autoincrement, name text not null, server text not null, port integer not null);\n" +
		"create table certificates (content text);";
	private static final int DATABASE_VERSION = 1;

	private static final String TAG = "DbHelper";
	private final Context context;

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS favorites");
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

	public void addCore(String name, String address, int port) {

		//TODO: If name exists, edit instead of add
		try {
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_NAME, name);
			initialValues.put(KEY_ADDRESS, address);
			initialValues.put(KEY_PORT, port);
			db.insert(CORE_TABLE, null, initialValues);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}


		public void deleteCore(long rowId) {
			db.delete(CORE_TABLE, KEY_ID + "=" + rowId, null);
		}

		public Cursor getAllCores() {
			return db.query(CORE_TABLE, new String[] {KEY_ID,KEY_NAME}, null, null, null, null, null);
		}

		public Bundle getCore(long rowId) throws SQLException {
			Cursor cursor = db.query(true, CORE_TABLE, new String[] {KEY_ADDRESS, KEY_PORT, KEY_NAME}, KEY_ID + "=" + rowId, null, null, null, null, null);
			Bundle b = new Bundle();
			if (cursor != null) {
				cursor.moveToFirst();
				b.putString("name", cursor.getString(cursor.getColumnIndex(KEY_NAME)));
				b.putInt("port", cursor.getInt(cursor.getColumnIndex(KEY_PORT)));
				b.putString("address", cursor.getString(cursor.getColumnIndex(KEY_ADDRESS)));
				cursor.close(); 
			}
			return b;
		}

		public void updateCore(long rowId, String name, String address, int port) {
			ContentValues args = new ContentValues();
			args.put(KEY_NAME, name);
			args.put(KEY_ADDRESS, address);
			args.put(KEY_PORT, port);

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
					if (c.getBlob(c.getColumnIndex(KEY_CERTIFICATE)).equals(certificate)) {
						ret = true;
					}
					c.close();
				}
				return ret;
			}
		}

