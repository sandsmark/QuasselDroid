package com.iskrembilen.quasseldroid.events;

public class RequestRemoveIdentityEvent {
    public final int identityId;

    public RequestRemoveIdentityEvent(int identityId) {
        this.identityId = identityId;
    }
}
