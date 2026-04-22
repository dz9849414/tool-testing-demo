
DROP TABLE IF EXISTS `pdm_tool_template_execute_log`;
DROP TABLE IF EXISTS `pdm_tool_template_job_log`;
DROP TABLE IF EXISTS `pdm_tool_template_job_item`;
DROP TABLE IF EXISTS `pdm_tool_template_job_batch`;
DROP TABLE IF EXISTS `pdm_tool_template_job`;
DROP TABLE IF EXISTS `pdm_tool_template_file`;
DROP TABLE IF EXISTS `pdm_tool_template_share`;
DROP TABLE IF EXISTS `pdm_tool_template_import_export`;
DROP TABLE IF EXISTS `pdm_tool_template_usage_log`;
DROP TABLE IF EXISTS `pdm_tool_template_favorite`;
DROP TABLE IF EXISTS `pdm_tool_template_history`;
DROP TABLE IF EXISTS `pdm_tool_template_environment`;
DROP TABLE IF EXISTS `pdm_tool_template_variable`;
DROP TABLE IF EXISTS `pdm_tool_template_post_processor`;
DROP TABLE IF EXISTS `pdm_tool_template_pre_processor`;
DROP TABLE IF EXISTS `pdm_tool_template_assertion`;
DROP TABLE IF EXISTS `pdm_tool_template_form_data`;
DROP TABLE IF EXISTS `pdm_tool_template_parameter`;
DROP TABLE IF EXISTS `pdm_tool_template_header`;
DROP TABLE IF EXISTS `pdm_tool_interface_template`;
DROP TABLE IF EXISTS `pdm_tool_template_folder`;
DROP TABLE IF EXISTS `pdm_tool_mock_pdm_json_data`;



-- ============================================
-- PDM工具模板模块数据库结构
-- 表前缀：pdm_tool_
-- ============================================

