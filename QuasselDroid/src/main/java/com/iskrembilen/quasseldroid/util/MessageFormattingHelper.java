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

public class MessageFormattingHelper {

    public static int getSenderColor(String nick) {
        return getSenderColor(nick, 1.0F, 1.0F);
    }

    public static int getSenderColor(String nick, float saturationMultiplicator, float luminosityMultiplicator) {
        double INTEGER_RANGE = 1L << 32;
        double doubleHash = ((long) nick.hashCode() - Integer.MIN_VALUE) / INTEGER_RANGE;
        return hslToRgb(doubleHash, ThemeUtil.nickConstants[0] * saturationMultiplicator, ThemeUtil.nickConstants[1] * luminosityMultiplicator); //Last to values comes from trial and error
    }

    private static double hue2rgb(double p, double q, double t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1.0 / 6.0) return p + (q - p) * 6 * t;
        if (t < 1.0 / 2.0) return q;
        if (t < 2.0 / 3.0) return p + (q - p) * (2.0 / 3.0 - t) * 6;
        return p;
    }

    private static int hslToRgb(double h, double s, double l) {
        double r, g, b;

        if (s == 0) {
            r = g = b = l; // achromatic
        } else {
            double q = l < 0.5 ? l * (1 + s) : l + s - l * s;
            double p = 2 * l - q;
            r = hue2rgb(p, q, h + 1.0 / 3.0);
            g = hue2rgb(p, q, h);
            b = hue2rgb(p, q, h - 1.0 / 3.0);
        }
        return Color.rgb((int) (r * 255), (int) (g * 255), (int) (b * 255));

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
            Spannable nickSpan;

            nickSpan = new SpannableString(nick);
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
