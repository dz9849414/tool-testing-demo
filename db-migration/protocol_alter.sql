SET NAMES utf8mb4;
ALTER TABLE `tool_testing`.`pdm_tool_protocol_type`
    ADD COLUMN `template_id` bigint NULL DEFAULT NULL COMMENT '关联模板ID',
ADD COLUMN `template_name` varchar(50) NULL DEFAULT NULL COMMENT '关联模板名称';

ALTER TABLE `tool_testing`.`pdm_tool_protocol_config`
    ADD COLUMN `tcp_udp` json NULL COMMENT 'TCP/UDP配置数据';