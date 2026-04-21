# 报告与分析管理模块 API 接口文档

## 📋 文档概述

本文档详细描述了报告与分析管理模块的4个核心Controller的API接口，包括：
- **ReportController** - 报告管理
- **ReportTemplateController** - 报告模板管理  
- **TemplateStatisticsController** - 模板统计管理
- **ReportChartController** - 报告图表管理

---

## 1. ReportController - 报告管理

**基础路径**: `/api/reports`

### 1.1 创建报告
- **接口**: `POST /api/reports`
- **权限**: `report:create` 或 ADMIN角色
- **描述**: 创建新的报告
- **请求体**:
```json
{
  "name": "报告名称",
  "description": "报告描述",
  "reportType": "报告类型",
  "templateId": 1,
  "content": "报告内容",
  "styleConfig": "样式配置"
}
```
- **响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": 12345
}
```

### 1.2 更新报告
- **接口**: `PUT /api/reports/{id}`
- **权限**: `report:update` 或 ADMIN角色
- **描述**: 更新指定ID的报告
- **路径参数**: `id` - 报告ID
- **请求体**: 同创建报告

### 1.3 删除报告
- **接口**: `DELETE /api/reports/{id}`
- **权限**: `report:delete` 或 ADMIN角色
- **描述**: 删除指定ID的报告（软删除）
- **路径参数**: `id` - 报告ID

### 1.4 获取报告列表
- **接口**: `GET /api/reports`
- **权限**: `report:view` 或 ADMIN角色
- **描述**: 获取报告列表，支持按类型和状态筛选
- **查询参数**:
  - `reportType` (可选) - 报告类型
  - `status` (可选) - 报告状态
- **响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "报告1",
      "reportType": "STATISTICAL",
      "status": "PUBLISHED"
    }
  ]
}
```

### 1.5 获取报告详情
- **接口**: `GET /api/reports/{id}`
- **权限**: `report:view` 或 ADMIN角色
- **描述**: 获取指定报告的详细信息
- **路径参数**: `id` - 报告ID

### 1.6 自动生成报告
- **接口**: `POST /api/reports/auto-generate`
- **权限**: `report:auto-generate` 或 ADMIN角色
- **描述**: 根据配置自动生成报告
- **查询参数**:
  - `reportType` - 报告类型
  - `dataSourceIds` - 数据源ID列表

### 1.7 预览报告
- **接口**: `POST /api/reports/preview`
- **权限**: `report:view` 或 ADMIN角色
- **描述**: 预览报告生成效果
- **请求体**: 报告配置信息

### 1.8 导出报告
- **接口**: `GET /api/reports/{id}/export`
- **权限**: `report:export` 或 ADMIN角色
- **描述**: 导出报告为指定格式
- **路径参数**: `id` - 报告ID
- **查询参数**: `format` - 导出格式(PDF/EXCEL/WORD)

### 1.9 获取测试结果表格
- **接口**: `GET /api/reports/test-results/table`
- **权限**: `report:view` 或 ADMIN角色
- **描述**: 获取测试结果的表格数据
- **查询参数**:
  - `templateId` (可选) - 模板ID
  - `startTime` (可选) - 开始时间
  - `endTime` (可选) - 结束时间

### 1.10 获取时间线数据
- **接口**: `GET /api/reports/timeline`
- **权限**: `report:view` 或 ADMIN角色
- **描述**: 获取报告生成和执行的时间线数据
- **查询参数**:
  - `templateId` (可选) - 模板ID
  - `timeRange` (可选) - 时间范围

---

## 2. ReportTemplateController - 报告模板管理

**基础路径**: `/api/report/templates`

### 2.1 创建模板
- **接口**: `POST /api/report/templates`
- **权限**: `report:template:create` 或 ADMIN角色
- **描述**: 创建新的报告模板
- **请求体**:
```json
{
  "name": "模板名称",
  "description": "模板描述",
  "templateType": "TEMPLATE_EFFICIENCY_STATISTICS",
  "templateStructure": "模板结构JSON",
  "chapterStructure": "章节结构JSON",
  "content": "模板内容",
  "styleConfig": "样式配置"
}
```

### 2.2 更新模板
- **接口**: `PUT /api/report/templates/{id}`
- **权限**: `report:template:update` 或 ADMIN角色
- **描述**: 更新指定ID的模板

### 2.3 删除模板
- **接口**: `DELETE /api/report/templates/{id}`
- **权限**: `report:template:delete` 或 ADMIN角色
- **描述**: 删除模板（软删除）

