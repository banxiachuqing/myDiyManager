package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Sensitive;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.enums.DesensitizedType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 小米步数配置对象 xm_step_config
 * @author zhangjie
 * @date 2024-09-26
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StepConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private String id;

    /**
     * 登陆用户名
     */
    @Excel(name = "登陆用户名")
    private String userName;

    /**
     * 登录密码
     */
    @Sensitive(desensitizedType = DesensitizedType.PASSWORD)
    @Excel(name = "登录密码")
    private String password;

    /**
     * 是否通知
     */
    @Excel(name = "是否通知")
    private Long notice;

    /**
     * 通知管道
     */
    @Excel(name = "通知管道")
    private String noticeId;

    /**
     * 模式
     */
    @Excel(name = "模式")
    private String model;

    /**
     * 定时表达式
     */
    @Excel(name = "定时表达式")
    private String cron;

    /**
     * 是否启用
     */
    @Excel(name = "是否启用")
    private Long status;

    /**
     * 步数
     */
    @Excel(name = "步数")
    private String stepCount;



    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setNotice(Long notice) {
        this.notice = notice;
    }

    public Long getNotice() {
        return notice;
    }

    public void setNoticeId(String noticeId) {
        this.noticeId = noticeId;
    }

    public String getNoticeId() {
        return noticeId;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getCron() {
        return cron;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public Long getStatus() {
        return status;
    }

    public void setStepCount(String stepCount) {
        this.stepCount = stepCount;
    }

    public String getStepCount() {
        return stepCount;
    }



    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("userName", getUserName())
                .append("password", getPassword())
                .append("notice", getNotice())
                .append("noticeId", getNoticeId())
                .append("model", getModel())
                .append("cron", getCron())
                .append("status", getStatus())
                .append("stepCount", getStepCount())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
