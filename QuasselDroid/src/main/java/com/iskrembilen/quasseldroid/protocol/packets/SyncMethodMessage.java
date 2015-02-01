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

import com.iskrembilen.quasseldroid.protocol.qtcomm.EmptyQVariantException;
import com.iskrembilen.quasseldroid.protocol.qtcomm.QVariant;
import com.iskrembilen.quasseldroid.protocol.state.Client;
import com.iskrembilen.quasseldroid.protocol.state.serializers.SyncableObject;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class SyncMethodMessage implements Request {
    private SyncableObject object;
    private QVariant<?>[] args;
    private String functionName;

    public void from(SyncableObject object, String functionName, List<QVariant<?>> args) {
        from(object, functionName, args.toArray(new QVariant[args.size()]));
    }

    private void from(SyncableObject object, String functionName, QVariant[] args) {
        this.object = object;
        this.args = args;
        this.functionName = functionName;
    }

    public void from(String className, String objectName, String functionName, List<QVariant<?>> args) {
        from(className,objectName,functionName,args.toArray(new QVariant[args.size()]));
    }

    public void from(String className, String objectName, String functionName, QVariant[] args) {
        SyncableObject obj = Client.getInstance().getObjects().getObject(className, objectName);
        if (obj==null)
            throw  new NullPointerException(className+"::"+functionName+" couldn’t be executed on nonexistant object "+objectName);

        from(obj, functionName, args);
    }

    @Override
    public void apply() {
        try {
            object.execute(this.functionName, this.args);
        } catch (InvocationTargetException|EmptyQVariantException|IllegalAccessException|NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
