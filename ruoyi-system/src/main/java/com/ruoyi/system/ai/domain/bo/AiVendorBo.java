package com.ruoyi.system.ai.domain.bo;

import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;

/** 业务入参：含 apiKey 明文。编辑时留空表示不修改。 */
public class AiVendorBo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long vendorId;
    private String vendorName;
    private String baseUrl;
    /** 明文入参（仅 create/必填；update/留空=不修改） */
    private String apiKey;
    private String modelName;
    private String status;
    private String isDefault;
    private Integer timeoutSec;
    private Integer maxTokens;
    private BigDecimal temperature;

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
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
}
