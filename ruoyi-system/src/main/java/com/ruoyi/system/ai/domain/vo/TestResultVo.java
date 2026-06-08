package com.ruoyi.system.ai.domain.vo;

import java.util.List;

public class TestResultVo {
    private Boolean success;
    private String message;
    private List<String> modelList;
    private Long elapsedMs;

    public static TestResultVo success(String m) { TestResultVo v = new TestResultVo(); v.success = true; v.message = m; return v; }
    public static TestResultVo failure(String m) { TestResultVo v = new TestResultVo(); v.success = false; v.message = m; return v; }

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<String> getModelList() { return modelList; }
    public void setModelList(List<String> modelList) { this.modelList = modelList; }
    public Long getElapsedMs() { return elapsedMs; }
    public void setElapsedMs(Long elapsedMs) { this.elapsedMs = elapsedMs; }
}
