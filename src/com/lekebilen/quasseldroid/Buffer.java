package com.lekebilen.quasseldroid;

import java.util.List;
import java.util.PriorityQueue;

import java.util.Observable;

import android.util.Log;

import com.lekebilen.quasseldroid.gui.ChatActivity;

public class Buffer extends Observable {
	private BufferInfo info;
	private PriorityQueue<IrcMessage> backlog = null;
	private int lastSeenMessage;
	private int markerLineMessage;
	
	private static final String TAG = Buffer.class.getSimpleName();
	
	public Buffer(BufferInfo info) {
		this.info = info;
		backlog = new PriorityQueue<IrcMessage>();
	}
	
	public void addBacklog(IrcMessage message) {
		Log.i(TAG, "Buffer add message " + message.content);
		backlog.add(message);
		this.setChanged();	
		notifyObservers();
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
	public PriorityQueue<IrcMessage> getBacklog() {
		return backlog;
	}
	public int getLastSeenMessage() {
		return lastSeenMessage;
	}
	public int getMarkerLineMessage() {
		return markerLineMessage;
	}
}
