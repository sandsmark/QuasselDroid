package com.iskrembilen.quasseldroid.events;

public class BufferOpenedEvent {

    public final int bufferId;
    public final boolean switchToBuffer;

    public BufferOpenedEvent(int bufferId, boolean switchToBuffer) {
        this.bufferId = bufferId;
        this.switchToBuffer = switchToBuffer;
    }

    public BufferOpenedEvent(int bufferId) {
        this(bufferId, true);
    }

}
