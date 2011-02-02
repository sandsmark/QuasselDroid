/**
 * Copyright Frederik M.J.V. 2010 
 * Copyright Martin Sandsmark 2011
 * LGPL 2.1 / GPLv3
 */
package com.lekebilen.quasseldroid.qtcomm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.lekebilen.quasseldroid.BufferInfo;
import com.lekebilen.quasseldroid.qtcomm.QMetaType.Type;
import com.lekebilen.quasseldroid.qtcomm.serializers.Bool;
import com.lekebilen.quasseldroid.qtcomm.serializers.QByteArray;
import com.lekebilen.quasseldroid.qtcomm.serializers.QDateTime;
import com.lekebilen.quasseldroid.qtcomm.serializers.QInteger;
import com.lekebilen.quasseldroid.qtcomm.serializers.QList;
import com.lekebilen.quasseldroid.qtcomm.serializers.QMap;
import com.lekebilen.quasseldroid.qtcomm.serializers.QString;
import com.lekebilen.quasseldroid.qtcomm.serializers.UnsignedInteger;
import com.lekebilen.quasseldroid.qtcomm.serializers.quassel.BufferInfoSerializer;


public class QMetaTypeRegistry {
	static QMetaTypeRegistry singleton = null;
	List<QMetaType<?>> types = new ArrayList<QMetaType<?>>();  
	private QMetaTypeRegistry(){
		//:%s/QT_ADD_STATIC_METATYPE(\(\"[^\"]\+\"\)\, QMetaType::\([^)]\+\)),/types.add(new QMetaType(QMetaType.Type.\2.getValue(),\1));/g
	    types.add(new QMetaType<java.lang.Void>(QMetaType.Type.Void.getValue(), "void", new com.lekebilen.quasseldroid.qtcomm.serializers.Void()));
	    types.add(new QMetaType<Boolean>(QMetaType.Type.Bool.getValue(),"bool", new Bool()));
	    types.add(new QMetaType<Integer>(QMetaType.Type.Int.getValue(),"int", new QInteger()));
	    types.add(new QMetaType<Integer>(QMetaType.Type.UserType.getValue(), "BufferId", new QInteger()));
	    types.add(new QMetaType<Integer>(QMetaType.Type.UserType.getValue(),"NetworkId", new QInteger()));
	    types.add(new QMetaType<Long>(QMetaType.Type.UInt.getValue(),"uint", new UnsignedInteger(32)));
	    types.add(new QMetaType<Long>(QMetaType.Type.UShort.getValue(),"ushort", new UnsignedInteger(16)));
	    types.add(new QMetaType<Map<String, QVariant<?>>>(QMetaType.Type.UserType.getValue(), "Identity", new QMap<String, QVariant<?>>("QString", "QVariant")));
	    types.add(new QMetaType<Integer>(QMetaType.Type.UserType.getValue(),"IdentityId", new QInteger()));
	    types.add(new QMetaType<BufferInfo>(QMetaType.Type.UserType.getValue(),"BufferInfo", new BufferInfoSerializer()));

	    
	    types.add(new QMetaType<Object>(QMetaType.Type.LongLong.getValue(),"qlonglong"));
	    types.add(new QMetaType<Object>(QMetaType.Type.ULongLong.getValue(),"qulonglong"));
	    types.add(new QMetaType<Object>(QMetaType.Type.Double.getValue(),"double"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QChar.getValue(),"QChar"));
	    types.add(new QMetaType<Map<String, QVariant<?>>>(QMetaType.Type.QVariantMap.getValue(),"QVariantMap", new QMap<String, QVariant<?>>("QString", "QVariant")));
	    types.add(new QMetaType<List<QVariant<?>> >(QMetaType.Type.QVariantList.getValue(),"QVariantList", new QList<QVariant<?>>("QVariant")));
	    types.add(new QMetaType<String>(QMetaType.Type.QString.getValue(),"QString", new QString()));
	    types.add(new QMetaType<List<String> >(QMetaType.Type.QStringList.getValue(),"QStringList", new QList<String>("QString")));
	    types.add(new QMetaType<Object>(QMetaType.Type.QStringList.getValue(),"QStringList"));
	    types.add(new QMetaType<ByteBuffer>(QMetaType.Type.QByteArray.getValue(),"QByteArray", new QByteArray()));
	    types.add(new QMetaType<Object>(QMetaType.Type.QBitArray.getValue(),"QBitArray"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QDate.getValue(),"QDate"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QTime.getValue(),"QTime"));
	    types.add(new QMetaType<Calendar>(QMetaType.Type.QDateTime.getValue(),"QDateTime", new QDateTime()));
	    types.add(new QMetaType<Object>(QMetaType.Type.QUrl.getValue(),"QUrl"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QLocale.getValue(),"QLocale"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QRect.getValue(),"QRect"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QRectF.getValue(),"QRectF"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QSize.getValue(),"QSize"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QSizeF.getValue(),"QSizeF"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QLine.getValue(),"QLine"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QLineF.getValue(),"QLineF"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QPoint.getValue(),"QPoint"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QPointF.getValue(),"QPointF"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QRegExp.getValue(),"QRegExp"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QVariantHash.getValue(),"QVariantHash"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QEasingCurve.getValue(),"QEasingCurve"));

	    /* All GUI types */
	    types.add(new QMetaType<Object>(QMetaType.Type.QColorGroup.getValue(),"QColorGroup"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QFont.getValue(),"QFont"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QPixmap.getValue(),"QPixmap"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QBrush.getValue(),"QBrush"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QColor.getValue(),"QColor"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QPalette.getValue(),"QPalette"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QIcon.getValue(),"QIcon"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QImage.getValue(),"QImage"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QPolygon.getValue(),"QPolygon"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QRegion.getValue(),"QRegion"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QBitmap.getValue(),"QBitmap"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QCursor.getValue(),"QCursor"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QSizePolicy.getValue(),"QSizePolicy"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QKeySequence.getValue(),"QKeySequence"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QPen.getValue(),"QPen"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QTextLength.getValue(),"QTextLength"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QTextFormat.getValue(),"QTextFormat"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QMatrix.getValue(),"QMatrix"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QTransform.getValue(),"QTransform"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QMatrix4x4.getValue(),"QMatrix4x4"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QVector2D.getValue(),"QVector2D"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QVector3D.getValue(),"QVector3D"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QVector4D.getValue(),"QVector4D"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QQuaternion.getValue(),"QQuaternion"));

	    /* All Metatype builtins */
	    types.add(new QMetaType<Object>(QMetaType.Type.VoidStar.getValue(),"void*"));
	    types.add(new QMetaType<Object>(QMetaType.Type.Long.getValue(),"long"));
	    types.add(new QMetaType<Object>(QMetaType.Type.Short.getValue(),"short"));
	    types.add(new QMetaType<Object>(QMetaType.Type.Char.getValue(),"char"));
	    types.add(new QMetaType<Object>(QMetaType.Type.ULong.getValue(),"ulong"));
	    types.add(new QMetaType<Object>(QMetaType.Type.UChar.getValue(),"uchar"));
	    types.add(new QMetaType<Object>(QMetaType.Type.Float.getValue(),"float"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QObjectStar.getValue(),"QObject*"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QWidgetStar.getValue(),"QWidget*"));
	    types.add(new QMetaType<QVariant<Object>>(QMetaType.Type.QVariant.getValue(),"QVariant", new QVariant.QVariantSerializer<Object>()));	    

	    /* Type aliases - order doesn't matter */
	    types.add(new QMetaType<Object>(QMetaType.Type.ULong.getValue(),"unsigned long"));
	    types.add(new QMetaType<Object>(QMetaType.Type.UInt.getValue(),"unsigned int"));
	    types.add(new QMetaType<Object>(QMetaType.Type.UShort.getValue(),"unsigned short"));
	    types.add(new QMetaType<Object>(QMetaType.Type.UChar.getValue(),"unsigned char"));
	    types.add(new QMetaType<Object>(QMetaType.Type.LongLong.getValue(),"long long"));
	    types.add(new QMetaType<Object>(QMetaType.Type.ULongLong.getValue(),"unsigned long long"));
	    types.add(new QMetaType<Object>(QMetaType.Type.Char.getValue(),"qint8"));
	    types.add(new QMetaType<Long>(QMetaType.Type.UChar.getValue(),"quint8", new UnsignedInteger(8)));
	    types.add(new QMetaType<Object>(QMetaType.Type.Short.getValue(),"qint16"));
	    types.add(new QMetaType<Long>(QMetaType.Type.UShort.getValue(),"quint16", new UnsignedInteger(16)));
	    types.add(new QMetaType<Object>(QMetaType.Type.Int.getValue(),"qint32"));
	    types.add(new QMetaType<Long>(QMetaType.Type.UInt.getValue(),"quint32", new UnsignedInteger(32)));
	    types.add(new QMetaType<Object>(QMetaType.Type.LongLong.getValue(),"qint64"));
	    types.add(new QMetaType<Long>(QMetaType.Type.ULongLong.getValue(),"quint64", new UnsignedInteger(32)));
	    types.add(new QMetaType<Object>(QMetaType.Type.QVariantList.getValue(),"QList<QVariant>"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QVariantHash.getValue(),"QHash<QString,QVariant>"));
	    types.add(new QMetaType<Object>(QMetaType.Type.QReal.getValue(),"qreal"));

	}
	public static QMetaTypeRegistry instance(){
		if(singleton==null){
			singleton = new QMetaTypeRegistry();
		}
		return singleton;
	}
	public synchronized int getIdForName(String name){
		for(QMetaType type: types){
			if(type.name.equals(name))
				return type.id;
		}
		throw new IllegalArgumentException();
	}
	public synchronized QMetaType getTypeForId(int id){
		for(QMetaType type: types){
			if(type.id == id) {
				System.out.println("Returning type: " + type.name);
				return type;
			}
		}
		throw new IllegalArgumentException();
	}
	public synchronized QMetaType getTypeForName(String name) {
		for (QMetaType type: types) {
			if(type.name.equals(name)) {
				System.out.println("Returning type: " + type.name);
				return type;
			}
		}
		throw new IllegalArgumentException();
	}
	public static Object unserialize(Type type,QDataInputStream stream, DataStreamVersion version) throws IOException {
		return instance().getTypeForId(type.getValue()).getSerializer().unserialize(stream, version);
	}
	public static Object unserialize(Type type,QDataInputStream stream) throws IOException {
		return unserialize(type, stream, DataStreamVersion.Qt_4_2);
	}
	@SuppressWarnings("unchecked")
	public static void serialize(Type type, QDataOutputStream stream, DataStreamVersion version, Object data) throws IOException{
		instance().getTypeForId(type.getValue()).getSerializer().serialize(stream, data, version);
	}
	public static void serialize(Type type, QDataOutputStream stream, Object data) throws IOException{
		serialize(type, stream, DataStreamVersion.Qt_4_2, data);
	}
}
