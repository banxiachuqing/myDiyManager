-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('小米刷步数运行日志', '2006', '1', 'log', 'step/log/index', 1, 0, 'C', '0', '0', 'step:log:list', '#', 'admin', sysdate(), '', null, '小米刷步数运行日志菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('小米刷步数运行日志查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'step:log:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('小米刷步数运行日志新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'step:log:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('小米刷步数运行日志修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'step:log:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('小米刷步数运行日志删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'step:log:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('小米刷步数运行日志导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'step:log:export',       '#', 'admin', sysdate(), '', null, '');