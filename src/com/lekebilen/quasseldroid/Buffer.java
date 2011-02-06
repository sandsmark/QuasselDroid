package com.lekebilen.quasseldroid;

import java.util.List;
import java.util.PriorityQueue;

import java.util.Observable;

public class Buffer extends Observable {
	private BufferInfo info;
	private PriorityQueue<IrcMessage> backlog = null;
	private int lastSeenMessage;
	private int markerLineMessage;
	

	public Buffer(BufferInfo info) {
		this.info = info;
		backlog = new PriorityQueue<IrcMessage>();
	}
	
	public void addBacklog(IrcMessage message) {
		backlog.add(message);
		notifyObservers(message);
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
