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

import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import de.kuschku.util.BetterSparseArray;

public enum QVariantType {
    Invalid(0,Void.class),

    Bool(1, boolean.class),
    Int(2, int.class),
    UInt(3, int.class),
    LongLong(4, long.class),
    ULongLong(5, long.class),
    Double(6, double.class),
    Char(7, char.class),
    Map(8, java.util.Map.class),
    List(9, java.util.List.class),
    String(10, String.class),
    StringList(11, java.util.List.class),
    ByteArray(12, byte[].class),
    BitArray(13, byte[].class),
    Date(14),
    Time(15, Calendar.class),
    DateTime(16, Calendar.class),
    Url(17),
    Locale(18),
    Rect(19),
    RectF(20),
    Size(21),
    SizeF(22),
    Line(23),
    LineF(24),
    Point(25),
    PointF(26),
    RegExp(27),
    Hash(28),
    EasingCurve(29),
    LastCoreType(29),

    // value 62 is internally reserved
    //#ifdef QT3_SUPPORT
    ColorGroup(63),
    //#endif
    Font(64),
    Pixmap(65),
    Brush(66),
    Color(67),
    Palette(68),
    Icon(69),
    Image(70),
    Polygon(71),
    Region(72),
    Bitmap(73),
    Cursor(74),
    SizePolicy(75),
    KeySequence(76),
    Pen(77),
    TextLength(78),
    TextFormat(79),
    Matrix(80),
    Transform(81),
    Matrix4x4(82),
    Vector2D(83),
    Vector3D(84),
    Vector4D(85),
    Quaternion(86),
    LastGuiType(86),

    UserType(127),
    //#ifdef QT3_SUPPORT
    IconSet(69),
    CString(12),
    PointArray(71),
    //#endif
    UShort(133),
    LastType(0xffffffff);

    int value;
    Class javaType;

    private static final Map<Integer, QVariantType> lookup = new BetterSparseArray<>(values().length);

    static {
        for (QVariantType s : EnumSet.allOf(QVariantType.class))
            lookup.put(s.getValue(), s);
    }

    QVariantType(int value) {
        this(value, Void.class);
    }

    QVariantType(int value, Class javaType) {
        this.value = value;
        this.javaType = javaType;
    }

    public int getValue() {
        return value;
    }

    public Class getJavaType() {
        return javaType;
    }

    public static QVariantType getByValue(int value) {
        return lookup.get(value);
    }
}
