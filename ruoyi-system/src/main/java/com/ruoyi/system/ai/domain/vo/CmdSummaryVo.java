package com.ruoyi.system.ai.domain.vo;

public class CmdSummaryVo {
    private String cmdId;
    private String cmdText;
    private String targetHostIds;
    private String cmdFingerprint;
    private String isHighRisk;
    private String execStatus;
    /** 本次会话已加入白名单（同 fingerprint 已点过"本次会话内允许"） */
    private Boolean inWhitelist;
    private String decision;
    private String execResult;
    private Integer execMs;

    public String getCmdId() { return cmdId; }
    public void setCmdId(String cmdId) { this.cmdId = cmdId; }
    public String getCmdText() { return cmdText; }
    public void setCmdText(String cmdText) { this.cmdText = cmdText; }
    public String getTargetHostIds() { return targetHostIds; }
    public void setTargetHostIds(String targetHostIds) { this.targetHostIds = targetHostIds; }
    public String getCmdFingerprint() { return cmdFingerprint; }
    public void setCmdFingerprint(String cmdFingerprint) { this.cmdFingerprint = cmdFingerprint; }
    public String getIsHighRisk() { return isHighRisk; }
    public void setIsHighRisk(String isHighRisk) { this.isHighRisk = isHighRisk; }
    public String getExecStatus() { return execStatus; }
    public void setExecStatus(String execStatus) { this.execStatus = execStatus; }
    public Boolean getInWhitelist() { return inWhitelist; }
    public void setInWhitelist(Boolean inWhitelist) { this.inWhitelist = inWhitelist; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getExecResult() { return execResult; }
    public void setExecResult(String execResult) { this.execResult = execResult; }
    public Integer getExecMs() { return execMs; }
    public void setExecMs(Integer execMs) { this.execMs = execMs; }
}
