# CAD 软件模拟接口与文件格式转换设计文档（简化增强版）

## 1. 文档信息

| 项目 | 内容 |
|---|---|
| 文档名称 | CAD 软件模拟接口与文件格式转换设计文档 |
| 所属系统 | PDM 系统 |
| 模块名称 | CAD 软件模拟接口配置 / CAD 文件格式转换配置 |
| 适用范围 | NX、CATIA、中望 CAD、自定义 CAD 的模拟接口、数据字段转换、文件格式转换 |
| 版本 | V1.1 |

---

## 2. 设计结论

这个功能不应该只理解成“数据类型转换”。

更准确地说，CAD 集成里会有两类转换：

```text
1. 数据格式转换
   CAD 接口返回字段 → PDM 业务字段
   例如：part_no → partCode

2. 文件格式转换
   不同 CAD 软件的原生文件 → PDM 可管理/可预览/可交换的标准文件
   例如：CATPart → STEP、PRT → STEP、DWG → PDF/DXF
```

因此，本模块需要同时支持：

```text
CAD 模拟接口配置
CAD 数据字段转换
CAD 文件格式转换
接口测试 / 转换测试
转换任务日志
```

---

## 3. 模块整体定位

当前菜单包括：

```text
配置 NX 软件模拟接口
配置 CATIA 软件模拟接口
配置中望 CAD 软件模拟接口
配置自定义 CAD 软件模拟接口
```

这几个菜单底层实现可以保持一致，不需要开发四套功能。

推荐设计：

```text
一个页面组件
一套后端接口
一套数据模型
通过 cad_type 区分不同 CAD 软件
通过 convert_type 区分数据转换和文件转换
```

---

## 4. CAD 类型设计

| CAD 类型 | 说明 | 常见文件格式 |
|---|---|---|
| NX | Siemens NX | .prt |
| CATIA | CATIA | .CATPart、.CATProduct、.CATDrawing |
| ZWCAD | 中望 CAD | .dwg、.dxf |
| CUSTOM | 自定义 CAD | 企业自定义格式 |

---

## 5. 转换类型设计

| 转换类型 | 说明 | 示例 |
|---|---|---|
| DATA | 数据字段转换 | part_no 转 partCode |
| FILE | 文件格式转换 | CATPart 转 STEP |
| BOTH | 同时包含数据和文件转换 | 解析文件后返回零件数据并生成 STEP |

---

## 6. 功能范围

### 6.1 第一版需要实现

| 功能 | 说明 |
|---|---|
| CAD 模拟接口配置 | 配置不同 CAD 软件的模拟接口 |
| 数据字段转换配置 | 配置 CAD 字段到 PDM 字段的映射 |
| 文件格式转换配置 | 配置源文件格式到目标文件格式的转换规则 |
| 模拟接口测试 | 测试接口是否可用 |
| 数据转换测试 | 测试字段映射是否正确 |
| 文件转换测试 | 测试文件格式转换是否可执行 |
| 转换任务日志 | 保存转换请求、转换结果、失败原因 |
| 统一执行接口 | 给业务模块调用，返回 PDM 统一结果 |

### 6.2 第一版暂不做

| 功能 | 说明 |
|---|---|
| 真实 CAD 内核解析 | 不在 PDM 系统内直接解析 CAD 文件 |
| CAD 插件开发 | 不开发 NX / CATIA 插件 |
| 复杂脚本转换 | 不支持 JavaScript / Groovy 脚本 |
| 复杂 BOM 树展开 | 第一版不做复杂装配 BOM 解析 |
| 在线 3D 预览 | 第一版只生成可预览文件或记录转换结果 |
| 复杂审批流程 | 只负责配置和转换，不负责流程审批 |

---

## 7. 推荐技术边界

PDM 系统不要直接负责解析 CAD 原生文件。

推荐方式：

