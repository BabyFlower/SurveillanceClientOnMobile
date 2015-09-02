package com.anviz.scom.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.anviz.scom.R;

/**
 * 事件查询列表适配器
 * 
 * @author 8444
 * 
 */
public class UI13_EventFilteringAdapter extends BaseAdapter {

	/** 保存选中的状态 */
	private Map<Integer, Boolean> selectedMap;
	
	private ArrayList<String> list = new ArrayList<String>();

	private Cursor cursor;
	private Context context;
	
	private String column;

	public UI13_EventFilteringAdapter(Context context, Cursor cursor, String column) {
		this.context = context;
		this.cursor = cursor;
		this.column = column;
		selectedMap = new HashMap<Integer, Boolean>();
	}

	public int getCount() {
		return cursor.getCount();
	}

	public Object getItem(int position) {
		return cursor.moveToPosition(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {

		convertView = LayoutInflater.from(context).inflate(
				R.layout.ui13_adapter, null);
		CheckBox cb = (CheckBox) convertView.findViewById(R.id.ui13_adapter_cb);
		
		
		cursor.moveToPosition(position);
		
		final int id = cursor.getInt(0);
		
		final String value = cursor.getString(cursor.getColumnIndex(column));
		
		cb.setText(value);
		
		if(null != selectedMap.get(id) && selectedMap.get(id)){
			cb.setChecked(true);
		}else{
			cb.setChecked(false);
		}
		
		convertView.setOnClickListener(new OnClickListener() {
			
			public void onClick(View view) {
				
			}
		});
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				selectedMap.put(id, isChecked);
				if(isChecked){
					list.add(value);
				}else{
					list.remove(value);
				}
			}
		});
		return convertView;
	}

	public ArrayList<String> getList() {
		return list;
	}
}