### 2.4 获取模板列表
- **接口**: `GET /api/report/templates`
- **权限**: `report:template:view` 或 ADMIN角色
- **描述**: 获取模板列表，支持按类型和公开状态筛选
- **查询参数**:
  - `templateType` (可选) - 模板类型
  - `isPublic` (可选) - 是否公开
  - `name` (可选) - 模板名称

### 2.5 获取模板详情
- **接口**: `GET /api/report/templates/{id}`
- **权限**: `report:template:view` 或 ADMIN角色
- **描述**: 获取模板详细信息

### 2.6 XML模板导入
- **接口**: `POST /api/report/templates/import`
- **权限**: `report:template:import` 或 ADMIN角色
- **描述**: 导入XML格式的模板文件
- **请求类型**: `multipart/form-data`
- **参数**:
  - `file` - XML模板文件
  - `newName` (可选) - 新模板名称

### 2.7 XML模板导出
- **接口**: `GET /api/report/templates/{id}/export`
- **权限**: `report:template:export` 或 ADMIN角色
- **描述**: 导出模板为XML格式文件
- **响应**: XML文件下载

### 2.8 XML模板预览
- **接口**: `POST /api/report/templates/xml-preview`
- **权限**: `report:template:view` 或 ADMIN角色
- **描述**: 预览XML模板内容
- **请求体**: XML内容字符串

### 2.9 关联业务对象
- **接口**: `PUT /api/report/templates/{id}/relate`
- **权限**: `report:template:relate` 或 ADMIN角色
- **描述**: 关联模板与业务对象
- **查询参数**: `businessType` - 业务对象类型

### 2.10 获取模板使用记录
- **接口**: `GET /api/report/templates/{id}/usage`
- **权限**: `report:template:view` 或 ADMIN角色
- **描述**: 获取模板的使用记录

---

## 3. TemplateStatisticsController - 模板统计管理

**基础路径**: `/api/report/template-statistics`

### 3.1 获取模板使用频率统计
- **接口**: `GET /api/report/template-statistics/usage`
- **权限**: `report:statistics:view` 或 ADMIN角色
- **描述**: 获取模板使用频率统计数据，支持三种数据源选择
- **查询参数**:
  - `timeRange` (可选, 默认: "7DAYS") - 时间范围：TODAY/7DAYS/30DAYS/CUSTOM
  - `startDate` (可选) - 开始日期（当timeRange为CUSTOM时使用）
  - `endDate` (可选) - 结束日期（当timeRange为CUSTOM时使用）
  - `templateType` (可选) - 模板类型筛选
  - `dataSource` (可选, 默认: "JOB_LOG") - 数据源：JOB_LOG（定时任务）/UNIFIED（手动+定时）/BATCH（批量任务）
- **响应**:
```json
{
  "templateStats": [
    {
      "templateId": 1,
      "templateName": "效率分析模板",
      "usageCount": 150,
      "successCount": 135,
      "failureCount": 15,
      "successRate": "90.00%",
      "avgDuration": "1200ms"
    }
  ]
}
```

**数据源说明**:
- **JOB_LOG**：定时任务数据源（template_job_log表）- 默认
- **UNIFIED**：手动+定时统一数据源（template_execute_log表）
- **BATCH**：批量任务数据源（template_job_batch表）

### 3.2 获取模板执行效率报告
- **接口**: `GET /api/report/template-statistics/efficiency`
- **权限**: `report:statistics:view` 或 ADMIN角色
- **描述**: 获取模板执行效率统计报告，支持三种数据源选择
- **查询参数**:
  - `startDate` - 开始日期
  - `endDate` - 结束日期
  - `templateId` (可选) - 模板ID
  - `dataSource` (可选, 默认: "JOB_LOG") - 数据源：JOB_LOG（定时任务）/UNIFIED（手动+定时）/BATCH（批量任务）
- **响应**:
```json
{
  "id": 1776672532387,
  "name": "模板执行效率报告",
  "description": "基于模板执行日志生成的效率分析报告",
  "content": "报告内容...",
  "generateTime": "2024-04-20T10:30:00"
}
```

### 3.3 生成模板使用图表
- **接口**: `GET /api/report/template-statistics/usage-chart`
- **权限**: `report:statistics:view` 或 ADMIN角色
- **描述**: 生成模板使用频率的图表数据
- **查询参数**:
  - `timeRange` - 时间范围
  - `chartType` - 图表类型

