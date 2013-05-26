package com.iskrembilen.quasseldroid;


import java.util.Date;
import java.util.GregorianCalendar;

public class CoreInfo {

	private int coreFeatures;
	private String coreInfo;
	private boolean supportSsl;
	private Date coreDate;
	private GregorianCalendar coreStartTime;
	private String coreVersion;
	private boolean configured;
	private boolean loginEnabled;
	private String msgType;
	private int protocolVersion;
	private boolean supportsCompression;
	public int getCoreFeatures() {
		return coreFeatures;
	}
	public void setCoreFeatures(int coreFeatures) {
		this.coreFeatures = coreFeatures;
	}
	public String getCoreInfo() {
		return coreInfo;
	}
	public void setCoreInfo(String coreInfo) {
		this.coreInfo = coreInfo;
	}
	public boolean isSupportSsl() {
		return supportSsl;
	}
	public void setSupportSsl(boolean supportSsl) {
		this.supportSsl = supportSsl;
	}
	public Date getCoreDate() {
		return coreDate;
	}
	public void setCoreDate(Date coreDate) {
		this.coreDate = coreDate;
	}
	public GregorianCalendar getCoreStartTime() {
		return coreStartTime;
	}
	public void setCoreStartTime(GregorianCalendar coreStartTime) {
		this.coreStartTime = coreStartTime;
	}
	public String getCoreVersion() {
		return coreVersion;
	}
	public void setCoreVersion(String coreVersion) {
		this.coreVersion = coreVersion;
	}
	public boolean isConfigured() {
		return configured;
	}
	public void setConfigured(boolean configured) {
		this.configured = configured;
	}
	public boolean isLoginEnabled() {
		return loginEnabled;
	}
	public void setLoginEnabled(boolean loginEnabled) {
		this.loginEnabled = loginEnabled;
	}
	public String getMsgType() {
		return msgType;
	}
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	public int getProtocolVersion() {
		return protocolVersion;
	}
	public void setProtocolVersion(int protocolVersion) {
		this.protocolVersion = protocolVersion;
	}
	public boolean isSupportsCompression() {
		return supportsCompression;
	}
	public void setSupportsCompression(boolean supportsCompression) {
		this.supportsCompression = supportsCompression;
	}
	
	
	
}
