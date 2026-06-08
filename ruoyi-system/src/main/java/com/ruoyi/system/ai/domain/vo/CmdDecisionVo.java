package com.ruoyi.system.ai.domain.vo;

public class CmdDecisionVo {
    private String cmdId;
    private String decision;
    private Boolean inWhitelist;
    private String execStatus;
    private String execResult;
    private Long elapsedMs;

    public String getCmdId() { return cmdId; }
    public void setCmdId(String cmdId) { this.cmdId = cmdId; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public Boolean getInWhitelist() { return inWhitelist; }
    public void setInWhitelist(Boolean inWhitelist) { this.inWhitelist = inWhitelist; }
    public String getExecStatus() { return execStatus; }
    public void setExecStatus(String execStatus) { this.execStatus = execStatus; }
    public String getExecResult() { return execResult; }
    public void setExecResult(String execResult) { this.execResult = execResult; }
    public Long getElapsedMs() { return elapsedMs; }
    public void setElapsedMs(Long elapsedMs) { this.elapsedMs = elapsedMs; }
}
