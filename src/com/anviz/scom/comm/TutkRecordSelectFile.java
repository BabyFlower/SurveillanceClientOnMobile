package com.anviz.scom.comm;

import org.json.JSONException;
import org.json.JSONObject;

import com.anviz.scom.control.TutkCommControl;

/**
 * 选定一个录像文件
 * 
 * @author 8444
 * 
 */
public class TutkRecordSelectFile extends TutkComm {

	/** 录像文件名，由支付串组成。 */
	private String filename;

	public TutkRecordSelectFile(String filename) {
		this.filename = filename;
	}

	protected void setCommand() throws JSONException {
		JSONObject f = new JSONObject();

		f.put("filename", filename);

		comm.put(TutkCommControl.RECORD_SELECT_FILE, f);
	}

	protected Object parse(JSONObject resp) throws JSONException {
		return resp.getInt(TutkCommControl.RECORD_SELECT_FILE);
	}

}
