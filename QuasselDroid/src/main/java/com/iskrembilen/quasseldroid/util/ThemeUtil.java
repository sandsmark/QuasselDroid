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
    public static int chatPlainColor, chatActionColor, chatErrorColor, chatHighlightColor, chatTimestampColor, chatActionBg;
    public static int chatPlainResource;
    public static int bufferPartedColor, bufferHighlightColor, bufferUnreadColor, bufferActivityColor, bufferReadColor;
    public static int def_color;
    public static int[] nick_bgs;
    public static int[] nick_colors;
    public static double[] nick_constants;
    public static Drawable drawable_buffer_away, drawable_buffer_active, drawable_buffer_gone, drawable_buffer_hidden_temp, drawable_buffer_hidden_perm;

    public static void initTheme(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String themeName = preferences.getString(context.getString(R.string.preference_theme), "");

        setTheme(context, themeName);
    }

    public static void setTheme(Context context, String themeName) {
        Resources resources = context.getResources();

        nick_colors = new int[] {
                resources.getColor(R.color.nick_owner_color),
                resources.getColor(R.color.nick_admin_color),
                resources.getColor(R.color.nick_operator_color),
                resources.getColor(R.color.nick_halfop_color),
                resources.getColor(R.color.nick_voice_color),
                resources.getColor(R.color.nick_user_color)
        };
        def_color = resources.getColor(R.color.nick_user_color);

        if (themeName.equals("light")) {
            theme = R.style.Theme_Quasseldroid_Light;

            chatPlainColor = resources.getColor(R.color.chat_line_plain_light);
            chatErrorColor = resources.getColor(R.color.chat_line_error_light);
            chatActionColor = resources.getColor(R.color.chat_line_action_light);
            chatTimestampColor = resources.getColor(R.color.chat_line_timestamp_light);
            chatHighlightColor = resources.getColor(R.color.chat_line_highlight_light);

            chatPlainResource = R.color.chat_line_plain_light;

            chatActionBg = resources.getColor(R.color.chat_bg_action_light);

            bufferReadColor = resources.getColor(R.color.buffer_read_color_light);
            bufferPartedColor = resources.getColor(R.color.buffer_parted_color_light);
            bufferHighlightColor = resources.getColor(R.color.buffer_highlight_color_light);
            bufferUnreadColor = resources.getColor(R.color.buffer_unread_color_light);
            bufferActivityColor = resources.getColor(R.color.buffer_activity_color_light);

            drawable_buffer_hidden_perm = resources.getDrawable(R.drawable.widget_buffer_hidden_perm_light);
            drawable_buffer_hidden_temp = resources.getDrawable(R.drawable.widget_buffer_hidden_temp_light);
            drawable_buffer_active = resources.getDrawable(R.drawable.widget_buffer_active_light);
            drawable_buffer_away = resources.getDrawable(R.drawable.widget_buffer_away_light);
            drawable_buffer_gone = resources.getDrawable(R.drawable.widget_buffer_gone_light);

            nick_bgs = new int[] {
                    resources.getColor(R.color.nick_owner_light),
                    resources.getColor(R.color.nick_admin_light),
                    resources.getColor(R.color.nick_operator_light),
                    resources.getColor(R.color.nick_halfop_light),
                    resources.getColor(R.color.nick_voice_light),
                    resources.getColor(R.color.nick_user_light)
            };

            nick_constants = new double[] {0.7, 0.5};
        } else if (themeName.equals("dark")) {
            theme = R.style.Theme_Quasseldroid_Dark;

            chatPlainColor = resources.getColor(R.color.chat_line_plain_dark);
            chatErrorColor = resources.getColor(R.color.chat_line_error_dark);
            chatActionColor = resources.getColor(R.color.chat_line_action_dark);
            chatTimestampColor = resources.getColor(R.color.chat_line_timestamp_dark);
            chatHighlightColor = resources.getColor(R.color.chat_line_highlight_dark);

            chatPlainResource = R.color.chat_line_plain_dark;

            chatActionBg = resources.getColor(R.color.chat_bg_action_dark);

            bufferReadColor = resources.getColor(R.color.buffer_read_color_dark);
            bufferPartedColor = resources.getColor(R.color.buffer_parted_color_dark);
            bufferHighlightColor = resources.getColor(R.color.buffer_highlight_color_dark);
            bufferUnreadColor = resources.getColor(R.color.buffer_unread_color_dark);
            bufferActivityColor = resources.getColor(R.color.buffer_activity_color_dark);

            drawable_buffer_hidden_perm = resources.getDrawable(R.drawable.widget_buffer_hidden_perm_dark);
            drawable_buffer_hidden_temp = resources.getDrawable(R.drawable.widget_buffer_hidden_temp_dark);
            drawable_buffer_active = resources.getDrawable(R.drawable.widget_buffer_active_dark);
            drawable_buffer_away = resources.getDrawable(R.drawable.widget_buffer_away_dark);
            drawable_buffer_gone = resources.getDrawable(R.drawable.widget_buffer_gone_dark);

            nick_bgs = new int[] {
                    resources.getColor(R.color.nick_owner_dark),
                    resources.getColor(R.color.nick_admin_dark),
                    resources.getColor(R.color.nick_operator_dark),
                    resources.getColor(R.color.nick_halfop_dark),
                    resources.getColor(R.color.nick_voice_dark),
                    resources.getColor(R.color.nick_user_dark)
            };

            nick_constants = new double[] {1.0, 0.8};
        } else {
            setTheme(context, "light");
        }
    }

    public static final int getNickColor(IrcMode mode) {
        switch (mode) {
            case OWNER:
                return nick_colors[0];
            case ADMIN:
                return nick_colors[1];
            case OPERATOR:
                return nick_colors[2];
            case HALF_OPERATOR:
                return nick_colors[3];
            case VOICE:
                return nick_colors[4];
            case USER:
                return nick_colors[5];
            default:
                return def_color;
        }
    }

    public static final int getNickBg(IrcMode mode) {
        switch (mode) {
            case OWNER:
                return nick_bgs[0];
            case ADMIN:
                return nick_bgs[1];
            case OPERATOR:
                return nick_bgs[2];
            case HALF_OPERATOR:
                return nick_bgs[3];
            case VOICE:
                return nick_bgs[4];
            case USER:
                return nick_bgs[5];
            default:
                return def_color;
        }
    }
}
