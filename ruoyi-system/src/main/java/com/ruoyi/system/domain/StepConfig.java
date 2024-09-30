package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Sensitive;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.enums.DesensitizedType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
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
@Data
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
    private String  status;

    /**
     * 步数
     */
    @Excel(name = "步数")
    private String stepCount;





}
