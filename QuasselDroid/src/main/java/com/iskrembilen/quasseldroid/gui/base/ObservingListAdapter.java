package com.iskrembilen.quasseldroid.gui.base;

import android.app.FragmentManager;
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
