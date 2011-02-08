package com.lekebilen.quasseldroid;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Adapter;

import com.lekebilen.quasseldroid.gui.BufferActivity;

/**
 * This Service holdes the connection to the core from the phone, it handles all the communication with the core. It talks to CoreConnection
 * 
 */

public class CoreConnService extends Service{
	
	private static final String TAG = CoreConnService.class.getSimpleName();
	
	private CoreConnection coreConn;
	private final IBinder binder = new LocalBinder();

	
	/**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
	public class LocalBinder extends Binder {
		public CoreConnService getService() {
            return CoreConnService.this;
        }
    }

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return binder;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleIntent(intent);
		return START_STICKY;

	}
	
	/**
	 * Handle the data in the intent, and use it to connect with CoreConnect
	 * @param intent
	 */
	private void handleIntent(Intent intent) {
		Bundle connectData = intent.getExtras();
		String address = connectData.getString("address");
		int port = connectData.getInt("port");
		String username = connectData.getString("username");
		String password = connectData.getString("password");
		Boolean ssl = connectData.getBoolean("ssl");
		Log.i(TAG, "Connecting to core: "+address+":"+port+" with username " +username);
		coreConn = new CoreConnection(address, port, username, password, ssl, this);
	}
	
	public void newMessage(IrcMessage message) {
		//TODO
	}
	
	public void newBuffer(Buffer buffer) {
		Log.i(TAG, "GETTING BUFFER");
	}
	
	public void newUser(IrcUser user) {
		
	}
	
	public void sendMessage(Buffer buffer, String message){
		//TODO
	}
	
	public void getBufferList(BufferActivity.BufferListAdapter adapter) {
		Buffer buffer = new Buffer(new BufferInfo());
		buffer.getInfo().name = "#MTDT12";
		adapter.addBuffer(buffer);
		adapter.notifyDataSetChanged();
		coreConn.requestBuffers();
	}

}