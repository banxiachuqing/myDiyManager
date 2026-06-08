-- V20260605110400__add_ai_host_table.sql
CREATE TABLE ai_host (
  host_id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主机ID',
  host_name          VARCHAR(64)  NOT NULL                COMMENT '主机名',
  ip                 VARCHAR(64)  NOT NULL                COMMENT 'IP',
  ssh_port           INT          NOT NULL DEFAULT 22     COMMENT 'SSH 端口',
  ssh_protocol       VARCHAR(8)   NOT NULL DEFAULT 'SSH2' COMMENT 'SSH 协议',
  username           VARCHAR(64)  NOT NULL                COMMENT '登录用户名',
  auth_type          CHAR(1)      NOT NULL                COMMENT '认证方式 0口令 1私钥',
  password_cipher    TEXT         DEFAULT NULL            COMMENT '口令密文（auth_type=0）',
  private_key_cipher TEXT         DEFAULT NULL            COMMENT '私钥密文（auth_type=1）',
  dept_id            BIGINT       NOT NULL                COMMENT '所属部门 ID',
  status             CHAR(1)      DEFAULT '2'             COMMENT '0在线 1离线 2未知',
  last_test_at       DATETIME     DEFAULT NULL            COMMENT '最后测试时间',
  last_test_msg      VARCHAR(500) DEFAULT NULL            COMMENT '最后测试结果',
  remark             VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  create_by          VARCHAR(64)  DEFAULT ''              COMMENT '创建人',
  create_time        DATETIME     DEFAULT NULL            COMMENT '创建时间',
  update_by          VARCHAR(64)  DEFAULT ''              COMMENT '更新人',
  update_time        DATETIME     DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (host_id),
  UNIQUE KEY uk_dept_hostname (dept_id, host_name),
  KEY idx_ip (ip)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI助手-主机';
