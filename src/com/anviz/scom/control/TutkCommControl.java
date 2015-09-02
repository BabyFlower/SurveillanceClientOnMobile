package com.anviz.scom.control;

import org.json.JSONException;

import com.anviz.scom.comm.TutkAudioGetConfig;
import com.anviz.scom.comm.TutkAudioMicStart;
import com.anviz.scom.comm.TutkAudioMicStop;
import com.anviz.scom.comm.TutkAudioSpeakerStart;
import com.anviz.scom.comm.TutkAudioSpeakerStop;
import com.anviz.scom.comm.TutkRecordAudioStart;
import com.anviz.scom.comm.TutkRecordAudioStop;
import com.anviz.scom.comm.TutkRecordBack;
import com.anviz.scom.comm.TutkRecordFast;
import com.anviz.scom.comm.TutkRecordJump;
import com.anviz.scom.comm.TutkRecordList;
import com.anviz.scom.comm.TutkRecordNext;
import com.anviz.scom.comm.TutkRecordPause;
import com.anviz.scom.comm.TutkRecordPrev;
import com.anviz.scom.comm.TutkRecordSelectFile;
import com.anviz.scom.comm.TutkRecordSlow;
import com.anviz.scom.comm.TutkRecordStart;
import com.anviz.scom.comm.TutkRecordStop;
import com.anviz.scom.comm.TutkSysGetDevInfo;
import com.anviz.scom.comm.TutkVideoGetConfig;
import com.anviz.scom.comm.TutkVideoPtzCmd;
import com.anviz.scom.comm.TutkVideoStart;
import com.anviz.scom.comm.TutkVideoStop;

/**
 * tutk命令控制
 * 
 * @author 8444
 * 
 */
public class TutkCommControl {

	/** 获取设备系统信息 */
	public static final String SYS_GET_DEVINFO = "SYS_GET_DEVINFO";

	/** 播放特定通道的特定码流 */
	public static final String VIDEO_START = "VIDEO_START";
	/** 停止特定通道的特定码流 */
	public static final String VIDEO_STOP = "VIDEO_STOP";
	/** 获取指定通道的编码参数 */
	public static final String VIDEO_GET_CONFIG = "VIDEO_GET_CONFIG";
	/** PDZ控制命令 */
	public static final String VIDEO_PTZ_CMD = "VIDEO_PTZ_CMD";

	/** 启动特定通道的mic */
	public static final String AUDIO_MIC_START = "AUDIO_MIC_START";
	/** 停止特定通道的mic */
	public static final String AUDIO_MIC_STOP = "AUDIO_MIC_STOP";
	/** 开启特定通道的speaker */
	public static final String AUDIO_SPEAKER_START = "AUDIO_SPEAKER_START";
	/** 停止特定通道的mic */
	public static final String AUDIO_SPEAKER_STOP = "AUDIO_SPEAKER_STOP";
	/** 获取指定通道的音频配置 */
	public static final String AUDIO_GET_CONFIG = "AUDIO_GET_CONFIG";

	/** 启动特定通道的录像 */
	public static final String RECORD_START = "RECORD_START";
	/** 停止特定通道的录像 */
	public static final String RECORD_STOP = "RECORD_STOP";
	/** 暂停特定通道的录像 */
	public static final String RECORD_PAUSE = "RECORD_PAUSE";
	/** 启动特定通道的音频 */
	public static final String RECORD_AUDIO_START = "RECORD_AUDIO_START";
	/** 停止特定通道的音频 */
	public static final String RECORD_AUDIO_STOP = "RECORD_AUDIO_STOP";
	/** 获取特定通道录像文件列表 */
	public static final String RECORD_LIST = "RECORE_LIST";
	/** 选定一个录像文件 */
	public static final String RECORD_SELECT_FILE = "RECORD_SELECT_FILE";
	/** 录像快放 */
	public static final String RECORD_FAST = "RECORD_FAST";
	/** 录像慢放 */
	public static final String RECORD_SLOW = "RECORD_SLOW";
	/** 录像向前跳转 */
	public static final String RECORD_PREV = "RECORD_PREV";
	/** 录像向后跳转 */
	public static final String RECORD_NEXT = "RECORD_NEXT";
	/** 录像倒放 */
	public static final String RECORD_BACK = "RECORD_BACK";
	/** 录像拖拽 */
	public static final String RECORD_JUMP = "RECORD_JUMP";

	
	
