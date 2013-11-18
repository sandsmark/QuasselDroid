package com.iskrembilen.quasseldroid.util;

import android.content.res.Resources;

import com.iskrembilen.quasseldroid.R;

import java.text.DecimalFormat;

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

}