### 3.4 生成模板效率图表
- **接口**: `GET /api/report/template-statistics/efficiency-chart`
- **权限**: `report:statistics:view` 或 ADMIN角色
- **描述**: 生成模板执行效率的图表数据
- **查询参数**:
  - `startDate` - 开始日期
  - `endDate` - 结束日期
  - `chartType` - 图表类型

---

## 4. ReportChartController - 报告图表管理

**基础路径**: `/api/report/charts`

### 4.1 创建图表
- **接口**: `POST /api/report/charts`
- **权限**: `report:chart:create` 或 ADMIN角色
- **描述**: 创建新的报告图表
- **请求体**:
```json
{
  "name": "图表名称",
  "chartType": "BAR",
  "dataSourceType": "TEMPLATE",
  "chartConfig": "图表配置JSON",
  "styleConfig": "样式配置JSON"
}
```

### 4.2 更新图表
- **接口**: `PUT /api/report/charts/{id}`
- **权限**: `report:chart:update` 或 ADMIN角色
- **描述**: 更新指定ID的图表

### 4.3 删除图表
- **接口**: `DELETE /api/report/charts/{id}`
- **权限**: `report:chart:delete` 或 ADMIN角色
- **描述**: 删除图表

### 4.4 获取图表列表
- **接口**: `GET /api/report/charts`
- **权限**: `report:chart:view` 或 ADMIN角色
- **描述**: 获取图表列表
- **查询参数**:
  - `chartType` (可选) - 图表类型
  - `isPublic` (可选) - 是否公开

### 4.5 获取图表详情
- **接口**: `GET /api/report/charts/{id}`
- **权限**: `report:chart:view` 或 ADMIN角色
- **描述**: 获取图表详细信息

### 4.6 获取图表数据
- **接口**: `GET /api/report/charts/{id}/data`
- **权限**: `report:chart:view` 或 ADMIN角色
- **描述**: 获取图表的实时数据
- **查询参数**:
  - `startTime` (可选) - 开始时间
  - `endTime` (可选) - 结束时间

### 4.7 预览图表
- **接口**: `POST /api/report/charts/preview`
- **权限**: `report:chart:view` 或 ADMIN角色
- **描述**: 预览图表效果
- **请求体**: 图表配置信息

---

## 🔗 数据模型说明

### ReportDTO - 报告数据传输对象
```java
public class ReportDTO {
    private Long id;                    // 报告ID
    private String name;                // 报告名称
    private String description;         // 报告描述
    private String reportType;          // 报告类型
    private Long templateId;            // 模板ID
    private String content;             // 报告内容
    private String styleConfig;         // 样式配置
    private String generateType;        // 生成方式
    private String status;              // 报告状态
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 更新时间
}
```

### ReportTemplateDTO - 报告模板数据传输对象
```java
public class ReportTemplateDTO {
    private Long id;                    // 模板ID
    private String name;                // 模板名称
    private String description;         // 模板描述
    private String templateType;        // 模板类型
    private String templateStructure;   // 模板结构
    private String chapterStructure;    // 章节结构
    private String content;             // 模板内容
    private String styleConfig;         // 样式配置
    private Boolean isPublic;           // 是否公开
    private Integer usageCount;         // 使用次数
    private Integer status;             // 状态
}
```

---

## 🔐 权限说明

所有接口都需要相应的权限或ADMIN角色才能访问：

- **报告管理权限**: `report:*`
- **模板管理权限**: `report:template:*`
- **统计查看权限**: `report:statistics:view`
- **图表管理权限**: `report:chart:*`

---

## 📊 测试建议

### 功能测试流程
1. **模板管理测试**
   - 创建模板 → 导入XML模板 → 导出模板 → 删除模板

2. **报告生成测试**
   - 根据模板生成报告 → 预览报告 → 导出报告

3. **统计分析测试**
   - 获取使用统计 → 查看效率报告 → 生成图表数据

4. **权限测试**
   - 测试不同角色的访问权限

### 性能测试重点
- 大数据量下的报告生成性能
- 图表数据的实时查询性能
- XML模板导入导出的性能

---

## 📝 版本历史

| 版本 | 日期 | 描述 |
|------|------|------|
| 1.0 | 2026-04-21 | 初始版本，包含4个Controller的完整API文档 |

---

**文档维护**: 开发团队  
**最后更新**: 2026-04-21  
**适用版本**: v1.0.0