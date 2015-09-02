package com.anviz.scom.struct;

/**
 * 
 * @author 8444
 * 设备
 */
public class Devipc {
	/**所属队列*/
	private int index;
	private int m_ID;
	private int	uiIp;
	private int usPort;
	private int usRtspPort;
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getM_ID() {
		return m_ID;
	}
	public void setM_ID(int m_ID) {
		this.m_ID = m_ID;
	}
	public int getUiIp() {
		return uiIp;
	}
	public void setUiIp(int uiIp) {
		this.uiIp = uiIp;
	}
	public int getUsPort() {
		return usPort;
	}
	public void setUsPort(int usPort) {
		this.usPort = usPort;
	}
	public int getUsRtspPort() {
		return usRtspPort;
	}
	public void setUsRtspPort(int usRtspPort) {
		this.usRtspPort = usRtspPort;
	}
	public String getSzRtspFile0() {
		return szRtspFile0;
	}
	public void setSzRtspFile0(String szRtspFile0) {
		this.szRtspFile0 = szRtspFile0;
	}
	public String getSzRtspFile1() {
		return szRtspFile1;
	}
	public void setSzRtspFile1(String szRtspFile1) {
		this.szRtspFile1 = szRtspFile1;
	}
	public String getSzDevInfo() {
		return szDevInfo;
	}
	public void setSzDevInfo(String szDevInfo) {
		this.szDevInfo = szDevInfo;
	}
	private String szRtspFile0;
	private String szRtspFile1;
	private String szDevInfo;
}
