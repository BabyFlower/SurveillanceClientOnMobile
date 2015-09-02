package com.anviz.scom.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anviz.scom.R;

/**
 * 其它管理页面
 * 
 * @author 8444
 * 
 */
public class UI21_OtherManageActivity extends Activity implements
		OnTouchListener, OnGestureListener {

	private TextView backTv;
	private LinearLayout layout, listLL;

	private GestureDetector mGestureDetector;

	private TextView deviceManageTv, featureSetTv, informationTv;

	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui21);

		layout = (LinearLayout) findViewById(R.id.ui21_layout);
		backTv = (TextView) findViewById(R.id.ui21_back);
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.ui21_title_layout);
		deviceManageTv = (TextView) findViewById(R.id.ui21_device_manage);
		featureSetTv = (TextView) findViewById(R.id.ui21_feature_set);
		informationTv = (TextView) findViewById(R.id.ui21_information);
		listLL = (LinearLayout) findViewById(R.id.ui21_list);

		backTv.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});

		mGestureDetector = new GestureDetector(this);
		layout.setOnTouchListener(this);

		WindowManager mWindowManager = this.getWindowManager();
		Display mDisplay = mWindowManager.getDefaultDisplay();
		float cellWwdth = mDisplay.getWidth() / 100;

		LayoutParams rlLayoutParams = rl.getLayoutParams();
		LayoutParams listLLLayputParams = listLL.getLayoutParams();

		rlLayoutParams.width = (int) (cellWwdth * 80);
		listLLLayputParams.width = (int) (cellWwdth * 80);

		deviceManageTv.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				startActivity(new Intent(UI21_OtherManageActivity.this,
						UI15_DeviceManageActivity.class));
			}
		});

		featureSetTv.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				startActivity(new Intent(UI21_OtherManageActivity.this,
						UI22_FeatureSetActivity.class));
			}
		});

		informationTv.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				startActivity(new Intent(UI21_OtherManageActivity.this,
						UI20_InfoActivity.class));
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
