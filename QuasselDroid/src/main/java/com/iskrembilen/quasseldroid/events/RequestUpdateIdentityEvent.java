package com.iskrembilen.quasseldroid.events;

import com.iskrembilen.quasseldroid.qtcomm.QVariant;

public class RequestUpdateIdentityEvent {
    public final String method;
    public final QVariant<?> data;
    public final int identityId;

    public RequestUpdateIdentityEvent(int identityId, String method, QVariant<?> data) {
        this.identityId = identityId;
        this.method = method;
        this.data = data;
    }
}