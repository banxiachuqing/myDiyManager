CREATE TABLE if not exists `xm_step_run_log`
(
    `id`                int NOT NULL AUTO_INCREMENT,
    `config_id`         varchar(255) DEFAULT NULL COMMENT '配置id',
    `user_name`         varchar(255) DEFAULT NULL COMMENT '用户名',
    `create_time`       datetime     DEFAULT NULL COMMENT '运行时间',
    `user_token_result` text COMMENT '获取用户token',
    `login_result`      text COMMENT '登录结果',
    `app_token_result`  text COMMENT '获取token结果',
    `step_result`       text COMMENT '刷步数结果',
    `step_count`        int          DEFAULT NULL COMMENT '刷入步数',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='小米刷步数运行日志';


CREATE TABLE if not exists `xm_step_config`
(
    `id`          varchar(64) NOT NULL,
    `user_name`   varchar(255)  DEFAULT NULL COMMENT '登陆用户名',
    `password`    varchar(255)  DEFAULT NULL COMMENT '登录密码',
    `notice`      int           DEFAULT NULL COMMENT '是否通知',
    `notice_id`   varchar(1024) DEFAULT NULL COMMENT '通知管道',
    `model`       varchar(64)   DEFAULT NULL COMMENT '模式：累加，一次性',
    `cron`        varchar(255)  DEFAULT NULL COMMENT '定时表达式',
    `status`      int           DEFAULT NULL COMMENT '是否启用',
    `step_count`  varchar(255)  DEFAULT NULL COMMENT '步数，支持范围-',
    `step`        varchar(255)  DEFAULT NULL COMMENT '步进',
    `create_time` datetime      DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime      DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='小米步数配置表';
