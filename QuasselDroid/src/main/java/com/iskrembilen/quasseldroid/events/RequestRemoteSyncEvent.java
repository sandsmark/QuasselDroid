package com.iskrembilen.quasseldroid.events;

import com.iskrembilen.quasseldroid.protocol.qtcomm.QVariant;

import java.util.List;

/**
 * Created by kuschku on 31.01.15.
 */
public class RequestRemoteSyncEvent {
    public final String className;
    public final String objectName;
    public final String functionName;
    public final QVariant<?> args;

    public RequestRemoteSyncEvent(String className, String objectName, String functionName, QVariant<?> args) {
        this.className = className;
        this.objectName = objectName;
        this.functionName = functionName;
        this.args = args;
    }
}
