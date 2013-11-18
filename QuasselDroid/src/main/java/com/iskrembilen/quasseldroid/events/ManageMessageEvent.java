package com.iskrembilen.quasseldroid.events;

public class ManageMessageEvent {
    public enum MessageAction {
        MARKER_LINE, LAST_SEEN;
    }

    public final int bufferId;
    public final int messageId;
    public final MessageAction action;


    public ManageMessageEvent(int bufferId, int messageId, MessageAction action) {
        this.bufferId = bufferId;
        this.messageId = messageId;
        this.action = action;
    }

}
