package com.lekebilen.quasseldroid.communication;

import com.lekebilen.quasseldroid.CoreConnection;
import com.lekebilen.quasseldroid.gui.LoginActivity;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * This Service holdes the connection to the core from the phone, it handles all the communication with the core. It talks to CoreConnection
 * 
 */

public class CoreConnService extends Service{
	
	private static final String TAG = CoreConnService.class.getSimpleName();
	
	private CoreConnection coreConn;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

}
