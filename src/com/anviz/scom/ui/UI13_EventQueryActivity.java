package com.anviz.scom.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.anviz.scom.R;
import com.anviz.scom.sqlite.EventSql;
import com.anviz.scom.util.DateFormatUtil;

/**
 * 事件查询界面
 * 
 * @author 8444
 * 
 */
public class UI13_EventQueryActivity extends Activity {

	private TextView backTv;

	/** 设备选择列表 */
	private ListView dLv;
	/** 事件过滤列表 */
	private ListView efLv;

	/** 选择开始时间，结束时间 */
	private EditText startTimeEt, endTimeEt;

	private Button queryBtn;

	private UI13_EventFilteringAdapter adapter, adapterType;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui13);

		backTv = (TextView) findViewById(R.id.ui13_back);
		dLv = (ListView) findViewById(R.id.ui13_lv);
		efLv = (ListView) findViewById(R.id.ui13_lv2);
		startTimeEt = (EditText) findViewById(R.id.ui13_start_time);
		endTimeEt = (EditText) findViewById(R.id.ui13_end_time);
		queryBtn = (Button) findViewById(R.id.ui13_query);
		
		Cursor cursor = EventSql
				.queryByGroup(this, EventSql.NAME, EventSql.TIME);
		adapter = new UI13_EventFilteringAdapter(this, cursor, EventSql.NAME);

		Cursor cursorType = EventSql.queryByGroup(this, EventSql.TYPE, EventSql.TIME);
		adapterType = new UI13_EventFilteringAdapter(this, cursorType, EventSql.TYPE);

		dLv.setAdapter(adapter);
		efLv.setAdapter(adapterType);

		backTv.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});

		startTimeEt.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View view, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					Handler mHandler = new Handler() {
						public void handleMessage(Message msg) {
							super.handleMessage(msg);
							String s = (String) msg.obj;
							startTimeEt.setText(s);
						}
					};
					new UI13_SelectTimeDialog(UI13_EventQueryActivity.this,
							mHandler).show();
				}
				return true;
			}
		});

		endTimeEt.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View view, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					Handler mHandler = new Handler() {
						public void handleMessage(Message msg) {
							super.handleMessage(msg);
							String s = (String) msg.obj;
							endTimeEt.setText(s);
						}
					};
					new UI13_SelectTimeDialog(UI13_EventQueryActivity.this,
							mHandler).show();
				}
				return true;
			}
		});
		
		queryBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View view) {
				Intent intent = new Intent(UI13_EventQueryActivity.this, UI14_EventQueryResult.class);
				Bundle mBundle = new Bundle();
				mBundle.putLong("startTime", DateFormatUtil.stringToLong(startTimeEt.getText().toString()));
				mBundle.putLong("endTime", DateFormatUtil.stringToLong(endTimeEt.getText().toString()));
				mBundle.putSerializable("devselList", adapter.getList());
				mBundle.putSerializable("typeList", adapterType.getList());
				intent.putExtras(mBundle);
				startActivity(intent);
			}
		});
	}
}
