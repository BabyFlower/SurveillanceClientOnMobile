package com.anviz.scom.comm;

import org.json.JSONException;
import org.json.JSONObject;

import com.anviz.scom.control.TutkCommControl;

/**
 * 停止特定通道的speaker
 * @author 8444
 *
 */
public class TutkAudioSpeakerStop extends TutkComm{

	/** 设备的通道号，0开始 */
	private String channel;
	
	public TutkAudioSpeakerStop(String channel){
		this.channel = channel;
	}
	
	protected void setCommand() throws JSONException {
		comm.put(TutkCommControl.AUDIO_SPEAKER_STOP, channel);
	}

	protected Object parse(JSONObject resp) throws JSONException {
		return resp.getInt(TutkCommControl.AUDIO_SPEAKER_STOP);
	}

}
