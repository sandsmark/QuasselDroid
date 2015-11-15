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
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;

import com.iskrembilen.quasseldroid.protocol.state.Buffer;
import com.iskrembilen.quasseldroid.protocol.state.BufferInfo;
import com.iskrembilen.quasseldroid.protocol.state.Client;
import com.iskrembilen.quasseldroid.protocol.state.Identity;
import com.iskrembilen.quasseldroid.protocol.state.IdentityCollection;
import com.iskrembilen.quasseldroid.protocol.state.IrcMessage;
import com.iskrembilen.quasseldroid.protocol.state.Network;
import com.iskrembilen.quasseldroid.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {
    private static final String TAG = MessageUtil.class.getSimpleName();

    public static void checkForHighlight(Context ctx, IrcMessage msg) {
        if (((msg.type!=IrcMessage.Type.Plain) && (msg.type!=IrcMessage.Type.Notice) && (msg.type!=IrcMessage.Type.Action)) ||
            (msg.flags==IrcMessage.Flag.Self.getValue()))
            return;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());

        String highlightNickPreference = preferences.getString("preference_highlight_type", "current");
        String highlightNickPreferenceAll = ctx.getResources().getStringArray(R.array.entryvalues_highlight_preference)[0];
        String highlightNickPreferenceCurrent = ctx.getResources().getStringArray(R.array.entryvalues_highlight_preference)[1];

        boolean preferenceNickCaseSensitive = preferences.getBoolean("perference_highlight_casesensitive",false);

        // TODO: Cache this (per network)
        Network net = Client.getInstance().getNetworks().getNetworkById(msg.bufferInfo.networkId);
        if (net!=null && !net.getMyNick().isEmpty()) {
            List<String> nickList = new ArrayList<>(1);
            if (highlightNickPreference.equals(highlightNickPreferenceCurrent)) {
                nickList.add(net.getMyNick());
            } else if (highlightNickPreference.equals(highlightNickPreferenceAll)) {
                Identity myIdentity = Client.getInstance().getIdentities().getIdentity(net.identityId);
                if (myIdentity!=null)
                    nickList = myIdentity.getNicks();
                if (!nickList.contains(net.getMyNick()))
                    nickList.add(net.getMyNick());
            }

            for (String nickname : nickList) {
                int flags = Pattern.CASE_INSENSITIVE;
                if (preferenceNickCaseSensitive) flags = 0;
                Pattern nickRegExp = Pattern.compile("(^|\\W)" + Pattern.quote(nickname) + "(\\W|$)", flags);
                Matcher matcher = nickRegExp.matcher(msg.content);
                if (matcher.find()) {
                    msg.setFlag(IrcMessage.Flag.Highlight);
                    return;
                }
            }

            // Custom highlight rules
/*
            for (int i = 0; i < _highlightRules.count(); i++) {
                const HighlightRule &rule = _highlightRules.at(i);
                if (!rule.isEnabled)
                    continue;

                if (rule.chanName.size() > 0 && rule.chanName.compare(".*") != 0) {
                    if (rule.chanName.startsWith("!")) {
                        QRegExp rx(rule.chanName.mid(1), Qt::CaseInsensitive);
                        if (rx.exactMatch(msg.bufferInfo().bufferName()))
                            continue;
                    }
                    else {
                        QRegExp rx(rule.chanName, Qt::CaseInsensitive);
                        if (!rx.exactMatch(msg.bufferInfo().bufferName()))
                            continue;
                    }
                }

                QRegExp rx;
                if (rule.isRegExp) {
                    rx = QRegExp(rule.name, rule.caseSensitive ? Qt::CaseSensitive : Qt::CaseInsensitive);
                }
                else {
                    rx = QRegExp("(^|\\W)" + QRegExp::escape(rule.name) + "(\\W|$)", rule.caseSensitive ? Qt::CaseSensitive : Qt::CaseInsensitive);
                }
                bool match = (rx.indexIn(msg.contents()) >= 0);
                if (match) {
                    msg.setFlags(msg.flags() | Message::Highlight);
                    return;
                }
            }
*/
        }
    }

    /**
     * Checks if there is a highlight in the message and then sets the flag of
     * that message to highlight
     *
     * @param ctx the context from which the preferences will be taken
     * @param notificationManager the NotificationManager that will receive a notification if highlighted
     * @param message the message to check
     */
    public static void processMessage(Context ctx, QuasseldroidNotificationManager notificationManager, IrcMessage message) {
        // Set the message filtered if it matches ignore rules
        message.setFiltered(Client.getInstance().getIgnoreListManager().matches(message));

        checkForHighlight(ctx, message);

        // All messages that match a highlight pattern or are in queries will trigger a notification
        if (message.isHighlighted() || message.bufferInfo.type == BufferInfo.Type.QueryBuffer && !message.isSelf()) {
            // Server messages with an empty sender in queries are "x is away: ..." messages
            // (I've found no exception to that rule in my 13-million-message database)
            // We can ignore them safely
            if (message.type == IrcMessage.Type.Server && message.getSender().length() == 0)
                return;

            Buffer buffer = Client.getInstance().getNetworks().getBufferById(message.bufferInfo.id);

            // We want to ignore the currently focused buffer
            if (buffer.isDisplayed())
                return;

            // If the message wasn’t read yet, we want to add a notification
            if (buffer.getLastSeenMessage() < message.messageId && !buffer.isPermanentlyHidden() && !message.isFiltered())
                notificationManager.addMessage(message);
        }
    }

    /**
     * Parse mIRC style codes in IrcMessage
     */
    public static SpannableString parseStyleCodes(Context context, String content, boolean parse) {
        if (!parse) {
            return new SpannableString(content
                    .replaceAll("\\x02","")
                    .replaceAll("\\x0F","")
                    .replaceAll("\\x1D","")
                    .replaceAll("\\x1F","")
                    .replaceAll("\\x03[0-9]{1,2}(,[0-9]{1,2})?","")
                    .replaceAll("\\x03",""));
        }

        final char boldIndicator = 2;
        final char normalIndicator = 15;
        final char italicIndicator = 29;
        final char underlineIndicator = 31;
        final char colorIndicator = 3;

        if (content.indexOf(boldIndicator) == -1
                && content.indexOf(italicIndicator) == -1
                && content.indexOf(underlineIndicator) == -1
                && content.indexOf(colorIndicator) == -1)
            return new SpannableString(content);

        SpannableStringBuilder newString = new SpannableStringBuilder(content);

        int start, end, endSearchOffset, startIndicatorLength, style, fg, bg;
        while (true) {
            content = newString.toString();
            start = -1;
            end = -1;
            startIndicatorLength = 1;
            style = 0;
            fg = -1;
            bg = -1;

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

                if (fg != -1 && mircCodeToColor(fg)!=android.R.color.transparent) {
                    newString.setSpan(new ForegroundColorSpan(context.getResources()
                            .getColor(mircCodeToColor(fg))), start, end,
                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                }
                if (bg != -1 && mircCodeToColor(fg)!=android.R.color.transparent) {
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

        return new SpannableString(newString);
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
                color = R.color.ircmessage_olive;
                break;
            case 8: // yellow
                color = R.color.ircmessage_yellow;
                break;
            case 9: // light green (lime)
                color = R.color.ircmessage_lime_green;
                break;
            case 10: // teal (a green/blue cyan)
                color = R.color.ircmessage_teal;
                break;
            case 11: // light cyan (cyan) (aqua)
                color = R.color.ircmessage_aqua_light;
                break;
            case 12: // light blue (royal)
                color = R.color.ircmessage_royal_blue;
                break;
            case 13: // pink (light purple) (fuchsia)
                color = R.color.ircmessage_pink;
                break;
            case 14: // grey
                color = R.color.ircmessage_dark_gray;
                break;
            case 15: // light grey
                color = R.color.ircmessage_light_gray;
                break;
            default:
                color = android.R.color.transparent;
        }
        return color;
    }


}
