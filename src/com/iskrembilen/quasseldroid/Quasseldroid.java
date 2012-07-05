package com.iskrembilen.quasseldroid;

import android.app.Application;
import android.preference.PreferenceManager;
import com.iskrembilen.quasseldroid.util.ThemeUtil;

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
