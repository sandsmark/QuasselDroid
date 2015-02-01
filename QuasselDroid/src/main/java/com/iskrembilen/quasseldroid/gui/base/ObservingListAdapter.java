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

package com.iskrembilen.quasseldroid.gui.base;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.util.Log;
import android.widget.ListAdapter;

import java.util.Observer;

public abstract class ObservingListAdapter implements ListAdapter, Observer {
    private static final String TAG = ObservingListAdapter.class.getSimpleName();

    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        try {
            mDataSetObservable.registerObserver(observer);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Observer " + observer.getClass().getSimpleName() + " could not be registered to " + mDataSetObservable.getClass().getSimpleName());
        }
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        try {
            mDataSetObservable.unregisterObserver(observer);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Observer " + observer.getClass().getSimpleName() + " could not be unregistered from " + mDataSetObservable.getClass().getSimpleName());
        }
    }

    public void notifyDataSetInvalidated() {
        mDataSetObservable.notifyInvalidated();
    }

    public void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }
}
