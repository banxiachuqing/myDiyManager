package com.ruoyi.system.ai.llm;

public class ChatResponse {
    private String id;
    private String model;
    private String content;
    private Integer totalTokens;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getTotalTokens() { return totalTokens; }
    public void setTotalTokens(Integer totalTokens) { this.totalTokens = totalTokens; }
}
