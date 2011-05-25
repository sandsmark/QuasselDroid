package com.lekebilen.quasseldroid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.util.Log;

public class BufferCollection extends Observable implements Observer {

	private HashMap<Integer, Buffer> buffers = new HashMap<Integer, Buffer>();
	private List<Buffer> bufferList = new ArrayList<Buffer>();
	private List<Buffer> filteredList = new ArrayList<Buffer>();

	private static final String TAG = BufferCollection.class.getSimpleName();

	public BufferCollection() {

	}

	public void addBuffer(Buffer buffer) {
		if (buffers.containsKey(buffer.getInfo().id)) {
			Log.e(TAG, "Getting buffer already have: " + buffer.getInfo().name);
			return;
		}

		buffers.put(buffer.getInfo().id, buffer);
		bufferList.add(buffer);
//		if(!isBufferFiltered(buffer)) {
//			filteredList.add(buffer);
//			Collections.sort(filteredList);
//		}
		Collections.sort(bufferList);
		filterBuffers();
		this.setChanged();
		buffer.addObserver(this);
		notifyObservers();
	}

	private boolean isBufferFiltered(Buffer buffer) {
		//TODO: for now hide all buffers that are hidden
		if (buffer.isPermanentlyHidden() || buffer.isTemporarilyHidden()) {
			return true;
		}else{
			return false;
		}
	}

	public int getBufferCount() {
		return filteredList.size();
	}

	public Buffer getPos(int pos) {
		return filteredList.get(pos);
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
			if (this.buffers.containsKey(buffer.getInfo().id)) {
				Log.e(TAG, "Getting buffer in buffers we already have: " + buffer.getInfo().name);
				continue;
			}
			Log.d(TAG, buffer.getInfo().name + " : " + buffer.getInfo().id);

			changed = true;
			this.buffers.put(buffer.getInfo().id, buffer);
			bufferList.add(buffer);
//			if(!isBufferFiltered(buffer)) {
//				filteredList.add(buffer);
//			}
			buffer.addObserver(this);
		}
		if (!changed) return;

		Collections.sort(bufferList);
		filterBuffers();
		this.setChanged();
		notifyObservers();
	}
	
	private void filterBuffers() {
		filteredList.clear();
		for (Buffer buf:bufferList) {
			if (!isBufferFiltered(buf)) 
				filteredList.add(buf);
		}
	}


	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1!=null && (Integer)arg1 == R.id.BUFFER_ORDER_CHANGED) {
			Collections.sort(bufferList);
			filterBuffers();
		}else if (arg1!=null && (Integer)arg1 == R.id.BUFFER_HIDDEN_CHANGED){
			filterBuffers();
		}
		this.setChanged();
		notifyObservers();

	}
}
