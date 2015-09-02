package com.anviz.scom.ui;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.anviz.scom.R;
import com.anviz.scom.struct.Devipc;

/**
 * 设备列表适配器
 * @author 8444
 *
 */
public class UI01_DevAdapter extends BaseAdapter{
	
	private Context context;
	private List<Devipc> data;
	
	public UI01_DevAdapter(Context context, List<Devipc> data) {
		this.context = context;
		this.data = data;
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
		
		convertView = LayoutInflater.from(context).inflate(R.layout.ui01_adapter, null);
		
		TextView uiIpTv = (TextView) convertView.findViewById(R.id.ui01_adapter_uiIp);
		TextView szDevInfoTv = (TextView) convertView.findViewById(R.id.ui01_adapter_szDevInfo);
		
		Devipc mDevipc = data.get(position);
		uiIpTv.setText(mDevipc.getUiIp() + "");
		szDevInfoTv.setText(mDevipc.getSzDevInfo());
		
		return convertView;
	}

}
