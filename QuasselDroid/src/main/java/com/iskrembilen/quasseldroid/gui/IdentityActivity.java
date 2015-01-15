package com.iskrembilen.quasseldroid.gui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TabHost;

import com.iskrembilen.quasseldroid.Identity;
import com.iskrembilen.quasseldroid.IdentityCollection;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.UpdateIdentityEvent;
import com.iskrembilen.quasseldroid.gui.dialogs.EditNickDialog;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class IdentityActivity extends ActionBarActivity implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {

    MyPageAdapter pageAdapter;
    private ViewPager mViewPager;
    private TabHost mTabHost;

    NicksFragment nicksFragment;
    MessagesFragment messagesFragment;

    int identityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_identity);

        Intent i = getIntent();
        if (i.hasExtra("identityId"))
            identityId = i.getIntExtra("identityId", 0);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        // Tab Initialization
        initialiseTabHost();

        // Fragments and ViewPager Initialization
        List<Fragment> fragments = getFragments();
        pageAdapter = new MyPageAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(pageAdapter);
        mViewPager.setOnPageChangeListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_check);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                nicksFragment.storeToIdentity();
                messagesFragment.storeToIdentity();
                finish();
                return true;
            case R.id.menu_cancel:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_identities, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Method to add a TabHost
    private static void AddTab(IdentityActivity activity, TabHost tabHost, TabHost.TabSpec tabSpec) {
        tabSpec.setContent(new ContentFactory(activity));
        tabHost.addTab(tabSpec);
    }

    // Manages the Tab changes, synchronizing it with Pages
    public void onTabChanged(String tag) {
        int pos = this.mTabHost.getCurrentTab();
        this.mViewPager.setCurrentItem(pos);
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    // Manages the Page changes, synchronizing it with Tabs
    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        int pos = this.mViewPager.getCurrentItem();
        this.mTabHost.setCurrentTab(pos);
    }

    @Override
    public void onPageSelected(int arg0) {
    }

    private List<Fragment> getFragments(){
        List<Fragment> fList = new ArrayList<>();
        Bundle bundle = new Bundle();
        bundle.putInt("identityId",identityId);

        nicksFragment = new NicksFragment();
        nicksFragment.setArguments(bundle);
        fList.add(nicksFragment);

        messagesFragment = new MessagesFragment();
        messagesFragment.setArguments(bundle);
        fList.add(messagesFragment);

        return fList;
    }

    // Tabs Creation
    private void initialiseTabHost() {
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();

        IdentityActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec("Tab1").setIndicator("Tab1"));
        IdentityActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec("Tab2").setIndicator("Tab2"));

        mTabHost.setOnTabChangedListener(this);
    }

    public class MyPageAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;

        public MyPageAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        @Override
        public int getCount() {
            return this.fragments.size();
        }
    }

    public static class ContentFactory implements TabHost.TabContentFactory {
        private Context mContext;

        public ContentFactory(Context context){
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {
            return new View(mContext);
        }
    }

    public static class NicksFragment extends Fragment {
        public static final String TAG = NicksFragment.class.getSimpleName();

        EditText realName;
        EditText ident;
        DragSortListView nickList;
        ArrayAdapter<String> adapter;

        boolean created = false;

        int identityId = -1;
        private DragSortController mController;

        @Override
        public void setArguments(Bundle bundle) {
            Log.d(TAG, "setting identity from intent");

            fromBundle(bundle);

            if (created)
                initData();
        }

        @Subscribe
        public void onUpdateIdentity(UpdateIdentityEvent event) {
            if (event.identity.getIdentityId() == this.identityId) {
                Log.d(TAG, "setting identity from event");
                initData();
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            fromBundle(savedInstanceState);

            BusProvider.getInstance().register(this);
        }

        public void fromBundle(Bundle bundle) {
            if (bundle!=null && bundle.containsKey("identityId"))
                identityId = bundle.getInt("identityId");
        }

        public DragSortController buildController(DragSortListView dslv) {
            DragSortController controller = new DragSortController(dslv);
            controller.setDragHandleId(R.id.drag_handle);
            controller.setRemoveEnabled(true);
            controller.setSortEnabled(true);
            controller.setDragInitMode(DragSortController.ON_DRAG);
            controller.setRemoveMode(DragSortController.FLING_REMOVE);
            controller.setBackgroundColor(Color.TRANSPARENT);
            return controller;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.fragment_identity_nicks, container, false);
            nickList = (DragSortListView) root.findViewById(R.id.nickList);
            realName = (EditText) root.findViewById(R.id.identity_realname);
            ident = (EditText) root.findViewById(R.id.identity_ident);
            adapter = new ArrayAdapter<>(getActivity(),R.layout.widget_identity_nick,R.id.text);

            initData();

            nickList.setAdapter(adapter);
            mController = buildController(nickList);
            nickList.setFloatViewManager(mController);
            nickList.setOnTouchListener(mController);
            nickList.setDropListener(new DragSortListView.DropListener() {
                @Override
                public void drop(int from, int to) {
                    if (from != to) {
                        String item = adapter.getItem(from);
                        Identity identity = IdentityCollection.getInstance().getIdentity(identityId);

                        List<String> nicks = identity.getNicks();
                        nicks.remove(from);
                        nicks.add(to, item);
                        identity.setNicks(nicks);
                        initData();
                    }
                }
            });
            nickList.setRemoveListener(new DragSortListView.RemoveListener() {
                @Override
                public void remove(int i) {
                    Identity identity = IdentityCollection.getInstance().getIdentity(identityId);

                    List<String> nicks = identity.getNicks();
                    nicks.remove(i);
                    identity.setNicks(nicks);
                    initData();
                }
            });
            nickList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                /**
                 * Callback method to be invoked when an item in this AdapterView has
                 * been clicked.
                 * <p/>
                 * Implementers can call getItemAtPosition(position) if they need
                 * to access the data associated with the selected item.
                 *
                 * @param parent   The AdapterView where the click happened.
                 * @param view     The view within the AdapterView that was clicked (this
                 *                 will be a view provided by the adapter)
                 * @param position The position of the view in the adapter.
                 * @param id       The row id of the item that was clicked.
                 */
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    EditNickDialog.newInstance(position,identityId).show(getFragmentManager(),TAG);
                }
            });

            Button addNick = (Button) root.findViewById(R.id.add_nick);
            addNick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditNickDialog.newInstance(-1, identityId).show(getFragmentManager(), TAG);
                }
            });

            created = true;

            return root;
        }

        public void storeToIdentity() {
            Identity identity = IdentityCollection.getInstance().getIdentity(identityId);

            identity.setRealName(realName.getText().toString());
            identity.setIdent(ident.getText().toString());
        }

        private void initData() {
            if (identityId==-1)
                Log.d(TAG, "Identity empty");

            Identity identity = IdentityCollection.getInstance().getIdentity(identityId);

            adapter.clear();
            adapter.addAll(identity.getNicks());
            realName.setText(identity.getRealName());
            ident.setText(identity.getIdent());
        }
    }

    public static class MessagesFragment extends Fragment {
        static final String TAG = MessagesFragment.class.getSimpleName();

        int identityId;

        EditText awayNick;
        EditText awayReason;
        EditText autoAwayTime;
        EditText autoAwayReason;
        EditText detachAwayReason;

        CheckBox awayNickEnabled;
        CheckBox awayReasonEnabled;
        CheckBox autoAwayEnabled;
        CheckBox autoAwayReasonEnabled;
        CheckBox detachAwayEnabled;
        CheckBox detachAwayReasonEnabled;

        EditText kickReason;
        EditText partReason;
        EditText quitReason;

        @Override
        public void setArguments(Bundle bundle) {
            if (bundle.containsKey("identityId"))
                identityId = bundle.getInt("identityId");
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        public void storeToIdentity() {
            Identity identity = IdentityCollection.getInstance().getIdentity(identityId);

            identity.setAwayNick(awayNick.getText().toString());
            identity.setAwayReason(awayReason.getText().toString());
            identity.setAutoAwayTime(Integer.valueOf(autoAwayTime.getText().toString()));
            identity.setAutoAwayReason(autoAwayReason.getText().toString());
            identity.setDetachAwayReason(detachAwayReason.getText().toString());
            identity.setKickReason(kickReason.getText().toString());
            identity.setPartReason(partReason.getText().toString());
            identity.setQuitReason(quitReason.getText().toString());

            identity.setAwayNickEnabled(awayNickEnabled.isChecked());
            identity.setAwayReasonEnabled(awayReasonEnabled.isChecked());
            identity.setAutoAwayEnabled(autoAwayEnabled.isChecked());
            identity.setAutoAwayReasonEnabled(autoAwayReasonEnabled.isChecked());
            identity.setDetachAwayEnabled(detachAwayEnabled.isChecked());
            identity.setDetachAwayReasonEnabled(detachAwayReasonEnabled.isChecked());
        }

        private void initData() {
            if (identityId==-1)
                Log.d(TAG, "Identity empty");

            Identity identity = IdentityCollection.getInstance().getIdentity(identityId);

            awayNick.setText(identity.getAwayNick());
            awayReason.setText(identity.getAwayReason());
            autoAwayTime.setText(String.valueOf(identity.getAutoAwayTime()));
            autoAwayReason.setText(identity.getAutoAwayReason());
            detachAwayReason.setText(identity.getDetachAwayReason());
            kickReason.setText(identity.getKickReason());
            partReason.setText(identity.getPartReason());
            quitReason.setText(identity.getQuitReason());

            awayNickEnabled.setChecked(identity.getAwayNickEnabled());
            awayReasonEnabled.setChecked(identity.getAwayReasonEnabled());
            autoAwayEnabled.setChecked(identity.getAutoAwayEnabled());
            autoAwayReasonEnabled.setChecked(identity.getAutoAwayReasonEnabled());
            detachAwayEnabled.setChecked(identity.getDetachAwayEnabled());
            detachAwayReasonEnabled.setChecked(identity.getDetachAwayReasonEnabled());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View root = inflater.inflate(R.layout.fragment_identity_messages, container, false);
            awayNick = (EditText) root.findViewById(R.id.identity_awayNick);
            awayReason = (EditText) root.findViewById(R.id.identity_awayReason);
            autoAwayTime = (EditText) root.findViewById(R.id.identity_autoAwayTime);
            autoAwayReason = (EditText) root.findViewById(R.id.identity_autoAwayReason);
            detachAwayReason = (EditText) root.findViewById(R.id.identity_detachAwayReason);
            kickReason = (EditText) root.findViewById(R.id.identity_kickReason);
            partReason = (EditText) root.findViewById(R.id.identity_partReason);
            quitReason = (EditText) root.findViewById(R.id.identity_quitReason);

            awayNickEnabled = (CheckBox) root.findViewById(R.id.identity_awayNickEnabled);
            awayReasonEnabled = (CheckBox) root.findViewById(R.id.identity_awayReasonEnabled);
            autoAwayEnabled = (CheckBox) root.findViewById(R.id.identity_autoAwayEnabled);
            autoAwayReasonEnabled = (CheckBox) root.findViewById(R.id.identity_autoAwayReasonEnabled);
            detachAwayEnabled = (CheckBox) root.findViewById(R.id.identity_detachAwayEnabled);
            detachAwayReasonEnabled = (CheckBox) root.findViewById(R.id.identity_detachAwayReasonEnabled);

            initData();

            return root;
        }
    }
}
