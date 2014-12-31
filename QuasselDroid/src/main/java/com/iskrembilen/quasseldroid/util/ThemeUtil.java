package com.iskrembilen.quasseldroid.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.SparseIntArray;

import com.iskrembilen.quasseldroid.IrcMode;
import com.iskrembilen.quasseldroid.R;

public class ThemeUtil {

    public static int theme, themeNoActionBar;
    public static int[] nickBackgrounds;
    public static int[] nickColors;
    public static double[] nickConstants;
    public static SparseIntArray messageColor = new SparseIntArray();

    public static void initTheme(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String themeName = preferences.getString(context.getString(R.string.preference_theme), "");

        setTheme(context, themeName);
    }

    public static void setTheme(Context context, String themeName) {
        Resources resources = context.getResources();

        nickColors = new int[] {
                resources.getColor(R.color.nick_owner_color),
                resources.getColor(R.color.nick_admin_color),
                resources.getColor(R.color.nick_operator_color),
                resources.getColor(R.color.nick_halfop_color),
                resources.getColor(R.color.nick_voice_color),
                resources.getColor(R.color.nick_user_color)
        };
        Color.defColor = resources.getColor(R.color.nick_user_color);

        for (int i = 0; i<16; i++) {
            messageColor.put(resources.getColor(MessageUtil.mircCodeToColor(i)),i);
        }

        if (themeName.equals("light")) {
            theme = R.style.Theme_QuasselDroid_Material_Light;
            themeNoActionBar = R.style.Theme_QuasselDroid_Material_Light_NoActionBar;


            Color.chatPlain = resources.getColor(R.color.chat_line_plain_light);
            Color.chatError = resources.getColor(R.color.chat_line_error_light);
            Color.chatAction = resources.getColor(R.color.chat_line_action_light);
            Color.chatTimestamp = resources.getColor(R.color.chat_line_timestamp_light);
            Color.chatHighlight = resources.getColor(R.color.chat_line_highlight_light);

            Color.chatActionBg = resources.getColor(R.color.chat_bg_action_light);
            Color.chatPlainBg = R.color.chat_line_plain_light;

            Color.bufferRead = resources.getColor(R.color.buffer_read_color_light);
            Color.bufferParted = resources.getColor(R.color.buffer_parted_color_light);
            Color.bufferHighlight = resources.getColor(R.color.buffer_highlight_color_light);
            Color.bufferUnread = resources.getColor(R.color.buffer_unread_color_light);
            Color.bufferActivity = resources.getColor(R.color.buffer_activity_color_light);

            Color.bufferStateTemp = resources.getColor(R.color.buffer_status_temp_light);
            Color.bufferStatePerm = resources.getColor(R.color.buffer_status_perm_light);
            Color.bufferStateActive = resources.getColor(R.color.buffer_status_active_light);
            Color.bufferStateAway = resources.getColor(R.color.buffer_status_away_light);
            Color.bufferStateParted = resources.getColor(R.color.buffer_status_parted_light);

            nickBackgrounds = new int[] {
                    resources.getColor(R.color.nick_owner_light),
                    resources.getColor(R.color.nick_admin_light),
                    resources.getColor(R.color.nick_operator_light),
                    resources.getColor(R.color.nick_halfop_light),
                    resources.getColor(R.color.nick_voice_light),
                    resources.getColor(R.color.nick_user_light)
            };

            nickConstants = new double[] {0.7, 0.5};
        } else if (themeName.equals("dark")) {
            theme = R.style.Theme_QuasselDroid_Material_Dark;
            themeNoActionBar = R.style.Theme_QuasselDroid_Material_Dark_NoActionBar;

            Color.chatPlain = resources.getColor(R.color.chat_line_plain_dark);
            Color.chatError = resources.getColor(R.color.chat_line_error_dark);
            Color.chatAction = resources.getColor(R.color.chat_line_action_dark);
            Color.chatTimestamp = resources.getColor(R.color.chat_line_timestamp_dark);
            Color.chatHighlight = resources.getColor(R.color.chat_line_highlight_dark);

            Color.chatActionBg = resources.getColor(R.color.chat_bg_action_dark);
            Color.chatPlainBg = R.color.chat_line_plain_dark;

            Color.bufferRead = resources.getColor(R.color.buffer_read_color_dark);
            Color.bufferParted = resources.getColor(R.color.buffer_parted_color_dark);
            Color.bufferHighlight = resources.getColor(R.color.buffer_highlight_color_dark);
            Color.bufferUnread = resources.getColor(R.color.buffer_unread_color_dark);
            Color.bufferActivity = resources.getColor(R.color.buffer_activity_color_dark);

            Color.bufferStateTemp = resources.getColor(R.color.buffer_status_temp_dark);
            Color.bufferStatePerm = resources.getColor(R.color.buffer_status_perm_dark);
            Color.bufferStateActive = resources.getColor(R.color.buffer_status_active_dark);
            Color.bufferStateAway = resources.getColor(R.color.buffer_status_away_dark);
            Color.bufferStateParted = resources.getColor(R.color.buffer_status_parted_dark);

            nickBackgrounds = new int[] {
                    resources.getColor(R.color.nick_owner_dark),
                    resources.getColor(R.color.nick_admin_dark),
                    resources.getColor(R.color.nick_operator_dark),
                    resources.getColor(R.color.nick_halfop_dark),
                    resources.getColor(R.color.nick_voice_dark),
                    resources.getColor(R.color.nick_user_dark)
            };

            nickConstants = new double[] {0.84, 0.71};
        } else {
            setTheme(context, "light");
        }
    }

    public static class Color {
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

        public static int defColor;
    }

    public static int getNickColor(IrcMode mode) {
        switch (mode) {
            case OWNER:
                return nickColors[0];
            case ADMIN:
                return nickColors[1];
            case OPERATOR:
                return nickColors[2];
            case HALF_OPERATOR:
                return nickColors[3];
            case VOICE:
                return nickColors[4];
            case USER:
                return nickColors[5];
            default:
                return Color.defColor;
        }
    }

    public static int getNickBg(IrcMode mode) {
        switch (mode) {
            case OWNER:
                return nickBackgrounds[0];
            case ADMIN:
                return nickBackgrounds[1];
            case OPERATOR:
                return nickBackgrounds[2];
            case HALF_OPERATOR:
                return nickBackgrounds[3];
            case VOICE:
                return nickBackgrounds[4];
            case USER:
                return nickBackgrounds[5];
            default:
                return Color.defColor;
        }
    }
}
