package com.iskrembilen.quasseldroid.events;

public class ManageNetworkEvent {
    public enum NetworkAction {
        CONNECT, DISCONNECT;
    }

    public final int networkId;
    public final NetworkAction action;

    public ManageNetworkEvent(int networkId, NetworkAction action) {
        this.networkId = networkId;
        this.action = action;
    }

}
