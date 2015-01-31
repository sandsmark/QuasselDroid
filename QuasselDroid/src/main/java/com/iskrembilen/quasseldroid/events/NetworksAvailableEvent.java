package com.iskrembilen.quasseldroid.events;

import com.iskrembilen.quasseldroid.protocol.state.NetworkCollection;

public class NetworksAvailableEvent {

    public final NetworkCollection networks;

    public NetworksAvailableEvent(NetworkCollection networks) {
        this.networks = networks;
    }

}
