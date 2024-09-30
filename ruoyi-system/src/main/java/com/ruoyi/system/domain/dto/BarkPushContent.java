package com.ruoyi.system.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @创建人
 * @创建时间 2024/9/29
 * @备注
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BarkPushContent {
    /**
     * 推送标题
     */
    private String title;

    /**
     * 推送内容
     */
    private String body;

    /**
     * 推送中断级别
     */
    private String level;

    /**
     * 推送角标，可以是任意数字
     */
    private int badge;

    /**
     * 是否自动复制推送内容
     */
    private boolean autoCopy;

    /**
     * 复制推送时指定的内容
     */
    private String copy;

    /**
     * 推送铃声
     */
    @Builder.Default
    private String sound = "shake";
    ;

    /**
     * 自定义图标 URL
     */
    @Builder.Default
    private String icon = "https://photo.tuchong.com/18681/f/26970923.jpg";

    /**
     * 消息分组
     */
    private String group;

    /**
     * 是否保存推送，1 为保存，其他值不保存
     */
    private int isArchive;

    /**
     * 点击推送时跳转的 URL
     */
    private String url;
}
