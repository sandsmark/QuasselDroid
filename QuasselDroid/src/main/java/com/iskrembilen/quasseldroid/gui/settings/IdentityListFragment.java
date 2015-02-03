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

package com.iskrembilen.quasseldroid.gui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.iskrembilen.quasseldroid.protocol.state.Client;
import com.iskrembilen.quasseldroid.protocol.state.Identity;
import com.iskrembilen.quasseldroid.protocol.state.IdentityCollection;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.RemoveIdentityEvent;
import com.iskrembilen.quasseldroid.events.UpdateIdentityEvent;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.squareup.otto.Subscribe;

public class IdentityListFragment extends PreferenceFragment {

    static final String TAG = IdentityListFragment.class.getCanonicalName();

    ListView identityList;
    ArrayAdapter<Identity> adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.layout_list, container, false);

        identityList = (ListView) root.findViewById(R.id.list);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);

        initData();

        identityList.setAdapter(adapter);

        identityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int identityId = Client.getInstance().getIdentities().getIdentities().get(position).getIdentityId();
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
        adapter.addAll(Client.getInstance().getIdentities().getIdentities());
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
