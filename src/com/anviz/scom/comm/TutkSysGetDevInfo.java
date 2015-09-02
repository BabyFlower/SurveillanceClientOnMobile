package com.anviz.scom.comm;

import org.json.JSONException;
import org.json.JSONObject;

import com.anviz.scom.control.TutkCommControl;

/**
 * tutk获取设备系统信息
 * 
 * @author 8444
 * 
 */
public class TutkSysGetDevInfo extends TutkComm {

	public TutkSysGetDevInfo() {

	}

	protected void setCommand() throws JSONException {
		comm.put(TutkCommControl.SYS_GET_DEVINFO, "");
	}

	protected Object parse(JSONObject resp) throws JSONException {
		JSONObject sgd = resp.getJSONObject(TutkCommControl.SYS_GET_DEVINFO);
		
		System.out.println(sgd.toString() + "==================");
//		System.out.println(sgd.getString("productmodel"));;
//		System.out.println(sgd.getString("softver"));;
//		System.out.println(sgd.getString("hardver"));;
//		System.out.println(sgd.getString("mac"));;
//		System.out.println(sgd.getString("uuid"));;
//		System.out.println(sgd.getString("maxchannel"));;
//		System.out.println(sgd.getString("maxstream"));;
		return null;
	}

}
