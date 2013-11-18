package com.iskrembilen.quasseldroid.events;

import com.iskrembilen.quasseldroid.IrcMessage.Type;

public class FilterMessagesEvent {

    public final int bufferId;
    public final boolean filtered;
    public final Type filterType;

    public FilterMessagesEvent(int bufferId, Type type, boolean filtered) {
        this.bufferId = bufferId;
        this.filtered = filtered;
        this.filterType = type;
    }

}
