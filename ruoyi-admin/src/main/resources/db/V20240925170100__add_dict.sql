INSERT INTO `sys_dict_type`
(`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`,
 `update_time`, `remark`)
VALUES (100, '小米运动步数模式', 'xm_step_model', '0', 'admin', '2024-09-25 06:40:27', '', NULL, NULL);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`,
                             `css_class`, `list_class`, `is_default`, `status`, `create_by`,
                             `create_time`, `update_by`, `update_time`, `remark`)
VALUES (103, 3, '小米运动', 'XMSTEP', 'sys_job_group', NULL, 'default', 'N', '0', 'admin', '2024-09-25 09:09:22', '',
        NULL, NULL);

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`,
                             `css_class`, `list_class`, `is_default`, `status`, `create_by`,
                             `create_time`, `update_by`, `update_time`, `remark`)
VALUES (104, 0, '累加', '1', 'xm_step_model', NULL, 'default', 'N', '0', 'admin', '2024-09-25 09:11:27', '', NULL,
        NULL);
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`,
                             `css_class`, `list_class`, `is_default`, `status`, `create_by`,
                             `create_time`, `update_by`, `update_time`, `remark`)
VALUES (105, 1, '一次性', '2', 'xm_step_model', NULL, 'default', 'N', '0', 'admin', '2024-09-25 09:11:41', '', NULL,
        NULL);
