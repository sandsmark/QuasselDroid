package com.iskrembilen.quasseldroid.util;

import com.iskrembilen.quasseldroid.R;

import android.content.Context;
import android.preference.PreferenceManager;

public class ThemeUtil {
	
	public static int theme;
	
	public static void initTheme(Context context) {
		String themeName = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).getString(context.getString(R.string.preference_theme), "");
		if(themeName.equals("light")) theme = R.style.QuasseldroidThemeLight;
		else if(themeName.equals("dark")) theme = R.style.QuasseldroidThemeDark;
	}

}
