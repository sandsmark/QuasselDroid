package com.iskrembilen.quasseldroid.protocol.packets;

import com.iskrembilen.quasseldroid.protocol.qtcomm.EmptyQVariantException;
import com.iskrembilen.quasseldroid.protocol.qtcomm.QVariant;

public interface SyncRequest<D> extends Request {
    public void from(QVariant<D> reference) throws EmptyQVariantException;
}