```text
PDM 系统
  ↓
调用 CAD 转换服务 / Mock 转换服务
  ↓
转换服务完成文件转换
  ↓
返回转换后的文件ID / 文件地址 / 元数据
  ↓
PDM 保存转换结果
```

也就是说：

```text
PDM 负责配置、调度、记录、字段映射
CAD 转换服务负责真正的文件转换
```

这样后期接入真实转换服务时，只需要替换接口实现，不需要大改 PDM 主流程。

---

## 8. 文件格式转换场景

### 8.1 三维模型交换

| 源格式 | 目标格式 | 说明 |
|---|---|---|
| .prt | .step / .stp | NX 模型转 STEP |
| .CATPart | .step / .stp | CATIA 零件转 STEP |
| .CATProduct | .step / .stp | CATIA 装配转 STEP |
| 自定义模型格式 | .step / .stp | 自定义模型转 STEP |

### 8.2 几何数据交换

| 源格式 | 目标格式 | 说明 |
|---|---|---|
| .prt | .iges / .igs | NX 转 IGES |
| .CATPart | .iges / .igs | CATIA 转 IGES |

### 8.3 图纸预览

| 源格式 | 目标格式 | 说明 |
|---|---|---|
| .dwg | .pdf | CAD 图纸转 PDF 预览 |
| .dxf | .pdf | DXF 图纸转 PDF |
| .CATDrawing | .pdf | CATIA 工程图转 PDF |
| .prt drawing | .pdf | NX 工程图转 PDF |

### 8.4 轻量化预览

| 源格式 | 目标格式 | 说明 |
|---|---|---|
| .prt | .stl / .obj / .glb | 用于轻量化模型预览 |
| .CATPart | .stl / .obj / .glb | 用于 Web 端模型预览 |

---

## 9. 页面设计

### 9.1 菜单入口

| 菜单 | 页面参数 |
|---|---|
| 配置 NX 软件模拟接口 | cadType=NX |
| 配置 CATIA 软件模拟接口 | cadType=CATIA |
| 配置中望 CAD 软件模拟接口 | cadType=ZWCAD |
| 配置自定义 CAD 软件模拟接口 | cadType=CUSTOM |

### 9.2 页面 Tab 设计

每个菜单进入后，建议页面分成三个 Tab：

```text
基础接口配置
数据字段转换配置
文件格式转换配置
```

### 9.3 列表字段

| 字段 | 说明 |
|---|---|
| 模拟接口ID | 主键ID |
| 模拟接口名称 | 接口名称 |
| CAD类型 | NX / CATIA / ZWCAD / CUSTOM |
| 转换类型 | DATA / FILE / BOTH |
| 适用流程 | 图纸导入、模型归档、BOM 同步等 |
| 请求方式 | GET / POST |
| 接口地址 | 模拟接口地址 |
| 状态 | 启用 / 停用 |
| 版本号 | 配置版本 |
| 负责人 | 负责人 |
| 创建时间 | 创建时间 |
| 操作 | 查看、编辑、测试、启用、停用、删除 |

---

## 10. 基础接口配置

### 10.1 字段设计

| 字段 | 是否必填 | 说明 |
|---|---|---|
| 模拟接口名称 | 是 | 例如：CATIA文件转换模拟接口 |
| CAD类型 | 是 | 菜单自动带入 |
| 转换类型 | 是 | DATA / FILE / BOTH |
| 适用流程 | 是 | MODEL_ARCHIVE、DRAWING_IMPORT、BOM_SYNC |
| 请求方式 | 是 | GET / POST |
| 接口地址 | 是 | 模拟接口或转换服务地址 |
| 请求头 | 否 | JSON |
| 请求参数 | 否 | JSON |
| 请求体模板 | 否 | JSON |
| 模拟响应内容 | 是 | JSON |
| 状态 | 是 | 启用 / 停用 |
| 版本号 | 否 | 默认 V1.0 |
| 负责人 | 否 | 负责人 |
| 备注 | 否 | 说明 |

---

