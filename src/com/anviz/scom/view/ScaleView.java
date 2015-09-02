package com.anviz.scom.view;

import java.sql.Date;
import java.text.SimpleDateFormat;

import com.anviz.scom.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ScaleView extends View {

	private Context context;
	public static String format = "yyyy-MM-dd HH:mm:ss";

	public ScaleView(Context context) {
		super(context);
		this.context = context;
	}

	public ScaleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	private String time = "10:48:22 12/03/2014";
	private float position = 0;
	/** 当前时间刻度 */
	private long currentTimeScale = 0;

	protected void onDraw(Canvas canvas) {

		int height = getHeight();
		int width = getWidth();
		Paint paint = new Paint();

		// 每小时长度
		float hourSize = width / 4;
		Rect bgRect = new Rect(0, height / 2, width, height);
		new Color();
		paint.setColor(Color.rgb(200, 200, 200));
		
		canvas.drawRect(bgRect, paint);

		paint.setColor(Color.GRAY);

		// 字体大小
		float textSize = 25f;
		paint.setTextSize(textSize);

		// 每move一个单位，改变的时间
		float moveTime = 3600 / hourSize;
		currentTimeScale = (long) (0 - (position * moveTime * 1000));
		Date date = new Date(currentTimeScale);
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		time = formatter.format(date);

		for (int i = 0; i < 25; i++) {
			String text = "";
			if(i < 10){
				text = "0" + i + ":00";
			}else{
				text = i + ":00";
			}
			
			canvas.drawText(text + "", position + hourSize * (i + 2), height,
					paint);
		}

		// 边距
		int margin = 40;
		// 文本与倒三角边距
		int space = margin / 4;

		paint.setColor(Color.WHITE);

		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.time_pointer);
		Bitmap downBitmap = BitmapFactory.decodeResource(
				context.getResources(), R.drawable.down);

		paint.setTextSize(textSize);
		float textLength = paint.measureText(time);

		float left = width / 2 - (downBitmap.getWidth() + textLength) / 2
				- margin;
		float right = width / 2 + (downBitmap.getWidth() + textLength) / 2
				+ margin;

//		drawBitmap(Bitmap bitmap, Rect src, RectF dst, Paint paint)；
//		Rect src: 是对图片进行裁截，若是空null则显示整个图片
//		RectF dst：是图片在Canvas画布中显示的区域，
//		           大于src则把src的裁截区放大，
//		           小于src则把src的裁截区缩小。
//		Rect src = new Rect(0, 0, width, height);
		RectF dst = new RectF(left, 0, right, height);

		canvas.drawBitmap(bitmap, null, dst, paint);

		float textX = left + margin;
		float textY = 40;
		canvas.drawText(time, textX, textY, paint);

		RectF downDst = new RectF((textX + textLength + space),
				(textY - textSize), (right - margin), textY);
		canvas.drawBitmap(downBitmap, null, downDst, paint);
		super.onDraw(canvas);
	}

	private int mLastX = 0;

	public boolean onTouchEvent(MotionEvent event) {

		int x = (int) event.getX();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:
			int deltaX = x - mLastX;

			// if (position > -2 || position < 26) {
			position = position + deltaX;
			// }
			break;
		case MotionEvent.ACTION_UP:
			break;
		}

		mLastX = x;
		postInvalidate();
		return true;
	}
}
