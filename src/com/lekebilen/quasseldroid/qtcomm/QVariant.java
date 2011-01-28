/**
 * Copyright Frederik M.J.V. 2010 - LGPL 2.1 / GPLv3
 */

package com.lekebilen.quasseldroid.qtcomm;

import java.io.IOException;
import java.nio.ByteBuffer;

public class QVariant<T extends Object>{

	public enum Type {
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
		LastType(0xffffffff);
		int value;
		Type(int value){
			this.value = value;
		}
		public int getValue(){
			return value;
		}
	};

	T data;
	DataStreamVersion version;
	QVariant.Type type = Type.Invalid;
	String userTypeName = null;
	public QVariant(T data, Type t){
		this.data = data;
		this.type = t;
	}
	private void clear(){
		data = null;
	}
	public QVariant(){

	}
	public Type getType(){
		return type;
	}
	public T getData() {
		return data;
	}
	public boolean isValid(){
		if(type==Type.Invalid)
			return false;
		if(data==null)
			return false;
		return true;
	}
	public static class QVariantSerializer<U extends Object> implements QMetaTypeSerializer<QVariant<U>>{
		public QVariantSerializer(){
			
		}
		@SuppressWarnings("unchecked")
		@Override
		public QVariant<U> unserialize(QDataInputStream src, DataStreamVersion version) throws IOException{	    
			int type = (int)src.readUInt(32);
			if (version.getValue() < DataStreamVersion.Qt_4_0.getValue()) {
				//FIXME: Implement?
				/*if (u >= MapFromThreeCount)
		            return;
		        u = map_from_three[u];
				 */
			}
			boolean is_null = false;
			if (version.getValue() >= DataStreamVersion.Qt_4_2.getValue())
				is_null = src.readUnsignedByte()!=0;
			
			QVariant<U> ret = new QVariant<U>();
			if (type == QVariant.Type.UserType.value) {
				String name = new String(((ByteBuffer)QMetaTypeRegistry.instance().getTypeForId(QMetaType.Type.QByteArray.getValue()).getSerializer().unserialize(src, version)).array());
				name = name.trim();
				ret.userTypeName = name;
				try{
					type = QMetaTypeRegistry.instance().getTypeForName(name);
				} catch (IllegalArgumentException e){
					throw new IOException("Corrupt data", e);
				}
			}

			
			for(Type tpe : QVariant.Type.values()){
				if(tpe.getValue() == type){
					ret.type = tpe;
					break;
				}
			}
			
			if (ret.type==Type.Invalid || is_null) { //includes data = null; FIXME: is this correct?
				// Since we wrote something, we should read something
				QMetaTypeRegistry.instance().getTypeForId(QMetaType.Type.QString.getValue()).getSerializer().unserialize(src, version);
				ret.data = null;
				return ret;
			}    
			//Unchecked cast so we can read unknown qvariants at run time and then inspect the contents
			ret.data = (U) QMetaTypeRegistry.instance().getTypeForId(type).getSerializer().unserialize(src, version);
			return ret;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void serialize(QDataOutputStream stream, QVariant<U> data, DataStreamVersion version) throws IOException {
			stream.writeUInt(data.type.getValue(), 32);
			if (version.getValue() < DataStreamVersion.Qt_4_0.getValue()) {
				//FIXME: Implement?
			}
			if (version.getValue() >= DataStreamVersion.Qt_4_2.getValue())
				stream.writeByte(data==null?1:0);
			if (data.type == QVariant.Type.UserType) {
				QMetaTypeRegistry.instance().getTypeForId(QMetaType.Type.QString.getValue()).getSerializer().serialize(stream, data.getUserTypeName(), version);
			}
			QMetaTypeRegistry.instance().getTypeForId(data.type.getValue()).getSerializer().serialize(stream, data.data,version);
		}

	}
	public String getUserTypeName() {
		return userTypeName;
		//TODO: Implement user types
	}
	
	public String toString() {
		switch (type) {
		case String:
			return (String)data;
		case UInt:
		case Bool:
			return data.toString();
		default:
			return type.toString();
		}	
	}
}
