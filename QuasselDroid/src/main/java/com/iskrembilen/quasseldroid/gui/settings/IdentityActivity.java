package com.iskrembilen.quasseldroid.gui.settings;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;

import com.iskrembilen.quasseldroid.protocol.state.Identity;
import com.iskrembilen.quasseldroid.protocol.state.IdentityCollection;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.UpdateIdentityEvent;
import com.iskrembilen.quasseldroid.gui.dialogs.EditNickDialog;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.melnykov.fab.FloatingActionButton;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class IdentityActivity extends ActionBarActivity implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
    static final String TAG = IdentityActivity.class.getSimpleName();

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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.activity_identities, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Method to add a TabHost
    private static void AddTab(@NonNull IdentityActivity activity, @NonNull TabHost tabHost, @NonNull TabHost.TabSpec tabSpec) {
        tabSpec.setContent(new ContentFactory(activity));
        tabHost.addTab(tabSpec);
    }

    // Manages the Tab changes, synchronizing it with Pages
    public void onTabChanged(@Nullable String tag) {
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

    private @NonNull List<Fragment> getFragments(){
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

        IdentityActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec(getResources().getString(R.string.identity_tab_nicks)).setIndicator(getResources().getString(R.string.identity_tab_nicks)));
        IdentityActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec(getResources().getString(R.string.identity_tab_advanced)).setIndicator(getResources().getString(R.string.identity_tab_advanced)));

        mTabHost.setOnTabChangedListener(this);
    }

    public class MyPageAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;

        public MyPageAdapter(@NonNull FragmentManager fm, @NonNull List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public @Nullable Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        @Override
        public int getCount() {
            return this.fragments.size();
        }
    }

    public static class ContentFactory implements TabHost.TabContentFactory {
        private Context mContext;

        public ContentFactory(@NonNull Context context){
            mContext = context;
        }

        @Override
        public @NonNull View createTabContent(@Nullable String tag) {
            return new View(mContext);
        }
    }

    public static class NicksFragment extends Fragment {
        public static final String IDENTITY_ID = "identityId";

        FloatingActionButton floatingAction;
        EditText realName;
        EditText ident;
        DragSortListView nickList;

        ArrayList<String> nicks;
        boolean nicksModified = false;

        int identityId = -1;

        @Override
        public void setArguments(@NonNull Bundle bundle) {
            Log.d(TAG, "setting identity from intent");

            fromBundle(bundle);
        }

        @Subscribe
        public void onUpdateIdentity(@NonNull UpdateIdentityEvent event) {
            if (event.identity.getIdentityId() == this.identityId) {
                Log.d(TAG, "setting identity from event");
                initData();
            }
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (savedInstanceState!=null)
                fromBundle(savedInstanceState);

            BusProvider.getInstance().register(this);
        }

        public void fromBundle(@NonNull Bundle bundle) {
            if (bundle.containsKey(IDENTITY_ID))
                identityId = bundle.getInt(IDENTITY_ID);
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            outState.putInt(IDENTITY_ID,identityId);
        }

        public @NonNull DragSortController buildController(DragSortListView dslv) {
            DragSortController controller = new DragSortController(dslv);
            controller.setDragHandleId(R.id.list_drag_handle);
            controller.setRemoveEnabled(true);
            controller.setSortEnabled(true);
            controller.setDragInitMode(DragSortController.ON_DRAG);
            controller.setRemoveMode(DragSortController.FLING_REMOVE);
            controller.setBackgroundColor(Color.TRANSPARENT);
            return controller;
        }

        @Override
        public @NonNull View onCreateView(@NonNull LayoutInflater inflater,
                                          @Nullable ViewGroup container,
                                          @Nullable Bundle savedInstanceState) {
            if (savedInstanceState!=null)
                fromBundle(savedInstanceState);

            View root = inflater.inflate(R.layout.fragment_identity_nicks, container, false);
            initElements(root);

            View header = inflater.inflate(R.layout.fragment_identity_nicks_header, nickList, false);
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),R.layout.widget_identity_nick, R.id.text);
            nicks = new ArrayList<>();

            nickList.addHeaderView(header);
            nickList.setAdapter(adapter);

            initElements(root);

            initData();

            DragSortController mController = buildController(nickList);
            nickList.setFloatViewManager(mController);
            nickList.setOnTouchListener(mController);
            nickList.setDropListener(new DragSortListView.DropListener() {
                @Override
                public void drop(int from, int to) {
                if (from != to) {
                    Log.d(TAG,"Requesting nick reorder");

                    String item = adapter.getItem(from);
                    nicks.remove(from);
                    nicks.add(to, item);
                    updateAdapter();
                }
                }
            });
            nickList.setRemoveListener(new DragSortListView.RemoveListener() {
                @Override
                public void remove(int i) {
                    Log.d(TAG,"Requesting nick remove");

                    nicks.remove(i);
                    updateAdapter();
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
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    EditNickDialog dialog = EditNickDialog.newInstance(position-nickList.getHeaderViewsCount(), identityId);
                    dialog.setOnResultListener(new EditNickDialog.OnResultListener<String>() {
                        @Override
                        public void onClick(String result) {
                            nicks.add(result);
                            updateAdapter();
                        }
                    });
                    dialog.show(getFragmentManager(), TAG);
                }
            });

            floatingAction.attachToListView(nickList);
            floatingAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditNickDialog dialog = EditNickDialog.newInstance(-1, identityId);
                    dialog.setOnResultListener(new EditNickDialog.OnResultListener<String>() {
                        @Override
                        public void onClick(String result) {
                            nicks.add(result);
                            updateAdapter();
                        }
                    });
                    dialog.show(getFragmentManager(), TAG);
                }
            });

            return root;
        }

        public void storeToIdentity() {
            Identity identity = IdentityCollection.getInstance().getIdentity(identityId);

            initElements(getView());

            identity.setNicks(nicks);
            identity.setRealName(realName.getText().toString());
            identity.setIdent(ident.getText().toString());
        }

        private void initElements(@NonNull View view) {
            floatingAction =    (FloatingActionButton)  view.findViewById(R.id.fab);
            realName =          (EditText)              view.findViewById(R.id.identity_realname);
            ident =             (EditText)              view.findViewById(R.id.identity_ident);
            nickList =          (DragSortListView)      view.findViewById(R.id.nickList);
        }

        private void initData() {
            Identity identity = IdentityCollection.getInstance().getIdentity(identityId);

            if (identityId == -1) {
                Log.d(TAG, "Identity empty");
                Toast.makeText(getActivity().getApplicationContext(),"Error: Identity could not be found",Toast.LENGTH_SHORT).show();
                getActivity().finish();
                return;
            } else if (identity == null) {
                Log.d(TAG, "Identity nonexistent");
                Toast.makeText(getActivity().getApplicationContext(),"Error: Identity could not be found",Toast.LENGTH_SHORT).show();
                getActivity().finish();
                return;
            }

            nicks.clear();
            nicks.addAll(identity.getNicks());
            updateAdapter();

            realName.setText(identity.getRealName());
            ident.setText(identity.getIdent());
        }

        private void updateAdapter() {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) nickList.getInputAdapter();
            adapter.clear();
            adapter.addAll(nicks);
        }
    }
    public static class MessagesFragment extends Fragment {
        public static final String IDENTITY_ID = "identityId";

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
            fromBundle(bundle);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            fromBundle(savedInstanceState);
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
            Identity identity = IdentityCollection.getInstance().getIdentity(identityId);
            if (identityId == -1) {
                Log.d(TAG, "Identity empty");
                Toast.makeText(getActivity().getApplicationContext(),"Error: Identity could not be found",Toast.LENGTH_SHORT).show();
                getActivity().finish();
                return;
            } else if (identity == null) {
                Log.d(TAG, "Identity nonexistent");
                Toast.makeText(getActivity().getApplicationContext(),"Error: Identity could not be found",Toast.LENGTH_SHORT).show();
                getActivity().finish();
                return;
            }

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
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {

            fromBundle(savedInstanceState);

            View root = inflater.inflate(R.layout.fragment_identity_messages, container, false);
            awayNick = (EditText) root.findViewById(R.id.identity_away_nick);
            awayReason = (EditText) root.findViewById(R.id.identity_away_reason);
            autoAwayTime = (EditText) root.findViewById(R.id.identity_auto_away_time);
            autoAwayReason = (EditText) root.findViewById(R.id.identity_auto_away_reason);
            detachAwayReason = (EditText) root.findViewById(R.id.identity_detach_away_reason);
            kickReason = (EditText) root.findViewById(R.id.identity_kick_reason);
            partReason = (EditText) root.findViewById(R.id.identity_part_reason);
            quitReason = (EditText) root.findViewById(R.id.identity_quit_reason);

            awayNickEnabled = (CheckBox) root.findViewById(R.id.identity_away_nick_enabled);
            awayReasonEnabled = (CheckBox) root.findViewById(R.id.identity_away_reason_enabled);
            autoAwayEnabled = (CheckBox) root.findViewById(R.id.identity_auto_away_enabled);
            autoAwayReasonEnabled = (CheckBox) root.findViewById(R.id.identity_auto_away_reason_enabled);
            detachAwayEnabled = (CheckBox) root.findViewById(R.id.identity_detach_away_enabled);
            detachAwayReasonEnabled = (CheckBox) root.findViewById(R.id.identity_detach_away_reason_enabled);

            initData();

            return root;
        }

        public void fromBundle(Bundle bundle) {
            if (bundle!=null && bundle.containsKey(IDENTITY_ID))
                identityId = bundle.getInt(IDENTITY_ID);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            outState.putInt(IDENTITY_ID,identityId);
        }
    }
}