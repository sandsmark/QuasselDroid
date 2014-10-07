package com.iskrembilen.quasseldroid.gui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iskrembilen.quasseldroid.BufferInfo;
import com.iskrembilen.quasseldroid.IrcUser;
import com.iskrembilen.quasseldroid.Network;
import com.iskrembilen.quasseldroid.NetworkCollection;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.BufferOpenedEvent;
import com.iskrembilen.quasseldroid.events.NetworksAvailableEvent;
import com.iskrembilen.quasseldroid.events.UserClickedEvent;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.squareup.otto.Subscribe;

public class DetailFragment extends Fragment {
    private final String TAG = NickListFragment.class.getSimpleName();
    private int bufferId = -1;
    private NetworkCollection networks;

    private TextView nick;
    private TextView realname;
    private TextView status;

    public static DetailFragment newInstance() {
        return new DetailFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            bufferId = savedInstanceState.getInt("bufferid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.detail_fragment_layout, container, false);
        nick = (TextView) root.findViewById(R.id.detail_nick);
        realname = (TextView) root.findViewById(R.id.detail_about);
        status = (TextView) root.findViewById(R.id.detail_status);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("bufferid", bufferId);
        super.onSaveInstanceState(outState);
    }

    private void queryUser(String nick) {
        BusProvider.getInstance().post(new UserClickedEvent(bufferId, nick));
    }

    @Subscribe
    public void onNetworksAvailable(NetworksAvailableEvent event) {
        if (event.networks != null) {
            this.networks = event.networks;
            if (bufferId != -1) {
                updateView();
            }
        }
    }

    @Subscribe
    public void onBufferOpened(BufferOpenedEvent event) {
        if (event.bufferId != -1) {
            this.bufferId = event.bufferId;
            if (networks != null) {
                updateView();
            }
        }
    }

    void updateView() {
        if (networks.getBufferById(bufferId).getInfo().type == BufferInfo.Type.QueryBuffer) {
            Network network = networks.getNetworkById(networks.getBufferById(bufferId).getInfo().networkId);
            IrcUser user = network.getUserByNick(networks.getBufferById(bufferId).getInfo().name);

            if (user!=null) {
                nick.setText(user.nick);
                if (user.away && !user.awayMessage.trim().equalsIgnoreCase("")) {
                    status.setText("Away ("+user.awayMessage+")");
                } else if (user.away) {
                    status.setText("Away");
                } else {
                    status.setText("Online");
                }

                if (user.realName!=null) {
                    realname.setText(user.realName);
                    realname.setTextColor(getResources().getColor(R.color.primary_text));
                } else {
                    realname.setText("");
                }
            } else {
                nick.setText(user.nick);
                status.setText("Offline");
                realname.setText("No Data Available");
                realname.setTextColor(getResources().getColor(R.color.secondary_text));
            }
        }
    }
}
