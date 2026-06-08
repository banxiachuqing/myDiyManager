package com.ruoyi.system.ai.llm;

public class ChatChunk {
    private String messageId;
    private String delta;
    /** null=未结束 / stop / length / content_filter */
    private String finishReason;

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getDelta() { return delta; }
    public void setDelta(String delta) { this.delta = delta; }
    public String getFinishReason() { return finishReason; }
    public void setFinishReason(String finishReason) { this.finishReason = finishReason; }
}
