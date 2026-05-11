CREATE TABLE IF NOT EXISTS `pdm_tool_template_job_automation_config` (
    `job_id` BIGINT NOT NULL PRIMARY KEY COMMENT '任务ID',
    `concurrent_config` TEXT COMMENT '并发接口配置JSON',
    `script_config` TEXT COMMENT '自动化脚本配置JSON',
    `log_config` TEXT COMMENT '自动化日志记录配置JSON',
    `report_config` TEXT COMMENT '接口传输报告配置JSON',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板任务自动化配置表';
