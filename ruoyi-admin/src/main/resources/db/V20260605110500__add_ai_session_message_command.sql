-- V20260605110500__add_ai_session_message_command.sql
CREATE TABLE ai_session (
  session_id       VARCHAR(64)   NOT NULL                COMMENT '会话ID（UUID）',
  user_id          BIGINT        NOT NULL                COMMENT '所属用户ID',
  tab_type         CHAR(1)       NOT NULL                COMMENT 'Tab类型 0智能问答 1主机运维',
  active_host_ids  VARCHAR(2000) DEFAULT NULL            COMMENT '当前选中的主机ID列表（逗号分隔）',
  last_active_at   DATETIME      NOT NULL                COMMENT '最后活跃时间（用于会话超时）',
  whitelist        TEXT          DEFAULT NULL            COMMENT '本次会话内允许的命令指纹集合（JSON）',
  create_time      DATETIME      DEFAULT NULL            COMMENT '创建时间',
  PRIMARY KEY (session_id),
  KEY idx_user_active (user_id, last_active_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI助手-会话';

CREATE TABLE ai_message (
  message_id       VARCHAR(64)   NOT NULL                COMMENT '消息ID',
  session_id       VARCHAR(64)   NOT NULL                COMMENT '所属会话ID',
  role             CHAR(1)       NOT NULL                COMMENT '角色 0用户 1AI 2系统',
  content          TEXT          NOT NULL                COMMENT '消息内容',
  vendor_id        BIGINT        DEFAULT NULL            COMMENT 'LLM 厂商ID（仅 AI 消息）',
  status           CHAR(1)       NOT NULL                COMMENT '状态 0生成中 1完成 2失败 3取消',
  first_token_ms   INT           DEFAULT NULL            COMMENT '首token时延（毫秒）',
  create_time      DATETIME      NOT NULL                COMMENT '创建时间',
  PRIMARY KEY (message_id),
  KEY idx_session_time (session_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI助手-消息';

CREATE TABLE ai_command (
  cmd_id           VARCHAR(64)   NOT NULL                COMMENT '命令ID',
  message_id       VARCHAR(64)   NOT NULL                COMMENT '所属消息ID',
  cmd_text         TEXT          NOT NULL                COMMENT '命令原文',
  target_host_ids  VARCHAR(2000) NOT NULL                COMMENT '目标主机ID列表（逗号分隔）',
  decision         CHAR(1)       NOT NULL DEFAULT '0'    COMMENT '裁决 0待裁决 1允许 2会话内允许 3拒绝',
  decision_user    VARCHAR(64)   DEFAULT NULL            COMMENT '裁决用户',
  decision_time    DATETIME      DEFAULT NULL            COMMENT '裁决时间',
  cmd_fingerprint  VARCHAR(128)  DEFAULT NULL            COMMENT '命令摘要指纹',
  is_high_risk     CHAR(1)       DEFAULT '0'             COMMENT '是否高危 0否 1是',
  exec_status      CHAR(1)       NOT NULL DEFAULT '0'    COMMENT '执行状态 0待执行 1执行中 2成功 3部分成功 4失败 5超时 6已拒绝',
  exec_result      TEXT          DEFAULT NULL            COMMENT '执行结果摘要（多主机）',
  exec_ms          INT           DEFAULT NULL            COMMENT '执行耗时（毫秒，多主机取最大）',
  create_time      DATETIME      NOT NULL                COMMENT '创建时间',
  PRIMARY KEY (cmd_id),
  KEY idx_message (message_id),
  KEY idx_fingerprint (cmd_fingerprint)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI助手-命令';

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('AI 高危命令关键词', 'ai.high_risk_keywords',
  'rm -rf /,chmod 777,dd of=/dev/,mkfs,shutdown,reboot,iptables -F,userdel,kill -9 1',
  'Y', 'admin', NOW(), 'AI 助手主机运维高危命令扫描');
