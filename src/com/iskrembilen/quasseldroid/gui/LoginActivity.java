/*
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

package com.iskrembilen.quasseldroid.gui;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.io.QuasselDbHelper;
import com.iskrembilen.quasseldroid.service.CoreConnService;

public class LoginActivity extends Activity implements Observer, DialogInterface.OnCancelListener {

	private static final String TAG = LoginActivity.class.getSimpleName();
	public static final String PREFS_ACCOUNT = "AccountPreferences";
	public static final String PREFS_CORE = "coreSelection";
	public static final String PREFS_USERNAME = "username";
	public static final String PREFS_PASSWORD = "password";
	public static final String PREFS_REMEMBERME = "rememberMe";
	
	SharedPreferences settings;
	QuasselDbHelper dbHelper;
	
	private ResultReceiver statusReceiver;

	Spinner core;
	EditText username;
	EditText password;
	CheckBox rememberMe;
	Button connect;
	
	private String hashedCert;//ugly

	/* EXample of how to get a preference
	 * SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean update = prefs.getBoolean("updatePref", false);
	 * 
	 */

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Create");

		setContentView(R.layout.login);

		settings = getSharedPreferences(PREFS_ACCOUNT, MODE_PRIVATE);
		dbHelper = new QuasselDbHelper(this);
		dbHelper.open();


		core = (Spinner)findViewById(R.id.serverSpinner);
		username = (EditText)findViewById(R.id.usernameField);
		password = (EditText)findViewById(R.id.passwordField);
		rememberMe = (CheckBox)findViewById(R.id.remember_me_checkbox);

		//setup the core spinner
		//dbHelper.addCore("testcore", "test.core.com", 8848);
		Cursor c = dbHelper.getAllCores();
		startManagingCursor(c);

		String[] from = new String[] {QuasselDbHelper.KEY_NAME};
		int[] to = new int[] {android.R.id.text1};
		SimpleCursorAdapter adapter  = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, c, from, to);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		//TODO: Ken:Implement view reuse
		core.setAdapter(adapter);

		//Use saved settings
		if(core.getCount()>settings.getInt(PREFS_CORE, 0))
			core.setSelection(settings.getInt(PREFS_CORE, 0));
		username.setText(settings.getString(PREFS_USERNAME,""));
		password.setText(settings.getString(PREFS_PASSWORD,""));
		rememberMe.setChecked(settings.getBoolean(PREFS_REMEMBERME, false));

		connect = (Button)findViewById(R.id.connect_button);
		connect.setOnClickListener(onConnect);
		
		statusReceiver = new ResultReceiver(null) {

			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				if (resultCode==CoreConnService.CONNECTION_CONNECTING) {
					removeDialog(R.id.DIALOG_CONNECTING);
					LoginActivity.this.startActivity(new Intent(LoginActivity.this, BufferActivity.class));
					finish();
				}else if (resultCode==CoreConnService.CONNECTION_DISCONNECTED) {
					if (resultData!=null){
						removeDialog(R.id.DIALOG_CONNECTING);
						Toast.makeText(LoginActivity.this, resultData.getString(CoreConnService.STATUS_KEY), Toast.LENGTH_LONG).show();
					}
				} else if (resultCode == CoreConnService.NEW_CERTIFICATE) {
					hashedCert = resultData.getString(CoreConnService.CERT_KEY);
					removeDialog(R.id.DIALOG_CONNECTING);
					showDialog(R.id.DIALOG_NEW_CERTIFICATE);
				} else if(resultCode == CoreConnService.UNSUPPORTED_PROTOCOL) {
					removeDialog(R.id.DIALOG_CONNECTING);
					Toast.makeText(LoginActivity.this, "Protocol version not supported, Quassel core is to old", Toast.LENGTH_LONG).show();
				}
				super.onReceiveResult(resultCode, resultData);
			}
			
		};
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	protected void onStart() {
		super.onStart();
		doBindService();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		doUnbindService();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dbHelper != null) {
			dbHelper.close();
			dbHelper=null;
		}

	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		return super.onPrepareOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add_core:
			showDialog(R.id.DIALOG_ADD_CORE);
			break;
		case R.id.menu_edit_core:
			if(dbHelper.hasCores()) {
				showDialog(R.id.DIALOG_EDIT_CORE);
			} else {
				Toast.makeText(this, "No cores to edit", Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.menu_delete_core:
			if(dbHelper.hasCores()) {
				dbHelper.deleteCore(core.getSelectedItemId());
				Toast.makeText(LoginActivity.this, "Deleted core", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "No cores to edit", Toast.LENGTH_LONG).show();
			}
			updateCoreSpinner();
			//TODO: mabye add some confirm dialog when deleting a core
			break;
		case R.id.menu_preferences:
			Intent i = new Intent(LoginActivity.this, PreferenceView.class);
			startActivity(i);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch(id) {
		case R.id.DIALOG_ADD_CORE:
			dialog.setTitle("Add new core");
			break;
		case R.id.DIALOG_EDIT_CORE:
			dialog.setTitle("Edit core");
			Bundle res = dbHelper.getCore(core.getSelectedItemId());
			((EditText)dialog.findViewById(R.id.dialog_name_field)).setText(res.getString(QuasselDbHelper.KEY_NAME));
			((EditText)dialog.findViewById(R.id.dialog_address_field)).setText(res.getString(QuasselDbHelper.KEY_ADDRESS));
			((EditText)dialog.findViewById(R.id.dialog_port_field)).setText(Integer.toString(res.getInt(QuasselDbHelper.KEY_PORT)));
			((CheckBox)dialog.findViewById(R.id.dialog_usessl_checkbox)).setChecked(res.getBoolean(QuasselDbHelper.KEY_SSL));
			break;
		}

		super.onPrepareDialog(id, dialog);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		final Dialog dialog;
		switch (id) {

		case R.id.DIALOG_EDIT_CORE: //fallthrough
		case R.id.DIALOG_ADD_CORE:
			dialog = new Dialog(this);
			dialog.setContentView(R.layout.dialog_add_core);
			dialog.setTitle("Add new core");

			OnClickListener buttonListener = new OnClickListener() {

				@Override
				public void onClick(View v) {
					EditText nameField = (EditText)dialog.findViewById(R.id.dialog_name_field);
					EditText addressField = (EditText)dialog.findViewById(R.id.dialog_address_field);
					EditText portField = (EditText)dialog.findViewById(R.id.dialog_port_field);
					CheckBox sslBox = (CheckBox)dialog.findViewById(R.id.dialog_usessl_checkbox);
					if (v.getId()==R.id.cancel_button) {
						nameField.setText("");
						addressField.setText("");
						portField.setText("");
						sslBox.setChecked(false);
						dialog.dismiss();


					}else if (v.getId()==R.id.save_button && !nameField.getText().toString().equals("") &&!addressField.getText().toString().equals("") && !portField.getText().toString().equals("")) {
						String name = nameField.getText().toString().trim();
						String address = addressField.getText().toString().trim();
						int port = Integer.parseInt(portField.getText().toString().trim());
						boolean useSSL = sslBox.isChecked();

						//TODO: Ken: mabye add some better check on what state the dialog is used for, edit/add. Atleast use a string from the resources so its the same if you change it.
						if ((String)dialog.getWindow().getAttributes().getTitle()=="Add new core") {
							dbHelper.addCore(name, address, port, useSSL);
						}else if ((String)dialog.getWindow().getAttributes().getTitle()=="Edit core") {
							dbHelper.updateCore(core.getSelectedItemId(), name, address, port, useSSL);
						}
						LoginActivity.this.updateCoreSpinner();
						nameField.setText("");
						addressField.setText("");
						portField.setText("");
						sslBox.setChecked(false);
						dialog.dismiss();
						if ((String)dialog.getWindow().getAttributes().getTitle()=="Add new core") {
							Toast.makeText(LoginActivity.this, "Added core", Toast.LENGTH_LONG).show();
						}else if ((String)dialog.getWindow().getAttributes().getTitle()=="Edit core") {
							Toast.makeText(LoginActivity.this, "Edited core", Toast.LENGTH_LONG).show();
						}
					}

				}
			};
			dialog.findViewById(R.id.cancel_button).setOnClickListener(buttonListener);
			dialog.findViewById(R.id.save_button).setOnClickListener(buttonListener);	
			break;
		
		case R.id.DIALOG_CONNECTING:
			ProgressDialog prog = new ProgressDialog(LoginActivity.this);
			prog.setMessage("Connecting...");
			prog.setCancelable(true);
			prog.setOnCancelListener(this);
			dialog = prog;
			break;

		case R.id.DIALOG_NEW_CERTIFICATE:
			AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
			final SharedPreferences certPrefs = getSharedPreferences("CertificateStorage", Context.MODE_PRIVATE);
			builder.setMessage("Received a new certificate, do you trust it?\n" + hashedCert)
			       .setCancelable(false)
			       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
							certPrefs.edit().putString("certificate", hashedCert).commit();
							onConnect.onClick(null);
			           }
			       })
			       .setNegativeButton("No", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           }
			       });
			dialog = builder.create();
			break;
			
		default:
			dialog = null;
			break;
		}
		return dialog;  
	}

	private OnClickListener onConnect = new OnClickListener() {
		public void onClick(View v) {
			if(username.getText().length()==0 ||
					password.getText().length()==0 ||
					core.getCount() == 0){

				AlertDialog.Builder diag=new AlertDialog.Builder(LoginActivity.this);
				diag.setMessage("Error, connection information not filled out properly");
				diag.setCancelable(false);

				AlertDialog dg = diag.create();
				dg.setOwnerActivity(LoginActivity.this);
				dg.setButton("Ok",  new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {}});
				dg.show();							
				return;
			}
			SharedPreferences.Editor settingsedit = settings.edit();
			if(rememberMe.isChecked()){//save info
				settingsedit.putInt(PREFS_CORE, core.getSelectedItemPosition());
				settingsedit.putString(PREFS_USERNAME,username.getText().toString());
				settingsedit.putString(PREFS_PASSWORD, password.getText().toString());
				settingsedit.putBoolean(PREFS_REMEMBERME, true);

			}else {
				settingsedit.putInt(PREFS_CORE, core.getSelectedItemPosition());
				settingsedit.remove(PREFS_USERNAME);
				settingsedit.remove(PREFS_PASSWORD);
				settingsedit.remove(PREFS_REMEMBERME);

			}
			settingsedit.commit();
			//dbHelper.open();
			Bundle res = dbHelper.getCore(core.getSelectedItemId());
			
			
			//TODO: quick fix for checking if we have internett before connecting, should remove some force closes, not sure if we should do it in another place tho, mabye in CoreConn
			//Check that the phone has either mobile or wifi connection to querry teh bus oracle
			ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			//0 is mobile, 1 is wifi
			if (conn.getActiveNetworkInfo() != null && !conn.getActiveNetworkInfo().isConnected()) {
				Toast.makeText(LoginActivity.this, "This application requires a internett connection", Toast.LENGTH_LONG).show();
				return;
			}
			
			showDialog(R.id.DIALOG_CONNECTING);

			//Make intent to send to the CoreConnect service, with connection data
			Intent connectIntent = new Intent(LoginActivity.this, CoreConnService.class);
			connectIntent.putExtra("name", res.getString(QuasselDbHelper.KEY_NAME));
			connectIntent.putExtra("address", res.getString(QuasselDbHelper.KEY_ADDRESS));
			connectIntent.putExtra("port", res.getInt(QuasselDbHelper.KEY_PORT));
			connectIntent.putExtra("ssl", res.getBoolean(QuasselDbHelper.KEY_SSL));
			connectIntent.putExtra("username", username.getText().toString().trim());
			connectIntent.putExtra("password", password.getText().toString());
			
			startService(connectIntent);

		}
	};

	public void updateCoreSpinner() {
		((SimpleCursorAdapter)core.getAdapter()).getCursor().requery();
	}

	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		
	}
	
	
	private CoreConnService boundConnService = null;
	private boolean isBound = false;
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. Because we have bound to a explicit
			// service that we know is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			Log.i(TAG, "BINDING ON SERVICE DONE");
			boundConnService = ((CoreConnService.LocalBinder)service).getService();
			if(boundConnService.isConnected()) {
				LoginActivity.this.startActivity(new Intent(LoginActivity.this, BufferActivity.class));
				finish();
			}
			boundConnService.registerStatusReceiver(statusReceiver);
		}

		public void onServiceDisconnected(ComponentName className) {
			boundConnService = null;
		}
	};
	
	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		bindService(new Intent(LoginActivity.this, CoreConnService.class), mConnection, Context.BIND_AUTO_CREATE);
		isBound = true;
		Log.i(TAG, "Binding Service");
	}

	void doUnbindService() {
		if (isBound) {
			Log.i(TAG, "Unbinding service");
			if (boundConnService != null)
				boundConnService.unregisterStatusReceiver(statusReceiver);
			unbindService(mConnection);
			isBound = false;
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		boundConnService.disconnectFromCore();
	}

}
