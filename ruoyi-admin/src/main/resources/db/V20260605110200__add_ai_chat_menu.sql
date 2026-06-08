-- V20260605110200__add_ai_chat_menu.sql
-- 顶级菜单：AI 助手（与 系统管理、监控 等并列）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('AI 助手', 0, 5, 'chat', 'ai/chat/index', 1, 0, 'C', '0', '0', '', 'service', 'admin', NOW(), 'AI 助手（智能问答 + 主机运维）');
SET @chatId = LAST_INSERT_ID();

-- 4 个隐藏子菜单（仅用于按钮级权限控制，visible=1 不在侧栏展示）
INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type, visible, create_by, create_time) VALUES
  ('智能问答', @chatId, 1, 'ai:chat:send', 'F', '1', 'admin', NOW()),
  ('主机运维', @chatId, 2, 'ai:chat:cmd',  'F', '1', 'admin', NOW()),
  ('历史会话', @chatId, 3, 'ai:chat:list', 'F', '1', 'admin', NOW()),
  ('主机抽屉', @chatId, 4, 'ai:host:list', 'F', '1', 'admin', NOW());

-- 授权给 admin 角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms IN ('ai:chat:send','ai:chat:cmd','ai:chat:list','ai:host:list');
