package com.lekebilen.quasseldroid;

import java.nio.ByteBuffer;

public class BufferInfo {
	public enum Type {
		    InvalidBuffer (0x00),
		    StatusBuffer (0x01),
		    ChannelBuffer (0x02),
		    QueryBuffer (0x04),
		    GroupBuffer (0x08);
			int value;
			private Type(int value){
				this.value = value;
			}
			public int getValue(){
				return value;
			}
			public static Type getType(int value) {
				for (Type t: values()) {
					if (t.value == value)
						return t;
				}
				return InvalidBuffer;
			}
	}

	public int id, networkId;
	public Type type;
	public long groupId;
	public ByteBuffer name;
	
	public String toString() {
		return new String(name.array()) + id + "[" + type.name() + "]";
	}
}
