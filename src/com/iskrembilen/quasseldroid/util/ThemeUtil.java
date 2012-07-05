package com.iskrembilen.quasseldroid.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.iskrembilen.quasseldroid.R;

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
