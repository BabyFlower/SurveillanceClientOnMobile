package com.anviz.scom.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.anviz.scom.R;
import com.anviz.scom.model.Device;

/**
 * 查找添加
 * @author 8444
 *
 */
public class UI18_FindAddDeviceActivity extends Activity {

	private ListView lv;
	private UI18_Adapter adapter;
	private List<Device> data;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui18);
		
		lv = (ListView) findViewById(R.id.ui18_lv);
		
		findViewById(R.id.ui18_back).setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});
		
		data = new ArrayList<Device>();
		for(int i = 0; i < 10; i ++){
			Device mDevice = new Device();
			mDevice.setName("测试" + i);
			data.add(mDevice);
		}
		
		adapter = new UI18_Adapter(this, data);
		lv.setAdapter(adapter);
	
	}
}
