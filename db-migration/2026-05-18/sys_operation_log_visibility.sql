SET NAMES utf8mb4;

ALTER TABLE `pdm_tool_sys_operation_log`
    ADD COLUMN `show_in_system_log` TINYINT DEFAULT 1 COMMENT '是否在系统日志页面展示：0-否，1-是' AFTER `create_time`,
    ADD COLUMN `generation_log_id` BIGINT COMMENT '自动生成日志ID' AFTER `show_in_system_log`,
    ADD INDEX `idx_show_in_system_log` (`show_in_system_log`),
    ADD INDEX `idx_generation_log_id` (`generation_log_id`);
