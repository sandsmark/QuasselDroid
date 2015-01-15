package com.iskrembilen.quasseldroid.events;

import com.iskrembilen.quasseldroid.Identity;

public class UpdateIdentityEvent {
    public final Identity identity;

    public UpdateIdentityEvent(Identity identity) {
        this.identity = identity;
    }
}
