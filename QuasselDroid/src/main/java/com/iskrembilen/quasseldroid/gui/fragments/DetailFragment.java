package com.iskrembilen.quasseldroid.gui.fragments;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.iskrembilen.quasseldroid.Buffer;
import com.iskrembilen.quasseldroid.BufferInfo;
import com.iskrembilen.quasseldroid.IrcMode;
import com.iskrembilen.quasseldroid.IrcUser;
import com.iskrembilen.quasseldroid.Network;
import com.iskrembilen.quasseldroid.NetworkCollection;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.UserCollection;
import com.iskrembilen.quasseldroid.events.BufferOpenedEvent;
import com.iskrembilen.quasseldroid.events.NetworksAvailableEvent;
import com.iskrembilen.quasseldroid.events.UserClickedEvent;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.iskrembilen.quasseldroid.util.ThemeUtil;
import com.squareup.otto.Subscribe;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class DetailFragment extends Fragment {
    private final String TAG = NickListFragment.class.getSimpleName();
    private int bufferId = -1;
    private NetworkCollection networks;

    private TextView nick;
    private TextView realname;
    private TextView status;

    NicksObserver observer = new NicksObserver();

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
                updateObserver();
                updateView();
            }
        }
    }

    @Subscribe
    public void onBufferOpened(BufferOpenedEvent event) {
        if (event.bufferId != -1) {
            this.bufferId = event.bufferId;
            if (networks != null) {
                updateObserver();
                updateView();
            }
        }
    }

    void updateObserver() {
        Buffer buffer = networks.getBufferById(bufferId);
        Network network = networks.getNetworkById(buffer.getInfo().networkId);
        IrcUser user = network.getUserByNick(buffer.getInfo().name);
        observer.setUser(user);
    }

    void updateView() {
        if (networks.getBufferById(bufferId).getInfo().type == BufferInfo.Type.QueryBuffer) {
            Network network = networks.getNetworkById(networks.getBufferById(bufferId).getInfo().networkId);
            IrcUser user = observer.user;

            if (user != null) {
                nick.setText(user.nick);
                if (user.away && user.awayMessage!=null) {
                    status.setText("Away: " + user.awayMessage);
                } else if (user.away) {
                    status.setText("Away");
                } else {
                    status.setText("Online");
                }

                if (user.realName != null) {
                    realname.setText(user.realName);
                } else {
                    realname.setText("");
                }
            } else {
                nick.setText(networks.getBufferById(bufferId).getInfo().name);
                status.setText("Offline");
                realname.setText("No Data Available");
            }
        }
    }

    public class NicksObserver implements Observer {

        private IrcUser user;

        public void setUser(IrcUser user) {
            if (this.user!=null) this.user.deleteObserver(this);
            this.user = user;
            if (this.user!=null) this.user.addObserver(this);
        }

        @Override
        public void update(Observable observable, Object data) {
            if (data == null) {
                return;
            }
            switch ((Integer) data) {
                case R.id.SET_USER_AWAY_MESSAGE:
                case R.id.SET_USER_AWAY:
                case R.id.SET_USER_REALNAME:
                case R.id.NEW_USER_INFO:
                    updateView();
                    updateObserver();
                    break;
                default:
            }
        }
    }
}
