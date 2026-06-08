package com.ruoyi.system.ai.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/** 响应：不含 apiKeyCipher；编辑时返回空 apiKey 字段（前端占位）。 */
public class AiVendorVo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long vendorId;
    private String vendorName;
    private String baseUrl;
    /** 前端不展示，仅占位 */
    @JsonIgnore
    private String apiKey;
    private String apiKeyMask;
    private String modelName;
    private String status;
    private String isDefault;
    private Integer timeoutSec;
    private Integer maxTokens;
    private BigDecimal temperature;
    private Date lastTestAt;
    private String lastTestMsg;

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getApiKeyMask() { return apiKeyMask; }
    public void setApiKeyMask(String apiKeyMask) { this.apiKeyMask = apiKeyMask; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getIsDefault() { return isDefault; }
    public void setIsDefault(String isDefault) { this.isDefault = isDefault; }
    public Integer getTimeoutSec() { return timeoutSec; }
    public void setTimeoutSec(Integer timeoutSec) { this.timeoutSec = timeoutSec; }
    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }
    public Date getLastTestAt() { return lastTestAt; }
    public void setLastTestAt(Date lastTestAt) { this.lastTestAt = lastTestAt; }
    public String getLastTestMsg() { return lastTestMsg; }
    public void setLastTestMsg(String lastTestMsg) { this.lastTestMsg = lastTestMsg; }
}
