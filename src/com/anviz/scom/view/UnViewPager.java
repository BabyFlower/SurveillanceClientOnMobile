package com.anviz.scom.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 屏蔽左右滑动的ViewPager
 * @author 8444
 *
 */
public class UnViewPager extends ViewPager{
	private boolean isScrollable;

	public UnViewPager(Context context) {
		super(context);
	}

	public UnViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isScrollable == false) {
            return false;
        } else {
            return super.onTouchEvent(ev);
        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isScrollable == false) {
            return false;
        } else {
            return super.onInterceptTouchEvent(ev);
        }

    }

    public boolean isScrollable() {
        return isScrollable;
    }

    public void setScrollable(boolean isScrollable) {
        this.isScrollable = isScrollable;
    }
}
