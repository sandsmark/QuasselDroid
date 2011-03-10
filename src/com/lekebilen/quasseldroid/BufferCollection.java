package com.lekebilen.quasseldroid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class BufferCollection extends Observable implements Observer {
	
	private HashMap<Integer, Buffer> buffers = new HashMap<Integer, Buffer>();
	private List<Buffer> bufferList = new ArrayList<Buffer>();
	
	private static final String TAG = BufferCollection.class.getSimpleName();
	
	public BufferCollection() {
		
	}
	
	public void addBuffer(Buffer buffer) {
		if (buffers.containsKey(buffer.getInfo().id)) {
			return;
		}
		
		buffers.put(buffer.getInfo().id, buffer);
		bufferList.add(buffer);
		Collections.sort(bufferList);
		this.setChanged();
		buffer.addObserver(this);
		notifyObservers();
	}
	
	public int getBufferCount() {
		return buffers.size();
	}
	
	public Buffer getPos(int pos) {
		//TODO: might want to make something better then this. For when we want to change possitions and crap we need to be able to move stuff around
		return (Buffer) bufferList.get(pos);
	}
	
	public Buffer getBuffer(int bufferId) {
		return this.buffers.get(bufferId);
		
	}

	public boolean hasBuffer(int id) {
		return buffers.containsKey(id);
	}
	
	public void addBuffers(Collection<Buffer> buffers) {
		boolean changed = false;
		for (Buffer buffer: buffers) {
			if (this.buffers.containsKey(buffer.getInfo().id))
				continue;
			
			changed = true;
			this.buffers.put(buffer.getInfo().id, buffer);
			bufferList.add(buffer);
			buffer.addObserver(this);
		}
		if (!changed) return;
		
		Collections.sort(bufferList);
		this.setChanged();
		notifyObservers();
	}
	

	@Override
	public void update(Observable arg0, Object arg1) {
		this.setChanged();
		notifyObservers();
		
	}
}
