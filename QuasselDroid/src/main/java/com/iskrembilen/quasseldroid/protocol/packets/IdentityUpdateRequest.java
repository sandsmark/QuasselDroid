/*
    QuasselDroid - Quassel client for Android
    Copyright (C) 2015 Ken Børge Viktil
    Copyright (C) 2015 Magnus Fjell
    Copyright (C) 2015 Martin Sandsmark <martin.sandsmark@kde.org>

    This program is free software: you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation, either version 3 of the License, or (at your option)
    any later version, or under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either version 2.1 of
    the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License and the
    GNU Lesser General Public License along with this program.  If not, see
    <http://www.gnu.org/licenses/>.
 */

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
