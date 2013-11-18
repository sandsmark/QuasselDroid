package com.iskrembilen.quasseldroid.util;

import android.graphics.Color;

public class SenderColorHelper {
    private static double INTEGER_RANGE = 1L << 32;

    public static int getSenderColor(String nick) {
        double doubleHash = ((long) nick.hashCode() - Integer.MIN_VALUE) / INTEGER_RANGE;
        int color = hslToRgb(doubleHash, 0.8, 0.5); //Last to values comes from trial and error
        return color;
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

}
