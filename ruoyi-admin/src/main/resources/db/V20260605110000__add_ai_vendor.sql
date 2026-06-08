-- V20260605110000__add_ai_vendor.sql
CREATE TABLE ai_vendor (
  vendor_id        BIGINT       NOT NULL AUTO_INCREMENT    COMMENT '厂商ID',
  vendor_name      VARCHAR(64)  NOT NULL                COMMENT '厂商名称',
  base_url         VARCHAR(256) NOT NULL                COMMENT 'OpenAI 兼容 baseUrl',
  api_key_cipher   TEXT         NOT NULL                COMMENT 'apiKey 密文（Jasypt）',
  api_key_mask     VARCHAR(32)  NOT NULL                COMMENT 'apiKey 末四位摘要',
  model_name       VARCHAR(128) NOT NULL                COMMENT '模型名',
  status           CHAR(1)      DEFAULT '0'             COMMENT '0启用 1停用',
  is_default       CHAR(1)      DEFAULT '0'             COMMENT '0否 1是',
  timeout_sec      INT          DEFAULT 60              COMMENT '调用超时秒',
  max_tokens       INT          DEFAULT 2048            COMMENT '最大 token',
  temperature      DECIMAL(3,1) DEFAULT 0.7              COMMENT '温度',
  last_test_at     DATETIME     DEFAULT NULL            COMMENT '最后测试时间',
  last_test_msg    VARCHAR(500) DEFAULT NULL            COMMENT '最后测试结果',
  remark           VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  create_by        VARCHAR(64)  DEFAULT ''              COMMENT '创建人',
  create_time      DATETIME     DEFAULT NULL            COMMENT '创建时间',
  update_by        VARCHAR(64)  DEFAULT ''              COMMENT '更新人',
  update_time      DATETIME     DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (vendor_id),
  UNIQUE KEY uk_vendor_name (vendor_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI助手-LLM厂商';
