package com.iskrembilen.quasseldroid;


public class CoreInfo {

    private String coreInfo;
    private boolean supportSsl;
    private boolean configured;
    private boolean loginEnabled;
    private String msgType;
    private int protocolVersion;
    private boolean supportsCompression;

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
