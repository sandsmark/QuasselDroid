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

package com.iskrembilen.quasseldroid.protocol.state;

import android.util.Log;

import com.iskrembilen.quasseldroid.protocol.state.serializers.SyncableObject;

import java.util.HashMap;
import java.util.Map;

public class ObjectCollection {

    public ObjectCollection() {
        objects = new HashMap<>();
    }

    private Map<String, Map<String, SyncableObject>> objects;

    public SyncableObject getObject(String className, String objectName) {
        if (objects.get(className)==null)
            objects.put(className,new HashMap<String,SyncableObject>());

        return objects.get(className).get(objectName);
    }

    public void putObject(String className, String objectName, SyncableObject object) {
        if (objects.get(className)==null)
            objects.put(className,new HashMap<String, SyncableObject>());

        objects.get(className).put(objectName, object);
    }

    public void renameObject(String className, String oldObjectName, String newObjectName) {
        if (objects.get(className)==null)
            objects.put(className,new HashMap<String, SyncableObject>());

        if (objects.get(className).containsKey(oldObjectName))
            objects.get(className).put(newObjectName, objects.get(className).remove(oldObjectName));
    }

    public void removeObject(String className, String objectName) {
        if (objects.get(className)==null)
            objects.put(className,new HashMap<String, SyncableObject>());

        objects.get(className).remove(objectName);
    }

    public Map<String, SyncableObject> getObjects(String className) {
        if (objects.get(className)==null)
            objects.put(className,new HashMap<String, SyncableObject>());

        return objects.get(className);
    }
}
