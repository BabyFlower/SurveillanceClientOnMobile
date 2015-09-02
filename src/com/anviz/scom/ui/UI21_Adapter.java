package com.anviz.scom.ui;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.anviz.scom.R;

/**
 * 其它功能列表适配器
 * @author 8444
 *
 */
public class UI21_Adapter extends SimpleAdapter{
	
	private Context context;
	private List<Map<String, Object>> data;

	public UI21_Adapter(Context context, List<Map<String, Object>> data) {
		super(context, data, 0, null, null);
		
		this.context = context;
		this.data = data;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		
		View view = LayoutInflater.from(context).inflate(R.layout.ui21_adapter, null);
		
		TextView tv = (TextView) view.findViewById(R.id.ui21_adapter_tv);
		
		tv.setText((String) data.get(position).get("text"));
		return view;
	}
	
}
