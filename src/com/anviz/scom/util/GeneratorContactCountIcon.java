package com.anviz.scom.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * 合成数字图片工具类
 * 
 * @author wanry
 * 
 */
public class GeneratorContactCountIcon {

	public GeneratorContactCountIcon() {

	}

	public Bitmap addSum(Bitmap bitmap, int queue) {
		String strQueue = String.valueOf(queue);
		// 初始化画布
		int width = 0;
		int height = 0;

		width = bitmap.getWidth();
		height = bitmap.getHeight();

		Bitmap contactIcon = Bitmap.createBitmap(width, height,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(contactIcon);

		// 复制图片
		Paint bitmapPaint = new Paint();
		// 防抖动
		bitmapPaint.setDither(true);
		// 用来对Bitmap进行滤波处理，这样，当你选择Drawable时，会有抗锯齿的效果
		bitmapPaint.setFilterBitmap(true);
		Rect src = new Rect(0, 0, width, height);
		Rect dst = new Rect(0, 0, width, height);
		canvas.drawBitmap(bitmap, src, dst, bitmapPaint);

		float margin = 4;

		// 启用抗锯齿和使用设备的文本字距
		Paint countPaint = new Paint(Paint.ANTI_ALIAS_FLAG
				| Paint.DEV_KERN_TEXT_FLAG);
		countPaint.setColor(Color.WHITE);
		countPaint.setTextSize(20f);
		float x = width - countPaint.measureText(strQueue) - margin * 4;
		float y = 20f + margin * 4;

		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(3);

		RectF rf2 = new RectF(x, 0, width, y);
		canvas.drawRoundRect(rf2, y / 2, y / 2, paint);

		paint.setColor(Color.RED);
		RectF rf = new RectF(x + margin, margin, width - margin, y - margin);
		canvas.drawRoundRect(rf, y / 2, y / 2, paint);

		canvas.drawText(strQueue, x + margin * 2, y - margin * 3, countPaint);
		return contactIcon;
	}

	public Bitmap addSum(Drawable d, int queue) {
		BitmapDrawable bd = (BitmapDrawable) d;
		Bitmap bitmap = bd.getBitmap();
		return addSum(bitmap, queue);
	}
}
