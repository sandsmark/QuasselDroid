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

import android.util.Log;

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
    private List<Buffer> filteredList = new ArrayList<Buffer>();

    public static boolean orderAlphabetical = true;

    private static final String TAG = BufferCollection.class.getSimpleName();

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
        } else {
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

    /**
     * @param name Searches the buffer with name name
     * @return A found Buffer or null
     */
    public Buffer getBuffer(String name) {
        for (Buffer buffer : this.bufferList) {
            if (buffer.getInfo().name.equals(name)) {
                return buffer;
            }
        }
        return null;
    }

    public boolean hasBuffer(int id) {
        return buffers.containsKey(id);
    }

    public void addBuffers(Collection<Buffer> buffers) {
        boolean changed = false;
        for (Buffer buffer : buffers) {
            if (this.buffers.containsKey(buffer.getInfo().id)) {
                Log.e(TAG, "Getting buffer in buffers we already have: " + buffer.getInfo().name);
                continue;
            }
            //Log.d(TAG, buffer.getInfo().name + " : " + buffer.getInfo().id);

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
        for (Buffer buf : bufferList) {
            if (!isBufferFiltered(buf))
                filteredList.add(buf);
        }
    }


    @Override
    public void update(Observable arg0, Object arg1) {
        if (arg1 != null && (Integer) arg1 == R.id.BUFFER_ORDER_CHANGED) {
            Collections.sort(bufferList);
//			for (Buffer buf:bufferList) {
//				Log.d("KEN", buf.getInfo().name + " : " +buf.getInfo().id);
//			}
            filterBuffers();
//			Log.e(TAG, "UPDATEEEEEEEEE");
        } else if (arg1 != null && (Integer) arg1 == R.id.BUFFER_HIDDEN_CHANGED) {
            filterBuffers();
        }
        this.setChanged();
        notifyObservers();

    }

    public List<Buffer> getRawBufferList() {
        return bufferList;
    }

    public void removeBuffer(int bufferId) {
        Buffer buffer = buffers.remove(bufferId);
        bufferList.remove(buffer);
        filteredList.remove(buffer);
        buffer.deleteObservers();

        this.setChanged();
        notifyObservers();
    }
}
