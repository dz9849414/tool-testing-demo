-- ===========================================
-- 系统管理模块建表SQL
-- 仿照 Metersphere 系统管理模块设计
-- ===========================================

-- 用户表
DROP TABLE IF EXISTS `pdm_tool_sys_user`;
CREATE TABLE `pdm_tool_sys_user` (
    `id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password` VARCHAR(256) NOT NULL COMMENT '密码',
    `email` VARCHAR(128) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `real_name` VARCHAR(64) COMMENT '真实姓名',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    `deleted_by` BIGINT DEFAULT NULL COMMENT '删除人ID',
    `deleted_time` DATETIME DEFAULT NULL COMMENT '删除时间',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(64) COMMENT '最后登录IP',
    `source` VARCHAR(32) DEFAULT 'LOCAL' COMMENT '用户来源：LOCAL-本地，LDAP-LDAP，OIDC-OIDC',
    `organization_id` VARCHAR(50) COMMENT '所属组织ID',
    `approver_id` VARCHAR(50) COMMENT '审批人ID',
    `approve_time` DATETIME COMMENT '审批时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX uk_username_deleted (username, is_deleted),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_approver_id` (`approver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
DROP TABLE IF EXISTS `pdm_tool_sys_role`;
CREATE TABLE `pdm_tool_sys_role` (
    `id` VARCHAR(50) NOT NULL COMMENT '角色ID',
    `name` VARCHAR(64) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(256) COMMENT '角色描述',
    `type` VARCHAR(32) DEFAULT 'SYSTEM' COMMENT '角色类型：SYSTEM-系统角色，CUSTOM-自定义角色',
    `scope_id` VARCHAR(50) COMMENT '作用域ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name_scope` (`name`, `scope_id`),
    KEY `idx_type` (`type`),
    KEY `idx_scope_id` (`scope_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
DROP TABLE IF EXISTS `pdm_tool_sys_permission`;
CREATE TABLE `pdm_tool_sys_permission` (
    `id` VARCHAR(50) NOT NULL COMMENT '权限ID',
    `name` VARCHAR(64) NOT NULL COMMENT '权限名称',
    `code` VARCHAR(128) NOT NULL COMMENT '权限编码',
    `description` VARCHAR(256) COMMENT '权限描述',
    `module` VARCHAR(64) COMMENT '所属模块',
    `type` VARCHAR(32) DEFAULT 'MENU' COMMENT '权限类型：MENU-菜单，BUTTON-按钮，API-接口',
    `parent_id` VARCHAR(50) DEFAULT '0' COMMENT '父权限ID',
    `level` INT DEFAULT 1 COMMENT '权限层级',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_module` (`module`),
    KEY `idx_type` (`type`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
DROP TABLE IF EXISTS `pdm_tool_sys_user_role`;
CREATE TABLE `pdm_tool_sys_user_role` (
    `id` VARCHAR(50) NOT NULL COMMENT '关联ID',
    `user_id` VARCHAR(50) NOT NULL COMMENT '用户ID',
    `role_id` VARCHAR(50) NOT NULL COMMENT '角色ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user` VARCHAR(50) COMMENT '创建人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
DROP TABLE IF EXISTS `pdm_tool_sys_role_permission`;
CREATE TABLE `pdm_tool_sys_role_permission` (
    `id` VARCHAR(50) NOT NULL COMMENT '关联ID',
    `role_id` VARCHAR(50) NOT NULL COMMENT '角色ID',
    `permission_id` VARCHAR(50) NOT NULL COMMENT '权限ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user` VARCHAR(50) COMMENT '创建人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 组织表
DROP TABLE IF EXISTS `pdm_tool_sys_organization`;
CREATE TABLE `pdm_tool_sys_organization` (
    `id` VARCHAR(50) NOT NULL COMMENT '组织ID',
    `name` VARCHAR(128) NOT NULL COMMENT '组织名称',
    `description` VARCHAR(512) COMMENT '组织描述',
    `parent_id` VARCHAR(50) DEFAULT '0' COMMENT '父组织ID',
    `level` INT DEFAULT 1 COMMENT '组织层级',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织表';

-- 用户组织关联表
DROP TABLE IF EXISTS `pdm_tool_sys_user_organization`;
CREATE TABLE `pdm_tool_sys_user_organization` (
    `id` VARCHAR(50) NOT NULL COMMENT '关联ID',
    `user_id` VARCHAR(50) NOT NULL COMMENT '用户ID',
    `org_id` VARCHAR(50) NOT NULL COMMENT '组织ID',
    `is_primary` TINYINT DEFAULT 0 COMMENT '是否主组织：0-否，1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_org` (`user_id`, `org_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户组织关联表';

-- 操作日志表
DROP TABLE IF EXISTS `pdm_tool_sys_operation_log`;
CREATE TABLE `pdm_tool_sys_operation_log` (
    `id` VARCHAR(50) NOT NULL COMMENT '日志ID',
    `user_id` VARCHAR(50) COMMENT '操作用户ID',
    `username` VARCHAR(64) COMMENT '操作用户名',
    `role_id` VARCHAR(50) COMMENT '角色ID',
    `operation` VARCHAR(128) NOT NULL COMMENT '操作内容',
    `module` VARCHAR(64) COMMENT '操作模块',
    `method` VARCHAR(128) COMMENT '操作方法',
    `request_url` VARCHAR(512) COMMENT '请求URL',
    `request_params` TEXT COMMENT '请求参数',
    `ip_address` VARCHAR(64) COMMENT 'IP地址',
    `user_agent` VARCHAR(512) COMMENT '用户代理',
    `status` TINYINT DEFAULT 1 COMMENT '操作状态：0-失败，1-成功',
    `error_message` TEXT COMMENT '错误信息',
    `execute_time` BIGINT COMMENT '执行时间(毫秒)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_module` (`module`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 系统配置表
DROP TABLE IF EXISTS `pdm_tool_sys_config`;
CREATE TABLE `pdm_tool_sys_config` (
    `id` VARCHAR(50) NOT NULL COMMENT '配置ID',
    `config_key` VARCHAR(128) NOT NULL COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_name` VARCHAR(128) COMMENT '配置名称',
    `description` VARCHAR(512) COMMENT '配置描述',
    `type` VARCHAR(32) DEFAULT 'TEXT' COMMENT '配置类型：TEXT-文本，JSON-JSON，BOOLEAN-布尔值',
    `is_encrypted` TINYINT DEFAULT 0 COMMENT '是否加密：0-否，1-是',
    `is_built_in` TINYINT DEFAULT 0 COMMENT '是否内置：0-否，1-是',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_user` VARCHAR(50) COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 字典表
DROP TABLE IF EXISTS `pdm_tool_sys_dict`;
CREATE TABLE `pdm_tool_sys_dict` (
    `id` VARCHAR(50) NOT NULL COMMENT '字典ID',
    `dict_type` VARCHAR(64) NOT NULL COMMENT '字典类型',
    `dict_code` VARCHAR(128) NOT NULL COMMENT '字典编码',
    `dict_value` VARCHAR(256) NOT NULL COMMENT '字典值',
    `description` VARCHAR(512) COMMENT '字典描述',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_type_code` (`dict_type`, `dict_code`),
    KEY `idx_dict_type` (`dict_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典表';

-- 登录日志表
DROP TABLE IF EXISTS `pdm_tool_sys_login_log`;
CREATE TABLE `pdm_tool_sys_login_log` (
    `id` VARCHAR(50) NOT NULL COMMENT '日志ID',
    `user_id` VARCHAR(50) COMMENT '用户ID',
    `username` VARCHAR(64) COMMENT '用户名',
    `ip_address` VARCHAR(64) COMMENT '登录IP',
    `user_agent` VARCHAR(512) COMMENT '用户代理',
    `login_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    `status` TINYINT DEFAULT 1 COMMENT '登录状态：0-失败，1-成功',
    `error_message` TEXT COMMENT '错误信息',
    `login_type` VARCHAR(32) DEFAULT 'LOCAL' COMMENT '登录类型：LOCAL-本地登录，LDAP-LDAP登录，OIDC-OIDC登录',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_username` (`username`),
    KEY `idx_login_time` (`login_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- ===========================================
-- 初始化数据
-- ===========================================

-- 插入默认管理员用户（密码：admin123，已加密）
INSERT IGNORE INTO `pdm_tool_sys_user` (`id`, `username`, `password`, `email`, `real_name`, `status`) VALUES
(1, 'admin', '$2a$10$SOb6oIm5cYI5l.DLRJgWJelN8l.hxk7ZlB21I8PAIw8KwVJG6srZe', 'admin@example.com', '系统管理员', 1);

-- 插入默认角色
INSERT IGNORE INTO `pdm_tool_sys_role` (`id`, `name`, `description`, `type`, `status`) VALUES
('admin', '系统管理员', '系统管理员，拥有所有权限', 'SYSTEM', 1),
('manager', '部门经理', '部门经理，拥有部门权限', 'SYSTEM', 1),
('user', '普通用户', '普通用户，拥有基础权限', 'SYSTEM', 1);

-- 插入用户角色关联
INSERT IGNORE INTO `pdm_tool_sys_user_role` (`id`, `user_id`, `role_id`) VALUES
('ur_admin', '1', 'admin');

-- 插入基础权限（示例）
INSERT IGNORE INTO `pdm_tool_sys_permission` (`id`, `name`, `code`, `description`, `module`, `type`, `parent_id`, `level`, `sort`) VALUES
('p1', '系统管理', 'system:management', '系统管理模块', 'system', 'MENU', '0', 1, 1);

-- ===========================================
-- 新增：用户权限直接分配表
-- ===========================================

-- 用户权限直接分配表（支持权限直接分配给用户）
DROP TABLE IF EXISTS `pdm_tool_sys_user_permission`;
CREATE TABLE `pdm_tool_sys_user_permission` (
    `id` VARCHAR(50) NOT NULL COMMENT '关联ID',
    `user_id` VARCHAR(50) NOT NULL COMMENT '用户ID',
    `permission_id` VARCHAR(50) NOT NULL COMMENT '权限ID',
    `permission_code` VARCHAR(128) NOT NULL COMMENT '权限编码（冗余字段，便于查询）',
    `grant_type` VARCHAR(32) DEFAULT 'DIRECT' COMMENT '授权类型：DIRECT-直接授权，INHERIT-继承授权',
    `scope_type` VARCHAR(32) DEFAULT 'GLOBAL' COMMENT '作用域类型：GLOBAL-全局，ORGANIZATION-组织内，PROJECT-项目内',
    `scope_id` VARCHAR(50) COMMENT '作用域ID（组织ID或项目ID）',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `start_time` DATETIME COMMENT '授权开始时间',
    `end_time` DATETIME COMMENT '授权结束时间',
    `is_temporary` TINYINT DEFAULT 0 COMMENT '是否临时授权：0-永久，1-临时',
    `grant_reason` VARCHAR(512) COMMENT '授权原因',
    `grant_user_id` VARCHAR(50) COMMENT '授权人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` VARCHAR(50) COMMENT '创建人',
    `update_user` VARCHAR(50) COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_permission_scope` (`user_id`, `permission_id`, `scope_type`, `scope_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_permission_id` (`permission_id`),
    KEY `idx_permission_code` (`permission_code`),
    KEY `idx_scope_type` (`scope_type`),
    KEY `idx_scope_id` (`scope_id`),
    KEY `idx_status` (`status`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_end_time` (`end_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户权限直接分配表';

-- 权限分配历史表（记录权限分配变更历史）
DROP TABLE IF EXISTS `pdm_tool_sys_permission_grant_history`;
CREATE TABLE `pdm_tool_sys_permission_grant_history` (
    `id` VARCHAR(50) NOT NULL COMMENT '历史记录ID',
    `user_id` VARCHAR(50) NOT NULL COMMENT '用户ID',
    `permission_id` VARCHAR(50) NOT NULL COMMENT '权限ID',
    `permission_code` VARCHAR(128) NOT NULL COMMENT '权限编码',
    `operation_type` VARCHAR(32) NOT NULL COMMENT '操作类型：GRANT-授权，REVOKE-撤销，UPDATE-更新',
    `old_status` TINYINT COMMENT '原状态',
    `new_status` TINYINT COMMENT '新状态',
    `old_start_time` DATETIME COMMENT '原开始时间',
    `new_start_time` DATETIME COMMENT '新开始时间',
    `old_end_time` DATETIME COMMENT '原结束时间',
    `new_end_time` DATETIME COMMENT '新结束时间',
    `scope_type` VARCHAR(32) COMMENT '作用域类型',
    `scope_id` VARCHAR(50) COMMENT '作用域ID',
    `operation_reason` VARCHAR(512) COMMENT '操作原因',
    `operator_id` VARCHAR(50) NOT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(64) NOT NULL COMMENT '操作人姓名',
    `operation_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `ip_address` VARCHAR(64) COMMENT '操作IP',
    `user_agent` VARCHAR(512) COMMENT '用户代理',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_permission_id` (`permission_id`),
    KEY `idx_permission_code` (`permission_code`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限分配历史表';

-- 插入用户权限直接分配的示例权限
INSERT IGNORE INTO `pdm_tool_sys_permission` (`id`, `name`, `code`, `description`, `module`, `type`, `parent_id`, `level`, `sort`) VALUES
('up1', '用户权限管理', 'system:user:permission:manage', '用户权限直接分配管理', 'system', 'MENU', 'p1', 2, 1),
('up2', '查看用户权限', 'system:user:permission:view', '查看用户权限分配', 'system', 'BUTTON', 'up1', 3, 1),
('up3', '分配用户权限', 'system:user:permission:grant', '为用户分配权限', 'system', 'BUTTON', 'up1', 3, 2),
('up4', '撤销用户权限', 'system:user:permission:revoke', '撤销用户权限', 'system', 'BUTTON', 'up1', 3, 3),
('up5', '批量分配权限', 'system:user:permission:batch', '批量分配用户权限', 'system', 'BUTTON', 'up1', 3, 4);

-- 为管理员角色分配用户权限管理权限
INSERT IGNORE INTO `pdm_tool_sys_role_permission` (`id`, `role_id`, `permission_id`) VALUES
('rp_up1', 'admin', 'up1'),
('rp_up2', 'admin', 'up2'),
('rp_up3', 'admin', 'up3'),
('rp_up4', 'admin', 'up4'),
('rp_up5', 'admin', 'up5');

-- 继续插入基础权限（示例）
INSERT IGNORE INTO `pdm_tool_sys_permission` (`id`, `name`, `code`, `description`, `module`, `type`, `parent_id`, `level`, `sort`) VALUES
('p2', '用户管理', 'system:user', '用户管理', 'system', 'MENU', 'p1', 2, 1),
('p3', '角色管理', 'system:role', '角色管理', 'system', 'MENU', 'p1', 2, 2),
('p4', '权限管理', 'system:permission', '权限管理', 'system', 'MENU', 'p1', 2, 3),
('p5', '用户管理API', 'system:user:api', '用户管理接口权限', 'system', 'API', 'p2', 3, 1),
('p6', '角色管理API', 'system:role:api', '角色管理接口权限', 'system', 'API', 'p3', 3, 1),
('p7', '权限管理API', 'system:permission:api', '权限管理接口权限', 'system', 'API', 'p4', 3, 1),
('p8', '日志管理', 'system:log', '日志管理', 'system', 'MENU', 'p1', 2, 4),
('p9', '日志管理API', 'system:log:api', '日志管理接口权限', 'system', 'API', 'p8', 3, 1),
('p10', '数据字典管理', 'system:dictionary', '数据字典管理', 'system', 'MENU', 'p1', 2, 4);





-- 插入角色权限关联
INSERT IGNORE INTO `pdm_tool_sys_role_permission` (`id`, `role_id`, `permission_id`) VALUES
('rp1', 'admin', 'p1'),
('rp2', 'admin', 'p2'),
('rp3', 'admin', 'p3'),
('rp4', 'admin', 'p4');

-- 插入默认配置
INSERT IGNORE INTO `pdm_tool_sys_config` (`id`, `config_key`, `config_value`, `config_name`, `description`, `is_built_in`, `status`) VALUES
('config1', 'system.name', '工具测试平台', '系统名称', '系统显示名称', 1, 1),
('config2', 'system.version', '1.0.0', '系统版本', '系统版本号', 1, 1);

-- ===========================================
-- 添加API类型权限
-- ===========================================

-- 插入API类型的权限
INSERT IGNORE INTO `pdm_tool_sys_permission` (`id`, `name`, `code`, `description`, `module`, `type`, `parent_id`, `level`, `sort`) VALUES
                                                                                                                       ('p5', '用户管理API', 'system:user:api', '用户管理接口权限', 'system', 'API', 'p2', 3, 1),
                                                                                                                       ('p6', '角色管理API', 'system:role:api', '角色管理接口权限', 'system', 'API', 'p3', 3, 1),
                                                                                                                       ('p7', '权限管理API', 'system:permission:api', '权限管理接口权限', 'system', 'API', 'p4', 3, 1),
                                                                                                                       ('p11', '数据字典管理API', 'system:dictionary:api', '数据字典管理接口权限', 'system', 'API', 'p10', 3, 1);

-- ===========================================
-- 补充协议配置管理和测试模板管理模块的权限记录
-- ===========================================

-- 插入协议配置管理模块的权限记录（使用INSERT IGNORE避免重复）
INSERT IGNORE INTO `pdm_tool_sys_permission` (`id`, `name`, `code`, `description`, `module`, `type`, `parent_id`, `level`, `sort`) VALUES
-- ====================== 协议配置管理模块 ======================
-- 一级菜单权限
('protocol_m1', '协议配置管理', 'protocol:config', '协议配置管理模块', 'protocol', 'MENU', '0', 1, 10),

-- 二级菜单权限
('protocol_m2', '协议类型管理', 'protocol:type', '协议类型管理', 'protocol', 'MENU', 'protocol_m1', 2, 10),
('protocol_m3', '协议参数配置', 'protocol:param', '协议参数配置', 'protocol', 'MENU', 'protocol_m1', 2, 20),
('protocol_m4', '协议测试', 'protocol:test', '协议测试功能', 'protocol', 'MENU', 'protocol_m1', 2, 30),
('protocol_m5', '协议搜索与过滤', 'protocol:search', '协议搜索与过滤', 'protocol', 'MENU', 'protocol_m1', 2, 40),
('protocol_m6', '协议导入导出', 'protocol:importExport', '协议导入导出', 'protocol', 'MENU', 'protocol_m1', 2, 50),
('protocol_m7', '协议权限设置', 'protocol:permission', '协议权限设置', 'protocol', 'MENU', 'protocol_m1', 2, 60),
('protocol_m8', '协议状态管理', 'protocol:status', '协议状态管理', 'protocol', 'MENU', 'protocol_m1', 2, 70),
('protocol_m9', 'CAD软件配置', 'protocol:cad', 'CAD软件配置', 'protocol', 'MENU', 'protocol_m1', 2, 80),
('protocol_m10', 'ERP软件配置', 'protocol:erp', 'ERP软件配置', 'protocol', 'MENU', 'protocol_m1', 2, 90),

-- 三级功能权限 - 协议类型管理
('protocol_m11', '新增协议类型', 'protocol:type:add', '新增协议类型功能', 'protocol', 'BUTTON', 'protocol_m2', 3, 1),
('protocol_m12', '编辑协议类型', 'protocol:type:edit', '编辑协议类型功能', 'protocol', 'BUTTON', 'protocol_m2', 3, 2),
('protocol_m13', '删除协议类型', 'protocol:type:delete', '删除协议类型功能', 'protocol', 'BUTTON', 'protocol_m2', 3, 3),
('protocol_m14', '查询协议类型', 'protocol:type:query', '查询协议类型功能', 'protocol', 'BUTTON', 'protocol_m2', 3, 4),
('protocol_m15', '按类型筛选协议', 'protocol:type:filter', '按类型筛选协议功能', 'protocol', 'BUTTON', 'protocol_m2', 3, 5),
('protocol_m16', '导出协议类型列表', 'protocol:type:export', '导出协议类型列表功能', 'protocol', 'BUTTON', 'protocol_m2', 3, 6),
('protocol_m17', '导入协议类型', 'protocol:type:import', '导入协议类型功能', 'protocol', 'BUTTON', 'protocol_m2', 3, 7),
('protocol_m18', '协议类型状态管理', 'protocol:type:status', '协议类型状态管理功能', 'protocol', 'BUTTON', 'protocol_m2', 3, 8),
('protocol_m19', '协议类型关联项目', 'protocol:type:relateProject', '协议类型关联项目功能', 'protocol', 'BUTTON', 'protocol_m2', 3, 9),

-- 三级功能权限 - 协议参数配置
('protocol_m20', '配置协议URL', 'protocol:param:url', '配置协议URL功能', 'protocol', 'BUTTON', 'protocol_m3', 3, 10),
('protocol_m21', '配置协议端口', 'protocol:param:port', '配置协议端口功能', 'protocol', 'BUTTON', 'protocol_m3', 3, 11),
('protocol_m22', '配置认证方式', 'protocol:param:auth', '配置认证方式功能', 'protocol', 'BUTTON', 'protocol_m3', 3, 12),
('protocol_m23', '配置超时时间', 'protocol:param:timeout', '配置超时时间功能', 'protocol', 'BUTTON', 'protocol_m3', 3, 13),
('protocol_m24', '配置重试机制', 'protocol:param:retry', '配置重试机制功能', 'protocol', 'BUTTON', 'protocol_m3', 3, 14),
('protocol_m25', '配置数据格式', 'protocol:param:format', '配置数据格式功能', 'protocol', 'BUTTON', 'protocol_m3', 3, 15),
('protocol_m26', '配置协议参数模板', 'protocol:param:template', '配置协议参数模板功能', 'protocol', 'BUTTON', 'protocol_m3', 3, 16),
('protocol_m27', '参数配置导入导出', 'protocol:param:importExport', '参数配置导入导出功能', 'protocol', 'BUTTON', 'protocol_m3', 3, 17),

-- 三级功能权限 - 协议测试
('protocol_m28', '测试连接', 'protocol:test:connect', '测试连接功能', 'protocol', 'BUTTON', 'protocol_m4', 3, 18),
('protocol_m29', '测试数据传输', 'protocol:test:transfer', '测试数据传输功能', 'protocol', 'BUTTON', 'protocol_m4', 3, 19),
('protocol_m30', '测试结果导出', 'protocol:test:export', '测试结果导出功能', 'protocol', 'BUTTON', 'protocol_m4', 3, 20),
('protocol_m31', '测试参数配置', 'protocol:test:param', '测试参数配置功能', 'protocol', 'BUTTON', 'protocol_m4', 3, 21),

-- 三级功能权限 - 协议搜索与过滤
('protocol_m32', '按名称/类型/状态搜索', 'protocol:search:basic', '按名称/类型/状态搜索功能', 'protocol', 'BUTTON', 'protocol_m5', 3, 22),
('protocol_m33', '按创建时间/更新时间筛选', 'protocol:search:time', '按创建时间/更新时间筛选功能', 'protocol', 'BUTTON', 'protocol_m5', 3, 23),
('protocol_m34', '组合条件搜索', 'protocol:search:complex', '组合条件搜索功能', 'protocol', 'BUTTON', 'protocol_m5', 3, 24),
('protocol_m35', '导出搜索结果', 'protocol:search:export', '导出搜索结果功能', 'protocol', 'BUTTON', 'protocol_m5', 3, 25),

-- 三级功能权限 - 协议导入导出
('protocol_m36', '导入协议配置', 'protocol:importExport:import', '导入协议配置功能', 'protocol', 'BUTTON', 'protocol_m6', 3, 26),
('protocol_m37', '导出协议配置', 'protocol:importExport:export', '导出协议配置功能', 'protocol', 'BUTTON', 'protocol_m6', 3, 27),
('protocol_m38', '导入格式验证', 'protocol:importExport:validate', '导入格式验证功能', 'protocol', 'BUTTON', 'protocol_m6', 3, 28),
('protocol_m39', '导入错误处理', 'protocol:importExport:error', '导入错误处理功能', 'protocol', 'BUTTON', 'protocol_m6', 3, 29),
('protocol_m40', '导入参数配置', 'protocol:importExport:paramImport', '导入参数配置功能', 'protocol', 'BUTTON', 'protocol_m6', 3, 30),
('protocol_m41', '导出参数配置', 'protocol:importExport:paramExport', '导出参数配置功能', 'protocol', 'BUTTON', 'protocol_m6', 3, 31),

-- 三级功能权限 - 协议权限设置
('protocol_m42', '为角色设置权限', 'protocol:permission:role', '为角色设置权限功能', 'protocol', 'BUTTON', 'protocol_m7', 3, 32),
('protocol_m43', '为用户设置权限', 'protocol:permission:user', '为用户设置权限功能', 'protocol', 'BUTTON', 'protocol_m7', 3, 33),
('protocol_m44', '权限分配', 'protocol:permission:assign', '权限分配功能', 'protocol', 'BUTTON', 'protocol_m7', 3, 34),
('protocol_m45', '权限查看', 'protocol:permission:view', '权限查看功能', 'protocol', 'BUTTON', 'protocol_m7', 3, 35),
('protocol_m46', '权限修改', 'protocol:permission:edit', '权限修改功能', 'protocol', 'BUTTON', 'protocol_m7', 3, 36),
('protocol_m47', '权限删除', 'protocol:permission:delete', '权限删除功能', 'protocol', 'BUTTON', 'protocol_m7', 3, 37),
('protocol_m48', '权限批量设置', 'protocol:permission:batch', '权限批量设置功能', 'protocol', 'BUTTON', 'protocol_m7', 3, 38),
('protocol_m49', '权限导出', 'protocol:permission:export', '权限导出功能', 'protocol', 'BUTTON', 'protocol_m7', 3, 39),
('protocol_m50', '权限导入', 'protocol:permission:import', '权限导入功能', 'protocol', 'BUTTON', 'protocol_m7', 3, 40),

-- 三级功能权限 - 协议状态管理
('protocol_m51', '状态批量操作', 'protocol:status:batch', '状态批量操作功能', 'protocol', 'BUTTON', 'protocol_m8', 3, 41),

-- 三级功能权限 - CAD软件配置
('protocol_m52', '配置NX软件模拟接口', 'protocol:cad:nx', '配置NX软件模拟接口功能', 'protocol', 'BUTTON', 'protocol_m9', 3, 42),
('protocol_m53', '配置CATIA软件模拟接口', 'protocol:cad:catia', '配置CATIA软件模拟接口功能', 'protocol', 'BUTTON', 'protocol_m9', 3, 43),
('protocol_m54', '配置中望CAD软件模拟接口', 'protocol:cad:zwcad', '配置中望CAD软件模拟接口功能', 'protocol', 'BUTTON', 'protocol_m9', 3, 44),
('protocol_m55', '配置自定义CAD软件模拟接口', 'protocol:cad:custom', '配置自定义CAD软件模拟接口功能', 'protocol', 'BUTTON', 'protocol_m9', 3, 45),
('protocol_m56', '配置CAD软件STEP数据交换格式', 'protocol:cad:step', '配置CAD软件STEP数据交换格式功能', 'protocol', 'BUTTON', 'protocol_m9', 3, 46),
('protocol_m57', '配置CAD软件IGES数据交换格式', 'protocol:cad:iges', '配置CAD软件IGES数据交换格式功能', 'protocol', 'BUTTON', 'protocol_m9', 3, 47),
('protocol_m58', '配置CAD软件自定义数据交换格式', 'protocol:cad:customFormat', '配置CAD软件自定义数据交换格式功能', 'protocol', 'BUTTON', 'protocol_m9', 3, 48),
('protocol_m59', 'CAD接口连通性测试', 'protocol:cad:test', 'CAD接口连通性测试功能', 'protocol', 'BUTTON', 'protocol_m9', 3, 49),
('protocol_m60', '配置CAD接口认证方式', 'protocol:cad:auth', '配置CAD接口认证方式功能', 'protocol', 'BUTTON', 'protocol_m9', 3, 50),

-- 三级功能权限 - ERP软件配置
('protocol_m61', '配置浪潮ERP软件模拟接口', 'protocol:erp:langchao', '配置浪潮ERP软件模拟接口功能', 'protocol', 'BUTTON', 'protocol_m10', 3, 51),
('protocol_m62', '配置自定义ERP软件模拟接口', 'protocol:erp:custom', '配置自定义ERP软件模拟接口功能', 'protocol', 'BUTTON', 'protocol_m10', 3, 52),
('protocol_m63', '配置ERP软件通用BOM表数据交换格式', 'protocol:erp:bom', '配置ERP软件通用BOM表数据交换格式功能', 'protocol', 'BUTTON', 'protocol_m10', 3, 53),
('protocol_m64', '配置ERP软件自定义数据交换格式', 'protocol:erp:customFormat', '配置ERP软件自定义数据交换格式功能', 'protocol', 'BUTTON', 'protocol_m10', 3, 54),
('protocol_m65', 'ERP接口连通性测试', 'protocol:erp:test', 'ERP接口连通性测试功能', 'protocol', 'BUTTON', 'protocol_m10', 3, 55),

-- ====================== 测试模板管理模块 ======================
-- 一级菜单权限
('test_m66', '测试模板管理', 'test:template', '测试模板管理模块', 'test', 'MENU', '0', 1, 20),

-- 二级菜单权限
('test_m67', 'ERP软件配置（自动化）', 'test:template:erpConfig', 'ERP软件配置（自动化）', 'test', 'MENU', 'test_m66', 2, 1),
('test_m68', '模板创建', 'test:template:create', '模板创建', 'test', 'MENU', 'test_m66', 2, 20),
('test_m69', '模板编辑', 'test:template:edit', '模板编辑', 'test', 'MENU', 'test_m66', 2, 30),
('test_m70', '模板删除', 'test:template:delete', '模板删除', 'test', 'MENU', 'test_m66', 2, 40),
('test_m71', '模板搜索', 'test:template:search', '模板搜索', 'test', 'MENU', 'test_m66', 2, 50),
('test_m72', '模板版本管理', 'test:template:version', '模板版本管理', 'test', 'MENU', 'test_m66', 2, 60),
('test_m73', '模板关联任务', 'test:template:relateTask', '模板关联任务', 'test', 'MENU', 'test_m66', 2, 70),
('test_m74', '模板参数配置', 'test:template:param', '模板参数配置', 'test', 'MENU', 'test_m66', 2, 80),

-- 三级功能权限 - ERP软件配置（自动化）
('test_m75', '配置ERP接口认证方式', 'test:template:erp:auth', '配置ERP接口认证方式功能', 'test', 'BUTTON', 'test_m67', 3, 56),
('test_m76', '并发接口配置', 'test:template:erp:concurrency', '并发接口配置功能', 'test', 'BUTTON', 'test_m67', 3, 57),
('test_m77', '自动化脚本配置', 'test:template:erp:script', '自动化脚本配置功能', 'test', 'BUTTON', 'test_m67', 3, 58),
('test_m78', '自动化日志记录配置', 'test:template:erp:log', '自动化日志记录配置功能', 'test', 'BUTTON', 'test_m67', 3, 59),
('test_m79', '接口传输报告导出', 'test:template:erp:export', '接口传输报告导出功能', 'test', 'BUTTON', 'test_m67', 3, 60),

-- 三级功能权限 - 模板创建
('test_m80', '新建测试模板', 'test:template:create:add', '新建测试模板功能', 'test', 'BUTTON', 'test_m68', 3, 61),
('test_m81', '模板基本信息配置', 'test:template:create:info', '模板基本信息配置功能', 'test', 'BUTTON', 'test_m68', 3, 62);



-- 为admin角色分配API权限
INSERT IGNORE INTO `pdm_tool_sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`, `create_user`) VALUES
                                                                                                       ('rp5', 'admin', 'p5', NOW(), 'admin'),
                                                                                                       ('rp6', 'admin', 'p6', NOW(), 'admin'),
                                                                                                       ('rp7', 'admin', 'p7', NOW(), 'admin'),
                                                                                                       ('rp8', 'admin', 'p8', NOW(), 'admin'),
                                                                                                       ('rp9', 'admin', 'p9', NOW(), 'admin');

-- 为manager角色分配部分API权限
INSERT IGNORE INTO `pdm_tool_sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`, `create_user`) VALUES
    ('rp10', 'manager', 'p5', NOW(), 'admin');

-- ===========================================
-- 为admin角色分配新增的权限
-- ===========================================

-- 为admin角色分配协议配置管理模块的所有权限（使用INSERT IGNORE避免重复）
INSERT IGNORE INTO `pdm_tool_sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`, `create_user`) VALUES
-- 协议配置管理模块权限分配
('rp_protocol_m1', 'admin', 'protocol_m1', NOW(), 'admin'),
('rp_protocol_m2', 'admin', 'protocol_m2', NOW(), 'admin'),
('rp_protocol_m3', 'admin', 'protocol_m3', NOW(), 'admin'),
('rp_protocol_m4', 'admin', 'protocol_m4', NOW(), 'admin'),
('rp_protocol_m5', 'admin', 'protocol_m5', NOW(), 'admin'),
('rp_protocol_m6', 'admin', 'protocol_m6', NOW(), 'admin'),
('rp_protocol_m7', 'admin', 'protocol_m7', NOW(), 'admin'),
('rp_protocol_m8', 'admin', 'protocol_m8', NOW(), 'admin'),
('rp_protocol_m9', 'admin', 'protocol_m9', NOW(), 'admin'),
('rp_protocol_m10', 'admin', 'protocol_m10', NOW(), 'admin'),

-- 为admin角色分配测试模板管理模块的所有权限
('rp_test_m66', 'admin', 'test_m66', NOW(), 'admin'),
('rp_test_m67', 'admin', 'test_m67', NOW(), 'admin'),
('rp_test_m68', 'admin', 'test_m68', NOW(), 'admin'),
('rp_test_m69', 'admin', 'test_m69', NOW(), 'admin'),
('rp_test_m70', 'admin', 'test_m70', NOW(), 'admin'),
('rp_test_m71', 'admin', 'test_m71', NOW(), 'admin'),
('rp_test_m72', 'admin', 'test_m72', NOW(), 'admin'),
('rp_test_m73', 'admin', 'test_m73', NOW(), 'admin'),
('rp_test_m74', 'admin', 'test_m74', NOW(), 'admin');

DROP TABLE IF EXISTS `pdm_tool_sys_menu`;
CREATE TABLE pdm_tool_sys_menu (
                          id VARCHAR(50) NOT NULL COMMENT '菜单ID',
                          name VARCHAR(100) NOT NULL COMMENT '菜单/功能名称',
                          code VARCHAR(128) NOT NULL COMMENT '权限编码',
                          description VARCHAR(256) COMMENT '菜单描述',
                          module VARCHAR(64) COMMENT '所属模块',
                          type VARCHAR(32) DEFAULT 'MENU' COMMENT '菜单类型：MENU-菜单，BUTTON-按钮，API-接口',
                          parent_id VARCHAR(50) DEFAULT '0' COMMENT '父菜单ID',
                          level INT DEFAULT 1 COMMENT '菜单层级',
                          sort INT DEFAULT 0 COMMENT '排序',
                          status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `uk_code` (`code`),
                          KEY `idx_module` (`module`),
                          KEY `idx_type` (`type`),
                          KEY `idx_parent_id` (`parent_id`),
                          KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单表';

INSERT IGNORE INTO `pdm_tool_sys_menu` (`id`, `name`, `code`, `description`, `module`, `type`, `parent_id`, `level`, `sort`, `status`) VALUES
-- ====================== 一、协议配置管理 ======================
('m1', '协议配置管理', 'protocol:config', '协议配置管理模块', 'protocol', 'MENU', '0', 1, 10, 1),

-- 二级菜单
('m2', '协议类型管理', 'protocol:type', '协议类型管理', 'protocol', 'MENU', 'm1', 2, 10, 1),
('m3', '协议参数配置', 'protocol:param', '协议参数配置', 'protocol', 'MENU', 'm1', 2, 20, 1),
('m4', '协议测试', 'protocol:test', '协议测试功能', 'protocol', 'MENU', 'm1', 2, 30, 1),
('m5', '协议搜索与过滤', 'protocol:search', '协议搜索与过滤', 'protocol', 'MENU', 'm1', 2, 40, 1),
('m6', '协议导入导出', 'protocol:importExport', '协议导入导出', 'protocol', 'MENU', 'm1', 2, 50, 1),
('m7', '协议权限设置', 'protocol:permission', '协议权限设置', 'protocol', 'MENU', 'm1', 2, 60, 1),
('m8', '协议状态管理', 'protocol:status', '协议状态管理', 'protocol', 'MENU', 'm1', 2, 70, 1),
('m9', 'CAD软件配置', 'protocol:cad', 'CAD软件配置', 'protocol', 'MENU', 'm1', 2, 80, 1),
('m10', 'ERP软件配置', 'protocol:erp', 'ERP软件配置', 'protocol', 'MENU', 'm1', 2, 90, 1),

-- 三级功能
('m11', '新增协议类型', 'protocol:type:add', '新增协议类型功能', 'protocol', 'BUTTON', 'm2', 3, 1, 1),
('m12', '编辑协议类型', 'protocol:type:edit', '编辑协议类型功能', 'protocol', 'BUTTON', 'm2', 3, 2, 1),
('m13', '删除协议类型', 'protocol:type:delete', '删除协议类型功能', 'protocol', 'BUTTON', 'm2', 3, 3, 1),
('m14', '查询协议类型', 'protocol:type:query', '查询协议类型功能', 'protocol', 'BUTTON', 'm2', 3, 4, 1),
('m15', '按类型筛选协议', 'protocol:type:filter', '按类型筛选协议功能', 'protocol', 'BUTTON', 'm2', 3, 5, 1),
('m16', '导出协议类型列表', 'protocol:type:export', '导出协议类型列表功能', 'protocol', 'BUTTON', 'm2', 3, 6, 1),
('m17', '导入协议类型', 'protocol:type:import', '导入协议类型功能', 'protocol', 'BUTTON', 'm2', 3, 7, 1),
('m18', '协议类型状态管理', 'protocol:type:status', '协议类型状态管理功能', 'protocol', 'BUTTON', 'm2', 3, 8, 1),
('m19', '协议类型关联项目', 'protocol:type:relateProject', '协议类型关联项目功能', 'protocol', 'BUTTON', 'm2', 3, 9, 1),

('m20', '配置协议URL', 'protocol:param:url', '配置协议URL功能', 'protocol', 'BUTTON', 'm3', 3, 10, 1),
('m21', '配置协议端口', 'protocol:param:port', '配置协议端口功能', 'protocol', 'BUTTON', 'm3', 3, 11, 1),
('m22', '配置认证方式', 'protocol:param:auth', '配置认证方式功能', 'protocol', 'BUTTON', 'm3', 3, 12, 1),
('m23', '配置超时时间', 'protocol:param:timeout', '配置超时时间功能', 'protocol', 'BUTTON', 'm3', 3, 13, 1),
('m24', '配置重试机制', 'protocol:param:retry', '配置重试机制功能', 'protocol', 'BUTTON', 'm3', 3, 14, 1),
('m25', '配置数据格式', 'protocol:param:format', '配置数据格式功能', 'protocol', 'BUTTON', 'm3', 3, 15, 1),
('m26', '配置协议参数模板', 'protocol:param:template', '配置协议参数模板功能', 'protocol', 'BUTTON', 'm3', 3, 16, 1),
('m27', '参数配置导入导出', 'protocol:param:importExport', '参数配置导入导出功能', 'protocol', 'BUTTON', 'm3', 3, 17, 1),

('m28', '测试连接', 'protocol:test:connect', '测试连接功能', 'protocol', 'BUTTON', 'm4', 3, 18, 1),
('m29', '测试数据传输', 'protocol:test:transfer', '测试数据传输功能', 'protocol', 'BUTTON', 'm4', 3, 19, 1),
('m30', '测试结果导出', 'protocol:test:export', '测试结果导出功能', 'protocol', 'BUTTON', 'm4', 3, 20, 1),
('m31', '测试参数配置', 'protocol:test:param', '测试参数配置功能', 'protocol', 'BUTTON', 'm4', 3, 21, 1),

('m32', '按名称/类型/状态搜索', 'protocol:search:basic', '按名称/类型/状态搜索功能', 'protocol', 'BUTTON', 'm5', 3, 22, 1),
('m33', '按创建时间/更新时间筛选', 'protocol:search:time', '按创建时间/更新时间筛选功能', 'protocol', 'BUTTON', 'm5', 3, 23, 1),
('m34', '组合条件搜索', 'protocol:search:complex', '组合条件搜索功能', 'protocol', 'BUTTON', 'm5', 3, 24, 1),
('m35', '导出搜索结果', 'protocol:search:export', '导出搜索结果功能', 'protocol', 'BUTTON', 'm5', 3, 25, 1),

('m36', '导入协议配置', 'protocol:importExport:import', '导入协议配置功能', 'protocol', 'BUTTON', 'm6', 3, 26, 1),
('m37', '导出协议配置', 'protocol:importExport:export', '导出协议配置功能', 'protocol', 'BUTTON', 'm6', 3, 27, 1),
('m38', '导入格式验证', 'protocol:importExport:validate', '导入格式验证功能', 'protocol', 'BUTTON', 'm6', 3, 28, 1),
('m39', '导入错误处理', 'protocol:importExport:error', '导入错误处理功能', 'protocol', 'BUTTON', 'm6', 3, 29, 1),
('m40', '导入参数配置', 'protocol:importExport:paramImport', '导入参数配置功能', 'protocol', 'BUTTON', 'm6', 3, 30, 1),
('m41', '导出参数配置', 'protocol:importExport:paramExport', '导出参数配置功能', 'protocol', 'BUTTON', 'm6', 3, 31, 1),

('m42', '为角色设置权限', 'protocol:permission:role', '为角色设置权限功能', 'protocol', 'BUTTON', 'm7', 3, 32, 1),
('m43', '为用户设置权限', 'protocol:permission:user', '为用户设置权限功能', 'protocol', 'BUTTON', 'm7', 3, 33, 1),
('m44', '权限分配', 'protocol:permission:assign', '权限分配功能', 'protocol', 'BUTTON', 'm7', 3, 34, 1),
('m45', '权限查看', 'protocol:permission:view', '权限查看功能', 'protocol', 'BUTTON', 'm7', 3, 35, 1),
('m46', '权限修改', 'protocol:permission:edit', '权限修改功能', 'protocol', 'BUTTON', 'm7', 3, 36, 1),
('m47', '权限删除', 'protocol:permission:delete', '权限删除功能', 'protocol', 'BUTTON', 'm7', 3, 37, 1),
('m48', '权限批量设置', 'protocol:permission:batch', '权限批量设置功能', 'protocol', 'BUTTON', 'm7', 3, 38, 1),
('m49', '权限导出', 'protocol:permission:export', '权限导出功能', 'protocol', 'BUTTON', 'm7', 3, 39, 1),
('m50', '权限导入', 'protocol:permission:import', '权限导入功能', 'protocol', 'BUTTON', 'm7', 3, 40, 1),

('m51', '状态批量操作', 'protocol:status:batch', '状态批量操作功能', 'protocol', 'BUTTON', 'm8', 3, 41, 1),

('m52', '配置NX软件模拟接口', 'protocol:cad:nx', '配置NX软件模拟接口功能', 'protocol', 'BUTTON', 'm9', 3, 42, 1),
('m53', '配置CATIA软件模拟接口', 'protocol:cad:catia', '配置CATIA软件模拟接口功能', 'protocol', 'BUTTON', 'm9', 3, 43, 1),
('m54', '配置中望CAD软件模拟接口', 'protocol:cad:zwcad', '配置中望CAD软件模拟接口功能', 'protocol', 'BUTTON', 'm9', 3, 44, 1),
('m55', '配置自定义CAD软件模拟接口', 'protocol:cad:custom', '配置自定义CAD软件模拟接口功能', 'protocol', 'BUTTON', 'm9', 3, 45, 1),
('m56', '配置CAD软件STEP数据交换格式', 'protocol:cad:step', '配置CAD软件STEP数据交换格式功能', 'protocol', 'BUTTON', 'm9', 3, 46, 1),
('m57', '配置CAD软件IGES数据交换格式', 'protocol:cad:iges', '配置CAD软件IGES数据交换格式功能', 'protocol', 'BUTTON', 'm9', 3, 47, 1),
('m58', '配置CAD软件自定义数据交换格式', 'protocol:cad:customFormat', '配置CAD软件自定义数据交换格式功能', 'protocol', 'BUTTON', 'm9', 3, 48, 1),
('m59', 'CAD接口连通性测试', 'protocol:cad:test', 'CAD接口连通性测试功能', 'protocol', 'BUTTON', 'm9', 3, 49, 1),
('m60', '配置CAD接口认证方式', 'protocol:cad:auth', '配置CAD接口认证方式功能', 'protocol', 'BUTTON', 'm9', 3, 50, 1),

('m61', '配置浪潮ERP软件模拟接口', 'protocol:erp:langchao', '配置浪潮ERP软件模拟接口功能', 'protocol', 'BUTTON', 'm10', 3, 51, 1),
('m62', '配置自定义ERP软件模拟接口', 'protocol:erp:custom', '配置自定义ERP软件模拟接口功能', 'protocol', 'BUTTON', 'm10', 3, 52, 1),
('m63', '配置ERP软件通用BOM表数据交换格式', 'protocol:erp:bom', '配置ERP软件通用BOM表数据交换格式功能', 'protocol', 'BUTTON', 'm10', 3, 53, 1),
('m64', '配置ERP软件自定义数据交换格式', 'protocol:erp:customFormat', '配置ERP软件自定义数据交换格式功能', 'protocol', 'BUTTON', 'm10', 3, 54, 1),
('m65', 'ERP接口连通性测试', 'protocol:erp:test', 'ERP接口连通性测试功能', 'protocol', 'BUTTON', 'm10', 3, 55, 1),

-- ====================== 二、测试模板管理 ======================
('m66', '测试模板管理', 'test:template', '测试模板管理模块', 'test', 'MENU', '0', 1, 20, 1),

('m67', 'ERP软件配置（自动化）', 'test:template:erpConfig', 'ERP软件配置（自动化）', 'test', 'MENU', 'm66', 2, 1, 1),
('m68', '模板创建', 'test:template:create', '模板创建', 'test', 'MENU', 'm66', 2, 20, 1),
('m69', '模板编辑', 'test:template:edit', '模板编辑', 'test', 'MENU', 'm66', 2, 30, 1),
('m70', '模板删除', 'test:template:delete', '模板删除', 'test', 'MENU', 'm66', 2, 40, 1),
('m71', '模板搜索', 'test:template:search', '模板搜索', 'test', 'MENU', 'm66', 2, 50, 1),
('m72', '模板版本管理', 'test:template:version', '模板版本管理', 'test', 'MENU', 'm66', 2, 60, 1),
('m73', '模板关联任务', 'test:template:relateTask', '模板关联任务', 'test', 'MENU', 'm66', 2, 70, 1),
('m74', '模板参数配置', 'test:template:param', '模板参数配置', 'test', 'MENU', 'm66', 2, 80, 1),

('m75', '配置ERP接口认证方式', 'test:template:erp:auth', '配置ERP接口认证方式功能', 'test', 'BUTTON', 'm67', 3, 56, 1),
('m76', '并发接口配置', 'test:template:erp:concurrency', '并发接口配置功能', 'test', 'BUTTON', 'm67', 3, 57, 1),
('m77', '自动化脚本配置', 'test:template:erp:script', '自动化脚本配置功能', 'test', 'BUTTON', 'm67', 3, 58, 1),
('m78', '自动化日志记录配置', 'test:template:erp:log', '自动化日志记录配置功能', 'test', 'BUTTON', 'm67', 3, 59, 1),
('m79', '接口传输报告导出', 'test:template:erp:export', '接口传输报告导出功能', 'test', 'BUTTON', 'm67', 3, 60, 1),

('m80', '新建测试模板', 'test:template:create:add', '新建测试模板功能', 'test', 'BUTTON', 'm68', 3, 61, 1),
('m81', '模板基本信息配置', 'test:template:create:info', '模板基本信息配置功能', 'test', 'BUTTON', 'm68', 3, 62, 1),
('m82', '模板类型选择', 'test:template:create:type', '模板类型选择功能', 'test', 'BUTTON', 'm68', 3, 63, 1),
('m83', '模板描述填写', 'test:template:create:desc', '模板描述填写功能', 'test', 'BUTTON', 'm68', 3, 64, 1),
('m84', '保存模板', 'test:template:create:save', '保存模板功能', 'test', 'BUTTON', 'm68', 3, 65, 1),
('m85', '模板关联协议', 'test:template:create:relateProtocol', '模板关联协议功能', 'test', 'BUTTON', 'm68', 3, 66, 1),
('m86', '模板参数配置', 'test:template:create:param', '模板参数配置功能', 'test', 'BUTTON', 'm68', 3, 67, 1),
('m87', '模板导入', 'test:template:create:import', '模板导入功能', 'test', 'BUTTON', 'm68', 3, 68, 1),

('m88', '编辑模板内容', 'test:template:edit:content', '编辑模板内容功能', 'test', 'BUTTON', 'm69', 3, 69, 1),
('m89', '模板关联修改', 'test:template:edit:relate', '模板关联修改功能', 'test', 'BUTTON', 'm69', 3, 70, 1),
('m90', '模板参数调整', 'test:template:edit:param', '模板参数调整功能', 'test', 'BUTTON', 'm69', 3, 71, 1),
('m91', '模板导出', 'test:template:edit:export', '模板导出功能', 'test', 'BUTTON', 'm69', 3, 72, 1),

('m92', '删除模板', 'test:template:delete:single', '删除模板功能', 'test', 'BUTTON', 'm70', 3, 73, 1),
('m93', '批量删除', 'test:template:delete:batch', '批量删除功能', 'test', 'BUTTON', 'm70', 3, 74, 1),
('m94', '模板关联清理', 'test:template:delete:clearRelate', '模板关联清理功能', 'test', 'BUTTON', 'm70', 3, 75, 1),

('m95', '按名称搜索', 'test:template:search:name', '按名称搜索功能', 'test', 'BUTTON', 'm71', 3, 76, 1),
('m96', '按状态搜索', 'test:template:search:status', '按状态搜索功能', 'test', 'BUTTON', 'm71', 3, 77, 1),
('m97', '按关联协议搜索', 'test:template:search:relate', '按关联协议搜索功能', 'test', 'BUTTON', 'm71', 3, 78, 1),
('m98', '组合条件搜索', 'test:template:search:complex', '组合条件搜索功能', 'test', 'BUTTON', 'm71', 3, 79, 1),

('m99', '创建模板版本', 'test:template:version:create', '创建模板版本功能', 'test', 'BUTTON', 'm72', 3, 80, 1),
('m100', '查看模板版本', 'test:template:version:view', '查看模板版本功能', 'test', 'BUTTON', 'm72', 3, 81, 1),
('m101', '删除模板版本', 'test:template:version:delete', '删除模板版本功能', 'test', 'BUTTON', 'm72', 3, 82, 1),
('m102', '模板版本关联任务', 'test:template:version:relateTask', '模板版本关联任务功能', 'test', 'BUTTON', 'm72', 3, 83, 1),

('m103', '关联测试任务', 'test:template:relateTask:add', '关联测试任务功能', 'test', 'BUTTON', 'm73', 3, 84, 1),
('m104', '取消关联', 'test:template:relateTask:cancel', '取消关联功能', 'test', 'BUTTON', 'm73', 3, 85, 1),
('m105', '查看关联任务', 'test:template:relateTask:view', '查看关联任务功能', 'test', 'BUTTON', 'm73', 3, 86, 1),
('m106', '批量关联', 'test:template:relateTask:batch', '批量关联功能', 'test', 'BUTTON', 'm73', 3, 87, 1),
('m107', '关联状态管理', 'test:template:relateTask:status', '关联状态管理功能', 'test', 'BUTTON', 'm73', 3, 88, 1),

('m108', '配置模板参数', 'test:template:param:config', '配置模板参数功能', 'test', 'BUTTON', 'm74', 3, 89, 1),
('m109', '参数导入导出', 'test:template:param:importExport', '参数导入导出功能', 'test', 'BUTTON', 'm74', 3, 90, 1),
('m110', '参数模板管理', 'test:template:param:template', '参数模板管理功能', 'test', 'BUTTON', 'm74', 3, 91, 1),

-- ====================== 三、测试任务管理 ======================
('m111', '测试任务管理', 'test:task', '测试任务管理模块', 'task', 'MENU', '0', 1, 30, 1),

('m112', '任务创建', 'test:task:create', '任务创建', 'task', 'MENU', 'm111', 2, 1, 1),
('m113', '任务编辑', 'test:task:edit', '任务编辑', 'task', 'MENU', 'm111', 2, 20, 1),
('m114', '任务执行', 'test:task:execute', '任务执行', 'task', 'MENU', 'm111', 2, 30, 1),
('m115', '任务批量操作', 'test:task:batch', '任务批量操作', 'task', 'MENU', 'm111', 2, 40, 1),

('m116', '新建测试任务', 'test:task:create:add', '新建测试任务功能', 'task', 'BUTTON', 'm112', 3, 92, 1),
('m117', '选择测试模板', 'test:task:create:selectTemplate', '选择测试模板功能', 'task', 'BUTTON', 'm112', 3, 93, 1),
('m118', '配置任务参数', 'test:task:create:param', '配置任务参数功能', 'task', 'BUTTON', 'm112', 3, 94, 1),
('m119', '保存任务', 'test:task:create:save', '保存任务功能', 'task', 'BUTTON', 'm112', 3, 95, 1),
('m120', '任务版本创建', 'test:task:create:version', '任务版本创建功能', 'task', 'BUTTON', 'm112', 3, 96, 1),
('m121', '任务关联协议', 'test:task:create:relateProtocol', '任务关联协议功能', 'task', 'BUTTON', 'm112', 3, 97, 1),
('m122', '任务导入', 'test:task:create:import', '任务导入功能', 'task', 'BUTTON', 'm112', 3, 98, 1),
('m123', '任务参数配置', 'test:task:create:paramConfig', '任务参数配置功能', 'task', 'BUTTON', 'm112', 3, 99, 1),

('m124', '编辑任务参数', 'test:task:edit:param', '编辑任务参数功能', 'task', 'BUTTON', 'm113', 3, 100, 1),
('m125', '任务关联修改', 'test:task:edit:relate', '任务关联修改功能', 'task', 'BUTTON', 'm113', 3, 101, 1),
('m126', '任务参数调整', 'test:task:edit:adjust', '任务参数调整功能', 'task', 'BUTTON', 'm113', 3, 102, 1),
('m127', '任务导出', 'test:task:edit:export', '任务导出功能', 'task', 'BUTTON', 'm113', 3, 103, 1),

('m128', '自动执行任务', 'test:task:execute:auto', '自动执行任务功能', 'task', 'BUTTON', 'm114', 3, 104, 1),
('m129', '执行进度显示', 'test:task:execute:progress', '执行进度显示功能', 'task', 'BUTTON', 'm114', 3, 105, 1),
('m130', '执行结果展示', 'test:task:execute:result', '执行结果展示功能', 'task', 'BUTTON', 'm114', 3, 106, 1),
('m131', '执行重试', 'test:task:execute:retry', '执行重试功能', 'task', 'BUTTON', 'm114', 3, 107, 1),
('m132', '执行暂停', 'test:task:execute:pause', '执行暂停功能', 'task', 'BUTTON', 'm114', 3, 108, 1),
('m133', '执行恢复', 'test:task:execute:resume', '执行恢复功能', 'task', 'BUTTON', 'm114', 3, 109, 1),
('m134', '执行取消', 'test:task:execute:cancel', '执行取消功能', 'task', 'BUTTON', 'm114', 3, 110, 1),
('m135', '执行结果分析', 'test:task:execute:analyze', '执行结果分析功能', 'task', 'BUTTON', 'm114', 3, 111, 1),

('m136', '批量执行', 'test:task:batch:execute', '批量执行功能', 'task', 'BUTTON', 'm115', 3, 112, 1),
('m137', '批量暂停', 'test:task:batch:pause', '批量暂停功能', 'task', 'BUTTON', 'm115', 3, 113, 1),
('m138', '批量停止', 'test:task:batch:stop', '批量停止功能', 'task', 'BUTTON', 'm115', 3, 114, 1),
('m139', '批量删除', 'test:task:batch:delete', '批量删除功能', 'task', 'BUTTON', 'm115', 3, 115, 1),
('m140', '批量导出', 'test:task:batch:export', '批量导出功能', 'task', 'BUTTON', 'm115', 3, 116, 1),
('m141', '批量导入', 'test:task:batch:import', '批量导入功能', 'task', 'BUTTON', 'm115', 3, 117, 1),
('m142', '批量关联', 'test:task:batch:relate', '批量关联功能', 'task', 'BUTTON', 'm115', 3, 118, 1),

-- ====================== 四、报告与分析管理 ======================
('m143', '报告与分析管理', 'report:analysis', '报告与分析管理模块', 'report', 'MENU', '0', 1, 40, 1),

('m144', '测试结果展示', 'report:result', '测试结果展示', 'report', 'MENU', 'm143', 2, 1, 1),
('m145', '多维度图表分析', 'report:chart', '多维度图表分析', 'report', 'MENU', 'm143', 2, 20, 1),
('m146', '报告生成', 'report:generate', '报告生成', 'report', 'MENU', 'm143', 2, 30, 1),
('m147', '报告模板管理', 'report:template', '报告模板管理', 'report', 'MENU', 'm143', 2, 40, 1),

('m148', '表格展示', 'report:result:table', '表格展示功能', 'report', 'BUTTON', 'm144', 3, 119, 1),
('m149', '时间线展示', 'report:result:timeline', '时间线展示功能', 'report', 'BUTTON', 'm144', 3, 120, 1),

('m150', '常见图表', 'report:chart:common', '常见图表功能', 'report', 'BUTTON', 'm145', 3, 121, 1),
('m151', '自定义图表', 'report:chart:custom', '自定义图表功能', 'report', 'BUTTON', 'm145', 3, 122, 1),
('m152', '图表导出', 'report:chart:export', '图表导出功能', 'report', 'BUTTON', 'm145', 3, 123, 1),
('m153', '图表对比', 'report:chart:compare', '图表对比功能', 'report', 'BUTTON', 'm145', 3, 124, 1),
('m154', '图表分析', 'report:chart:analyze', '图表分析功能', 'report', 'BUTTON', 'm145', 3, 125, 1),

('m155', '自动生成报告', 'report:generate:auto', '自动生成报告功能', 'report', 'BUTTON', 'm146', 3, 126, 1),
('m156', '报告模板选择', 'report:generate:selectTemplate', '报告模板选择功能', 'report', 'BUTTON', 'm146', 3, 127, 1),
('m157', '报告内容配置', 'report:generate:config', '报告内容配置功能', 'report', 'BUTTON', 'm146', 3, 128, 1),
('m158', '报告预览', 'report:generate:preview', '报告预览功能', 'report', 'BUTTON', 'm146', 3, 129, 1),
('m159', '报告导出', 'report:generate:export', '报告导出功能', 'report', 'BUTTON', 'm146', 3, 130, 1),
('m160', '报告统计', 'report:generate:statistics', '报告统计功能', 'report', 'BUTTON', 'm146', 3, 131, 1),
('m161', '报告模板管理', 'report:generate:templateManage', '报告模板管理功能', 'report', 'BUTTON', 'm146', 3, 132, 1),

('m162', '创建报告模板', 'report:template:create', '创建报告模板功能', 'report', 'BUTTON', 'm147', 3, 133, 1),
('m163', '编辑报告模板', 'report:template:edit', '编辑报告模板功能', 'report', 'BUTTON', 'm147', 3, 134, 1),
('m164', '删除报告模板', 'report:template:delete', '删除报告模板功能', 'report', 'BUTTON', 'm147', 3, 135, 1),
('m165', '查看报告模板', 'report:template:view', '查看报告模板功能', 'report', 'BUTTON', 'm147', 3, 136, 1),
('m166', '模板导入导出', 'report:template:importExport', '模板导入导出功能', 'report', 'BUTTON', 'm147', 3, 137, 1),
('m167', '模板关联', 'report:template:relate', '模板关联功能', 'report', 'BUTTON', 'm147', 3, 138, 1),

-- ====================== 五、系统管理 ======================
('m168', '系统管理', 'system:manage', '系统管理模块', 'system', 'MENU', '0', 1, 50, 1),

('m169', '用户管理', 'system:user', '用户管理', 'system', 'MENU', 'm168', 2, 1, 1),
('m170', '角色管理', 'system:role', '角色管理', 'system', 'MENU', 'm168', 2, 20, 1),

('m171', '用户注册', 'system:user:register', '用户注册功能', 'system', 'BUTTON', 'm169', 3, 139, 1),
('m172', '用户登录', 'system:user:login', '用户登录功能', 'system', 'BUTTON', 'm169', 3, 140, 1),
('m173', '用户信息编辑', 'system:user:edit', '用户信息编辑功能', 'system', 'BUTTON', 'm169', 3, 141, 1),
('m174', '用户状态管理', 'system:user:status', '用户状态管理功能', 'system', 'BUTTON', 'm169', 3, 142, 1),
('m175', '用户角色分配', 'system:user:assignRole', '用户角色分配功能', 'system', 'BUTTON', 'm169', 3, 143, 1),
('m176', '用户权限查看', 'system:user:viewPermission', '用户权限查看功能', 'system', 'BUTTON', 'm169', 3, 144, 1),
('m177', '用户日志', 'system:user:log', '用户日志功能', 'system', 'BUTTON', 'm169', 3, 145, 1),
('m178', '用户搜索', 'system:user:search', '用户搜索功能', 'system', 'BUTTON', 'm169', 3, 146, 1),

('m179', '角色创建', 'system:role:create', '角色创建功能', 'system', 'BUTTON', 'm170', 3, 147, 1),
('m180', '角色编辑', 'system:role:edit', '角色编辑功能', 'system', 'BUTTON', 'm170', 3, 148, 1),
('m181', '角色删除', 'system:role:delete', '角色删除功能', 'system', 'BUTTON', 'm170', 3, 149, 1),
('m182', '角色查看', 'system:role:view', '角色查看功能', 'system', 'BUTTON', 'm170', 3, 150, 1),
('m183', '角色权限分配', 'system:role:assignPermission', '角色权限分配功能', 'system', 'BUTTON', 'm170', 3, 151, 1),
('m184', '角色状态管理', 'system:role:status', '角色状态管理功能', 'system', 'BUTTON', 'm170', 3, 152, 1),
('m185', '角色权限查看', 'system:role:viewPermission', '角色权限查看功能', 'system', 'BUTTON', 'm170', 3, 153, 1),
('m186', '角色批量操作', 'system:role:batch', '角色批量操作功能', 'system', 'BUTTON', 'm170', 3, 154, 1),
('m187', '角色搜索', 'system:role:search', '角色搜索功能', 'system', 'BUTTON', 'm170', 3, 155, 1),
('m188', '角色关联', 'system:role:relate', '角色关联功能', 'system', 'BUTTON', 'm170', 3, 156, 1);

-- ===========================================
-- 补充报告与分析管理模块的API权限
-- ===========================================

-- 插入报告与分析管理模块的API权限（使用INSERT IGNORE避免重复）
INSERT IGNORE INTO `pdm_tool_sys_permission` (`id`, `name`, `code`, `description`, `module`, `type`, `parent_id`, `level`, `sort`) VALUES
-- ====================== 报告模板管理API权限 ======================
('report_api_1', '创建报告模板API', 'report:template:create', '创建报告模板接口权限', 'report', 'API', 'm162', 3, 157),
('report_api_2', '更新报告模板API', 'report:template:update', '更新报告模板接口权限', 'report', 'API', 'm163', 3, 158),
('report_api_3', '删除报告模板API', 'report:template:delete', '删除报告模板接口权限', 'report', 'API', 'm164', 3, 159),
('report_api_4', '查看报告模板API', 'report:template:view', '查看报告模板接口权限', 'report', 'API', 'm165', 3, 160),
('report_api_5', '导入报告模板API', 'report:template:import', '导入报告模板接口权限', 'report', 'API', 'm166', 3, 161),
('report_api_6', '导出报告模板API', 'report:template:export', '导出报告模板接口权限', 'report', 'API', 'm166', 3, 162),
('report_api_7', '关联业务对象API', 'report:template:relate', '关联业务对象接口权限', 'report', 'API', 'm167', 3, 163),

-- ====================== 报告管理API权限 ======================
('report_api_8', '创建报告API', 'report:create', '创建报告接口权限', 'report', 'API', 'm155', 3, 164),
('report_api_9', '更新报告API', 'report:update', '更新报告接口权限', 'report', 'API', 'm157', 3, 165),
('report_api_10', '删除报告API', 'report:delete', '删除报告接口权限', 'report', 'API', 'm159', 3, 166),
('report_api_11', '查看报告API', 'report:view', '查看报告接口权限', 'report', 'API', 'm155', 3, 167),
('report_api_12', '自动生成报告API', 'report:auto-generate', '自动生成报告接口权限', 'report', 'API', 'm155', 3, 168),
('report_api_13', '预览报告API', 'report:preview', '预览报告接口权限', 'report', 'API', 'm158', 3, 169),
('report_api_14', '导出报告API', 'report:export', '导出报告接口权限', 'report', 'API', 'm159', 3, 170),
('report_api_15', '批量导出报告API', 'report:batch-export', '批量导出报告接口权限', 'report', 'API', 'm159', 3, 171),
('report_api_16', '设置定时生成API', 'report:schedule', '设置定时生成接口权限', 'report', 'API', 'm155', 3, 172),
('report_api_17', '获取报告统计API', 'report:statistics', '获取报告统计接口权限', 'report', 'API', 'm160', 3, 173),

-- ====================== 报告图表管理API权限 ======================
('report_api_18', '创建图表API', 'report:chart:create', '创建图表接口权限', 'report', 'API', 'm150', 3, 174),
('report_api_19', '更新图表API', 'report:chart:update', '更新图表接口权限', 'report', 'API', 'm150', 3, 175),
('report_api_20', '删除图表API', 'report:chart:delete', '删除图表接口权限', 'report', 'API', 'm150', 3, 176),
('report_api_21', '查看图表API', 'report:chart:view', '查看图表接口权限', 'report', 'API', 'm150', 3, 177),
('report_api_22', '生成预设图表API', 'report:chart:preset', '生成预设图表接口权限', 'report', 'API', 'm150', 3, 178),
('report_api_23', '自定义图表配置API', 'report:chart:customize', '自定义图表配置接口权限', 'report', 'API', 'm151', 3, 179),
('report_api_24', '导出图表API', 'report:chart:export', '导出图表接口权限', 'report', 'API', 'm152', 3, 180),
('report_api_25', '批量导出图表API', 'report:chart:batch-export', '批量导出图表接口权限', 'report', 'API', 'm152', 3, 181),
('report_api_26', '图表对比API', 'report:chart:compare', '图表对比接口权限', 'report', 'API', 'm153', 3, 182),
('report_api_27', '图表数据分析API', 'report:chart:analyze', '图表数据分析接口权限', 'report', 'API', 'm154', 3, 183),

-- ====================== 系统管理模块API权限 ======================
('system_api_1', '系统配置查询API', 'system:config:query', '系统配置查询接口权限', 'system', 'API', 'm168', 3, 195),
('system_api_2', '系统配置更新API', 'system:config:update', '系统配置更新接口权限', 'system', 'API', 'm168', 3, 196),
('system_api_3', '数据字典查询API', 'system:dictionary:query', '数据字典查询接口权限', 'system', 'API', 'm168', 3, 197),
('system_api_4', '操作日志查询API', 'system:log:query', '操作日志查询接口权限', 'system', 'API', 'm168', 3, 198);

-- ===========================================
-- 为管理员角色分配报告与分析管理模块的权限
-- ===========================================

-- 为admin角色分配报告与分析管理模块的权限
INSERT IGNORE INTO `pdm_tool_sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`, `create_user`) VALUES
-- 报告模板管理权限
('report_rp_1', 'admin', 'report_api_1', NOW(), 'admin'),
('report_rp_2', 'admin', 'report_api_2', NOW(), 'admin'),
('report_rp_3', 'admin', 'report_api_3', NOW(), 'admin'),
('report_rp_4', 'admin', 'report_api_4', NOW(), 'admin'),
('report_rp_5', 'admin', 'report_api_5', NOW(), 'admin'),
('report_rp_6', 'admin', 'report_api_6', NOW(), 'admin'),
('report_rp_7', 'admin', 'report_api_7', NOW(), 'admin'),

-- 报告管理权限
('report_rp_8', 'admin', 'report_api_8', NOW(), 'admin'),
('report_rp_9', 'admin', 'report_api_9', NOW(), 'admin'),
('report_rp_10', 'admin', 'report_api_10', NOW(), 'admin'),
('report_rp_11', 'admin', 'report_api_11', NOW(), 'admin'),
('report_rp_12', 'admin', 'report_api_12', NOW(), 'admin'),
('report_rp_13', 'admin', 'report_api_13', NOW(), 'admin'),
('report_rp_14', 'admin', 'report_api_14', NOW(), 'admin'),
('report_rp_15', 'admin', 'report_api_15', NOW(), 'admin'),
('report_rp_16', 'admin', 'report_api_16', NOW(), 'admin'),
('report_rp_17', 'admin', 'report_api_17', NOW(), 'admin'),

-- 报告图表管理权限
('report_rp_18', 'admin', 'report_api_18', NOW(), 'admin'),
('report_rp_19', 'admin', 'report_api_19', NOW(), 'admin'),
('report_rp_20', 'admin', 'report_api_20', NOW(), 'admin'),
('report_rp_21', 'admin', 'report_api_21', NOW(), 'admin'),
('report_rp_22', 'admin', 'report_api_22', NOW(), 'admin'),
('report_rp_23', 'admin', 'report_api_23', NOW(), 'admin'),
('report_rp_24', 'admin', 'report_api_24', NOW(), 'admin'),
('report_rp_25', 'admin', 'report_api_25', NOW(), 'admin'),
('report_rp_26', 'admin', 'report_api_26', NOW(), 'admin'),
('report_rp_27', 'admin', 'report_api_27', NOW(), 'admin'),

-- 系统管理权限
('system_rp_1', 'admin', 'system_api_1', NOW(), 'admin'),
('system_rp_2', 'admin', 'system_api_2', NOW(), 'admin'),
('system_rp_3', 'admin', 'system_api_3', NOW(), 'admin'),
('system_rp_4', 'admin', 'system_api_4', NOW(), 'admin');

-- ===========================================
-- 新增角色和权限分配
-- ===========================================

-- 插入新的角色
INSERT IGNORE INTO `pdm_tool_sys_role` (`id`, `name`, `description`, `type`, `status`) VALUES
('1001', '项目经理', '项目管理部各业务事业部 - 创建并管理项目、规划迭代、跟踪项目进度与风险、组织评审', 'SYSTEM', 1),
('1002', '产品经理', '产品部 - 创建和管理产品需求、编写需求文档、定义需求优先级、跟踪需求实现状态', 'SYSTEM', 1),
('1003', '开发人员', '研发中心 - 领取开发任务、编写代码、提交代码至代码库、修复缺陷、进行代码评审', 'SYSTEM', 1),
('1004', '测试人员', '质量保障部 - 编写测试用例、执行测试任务、提交并跟踪缺陷、生成测试报告', 'SYSTEM', 1),
('1005', '运维人员', '运维部 - 管理生产环境、查看系统运行状态、管理部署流程', 'SYSTEM', 1),
('1006', '系统管理员', '信息技术部 - 管理平台用户、配置组织架构、分配全局权限、进行系统配置与维护', 'SYSTEM', 1);

-- 为项目经理分配权限
INSERT IGNORE INTO `pdm_tool_sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`, `create_user`) VALUES
('rp_1001_1', '1001', 'protocol:search:basic', NOW(), 'admin'),
('rp_1001_2', '1001', 'protocol:search:time', NOW(), 'admin'),
('rp_1001_3', '1001', 'protocol:search:complex', NOW(), 'admin'),
('rp_1001_4', '1001', 'test:template:search:name', NOW(), 'admin'),
('rp_1001_5', '1001', 'test:template:search:status', NOW(), 'admin'),
('rp_1001_6', '1001', 'test:template:search:relate', NOW(), 'admin'),
('rp_1001_7', '1001', 'test:template:search:complex', NOW(), 'admin'),
('rp_1001_8', '1001', 'report:view', NOW(), 'admin'),
('rp_1001_9', '1001', 'report:statistics', NOW(), 'admin');

-- 为产品经理分配权限
INSERT IGNORE INTO `pdm_tool_sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`, `create_user`) VALUES
('rp_1002_1', '1002', 'protocol:search:basic', NOW(), 'admin'),
('rp_1002_2', '1002', 'protocol:search:time', NOW(), 'admin'),
('rp_1002_3', '1002', 'protocol:search:complex', NOW(), 'admin'),
('rp_1002_4', '1002', 'test:template:search:name', NOW(), 'admin'),
('rp_1002_5', '1002', 'test:template:search:status', NOW(), 'admin'),
('rp_1002_6', '1002', 'test:template:search:relate', NOW(), 'admin'),
('rp_1002_7', '1002', 'test:template:search:complex', NOW(), 'admin'),
('rp_1002_8', '1002', 'report:view', NOW(), 'admin'),
('rp_1002_9', '1002', 'report:statistics', NOW(), 'admin');

-- 为开发人员分配权限
INSERT IGNORE INTO `pdm_tool_sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`, `create_user`) VALUES
('rp_1003_1', '1003', 'protocol:search:basic', NOW(), 'admin'),
('rp_1003_2', '1003', 'protocol:search:time', NOW(), 'admin'),
('rp_1003_3', '1003', 'protocol:search:complex', NOW(), 'admin'),
('rp_1003_4', '1003', 'test:template:search:name', NOW(), 'admin'),
('rp_1003_5', '1003', 'test:template:search:status', NOW(), 'admin'),
('rp_1003_6', '1003', 'test:template:search:relate', NOW(), 'admin'),
('rp_1003_7', '1003', 'test:template:search:complex', NOW(), 'admin'),
('rp_1003_8', '1003', 'report:view', NOW(), 'admin'),
('rp_1003_9', '1003', 'report:statistics', NOW(), 'admin');

-- 为测试人员分配权限
INSERT IGNORE INTO `pdm_tool_sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`, `create_user`) VALUES
('rp_1004_1', '1004', 'protocol:search:basic', NOW(), 'admin'),
('rp_1004_2', '1004', 'protocol:search:time', NOW(), 'admin'),
('rp_1004_3', '1004', 'protocol:search:complex', NOW(), 'admin'),
('rp_1004_4', '1004', 'test:template:search:name', NOW(), 'admin'),
('rp_1004_5', '1004', 'test:template:search:status', NOW(), 'admin'),
('rp_1004_6', '1004', 'test:template:search:relate', NOW(), 'admin'),
('rp_1004_7', '1004', 'test:template:search:complex', NOW(), 'admin'),
('rp_1004_8', '1004', 'report:view', NOW(), 'admin'),
('rp_1004_9', '1004', 'report:statistics', NOW(), 'admin'),
('rp_1004_10', '1004', 'test:template:create:add', NOW(), 'admin'),
('rp_1004_11', '1004', 'test:template:edit:content', NOW(), 'admin'),
('rp_1004_12', '1004', 'test:template:delete:single', NOW(), 'admin');

-- 为运维人员分配权限
INSERT IGNORE INTO `pdm_tool_sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`, `create_user`) VALUES
('rp_1005_1', '1005', 'protocol:search:basic', NOW(), 'admin'),
('rp_1005_2', '1005', 'protocol:search:time', NOW(), 'admin'),
('rp_1005_3', '1005', 'protocol:search:complex', NOW(), 'admin'),
('rp_1005_4', '1005', 'test:template:search:name', NOW(), 'admin'),
('rp_1005_5', '1005', 'test:template:search:status', NOW(), 'admin'),
('rp_1005_6', '1005', 'test:template:search:relate', NOW(), 'admin'),
('rp_1005_7', '1005', 'test:template:search:complex', NOW(), 'admin'),
('rp_1005_8', '1005', 'report:view', NOW(), 'admin'),
('rp_1005_9', '1005', 'report:statistics', NOW(), 'admin'),
('rp_1005_10', '1005', 'system:user:view', NOW(), 'admin'),
('rp_1005_11', '1005', 'system:role:view', NOW(), 'admin');

-- ===========================================
-- 新增组织部门数据
-- ===========================================

-- 插入组织部门数据
INSERT IGNORE INTO `pdm_tool_sys_organization` (`id`, `name`, `description`, `parent_id`, `level`, `sort`, `status`) VALUES
('org_1001', '项目管理部各业务事业部', '负责项目管理和各业务事业部的协调工作', '0', 1, 1, 1),
('org_1002', '产品部', '负责产品需求管理和产品规划', '0', 1, 2, 1),
('org_1003', '研发中心', '负责软件开发和代码实现', '0', 1, 3, 1),
('org_1004', '质量保障部', '负责软件测试和质量保证', '0', 1, 4, 1),
('org_1005', '运维部', '负责生产环境管理和系统运维', '0', 1, 5, 1),
('org_1006', '信息技术部', '负责系统管理和技术支持', '0', 1, 6, 1);