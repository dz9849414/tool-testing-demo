-- ===========================================
-- 报告与分析管理模块 - 数据库表结构
-- 创建日期: 2026-04-17
-- ===========================================

-- ----------------------------
-- 1. 报告模板表 (report_template)
-- ----------------------------
CREATE TABLE `report_template` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    `name` VARCHAR(200) NOT NULL COMMENT '模板名称',
    `description` TEXT COMMENT '模板描述',
    `template_type` VARCHAR(50) COMMENT '模板类型：STATISTICAL/ANALYTICAL/ARCHIVAL',
    `applicable_scene` VARCHAR(200) COMMENT '适用场景',
    `template_structure` JSON COMMENT '模板结构（章节、图表位置等）',
    `style_config` JSON COMMENT '样式配置',
    `is_system_template` TINYINT DEFAULT 0 COMMENT '是否系统预设模板',
    `is_public` TINYINT DEFAULT 0 COMMENT '是否公开模板',
    `related_business_type` VARCHAR(100) COMMENT '关联业务对象类型',
    `usage_count` INT DEFAULT 0 COMMENT '使用次数',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    `preview_image` VARCHAR(500) COMMENT '预览图路径',
    
    -- 审计字段
    `create_id` BIGINT COMMENT '创建人ID',
    `create_name` VARCHAR(50) COMMENT '创建人姓名',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '修改人ID',
    `update_name` VARCHAR(50) COMMENT '修改人姓名',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除',
    
    INDEX `idx_template_type` (`template_type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_is_public` (`is_public`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报告模板表';

-- ----------------------------
-- 2. 报告表 (report)
-- ----------------------------
CREATE TABLE `report` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报告ID',
    `name` VARCHAR(200) NOT NULL COMMENT '报告名称',
    `description` TEXT COMMENT '报告描述',
    `report_type` VARCHAR(50) COMMENT '报告类型',
    `template_id` BIGINT COMMENT '关联的模板ID',
    `content` JSON COMMENT '报告内容（章节、图表数据等）',
    `style_config` JSON COMMENT '报告样式配置',
    `generate_type` VARCHAR(20) DEFAULT 'MANUAL' COMMENT '生成方式：AUTO/MANUAL',
    `generate_frequency` VARCHAR(20) COMMENT '生成频率：ONCE/DAILY/WEEKLY/MONTHLY',
    `next_generate_time` DATETIME COMMENT '下次生成时间',
    `data_source_ids` JSON COMMENT '关联的数据源ID列表',
    `chart_ids` JSON COMMENT '关联的图表ID列表',
    `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT '报告状态：DRAFT/PUBLISHED/ARCHIVED',
    `is_scheduled` TINYINT DEFAULT 0 COMMENT '是否定时生成',
    `is_notified` TINYINT DEFAULT 0 COMMENT '是否已推送通知',
    `export_count` INT DEFAULT 0 COMMENT '导出次数',
    `last_export_time` DATETIME COMMENT '最后导出时间',
    
    -- 审计字段
    `create_id` BIGINT COMMENT '创建人ID',
    `create_name` VARCHAR(50) COMMENT '创建人姓名',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '修改人ID',
    `update_name` VARCHAR(50) COMMENT '修改人姓名',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除',
    
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_report_type` (`report_type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_is_scheduled` (`is_scheduled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报告表';

-- ----------------------------
-- 3. 报告图表表 (report_chart)
-- ----------------------------
CREATE TABLE `report_chart` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '图表ID',
    `name` VARCHAR(200) NOT NULL COMMENT '图表名称',
    `description` TEXT COMMENT '图表描述',
    `chart_type` VARCHAR(50) COMMENT '图表类型：BAR/LINE/PIE/SCATTER/RADAR',
    `data_source_type` VARCHAR(50) COMMENT '数据源类型：TEMPLATE/TASK/PROTOCOL',
    `data_source_ids` JSON COMMENT '数据源ID列表',
    `chart_config` JSON COMMENT '图表配置（坐标轴、图例、颜色等）',
    `style_config` JSON COMMENT '样式配置',
    `chart_data` JSON COMMENT '图表数据',
    `is_custom` TINYINT DEFAULT 0 COMMENT '是否为自定义图表',
    `is_public` TINYINT DEFAULT 0 COMMENT '是否公开',
    `chart_group` VARCHAR(100) COMMENT '图表分组',
    `usage_count` INT DEFAULT 0 COMMENT '使用次数',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    
    -- 审计字段
    `create_id` BIGINT COMMENT '创建人ID',
    `create_name` VARCHAR(50) COMMENT '创建人姓名',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_id` BIGINT COMMENT '修改人ID',
    `update_name` VARCHAR(50) COMMENT '修改人姓名',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除',
    
    INDEX `idx_chart_type` (`chart_type`),
    INDEX `idx_data_source_type` (`data_source_type`),
    INDEX `idx_is_custom` (`is_custom`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报告图表表';

-- ===========================================
-- 初始化数据 - 系统预设报告模板
-- ===========================================

-- 任务执行周报模板
INSERT INTO `report_template` (
    `name`, `description`, `template_type`, `applicable_scene`, 
    `template_structure`, `style_config`, `is_system_template`, `is_public`,
    `related_business_type`, `sort_order`, `create_name`
) VALUES (
    '任务执行周报', 
    '用于统计和分析每周任务执行情况的报告模板',
    'STATISTICAL',
    '每周任务执行总结',
    '{"sections": ["任务概览", "执行统计", "成功率分析", "性能趋势", "问题总结"]}',
    '{"font": "宋体", "fontSize": 12, "theme": "default"}',
    1, 1,
    'TASK', 10, '系统'
);

-- 接口测试报告模板
INSERT INTO `report_template` (
    `name`, `description`, `template_type`, `applicable_scene`, 
    `template_structure`, `style_config`, `is_system_template`, `is_public`,
    `related_business_type`, `sort_order`, `create_name`
) VALUES (
    '接口测试报告', 
    '用于记录和分析接口测试结果的报告模板',
    'ANALYTICAL',
    '接口测试结果分析',
    '{"sections": ["测试概览", "接口详情", "响应时间分析", "错误统计", "测试结论"]}',
    '{"font": "微软雅黑", "fontSize": 11, "theme": "professional"}',
    1, 1,
    'PROTOCOL', 20, '系统'
);

-- 协议类型占比分析模板
INSERT INTO `report_template` (
    `name`, `description`, `template_type`, `applicable_scene`, 
    `template_structure`, `style_config`, `is_system_template`, `is_public`,
    `related_business_type`, `sort_order`, `create_name`
) VALUES (
    '协议类型占比分析', 
    '用于分析系统中各类协议使用占比的报告模板',
    'ANALYTICAL',
    '协议使用情况分析',
    '{"sections": ["协议分布", "使用趋势", "性能对比", "优化建议"]}',
    '{"font": "Arial", "fontSize": 10, "theme": "modern"}',
    1, 1,
    'PROTOCOL', 30, '系统'
);

-- ===========================================
-- 索引优化
-- ===========================================

-- 为报告表添加复合索引
ALTER TABLE `report` ADD INDEX `idx_template_status` (`template_id`, `status`);
ALTER TABLE `report` ADD INDEX `idx_type_scheduled` (`report_type`, `is_scheduled`);

-- 为图表表添加复合索引
ALTER TABLE `report_chart` ADD INDEX `idx_type_group` (`chart_type`, `chart_group`);
ALTER TABLE `report_chart` ADD INDEX `idx_custom_public` (`is_custom`, `is_public`);

-- ===========================================
-- 表注释更新
-- ===========================================

ALTER TABLE `report_template` COMMENT = '报告模板表 - 存储报告模板定义和配置';
ALTER TABLE `report` COMMENT = '报告表 - 存储生成的报告实例和内容';
ALTER TABLE `report_chart` COMMENT = '报告图表表 - 存储图表配置和数据';

-- ===========================================
-- 完成提示
-- ===========================================

SELECT '报告与分析管理模块表结构创建完成' AS '完成状态';