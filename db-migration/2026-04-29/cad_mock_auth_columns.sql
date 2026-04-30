ALTER TABLE `pdm_tool_cad_mock_interface`
    ADD COLUMN `auth_type` VARCHAR(32) DEFAULT 'NONE' COMMENT '认证方式：NONE/BASIC/BEARER/API_KEY/CUSTOM_HEADERS' AFTER `success_value`,
    ADD COLUMN `auth_config` LONGTEXT COMMENT '认证配置JSON' AFTER `auth_type`;
