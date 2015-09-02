package com.anviz.scom.ui;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.anviz.scom.R;
import com.anviz.scom.model.Device;

/**
 * 查找添加设备适配器
 * 
 * @author 8444
 * 
 */
public class UI18_Adapter extends BaseAdapter {

	private List<Device> data;
	private Context context;

	public UI18_Adapter(Context context, List<Device> data) {
		this.data = data;
		this.context = context;
	}

	public int getCount() {
		return data.size();
	}

	public Object getItem(int position) {
		return data.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		convertView = LayoutInflater.from(context).inflate(
				R.layout.ui18_adapter, null);
		ImageView iv = (ImageView) convertView.findViewById(R.id.ui18_adapter_iv);
		TextView nameTv = (TextView) convertView
				.findViewById(R.id.ui18_adapter_name);
		ImageView addIv = (ImageView) convertView.findViewById(R.id.ui18_adapter_add);

		iv.setImageResource(R.drawable.play_default);
		nameTv.setText(data.get(position).getName());
		addIv.setOnClickListener(new OnClickListener() {
			
			public void onClick(View view) {
				
			}
		});

		return convertView;
	}

}
