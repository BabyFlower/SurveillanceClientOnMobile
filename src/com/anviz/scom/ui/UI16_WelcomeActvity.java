package com.anviz.scom.ui;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.anviz.scom.R;

/**
 *  首次进入系统 滑动欢迎页面  
 * @author anviz
 *
 */
public class UI16_WelcomeActvity extends Activity{
	
	private UI16_MyScrollLayout scroll;
	private ImageView[] imgs;
	private LinearLayout pointLLayout;
	/**当前所在子项*/
	private int currentItem;
	private Button welcomeBtn;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui16);
		
		scroll = (UI16_MyScrollLayout) findViewById(R.id.ui16_scrollLayout);
		pointLLayout = (LinearLayout) findViewById(R.id.ui16_pointLayout);
		welcomeBtn = (Button) findViewById(R.id.ui16_welcomeButton);
		
		welcomeBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View view) {
				startActivity(new Intent(UI16_WelcomeActvity.this, UI04_LoginActivity.class));
			}
		});
		
		imgs = new ImageView[scroll.getChildCount()];
		for(int i = 0; i < scroll.getChildCount(); i++){
			imgs[i] = (ImageView) pointLLayout.getChildAt(i);
			imgs[i].setEnabled(true);
			imgs[i].setTag(i);
		}
		currentItem = 0;
		imgs[currentItem].setEnabled(false);
		scroll.setOnViewChangeListener(new UI16_OnViewChangeListener() {

			public void OnViewChange(int position) {
				setCurrentPoint(position);
			}
		});
	}
	
	protected void setCurrentPoint(int position) {
		if(position < 0 || position > pointLLayout.getChildCount() - 1
				|| position == currentItem){
			return;
		}
		imgs[currentItem].setEnabled(true);
		imgs[position].setEnabled(false);
		currentItem = position;
	}
}
