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

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent;
import com.iskrembilen.quasseldroid.protocol.state.serializers.SyncableObject;
import com.squareup.otto.Subscribe;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class Client implements Observer {
    private static final String TAG = Client.class.getSimpleName();

    private static Client instance;
    private NetworkCollection networks = new NetworkCollection();
    private IdentityCollection identities = new IdentityCollection();
    private ObjectCollection objects = new ObjectCollection();
    private IgnoreListManager ignoreListManager = new IgnoreListManager();
    private Activity activity;
    public ConnectionChangedEvent.Status status;

    private Client() {
        ignoreListManager.addObserver(this);
    }

    public static Client getInstance() {
        if (instance==null)
            instance = new Client();

        return instance;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public IdentityCollection getIdentities() {
        return identities;
    }

    public NetworkCollection getNetworks() {
        return networks;
    }

    public ObjectCollection getObjects() {
        return objects;
    }

    public IgnoreListManager getIgnoreListManager() {
        return ignoreListManager;
    }

    /**
     * This method is called if the specified {@code Observable} object's
     * {@code notifyObservers} method is called (because the {@code Observable}
     * object has been updated.
     *
     * @param observable the {@link java.util.Observable} object.
     * @param data       the data passed to {@link java.util.Observable#notifyObservers(Object)}.
     */
    @Override
    public void update(Observable observable, Object data) {
        if (observable == ignoreListManager) {
            networks.updateIgnore();
        }
    }

    public void runOnUiThread(Runnable runnable) {
        activity.runOnUiThread(runnable);
    }

    @Subscribe
    public void onConnectionChanged(ConnectionChangedEvent event) {
        Log.d(TAG, "Changing connection status to: " + event.status);
        this.status = event.status;
    }
}
