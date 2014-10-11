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
    public static int nick_owner_bg, nick_admin_bg, nick_operator_bg, nick_halfop_bg, nick_voice_bg, nick_user_bg;
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

            chatPlainResource = R.color.chat_line_plain;

            bufferPartedColor = resources.getColor(R.color.buffer_parted_color);
            bufferHighlightColor = resources.getColor(R.color.buffer_highlight_color);
            bufferUnreadColor = resources.getColor(R.color.buffer_unread_color);
            bufferActivityColor = resources.getColor(R.color.buffer_activity_color);
            bufferReadColor = resources.getColor(R.color.buffer_read_color);

            chatPlainColor = resources.getColor(R.color.chat_line_plain);
            chatActionColor = resources.getColor(R.color.chat_line_action);
            chatErrorColor = resources.getColor(R.color.chat_line_error);
            chatHighlightColor = resources.getColor(R.color.chat_line_highlight);
            chatTimestampColor = resources.getColor(R.color.chat_line_timestamp);

            chatActionBg = resources.getColor(R.color.chat_bg_action);

            nick_owner = resources.getColor(R.color.nick_owner_color);
            nick_admin = resources.getColor(R.color.nick_admin_color);
            nick_operator = resources.getColor(R.color.nick_operator_color);
            nick_halfop = resources.getColor(R.color.nick_halfop_color);
            nick_voice = resources.getColor(R.color.nick_voice_color);
            nick_user = resources.getColor(R.color.nick_user_color);

            nick_owner_bg = resources.getColor(R.color.nick_owner_bg);
            nick_admin_bg = resources.getColor(R.color.nick_admin_bg);
            nick_operator_bg = resources.getColor(R.color.nick_operator_bg);
            nick_halfop_bg = resources.getColor(R.color.nick_halfop_bg);
            nick_voice_bg = resources.getColor(R.color.nick_voice_bg);
            nick_user_bg = resources.getColor(R.color.nick_user_bg);

            drawable_buffer_active = resources.getDrawable(R.drawable.buffer_bg_active);
            drawable_buffer_away = resources.getDrawable(R.drawable.buffer_bg_away);
            drawable_buffer_gone = resources.getDrawable(R.drawable.buffer_bg_gone);
            drawable_bg_list_item = resources.getDrawable(R.drawable.bg_list_item);
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

    public static final int getNickBg(IrcMode mode) {
        switch (mode) {
            case OWNER:
                return nick_owner_bg;
            case ADMIN:
                return nick_admin_bg;
            case OPERATOR:
                return nick_operator_bg;
            case HALF_OPERATOR:
                return nick_halfop_bg;
            case VOICE:
                return nick_voice_bg;
            case USER:
                return nick_user_bg;
            default:
                return 0;
        }
    }
}
