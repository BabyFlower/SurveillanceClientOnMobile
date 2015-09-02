/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anviz.scom.zxing.view;

import java.util.Collection;
import java.util.HashSet;

import com.anviz.scom.R;
import com.anviz.scom.zxing.camera.CameraManager;
import com.google.zxing.ResultPoint;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * 覆盖在相机预览。增加了取景器矩形和部分 透明度外,以及激光扫描仪点动画和返回结果。
 */
public final class ViewfinderView extends View {

//	private static final int[] SCANNER_ALPHA = { 0, 64, 128, 192, 255, 192,
//			128, 64 };
	private static final long ANIMATION_DELAY = 100L;
	private static final int OPAQUE = 0xFF;

	private final Paint paint;
	private Bitmap resultBitmap;
	private final int maskColor;
	private final int resultColor;
	private final int frameColor;
	private final int resultPointColor;
//	private int scannerAlpha;
	private Collection<ResultPoint> possibleResultPoints;
	private Collection<ResultPoint> lastPossibleResultPoints;

	/** 中间滑动线的最顶端位置 */
	private int slideTop;
	/** 中间滑动线的最底端位置 */
	private int slideBottom;
	/** 中间那条线每次刷新移动的距离 */
	private static final int SPEEN_DISTANCE = 5;
	private boolean isFirst;
	/** 四个蓝色边角对应的宽度 */
	private static final int CORNER_WIDTH = 16;
	/** 扫描框中的中间线的与扫描框上下的间隙 */
	private static final int MIDDLE_LINE_PADDING = 50;
	/** 四个蓝色边角对应的长度 */
	private int ScreenRate;
	/** 手机的屏幕密度 */
	private static float density;
	/**四角的颜色*/
	private int angleColor;

	// 这个构造函数时使用类是由XML资源。
	public ViewfinderView(Context context, AttributeSet attrs) {
		super(context, attrs);

		density = context.getResources().getDisplayMetrics().density;
		// 将像素转化成dp
		ScreenRate = (int) (25 * density);

		paint = new Paint();
		Resources resources = getResources();
		maskColor = resources.getColor(R.color.viewfinder_mask);
		resultColor = resources.getColor(R.color.result_view);
		frameColor = resources.getColor(R.color.viewfinder_frame);
		resultPointColor = resources.getColor(R.color.possible_result_points);
		angleColor = resources.getColor(R.color.angle);
//		scannerAlpha = 0;
		possibleResultPoints = new HashSet<ResultPoint>(5);
	}

	@Override
	public void onDraw(Canvas canvas) {
		Rect frame = CameraManager.get().getFramingRectInPreview();
		if (frame == null) {
			return;
		}

		if (!isFirst) {
			isFirst = true;
			slideTop = frame.top + CORNER_WIDTH;
			slideBottom = frame.bottom - CORNER_WIDTH;
		}

		int width = canvas.getWidth();
		int height = canvas.getHeight();

		// Draw the exterior (i.e. outside the framing rect) darkened
		paint.setColor(resultBitmap != null ? resultColor : maskColor);
		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
				paint);
		canvas.drawRect(0, frame.bottom + 1, width, height, paint);

