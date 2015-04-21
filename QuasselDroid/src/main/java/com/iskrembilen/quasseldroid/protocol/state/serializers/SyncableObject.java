package com.iskrembilen.quasseldroid.protocol.state.serializers;

import android.util.Log;

import com.iskrembilen.quasseldroid.events.RequestRemoteSyncEvent;
import com.iskrembilen.quasseldroid.protocol.qtcomm.EmptyQVariantException;
import com.iskrembilen.quasseldroid.protocol.qtcomm.QVariant;
import com.iskrembilen.quasseldroid.protocol.qtcomm.QVariantHelper;
import com.iskrembilen.quasseldroid.protocol.qtcomm.QVariantType;
import com.iskrembilen.quasseldroid.protocol.state.Client;
import com.iskrembilen.quasseldroid.protocol.state.Identity;
import com.iskrembilen.quasseldroid.util.BusProvider;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import de.kuschku.util.HelperUtils;

public abstract class SyncableObject extends Observable {

    public void register() {
        register(getObjectName());
    }

    public void register(String name) {
        Client.getInstance().getObjects().putObject(getClassName(),name,this);
    }

    public void unregister() {
        unregister(getObjectName());
    }

    public void unregister(String name) {
        Client.getInstance().getObjects().removeObject(getClassName(),name);
    }

    public String getClassName() {
        return getClass().getSimpleName();
    }

    public String getObjectName() {
        return String.valueOf(this.hashCode());
    }

    /** Stores the object's state into a QVariantMap.
     *  The default implementation takes dynamic properties as well as getters that have
     *  names starting with "init" and stores them in a QVariantMap. Override this method in
     *  derived classes in order to store the object state in a custom form.
     *         DO NOT OVERRIDE THIS unless you know exactly what you do!
     *
     *  @return The object's state in a QVariantMap
     */
    public QVariant<Map<String,QVariant<?>>> toVariantMap() {
        Map<String,QVariant<?>> map = new HashMap<>();

        // Iterate through all attributes of the class
        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                Syncable annotation = field.getAnnotation(Syncable.class);
                if (annotation!=null) {
                    // If the attribute is annotated as Syncable
                    String name;

                    // If no custom name is specified, use the name of the annotation
                    if (annotation.name().isEmpty())
                        name = field.getName();
                    else
                        name = annotation.name();

                    // Set the field accessible for us
                    field.setAccessible(true);

                    // If the type is usertype, use the usertype instead
                    if (annotation.type()== QVariantType.UserType)
                        map.put(name, new QVariant<>(field.get(this), annotation.userType()));
                    else
                        map.put(name, new QVariant<>(field.get(this), annotation.type()));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return new QVariant<>(map,QVariantType.Map);
    }

    public void fromVariantMap(QVariant<Map<String,QVariant<?>>> packedMap) throws EmptyQVariantException {
        fromVariantMap(packedMap.getData());
    }

    /** Initialize the object's state from a given QVariantMap.
     *  see toVariantMap for important information concerning this method.
     *  @param map Map of field name to QVariant of value for the field
     */
    public void fromVariantMap(Map<String,QVariant<?>> map) throws EmptyQVariantException {
        // Iterate through all attributes of the class
        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                Syncable annotation = field.getAnnotation(Syncable.class);
                // If the attribute is syncable
                if (annotation != null) {
                    // Set the attribute accessible
                    field.setAccessible(true);

                    // Use field name if no custom name is set
                    String name;
                    if (annotation.name().isEmpty()) {
                        name = field.getName();
                    } else {
                        name = annotation.name();
                    }

                    // Set the value from the QVariantMap
                    if (map.containsKey(name)) {
                        field.set(this, map.get(name).getData());
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void fromOther(SyncableObject other) {
        if (this.getClass() != other.getClass())
            throw new IllegalArgumentException("Can’t initialize "+this.getClass().getSimpleName()+" with values from object of type "+other.getClass().getSimpleName());

        // Iterate through all fields in the object
        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                Syncable annotation = field.getAnnotation(Syncable.class);
                // If the field is syncable
                if (annotation!=null) {
                    // Set the attribute accessible for us
                    field.setAccessible(true);
                    // Set the value of the attribute from the other object’s attribute
                    field.set(this,field.get(other));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void execute(String function, QVariant<?>... rawArgs) throws EmptyQVariantException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Object[] args = new Object[rawArgs.length];
        Class[] argTypes = new Class[rawArgs.length];

        for (int i = 0; i< rawArgs.length; i++) {
            args[i] = rawArgs[i].getData();
            argTypes[i] = rawArgs[i].getType().getJavaType();
        }

        Method method = this.getClass().getMethod(function, argTypes);
        method.setAccessible(true);
        method.invoke(this, args);
    }

    protected void sync(Object... args) {
        // If we called the method via a sync call, don’t sync back again
        if (Thread.currentThread().getStackTrace().length>=7) {
            StackTraceElement parentMethod = Thread.currentThread().getStackTrace()[6];
            if (parentMethod.getClassName().equals(SyncableObject.class.getCanonicalName()) && parentMethod.getMethodName().equals("execute"))
                return;
        }

        String callingMethod = Thread.currentThread().getStackTrace()[3].getMethodName();
        Method[] methods = getClass().getMethods();
        QVariantType[] paramTypes = new QVariantType[0];
        String remoteMethodName = callingMethod;
        for (Method method : methods) {
            if (method.getName().equals(callingMethod)) {
                Syncable annotation = method.getAnnotation(Syncable.class);

                if (annotation==null || !annotation.name().isEmpty()) {
                    remoteMethodName = HelperUtils.appendCamelCase("request", callingMethod);
                } else {
                    remoteMethodName = annotation.name();
                }

                if (annotation==null || (annotation.paramTypes().length==0)) {
                    paramTypes = extractTypes(args);
                } else {
                    paramTypes = annotation.paramTypes();
                }

                break;
            }
        }

        sync(new RequestRemoteSyncEvent(getClassName(), getObjectName(), remoteMethodName, args(args, paramTypes)));
    }

    protected void sync(String remoteMethodName, QVariant<?> args) {
        sync(new RequestRemoteSyncEvent(getClassName(), getObjectName(), remoteMethodName, args));
    }

    private QVariantType[] extractTypes(Object[] objects) {
        QVariantType[] types = new QVariantType[objects.length];
        for (int i = 0; i < objects.length; i++) {
            if (types[i] == null)
                types[i] = QVariantHelper.fromJavaType(objects[i]);

        }
        return types;
    }

    private QVariant<?> args(Object[] args, QVariantType[] types) {
        List<QVariant<?>> arguments = new ArrayList<>(args.length);
        for (int i = 0; i < args.length; i++) {
            Object obj = args[i];
            arguments.add(QVariantHelper.box(obj, types[i]));
        }
        if (arguments.size()==1)
            return arguments.get(0);
        else
            return new QVariant<>(arguments, QVariantType.List);
    }

    protected void sync(RequestRemoteSyncEvent event) {
        Log.d(Identity.class.getSimpleName(),"Sending Events");
        BusProvider.getInstance().post(event);
        setChanged();
        notifyObservers();
    }
}