## 11. 数据字段转换配置

数据字段转换用于把 CAD 返回的业务数据转换成 PDM 统一字段。

### 11.1 示例

CAD 返回：

```json
{
  "code": 200,
  "data": {
    "part_no": "P10001",
    "part_name": "连接板",
    "mat": "AL6061",
    "rev": "A",
    "weight_kg": 2.35
  }
}
```

转换后：

```json
{
  "partCode": "P10001",
  "partName": "连接板",
  "material": "AL6061",
  "version": "A",
  "weight": 2.35
}
```

### 11.2 字段配置

| 源字段 | 目标字段 | 目标模块 | 字段类型 | 是否必填 |
|---|---|---|---|---|
| data.part_no | partCode | PART | STRING | 是 |
| data.part_name | partName | PART | STRING | 是 |
| data.mat | material | PART | STRING | 否 |
| data.rev | version | PART | STRING | 否 |
| data.weight_kg | weight | PART | NUMBER | 否 |

### 11.3 第一版支持能力

| 能力 | 是否支持 |
|---|---|
| 字段重命名 | 支持 |
| 嵌套字段取值 | 支持，例如 data.part_no |
| 默认值 | 支持 |
| 必填校验 | 支持 |
| STRING / NUMBER / BOOLEAN 转换 | 支持 |
| 日期格式转换 | 可选 |
| 字典转换 | 可选 |
| JavaScript 脚本转换 | 暂不支持 |
| 复杂数组展开 | 暂不支持 |

---

## 12. 文件格式转换配置

文件格式转换用于配置不同 CAD 软件的原生文件如何转换成 PDM 需要的标准文件。

### 12.1 配置字段

| 字段 | 是否必填 | 说明 |
|---|---|---|
| CAD类型 | 是 | NX / CATIA / ZWCAD / CUSTOM |
| 适用流程 | 是 | MODEL_ARCHIVE、DRAWING_IMPORT、MODEL_PREVIEW |
| 源文件格式 | 是 | .prt、.CATPart、.dwg 等 |
| 目标文件格式 | 是 | .step、.iges、.pdf、.dxf、.stl 等 |
| 转换服务地址 | 是 | 文件转换接口地址 |
| 是否异步转换 | 是 | 0-否，1-是 |
| 超时时间 | 否 | 单位秒 |
| 是否生成预览图 | 否 | 0-否，1-是 |
| 是否保留源文件 | 否 | 0-否，1-是 |
| 是否覆盖目标文件 | 否 | 0-否，1-是 |
| 状态 | 是 | 启用 / 停用 |

### 12.2 常见配置示例

| CAD类型 | 源格式 | 目标格式 | 适用流程 |
|---|---|---|---|
| NX | .prt | .step | MODEL_ARCHIVE |
| NX | .prt | .pdf | DRAWING_IMPORT |
| CATIA | .CATPart | .step | MODEL_ARCHIVE |
| CATIA | .CATProduct | .step | BOM_SYNC |
| CATIA | .CATDrawing | .pdf | DRAWING_IMPORT |
| ZWCAD | .dwg | .pdf | DRAWING_IMPORT |
| ZWCAD | .dwg | .dxf | FILE_EXCHANGE |
| CUSTOM | .custom | .step | MODEL_ARCHIVE |

---

## 13. 简化业务流程

### 13.1 模拟接口配置流程

```text
进入 CAD 菜单
  ↓
新增模拟接口
  ↓
选择转换类型：DATA / FILE / BOTH
  ↓
配置接口地址、请求方式、模拟响应
  ↓
配置数据字段映射
  ↓
配置文件格式转换规则
  ↓
保存
```

### 13.2 数据转换流程

```text
CAD 原始响应
  ↓
读取字段映射配置
  ↓
按 source_field 取值
  ↓
类型转换 / 默认值处理 / 必填校验
  ↓
输出 PDM 统一字段
```

### 13.3 文件转换流程

