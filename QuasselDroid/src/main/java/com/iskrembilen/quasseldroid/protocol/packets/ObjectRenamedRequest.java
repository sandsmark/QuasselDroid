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

import android.util.Log;

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
        Log.d("ObjectRenamedRequest", "rename object of type" + className + " from " + oldObjectName + " to " + newObjectName);
    }

    @Override
    public void setArgs(QVariant<?>[] args) throws EmptyQVariantException {
        className = (String) args[0].getData();
        newObjectName = (String) args[1].getData();
        oldObjectName = (String) args[2].getData();
    }
}
