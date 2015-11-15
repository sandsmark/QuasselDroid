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
