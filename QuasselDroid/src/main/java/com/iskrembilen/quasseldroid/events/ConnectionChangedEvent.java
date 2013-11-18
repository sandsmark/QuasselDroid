package com.iskrembilen.quasseldroid.events;

public class ConnectionChangedEvent {

    public enum Status {
        Connected, Connecting, Disconnected;
    }

    public final Status status;
    public final String reason;

    public ConnectionChangedEvent(Status status) {
        this(status, "");
    }

    public ConnectionChangedEvent(Status status, String reason) {
        this.status = status;
        this.reason = reason;
    }
}
