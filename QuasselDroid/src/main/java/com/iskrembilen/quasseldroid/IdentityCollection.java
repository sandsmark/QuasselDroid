package com.iskrembilen.quasseldroid;

import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collection;
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
        identities.put(identity.identityId,identity);
        ids.add(identity.identityId);
    }

    public void removeIdentity(Identity identity) {
        Log.d(TAG, "removeidentity: " + identity.identityId);
        identities.remove(identity.identityId);
        ids.remove(identity.identityId);
    }

    public void removeIdentity(int identityId) {
        Log.d(TAG, "removeidentity: " + identityId);
        identities.remove(identityId);
        ids.remove(identityId);
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