```text
上传 CAD 源文件
  ↓
识别 CAD 类型和文件后缀
  ↓
查询文件格式转换配置
  ↓
调用转换服务
  ↓
生成目标格式文件
  ↓
保存转换任务记录
  ↓
返回目标文件ID / 文件地址
```

### 13.4 同时转换数据和文件

```text
业务上传 CAD 文件
  ↓
调用 CAD 模拟接口 / 转换服务
  ↓
获取原始响应数据
  ↓
执行数据字段转换
  ↓
执行文件格式转换
  ↓
返回 PDM 统一业务数据 + 目标文件信息
```

---

## 14. 状态设计

### 14.1 配置状态

| 状态值 | 状态名称 | 说明 |
|---|---|---|
| 0 | 停用 | 当前配置不可使用 |
| 1 | 启用 | 当前配置可使用 |

### 14.2 文件转换任务状态

| 状态码 | 状态名称 | 说明 |
|---|---|---|
| 10 | 待转换 | 任务已创建，等待执行 |
| 20 | 转换中 | 文件正在转换 |
| 30 | 转换成功 | 文件转换完成 |
| 40 | 转换失败 | 文件转换失败 |
| 50 | 已取消 | 任务被取消 |

---

## 15. 数据库设计

> 逻辑删除字段 `deleted`：0-已删除，1-未删除。

---

## 15.1 CAD 模拟接口配置表

表名：`pdm_cad_mock_interface`

```sql
CREATE TABLE pdm_cad_mock_interface (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    interface_name VARCHAR(128) NOT NULL COMMENT '模拟接口名称',
    cad_type VARCHAR(32) NOT NULL COMMENT 'CAD类型：NX、CATIA、ZWCAD、CUSTOM',
    convert_type VARCHAR(32) NOT NULL DEFAULT 'DATA' COMMENT '转换类型：DATA数据转换、FILE文件转换、BOTH数据和文件转换',
    apply_flow VARCHAR(64) NOT NULL COMMENT '适用流程：DRAWING_IMPORT图纸导入、MODEL_ARCHIVE模型归档、BOM_SYNC BOM同步',
    request_method VARCHAR(16) NOT NULL DEFAULT 'POST' COMMENT '请求方式：GET、POST',
    interface_url VARCHAR(255) NOT NULL COMMENT '模拟接口地址',
    request_headers TEXT DEFAULT NULL COMMENT '请求头JSON',
    request_params TEXT DEFAULT NULL COMMENT '请求参数JSON',
    request_body_template TEXT DEFAULT NULL COMMENT '请求体模板JSON',
    response_body TEXT NOT NULL COMMENT '模拟响应内容JSON',
    success_field VARCHAR(64) DEFAULT 'code' COMMENT '成功标识字段，如code',
    success_value VARCHAR(64) DEFAULT '200' COMMENT '成功标识值，如200',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    version_no VARCHAR(32) NOT NULL DEFAULT 'V1.0' COMMENT '版本号',
    owner_id BIGINT DEFAULT NULL COMMENT '负责人ID',
    owner_name VARCHAR(64) DEFAULT NULL COMMENT '负责人名称',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_id BIGINT DEFAULT NULL COMMENT '创建人ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_id BIGINT DEFAULT NULL COMMENT '更新人ID',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 1 COMMENT '逻辑删除：0-已删除，1-未删除',
    KEY idx_owner_id (owner_id)
) COMMENT='PDM-CAD软件模拟接口配置表';
```

---

## 15.2 CAD 数据字段转换配置表

表名：`pdm_cad_data_convert_mapping`

