-- ===========================================
-- 系统管理模块建表SQL
-- 仿照 Metersphere 系统管理模块设计
-- ===========================================

-- 用户表
CREATE TABLE `sys_user` (
    `id` VARCHAR(50) NOT NULL COMMENT '用户ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password` VARCHAR(256) NOT NULL COMMENT '密码',
    `email` VARCHAR(128) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `real_name` VARCHAR(64) COMMENT '真实姓名',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(64) COMMENT '最后登录IP',
    `source` VARCHAR(32) DEFAULT 'LOCAL' COMMENT '用户来源：LOCAL-本地，LDAP-LDAP，OIDC-OIDC',
    `organization_id` VARCHAR(50) COMMENT '所属组织ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE `sys_role` (
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
CREATE TABLE `sys_permission` (
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
CREATE TABLE `sys_user_role` (
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
CREATE TABLE `sys_role_permission` (
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
CREATE TABLE `sys_organization` (
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
CREATE TABLE `sys_user_organization` (
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
CREATE TABLE `sys_operation_log` (
    `id` VARCHAR(50) NOT NULL COMMENT '日志ID',
    `user_id` VARCHAR(50) COMMENT '操作用户ID',
    `username` VARCHAR(64) COMMENT '操作用户名',
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
    KEY `idx_module` (`module`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 系统配置表
CREATE TABLE `sys_config` (
    `id` VARCHAR(50) NOT NULL COMMENT '配置ID',
    `config_key` VARCHAR(128) NOT NULL COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_name` VARCHAR(128) COMMENT '配置名称',
    `description` VARCHAR(512) COMMENT '配置描述',
    `type` VARCHAR(32) DEFAULT 'TEXT' COMMENT '配置类型：TEXT-文本，JSON-JSON，BOOLEAN-布尔值',
    `is_encrypted` TINYINT DEFAULT 0 COMMENT '是否加密：0-否，1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_user` VARCHAR(50) COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 字典表
CREATE TABLE `sys_dict` (
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
CREATE TABLE `sys_login_log` (
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
INSERT INTO `sys_user` (`id`, `username`, `password`, `email`, `real_name`, `status`) VALUES 
('admin', 'admin', '$2a$10$r3xPKMnGkh/4qGZubMuK1u7T/2eSj7V9X3qIo5MT7K4Lr6gCQYfW6', 'admin@example.com', '系统管理员', 1);

-- 插入默认角色
INSERT INTO `sys_role` (`id`, `name`, `description`, `type`, `status`) VALUES 
('admin', '系统管理员', '系统管理员，拥有所有权限', 'SYSTEM', 1),
('user', '普通用户', '普通用户，拥有基础权限', 'SYSTEM', 1);

-- 插入用户角色关联
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`) VALUES 
('ur_admin', 'admin', 'admin');

-- 插入基础权限（示例）
INSERT INTO `sys_permission` (`id`, `name`, `code`, `description`, `module`, `type`, `parent_id`, `level`, `sort`) VALUES 
('p1', '系统管理', 'system:management', '系统管理模块', 'system', 'MENU', '0', 1, 1),
('p2', '用户管理', 'system:user', '用户管理', 'system', 'MENU', 'p1', 2, 1),
('p3', '角色管理', 'system:role', '角色管理', 'system', 'MENU', 'p1', 2, 2),
('p4', '权限管理', 'system:permission', '权限管理', 'system', 'MENU', 'p1', 2, 3);

-- 插入角色权限关联
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`) VALUES 
('rp1', 'admin', 'p1'),
('rp2', 'admin', 'p2'),
('rp3', 'admin', 'p3'),
('rp4', 'admin', 'p4');

-- 插入默认配置
INSERT INTO `sys_config` (`id`, `config_key`, `config_value`, `config_name`, `description`) VALUES 
('config1', 'system.name', '工具测试平台', '系统名称', '系统显示名称'),
('config2', 'system.version', '1.0.0', '系统版本', '系统版本号');