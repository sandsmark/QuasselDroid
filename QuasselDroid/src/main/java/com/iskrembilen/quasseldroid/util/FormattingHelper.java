package com.iskrembilen.quasseldroid.util;

import android.graphics.Typeface;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

public class FormattingHelper {
    public static String toEscapeCodes(Spanned text) {
        StringBuilder out = new StringBuilder();
        withinParagraph(out, text, 0, text.length());
        return out.toString();
    }

    public static int colorToId(int color) {
        return ThemeUtil.messageColor.get(color);
    }

    private static void withinParagraph(StringBuilder out, Spanned text,
                                        int start, int end) {
        int next;
        for (int i = start; i < end; i = next) {
            next = text.nextSpanTransition(i, end, CharacterStyle.class);
            CharacterStyle[] style = text.getSpans(i, next,
                    CharacterStyle.class);

            boolean jump = false;

            for (int j = 0; j < style.length; j++) {
                if (jump) {
                    jump = false;
                } else {
                    if (style[j] instanceof StyleSpan) {
                        int s = ((StyleSpan) style[j]).getStyle();

                        if ((s & Typeface.BOLD) != 0) {
                            out.append((char) 0x02);
                        }
                        if ((s & Typeface.ITALIC) != 0) {
                            out.append((char) 0x1D);
                        }
                    } else if (style[j] instanceof UnderlineSpan) {
                        out.append((char) 0x1F);
                    } else if (style[j] instanceof ForegroundColorSpan) {
                        int fg;
                        int bg;
                        fg = colorToId(((ForegroundColorSpan) style[j]).getForegroundColor());

                        if ((j + 1 < style.length) && (style[j + 1] instanceof BackgroundColorSpan)) {
                            bg = colorToId(((BackgroundColorSpan) style[j + 1]).getBackgroundColor());
                        } else {
                            bg = 99;
                        }

                        out.append((char) 0x03);
                        out.append(String.format("%02d,%02d", fg, bg));

                        jump = true;
                    } else if (style[j] instanceof BackgroundColorSpan) {
                        int fg;
                        int bg;
                        if ((j + 1 < style.length) && (style[j + 1] instanceof ForegroundColorSpan)) {
                            fg = colorToId(((ForegroundColorSpan) style[j + 1]).getForegroundColor());
                        } else {
                            fg = 99;
                        }

                        bg = colorToId(((BackgroundColorSpan) style[j]).getBackgroundColor());

                        out.append((char) 0x03);
                        out.append(String.format("%02d,%02d", fg, bg));

                        jump = true;
                    }
                }
            }

            out.append(text.subSequence(i,next));

            for (int j = style.length - 1; j >= 0; j--) {
                if (style[j] instanceof ForegroundColorSpan) {
                    out.append((char) 0x03);
                }
                if (style[j] instanceof BackgroundColorSpan) {
                    out.append((char) 0x03);
                }
                if (style[j] instanceof UnderlineSpan) {
                    out.append((char) 0x1F);
                }
                if (style[j] instanceof StyleSpan) {
                    int s = ((StyleSpan) style[j]).getStyle();

                    if ((s & Typeface.BOLD) != 0) {
                        out.append((char) 0x02);
                    }
                    if ((s & Typeface.ITALIC) != 0) {
                        out.append((char) 0x1D);
                    }
                }
            }
        }
    }
}
