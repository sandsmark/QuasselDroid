package com.iskrembilen.quasseldroid.gui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.InitProgressEvent;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.squareup.otto.Subscribe;

import java.io.Serializable;

public class ConnectingFragment extends Fragment implements Serializable {

    private TextView progressTextView;

    public static ConnectingFragment newInstance() {
        ConnectingFragment f = new ConnectingFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_loading, container, false);
        progressTextView = (TextView) root.findViewById(R.id.buffer_list_progress_text);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onInitProgressed(InitProgressEvent event) {
        if (!event.done) {
            progressTextView.setText(event.progress);
        }
    }

}
