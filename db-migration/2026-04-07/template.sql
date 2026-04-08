-- ============================================
-- 船舶 PDM 接口测试工具 - 模板管理模块数据库设计
-- ============================================

-- ----------------------------
-- 1. 模板分类/文件夹表
-- ----------------------------
CREATE TABLE template_folder (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    parent_id       BIGINT DEFAULT 0 COMMENT '父分类ID，0表示根分类',
    name            VARCHAR(100) NOT NULL COMMENT '分类名称',
    description     VARCHAR(500) COMMENT '分类描述',
    sort_order      INT DEFAULT 0 COMMENT '排序序号',
    icon            VARCHAR(50) COMMENT '图标',
    color           VARCHAR(20) COMMENT '颜色标识',
    
    -- 权限相关
    owner_id        BIGINT NOT NULL COMMENT '创建人ID',
    owner_name      VARCHAR(50) COMMENT '创建人姓名',
    team_id         BIGINT COMMENT '所属团队ID',
    visibility      TINYINT DEFAULT 1 COMMENT '可见性：1-私有 2-团队 3-公开',
    
    -- 状态
    status          TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    
    -- 时间戳
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    delete_time     DATETIME COMMENT '删除时间（软删除）',
    
    INDEX idx_parent_id (parent_id),
    INDEX idx_owner_id (owner_id),
    INDEX idx_team_id (team_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板分类/文件夹表';

-- ----------------------------
-- 2. 接口模板主表
-- ----------------------------
CREATE TABLE interface_template (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    folder_id           BIGINT COMMENT '所属分类ID',
    
    -- 基本信息
    name                VARCHAR(200) NOT NULL COMMENT '模板名称',
    description         TEXT COMMENT '模板描述/备注',
    
    -- 协议配置（对应船舶PDM系统的协议类型）
    protocol_type       VARCHAR(50) NOT NULL COMMENT '协议类型：HTTP/HTTPS/WEBSOCKET/SOAP/REST/MQTT/TCP/UDP',
    method              VARCHAR(20) COMMENT '请求方法：GET/POST/PUT/DELETE/PATCH/HEAD/OPTIONS（HTTP协议适用）',
    
    -- URL配置
    base_url            VARCHAR(500) COMMENT '基础URL',
    path                VARCHAR(1000) COMMENT '请求路径',
    full_url            VARCHAR(1500) COMMENT '完整URL（baseUrl + path）',
    
    -- 认证配置
    auth_type           VARCHAR(30) COMMENT '认证类型：NONE/BASIC/DIGEST/OAUTH1/OAUTH2/BEARER/APIKEY/JWT',
    auth_config         JSON COMMENT '认证配置详情（JSON格式存储）',
    
    -- 内容配置
    content_type        VARCHAR(100) COMMENT 'Content-Type',
    charset             VARCHAR(20) DEFAULT 'UTF-8' COMMENT '字符编码',
    
    -- 请求体配置
    body_type           VARCHAR(20) COMMENT '请求体类型：NONE/FORM_DATA/X_WWW_FORM_URLENCODED/RAW/BINARY/GRAPHQL',
    body_content        LONGTEXT COMMENT '请求体内容',
    body_raw_type       VARCHAR(20) COMMENT 'RAW类型：JSON/XML/HTML/TEXT/JavaScript',
    
    -- 超时配置
    connect_timeout     INT DEFAULT 30000 COMMENT '连接超时时间（毫秒）',
    read_timeout        INT DEFAULT 30000 COMMENT '读取超时时间（毫秒）',
    
    -- 重试配置
    retry_count         INT DEFAULT 0 COMMENT '重试次数',
    retry_interval      INT DEFAULT 1000 COMMENT '重试间隔（毫秒）',
    
    -- 版本控制
    version             VARCHAR(20) DEFAULT '1.0.0' COMMENT '版本号',
    version_remark      VARCHAR(500) COMMENT '版本说明',
    is_latest           TINYINT DEFAULT 1 COMMENT '是否为最新版本：0-否 1-是',
    ref_template_id     BIGINT COMMENT '关联的模板ID（版本链）',
    
    -- 权限相关
    owner_id            BIGINT NOT NULL COMMENT '创建人ID',
    owner_name          VARCHAR(50) COMMENT '创建人姓名',
    team_id             BIGINT COMMENT '所属团队ID',
    visibility          TINYINT DEFAULT 1 COMMENT '可见性：1-私有 2-团队 3-公开',
    
    -- 标签（用于快速检索）
    tags                VARCHAR(500) COMMENT '标签，逗号分隔',
    
    -- PDM系统相关（船舶PDM特有）
    pdm_system_type     VARCHAR(50) COMMENT 'PDM系统类型：CAD/ERP/PLM/CAM/CAE',
    pdm_module          VARCHAR(100) COMMENT 'PDM模块：物料管理/BOM管理/变更管理/图纸管理',
    business_scene      VARCHAR(200) COMMENT '业务场景描述',
    
    -- 状态
    status              TINYINT DEFAULT 1 COMMENT '状态：0-草稿 1-已发布 2-已归档 3-已禁用',
    
    -- 统计信息
    use_count           INT DEFAULT 0 COMMENT '使用次数',
    last_use_time       DATETIME COMMENT '最后使用时间',
    
    -- 时间戳
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    delete_time         DATETIME COMMENT '删除时间（软删除）',
    
    INDEX idx_folder_id (folder_id),
    INDEX idx_protocol_type (protocol_type),
    INDEX idx_method (method),
    INDEX idx_owner_id (owner_id),
    INDEX idx_team_id (team_id),
    INDEX idx_visibility (visibility),
    INDEX idx_status (status),
    INDEX idx_ref_template_id (ref_template_id),
    INDEX idx_is_latest (is_latest),
    INDEX idx_pdm_system_type (pdm_system_type),
    FULLTEXT INDEX idx_name_tags (name, tags)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接口模板主表';

-- ----------------------------
-- 3. 模板请求头表
-- ----------------------------
CREATE TABLE template_header (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    template_id         BIGINT NOT NULL COMMENT '模板ID',
    
    -- Header配置
    header_name         VARCHAR(200) NOT NULL COMMENT 'Header名称',
    header_value        VARCHAR(1000) COMMENT 'Header值',
    description         VARCHAR(500) COMMENT '描述说明',
    is_enabled          TINYINT DEFAULT 1 COMMENT '是否启用：0-否 1-是',
    is_required         TINYINT DEFAULT 0 COMMENT '是否必填：0-否 1-是',
    
    -- 动态变量支持
    is_variable         TINYINT DEFAULT 0 COMMENT '是否为变量：0-否 1-是',
    variable_name       VARCHAR(100) COMMENT '变量名',
    
    sort_order          INT DEFAULT 0 COMMENT '排序序号',
    
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_template_id (template_id),
    INDEX idx_header_name (header_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板请求头表';

-- ----------------------------
-- 4. 模板请求参数表（Query/Path参数）
-- ----------------------------
CREATE TABLE template_parameter (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    template_id         BIGINT NOT NULL COMMENT '模板ID',
    
    -- 参数配置
    param_type          VARCHAR(20) NOT NULL COMMENT '参数类型：QUERY/PATH',
    param_name          VARCHAR(200) NOT NULL COMMENT '参数名称',
    param_value         VARCHAR(1000) COMMENT '参数值',
    data_type           VARCHAR(30) DEFAULT 'STRING' COMMENT '数据类型：STRING/INTEGER/LONG/FLOAT/DOUBLE/BOOLEAN/DATE/DATETIME/ARRAY/OBJECT/FILE',
    
    -- 描述
    description         VARCHAR(500) COMMENT '参数描述',
    example_value       VARCHAR(1000) COMMENT '示例值',
    
    -- 约束
    is_required         TINYINT DEFAULT 0 COMMENT '是否必填：0-否 1-是',
    is_enabled          TINYINT DEFAULT 1 COMMENT '是否启用：0-否 1-是',
    
    -- 动态变量
    is_variable         TINYINT DEFAULT 0 COMMENT '是否为变量：0-否 1-是',
    variable_name       VARCHAR(100) COMMENT '变量名',
    
    -- 验证规则（JSON格式）
    validation_rules    JSON COMMENT '验证规则：{minLength, maxLength, pattern, min, max, enum}',
    
    sort_order          INT DEFAULT 0 COMMENT '排序序号',
    
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_template_id (template_id),
    INDEX idx_param_type (param_type),
    INDEX idx_param_name (param_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板请求参数表';

-- ----------------------------
-- 5. 模板FormData参数表（用于multipart/form-data请求）
-- ----------------------------
CREATE TABLE template_form_data (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    template_id         BIGINT NOT NULL COMMENT '模板ID',
    
    -- 参数配置
    field_name          VARCHAR(200) NOT NULL COMMENT '字段名称',
    field_type          VARCHAR(20) DEFAULT 'TEXT' COMMENT '字段类型：TEXT/FILE',
    field_value         TEXT COMMENT '字段值（TEXT类型）',
    file_path           VARCHAR(1000) COMMENT '文件路径（FILE类型）',
    file_name           VARCHAR(255) COMMENT '文件名',
    content_type        VARCHAR(100) COMMENT '文件Content-Type',
    
    description         VARCHAR(500) COMMENT '描述',
    is_required         TINYINT DEFAULT 0 COMMENT '是否必填',
    is_enabled          TINYINT DEFAULT 1 COMMENT '是否启用',
    
    -- 动态变量
    is_variable         TINYINT DEFAULT 0 COMMENT '是否为变量',
    variable_name       VARCHAR(100) COMMENT '变量名',
    
    sort_order          INT DEFAULT 0 COMMENT '排序序号',
    
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_template_id (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板FormData参数表';

-- ----------------------------
-- 6. 模板响应验证规则表
-- ----------------------------
CREATE TABLE template_assertion (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    template_id         BIGINT NOT NULL COMMENT '模板ID',
    
    -- 断言配置
    assert_name         VARCHAR(200) COMMENT '断言名称',
    assert_type         VARCHAR(50) NOT NULL COMMENT '断言类型：STATUS_CODE/STATUS_MESSAGE/RESPONSE_HEADER/RESPONSE_BODY/RESPONSE_TIME/RESPONSE_SIZE/JSON_PATH/XML_PATH/REGEX/CONTAINS/EQUALS/NOT_EQUALS/GREATER_THAN/LESS_THAN/IS_EMPTY/IS_NOT_EMPTY/IS_NULL/IS_NOT_NULL',
    
    -- 断言参数
    extract_path        VARCHAR(500) COMMENT '提取路径（JSONPath/XPath等）',
    expected_value      TEXT COMMENT '期望值',
    operator            VARCHAR(30) COMMENT '比较运算符：EQUALS/NOT_EQUALS/CONTAINS/STARTS_WITH/ENDS_WITH/MATCHES/GREATER_THAN/LESS_THAN/GREATER_EQUALS/LESS_EQUALS',
    
    -- 数据类型转换
    data_type           VARCHAR(20) DEFAULT 'STRING' COMMENT '数据类型：STRING/INTEGER/FLOAT/BOOLEAN/DATETIME',
    
    -- 错误处理
    error_message       VARCHAR(500) COMMENT '断言失败时的自定义错误信息',
    is_enabled          TINYINT DEFAULT 1 COMMENT '是否启用',
    
    -- 断言分组（支持分组验证）
    assert_group        VARCHAR(100) DEFAULT 'default' COMMENT '断言分组',
    logic_type          VARCHAR(10) DEFAULT 'AND' COMMENT '逻辑关系：AND/OR',
    
    sort_order          INT DEFAULT 0 COMMENT '排序序号',
    
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_template_id (template_id),
    INDEX idx_assert_type (assert_type),
    INDEX idx_assert_group (assert_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板响应验证规则表';

-- ----------------------------
-- 7. 模板前置处理器表（请求前处理）
-- ----------------------------
CREATE TABLE template_pre_processor (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    template_id         BIGINT NOT NULL COMMENT '模板ID',
    
    processor_name      VARCHAR(200) COMMENT '处理器名称',
    processor_type      VARCHAR(50) NOT NULL COMMENT '处理器类型：SET_VARIABLE/REMOVE_VARIABLE/TIMESTAMP/RANDOM_STRING/RANDOM_NUMBER/RANDOM_UUID/BASE64_ENCODE/BASE64_DECODE/URL_ENCODE/URL_DECODE/MD5/SHA1/SHA256/HMAC/JS_SCRIPT/GROOVY_SCRIPT/DATABASE_QUERY/CACHE_GET',
    
    -- 处理器配置
    config              JSON COMMENT '处理器配置参数',
    script_content      TEXT COMMENT '脚本内容（JS/GROOVY等）',
    
    -- 变量设置
    target_variable     VARCHAR(200) COMMENT '目标变量名',
    variable_scope      VARCHAR(20) DEFAULT 'TEMPLATE' COMMENT '变量作用域：TEMPLATE/STEP/GLOBAL',
    
    description         VARCHAR(500) COMMENT '描述',
    is_enabled          TINYINT DEFAULT 1 COMMENT '是否启用',
    sort_order          INT DEFAULT 0 COMMENT '执行顺序',
    
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_template_id (template_id),
    INDEX idx_processor_type (processor_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板前置处理器表';

-- ----------------------------
-- 8. 模板后置处理器表（响应后处理）
-- ----------------------------
CREATE TABLE template_post_processor (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    template_id         BIGINT NOT NULL COMMENT '模板ID',
    
    processor_name      VARCHAR(200) COMMENT '处理器名称',
    processor_type      VARCHAR(50) NOT NULL COMMENT '处理器类型：JSON_EXTRACT/XML_EXTRACT/REGEX_EXTRACT/HEADER_EXTRACT/COOKIE_EXTRACT/JS_SCRIPT/GROOVY_SCRIPT/DATABASE_INSERT/CACHE_SET/RESPONSE_HANDLE',
    
    -- 提取配置
    extract_type        VARCHAR(30) COMMENT '提取方式：JSON_PATH/XPATH/REGEX/HEADER/COOKIE',
    extract_expression  VARCHAR(1000) COMMENT '提取表达式',
    extract_match_no    INT DEFAULT 0 COMMENT '匹配序号，0表示所有匹配',
    
    -- 变量设置
    target_variable     VARCHAR(200) COMMENT '目标变量名',
    variable_scope      VARCHAR(20) DEFAULT 'TEMPLATE' COMMENT '变量作用域：TEMPLATE/STEP/GLOBAL',
    default_value       VARCHAR(500) COMMENT '默认值（提取失败时使用）',
    
    -- 处理器配置
    config              JSON COMMENT '处理器配置参数',
    script_content      TEXT COMMENT '脚本内容',
    
    description         VARCHAR(500) COMMENT '描述',
    is_enabled          TINYINT DEFAULT 1 COMMENT '是否启用',
    sort_order          INT DEFAULT 0 COMMENT '执行顺序',
    
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_template_id (template_id),
    INDEX idx_processor_type (processor_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板后置处理器表';

-- ----------------------------
-- 9. 模板变量定义表
-- ----------------------------
CREATE TABLE template_variable (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    template_id         BIGINT NOT NULL COMMENT '模板ID',
    
    -- 变量定义
    variable_name       VARCHAR(200) NOT NULL COMMENT '变量名',
    variable_type       VARCHAR(30) DEFAULT 'STRING' COMMENT '变量类型：STRING/INTEGER/DOUBLE/BOOLEAN/JSON/ARRAY/FILE/DATE/DATETIME',
    default_value       TEXT COMMENT '默认值',
    current_value       TEXT COMMENT '当前值',
    
    -- 变量描述
    description         VARCHAR(500) COMMENT '变量描述',
    example_value       VARCHAR(500) COMMENT '示例值',
    
    -- 变量属性
    is_required         TINYINT DEFAULT 0 COMMENT '是否必填',
    is_editable         TINYINT DEFAULT 1 COMMENT '是否可编辑',
    is_persistent       TINYINT DEFAULT 0 COMMENT '是否持久化（跨请求保持）',
    
    -- 变量来源
    source_type         VARCHAR(30) DEFAULT 'MANUAL' COMMENT '来源类型：MANUAL/EXTRACT/SCRIPT/ENVIRONMENT/DATABASE',
    source_config       JSON COMMENT '来源配置',
    
    -- 验证规则
    validation_rules    JSON COMMENT '验证规则',
    
    sort_order          INT DEFAULT 0 COMMENT '排序序号',
    
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_template_id (template_id),
    INDEX idx_variable_name (variable_name),
    UNIQUE KEY uk_template_variable (template_id, variable_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板变量定义表';

-- ----------------------------
-- 10. 模板环境配置表
-- ----------------------------
CREATE TABLE template_environment (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    template_id         BIGINT NOT NULL COMMENT '模板ID',
    
    env_name            VARCHAR(100) NOT NULL COMMENT '环境名称：开发/测试/生产',
    env_code            VARCHAR(50) COMMENT '环境代码：DEV/TEST/PROD',
    
    -- 环境配置
    base_url            VARCHAR(500) COMMENT '基础URL（覆盖模板配置）',
    headers             JSON COMMENT '环境特定的Header（JSON格式）',
    variables           JSON COMMENT '环境特定的变量（JSON格式）',
    
    -- 认证覆盖
    auth_type           VARCHAR(30) COMMENT '认证类型（覆盖）',
    auth_config         JSON COMMENT '认证配置（覆盖）',
    
    -- 代理配置
    proxy_enabled       TINYINT DEFAULT 0 COMMENT '是否启用代理',
    proxy_host          VARCHAR(200) COMMENT '代理主机',
    proxy_port          INT COMMENT '代理端口',
    proxy_username      VARCHAR(100) COMMENT '代理用户名',
    proxy_password      VARCHAR(200) COMMENT '代理密码',
    
    is_default          TINYINT DEFAULT 0 COMMENT '是否为默认环境',
    description         VARCHAR(500) COMMENT '描述',
    
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_template_id (template_id),
    INDEX idx_env_code (env_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板环境配置表';

-- ----------------------------
-- 11. 模板历史版本表（记录每次修改）
-- ----------------------------
CREATE TABLE template_history (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    template_id         BIGINT NOT NULL COMMENT '模板ID',
    
    -- 版本信息
    version             VARCHAR(20) NOT NULL COMMENT '版本号',
    version_type        VARCHAR(20) DEFAULT 'AUTO' COMMENT '版本类型：AUTO-自动 MAJOR-主版本 MINOR-次版本 PATCH-修订',
    change_summary      VARCHAR(1000) COMMENT '变更摘要',
    change_details      TEXT COMMENT '变更详情（JSON格式记录差异）',
    
    -- 快照内容（保存完整模板JSON）
    template_snapshot   LONGTEXT COMMENT '模板快照（JSON格式）',
    
    -- 操作人
    operator_id         BIGINT NOT NULL COMMENT '操作人ID',
    operator_name       VARCHAR(50) COMMENT '操作人姓名',
    operation_type      VARCHAR(30) COMMENT '操作类型：CREATE/UPDATE/DELETE/PUBLISH/ARCHIVE/COPY',
    
    -- 回滚相关
    can_rollback        TINYINT DEFAULT 1 COMMENT '是否可回滚',
    rollback_to_time    DATETIME COMMENT '回滚到的时间点',
    
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_template_id (template_id),
    INDEX idx_version (version),
    INDEX idx_operator_id (operator_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板历史版本表';

-- ----------------------------
-- 12. 模板收藏/关注表
-- ----------------------------
CREATE TABLE template_favorite (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    template_id         BIGINT NOT NULL COMMENT '模板ID',
    user_id             BIGINT NOT NULL COMMENT '用户ID',
    
    favorite_type       TINYINT DEFAULT 1 COMMENT '类型：1-收藏 2-关注',
    remark              VARCHAR(200) COMMENT '备注',
    
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY uk_template_user (template_id, user_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板收藏/关注表';

-- ----------------------------
-- 13. 模板使用记录表（统计用）
-- ----------------------------
CREATE TABLE template_usage_log (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    template_id         BIGINT NOT NULL COMMENT '模板ID',
    
    -- 使用信息
    usage_type          VARCHAR(30) COMMENT '使用类型：TEST/RUN/DEBUG/IMPORT/EXPORT/COPY',
    user_id             BIGINT COMMENT '使用人ID',
    user_name           VARCHAR(50) COMMENT '使用人姓名',
    
    -- 执行信息
    task_id             BIGINT COMMENT '关联任务ID（如果是任务调度的执行）',
    execution_result    TINYINT COMMENT '执行结果：0-失败 1-成功',
    execution_duration  INT COMMENT '执行耗时（毫秒）',
    
    -- 请求/响应摘要（可选，用于调试）
    request_summary     VARCHAR(1000) COMMENT '请求摘要',
    response_summary    VARCHAR(1000) COMMENT '响应摘要',
    error_message       TEXT COMMENT '错误信息',
    
    client_ip           VARCHAR(50) COMMENT '客户端IP',
    user_agent          VARCHAR(500) COMMENT 'User-Agent',
    
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_template_id (template_id),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time),
    INDEX idx_usage_type (usage_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板使用记录表';

-- ----------------------------
-- 14. 模板导入导出记录表
-- ----------------------------
CREATE TABLE template_import_export (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    
    operation_type      VARCHAR(20) NOT NULL COMMENT '操作类型：IMPORT/EXPORT',
    
    -- 模板信息
    template_ids        VARCHAR(1000) COMMENT '涉及的模板ID列表',
    folder_id           BIGINT COMMENT '目标文件夹ID（导入时）',
    
    -- 文件信息
    file_name           VARCHAR(255) COMMENT '文件名',
    file_path           VARCHAR(1000) COMMENT '文件路径',
    file_size           BIGINT COMMENT '文件大小（字节）',
    file_format         VARCHAR(20) COMMENT '文件格式：JSON/YAML/POSTMAN/HTTP/OPENAPI',
    
    -- 操作结果
    status              TINYINT DEFAULT 0 COMMENT '状态：0-处理中 1-成功 2-失败',
    success_count       INT COMMENT '成功数量',
    fail_count          INT COMMENT '失败数量',
    error_message       TEXT COMMENT '错误信息',
    
    -- 操作人
    operator_id         BIGINT NOT NULL COMMENT '操作人ID',
    operator_name       VARCHAR(50) COMMENT '操作人姓名',
    
    -- 时间
    start_time          DATETIME COMMENT '开始时间',
    end_time            DATETIME COMMENT '结束时间',
    
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_operation_type (operation_type),
    INDEX idx_status (status),
    INDEX idx_operator_id (operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板导入导出记录表';

-- ----------------------------
-- 15. 模板共享/授权表
-- ----------------------------
CREATE TABLE template_share (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    template_id         BIGINT NOT NULL COMMENT '模板ID',
    
    -- 共享对象
    share_type          VARCHAR(20) NOT NULL COMMENT '共享类型：USER/TEAM/ROLE/DEPARTMENT',
    share_target_id     BIGINT NOT NULL COMMENT '共享对象ID',
    share_target_name   VARCHAR(100) COMMENT '共享对象名称',
    
    -- 权限配置
    permission          VARCHAR(50) COMMENT '权限：VIEW/EDIT/EXECUTE/ADMIN',
    can_share           TINYINT DEFAULT 0 COMMENT '是否允许再次共享',
    expire_time         DATETIME COMMENT '过期时间',
    
    -- 共享链接（可选）
    share_code          VARCHAR(100) COMMENT '共享码',
    share_link          VARCHAR(500) COMMENT '共享链接',
    access_password     VARCHAR(100) COMMENT '访问密码',
    
    -- 创建人
    created_by          BIGINT NOT NULL COMMENT '创建人ID',
    
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_template_id (template_id),
    INDEX idx_share_type_target (share_type, share_target_id),
    INDEX idx_share_code (share_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板共享/授权表';

-- ----------------------------
-- 外键约束（可选，根据项目规范决定是否启用）
-- ----------------------------

ALTER TABLE interface_template ADD CONSTRAINT fk_template_folder
    FOREIGN KEY (folder_id) REFERENCES template_folder(id) ON DELETE SET NULL;

ALTER TABLE template_header ADD CONSTRAINT fk_header_template
    FOREIGN KEY (template_id) REFERENCES interface_template(id) ON DELETE CASCADE;

ALTER TABLE template_parameter ADD CONSTRAINT fk_param_template
    FOREIGN KEY (template_id) REFERENCES interface_template(id) ON DELETE CASCADE;

ALTER TABLE template_form_data ADD CONSTRAINT fk_form_template
    FOREIGN KEY (template_id) REFERENCES interface_template(id) ON DELETE CASCADE;

ALTER TABLE template_assertion ADD CONSTRAINT fk_assert_template
    FOREIGN KEY (template_id) REFERENCES interface_template(id) ON DELETE CASCADE;

ALTER TABLE template_pre_processor ADD CONSTRAINT fk_pre_proc_template
    FOREIGN KEY (template_id) REFERENCES interface_template(id) ON DELETE CASCADE;

ALTER TABLE template_post_processor ADD CONSTRAINT fk_post_proc_template
    FOREIGN KEY (template_id) REFERENCES interface_template(id) ON DELETE CASCADE;

ALTER TABLE template_variable ADD CONSTRAINT fk_var_template
    FOREIGN KEY (template_id) REFERENCES interface_template(id) ON DELETE CASCADE;

ALTER TABLE template_environment ADD CONSTRAINT fk_env_template
    FOREIGN KEY (template_id) REFERENCES interface_template(id) ON DELETE CASCADE;

-- ----------------------------
-- 初始数据
-- ----------------------------

-- 默认根分类
INSERT INTO template_folder (id, parent_id, name, description, sort_order, owner_id, visibility) VALUES
(1, 0, '默认分类', '系统默认分类', 0, 1, 3),
(2, 0, '船舶CAD接口', '船舶CAD系统相关接口模板', 1, 1, 3),
(3, 0, 'ERP接口', '企业资源计划系统接口模板', 2, 1, 3),
(4, 0, 'PLM接口', '产品生命周期管理接口模板', 3, 1, 3);

-- 设置自增起始值
ALTER TABLE template_folder AUTO_INCREMENT = 100;
ALTER TABLE interface_template AUTO_INCREMENT = 1000;
