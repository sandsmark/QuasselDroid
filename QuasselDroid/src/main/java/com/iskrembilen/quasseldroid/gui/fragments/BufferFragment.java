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

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.TextView;
import android.widget.Toast;

import com.iskrembilen.quasseldroid.util.ThemeUtil;
import com.squareup.otto.Subscribe;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.iskrembilen.quasseldroid.Buffer;
import com.iskrembilen.quasseldroid.BufferInfo;
import com.iskrembilen.quasseldroid.BufferUtils;
import com.iskrembilen.quasseldroid.Network;
import com.iskrembilen.quasseldroid.NetworkCollection;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.BufferListFontSizeChangedEvent;
import com.iskrembilen.quasseldroid.events.BufferOpenedEvent;
import com.iskrembilen.quasseldroid.events.NetworksAvailableEvent;
import com.iskrembilen.quasseldroid.events.QueryUserEvent;
import com.iskrembilen.quasseldroid.events.UserClickedEvent;
import com.iskrembilen.quasseldroid.util.BufferHelper;
import com.iskrembilen.quasseldroid.util.BusProvider;

public class BufferFragment extends Fragment implements OnGroupExpandListener, OnChildClickListener, OnGroupCollapseListener {

    public static final String BUFFER_ID_EXTRA = "bufferid";
    public static final String BUFFER_NAME_EXTRA = "buffername";
    private static final String TAG = BufferFragment.class.getSimpleName();
    private static final String ITEM_POSITION_KEY = "itempos";

    private static final String LIST_POSITION_KEY = "listpos";

    BufferListAdapter bufferListAdapter;
    ExpandableListView bufferList;

    SharedPreferences preferences;
    OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

    private int restoreListPosition = 0;
    private int restoreItemPosition = 0;
    private boolean showHiddenBuffers = false;

    private ActionModeData actionModeData = new ActionModeData();

    private int openedBufferId = -1;

