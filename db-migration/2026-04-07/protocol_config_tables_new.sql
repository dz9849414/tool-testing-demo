-- 1.协议类型主表 - 存储协议类型定义
DROP TABLE protocol_type;
CREATE TABLE `protocol_type`
(
    `id`                bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `protocol_code`     varchar(50)  NOT NULL COMMENT '协议编码（唯一标识）',
    `protocol_name`     varchar(100) NOT NULL COMMENT '协议名称',
    `protocol_category` varchar(50)  NOT NULL COMMENT '协议分类（CAD/ERP/PLM/数据交换/接口协议）',
    `system_type`       varchar(50)  NOT NULL COMMENT '适用系统类型（CAD/ERP/PLM等）',
    `description`       varchar(500)          DEFAULT NULL COMMENT '协议描述',
    `status`            tinyint(1)  NOT NULL DEFAULT 0 COMMENT '状态：0-禁用，1-启用',
    `version`           int(11) NOT NULL DEFAULT 1 COMMENT '版本号（乐观锁）',

    -- 必须字段
    `create_id`         bigint(20) DEFAULT NULL COMMENT '创建人ID',
    `create_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`         bigint(20) DEFAULT NULL COMMENT '更新人ID',
    `update_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    `deleted_by`        bigint(20) DEFAULT NULL COMMENT '删除人ID',
    `deleted_time`      datetime              DEFAULT NULL COMMENT '删除时间',

    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='协议类型主表';


-- 2.协议参数配置表 - 存储具体的协议实例配置
DROP TABLE protocol_config;
CREATE TABLE `protocol_config`
(
    `id`                bigint                                  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `protocol_id`       bigint                                  NOT NULL COMMENT '关联协议类型ID',
    `config_name`       varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置名称',
    `url_config`        json                                                         DEFAULT NULL COMMENT 'URL配置（支持多个，JSON格式存储）',
    `auth_config`       json                                                         DEFAULT NULL COMMENT '认证配置（JSON格式，加密存储）',
    `timeout_connect`   int                                                          DEFAULT '5000' COMMENT '连接超时时间（毫秒）',
    `timeout_read`      int                                                          DEFAULT '30000' COMMENT '读取超时时间（毫秒）',
    `retry_count`       int                                                          DEFAULT '3' COMMENT '重试次数（0-10）',
    `retry_interval`    int                                                          DEFAULT '1000' COMMENT '重试间隔（毫秒）',
    `retry_condition`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '重试触发条件：1-链接超时，2-响应超时，3-响应错误码',
    `data_format`       varchar(20) COLLATE utf8mb4_unicode_ci                       DEFAULT 'JSON' COMMENT '数据格式：JSON/XML/FORM/TEXT/BINARY',
    `format_config`     json                                                         DEFAULT NULL COMMENT '格式校验配置（如JSON Schema、XSD等）',
    `additional_params` json                                                         DEFAULT NULL COMMENT '额外参数（JSON格式存储）',
    `status`            tinyint                                 NOT NULL             DEFAULT '0' COMMENT '状态：0-禁用，1-启用',
    `create_id`         bigint                                                       DEFAULT NULL COMMENT '创建人ID',
    `create_time`       datetime                                NOT NULL             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`         bigint                                                       DEFAULT NULL COMMENT '更新人ID',
    `update_time`       datetime                                NOT NULL             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    `deleted_by`        bigint                                                       DEFAULT NULL COMMENT '删除人ID',
    `deleted_time`      datetime                                                     DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='协议参数配置表';


-- 3.协议-项目关联表 - 记录协议类型与船舶项目的关联关系
DROP TABLE protocol_project;
CREATE TABLE `protocol_project`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `protocol_id`    bigint(20) NOT NULL COMMENT '协议类型ID',
    `project_id`     bigint(20) NOT NULL COMMENT '项目ID',
    `project_name`   varchar(100) NOT NULL COMMENT '项目名称（冗余字段，便于查询）',
    `effective_time` datetime     NOT NULL COMMENT '关联生效时间',
    `expire_time`    datetime              DEFAULT NULL COMMENT '关联失效时间',
    `status`         varchar(20)  NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED-启用, DISABLED-禁用',

    -- 必须字段
    `create_id`      bigint(20) DEFAULT NULL COMMENT '创建人ID',
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`      bigint(20) DEFAULT NULL COMMENT '更新人ID',
    `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`     tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    `deleted_by`     bigint(20) DEFAULT NULL COMMENT '删除人ID',
    `deleted_time`   datetime              DEFAULT NULL COMMENT '删除时间',

    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='协议-项目关联表';

-- 4.协议参数模板表 - 存储常用参数组合模板
DROP TABLE protocol_template;
CREATE TABLE `protocol_template`
(
    `id`              bigint                                  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `protocol_id`     bigint                                  NOT NULL COMMENT '协议类型ID',
    `template_name`   varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板名称',
    `template_code`   varchar(50) COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '模板编码（唯一）',
    `params_snapshot` json                                    NOT NULL COMMENT '参数快照（JSON格式存储完整参数配置）',
    `is_public`       tinyint(1) DEFAULT 0 COMMENT '是否公开模板：0-私有，1-公开',
    `create_id`       bigint                                           DEFAULT NULL COMMENT '创建人ID',
    `create_time`     datetime                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`       bigint                                           DEFAULT NULL COMMENT '更新人ID',
    `update_time`     datetime                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`      tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    `deleted_by`      bigint                                           DEFAULT NULL COMMENT '删除人ID',
    `deleted_time`    datetime                                         DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='协议参数模板表';

-- 5.协议测试记录表 - 存储连接测试、数据传输测试结果
DROP TABLE protocol_test_record;
CREATE TABLE `protocol_test_record`
(
    `id`                bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `protocol_id`       bigint(20) NOT NULL COMMENT '协议类型ID',
    `config_id`         bigint(20) DEFAULT NULL COMMENT '协议配置ID（可为空，表示使用默认配置）',
    `test_type`         varchar(20) NOT NULL COMMENT '测试类型：CONNECT-连接测试, TRANSFER-数据传输, COMPREHENSIVE-综合测试',
    `test_scenario`     varchar(50)          DEFAULT NULL COMMENT '测试场景：NETWORK-网络连通, AUTH-认证, PROTOCOL-协议',
    `test_data`         text                 DEFAULT NULL COMMENT '测试数据（JSON格式）',
    `result_status`     varchar(20) NOT NULL COMMENT '结果状态：SUCCESS-成功, FAILED-失败',
    `response_code`     varchar(10)          DEFAULT NULL COMMENT '响应码（如200, 401, 500等）',
    `response_time`     int(11) DEFAULT NULL COMMENT '响应时间（毫秒）',
    `error_message`     text                 DEFAULT NULL COMMENT '错误信息',
    `comparison_result` json                 DEFAULT NULL COMMENT '比对结果（JSON格式存储校验和、数据差异等）',
    `test_params`       json                 DEFAULT NULL COMMENT '测试专用参数配置',
    `is_manual`         tinyint(1) DEFAULT 0 COMMENT '是否手动测试：0-自动，1-手动',

    -- 必须字段
    `create_id`         bigint(20) DEFAULT NULL COMMENT '创建人ID',
    `create_time`       datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`         bigint(20) DEFAULT NULL COMMENT '更新人ID',
    `update_time`       datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    `deleted_by`        bigint(20) DEFAULT NULL COMMENT '删除人ID',
    `deleted_time`      datetime             DEFAULT NULL COMMENT '删除时间',

    PRIMARY KEY (`id`),
    KEY                 `idx_protocol_id` (`protocol_id`),
    KEY                 `idx_config_id` (`config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='协议测试记录表';


-- 6.测试数据样本库 - 存储标准测试数据样本
DROP TABLE test_data_sample;
CREATE TABLE `test_data_sample`
(
    `id`                bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sample_name`       varchar(100) NOT NULL COMMENT '样本名称',
    `sample_type`       varchar(20)  NOT NULL COMMENT '样本类型：BOM-物料清单, DRAWING-图纸, DOCUMENT-文档, CUSTOM-自定义',
    `protocol_category` varchar(50)           DEFAULT NULL COMMENT '适用的协议分类',
    `data_format`       varchar(20)           DEFAULT 'JSON' COMMENT '数据格式',
    `sample_data`       mediumtext   NOT NULL COMMENT '样本数据（支持大文本存储）',
    `data_size`         int(11) DEFAULT NULL COMMENT '数据大小（字节）',
    `checksum`          varchar(100)          DEFAULT NULL COMMENT '校验和',
    `description`       varchar(500)          DEFAULT NULL COMMENT '样本描述',
    `is_standard`       tinyint(1) DEFAULT 0 COMMENT '是否标准样本：0-用户自定义，1-系统标准',

    -- 必须字段
    `create_id`         bigint(20) DEFAULT NULL COMMENT '创建人ID',
    `create_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`         bigint(20) DEFAULT NULL COMMENT '更新人ID',
    `update_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    `deleted_by`        bigint(20) DEFAULT NULL COMMENT '删除人ID',
    `deleted_time`      datetime              DEFAULT NULL COMMENT '删除时间',

    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试数据样本库';

-- 7.文件导入记录表
DROP TABLE file_import_record;
CREATE TABLE `file_import_record`
(
    `id`                bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `import_session_id` varchar(50)  NOT NULL COMMENT '导入会话ID',
    `file_name`         varchar(255) NOT NULL COMMENT '文件名',
    `file_type`         varchar(20)  NOT NULL COMMENT '文件类型：EXCEL, JSON, CSV',
    `import_type`       varchar(20)  NOT NULL COMMENT '导入类型：PROTOCOL_TYPE-协议类型, PROTOCOL_CONFIG-协议配置, PARAM_TEMPLATE-参数模板',
    `total_count`       int(11) DEFAULT 0 COMMENT '总记录数',
    `success_count`     int(11) DEFAULT 0 COMMENT '成功记录数',
    `fail_count`        int(11) DEFAULT 0 COMMENT '失败记录数',
    `import_params`     json                  DEFAULT NULL COMMENT '导入参数配置',
    `result_file_url`   varchar(500)          DEFAULT NULL COMMENT '结果文件URL',
    `status`            varchar(20)  NOT NULL COMMENT '状态：PROCESSING-处理中, SUCCESS-成功, FAILED-失败, PARTIAL-部分成功',
    `start_time`        datetime     NOT NULL COMMENT '开始时间',
    `end_time`          datetime              DEFAULT NULL COMMENT '结束时间',
    `error_message`     text                  DEFAULT NULL COMMENT '错误信息',

    -- 必须字段
    `create_id`         bigint(20) DEFAULT NULL COMMENT '创建人ID',
    `create_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`         bigint(20) DEFAULT NULL COMMENT '更新人ID',
    `update_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    `deleted_by`        bigint(20) DEFAULT NULL COMMENT '删除人ID',
    `deleted_time`      datetime              DEFAULT NULL COMMENT '删除时间',

    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件导入记录表';

-- 8.导入错误日志表
DROP TABLE import_error_log;
CREATE TABLE `import_error_log`
(
    `id`               bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `import_record_id` bigint(20) NOT NULL COMMENT '导入记录ID',
    `row_number`       int(11) NOT NULL COMMENT '错误行号',
    `field_name`       varchar(100)          DEFAULT NULL COMMENT '字段名',
    `error_type`       varchar(50)  NOT NULL COMMENT '错误类型：FORMAT-格式错误, REQUIRED-必填字段缺失, DUPLICATE-重复, VALIDATION-验证失败',
    `error_message`    varchar(500) NOT NULL COMMENT '错误信息',
    `original_value`   text                  DEFAULT NULL COMMENT '原始值',
    `suggested_value`  text                  DEFAULT NULL COMMENT '建议值',

    -- 必须字段
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`       tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    `deleted_by`       bigint(20) DEFAULT NULL COMMENT '删除人ID',
    `deleted_time`     datetime              DEFAULT NULL COMMENT '删除时间',

    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='导入错误日志表';

-- 9.文件导出记录表
DROP TABLE file_export_record;
CREATE TABLE `file_export_record`
(
    `id`                bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `export_session_id` varchar(50)  NOT NULL COMMENT '导出会话ID',
    `file_name`         varchar(255) NOT NULL COMMENT '文件名',
    `file_type`         varchar(20)  NOT NULL COMMENT '文件类型：EXCEL, CSV, PDF, JSON',
    `export_type`       varchar(20)  NOT NULL COMMENT '导出类型：PROTOCOL_TYPE-协议类型, PROTOCOL_CONFIG-协议配置, TEST_REPORT-测试报告',
    `record_count`      int(11) DEFAULT 0 COMMENT '记录数量',
    `export_options`    json                  DEFAULT NULL COMMENT '导出选项（是否脱敏、包含字段等）',
    `file_url`          varchar(500)          DEFAULT NULL COMMENT '文件存储URL',
    `file_size`         bigint(20) DEFAULT NULL COMMENT '文件大小（字节）',
    `status`            varchar(20)  NOT NULL COMMENT '状态：PROCESSING-处理中, SUCCESS-成功, FAILED-失败',
    `export_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '导出时间',
    `download_count`    int(11) DEFAULT 0 COMMENT '下载次数',
    `expire_time`       datetime              DEFAULT NULL COMMENT '过期时间',

    -- 必须字段
    `create_id`         bigint(20) DEFAULT NULL COMMENT '创建人ID',
    `create_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`         bigint(20) DEFAULT NULL COMMENT '更新人ID',
    `update_time`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    `deleted_by`        bigint(20) DEFAULT NULL COMMENT '删除人ID',
    `deleted_time`      datetime              DEFAULT NULL COMMENT '删除时间',

    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件导出记录表';

CREATE TABLE `protocol_template_group`
(
    `id`                   bigint   NOT NULL COMMENT '主键ID',
    `protocol_template_id` bigint   NOT NULL COMMENT '参数模板ID',
    `group_name`           varchar(50)       DEFAULT NULL COMMENT '分组名称',
    `params_config`        json              DEFAULT NULL COMMENT '模板分组参数配置',
    `create_id`            bigint            DEFAULT NULL COMMENT '创建人ID',
    `create_time`          datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`            bigint            DEFAULT NULL COMMENT '更新人ID',
    `update_time`          datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`           tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    `deleted_by`           bigint            DEFAULT NULL COMMENT '删除人ID',
    `deleted_time`         datetime          DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='协议参数模板分组表';