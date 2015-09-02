package com.anviz.scom.comm;

import org.json.JSONException;
import org.json.JSONObject;

import com.anviz.scom.control.TutkCommControl;

/**
 * 停止特定通道的mic
 * @author 8444
 *
 */
public class TutkAudioMicStop extends TutkComm{
	
	/**设备的通道号，0开始*/
	private String channel;
	
	public TutkAudioMicStop(String channel){
		this.channel = channel;
	}
	
	protected void setCommand() throws JSONException {
		comm.put(TutkCommControl.AUDIO_MIC_STOP, channel);
	}

	/**
	 * 返回值0成功，其他错误码
	 */
	protected Object parse(JSONObject resp) throws JSONException {
		return resp.getInt(TutkCommControl.AUDIO_MIC_STOP);
	}

}