	/** 报警信息 */
	public static final String ALARM_INFO = "ALARM_INFO";

	/**
	 * 获取设备系统信息
	 * 
	 * @return
	 * @throws JSONException
	 */
	public static Object sysGetDevInfo() throws JSONException {
		return new TutkSysGetDevInfo().getParse();
	}

	/**
	 * 播放特定通道的特定码流
	 * 
	 * @param channel
	 *            设备的通道号，0开始
	 * @param streamtype
	 *            指定通道的第几个码流，0开始
	 * @return 返回值0成功，其他错误码
	 * @throws JSONException
	 */
	public static Integer videoStart(String channel, String streamtype)
			throws JSONException {
		return (Integer) new TutkVideoStart(channel, streamtype).getParse();
	}

	/**
	 * 停止特定通道的特定码流
	 * 
	 * @param channel
	 *            设备的通道号，0开始
	 * @param streamtype
	 *            指定通道的第几个码流，0开始
	 * @return 返回值0成功，其他错误码
	 * @throws JSONException
	 */
	public static Integer videoStop(String channel, String streamtype)
			throws JSONException {
		return (Integer) new TutkVideoStop(channel, streamtype).getParse();
	}

	/**
	 * 获取设备系统信息
	 * 
	 * @param channel
	 *            设备的通道号，0开始
	 * @param streamtype
	 *            指定通道的第几个码流，0开始
	 * @return
	 * @throws JSONException
	 */
	public static Object videoGetConfig(String channel, String streamtype)
			throws JSONException {
		return new TutkVideoGetConfig(channel, streamtype).getParse();
	}

	/**
	 * Ptz控制
	 * 
	 * @param channel
	 *            设备的通道号，0开始
	 * @param cmdvalue
	 *            ptz的具体操作
	 * @return 0 成功，大于0失败，数值代码错误码
	 * @throws JSONException
	 */
	public static Integer videoPtzCmd(String channel, int cmdvalue)
			throws JSONException {
		return (Integer) new TutkVideoPtzCmd(channel, cmdvalue).getParse();
	}

	/**
	 * 启动特定通道的mic
	 * 
	 * @param channel
	 *            设备的通道号，0开始
	 * @return 返回值0成功，其他错误码
	 * @throws JSONException
	 */
	public static Integer audioMicStart(String channel) throws JSONException {
		return (Integer) new TutkAudioMicStart(channel).getParse();
	}

	/**
	 * 停止特定通道的mic
	 * 
	 * @param channel
	 *            设备的通道号，0开始
	 * @return 返回值0成功，其他错误码
	 * @throws JSONException
	 */
	public static Integer audioMicStop(String channel) throws JSONException {
		return (Integer) new TutkAudioMicStop(channel).getParse();
	}

	/**
	 * 开启特定通道的speaker
	 * 
	 * @param channel
	 *            设备的通道号，0开始
	 * @return 返回值0成功，其他错误码
	 * @throws JSONException
	 */
	public static Integer audioSpeakerStart(String channel)
			throws JSONException {
		return (Integer) new TutkAudioSpeakerStart(channel).getParse();
	}

	/**
	 * 停止特定通道的speaker
	 * 
	 * @param channel
	 *            设备的通道号，0开始
	 * @return 返回值0成功，其他错误码
	 * @throws JSONException
	 */
	public static Integer audioSpeakerStop(String channel) throws JSONException {
		return (Integer) new TutkAudioSpeakerStop(channel).getParse();
	}

	/**
	 * 获取指定通道的音频配置
	 * 
	 * @param channel
	 *            设备的通道号，0开始
	 * @return
	 * @throws JSONException
	 */
	public static Object audioGetConfig(String channel) throws JSONException {
		return new TutkAudioGetConfig(channel).getParse();
	}

