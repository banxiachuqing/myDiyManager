package com.ruoyi.system.ai.domain.bo;

import java.util.List;

public class ChatSendBo {
    private String sessionId;       // null=新建会话
    private String tabType;          // 必填 0/1
    private String content;          // 必填 1~4000
    private Long vendorId;           // 必填
    private List<Long> hostIds;      // 主机运维必填
    /** 命令执行总结模式：非空时表示把此 cmdId 的执行结果回灌给 LLM 生成结论，content 必填作为总结指令 */
    private String cmdId;

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getTabType() { return tabType; }
    public void setTabType(String tabType) { this.tabType = tabType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public List<Long> getHostIds() { return hostIds; }
    public void setHostIds(List<Long> hostIds) { this.hostIds = hostIds; }
    public String getCmdId() { return cmdId; }
    public void setCmdId(String cmdId) { this.cmdId = cmdId; }
}
