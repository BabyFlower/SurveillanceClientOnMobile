package com.anviz.scom.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.anviz.scom.R;
import com.anviz.scom.sqlite.EventSql;
import com.anviz.scom.util.GeneratorContactCountIcon;

/**
 * 用户主界面
 * 
 * @author 8444
 * 
 */
@SuppressWarnings("deprecation")
public class UI06_MainActivity extends Activity {

	/** 页卡内容 */
	private ViewPager mPager;
	/** 当前页卡编号 */
	private LocalActivityManager manager = null;
	/** Tab页面列表 */
	private List<View> listViews;
	private MyPagerAdapter mpAdapter = null;

	private RadioGroup radioGroup;
	private RadioButton eventRb;
	private TextView otherManageTv, queryTv;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui06);

		mPager = (ViewPager) findViewById(R.id.vPager);
		radioGroup = (RadioGroup) this.findViewById(R.id.rg_main_btns);
		otherManageTv = (TextView) findViewById(R.id.ui06_other_manage);
		queryTv = (TextView) findViewById(R.id.ui06_query);
		eventRb = (RadioButton) findViewById(R.id.ui06_event);

		queryTv.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				startActivity(new Intent(UI06_MainActivity.this,
						UI13_EventQueryActivity.class));
			}
		});

		otherManageTv.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				startActivity(new Intent(UI06_MainActivity.this,
						UI21_OtherManageActivity.class));
			}
		});

		manager = new LocalActivityManager(this, true);
		manager.dispatchCreate(savedInstanceState);

		InitViewPager();

		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.ui06_live:
					listViews.set(
							0,
							getView("A", new Intent(UI06_MainActivity.this,
									UI07_LiveListActivity.class)));
					mpAdapter.notifyDataSetChanged();
					mPager.setCurrentItem(0);
					eventCountChange();
					break;

				case R.id.ui06_playback:
					listViews.set(
							1,
							getView("B", new Intent(UI06_MainActivity.this,
									UI08_PlayBackListActivity.class)));
					mpAdapter.notifyDataSetChanged();
					mPager.setCurrentItem(1);
					eventCountChange();
					break;

				case R.id.ui06_event:
					listViews.set(
							2,
							getView("C", new Intent(UI06_MainActivity.this,
									UI09_EventManageActivity.class)));
					mpAdapter.notifyDataSetChanged();
					mPager.setCurrentItem(2);
					eventCountChange();
					break;
				}
			}
		});

	}

	protected void onResume() {
		super.onResume();
		eventCountChange();
	}

	private void eventCountChange() {

		int queue = EventSql.queryUnreadCount(this, null);

		if (queue > 0) {
			if (eventRb.isChecked()) {
				Bitmap bitmap = new GeneratorContactCountIcon().addSum(
						getResources().getDrawable(R.drawable.event_checked),
						queue);
				Drawable d = new BitmapDrawable(getResources(), bitmap);
				eventRb.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
						d, null, null);
			} else {
				Bitmap bitmap = new GeneratorContactCountIcon().addSum(
						getResources().getDrawable(R.drawable.event), queue);
				Drawable d = new BitmapDrawable(getResources(), bitmap);
				eventRb.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
						d, null, null);
			}
		} else {
			eventRb.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.event_bg, 0, 0);
		}

	}

	/**
	 * 初始化ViewPager
	 */
	private void InitViewPager() {
		Intent intent = null;
		listViews = new ArrayList<View>();
		mpAdapter = new MyPagerAdapter(listViews);
		intent = new Intent(this, UI07_LiveListActivity.class);
		listViews.add(getView("A", intent));
		intent = new Intent(this, UI08_PlayBackListActivity.class);
		listViews.add(getView("B", intent));
		intent = new Intent(this, UI09_EventManageActivity.class);
		listViews.add(getView("C", intent));

		mPager.setOffscreenPageLimit(0);
		mPager.setAdapter(mpAdapter);
		mPager.setCurrentItem(0);
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}

	private View getView(String id, Intent intent) {
		return manager.startActivity(id, intent).getDecorView();
	}

	/**
	 * ViewPager适配器
	 * 
	 * @author 8444
	 * 
	 */
	public class MyPagerAdapter extends PagerAdapter {

		public List<View> mListViews;

		public MyPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		public int getCount() {
			return mListViews.size();
		}

		public boolean isViewFromObject(View view, Object obj) {
			return view == (obj);
		}

		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(mListViews.get(position));
		}

		public Object instantiateItem(View container, int position) {
			((ViewPager) container).addView(mListViews.get(position), 0);
			return mListViews.get(position);
		}

	}

	/**
	 * 页卡切换监听，ViewPager改变同样改变TabHost内容
	 * 
	 * @author 8444
	 * 
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {

		boolean isScrolled = false;

		/**
		 * 在状态改变的时候调用，其中state这个参数有三种状态（0，1，2）。 state==1表示正在滑动， state==2表示滑动完毕了，
		 * state==0表示什么都没做。 当页面开始滑动的时候，三种状态的变化顺序为（1，2，0） Called when the scroll
		 * state changes. Useful for discovering when the user begins dragging,
		 * when the pager is automatically settling to the current page, or when
		 * it is fully stopped/idle.
		 * 
		 * @param state
		 *            The new scroll state.
		 * @see ViewPager#SCROLL_STATE_IDLE
		 * @see ViewPager#SCROLL_STATE_DRAGGING
		 * @see ViewPager#SCROLL_STATE_SETTLING
		 */
		public void onPageScrollStateChanged(int state) {
			switch (state) {
			// 手势滑动
			case 1:
				isScrolled = false;
				break;
			case 2:
				// 界面切换
				isScrolled = true;
				break;
			// 滑动结束
			case 0:
				if (mPager.getCurrentItem() == 2) {
					queryTv.setVisibility(View.VISIBLE);
				} else {
					queryTv.setVisibility(View.GONE);
				}
				// 当滑动到顶的时候也就0 跟 mpager.getadapter.getcount -
				// 1的时候再滑不会触发2，没有2就证明到顶了
				if (mPager.getCurrentItem() == mPager.getAdapter().getCount() - 1
						&& !isScrolled) {

					startActivity(new Intent(UI06_MainActivity.this,
							UI21_OtherManageActivity.class));
				}
				break;
			}
		}

		/**
		 * 当页面在滑动的时候会调用此方法，在滑动被停止之前，此方法回一直得到调用。其中三个参数的含义分别为：position
		 * :当前页面，及你点击滑动的页面
		 * 。positionOffset:当前页面偏移的百分比。positionOffsetPixels:当前页面偏移的像素位置 This
		 * method will be invoked when the current page is scrolled, either as
		 * part of a programmatically initiated smooth scroll or a user
		 * initiated touch scroll.
		 * 
		 * @param position
		 *            Position index of the first page currently being
		 *            displayed. Page position+1 will be visible if
		 *            positionOffset is nonzero.
		 * @param positionOffset
		 *            Value from [0, 1) indicating the offset from the page at
		 *            position.
		 * @param positionOffsetPixels
		 *            Value in pixels indicating the offset from position.
		 */
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
		}

		/**
		 * This method will be invoked when a new page becomes selected.
		 * Animation is not necessarily complete.
		 * 
		 * @param position
		 *            Position index of the new selected page.
		 */
		public void onPageSelected(int position) {
			manager.dispatchResume();

			switch (position) {
			case 0:
				radioGroup.check(R.id.ui06_live);
				listViews.set(
						0,
						getView("A", new Intent(UI06_MainActivity.this,
								UI07_LiveListActivity.class)));
				mpAdapter.notifyDataSetChanged();
				break;
			case 1:
				radioGroup.check(R.id.ui06_playback);
				listViews.set(
						1,
						getView("B", new Intent(UI06_MainActivity.this,
								UI08_PlayBackListActivity.class)));
				mpAdapter.notifyDataSetChanged();
				break;
			case 2:
				radioGroup.check(R.id.ui06_event);
				listViews.set(
						2,
						getView("C", new Intent(UI06_MainActivity.this,
								UI09_EventManageActivity.class)));
				mpAdapter.notifyDataSetChanged();
				break;
			}
		}
	}

	/**
	 * 接收事件告警数量改变广播
	 * 后期扩展用：从NVR中取到新的告警处发送更新广播，相关组件接收广播，改变数量
	 * @author 8444
	 * 
	 */
	public class EventChangeCountReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
//			eventCountChange();
		}
	}
}
