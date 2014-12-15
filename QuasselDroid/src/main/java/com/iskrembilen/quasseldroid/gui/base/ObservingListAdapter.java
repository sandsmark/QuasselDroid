package com.iskrembilen.quasseldroid.gui.base;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.widget.ListAdapter;

import java.util.Observer;

public abstract class ObservingListAdapter implements ListAdapter, Observer {
    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        try {
            mDataSetObservable.registerObserver(observer);
        } catch (IllegalStateException e) {

        }
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        try {
            mDataSetObservable.unregisterObserver(observer);
        } catch (IllegalStateException e) {

        }
    }

    public void notifyDataSetInvalidated() {
        mDataSetObservable.notifyInvalidated();
    }

    public void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }
}
