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

import android.support.annotation.NonNull;

import com.iskrembilen.quasseldroid.Quasseldroid;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.protocol.qtcomm.QVariantType;
import com.iskrembilen.quasseldroid.protocol.state.serializers.Syncable;
import com.iskrembilen.quasseldroid.protocol.state.serializers.SyncableObject;
import de.kuschku.util.BetterSparseArray;
import com.iskrembilen.quasseldroid.util.BufferCollectionHelper;
import com.iskrembilen.quasseldroid.util.Helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class Network extends SyncableObject implements Observer, Comparable<Network> {

    public enum ConnectionState {
        Disconnected(0),
        Connecting(1),
        Initializing(2),
        Initialized(3),
        Reconnecting(4),
        Disconnecting(5);
        private int value;
        static final Map<Integer, ConnectionState> intToStateMap = new BetterSparseArray<>();

        static {
            for (ConnectionState type : ConnectionState.values()) {
                intToStateMap.put(type.getValue(), type);
            }
        }

        public static ConnectionState getForValue(int value) {
            return intToStateMap.get(value);
        }

        ConnectionState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

    private int networkId;
    private Buffer statusBuffer;
    private BufferCollection buffers;
    private List<IrcUser> userList;
    private HashMap<String, IrcUser> nickUserMap;

    private boolean open;

    @Syncable(type=QVariantType.String)
    private String          networkName;

    @Syncable(type=QVariantType.Bool)
    private boolean         isConnected;
    @Syncable(type=QVariantType.Int)
    private ConnectionState connectionState;
    @Syncable(type=QVariantType.String)
    private String          currentServer;
    @Syncable(type=QVariantType.Int)
    private int             latency;
    @Syncable(type=QVariantType.String)
    private String          myNick;

    @Syncable(type=QVariantType.Int)
    public int identityId;

    @Syncable(type=QVariantType.StringList)
    private List<String>    ServerList;

    @Syncable(type=QVariantType.Bool)
    private boolean         useAutoReconnect;
    @Syncable(type=QVariantType.UShort)
    private short           autoReconnectRetries;
    @Syncable(type=QVariantType.Int)
    private int             autoReconnectInterval;
    @Syncable(type=QVariantType.Bool)
    private boolean         useRandomServer;
    @Syncable(type=QVariantType.Bool)
    private boolean         rejoinChannels;
    @Syncable(type=QVariantType.Bool)
    private boolean         unlimitedReconnectRetries;
    @Syncable(type=QVariantType.ByteArray)
    private byte[]          codecForEncoding;
    @Syncable(type=QVariantType.ByteArray)
    private byte[]          codecForServer;
    @Syncable(type=QVariantType.ByteArray)
    private byte[]          codecForDecoding;

    @Syncable(type=QVariantType.Bool)
    private boolean         useAutoIdentify;
    @Syncable(type=QVariantType.String)
    private String          autoIdentifyService;
    @Syncable(type=QVariantType.String)
    private String          autoIdentifyPassword;
    @Syncable(type=QVariantType.Bool)
    private boolean         useSasl;
    @Syncable(type=QVariantType.String)
    private String          saslAccount;
    @Syncable(type=QVariantType.String)
    private String          saslPassword;

    @Syncable(type=QVariantType.StringList)
    private List<String>    perform;

    @Syncable(type=QVariantType.Map)
    private Map<String,String> Supports;

    public Network(int networkId) {
        this.networkId = networkId;
        userList = new ArrayList<IrcUser>();
        buffers = new BufferCollection();
        buffers.addObserver(this);
        nickUserMap = new HashMap<String, IrcUser>();
        open = false;
        connectionState = ConnectionState.Disconnected;
        isConnected = false;
        latency = 0;
    }

    public Buffer getStatusBuffer() {
        return statusBuffer;
    }

    public void setStatusBuffer(Buffer statusBuffer) {
        this.statusBuffer = statusBuffer;
        statusBuffer.addObserver(this);
        this.setChanged();
        notifyObservers();
    }

    public void setIdentity(int identityId) {
        this.identityId = identityId;
    }

    public int getId() {
        return networkId;
    }

    public BufferCollection getBuffers() {
        return buffers;
    }

    public void addBuffer(Buffer buffer) {
        buffers.addBuffer(buffer);
    }


    public void setNetworkName(String networkName) {
        this.networkName = networkName;
        setChanged();
        notifyObservers();

        updateTopic();
    }


    public String getName() {
        return networkName;
    }

    public Boolean isConnected() {
        return isConnected;
    }


    public void setUserList(@NonNull List<IrcUser> userList) {
        for (IrcUser user : userList) {
            user.deleteObserver(this);
            user.unregister();
        }
        this.userList = userList;
        nickUserMap.clear();
        for (IrcUser user : userList) {
            nickUserMap.put(user.nick, user);
            user.addObserver(this);
            user.register();
        }

        updateTopic();
    }


    public List<IrcUser> getUserList() {
        return userList;
    }


    @Override
    public void update(Observable observable, Object data) {
        if (data != null && ((Integer) data == R.id.USER_CHANGEDNICK)) {
            IrcUser changedUser = (IrcUser) observable;
            for (Map.Entry<String, IrcUser> entry : nickUserMap.entrySet()) {
                if (entry.getValue().nick.equals(changedUser.nick)) {
                    nickUserMap.remove(entry.getKey());
                    nickUserMap.put(entry.getValue().nick, entry.getValue());
                    break;
                }
            }
        }
        setChanged();
        notifyObservers();
    }


    @Override
    public int compareTo(Network another) {
        return getName().compareTo(another.getName());
    }


    public void setOpen(boolean open) {
        this.open = open;
    }


    public boolean isOpen() {
        return open;
    }


    public String getMyNick() {
        return myNick;
    }


    public void setMyNick(String nick) {
        this.myNick = nick;
    }

    public void onUserJoined(IrcUser user) {
        userList.add(user);
        nickUserMap.put(user.nick, user);
        user.addObserver(this);
        user.register();
        updateTopic();
    }


    public void onUserQuit(String nick) {
        IrcUser user = nickUserMap.get(nick);
        // The user already was removed
        if (user==null)
            return;

        for (Buffer buffer : buffers.getBufferList(BufferCollectionHelper.FILTER_SET_ALL)) {
            if (user.channels.contains(buffer.getInfo().name)) {
                buffer.getUsers().removeUserByNick(nick);
            }
        }
        userList.remove(user);
        nickUserMap.remove(nick);
        user.deleteObserver(this);
        user.unregister();
        updateTopic();
    }


    public void onUserParted(String nick, String bufferName) {
        IrcUser user = nickUserMap.get(nick);
        // The user already was removed
        if (user==null)
            return;

        if (user.channels.contains(bufferName)) {
            user.channels.remove(bufferName);
        }
        for (Buffer buffer : buffers.getBufferList(BufferCollectionHelper.FILTER_SET_ALL)) {
            if (buffer.getInfo().name.equalsIgnoreCase(bufferName)) {
                buffer.getUsers().removeUserByNick(nick);
                if (nick.equalsIgnoreCase(getMyNick())) {
                    buffer.setActive(false);
                }
                break;
            }
        }
    }


    public boolean hasNick(String nick) {
        return nickUserMap.containsKey(nick);
    }


    public IrcUser getUserByNick(String nick) {
        return nickUserMap.get(nick);
    }

    public boolean containsBuffer(int id) {
        return buffers.hasBuffer(id);
    }


    public int getBufferCount() {
        return buffers.getBufferCount(BufferCollectionHelper.FILTER_SET_VISIBLE);
    }


    public void setConnectionState(ConnectionState state) {
        this.connectionState = state;
    }


    public void setConnected(boolean connected) {
        if (connected) {
            setOpen(true);
            if (statusBuffer != null) statusBuffer.setActive(true);

        } else {
            setOpen(false);
            if (statusBuffer != null) statusBuffer.setActive(false);
            for (Buffer buffer : buffers.getBufferList(BufferCollectionHelper.FILTER_SET_ALL)) {
                buffer.setActive(false);
            }
        }
        this.isConnected = connected;
        this.setChanged();
        notifyObservers();
    }


    public void removeBuffer(int bufferId) {
        buffers.removeBuffer(bufferId);

    }

    public void setLatency(int latency) {
        this.latency = latency;

        updateTopic();
    }

    public int getLatency() {
        return latency;
    }

    public void setCurrentServer(String server) {
        this.currentServer = server;

        updateTopic();
    }

    public String getCurrentServer() {
        return currentServer;
    }

    public int getCountUsers() {
        return userList.size();
    }

    private void updateTopic(){
        if(statusBuffer != null){
            statusBuffer.setTopic(networkName + " (" + currentServer + ") | "
                    + Quasseldroid.applicationContext.getResources().getString(R.string.users) + ": "
                    + userList.size() + " | " + Helper.formatLatency(latency, Quasseldroid.applicationContext.getResources()));
        }
    }

    public void renameUser(String oldNick, String newNick) {
        nickUserMap.put(newNick,nickUserMap.remove(oldNick));
    }

    @Override
    public String getObjectName() {
        return String.valueOf(networkId);
    }

    public void updateIgnore() {
        buffers.updateIgnore();
    }
}
