package com.anviz.scom.comm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.anviz.scom.control.TutkCommControl;

/**
 * 返回录像文件列表
 * @author 8444
 * 
 */
public class TutkRecordList extends TutkComm {

	/** 设备的通道号，0开始 */
	private String channel;

	public TutkRecordList(String channel) {
		this.channel = channel;
	}

	protected void setCommand() throws JSONException {
		comm.put(TutkCommControl.RECORD_LIST, channel);
	}

	protected Object parse(JSONObject resp) throws JSONException {
		JSONArray rl = resp.getJSONArray(TutkCommControl.RECORD_LIST);

		for (int i = 0; i < rl.length(); i++) {
			// 录像文件名，由字符串组成。
			rl.getJSONObject(i).getString("filename");
		}
		return null;
	}

}
