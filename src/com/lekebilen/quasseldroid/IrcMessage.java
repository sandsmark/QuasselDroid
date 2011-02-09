package com.lekebilen.quasseldroid;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class IrcMessage implements Comparable<IrcMessage>{
	public enum Type {
		Plain     (0x00001),
		Notice    (0x00002),
		Action    (0x00004),
		Nick      (0x00008),
		Mode      (0x00010),
		Join      (0x00020),
		Part      (0x00040),
		Quit      (0x00080),
		Kick      (0x00100),
		Kill      (0x00200),
		Server    (0x00400),
		Info      (0x00800),
		Error     (0x01000),
		DayChange (0x02000),
		Topic     (0x04000),
		NetsplitJoin (0x08000),
		NetsplitQuit (0x10000),
		Invite    (0x20000);
		int value;
		Type(int value){
			this.value = value;
		}
		public int getValue(){
			return value;
		}
		public static Type getForValue(int value) {
			for (Type type: Type.values()) {
				if (type.value == value)
					return type;
			}
			return Plain;
		}
	}
	public enum Flag {
		    None (0x00),
		    Self (0x01),
		    Highlight (0x02),
		    Redirected (0x04),
		    ServerMsg (0x08),
		    Backlog (0x80);
			int value;
			Flag (int value){
				this.value = value;
			}
			public int getValue(){
				return value;
			}		    
		  };
	
	public Date timestamp; //TODO: timezones, Java bleh as usual
	public int messageId;
	public BufferInfo bufferInfo;
	public String content;
	public String sender;
	public Type type;
	public byte flags;

	
	public int compareTo(IrcMessage other) {
		return this.timestamp.compareTo(other.timestamp);
	}
	
	public String toString() {
		return sender +": " + content;
	}
	

	public String getTime(){
		return timestamp.getHours() + ":" + timestamp.getMinutes() + ":" + timestamp.getSeconds();
	}
	
	public String getNick(){
		return sender.split("!")[0];
	}
}
