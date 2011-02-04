package com.lekebilen.quasseldroid.gui;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.lekebilen.quasseldroid.CoreConnection;
import com.lekebilen.quasseldroid.R;

import android.R.integer;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;	
import android.test.PerformanceTestCase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class ChatActivity extends Activity{

	private static final String TAG = ChatActivity.class.getSimpleName();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_layout);
		
		
		//Populate with test data
		((TextView)findViewById(R.id.chatNameView)).setText("#mtdt12");

		BacklogAdapter adapter = new BacklogAdapter(this, null);
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