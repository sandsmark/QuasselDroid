package com.lekebilen.quasseldroid;

import java.util.List;
import java.util.PriorityQueue;

public class Buffer {
	private BufferInfo info;
	private PriorityQueue<Message> backlog = null;
	private int lastSeenMessage;
	private int markerLineMessage;
	

	public Buffer(BufferInfo info) {
		this.info = info;
	}
	
	public void addBacklog(Message message) {
		if (backlog == null)
			backlog = new PriorityQueue<Message>();
		
		backlog.add(message); // TODO: sort
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
	public PriorityQueue<Message> getBacklog() {
		return backlog;
	}
	public int getLastSeenMessage() {
		return lastSeenMessage;
	}
	public int getMarkerLineMessage() {
		return markerLineMessage;
	}
}
