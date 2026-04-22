-- ========================================
-- -- 船舶 PDM 接口测试工具 - 协议配置管理模块 - 表结构设计
-- ========================================

-- 1. 协议类型主表
DROP TABLE IF EXISTS protocol_type;
DROP TABLE IF EXISTS pdm_tool_protocol_type;
CREATE TABLE `pdm_tool_protocol_type`
(
    `id`                bigint                                                       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `protocol_code`     varchar(50) COLLATE utf8mb4_unicode_ci                       NOT NULL COMMENT '协议编码（唯一标识）',
    `protocol_name`     varchar(100) COLLATE utf8mb4_unicode_ci                      NOT NULL COMMENT '协议名称',
    `protocol_category` varchar(50) COLLATE utf8mb4_unicode_ci                       NOT NULL COMMENT '协议分类（CAD/ERP/PLM/数据交换/接口协议）',
    `system_type`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '适用系统类型（CAD/ERP/PLM等）',
    `description`       varchar(500) COLLATE utf8mb4_unicode_ci                               DEFAULT NULL COMMENT '协议描述',
    `status`            tinyint(1)                                                   NOT NULL DEFAULT '0' COMMENT '状态：0-禁用，1-启用',
    `version`           int                                                          NOT NULL DEFAULT '1' COMMENT '版本号（乐观锁）',
    `create_id`         bigint                                                                DEFAULT NULL COMMENT '创建人ID',
    `create_time`       datetime                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_name`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '创建人名称',
    `update_id`         bigint                                                                DEFAULT NULL COMMENT '更新人ID',
    `update_name`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '修改人名称',
    `update_time`       datetime                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        tinyint(1)                                                   NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    `deleted_by`        bigint                                                                DEFAULT NULL COMMENT '删除人ID',
    `deleted_time`      datetime                                                              DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='协议类型主表';

-- 2. 协议参数配置表
DROP TABLE IF EXISTS protocol_config;
DROP TABLE IF EXISTS pdm_tool_protocol_config;
CREATE TABLE `pdm_tool_protocol_config`
(
    `id`                bigint                                  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `protocol_id`       bigint                                  NOT NULL COMMENT '关联协议类型ID',
    `config_name`       varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置名称',
    `url_config`        json                                                          DEFAULT NULL COMMENT 'URL配置（支持多个，JSON格式存储）',
    `auth_config`       json                                                          DEFAULT NULL COMMENT '认证配置（JSON格式，加密存储）',
    `timeout_connect`   int                                                           DEFAULT 5000 COMMENT '连接超时时间（毫秒）',
    `timeout_read`      int                                                           DEFAULT 30000 COMMENT '读取超时时间（毫秒）',
    `retry_count`       int                                                           DEFAULT 3 COMMENT '重试次数（0-10）',
    `retry_interval`    int                                                           DEFAULT 1000 COMMENT '重试间隔（毫秒）',
    `retry_condition`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '重试触发条件：1-链接超时，2-响应超时，3-响应错误码（可多选）',
    `data_format`       varchar(20) COLLATE utf8mb4_unicode_ci                        DEFAULT 'JSON' COMMENT '数据格式：JSON/XML/FORM/TEXT/BINARY',
    `format_config`     json                                                          DEFAULT NULL COMMENT '格式校验配置（如JSON Schema、XSD等）',
    `additional_params` json                                                          DEFAULT NULL COMMENT '额外参数（JSON格式存储）',
    `status`            tinyint                                 NOT NULL              DEFAULT 0 COMMENT '状态：0-禁用，1-启用',
    `description`       varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '协议参数配置描述',
    `create_id`         bigint                                                        DEFAULT NULL COMMENT '创建人ID',
    `create_name`       varchar(50) COLLATE utf8mb4_unicode_ci                        DEFAULT NULL COMMENT '创建人名称',
    `create_time`       datetime                                NOT NULL              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`         bigint                                                        DEFAULT NULL COMMENT '更新人ID',
    `update_name`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '修改人名称',
    `update_time`       datetime                                NOT NULL              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        tinyint(1)                              NOT NULL              DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    `deleted_by`        bigint                                                        DEFAULT NULL COMMENT '删除人ID',
    `deleted_time`      datetime                                                      DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='协议参数配置表';

-- 3. 协议-项目关联表
DROP TABLE IF EXISTS protocol_project;
DROP TABLE IF EXISTS pdm_tool_protocol_project;
CREATE TABLE `pdm_tool_protocol_project`
(
    `id`             bigint                                  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `protocol_id`    bigint                                  NOT NULL COMMENT '协议类型ID',
    `project_id`     bigint                                  NOT NULL COMMENT '项目ID',
    `project_name`   varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目名称（冗余字段，便于查询）',
    `effective_time` datetime                                NOT NULL COMMENT '关联生效时间',
    `expire_time`    datetime                                                     DEFAULT NULL COMMENT '关联失效时间',
    `status`         tinyint                                 NOT NULL             DEFAULT 0 COMMENT '状态：0-禁用，1-启用',
    `create_id`      bigint                                                       DEFAULT NULL COMMENT '创建人ID',
    `create_name`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人名称',
    `create_time`    datetime                                NOT NULL             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`      bigint                                                       DEFAULT NULL COMMENT '更新人ID',
    `update_name`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '修改人名称',
    `update_time`    datetime                                NOT NULL             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`     tinyint(1)                              NOT NULL             DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    `deleted_by`     bigint                                                       DEFAULT NULL COMMENT '删除人ID',
    `deleted_time`   datetime                                                     DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='协议-项目关联表';

-- 4. 协议测试记录表
DROP TABLE IF EXISTS protocol_test_record;
DROP TABLE IF EXISTS pdm_tool_protocol_test_record;
CREATE TABLE `pdm_tool_protocol_test_record`
(
    `id`                bigint                                 NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `protocol_id`       bigint                                 NOT NULL COMMENT '协议类型ID',
    `config_id`         bigint                                                       DEFAULT NULL COMMENT '协议配置ID（可为空，表示使用默认配置）',
    `test_type`         varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '测试类型：CONNECT-连接测试, TRANSFER-数据传输, COMPREHENSIVE-综合测试',
    `test_scenario`     varchar(50) COLLATE utf8mb4_unicode_ci                       DEFAULT NULL COMMENT '测试场景：NETWORK-网络连通, AUTH-认证, PROTOCOL-协议',
    `test_data`         text COLLATE utf8mb4_unicode_ci COMMENT '测试数据（JSON格式）',
    `result_status`     varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '结果状态：SUCCESS-成功, FAILED-失败',
    `response_code`     varchar(10) COLLATE utf8mb4_unicode_ci                       DEFAULT NULL COMMENT '响应码（如200, 401, 500等）',
    `response_time`     int                                                          DEFAULT NULL COMMENT '响应时间（毫秒）',
    `error_message`     text COLLATE utf8mb4_unicode_ci COMMENT '错误信息',
    `comparison_result` json                                                         DEFAULT NULL COMMENT '比对结果（JSON格式存储校验和、数据差异等）',
    `test_params`       json                                                         DEFAULT NULL COMMENT '测试专用参数配置',
    `is_manual`         tinyint(1)                                                   DEFAULT 0 COMMENT '是否手动测试：0-自动，1-手动',
    `create_id`         bigint                                                       DEFAULT NULL COMMENT '创建人ID',
    `create_name`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人名称',
    `create_time`       datetime                               NOT NULL              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`         bigint                                                       DEFAULT NULL COMMENT '更新人ID',
    `update_name`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '修改人名称',
    `update_time`       datetime                               NOT NULL              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        tinyint(1)                             NOT NULL              DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    `deleted_by`        bigint                                                       DEFAULT NULL COMMENT '删除人ID',
    `deleted_time`      datetime                                                     DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_protocol_id` (`protocol_id`),
    KEY `idx_config_id` (`config_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='协议测试记录表';