```sql
CREATE TABLE pdm_cad_data_convert_mapping (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    mock_interface_id BIGINT NOT NULL COMMENT '模拟接口ID',
    cad_type VARCHAR(32) NOT NULL COMMENT 'CAD类型：NX、CATIA、ZWCAD、CUSTOM',
    apply_flow VARCHAR(64) NOT NULL COMMENT '适用流程',
    convert_direction VARCHAR(32) NOT NULL DEFAULT 'RESPONSE' COMMENT '转换方向：REQUEST请求转换、RESPONSE响应转换',
    source_field VARCHAR(128) NOT NULL COMMENT '源字段路径，如data.part_no',
    source_field_name VARCHAR(128) DEFAULT NULL COMMENT '源字段名称',
    target_module VARCHAR(64) NOT NULL COMMENT '目标模块：PART零件、DRAWING图纸、BOM物料清单、MODEL模型',
    target_field VARCHAR(128) NOT NULL COMMENT '目标字段，如partCode',
    target_field_name VARCHAR(128) DEFAULT NULL COMMENT '目标字段名称',
    field_type VARCHAR(32) NOT NULL DEFAULT 'STRING' COMMENT '字段类型：STRING、NUMBER、DATE、BOOLEAN、JSON',
    required TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否必填：0-否，1-是',
    default_value VARCHAR(255) DEFAULT NULL COMMENT '默认值',
    transform_rule VARCHAR(500) DEFAULT NULL COMMENT '转换规则',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    created_id BIGINT DEFAULT NULL COMMENT '创建人ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_id BIGINT DEFAULT NULL COMMENT '更新人ID',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 1 COMMENT '逻辑删除：0-已删除，1-未删除',
    KEY idx_mock_interface_id (mock_interface_id)
) COMMENT='PDM-CAD数据字段转换配置表';
```

---

## 15.3 CAD 文件格式转换配置表

表名：`pdm_cad_file_convert_config`

```sql
CREATE TABLE pdm_cad_file_convert_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    mock_interface_id BIGINT DEFAULT NULL COMMENT '模拟接口ID',
    cad_type VARCHAR(32) NOT NULL COMMENT 'CAD类型：NX、CATIA、ZWCAD、CUSTOM',
    apply_flow VARCHAR(64) NOT NULL COMMENT '适用流程：DRAWING_IMPORT、MODEL_ARCHIVE、BOM_SYNC、MODEL_PREVIEW',
    source_format VARCHAR(32) NOT NULL COMMENT '源文件格式，如.prt、.CATPart、.dwg',
    target_format VARCHAR(32) NOT NULL COMMENT '目标文件格式，如.step、.iges、.pdf、.dxf、.stl',
    convert_url VARCHAR(255) NOT NULL COMMENT '文件转换服务地址',
    async_convert TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否异步转换：0-否，1-是',
    timeout_seconds INT NOT NULL DEFAULT 60 COMMENT '超时时间，单位秒',
    generate_preview TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否生成预览图：0-否，1-是',
    keep_source_file TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否保留源文件：0-否，1-是',
    overwrite_target_file TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否覆盖目标文件：0-否，1-是',
    request_template TEXT DEFAULT NULL COMMENT '转换请求模板JSON',
    response_template TEXT DEFAULT NULL COMMENT '转换响应模板JSON',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_id BIGINT DEFAULT NULL COMMENT '创建人ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_id BIGINT DEFAULT NULL COMMENT '更新人ID',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 1 COMMENT '逻辑删除：0-已删除，1-未删除',
    KEY idx_mock_interface_id (mock_interface_id)
) COMMENT='PDM-CAD文件格式转换配置表';
```

---

## 15.4 CAD 文件转换任务表

表名：`pdm_cad_file_convert_task`

