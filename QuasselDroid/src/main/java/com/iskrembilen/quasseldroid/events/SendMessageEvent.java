package com.iskrembilen.quasseldroid.events;

public class SendMessageEvent {

    public final String message;
    public final int bufferId;

    public SendMessageEvent(int bufferId, String message) {
        this.message = message;
        this.bufferId = bufferId;
    }

}
