package com.anviz.scom.util;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;

/**
 * 验证码生成类 调用要先cretaeBitmap才会createCode所以想取得验证码字符串。请先createBitmap
 * 
 * @author 8444
 * 
 */
public class AuthCodeUtil {

	/** 验证码包含的字符 */
	private static final char[] CHARS = {'A',
			'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
			'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

	/** 验证码字符长度 */
	private static final int DEFAULT_CODE_LENGTH = 4;
	/** 字体大小 */
	private static final int DEFAULT_FONT_SIZE = 25;
	/** 图片宽 */
	private static final int DEFAULT_WIDTH = 150;
	/** 图片高 */
	private static final int DEFAULT_HEIGHT = 50;

	/** 验证码字符串形式 */
	private String code;

	/** 字符x坐标 */
	private static int padding_left;
	/** 字符y坐标 */
	private int padding_top;

	private Random random = new Random();

	private static AuthCodeUtil bmpCode;

	private AuthCodeUtil() {

	}

	public static AuthCodeUtil getInstance() {
		padding_left = 0;
		if (bmpCode == null)
			bmpCode = new AuthCodeUtil();
		return bmpCode;
	}

	// 验证码图片
	public Bitmap createBitmap() {

		Bitmap bp = Bitmap.createBitmap(DEFAULT_WIDTH, DEFAULT_HEIGHT,
				Config.ARGB_8888);
		Canvas c = new Canvas(bp);

		code = createCode();

		c.drawColor(Color.WHITE);
		Paint paint = new Paint();
		paint.setTextSize(DEFAULT_FONT_SIZE);

		for (int i = 0; i < code.length(); i++) {
			randomTextStyle(paint);
			randomPadding();
			c.drawText(code.charAt(i) + "", padding_left, padding_top, paint);
		}

		// 保存
		c.save(Canvas.ALL_SAVE_FLAG);
		c.restore();
		return bp;
	}

	public String getCode() {
		return code;
	}

	// 验证码
	private String createCode() {
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < DEFAULT_CODE_LENGTH; i++) {
			buffer.append(CHARS[random.nextInt(CHARS.length)]);
		}
		return buffer.toString();
	}

	private int randomColor() {
		return randomColor(1);
	}

	private int randomColor(int rate) {
		int red = random.nextInt(256) / rate;
		int green = random.nextInt(256) / rate;
		int blue = random.nextInt(256) / rate;
		return Color.rgb(red, green, blue);
	}

	private void randomTextStyle(Paint paint) {
		int color = randomColor();
		paint.setColor(color);
		// true为粗体，false为非粗体
		paint.setFakeBoldText(random.nextBoolean());
		float skewX = random.nextInt(11) / 10;
		skewX = random.nextBoolean() ? skewX : -skewX;
		// float类型参数，负数表示右斜，整数左斜
		paint.setTextSkewX(skewX);
	}

	private void randomPadding() {
		padding_left += 25;
		padding_top = 25;
	}
}