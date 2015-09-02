package com.anviz.scom.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.anviz.scom.R;
import com.anviz.scom.sqlite.EventSql;


/**
 * 事件查询结果
 * @author 8444
 * 从UI13传递过来的查询条件
 * 从UI07传递过来的单个设备名称
 */
public class UI14_EventQueryResult extends Activity implements OnCheckedChangeListener{
	
	private ListView lv;
	private UI09_EventAdapter adapter;
	private Cursor cursor;
	
	private long startTime;
	private long endTime;
	private ArrayList<String> devselList;
	private ArrayList<String> typeList;
	
	private RadioGroup rg;
	
	@SuppressWarnings("unchecked")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui14);
		
		lv = (ListView) findViewById(R.id.ui14_lv);
		rg = (RadioGroup) findViewById(R.id.ui14_rg);		
		
		//返回
		findViewById(R.id.ui14_back).setOnClickListener(new OnClickListener() {
			
			public void onClick(View view) {
				finish();
			}
		});
		
		Intent mIntent = getIntent();
		
		Bundle mBundle = mIntent.getExtras();
		startTime = mBundle.getLong("startTime");
		endTime = mBundle.getLong("endTime");
		devselList = (ArrayList<String>) mBundle.getSerializable("devselList");
		typeList = (ArrayList<String>) mBundle.getSerializable("typeList");
		
		
		cursor = EventSql.queryEventFiltering(this, startTime, endTime, devselList, typeList, EventSql.TIME);
		adapter = new UI09_EventAdapter(this, cursor);
		lv.setAdapter(adapter);
		
		rg.setOnCheckedChangeListener(this);
	}

	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.ui14_time_group:
			cursor = EventSql.queryEventFiltering(this, startTime, endTime, devselList, typeList, EventSql.TIME);
			break;
		case R.id.ui14_event_group:
			cursor = EventSql.queryEventFiltering(this, startTime, endTime, devselList, typeList, EventSql.ALARM);
			break;
		case R.id.ui14_device_group:
			cursor = EventSql.queryEventFiltering(this, startTime, endTime, devselList, typeList, EventSql.NAME);
			break;
		}
		adapter = new UI09_EventAdapter(this, cursor);
		lv.setAdapter(adapter);
	}
}
