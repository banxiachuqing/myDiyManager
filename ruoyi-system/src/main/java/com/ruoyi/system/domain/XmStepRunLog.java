package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 小米刷步数运行日志对象 xm_step_run_log
 *
 * @author zhangjie
 * @date 2024-09-26
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class XmStepRunLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    private Long id;

    /** 配置id */
    @Excel(name = "配置id")
    private String configId;

    /** 用户名 */
    @Excel(name = "用户名")
    private String userName;

    /** 获取用户token */
    @Excel(name = "获取用户token")
    private String userTokenResult;

    /** 登录结果 */
    @Excel(name = "登录结果")
    private String loginResult;

    /** 获取token结果 */
    @Excel(name = "获取token结果")
    private String appTokenResult;

    /** 刷步数结果 */
    @Excel(name = "刷步数结果")
    private String stepResult;

    /** 刷入步数 */
    @Excel(name = "刷入步数")
    private Integer stepCount;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }
    public void setConfigId(String configId)
    {
        this.configId = configId;
    }

    public String getConfigId()
    {
        return configId;
    }
    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getUserName()
    {
        return userName;
    }
    public void setUserTokenResult(String userTokenResult)
    {
        this.userTokenResult = userTokenResult;
    }

    public String getUserTokenResult()
    {
        return userTokenResult;
    }
    public void setLoginResult(String loginResult)
    {
        this.loginResult = loginResult;
    }

    public String getLoginResult()
    {
        return loginResult;
    }
    public void setAppTokenResult(String appTokenResult)
    {
        this.appTokenResult = appTokenResult;
    }

    public String getAppTokenResult()
    {
        return appTokenResult;
    }
    public void setStepResult(String stepResult)
    {
        this.stepResult = stepResult;
    }

    public String getStepResult()
    {
        return stepResult;
    }
    public void setStepCount(Integer stepCount)
    {
        this.stepCount = stepCount;
    }

    public Integer getStepCount()
    {
        return stepCount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("configId", getConfigId())
            .append("userName", getUserName())
            .append("createTime", getCreateTime())
            .append("userTokenResult", getUserTokenResult())
            .append("loginResult", getLoginResult())
            .append("appTokenResult", getAppTokenResult())
            .append("stepResult", getStepResult())
            .append("stepCount", getStepCount())
            .toString();
    }
}
