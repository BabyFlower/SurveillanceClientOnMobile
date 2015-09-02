package com.anviz.scom.comm;

import org.json.JSONException;
import org.json.JSONObject;

import com.anviz.scom.control.TutkCommControl;

/**
 * 停止特定通道的特定码流
 * 
 * @author 8444
 * 
 */
public class TutkVideoStop extends TutkComm {

	/** 设备的通道号，0开始 */
	private String channel;
	/** 指定通道的第几个码流，0开始 */
	private String streamtype;

	public TutkVideoStop(String channel, String streamtype) {
		this.channel = channel;
		this.streamtype = streamtype;
	}

	protected void setCommand() throws JSONException {
		JSONObject vs = new JSONObject();

		vs.put("channel", channel);
		vs.put("streamtype", streamtype);

		comm.put(TutkCommControl.VIDEO_STOP, vs);
	}

	/**
	 * 返回值0成功，其他错误码
	 */
	protected Object parse(JSONObject resp) throws JSONException {
		return resp.getInt(TutkCommControl.VIDEO_STOP);
	}

}
