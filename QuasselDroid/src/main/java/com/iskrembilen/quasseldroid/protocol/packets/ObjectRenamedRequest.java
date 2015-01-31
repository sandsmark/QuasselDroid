package com.iskrembilen.quasseldroid.protocol.packets;

import com.iskrembilen.quasseldroid.protocol.qtcomm.EmptyQVariantException;
import com.iskrembilen.quasseldroid.protocol.qtcomm.QVariant;
import com.iskrembilen.quasseldroid.protocol.state.Client;

public class ObjectRenamedRequest implements RpcRequest {
    private String className;
    private String oldObjectName;
    private String newObjectName;

    @Override
    public void apply() {
        Client.getInstance().getObjects().renameObject(className, oldObjectName, newObjectName);
    }

    @Override
    public void setArgs(QVariant<?>[] args) throws EmptyQVariantException {
        className = (String) args[0].getData();
        newObjectName = (String) args[1].getData();
        oldObjectName = (String) args[2].getData();
    }
}
