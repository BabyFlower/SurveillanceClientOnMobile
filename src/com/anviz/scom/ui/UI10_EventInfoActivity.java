package com.anviz.scom.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.anviz.scom.R;

/**
 * 事件详细信息页面
 * @author 8444
 *
 */
public class UI10_EventInfoActivity extends Activity{

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui10);
		
		findViewById(R.id.ui10_back).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	
	}
}
