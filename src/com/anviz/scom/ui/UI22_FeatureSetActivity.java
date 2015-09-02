package com.anviz.scom.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.anviz.scom.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 功能设置
 * 
 * @author 8444
 * 
 */
public class UI22_FeatureSetActivity extends Activity implements
		OnTouchListener, OnGestureListener {

	private ListView lv;
	private List<Map<String, Object>> data;
	private UI21_Adapter adapter;

	private TextView backTv;
	private LinearLayout layout;

	private GestureDetector mGestureDetector;

	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui22);

		layout = (LinearLayout) findViewById(R.id.ui22_layout);
		lv = (ListView) findViewById(R.id.ui22_lv);
		backTv = (TextView) findViewById(R.id.ui22_back);
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.ui22_title_layout);

		backTv.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});

		mGestureDetector = new GestureDetector(this);
		layout.setOnTouchListener(this);
		lv.setOnTouchListener(this);

		WindowManager mWindowManager = this.getWindowManager();
		Display mDisplay = mWindowManager.getDefaultDisplay();
		float cellWwdth = mDisplay.getWidth() / 100;

		LayoutParams lvLayoutParams = lv.getLayoutParams();
		LayoutParams rlLayoutParams = rl.getLayoutParams();
		//
		lvLayoutParams.width = (int) (cellWwdth * 80);
		rlLayoutParams.width = (int) (cellWwdth * 80);
		
		String[] texts2 = { "2G/3G/4G流量提醒", "自动更新", "手动更新", "设置参数保存",
				"Snap 路径配置", "Record 路径配置", "其他配置" };

		data = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < texts2.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("text", texts2[i]);
			data.add(map);
		}

		adapter = new UI21_Adapter(this, data);

		lv.setAdapter(adapter);

		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:

					break;
				case 1:

					break;
				case 2:

					break;
				case 3:

					break;
				case 4:

					break;
				case 5:

					break;
				case 6:

					break;

				}
			}
		});

	}

	public boolean onTouch(View v, MotionEvent event) {

		return mGestureDetector.onTouchEvent(event);
	}

	public boolean onDown(MotionEvent e) {
		// 必须返回true，否刚layout.setOnTouchListener(this);失效，目前原因不知道
		return true;
	}

	public void onShowPress(MotionEvent e) {

	}

	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	public void onLongPress(MotionEvent e) {

	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		float fling = e2.getX() - e1.getX();

		if (fling > 100) {
			finish();
			return true;
		}
		return false;
	}
}
