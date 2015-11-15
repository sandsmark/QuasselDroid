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

package com.iskrembilen.quasseldroid.gui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iskrembilen.quasseldroid.protocol.state.Buffer;
import com.iskrembilen.quasseldroid.protocol.state.BufferInfo;
import com.iskrembilen.quasseldroid.protocol.state.IrcUser;
import com.iskrembilen.quasseldroid.protocol.state.Network;
import com.iskrembilen.quasseldroid.protocol.state.NetworkCollection;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.BufferOpenedEvent;
import com.iskrembilen.quasseldroid.events.NetworksAvailableEvent;
import com.iskrembilen.quasseldroid.events.UserClickedEvent;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.squareup.otto.Subscribe;

import java.io.Serializable;
import java.util.Observable;
import java.util.Observer;

public class DetailFragment extends Fragment implements Serializable {
    private final String TAG = NickListFragment.class.getSimpleName();
    private int bufferId = -1;
    private NetworkCollection networks;

    private TextView nick;
    private TextView realname;
    private TextView status;

    private NicksObserver observer = new NicksObserver();

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
        View root = inflater.inflate(R.layout.fragment_detail, container, false);
        nick = (TextView) root.findViewById(R.id.detail_nick);
        realname = (TextView) root.findViewById(R.id.detail_about);
        status = (TextView) root.findViewById(R.id.detail_status);
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
        if (buffer == null) {
            observer.setUser(null);
        } else {
            Network network = networks.getNetworkById(buffer.getInfo().networkId);
            IrcUser user = network.getUserByNick(buffer.getInfo().name);
            observer.setUser(user);
        }
    }

    void updateView() {
        if (networks.getBufferById(bufferId).getInfo().type == BufferInfo.Type.QueryBuffer) {
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

    public void setNetworks(NetworkCollection networks) {
        this.networks = networks;
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
