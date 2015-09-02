package com.anviz.scom.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.anviz.scom.R;

/**
 * 设备信息
 * 
 * @author 8444
 * 
 */
public class UI19_DeviceInfoActivity extends Activity {

	private ImageView iv;
	private TextView nameTv;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui19);

		iv = (ImageView) findViewById(R.id.ui19_qrcode);
		nameTv = (TextView) findViewById(R.id.ui19_name);

		iv.setImageResource(R.drawable.testcapture);
		Bundle mBundle = getIntent().getExtras();
		String name = mBundle.getString("name");
		
		nameTv.setText(name);
		
		// 返回
		findViewById(R.id.ui19_back).setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});

	}
}
