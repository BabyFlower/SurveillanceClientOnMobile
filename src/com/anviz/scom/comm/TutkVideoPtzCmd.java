package com.anviz.scom.comm;

import org.json.JSONException;
import org.json.JSONObject;

import com.anviz.scom.control.TutkCommControl;

/**
 * Ptz控制
 * @author 8444
 *
 */
public class TutkVideoPtzCmd extends TutkComm {
	
	/**上*/
	public static final int TILT_UP = 0;
	/**下*/
	public static final int TILT_DOWN = 1;
	/**左*/
	public static final int PAN_LEFT = 2;
	/**右*/
	public static final int PAN_RIGHT = 3;
	/**左上*/
	public static final int PAN_LEFTTOP = 4;
	/**左下*/
	public static final int PAN_LEFTDOWN = 5;
	/**右上*/
	public static final int PAN_RIGTHTOP = 6;
	/**右下*/
	public static final int PAN_RIGTHDOWN = 7;
	/**变倍大*/
	public static final int ZOOM_IN = 8;
	/**变倍小*/
	public static final int ZOOM_OUT = 9;
	/**焦点后调*/
	public static final int FOCUS_FAR = 11;
	/**焦点前调*/
	public static final int FOCUS_NEAR = 12;
	/**光圈扩大*/
	public static final int IRIS_OPEN = 13;
	/**光圈缩小*/
	public static final int IRIS_CLOSE = 14;
	/**报警功能*/
	public static final int EXTPTZ_OPERATION_ALARM = 15;
	/**灯光开*/
	public static final int EXTPTZ_LAMP_ON = 16;
	/**灯光关*/
	public static final int EXTPTZ_LAMP_OFF = 17;
	/**设置预置点*/
	public static final int EXTPTZ_POINT_SET_CONTROL = 18;
	/**清除预置点*/
	public static final int EXTPTZ_POINT_DEL_CONTROL = 19;
	/**转预置点*/
	public static final int EXTPTZ_POINT_MOVE_CONTROL = 20;
	/**开始水平旋转*/
	public static final int EXTPTZ_STARTPANCRUISE = 21;
	/**停止水平旋转*/
	public static final int EXTPTZ_STOPPANCRUISE = 22;
	/**设置左边界*/
	public static final int EXTPTZ_SETLEFTBORDER = 23;
	/**设置右边界*/
	public static final int EXTPTZ_SETRIGHTBORDER = 24;
	/**自动扫描开始*/
	public static final int EXTPTZ_STARTLINESCAN = 25;
	/**自动扫描开停止*/
	public static final int EXTPTZ_CLOSELINESCAN = 26;
	/**加入预置点到巡航*/
	public static final int EXTPTZ_ADDTOLOOP = 27;
	/**删除巡航中预置点*/
	public static final int EXTPTZ_DELFROMLOOP = 28;
	/**开始巡航*/
	public static final int EXTPTZ_POINT_LOOP_CONTROL = 29;
	/**停止巡航*/
	public static final int EXTPTZ_POINT_STOP_LOOP_CONTROL = 30;
	/**清除巡航	p1巡航线路	*/
	public static final int EXTPTZ_CLOSELOOP = 31;
	/**快速定位*/
	public static final int EXTPTZ_FASTGOTO = 32;
	/**辅助开关，关闭在子命令中*/
	public static final int EXTPTZ_AUXIOPEN = 33;
	/**球机菜单操作，其中包括开，关，确定等等*/
	public static final int EXTPTZ_OPERATION_MENU = 34;
	/**镜头翻转*/
	public static final int EXTPTZ_REVERSECOMM = 35;
	/**云台复位*/
	public static final int EXTPTZ_OPERATION_RESET = 36;
	
	/** 设备的通道号，0开始 */
	private String channel;
	/** ptz的具体操作 */
	private int cmdvalue;

	public TutkVideoPtzCmd(String channel, int cmdvalue) {
		this.channel = channel;
		this.cmdvalue = cmdvalue;
	}

	protected void setCommand() throws JSONException {
		JSONObject vpc = new JSONObject();

		vpc.put("channel", channel);
		vpc.put("cmdvalue", cmdvalue);

		comm.put(TutkCommControl.VIDEO_PTZ_CMD, vpc);
	}

	/**
	 * 0 成功，大于0失败，数值代码错误码
	 */
	protected Object parse(JSONObject resp) throws JSONException {
		return resp.getInt(TutkCommControl.VIDEO_PTZ_CMD);
	}

}
