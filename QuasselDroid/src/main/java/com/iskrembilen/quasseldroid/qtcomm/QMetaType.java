/**
 QuasselDroid - Quassel client for Android
 Copyright (C) 2010 Frederik M. J. Vestre

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

public class QMetaType<T extends Object> extends Object {
    public enum Type {
        //%s/\(\S\+\) = \(\d\+\),/\1(\2),\r/g
        // these are merged with QVariant
        Void(0),
        Bool(1),
        Int(2),
        UInt(3),
        LongLong(4),
        ULongLong(5),

        Double(6),
        QChar(7),
        QVariantMap(8),
        QVariantList(9),

        QString(10),
        QStringList(11),
        QByteArray(12),

        QBitArray(13),
        QDate(14),
        QTime(15),
        QDateTime(16),
        QUrl(17),

        QLocale(18),
        QRect(19),
        QRectF(20),
        QSize(21),
        QSizeF(22),

        QLine(23),
        QLineF(24),
        QPoint(25),
        QPointF(26),
        QRegExp(27),

        QVariantHash(28),
        QEasingCurve(29),
        LastCoreType(29),

        FirstGuiType(63), /* QColorGroup */
        //#ifdef QT3_SUPPORT
        QColorGroup(63),

        //#endif
        QFont(64),
        QPixmap(65),
        QBrush(66),
        QColor(67),
        QPalette(68),

        QIcon(69),
        QImage(70),
        QPolygon(71),
        QRegion(72),
        QBitmap(73),

        QCursor(74),
        QSizePolicy(75),
        QKeySequence(76),
        QPen(77),

        QTextLength(78),
        QTextFormat(79),
        QMatrix(80),
        QTransform(81),

        QMatrix4x4(82),
        QVector2D(83),
        QVector3D(84),
        QVector4D(85),

        QQuaternion(86),

        LastGuiType(86),

        FirstCoreExtType(128),
        /* VoidStar */
        VoidStar(128),
        Long(129),
        Short(130),
        Char(131),
        ULong(132),

        UShort(133),
        UChar(134),
        Float(135),
        QObjectStar(136),
        QWidgetStar(137),

        QVariant(138),

        LastCoreExtType(138),

        QReal(6),//double
        User(256),
        UserType(127);//From qvariant

        int value;

        Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    ;

    int id;
    String name;
    QMetaTypeSerializer<T> serializer;

    public QMetaType(int id, String name) {
        this.id = id;
        this.name = name;

    }

    public QMetaType(int id, String name, QMetaTypeSerializer<T> serializer) {
        this.id = id;
        this.name = name;
        this.serializer = serializer;

    }

    public QMetaTypeSerializer<T> getSerializer() {
        if (serializer == null) {
            System.err.println("Unimplemented serializer!: " + this.name);
        }

        return serializer;
    }
}