package com.lekebilen.quasseldroid.gui;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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

import com.lekebilen.quasseldroid.CoreConnection;
import com.lekebilen.quasseldroid.R;

public class LoginActivity extends Activity{

	public static final String TAG = "QuasselLogin";
	public static final String PREFS_ACCOUNT = "AccountPreferences";
	public static final String PREFS_CORE = "coreSelection";
	public static final String PREFS_USERNAME = "username";
	public static final String PREFS_PASSWORD = "password";
	public static final String PREFS_REMEMBERME = "rememberMe";
	private final int DIALOG_ADD_CORE = 0;
	SharedPreferences settings;
	QuasselDbHelper dbHelper;
	
	Spinner core;
	EditText username;
	EditText password;
	CheckBox rememberMe;
	Button connect;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Create");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);
		

		settings = getSharedPreferences(PREFS_ACCOUNT, MODE_PRIVATE);
		dbHelper = new QuasselDbHelper(this);
		dbHelper.open();
		

		core = (Spinner)findViewById(R.id.serverSpinner);
		username = (EditText)findViewById(R.id.usernameField);
		password = (EditText)findViewById(R.id.passwordField);
		rememberMe = (CheckBox)findViewById(R.id.remember_me_checkbox);

		core.setSelection(settings.getInt(PREFS_CORE, 0));
		username.setText(settings.getString(PREFS_USERNAME,""));
		password.setText(settings.getString(PREFS_PASSWORD,""));
		rememberMe.setChecked(settings.getBoolean(PREFS_REMEMBERME, false));

		connect = (Button)findViewById(R.id.connect_button);
		connect.setOnClickListener(onConnect);
		
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
			
		//Not sure if this is good design so commented out for now
		/*if(rememberMe.isChecked()){
        	ScrollView sw=((ScrollView)findViewById(R.id.accountScroll));//scroll to bottom (connect button)
        	sw.scrollTo(0, sw.getHeight());
        }*/
		
		//Start connection service
		//startService(new Intent(LoginActivity.this, ServerService.class));
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add_core:
			showDialog(DIALOG_ADD_CORE);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		final Dialog dialog;
		switch (id) {
		case DIALOG_ADD_CORE:
			Log.i("Ken", "Creating dialog");
			//TODO:Ken:Add dialog
			dialog = new Dialog(this);
			dialog.setContentView(R.layout.dialog_add_core);
			dialog.setTitle("Add new core");
			
			OnClickListener buttonListener = new OnClickListener() {

				@Override
				public void onClick(View v) {
					EditText nameField = (EditText)dialog.findViewById(R.id.dialog_name_field);
					EditText addressField = (EditText)dialog.findViewById(R.id.dialog_address_field);
					EditText portField = (EditText)dialog.findViewById(R.id.dialog_port_field);
					//Log.i("KEN", Log.i("KEN", String.valueOf()!portField.equals("")));
					if (v.getId()==R.id.cancel_button) {
						nameField.setText("");
						addressField.setText("");
						portField.setText("");
						dialog.dismiss();
						
						
					}else if (v.getId()==R.id.save_button && !nameField.getText().toString().equals("") &&!addressField.getText().toString().equals("") && !portField.getText().toString().equals("")) {
						Log.i("KEN", "Saving");
						String name = nameField.getText().toString();
						String address = addressField.getText().toString();
						int port = Integer.parseInt(portField.getText().toString());
						dbHelper.addCore(name, address, port);
						LoginActivity.this.updateCoreSpinner();
						nameField.setText("");
						addressField.setText("");
						portField.setText("");
						dialog.dismiss();
						Toast.makeText(LoginActivity.this, "Added favorite", Toast.LENGTH_LONG).show();
					}
					
				}
			};
			dialog.findViewById(R.id.cancel_button).setOnClickListener(buttonListener);
			dialog.findViewById(R.id.save_button).setOnClickListener(buttonListener);	
			break;
		
		default:
			dialog = null;
			break;
		}
		return dialog;
	}
	
	private OnClickListener onConnect = new OnClickListener() {
		public void onClick(View v) {
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
        	Bundle res = dbHelper.getCore(core.getSelectedItemId());
			HashMap<String,String> paramMap=new HashMap<String, String>();
        	paramMap.put("username",(username.getText().toString()));
        	paramMap.put("password",password.getText().toString());
        	paramMap.put("serverHost",res.getString("address"));
        	paramMap.put("serverPort", String.valueOf(res.getInt("port")));

        	Uri.Builder dataUri=new Uri.Builder();
        	dataUri.scheme("data");
        	dataUri.path("");
        	dataUri.authority("");
        	dataUri.fragment("");
        	dataUri.query(encodeMap(paramMap));
      	
        	dbHelper.close();
        	
        	try {
				CoreConnection conn = new CoreConnection(res.getString("address"), 4242, username.getText().toString(), password.getText().toString(), LoginActivity.this);
			} catch (UnknownHostException e) {
				// Show the user a message about host not found
				e.printStackTrace();
			} catch (IOException e) {
				// Network trouble?
				e.printStackTrace();
			} catch (GeneralSecurityException e) {
				// SSL not enabled?
				e.printStackTrace();
			}
        	
			
		}
	};
	public static String encodeMap(Map<String,String> map){
		StringBuilder ret=new StringBuilder();
		for(String key:map.keySet()){
			ret.append(Uri.encode(key));
			ret.append("=");
			ret.append(Uri.encode(map.get(key)));
			ret.append("&");
		}
		if(ret.length()>0)
			return ret.substring(0, ret.length()-1);
		else
			return "";
	}
	
	public void updateCoreSpinner() {
		((SimpleCursorAdapter)core.getAdapter()).getCursor().requery();
	}

	private boolean trust;
	public boolean trustCertificate(byte [] certificate) {
		if (dbHelper.hasCertificate(certificate))
			return true;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Do you want to trust this certificate?:\n" + md5(certificate))
			       .setCancelable(false)
			       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                System.out.println("cake");
			                LoginActivity.this.trust = true;
			           }
			       })
			       .setNegativeButton("No", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                LoginActivity.this.trust = false;
			                dialog.cancel();
			           }
			       });
		if (trust) {
			dbHelper.storeCertificate(certificate);
			return true;
		} else {
			return false;
		}
		
	}
	
	public String md5(byte [] s) {
	    try {
	        // Create MD5 Hash
	        MessageDigest digest = java.security.MessageDigest.getInstance("SHA1");
	        digest.update(s);
	        byte messageDigest[] = digest.digest();
	        
	        // Create Hex String
	        StringBuffer hexString = new StringBuffer();
	        for (int i=0; i<messageDigest.length; i++)
	            hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
	        return hexString.toString();
	        
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    return "";
	}

}