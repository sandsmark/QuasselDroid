package com.iskrembilen.quasseldroid.util;

import android.text.Spannable;
import android.text.style.CharacterStyle;

public class SpanUtils {
    public static void setFullSpan(Spannable text, CharacterStyle style) {
        text.setSpan(style, 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    }
}
