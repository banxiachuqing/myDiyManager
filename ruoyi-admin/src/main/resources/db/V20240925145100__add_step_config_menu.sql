-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('小米步数配置', '2006', '1', 'stepConfig', 'step/stepConfig/index', 1, 0, 'C', '0', '0', 'step:stepConfig:list', '#', 'admin', sysdate(), '', null, '小米步数配置菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('小米步数配置查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'step:stepConfig:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('小米步数配置新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'step:stepConfig:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('小米步数配置修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'step:stepConfig:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('小米步数配置删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'step:stepConfig:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('小米步数配置导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'step:stepConfig:export',       '#', 'admin', sysdate(), '', null, '');
