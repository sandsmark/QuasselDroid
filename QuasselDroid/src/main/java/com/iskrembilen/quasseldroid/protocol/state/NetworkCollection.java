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

package com.iskrembilen.quasseldroid.protocol.state;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class NetworkCollection extends Observable implements Observer {
    private static final String TAG = NetworkCollection.class.getSimpleName();
    private List<Network> networkList = new ArrayList<>();
    private SparseArray<Network> networkMap = new SparseArray<Network>();

    public void addNetwork(Network network) {
        networkMap.put(network.getId(), network);
        networkList.add(network);
        network.addObserver(this);
        network.register();
        Collections.sort(networkList);
        setChanged();
        notifyObservers();
        Client.getInstance().getIgnoreListManager().addObserver(this);
    }

    public Network getNetwork(int location) {
        return networkList.get(location);
    }

    public Buffer getBufferById(int bufferId) {
        for (Network network : networkList) {
            if (network.getStatusBuffer() != null && network.getStatusBuffer().getInfo().id == bufferId)
                return network.getStatusBuffer();
            if (network.getBuffers().hasBuffer(bufferId)) {
                return network.getBuffers().getBuffer(bufferId);
            }
        }
        return null;
    }

    public Network getNetworkById(int networkId) {
        return networkMap.get(networkId);
    }

    public void addBuffer(Buffer buffer) {
        int id = buffer.getInfo().networkId;
        for (Network network : networkList) {
            if (network.getId() == id) {
                network.addBuffer(buffer);
                return;
            }
        }
        throw new RuntimeException("Buffer + " + buffer.getInfo().name + " has no valid network id " + id);
    }

    public List<Network> getNetworkList() {
        return networkList;
    }

    public int size() {
        return networkList.size();
    }

    @Override
    public void update(Observable observable, Object data) {
        if (observable == Client.getInstance().getIgnoreListManager())
            updateIgnore();

        setChanged();
        notifyObservers();
    }

    public void removeNetwork(int networkId) {
        Network network = networkMap.get(networkId);
        networkMap.remove(networkId);
        networkList.remove(network);
        network.deleteObservers();
        networkList.remove(network);
        network.unregister();
        Collections.sort(networkList);
        setChanged();
        notifyObservers();
    }

    public void clear() {
        networkList.clear();
        networkMap.clear();
    }

    public void updateIgnore() {
        for (Network network : networkList) {
            network.updateIgnore();
        }
    }
}
