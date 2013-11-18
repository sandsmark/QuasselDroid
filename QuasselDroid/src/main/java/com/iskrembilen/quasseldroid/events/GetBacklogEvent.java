package com.iskrembilen.quasseldroid.events;

public class GetBacklogEvent {
    public final int bufferId;
    public final int backlogAmount;

    public GetBacklogEvent(int bufferId, int backlogAmount) {
        this.bufferId = bufferId;
        this.backlogAmount = backlogAmount;
    }
}