		if (resultBitmap != null) {
			// Draw the opaque result bitmap over the scanning rectangle
			paint.setAlpha(OPAQUE);
			canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
		} else {

//			画边框
			paint.setColor(frameColor);
			canvas.drawRect(frame.left, frame.top, frame.right + 1,
					frame.top + 2, paint);
			canvas.drawRect(frame.left, frame.top + 2, frame.left + 2,
					frame.bottom - 1, paint);
			canvas.drawRect(frame.right - 1, frame.top, frame.right + 1,
					frame.bottom - 1, paint);
			canvas.drawRect(frame.left, frame.bottom - 1, frame.right + 1,
					frame.bottom + 1, paint);

			// 画四个角
			paint.setColor(angleColor);
			canvas.drawRect(frame.left - CORNER_WIDTH / 2, frame.top
					- CORNER_WIDTH / 2, frame.left + ScreenRate, frame.top
					+ CORNER_WIDTH / 2, paint);
			canvas.drawRect(frame.left - CORNER_WIDTH / 2, frame.top
					- CORNER_WIDTH / 2, frame.left + CORNER_WIDTH / 2,
					frame.top + ScreenRate, paint);
			canvas.drawRect(frame.left - CORNER_WIDTH / 2, frame.bottom
					- ScreenRate, frame.left + CORNER_WIDTH / 2, frame.bottom
					+ CORNER_WIDTH / 2, paint);
			canvas.drawRect(frame.left - CORNER_WIDTH / 2, frame.bottom
					- CORNER_WIDTH / 2, frame.left + ScreenRate, frame.bottom
					+ CORNER_WIDTH / 2, paint);
			canvas.drawRect(frame.right - ScreenRate, frame.top - CORNER_WIDTH
					/ 2, frame.right + CORNER_WIDTH / 2, frame.top
					+ CORNER_WIDTH / 2, paint);
			canvas.drawRect(frame.right - CORNER_WIDTH / 2, frame.top
					- CORNER_WIDTH / 2, frame.right + CORNER_WIDTH / 2,
					frame.top + ScreenRate, paint);
			canvas.drawRect(frame.right - CORNER_WIDTH / 2, frame.bottom
					- ScreenRate, frame.right + CORNER_WIDTH / 2, frame.bottom
					+ CORNER_WIDTH / 2, paint);
			canvas.drawRect(frame.right - ScreenRate, frame.bottom
					- CORNER_WIDTH / 2, frame.right + CORNER_WIDTH / 2,
					frame.bottom + CORNER_WIDTH / 2, paint);
			// 画中间移动的线
			slideTop += SPEEN_DISTANCE;
			if (slideTop >= slideBottom) {
				slideTop = frame.top + CORNER_WIDTH;
			}

			Rect lineRect = new Rect();
			lineRect.left = frame.left;
			lineRect.right = frame.right;
			lineRect.top = slideTop;
			lineRect.bottom = slideTop + MIDDLE_LINE_PADDING;
			canvas.drawBitmap(((BitmapDrawable) (getResources()
					.getDrawable(R.drawable.qrcode_scan_line))).getBitmap(),
					null, lineRect, paint);

			// 中间画线
//			 paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
//			 scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
//			 int middle = frame.height() / 2 + frame.top;
//			 canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1,
//			 middle + 2, paint);

			Collection<ResultPoint> currentPossible = possibleResultPoints;
			Collection<ResultPoint> currentLast = lastPossibleResultPoints;
			if (currentPossible.isEmpty()) {
				lastPossibleResultPoints = null;
			} else {
				possibleResultPoints = new HashSet<ResultPoint>(5);
				lastPossibleResultPoints = currentPossible;
				paint.setAlpha(OPAQUE);
				paint.setColor(resultPointColor);
				for (ResultPoint point : currentPossible) {
					canvas.drawCircle(frame.left + point.getX(), frame.top
							+ point.getY(), 6.0f, paint);
				}
			}
			if (currentLast != null) {
				paint.setAlpha(OPAQUE / 2);
				paint.setColor(resultPointColor);
				for (ResultPoint point : currentLast) {
					canvas.drawCircle(frame.left + point.getX(), frame.top
							+ point.getY(), 3.0f, paint);
				}
			}

			// Request another update at the animation interval, but only
			// repaint the laser line,
			// not the entire viewfinder mask.
			postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,
					frame.right, frame.bottom);
		}
	}

	public void drawViewfinder() {
		resultBitmap = null;
		invalidate();
	}

	/**
	 * Draw a bitmap with the result points highlighted instead of the live
	 * scanning display.
	 * 
	 * @param barcode
	 *            An image of the decoded barcode.
	 */
	public void drawResultBitmap(Bitmap barcode) {
		resultBitmap = barcode;
		invalidate();
	}

	public void addPossibleResultPoint(ResultPoint point) {
		possibleResultPoints.add(point);
	}

}
