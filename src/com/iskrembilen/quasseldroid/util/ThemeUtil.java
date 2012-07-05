package com.iskrembilen.quasseldroid.util;

import com.iskrembilen.quasseldroid.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class ThemeUtil {
	
	public static int theme;
	
	public static void initTheme(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		String themeName = preferences.getString(context.getString(R.string.preference_theme), "");
		setTheme(themeName);
	}

	public static void setTheme(String themeName) {
		if(themeName.equals("light")) theme = R.style.QuasseldroidThemeLight;
		else if(themeName.equals("dark")) theme = R.style.QuasseldroidThemeDark;
	}
}
