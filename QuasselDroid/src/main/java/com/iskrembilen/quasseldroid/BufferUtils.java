package com.iskrembilen.quasseldroid;

import android.content.Context;
import android.widget.TextView;

import com.iskrembilen.quasseldroid.util.ThemeUtil;

public class BufferUtils {
    public static int compareBuffers(Buffer buffer1, Buffer buffer2) {
        if (!(buffer1.isTemporarilyHidden() || buffer1.isPermanentlyHidden()) && (buffer2.isTemporarilyHidden() || buffer2.isPermanentlyHidden()))
            return -1;
        if ((buffer1.isTemporarilyHidden() || buffer1.isPermanentlyHidden()) && !(buffer2.isTemporarilyHidden() || buffer2.isPermanentlyHidden()))
            return 1;
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
        if (entry == null || !entry.isActive()) {
            bufferView.setTextColor(ThemeUtil.bufferPartedColor);
        } else if (entry.hasUnseenHighlight()) {
            bufferView.setTextColor(ThemeUtil.bufferHighlightColor);
        } else if (entry.hasUnreadMessage()) {
            bufferView.setTextColor(ThemeUtil.bufferUnreadColor);
        } else if (entry.hasUnreadActivity()) {
            bufferView.setTextColor(ThemeUtil.bufferActivityColor);
        } else {
            bufferView.setTextColor(ThemeUtil.bufferReadColor);
        }
    }

}
