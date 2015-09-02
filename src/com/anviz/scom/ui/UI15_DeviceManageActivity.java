package com.anviz.scom.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.anviz.scom.R;
import com.anviz.scom.sqlite.LiveSql;


/**
 * 设备管理页面
 * @author 8444
 *
 */
public class UI15_DeviceManageActivity extends Activity{
	
	private ListView lv;
	private UI15_Adapter adapter;
	
	private TextView addTv;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui15);
	
		lv = (ListView) findViewById(R.id.ui15_lv);
		addTv = (TextView) findViewById(R.id.ui15_add);
		
		findViewById(R.id.ui15_back).setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});
//		按照ID 升序查找 _id desc 为降序
		final Cursor cursor = LiveSql.query(this, null, null, null, null, null, "_id");
		adapter = new UI15_Adapter(this, cursor);
		lv.setAdapter(adapter);
		
		addTv.setOnClickListener(new OnClickListener() {
			
			public void onClick(View view) {
				startActivity(new Intent(UI15_DeviceManageActivity.this, UI02_CaptureActivity.class));
			}
		});
	}

}
