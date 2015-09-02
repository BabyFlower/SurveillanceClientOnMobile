package org.libsdl.app;

import javax.microedition.khronos.egl.*;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.media.*;

/**
 * SDL Activity
 */
public class SDLActivity {

	// Main components
	private static SDLActivity mSingleton;
	private static SDLSurface mSurface;
	private static Handler mHandler;

	// This is what SDL runs in. It invokes SDL_main(), eventually
	private static Thread mSDLThread;
	private static boolean mInit = false;

	// Audio
	private static Thread mAudioThread;
	private static AudioTrack mAudioTrack;

	// EGL private objects
	private static EGLContext mEGLContext;
	private static EGLSurface mEGLSurface;
	private static EGLDisplay mEGLDisplay;
	private static EGLConfig mEGLConfig;
	private static int mGLMajor, mGLMinor;
	private static String mFilename;

	// Load the .so
	static {
		System.loadLibrary("IOTCAPIs");
		System.loadLibrary("RDTAPIs");
		System.loadLibrary("AVAPIs");
		System.loadLibrary("ffmpeg");
		System.loadLibrary("SDL");
		System.loadLibrary("main");
	}

	public SDLActivity(Context context, Handler handler, String filename) {
		mSingleton = this;
		mFilename = filename;
		mSurface = new SDLSurface(context);
		mHandler = handler;

	}

	public SDLSurface getSDLSurface() {
		return mSurface;
	}

	/**取得视频总时长*/
	public int getDuration() {
		return PlayerGetDuration();
	}

	/**取得当前播放进度*/
	public int getCurrentPosition() {
		return PlayergetCurrentPosition();
	}

	public void start() {
		if (isPlaying() == false) {
			PlayerPause();
		}

	}

	public void stop() {
		if (isPlaying() == true) {
			PlayerPause();
		}
	}

	public boolean isPlaying() {
		return PlayerIsPlay() == 1 ? true : false;
	}

	public void seekTo(int msec) {
		PlayerSeekTo(msec);
	}

	public void exit() {
		PlayerExit();

	}

	public void resize(boolean fullscreen) {
	}

	// Events
	public void onPause() {
		// super.onPause();
		Log.v("SDL", "onPause()");
		SDLActivity.nativePause();
	}

	public void onResume() {
		// super.onResume();
		Log.v("SDL", "onResume()");

		SDLActivity.nativeResume();
	}

	public void onDestroy() {
		Log.v("SDL", "onDestroy()");
		// Send a quit message to the application
		SDLActivity.nativeQuit();

		// Now wait for the SDL thread to quit
		if (mSDLThread != null) {
			try {
				mSDLThread.join();
			} catch (Exception e) {
				Log.v("SDL", "Problem stopping thread: " + e);
			}
			mSDLThread = null;

		}
	}

	// Messages from the SDLMain thread
	static int COMMAND_CHANGE_TITLE = 1;

