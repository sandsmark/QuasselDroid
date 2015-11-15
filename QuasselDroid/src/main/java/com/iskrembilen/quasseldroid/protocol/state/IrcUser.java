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
import android.util.Log;

import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.protocol.qtcomm.EmptyQVariantException;
import com.iskrembilen.quasseldroid.protocol.qtcomm.QVariant;
import com.iskrembilen.quasseldroid.protocol.qtcomm.QVariantType;
import com.iskrembilen.quasseldroid.protocol.state.serializers.Syncable;
import com.iskrembilen.quasseldroid.protocol.state.serializers.SyncableObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

public class IrcUser extends SyncableObject implements Comparable<IrcUser> {
    @Syncable(type=QVariantType.String)
    public String name;
    @Syncable(type=QVariantType.Bool)
    public boolean away;
    @Syncable(type=QVariantType.String)
    public String awayMessage;
    @Syncable(type=QVariantType.String)
    public String ircOperator;
    @Syncable(type=QVariantType.String)
    public String nick;
    @Syncable(type=QVariantType.StringList)
    public List<String> channels = new ArrayList<>();
    @Syncable(type=QVariantType.String)
    public String server;
    @Syncable(type=QVariantType.String)
    public String realName;
    @Syncable(type=QVariantType.String)
    public String host;
    @Syncable(type=QVariantType.String)
    public String user;
    // public Date loginTime;
    // public Date idleTime;
    @Syncable(type=QVariantType.Bool)
    public boolean encrypted;
    @Syncable(type=QVariantType.Int)
    public int lastAwayMessage;

    public int networkId;

    public String toString() {
        return nick + " away: " + away + " Num chans: " + channels.size();
    }

    public void changed(Object data) {
        this.setChanged();
        notifyObservers(data);
    }

    public void changed() {
        changed(null);
    }

    @Override
    public int compareTo(@NonNull IrcUser another) {
        return this.nick.compareToIgnoreCase(another.nick);
    }

    public void setServer(String server) {
        this.server = server;
        changed();
    }

    public void setNick(String nick) {
        Client.getInstance().getObjects().renameObject(getClassName(), this.nick, nick);
        this.nick = nick;
        changed();
    }

    public String getObjectName() {
        return networkId+"/"+nick;
    }

    public void setAway(boolean away) {
        this.away = away;
        changed();
    }

    public void setAwayMessage(String awayMessage) {
        this.awayMessage = awayMessage;
        changed();
    }

    public void setRealName(String realName) {
        this.realName = realName;
        changed();
    }
}
