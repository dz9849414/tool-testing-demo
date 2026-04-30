-- ===========================================
-- 报告与分析管理模块 - 数据库表结构
-- 创建日期: 2026-04-17
-- ===========================================

-- ----------------------------
-- 1. 报告模板表 (report_template)
-- ----------------------------
CREATE TABLE `pdm_tool_report_template` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    `name` VARCHAR(200) NOT NULL COMMENT '模板名称',
    `description` TEXT COMMENT '模板描述',
    `template_type` VARCHAR(50) COMMENT '模板类型：STATISTICAL/ANALYTICAL/ARCHIVAL',
    `applicable_scene` VARCHAR(200) COMMENT '适用场景',
    `template_structure` JSON COMMENT '模板结构（章节、图表位置等）',
    `chapter_structure` JSON COMMENT '章节结构配置',
    `content` TEXT COMMENT '模板内容（用于前端展示）',
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
CREATE TABLE `pdm_tool_report` (
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
CREATE TABLE `pdm_tool_report_chart` (
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
INSERT INTO `pdm_tool_report_template` (
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
INSERT INTO `pdm_tool_report_template` (
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
INSERT INTO `pdm_tool_report_template` (
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
ALTER TABLE `pdm_tool_report` ADD INDEX `idx_template_status` (`template_id`, `status`);
ALTER TABLE `pdm_tool_report` ADD INDEX `idx_type_scheduled` (`report_type`, `is_scheduled`);

-- 为图表表添加复合索引
ALTER TABLE `pdm_tool_report_chart` ADD INDEX `idx_type_group` (`chart_type`, `chart_group`);
ALTER TABLE `pdm_tool_report_chart` ADD INDEX `idx_custom_public` (`is_custom`, `is_public`);

-- ===========================================
-- 表注释更新
-- ===========================================

ALTER TABLE `pdm_tool_report_template` COMMENT = '报告模板表 - 存储报告模板定义和配置';
ALTER TABLE `pdm_tool_report` COMMENT = '报告表 - 存储生成的报告实例和内容';
ALTER TABLE `pdm_tool_report_chart` COMMENT = '报告图表表 - 存储图表配置和数据';

-- ===========================================
-- 完成提示
-- ===========================================

SELECT '报告与分析管理模块表结构创建完成' AS '完成状态';

DELETE FROM  `tool_testing`.`pdm_tool_report_template` WHERE id = 6;
DELETE FROM  `tool_testing`.`pdm_tool_report_template` WHERE id = 12;
DELETE FROM  `tool_testing`.`pdm_tool_report_template` WHERE id = 13;
DELETE FROM  `tool_testing`.`pdm_tool_report_template` WHERE id = 16;
DELETE FROM  `tool_testing`.`pdm_tool_report_template` WHERE id = 17;

INSERT IGNORE INTO `tool_testing`.`pdm_tool_report_template` (`id`, `name`, `description`, `template_type`, `applicable_scene`, `template_structure`, `chapter_structure`, `content`, `style_config`, `is_system_template`, `is_public`, `related_business_type`, `usage_count`, `status`, `sort_order`, `preview_image`, `create_id`, `create_name`, `create_time`, `update_id`, `update_name`, `update_time`, `is_deleted`) VALUES (6, '响应时间标准模板', '协议类型占比分析', 'RESPONSE_TIME', NULL, '{\"sections\": [\"执行概览\", \"响应时间趋势\", \"响应时间分布\", \"性能对比\", \"优化建议\"]}', '[{\"type\": \"chart\", \"title\": \"响应时间分布\", \"chartType\": \"HISTOGRAM\", \"dataSource\": \"RESPONSE_TIME\"}, {\"type\": \"chart\", \"title\": \"性能对比\", \"chartType\": \"BAR\", \"dataSource\": \"RESPONSE_TIME\"}, {\"type\": \"text\", \"title\": \"优化建议\", \"dataSource\": \"OPTIMIZATION_SUGGESTIONS\"}]', NULL, '{\"font\": \"宋体\", \"theme\": \"default\", \"fontSize\": 12}', 1, 1, NULL, 0, 1, 0, NULL, NULL, NULL, '2026-04-23 00:35:07', NULL, NULL, '2026-04-30 14:16:18', 0);
INSERT IGNORE INTO `tool_testing`.`pdm_tool_report_template` (`id`, `name`, `description`, `template_type`, `applicable_scene`, `template_structure`, `chapter_structure`, `content`, `style_config`, `is_system_template`, `is_public`, `related_business_type`, `usage_count`, `status`, `sort_order`, `preview_image`, `create_id`, `create_name`, `create_time`, `update_id`, `update_name`, `update_time`, `is_deleted`) VALUES (12, '成功率标准模板', '模板描述', 'SUCCESS_RATE', NULL, '{\"sections\": [\"执行概览\", \"作业日志\", \"成功率趋势\", \"成功率分析\", \"问题总结\", \"改进建议\"]}', '[{\"type\": \"text\", \"title\": \"执行概览\", \"dataSource\": \"OVERVIEW\"}, {\"type\": \"chart\", \"title\": \"成功率趋势\", \"chartType\": \"LINE\", \"dataSource\": \"SUCCESS_RATE\"}, {\"type\": \"chart\", \"title\": \"成功率分析\", \"chartType\": \"PIE\", \"dataSource\": \"SUCCESS_RATE\"}, {\"type\": \"chart\", \"title\": \"问题总结\", \"chartType\": \"BAR\", \"dataSource\": \"FAILURE_REASONS\"}, {\"type\": \"text\", \"title\": \"改进建议\", \"dataSource\": \"OPTIMIZATION_SUGGESTIONS\"}]', NULL, '{}', 1, 1, NULL, 0, 1, 0, NULL, NULL, NULL, '2026-04-29 14:21:45', NULL, NULL, '2026-04-30 13:49:42', 0);
INSERT IGNORE INTO `tool_testing`.`pdm_tool_report_template` (`id`, `name`, `description`, `template_type`, `applicable_scene`, `template_structure`, `chapter_structure`, `content`, `style_config`, `is_system_template`, `is_public`, `related_business_type`, `usage_count`, `status`, `sort_order`, `preview_image`, `create_id`, `create_name`, `create_time`, `update_id`, `update_name`, `update_time`, `is_deleted`) VALUES (13, '故障原因标准模板', '模板描述', 'FAILURE_REASONS', NULL, '{\"sections\": [\"故障概览\", \"故障原因TOP5\", \"影响因素\", \"异常检测\", \"优化建议\"]}', '[{\"type\": \"text\", \"title\": \"故障概览\", \"dataSource\": \"OVERVIEW\"}, {\"type\": \"chart\", \"title\": \"故障原因TOP5\", \"chartType\": \"BAR\", \"dataSource\": \"FAILURE_REASONS\"}, {\"type\": \"text\", \"title\": \"影响因素\", \"dataSource\": \"INFLUENCING_FACTORS\"}, {\"type\": \"chart\", \"title\": \"异常检测\", \"chartType\": \"SCATTER\", \"dataSource\": \"ANOMALY_DETECTION\"}, {\"type\": \"text\", \"title\": \"优化建议\", \"dataSource\": \"OPTIMIZATION_SUGGESTIONS\"}]', NULL, '{}', 1, 1, NULL, 0, 1, 0, NULL, NULL, NULL, '2026-04-29 14:22:26', NULL, NULL, '2026-04-30 13:49:42', 0);
INSERT IGNORE INTO `tool_testing`.`pdm_tool_report_template` (`id`, `name`, `description`, `template_type`, `applicable_scene`, `template_structure`, `chapter_structure`, `content`, `style_config`, `is_system_template`, `is_public`, `related_business_type`, `usage_count`, `status`, `sort_order`, `preview_image`, `create_id`, `create_name`, `create_time`, `update_id`, `update_name`, `update_time`, `is_deleted`) VALUES (16, '协议分配标准模板', '模板描述', 'PROTOCOL_DISTRIBUTION', NULL, '{\"sections\": [\"协议分布\", \"使用趋势\", \"性能对比\", \"协议类型分布\", \"优化建议\"]}', '[{\"type\": \"chart\", \"title\": \"协议分布\", \"chartType\": \"PIE\", \"dataSource\": \"PROTOCOL_DISTRIBUTION\"}, {\"type\": \"chart\", \"title\": \"使用趋势\", \"chartType\": \"LINE\", \"dataSource\": \"WEEKLY_EXECUTION\"}, {\"type\": \"chart\", \"title\": \"性能对比\", \"chartType\": \"BAR\", \"dataSource\": \"RESPONSE_TIME\"}, {\"type\": \"chart\", \"title\": \"协议类型分布\", \"chartType\": \"BAR\", \"dataSource\": \"PROTOCOL_DISTRIBUTION\"}, {\"type\": \"text\", \"title\": \"优化建议\", \"dataSource\": \"OPTIMIZATION_SUGGESTIONS\"}]', NULL, '{}', 1, 1, NULL, 0, 1, 0, NULL, NULL, NULL, '2026-04-29 14:29:15', NULL, NULL, '2026-04-30 13:49:42', 0);
INSERT IGNORE INTO `tool_testing`.`pdm_tool_report_template` (`id`, `name`, `description`, `template_type`, `applicable_scene`, `template_structure`, `chapter_structure`, `content`, `style_config`, `is_system_template`, `is_public`, `related_business_type`, `usage_count`, `status`, `sort_order`, `preview_image`, `create_id`, `create_name`, `create_time`, `update_id`, `update_name`, `update_time`, `is_deleted`) VALUES (17, '周执行计划标准模板', '模板描述', 'WEEKLY_EXECUTION', NULL, '{\"sections\": [\"执行统计\", \"成功率分析\", \"性能趋势\", \"问题总结\", \"下周计划\"]}', '[{\"type\": \"chart\", \"title\": \"执行统计\", \"chartType\": \"BAR\", \"dataSource\": \"WEEKLY_EXECUTION\"}, {\"type\": \"chart\", \"title\": \"成功率分析\", \"chartType\": \"LINE\", \"dataSource\": \"SUCCESS_RATE\"}, {\"type\": \"chart\", \"title\": \"性能趋势\", \"chartType\": \"LINE\", \"dataSource\": \"RESPONSE_TIME\"}, {\"type\": \"chart\", \"title\": \"问题总结\", \"chartType\": \"BAR\", \"dataSource\": \"FAILURE_REASONS\"}, {\"type\": \"text\", \"title\": \"下周计划\", \"dataSource\": \"OPTIMIZATION_SUGGESTIONS\"}]', NULL, '{}', 1, 1, NULL, 0, 1, 0, NULL, NULL, NULL, '2026-04-29 14:32:43', NULL, NULL, '2026-04-30 13:49:37', 0);