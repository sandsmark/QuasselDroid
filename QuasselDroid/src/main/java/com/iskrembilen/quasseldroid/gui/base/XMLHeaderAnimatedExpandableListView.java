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

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.idunnololz.widgets.AnimatedExpandableListView;
import com.iskrembilen.quasseldroid.R;

public class XMLHeaderAnimatedExpandableListView extends AnimatedExpandableListView {
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
            int headerId = a.getResourceId(R.styleable.XMLHeaderAnimatedExpandableListView_headerView, View.NO_ID);
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