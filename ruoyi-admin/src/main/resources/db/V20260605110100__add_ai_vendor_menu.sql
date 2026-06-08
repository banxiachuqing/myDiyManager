-- V20260605110100__add_ai_vendor_menu.sql
-- 顶级菜单：系统管理(parent_id=1)下，与字典管理等并列
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('LLM 厂商', 1, 6, 'vendor', 'ai/vendor/index', 1, 0, 'C', '0', '0', 'ai:vendor:list', 'chat-dot-square', 'admin', NOW(), 'AI 助手-LLM 厂商');
SET @parentId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, perms, menu_type, create_by, create_time) VALUES
  ('厂商查询',   @parentId, 1, 'ai:vendor:query',  'F', 'admin', NOW()),
  ('厂商新增',   @parentId, 2, 'ai:vendor:add',    'F', 'admin', NOW()),
  ('厂商修改',   @parentId, 3, 'ai:vendor:edit',   'F', 'admin', NOW()),
  ('厂商删除',   @parentId, 4, 'ai:vendor:remove', 'F', 'admin', NOW()),
  ('厂商测试',   @parentId, 5, 'ai:vendor:test',   'F', 'admin', NOW()),
  ('设为默认',   @parentId, 6, 'ai:vendor:default','F', 'admin', NOW());

-- 授权给 admin 角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'ai:vendor:%';