CREATE TABLE IF NOT EXISTS `pdm_tool_template_folder` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '目录ID',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父级目录ID',
    `name` VARCHAR(100) NOT NULL COMMENT '目录名称',
    `description` VARCHAR(500) COMMENT '目录描述',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `icon` VARCHAR(50) COMMENT '图标',
    `color` VARCHAR(20) COMMENT '颜色',
    `team_id` BIGINT COMMENT '团队ID',
    `visibility` TINYINT DEFAULT 1 COMMENT '可见性：1-私有，2-团队，3-公开',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    `create_id` BIGINT COMMENT '创建人',
    `create_name`  VARCHAR(200) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name`  VARCHAR(200) COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_team_id` (`team_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板目录表';

CREATE TABLE IF NOT EXISTS `pdm_tool_interface_template` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    `folder_id` BIGINT COMMENT '目录ID',
    `name` VARCHAR(200) NOT NULL COMMENT '模板名称',
    `description` TEXT COMMENT '模板描述',
    `protocol_id` BIGINT COMMENT '协议ID',
    `protocol_type` VARCHAR(50) NOT NULL COMMENT '协议类型',
    `method` VARCHAR(20) COMMENT 'HTTP请求方法',
    `base_url` VARCHAR(500) COMMENT '基础URL',
    `path` VARCHAR(1000) COMMENT '请求路径',
    `full_url` VARCHAR(1500) COMMENT '完整URL',
    `auth_type` VARCHAR(30) COMMENT '认证类型',
    `auth_config` JSON COMMENT '认证配置',
    `content_type` VARCHAR(100) COMMENT '内容类型',
    `charset` VARCHAR(20) DEFAULT 'UTF-8' COMMENT '字符集',
    `body_type` VARCHAR(20) COMMENT '请求体类型',
    `body_content` LONGTEXT COMMENT '请求体内容',
    `body_raw_type` VARCHAR(20) COMMENT '原始请求体类型',
    `connect_timeout` INT DEFAULT 30000 COMMENT '连接超时时间(ms)',
    `read_timeout` INT DEFAULT 30000 COMMENT '读取超时时间(ms)',
    `retry_count` INT DEFAULT 0 COMMENT '重试次数',
    `retry_interval` INT DEFAULT 1000 COMMENT '重试间隔(ms)',
    `version` VARCHAR(20) DEFAULT '1.0.0' COMMENT '版本号',
    `version_remark` VARCHAR(500) COMMENT '版本备注',
    `is_latest` TINYINT DEFAULT 1 COMMENT '是否最新版本',
    `ref_template_id` BIGINT COMMENT '引用模板ID',
    `tags` VARCHAR(500) COMMENT '标签',
    `team_id` BIGINT COMMENT '团队ID',
    `visibility` TINYINT DEFAULT 1 COMMENT '可见性：1-私有，2-团队，3-公开',
    `pdm_system_type` VARCHAR(50) COMMENT 'PDM系统类型',
    `pdm_module` VARCHAR(100) COMMENT 'PDM模块',
    `business_scene` VARCHAR(200) COMMENT '业务场景',
    `file_count` INT DEFAULT 0 COMMENT '文件数量',
    `has_request_file` INT DEFAULT 0 COMMENT '是否包含请求文件',
    `has_response_file` INT DEFAULT 0 COMMENT '是否包含响应文件',
    `status` TINYINT DEFAULT 1 COMMENT '状态',
    `use_count` INT DEFAULT 0 COMMENT '使用次数',
    `last_use_time` DATETIME COMMENT '最后使用时间',
    `ext_field1` VARCHAR(500) COMMENT '扩展字段1',
    `ext_field2` VARCHAR(500) COMMENT '扩展字段2',
    `ext_field3` VARCHAR(500) COMMENT '扩展字段3',
    `ext_field4` TEXT COMMENT '扩展字段4',
    `ext_field5` JSON COMMENT '扩展字段5',
    `ext_num1` BIGINT COMMENT '扩展数字字段1',
    `ext_num2` BIGINT COMMENT '扩展数字字段2',
    `create_id` BIGINT COMMENT '创建人',
    `create_name`  VARCHAR(200) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name`  VARCHAR(200) COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_folder_id` (`folder_id`),
    INDEX `idx_protocol_id` (`protocol_id`),
    INDEX `idx_protocol_type` (`protocol_type`),
    INDEX `idx_method` (`method`),
    INDEX `idx_team_id` (`team_id`),
    INDEX `idx_visibility` (`visibility`),
    INDEX `idx_status` (`status`),
    INDEX `idx_ref_template_id` (`ref_template_id`),
    INDEX `idx_is_latest` (`is_latest`),
    INDEX `idx_pdm_system_type` (`pdm_system_type`),
    INDEX `idx_is_deleted` (`is_deleted`),
    FULLTEXT INDEX `idx_name_tags` (`name`, `tags`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接口模板主表';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_header` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `header_name` VARCHAR(200) NOT NULL COMMENT '请求头名称',
    `header_value` VARCHAR(1000) COMMENT '请求头值',
    `description` VARCHAR(500) COMMENT '描述',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用',
    `is_required` TINYINT DEFAULT 0 COMMENT '是否必填',
    `is_variable` TINYINT DEFAULT 0 COMMENT '是否变量',
    `variable_name` VARCHAR(100) COMMENT '变量名称',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `create_id` BIGINT COMMENT '创建人',
    `create_name`  VARCHAR(200) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name`  VARCHAR(200) COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板请求头表';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_parameter` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `param_type` VARCHAR(20) NOT NULL COMMENT '参数类型',
    `param_name` VARCHAR(200) NOT NULL COMMENT '参数名称',
    `param_value` VARCHAR(1000) COMMENT '参数值',
    `data_type` VARCHAR(30) DEFAULT 'STRING' COMMENT '数据类型',
    `description` VARCHAR(500) COMMENT '描述',
    `example_value` VARCHAR(1000) COMMENT '示例值',
    `is_required` TINYINT DEFAULT 0 COMMENT '是否必填',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用',
    `is_variable` TINYINT DEFAULT 0 COMMENT '是否变量',
    `variable_name` VARCHAR(100) COMMENT '变量名称',
    `validation_rules` JSON COMMENT '校验规则',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `create_id` BIGINT COMMENT '创建人',
    `create_name`  VARCHAR(200) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name`  VARCHAR(200) COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_param_type` (`param_type`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板参数表';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_form_data` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `field_name` VARCHAR(200) NOT NULL COMMENT '字段名称',
    `field_type` VARCHAR(20) DEFAULT 'TEXT' COMMENT '字段类型',
    `field_value` TEXT COMMENT '字段值',
    `file_path` VARCHAR(1000) COMMENT '文件路径',
    `file_name` VARCHAR(255) COMMENT '文件名称',
    `content_type` VARCHAR(100) COMMENT '内容类型',
    `description` VARCHAR(500) COMMENT '描述',
    `is_required` TINYINT DEFAULT 0 COMMENT '是否必填',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用',
    `is_variable` TINYINT DEFAULT 0 COMMENT '是否变量',
    `variable_name` VARCHAR(100) COMMENT '变量名称',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `create_id` BIGINT COMMENT '创建人',
    `create_name`  VARCHAR(200) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name`  VARCHAR(200) COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板表单数据表';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_assertion` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `assert_name` VARCHAR(200) COMMENT '断言名称',
    `assert_type` VARCHAR(50) NOT NULL COMMENT '断言类型',
    `extract_path` VARCHAR(500) COMMENT '提取路径',
    `expected_value` TEXT COMMENT '期望值',
    `operator` VARCHAR(30) COMMENT '操作符',
    `data_type` VARCHAR(20) DEFAULT 'STRING' COMMENT '数据类型',
    `error_message` VARCHAR(500) COMMENT '错误信息',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用',
    `assert_group` VARCHAR(100) DEFAULT 'default' COMMENT '断言分组',
    `logic_type` VARCHAR(10) DEFAULT 'AND' COMMENT '逻辑类型',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `create_id` BIGINT COMMENT '创建人',
    `create_name`  VARCHAR(200) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name`  VARCHAR(200) COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板断言表';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_pre_processor` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `processor_name` VARCHAR(200) COMMENT '处理器名称',
    `processor_type` VARCHAR(50) NOT NULL COMMENT '处理器类型',
    `config` JSON COMMENT '处理器配置',
    `script_content` TEXT COMMENT '脚本内容',
    `target_variable` VARCHAR(200) COMMENT '目标变量',
    `variable_scope` VARCHAR(20) DEFAULT 'TEMPLATE' COMMENT '变量作用域',
    `description` VARCHAR(500) COMMENT '描述',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `create_id` BIGINT COMMENT '创建人',
    `create_name`  VARCHAR(200) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name`  VARCHAR(200) COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板前置处理器表';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_post_processor` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `processor_name` VARCHAR(200) COMMENT '处理器名称',
    `processor_type` VARCHAR(50) NOT NULL COMMENT '处理器类型',
    `extract_type` VARCHAR(30) COMMENT '提取类型',
    `extract_expression` VARCHAR(1000) COMMENT '提取表达式',
    `extract_match_no` INT DEFAULT 0 COMMENT '提取匹配序号',
    `target_variable` VARCHAR(200) COMMENT '目标变量',
    `variable_scope` VARCHAR(20) DEFAULT 'TEMPLATE' COMMENT '变量作用域',
    `default_value` VARCHAR(500) COMMENT '默认值',
    `config` JSON COMMENT '处理器配置',
    `script_content` TEXT COMMENT '脚本内容',
    `description` VARCHAR(500) COMMENT '描述',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `create_id` BIGINT COMMENT '创建人',
    `create_name`  VARCHAR(200) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name`  VARCHAR(200) COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板后置处理器表';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_variable` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `variable_name` VARCHAR(200) NOT NULL COMMENT '变量名称',
    `variable_type` VARCHAR(30) DEFAULT 'STRING' COMMENT '变量类型',
    `default_value` TEXT COMMENT '默认值',
    `current_value` TEXT COMMENT '当前值',
    `description` VARCHAR(500) COMMENT '描述',
    `example_value` VARCHAR(500) COMMENT '示例值',
    `is_required` TINYINT DEFAULT 0 COMMENT '是否必填',
    `is_editable` TINYINT DEFAULT 1 COMMENT '是否可编辑',
    `is_persistent` TINYINT DEFAULT 0 COMMENT '是否持久化',
    `source_type` VARCHAR(30) DEFAULT 'MANUAL' COMMENT '来源类型',
    `source_config` JSON COMMENT '来源配置',
    `validation_rules` JSON COMMENT '校验规则',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `create_id` BIGINT COMMENT '创建人',
    `create_name`  VARCHAR(200) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name`  VARCHAR(200) COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_is_deleted` (`is_deleted`),
    UNIQUE KEY `uk_template_variable` (`template_id`, `variable_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板变量表';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_environment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `env_name` VARCHAR(100) NOT NULL COMMENT '环境名称',
    `env_code` VARCHAR(50) COMMENT '环境编码',
    `base_url` VARCHAR(500) COMMENT '基础URL',
    `headers` JSON COMMENT '请求头配置',
    `variables` JSON COMMENT '变量配置',
    `auth_type` VARCHAR(30) COMMENT '认证类型',
    `auth_config` JSON COMMENT '认证配置',
    `proxy_enabled` TINYINT DEFAULT 0 COMMENT '是否启用代理',
    `proxy_host` VARCHAR(200) COMMENT '代理主机',
    `proxy_port` INT COMMENT '代理端口',
    `proxy_username` VARCHAR(100) COMMENT '代理用户名',
    `proxy_password` VARCHAR(200) COMMENT '代理密码',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认',
    `description` VARCHAR(500) COMMENT '描述',
    `create_id` BIGINT COMMENT '创建人',
    `create_name`  VARCHAR(200) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name`  VARCHAR(200) COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板环境配置表';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_history` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `version` VARCHAR(20) NOT NULL COMMENT '版本号',
    `version_type` VARCHAR(20) DEFAULT 'AUTO' COMMENT '版本类型',
    `change_summary` VARCHAR(1000) COMMENT '变更摘要',
    `change_details` TEXT COMMENT '变更详情',
    `template_snapshot` LONGTEXT COMMENT '模板快照',
    `operation_type` VARCHAR(30) COMMENT '操作类型',
    `can_rollback` TINYINT DEFAULT 1 COMMENT '是否可回滚',
    `rollback_to_time` DATETIME COMMENT '回滚时间',
    `create_id` BIGINT COMMENT '创建人',
    `create_name`  VARCHAR(200) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name`  VARCHAR(200) COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_version` (`version`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板历史版本表';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_favorite` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `favorite_type` TINYINT DEFAULT 1 COMMENT '收藏类型',
    `remark` VARCHAR(200) COMMENT '备注',
    `create_id` BIGINT COMMENT '创建人',
    `create_name`  VARCHAR(200) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name`  VARCHAR(200) COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_create_id` (`create_id`),
    INDEX `idx_is_deleted` (`is_deleted`),
    UNIQUE KEY `uk_template_user` (`template_id`, `create_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板收藏表';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_usage_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `usage_type` VARCHAR(30) COMMENT '使用类型',
    `task_id` BIGINT COMMENT '任务ID',
    `execution_result` TINYINT COMMENT '执行结果',
    `execution_duration` INT COMMENT '执行耗时',
    `request_summary` VARCHAR(1000) COMMENT '请求摘要',
    `response_summary` VARCHAR(1000) COMMENT '响应摘要',
    `error_message` TEXT COMMENT '错误信息',
    `client_ip` VARCHAR(50) COMMENT '客户端IP',
    `user_agent` VARCHAR(500) COMMENT '用户代理',
    `create_id` BIGINT COMMENT '创建人',
    `create_name`  VARCHAR(200) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name`  VARCHAR(200) COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_create_id` (`create_id`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板使用日志表';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_import_export` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `operation_type` VARCHAR(20) NOT NULL COMMENT '操作类型',
    `template_ids` VARCHAR(1000) COMMENT '模板ID集合',
    `folder_id` BIGINT COMMENT '目录ID',
    `file_name` VARCHAR(255) COMMENT '文件名称',
    `file_path` VARCHAR(1000) COMMENT '文件路径',
    `file_size` BIGINT COMMENT '文件大小',
    `file_format` VARCHAR(20) COMMENT '文件格式',
    `status` TINYINT DEFAULT 0 COMMENT '状态',
    `success_count` INT COMMENT '成功数量',
    `fail_count` INT COMMENT '失败数量',
    `error_message` TEXT COMMENT '错误信息',
    `start_time` DATETIME COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `create_id` BIGINT COMMENT '创建人',
    `create_name`  VARCHAR(200) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name`  VARCHAR(200) COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_operation_type` (`operation_type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板导入导出记录表';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_share` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `share_type` VARCHAR(20) NOT NULL COMMENT '分享类型',
    `share_target_id` BIGINT NOT NULL COMMENT '分享目标ID',
    `share_target_name` VARCHAR(100) COMMENT '分享目标名称',
    `permission` VARCHAR(50) COMMENT '权限',
    `can_share` TINYINT DEFAULT 0 COMMENT '是否可继续分享',
    `expire_time` DATETIME COMMENT '过期时间',
    `share_code` VARCHAR(100) COMMENT '分享码',
    `share_link` VARCHAR(500) COMMENT '分享链接',
    `access_password` VARCHAR(100) COMMENT '访问密码',
    `create_id` BIGINT COMMENT '创建人',
    `create_name`  VARCHAR(200) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name`  VARCHAR(200) COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板分享表';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_file` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文件ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `file_name` VARCHAR(255) NOT NULL COMMENT '存储文件名',
    `file_original_name` VARCHAR(255) COMMENT '原始文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
    `file_url` VARCHAR(500) COMMENT '文件访问URL',
    `file_size` BIGINT COMMENT '文件大小',
    `file_type` VARCHAR(100) COMMENT 'MIME类型',
    `file_extension` VARCHAR(50) COMMENT '文件扩展名',
    `file_category` VARCHAR(50) DEFAULT 'ATTACHMENT' COMMENT '文件分类',
    `file_description` VARCHAR(500) COMMENT '文件描述',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `create_id` BIGINT COMMENT '创建人',
    `create_name`  VARCHAR(200) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name`  VARCHAR(200) COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_file_category` (`file_category`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板文件表';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_job` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    `job_name` VARCHAR(100) NOT NULL COMMENT '任务名称',
    `cron_expression` VARCHAR(50) COMMENT 'Cron表达式',
    `status` TINYINT DEFAULT 1 COMMENT '状态',
    `description` VARCHAR(500) COMMENT '描述',
    `xxl_job_id` INT COMMENT 'XXL任务ID',
    `last_execute_time` DATETIME COMMENT '最后执行时间',
    `create_id` BIGINT COMMENT '创建人',
    `create_name`  VARCHAR(200) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name`  VARCHAR(200) COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_status` (`status`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板任务表';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_job_item` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务项ID',
    `job_id` BIGINT NOT NULL COMMENT '任务ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `environment_id` BIGINT COMMENT '环境ID',
    `variables` TEXT COMMENT '变量配置JSON',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `status` TINYINT DEFAULT 1 COMMENT '状态',
    `create_id` BIGINT COMMENT '创建人',
    `create_name`  VARCHAR(200) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name`  VARCHAR(200) COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_job_id` (`job_id`),
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_environment_id` (`environment_id`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板任务项表';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_job_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务日志ID',
    `job_id` BIGINT NOT NULL COMMENT '任务ID',
    `template_id` BIGINT COMMENT '模板ID',
    `xxl_job_log_id` BIGINT COMMENT 'XXL任务日志ID',
    `execute_result` TEXT COMMENT '执行结果JSON',
    `success` TINYINT DEFAULT 0 COMMENT '是否成功',
    `duration_ms` BIGINT COMMENT '耗时(ms)',
    `error_msg` TEXT COMMENT '错误信息',
    `trace_id` VARCHAR(64) COMMENT '链路ID',
    `execute_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
    `create_id` BIGINT COMMENT '创建人',
    `create_name`  VARCHAR(200) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name`  VARCHAR(200) COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_job_id` (`job_id`),
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_trace_id` (`trace_id`),
    INDEX `idx_execute_at` (`execute_at`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板任务日志表';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_execute_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '执行日志ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `template_name` VARCHAR(200) COMMENT '模板名称',
    `job_id` BIGINT COMMENT '任务ID',
    `job_name` VARCHAR(100) COMMENT '任务名称',
    `execute_type` VARCHAR(20) NOT NULL COMMENT '执行类型',
    `environment_id` BIGINT COMMENT '环境ID',
    `success` TINYINT DEFAULT 0 COMMENT '是否成功',
    `status_code` INT COMMENT '状态码',
    `duration_ms` BIGINT COMMENT '耗时(ms)',
    `execute_result` TEXT COMMENT '执行结果',
    `error_msg` TEXT COMMENT '错误信息',
    `trace_id` VARCHAR(64) COMMENT '链路ID',
    `execute_user_id` BIGINT COMMENT '执行用户ID',
    `execute_user_name` VARCHAR(100) COMMENT '执行人姓名',
    `execute_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
    `create_id` BIGINT COMMENT '创建人',
    `create_name`  VARCHAR(200) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name`  VARCHAR(200) COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_job_id` (`job_id`),
    INDEX `idx_environment_id` (`environment_id`),
    INDEX `idx_execute_type` (`execute_type`),
    INDEX `idx_success` (`success`),
    INDEX `idx_trace_id` (`trace_id`),
    INDEX `idx_execute_at` (`execute_at`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板执行日志表';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_job_batch` (
    `id` VARCHAR(64) PRIMARY KEY COMMENT '批次ID',
    `status` VARCHAR(20) NOT NULL COMMENT '批次状态',
    `result` LONGTEXT COMMENT '批次结果',
    `create_id` BIGINT COMMENT '创建人',
    `create_name`  VARCHAR(200) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name`  VARCHAR(200) COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_status` (`status`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板任务批次表';

CREATE TABLE IF NOT EXISTS `pdm_tool_mock_pdm_json_data` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模拟数据ID',
    `data_json` JSON NOT NULL COMMENT 'PDM模拟JSON数据表',
    `create_id` BIGINT COMMENT '创建人',
    `create_name`  VARCHAR(200) COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name`  VARCHAR(200) COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PDM模拟JSON数据表';

ALTER TABLE `pdm_tool_interface_template`
    ADD CONSTRAINT `fk_pdm_tool_template_folder`
    FOREIGN KEY (`folder_id`) REFERENCES `pdm_tool_template_folder` (`id`) ON DELETE SET NULL;

ALTER TABLE `pdm_tool_template_header`
    ADD CONSTRAINT `fk_pdm_tool_header_template`
    FOREIGN KEY (`template_id`) REFERENCES `pdm_tool_interface_template` (`id`) ON DELETE CASCADE;

ALTER TABLE `pdm_tool_template_parameter`
    ADD CONSTRAINT `fk_pdm_tool_parameter_template`
    FOREIGN KEY (`template_id`) REFERENCES `pdm_tool_interface_template` (`id`) ON DELETE CASCADE;

ALTER TABLE `pdm_tool_template_form_data`
    ADD CONSTRAINT `fk_pdm_tool_form_data_template`
    FOREIGN KEY (`template_id`) REFERENCES `pdm_tool_interface_template` (`id`) ON DELETE CASCADE;

ALTER TABLE `pdm_tool_template_assertion`
    ADD CONSTRAINT `fk_pdm_tool_assertion_template`
    FOREIGN KEY (`template_id`) REFERENCES `pdm_tool_interface_template` (`id`) ON DELETE CASCADE;

ALTER TABLE `pdm_tool_template_pre_processor`
    ADD CONSTRAINT `fk_pdm_tool_pre_processor_template`
    FOREIGN KEY (`template_id`) REFERENCES `pdm_tool_interface_template` (`id`) ON DELETE CASCADE;

ALTER TABLE `pdm_tool_template_post_processor`
    ADD CONSTRAINT `fk_pdm_tool_post_processor_template`
    FOREIGN KEY (`template_id`) REFERENCES `pdm_tool_interface_template` (`id`) ON DELETE CASCADE;

ALTER TABLE `pdm_tool_template_variable`
    ADD CONSTRAINT `fk_pdm_tool_variable_template`
    FOREIGN KEY (`template_id`) REFERENCES `pdm_tool_interface_template` (`id`) ON DELETE CASCADE;

ALTER TABLE `pdm_tool_template_environment`
    ADD CONSTRAINT `fk_pdm_tool_environment_template`
    FOREIGN KEY (`template_id`) REFERENCES `pdm_tool_interface_template` (`id`) ON DELETE CASCADE;

ALTER TABLE `pdm_tool_template_history`
    ADD CONSTRAINT `fk_pdm_tool_history_template`
    FOREIGN KEY (`template_id`) REFERENCES `pdm_tool_interface_template` (`id`) ON DELETE CASCADE;

ALTER TABLE `pdm_tool_template_favorite`
    ADD CONSTRAINT `fk_pdm_tool_favorite_template`
    FOREIGN KEY (`template_id`) REFERENCES `pdm_tool_interface_template` (`id`) ON DELETE CASCADE;

ALTER TABLE `pdm_tool_template_usage_log`
    ADD CONSTRAINT `fk_pdm_tool_usage_log_template`
    FOREIGN KEY (`template_id`) REFERENCES `pdm_tool_interface_template` (`id`) ON DELETE CASCADE;

ALTER TABLE `pdm_tool_template_share`
    ADD CONSTRAINT `fk_pdm_tool_share_template`
    FOREIGN KEY (`template_id`) REFERENCES `pdm_tool_interface_template` (`id`) ON DELETE CASCADE;

ALTER TABLE `pdm_tool_template_file`
    ADD CONSTRAINT `fk_pdm_tool_file_template`
    FOREIGN KEY (`template_id`) REFERENCES `pdm_tool_interface_template` (`id`) ON DELETE CASCADE;

ALTER TABLE `pdm_tool_template_job_item`
    ADD CONSTRAINT `fk_pdm_tool_job_item_job`
    FOREIGN KEY (`job_id`) REFERENCES `pdm_tool_template_job` (`id`) ON DELETE CASCADE;

ALTER TABLE `pdm_tool_template_job_item`
    ADD CONSTRAINT `fk_pdm_tool_job_item_template`
    FOREIGN KEY (`template_id`) REFERENCES `pdm_tool_interface_template` (`id`) ON DELETE CASCADE;

ALTER TABLE `pdm_tool_template_job_item`
    ADD CONSTRAINT `fk_pdm_tool_job_item_environment`
    FOREIGN KEY (`environment_id`) REFERENCES `pdm_tool_template_environment` (`id`) ON DELETE SET NULL;

ALTER TABLE `pdm_tool_template_job_log`
    ADD CONSTRAINT `fk_pdm_tool_job_log_job`
    FOREIGN KEY (`job_id`) REFERENCES `pdm_tool_template_job` (`id`) ON DELETE CASCADE;

ALTER TABLE `pdm_tool_template_job_log`
    ADD CONSTRAINT `fk_pdm_tool_job_log_template`
    FOREIGN KEY (`template_id`) REFERENCES `pdm_tool_interface_template` (`id`) ON DELETE SET NULL;

ALTER TABLE `pdm_tool_template_execute_log`
    ADD CONSTRAINT `fk_pdm_tool_execute_log_template`
    FOREIGN KEY (`template_id`) REFERENCES `pdm_tool_interface_template` (`id`) ON DELETE CASCADE;

ALTER TABLE `pdm_tool_template_execute_log`
    ADD CONSTRAINT `fk_pdm_tool_execute_log_job`
    FOREIGN KEY (`job_id`) REFERENCES `pdm_tool_template_job` (`id`) ON DELETE SET NULL;

INSERT INTO `pdm_tool_template_folder`
    (`id`, `parent_id`, `name`, `description`, `sort_order`, `create_id`, `create_name`, `visibility`)
VALUES
    (1, 0, 'Default Folder', 'System default folder', 0, 1, 1, 3),
    (2, 0, 'CAD APIs', 'CAD related API templates', 1, 1, 1, 3),
    (3, 0, 'ERP APIs', 'ERP related API templates', 2, 1, 1, 3),
    (4, 0, 'PLM APIs', 'PLM related API templates', 3, 1, 1, 3);

ALTER TABLE `pdm_tool_template_folder` AUTO_INCREMENT = 100;
ALTER TABLE `pdm_tool_interface_template` AUTO_INCREMENT = 1000;
