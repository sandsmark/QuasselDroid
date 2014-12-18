package com.iskrembilen.quasseldroid.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;

import com.iskrembilen.quasseldroid.IrcMode;
import com.iskrembilen.quasseldroid.R;

public class ThemeUtil {

    public static int theme, theme_noactionbar;
    public static int[] nick_bgs;
    public static int[] nick_colors;
    public static double[] nick_constants;

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
        color.def_color = resources.getColor(R.color.nick_user_color);

        if (themeName.equals("light")) {
            theme = R.style.Theme_QuasselDroid_Material_Light;
            theme_noactionbar = R.style.Theme_QuasselDroid_Material_Light_NoActionBar;

            color.chatPlain = resources.getColor(R.color.chat_line_plain_light);
            color.chatError = resources.getColor(R.color.chat_line_error_light);
            color.chatAction = resources.getColor(R.color.chat_line_action_light);
            color.chatTimestamp = resources.getColor(R.color.chat_line_timestamp_light);
            color.chatHighlight = resources.getColor(R.color.chat_line_highlight_light);

            color.chatActionBg = resources.getColor(R.color.chat_bg_action_light);
            color.chatPlainBg = R.color.chat_line_plain_light;

            color.bufferRead = resources.getColor(R.color.buffer_read_color_light);
            color.bufferParted = resources.getColor(R.color.buffer_parted_color_light);
            color.bufferHighlight = resources.getColor(R.color.buffer_highlight_color_light);
            color.bufferUnread = resources.getColor(R.color.buffer_unread_color_light);
            color.bufferActivity = resources.getColor(R.color.buffer_activity_color_light);

            color.bufferStateTemp = resources.getColor(R.color.buffer_status_temp_light);
            color.bufferStatePerm = resources.getColor(R.color.buffer_status_perm_light);
            color.bufferStateActive = resources.getColor(R.color.buffer_status_active_light);
            color.bufferStateAway = resources.getColor(R.color.buffer_status_away_light);
            color.bufferStateParted = resources.getColor(R.color.buffer_status_parted_light);

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
            theme = R.style.Theme_QuasselDroid_Material_Dark;
            theme_noactionbar = R.style.Theme_QuasselDroid_Material_Dark_NoActionBar;

            color.chatPlain = resources.getColor(R.color.chat_line_plain_dark);
            color.chatError = resources.getColor(R.color.chat_line_error_dark);
            color.chatAction = resources.getColor(R.color.chat_line_action_dark);
            color.chatTimestamp = resources.getColor(R.color.chat_line_timestamp_dark);
            color.chatHighlight = resources.getColor(R.color.chat_line_highlight_dark);

            color.chatActionBg = resources.getColor(R.color.chat_bg_action_dark);
            color.chatPlainBg = R.color.chat_line_plain_dark;

            color.bufferRead = resources.getColor(R.color.buffer_read_color_dark);
            color.bufferParted = resources.getColor(R.color.buffer_parted_color_dark);
            color.bufferHighlight = resources.getColor(R.color.buffer_highlight_color_dark);
            color.bufferUnread = resources.getColor(R.color.buffer_unread_color_dark);
            color.bufferActivity = resources.getColor(R.color.buffer_activity_color_dark);

            color.bufferStateTemp = resources.getColor(R.color.buffer_status_temp_dark);
            color.bufferStatePerm = resources.getColor(R.color.buffer_status_perm_dark);
            color.bufferStateActive = resources.getColor(R.color.buffer_status_active_dark);
            color.bufferStateAway = resources.getColor(R.color.buffer_status_away_dark);
            color.bufferStateParted = resources.getColor(R.color.buffer_status_parted_dark);

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

    public static class color {
        public static int
                chatPlain,
                chatAction,
                chatError,
                chatHighlight,
                chatTimestamp,
                chatActionBg,
                chatPlainBg;

        public static int
                bufferParted,
                bufferHighlight,
                bufferUnread,
                bufferActivity,
                bufferRead;

        public static int
                bufferStateTemp,
                bufferStatePerm,
                bufferStateActive,
                bufferStateAway,
                bufferStateParted;

        public static int def_color;
    }

    public static int getNickColor(IrcMode mode) {
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
                return color.def_color;
        }
    }

    public static int getNickBg(IrcMode mode) {
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
                return color.def_color;
        }
    }
}