	// Handler for the messages
	static Handler commandHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.arg1 == COMMAND_CHANGE_TITLE) {
				// setTitle((String)msg.obj);
				Log.v("SDL", "Set title");
			}
			if (msg.arg1 == 1000) {
				SDLActivity.notify(1000);
			}
		}
	};

	public static void notify(int msg) {
		Log.v("SDL", "notify :" + msg);
		mHandler.sendEmptyMessage(msg);

	}

	// C functions we call
	// public static native void NotifyInit();
	public static native String getDevList(int index);
	
	public static native void isChange(int width, int height);

	public static native int initTutk(String uid);

	public static native int startDev(int mid);

	public static native void nativeInit();
	
	public static native void nativeQuit();

	public static native void nativePause();

	public static native void nativeResume();

	public static native void onNativeResize(int x, int y, int format);

	public static native void onNativeKeyDown(int keycode);

	public static native void onNativeKeyUp(int keycode);

	public static native void onNativeTouch(int touchDevId,
			int pointerFingerId, int action, float x, float y, float p);

	public static native void onNativeAccel(float x, float y, float z);

	public static native void nativeRunAudioThread();

	public static native int PlayerInit();

	public static native int PlayerPrepare(String url);

	public static native int PlayerMain();

	public static native int PlayerExit();

	public static native int PlayerSeekTo(int msec);

	public static native int PlayerPause();

	public static native int PlayerIsPlay();

	public static native int PlayerGetDuration();

	public static native int PlayergetCurrentPosition();
	
	/**将JSON字符串命令传给JNI,返回JSON形式字符串*/
	public native static String visit(String comm);

	// Java functions called from C

	public static boolean createGLContext(int majorVersion, int minorVersion) {
		return initEGL(majorVersion, minorVersion);
	}

	public static void flipBuffers() {
		flipEGL();
	}

	public static void setActivityTitle(String title) {
		// Called from SDLMain() thread and can't directly affect the view
		// mSingleton.sendCommand(COMMAND_CHANGE_TITLE, title);
	}

	// public static Context getContext() {
	// return mSingleton;
	// }

	public static void startApp() {
		// Start up the C app thread
		// PlayerInit();
		if (mSDLThread == null) {
			PlayerInit();

			mSDLThread = new Thread(new SDLMain(mFilename), "SDLThread");
			mSDLThread.start();

		} else {
			SDLActivity.nativeResume();
		}
	}

	// EGL functions
	public static boolean initEGL(int majorVersion, int minorVersion) {
		if (SDLActivity.mEGLDisplay == null) {
			// Log.v("SDL", "Starting up OpenGL ES " + majorVersion + "." +
			// minorVersion);

			try {
				EGL10 egl = (EGL10) EGLContext.getEGL();

				EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

				int[] version = new int[2];
				egl.eglInitialize(dpy, version);

				int EGL_OPENGL_ES_BIT = 1;
				int EGL_OPENGL_ES2_BIT = 4;
				int renderableType = 0;
				if (majorVersion == 2) {
					renderableType = EGL_OPENGL_ES2_BIT;
				} else if (majorVersion == 1) {
					renderableType = EGL_OPENGL_ES_BIT;
				}
				int[] configSpec = {
						// EGL10.EGL_DEPTH_SIZE, 16,
						EGL10.EGL_RENDERABLE_TYPE, renderableType,
						EGL10.EGL_NONE };
				EGLConfig[] configs = new EGLConfig[1];
				int[] num_config = new int[1];
				if (!egl.eglChooseConfig(dpy, configSpec, configs, 1,
						num_config) || num_config[0] == 0) {
					Log.e("SDL", "No EGL config available");
					return false;
				}
				EGLConfig config = configs[0];

				/*
				 * int EGL_CONTEXT_CLIENT_VERSION=0x3098; int contextAttrs[] =
				 * new int[] { EGL_CONTEXT_CLIENT_VERSION, majorVersion,
				 * EGL10.EGL_NONE }; EGLContext ctx = egl.eglCreateContext(dpy,
				 * config, EGL10.EGL_NO_CONTEXT, contextAttrs);
				 * 
				 * if (ctx == EGL10.EGL_NO_CONTEXT) { Log.e("SDL",
				 * "Couldn't create context"); return false; }
				 * SDLActivity.mEGLContext = ctx;
				 */
				SDLActivity.mEGLDisplay = dpy;
				SDLActivity.mEGLConfig = config;
				SDLActivity.mGLMajor = majorVersion;
				SDLActivity.mGLMinor = minorVersion;

				SDLActivity.createEGLSurface();
			} catch (Exception e) {
				Log.v("SDL", e + "");
				for (StackTraceElement s : e.getStackTrace()) {
					Log.v("SDL", s.toString());
				}
			}
		} else
			SDLActivity.createEGLSurface();

		return true;
	}

	public static boolean createEGLContext() {
		EGL10 egl = (EGL10) EGLContext.getEGL();
		int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
		int contextAttrs[] = new int[] { EGL_CONTEXT_CLIENT_VERSION,
				SDLActivity.mGLMajor, EGL10.EGL_NONE };
		SDLActivity.mEGLContext = egl.eglCreateContext(SDLActivity.mEGLDisplay,
				SDLActivity.mEGLConfig, EGL10.EGL_NO_CONTEXT, contextAttrs);
		if (SDLActivity.mEGLContext == EGL10.EGL_NO_CONTEXT) {
			Log.e("SDL", "Couldn't create context");
			return false;
		}
		return true;
	}

	public static boolean createEGLSurface() {
		if (SDLActivity.mEGLDisplay != null && SDLActivity.mEGLConfig != null) {
			EGL10 egl = (EGL10) EGLContext.getEGL();
			if (SDLActivity.mEGLContext == null)
				createEGLContext();

			Log.v("SDL", "Creating new EGL Surface");
			EGLSurface surface = egl.eglCreateWindowSurface(
					SDLActivity.mEGLDisplay, SDLActivity.mEGLConfig,
					SDLActivity.mSurface, null);
			if (surface == EGL10.EGL_NO_SURFACE) {
				Log.e("SDL", "Couldn't create surface");
				return false;
			}

			if (!egl.eglMakeCurrent(SDLActivity.mEGLDisplay, surface, surface,
					SDLActivity.mEGLContext)) {
				Log.e("SDL",
						"Old EGL Context doesnt work, trying with a new one");
				createEGLContext();
				if (!egl.eglMakeCurrent(SDLActivity.mEGLDisplay, surface,
						surface, SDLActivity.mEGLContext)) {
					Log.e("SDL", "Failed making EGL Context current");
					return false;
				}
			}
			SDLActivity.mEGLSurface = surface;
			return true;
		}
		return false;
	}

	// EGL buffer flip
	public static void flipEGL() {
		try {
			EGL10 egl = (EGL10) EGLContext.getEGL();

			egl.eglWaitNative(EGL10.EGL_CORE_NATIVE_ENGINE, null);

			// drawing here

			egl.eglWaitGL();

			egl.eglSwapBuffers(SDLActivity.mEGLDisplay, SDLActivity.mEGLSurface);

		} catch (Exception e) {
			Log.v("SDL", "flipEGL(): " + e);
			for (StackTraceElement s : e.getStackTrace()) {
				Log.v("SDL", s.toString());
			}
		}
	}

	// Audio
	private static Object buf;

	public static Object audioInit(int sampleRate, boolean is16Bit,
			boolean isStereo, int desiredFrames) {
		@SuppressWarnings("deprecation")
		int channelConfig = isStereo ? AudioFormat.CHANNEL_CONFIGURATION_STEREO
				: AudioFormat.CHANNEL_CONFIGURATION_MONO;
		int audioFormat = is16Bit ? AudioFormat.ENCODING_PCM_16BIT
				: AudioFormat.ENCODING_PCM_8BIT;
		int frameSize = (isStereo ? 2 : 1) * (is16Bit ? 2 : 1);

		Log.v("SDL", "SDL audio: wanted " + (isStereo ? "stereo" : "mono")
				+ " " + (is16Bit ? "16-bit" : "8-bit") + " "
				+ ((float) sampleRate / 1000f) + "kHz, " + desiredFrames
				+ " frames buffer");

		// Let the user pick a larger buffer if they really want -- but ye
		// gods they probably shouldn't, the minimums are horrifyingly high
		// latency already
		desiredFrames = Math.max(
				desiredFrames,
				(AudioTrack.getMinBufferSize(sampleRate, channelConfig,
						audioFormat) + frameSize - 1)
						/ frameSize);

		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
				channelConfig, audioFormat, desiredFrames * frameSize,
				AudioTrack.MODE_STREAM);

		audioStartThread();

		Log.v("SDL",
				"SDL audio: got "
						+ ((mAudioTrack.getChannelCount() >= 2) ? "stereo"
								: "mono")
						+ " "
						+ ((mAudioTrack.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) ? "16-bit"
								: "8-bit") + " "
						+ ((float) mAudioTrack.getSampleRate() / 1000f)
						+ "kHz, " + desiredFrames + " frames buffer");

		if (is16Bit) {
			buf = new short[desiredFrames * (isStereo ? 2 : 1)];
		} else {
			buf = new byte[desiredFrames * (isStereo ? 2 : 1)];
		}
		return buf;
	}

	public static void audioStartThread() {
		mAudioThread = new Thread(new Runnable() {
			public void run() {
				mAudioTrack.play();
				nativeRunAudioThread();
			}
		});

		// I'd take REALTIME if I could get it!
		mAudioThread.setPriority(Thread.MAX_PRIORITY);
		mAudioThread.start();
	}

	public static void audioWriteShortBuffer(short[] buffer) {
		for (int i = 0; i < buffer.length;) {
			int result = mAudioTrack.write(buffer, i, buffer.length - i);
			if (result > 0) {
				i += result;
			} else if (result == 0) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// Nom nom
				}
			} else {
				Log.w("SDL", "SDL audio: error return from write(short)");
				return;
			}
		}
	}

	public static void audioWriteByteBuffer(byte[] buffer) {
		for (int i = 0; i < buffer.length;) {
			int result = mAudioTrack.write(buffer, i, buffer.length - i);
			if (result > 0) {
				i += result;
			} else if (result == 0) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// Nom nom
				}
			} else {
				Log.w("SDL", "SDL audio: error return from write(short)");
				return;
			}
		}
	}

	public static void audioQuit() {
		if (mAudioThread != null) {
			try {
				mAudioThread.join();
			} catch (Exception e) {
				Log.v("SDL", "Problem stopping audio thread: " + e);
			}
			mAudioThread = null;

			// Log.v("SDL", "Finished waiting for audio thread");
		}

		if (mAudioTrack != null) {
			mAudioTrack.stop();
			mAudioTrack = null;
		}
	}
}

/**
 * Simple nativeInit() runnable
 */
class SDLMain implements Runnable {
	private String mFile;

	SDLMain(String f) {
		mFile = f;
	}

	public void run() {
		SDLActivity.nativeInit();
		SDLActivity.PlayerPrepare(mFile);
		SDLActivity.PlayerMain();
	}
}
