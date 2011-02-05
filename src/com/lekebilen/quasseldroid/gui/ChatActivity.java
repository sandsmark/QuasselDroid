package com.lekebilen.quasseldroid.gui;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.lekebilen.quasseldroid.R;

public class ChatActivity extends Activity{
	
	
	public static final int MESSAGE_RECEIVED = 0;
	
	private BacklogAdapter adapter;
	private static final String TAG = ChatActivity.class.getSimpleName();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_layout);
		
		//Populate with test data
		((TextView)findViewById(R.id.chatNameView)).setText("#mtdt12");

		adapter = new BacklogAdapter(this, null);
		adapter.addItem(new BacklogEntry("1", "nr1", "Woo"));
		adapter.addItem(new BacklogEntry("2", "n2", "Weee"));
		adapter.addItem(new BacklogEntry("3", "nr3", "Djiz"));
		adapter.addItem(new BacklogEntry("4", "nr4", "Pfft"));
		adapter.addItem(new BacklogEntry("5", "nr5", "Meh"));
		adapter.addItem(new BacklogEntry("6", "nr6", ":D"));
		adapter.addItem(new BacklogEntry("7", "nr7", "Hax"));
		adapter.addItem(new BacklogEntry("8", "nr8", "asdasa sdasd asd asds a"));
		adapter.addItem(new BacklogEntry("9", "nr9", "MER SPAM"));
		((ListView)findViewById(R.id.chatBacklogList)).setAdapter(adapter);
		
		findViewById(R.id.ChatInputView).setOnKeyListener(inputfieldKeyListener);
		
	}
	
	private OnKeyListener inputfieldKeyListener =  new View.OnKeyListener() {

        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction()==0 ) { //On key down as well
                EditText inputfield = (EditText)findViewById(R.id.ChatInputView);
                String inputText = inputfield.getText().toString();
                
                if ( ! "".equals(inputText) ) {
                	//coreconnection.sendmessage(inputText);
                	inputfield.setText("");
                }
                
                return true;
            }
            return false;
        }
    };
	
	//Nick autocomplete when pressing the search-button
	@Override
	public boolean onSearchRequested() {
		ArrayList<String> nicks = new ArrayList<String>();
		nicks.add("hei");
		nicks.add("Hadet");
		nicks.add("hvordan");
		nicks.add("går");
		nicks.add("det");
		
		EditText inputfield = (EditText)findViewById(R.id.ChatInputView); 
		String inputNick = inputfield.getText().toString();
		
		if ( "".equals(inputNick) ) {
			if ( nicks.size() > 0 ) {
				inputfield.setText(nicks.get(0)+ ": ");
				inputfield.setSelection(nicks.get(0).length() + 2);
			}
		} else {
			for (String nick : nicks) {
				if ( nick.matches("(?i)"+inputNick+".*")  ) {
					inputfield.setText(nick+ ": ");
					inputfield.setSelection(nick.length() + 2);
					break;
				}
			}
		}
		return false;  // don't go ahead and show the search box
	}
	
	private class BacklogAdapter extends BaseAdapter {
		
		private ArrayList<BacklogEntry> backlog;
		private LayoutInflater inflater;

		
		public BacklogAdapter(Context context, ArrayList<BacklogEntry> backlog) {
			if (backlog==null) {
				this.backlog = new ArrayList<BacklogEntry>();
			}else {
				this.backlog = backlog;				
			}
			inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}
		
		public void addItem(BacklogEntry item) {
			Log.i(TAG, item.time);
            this.backlog.add(item);
            notifyDataSetChanged();
        }


		@Override
		public int getCount() {
			return backlog.size();
		}

		@Override
		public BacklogEntry getItem(int position) {
			return backlog.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView==null) {
				convertView = inflater.inflate(R.layout.backlog_item, null);
				holder = new ViewHolder();
				holder.timeView = (TextView)convertView.findViewById(R.id.backlog_time_view);
				holder.nickView = (TextView)convertView.findViewById(R.id.backlog_nick_view);
				holder.msgView = (TextView)convertView.findViewById(R.id.backlog_msg_view);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			BacklogEntry entry = backlog.get(position);
			holder.timeView.setText(entry.time);
			holder.nickView.setText(entry.nick);
			holder.msgView.setText(entry.msg);
			return convertView;
		}

		
	}
	
	
	// The Handler that gets information back from the CoreConnService
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
	            case MESSAGE_RECEIVED:
	            	com.lekebilen.quasseldroid.Message message = (com.lekebilen.quasseldroid.Message) msg.obj;
	            	adapter.addItem(new BacklogEntry(message.timestamp.toString(), message.sender, message.content));
	                break;
            }
        }
    };
	
	
	public static class ViewHolder {
        public TextView timeView;
        public TextView nickView;
        public TextView msgView;
    }
	
	public class BacklogEntry {
		public String time;
		public String nick;
		public String msg;
		
		public BacklogEntry(String time, String nick, String msg) {
			this.time = time;
			this.nick = nick;
			this.msg = msg;
		}
	}

}