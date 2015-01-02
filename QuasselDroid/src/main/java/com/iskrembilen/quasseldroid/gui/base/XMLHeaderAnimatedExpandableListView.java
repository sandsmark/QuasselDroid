package com.iskrembilen.quasseldroid.gui.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.idunnololz.widgets.AnimatedExpandableListView;
import com.iskrembilen.quasseldroid.R;

public class XMLHeaderAnimatedExpandableListView extends AnimatedExpandableListView {
    private int headerId;
    private View headerView;

    public XMLHeaderAnimatedExpandableListView(Context context) {
        this(context, null);
    }

    public XMLHeaderAnimatedExpandableListView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.expandableListViewStyle);
    }

    public XMLHeaderAnimatedExpandableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.XMLHeaderAnimatedExpandableListView, defStyle, defStyle);

        try {
            headerId = a.getResourceId(R.styleable.XMLHeaderAnimatedExpandableListView_headerView, View.NO_ID);
            if (headerId != View.NO_ID) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View header = inflater.inflate(headerId, null);
                addHeaderView(header);
            }
        } finally {
            a.recycle();
        }
    }

    public View getHeaderView() {
        return headerView;
    }

    public boolean hasHeaderView() {
        return headerView!=null;
    }
}