```sql
CREATE TABLE pdm_cad_file_convert_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_no VARCHAR(64) NOT NULL COMMENT '转换任务编号',
    file_convert_config_id BIGINT NOT NULL COMMENT '文件格式转换配置ID',
    cad_type VARCHAR(32) NOT NULL COMMENT 'CAD类型：NX、CATIA、ZWCAD、CUSTOM',
    apply_flow VARCHAR(64) NOT NULL COMMENT '适用流程',
    source_file_id BIGINT NOT NULL COMMENT '源文件ID',
    source_file_name VARCHAR(255) NOT NULL COMMENT '源文件名称',
    source_format VARCHAR(32) NOT NULL COMMENT '源文件格式',
    target_file_id BIGINT DEFAULT NULL COMMENT '目标文件ID',
    target_file_name VARCHAR(255) DEFAULT NULL COMMENT '目标文件名称',
    target_format VARCHAR(32) NOT NULL COMMENT '目标文件格式',
    task_status TINYINT NOT NULL DEFAULT 10 COMMENT '任务状态：10待转换，20转换中，30转换成功，40转换失败，50已取消',
    request_content TEXT DEFAULT NULL COMMENT '请求内容JSON',
    response_content TEXT DEFAULT NULL COMMENT '响应内容JSON',
    error_message VARCHAR(1000) DEFAULT NULL COMMENT '错误信息',
    cost_time_ms BIGINT DEFAULT NULL COMMENT '耗时，单位毫秒',
    created_id BIGINT DEFAULT NULL COMMENT '创建人ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_id BIGINT DEFAULT NULL COMMENT '更新人ID',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 1 COMMENT '逻辑删除：0-已删除，1-未删除',
    UNIQUE KEY uk_task_no (task_no),
    KEY idx_file_convert_config_id (file_convert_config_id),
    KEY idx_source_file_id (source_file_id),
    KEY idx_target_file_id (target_file_id)
) COMMENT='PDM-CAD文件格式转换任务表';
```

---

## 15.5 CAD 模拟接口测试日志表

表名：`pdm_cad_mock_test_log`

```sql
CREATE TABLE pdm_cad_mock_test_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    mock_interface_id BIGINT NOT NULL COMMENT '模拟接口ID',
    cad_type VARCHAR(32) NOT NULL COMMENT 'CAD类型：NX、CATIA、ZWCAD、CUSTOM',
    convert_type VARCHAR(32) NOT NULL COMMENT '转换类型：DATA、FILE、BOTH',
    request_method VARCHAR(16) NOT NULL COMMENT '请求方式：GET、POST',
    interface_url VARCHAR(255) NOT NULL COMMENT '接口地址',
    request_content TEXT DEFAULT NULL COMMENT '请求内容JSON',
    response_content TEXT DEFAULT NULL COMMENT '原始响应内容JSON',
    converted_content TEXT DEFAULT NULL COMMENT '转换后数据内容JSON',
    file_convert_result TEXT DEFAULT NULL COMMENT '文件转换结果JSON',
    test_result TINYINT(1) NOT NULL COMMENT '测试结果：0-失败，1-成功',
    error_message VARCHAR(1000) DEFAULT NULL COMMENT '错误信息',
    cost_time_ms BIGINT DEFAULT NULL COMMENT '耗时，单位毫秒',
    created_id BIGINT DEFAULT NULL COMMENT '创建人ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_id BIGINT DEFAULT NULL COMMENT '更新人ID',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) NOT NULL DEFAULT 1 COMMENT '逻辑删除：0-已删除，1-未删除',
    KEY idx_mock_interface_id (mock_interface_id)
) COMMENT='PDM-CAD软件模拟接口测试日志表';
```

---

## 16. 后端接口设计

### 16.1 CAD 模拟接口分页查询

```http
GET /api/pdm/cad/mock/page
```

参数：

| 参数 | 说明 |
|---|---|
| pageNo | 当前页 |
| pageSize | 每页条数 |
| cadType | CAD 类型 |
| convertType | DATA / FILE / BOTH |
| interfaceName | 接口名称 |
| applyFlow | 适用流程 |
| status | 状态 |

---

### 16.2 新增模拟接口

```http
POST /api/pdm/cad/mock/create
```

请求示例：

