/*
    QuasselDroid - Quassel client for Android
 	Copyright (C) 2011 Ken Børge Viktil
 	Copyright (C) 2011 Magnus Fjell
 	Copyright (C) 2011 Martin Sandsmark <martin.sandsmark@kde.org>

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

package com.iskrembilen.quasseldroid;


/**
 * Class holds the basic information about a buffer, the name, the type of buffer, the network etc
 */
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
	public String name;
	
	public String toString() {
		return name + id + "[" + type.name() + "]";
	}
}
