SET NAMES utf8mb4;
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
    `ext_field5` varchar(2000) COMMENT '扩展字段5',
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


-- 5 产品
INSERT INTO tool_testing.pdm_tool_template_folder
(id, parent_id, name, description, sort_order, icon, color, team_id, visibility, status, create_id, create_name, create_time, update_id, update_name, update_time, is_deleted, deleted_by, deleted_time)
VALUES(5, 0, '产品', '产品类型', 1, NULL, NULL, NULL, 3, 1, 1, '1', now(), NULL, NULL, now(), 0, NULL, NULL);

-- 6 工程专业
INSERT INTO tool_testing.pdm_tool_template_folder
(id, parent_id, name, description, sort_order, icon, color, team_id, visibility, status, create_id, create_name, create_time, update_id, update_name, update_time, is_deleted, deleted_by, deleted_time)
VALUES(6, 0, '工程专业', '工程专业类型', 2, NULL, NULL, NULL, 3, 1, 1, '1', now(), NULL, NULL, now(), 0, NULL, NULL);

-- 7 图纸文档
INSERT INTO tool_testing.pdm_tool_template_folder
(id, parent_id, name, description, sort_order, icon, color, team_id, visibility, status, create_id, create_name, create_time, update_id, update_name, update_time, is_deleted, deleted_by, deleted_time)
VALUES(7, 0, '图纸文档', '图纸文档类型', 3, NULL, NULL, NULL, 3, 1, 1, '1', now(), NULL, NULL, now(), 0, NULL, NULL);

-- 8 物料部件
INSERT INTO tool_testing.pdm_tool_template_folder
(id, parent_id, name, description, sort_order, icon, color, team_id, visibility, status, create_id, create_name, create_time, update_id, update_name, update_time, is_deleted, deleted_by, deleted_time)
VALUES(8, 0, '物料部件', '物料部件类型', 4, NULL, NULL, NULL, 3, 1, 1, '1', now(), NULL, NULL, now(), 0, NULL, NULL);

-- 9 工艺制造
INSERT INTO tool_testing.pdm_tool_template_folder
(id, parent_id, name, description, sort_order, icon, color, team_id, visibility, status, create_id, create_name, create_time, update_id, update_name, update_time, is_deleted, deleted_by, deleted_time)
VALUES(9, 0, '工艺制造', '工艺制造类型', 5, NULL, NULL, NULL, 3, 1, 1, '1', now(), NULL, NULL, now(), 0, NULL, NULL);

-- 10 项目阶段
INSERT INTO tool_testing.pdm_tool_template_folder
(id, parent_id, name, description, sort_order, icon, color, team_id, visibility, status, create_id, create_name, create_time, update_id, update_name, update_time, is_deleted, deleted_by, deleted_time)
VALUES(10, 0, '项目阶段', '项目阶段类型', 6, NULL, NULL, NULL, 3, 1, 1, '1', now(), NULL, NULL, now(), 0, NULL, NULL);

