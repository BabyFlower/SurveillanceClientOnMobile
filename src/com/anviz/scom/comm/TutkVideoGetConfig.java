package com.anviz.scom.comm;

import org.json.JSONException;
import org.json.JSONObject;

import com.anviz.scom.control.TutkCommControl;

/**
 * 获取指定通道的编码参数
 * 
 * @author 8444
 * 
 */
public class TutkVideoGetConfig extends TutkComm {

	/** 设备的通道号，0开始 */
	private String channel;
	/** 指定通道的第几个码流，0开始 */
	private String streamtype;

	public TutkVideoGetConfig(String channel, String streamtype) {
		this.channel = channel;
		this.streamtype = streamtype;
	}

	protected void setCommand() throws JSONException {
		JSONObject vs = new JSONObject();

		vs.put("channel", channel);
		vs.put("streamtype", streamtype);

		comm.put(TutkCommControl.VIDEO_GET_CONFIG, vs);
	}

	protected Object parse(JSONObject resp) throws JSONException {
		JSONObject vgc = resp.getJSONObject(TutkCommControl.VIDEO_GET_CONFIG);
		vgc.getString("compression");
		vgc.getString("solution");
		vgc.getString("framerate");
		vgc.getString("bitrate");
		vgc.getString("quality");
		vgc.getString("GOP");
		vgc.getString("minbitrate");
		vgc.getString("maxbitrate");
		return null;
	}

}
