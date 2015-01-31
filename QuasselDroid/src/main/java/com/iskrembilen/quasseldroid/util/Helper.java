package com.iskrembilen.quasseldroid.util;

import android.content.Context;
import android.content.res.Resources;

import com.iskrembilen.quasseldroid.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Helper {

    public static String formatLatency(float number, Resources res) {
        String latency;
        String unit;
        if (number >= 100) {
            unit = res.getString(R.string.unit_seconds);
            DecimalFormat formatter = new DecimalFormat("##0.0");
            latency = formatter.format(number / 1000.);
        } else {
            unit = res.getString(R.string.unit_milliseconds);
            latency = String.valueOf((int) number);
        }
        return String.format(res.getString(R.string.title_lag), latency, unit);
    }

    public static CharSequence[] split(CharSequence string, String pattern) {
        String[] parts = string.toString().split(pattern);
        List<CharSequence> res = new ArrayList<>();
        int pos = 0;
        for (String part : parts) {
            res.add(string.subSequence(pos,pos+part.length()));
            pos += part.length();
        }
        return res.toArray(new CharSequence[res.size()]);
    }

    public static int getStatusBarHeight(Context ctx) {
        int result = 0;
        int resourceId = ctx.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = ctx.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static String appendCamelCase(String... rawStrings)  {
        return appendCamelCase(Locale.US, rawStrings);
    }

    public static String appendCamelCase(Locale l, String... rawStrings) {
        if (rawStrings.length==1)
            return rawStrings[0];

        String[] strings = Arrays.copyOfRange(rawStrings, 1, rawStrings.length);
        StringBuilder builder = new StringBuilder(rawStrings[0]);
        for (String s : strings) {
            builder.append(s.substring(0,1).toUpperCase(l));
            builder.append(s.substring(1));
        }

        return builder.toString();
    }

    public static String printSlice(Object[] slice) {
        StringBuilder builder = new StringBuilder("[ ");
        for (Object o : slice) {
            builder.append(o.toString()+", ");
        }
        builder.append(" ]");
        return builder.toString();
    }
}
