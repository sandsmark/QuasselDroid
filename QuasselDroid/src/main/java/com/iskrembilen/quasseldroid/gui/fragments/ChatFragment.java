package com.iskrembilen.quasseldroid.gui.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.iskrembilen.quasseldroid.Buffer;
import com.iskrembilen.quasseldroid.BufferInfo;
import com.iskrembilen.quasseldroid.IrcMessage;
import com.iskrembilen.quasseldroid.Network;
import com.iskrembilen.quasseldroid.NetworkCollection;
import com.iskrembilen.quasseldroid.Quasseldroid;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.IrcMessage.Type;
import com.iskrembilen.quasseldroid.events.BufferOpenedEvent;
import com.iskrembilen.quasseldroid.events.CompleteNickEvent;
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent;
import com.iskrembilen.quasseldroid.events.GetBacklogEvent;
import com.iskrembilen.quasseldroid.events.ManageChannelEvent;
import com.iskrembilen.quasseldroid.events.ManageMessageEvent;
import com.iskrembilen.quasseldroid.events.NetworksAvailableEvent;
import com.iskrembilen.quasseldroid.events.SendMessageEvent;
import com.iskrembilen.quasseldroid.events.ConnectionChangedEvent.Status;
import com.iskrembilen.quasseldroid.events.ManageChannelEvent.ChannelAction;
import com.iskrembilen.quasseldroid.events.ManageMessageEvent.MessageAction;
import com.iskrembilen.quasseldroid.events.UpdateReadBufferEvent;
import com.iskrembilen.quasseldroid.gui.MainActivity;
import com.iskrembilen.quasseldroid.gui.LoginActivity;
import com.iskrembilen.quasseldroid.gui.PreferenceView;
import com.iskrembilen.quasseldroid.util.BusProvider;
import com.iskrembilen.quasseldroid.util.Helper;
import com.iskrembilen.quasseldroid.util.InputHistoryHelper;
import com.iskrembilen.quasseldroid.util.NetsplitHelper;
import com.iskrembilen.quasseldroid.util.NickCompletionHelper;
import com.iskrembilen.quasseldroid.util.ThemeUtil;
import com.squareup.otto.Subscribe;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.opengl.Visibility;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.TextView.OnEditorActionListener;

public class ChatFragment extends SherlockFragment {

	private static final String TAG = ChatFragment.class.getSimpleName();
	public static final String BUFFER_ID = "bufferid";
	private static final String BUFFER_NAME = "buffername";

	private BacklogAdapter adapter;
	private ListView backlogList;
	private EditText inputField;
	private TextView topicView;
	private TextView topicViewFull;
	private ImageButton autoCompleteButton;
	private int dynamicBacklogAmout;
	private NickCompletionHelper nickCompletionHelper;
	private int bufferId = -1;

	SharedPreferences preferences;
	private boolean connected;
	private NetworkCollection networks;

