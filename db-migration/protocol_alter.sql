SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'pdm_tool_protocol_config'
      AND COLUMN_NAME = 'tcp_udp'
);

SET @ddl = IF(
    @column_exists = 0,
    'ALTER TABLE `pdm_tool_protocol_config` ADD COLUMN `tcp_udp` json NULL COMMENT ''TCP/UDP配置数据''',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
