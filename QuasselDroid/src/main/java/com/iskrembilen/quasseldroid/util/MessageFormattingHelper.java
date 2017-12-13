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
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.protocol.state.Buffer;
import com.iskrembilen.quasseldroid.protocol.state.Client;
import com.iskrembilen.quasseldroid.protocol.state.IrcMessage;
import com.iskrembilen.quasseldroid.protocol.state.IrcMode;
import com.iskrembilen.quasseldroid.protocol.state.IrcUser;
import com.iskrembilen.quasseldroid.protocol.state.Network;

import java.net.IDN;
import java.nio.charset.Charset;
import java.util.Locale;

public class MessageFormattingHelper {
    public static class IrcUserUtils {
        private IrcUserUtils() {

        }

        static int getSenderColor(@NonNull String nick) {
            nick = trimEnd(nick, '_').toLowerCase(Locale.US);
            byte[] data = nick.getBytes(Charset.forName("ISO-8859-1"));
            return (0xf & CRCUtils.qChecksum(data));
        }

        @NonNull
        private static String trimEnd(@NonNull String str, char character) {
            char[] val = str.toCharArray();
            int len = val.length;
            while ((0 < len) && (val[len - 1] == character)) {
                len--;
            }
            return ((len < val.length)) ? str.substring(0, len) : str;
        }

        static class CRCUtils {
            private CRCUtils() {

            }

            static int qChecksum(@NonNull byte[] data) {
                int crc = 0xffff;
                int crcHighBitMask = 0x8000;

                for (byte b : data) {
                    int c = reflect(b, 8);
                    for (int j = 0x80; j > 0; j >>= 1) {
                        int highBit = crc & crcHighBitMask;
                        crc <<= 1;
                        if ((c & j) > 0) {
                            highBit ^= crcHighBitMask;
                        }
                        if (highBit > 0) {
                            crc ^= 0x1021;
                        }
                    }
                }

                crc = reflect(crc, 16);
                crc ^= 0xffff;
                crc &= 0xffff;

                return crc;
            }

            private static int reflect(int crc, int n) {
                int j = 1, crcout = 0;
                for (int i = (1 << (n - 1)); i > 0; i >>= 1) {
                    if ((crc & i) > 0) {
                        crcout |= j;
                    }
                    j <<= 1;
                }
                return crcout;
            }
        }
    }

    private static int[] senderColors = {
            0xFFcc0000, 0xFF006cad, 0xFF4d9900, 0xFF6600cc,
            0xFFa67d00, 0xFF009927, 0xFF0030c0, 0xFFcc009a,
            0xFFb94600, 0xFF869900, 0xFF149900, 0xFF009960,
            0xFF006cad, 0xFF0099cc, 0xFFb300cc, 0xFFcc004d,
    };

    public static int getSenderColor(String nick) {
        return senderColors[IrcUserUtils.getSenderColor(nick) % senderColors.length];
    }

    public static class NickFormatter {
        private boolean useBrackets;
        private String[] defaultBrackets;

        public NickFormatter(boolean useBrackets, String[] defaultBrackets) {
            this.useBrackets = useBrackets;
            this.defaultBrackets = defaultBrackets;
        }

        public CharSequence formatNick(String nick, boolean reduced) {
            return formatNick(nick, reduced, defaultBrackets);
        }

        public CharSequence formatNick(String nick, boolean reduced, String[] brackets) {
            Spannable nickSpan = new SpannableString(nick);
            SpanUtils.setFullSpan(nickSpan, new StyleSpan(Typeface.BOLD));

            if (reduced)
                SpanUtils.setFullSpan(nickSpan, new ForegroundColorSpan(ThemeUtil.Color.nickSelfColor));
            else
                SpanUtils.setFullSpan(nickSpan, new ForegroundColorSpan(getSenderColor(nick)));

            if (useBrackets)
                return TextUtils.concat(brackets[0], nickSpan, brackets[1]);
            else
                return nickSpan;
        }
    }

    public static CharSequence formatNick(Context ctx, String nick, boolean self, boolean reduced) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean useBrackets = preferences.getBoolean(ctx.getString(R.string.preference_nickbrackets), false);

        return new NickFormatter(useBrackets, new String[] {"<",">"}).formatNick(nick, self);
    }
}
