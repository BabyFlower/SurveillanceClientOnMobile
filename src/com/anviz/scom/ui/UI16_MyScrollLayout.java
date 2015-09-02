package com.anviz.scom.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class UI16_MyScrollLayout extends ViewGroup{
	
	/**当前屏幕位置*/
    private int mCurScreen;
    /**默认屏幕位置*/
    private int mDefaultScreen = 0;   
    /**滑动控制*/
    private Scroller  mScroller;
    /**用于判断甩动手势*/
    private VelocityTracker mVelocityTracker;
    /**上次点下的的x坐标*/
	private float mLastMotionX;
	private static final int SNAP_VELOCITY = 600;
	private UI16_OnViewChangeListener mOnViewChangeListener;
    
	
	public UI16_MyScrollLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public UI16_MyScrollLayout(Context context) {
		super(context);
		init(context);
	}

	public UI16_MyScrollLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public void init(Context context){
		mCurScreen = mDefaultScreen;
		mScroller = new Scroller(context);
	}
	
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int count = getChildCount();
		for(int i = 0; i < count; i++){
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}
		scrollTo(mCurScreen * width, 0);
	}
	
	/**实现ViewGroup类的抽象方法*/
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if(changed){
			int childLeft = 0;
			int childCount = getChildCount();
			for(int i = 0; i < childCount; i++){
				View childView = getChildAt(i);
				int childWidth = childView.getMeasuredWidth();
				childView.layout(childLeft, 0, 
						childLeft + childWidth, childView.getMeasuredHeight());
				childLeft += childWidth;
			}
		}
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		float x = event.getX();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if(mVelocityTracker == null){
				mVelocityTracker = VelocityTracker.obtain();
				mVelocityTracker.addMovement(event);
			}
			if(!mScroller.isFinished()){
				mScroller.abortAnimation();
			}
			mLastMotionX = x;
			break;
		case MotionEvent.ACTION_MOVE:
			int deltaX = (int) (mLastMotionX - x);
			if(IsCanMove(deltaX)){
				if(mVelocityTracker != null){
					mVelocityTracker.addMovement(event);
				}
				mLastMotionX = x;
				scrollBy(deltaX, 0);
			}
			break;
		case MotionEvent.ACTION_UP:
			int velocityX = 0;
			if(mVelocityTracker != null){
				mVelocityTracker.addMovement(event);
				mVelocityTracker.computeCurrentVelocity(1000);
				velocityX = (int) mVelocityTracker.getXVelocity();
			}
			
			if(velocityX > SNAP_VELOCITY && mCurScreen > 0){
				snapToScreen(mCurScreen - 1);
			}else if(velocityX < -SNAP_VELOCITY && 
					mCurScreen < getChildCount() - 1){
				snapToScreen(mCurScreen + 1);
			}else{
				snapToDestination();
			}
			
			if(mVelocityTracker != null){
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			break;
		}
		return true;
	}
	
	private void snapToDestination() {
		final int screenWidth = getWidth();
		final int destScreen = (getScrollX() + screenWidth / 2) / screenWidth;
		snapToScreen(destScreen); 
	}

	private void snapToScreen(int whichScreen) {
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		if(getScrollX() != (whichScreen * getWidth())){
			final int delta = whichScreen * getWidth() - getScrollX();
			mScroller.startScroll(getScrollX(), 0,     
                    delta, 0, Math.abs(delta) * 2);
			mCurScreen = whichScreen;    
            invalidate();
            if(mOnViewChangeListener != null){
            	mOnViewChangeListener.OnViewChange(mCurScreen);
            }
		}
	
	}

	/**
	 * 根据当前X水平坐标与上次X水平坐标值之差，判断是否移动
	 * @param deltaX
	 * @return
	 */
	private boolean IsCanMove(int deltaX){
//		deltaX < 0时往左边拖
		if(getScrollX() <= 0 && deltaX < 0){
			return false;
		}
//		delatX > 0时往右边拖
		if(getScrollX() >= (getChildCount() - 1) * getWidth() && deltaX > 0){
			return false;
		}
		return true;
	}
	
	public void computeScroll() {
		System.out.println("computeScroll()");
		if (mScroller.computeScrollOffset()) {    
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());  
            postInvalidate();    
        }   
	}
	
	public void setOnViewChangeListener(UI16_OnViewChangeListener mOnViewChangeListener) {
		this.mOnViewChangeListener = mOnViewChangeListener;
	}
}