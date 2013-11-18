package com.iskrembilen.quasseldroid.events;

public class InitProgressEvent {

    public final String progress;
    public final Boolean done;

    public InitProgressEvent(boolean done, String progress) {
        this.progress = progress;
        this.done = done;
    }
}
