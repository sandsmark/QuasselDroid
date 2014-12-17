package com.iskrembilen.quasseldroid.gui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.iskrembilen.quasseldroid.gui.dialogs.HideEventsDialog;
import com.squareup.otto.Subscribe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import com.iskrembilen.quasseldroid.Buffer;
import com.iskrembilen.quasseldroid.IrcMessage;
import com.iskrembilen.quasseldroid.IrcMessage.Type;
import com.iskrembilen.quasseldroid.NetworkCollection;
import com.iskrembilen.quasseldroid.Quasseldroid;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.events.BufferOpenedEvent;
import com.iskrembilen.quasseldroid.events.CompleteNickEvent;
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent;
import com.iskrembilen.quasseldroid.events.GetBacklogEvent;
import com.iskrembilen.quasseldroid.events.ManageChannelEvent;
import com.iskrembilen.quasseldroid.events.ManageChannelEvent.ChannelAction;
import com.iskrembilen.quasseldroid.events.ManageMessageEvent;
import com.iskrembilen.quasseldroid.events.ManageMessageEvent.MessageAction;
import com.iskrembilen.quasseldroid.events.NetworksAvailableEvent;
import com.iskrembilen.quasseldroid.events.SendMessageEvent;
import com.iskrembilen.quasseldroid.events.UpdateReadBufferEvent;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.iskrembilen.quasseldroid.util.InputHistoryHelper;
import com.iskrembilen.quasseldroid.util.NetsplitHelper;
import com.iskrembilen.quasseldroid.util.NickCompletionHelper;
import com.iskrembilen.quasseldroid.util.SenderColorHelper;
import com.iskrembilen.quasseldroid.util.ThemeUtil;

public class ChatFragment extends Fragment implements Serializable {

    public static final String BUFFER_ID = "bufferid";
    private static final String TAG = ChatFragment.class.getSimpleName();
    private static final String BUFFER_NAME = "buffername";
    SharedPreferences preferences;
    private BacklogAdapter adapter;
    private ListView backlogList;
    private EditText inputField;
    private ImageButton autoCompleteButton;
    private int dynamicBacklogAmount;
    private NickCompletionHelper nickCompletionHelper;
    private int bufferId = -1;
    private boolean connected;
    private NetworkCollection networks;
    private String timeFormat;

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Creating fragment");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        adapter = new BacklogAdapter(getActivity(), null);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (savedInstanceState != null && savedInstanceState.containsKey(BUFFER_ID)) {
            bufferId = savedInstanceState.getInt(BUFFER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat, container, false);
        backlogList = (ListView) root.findViewById(R.id.chat_backlog_list_view);
        inputField = (EditText) root.findViewById(R.id.chat_input_view);
        autoCompleteButton = (ImageButton) root.findViewById(R.id.chat_auto_complete_button);

        String timeType = preferences.getString(getResources().getString(R.string.preference_timestamp_seconds),"orientation");
        if (timeType.equalsIgnoreCase("always")) {
            timeFormat = "%02d:%02d:%02d";
        } else if (timeType.equalsIgnoreCase("never")) {
            timeFormat = "%02d:%02d";
        } else if (timeType.equalsIgnoreCase("orientation")) {
            if (getActivity().getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE) {
                timeFormat = "%02d:%02d:%02d";
            } else {
                timeFormat = "%02d:%02d";
            }
        } else {
            timeFormat = "%02d:%02d";
        }
        Log.d(TAG,"Setting time format to include seconds: "+ timeType + ", resulting format: "+ timeFormat);

        backlogList.setAdapter(adapter);
        backlogList.setOnScrollListener(new BacklogScrollListener(5));
        backlogList.setSelection(backlogList.getChildCount());
        backlogList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            private int lastItem;
            private long lastTime;

