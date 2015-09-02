package com.anviz.scom.comm;

import org.json.JSONException;
import org.json.JSONObject;

import com.anviz.scom.control.TutkCommControl;

/**
 * 录像向后跳转
 * @author 8444
 *
 */
public class TutkRecordNext extends TutkComm {

	/** 设备的通道号，0开始 */
	private String channel;

	public TutkRecordNext(String channel) {
		this.channel = channel;
	}

	protected void setCommand() throws JSONException {
		comm.put(TutkCommControl.RECORD_NEXT, channel);
	}

	protected Object parse(JSONObject resp) throws JSONException {
		return resp.getInt(TutkCommControl.RECORD_NEXT);
	}

}
