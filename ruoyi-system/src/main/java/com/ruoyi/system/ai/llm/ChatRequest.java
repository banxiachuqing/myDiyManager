package com.ruoyi.system.ai.llm;

import java.util.List;
import java.util.Map;

public class ChatRequest {
    private String model;
    private List<Map<String, String>> messages;
    private boolean stream;
    private Integer maxTokens;
    private Double temperature;

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public List<Map<String, String>> getMessages() { return messages; }
    public void setMessages(List<Map<String, String>> messages) { this.messages = messages; }
    public boolean isStream() { return stream; }
    public void setStream(boolean stream) { this.stream = stream; }
    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
}
