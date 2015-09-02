package com.anviz.scom.comm;

import org.json.JSONException;
import org.json.JSONObject;

import com.anviz.scom.control.TutkCommControl;

/**
 * 启动特定通道的音频
 * @author 8444
 *
 */
public class TutkRecordAudioStart extends TutkComm {

	/** 设备的通道号，0开始 */
	private String channel;
	
	public TutkRecordAudioStart(String channel){
		this.channel = channel;
	}
	
	protected void setCommand() throws JSONException {
		comm.put(TutkCommControl.RECORD_AUDIO_START, channel);
	}

	protected Object parse(JSONObject resp) throws JSONException {
		return resp.getInt(TutkCommControl.RECORD_AUDIO_START);
	}

}
