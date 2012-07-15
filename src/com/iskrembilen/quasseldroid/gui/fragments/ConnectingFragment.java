package com.iskrembilen.quasseldroid.gui.fragments;

import com.iskrembilen.quasseldroid.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ConnectingFragment extends Fragment {

	static ConnectingFragment newInstance() {
		ConnectingFragment f = new ConnectingFragment();
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.connecting_fragment_layout, container, false);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

}
