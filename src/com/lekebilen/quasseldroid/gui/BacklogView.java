package com.lekebilen.quasseldroid.gui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class BacklogView extends ListView {

	public BacklogView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	public BacklogView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public BacklogView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	 
	@Override
	protected void onScrollChanged (int l, int t, int oldl, int oldt)
	{
		System.out.println(t);
		if (t == 0) {
			System.out.println("fuck");
		}
	}
	
}
