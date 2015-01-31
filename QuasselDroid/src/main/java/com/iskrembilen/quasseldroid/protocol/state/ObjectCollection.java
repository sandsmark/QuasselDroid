package com.iskrembilen.quasseldroid.protocol.state;

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
