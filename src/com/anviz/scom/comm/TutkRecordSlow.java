package com.anviz.scom.comm;

import org.json.JSONException;
import org.json.JSONObject;

import com.anviz.scom.control.TutkCommControl;

/**
 * 录像慢放
 * @author 8444
 *
 */
public class TutkRecordSlow extends TutkComm {

	/** 设备的通道号，0开始 */
	private String channel;
	
	public TutkRecordSlow(String channel){
		this.channel = channel;
	}
	
	protected void setCommand() throws JSONException {
		comm.put(TutkCommControl.RECORD_SLOW, channel);
	}

	protected Object parse(JSONObject resp) throws JSONException {
		return resp.getInt(TutkCommControl.RECORD_SLOW);
	}

}

