package com.iskrembilen.quasseldroid.events;

public class UserClickedEvent {
    public final String nick;
    public final int bufferId;

    public UserClickedEvent(int bufferId, String nick) {
        this.bufferId = bufferId;
        this.nick = nick;
    }
}