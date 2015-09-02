package com.anviz.scom.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.anviz.scom.R;

/**
 * 录像详细信息列表
 * 
 * @author 8444
 * 
 */
public class UI12_PlayBackInfoListActivity extends Activity {

	private TextView nameTv, backTv;
	private RadioGroup rg;
	private Button calendarBtn;

	private CalendarView cv;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui12);

		nameTv = (TextView) findViewById(R.id.ui12_name);
		backTv = (TextView) findViewById(R.id.ui12_back);
		rg = (RadioGroup) findViewById(R.id.ui12_rg);
		cv = (CalendarView) findViewById(R.id.ui12_cv);
		calendarBtn = (Button) findViewById(R.id.ui12_calendar);

		Intent parentIntent = getIntent();
		final String name = parentIntent.getStringExtra("name");

		nameTv.setText(name);

		backTv.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});

		cv.setOnDateChangeListener(new OnDateChangeListener() {

			public void onSelectedDayChange(CalendarView view, int year,
					int month, int dayOfMonth) {
				// String date = year + "年" + month +1 + "月" + dayOfMonth + "日";

				Intent it = new Intent(UI12_PlayBackInfoListActivity.this,
						UI03_AnvizPlayerActivity.class);
				it.putExtra("type", 1);
				startActivity(it);
			}
		});

		rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.ui12_remote:
					break;
				case R.id.ui12_local:
					break;
				}
			}
		});

		calendarBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				if (cv.getVisibility() == View.VISIBLE) {
					
					cv.setVisibility(View.INVISIBLE);
				} else {
					cv.setVisibility(View.VISIBLE);
				}

			}
		});
		
		nameTv.setOnClickListener(new OnClickListener() {
			
			public void onClick(View view) {
				if(rg.getVisibility() == View.VISIBLE){
					rg.setVisibility(View.INVISIBLE);
				}else{
					rg.setVisibility(View.VISIBLE);
				}
			
			}
		});

	}

}
