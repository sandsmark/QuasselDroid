package com.lekebilen.quasseldroid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeSet;

import java.util.Observable;

import android.util.Log;

import com.lekebilen.quasseldroid.gui.ChatActivity;

public class Buffer extends Observable {
	private BufferInfo info;
	private ArrayList<IrcMessage> backlog = null;
	private int lastSeenMessage;
	private int markerLineMessage;
	private boolean unread;
	private static final String TAG = Buffer.class.getSimpleName();
	private List<String> nicks;

	public Buffer(BufferInfo info) {
		this.info = info;
		backlog = new ArrayList<IrcMessage>();
	}

	public void addBacklog(IrcMessage message) {
		//Log.i(TAG, "Buffer add message " + message.content);
		if (backlog.isEmpty()) {
			backlog.add(message);
			this.setChanged();	
		}else {
			int i = Collections.binarySearch(backlog, message);
			if (i<0) {
				backlog.add(i*-1-1, message);
				this.setChanged();
			}else {
				Log.e(TAG, "Getting message buffer already has");
			}
		}

		notifyObservers();
		unread = true;
	}

	public void setLastSeenMessage(int lastSeenMessage) {
		this.lastSeenMessage = lastSeenMessage;
	}	
	public void setMarkerLineMessage(int markerLineMessage) {
		this.markerLineMessage = markerLineMessage;
	}
	public BufferInfo getInfo() {
		return info;
	}
	public IrcMessage getBacklogEntry(int pos) {
		return backlog.get(pos);
	}
	public int getLastSeenMessage() {
		return lastSeenMessage;
	}
	public int getMarkerLineMessage() {
		return markerLineMessage;
	}

	public boolean hasMessage(IrcMessage message) {
		return Collections.binarySearch(backlog, message)>=0;
	}
	
	public int getSize() {
		return backlog.size();
	}
	
	public void setRead() {
		this.unread = false;
	}

	public boolean hasUnread() {
		return unread;
	}

	public void setNicks(List<String> nicks) {
		this.nicks = nicks;
	}

	public List<String> nicks() {
		return nicks;
	}
	
	public void removeNick(String nick) {
		nicks.remove(nick);
	}
	
	public void addNick(String nick) {
		nicks.add(nick);
	}
	
}
