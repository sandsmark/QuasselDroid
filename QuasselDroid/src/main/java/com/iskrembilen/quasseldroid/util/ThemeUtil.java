package com.iskrembilen.quasseldroid.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.iskrembilen.quasseldroid.R;

public class ThemeUtil {

    public static int theme;
    public static int bufferPartedColor, bufferHighlightColor, bufferUnreadColor, bufferActivityColor, bufferReadColor;
    public static int chatPlainColor, chatNoticeColor, chatActionColor, chatNickColor, chatModeColor, chatJoinColor, chatPartColor, chatQuitColor, chatKickColor, chatKillColor, chatServerColor, chatInfoColor, chatErrorColor, chatDayChangeColor, chatTopicColor, chatNetsplitQuitColor, chatNetsplitJoinColor, chatHighlightColor, chatSelfColor, chatTimestampColor, chatActionBg;
    public static int chatPlainResource;

    public static void initTheme(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        loadTheme(context);
    }

    public static void loadTheme(Context context) {
        Resources resources = context.getResources();

        theme = R.style.Theme_Quasseldroid;

        chatPlainResource = R.color.chat_line_plain;

        bufferPartedColor = resources.getColor(R.color.buffer_parted_color);
        bufferHighlightColor = resources.getColor(R.color.buffer_highlight_color);
        bufferUnreadColor = resources.getColor(R.color.buffer_unread_color);
        bufferActivityColor = resources.getColor(R.color.buffer_activity_color);
        bufferReadColor = resources.getColor(R.color.buffer_read_color);

        chatSelfColor = resources.getColor(R.color.chat_line_self);
        chatPlainColor = resources.getColor(R.color.chat_line_plain);
        chatNoticeColor = resources.getColor(R.color.chat_line_notice);
        chatActionColor = resources.getColor(R.color.chat_line_action);
        chatNickColor = resources.getColor(R.color.chat_line_nick);
        chatModeColor = resources.getColor(R.color.chat_line_mode);
        chatJoinColor = resources.getColor(R.color.chat_line_join);
        chatPartColor = resources.getColor(R.color.chat_line_part);
        chatQuitColor = resources.getColor(R.color.chat_line_quit);
        chatKickColor = resources.getColor(R.color.chat_line_kick);
        chatKillColor = resources.getColor(R.color.chat_line_kill);
        chatServerColor = resources.getColor(R.color.chat_line_server);
        chatInfoColor = resources.getColor(R.color.chat_line_info);
        chatErrorColor = resources.getColor(R.color.chat_line_error);
        chatDayChangeColor = resources.getColor(R.color.chat_line_daychange);
        chatTopicColor = resources.getColor(R.color.chat_line_topic);
        chatNetsplitJoinColor = resources.getColor(R.color.chat_line_netsplitjoin);
        chatNetsplitQuitColor = resources.getColor(R.color.chat_line_netsplitquit);
        chatHighlightColor = resources.getColor(R.color.chat_line_highlight);
        chatTimestampColor = resources.getColor(R.color.chat_line_timestamp);

        chatActionBg = resources.getColor(R.color.chat_bg_action);
    }

    public static final String colorToHex(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }
}
