package com.iskrembilen.quasseldroid.gui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.iskrembilen.quasseldroid.protocol.state.IdentityCollection;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.RemoveIdentityEvent;
import com.iskrembilen.quasseldroid.events.UpdateIdentityEvent;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.squareup.otto.Subscribe;

public class IdentityListFragment extends PreferenceFragment {

    static final String TAG = IdentityListFragment.class.getCanonicalName();

    ListView identityList;
    ArrayAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.layout_identities, container, false);

        identityList = (ListView) root.findViewById(R.id.identity_list);
        adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1);

        initData();

        identityList.setAdapter(adapter);

        identityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int identityId = IdentityCollection.getInstance().getIdentities().get(position).getIdentityId();
                Intent i = new Intent(getActivity(), IdentityActivity.class);
                i.putExtra("identityId",identityId);
                startActivity(i);
            }
        });

        BusProvider.getInstance().register(this);

        return root;
    }

    public void initData() {
        adapter.clear();
        adapter.addAll(IdentityCollection.getInstance().getIdentities());
    }

    @Subscribe
    public void onUpdateIdentity(UpdateIdentityEvent event) {
        initData();
    }

    @Subscribe
    public void onRemoveIdentity(RemoveIdentityEvent event) {
        initData();
    }

}