            private static final int DOUBLE_CLICK_TIMESPAN = 200;

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final long currentTime = System.currentTimeMillis();
                if (lastItem==i) {
                    if ((currentTime-lastTime)<DOUBLE_CLICK_TIMESPAN) {
                        IrcMessage msg = adapter.getItem(i);
                        inputField.getText().append(" "+msg.getNick());
                        nickCompletionHelper.completeNick(inputField);
                        lastTime = 0;
                    } else {
                        lastTime = currentTime;
                    }
                } else {
                    lastItem = i;
                    lastTime = currentTime;
                }
            }
        });

        autoCompleteButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                nickCompletionHelper.completeNick(inputField);
            }
        });

        inputField.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getAction() == KeyEvent.ACTION_DOWN &&
                        ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) || (event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER)))) {
                    String inputText = inputField.getText().toString();

                    if (!"" .equals(inputText.trim())) {
                        BusProvider.getInstance().post(new SendMessageEvent(adapter.buffer.getInfo().id, inputText));
                        InputHistoryHelper.addHistoryEntry(inputText);
                        inputField.setText("");
                        InputHistoryHelper.tempStoreCurrentEntry("");
                    }

                    return true;
                }
                return false;
            }
        });

        inputField.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_TAB && event.getAction() == KeyEvent.ACTION_DOWN) {
                    onNickComplete();
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
                    EditText text = (EditText) v;
                    InputHistoryHelper.tempStoreCurrentEntry(text.getText().toString());
                    text.setText(InputHistoryHelper.getNextHistoryEntry());
                    text.setSelection(text.getText().length());
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
                    EditText text = (EditText) v;
                    if (InputHistoryHelper.isViewingHistory()) {
                        // Currently viewing history, so progress back down towards "entry zero"
                        text.setText(InputHistoryHelper.getPreviousHistoryEntry());
                    } else if (!text.getText().toString().equals("")) {
                        // Not viewing history, so push the current input text into the history and clear the input
                        InputHistoryHelper.addHistoryEntry(text.getText().toString());
                        text.setText("");
                    }
                    text.setSelection(text.getText().length());
                    return true;
                }
                return false;
            }
        });

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_hide_events:
                if (adapter.buffer == null)
                    Toast.makeText(getActivity(), getString(R.string.not_available), Toast.LENGTH_SHORT).show();
                else showHideEventsDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "Starting fragment");
        super.onStart();
        dynamicBacklogAmount = Integer.parseInt(preferences.getString(getString(R.string.preference_dynamic_backlog), "10"));
        autoCompleteButton.setEnabled(false);
        inputField.setEnabled(false);
        BusProvider.getInstance().register(this);
        setUserVisibleHint(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "pausing fragment");
    }

    @Override
    public void onStop() {
        Log.d(TAG, "Stopping fragment");
        super.onStop();
        if (Quasseldroid.status == ConnectionChangedEvent.Status.Connected && getUserVisibleHint()) updateRead();
        adapter.clearBuffer();
        BusProvider.getInstance().unregister(this);
        setUserVisibleHint(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(BUFFER_ID, bufferId);
        super.onSaveInstanceState(outState);
    }

    private void showHideEventsDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = HideEventsDialog.newInstance(adapter.buffer);
        newFragment.show(ft, "dialog");
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "ChatFragment visible hint: " + isVisibleToUser);
    }

    private void updateRead() {
        Log.d(TAG, "Updating buffer read, chat is visible: " + getUserVisibleHint());
        if (adapter.buffer != null) {
            adapter.buffer.setDisplayed(false);

            //Don't save position if list is at bottom
            if (backlogList.getLastVisiblePosition() == adapter.getCount() - 1) {
                adapter.buffer.setTopMessageShown(0);
            } else {
                adapter.buffer.setTopMessageShown(adapter.getListTopMessageId());
            }
            if (adapter.buffer.getUnfilteredSize() != 0) {
                BusProvider.getInstance().post(new ManageChannelEvent(adapter.getBufferId(), ChannelAction.MARK_AS_READ));
                BusProvider.getInstance().post(new ManageMessageEvent(adapter.getBufferId(), adapter.buffer.getUnfilteredBacklogEntry(adapter.buffer.getUnfilteredSize() - 1).messageId, MessageAction.LAST_SEEN));
            }

        }
    }

    private void updateMarkerLine() {
        BusProvider.getInstance().post(new ManageMessageEvent(adapter.getBufferId(), adapter.buffer.getLastSeenMessage(), MessageAction.MARKER_LINE));
    }

    public void setBuffer(int bufferId) {
        Log.d(TAG, "Setting buffer and chat is visible: " + getUserVisibleHint());
        this.bufferId = bufferId;
        if (adapter != null && networks != null) {
            if (adapter.buffer != null && bufferId != adapter.buffer.getInfo().id) {
                updateMarkerLine();
            }
            adapter.clearBuffer();
            Buffer buffer = networks.getBufferById(bufferId);
            if (buffer != null) {
                adapter.setBuffer(buffer, networks);
                nickCompletionHelper = new NickCompletionHelper(buffer.getUsers().getUniqueUsers());
                autoCompleteButton.setEnabled(true);
                inputField.setEnabled(true);
                buffer.setDisplayed(true);
                BusProvider.getInstance().post(new ManageChannelEvent(buffer.getInfo().id, ChannelAction.HIGHLIGHTS_READ));

                //Move list to correct position
                if (adapter.buffer.getTopMessageShown() == 0) {
                    backlogList.setSelection(adapter.getCount() - 1);
                } else {
                    adapter.setListTopMessage(adapter.buffer.getTopMessageShown());
                }
            } else {
                resetFragment();
            }
        }
    }

    private void onNickComplete() {
        if (nickCompletionHelper != null) {
            nickCompletionHelper.completeNick(inputField);
        }
    }

    @Subscribe
    public void onNetworksAvailable(NetworksAvailableEvent event) {
        Log.d(TAG, "onNetworksAvailable event");
        if (event.networks != null) {
            this.networks = event.networks;
            if (bufferId != -1) {
                setBuffer(bufferId);
            }
        }
        Log.d(TAG, "onNetworksAvailable done");
    }

    @Subscribe
    public void onBufferOpened(BufferOpenedEvent event) {
        Log.d(TAG, "onBufferOpened event");
        this.bufferId = event.bufferId;
        if (event.bufferId != -1) {
            setBuffer(bufferId);
        } else {
            resetFragment();
        }
        Log.d(TAG, "onBufferOpened done");
    }

    @Subscribe
    public void onUpdateBufferRead(UpdateReadBufferEvent event) {
        Log.d(TAG, "onUpdateBufferRead event");
        updateRead();
    }

    @Subscribe
    public void onCompleteNick(CompleteNickEvent event) {
        onNickComplete();
    }

    private void resetFragment() {
        adapter.clearBuffer();
        autoCompleteButton.setEnabled(false);
        inputField.setText("");
        inputField.setEnabled(false);
        nickCompletionHelper = null;
    }

    public static class ViewHolder {
        public TextView timeView;
        public TextView msgView;
        public TextView separatorView;
        public LinearLayout item_layout;
        public View parent;

        public int messageID;
    }

    public class BacklogAdapter extends BaseAdapter implements Observer {

        private LayoutInflater inflater;
        private Buffer buffer;


        public BacklogAdapter(Context context, ArrayList<IrcMessage> backlog) {
            inflater = LayoutInflater.from(context);

        }

        public void setBuffer(Buffer buffer, NetworkCollection networks) {
            this.buffer = buffer;
            buffer.addObserver(this);
            notifyDataSetChanged();
            backlogList.scrollTo(backlogList.getScrollX(), backlogList.getScrollY());
        }

        @Override
        public int getCount() {
            if (this.buffer == null) return 0;
            return buffer.getSize();
        }

        @Override
        public IrcMessage getItem(int position) {
            //TODO: PriorityQueue is fucked, we don't want to convert to array here, so change later
            return buffer.getBacklogEntry(position);
        }

        @Override
        public long getItemId(int position) {
            return buffer.getBacklogEntry(position).messageId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.widget_backlog, null);
                holder = new ViewHolder();
                holder.parent = convertView;
                holder.timeView = (TextView) convertView.findViewById(R.id.backlog_time_view);
                holder.timeView.setTextColor(ThemeUtil.color.chatTimestamp);
                holder.msgView = (TextView) convertView.findViewById(R.id.backlog_msg_view);
                holder.separatorView = (TextView) convertView.findViewById(R.id.backlog_list_separator);
                holder.item_layout = (LinearLayout) convertView.findViewById(R.id.backlog_item_linearlayout);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            //Set separator line here
            if (position != (getCount() - 1) && (buffer.getMarkerLineMessage() == getItem(position).messageId || (buffer.isMarkerLineFiltered() && getItem(position).messageId < buffer.getMarkerLineMessage() && getItem(position + 1).messageId > buffer.getMarkerLineMessage()))) {
                holder.separatorView.getLayoutParams().height = Math.round(getResources().getDimension(R.dimen.markerline_height));
            } else {
                holder.separatorView.getLayoutParams().height = 0;
            }

            int fontsize = Integer.valueOf(preferences.getString(getString(R.string.preference_fontsize_channel_list), "14"));
            holder.msgView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontsize);
            holder.timeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontsize);

            IrcMessage entry = this.getItem(position);
            holder.messageID = entry.messageId;
            holder.timeView.setText(entry.getTime(timeFormat));

            if (!preferences.getBoolean(getString(R.string.preference_colored_text), false)) {
                entry.content = new SpannableString(entry.content.toString());
            }

            Spannable spannable;
            String rawText;
            String nick;
            boolean detailedActions = preferences.getBoolean(getString(R.string.preference_detailed_actions),false);

            switch (entry.type) {
                case Action:
                    holder.msgView.setTextColor(ThemeUtil.color.chatAction);
                    holder.msgView.setTypeface(Typeface.DEFAULT);
                    holder.parent.setBackgroundColor(ThemeUtil.color.chatActionBg);


                    int color;
                    if (entry.isSelf()) {
                        color = ThemeUtil.color.chatPlain;
                    } else {
                        color = entry.getSenderColor();
                    }

                    Spannable nickSpan = new SpannableString(entry.getNick());
                    entry.content.setSpan(new StyleSpan(Typeface.ITALIC), 0, entry.content.length(), 0);
                    nickSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, entry.getNick().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    nickSpan.setSpan(new StyleSpan(Typeface.ITALIC), 0, entry.getNick().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    nickSpan.setSpan(new ForegroundColorSpan(color), 0, entry.getNick().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    holder.msgView.setText(TextUtils.concat(nickSpan, " ", entry.content));
                    break;
                case Error:
                    holder.msgView.setText(entry.content);
                    holder.msgView.setTextColor(ThemeUtil.color.chatError);
                    holder.parent.setBackgroundColor(ThemeUtil.color.chatActionBg);
                    break;
                case Server:
                case Info:
                    holder.msgView.setText(entry.content);
                    holder.msgView.setTextColor(ThemeUtil.color.chatAction);
                    holder.parent.setBackgroundColor(ThemeUtil.color.chatActionBg);
                    break;
                case Topic:
                    holder.msgView.setText(entry.content);
                    holder.msgView.setTextColor(ThemeUtil.color.chatAction);
                    holder.parent.setBackgroundColor(ThemeUtil.color.chatActionBg);
                    break;
                case Notice:
                    holder.msgView.setText(entry.content);
                    holder.msgView.setTextColor(ThemeUtil.color.chatAction);
                    holder.parent.setBackgroundColor(ThemeUtil.color.chatActionBg);
                    break;
                case Join:
                    nick = entry.getNick();
                    if (detailedActions) {nick += " ("+entry.getHostmask()+")";}
                    spannable = new SpannableString(String.format("%s joined", nick));
                    spannable.setSpan(new ForegroundColorSpan(entry.getSenderColor()), 0, entry.getNick().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    holder.msgView.setText(spannable);
                    holder.msgView.setTextColor(ThemeUtil.color.chatAction);
                    holder.parent.setBackgroundColor(ThemeUtil.color.chatActionBg);
                    nickCompletionHelper = new NickCompletionHelper(buffer.getUsers().getUniqueUsers());
                    break;
                case Part:
                    nick = entry.getNick();
                    if (detailedActions) {nick += " ("+entry.getHostmask()+")";}
                    spannable = new SpannableString(String.format("%s left: ", nick));
                    spannable.setSpan(new ForegroundColorSpan(entry.getSenderColor()), 0, entry.getNick().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    holder.msgView.setText(TextUtils.concat(spannable, entry.content));
                    holder.msgView.setTextColor(ThemeUtil.color.chatAction);
                    holder.parent.setBackgroundColor(ThemeUtil.color.chatActionBg);
                    nickCompletionHelper = new NickCompletionHelper(buffer.getUsers().getUniqueUsers());
                    break;
                case Quit:
                    nick = entry.getNick();
                    if (detailedActions) {nick += " ("+entry.getHostmask()+")";}
                    spannable = new SpannableString(String.format("%s quit: ", nick));
                    spannable.setSpan(new ForegroundColorSpan(entry.getSenderColor()), 0, entry.getNick().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    holder.msgView.setText(TextUtils.concat(spannable, entry.content));
                    holder.msgView.setTextColor(ThemeUtil.color.chatAction);
                    holder.parent.setBackgroundColor(ThemeUtil.color.chatActionBg);
                    nickCompletionHelper = new NickCompletionHelper(buffer.getUsers().getUniqueUsers());
                    break;
                case Kill:
                    nick = entry.getNick();
                    if (detailedActions) {nick += " ("+entry.getHostmask()+")";}
                    spannable = new SpannableString(String.format("%s was killed: ", nick));
                    spannable.setSpan(new ForegroundColorSpan(entry.getSenderColor()), 0, nick.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    holder.msgView.setText(TextUtils.concat(spannable, entry.content));
                    holder.msgView.setTextColor(ThemeUtil.color.chatAction);
                    holder.parent.setBackgroundColor(ThemeUtil.color.chatActionBg);
                    nickCompletionHelper = new NickCompletionHelper(buffer.getUsers().getUniqueUsers());
                    break;
                case Kick:
                    nick = "";
                    String reason = "";

                    int nickEnd = entry.content.toString().indexOf(" ");
                    if (nickEnd >= 0) {
                        nick = entry.content.toString().substring(0, nickEnd);
                        reason = " (" + entry.content.toString().substring(nickEnd + 1) + ")";
                    } else {
                        nick = entry.content.toString();
                    }

                    if (detailedActions) {nick += " ("+entry.getHostmask()+")";}

                    rawText = String.format("%s has kicked %s from %s: %s", entry.getNick(), nick, entry.bufferInfo.name, reason);

                    int color_nick;
                    if (nick.equalsIgnoreCase(entry.getSender())) {
                        color_nick = entry.getSenderColor();
                    } else {
                        color_nick = SenderColorHelper.getSenderColor(nick);
                    }

                    spannable = new SpannableString(rawText);
                    spannable.setSpan(new ForegroundColorSpan(entry.getSenderColor()), 0, entry.getNick().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    spannable.setSpan(new ForegroundColorSpan(color_nick), rawText.indexOf(nick), rawText.indexOf(nick) + nick.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    holder.msgView.setText(spannable);
                    holder.msgView.setTextColor(ThemeUtil.color.chatAction);
                    holder.parent.setBackgroundColor(ThemeUtil.color.chatActionBg);
                    nickCompletionHelper = new NickCompletionHelper(buffer.getUsers().getUniqueUsers());
                    break;
                case Mode:
                    int color_affected_nick;
                    String affected_nick = null;

                    rawText = String.format(String.format("Mode %s by %s", entry.content.toString(), entry.getNick()));
                    spannable = new SpannableString(rawText);

                    if (entry.content.toString().trim().length()-entry.content.toString().trim().replaceAll(" ","").length()==2 && entry.content.toString().startsWith("#")) {
                        affected_nick = entry.content.toString().substring(entry.content.toString().lastIndexOf(" "), entry.content.toString().trim().length()).trim();
                    } else if (!entry.content.toString().startsWith("#")&&entry.content.toString().trim().length()-entry.content.toString().trim().replaceAll(" ","").length()==1){
                        affected_nick = entry.content.toString().substring(0, entry.content.toString().lastIndexOf(" ")).trim();
                    }

                    if (affected_nick!=null) {
                        if (affected_nick.equalsIgnoreCase(entry.getSender())) {
                            color_affected_nick = entry.getSenderColor();
                        } else {
                            color_affected_nick = SenderColorHelper.getSenderColor(affected_nick);
                        }
                        spannable.setSpan(new ForegroundColorSpan(color_affected_nick), rawText.indexOf(affected_nick), rawText.indexOf(affected_nick) + affected_nick.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        spannable.setSpan(new ForegroundColorSpan(entry.getSenderColor()), rawText.indexOf(affected_nick) + affected_nick.length() + rawText.substring(rawText.indexOf(affected_nick) + affected_nick.length()).indexOf(entry.getNick()), rawText.indexOf(affected_nick) + affected_nick.length() + rawText.substring(rawText.indexOf(affected_nick) + affected_nick.length()).indexOf(entry.getNick()) + entry.getNick().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }

                    holder.msgView.setText(spannable);
                    holder.msgView.setTextColor(ThemeUtil.color.chatAction);
                    holder.parent.setBackgroundColor(ThemeUtil.color.chatActionBg);
                    break;
                case Nick:
                    if (entry.getNick().equals(entry.content.toString())) {
                        rawText = String.format("You changed your nick to %s", entry.content.toString());
                        spannable = new SpannableString(rawText);
                        holder.msgView.setText(spannable);
                    } else {
                        rawText = String.format("%s is now known as %s", entry.getNick(), entry.content.toString());
                        spannable = new SpannableString(rawText);
                        spannable.setSpan(new ForegroundColorSpan(entry.getSenderColor()), 0, entry.getNick().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                        int color_new = SenderColorHelper.getSenderColor(entry.content.toString());

                        spannable.setSpan(new ForegroundColorSpan(color_new), rawText.lastIndexOf(entry.content.toString()), rawText.lastIndexOf(entry.content.toString()) + entry.content.toString().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        holder.msgView.setText(spannable);
                    }
                    holder.msgView.setTextColor(ThemeUtil.color.chatAction);
                    holder.parent.setBackgroundColor(ThemeUtil.color.chatActionBg);
                    nickCompletionHelper = new NickCompletionHelper(buffer.getUsers().getUniqueUsers());
                    break;
                case NetsplitJoin:
                    holder.msgView.setText(new NetsplitHelper(entry.content.toString()).formatJoinMessage());
                    holder.msgView.setTextColor(ThemeUtil.color.chatAction);
                    holder.parent.setBackgroundColor(ThemeUtil.color.chatActionBg);
                    nickCompletionHelper = new NickCompletionHelper(buffer.getUsers().getUniqueUsers());
                    break;
                case NetsplitQuit:
                    holder.msgView.setText(new NetsplitHelper(entry.content.toString()).formatQuitMessage());
                    holder.msgView.setTextColor(ThemeUtil.color.chatAction);
                    holder.parent.setBackgroundColor(ThemeUtil.color.chatActionBg);
                    nickCompletionHelper = new NickCompletionHelper(buffer.getUsers().getUniqueUsers());
                    break;
                case DayChange:
                    holder.msgView.setText(Html.fromHtml(String.format("<i> Day changed to %s</i>", entry.content.toString())));
                    holder.msgView.setTextColor(ThemeUtil.color.chatAction);
                    holder.parent.setBackgroundColor(ThemeUtil.color.chatActionBg);
                case Plain:
                default:
                    if (entry.isSelf() || entry.isHighlighted()) {
                        color = ThemeUtil.color.chatPlain;
                    } else {
                        color = entry.getSenderColor();
                    }
                    holder.msgView.setTextColor(ThemeUtil.color.chatPlain);
                    holder.msgView.setTypeface(Typeface.DEFAULT);

                    nickSpan = new SpannableString(entry.getNick());
                    nickSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, entry.getNick().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    nickSpan.setSpan(new ForegroundColorSpan(color), 0, entry.getNick().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    holder.msgView.setText((SpannedString) TextUtils.concat(nickSpan, " ", entry.content));
                    holder.parent.setBackgroundColor(Color.TRANSPARENT);
                    break;
            }
            if (entry.isHighlighted()) {
                holder.item_layout.setBackgroundColor(ThemeUtil.color.chatHighlight);
            } else {
                holder.item_layout.setBackgroundResource(0);
            }
            //Log.i(TAG, "CONTENT:" + entry.content);
            return convertView;
        }

        @Override
        public void update(Observable observable, Object data) {
            if (data == null) {
                notifyDataSetChanged();
                return;
            }
            switch ((Integer) data) {
                case R.id.BUFFERUPDATE_NEWMESSAGE:
                    notifyDataSetChanged();
                    if (getUserVisibleHint()) {
                        updateRead();
                    }
                    break;
                case R.id.BUFFERUPDATE_BACKLOG:
                    int topId = getListTopMessageId();
                    notifyDataSetChanged();
                    setListTopMessage(topId);
                    break;
                case R.id.BUFFERUPDATE_TOPICCHANGED:
                    notifyDataSetChanged();
                    break;
                default:
                    notifyDataSetChanged();
            }

        }

        /*
         * Returns the messageid for the ircmessage that is currently at the top of the screen
         */
        public int getListTopMessageId() {
            int topId;
            if (backlogList.getChildCount() == 0) {
                topId = 0;
            } else {
                topId = ((ViewHolder) backlogList.getChildAt(0).getTag()).messageID;
            }
            return topId;
        }

        /*
         * Sets what message from the adapter will be at the top of the visible screen
         */
        public void setListTopMessage(int messageid) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItemId(i) == messageid) {
                    backlogList.setSelectionFromTop(i, 5);
                    break;
                }
            }
        }

        public void clearBuffer() {
            if (buffer != null) {
                buffer.deleteObserver(this);
                buffer.setDisplayed(false);
                buffer = null;
                notifyDataSetChanged();
            }
        }

        public int getBufferId() {
            return buffer.getInfo().id;
        }

        public void getMoreBacklog() {
            adapter.buffer.setBacklogPending(true);
            BusProvider.getInstance().post(new GetBacklogEvent(adapter.getBufferId(), dynamicBacklogAmount));
        }

        public void removeFilter(Type type) {
            buffer.removeFilterType(type);

        }

        public void addFilter(Type type) {
            buffer.addFilterType(type);

        }
    }

    private class BacklogScrollListener implements OnScrollListener {

        private int visibleThreshold;

        public BacklogScrollListener(int visibleThreshold) {
            this.visibleThreshold = visibleThreshold;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            //			Log.d(TAG, "loading: "+ Boolean.toString(loading) +"totalItemCount: "+totalItemCount+ "visibleItemCount: " +visibleItemCount+"firstVisibleItem: "+firstVisibleItem+ "visibleThreshold: "+visibleThreshold);
            if (adapter.buffer != null && !adapter.buffer.hasPendingBacklog() && (firstVisibleItem <= visibleThreshold)) {
                adapter.getMoreBacklog();
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            // Not interesting for us to use
        }
    }
}