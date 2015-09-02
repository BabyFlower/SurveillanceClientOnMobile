package com.anviz.scom.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.anviz.scom.R;
import com.anviz.scom.sqlite.EventSql;

/**
 * 事件管理界面
 * 
 * @author 8444
 * 
 */
public class UI09_EventManageActivity extends Activity implements
		OnCheckedChangeListener {

	private ListView lv;
	/** 适配器 */
	private UI09_EventAdapter mAdapter;
	/** 数据源 */
	private Cursor cursor;

	private RadioGroup rg;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui09);

		lv = (ListView) findViewById(R.id.ui09_lv);
		rg = (RadioGroup) findViewById(R.id.ui09_rg);

//		 EventSql.insert(this, System.currentTimeMillis() + "", "测试1",
//		 "1警告信息", R.drawable.play_default, 0, "IO");
//		 EventSql.insert(this, System.currentTimeMillis() + "", "测试2", "警告信息",
//		 R.drawable.play_default, 0, "移动侦测");
//		 EventSql.insert(this, System.currentTimeMillis() + "", "测试3",
//		 "2警告信息",
//		 R.drawable.play_default, 0, "移动侦测");

		// 加入点击事件监听
		rg.setOnCheckedChangeListener(this);
		cursor = EventSql.query(this, null);
		mAdapter = new UI09_EventAdapter(this, cursor);

		lv.setAdapter(mAdapter);

		lv.setOnItemClickListener(new OnItemClickListener() {

			@SuppressWarnings("deprecation")
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				startActivity(new Intent(UI09_EventManageActivity.this,
						UI10_EventInfoActivity.class));
				// 设置为已读
				Cursor cursor = (Cursor) lv.getItemAtPosition(position);
				final int _id = cursor.getInt(0);
				EventSql.setIsRead(UI09_EventManageActivity.this, _id);
				cursor.requery();
				mAdapter.notifyDataSetChanged();
			}
		});

	}

	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {

		case R.id.ui09_time_group:
			cursor = EventSql.query(this, EventSql.TIME);
			break;
		case R.id.ui09_event_group:
			cursor = EventSql.query(this, EventSql.ALARM);
			break;
		case R.id.ui09_device_group:
			cursor = EventSql.query(this, EventSql.NAME);
			break;
		}

		mAdapter = new UI09_EventAdapter(this, cursor);
		lv.setAdapter(mAdapter);
	}
}
