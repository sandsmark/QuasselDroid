package com.iskrembilen.quasseldroid.events;

import com.iskrembilen.quasseldroid.qtcomm.QVariant;

public class RequestCreateIdentityEvent {
    public final int identityId;
    public final QVariant<?> identity;

    public RequestCreateIdentityEvent(int identityId, QVariant<?> identity) {
        this.identityId = identityId;
        this.identity = identity;
    }

    public RequestCreateIdentityEvent(int identityId, QVariant<?> identity, QVariant<?> ssldata) {
        this.identityId = identityId;
        this.identity = identity;
    }
}
