package com.ruoyi.system.ai.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.util.Date;

/**
 * LLM 厂商对象 ai_vendor
 */
@Alias("AiVendor")
public class AiVendor extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long vendorId;

    @Excel(name = "厂商名称")
    private String vendorName;

    private String baseUrl;

    @JsonIgnore
    private String apiKeyCipher;

    private String apiKeyMask;

    @Excel(name = "模型")
    private String modelName;

    /** 0启用 1停用 */
    @Excel(name = "状态", readConverterExp = "0=启用,1=停用")
    private String status;

    /** 0否 1是 */
    @Excel(name = "默认", readConverterExp = "0=否,1=是")
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
    public String getApiKeyCipher() { return apiKeyCipher; }
    public void setApiKeyCipher(String apiKeyCipher) { this.apiKeyCipher = apiKeyCipher; }
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
