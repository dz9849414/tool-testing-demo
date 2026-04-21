-- 模板定时任务配置表
CREATE TABLE IF NOT EXISTS template_job (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    job_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    cron_expression VARCHAR(50) COMMENT 'Cron表达式',
    status TINYINT DEFAULT 1 COMMENT '状态：0-停用 1-启用',
    description VARCHAR(500) COMMENT '任务描述',
    xxl_job_id INT COMMENT 'XXL-JOB任务ID',
    last_execute_time DATETIME COMMENT '上次执行时间',
    create_id BIGINT COMMENT '创建人ID',
    create_name VARCHAR(50) COMMENT '创建人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-否 1-是',
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板定时任务配置表';

-- 模板定时任务子项表（一个任务关联多个模板）
CREATE TABLE IF NOT EXISTS template_job_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    job_id BIGINT NOT NULL COMMENT '任务ID',
    template_id BIGINT NOT NULL COMMENT '关联模板ID',
    environment_id BIGINT COMMENT '关联环境ID',
    variables TEXT COMMENT '执行变量JSON',
    sort_order INT DEFAULT 0 COMMENT '执行顺序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-停用 1-启用',
    INDEX idx_job_id (job_id),
    INDEX idx_template_id (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板定时任务子项表';

-- 模板定时任务执行日志表
CREATE TABLE IF NOT EXISTS template_job_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    job_id BIGINT NOT NULL COMMENT '任务ID',
    template_id BIGINT COMMENT '首个模板ID',
    xxl_job_log_id BIGINT COMMENT 'XXL-JOB日志ID',
    execute_result TEXT COMMENT '执行结果JSON',
    success TINYINT DEFAULT 0 COMMENT '是否成功：0-否 1-是',
    duration_ms BIGINT COMMENT '执行耗时ms',
    error_msg TEXT COMMENT '错误信息',
    trace_id VARCHAR(64) COMMENT '链路追踪ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
    INDEX idx_job_id (job_id),
    INDEX idx_template_id (template_id),
    INDEX idx_trace_id (trace_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板定时任务执行日志表';

-- 模板执行统一日志表（手动执行 + 定时任务执行）
CREATE TABLE IF NOT EXISTS template_execute_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    template_id BIGINT NOT NULL COMMENT '模板ID',
    template_name VARCHAR(200) COMMENT '模板名称',
    job_id BIGINT COMMENT '关联任务ID（定时任务时填充）',
    job_name VARCHAR(100) COMMENT '任务名称（定时任务时填充）',
    execute_type VARCHAR(20) NOT NULL COMMENT '执行方式：MANUAL-手动执行 JOB-定时任务',
    environment_id BIGINT COMMENT '环境ID',
    success TINYINT DEFAULT 0 COMMENT '是否成功：0-否 1-是',
    status_code INT COMMENT 'HTTP 状态码',
    duration_ms BIGINT COMMENT '执行耗时（ms）',
    execute_result TEXT COMMENT '执行结果（JSON）',
    error_msg TEXT COMMENT '错误信息',
    trace_id VARCHAR(64) COMMENT '链路追踪ID',
    create_id BIGINT COMMENT '执行人ID（手动执行时填充）',
    create_name VARCHAR(50) COMMENT '执行人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
    INDEX idx_template_id (template_id),
    INDEX idx_job_id (job_id),
    INDEX idx_execute_type (execute_type),
    INDEX idx_success (success),
    INDEX idx_trace_id (trace_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板执行统一日志表';

CREATE TABLE IF NOT EXISTS template_job_batch (
                                                  id VARCHAR(64) PRIMARY KEY,
    status VARCHAR(20) NOT NULL,
    result LONGTEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;




CREATE TABLE mock_pdm_json_data (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                                    data_json JSON NOT NULL COMMENT 'PDM模拟业务数据JSON'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PDM模拟数据表';


ALTER TABLE template_job_log
    ADD COLUMN trace_id VARCHAR(64) NULL COMMENT '链路追踪ID' AFTER error_msg;

ALTER TABLE template_job_log
    ADD INDEX idx_trace_id (trace_id);

ALTER TABLE template_execute_log
    ADD COLUMN trace_id VARCHAR(64) NULL COMMENT '链路追踪ID' AFTER error_msg;

ALTER TABLE template_execute_log
    ADD INDEX idx_trace_id (trace_id);
