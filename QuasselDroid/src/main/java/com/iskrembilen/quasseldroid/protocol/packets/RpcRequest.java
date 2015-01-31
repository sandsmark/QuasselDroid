package com.iskrembilen.quasseldroid.protocol.packets;

import com.iskrembilen.quasseldroid.protocol.qtcomm.EmptyQVariantException;
import com.iskrembilen.quasseldroid.protocol.qtcomm.QVariant;

public interface RpcRequest extends Request {
    public void setArgs(QVariant<?>[] args) throws EmptyQVariantException;
}