```json
{
  "interfaceName": "CATIA文件转换模拟接口",
  "cadType": "CATIA",
  "convertType": "BOTH",
  "applyFlow": "MODEL_ARCHIVE",
  "requestMethod": "POST",
  "interfaceUrl": "/mock/catia/convert",
  "responseBody": "{\"code\":200,\"data\":{\"part_no\":\"P10001\",\"targetFileId\":20001}}",
  "status": 1,
  "versionNo": "V1.0"
}
```

---

### 16.3 保存数据字段转换配置

```http
POST /api/pdm/cad/mock/data-convert/save-batch
```

---

### 16.4 查询数据字段转换配置

```http
GET /api/pdm/cad/mock/data-convert/list/{mockInterfaceId}
```

---

### 16.5 保存文件格式转换配置

```http
POST /api/pdm/cad/mock/file-convert/save
```

请求示例：

```json
{
  "mockInterfaceId": 1,
  "cadType": "CATIA",
  "applyFlow": "MODEL_ARCHIVE",
  "sourceFormat": ".CATPart",
  "targetFormat": ".step",
  "convertUrl": "/mock/catia/file/convert",
  "asyncConvert": 1,
  "timeoutSeconds": 60,
  "generatePreview": 1,
  "keepSourceFile": 1,
  "overwriteTargetFile": 0
}
```

---

### 16.6 查询文件格式转换配置

```http
GET /api/pdm/cad/mock/file-convert/list
```

参数：

| 参数 | 说明 |
|---|---|
| cadType | CAD 类型 |
| applyFlow | 适用流程 |
| sourceFormat | 源格式 |
| targetFormat | 目标格式 |
| status | 状态 |

---

### 16.7 执行文件转换

```http
POST /api/pdm/cad/mock/file-convert/execute
```

请求示例：

```json
{
  "cadType": "CATIA",
  "applyFlow": "MODEL_ARCHIVE",
  "sourceFileId": 10001,
  "sourceFileName": "P10001.CATPart",
  "sourceFormat": ".CATPart",
  "targetFormat": ".step"
}
```

返回示例：

```json
{
  "code": 200,
  "msg": "转换任务已创建",
  "data": {
    "taskNo": "CAD_CONVERT_202604290001",
    "taskStatus": 10
  }
}
```

---

### 16.8 查询文件转换任务

```http
GET /api/pdm/cad/mock/file-convert/task/{taskNo}
```

返回示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "taskNo": "CAD_CONVERT_202604290001",
    "taskStatus": 30,
    "targetFileId": 20001,
    "targetFileName": "P10001.step",
    "targetFormat": ".step"
  }
}
```

---

### 16.9 统一执行接口

```http
POST /api/pdm/cad/mock/execute
```

请求示例：

```json
{
  "cadType": "CATIA",
  "applyFlow": "MODEL_ARCHIVE",
  "sourceFileId": 10001,
  "sourceFileName": "P10001.CATPart",
  "requestData": {
    "fileId": "10001"
  }
}
```

处理逻辑：

```text
1. 根据 cadType + applyFlow 查询启用的模拟接口
2. 读取模拟响应 response_body
3. 如果配置了数据转换，执行字段映射转换
4. 如果配置了文件转换，创建文件转换任务或直接执行转换
5. 返回 PDM 统一数据 + 文件转换结果
```

返回示例：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "partCode": "P10001",
    "partName": "连接板",
    "material": "AL6061",
    "version": "A",
    "fileConvert": {
      "taskNo": "CAD_CONVERT_202604290001",
      "taskStatus": 30,
      "targetFileId": 20001,
      "targetFileName": "P10001.step"
    }
  }
}
```

---

## 17. 核心查询规则

### 17.1 查找模拟接口

```sql
SELECT *
FROM pdm_cad_mock_interface
WHERE cad_type = ?
  AND apply_flow = ?
  AND status = 1
  AND deleted = 1
LIMIT 1;
```

### 17.2 查找文件转换配置

```sql
SELECT *
FROM pdm_cad_file_convert_config
WHERE cad_type = ?
  AND apply_flow = ?
  AND source_format = ?
  AND target_format = ?
  AND status = 1
  AND deleted = 1
LIMIT 1;
```

