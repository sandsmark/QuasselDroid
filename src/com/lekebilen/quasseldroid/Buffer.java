package com.lekebilen.quasseldroid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeSet;

import java.util.Observable;

import android.app.PendingIntent;
import android.util.Log;

import com.lekebilen.quasseldroid.BufferInfo.Type;
import com.lekebilen.quasseldroid.gui.ChatActivity;

public class Buffer extends Observable implements Comparable<Buffer> {
	private BufferInfo info;
	private ArrayList<IrcMessage> backlog = null;
	/*
	 * the message id of the message that was on top of the screen when this buffer was last displayed
	 * used to remember position when going back to a buffer
	 */
	private int topMessageShown = 0;
	

	private int lastSeenMessage;
	private int markerLineMessage;
	private int lastHighlightMessageId;
	private static final String TAG = Buffer.class.getSimpleName();
	private List<String> nicks;
	private String topic;
	
	private int  backlogPending = 0;
	private List<IrcMessage> backlogStash;

	public Buffer(BufferInfo info) {
		this.info = info;
		backlog = new ArrayList<IrcMessage>();
		backlogStash = new ArrayList<IrcMessage>();
	}

	
	public void addMessage(IrcMessage message ) {
		newBufferEntry(message);
		notifyObservers(R.id.BUFFERUPDATE_NEWMESSAGE);
	}
	
	private void newBufferEntry(IrcMessage message) {		
		if (message.isHighlighted() && message.messageId > lastHighlightMessageId){
			lastHighlightMessageId = message.messageId;
			this.setChanged();
		}
		
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
	}
	
	public void addBacklogMessage(IrcMessage message) {
		backlogStash.add(message);
		
		if (backlogPending==0 || backlogPending<=backlogStash.size()) {
			for (IrcMessage item : backlogStash) {
				newBufferEntry(item);
			}
			backlogStash.clear();
			backlogPending=0;
		}
		notifyObservers(R.id.BUFFERUPDATE_BACKLOG);
	}
	
	public void setBacklogPending(int amount) {
		backlogPending = amount;
	}
	
	public boolean hasPendingBacklog() {
		return backlogPending>0;
	}
	public boolean hasUnseenHighlight(){
		if (lastHighlightMessageId > lastSeenMessage){
			return true;
		}
		return false;
	}
	public boolean hasUnreadMessage(){
		//Last message in the backlog has a bigger messageId than the last seen message
		if (backlog.size() != 0 && lastSeenMessage < backlog.get(backlog.size()-1).messageId){
			return true;
		}
		return false;
	}
	public void setLastSeenMessage(int lastSeenMessage) {
		Log.d(TAG, this.info.name + ": LASTSEEN SET");
		this.lastSeenMessage = lastSeenMessage;
		this.setChanged();
		notifyObservers();
	}	
	public void setMarkerLineMessage(int markerLineMessage) {
		Log.d(TAG, this.info.name+": MARKELINE SET");
		this.markerLineMessage = markerLineMessage;
		this.setChanged();
		notifyObservers();
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
		if (backlog.isEmpty())
			return;
		
		lastSeenMessage = backlog.get(backlog.size()-1).messageId;
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

	public void setTopic(String topic) {
		//TODO: notify observers
		this.topic = topic;
	}

	public String topic() {
		return topic;
	}
	
	public void setName(String name) {
		info.name = name;
		notifyObservers();
	}
	
	public int getTopMessageShown() {
		return topMessageShown;
	}


	public void setTopMessageShown(int topMessageShown) {
		this.topMessageShown = topMessageShown;
	}

	@Override
	public int compareTo(Buffer another) {
		if (info.networkId != another.info.networkId)
			return info.networkId - another.info.networkId;
		else if (info.type != another.info.type)
			return info.type.value - another.info.type.value;
		else return info.name.compareTo(another.info.name);
	}
}
