package com.iskrembilen.quasseldroid.gui.fragments;

import com.iskrembilen.quasseldroid.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ConnectingFragment extends Fragment {

	private TextView progressTextView;

	public static ConnectingFragment newInstance() {
		ConnectingFragment f = new ConnectingFragment();
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.connecting_fragment_layout, container, false);
		progressTextView = (TextView) root.findViewById(R.id.buffer_list_progress_text);
		return root;
	}

	public void updateProgress(String info) {
		progressTextView.setText(info);				
	}

}
