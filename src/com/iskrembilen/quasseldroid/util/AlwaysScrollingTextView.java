package com.iskrembilen.quasseldroid.util;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

public final class AlwaysScrollingTextView extends TextView {


	public AlwaysScrollingTextView(Context context) {
		super(context);
	}

	public AlwaysScrollingTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AlwaysScrollingTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean isFocused() {
		return true;
	}
	
	@Override
    protected void onFocusChanged(boolean isFocused, int direction, Rect previouslyFocusedRect) {
        if (isFocused) {
            super.onFocusChanged(isFocused, direction, previouslyFocusedRect);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean isFocused) {
        if (isFocused) {
            super.onWindowFocusChanged(isFocused);
        }
    }

}