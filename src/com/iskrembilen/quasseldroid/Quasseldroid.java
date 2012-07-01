package com.iskrembilen.quasseldroid;

import android.app.Application;
import android.preference.PreferenceManager;

public class Quasseldroid extends Application{
	
	@Override
	public void onCreate() {
		super.onCreate();
		PreferenceManager.setDefaultValues(this, R.layout.preferences, false);
	}

}
