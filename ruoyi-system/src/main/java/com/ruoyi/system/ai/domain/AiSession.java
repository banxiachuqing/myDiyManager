package com.ruoyi.system.ai.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.ibatis.type.Alias;

import java.util.Date;

@Alias("AiSession")
public class AiSession extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private String sessionId;
    private Long userId;
    /** 0智能问答 1主机运维 */
    private String tabType;
    private String activeHostIds;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastActiveAt;
    @JsonIgnore
    private String whitelist;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /** 首条 user 消息内容（不持久化，仅列表查询时由 SQL 子查询填充） */
    private String preview;

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTabType() { return tabType; }
    public void setTabType(String tabType) { this.tabType = tabType; }
    public String getActiveHostIds() { return activeHostIds; }
    public void setActiveHostIds(String activeHostIds) { this.activeHostIds = activeHostIds; }
    public Date getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(Date lastActiveAt) { this.lastActiveAt = lastActiveAt; }
    public String getWhitelist() { return whitelist; }
    public void setWhitelist(String whitelist) { this.whitelist = whitelist; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getPreview() { return preview; }
    public void setPreview(String preview) { this.preview = preview; }
}
