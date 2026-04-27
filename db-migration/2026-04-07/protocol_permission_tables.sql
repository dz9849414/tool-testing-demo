-- ================================================
-- 协议权限系统数据库表结构
-- 创建时间：2026-04-27
-- 说明：协议权限独立于系统权限，完全隔离的权限体系
-- ================================================

-- 先删表（按外键依赖顺序）
DROP TABLE IF EXISTS `pdm_tool_user_protocol_role_rel`;
DROP TABLE IF EXISTS `pdm_tool_role_protocol_rel`;
DROP TABLE IF EXISTS `pdm_tool_user_protocol_rel`;
DROP TABLE IF EXISTS `pdm_tool_sys_route_protocol`;
DROP TABLE IF EXISTS `pdm_tool_protocol_role`;

-- 1. 协议角色表（协议权限系统的角色定义）
CREATE TABLE IF NOT EXISTS `pdm_tool_protocol_role` (
                                                        `id` BIGINT AUTO_INCREMENT COMMENT '角色ID',
                                                        `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色代码',
    `description` VARCHAR(500) COMMENT '角色描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`),
    KEY `idx_role_status` (`status`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协议角色表';

-- 2. 路由协议绑定表
CREATE TABLE IF NOT EXISTS `pdm_tool_sys_route_protocol` (
                                                             `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
                                                             `request_uri` VARCHAR(500) NOT NULL COMMENT '请求URI：/api/test/rtsp',
    `protocol_code` VARCHAR(50) NOT NULL COMMENT '协议代码',
    `description` VARCHAR(500) COMMENT '绑定描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_request_uri` (`request_uri`),
    KEY `idx_protocol_code` (`protocol_code`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路由协议绑定表';

-- 3. 角色协议关联表
CREATE TABLE IF NOT EXISTS `pdm_tool_role_protocol_rel` (
                                                            `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
                                                            `role_id` BIGINT NOT NULL COMMENT '角色ID',
                                                            `protocol_code` VARCHAR(50) NOT NULL COMMENT '协议代码',
    `description` VARCHAR(500) COMMENT '关联描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_protocol` (`role_id`,`protocol_code`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_protocol_code` (`protocol_code`),
    CONSTRAINT `fk_role_protocol_role` FOREIGN KEY (`role_id`) REFERENCES `pdm_tool_protocol_role` (`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色协议关联表';

-- 4. 用户协议关联表
CREATE TABLE IF NOT EXISTS `pdm_tool_user_protocol_rel` (
                                                            `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
                                                            `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                                            `protocol_code` VARCHAR(50) NOT NULL COMMENT '协议代码',
    `description` VARCHAR(500) COMMENT '关联描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_protocol` (`user_id`,`protocol_code`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_protocol_code` (`protocol_code`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户协议关联表';

-- 5. 用户协议角色关联表
CREATE TABLE IF NOT EXISTS `pdm_tool_user_protocol_role_rel` (
                                                                 `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
                                                                 `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                                                 `role_id` BIGINT NOT NULL COMMENT '角色ID',
                                                                 `description` VARCHAR(500) COMMENT '关联描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`),
    CONSTRAINT `fk_user_protocol_role_role` FOREIGN KEY (`role_id`) REFERENCES `pdm_tool_protocol_role` (`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户协议角色关联表';

-- ================================================
-- 初始化数据
-- ================================================
INSERT IGNORE INTO `pdm_tool_protocol_role` (`role_name`, `role_code`, `description`, `status`) VALUES
('协议管理员', 'PROTOCOL_ADMIN', '拥有所有协议权限的管理员角色', 1),
('协议测试员', 'PROTOCOL_TESTER', '拥有常用协议测试权限的角色', 1),
('协议观察员', 'PROTOCOL_VIEWER', '只能查看协议信息的角色', 1);

INSERT IGNORE INTO `pdm_tool_user_protocol_role_rel` (`user_id`, `role_id`, `description`, `status`) VALUES
(1, 1, '管理员拥有协议管理员角色', 1),
(1, 2, '管理员同时拥有协议测试员角色', 1),
(2, 2, '普通用户拥有协议测试员角色', 1),
(3, 3, '观察用户拥有协议观察员角色', 1);

INSERT IGNORE INTO `pdm_tool_sys_route_protocol` (`request_uri`, `protocol_code`, `description`, `status`) VALUES
('/api/protocol/http', 'HTTP', 'HTTP协议测试接口', 1),
('/api/protocol/https', 'HTTPS', 'HTTPS协议测试接口', 1),
('/api/protocol/websocket', 'WEBSOCKET', 'WebSocket协议测试接口', 1),
('/api/protocol/tcp', 'TCP', 'TCP协议测试接口', 1),
('/api/protocol/udp', 'UDP', 'UDP协议测试接口', 1),
('/api/protocol/ftp', 'FTP', 'FTP协议测试接口', 1),
('/api/protocol/sftp', 'SFTP', 'SFTP协议测试接口', 1),
('/api/protocol/ssh', 'SSH', 'SSH协议测试接口', 1),
('/api/protocol/smtp', 'SMTP', 'SMTP协议测试接口', 1),
('/api/protocol/mqtt', 'MQTT', 'MQTT协议测试接口', 1);

INSERT IGNORE INTO `pdm_tool_role_protocol_rel` (`role_id`, `protocol_code`, `description`, `status`) VALUES
(1, 'HTTP', '协议管理员HTTP协议权限', 1),
(1, 'HTTPS', '协议管理员HTTPS协议权限', 1),
(1, 'WEBSOCKET', '协议管理员WebSocket协议权限', 1),
(1, 'TCP', '协议管理员TCP协议权限', 1),
(1, 'UDP', '协议管理员UDP协议权限', 1),
(1, 'FTP', '协议管理员FTP协议权限', 1),
(1, 'SFTP', '协议管理员SFTP协议权限', 1),
(1, 'SSH', '协议管理员SSH协议权限', 1),
(1, 'SMTP', '协议管理员SMTP协议权限', 1),
(1, 'MQTT', '协议管理员MQTT协议权限', 1),
(2, 'HTTP', '协议测试员HTTP协议权限', 1),
(2, 'HTTPS', '协议测试员HTTPS协议权限', 1),
(2, 'WEBSOCKET', '协议测试员WebSocket协议权限', 1),
(2, 'TCP', '协议测试员TCP协议权限', 1),
(2, 'UDP', '协议测试员UDP协议权限', 1),
(2, 'FTP', '协议测试员FTP协议权限', 1),
(3, 'HTTP', '协议观察员HTTP协议权限', 1),
(3, 'HTTPS', '协议观察员HTTPS协议权限', 1);

-- ================================================
-- 初始化用户直接协议权限数据
-- ================================================
INSERT IGNORE INTO `pdm_tool_user_protocol_rel` (`user_id`, `protocol_code`, `description`, `status`) VALUES
(1, 'HTTP', '管理员直接HTTP协议权限', 1),
(1, 'HTTPS', '管理员直接HTTPS协议权限', 1),
(1, 'WEBSOCKET', '管理员直接WebSocket协议权限', 1),
(2, 'HTTP', '普通用户直接HTTP协议权限', 1),
(2, 'HTTPS', '普通用户直接HTTPS协议权限', 1),
(3, 'HTTP', '观察用户直接HTTP协议权限', 1);

-- ================================================
-- 索引优化
-- ================================================
CREATE INDEX `idx_route_status` ON `pdm_tool_sys_route_protocol` (`status`);
CREATE INDEX `idx_route_create_time` ON `pdm_tool_sys_route_protocol` (`create_time`);
CREATE INDEX `idx_rel_status` ON `pdm_tool_role_protocol_rel` (`status`);
CREATE INDEX `idx_rel_create_time` ON `pdm_tool_role_protocol_rel` (`create_time`);
CREATE INDEX `idx_protocol_role_create_time` ON `pdm_tool_protocol_role` (`create_time`);
CREATE INDEX `idx_user_role_status` ON `pdm_tool_user_protocol_role_rel` (`status`);
CREATE INDEX `idx_user_role_create_time` ON `pdm_tool_user_protocol_role_rel` (`create_time`);

-- 用户协议关联表索引
CREATE INDEX `idx_user_protocol_status` ON `pdm_tool_user_protocol_rel` (`status`);
CREATE INDEX `idx_user_protocol_create_time` ON `pdm_tool_user_protocol_rel` (`create_time`);