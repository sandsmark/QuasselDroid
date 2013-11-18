/**
 QuasselDroid - Quassel client for Android
 Copyright (C) 2010 Frederik M. J. Vestre
 Copyright (C) 2011 Martin Sandsmark <martin.sandsmark@kde.org>
 Copyright (C) 2011 Ken Børge Viktil

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

package com.iskrembilen.quasseldroid.qtcomm;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum QVariantType {
    Invalid(0),

    Bool(1),
    Int(2),
    UInt(3),
    LongLong(4),
    ULongLong(5),
    Double(6),
    Char(7),
    Map(8),
    List(9),
    String(10),
    StringList(11),
    ByteArray(12),
    BitArray(13),
    Date(14),
    Time(15),
    DateTime(16),
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

    private static final Map<Integer, QVariantType> lookup = new HashMap<Integer, QVariantType>();

    static {
        for (QVariantType s : EnumSet.allOf(QVariantType.class))
            lookup.put(s.getValue(), s);
    }


    QVariantType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static QVariantType getByValue(int value) {
        return lookup.get(value);
    }

}
