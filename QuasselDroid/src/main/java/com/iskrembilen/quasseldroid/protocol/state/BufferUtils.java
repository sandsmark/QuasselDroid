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

package com.iskrembilen.quasseldroid.protocol.state;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.util.ThemeUtil;

public class BufferUtils {
    public static int compareBuffers(@NonNull Buffer buffer1, @NonNull Buffer buffer2) {
        if (!(buffer1.isTemporarilyHidden() || buffer1.isPermanentlyHidden()) && (buffer2.isTemporarilyHidden() || buffer2.isPermanentlyHidden()))
            return -1;
        if ((buffer1.isTemporarilyHidden() || buffer1.isPermanentlyHidden()) && !(buffer2.isTemporarilyHidden() || buffer2.isPermanentlyHidden()))
            return 1;
        // FIXME: (buffer2.isTemporarilyHidden() || buffer2.isPermanentlyHidden()) will always be true at this point
        if ((buffer1.isTemporarilyHidden() || buffer1.isPermanentlyHidden()) && (buffer2.isTemporarilyHidden() || buffer2.isPermanentlyHidden())) {
            if (buffer1.getInfo().type != buffer2.getInfo().type)
                return buffer1.getInfo().type.value - buffer2.getInfo().type.value;
        }
        if (buffer1.isPermanentlyHidden() && !buffer2.isPermanentlyHidden())
            return 1;
        if (!buffer1.isPermanentlyHidden() && buffer2.isPermanentlyHidden())
            return -1;
        if (buffer1.isTemporarilyHidden() && !buffer2.isTemporarilyHidden())
            return 1;
        if (!buffer1.isTemporarilyHidden() && buffer2.isTemporarilyHidden())
            return -1;
        // FIXME: buffer2.isPermanentlyHidden() will always be true at this point
        if ((buffer1.isPermanentlyHidden() && buffer2.isPermanentlyHidden()) || (buffer1.isTemporarilyHidden() && buffer2.isTemporarilyHidden())) {
            return buffer1.getInfo().name.compareToIgnoreCase(buffer2.getInfo().name);
        }
        if (!BufferCollection.orderAlphabetical)
            return buffer1.getOrder() - buffer2.getOrder();
        else {
            if (buffer1.getInfo().type != buffer2.getInfo().type)
                return buffer1.getInfo().type.value - buffer2.getInfo().type.value;
            else return buffer1.getInfo().name.compareToIgnoreCase(buffer2.getInfo().name);
        }
        //		}
    }

    public static void setBufferViewStatus(Context context, Buffer entry, TextView bufferView) {
        //Check here if there are any unread messages in the buffer, and then set this color if there is
        if (entry == null) {
            bufferView.setTextColor(ThemeUtil.Color.bufferParted);
        } else if (entry.isDisplayed()) {
            bufferView.setTextColor(ThemeUtil.Color.bufferFocused);
        } else if (entry.hasUnseenHighlight()) {
            bufferView.setTextColor(ThemeUtil.Color.bufferHighlight);
        } else if (entry.hasUnreadMessage()) {
            bufferView.setTextColor(ThemeUtil.Color.bufferUnread);
        } else if (entry.hasUnreadActivity()) {
            bufferView.setTextColor(ThemeUtil.Color.bufferActivity);
        } else if (!entry.isActive()) {
            bufferView.setTextColor(ThemeUtil.Color.bufferParted);
        } else {
            bufferView.setTextColor(ThemeUtil.Color.bufferRead);
        }
    }

    public static int getBufferIconColor(Context context, Buffer entry) {
        if (entry == null) return ThemeUtil.Color.bufferStateParted;

        if(entry.isPermanentlyHidden()){
            return ThemeUtil.Color.bufferStatePerm;
        } else if (entry.isTemporarilyHidden()) {
            return ThemeUtil.Color.bufferStateTemp;
        } else {
            switch (entry.getInfo().type) {
                case StatusBuffer:
                case ChannelBuffer:
                    if (entry.isActive()) {
                        return ThemeUtil.Color.bufferStateActive;
                    } else {
                        return ThemeUtil.Color.bufferStateParted;
                    }
                case QueryBuffer:
                    if (!Client.getInstance().getNetworks().getNetworkById(entry.getInfo().networkId).hasNick(entry.getInfo().name)) {
                        return ThemeUtil.Color.bufferStateParted;
                    } else if (Client.getInstance().getNetworks().getNetworkById(entry.getInfo().networkId).getUserByNick(entry.getInfo().name).away) {
                        return ThemeUtil.Color.bufferStateAway;
                    } else {
                        return ThemeUtil.Color.bufferStateActive;
                    }
                default:
                    return ThemeUtil.Color.bufferStateParted;
            }
        }
    }

    public static Drawable getBufferIcon(Context context, Buffer entry) {
        if (entry == null) return context.getResources().getDrawable(R.drawable.ic_status_offline);

        switch (entry.getInfo().type) {
            case QueryBuffer:
                if (Client.getInstance().getNetworks().getNetworkById(entry.getInfo().networkId).hasNick(entry.getInfo().name))
                    return context.getResources().getDrawable(R.drawable.ic_status);
                else
                    return context.getResources().getDrawable(R.drawable.ic_status_offline);
            case StatusBuffer:
            case ChannelBuffer:
                if (entry.isActive())
                    return context.getResources().getDrawable(R.drawable.ic_status_channel);
                else
                    return context.getResources().getDrawable(R.drawable.ic_status_channel_offline);
            default:
                if (entry.isActive())
                    return context.getResources().getDrawable(R.drawable.ic_status);
                else
                    return context.getResources().getDrawable(R.drawable.ic_status_offline);
        }
    }

    public static void setBufferActive(Buffer entry) {
        if (entry == null) return;

        if (entry.getInfo().type== BufferInfo.Type.QueryBuffer) {
            String nick = entry.getInfo().name;
            Network net = Client.getInstance().getNetworks().getNetworkById(entry.getInfo().networkId);
            entry.setActive(net.hasNick(nick));
        }
    }
}
