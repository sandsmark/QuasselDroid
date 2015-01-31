package com.iskrembilen.quasseldroid.protocol.qtcomm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QVariantHelper {

    public static QVariant box(Object obj) {
        return box(obj, null);
    }

    public static QVariant box(Object obj, QVariantType type) {
        if (type == null) {
            type = fromJavaType(obj);
            if (type == null) {
                return (QVariant) obj;
            }
        }
        switch (type) {
            case Map:
                Map copyMap = new HashMap<>();
                Map<String,?> originalMap = (Map<String, ?>) obj;
                for (Map.Entry<String,?> e : originalMap.entrySet()) {
                    copyMap.put(e.getKey(),box(e.getValue()));
                }
                return new QVariant(copyMap, QVariantType.Map);
            case List:
                List originalList = (List) obj;
                List copyList = new ArrayList(originalList.size());
                for (Object elem : originalList) {
                    copyList.add(box(elem));
                }
                return new QVariant(copyList, QVariantType.List);
            default:
                return new QVariant(obj, type);
        }
    }

    public static QVariantType fromJavaType(Object obj) {
        switch (obj.getClass().getSimpleName()) {
            case "boolean":
            case "Boolean":
                return QVariantType.Bool;
            case "byte":
            case "Byte":
            case "short":
            case "Short":
            case "int":
            case "Integer":
                return QVariantType.Int;
            case "long":
            case "Long":
                return QVariantType.LongLong;
            case "float":
            case "Float":
            case "double":
            case "Double":
                return QVariantType.Double;
            case "char":
            case "Char":
                return QVariantType.Char;
            case "Calendar":
                return QVariantType.DateTime;
            case "Map":
            case "HashMap":
                return QVariantType.Map;
            case "List":
                if (((List) obj).size()>0 && ((List) obj).get(0).getClass()==String.class)
                    return QVariantType.StringList;
                else
                    return QVariantType.List;
            case "Void":
            case "null":
                return QVariantType.Invalid;
            case "QVariant":
                return null;
            default:
                throw new IllegalArgumentException("Type not existant: "+obj.getClass().getSimpleName());
        }
    }
}
