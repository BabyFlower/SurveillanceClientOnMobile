package com.anviz.scom.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * 自定义滑动子项
 * @author 8444
 *
 */
public class ScrollerItemView extends LinearLayout{

	private Scroller mScroller;
	private Context mContext;
	
	public ScrollerItemView(Context context) {
		super(context);
		initView();
	}

	public ScrollerItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	public ScrollerItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}
	
	private void initView(){
		mContext = getContext();
		mScroller = new Scroller(mContext);
	}
	
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	}
	
	public void smoothScrollTo(int destX, int destY) {
		// 缓慢滚动到指定位置
		int scrollX = getScrollX();
		int delta = destX - scrollX;
		mScroller.startScroll(scrollX, 0, delta, 0, Math.abs(delta) * 3);
		invalidate();
	}
	
	/**
	 * 调用该方法，将右侧组件滑动隐藏
	 */
	public void shrink() {
		if (getScrollX() != 0) {
			smoothScrollTo(0, 0);
		}
	}
}
