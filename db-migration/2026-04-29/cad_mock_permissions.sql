INSERT IGNORE INTO `pdm_tool_sys_permission` (`id`, `name`, `code`, `description`, `module`, `type`, `parent_id`, `level`, `sort`) VALUES
('cad_m1', 'CAD模拟接口与文件转换', 'cad:mock', 'CAD模拟接口与文件转换模块', 'cad', 'MENU', '0', 1, 1),
('cad_m2', '查询模拟接口', 'cad:mock:query', '查询CAD模拟接口', 'cad', 'BUTTON', 'cad_m1', 2, 1),
('cad_m3', '创建模拟接口', 'cad:mock:create', '创建或更新CAD模拟接口', 'cad', 'BUTTON', 'cad_m1', 2, 2),
('cad_m4', '编辑数据映射', 'cad:mock:data:edit', '编辑CAD数据字段映射', 'cad', 'BUTTON', 'cad_m1', 2, 3),
('cad_m5', '查询数据映射', 'cad:mock:data:query', '查询CAD数据字段映射', 'cad', 'BUTTON', 'cad_m1', 2, 4),
('cad_m6', '编辑文件转换配置', 'cad:mock:file:edit', '编辑CAD文件转换配置', 'cad', 'BUTTON', 'cad_m1', 2, 5),
('cad_m7', '查询文件转换配置', 'cad:mock:file:query', '查询CAD文件转换配置和转换任务', 'cad', 'BUTTON', 'cad_m1', 2, 6),
('cad_m8', '执行文件转换', 'cad:mock:file:execute', '执行CAD文件转换', 'cad', 'BUTTON', 'cad_m1', 2, 7),
('cad_m9', '统一执行', 'cad:mock:execute', '统一执行CAD数据和文件转换', 'cad', 'BUTTON', 'cad_m1', 2, 8),
('cad_m10', '模拟接口测试', 'cad:mock:test', '测试CAD模拟接口', 'cad', 'BUTTON', 'cad_m1', 2, 9),
('cad_m11', '编辑认证方式', 'cad:mock:auth:edit', '编辑CAD接口认证方式', 'cad', 'BUTTON', 'cad_m1', 2, 10),
('cad_m12', '接口连通性测试', 'cad:mock:connectivity:test', '真实测试CAD接口连通性', 'cad', 'BUTTON', 'cad_m1', 2, 11);

INSERT IGNORE INTO `pdm_tool_sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`, `create_user`) VALUES
('cad_rp_1', 'admin', 'cad_m1', NOW(), 'admin'),
('cad_rp_2', 'admin', 'cad_m2', NOW(), 'admin'),
('cad_rp_3', 'admin', 'cad_m3', NOW(), 'admin'),
('cad_rp_4', 'admin', 'cad_m4', NOW(), 'admin'),
('cad_rp_5', 'admin', 'cad_m5', NOW(), 'admin'),
('cad_rp_6', 'admin', 'cad_m6', NOW(), 'admin'),
('cad_rp_7', 'admin', 'cad_m7', NOW(), 'admin'),
('cad_rp_8', 'admin', 'cad_m8', NOW(), 'admin'),
('cad_rp_9', 'admin', 'cad_m9', NOW(), 'admin'),
('cad_rp_10', 'admin', 'cad_m10', NOW(), 'admin'),
('cad_rp_11', 'admin', 'cad_m11', NOW(), 'admin'),
('cad_rp_12', 'admin', 'cad_m12', NOW(), 'admin');