    public static BufferFragment newInstance() {
        return new BufferFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            restoreListPosition = savedInstanceState.getInt(LIST_POSITION_KEY);
            restoreItemPosition = savedInstanceState.getInt(ITEM_POSITION_KEY);
        }
        setHasOptionsMenu(true);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(getResources().getString(R.string.preference_fontsize_channel_list))) {
                    bufferListAdapter.notifyDataSetChanged();
                }
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener); //To avoid GC issues
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_buffers, container, false);
        bufferList = (ExpandableListView) root.findViewById(R.id.buffer_list);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bufferListAdapter = new BufferListAdapter(getActivity());
        bufferList.setAdapter(bufferListAdapter);
        bufferList.setDividerHeight(0);
        bufferList.setOnChildClickListener(this);
        bufferList.setOnGroupCollapseListener(this);
        bufferList.setOnGroupExpandListener(this);

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
                        mode.finish();
                        return true;
                    case R.id.context_menu_disconnect:
                        BufferHelper.disconnectNetwork(actionModeData.id);
                        mode.finish();
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
                return false; // Return false if nothing is done
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.context_menu_join:
                        BufferHelper.joinChannel(actionModeData.id, bufferListAdapter.networks);
                        mode.finish();
                        return true;
                    case R.id.context_menu_part:
                        BufferHelper.partChannel(actionModeData.id, bufferListAdapter.networks);
                        mode.finish();
                        return true;
                    case R.id.context_menu_delete:
                        BufferHelper.showDeleteConfirmDialog(getActivity(), actionModeData.id);
                        mode.finish();
                        return true;
                    case R.id.context_menu_hide_temp:
                        BufferHelper.tempHideChannel(actionModeData.id);
                        mode.finish();
                        return true;
                    case R.id.context_menu_hide_perm:
                        BufferHelper.permHideChannel(actionModeData.id);
                        mode.finish();
                        return true;
                    case R.id.context_menu_unhide:
                        BufferHelper.unhideChannel(actionModeData.id);
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            // Called when the user exits the action mode
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                //				actionModeData.listItem.setActivated(false);
                actionModeData.actionMode = null;
                bufferList.setItemChecked(actionModeData.index, false);
            }
        };

        bufferList.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                ((ExpandableListView) parent).setItemChecked(position, true);

                long packedPosition = bufferList.getExpandableListPosition(position);
                int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
                int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);

                if (ExpandableListView.getPackedPositionType(packedPosition) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    Buffer buffer = bufferListAdapter.getChild(groupPosition, childPosition);
                    actionModeData.index = bufferList.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition));

                    actionModeData.actionMode = getActivity().startActionMode(actionModeData.actionModeCallbackBuffer);
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
                    actionModeData.actionMode = getActivity().startActionMode(actionModeData.actionModeCallbackNetwork);
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
        preferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
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


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_buffer, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (showHiddenBuffers) {
            menu.findItem(R.id.context_menu_toggle_hidden).setTitle(R.string.context_hide_hidden);
        } else {
            menu.findItem(R.id.context_menu_toggle_hidden).setTitle(R.string.context_show_hidden);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_join_channel:
                if (bufferListAdapter.networks == null)
                    Toast.makeText(getActivity(), getString(R.string.not_available), Toast.LENGTH_SHORT).show();
                else showJoinChannelDialog();
                return true;
            case R.id.context_menu_toggle_hidden:
                showHiddenBuffers = !showHiddenBuffers;
                bufferListAdapter.notifyDataSetChanged();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        openBuffer(bufferListAdapter.getChild(groupPosition, childPosition));
        return true;
    }

    @Override
    public void onGroupExpand(int groupPosition) {
        bufferListAdapter.getGroup(groupPosition).setOpen(true);
    }

    @Override
    public void onGroupCollapse(int groupPosition) {
        bufferListAdapter.getGroup(groupPosition).setOpen(false);
    }

    private void openBuffer(Buffer buffer) {
        this.openedBufferId = buffer.getInfo().id;
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
        public View parent;
    }

    public static class ViewHolderGroup {
        public TextView statusView;
        public int networkId;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public class BufferListAdapter extends BaseExpandableListAdapter implements Observer {
        private NetworkCollection networks;
        private LayoutInflater inflater;

        public BufferListAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        public void setNetworks(NetworkCollection networks) {
            this.networks = networks;
            if (networks == null)
                return;
            networks.addObserver(this);
            notifyDataSetChanged();
            if (bufferListAdapter != null) {
                for (int group = 0; group < getGroupCount(); group++) {
                    if (getGroup(group).isOpen()) bufferList.expandGroup(group);
                    else bufferList.collapseGroup(group);
                }
                bufferList.setSelectionFromTop(restoreListPosition, restoreItemPosition);
            }
        }

        @Override
        public void update(Observable observable, Object data) {
            notifyDataSetChanged();
            for (int group = 0; group < getGroupCount(); group++) {
                if (getGroup(group).isOpen()) bufferList.expandGroup(group);
                else bufferList.collapseGroup(group);
            }
        }

        @Override
        public Buffer getChild(int groupPosition, int childPosition) {
            if(showHiddenBuffers) {
                return networks.getNetwork(groupPosition).getBuffers().getUnfilteredPos(childPosition);
            } else {
                return networks.getNetwork(groupPosition).getBuffers().getPos(childPosition);
            }
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            if(showHiddenBuffers) {
                return networks.getNetwork(groupPosition).getBuffers().getUnfilteredPos(childPosition).getInfo().id;
            } else {
                return networks.getNetwork(groupPosition).getBuffers().getPos(childPosition).getInfo().id;
            }
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ViewHolderChild holder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.widget_buffer_single, null);
                holder = new ViewHolderChild();
                holder.parent = convertView;
                holder.bufferView = (TextView) convertView.findViewById(R.id.buffer_list_item_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolderChild) convertView.getTag();
            }
            Buffer entry = getChild(groupPosition, childPosition);
            switch (entry.getInfo().type) {
                case StatusBuffer:
                case ChannelBuffer:
                    holder.bufferView.setText(entry.getInfo().name);
                    if (entry.isActive()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                            holder.parent.setBackground(ThemeUtil.drawable_buffer_active);
                        else
                            holder.parent.setBackgroundDrawable(ThemeUtil.drawable_buffer_active);
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                            holder.parent.setBackground(ThemeUtil.drawable_buffer_gone);
                        else
                            holder.parent.setBackgroundDrawable(ThemeUtil.drawable_buffer_gone);
                    }
                    break;
                case QueryBuffer:
                    String nick = entry.getInfo().name;
                    if (!bufferListAdapter.networks.getNetworkById(entry.getInfo().networkId).hasNick(nick)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                            holder.parent.setBackground(ThemeUtil.drawable_buffer_gone);
                        else
                            holder.parent.setBackgroundDrawable(ThemeUtil.drawable_buffer_gone);
                        if (entry.isActive()) {
                            entry.setActive(false);
                        }
                    } else if (bufferListAdapter.networks.getNetworkById(entry.getInfo().networkId).getUserByNick(nick).away) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                            holder.parent.setBackground(ThemeUtil.drawable_buffer_away);
                        else
                            holder.parent.setBackgroundDrawable(ThemeUtil.drawable_buffer_away);
                        if (!entry.isActive()) {
                            entry.setActive(true);
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                            holder.parent.setBackground(ThemeUtil.drawable_buffer_active);
                        else
                            holder.parent.setBackgroundDrawable(ThemeUtil.drawable_buffer_active);
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    convertView.setBackground(ThemeUtil.drawable_buffer_hidden_perm);
                else
                    convertView.setBackgroundDrawable(ThemeUtil.drawable_buffer_hidden_perm);
            } else if (entry.isTemporarilyHidden()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    convertView.setBackground(ThemeUtil.drawable_buffer_hidden_temp);
                else
                    convertView.setBackgroundDrawable(ThemeUtil.drawable_buffer_hidden_temp);
            }

            BufferUtils.setBufferViewStatus(getActivity(), entry, holder.bufferView);
            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            if (networks != null) {
                if (showHiddenBuffers)
                    return networks.getNetwork(groupPosition).getBuffers().getUnfilteredBufferCount();
                else
                    return networks.getNetwork(groupPosition).getBuffers().getBufferCount();
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
            if (convertView == null) {
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
                holder.statusView.setOnLongClickListener(null); //Apparently need this so long click propagates to parent
                convertView.setTag(holder);
            } else {
                holder = (ViewHolderGroup) convertView.getTag();
            }
            Network entry = getGroup(groupPosition);
            holder.networkId = entry.getId();
            holder.statusView.setText(entry.getName());
            holder.statusView.setTag(groupPosition); //Used in click listener to know what item this is
            BufferUtils.setBufferViewStatus(getActivity(), entry.getStatusBuffer(), holder.statusView);

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return true;
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
