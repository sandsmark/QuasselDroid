package com.lekebilen.quasseldroid;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Observer;
import java.util.PriorityQueue;

import java.util.Observable;

import android.util.Log;

import com.lekebilen.quasseldroid.gui.ChatActivity;

public class BufferCollection extends Observable implements Observer {
	
	private HashMap<Integer, Buffer> buffers = new HashMap<Integer, Buffer>();
	
	private static final String TAG = BufferCollection.class.getSimpleName();
	
	public BufferCollection() {
		
	}
	
	public void addBuffer(Buffer buffer) {
		buffers.put(buffer.getInfo().id, buffer);
		this.setChanged();
		buffer.addObserver(this);
		notifyObservers();
	}
	
	public int getBufferCount() {
		return buffers.size();
	}
	
	public Buffer getPos(int pos) {
		//TODO: might want to make something better then this. For when we want to change possitions and crap we need to be able to move stuff around
		return (Buffer)buffers.values().toArray()[pos];
	}
	
	public Buffer getBuffer(int bufferId) {
		return this.buffers.get(bufferId);
		
	}

	public boolean hasBuffer(int id) {
		return buffers.containsKey(id);
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		this.setChanged();
		notifyObservers();
		
	}
}
