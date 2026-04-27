- ===========================================
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
