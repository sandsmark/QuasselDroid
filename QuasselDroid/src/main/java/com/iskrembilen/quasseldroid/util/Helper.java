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

    public static String printSlice(Object[] idlist) {
        StringBuilder builder = new StringBuilder("[");
        for (Object o : idlist) {
            builder.append(o.toString()).append(", ");
        }
        return builder.append("]").toString();
    }

    public static String printSlice(int[] idlist) {
        StringBuilder builder = new StringBuilder("[");
        for (Object o : idlist) {
            builder.append(o.toString()).append(", ");
        }
        return builder.append("]").toString();
    }
}
