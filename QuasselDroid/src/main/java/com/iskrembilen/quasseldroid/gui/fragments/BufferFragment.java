/*
    QuasselDroid - Quassel client for Android
 	Copyright (C) 2011 Ken Børge Viktil
 	Copyright (C) 2011 Magnus Fjell
 	Copyright (C) 2011 Martin Sandsmark <martin.sandsmark@kde.org>

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

package com.iskrembilen.quasseldroid.gui.fragments;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Predicate;
import com.idunnololz.widgets.AnimatedExpandableListView;
import com.iskrembilen.quasseldroid.Buffer;
import com.iskrembilen.quasseldroid.BufferInfo;
import com.iskrembilen.quasseldroid.BufferUtils;
import com.iskrembilen.quasseldroid.Network;
import com.iskrembilen.quasseldroid.NetworkCollection;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.BufferListFontSizeChangedEvent;
import com.iskrembilen.quasseldroid.events.BufferOpenedEvent;
import com.iskrembilen.quasseldroid.events.DisconnectCoreEvent;
import com.iskrembilen.quasseldroid.events.NetworksAvailableEvent;
import com.iskrembilen.quasseldroid.events.QueryUserEvent;
import com.iskrembilen.quasseldroid.events.UserClickedEvent;
import com.iskrembilen.quasseldroid.gui.MainActivity;
import com.iskrembilen.quasseldroid.gui.PreferenceActivity;
import com.iskrembilen.quasseldroid.gui.base.XMLHeaderAnimatedExpandableListView;
import com.iskrembilen.quasseldroid.gui.dialogs.JoinChannelDialog;
import com.iskrembilen.quasseldroid.util.BufferCollectionHelper;
import com.iskrembilen.quasseldroid.util.BufferHelper;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.iskrembilen.quasseldroid.util.Helper;
import com.iskrembilen.quasseldroid.util.ThemeUtil;
import com.squareup.otto.Subscribe;

import java.io.Serializable;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class BufferFragment extends Fragment implements Serializable {

    public static final String BUFFER_ID_EXTRA = "bufferid";
    public static final String BUFFER_NAME_EXTRA = "buffername";

    private static final String TAG = BufferFragment.class.getSimpleName();

    private static final String ITEM_POSITION_KEY = "itempos";
    private static final String LIST_POSITION_KEY = "listpos";

    private static final Set<Predicate<Buffer>> DEFAULT_FITLERS = BufferCollectionHelper.FILTER_SET_VISIBLE;

    BufferListAdapter bufferListAdapter;
    XMLHeaderAnimatedExpandableListView bufferList;
    Spinner filterSpinner;

    private int restoreListPosition = 0;
    private int restoreItemPosition = 0;

    private ActionModeData actionModeData = new ActionModeData();

    public static BufferFragment newInstance() {
        return new BufferFragment();
    }

    public void finishActionMode() {
        if (actionModeData!=null && actionModeData.actionMode!=null) actionModeData.actionMode.finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            restoreListPosition = savedInstanceState.getInt(LIST_POSITION_KEY);
            restoreItemPosition = savedInstanceState.getInt(ITEM_POSITION_KEY);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Toolbar toolbar;

        View root = inflater.inflate(R.layout.fragment_buffers, container, false);

        bufferList = (XMLHeaderAnimatedExpandableListView) root.findViewById(R.id.buffer_list);
        if (bufferList.hasHeaderView()) {
            toolbar = (Toolbar) bufferList.getHeaderView();
        } else {
            toolbar = (Toolbar) root.findViewById(R.id.buffer_toolbar);
        }

        if (toolbar.getPaddingTop()==getResources().getDimension(R.dimen.status_bar_height)) {
            toolbar.setPadding(
                    toolbar.getPaddingLeft(),
                    Math.round(Helper.getStatusBarHeight(getActivity())),
                    toolbar.getPaddingRight(),
                    toolbar.getPaddingBottom());
        }

        ArrayAdapter adapter = new ArrayAdapter<>(getActivity(),R.layout.widget_spinner_item,BufferCollectionHelper.FILTER_NAMES);
        adapter.setDropDownViewResource(R.layout.widget_spinner_dropdown_item);
        filterSpinner = (Spinner) toolbar.findViewById(R.id.filter_spinner);
        filterSpinner.setAdapter(adapter);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (BufferCollectionHelper.LIST_FILTERS.size()<position||BufferCollectionHelper.LIST_FILTERS.get(position)==null)
                    onNothingSelected(parent);

                bufferListAdapter.setFilters(BufferCollectionHelper.LIST_FILTERS.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                bufferListAdapter.setFilters(DEFAULT_FITLERS);
            }
        });
        filterSpinner.setSelection(1);

        toolbar.inflateMenu(R.menu.fragment_buffer);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_join_channel:
                        if (bufferListAdapter.networks == null)
                            Toast.makeText(getActivity(), getString(R.string.not_available), Toast.LENGTH_SHORT).show();
                        else showJoinChannelDialog();
                        return true;
                    case R.id.menu_preferences:
                        Intent i = new Intent(getActivity(), PreferenceActivity.class);
                        startActivity(i);
                        return true;
                    case R.id.menu_disconnect:
                        BusProvider.getInstance().post(new DisconnectCoreEvent());
                        return true;
                }
                return false;
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bufferListAdapter = new BufferListAdapter(getActivity());
        bufferList.setAdapter(bufferListAdapter);
        bufferList.setDividerHeight(0);
        bufferList.setGroupIndicator(getResources().getDrawable(android.R.color.transparent));

        bufferList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (bufferList.isGroupExpanded(groupPosition)) {
                    bufferList.collapseGroupWithAnimation(groupPosition);
                    bufferListAdapter.getGroup(groupPosition).setOpen(false);
                } else {
                    bufferList.expandGroupWithAnimation(groupPosition);
                    bufferListAdapter.getGroup(groupPosition).setOpen(true);
                }
                return true;
            }
        });

        bufferList.setOnChildClickListener(new OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                openBuffer(bufferListAdapter.getChild(groupPosition, childPosition));
                return true;
            }
        });

        initActionMenu();

    }

    private void initActionMenu() {
        actionModeData.actionModeCallbackNetwork = new ActionMode.Callback() {

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_buffer_network, menu);

                bufferList.setItemChecked(actionModeData.index, true);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.context_menu_connect:
                        BufferHelper.connectNetwork(actionModeData.id);
                        finishActionMode();
                        return true;
                    case R.id.context_menu_disconnect:
                        BufferHelper.disconnectNetwork(actionModeData.id);
                        finishActionMode();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                actionModeData.actionMode = null;
                bufferList.setItemChecked(actionModeData.index, false);
            }

        };

        actionModeData.actionModeCallbackBuffer = new ActionMode.Callback() {

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_buffer_channel, menu);

                bufferList.setItemChecked(actionModeData.index, true);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.context_menu_join:
                        BufferHelper.joinChannel(actionModeData.id, bufferListAdapter.networks);
                        break;
                    case R.id.context_menu_part:
                        BufferHelper.partChannel(actionModeData.id, bufferListAdapter.networks);
                        break;
                    case R.id.context_menu_delete:
                        BufferHelper.showDeleteConfirmDialog(getActivity(), actionModeData.id);
                        break;
                    case R.id.context_menu_hide_temp:
                        BufferHelper.tempHideChannel(actionModeData.id);
                        break;
                    case R.id.context_menu_hide_perm:
                        BufferHelper.permHideChannel(actionModeData.id);
                        break;
                    case R.id.context_menu_unhide:
                        BufferHelper.unhideChannel(actionModeData.id);
                        break;
                    default:
                        return false;
                }
                mode.finish();
                return true;
            }

            // Called when the user exits the action mode
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                actionModeData.actionMode = null;
                bufferList.setItemChecked(actionModeData.index, false);
            }
        };

        bufferList.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                long packedPosition = bufferList.getExpandableListPosition(position);
                int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
                int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);

                if (ExpandableListView.getPackedPositionType(packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    Buffer buffer = bufferListAdapter.getChild(groupPosition, childPosition);

                    actionModeData.index = bufferList.getFlatListPosition(packedPosition);
                    actionModeData.actionMode = ((MainActivity) getActivity()).getSupportActionBar().startActionMode(actionModeData.actionModeCallbackBuffer);
                    actionModeData.id = buffer.getInfo().id;
                    actionModeData.listItem = view;
                    if (buffer.getInfo().type == BufferInfo.Type.QueryBuffer) {
                        actionModeData.actionMode.getMenu().findItem(R.id.context_menu_part).setVisible(false);
                        actionModeData.actionMode.getMenu().findItem(R.id.context_menu_delete).setVisible(true);
                        actionModeData.actionMode.getMenu().findItem(R.id.context_menu_join).setVisible(false);
                    } else if (buffer.isActive()) {
                        actionModeData.actionMode.getMenu().findItem(R.id.context_menu_part).setVisible(true);
                        actionModeData.actionMode.getMenu().findItem(R.id.context_menu_join).setVisible(false);
                        actionModeData.actionMode.getMenu().findItem(R.id.context_menu_delete).setVisible(false);
                    } else {
                        actionModeData.actionMode.getMenu().findItem(R.id.context_menu_part).setVisible(false);
                        actionModeData.actionMode.getMenu().findItem(R.id.context_menu_delete).setVisible(true);
                        actionModeData.actionMode.getMenu().findItem(R.id.context_menu_join).setVisible(true);
                    }

                    if(buffer.isPermanentlyHidden()) {
                        actionModeData.actionMode.getMenu().findItem(R.id.context_menu_hide_perm).setVisible(false);
                        actionModeData.actionMode.getMenu().findItem(R.id.context_menu_hide_temp).setVisible(false);
                        actionModeData.actionMode.getMenu().findItem(R.id.context_menu_unhide).setVisible(true);
                    } else if(buffer.isTemporarilyHidden()){
                        actionModeData.actionMode.getMenu().findItem(R.id.context_menu_hide_perm).setVisible(true);
                        actionModeData.actionMode.getMenu().findItem(R.id.context_menu_hide_perm).setVisible(true);
                        actionModeData.actionMode.getMenu().findItem(R.id.context_menu_hide_temp).setVisible(false);
                        actionModeData.actionMode.getMenu().findItem(R.id.context_menu_unhide).setVisible(true);
                    } else {
                        actionModeData.actionMode.getMenu().findItem(R.id.context_menu_hide_perm).setVisible(true);
                        actionModeData.actionMode.getMenu().findItem(R.id.context_menu_hide_temp).setVisible(true);
                        actionModeData.actionMode.getMenu().findItem(R.id.context_menu_unhide).setVisible(false);
                    }
                } else if (ExpandableListView.getPackedPositionType(packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                    Network network = bufferListAdapter.getGroup(groupPosition);
                    actionModeData.index = bufferList.getFlatListPosition(packedPosition);
                    actionModeData.actionMode = ((MainActivity) getActivity()).getSupportActionBar().startActionMode(actionModeData.actionModeCallbackNetwork);
                    actionModeData.id = network.getId();
                    actionModeData.listItem = view;
                    if (network.isConnected()) {
                        actionModeData.actionMode.getMenu().findItem(R.id.context_menu_disconnect).setVisible(true);
                        actionModeData.actionMode.getMenu().findItem(R.id.context_menu_connect).setVisible(false);
                    } else {
                        actionModeData.actionMode.getMenu().findItem(R.id.context_menu_disconnect).setVisible(false);
                        actionModeData.actionMode.getMenu().findItem(R.id.context_menu_connect).setVisible(true);
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save position of first visible item
        restoreListPosition = bufferList.getFirstVisiblePosition();
        outState.putInt(LIST_POSITION_KEY, restoreListPosition);

        // Save scroll position of item
        View itemView = bufferList.getChildAt(0);
        restoreItemPosition = itemView == null ? 0 : itemView.getTop();
        outState.putInt(ITEM_POSITION_KEY, restoreItemPosition);
    }

    private void showJoinChannelDialog() {
        List<Network> networkList = bufferListAdapter.networks.getNetworkList();
        String[] networkArray = new String[networkList.size()];

        for (int i = 0; i < networkList.size(); i++) {
            networkArray[i] = networkList.get(i).getName();
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = JoinChannelDialog.newInstance(networkArray);
        newFragment.show(ft, "dialog");
    }

    private void openBuffer(Buffer buffer) {
        buffer.setTemporarilyHidden(false);
        BusProvider.getInstance().post(new BufferOpenedEvent(buffer.getInfo().id));
    }

    @Subscribe
    public void onNetworksAvailable(NetworksAvailableEvent event) {
        if (event.networks != null) {
            event.networks.addObserver(bufferListAdapter);
            bufferListAdapter.setNetworks(event.networks);
        }
    }

    @Subscribe
    public void onBufferListFontSizeChanged(BufferListFontSizeChangedEvent event) {
        bufferListAdapter.notifyDataSetChanged();
    }

    /**
     * Check if a buffer is already existing and switch to it
     * If not a QueryUserEvent is created so the CoreConnService queries the user
     *
     * @param event
     */
    @Subscribe
    public void onUserClicked(UserClickedEvent event) {
        Buffer buffer = bufferListAdapter.networks.getBufferById(event.bufferId);
        Network network = bufferListAdapter.networks.getNetworkById(buffer.getInfo().networkId);
        buffer = network.getBuffers().getBuffer(event.nick);
        if (buffer != null) {
            openBuffer(buffer);
        } else {
            BusProvider.getInstance().post(new QueryUserEvent(event.bufferId, event.nick));
        }
    }

    public static class ViewHolderChild {
        public TextView bufferView;
        public ImageView stateView;
        public View parent;
    }

    public static class ViewHolderGroup {
        public TextView statusView;
        public int networkId;
        public ImageView indicatorView;
    }

    public void setNetworks(NetworkCollection networks) {
        if (bufferListAdapter!=null) bufferListAdapter.setNetworks(networks);
    }

    public class BufferListAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter implements Observer {
        private NetworkCollection networks;
        private LayoutInflater inflater;
        private Activity activity;

        private Set<Predicate<Buffer>> filters = DEFAULT_FITLERS;

        public BufferListAdapter(Activity activity) {
            this.inflater = LayoutInflater.from(activity);
            this.activity = activity;
        }

        public void setNetworks(NetworkCollection networks) {
            if (this.networks!=null) this.networks.deleteObserver(this);

            this.networks = networks;
            if (networks == null)
                return;
            networks.addObserver(this);

            update();
        }

        @Override
        public void update(Observable observable, Object data) {
            update();
        }

        public void update() {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                    for (int group = 0; group < getGroupCount(); group++) {
                        if (getGroup(group).isOpen()) bufferList.expandGroup(group);
                        else bufferList.collapseGroup(group);
                    }
                }
            });
        }

        @Override
        public Buffer getChild(int groupPosition, int childPosition) {
            return networks.getNetwork(groupPosition).getBuffers().getPos(filters,childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return getChild(groupPosition,childPosition).getInfo().id;
        }

        @Override
        public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ViewHolderChild holder = null;
            if (convertView == null || !(convertView.getTag() instanceof ViewHolderChild)) {
                convertView = inflater.inflate(R.layout.widget_buffer_single, null);
                holder = new ViewHolderChild();
                holder.parent = convertView;
                holder.bufferView = (TextView) convertView.findViewById(R.id.buffer_list_item_name);
                holder.stateView = (ImageView) convertView.findViewById(R.id.buffer_status);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolderChild) convertView.getTag();
            }
            Buffer entry = getChild(groupPosition, childPosition);

            int color = ThemeUtil.Color.bufferParted;
            int drawable = R.drawable.ic_status;

            switch (entry.getInfo().type) {
                case StatusBuffer:
                case ChannelBuffer:
                    holder.bufferView.setText(entry.getInfo().name);
                    if (entry.isActive()) {
                        color = ThemeUtil.Color.bufferStateActive;
                    }
                    drawable = R.drawable.ic_status_channel;
                    break;
                case QueryBuffer:
                    String nick = entry.getInfo().name;
                    if (!networks.getNetworkById(entry.getInfo().networkId).hasNick(nick)) {
                        if (entry.isActive()) {
                            entry.setActive(false);
                        }
                    } else if (networks.getNetworkById(entry.getInfo().networkId).getUserByNick(nick).away) {
                        color = ThemeUtil.Color.bufferStateAway;
                        if (!entry.isActive()) {
                            entry.setActive(true);
                        }
                    } else {
                        color = ThemeUtil.Color.bufferStateActive;
                        if (!entry.isActive()) {
                            entry.setActive(true);
                        }
                    }

                    holder.bufferView.setText(nick);

                    break;
                case GroupBuffer:
                case InvalidBuffer:
                    holder.bufferView.setText("XXXX " + entry.getInfo().name);
            }

            if(entry.isPermanentlyHidden()){
                color = ThemeUtil.Color.bufferStatePerm;
            } else if (entry.isTemporarilyHidden()) {
                color = ThemeUtil.Color.bufferStateTemp;
            }

            holder.stateView.setImageResource(drawable);
            holder.stateView.setColorFilter(color, PorterDuff.Mode.SRC_IN);

            BufferUtils.setBufferViewStatus(getActivity(), entry, holder.bufferView);
            return convertView;
        }

        @Override
        public int getRealChildrenCount(int groupPosition) {
            if (networks != null) {
                return networks.getNetwork(groupPosition).getBuffers().getBufferCount(filters);
            }
            return 0;
        }

        @Override
        public Network getGroup(int groupPosition) {
            return networks.getNetwork(groupPosition);
        }

        @Override
        public int getGroupCount() {
            if (networks == null) {
                return 0;
            } else {
                return networks.size();
            }
        }

        @Override
        public long getGroupId(int groupPosition) {
            return networks.getNetwork(groupPosition).getId();
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            ViewHolderGroup holder = null;
            if (convertView == null || !(convertView.getTag() instanceof ViewHolderGroup)) {
                convertView = inflater.inflate(R.layout.widget_buffer_group, null);
                holder = new ViewHolderGroup();
                holder.statusView = (TextView) convertView.findViewById(R.id.buffer_list_item_name);
                holder.statusView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (getGroup((Integer) v.getTag()).getStatusBuffer() != null) {
                            openBuffer(getGroup((Integer) v.getTag()).getStatusBuffer());
                        } else { //TODO: maybe show the chatActivity but have it be empty, logo or something
                            Toast.makeText(getActivity(), "Not Available", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                holder.indicatorView = (ImageView) convertView.findViewById(R.id.buffer_list_item_indicator);
                holder.statusView.setOnLongClickListener(null); //Apparently need this so long click propagates to parent
                convertView.setTag(holder);
            } else {
                holder = (ViewHolderGroup) convertView.getTag();
            }
            Network entry = getGroup(groupPosition);
            holder.networkId = entry.getId();

            TypedArray ta = getActivity().obtainStyledAttributes(new int[]{R.attr.ic_collapse, R.attr.ic_expand});
            Drawable collapse = ta.getDrawable(0);
            Drawable expand = ta.getDrawable(1);
            ta.recycle();

            if (isExpanded)
                holder.indicatorView.setImageDrawable(collapse);
            else
                holder.indicatorView.setImageDrawable(expand);

            holder.statusView.setText(entry.getName());
            holder.statusView.setTag(groupPosition); //Used in click listener to know what item this is
            BufferUtils.setBufferViewStatus(getActivity(), entry.getStatusBuffer(), holder.statusView);

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }


        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public void clearBuffers() {
            networks = null;
            notifyDataSetChanged();
        }

        public void stopObserving() {
            if (networks == null) return;
            for (Network network : networks.getNetworkList())
                network.deleteObserver(this);
        }

        public void setFilters(Set<Predicate<Buffer>> filters) {
            this.filters = filters;
            update();
        }
    }

    class ActionModeData {
        public int id;
        public int index;
        public View listItem;
        public ActionMode actionMode;
        public ActionMode.Callback actionModeCallbackNetwork;
        public ActionMode.Callback actionModeCallbackBuffer;
    }
}
