-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('小米步数配置', '2006', '1', 'config', 'step/config/index', 1, 0, 'C', '0', '0', 'step:config:list', '#', 'admin', sysdate(), '', null, '小米步数配置菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('小米步数配置查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'step:config:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('小米步数配置新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'step:config:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('小米步数配置修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'step:config:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('小米步数配置删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'step:config:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('小米步数配置导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'step:config:export',       '#', 'admin', sysdate(), '', null, '');