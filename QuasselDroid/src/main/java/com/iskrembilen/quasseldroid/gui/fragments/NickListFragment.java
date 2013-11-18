package com.iskrembilen.quasseldroid.gui.fragments;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.iskrembilen.quasseldroid.Buffer;
import com.iskrembilen.quasseldroid.IrcMode;
import com.iskrembilen.quasseldroid.IrcUser;
import com.iskrembilen.quasseldroid.NetworkCollection;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.UserCollection;
import com.iskrembilen.quasseldroid.events.BufferOpenedEvent;
import com.iskrembilen.quasseldroid.events.NetworksAvailableEvent;
import com.iskrembilen.quasseldroid.events.UserClickedEvent;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.squareup.otto.Subscribe;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class NickListFragment extends SherlockFragment {
    private NicksAdapter adapter;
    private ExpandableListView list;
    private int bufferId = -1;
    private NetworkCollection networks;
    private static final int[] EXPANDED_STATE = {android.R.attr.state_expanded};
    private static final int[] NOT_EXPANDED_STATE = {android.R.attr.state_empty};
    private final String TAG = NickListFragment.class.getSimpleName();

    public static NickListFragment newInstance() {
        return new NickListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new NicksAdapter();

        if (savedInstanceState != null) {
            bufferId = savedInstanceState.getInt("bufferid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.nick_list_fragment_layout, container, false);
        list = (ExpandableListView) root.findViewById(R.id.userList);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        list.setAdapter(adapter);

    }

    @Override
    public void onStart() {
        super.onStart();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopObserving();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("bufferid", bufferId);
        super.onSaveInstanceState(outState);
    }

    private void queryUser(String nick) {
        BusProvider.getInstance().post(new UserClickedEvent(bufferId, nick));
    }

    public class NicksAdapter extends BaseExpandableListAdapter implements Observer {

        private LayoutInflater inflater;
        private UserCollection users;


        public NicksAdapter() {
            inflater = getActivity().getLayoutInflater();
            this.users = null;
        }

        public void setUsers(UserCollection users) {
            users.addObserver(this);
            this.users = users;
            notifyDataSetChanged();
            for (int i = 0; i < getGroupCount(); i++) {
                list.expandGroup(i);
            }
        }

        @Override
        public void update(Observable observable, Object data) {
            if (data == null) {
                return;
            }
            switch ((Integer) data) {
                case R.id.BUFFERUPDATE_USERSCHANGED:
                    notifyDataSetChanged();
                    break;
            }
        }

        public void stopObserving() {
            if (users != null)
                users.deleteObserver(this);

        }

        @Override
        public IrcUser getChild(int groupPosition, int childPosition) {
            return getGroup(groupPosition).second.get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            //TODO: This will cause bugs when you have more than 99 children in a group
            return groupPosition * 100 + childPosition;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {

            ViewHolderChild holder = null;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.nicklist_item, null);
                holder = new ViewHolderChild();
                holder.nickView = (TextView) convertView.findViewById(R.id.nicklist_nick_view);
                holder.userImage = (ImageView) convertView.findViewById(R.id.nicklist_nick_image);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolderChild) convertView.getTag();
            }
            final IrcUser entry = getChild(groupPosition, childPosition);
            holder.nickView.setText(entry.nick);
            if (entry.away) {
                holder.userImage.setImageResource(R.drawable.im_user_away);
            } else {
                holder.userImage.setImageResource(R.drawable.im_user);
            }

            holder.nickView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    queryUser(entry.nick);
                }
            });

            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            if (this.users == null) return 0;
            return getGroup(groupPosition).second.size();
        }

        @Override
        public Pair<IrcMode, List<IrcUser>> getGroup(int groupPosition) {
            int counter = 0;
            for (IrcMode mode : IrcMode.values()) {
                if (counter == groupPosition) {
                    return new Pair<IrcMode, List<IrcUser>>(mode, users.getUniqueUsersWithMode(mode));
                } else {
                    counter++;
                }
            }
            return null;
        }

        @Override
        public int getGroupCount() {
            if (users == null) return 0;
            return IrcMode.values().length;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            ViewHolderGroup holder = null;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.nicklist_group_item, null);
                holder = new ViewHolderGroup();
                holder.nameView = (TextView) convertView.findViewById(R.id.nicklist_group_name_view);
                holder.imageView = (ImageView) convertView.findViewById(R.id.nicklist_group_image_view);
                holder.expanderView = (ImageView) convertView.findViewById(R.id.nicklist_expander_image_view);
                holder.groupHolderView = (LinearLayout) convertView.findViewById(R.id.nicklist_holder_view);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolderGroup) convertView.getTag();
            }
            Pair<IrcMode, List<IrcUser>> group = getGroup(groupPosition);
            if (group.second.size() < 1) {
                holder.nameView.setVisibility(View.GONE);
                holder.imageView.setVisibility(View.GONE);
                holder.expanderView.setVisibility(View.GONE);
                holder.groupHolderView.setPadding(0, 0, 0, 0);


            } else {
                if (group.second.size() > 1) {
                    holder.nameView.setText(group.second.size() + " " + group.first.modeName + group.first.pluralization);
                } else {
                    holder.nameView.setText(group.second.size() + " " + group.first.modeName);
                }
                holder.nameView.setVisibility(View.VISIBLE);
                holder.imageView.setVisibility(View.VISIBLE);
                holder.expanderView.setVisibility(View.VISIBLE);
                holder.imageView.setImageResource(group.first.iconResource);
                holder.groupHolderView.setPadding(5, 2, 2, 2);
                holder.expanderView.getDrawable().setState(list.isGroupExpanded(groupPosition) ? EXPANDED_STATE : NOT_EXPANDED_STATE);
            }
            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }


    public static class ViewHolderChild {
        public TextView nickView;
        public ImageView userImage;
    }

    public static class ViewHolderGroup {
        public TextView nameView;
        public ImageView imageView;
        public ImageView expanderView;
        public LinearLayout groupHolderView;
    }

    @Subscribe
    public void onNetworksAvailable(NetworksAvailableEvent event) {
        if (event.networks != null) {
            this.networks = event.networks;
            if (bufferId != -1) {
                updateUsers();
            }
        }
    }

    @Subscribe
    public void onBufferOpened(BufferOpenedEvent event) {
        if (event.bufferId != -1) {
            this.bufferId = event.bufferId;
            if (networks != null) {
                updateUsers();
            }
        }
    }

    private void updateUsers() {
        Buffer buffer = networks.getBufferById(bufferId);
        if (buffer != null) {
            adapter.setUsers(buffer.getUsers());
        }
    }
}
