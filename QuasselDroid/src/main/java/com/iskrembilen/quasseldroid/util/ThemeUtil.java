/*
    QuasselDroid - Quassel client for Android
    Copyright (C) 2015 Ken Børge Viktil
    Copyright (C) 2015 Magnus Fjell
    Copyright (C) 2015 Martin Sandsmark <martin.sandsmark@kde.org>

    This program is free software: you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation, either version 3 of the License, or (at your option)
    any later version, or under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either version 2.1 of
    the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License and the
    GNU Lesser General Public License along with this program.  If not, see
    <http://www.gnu.org/licenses/>.
 */

package com.iskrembilen.quasseldroid.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.SparseIntArray;

import com.iskrembilen.quasseldroid.protocol.state.IrcMode;
import com.iskrembilen.quasseldroid.R;

public class ThemeUtil {

    public static int theme, themeNoActionBar, themeDrawStatusBar, themeNoActionBarDrawStatusBar;
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
        Color.transparent = resources.getColor(android.R.color.transparent);

        for (int i = 0; i<16; i++) {
            messageColor.put(resources.getColor(MessageUtil.mircCodeToColor(i)),i);
        }

        if (themeName.equals("light")) {
            theme = R.style.Theme_QuasselDroid_Material_Light;
            themeNoActionBar = R.style.Theme_QuasselDroid_Material_Light_NoActionBar;
            themeDrawStatusBar = R.style.Theme_QuasselDroid_Material_Light_DrawOverStatusBar;
            themeNoActionBarDrawStatusBar = R.style.Theme_QuasselDroid_Material_Light_NoActionBar_DrawOverStatusBar;

            Color.chatPlain = resources.getColor(R.color.chat_line_plain_light);
            Color.chatError = resources.getColor(R.color.chat_line_error_light);
            Color.chatAction = resources.getColor(R.color.chat_line_action_light);
            Color.chatServer = resources.getColor(R.color.chat_line_server_light);
            Color.chatTimestamp = resources.getColor(R.color.chat_line_timestamp_light);
            Color.chatHighlight = resources.getColor(R.color.chat_line_highlight_light);

            Color.chatServerBg = resources.getColor(R.color.chat_bg_action_light);
            Color.chatPlainBg = resources.getColor(android.R.color.transparent);

            Color.bufferRead = resources.getColor(R.color.buffer_read_color_light);
            Color.bufferParted = resources.getColor(R.color.buffer_parted_color_light);
            Color.bufferHighlight = resources.getColor(R.color.buffer_highlight_color_light);
            Color.bufferUnread = resources.getColor(R.color.buffer_unread_color_light);
            Color.bufferActivity = resources.getColor(R.color.buffer_activity_color_light);
            Color.bufferFocused = resources.getColor(R.color.accent);

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

            Color.nickSelfColor = resources.getColor(R.color.nick_self_light);

            nickConstants = new double[] {0.7, 0.5};
        } else if (themeName.equals("dark")) {
            theme = R.style.Theme_QuasselDroid_Material_Dark;
            themeNoActionBar = R.style.Theme_QuasselDroid_Material_Dark_NoActionBar;
            themeDrawStatusBar = R.style.Theme_QuasselDroid_Material_Dark_DrawOverStatusBar;
            themeNoActionBarDrawStatusBar = R.style.Theme_QuasselDroid_Material_Dark_NoActionBar_DrawOverStatusBar;

            Color.chatPlain = resources.getColor(R.color.chat_line_plain_dark);
            Color.chatError = resources.getColor(R.color.chat_line_error_dark);
            Color.chatAction = resources.getColor(R.color.chat_line_action_dark);
            Color.chatServer = resources.getColor(R.color.chat_line_server_dark);
            Color.chatTimestamp = resources.getColor(R.color.chat_line_timestamp_dark);
            Color.chatHighlight = resources.getColor(R.color.chat_line_highlight_dark);

            Color.chatServerBg = resources.getColor(R.color.chat_bg_action_dark);
            Color.chatPlainBg = resources.getColor(android.R.color.transparent);

            Color.bufferRead = resources.getColor(R.color.buffer_read_color_dark);
            Color.bufferParted = resources.getColor(R.color.buffer_parted_color_dark);
            Color.bufferHighlight = resources.getColor(R.color.buffer_highlight_color_dark);
            Color.bufferUnread = resources.getColor(R.color.buffer_unread_color_dark);
            Color.bufferActivity = resources.getColor(R.color.buffer_activity_color_dark);
            Color.bufferFocused = resources.getColor(R.color.accent);

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

            Color.nickSelfColor = resources.getColor(R.color.nick_self_dark);

            nickConstants = new double[] {0.84, 0.71};
        } else {
            setTheme(context, "light");
        }
    }

    public static class Color {
        public static int
                chatPlain,
                chatAction,
                chatServer,
                chatError,
                chatHighlight,
                chatTimestamp,
                chatServerBg,
                chatPlainBg;

        public static int
                bufferParted,
                bufferHighlight,
                bufferUnread,
                bufferActivity,
                bufferRead,
                bufferFocused;

        public static int
                bufferStateTemp,
                bufferStatePerm,
                bufferStateActive,
                bufferStateAway,
                bufferStateParted;

        public static int nickSelfColor;

        public static int defColor;
        public static int transparent;
    }

    public static int getModeColor(IrcMode mode) {
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
                return Color.nickSelfColor;
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
