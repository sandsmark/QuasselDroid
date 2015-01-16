package com.iskrembilen.quasseldroid.events;

public class BufferDetailsChangedEvent {

    public final int bufferId;

    public BufferDetailsChangedEvent(int bufferId) {
        this.bufferId = bufferId;
    }
}