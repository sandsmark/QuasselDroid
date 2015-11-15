/*
    Copyright © 2015 Janne Koschinski

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

package de.kuschku.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class HelperUtils {
    static void printStackTraceLimited(Exception e, int maxLength) {
        StringBuilder builder = new StringBuilder(String.format("Exception in thread %1$s %2$s: %3$s", Thread.currentThread().getName(), e.getClass().getCanonicalName(), e.getMessage())).append("\n");
        for (int i = 0; i < 10 && i < e.getStackTrace().length; i++) {
            StackTraceElement elem = e.getStackTrace()[i];
            builder.append("    ").append("at ").append(elem.toString()).append("\n");
        }
        Log.e("System.err", builder.toString());
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
