package com.anviz.scom.comm;

import org.json.JSONException;
import org.json.JSONObject;

import com.anviz.scom.control.TutkCommControl;

/**
 * 获取指定通道的音频配置
 * 
 * @author 8444
 * 
 */
public class TutkAudioGetConfig extends TutkComm {

	/** 设备的通道号，0开始 */
	private String channel;

	public TutkAudioGetConfig(String channel) {
		this.channel = channel;
	}

	protected void setCommand() throws JSONException {
		comm.put(TutkCommControl.AUDIO_GET_CONFIG, channel);
	}

	protected Object parse(JSONObject resp) throws JSONException {
		JSONObject agc = resp.getJSONObject(TutkCommControl.AUDIO_GET_CONFIG);
		// 编码方式，
		agc.getString("compression");
		// 码率大小，kbps为单位
		agc.getString("bitrate");
		// 采用率，Hz为单位
		agc.getString("srate");
		// 采样深度
		agc.getString("sbit");
		return null;
	}

}
