-- V20260605110600__enable_host_referencing_check.sql
-- 标记：P3 已启用 P2 留的 ai_host 引用检查占位（mapper XML 已同步更新）
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('P3 启用主机引用检查', 'ai.phase.host_referencing_enabled', 'true', 'Y', 'admin', NOW(), '标记 P3 已启用 P2 占位');
