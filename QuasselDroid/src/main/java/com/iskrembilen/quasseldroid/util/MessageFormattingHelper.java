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

    public static CharSequence formatNick(IrcMessage entry, boolean fancyPrefix, boolean nickBrackets) {
        Network network = Client.getInstance().getNetworks().getNetworkById(entry.bufferInfo.networkId);
        Buffer buffer = network.getBuffers().getBuffer(entry.bufferInfo.id);
        IrcUser user = network.getUserByNick(entry.getNick());

        Spannable nickPrefix;
        Spannable nickSpan;

        nickSpan = new SpannableString(entry.getNick());
        SpanUtils.setFullSpan(nickSpan, new StyleSpan(Typeface.BOLD));

        if (network.getMyNick().equals(entry.getNick()))
            SpanUtils.setFullSpan(nickSpan, new ForegroundColorSpan(ThemeUtil.Color.nickSelfColor));
        else if (entry.isHighlighted())
            SpanUtils.setFullSpan(nickSpan, new ForegroundColorSpan(getSenderColor(entry.getNick(), 0.5F, 0.5F)));
        else
            SpanUtils.setFullSpan(nickSpan, new ForegroundColorSpan(getSenderColor(entry.getNick())));

        if (user==null || !buffer.getUsers().getUniqueUsers().contains(user)) {
            nickPrefix = new SpannableString("×");
            // ← ⇐ →
        }

        if (nickBrackets) {
            return TextUtils.concat("<", nickSpan, ">");
        } else {
            return nickSpan;
        }
    }

    public static CharSequence formatNick(Context ctx, String nick, boolean reduced) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean nickBrackets = preferences.getBoolean(ctx.getString(R.string.preference_nickbrackets), false);
        Spannable nickSpan = new SpannableString(nick);
        SpanUtils.setFullSpan(nickSpan, new StyleSpan(Typeface.BOLD));

        if (reduced)
            SpanUtils.setFullSpan(nickSpan, new ForegroundColorSpan(getSenderColor(nick, 0.5F, 0.5F)));
        else
            SpanUtils.setFullSpan(nickSpan, new ForegroundColorSpan(getSenderColor(nick)));

        if (nickBrackets) {
            return TextUtils.concat("<",nickSpan, ">");
        } else {
            return nickSpan;
        }
    }
}
