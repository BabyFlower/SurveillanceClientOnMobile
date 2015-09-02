package com.anviz.scom.ui;

import org.libsdl.app.SDLActivity;
import org.libsdl.app.SDLSurface;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.anviz.scom.R;

/**
 * 播放UI
 * 
 * @author 8444
 * 
 */
@SuppressWarnings("deprecation")
public class UI03_AnvizPlayerActivity extends Activity implements
		OnTouchListener, OnGestureListener {
	PowerManager powerManager = null;
	WakeLock wakeLock = null;

	private SDLActivity mSoftPlayer = null;
	private View mHideContainer;
	private SeekBar mSeekBar;
	private TextView currentTime, totalTime, centerTv;

	private String fileName = "/sdcard/test.mp4";
	// private String fileName = "";
	private int totalDuration = 0;
	private Handler handler;
	private SeekUpdater seekUpdater = null;

	public final int MSG_LOAD_FINISHED = 10;
	public final int MSG_LOAD_UNFINISHED = 11;
	public final int MSG_OPEN_ERROR = 12;
	public final int MSG_OPEN_OK = 13;
	public final int MSG_SEEK_UPDATE = 30;

	FrameLayout frameContainer;

	/** 播放区域上面的Layout */
	private LinearLayout topLin;

	/** 计时器 */
	private Chronometer ch;

	private SDLSurface surface;

	private GestureDetector mGestureDetector;

	private LayoutParams layoutlp;

	/**
	 * 播放暂停，本地录像，设备快照，PTZ控制，麦克风控制，喇叭控制，剪辑，分享，删除
	 */
	private ImageView mediaStopIv, localVideoIv, snaphotIv, ptzIv,
			microphoneIv, frequencyIv, editingIv, shareIv, deleteIv;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui03);

		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = 0.5f;
		beforeBrightness = lp.screenBrightness;

		powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
				"My Lock");

		audioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
		beforeFlingVolume = audioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);

		frameContainer = (FrameLayout) findViewById(R.id.framecontainer);
		topLin = (LinearLayout) findViewById(R.id.ui03_top);
		ch = (Chronometer) findViewById(R.id.ui03_ch);
		mGestureDetector = new GestureDetector(this);
		centerTv = (TextView) findViewById(R.id.ui03_center_tv);

		frameContainer.setOnTouchListener(this);

		findViews();

		ch.setOnChronometerTickListener(new OnChronometerTickListener() {
			public void onChronometerTick(Chronometer ch) {
				if (SystemClock.elapsedRealtime() - ch.getBase() > 3 * 1000) {
					mHideContainer.setVisibility(View.INVISIBLE);
					if (seekUpdater != null) {
						seekUpdater.stopIt();
					}
				}
			}
		});
		chStart();

		handler = new Handler() {
			public void handleMessage(Message msg) {
				if (!Thread.currentThread().isInterrupted()) {
					switch (msg.what) {
					case MSG_OPEN_OK:
						startPlayer();
						break;
					case MSG_OPEN_ERROR:
						break;
					case MSG_LOAD_FINISHED:
						break;
					case MSG_LOAD_UNFINISHED:
						break;
					case MSG_SEEK_UPDATE:
						if (seekUpdater != null)
							seekUpdater.refresh();
						break;
					}
				}
				super.handleMessage(msg);
			}
		};

		Uri tmpUri = (Uri) this.getIntent().getData();
		if (tmpUri != null) {
			fileName = tmpUri.getPath();
		} else {
			Bundle bundle = this.getIntent().getExtras();
			if (bundle != null) {
				String path = bundle.getString("PATH");

				if (path != null) {
					fileName = path;
				}
			}
		}
		mSoftPlayer = new SDLActivity(getApplication(), handler, fileName);

		surface = mSoftPlayer.getSDLSurface();
		frameContainer.addView(surface);

		changeLayout();
	}

	/**
	 * 改变播放区域 初始化调用，屏幕横竖屏切换调用
	 */
	public void changeLayout() {
		// 取得窗口管理
		WindowManager mWindowManager = getWindowManager();
		// 取得属性
		Display windowDisplay = mWindowManager.getDefaultDisplay();
		layoutlp = frameContainer.getLayoutParams();

		layoutlp.width = windowDisplay.getWidth();
		layoutlp.height = windowDisplay.getWidth() * windowDisplay.getWidth()
				/ windowDisplay.getHeight();
		if (windowDisplay.getHeight() > windowDisplay.getWidth()) {
			layoutlp.width = windowDisplay.getWidth();
			layoutlp.height = windowDisplay.getWidth()
					* windowDisplay.getWidth() / windowDisplay.getHeight();
			if (topLin.getVisibility() == View.GONE) {
				topLin.setVisibility(View.VISIBLE);
			}
		} else {
			layoutlp.width = windowDisplay.getWidth();
			layoutlp.height = windowDisplay.getHeight();
			if (topLin.getVisibility() == View.VISIBLE) {
				topLin.setVisibility(View.GONE);
			}
		}
		// 将改变后的宽和高传递给JNI
		SDLActivity.isChange(layoutlp.width, layoutlp.height);
	}

	public void startPlayer() {
		totalDuration = mSoftPlayer.getDuration();
		totalTime.setText(formatTime(totalDuration / 1000));
		if (seekUpdater == null) {
			seekUpdater = new SeekUpdater();
			seekUpdater.startIt();
		}
	}

	public String formatTime(long sec) {
		int h = (int) sec / 3600;
		int m = (int) (sec % 3600) / 60;
		int s = (int) sec % 60;

		if (h == 0) {
			return String.format("%02d:%02d", m, s);
		} else {
			return String.format("%d:%02d:%02d", h, m, s);
		}
	}

	public void findViews() {
		findViewById(R.id.ui03_back).setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				finish();
			}
		});

		mSeekBar = (SeekBar) findViewById(R.id.progressbar);
		currentTime = (TextView) findViewById(R.id.currenttime);
		totalTime = (TextView) findViewById(R.id.totaltime);

		mediaStopIv = (ImageView) findViewById(R.id.ui03_media_stop);
		mediaStopIv.setOnClickListener(imgPlayListener);
		snaphotIv = (ImageView) findViewById(R.id.ui03_snapshot);
		snaphotIv.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {

			}
		});
		mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
		mHideContainer = findViewById(R.id.hidecontainer);
		frameContainer.setOnClickListener(mVisibleListener);

		localVideoIv = (ImageView) findViewById(R.id.ui03_local_video);
		ptzIv = (ImageView) findViewById(R.id.ui03_ptz);
		microphoneIv = (ImageView) findViewById(R.id.ui03_microphone);
		frequencyIv = (ImageView) findViewById(R.id.ui03_frequency);
		editingIv = (ImageView) findViewById(R.id.ui03_editing);
		shareIv = (ImageView) findViewById(R.id.ui03_share);
		deleteIv = (ImageView) findViewById(R.id.ui03_delete);

		int type = getIntent().getIntExtra("type", 0);
		switch (type) {
		// 实时
		case 0:
			// findViewById(R.id.ui03_bar_ll).setVisibility(View.VISIBLE);
			mediaStopIv.setVisibility(View.VISIBLE);
			localVideoIv.setVisibility(View.VISIBLE);
			snaphotIv.setVisibility(View.VISIBLE);
			ptzIv.setVisibility(View.VISIBLE);
			microphoneIv.setVisibility(View.VISIBLE);
			frequencyIv.setVisibility(View.VISIBLE);
			break;
		// 远程录像
		case 1:
			findViewById(R.id.ui03_sv).setVisibility(View.VISIBLE);
			mediaStopIv.setVisibility(View.VISIBLE);
			snaphotIv.setVisibility(View.VISIBLE);
			editingIv.setVisibility(View.VISIBLE);
			frequencyIv.setVisibility(View.VISIBLE);
			break;
		// 本地录像
		case 2:
			mediaStopIv.setVisibility(View.VISIBLE);
			snaphotIv.setVisibility(View.VISIBLE);
			shareIv.setVisibility(View.VISIBLE);
			deleteIv.setVisibility(View.VISIBLE);
			break;
		}

	}

	OnClickListener imgPlayListener = new OnClickListener() {
		public void onClick(View v) {
			ImageView img = (ImageView) v;

			SDLActivity sp = mSoftPlayer;

			if (sp != null) {
				if (sp.isPlaying()) {
					img.setImageResource(R.drawable.live_checked);
					sp.stop();
				} else {
					img.setImageResource(R.drawable.pause);
					sp.start();
				}
			}
		}
	};

	OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {
		public void onStopTrackingTouch(SeekBar seekBar) {
			int totalTime, seekTo = 0;
			int progress = seekBar.getProgress();
			SDLActivity sp = mSoftPlayer;
			if (sp != null) {
				totalTime = sp.getDuration();
				seekTo = totalTime / 1000 * progress;
				sp.seekTo(seekTo);

			}
		}

		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {

		}

		public void onStartTrackingTouch(SeekBar seekBar) {

		}
	};

	OnClickListener mVisibleListener = new OnClickListener() {
		public void onClick(View v) {

			if ((mHideContainer.getVisibility() == View.GONE)
					|| (mHideContainer.getVisibility() == View.INVISIBLE)) {
				chStart();
				if (seekUpdater != null) {
					seekUpdater.startIt();
					seekUpdater.refresh();
				}
				mHideContainer.setVisibility(View.VISIBLE);
			} else {
				mHideContainer.setVisibility(View.INVISIBLE);
				if (seekUpdater != null)
					seekUpdater.stopIt();
			}
		}
	};

	protected void onPause() {
		super.onPause();
		mSoftPlayer.exit();
		mSoftPlayer.onPause();
	}

	protected void onResume() {
		wakeLock.acquire();
		super.onResume();

	}

	protected void onDestroy() {
		super.onDestroy();
		mSoftPlayer.onDestroy();
		wakeLock.release();
	}

	private class SeekUpdater {

		public void startIt() {
			handler.sendEmptyMessage(MSG_SEEK_UPDATE);
		}

		public void stopIt() {
			handler.removeMessages(MSG_SEEK_UPDATE);
		}

		public void refresh() {
			SDLActivity sp = mSoftPlayer;
			if (currentTime != null) {
				long playedDuration = 1;

				if (sp != null)
					playedDuration = sp.getCurrentPosition();
				currentTime.setText(formatTime(playedDuration / 1000));
				if (totalDuration != 0) {
					int progress = (int) ((1000 * playedDuration) / totalDuration);
					mSeekBar.setProgress(progress);
				}
			}
			handler.sendEmptyMessageDelayed(MSG_SEEK_UPDATE, 1000);
		}
	}

	/**
	 * 开始计时
	 */
	private void chStart() {
		// 设置开始计时时间
		ch.setBase(SystemClock.elapsedRealtime());
		// 启动计时器
		ch.start();
	}

	/** 滑动前亮度 */
	private float beforeBrightness;

	/**
	 * 调节亮度
	 * 
	 * @param value
	 */
	private void adjustBrightness(float value) {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = beforeBrightness - value / 500;

		if (lp.screenBrightness > 1) {
			lp.screenBrightness = 1f;
		}

		if (lp.screenBrightness < 0) {
			lp.screenBrightness = 0f;
		}
		getWindow().setAttributes(lp);

		centerTv.setText((int) (lp.screenBrightness * 100) + "%");
		centerTv.setCompoundDrawablesWithIntrinsicBounds(0,
				R.drawable.play_gesture_brightness, 0, 0);
	}

	private AudioManager audioManager;
	/** 滑动前的音量 */
	private int beforeFlingVolume;

	/**
	 * 调节音量
	 * 
	 * @param value
	 */
	private void adjustVolume(float value) {
		int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float cell = 100 / max;

		// 滑动音量的百分比=滑动前+滑动的百分比，每5个滑动单位算一个百分比
		float current = (int) (beforeFlingVolume * cell - value / 5);
		centerTv.setCompoundDrawablesWithIntrinsicBounds(0,
				R.drawable.play_gesture_volume, 0, 0);
		if (current < 0) {
			centerTv.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.play_gesture_volume_no, 0, 0);
			current = 0;
		}
		if (current > 100) {
			current = 100;
		}

		int currentVolume = audioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);

		if (current / 100 * max >= currentVolume + 1) {
			// 第一个参数：声音类型
			// 第二个参数：调整音量的方向 + -
			// 第三个参数：可选的标志位 FLAG_SHOW_UI可见系统音量进度条 0为不可见
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_RAISE, 0);
		}

		if (current / 100 * max <= currentVolume - 1) {
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_LOWER, 0);
		}

		centerTv.setText((int) current + "%");
	}

	/** 滑动前的进度 */
	private int beforeFlingSeek = -1;
	private int seek;

	/**
	 * 滑动快进快退
	 * 
	 * @param value
	 */
	private void flingSeek(float value) {

		SDLActivity sp = mSoftPlayer;
		if (sp != null) {
			if (beforeFlingSeek == -1) {
				beforeFlingSeek = sp.getCurrentPosition();
			}

			seek = (int) (beforeFlingSeek + value / 5 * 1000);
			if (value > 0) {
				centerTv.setCompoundDrawablesWithIntrinsicBounds(0,
						R.drawable.play_gesture_forward, 0, 0);
			} else {
				centerTv.setCompoundDrawablesWithIntrinsicBounds(0,
						R.drawable.play_gesture_rewind, 0, 0);
			}
			if (seek < 0) {
				seek = 0;
			}
			if (seek > sp.getDuration()) {
				seek = sp.getDuration();
			}
			centerTv.setText(formatTime(seek / 1000) + "/"
					+ formatTime(sp.getDuration() / 1000));
		}
	}

	/**
	 * 横竖屏切换回调方法
	 */
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		changeLayout();
	}

	public boolean onTouch(View v, MotionEvent event) {
		// 屏幕结束滑动
		if (event.getAction() == MotionEvent.ACTION_UP) {
			// 中间文本不显示
			centerTv.setVisibility(View.GONE);
			// 当前亮度值设为当前屏幕亮度
			WindowManager.LayoutParams lp = getWindow().getAttributes();
			beforeBrightness = lp.screenBrightness;
			// 滑动前的音量
			beforeFlingVolume = audioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC);
			// 恢复没有横向也没有纵向滑动记录
			isVertical = false;
			if (isHorizontal) {
				isHorizontal = false;
				SDLActivity sp = mSoftPlayer;
				if (sp != null) {
					sp.seekTo(seek);
				}
			}
			// 滑动前的进度
			beforeFlingSeek = -1;
		}
		return mGestureDetector.onTouchEvent(event);
	}

	public boolean onDown(MotionEvent e) {
		return false;
	}

	public void onShowPress(MotionEvent e) {

	}

	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	private boolean isVertical = false;
	private boolean isHorizontal = false;

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		if (centerTv.getVisibility() == View.GONE) {
			centerTv.setVisibility(View.VISIBLE);
		}

		// 如果滑动的X是Y的两倍，则认为是水平滑动
		if (Math.abs(e2.getX() - e1.getX()) / Math.abs(e2.getY() - e1.getY()) > 2
				&& !isVertical) {
			float valueX = e2.getX() - e1.getX();
			isHorizontal = true;
			flingSeek(valueX);
			return true;
		}

		// 如果滑动的Y是X的两倍，则认为是垂直滑动
		if (Math.abs(e2.getY() - e1.getY()) / Math.abs(e2.getX() - e1.getX()) > 2
				&& !isHorizontal) {
			isVertical = true;
			float valueY = e2.getY() - e1.getY();
			if (e2.getX() < layoutlp.width / 2) {
				adjustBrightness(valueY);
			} else {
				adjustVolume(valueY);
			}
			return true;
		}
		return false;
	}

	public void onLongPress(MotionEvent e) {

	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}
}
