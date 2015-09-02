package com.anviz.scom.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.anviz.scom.R;

/**
 * 全屏展示图片
 * 需要传入byte[]
 * @author 8444
 *
 */
public class UI11_FullScreenImageActivity extends Activity{
	
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.ui11);
		
		ImageView iv = (ImageView) findViewById(R.id.ui11_iv);
		
		byte[] b = getIntent().getByteArrayExtra("byte");
		
		Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length, null);
		BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
		Drawable drawable = bitmapDrawable;
		iv.setImageDrawable(drawable);
		
		iv.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				finish();
			}
		});
	}

}
