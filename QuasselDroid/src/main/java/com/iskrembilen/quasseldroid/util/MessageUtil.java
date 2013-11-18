package com.iskrembilen.quasseldroid.util;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;

import com.iskrembilen.quasseldroid.Buffer;
import com.iskrembilen.quasseldroid.IrcMessage;
import com.iskrembilen.quasseldroid.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {
    private static final String TAG = MessageUtil.class.getSimpleName();

    /**
     * Checks if there is a highlight in the message and then sets the flag of
     * that message to highlight
     *
     * @param buffer  the buffer the message belongs to
     * @param message the message to check
     */
    public static void checkMessageForHighlight(String nick, Buffer buffer, IrcMessage message) {
        if (message.type == IrcMessage.Type.Plain || message.type == IrcMessage.Type.Action) {
            if (nick == null) {
                Log.e(TAG, "Nick is null in check message for highlight");
                return;
            } else if (nick.equals("")) return;
            Pattern regexHighlight = Pattern.compile(".*(?<!(\\w|\\d))" + Pattern.quote(nick) + "(?!(\\w|\\d)).*", Pattern.CASE_INSENSITIVE);
            Matcher matcher = regexHighlight.matcher(message.content);
            if (matcher.find()) {
                message.setFlag(IrcMessage.Flag.Highlight);
                // FIXME: move to somewhere proper
            }
        }
    }

    /**
     * Parse mIRC style codes in IrcMessage
     */
    public static void parseStyleCodes(Context context, IrcMessage message) {
        final char boldIndicator = 2;
        final char normalIndicator = 15;
        final char italicIndicator = 29;
        final char underlineIndicator = 31;
        final char colorIndicator = 3;

        String content = message.content.toString();

        if (content.indexOf(boldIndicator) == -1
                && content.indexOf(italicIndicator) == -1
                && content.indexOf(underlineIndicator) == -1
                && content.indexOf(colorIndicator) == -1)
            return;

        SpannableStringBuilder newString = new SpannableStringBuilder(message.content);

        int start, end, endSearchOffset, startIndicatorLength, style, fg, bg;
        while (true) {
            content = newString.toString();
            start = -1;
            end = -1;
            startIndicatorLength = 1;
            style = 0;
            fg = -1;
            bg = -1;

            endSearchOffset = start + 1;

            // Colors?
            if (start == -1) {
                start = content.indexOf(colorIndicator);

                if (start != -1) {
                    // Note that specifying colour codes here is optional, as the same indicator will cancel existing colours
                    endSearchOffset = start + 1;
                    if (endSearchOffset < content.length()) {
                        if (Character.isDigit(content.charAt(endSearchOffset))) {
                            if (endSearchOffset + 1 < content.length() && Character.isDigit(content.charAt(endSearchOffset + 1))) {
                                fg = Integer.parseInt(content.substring(endSearchOffset, endSearchOffset + 2));
                                endSearchOffset += 2;
                            } else {
                                fg = Integer.parseInt(content.substring(endSearchOffset, endSearchOffset + 1));
                                endSearchOffset += 1;
                            }

                            if (endSearchOffset < content.length() && content.charAt(endSearchOffset) == ',') {
                                if (endSearchOffset + 1 < content.length() && Character.isDigit(content.charAt(endSearchOffset + 1))) {
                                    endSearchOffset++;
                                    if (endSearchOffset + 1 < content.length() && Character.isDigit(content.charAt(endSearchOffset + 1))) {
                                        bg = Integer.parseInt(content.substring(endSearchOffset, endSearchOffset + 2));
                                        endSearchOffset += 2;
                                    } else {
                                        bg = Integer.parseInt(content.substring(endSearchOffset, endSearchOffset + 1));
                                        endSearchOffset += 1;
                                    }
                                }
                            }
                        }
                    }
                    startIndicatorLength = endSearchOffset - start;

                    end = content.indexOf(colorIndicator, endSearchOffset);
                }
            }

            if (start == -1) {
                start = content.indexOf(boldIndicator);
                if (start != -1) {
                    end = content.indexOf(boldIndicator, start + 1);
                    style = Typeface.BOLD;
                }
            }

            if (start == -1) {
                start = content.indexOf(italicIndicator);
                if (start != -1) {
                    end = content.indexOf(italicIndicator, start + 1);
                    style = Typeface.ITALIC;
                }
            }

            if (start == -1) {
                start = content.indexOf(underlineIndicator);
                if (start != -1) {
                    end = content.indexOf(underlineIndicator, start + 1);
                    style = -1;
                }
            }

            if (start == -1)
                break;

            int norm = content.indexOf(normalIndicator, start + 1);
            if (norm != -1 && (end == -1 || norm < end))
                end = norm;

            if (end == -1)
                end = content.length();

            if (end - (start + startIndicatorLength) > 0) {
                // Only set spans if there's any text between start & end
                if (style == -1) {
                    newString.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                } else {
                    newString.setSpan(new StyleSpan(style), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                }

                if (fg != -1) {
                    newString.setSpan(new ForegroundColorSpan(context.getResources()
                            .getColor(mircCodeToColor(fg))), start, end,
                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                }
                if (bg != -1) {
                    newString.setSpan(new BackgroundColorSpan(context.getResources()
                            .getColor(mircCodeToColor(bg))), start, end,
                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }

            // Intentionally don't remove "normal" indicators or color here, as they are multi-purpose
            if (end < content.length() && (content.charAt(end) == boldIndicator
                    || content.charAt(end) == italicIndicator
                    || content.charAt(end) == underlineIndicator))
                newString.delete(end, end + 1);

            newString.delete(start, start + startIndicatorLength);
        }

        // NOW we remove the "normal" and color indicator
        while (true) {
            content = newString.toString();
            int normPos = content.indexOf(normalIndicator);
            if (normPos != -1)
                newString.delete(normPos, normPos + 1);

            int colorPos = content.indexOf(colorIndicator);
            if (colorPos != -1)
                newString.delete(colorPos, colorPos + 1);

            if (normPos == -1 && colorPos == -1)
                break;
        }

        message.content = newString;
    }

    public static int mircCodeToColor(int code) {
        int color;
        switch (code) {
            case 0: // white
                color = R.color.ircmessage_white;
                break;
            case 1: // black
                color = R.color.ircmessage_black;
                break;
            case 2: // blue (navy)
                color = R.color.ircmessage_blue;
                break;
            case 3: // green
                color = R.color.ircmessage_green;
                break;
            case 4: // red
                color = R.color.ircmessage_red;
                break;
            case 5: // brown (maroon)
                color = R.color.ircmessage_brown;
                break;
            case 6: // purple
                color = R.color.ircmessage_purple;
                break;
            case 7: // orange (olive)
                color = R.color.ircmessage_orange;
                break;
            case 8: // yellow
                color = R.color.ircmessage_yellow;
                break;
            case 9: // light green (lime)
                color = R.color.ircmessage_light_green;
                break;
            case 10: // teal (a green/blue cyan)
                color = R.color.ircmessage_teal;
                break;
            case 11: // light cyan (cyan) (aqua)
                color = R.color.ircmessage_light_cyan;
                break;
            case 12: // light blue (royal)
                color = R.color.ircmessage_light_blue;
                break;
            case 13: // pink (light purple) (fuchsia)
                color = R.color.ircmessage_pink;
                break;
            case 14: // grey
                color = R.color.ircmessage_gray;
                break;
            default:
                color = ThemeUtil.chatPlainResource;
        }
        return color;
    }


}
