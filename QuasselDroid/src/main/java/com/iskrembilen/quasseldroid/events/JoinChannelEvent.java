package com.iskrembilen.quasseldroid.events;

public class JoinChannelEvent {

    public final String networkName;
    public final String channelName;

    public JoinChannelEvent(String networkName, String channelName) {
        this.networkName = networkName;
        this.channelName = channelName;
    }

}