INSERT INTO tool_testing.pdm_tool_template_folder VALUES(11,5,'散货船','散货船',1,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(12,5,'集装箱船','集装箱船',2,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(13,5,'油船','油船',3,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(14,5,'化学品船','化学品船',4,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(15,5,'LNG船','LNG船',5,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(16,5,'滚装船','滚装船',6,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(17,5,'工程船','工程船',7,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(18,5,'海工平台','海工平台',8,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(19,5,'分段/模块','分段/模块',9,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);

INSERT INTO tool_testing.pdm_tool_template_folder VALUES(20,6,'船体工程','船体工程',1,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(21,6,'舾装工程','舾装工程',2,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(22,6,'轮机工程','轮机工程',3,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(23,6,'管系工程','管系工程',4,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(24,6,'电气工程','电气工程',5,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(25,6,'涂装工程','涂装工程',6,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(26,6,'内装工程','内装工程',7,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(27,6,'通风空调','通风空调',8,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);


INSERT INTO tool_testing.pdm_tool_template_folder VALUES(28,7,'总体图纸','总体图纸',1,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(29,7,'结构图纸','结构图纸',2,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(30,7,'管系图纸','管系图纸',3,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(31,7,'电气图纸','电气图纸',4,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(32,7,'舾装图纸','舾装图纸',5,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(33,7,'安装图纸','安装图纸',6,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(34,7,'原理图','原理图',7,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(35,7,'完工图纸','完工图纸',8,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(36,7,'技术文档','技术文档',9,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);

INSERT INTO tool_testing.pdm_tool_template_folder VALUES(37,8,'船体结构件','船体结构件',1,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(38,8,'舾装件','舾装件',2,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(39,8,'轮机设备','轮机设备',3,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(40,8,'管系附件','管系附件',4,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(41,8,'电气设备','电气设备',5,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(42,8,'标准件','标准件',6,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(43,8,'原材料','原材料',7,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(44,8,'涂装材料','涂装材料',8,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(45,8,'内装材料','内装材料',9,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);

INSERT INTO tool_testing.pdm_tool_template_folder VALUES(46,9,'切割工艺','切割工艺',1,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(47,9,'装配工艺','装配工艺',2,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(48,9,'焊接工艺','焊接工艺',3,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(49,9,'涂装工艺','涂装工艺',4,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(50,9,'安装工艺','安装工艺',5,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(51,9,'检验工艺','检验工艺',6,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);

INSERT INTO tool_testing.pdm_tool_template_folder VALUES(52,10,'初步设计','初步设计',1,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(53,10,'详细设计','详细设计',2,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(54,10,'生产设计','生产设计',3,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(55,10,'建造施工','建造施工',4,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(56,10,'下水试验','下水试验',5,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);
INSERT INTO tool_testing.pdm_tool_template_folder VALUES(57,10,'交船运维','交船运维',6,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);


INSERT INTO tool_testing.pdm_tool_template_folder
(id, parent_id, name, description, sort_order, icon, color, team_id, visibility, status, create_id, create_name, create_time, update_id, update_name, update_time, is_deleted, deleted_by, deleted_time)
VALUES
(58,5,'底部分段','船底结构、双层底、肋板',10,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(59,5,'舷侧分段','舷侧外板、纵骨、肋骨',11,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(60,5,'甲板分段','主甲板、平台甲板',12,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(61,5,'舱壁分段','横舱壁、纵舱壁、水密舱壁',13,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(62,5,'上层建筑分段','驾驶台、住舱、甲板室',14,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(63,5,'艏部分段','球鼻艏、锚机舱、艏尖舱',15,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(64,5,'艉部分段','舵机舱、艉尖舱、推进区域',16,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(65,5,'机舱分段','机舱基座、加强结构、设备区域',17,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);

INSERT INTO tool_testing.pdm_tool_template_folder
(id, parent_id, name, description, sort_order, icon, color, team_id, visibility, status, create_id, create_name, create_time, update_id, update_name, update_time, is_deleted, deleted_by, deleted_time)
VALUES
(66,6,'外舾装','锚泊、系泊、救生、消防、梯道栏杆',9,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(67,6,'内舾装','舱室、家具、门窗、绝缘、内装',10,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(68,6,'铁舾件','支架、吊架、基座、加强件、格栅',11,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(69,6,'动力系统','主机、辅机、轴系、推进器',12,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(70,6,'压载系统','压载水舱、压载泵、管路',13,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(71,6,'消防系统','水消防、CO2、泡沫、喷淋',14,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(72,6,'导航通讯系统','雷达、GPS、VDR、通导设备',15,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);

INSERT INTO tool_testing.pdm_tool_template_folder
(id, parent_id, name, description, sort_order, icon, color, team_id, visibility, status, create_id, create_name, create_time, update_id, update_name, update_time, is_deleted, deleted_by, deleted_time)
VALUES
(73,7,'工艺文件','焊接、涂装、装配、切割工艺',10,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(74,7,'检验文件','探伤、试验、报验、船检报告',11,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(75,7,'计算书','强度、稳性、水力、结构计算',12,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(76,7,'技术协议','设备协议、采购规范、技术要求',13,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(77,7,'完工文件','完工图、完工报告、交付文件',14,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(78,7,'变更文件','设计变更、材料代用、现场变更',15,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);

INSERT INTO tool_testing.pdm_tool_template_folder
(id, parent_id, name, description, sort_order, icon, color, team_id, visibility, status, create_id, create_name, create_time, update_id, update_name, update_time, is_deleted, deleted_by, deleted_time)
VALUES
(79,8,'设计BOM(EBOM)','设计阶段物料清单',10,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(80,8,'工艺BOM(PBOM)','工艺规划阶段物料清单',11,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(81,8,'制造BOM(MBOM)','生产制造物料清单',12,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(82,8,'采购BOM','采购计划、采购清单',13,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(83,8,'自制件','船厂自行加工零件',14,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(84,8,'外购件','外部采购成品设备',15,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(85,8,'外协件','委外加工部件、分段',16,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);

INSERT INTO tool_testing.pdm_tool_template_folder
(id, parent_id, name, description, sort_order, icon, color, team_id, visibility, status, create_id, create_name, create_time, update_id, update_name, update_time, is_deleted, deleted_by, deleted_time)
VALUES
(86,10,'CCS规范','中国船级社规范',7,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(87,10,'LR规范','英国劳氏船级社规范',8,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(88,10,'ABS规范','美国船级社规范',9,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(89,10,'SOLAS公约','国际海上人命安全公约',10,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL),
(90,10,'MARPOL公约','国际防污染公约',11,NULL,NULL,NULL,3,1,1,'1',now(),NULL,NULL,now(),0,NULL,NULL);


INSERT INTO tool_testing.pdm_tool_interface_template
(id, folder_id, name, description, protocol_id, protocol_type, `method`, base_url, `path`, full_url, auth_type, auth_config, content_type, charset, body_type, body_content, body_raw_type, connect_timeout, read_timeout, retry_count, retry_interval, version, version_remark, is_latest, ref_template_id, tags, team_id, visibility, pdm_system_type, pdm_module, business_scene, file_count, has_request_file, has_response_file, status, use_count, last_use_time, ext_field1, ext_field2, ext_field3, ext_field4, ext_field5, ext_num1, ext_num2, create_id, create_name, create_time, update_id, update_name, update_time, is_deleted, deleted_by, deleted_time)
VALUES

-- 产品 (folder_id:11-19)
(7,11,'散货船',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'散货船负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(8,12,'集装箱船',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'集装箱船负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(9,13,'油船',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'油船负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(10,14,'化学品船',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'化学品船负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(11,15,'LNG船',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'LNG船负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(12,16,'滚装船',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'滚装船负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(13,17,'工程船',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'工程船负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(14,18,'海工平台',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'海工平台负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(15,19,'分段/模块',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'分段/模块负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),

-- 工程专业 (20-27)
(16,20,'船体工程',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'船体工程负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(17,21,'舾装工程',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'舾装工程负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(18,22,'轮机工程',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'轮机工程负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(19,23,'管系工程',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'管系工程负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(20,24,'电气工程',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'电气工程负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(21,25,'涂装工程',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'涂装工程负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(22,26,'内装工程',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'内装工程负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(23,27,'通风空调',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'通风空调负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),

-- 图纸文档 (28-36)
(24,28,'总体图纸',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'总体图纸负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(25,29,'结构图纸',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'结构图纸负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(26,30,'管系图纸',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'管系图纸负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(27,31,'电气图纸',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'电气图纸负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(28,32,'舾装图纸',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'舾装图纸负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(29,33,'安装图纸',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'安装图纸负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(30,34,'原理图',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'原理图负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(31,35,'完工图纸',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'完工图纸负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(32,36,'技术文档',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'技术文档负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),

-- 物料部件 (37-45)
(33,37,'船体结构件',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'船体结构件负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(34,38,'舾装件',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'舾装件负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(35,39,'轮机设备',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'轮机设备负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(36,40,'管系附件',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'管系附件负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(37,41,'电气设备',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'电气设备负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(38,42,'标准件',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'标准件负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(39,43,'原材料',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'原材料负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(40,44,'涂装材料',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'涂装材料负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(41,45,'内装材料',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'内装材料负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),

-- 工艺制造 (46-51)
(42,46,'切割工艺',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'切割工艺负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(43,47,'装配工艺',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'装配工艺负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(44,48,'焊接工艺',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'焊接工艺负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(45,49,'涂装工艺',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'涂装工艺负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(46,50,'安装工艺',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'安装工艺负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(47,51,'检验工艺',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'检验工艺负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),

-- 项目阶段 (52-57)
(48,52,'初步设计',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'初步设计负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(49,53,'详细设计',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'详细设计负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(50,54,'生产设计',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'生产设计负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(51,55,'建造施工',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'建造施工负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(52,56,'下水试验',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'下水试验负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(53,57,'交船运维',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'交船运维负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),

-- 补充：分段/区域 (58-65)
(54,58,'底部分段',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'底部分段负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(55,59,'舷侧分段',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'舷侧分段负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(56,60,'甲板分段',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'甲板分段负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(57,61,'舱壁分段',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'舱壁分段负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(58,62,'上层建筑分段',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'上层建筑分段负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(59,63,'艏部分段',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'艏部分段负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(60,64,'艉部分段',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'艉部分段负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(61,65,'机舱分段',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'机舱分段负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),

-- 补充：舾装/系统 (66-72)
(62,66,'外舾装',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'外舾装负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(63,67,'内舾装',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'内舾装负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(64,68,'铁舾件',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'铁舾件负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(65,69,'动力系统',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'动力系统负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(66,70,'压载系统',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'压载系统负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(67,71,'消防系统',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'消防系统负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(68,72,'导航通讯系统',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'导航通讯系统负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),

-- 补充：工艺/检验/完工 (73-78)
(69,73,'工艺文件',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'工艺文件负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(70,74,'检验文件',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'检验文件负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(71,75,'计算书',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'计算书负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(72,76,'技术协议',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'技术协议负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(73,77,'完工文件',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'完工文件负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(74,78,'变更文件',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'变更文件负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),

-- 补充：BOM/来源 (79-85)
(75,79,'设计BOM(EBOM)',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'设计BOM(EBOM)负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(76,80,'工艺BOM(PBOM)',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'工艺BOM(PBOM)负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(77,81,'制造BOM(MBOM)',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'制造BOM(MBOM)负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(78,82,'采购BOM',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'采购BOM负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(79,83,'自制件',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'自制件负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(80,84,'外购件',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'外购件负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(81,85,'外协件',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'外协件负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),

-- 补充：船级社/规范 (86-90)
(82,86,'CCS规范',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'CCS规范负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(83,87,'LR规范',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'LR规范负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(84,88,'ABS规范',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'ABS规范负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(85,89,'SOLAS公约',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'SOLAS公约负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL),
(86,90,'MARPOL公约',NULL,NULL,'HTTP','GET',NULL,NULL,NULL,'NONE',NULL,'application/json','UTF-8','NONE',NULL,'JSON',30000,30000,0,1000,'V1.0',NULL,1,NULL,NULL,NULL,1,'0',NULL,NULL,0,0,0,0,0,NULL,'MARPOL公约负责人',NULL,NULL,NULL,NULL,NULL,NULL,1,'管理员',now(),NULL,NULL,now(),0,NULL,NULL);