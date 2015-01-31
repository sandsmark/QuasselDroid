package com.iskrembilen.quasseldroid.protocol.state;

import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class IdentityCollection {

    static final String TAG = IdentityCollection.class.getSimpleName();

    private SparseArray<Identity> identities = new SparseArray<>();
    private Set<Integer> ids = new LinkedHashSet<>();

    private static IdentityCollection instance = new IdentityCollection();

    public static IdentityCollection getInstance() {
        return instance;
    }

    public void clear() {
        Log.d(TAG, "clear");
        identities.clear();
        ids.clear();
    }

    public void putIdentity(Identity identity) {
        identities.put(identity.getIdentityId(),identity);
        ids.add(identity.getIdentityId());
        Client.getInstance().getObjects().putObject("Identity",String.valueOf(identity.getIdentityId()),identity);
    }

    public void removeIdentity(Identity identity) {
        Log.d(TAG, "removeidentity: " + identity.getIdentityId());
        identities.remove(identity.getIdentityId());
        ids.remove(identity.getIdentityId());
        Client.getInstance().getObjects().removeObject("Identity",String.valueOf(identity.getIdentityId()));
    }

    public void removeIdentity(int identityId) {
        Log.d(TAG, "removeidentity: " + identityId);
        identities.remove(identityId);
        ids.remove(identityId);
        Client.getInstance().getObjects().removeObject("Identity",String.valueOf(identityId));
    }

    public List<Identity> getIdentities() {
        List<Identity> list = new ArrayList<>(ids.size());
        for (int id : ids) {
            list.add(identities.get(id));
        }
        return list;
    }

    public Identity getIdentity(int id) {
        return identities.get(id);
    }
}
