package com.iskrembilen.quasseldroid.util;

import android.R.color;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import com.iskrembilen.quasseldroid.R;

public class ThemeUtil {

	public static int theme;
	public static int bufferPartedColor, bufferHighlightColor, bufferUnreadColor, bufferActivityColor, bufferReadColor;
	public static int chatPlainColor, chatNoticeColor, chatActionColor, chatNickColor, chatModeColor, chatJoinColor, chatPartColor, chatQuitColor, chatKickColor, chatKillColor, chatServerColor, chatInfoColor, chatErrorColor, chatDayChangeColor, chatTopicColor, chatNetsplitQuitColor, chatNetsplitJoinColor, chatHighlightColor, chatSelfColor, chatTimestampColor;
	public static int chatPlainResource;

	public static void initTheme(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		String themeName = preferences.getString(context.getString(R.string.preference_theme), "");
		setTheme(context, themeName);
	}

	public static void setTheme(Context context, String themeName) {
		Resources resources = context.getResources();
		if(themeName.equals("light")) {
			theme = R.style.QuasseldroidThemeLight;

			chatPlainResource = R.color.chat_line_plain_light;
			
			bufferPartedColor = resources.getColor(R.color.buffer_parted_color_light);
			bufferHighlightColor = resources.getColor(R.color.buffer_highlight_color_light);
			bufferUnreadColor = resources.getColor(R.color.buffer_unread_color_light);
			bufferActivityColor = resources.getColor(R.color.buffer_activity_color_light);
			bufferReadColor = resources.getColor(R.color.buffer_read_color_light);

			chatSelfColor = resources.getColor(R.color.chat_line_self_light);
			chatPlainColor = resources.getColor(R.color.chat_line_plain_light);
			chatNoticeColor = resources.getColor(R.color.chat_line_notice_light);
			chatActionColor = resources.getColor(R.color.chat_line_action_light);
			chatNickColor = resources.getColor(R.color.chat_line_nick_light);
			chatModeColor = resources.getColor(R.color.chat_line_mode_light);
			chatJoinColor = resources.getColor(R.color.chat_line_join_light);
			chatPartColor = resources.getColor(R.color.chat_line_part_light);
			chatQuitColor = resources.getColor(R.color.chat_line_quit_light);
			chatKickColor = resources.getColor(R.color.chat_line_kick_light);
			chatKillColor = resources.getColor(R.color.chat_line_kill_light);
			chatServerColor = resources.getColor(R.color.chat_line_server_light);
			chatInfoColor = resources.getColor(R.color.chat_line_info_light);
			chatErrorColor = resources.getColor(R.color.chat_line_error_light);
			chatDayChangeColor = resources.getColor(R.color.chat_line_daychange_light);
			chatTopicColor = resources.getColor(R.color.chat_line_topic_light);
			chatNetsplitJoinColor = resources.getColor(R.color.chat_line_netsplitjoin_light);
			chatNetsplitQuitColor = resources.getColor(R.color.chat_line_netsplitquit_light);
			chatHighlightColor = resources.getColor(R.color.chat_line_highlight_light);
			chatTimestampColor = resources.getColor(R.color.chat_line_timestamp_light);

		}
		else if(themeName.equals("dark")) {
			theme = R.style.QuasseldroidThemeDark;
			
			chatPlainResource = R.color.chat_line_plain_dark;

			bufferPartedColor = resources.getColor(R.color.buffer_parted_color_dark);
			bufferHighlightColor = resources.getColor(R.color.buffer_highlight_color_dark);
			bufferUnreadColor = resources.getColor(R.color.buffer_unread_color_dark);
			bufferActivityColor = resources.getColor(R.color.buffer_activity_color_dark);
			bufferReadColor = resources.getColor(R.color.buffer_read_color_dark);

			chatSelfColor = resources.getColor(R.color.chat_line_self_dark);
			chatPlainColor = resources.getColor(R.color.chat_line_plain_dark);
			chatNoticeColor = resources.getColor(R.color.chat_line_notice_dark);
			chatActionColor = resources.getColor(R.color.chat_line_action_dark);
			chatNickColor = resources.getColor(R.color.chat_line_nick_dark);
			chatModeColor = resources.getColor(R.color.chat_line_mode_dark);
			chatJoinColor = resources.getColor(R.color.chat_line_join_dark);
			chatPartColor = resources.getColor(R.color.chat_line_part_dark);
			chatQuitColor = resources.getColor(R.color.chat_line_quit_dark);
			chatKickColor = resources.getColor(R.color.chat_line_kick_dark);
			chatKillColor = resources.getColor(R.color.chat_line_kill_dark);
			chatServerColor = resources.getColor(R.color.chat_line_server_dark);
			chatInfoColor = resources.getColor(R.color.chat_line_info_dark);
			chatErrorColor = resources.getColor(R.color.chat_line_error_dark);
			chatDayChangeColor = resources.getColor(R.color.chat_line_daychange_dark);
			chatTopicColor = resources.getColor(R.color.chat_line_topic_dark);
			chatNetsplitJoinColor = resources.getColor(R.color.chat_line_netsplitjoin_dark);
			chatNetsplitQuitColor = resources.getColor(R.color.chat_line_netsplitquit_dark);
			chatHighlightColor = resources.getColor(R.color.chat_line_highlight_dark);
			chatTimestampColor = resources.getColor(R.color.chat_line_timestamp_dark);
		} else {
			setTheme(context, "light");
		}
	} 
}
