package com.iskrembilen.quasseldroid.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;

import com.iskrembilen.quasseldroid.IrcMode;
import com.iskrembilen.quasseldroid.R;

public class ThemeUtil {

    public static int theme;
    public static int chatPlainResource, chatPlainColor, chatActionColor, chatErrorColor, chatHighlightColor, chatTimestampColor, chatActionBg;
    public static int bufferPartedColor, bufferHighlightColor, bufferUnreadColor, bufferActivityColor, bufferReadColor;
    public static int nick_owner, nick_admin, nick_operator, nick_halfop, nick_voice, nick_user;
    public static Drawable drawable_buffer_away, drawable_buffer_active, drawable_buffer_gone, drawable_bg_list_item;

    public static void initTheme(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String themeName = preferences.getString(context.getString(R.string.preference_theme), "");
        setTheme(context, themeName);
    }

    public static void setTheme(Context context, String themeName) {
        Resources resources = context.getResources();
        if (themeName.equals("light")) {
            theme = R.style.Theme_Quasseldroid_Light;

            chatPlainResource = R.color.chat_line_plain_light;

            bufferPartedColor = resources.getColor(R.color.buffer_parted_color_light);
            bufferHighlightColor = resources.getColor(R.color.buffer_highlight_color_light);
            bufferUnreadColor = resources.getColor(R.color.buffer_unread_color_light);
            bufferActivityColor = resources.getColor(R.color.buffer_activity_color_light);
            bufferReadColor = resources.getColor(R.color.buffer_read_color_light);

            chatPlainColor = resources.getColor(R.color.chat_line_plain_light);
            chatActionColor = resources.getColor(R.color.chat_line_action_light);
            chatErrorColor = resources.getColor(R.color.chat_line_error_light);
            chatHighlightColor = resources.getColor(R.color.chat_line_highlight_light);
            chatTimestampColor = resources.getColor(R.color.chat_line_timestamp_light);

            chatActionBg = resources.getColor(R.color.chat_bg_action_light);

            nick_owner = resources.getColor(R.color.nick_owner_color_light);
            nick_admin = resources.getColor(R.color.nick_admin_color_light);
            nick_operator = resources.getColor(R.color.nick_operator_color_light);
            nick_halfop = resources.getColor(R.color.nick_halfop_color_light);
            nick_voice = resources.getColor(R.color.nick_voice_color_light);
            nick_user = resources.getColor(R.color.nick_user_color_light);

            drawable_buffer_active = resources.getDrawable(R.drawable.buffer_bg_active_light);
            drawable_buffer_away = resources.getDrawable(R.drawable.buffer_bg_away_light);
            drawable_buffer_gone = resources.getDrawable(R.drawable.buffer_bg_gone_light);
            drawable_bg_list_item = resources.getDrawable(R.drawable.bg_list_item_light);
        } else if (themeName.equals("dark")) {
            theme = R.style.Theme_Quasseldroid;

            chatPlainResource = R.color.chat_line_plain_dark;

            bufferPartedColor = resources.getColor(R.color.buffer_parted_color_dark);
            bufferHighlightColor = resources.getColor(R.color.buffer_highlight_color_dark);
            bufferUnreadColor = resources.getColor(R.color.buffer_unread_color_dark);
            bufferActivityColor = resources.getColor(R.color.buffer_activity_color_dark);
            bufferReadColor = resources.getColor(R.color.buffer_read_color_dark);

            chatPlainColor = resources.getColor(R.color.chat_line_plain_dark);
            chatActionColor = resources.getColor(R.color.chat_line_action_dark);
            chatErrorColor = resources.getColor(R.color.chat_line_error_dark);
            chatHighlightColor = resources.getColor(R.color.chat_line_highlight_dark);
            chatTimestampColor = resources.getColor(R.color.chat_line_timestamp_dark);

            nick_owner = resources.getColor(R.color.nick_owner_color_dark);
            nick_admin = resources.getColor(R.color.nick_admin_color_dark);
            nick_operator = resources.getColor(R.color.nick_operator_color_dark);
            nick_halfop = resources.getColor(R.color.nick_halfop_color_dark);
            nick_voice = resources.getColor(R.color.nick_voice_color_dark);
            nick_user = resources.getColor(R.color.nick_user_color_dark);

            chatActionBg = resources.getColor(R.color.chat_bg_action_dark);

            drawable_buffer_active = resources.getDrawable(R.drawable.buffer_bg_active_dark);
            drawable_buffer_away = resources.getDrawable(R.drawable.buffer_bg_away_dark);
            drawable_buffer_gone = resources.getDrawable(R.drawable.buffer_bg_gone_dark);
            drawable_bg_list_item = resources.getDrawable(R.drawable.bg_list_item_dark);
        } else {
            setTheme(context, "light");
        }
    }

    public static final int getNickColor(IrcMode mode) {
        switch (mode) {
            case OWNER:
                return nick_owner;
            case ADMIN:
                return nick_admin;
            case OPERATOR:
                return nick_operator;
            case HALF_OPERATOR:
                return nick_halfop;
            case VOICE:
                return nick_voice;
            case USER:
                return nick_user;
            default:
                return 0;
        }
    }
}
