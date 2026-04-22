-- ===========================================
-- sys_user表字段添加SQL
-- 用于在现有sys_user表上添加缺失的字段
-- ===========================================

-- 添加创建人ID字段
ALTER TABLE `sys_user` 
ADD COLUMN `create_id` BIGINT DEFAULT NULL COMMENT '创建人ID' 
AFTER `status`;

-- 修改创建时间字段约束
ALTER TABLE `sys_user` 
MODIFY COLUMN `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';

-- 添加更新人ID字段
ALTER TABLE `sys_user` 
ADD COLUMN `update_id` BIGINT DEFAULT NULL COMMENT '更新人ID' 
AFTER `create_time`;

-- 修改更新时间字段约束
ALTER TABLE `sys_user` 
MODIFY COLUMN `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 添加逻辑删除标记字段
ALTER TABLE `sys_user` 
ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除' 
AFTER `update_time`;

-- 添加删除人ID字段
ALTER TABLE `sys_user` 
ADD COLUMN `deleted_by` BIGINT DEFAULT NULL COMMENT '删除人ID' 
AFTER `is_deleted`;

-- 添加删除时间字段
ALTER TABLE `sys_user` 
ADD COLUMN `deleted_time` DATETIME DEFAULT NULL COMMENT '删除时间' 
AFTER `deleted_by`;

-- 为逻辑删除字段添加索引
ALTER TABLE `sys_user` 
ADD INDEX `idx_is_deleted` (`is_deleted`);

-- 验证字段添加结果
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    IS_NULLABLE, 
    COLUMN_DEFAULT, 
    COLUMN_COMMENT 
FROM 
    INFORMATION_SCHEMA.COLUMNS 
WHERE 
    TABLE_NAME = 'sys_user' 
    AND TABLE_SCHEMA = DATABASE()
ORDER BY 
    ORDINAL_POSITION;