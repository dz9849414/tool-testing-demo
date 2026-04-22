-- ============================================
-- PDM tool template module schema
-- Table prefix: pdm_tool_
-- ============================================

CREATE TABLE IF NOT EXISTS `pdm_tool_template_folder` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Folder ID',
    `parent_id` BIGINT DEFAULT 0 COMMENT 'Parent folder ID',
    `name` VARCHAR(100) NOT NULL COMMENT 'Folder name',
    `description` VARCHAR(500) COMMENT 'Folder description',
    `sort_order` INT DEFAULT 0 COMMENT 'Sort order',
    `icon` VARCHAR(50) COMMENT 'Icon',
    `color` VARCHAR(20) COMMENT 'Color',
    `team_id` BIGINT COMMENT 'Team ID',
    `visibility` TINYINT DEFAULT 1 COMMENT 'Visibility: 1-private, 2-team, 3-public',
    `status` TINYINT DEFAULT 1 COMMENT 'Status: 0-disabled, 1-enabled',
    `create_id` BIGINT COMMENT '创建人',
    `create_name` BIGINT COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name` BIGINT COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_team_id` (`team_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Template folder';

CREATE TABLE IF NOT EXISTS `pdm_tool_interface_template` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Template ID',
    `folder_id` BIGINT COMMENT 'Folder ID',
    `name` VARCHAR(200) NOT NULL COMMENT 'Template name',
    `description` TEXT COMMENT 'Template description',
    `protocol_id` BIGINT COMMENT 'Protocol ID',
    `protocol_type` VARCHAR(50) NOT NULL COMMENT 'Protocol type',
    `method` VARCHAR(20) COMMENT 'HTTP method',
    `base_url` VARCHAR(500) COMMENT 'Base URL',
    `path` VARCHAR(1000) COMMENT 'Request path',
    `full_url` VARCHAR(1500) COMMENT 'Full URL',
    `auth_type` VARCHAR(30) COMMENT 'Auth type',
    `auth_config` JSON COMMENT 'Auth config',
    `content_type` VARCHAR(100) COMMENT 'Content-Type',
    `charset` VARCHAR(20) DEFAULT 'UTF-8' COMMENT 'Charset',
    `body_type` VARCHAR(20) COMMENT 'Body type',
    `body_content` LONGTEXT COMMENT 'Body content',
    `body_raw_type` VARCHAR(20) COMMENT 'Raw body type',
    `connect_timeout` INT DEFAULT 30000 COMMENT 'Connect timeout(ms)',
    `read_timeout` INT DEFAULT 30000 COMMENT 'Read timeout(ms)',
    `retry_count` INT DEFAULT 0 COMMENT 'Retry count',
    `retry_interval` INT DEFAULT 1000 COMMENT 'Retry interval(ms)',
    `version` VARCHAR(20) DEFAULT '1.0.0' COMMENT 'Version',
    `version_remark` VARCHAR(500) COMMENT 'Version remark',
    `is_latest` TINYINT DEFAULT 1 COMMENT 'Is latest version',
    `ref_template_id` BIGINT COMMENT 'Reference template ID',
    `tags` VARCHAR(500) COMMENT 'Tags',
    `team_id` BIGINT COMMENT 'Team ID',
    `visibility` TINYINT DEFAULT 1 COMMENT 'Visibility: 1-private, 2-team, 3-public',
    `pdm_system_type` VARCHAR(50) COMMENT 'PDM system type',
    `pdm_module` VARCHAR(100) COMMENT 'PDM module',
    `business_scene` VARCHAR(200) COMMENT 'Business scene',
    `file_count` INT DEFAULT 0 COMMENT 'File count',
    `has_request_file` INT DEFAULT 0 COMMENT 'Has request file',
    `has_response_file` INT DEFAULT 0 COMMENT 'Has response file',
    `status` TINYINT DEFAULT 1 COMMENT 'Status',
    `use_count` INT DEFAULT 0 COMMENT 'Use count',
    `last_use_time` DATETIME COMMENT 'Last use time',
    `ext_field1` VARCHAR(500) COMMENT 'Extension field 1',
    `ext_field2` VARCHAR(500) COMMENT 'Extension field 2',
    `ext_field3` VARCHAR(500) COMMENT 'Extension field 3',
    `ext_field4` TEXT COMMENT 'Extension field 4',
    `ext_field5` JSON COMMENT 'Extension field 5',
    `ext_num1` BIGINT COMMENT 'Extension numeric field 1',
    `ext_num2` BIGINT COMMENT 'Extension numeric field 2',
    `create_id` BIGINT COMMENT '创建人',
    `create_name` BIGINT COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name` BIGINT COMMENT '更新人名称',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Interface template';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_header` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `template_id` BIGINT NOT NULL COMMENT 'Template ID',
    `header_name` VARCHAR(200) NOT NULL COMMENT 'Header name',
    `header_value` VARCHAR(1000) COMMENT 'Header value',
    `description` VARCHAR(500) COMMENT 'Description',
    `is_enabled` TINYINT DEFAULT 1 COMMENT 'Is enabled',
    `is_required` TINYINT DEFAULT 0 COMMENT 'Is required',
    `is_variable` TINYINT DEFAULT 0 COMMENT 'Is variable',
    `variable_name` VARCHAR(100) COMMENT 'Variable name',
    `sort_order` INT DEFAULT 0 COMMENT 'Sort order',
    `create_id` BIGINT COMMENT '创建人',
    `create_name` BIGINT COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name` BIGINT COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Template headers';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_parameter` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `template_id` BIGINT NOT NULL COMMENT 'Template ID',
    `param_type` VARCHAR(20) NOT NULL COMMENT 'Parameter type',
    `param_name` VARCHAR(200) NOT NULL COMMENT 'Parameter name',
    `param_value` VARCHAR(1000) COMMENT 'Parameter value',
    `data_type` VARCHAR(30) DEFAULT 'STRING' COMMENT 'Data type',
    `description` VARCHAR(500) COMMENT 'Description',
    `example_value` VARCHAR(1000) COMMENT 'Example value',
    `is_required` TINYINT DEFAULT 0 COMMENT 'Is required',
    `is_enabled` TINYINT DEFAULT 1 COMMENT 'Is enabled',
    `is_variable` TINYINT DEFAULT 0 COMMENT 'Is variable',
    `variable_name` VARCHAR(100) COMMENT 'Variable name',
    `validation_rules` JSON COMMENT 'Validation rules',
    `sort_order` INT DEFAULT 0 COMMENT 'Sort order',
    `create_id` BIGINT COMMENT '创建人',
    `create_name` BIGINT COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name` BIGINT COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_param_type` (`param_type`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Template parameters';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_form_data` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `template_id` BIGINT NOT NULL COMMENT 'Template ID',
    `field_name` VARCHAR(200) NOT NULL COMMENT 'Field name',
    `field_type` VARCHAR(20) DEFAULT 'TEXT' COMMENT 'Field type',
    `field_value` TEXT COMMENT 'Field value',
    `file_path` VARCHAR(1000) COMMENT 'File path',
    `file_name` VARCHAR(255) COMMENT 'File name',
    `content_type` VARCHAR(100) COMMENT 'Content-Type',
    `description` VARCHAR(500) COMMENT 'Description',
    `is_required` TINYINT DEFAULT 0 COMMENT 'Is required',
    `is_enabled` TINYINT DEFAULT 1 COMMENT 'Is enabled',
    `is_variable` TINYINT DEFAULT 0 COMMENT 'Is variable',
    `variable_name` VARCHAR(100) COMMENT 'Variable name',
    `sort_order` INT DEFAULT 0 COMMENT 'Sort order',
    `create_id` BIGINT COMMENT '创建人',
    `create_name` BIGINT COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name` BIGINT COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Template form data';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_assertion` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `template_id` BIGINT NOT NULL COMMENT 'Template ID',
    `assert_name` VARCHAR(200) COMMENT 'Assertion name',
    `assert_type` VARCHAR(50) NOT NULL COMMENT 'Assertion type',
    `extract_path` VARCHAR(500) COMMENT 'Extract path',
    `expected_value` TEXT COMMENT 'Expected value',
    `operator` VARCHAR(30) COMMENT 'Operator',
    `data_type` VARCHAR(20) DEFAULT 'STRING' COMMENT 'Data type',
    `error_message` VARCHAR(500) COMMENT 'Error message',
    `is_enabled` TINYINT DEFAULT 1 COMMENT 'Is enabled',
    `assert_group` VARCHAR(100) DEFAULT 'default' COMMENT 'Assertion group',
    `logic_type` VARCHAR(10) DEFAULT 'AND' COMMENT 'Logic type',
    `sort_order` INT DEFAULT 0 COMMENT 'Sort order',
    `create_id` BIGINT COMMENT '创建人',
    `create_name` BIGINT COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name` BIGINT COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Template assertions';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_pre_processor` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `template_id` BIGINT NOT NULL COMMENT 'Template ID',
    `processor_name` VARCHAR(200) COMMENT 'Processor name',
    `processor_type` VARCHAR(50) NOT NULL COMMENT 'Processor type',
    `config` JSON COMMENT 'Processor config',
    `script_content` TEXT COMMENT 'Script content',
    `target_variable` VARCHAR(200) COMMENT 'Target variable',
    `variable_scope` VARCHAR(20) DEFAULT 'TEMPLATE' COMMENT 'Variable scope',
    `description` VARCHAR(500) COMMENT 'Description',
    `is_enabled` TINYINT DEFAULT 1 COMMENT 'Is enabled',
    `sort_order` INT DEFAULT 0 COMMENT 'Sort order',
    `create_id` BIGINT COMMENT '创建人',
    `create_name` BIGINT COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name` BIGINT COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Template pre-processors';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_post_processor` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `template_id` BIGINT NOT NULL COMMENT 'Template ID',
    `processor_name` VARCHAR(200) COMMENT 'Processor name',
    `processor_type` VARCHAR(50) NOT NULL COMMENT 'Processor type',
    `extract_type` VARCHAR(30) COMMENT 'Extract type',
    `extract_expression` VARCHAR(1000) COMMENT 'Extract expression',
    `extract_match_no` INT DEFAULT 0 COMMENT 'Extract match index',
    `target_variable` VARCHAR(200) COMMENT 'Target variable',
    `variable_scope` VARCHAR(20) DEFAULT 'TEMPLATE' COMMENT 'Variable scope',
    `default_value` VARCHAR(500) COMMENT 'Default value',
    `config` JSON COMMENT 'Processor config',
    `script_content` TEXT COMMENT 'Script content',
    `description` VARCHAR(500) COMMENT 'Description',
    `is_enabled` TINYINT DEFAULT 1 COMMENT 'Is enabled',
    `sort_order` INT DEFAULT 0 COMMENT 'Sort order',
    `create_id` BIGINT COMMENT '创建人',
    `create_name` BIGINT COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name` BIGINT COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Template post-processors';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_variable` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `template_id` BIGINT NOT NULL COMMENT 'Template ID',
    `variable_name` VARCHAR(200) NOT NULL COMMENT 'Variable name',
    `variable_type` VARCHAR(30) DEFAULT 'STRING' COMMENT 'Variable type',
    `default_value` TEXT COMMENT 'Default value',
    `current_value` TEXT COMMENT 'Current value',
    `description` VARCHAR(500) COMMENT 'Description',
    `example_value` VARCHAR(500) COMMENT 'Example value',
    `is_required` TINYINT DEFAULT 0 COMMENT 'Is required',
    `is_editable` TINYINT DEFAULT 1 COMMENT 'Is editable',
    `is_persistent` TINYINT DEFAULT 0 COMMENT 'Is persistent',
    `source_type` VARCHAR(30) DEFAULT 'MANUAL' COMMENT 'Source type',
    `source_config` JSON COMMENT 'Source config',
    `validation_rules` JSON COMMENT 'Validation rules',
    `sort_order` INT DEFAULT 0 COMMENT 'Sort order',
    `create_id` BIGINT COMMENT '创建人',
    `create_name` BIGINT COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name` BIGINT COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_is_deleted` (`is_deleted`),
    UNIQUE KEY `uk_template_variable` (`template_id`, `variable_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Template variables';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_environment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `template_id` BIGINT NOT NULL COMMENT 'Template ID',
    `env_name` VARCHAR(100) NOT NULL COMMENT 'Environment name',
    `env_code` VARCHAR(50) COMMENT 'Environment code',
    `base_url` VARCHAR(500) COMMENT 'Base URL',
    `headers` JSON COMMENT 'Headers',
    `variables` JSON COMMENT 'Variables',
    `auth_type` VARCHAR(30) COMMENT 'Auth type',
    `auth_config` JSON COMMENT 'Auth config',
    `proxy_enabled` TINYINT DEFAULT 0 COMMENT 'Proxy enabled',
    `proxy_host` VARCHAR(200) COMMENT 'Proxy host',
    `proxy_port` INT COMMENT 'Proxy port',
    `proxy_username` VARCHAR(100) COMMENT 'Proxy username',
    `proxy_password` VARCHAR(200) COMMENT 'Proxy password',
    `is_default` TINYINT DEFAULT 0 COMMENT 'Is default',
    `description` VARCHAR(500) COMMENT 'Description',
    `create_id` BIGINT COMMENT '创建人',
    `create_name` BIGINT COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name` BIGINT COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Template environments';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_history` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `template_id` BIGINT NOT NULL COMMENT 'Template ID',
    `version` VARCHAR(20) NOT NULL COMMENT 'Version',
    `version_type` VARCHAR(20) DEFAULT 'AUTO' COMMENT 'Version type',
    `change_summary` VARCHAR(1000) COMMENT 'Change summary',
    `change_details` TEXT COMMENT 'Change details',
    `template_snapshot` LONGTEXT COMMENT 'Template snapshot',
    `operation_type` VARCHAR(30) COMMENT 'Operation type',
    `can_rollback` TINYINT DEFAULT 1 COMMENT 'Can rollback',
    `rollback_to_time` DATETIME COMMENT 'Rollback time',
    `create_id` BIGINT COMMENT '创建人',
    `create_name` BIGINT COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name` BIGINT COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_version` (`version`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Template history';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_favorite` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `template_id` BIGINT NOT NULL COMMENT 'Template ID',
    `favorite_type` TINYINT DEFAULT 1 COMMENT 'Favorite type',
    `remark` VARCHAR(200) COMMENT 'Remark',
    `create_id` BIGINT COMMENT '创建人',
    `create_name` BIGINT COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name` BIGINT COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_create_id` (`create_id`),
    INDEX `idx_is_deleted` (`is_deleted`),
    UNIQUE KEY `uk_template_user` (`template_id`, `create_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Template favorites';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_usage_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `template_id` BIGINT NOT NULL COMMENT 'Template ID',
    `usage_type` VARCHAR(30) COMMENT 'Usage type',
    `task_id` BIGINT COMMENT 'Task ID',
    `execution_result` TINYINT COMMENT 'Execution result',
    `execution_duration` INT COMMENT 'Execution duration',
    `request_summary` VARCHAR(1000) COMMENT 'Request summary',
    `response_summary` VARCHAR(1000) COMMENT 'Response summary',
    `error_message` TEXT COMMENT 'Error message',
    `client_ip` VARCHAR(50) COMMENT 'Client IP',
    `user_agent` VARCHAR(500) COMMENT 'User-Agent',
    `create_id` BIGINT COMMENT '创建人',
    `create_name` BIGINT COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name` BIGINT COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_create_id` (`create_id`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Template usage logs';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_import_export` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `operation_type` VARCHAR(20) NOT NULL COMMENT 'Operation type',
    `template_ids` VARCHAR(1000) COMMENT 'Template IDs',
    `folder_id` BIGINT COMMENT 'Folder ID',
    `file_name` VARCHAR(255) COMMENT 'File name',
    `file_path` VARCHAR(1000) COMMENT 'File path',
    `file_size` BIGINT COMMENT 'File size',
    `file_format` VARCHAR(20) COMMENT 'File format',
    `status` TINYINT DEFAULT 0 COMMENT 'Status',
    `success_count` INT COMMENT 'Success count',
    `fail_count` INT COMMENT 'Fail count',
    `error_message` TEXT COMMENT 'Error message',
    `start_time` DATETIME COMMENT 'Start time',
    `end_time` DATETIME COMMENT 'End time',
    `create_id` BIGINT COMMENT '创建人',
    `create_name` BIGINT COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name` BIGINT COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_operation_type` (`operation_type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Template import/export logs';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_share` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    `template_id` BIGINT NOT NULL COMMENT 'Template ID',
    `share_type` VARCHAR(20) NOT NULL COMMENT 'Share type',
    `share_target_id` BIGINT NOT NULL COMMENT 'Share target ID',
    `share_target_name` VARCHAR(100) COMMENT 'Share target name',
    `permission` VARCHAR(50) COMMENT 'Permission',
    `can_share` TINYINT DEFAULT 0 COMMENT 'Can share',
    `expire_time` DATETIME COMMENT 'Expire time',
    `share_code` VARCHAR(100) COMMENT 'Share code',
    `share_link` VARCHAR(500) COMMENT 'Share link',
    `access_password` VARCHAR(100) COMMENT 'Access password',
    `create_id` BIGINT COMMENT '创建人',
    `create_name` BIGINT COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name` BIGINT COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Template shares';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_file` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'File ID',
    `template_id` BIGINT NOT NULL COMMENT 'Template ID',
    `file_name` VARCHAR(255) NOT NULL COMMENT 'Stored file name',
    `file_original_name` VARCHAR(255) COMMENT 'Original file name',
    `file_path` VARCHAR(500) NOT NULL COMMENT 'File path',
    `file_url` VARCHAR(500) COMMENT 'File URL',
    `file_size` BIGINT COMMENT 'File size',
    `file_type` VARCHAR(100) COMMENT 'MIME type',
    `file_extension` VARCHAR(50) COMMENT 'File extension',
    `file_category` VARCHAR(50) DEFAULT 'ATTACHMENT' COMMENT 'File category',
    `file_description` VARCHAR(500) COMMENT 'File description',
    `sort_order` INT DEFAULT 0 COMMENT 'Sort order',
    `create_id` BIGINT COMMENT '创建人',
    `create_name` BIGINT COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name` BIGINT COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_file_category` (`file_category`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Template files';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_job` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Job ID',
    `job_name` VARCHAR(100) NOT NULL COMMENT 'Job name',
    `cron_expression` VARCHAR(50) COMMENT 'Cron expression',
    `status` TINYINT DEFAULT 1 COMMENT 'Status',
    `description` VARCHAR(500) COMMENT 'Description',
    `xxl_job_id` INT COMMENT 'XXL job ID',
    `last_execute_time` DATETIME COMMENT 'Last execute time',
    `create_id` BIGINT COMMENT '创建人',
    `create_name` BIGINT COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name` BIGINT COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_status` (`status`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Template jobs';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_job_item` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Job item ID',
    `job_id` BIGINT NOT NULL COMMENT 'Job ID',
    `template_id` BIGINT NOT NULL COMMENT 'Template ID',
    `environment_id` BIGINT COMMENT 'Environment ID',
    `variables` TEXT COMMENT 'Variables JSON',
    `sort_order` INT DEFAULT 0 COMMENT 'Sort order',
    `status` TINYINT DEFAULT 1 COMMENT 'Status',
    `create_id` BIGINT COMMENT '创建人',
    `create_name` BIGINT COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name` BIGINT COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_job_id` (`job_id`),
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_environment_id` (`environment_id`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Template job items';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_job_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Job log ID',
    `job_id` BIGINT NOT NULL COMMENT 'Job ID',
    `template_id` BIGINT COMMENT 'Template ID',
    `xxl_job_log_id` BIGINT COMMENT 'XXL job log ID',
    `execute_result` TEXT COMMENT 'Execute result JSON',
    `success` TINYINT DEFAULT 0 COMMENT 'Success',
    `duration_ms` BIGINT COMMENT 'Duration(ms)',
    `error_msg` TEXT COMMENT 'Error message',
    `trace_id` VARCHAR(64) COMMENT 'Trace ID',
    `execute_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Execution time',
    `create_id` BIGINT COMMENT '创建人',
    `create_name` BIGINT COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name` BIGINT COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_job_id` (`job_id`),
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_trace_id` (`trace_id`),
    INDEX `idx_execute_at` (`execute_at`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Template job logs';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_execute_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Execute log ID',
    `template_id` BIGINT NOT NULL COMMENT 'Template ID',
    `template_name` VARCHAR(200) COMMENT 'Template name',
    `job_id` BIGINT COMMENT 'Job ID',
    `job_name` VARCHAR(100) COMMENT 'Job name',
    `execute_type` VARCHAR(20) NOT NULL COMMENT 'Execute type',
    `environment_id` BIGINT COMMENT 'Environment ID',
    `success` TINYINT DEFAULT 0 COMMENT 'Success',
    `status_code` INT COMMENT 'Status code',
    `duration_ms` BIGINT COMMENT 'Duration(ms)',
    `execute_result` TEXT COMMENT 'Execute result',
    `error_msg` TEXT COMMENT 'Error message',
    `trace_id` VARCHAR(64) COMMENT 'Trace ID',
    `execute_user_id` BIGINT COMMENT 'Execute user ID',
    `execute_user_name` VARCHAR(100) COMMENT 'Execute user name',
    `execute_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Execution time',
    `create_id` BIGINT COMMENT '创建人',
    `create_name` BIGINT COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name` BIGINT COMMENT '更新人名称',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Template execute logs';

CREATE TABLE IF NOT EXISTS `pdm_tool_template_job_batch` (
    `id` VARCHAR(64) PRIMARY KEY COMMENT 'Batch ID',
    `status` VARCHAR(20) NOT NULL COMMENT 'Batch status',
    `result` LONGTEXT COMMENT 'Batch result',
    `create_id` BIGINT COMMENT '创建人',
    `create_name` BIGINT COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name` BIGINT COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_status` (`status`),
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Template job batches';

CREATE TABLE IF NOT EXISTS `pdm_tool_mock_pdm_json_data` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Mock data ID',
    `data_json` JSON NOT NULL COMMENT 'Mock PDM JSON data',
    `create_id` BIGINT COMMENT '创建人',
    `create_name` BIGINT COMMENT '创建人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '更新人',
    `update_name` BIGINT COMMENT '更新人名称',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `deleted_by` BIGINT COMMENT '删除人',
    `deleted_time` DATETIME COMMENT '删除时间',
    INDEX `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Mock PDM JSON data';

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
