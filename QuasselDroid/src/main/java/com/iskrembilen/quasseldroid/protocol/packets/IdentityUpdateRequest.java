package com.iskrembilen.quasseldroid.protocol.packets;

import com.iskrembilen.quasseldroid.protocol.state.Client;
import com.iskrembilen.quasseldroid.protocol.state.Identity;
import com.iskrembilen.quasseldroid.protocol.qtcomm.EmptyQVariantException;
import com.iskrembilen.quasseldroid.protocol.qtcomm.QVariant;

import java.util.Map;

public class IdentityUpdateRequest implements SyncRequest<Map<String,QVariant<?>>> {
    private Identity identity;

    @Override
    public void from(QVariant<Map<String,QVariant<?>>> data) throws EmptyQVariantException {
        identity = new Identity();
        identity.fromVariantMap(data);
    }

    @Override
    public void apply() {
        Client.getInstance().getObjects().putObject("Identity",String.valueOf(identity.getIdentityId()),identity);
    }
}
