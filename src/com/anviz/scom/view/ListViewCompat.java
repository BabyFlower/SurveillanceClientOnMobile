package com.anviz.scom.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.ListView;
import android.widget.Scroller;

/**
 * 支持子项侧滑的ListView
 * 
 * @author 8444
 * 
 */
public class ListViewCompat extends ListView {

	private ScrollerItemView mFocusedItemView;

	private ScrollerItemView mLastItemView;

	private boolean isX;
	private boolean isY;

	public ListViewCompat(Context context) {
		super(context);
		initView();
	}

	public ListViewCompat(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public ListViewCompat(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	private void initView() {
		mContext = getContext();
		mScroller = new Scroller(mContext);
		mHolderWidth = Math.round(TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, mHolderWidth, getResources()
						.getDisplayMetrics()));
	}

	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			int x = (int) event.getX();
			int y = (int) event.getY();
			int position = pointToPosition(x, y);
			if (position != INVALID_POSITION) {
				Object obj = getChildAt(position - getFirstVisiblePosition());
				if (obj instanceof ScrollerItemView) {
					mFocusedItemView = (ScrollerItemView) obj;
				}
			}

		}
			break;
		default:
			break;
		}

		if (mFocusedItemView != null) {

			onRequireTouchEvent(event);
		}
		if (isX) {
			return false;
		}
		return super.onTouchEvent(event);
	}

	private Context mContext;
	private Scroller mScroller;

	private int mLastX = 0;
	private int mLastY = 0;

	private static final int TAN = 2;
	private int mHolderWidth = 120;

	private void shrink() {
		if (mLastItemView == null) {
			return;
		}
		mLastItemView.shrink();
		mLastItemView = null;

	}

	public void onRequireTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		
		int scrollX = mFocusedItemView.getScrollX();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			shrink();
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			
			int deltaX = x - mLastX;
			int deltaY = y - mLastY;

			if (Math.abs(deltaY) > Math.abs(deltaX) * TAN && isX == false) {
				isY = true;
			}

			if (Math.abs(deltaX) > Math.abs(deltaY) * TAN && isY == false) {
				// 滑动Item时，取消点击功能
				MotionEvent cancelEvent = MotionEvent.obtain(event);
				cancelEvent
						.setAction(MotionEvent.ACTION_CANCEL
								| (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
				onTouchEvent(cancelEvent);
				isX = true;
				int newScrollX = scrollX - deltaX;
				if (deltaX != 0) {
					if (newScrollX < 0) {
						newScrollX = 0;
					} else if (newScrollX > mHolderWidth) {
						newScrollX = mHolderWidth;
					}
					mFocusedItemView.scrollTo(newScrollX, 0);
				}
				
			}

			break;
		}
		case MotionEvent.ACTION_UP: {

			int newScrollX = 0;
			if (scrollX - mHolderWidth * 0.75 > 0) {
				newScrollX = mHolderWidth;
			}
			mFocusedItemView.smoothScrollTo(newScrollX, 0);

			if (newScrollX != 0) {
				mLastItemView = mFocusedItemView;
			}
			isX = false;
			isY = false;
			mFocusedItemView = null;

			break;
		}
		default:
			break;
		}

		mLastX = x;
		mLastY = y;
	}
	
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return super.dispatchTouchEvent(ev);
	}
}
