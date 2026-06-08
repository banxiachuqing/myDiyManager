package com.ruoyi.system.ai.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.ibatis.type.Alias;

import java.util.Date;

@Alias("AiMessage")
public class AiMessage extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private String messageId;
    private String sessionId;
    /** 0用户 1AI 2系统 */
    private String role;
    private String content;
    private Long vendorId;
    /** 0生成中 1完成 2失败 3取消 */
    private String status;
    private Integer firstTokenMs;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getFirstTokenMs() { return firstTokenMs; }
    public void setFirstTokenMs(Integer firstTokenMs) { this.firstTokenMs = firstTokenMs; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
