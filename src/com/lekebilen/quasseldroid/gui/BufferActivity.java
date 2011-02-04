package com.lekebilen.quasseldroid.gui;

import java.util.ArrayList;
import java.util.HashMap;

import com.lekebilen.quasseldroid.Buffer;
import com.lekebilen.quasseldroid.BufferInfo;
import com.lekebilen.quasseldroid.R;
import com.lekebilen.quasseldroid.gui.ChatActivity.BacklogEntry;
import com.lekebilen.quasseldroid.gui.ChatActivity.ViewHolder;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class BufferActivity extends ListActivity{
	ArrayList<Buffer> bufferList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.buffer_list);
		bufferList = new ArrayList<Buffer>();
		
		Buffer test = new Buffer(new BufferInfo());
		test.getInfo().name = "#testlolol1";
		bufferList.add(test);
		test = new Buffer(new BufferInfo());
		test.getInfo().name = "#testlolol2";
		bufferList.add(test);
		test = new Buffer(new BufferInfo());
		test.getInfo().name = "#testlolol3";
		bufferList.add(test);
		test = new Buffer(new BufferInfo());
		test.getInfo().name = "#testlolol4";
		bufferList.add(test);

		

		ListAdapter adapter = new BufferListAdapter(this, bufferList);

		setListAdapter(adapter);
	}
	
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Toast.makeText(this, bufferList.get(position).getInfo().name, Toast.LENGTH_SHORT).show();
		
		
		
	}
	

	private class BufferListAdapter extends BaseAdapter {
		private ArrayList<Buffer> list;
		private LayoutInflater inflater;

		public BufferListAdapter(Context context, ArrayList<Buffer> list) {
			if (list==null) {
				this.list = new ArrayList<Buffer>();
			}else {
				this.list = list;				
			}
			inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Buffer getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView==null) {
				convertView = inflater.inflate(R.layout.buffer_list_item, null);
				holder = new ViewHolder();
				holder.bufferView = (TextView)convertView.findViewById(R.id.buffer_list_item_name);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			Buffer entry = list.get(position);
			holder.bufferView.setText(entry.getInfo().name);
			return convertView;
		}
	}
	
	public static class ViewHolder {
		public TextView bufferView;
	}

}
