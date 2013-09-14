package com.iskrembilen.quasseldroid.events;

public class QueryUserEvent {
    public final String nick;
    public final int bufferId;

    public QueryUserEvent(int bufferId, String nick) {
        this.bufferId = bufferId;
        this.nick = nick;
    }

}