	public static ChatFragment newInstance() {
		return new ChatFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		adapter = new BacklogAdapter(getSherlockActivity(), null);
		preferences = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());
		if(savedInstanceState != null && savedInstanceState.containsKey(BUFFER_ID)) {
			bufferId = savedInstanceState.getInt(BUFFER_ID);
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.chat_fragment_layout, container, false);
		backlogList = (ListView) root.findViewById(R.id.chat_backlog_list_view);
		inputField = (EditText) root.findViewById(R.id.chat_input_view);
		topicView = (TextView) root.findViewById(R.id.chat_topic_view);
		topicViewFull = (TextView) root.findViewById(R.id.chat_topic_view_full);
		OnClickListener topicListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(topicView.isShown()) {
					topicView.setVisibility(View.GONE);
					topicViewFull.setVisibility(View.VISIBLE);
				} else {
					topicViewFull.setVisibility(View.GONE);
					topicView.setVisibility(View.VISIBLE);
				}
			}
		};
		topicView.setOnClickListener(topicListener);
		topicViewFull.setOnClickListener(topicListener);
		autoCompleteButton = (ImageButton) root.findViewById(R.id.chat_auto_complete_button);

		backlogList.setAdapter(adapter);
		backlogList.setOnScrollListener(new BacklogScrollListener(5));
		backlogList.setSelection(backlogList.getChildCount());

		autoCompleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				nickCompletionHelper.completeNick(inputField);
			}
		});

		inputField.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (event != null && event.getAction() == KeyEvent.ACTION_DOWN && 
						((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) || (event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER))) {
					String inputText = inputField.getText().toString();

					if (!"".equals(inputText)) {
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
					EditText text = (EditText)v;
					InputHistoryHelper.tempStoreCurrentEntry(text.getText().toString());
					text.setText(InputHistoryHelper.getNextHistoryEntry());
					text.setSelection(text.getText().length());
					return true;
				}
				if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
					EditText text = (EditText)v;
					text.setText(InputHistoryHelper.getPreviousHistoryEntry());
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
		inflater.inflate(R.menu.chat_fragment_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_hide_events:
			if(adapter.buffer == null)
				Toast.makeText(getSherlockActivity(), getString(R.string.not_available), Toast.LENGTH_SHORT).show();
			else showHideEventsDialog();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStart() {
		super.onStart();
		dynamicBacklogAmout = Integer.parseInt(preferences.getString(getString(R.string.preference_dynamic_backlog), "10"));
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
		if (Quasseldroid.connected && getUserVisibleHint()) updateRead();
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
		if(adapter.buffer != null) {
			adapter.buffer.setDisplayed(false);

			//Dont save position if list is at bottom
			if (backlogList.getLastVisiblePosition()==adapter.getCount()-1) {
				adapter.buffer.setTopMessageShown(0);
			}else{
				adapter.buffer.setTopMessageShown(adapter.getListTopMessageId());
			}
			if (adapter.buffer.getUnfilteredSize()!= 0){
				BusProvider.getInstance().post(new ManageChannelEvent(adapter.getBufferId(), ChannelAction.MARK_AS_READ));
				BusProvider.getInstance().post(new ManageMessageEvent(adapter.getBufferId(), adapter.buffer.getUnfilteredBacklogEntry(adapter.buffer.getUnfilteredSize()-1).messageId, MessageAction.LAST_SEEN));
			}

		}
	}

    private void updateMarkerLine() {
        BusProvider.getInstance().post(new ManageMessageEvent(adapter.getBufferId(), adapter.buffer.getLastSeenMessage(), MessageAction.MARKER_LINE));
    }

	public void setBuffer(int bufferId) {
        Log.d(TAG, "Setting buffer and chat is visible: " + getUserVisibleHint());
		this.bufferId = bufferId;
		if(adapter != null && networks != null) {
            if(adapter.buffer != null && bufferId != adapter.buffer.getInfo().id) {
                updateMarkerLine();
            }
			adapter.clearBuffer();
			Buffer buffer = networks.getBufferById(bufferId);
            if(buffer!=null){
                adapter.setBuffer(buffer, networks);
                nickCompletionHelper = new NickCompletionHelper(buffer.getUsers().getUniqueUsers());
                autoCompleteButton.setEnabled(true);
                inputField.setEnabled(true);
                buffer.setDisplayed(true);
                BusProvider.getInstance().post(new ManageChannelEvent(buffer.getInfo().id, ChannelAction.HIGHLIGHTS_READ));

                //Move list to correect position
                if (adapter.buffer.getTopMessageShown() == 0) {
                    backlogList.setSelection(adapter.getCount()-1);
                }else{
                    adapter.setListTopMessage(adapter.buffer.getTopMessageShown());
                }
            }else{
                resetFragment();
            }
		}
	}

	public class BacklogAdapter extends BaseAdapter implements Observer {

		//private ArrayList<IrcMessage> backlog;
		private LayoutInflater inflater;
		private Buffer buffer;


		public BacklogAdapter(Context context, ArrayList<IrcMessage> backlog) {
			inflater = getLayoutInflater(null);

		}

		public void setBuffer(Buffer buffer, NetworkCollection networks) {
			this.buffer = buffer;
			buffer.addObserver(this);
			String topic = "";
			if ( buffer.getInfo().type == BufferInfo.Type.QueryBuffer ){
				topic = buffer.getInfo().name;
			} else if ( buffer.getInfo().type == BufferInfo.Type.StatusBuffer ){
				Network network = networks.getNetworkById(buffer.getInfo().networkId);
				topic = network.getName() + " ("
						+ network.getServer() + ") | "
						+ getResources().getString(R.string.users) + ": "
						+ network.getCountUsers() + " | "
						+ Helper.formatLatency(network.getLatency(), getResources());
			} else{
				 topic = buffer.getInfo().name + ": " + buffer.getTopic();
			}
			topicView.setText(topic);
			topicViewFull.setText(topic);
			notifyDataSetChanged();
			backlogList.scrollTo(backlogList.getScrollX(), backlogList.getScrollY());
		}


		@Override
		public int getCount() {
			if (this.buffer==null) return 0;
			return buffer.getSize();
		}

		@Override
		public IrcMessage getItem(int position) {
			//TODO: QriorityQueue is fucked, we dont want to convert to array here, so change later
			return (IrcMessage) buffer.getBacklogEntry(position);
		}

		@Override
		public long getItemId(int position) {
			return buffer.getBacklogEntry(position).messageId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;

			if (convertView==null) {
				convertView = inflater.inflate(R.layout.backlog_item, null);
				holder = new ViewHolder();
				holder.timeView = (TextView)convertView.findViewById(R.id.backlog_time_view);
				holder.timeView.setTextColor(ThemeUtil.chatTimestampColor);
				holder.nickView = (TextView)convertView.findViewById(R.id.backlog_nick_view);
				holder.msgView = (TextView)convertView.findViewById(R.id.backlog_msg_view);
				holder.separatorView = (TextView)convertView.findViewById(R.id.backlog_list_separator);
				holder.item_layout = (LinearLayout)convertView.findViewById(R.id.backlog_item_linearlayout);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}

			//Set separator line here
			if (position != (getCount()-1) && (buffer.getMarkerLineMessage() == getItem(position).messageId || (buffer.isMarkerLineFiltered() && getItem(position).messageId<buffer.getMarkerLineMessage() && getItem(position+1).messageId>buffer.getMarkerLineMessage()))) { 
				holder.separatorView.getLayoutParams().height = 1;
			} else {
				holder.separatorView.getLayoutParams().height = 0;
			}

			IrcMessage entry = this.getItem(position);
			holder.messageID = entry.messageId;
			holder.timeView.setText(entry.getTime());

			switch (entry.type) {
			case Action:
				holder.nickView.setText("-*-");
				holder.msgView.setTextColor(ThemeUtil.chatActionColor);
				holder.nickView.setTextColor(ThemeUtil.chatActionColor);
				holder.msgView.setText(entry.getNick()+" "+entry.content);
				break;
			case Error:
				holder.nickView.setText("*");
				holder.msgView.setTextColor(ThemeUtil.chatErrorColor);
				holder.nickView.setTextColor(ThemeUtil.chatErrorColor);
				holder.msgView.setText(entry.content);
				break;
			case Server:
				holder.nickView.setText("*");
				holder.msgView.setTextColor(ThemeUtil.chatServerColor);
				holder.nickView.setTextColor(ThemeUtil.chatServerColor);
				holder.msgView.setText(entry.content);
				break;
			case Topic:
				holder.nickView.setText("*");
				holder.msgView.setTextColor(ThemeUtil.chatTopicColor);
				holder.nickView.setTextColor(ThemeUtil.chatTopicColor);
				holder.msgView.setText(entry.content);
				break;
			case Notice:
				holder.nickView.setText("["+entry.getNick()+"]");
				holder.msgView.setTextColor(ThemeUtil.chatNoticeColor);
				holder.nickView.setTextColor(ThemeUtil.chatNoticeColor);
				holder.msgView.setText(entry.content);
				break;
			case Join:
				holder.nickView.setText("-->");
				holder.msgView.setText(entry.getNick() + " has joined " + entry.content);
				holder.msgView.setTextColor(ThemeUtil.chatJoinColor);
				holder.nickView.setTextColor(ThemeUtil.chatJoinColor);
				break;
			case Part:
				holder.nickView.setText("<--");
				holder.msgView.setText(entry.getNick() + " has left (" + entry.content + ")");
				holder.msgView.setTextColor(ThemeUtil.chatPartColor);
				holder.nickView.setTextColor(ThemeUtil.chatPartColor);
				break;
			case Quit:				
				holder.nickView.setText("<--");
				holder.msgView.setText(entry.getNick() + " has quit (" + entry.content + ")");
				holder.msgView.setTextColor(ThemeUtil.chatQuitColor);
				holder.nickView.setTextColor(ThemeUtil.chatQuitColor);
				break;
			case Kill:
				holder.nickView.setText("<--");
				holder.msgView.setText(entry.getNick() + " was killed (" + entry.content + ")");
				holder.msgView.setTextColor(ThemeUtil.chatKillColor);
				holder.nickView.setTextColor(ThemeUtil.chatKillColor);
				break;
			case Kick:
				holder.nickView.setText("<-*");
				int nickEnd = entry.content.toString().indexOf(" ");
				String nick = entry.content.toString().substring(0, nickEnd);
				String reason = entry.content.toString().substring(nickEnd+1);
				holder.msgView.setText(entry.getNick() + " has kicked " + nick + " from " + entry.bufferInfo.name + " (" + reason + ")");
				holder.msgView.setTextColor(ThemeUtil.chatKickColor);
				holder.nickView.setTextColor(ThemeUtil.chatKickColor);
				break;
			case Mode:
				holder.nickView.setText("***");
				holder.msgView.setText("Mode " + entry.content.toString() + " by " + entry.getNick());
				holder.msgView.setTextColor(ThemeUtil.chatModeColor);
				holder.nickView.setTextColor(ThemeUtil.chatModeColor);
				break;
			case Nick:
				holder.nickView.setText("<->");
				holder.msgView.setText(entry.getNick()+" is now known as " + entry.content.toString());
				holder.msgView.setTextColor(ThemeUtil.chatNickColor);
				holder.nickView.setTextColor(ThemeUtil.chatNickColor);
				break;
			case NetsplitJoin:
				holder.nickView.setText("=>");
				holder.msgView.setText(new NetsplitHelper(entry.content.toString()).formatJoinMessage());
				holder.msgView.setTextColor(ThemeUtil.chatNetsplitJoinColor);
				holder.nickView.setTextColor(ThemeUtil.chatNetsplitJoinColor);
				break;
			case NetsplitQuit:
				holder.nickView.setText("<=");
				holder.msgView.setText(new NetsplitHelper(entry.content.toString()).formatQuitMessage());
				holder.msgView.setTextColor(ThemeUtil.chatNetsplitQuitColor);
				holder.nickView.setTextColor(ThemeUtil.chatNetsplitQuitColor);
				break;
			case DayChange:
				holder.nickView.setText("-");
				holder.msgView.setText("{Day changed to " + entry.content.toString() + "}");
				holder.msgView.setTextColor(ThemeUtil.chatDayChangeColor);
				holder.nickView.setTextColor(ThemeUtil.chatDayChangeColor);
			case Plain:
			default:
				if(entry.isSelf()) {
					holder.nickView.setTextColor(ThemeUtil.chatSelfColor);
				}else{
					holder.nickView.setTextColor(entry.senderColor);
				}
				holder.msgView.setTextColor(ThemeUtil.chatPlainColor);
				holder.msgView.setTypeface(Typeface.DEFAULT);

				holder.nickView.setText("<" + entry.getNick() + ">");
				holder.msgView.setText(entry.content);
				break;
			}
			if (entry.isHighlighted()) {
				holder.item_layout.setBackgroundColor(ThemeUtil.chatHighlightColor);
			}else {
				holder.item_layout.setBackgroundResource(0);
			}
			//Log.i(TAG, "CONTENT:" + entry.content);
			return convertView;
		}

		@Override
		public void update(Observable observable, Object data) {
			if (data==null) {
				notifyDataSetChanged();
				return;
			}
			switch ((Integer)data) {
			case R.id.BUFFERUPDATE_NEWMESSAGE:
				notifyDataSetChanged();
                if(getUserVisibleHint()) {
                    updateRead();
                }
				break;
			case R.id.BUFFERUPDATE_BACKLOG:
				int topId = getListTopMessageId();
				notifyDataSetChanged();
				setListTopMessage(topId);
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
			if (backlogList.getChildCount()==0) {
				topId = 0;
			}else {
				topId = ((ViewHolder)backlogList.getChildAt(0).getTag()).messageID;
			}
			return topId;
		}

		/*
		 * Sets what message from the adapter will be at the top of the visible screen
		 */
		public void setListTopMessage(int messageid) {
			for(int i=0;i<adapter.getCount();i++){
				if (adapter.getItemId(i)==messageid){
					backlogList.setSelectionFromTop(i,5);
					break;
				}
			}
		}

		public void clearBuffer() {
			if(buffer != null) {
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
			adapter.buffer.setBacklogPending(dynamicBacklogAmout);
			BusProvider.getInstance().post(new GetBacklogEvent(adapter.getBufferId(), dynamicBacklogAmout));
		}

		public void removeFilter(Type type) {
			buffer.removeFilterType(type);

		}

		public void addFilter(Type type) {
			buffer.addFilterType(type);

		}
	}	

	private void onNickComplete() {
        if(nickCompletionHelper!=null){
           nickCompletionHelper.completeNick(inputField);
        }
	}

	public static class ViewHolder {
		public TextView timeView;
		public TextView nickView;
		public TextView msgView;
		public TextView separatorView;
		public LinearLayout item_layout;

		public int messageID;
	}

	private class BacklogScrollListener implements OnScrollListener {

		private int visibleThreshold;

		public BacklogScrollListener(int visibleThreshold) {
			this.visibleThreshold = visibleThreshold;
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			//			Log.d(TAG, "loading: "+ Boolean.toString(loading) +"totalItemCount: "+totalItemCount+ "visibleItemCount: " +visibleItemCount+"firstVisibleItem: "+firstVisibleItem+ "visibleThreshold: "+visibleThreshold);
			if (adapter.buffer!=null && !adapter.buffer.hasPendingBacklog() && (firstVisibleItem <= visibleThreshold)) {
				adapter.getMoreBacklog();
			}	
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// Not interesting for us to use
		}
	}

	@Subscribe
	public void onNetworksAvailable(NetworksAvailableEvent event) {
        Log.d(TAG, "onNetworksAvailable event");
		if(event.networks != null) {
			this.networks = event.networks;
			if(bufferId != -1) {
				setBuffer(bufferId);
			}
		}
        Log.d(TAG, "onNetworksAvailable done");
	}

	@Subscribe
	public void onBufferOpened(BufferOpenedEvent event) {
        Log.d(TAG, "onBufferOpened event");
        this.bufferId = event.bufferId;
		if(event.bufferId != -1) {
			setBuffer(bufferId);
		}else{
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

    private void resetFragment(){
        adapter.clearBuffer();
        topicView.setText("");
        topicViewFull.setText("");
        autoCompleteButton.setEnabled(false);
        inputField.setText("");
        inputField.setEnabled(false);
        nickCompletionHelper=null;
    }
}

