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
