-- ========================================
-- -- 船舶 PDM 接口测试工具 - 协议配置管理模块 - 表结构设计
-- ========================================

-- 1. 协议类型主表
CREATE TABLE `protocol_type`
(
    `id`                  BIGINT NOT NULL AUTO_INCREMENT COMMENT '协议类型ID',
    `protocol_name`       VARCHAR(100) COMMENT '协议类型名称',
    `protocol_identifier` VARCHAR(50) COMMENT '协议标识符',
    `applicable_system`   VARCHAR(50) COMMENT '适用系统类型（CAD、ERP、PLM等）',
    `description`         VARCHAR(500) COMMENT '描述信息',
    `status`              TINYINT  DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_id`           BIGINT COMMENT '创建人',
    `create_time`         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`           BIGINT COMMENT '更新人',
    `update_time`         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`         TINYINT  DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by`           BIGINT COMMENT '删除人',
    `deleted_time`         DATETIME COMMENT '删除时间',
    `version`             INT      DEFAULT 1 COMMENT '版本号，用于乐观锁',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_protocol_identifier` (`protocol_identifier`),
    KEY                   `idx_applicable_system` (`applicable_system`),
    KEY                   `idx_status` (`status`),
    KEY                   `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协议类型主表';

-- 2. 协议-项目关联表
CREATE TABLE `protocol_project_relation`
(
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联记录ID',
    `protocol_id` BIGINT COMMENT '协议类型ID',
    `project_id`  BIGINT COMMENT '项目ID',
    `create_id`   BIGINT COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`   BIGINT COMMENT '更新人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT  DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by`   BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_protocol_project` (`protocol_id`, `project_id`),
    KEY           `idx_protocol_id` (`protocol_id`),
    KEY           `idx_project_id` (`project_id`),
    CONSTRAINT `fk_ppr_protocol_id` FOREIGN KEY (`protocol_id`) REFERENCES `protocol_type` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协议-项目关联表';

-- 3. 协议参数配置表
CREATE TABLE `protocol_parameter_config`
(
    `id`              BIGINT NOT NULL AUTO_INCREMENT COMMENT '参数配置ID',
    `protocol_id`     BIGINT COMMENT '协议类型ID',
    `parameter_name`  VARCHAR(50) COMMENT '参数名称（URL、PORT、AUTH_TYPE、TIMEOUT_CONNECT、TIMEOUT_READ、RETRY_COUNT、RETRY_INTERVAL、DATA_FORMAT等）',
    `parameter_value` VARCHAR(500) COMMENT '参数值',
    `is_sensitive`    TINYINT  DEFAULT 0 COMMENT '是否敏感字段：0-否，1-是',
    `encrypted_value` VARCHAR(500) COMMENT '加密后的参数值（敏感字段使用）',
    `create_id`       BIGINT COMMENT '创建人',
    `create_time`     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`       BIGINT COMMENT '更新人',
    `update_time`     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`     TINYINT  DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by`       BIGINT COMMENT '删除人',
    `deleted_time`     DATETIME COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY               `idx_protocol_id` (`protocol_id`),
    KEY               `idx_parameter_name` (`parameter_name`),
    CONSTRAINT `fk_ppc_protocol_id` FOREIGN KEY (`protocol_id`) REFERENCES `protocol_type` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协议参数配置表';

-- 4. 协议参数模板表
CREATE TABLE `protocol_parameter_template`
(
    `id`                   BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `template_name`        VARCHAR(100) COMMENT '模板名称',
    `template_description` VARCHAR(500) COMMENT '模板描述',
    `parameters_json`      JSON COMMENT '参数快照（JSON格式，包含URL、端口、认证方式等）',
    `create_id`            BIGINT COMMENT '创建人',
    `create_time`          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`            BIGINT COMMENT '更新人',
    `update_time`          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`          TINYINT  DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by`            BIGINT COMMENT '删除人',
    `deleted_time`          DATETIME COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY                    `idx_create_id` (`create_id`),
    KEY                    `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协议参数模板表';

-- 5. 协议测试记录表
CREATE TABLE `protocol_test_record`
(
    `id`                  BIGINT NOT NULL AUTO_INCREMENT COMMENT '测试记录ID',
    `protocol_id`         BIGINT COMMENT '协议类型ID',
    `test_type`           VARCHAR(20) COMMENT '测试类型：CONNECT-连接测试，DATA_TRANSFER-数据传输测试',
    `test_status`         TINYINT COMMENT '测试结果：0-失败，1-成功',
    `response_time`       INT COMMENT '响应时间（毫秒）',
    `error_code`          VARCHAR(20) COMMENT '错误码（如401、500）',
    `error_message`       VARCHAR(500) COMMENT '错误信息',
    `test_data_sample_id` BIGINT COMMENT '测试数据样本ID',
    `create_id`           BIGINT COMMENT '创建人',
    `create_time`         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`           BIGINT COMMENT '更新人',
    `update_time`         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`         TINYINT  DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by`           BIGINT COMMENT '删除人',
    `deleted_time`         DATETIME COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY                   `idx_protocol_id` (`protocol_id`),
    KEY                   `idx_test_type` (`test_type`),
    KEY                   `idx_test_status` (`test_status`),
    KEY                   `idx_create_time` (`create_time`),
    CONSTRAINT `fk_ptr_protocol_id` FOREIGN KEY (`protocol_id`) REFERENCES `protocol_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协议测试记录表';

-- 6. 测试数据样本库
CREATE TABLE `test_data_sample`
(
    `id`                 BIGINT  NOT NULL AUTO_INCREMENT COMMENT '样本ID',
    `sample_name`        VARCHAR(100) COMMENT '样本名称',
    `sample_description` VARCHAR(500) COMMENT '样本描述',
    `sample_data`        TEXT COMMENT '样本数据内容',
    `data_format`        VARCHAR(20) COMMENT '数据格式：JSON、XML、CSV',
    `is_public`          TINYINT NOT NULL DEFAULT 0 COMMENT '是否公开：0-私有，1-公开',
    `create_id`          BIGINT COMMENT '创建人',
    `create_time`        DATETIME         DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`          BIGINT COMMENT '更新人',
    `update_time`        DATETIME         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        TINYINT          DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by`          BIGINT COMMENT '删除人',
    `deleted_time`        DATETIME COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY                  `idx_create_id` (`create_id`),
    KEY                  `idx_is_public` (`is_public`),
    KEY                  `idx_data_format` (`data_format`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试数据样本库';

-- 7. 协议测试参数表
CREATE TABLE `protocol_test_parameter`
(
    `id`              BIGINT NOT NULL AUTO_INCREMENT COMMENT '测试参数ID',
    `test_record_id`  BIGINT COMMENT '测试记录ID',
    `parameter_name`  VARCHAR(50) COMMENT '参数名称',
    `parameter_value` VARCHAR(500) COMMENT '参数值',
    `create_id`       BIGINT COMMENT '创建人',
    `create_time`     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`       BIGINT COMMENT '更新人',
    `update_time`     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`     TINYINT  DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by`       BIGINT COMMENT '删除人',
    `deleted_time`     DATETIME COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY               `idx_test_record_id` (`test_record_id`),
    CONSTRAINT `fk_ptp_test_record_id` FOREIGN KEY (`test_record_id`) REFERENCES `protocol_test_record` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协议测试参数表';

-- 8. 用户搜索模板表
CREATE TABLE `user_search_template`
(
    `id`                     BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `user_id`                BIGINT COMMENT '用户ID',
    `template_name`          VARCHAR(100) COMMENT '模板名称',
    `search_conditions_json` JSON COMMENT '搜索条件JSON（包含名称、类型、状态、时间范围等）',
    `create_id`              BIGINT COMMENT '创建人',
    `create_time`            DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`              BIGINT COMMENT '更新人',
    `update_time`            DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`            TINYINT  DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by`              BIGINT COMMENT '删除人',
    `deleted_time`            DATETIME COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY                      `idx_user_id` (`user_id`),
    KEY                      `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户搜索模板表';

-- 9. 协议导入模板Schema表
CREATE TABLE `protocol_import_schema`
(
    `id`                BIGINT  NOT NULL AUTO_INCREMENT COMMENT 'Schema ID',
    `schema_name`       VARCHAR(100) COMMENT 'Schema名称',
    `schema_version`    VARCHAR(20) COMMENT 'Schema版本',
    `schema_definition` JSON COMMENT 'Schema定义（JSON Schema格式）',
    `file_format`       VARCHAR(20) COMMENT '文件格式：JSON、YAML、EXCEL',
    `is_active`         TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0-否，1-是',
    `create_id`         BIGINT COMMENT '创建人',
    `create_time`       DATETIME         DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`         BIGINT COMMENT '更新人',
    `update_time`       DATETIME         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`       TINYINT          DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by`         BIGINT COMMENT '删除人',
    `deleted_time`       DATETIME COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY                 `idx_file_format` (`file_format`),
    KEY                 `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协议导入模板Schema表';

-- 10. 文件导入记录表
CREATE TABLE `file_import_record`
(
    `id`                BIGINT NOT NULL AUTO_INCREMENT COMMENT '导入记录ID',
    `user_id`           BIGINT COMMENT '操作用户ID',
    `file_name`         VARCHAR(200) COMMENT '文件名',
    `file_size`         BIGINT COMMENT '文件大小（字节）',
    `file_format`       VARCHAR(20) COMMENT '文件格式：JSON、YAML、EXCEL、CSV',
    `import_type`       VARCHAR(50) COMMENT '导入类型：PROTOCOL_TYPE-协议类型，PROTOCOL_CONFIG-协议配置，PERMISSION-权限',
    `total_count`       INT    NOT NULL DEFAULT 0 COMMENT '总记录数',
    `success_count`     INT    NOT NULL DEFAULT 0 COMMENT '成功记录数',
    `failure_count`     INT    NOT NULL DEFAULT 0 COMMENT '失败记录数',
    `import_status`     TINYINT COMMENT '导入状态：0-失败，1-成功，2-部分成功',
    `import_session_id` VARCHAR(100) COMMENT '导入会话ID',
    `import_start_time` DATETIME COMMENT '导入开始时间',
    `import_end_time`   DATETIME COMMENT '导入结束时间',
    `create_id`         BIGINT COMMENT '创建人',
    `create_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`         BIGINT COMMENT '更新人',
    `update_time`       DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`       TINYINT         DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by`         BIGINT COMMENT '删除人',
    `deleted_time`       DATETIME COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY                 `idx_user_id` (`user_id`),
    KEY                 `idx_import_type` (`import_type`),
    KEY                 `idx_import_status` (`import_status`),
    KEY                 `idx_import_session_id` (`import_session_id`),
    KEY                 `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件导入记录表';

-- 11. 文件导出记录表
CREATE TABLE `file_export_record`
(
    `id`                BIGINT  NOT NULL AUTO_INCREMENT COMMENT '导出记录ID',
    `user_id`           BIGINT COMMENT '操作用户ID',
    `file_name`         VARCHAR(200) COMMENT '导出文件名',
    `file_size`         BIGINT COMMENT '文件大小（字节）',
    `file_format`       VARCHAR(20) COMMENT '文件格式：JSON、YAML、EXCEL、CSV、PDF',
    `export_type`       VARCHAR(50) COMMENT '导出类型：PROTOCOL_TYPE-协议类型，PROTOCOL_CONFIG-协议配置，TEST_RESULT-测试结果，PERMISSION-权限',
    `record_count`      INT     NOT NULL DEFAULT 0 COMMENT '导出记录数',
    `include_sensitive` TINYINT NOT NULL DEFAULT 0 COMMENT '是否包含敏感参数：0-否，1-是',
    `is_masked`         TINYINT NOT NULL DEFAULT 0 COMMENT '是否脱敏：0-否，1-是',
    `storage_path`      VARCHAR(500) COMMENT '文件存储路径',
    `storage_key`       VARCHAR(200) COMMENT '对象存储Key',
    `export_time`       DATETIME COMMENT '导出时间',
    `create_id`         BIGINT COMMENT '创建人',
    `create_time`       DATETIME         DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`         BIGINT COMMENT '更新人',
    `update_time`       DATETIME         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`       TINYINT          DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by`         BIGINT COMMENT '删除人',
    `deleted_time`       DATETIME COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY                 `idx_user_id` (`user_id`),
    KEY                 `idx_export_type` (`export_type`),
    KEY                 `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件导出记录表';

-- 12. 导入错误日志表
CREATE TABLE `import_error_log`
(
    `id`               BIGINT NOT NULL AUTO_INCREMENT COMMENT '错误日志ID',
    `import_record_id` BIGINT COMMENT '导入记录ID',
    `row_number`       INT COMMENT '行号',
    `field_name`       VARCHAR(100) COMMENT '字段名',
    `error_type`       VARCHAR(50) COMMENT '错误类型：FORMAT_ERROR-格式错误，DUPLICATE_ERROR-重复错误，VALIDATION_ERROR-校验错误',
    `error_message`    VARCHAR(500) COMMENT '错误信息',
    `original_value`   VARCHAR(500) COMMENT '原始值',
    `create_id`        BIGINT COMMENT '创建人',
    `create_time`      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`        BIGINT COMMENT '更新人',
    `update_time`      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`      TINYINT  DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by`        BIGINT COMMENT '删除人',
    `deleted_time`      DATETIME COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY                `idx_import_record_id` (`import_record_id`),
    KEY                `idx_error_type` (`error_type`),
    CONSTRAINT `fk_iel_import_record_id` FOREIGN KEY (`import_record_id`) REFERENCES `file_import_record` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导入错误日志表';

-- 13. 格式校验日志表
CREATE TABLE `format_validation_log`
(
    `id`                 BIGINT NOT NULL AUTO_INCREMENT COMMENT '校验日志ID',
    `import_record_id`   BIGINT COMMENT '导入记录ID',
    `schema_id`          BIGINT COMMENT '使用的Schema ID',
    `validation_type`    VARCHAR(50) COMMENT '校验类型：SCHEMA_VALIDATION-Schema校验，STRUCTURE_VALIDATION-结构校验',
    `validation_status`  TINYINT COMMENT '校验状态：0-失败，1-成功',
    `validation_details` JSON COMMENT '校验详情（JSON格式，包含缺失字段、类型错误等）',
    `validation_time`    DATETIME COMMENT '校验时间',
    `create_id`          BIGINT COMMENT '创建人',
    `create_time`        DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`          BIGINT COMMENT '更新人',
    `update_time`        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        TINYINT  DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by`          BIGINT COMMENT '删除人',
    `deleted_time`        DATETIME COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY                  `idx_import_record_id` (`import_record_id`),
    KEY                  `idx_validation_status` (`validation_status`),
    CONSTRAINT `fk_fvl_import_record_id` FOREIGN KEY (`import_record_id`) REFERENCES `file_import_record` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='格式校验日志表';

-- 14. 文件存储元数据表
CREATE TABLE `file_storage_metadata`
(
    `id`           BIGINT NOT NULL AUTO_INCREMENT COMMENT '元数据ID',
    `file_name`    VARCHAR(200) COMMENT '文件名',
    `file_size`    BIGINT COMMENT '文件大小（字节）',
    `file_format`  VARCHAR(20) COMMENT '文件格式',
    `file_type`    VARCHAR(50) COMMENT '文件类型：IMPORT_FILE-导入文件，EXPORT_FILE-导出文件，REPORT-报告',
    `storage_type` VARCHAR(20) COMMENT '存储类型：LOCAL-本地，OSS-对象存储',
    `storage_path` VARCHAR(500) COMMENT '存储路径',
    `storage_key`  VARCHAR(200) COMMENT '对象存储Key',
    `md5_hash`     VARCHAR(50) COMMENT '文件MD5',
    `related_id`   BIGINT COMMENT '关联记录ID（导入记录、导出记录等）',
    `related_type` VARCHAR(50) COMMENT '关联类型：IMPORT_RECORD、EXPORT_RECORD、TEST_REPORT',
    `expired_at`   DATETIME COMMENT '过期时间',
    `create_id`    BIGINT COMMENT '创建人',
    `create_time`  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`    BIGINT COMMENT '更新人',
    `update_time`  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`  TINYINT  DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by`    BIGINT COMMENT '删除人',
    `deleted_time`  DATETIME COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY            `idx_file_type` (`file_type`),
    KEY            `idx_storage_type` (`storage_type`),
    KEY            `idx_related_id` (`related_id`),
    KEY            `idx_create_time` (`create_time`),
    KEY            `idx_expired_at` (`expired_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件存储元数据表';

-- 15. 导入会话参数表
CREATE TABLE `import_session_parameter`
(
    `id`                BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话参数ID',
    `import_session_id` VARCHAR(100) COMMENT '导入会话ID',
    `parameter_name`    VARCHAR(50) COMMENT '参数名称',
    `parameter_value`   VARCHAR(500) COMMENT '参数值',
    `parameter_type`    VARCHAR(20) COMMENT '参数类型：GLOBAL-全局参数，OVERRIDE-覆盖参数',
    `create_id`         BIGINT COMMENT '创建人',
    `create_time`       DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`         BIGINT COMMENT '更新人',
    `update_time`       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`       TINYINT  DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by`         BIGINT COMMENT '删除人',
    `deleted_time`       DATETIME COMMENT '删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_session_param` (`import_session_id`, `parameter_name`),
    KEY                 `idx_import_session_id` (`import_session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导入会话参数表';

-- 16. 报告导出记录表
CREATE TABLE `report_export_record`
(
    `id`              BIGINT NOT NULL AUTO_INCREMENT COMMENT '报告导出记录ID',
    `user_id`         BIGINT COMMENT '操作用户ID',
    `report_name`     VARCHAR(200) COMMENT '报告名称',
    `report_type`     VARCHAR(50) COMMENT '报告类型：TEST_RESULT-测试结果报告，PROTOCOL_SUMMARY-协议汇总报告',
    `file_format`     VARCHAR(20) COMMENT '文件格式：PDF、WORD、EXCEL',
    `test_record_ids` JSON COMMENT '测试记录ID列表（JSON数组）',
    `file_size`       BIGINT COMMENT '文件大小（字节）',
    `storage_path`    VARCHAR(500) COMMENT '文件存储路径',
    `storage_key`     VARCHAR(200) COMMENT '对象存储Key',
    `export_time`     DATETIME COMMENT '导出时间',
    `create_id`       BIGINT COMMENT '创建人',
    `create_time`     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`       BIGINT COMMENT '更新人',
    `update_time`     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`     TINYINT  DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by`       BIGINT COMMENT '删除人',
    `deleted_time`     DATETIME COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY               `idx_user_id` (`user_id`),
    KEY               `idx_report_type` (`report_type`),
    KEY               `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报告导出记录表';

-- 17. 角色-协议权限表
CREATE TABLE `role_protocol_permission`
(
    `id`                BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限记录ID',
    `role_id`           BIGINT COMMENT '角色ID',
    `protocol_id`       BIGINT COMMENT '协议类型ID',
    `permission_bitmap` VARCHAR(20) COMMENT '权限位图：C-创建，R-读取，U-更新，D-删除（如"CRUD"）',
    `create_id`         BIGINT COMMENT '创建人',
    `create_time`       DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`         BIGINT COMMENT '更新人',
    `update_time`       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`       TINYINT  DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by`         BIGINT COMMENT '删除人',
    `deleted_time`       DATETIME COMMENT '删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_protocol` (`role_id`, `protocol_id`),
    KEY                 `idx_protocol_id` (`protocol_id`),
    CONSTRAINT `fk_rpp_protocol_id` FOREIGN KEY (`protocol_id`) REFERENCES `protocol_type` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-协议权限表';

-- 18. 用户-协议权限表
CREATE TABLE `user_protocol_permission`
(
    `id`                BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限记录ID',
    `user_id`           BIGINT COMMENT '用户ID',
    `protocol_id`       BIGINT COMMENT '协议类型ID',
    `permission_bitmap` VARCHAR(20) COMMENT '权限位图：C-创建，R-读取，U-更新，D-删除（如"CRUD"）',
    `create_id`         BIGINT COMMENT '创建人',
    `create_time`       DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id`         BIGINT COMMENT '更新人',
    `update_time`       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`       TINYINT  DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by`         BIGINT COMMENT '删除人',
    `deleted_time`       DATETIME COMMENT '删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_protocol` (`user_id`, `protocol_id`),
    KEY                 `idx_protocol_id` (`protocol_id`),
    CONSTRAINT `fk_upp_protocol_id` FOREIGN KEY (`protocol_id`) REFERENCES `protocol_type` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-协议权限表';
