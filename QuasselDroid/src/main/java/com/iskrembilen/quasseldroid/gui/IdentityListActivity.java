package com.iskrembilen.quasseldroid.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.iskrembilen.quasseldroid.IdentityCollection;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.RemoveIdentityEvent;
import com.iskrembilen.quasseldroid.events.UpdateIdentityEvent;
import com.iskrembilen.quasseldroid.gui.dialogs.AboutDialog;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.squareup.otto.Subscribe;

public class IdentityListActivity extends ActionBarActivity {

    static final String TAG = IdentityListActivity.class.getCanonicalName();

    ListView identityList;
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_identities);

        identityList = (ListView) findViewById(R.id.identity_list);
        adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1);

        initData();

        identityList.setAdapter(adapter);

        identityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int identityId = IdentityCollection.getInstance().getIdentities().get(position).getIdentityId();
                Intent i = new Intent(IdentityListActivity.this, IdentityActivity.class);
                i.putExtra("identityId",identityId);
                startActivity(i);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        BusProvider.getInstance().register(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mi) {
        switch (mi.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
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
