ALTER TABLE `tool_testing`.`pdm_tool_sys_operation_log`
MODIFY COLUMN `ip_address` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'IP地址' AFTER `request_params`;

ALTER TABLE `tool_testing`.`pdm_tool_sys_operation_log`
    ADD COLUMN `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '追踪ID，用于链路追踪和日志还原追踪' AFTER `id`;


ALTER TABLE `tool_testing`.`pdm_tool_sys_operation_log`
    ADD COLUMN `method_json` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '方法调用链JSON' AFTER `method`;