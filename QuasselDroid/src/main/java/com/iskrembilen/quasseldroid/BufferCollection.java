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

import android.util.SparseArray;

import com.iskrembilen.quasseldroid.util.Helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class BufferCollection extends Observable implements Observer {

    private SparseArray<String> bufferNames = new SparseArray<>();
    private List<Integer> bufferIds = new ArrayList<>();
    private Map<String, Buffer> buffersByName = new HashMap<>();

    private List<Buffer> filteredList = new ArrayList<>();

    private static final String TAG = BufferCollection.class.getSimpleName();

    public static boolean orderAlphabetical;

    public void addBuffer(Buffer buffer) {
        putBuffer(buffer);
        filteredList = getFilteredList();
        this.setChanged();
        buffer.addObserver(this);
        notifyObservers();
    }

    private void putBuffer(Buffer buffer) {
        if (buffer.getInfo().id != -1) {
            bufferNames.put(buffer.getInfo().id, buffer.getInfo().name.toLowerCase(Locale.US));
            bufferIds.add(buffer.getInfo().id);
        }
        buffersByName.put(buffer.getInfo().name.toLowerCase(Locale.US), buffer);
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
        return getFilteredList().size();
    }

    public int getUnfilteredBufferCount() {
        return bufferIds.size();
    }

    public Buffer getPos(int pos) {
        return getFilteredList().get(pos);
    }

    public Buffer getUnfilteredPos(int pos) {
        return buffersByName.get(bufferNames.get(bufferIds.get(pos)));
    }

    public Buffer getBuffer(int bufferId) {
        return getBuffer(bufferNames.get(bufferId));
    }

    public Buffer getBuffer(String name) {
        return buffersByName.get(name.toLowerCase(Locale.US));
    }

    public boolean hasBuffer(int id) {
        return bufferNames.get(id)!=null;
    }

    public boolean hasBuffer(Buffer buffer) {
        return hasBuffer(buffer.getInfo().id);
    }

    public boolean hasBuffer(String bufferName) {
        return buffersByName.containsKey(bufferName.toLowerCase(Locale.US));
    }

    public void addBuffers(Collection<Buffer> buffers) {
        boolean changed = false;
        for (Buffer buffer : buffers) {
            changed = true;
            putBuffer(buffer);
            buffer.addObserver(this);
        }
        filteredList = getFilteredList();
        if (!changed) return;
        this.setChanged();
        notifyObservers();
    }

    private List<Buffer> getFilteredList() {
        List<Buffer> list = new ArrayList<>();
        for (Buffer buf : getRawBufferList()) {
            if (!isBufferFiltered(buf))
                list.add(buf);
        }
        return list;
    }


    @Override
    public void update(Observable arg0, Object arg1) {
        if (arg1 != null && (Integer) arg1 == R.id.BUFFER_ORDER_CHANGED) {
            //filteredList = getFilteredList();
        } else if (arg1 != null && (Integer) arg1 == R.id.BUFFER_HIDDEN_CHANGED) {
            //filteredList = getFilteredList();
        }
        filteredList = getFilteredList();
        this.setChanged();
        notifyObservers();

    }

    public Collection<Buffer> getRawBufferList() {
        List<Buffer> rawBufferList = new ArrayList<>(bufferIds.size());
        if (orderAlphabetical) {
            List<String> names = new ArrayList<>();
            names.addAll(buffersByName.keySet());

            Collections.sort(names,new Helper.AlphabeticalComparator());
            Buffer b;
            for (String name: names) {
                b = buffersByName.get(name);
                if (bufferIds.contains(b.getInfo().id))
                    rawBufferList.add(b);
            }
        } else {
            List<String> names = new ArrayList<>();
            names.addAll(buffersByName.keySet());

            Buffer b;
            for (String name: names) {
                b = buffersByName.get(name);
                if (bufferIds.contains(b.getInfo().id))
                    rawBufferList.add(b);
            }
            Collections.sort(rawBufferList,new Helper.OrderComparator());
        }
        return rawBufferList;
    }

    public void removeBuffer(int bufferId) {
        Buffer buffer = getBuffer(bufferId);
        bufferNames.remove(bufferId);
        buffersByName.remove(buffer.getInfo().name.toLowerCase(Locale.US));
        bufferIds.remove(Integer.valueOf(bufferId));

        filteredList = getFilteredList();
        buffer.deleteObservers();

        this.setChanged();
        notifyObservers();
    }
}
