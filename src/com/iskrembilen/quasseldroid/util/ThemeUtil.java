package com.iskrembilen.quasseldroid.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import com.iskrembilen.quasseldroid.R;

public class ThemeUtil {

	public static int theme;
	public static int bufferPartedColor, bufferHighlightColor, bufferUnreadColor, bufferActivityColor, bufferReadColor;
	public static int messageHighlightColor, messageNormalColor, messageCommandColor, messageServerColor, messageActionColor, messageSelfColor;

	public static void initTheme(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		String themeName = preferences.getString(context.getString(R.string.preference_theme), "");
		setTheme(context, themeName);
	}

	public static void setTheme(Context context, String themeName) {
		Resources resources = context.getResources();
		if(themeName.equals("light")) {
			theme = R.style.QuasseldroidThemeLight;

			bufferPartedColor = resources.getColor(R.color.buffer_parted_color_light);
			bufferHighlightColor = resources.getColor(R.color.buffer_highlight_color_light);
			bufferUnreadColor = resources.getColor(R.color.buffer_unread_color_light);
			bufferActivityColor = resources.getColor(R.color.buffer_activity_color_light);
			bufferReadColor = resources.getColor(R.color.buffer_read_color_light);

			messageHighlightColor = resources.getColor(R.color.ircmessage_highlight_color);
			messageNormalColor = resources.getColor(R.color.ircmessage_normal_color_light);
			messageCommandColor = resources.getColor(R.color.ircmessage_commandmessages_color);
			messageServerColor = resources.getColor(R.color.ircmessage_servermessage_color);
			messageActionColor = resources.getColor(R.color.ircmessage_actionmessage_color);
			messageSelfColor = resources.getColor(R.color.ircmessage_self_color_light);

		}
		else if(themeName.equals("dark")) {
			theme = R.style.QuasseldroidThemeDark;

			bufferPartedColor = resources.getColor(R.color.buffer_parted_color_dark);
			bufferHighlightColor = resources.getColor(R.color.buffer_highlight_color_dark);
			bufferUnreadColor = resources.getColor(R.color.buffer_unread_color_dark);
			bufferActivityColor = resources.getColor(R.color.buffer_activity_color_dark);
			bufferReadColor = resources.getColor(R.color.buffer_read_color_dark);

			messageHighlightColor = resources.getColor(R.color.ircmessage_highlight_color);
			messageNormalColor = resources.getColor(R.color.ircmessage_normal_color_dark);
			messageCommandColor = resources.getColor(R.color.ircmessage_commandmessages_color);
			messageServerColor = resources.getColor(R.color.ircmessage_servermessage_color);
			messageActionColor = resources.getColor(R.color.ircmessage_actionmessage_color);
			messageSelfColor = resources.getColor(R.color.ircmessage_self_color_dark);
		}
	}
}
