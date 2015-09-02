package com.anviz.scom.ui;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.anviz.scom.R;
import com.anviz.scom.util.AuthCodeUtil;

/**
 * 登录UI
 * 
 * @author 8444
 * 
 */
public class UI04_LoginActivity extends Activity {

	/** 登录按钮 */
	private Button loginBtn;
	/** 注册按钮 */
	private TextView registerTv;
	/** 验证码图片 */
	private ImageView authCodeIv;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui04);

		registerTv = (TextView) findViewById(R.id.ui04_register);
		loginBtn = (Button) findViewById(R.id.ui04_login);
		authCodeIv = (ImageView) findViewById(R.id.ui04_authcode_iv);

		registerTv.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				startActivity(new Intent(UI04_LoginActivity.this,
						UI05_RegisterActivity.class));
			}
		});

		loginBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				 startActivity(new Intent(UI04_LoginActivity.this,
				 UI06_MainActivity.class));
			}
		});
		authCodeIv.setImageBitmap(AuthCodeUtil.getInstance().createBitmap());
		authCodeIv.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				authCodeIv.setImageBitmap(AuthCodeUtil.getInstance()
						.createBitmap());
			}
		});
	}

}
