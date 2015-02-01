/*
    Copyright © 2015 Janne Koschinski

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

package de.kuschku.util;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BetterSparseArray<E> extends SparseArray<E> implements Map<Integer,E> {
    public BetterSparseArray() {
        this(10);
    }

    public BetterSparseArray(int initialCapacity) {
        super(initialCapacity);
    }

    public boolean containsKey(Object key) {
        return containsKey((int) key);
    }

    /**
     * Returns whether this {@code Map} contains the specified value.
     *
     * @param value the value to search for.
     * @return {@code true} if this map contains the specified value,
     * {@code false} otherwise.
     */
    @Override
    public boolean containsValue(Object value) {
        if (value==null)
            return false;

        return indexOfValue((E) value) > 0;
    }

    /**
     * Returns a {@code Set} containing all of the mappings in this {@code Map}. Each mapping is
     * an instance of {@link java.util.Map.Entry}. As the {@code Set} is backed by this {@code Map},
     * changes in one will be reflected in the other.
     *
     * @return a set of the mappings
     */
    @NonNull
    @Override
    public Set<Entry<Integer, E>> entrySet() {
        Set<Entry<Integer, E>> set = new HashSet<>(size());
        for (int i = 0; i < size(); i++) {
            set.add(new SparseArrayEntry<>(keyAt(i),valueAt(i)));
        }
        return set;
    }

    public static class SparseArrayEntry<V> implements Entry<Integer,V> {
        public SparseArrayEntry(int key, V value) {
            this.key = key;
            this.value = value;
        }

        int key;
        V value;

        /**
         * Returns the key.
         *
         * @return the key
         */
        @Override
        public Integer getKey() {
            return key;
        }

        /**
         * Returns the value.
         *
         * @return the value
         */
        @Override
        public V getValue() {
            return value;
        }

        /**
         * Sets the value of this entry to the specified value, replacing any
         * existing value.
         *
         * @param object the new value to set.
         * @return object the replaced value of this entry.
         */
        @Override
        public V setValue(V object) {
            this.value = object;
            return this.value;
        }
    }

    public boolean containsKey(int key) {
        return indexOfKey(key)>0;
    }

    /**
     * Returns the value of the mapping with the specified key.
     *
     * @param key the key.
     * @return the value of the mapping with the specified key, or {@code null}
     * if no mapping for the specified key is found.
     */
    @Override
    public E get(Object key) {
        return get((int) key);
    }

    /**
     * Returns whether this map is empty.
     *
     * @return {@code true} if this map has no elements, {@code false}
     * otherwise.
     * @see #size()
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * Returns a set of the keys contained in this {@code Map}. The {@code Set} is backed by
     * this {@code Map} so changes to one are reflected by the other. The {@code Set} does not
     * support adding.
     *
     * @return a set of the keys.
     */
    @NonNull
    @Override
    public Set<Integer> keySet() {
        Set<Integer> set = new HashSet<>(size());
        for (int i = 0; i < size(); i++) {
            set.add(keyAt(i));
        }
        return set;
    }

    /**
     * Maps the specified key to the specified value.
     *
     * @param key   the key.
     * @param value the value.
     * @return the value of any previous mapping with the specified key or
     * {@code null} if there was no mapping.
     * @throws UnsupportedOperationException if adding to this {@code Map} is not supported.
     * @throws ClassCastException            if the class of the key or value is inappropriate for
     *                                       this {@code Map}.
     * @throws IllegalArgumentException      if the key or value cannot be added to this {@code Map}.
     * @throws NullPointerException          if the key or value is {@code null} and this {@code Map} does
     *                                       not support {@code null} keys or values.
     */
    @Override
    public E put(Integer key, E value) {
        super.put(key, value);
        return value;
    }

    /**
     * Copies every mapping in the specified {@code Map} to this {@code Map}.
     *
     * @param map the {@code Map} to copy mappings from.
     * @throws UnsupportedOperationException if adding to this {@code Map} is not supported.
     * @throws ClassCastException            if the class of a key or a value of the specified {@code Map} is
     *                                       inappropriate for this {@code Map}.
     * @throws IllegalArgumentException      if a key or value cannot be added to this {@code Map}.
     * @throws NullPointerException          if a key or value is {@code null} and this {@code Map} does not
     *                                       support {@code null} keys or values.
     */
    @Override
    public void putAll(Map<? extends Integer, ? extends E> map) {
        for (Entry<? extends Integer, ? extends E> entry : map.entrySet()) {
            put(entry.getKey(),entry.getValue());
        }
    }

    /**
     * Removes a mapping with the specified key from this {@code Map}.
     *
     * @param key the key of the mapping to remove.
     * @return the value of the removed mapping or {@code null} if no mapping
     * for the specified key was found.
     * @throws UnsupportedOperationException if removing from this {@code Map} is not supported.
     */
    @Override
    public E remove(Object key) {
        if (!(key instanceof Integer))
            throw new UnsupportedOperationException();

        E value = super.get((int) key);
        super.remove((int) key);
        return value;
    }

    @NonNull
    @Override
    public Collection<E> values() {
        List<E> values = new ArrayList<>(size());
        for (int i = 0; i<size(); i++) {
            values.add(valueAt(i));
        }
        return values;
    }
}
