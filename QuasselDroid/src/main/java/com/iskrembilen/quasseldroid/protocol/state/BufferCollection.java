/*
    QuasselDroid - Quassel client for Android
    Copyright (C) 2015 Ken Børge Viktil
    Copyright (C) 2015 Magnus Fjell
    Copyright (C) 2015 Martin Sandsmark <martin.sandsmark@kde.org>

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

package com.iskrembilen.quasseldroid.protocol.state;

import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.util.BufferCollectionHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class BufferCollection extends Observable implements Observer {

    private SparseArray<String> bufferNames = new SparseArray<>();
    private Set<Integer> bufferIds = new HashSet<>();
    private Map<String, Buffer> buffersByName = new HashMap<>();

    private List<Buffer> cachedList = new ArrayList<>();
    private List<Buffer> filteredList = new ArrayList<>();
    private Set<Predicate<Buffer>> filters = BufferCollectionHelper.FILTER_SET_VISIBLE;

    private static final String TAG = BufferCollection.class.getSimpleName();

    public static boolean orderAlphabetical;

    public void addBuffer(Buffer buffer) {
        putBuffer(buffer);
        this.setChanged();
        updateBufferList();
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

    public int getBufferCount(Set<Predicate<Buffer>> filters) {
        return getBufferList(filters).size();
    }

    public @Nullable Buffer getPos(Set<Predicate<Buffer>> filters, int pos) {
        if (getBufferList(filters).size() > pos)
            return getBufferList(filters).get(pos);
        else
            return null;
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

        if (!changed) return;
        this.setChanged();
        notifyObservers();
    }


    @Override
    public void update(Observable arg0, Object arg1) {
        if (arg1 != null && (Integer) arg1 == R.id.BUFFER_ORDER_CHANGED) {
            updateBufferList();
        } else if (arg1 != null && (Integer) arg1 == R.id.BUFFER_HIDDEN_CHANGED) {
            updateBufferList();
        }
        this.setChanged();
        notifyObservers();

    }

    private List<Buffer> getListNotLazy() {
        return new ArrayList<Buffer>(
                Collections2.transform(
                    bufferIds,
                    new Function<Integer,Buffer>() { public Buffer apply(Integer id) {
                        return buffersByName.get(bufferNames.get(id));
                    }}));
    }

    private List<Buffer> getFilteredNotLazy(Set<Predicate<Buffer>> filters) {
        Collection<Buffer> cache = cachedList;

        for (Predicate<Buffer> filter : filters) {
            cache = Collections2.filter(cache, filter);
        }

        List<Buffer> list = new ArrayList<>(cache);
        Collections.sort(list,
                orderAlphabetical ? BufferCollectionHelper.COMPARATOR_ALPHABETICAL : BufferCollectionHelper.COMPARATOR_ORDER);
        return list;
    }

    public List<Buffer> getBufferList(Set<Predicate<Buffer>> filters) {
        if (filters != this.filters) {
            this.filters = filters;
            filteredList = getFilteredNotLazy(filters);
        }

        return filteredList;
    }

    public void updateBufferList() {
        cachedList = getListNotLazy();
        filteredList = getFilteredNotLazy(filters);
    }

    public void removeBuffer(int bufferId) {
        Buffer buffer = getBuffer(bufferId);
        bufferNames.remove(bufferId);
        buffersByName.remove(buffer.getInfo().name.toLowerCase(Locale.US));
        bufferIds.remove(Integer.valueOf(bufferId));

        buffer.deleteObservers();
        updateBufferList();
        this.setChanged();
        notifyObservers();
    }

    public void updateIgnore() {
        for (Buffer buffer : getBufferList(BufferCollectionHelper.FILTER_SET_ALL)) {
            buffer.updateIgnore();
        }
    }
}
