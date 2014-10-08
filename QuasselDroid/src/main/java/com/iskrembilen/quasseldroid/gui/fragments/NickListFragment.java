package com.iskrembilen.quasseldroid.gui.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iskrembilen.quasseldroid.IrcMessage;
import com.iskrembilen.quasseldroid.util.NetsplitHelper;
import com.iskrembilen.quasseldroid.util.NickCompletionHelper;
import com.iskrembilen.quasseldroid.util.SenderColorHelper;
import com.iskrembilen.quasseldroid.util.ThemeUtil;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

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

public class NickListFragment extends Fragment {
    private static final int[] EXPANDED_STATE = {android.R.attr.state_expanded};
    private static final int[] NOT_EXPANDED_STATE = {android.R.attr.state_empty};
    private final String TAG = NickListFragment.class.getSimpleName();
    private NicksAdapter adapter;
    private ExpandableListView list;
    private int bufferId = -1;
    private NetworkCollection networks;

    private TextView name;
    private Button topic;

    private String[] data = new String[2];

    private BacklogObserver observer = new BacklogObserver();

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
        name = (TextView) root.findViewById(R.id.channel_name);
        topic = (Button) root.findViewById(R.id.channel_topic);
        topic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TopicDialog.newInstance(data[0],data[1]).show(getFragmentManager(),TAG);
            }
        });
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

    @Subscribe
    public void onNetworksAvailable(NetworksAvailableEvent event) {
        if (event.networks != null) {
            this.networks = event.networks;
            if (bufferId != -1) {
                observer.setBuffer(networks.getBufferById(bufferId));
                updateUsers();
                updateDetails();
            }
        }
    }

    @Subscribe
    public void onBufferOpened(BufferOpenedEvent event) {
        if (event.bufferId != -1) {
            this.bufferId = event.bufferId;
            if (networks != null) {
                observer.setBuffer(networks.getBufferById(bufferId));
                updateUsers();
                updateDetails();
            }
        }
    }

    private void updateDetails() {
        Buffer buffer = networks.getBufferById(bufferId);
        if (buffer != null) {
            name.setText(buffer.getInfo().name);
            topic.setText(buffer.getTopic());
            observer.setBuffer(buffer);
            data = new String[] {buffer.getInfo().name,buffer.getTopic()};
        }
    }

    private void updateUsers() {
        Buffer buffer = networks.getBufferById(bufferId);
        if (buffer != null) {
            adapter.setUsers(buffer.getUsers());
        }
    }

    public static class ViewHolderChild {
        public TextView nickView;
        public ImageView userImage;
    }

    public static class ViewHolderGroup {
        public TextView nameView;
        public ImageView expanderView;
        public LinearLayout groupHolderView;
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
            return getCombinedChildId(groupPosition, childPosition);
            //return groupPosition * 100 + childPosition;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {

            ViewHolderChild holder = null;

            int availablecolor = getResources().getColor(R.color.buffer_read_color);
            int awaycolor = getResources().getColor(R.color.buffer_parted_color);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.nicklist_item, null);
                holder = new ViewHolderChild();
                holder.nickView = (TextView) convertView.findViewById(R.id.nicklist_nick_view);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolderChild) convertView.getTag();
            }
            final IrcUser entry = getChild(groupPosition, childPosition);
            IrcMode mode = getGroup(groupPosition).first;

            Spannable spannable = new SpannableString(((mode.icon.trim().equalsIgnoreCase("")) ? "" :mode.icon + " ") + entry.nick);
            spannable.setSpan(new ForegroundColorSpan(getResources().getColor(mode.colorResource)), 0, mode.icon.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            holder.nickView.setText(spannable);

            if (entry.away) {
                holder.nickView.setTextColor(awaycolor);
            } else {
                holder.nickView.setTextColor(availablecolor);
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
            return inflater.inflate(R.layout.nicklist_group_item, null);
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

    public class BacklogObserver implements Observer {
        private Buffer buffer;

        public void setBuffer(Buffer buffer) {
            if (this.buffer!=null) this.buffer.deleteObserver(this);
            this.buffer = buffer;
            if (this.buffer!=null) this.buffer.addObserver(this);
        }

        @Override
        public void update(Observable observable, Object data) {
            if (data == null) {
                return;
            }
            switch ((Integer) data) {
                case R.id.BUFFERUPDATE_TOPICCHANGED:
                    updateDetails();
                    break;
                default:
            }
        }
    }
}
