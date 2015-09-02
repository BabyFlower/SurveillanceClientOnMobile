package com.anviz.scom.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.anviz.scom.R;

/**
 * 高级添加设备
 * 
 * @author 8444
 * 
 */
public class UI17_AdvancedAddDeviceActivity extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui17);

		findViewById(R.id.ui17_back).setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});

		findViewById(R.id.ui17_find_add).setOnClickListener(
				new OnClickListener() {
					public void onClick(View view) {
						startActivity(new Intent(
								UI17_AdvancedAddDeviceActivity.this,
								UI18_FindAddDeviceActivity.class));
					}
				});
	}
}
