package com.iskrembilen.quasseldroid;

import com.iskrembilen.quasseldroid.events.ThemeChangedEvent;
import com.iskrembilen.quasseldroid.util.ThemeUtil;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class Quasseldroid extends Application{

	@Override
	public void onCreate() {
		super.onCreate();

		//Populate the preferences with default vaules if this has not been done before
		PreferenceManager.setDefaultValues(this, R.layout.preferences, false);
		//Load current theme
		ThemeUtil.initTheme(this);
	}
}
