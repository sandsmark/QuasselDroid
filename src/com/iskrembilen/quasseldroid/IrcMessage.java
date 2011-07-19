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

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;

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
		static String[] filterList = {Type.Join.name(),Type.Part.name(),Type.Quit.name(),Type.Nick.name(),Type.Mode.name(),Type.Topic.name(),Type.DayChange.name()};
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

		public static String[] getFilterList(){
			return filterList;
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
	public Spannable content;
	public String sender;
	public Type type;
	public byte flags;

	private ArrayList<String> urls = new ArrayList<String>();


	public int compareTo(IrcMessage other) {
		return ((Integer)messageId).compareTo((Integer)other.messageId);
	}

	public String toString() {
		return sender +": " + content;
	}


	public String getTime(){
		return String.format("%02d:%02d:%02d", timestamp.getHours(), timestamp.getMinutes(), timestamp.getSeconds());
	}

	public String getNick(){
		return sender.split("!")[0];
	}

	public void setFlag(Flag flag) {
		this.flags |= flag.value;
	}

	public boolean isHighlighted() {
		if (((flags & Flag.Highlight.value) != 0) && ((flags & Flag.Self.value) == 0)) {
			return true;
		}
		return false;
	}

	public boolean isSelf() {
		return ((flags & Flag.Self.value) != 0);
	}

	public void addURL(Context context, String url) {
		urls.add(url);
		int start = content.toString().indexOf(url);
		content.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.ircmessage_url_color)), start, start+url.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		content.setSpan(new UnderlineSpan(), start, start+url.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
	}

	public ArrayList<String> getURLs() {
		return urls;
	}

	public boolean hasURLs() {
		return !urls.isEmpty();
	}
}