---

## 18. 核心校验规则

### 18.1 模拟接口校验

1. 模拟接口名称不能为空；
2. CAD 类型不能为空；
3. 转换类型不能为空；
4. 适用流程不能为空；
5. 请求方式不能为空；
6. 接口地址不能为空；
7. 模拟响应内容必须是合法 JSON。

### 18.2 数据字段转换校验

1. 源字段不能为空；
2. 目标字段不能为空；
3. 目标模块不能为空；
4. 同一个接口下，目标模块 + 目标字段不能重复；
5. 必填字段转换后不能为空。

### 18.3 文件格式转换校验

1. CAD 类型不能为空；
2. 源文件格式不能为空；
3. 目标文件格式不能为空；
4. 转换服务地址不能为空；
5. 同一个 CAD 类型 + 适用流程 + 源格式 + 目标格式下只能有一条启用配置；
6. 文件后缀必须和源格式匹配；
7. 转换失败要保存失败原因。

---

## 19. 最小可用实现方案

如果只是为了先实现功能，建议第一版做到：

```text
1. 一个页面组件
2. 四个菜单入口，通过 cadType 区分
3. 基础接口配置
4. 数据字段映射配置
5. 文件格式转换配置
6. 文件转换任务记录
7. 模拟接口测试
8. 统一执行接口
```

第一版不要做：

```text
1. CAD 原生文件内容解析
2. 真实 CAD 插件
3. 复杂脚本引擎
4. 复杂 BOM 树解析
5. 在线 3D 预览
6. 复杂版本发布流程
```

---

## 20. 推荐包结构

```text
com.xxx.pdm.cad.mock
  ├── controller
  │     └── CadMockInterfaceController.java
  ├── service
  │     ├── CadMockInterfaceService.java
  │     ├── CadDataConvertService.java
  │     └── CadFileConvertService.java
  ├── service.impl
  ├── mapper
  │     ├── CadMockInterfaceMapper.java
  │     ├── CadDataConvertMappingMapper.java
  │     ├── CadFileConvertConfigMapper.java
  │     └── CadFileConvertTaskMapper.java
  ├── entity
  ├── dto
  ├── vo
  └── enums
```

---

## 21. 枚举建议

### 21.1 CAD 类型

```java
public enum CadTypeEnum {
    NX("NX", "NX软件"),
    CATIA("CATIA", "CATIA软件"),
    ZWCAD("ZWCAD", "中望CAD软件"),
    CUSTOM("CUSTOM", "自定义CAD软件");
}
```

### 21.2 转换类型

```java
public enum CadConvertTypeEnum {
    DATA("DATA", "数据字段转换"),
    FILE("FILE", "文件格式转换"),
    BOTH("BOTH", "数据和文件转换");
}
```

### 21.3 文件转换任务状态

```java
public enum CadFileConvertTaskStatusEnum {
    WAITING(10, "待转换"),
    RUNNING(20, "转换中"),
    SUCCESS(30, "转换成功"),
    FAILED(40, "转换失败"),
    CANCELED(50, "已取消");
}
```

---

## 22. 总结

这个模块不应该只做“数据类型转换”。

更合理的简化设计是：

```text
CAD 软件模拟接口配置
  ↓
数据字段转换
  ↓
文件格式转换
  ↓
PDM 统一数据输出
```

最终落地建议：

```text
一套菜单页面
一套后端接口
五张核心表
支持数据字段转换
支持文件格式转换
支持转换任务记录
支持测试
```

五张核心表：

```text
pdm_cad_mock_interface
pdm_cad_data_convert_mapping
pdm_cad_file_convert_config
pdm_cad_file_convert_task
pdm_cad_mock_test_log
```

这样既保留了实现的简单性，又能覆盖不同 CAD 软件之间真实存在的文件格式转换问题。
