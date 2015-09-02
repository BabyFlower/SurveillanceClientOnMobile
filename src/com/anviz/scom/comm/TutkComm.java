package com.anviz.scom.comm;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.libsdl.app.SDLActivity;

/**
 * 发送控制指令，并解析返回结果的基类
 * 
 * @author 8444
 * 
 */
public abstract class TutkComm {

	/** 命令的JSON形式 */
	protected JSONObject comm;

	public TutkComm() {
		comm = new JSONObject();
	}

	/**
	 * 返回解析后的数据
	 * 
	 * @return
	 * @throws JSONException
	 */
	public Object getParse() throws JSONException {
		setCommand();
		JSONObject resp = (JSONObject) new JSONTokener(SDLActivity.visit(comm.toString()))
				.nextValue();
		System.out.println(resp.toString());
		return parse(resp);
	}

	/**
	 * 命令格式不确定，所以抽象
	 * 
	 * @throws JSONException
	 */
	abstract protected void setCommand() throws JSONException;

	/**
	 * 不确定tutk返回的格式，抽象
	 * 
	 * @throws JSONException
	 */
	abstract protected Object parse(JSONObject resp) throws JSONException;
}