	/**
	 * 启动特定通道的录像
	 * 
	 * @param channel
	 *            设备的通道号，0开始
	 * @return 0 成功，大于0失败，数值代码错误码
	 * @throws JSONException
	 */
	public static Integer recordStart(String channel) throws JSONException {
		return (Integer) new TutkRecordStart(channel).getParse();
	}

	/**
	 * 停止特定通道的录像
	 * 
	 * @param channel
	 *            设备的通道号，0开始
	 * @return 0 成功，大于0失败，数值代码错误码
	 * @throws JSONException
	 */
	public static Integer recordStop(String channel) throws JSONException {
		return (Integer) new TutkRecordStop(channel).getParse();
	}

	/**
	 * 暂停特定通道的录像
	 * 
	 * @param channel
	 *            设备的通道号，0开始
	 * @return 0 成功，大于0失败，数值代码错误码
	 * @throws JSONException
	 */
	public static Integer recordPause(String channel) throws JSONException {
		return (Integer) new TutkRecordPause(channel).getParse();
	}

	/**
	 * 启动特定通道的音频
	 * 
	 * @param channel
	 *            设备的通道号，0开始
	 * @return 0 成功，大于0失败，数值代码错误码
	 * @throws JSONException
	 */
	public static Integer recordAudioStart(String channel) throws JSONException {
		return (Integer) new TutkRecordAudioStart(channel).getParse();
	}

	/**
	 * 停止特定通道的音频
	 * 
	 * @param channel
	 *            设备的通道号，0开始
	 * @return 0 成功，大于0失败，数值代码错误码
	 * @throws JSONException
	 */
	public static Integer recordAudioStop(String channel) throws JSONException {
		return (Integer) new TutkRecordAudioStop(channel).getParse();
	}

	/**
	 * 返回录像文件列表
	 * 
	 * @param channel
	 *            设备的通道号，0开始
	 * @return
	 * @throws JSONException
	 */
	public static Object recordList(String channel) throws JSONException {
		return new TutkRecordList(channel).getParse();
	}

	/**
	 * 选定一个录像文件
	 * 
	 * @param filename
	 *            录像文件名，由字符串组成
	 * @return 返回值0成功，其他错误码
	 * @throws JSONException
	 */
	public static Integer recordSelectFile(String filename)
			throws JSONException {
		return (Integer) new TutkRecordSelectFile(filename).getParse();
	}

	/**
	 * 录像快放
	 * 
	 * @param channel
	 *            设备的通道号，0开始
	 * @return 返回值0成功，其他错误码
	 * @throws JSONException
	 */
	public static Integer recordFast(String channel) throws JSONException {
		return (Integer) new TutkRecordFast(channel).getParse();
	}

	/**
	 * 录像慢放
	 * 
	 * @param channel
	 *            设备的通道号，0开始
	 * @return 返回值0成功，其他错误码
	 * @throws JSONException
	 */
	public static Integer recordSlow(String channel) throws JSONException {
		return (Integer) new TutkRecordSlow(channel).getParse();
	}

	/**
	 * 录像向前跳转
	 * 
	 * @param channel
	 *            设备的通道号，0开始
	 * @return 返回值0成功，其他错误码
	 * @throws JSONException
	 */
	public static Integer recordPrev(String channel) throws JSONException {
		return (Integer) new TutkRecordPrev(channel).getParse();
	}

	/**
	 * 录像向后跳转
	 * 
	 * @param channel
	 *            设备的通道号，0开始
	 * @return 返回值0成功，其他错误码
	 * @throws JSONException
	 */
	public static Integer recordNext(String channel) throws JSONException {
		return (Integer) new TutkRecordNext(channel).getParse();
	}

	/**
	 * 录像倒放
	 * 
	 * @param channel
	 *            设备的通道号，0开始
	 * @return 返回值0成功，其他错误码
	 * @throws JSONException
	 */
	public static Integer recordBack(String channel) throws JSONException {
		return (Integer) new TutkRecordBack(channel).getParse();
	}

	/**
	 * 录像拖拽
	 * 
	 * @param channel
	 *            设备的通道号，0开始
	 * @return 返回值0成功，其他错误码
	 * @throws JSONException
	 */
	public static Integer recordJump(String channel) throws JSONException {
		return (Integer) new TutkRecordJump(channel).getParse();
	}
}
