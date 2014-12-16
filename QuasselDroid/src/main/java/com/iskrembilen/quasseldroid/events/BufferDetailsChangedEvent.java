package com.iskrembilen.quasseldroid.events;

/**
 * Created by kuschku on 12/16/14.
 */
public class BufferDetailsChangedEvent {

    public final int bufferId;

    public BufferDetailsChangedEvent(int bufferId) {
        this.bufferId = bufferId;
    }
}