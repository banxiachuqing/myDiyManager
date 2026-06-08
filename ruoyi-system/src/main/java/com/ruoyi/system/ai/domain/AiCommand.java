package com.ruoyi.system.ai.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.ibatis.type.Alias;

import java.util.Date;

@Alias("AiCommand")
public class AiCommand extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private String cmdId;
    private String messageId;
    private String cmdText;
    private String targetHostIds;
    /** 0待裁决 1允许 2会话内允许 3拒绝 */
    private String decision;
    private String decisionUser;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date decisionTime;
    private String cmdFingerprint;
    /** 0否 1是 */
    private String isHighRisk;
    /** 0待执行 1执行中 2成功 3部分成功 4失败 5超时 6已拒绝 */
    private String execStatus;
    private String execResult;
    private Integer execMs;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public String getCmdId() { return cmdId; }
    public void setCmdId(String cmdId) { this.cmdId = cmdId; }
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getCmdText() { return cmdText; }
    public void setCmdText(String cmdText) { this.cmdText = cmdText; }
    public String getTargetHostIds() { return targetHostIds; }
    public void setTargetHostIds(String targetHostIds) { this.targetHostIds = targetHostIds; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getDecisionUser() { return decisionUser; }
    public void setDecisionUser(String decisionUser) { this.decisionUser = decisionUser; }
    public Date getDecisionTime() { return decisionTime; }
    public void setDecisionTime(Date decisionTime) { this.decisionTime = decisionTime; }
    public String getCmdFingerprint() { return cmdFingerprint; }
    public void setCmdFingerprint(String cmdFingerprint) { this.cmdFingerprint = cmdFingerprint; }
    public String getIsHighRisk() { return isHighRisk; }
    public void setIsHighRisk(String isHighRisk) { this.isHighRisk = isHighRisk; }
    public String getExecStatus() { return execStatus; }
    public void setExecStatus(String execStatus) { this.execStatus = execStatus; }
    public String getExecResult() { return execResult; }
    public void setExecResult(String execResult) { this.execResult = execResult; }
    public Integer getExecMs() { return execMs; }
    public void setExecMs(Integer execMs) { this.execMs = execMs; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
