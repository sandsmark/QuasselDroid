package com.iskrembilen.quasseldroid;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class BufferUtils {
	public static int compareBuffers(Buffer buffer1, Buffer buffer2) {
		if(buffer1.getInfo().type == BufferInfo.Type.StatusBuffer && buffer2.getInfo().type == BufferInfo.Type.StatusBuffer) {
			return buffer1.getInfo().networkId - buffer2.getInfo().networkId;
		}
		else {

			if (!(buffer1.isTemporarilyHidden() || buffer1.isPermanentlyHidden()) && (buffer2.isTemporarilyHidden() || buffer2.isPermanentlyHidden()))
				return 1;
			else if ((buffer1.isTemporarilyHidden() || buffer1.isPermanentlyHidden()) && !(buffer2.isTemporarilyHidden() || buffer2.isPermanentlyHidden()))
				return -1;
			else if (buffer1.isTemporarilyHidden() && !buffer2.isTemporarilyHidden())
				return -1;
			else if (!buffer1.isTemporarilyHidden() && buffer2.isTemporarilyHidden())
				return 1;
			else if (buffer1.isTemporarilyHidden() && !buffer2.isPermanentlyHidden())
				return -1;
			else if (!buffer1.isTemporarilyHidden() && buffer2.isPermanentlyHidden())
				return 1;
			else if ((buffer1.isTemporarilyHidden() && buffer2.isPermanentlyHidden())||(buffer1.isTemporarilyHidden() && buffer2.isTemporarilyHidden())) {
				if (buffer1.getInfo().type != buffer2.getInfo().type)
					return buffer1.getInfo().type.value - buffer2.getInfo().type.value;
				else return buffer1.getInfo().name.compareToIgnoreCase(buffer2.getInfo().name);
			} else if (!BufferCollection.orderAlphabetical)
				return buffer1.getOrder() - buffer2.getOrder();
			else {
				if (buffer1.getInfo().type != buffer2.getInfo().type)
					return buffer1.getInfo().type.value - buffer2.getInfo().type.value;
				else return buffer1.getInfo().name.compareToIgnoreCase(buffer2.getInfo().name);
			}
		}
	}

	public static void setBufferViewStatus(Context context, Buffer entry, TextView bufferView) {
		//Check here if there are any unread messages in the buffer, and then set this color if there is
		if (entry.hasUnseenHighlight()){
			bufferView.setTextColor(context.getResources().getColor(R.color.buffer_highlight_color));
		} else if (entry.hasUnreadMessage()){
			bufferView.setTextColor(context.getResources().getColor(R.color.buffer_unread_color));
		} else if (entry.hasUnreadActivity()) {
			bufferView.setTextColor(context.getResources().getColor(R.color.buffer_activity_color));
		}else {
			bufferView.setTextColor(context.getResources().getColor(R.color.buffer_read_color));
		}
	}

}
