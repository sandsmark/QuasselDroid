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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.iskrembilen.quasseldroid.BufferInfo;
import com.iskrembilen.quasseldroid.IrcMessage;
import com.iskrembilen.quasseldroid.NetworkServer;
import com.iskrembilen.quasseldroid.qtcomm.QMetaType.Type;
import com.iskrembilen.quasseldroid.qtcomm.serializers.Bool;
import com.iskrembilen.quasseldroid.qtcomm.serializers.QByteArray;
import com.iskrembilen.quasseldroid.qtcomm.serializers.QChar;
import com.iskrembilen.quasseldroid.qtcomm.serializers.QDateTime;
import com.iskrembilen.quasseldroid.qtcomm.serializers.QInteger;
import com.iskrembilen.quasseldroid.qtcomm.serializers.QList;
import com.iskrembilen.quasseldroid.qtcomm.serializers.QMap;
import com.iskrembilen.quasseldroid.qtcomm.serializers.QString;
import com.iskrembilen.quasseldroid.qtcomm.serializers.QTime;
import com.iskrembilen.quasseldroid.qtcomm.serializers.UnsignedInteger;
import com.iskrembilen.quasseldroid.qtcomm.serializers.quassel.BufferInfoSerializer;
import com.iskrembilen.quasseldroid.qtcomm.serializers.quassel.MessageSerializer;
import com.iskrembilen.quasseldroid.qtcomm.serializers.quassel.NetworkServerSerializer;


public class QMetaTypeRegistry {
	static QMetaTypeRegistry singleton = null;
	List<QMetaType<?>> types = null;
	Map<String,Integer> lookupName;
	Map<Integer,Integer> lookupId;
	private QMetaTypeRegistry(){
		types = new ArrayList<QMetaType<?>>();
		lookupName = new HashMap<String,Integer>();
		lookupId = new HashMap<Integer,Integer>();
		//:%s/QT_ADD_STATIC_METATYPE(\(\"[^\"]\+\"\)\, QMetaType::\([^)]\+\)),/types.add(new QMetaType(QMetaType.Type.\2.getValue(),\1));/g
		types.add(new QMetaType<java.lang.Void>(QMetaType.Type.Void.getValue(), "void", new com.iskrembilen.quasseldroid.qtcomm.serializers.Void()));
		types.add(new QMetaType<Boolean>(QMetaType.Type.Bool.getValue(),"bool", new Bool()));
		types.add(new QMetaType<Integer>(QMetaType.Type.Int.getValue(),"int", new QInteger()));
		types.add(new QMetaType<Integer>(QMetaType.Type.UserType.getValue(), "BufferId", new QInteger()));
		types.add(new QMetaType<Integer>(QMetaType.Type.UserType.getValue(),"NetworkId", new QInteger()));
		types.add(new QMetaType<Long>(QMetaType.Type.UInt.getValue(),"uint", new UnsignedInteger(32)));
		types.add(new QMetaType<Long>(QMetaType.Type.UShort.getValue(),"ushort", new UnsignedInteger(16)));
		types.add(new QMetaType<Map<String, QVariant<?>>>(QMetaType.Type.UserType.getValue(), "Identity", new QMap<String, QVariant<?>>("QString", "QVariant")));
		types.add(new QMetaType<Integer>(QMetaType.Type.UserType.getValue(),"IdentityId", new QInteger()));
		types.add(new QMetaType<Integer>(QMetaType.Type.UserType.getValue(),"MsgId", new QInteger()));
		types.add(new QMetaType<BufferInfo>(QMetaType.Type.UserType.getValue(),"BufferInfo", new BufferInfoSerializer()));
		types.add(new QMetaType<NetworkServer>(QMetaType.Type.UserType.getValue(),"Network::Server", new NetworkServerSerializer()));
		types.add(new QMetaType<IrcMessage>(QMetaType.Type.UserType.getValue(),"Message", new MessageSerializer()));
		types.add(new QMetaType<Calendar>(QMetaType.Type.QTime.getValue(), "QTime", new QTime()));


		types.add(new QMetaType<Object>(QMetaType.Type.LongLong.getValue(),"qlonglong"));
		types.add(new QMetaType<Object>(QMetaType.Type.ULongLong.getValue(),"qulonglong"));
		types.add(new QMetaType<Object>(QMetaType.Type.Double.getValue(),"double"));
		types.add(new QMetaType<Character>(QMetaType.Type.QChar.getValue(),"QChar", new QChar()));
		types.add(new QMetaType<Map<String, QVariant<?>>>(QMetaType.Type.QVariantMap.getValue(),"QVariantMap", new QMap<String, QVariant<?>>("QString", "QVariant")));
		types.add(new QMetaType<List<QVariant<?>> >(QMetaType.Type.QVariantList.getValue(),"QVariantList", new QList<QVariant<?>>("QVariant")));
		types.add(new QMetaType<String>(QMetaType.Type.QString.getValue(),"QString", new QString()));
		types.add(new QMetaType<List<String> >(QMetaType.Type.QStringList.getValue(),"QStringList", new QList<String>("QString")));
		types.add(new QMetaType<Object>(QMetaType.Type.QStringList.getValue(),"QStringList"));
		types.add(new QMetaType<String>(QMetaType.Type.QByteArray.getValue(),"QByteArray", new QByteArray()));
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

		for(int i=0;i<types.size();i++) {
			lookupName.put(types.get(i).name, i); 
			if (!lookupId.containsKey(types.get(i).id)) { //NB:  Several names map to the same key so don't override it
				lookupId.put(types.get(i).id, i);
			}
		}
	}
	public static QMetaTypeRegistry instance(){
		if(singleton==null){
			singleton = new QMetaTypeRegistry();
		}
		return singleton;
	}
	public synchronized int getIdForName(String name){
		if(lookupName.containsKey(name)) return types.get(lookupName.get(name)).id;
		throw new IllegalArgumentException();
	}
	public synchronized QMetaType getTypeForId(int id){
		//		Log.d("AAAAAA", types.get(lookupId.get(id)).toString());
		//		for(QMetaType type: types){
		//			if(type.id == id) {
		//				return type;
		//			}
		//		}
		if(lookupId.containsKey(id)) return types.get(lookupId.get(id));
		throw new IllegalArgumentException("Illegal id " + id);
	}
	public synchronized QMetaType getTypeForName(String name) {
		if(lookupName.containsKey(name)) return types.get(lookupName.get(name));
		throw new IllegalArgumentException("Unable to find meta type: " + name);
	}
	public static Object unserialize(Type type,QDataInputStream stream, DataStreamVersion version) throws IOException, EmptyQVariantException {
		return instance().getTypeForId(type.getValue()).getSerializer().unserialize(stream, version);
	}
	public static Object unserialize(Type type,QDataInputStream stream) throws IOException, EmptyQVariantException {
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
