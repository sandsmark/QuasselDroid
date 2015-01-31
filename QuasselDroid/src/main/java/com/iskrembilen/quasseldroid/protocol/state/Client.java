package com.iskrembilen.quasseldroid.protocol.state;

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
    public ConnectionChangedEvent.Status status;

    private Client() {
        ignoreListManager.addObserver(this);
    }

    public static Client getInstance() {
        if (instance==null)
            instance = new Client();

        return instance;
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
            networks.updateFiltered();
        }
    }

    @Subscribe
    public void onConnectionChanged(ConnectionChangedEvent event) {
        Log.d(TAG, "Changing connection status to: " + event.status);
        this.status = event.status;
    }
}
