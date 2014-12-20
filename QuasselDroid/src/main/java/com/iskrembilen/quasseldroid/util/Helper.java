package com.iskrembilen.quasseldroid.util;

import android.content.res.Resources;
import android.text.SpannableString;

import com.iskrembilen.quasseldroid.Buffer;
import com.iskrembilen.quasseldroid.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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

    public static Map<String,String> parseModeChange(String modechange) {
        //TODO: Implement proper UserModeChange parser
        return null;
    }

    public static class AlphabeticalComparator implements Comparator<String> {
        public int compare(String s1, String s2)
        {
            return s1.compareToIgnoreCase(s2);
        }
    }

    public static class OrderComparator implements Comparator<Buffer> {
        @Override
        public int compare(Buffer lhs, Buffer rhs) {
            return lhs.getOrder() - rhs.getOrder();
        }
    }

    public static CharSequence[] split(CharSequence string, String pattern) {
        String[] parts = string.toString().split(pattern);
        List<CharSequence> res = new ArrayList<>();
        CharSequence temp = string;
        int pos = 0;
        for (String part : parts) {
            res.add(string.subSequence(pos,pos+part.length()));
            pos += part.length();
        }
        return res.toArray(new CharSequence[res.size()]);
    }
}
