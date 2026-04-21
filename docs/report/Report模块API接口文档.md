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
  "data": {
    "id": 8,
    "name": "自动生成报告 - 2026-04-21T19:53:58.309849400",
    "description": "基于数据源自动生成的报告",
    "reportType": "1",
    "templateId": null,
    "templateName": null,
    "content": null,
    "styleConfig": null,
    "generateType": "AUTO",
    "generateFrequency": null,
    "nextGenerateTime": null,
    "dataSourceIds": "[1]",
    "chartIds": null,
    "status": "DRAFT",
    "isScheduled": false,
    "isNotified": false,
    "exportCount": 0,
    "lastExportTime": null,
    "createName": null,
    "createTime": "2026-04-21T19:53:58",
    "updateTime": "2026-04-21T19:53:58"
  },
  "timestamp": 1776772449435,
  "success": true
}
```

### 1.2 更新报告
- **接口**: `PUT /api/reports/{id}`
- **权限**: `report:update` 或 ADMIN角色
- **描述**: 更新指定ID的报告
- **路径参数**: `id` - 报告ID
- **请求体**: 同创建报告
- **响应**:
```json
{
  "code": 200,
  "message": "报告更新成功",
  "data": {
    "id": 8,
    "name": "更新后的报告名称",
    "description": "更新后的报告描述",
    "reportType": "1",
    "templateId": 5,
    "templateName": "效率分析模板",
    "content": "{\"sections\": [{\"title\": \"概述\", \"content\": \"更新后的内容\"}]}",
    "styleConfig": "{\"theme\": \"blue\", \"fontSize\": 14}",
    "generateType": "MANUAL",
    "generateFrequency": "DAILY",
    "nextGenerateTime": "2026-04-22T09:00:00",
    "dataSourceIds": "[1,2,3]",
    "chartIds": "[10,11,12]",
    "status": "PUBLISHED",
    "isScheduled": true,
    "isNotified": true,
    "exportCount": 3,
    "lastExportTime": "2026-04-21T15:30:00",
    "createName": "admin",
    "createTime": "2026-04-21T19:53:58",
    "updateTime": "2026-04-21T20:15:30"
  },
  "timestamp": 1776772530000,
  "success": true
}
```

### 1.3 删除报告
- **接口**: `DELETE /api/reports/{id}`
- **权限**: `report:delete` 或 ADMIN角色
- **描述**: 删除指定ID的报告（软删除）
- **路径参数**: `id` - 报告ID
- **响应**:
```json
{
  "code": 200,
  "message": "报告删除成功",
  "data": null,
  "timestamp": 1776772600000,
  "success": true
}
```

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
      "name": "月度效率报告",
      "description": "4月份模板执行效率分析报告",
      "reportType": "效率分析",
      "templateId": 5,
      "templateName": "效率分析模板",
      "generateType": "AUTO",
      "status": "PUBLISHED",
      "isScheduled": true,
      "exportCount": 12,
      "lastExportTime": "2026-04-21T15:30:00",
      "createName": "admin",
      "createTime": "2026-04-20T10:30:00",
      "updateTime": "2026-04-21T14:20:00"
    },
    {
      "id": 2,
      "name": "协议类型分布报告",
      "description": "协议测试类型分布统计报告",
      "reportType": "统计分析",
      "templateId": 3,
      "templateName": "统计分析模板",
      "generateType": "MANUAL",
      "status": "DRAFT",
      "isScheduled": false,
      "exportCount": 0,
      "lastExportTime": null,
      "createName": "user1",
      "createTime": "2026-04-21T09:15:00",
      "updateTime": "2026-04-21T09:15:00"
    }
  ],
  "timestamp": 1776772650000,
  "success": true
}
```

### 1.5 获取报告详情
- **接口**: `GET /api/reports/{id}`
- **权限**: `report:view` 或 ADMIN角色
- **描述**: 获取指定报告的详细信息
- **路径参数**: `id` - 报告ID
- **响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 8,
    "name": "月度效率报告",
    "description": "4月份模板执行效率分析报告",
    "reportType": "效率分析",
    "templateId": 5,
    "templateName": "效率分析模板",
    "content": "{\"sections\": [{\"title\": \"概述\", \"content\": \"本月共执行模板任务1500次，成功率95%\"}, {\"title\": \"详细分析\", \"content\": \"响应时间平均1200ms，最大并发50\"}]}",
    "styleConfig": "{\"theme\": \"blue\", \"fontSize\": 14, \"chartType\": \"bar\"}",
    "generateType": "AUTO",
    "generateFrequency": "MONTHLY",
    "nextGenerateTime": "2026-05-01T00:00:00",
    "dataSourceIds": "[1,2,3]",
    "chartIds": "[10,11,12]",
    "status": "PUBLISHED",
    "isScheduled": true,
    "isNotified": true,
    "exportCount": 12,
    "lastExportTime": "2026-04-21T15:30:00",
    "createName": "admin",
    "createTime": "2026-04-20T10:30:00",
    "updateTime": "2026-04-21T14:20:00"
  },
  "timestamp": 1776772700000,
  "success": true
}
```

### 1.6 自动生成报告
- **接口**: `POST /api/reports/auto-generate`
- **权限**: `report:auto-generate` 或 ADMIN角色
- **描述**: 根据配置自动生成报告
- **查询参数**:
  - `reportType` - 报告类型
  - `dataSourceIds` - 数据源ID列表
- **响应**:
```json
{
  "code": 200,
  "message": "自动生成报告成功",
  "data": {
    "id": 15,
    "name": "自动生成报告 - 2026-04-21T20:25:30.123456789",
    "description": "基于数据源[1,2,3]自动生成的效率分析报告",
    "reportType": "效率分析",
    "templateId": null,
    "templateName": null,
    "content": "{\"sections\": [{\"title\": \"执行统计\", \"content\": \"共执行任务1500次，成功率95.2%\"}, {\"title\": \"性能分析\", \"content\": \"平均响应时间1250ms，最大并发45\"}]}",
    "styleConfig": "{\"theme\": \"default\", \"fontSize\": 12}",
    "generateType": "AUTO",
    "generateFrequency": null,
    "nextGenerateTime": null,
    "dataSourceIds": "[1,2,3]",
    "chartIds": null,
    "status": "DRAFT",
    "isScheduled": false,
    "isNotified": false,
    "exportCount": 0,
    "lastExportTime": null,
    "createName": "system",
    "createTime": "2026-04-21T20:25:30",
    "updateTime": "2026-04-21T20:25:30"
  },
  "timestamp": 1776772730000,
  "success": true
}
```

### 1.7 预览报告
- **接口**: `POST /api/reports/preview`
- **权限**: `report:view` 或 ADMIN角色
- **描述**: 预览报告生成效果
- **请求体**: 报告配置信息
- **响应**:
```json
{
  "code": 200,
  "message": "预览生成成功",
  "data": {
    "previewId": "preview_1776772750000",
    "reportName": "月度效率报告预览",
    "reportType": "效率分析",
    "previewContent": "<div class='report-preview'><h1>月度效率报告预览</h1><p>基于数据源[1,2,3]生成的效率分析报告预览</p><section><h2>执行统计</h2><p>共执行任务1500次，成功率95.2%</p></section><section><h2>性能分析</h2><p>平均响应时间1250ms，最大并发45</p></section></div>",
    "styleConfig": "{\"theme\": \"blue\", \"fontSize\": 14}",
    "generateTime": "2026-04-21T20:26:30",
    "validDuration": 3600
  },
  "timestamp": 1776772750000,
  "success": true
}
```

### 1.8 导出报告
- **接口**: `GET /api/reports/{id}/export`
- **权限**: `report:export` 或 ADMIN角色
- **描述**: 导出报告为指定格式
- **路径参数**: `id` - 报告ID
- **查询参数**: `format` - 导出格式(PDF/EXCEL/WORD)
- **响应**:
```json
{
  "code": 200,
  "message": "报告导出成功",
  "data": {
    "exportId": "export_1776772800000",
    "reportId": 8,
    "reportName": "月度效率报告",
    "exportFormat": "PDF",
    "fileSize": 24576,
    "downloadUrl": "/api/reports/download/export_1776772800000.pdf",
    "exportTime": "2026-04-21T20:30:00",
    "exportStatus": "COMPLETED"
  },
  "timestamp": 1776772800000,
  "success": true
}
```

### 1.9 获取测试结果表格
- **接口**: `GET /api/reports/test-results/table`
- **权限**: `report:view` 或 ADMIN角色
- **描述**: 获取测试结果的表格数据
- **查询参数**:
  - `templateId` (可选) - 模板ID
  - `startTime` (可选) - 开始时间
  - `endTime` (可选) - 结束时间
- **响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 150,
    "records": [
      {
        "id": 1001,
        "templateId": 5,
        "templateName": "效率分析模板",
        "executeTime": "2026-04-21T10:30:00",
        "responseTime": 1250,
        "status": "SUCCESS",
        "responseCode": "200",
        "errorMessage": null,
        "protocolType": "HTTP",
        "dataSource": "JOB_LOG"
      },
      {
        "id": 1002,
        "templateId": 5,
        "templateName": "效率分析模板",
        "executeTime": "2026-04-21T10:32:15",
        "responseTime": 980,
        "status": "SUCCESS",
        "responseCode": "200",
        "errorMessage": null,
        "protocolType": "HTTPS",
        "dataSource": "UNIFIED"
      },
      {
        "id": 1003,
        "templateId": 3,
        "templateName": "统计分析模板",
        "executeTime": "2026-04-21T10:35:20",
        "responseTime": 2100,
        "status": "FAILED",
        "responseCode": "500",
        "errorMessage": "服务器内部错误",
        "protocolType": "FTP",
        "dataSource": "BATCH"
      }
    ],
    "summary": {
      "totalCount": 150,
      "successCount": 142,
      "failureCount": 8,
      "successRate": 94.67,
      "avgResponseTime": 1350
    }
  },
  "timestamp": 1776772850000,
  "success": true
}
```

### 1.10 获取时间线数据
- **接口**: `GET /api/reports/timeline`
- **权限**: `report:view` 或 ADMIN角色
- **描述**: 获取报告生成和执行的时间线数据
- **查询参数**:
  - `templateId` (可选) - 模板ID
  - `timeRange` (可选) - 时间范围
- **响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "timelineNodes": [
      {
        "id": "node_1",
        "type": "REPORT_GENERATED",
        "title": "月度效率报告生成",
        "description": "自动生成4月份效率分析报告",
        "timestamp": "2026-04-21T10:00:00",
        "reportId": 8,
        "reportName": "月度效率报告",
        "status": "SUCCESS",
        "duration": 1500,
        "dataSource": "JOB_LOG"
      },
      {
        "id": "node_2",
        "type": "TEMPLATE_EXECUTED",
        "title": "效率分析模板执行",
        "description": "执行效率分析模板，处理数据1500条",
        "timestamp": "2026-04-21T10:30:00",
        "templateId": 5,
        "templateName": "效率分析模板",
        "status": "SUCCESS",
        "duration": 1250,
        "dataSource": "UNIFIED"
      },
      {
        "id": "node_3",
        "type": "REPORT_EXPORTED",
        "title": "报告导出",
        "description": "导出月度效率报告为PDF格式",
        "timestamp": "2026-04-21T15:30:00",
        "reportId": 8,
        "reportName": "月度效率报告",
        "status": "SUCCESS",
        "format": "PDF",
        "fileSize": 24576
      }
    ],
    "timeRange": {
      "startTime": "2026-04-21T00:00:00",
      "endTime": "2026-04-21T23:59:59"
    },
    "summary": {
      "totalEvents": 3,
      "successEvents": 3,
      "failedEvents": 0,
      "avgDuration": 1333
    }
  },
  "timestamp": 1776772900000,
  "success": true
}
```

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
  - `name` (可选) - 模板名称（模糊搜索）

### 2.5 分页获取模板列表
- **接口**: `GET /api/report/templates/page`
- **权限**: `report:template:view` 或 ADMIN角色
- **描述**: 分页获取模板列表，支持按类型、公开状态和名称筛选
- **查询参数**:
  - `page` (可选, 默认: 1) - 当前页码
  - `size` (可选, 默认: 10) - 每页大小
  - `templateType` (可选) - 模板类型
  - `isPublic` (可选) - 是否公开
  - `name` (可选) - 模板名称（模糊搜索）
- **响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "模板名称",
        "templateType": "一致性",
        "description": "模板描述"
      }
    ],
    "total": 17,
    "size": 10,
    "current": 1,
    "pages": 2
  }
}
```

### 2.6 获取模板详情
- **接口**: `GET /api/report/templates/{id}`
- **权限**: `report:template:view` 或 ADMIN角色
- **描述**: 获取模板详细信息

### 2.7 XML模板导入
- **接口**: `POST /api/report/templates/import`
- **权限**: `report:template:import` 或 ADMIN角色
- **描述**: 导入XML格式的模板文件
- **请求类型**: `multipart/form-data`
- **参数**:
  - `file` - XML模板文件
  - `newName` (可选) - 新模板名称

### 2.8 XML模板导出
- **接口**: `GET /api/report/templates/{id}/export`
- **权限**: `report:template:export` 或 ADMIN角色
- **描述**: 导出模板为XML格式文件
- **响应**: XML文件下载

### 2.9 XML模板预览
- **接口**: `POST /api/report/templates/xml-preview`
- **权限**: `report:template:view` 或 ADMIN角色
- **描述**: 预览XML模板内容
- **请求体**: XML内容字符串

### 2.10 关联业务对象
- **接口**: `PUT /api/report/templates/{id}/relate`
- **权限**: `report:template:relate` 或 ADMIN角色
- **描述**: 关联模板与业务对象
- **查询参数**: `businessType` - 业务对象类型

### 2.11 获取模板使用记录
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

### 3.3 获取每2小时平均响应时间报告
- **接口**: `GET /api/report/template-statistics/response-time/hourly`
- **权限**: `report:statistics:view` 或 ADMIN角色
- **描述**: 获取每2小时平均响应时间统计报告，横坐标是每2小时，纵坐标是毫秒平均响应时间
- **查询参数**:
  - `startDate` - 开始日期
  - `endDate` - 结束日期
  - `dataSource` (可选, 默认: "JOB_LOG") - 数据源：JOB_LOG（定时任务）/UNIFIED（手动+定时）
- **响应**:
```json
{
  "id": 1776760706888,
  "name": "每2小时平均响应时间报告",
  "description": "基于协议测试记录生成的每2小时平均响应时间统计报告",
  "content": {
    "hourlyData": [
      {
        "timeSlot": "00:00-02:00",
        "avgResponseTime": 1200,
        "testCount": 50
      }
    ]
  }
}
```

### 3.4 获取周一到周日执行量统计报告
- **接口**: `GET /api/report/template-statistics/execution/weekly`
- **权限**: `report:statistics:view` 或 ADMIN角色
- **描述**: 获取周一到周日执行量统计报告，横坐标是周一到周日，纵坐标是执行次数
- **查询参数**:
  - `startDate` - 开始日期
  - `endDate` - 结束日期
  - `dataSource` (可选, 默认: "JOB_LOG") - 数据源：JOB_LOG（定时任务）/UNIFIED（手动+定时）
- **响应**:
```json
{
  "id": 1776760706889,
  "name": "周一到周日执行量统计报告",
  "description": "基于协议测试记录生成的周一到周日执行量统计报告",
  "content": {
    "weeklyData": [
      {
        "weekDay": "周一",
        "executionCount": 150,
        "successCount": 135
      }
    ]
  }
}
```

### 3.5 获取成功率分析报告
- **接口**: `GET /api/report/template-statistics/success-rate`
- **权限**: `report:statistics:view` 或 ADMIN角色
- **描述**: 获取成功率分析报告（成功失败占比）
- **查询参数**:
  - `startDate` - 开始日期
  - `endDate` - 结束日期
  - `dataSource` (可选, 默认: "JOB_LOG") - 数据源：JOB_LOG（定时任务）/UNIFIED（手动+定时）/BATCH（批量任务）
- **响应**:
```json
{
  "id": 1776760706890,
  "name": "成功率分析报告",
  "description": "基于协议测试记录生成的成功率分析报告",
  "content": {
    "successRate": 90.0,
    "successCount": 135,
    "failureCount": 15,
    "totalCount": 150
  }
}
```

### 3.6 获取协议类型分布统计报告
- **接口**: `GET /api/report/template-statistics/protocol-distribution`
- **权限**: `report:statistics:view` 或 ADMIN角色
- **描述**: 获取协议类型分布统计报告，支持三种统计维度
- **查询参数**:
  - `startDate` - 开始日期
  - `endDate` - 结束日期
  - `reportType` (可选, 默认: "CATEGORY") - 报告类型：CATEGORY（按协议名称）/DETAIL（按具体协议）/TEST_TYPE（按测试类型）
- **响应**:
```json
{
  "id": 1776760706891,
  "name": "协议类型分布统计报告",
  "description": "基于协议测试记录生成的协议类型分布统计报告",
  "content": {
    "categoryData": [
      {
        "category": "CAD数据同步协议",
        "categoryName": "CAD数据同步协议",
        "usageCount": 150,
        "successCount": 135,
        "failureCount": 15,
        "successRate": 90.0
      }
    ],
    "totalUsageCount": 270,
    "totalSuccessCount": 243,
    "overallSuccessRate": 90.0,
    "reportType": "CATEGORY",
    "reportTypeName": "按协议名称"
  }
}
```

**报告类型说明**:
- **CATEGORY**：按协议名称统计
- **DETAIL**：按具体协议统计（包含协议编码和名称）
- **TEST_TYPE**：按测试类型统计（连接测试/数据传输/综合测试）

### 3.7 获取前5失败原因分析报告
- **接口**: `GET /api/report/template-statistics/failure-reasons`
- **权限**: `report:statistics:view` 或 ADMIN角色
- **描述**: 获取前5的失败原因统计报告，分析协议测试记录中最常见的失败原因
- **查询参数**:
  - `startDate` - 开始日期
  - `endDate` - 结束日期
  - `dataSource` (可选, 默认: "JOB_LOG") - 数据源：JOB_LOG（定时任务）/UNIFIED（手动+定时）
- **响应**:
```json
{
  "id": 1776768562963,
  "name": "前5失败原因分析报告 - 2026-04-19 至 2026-04-22",
  "description": "基于协议测试记录生成的前5失败原因统计分析报告",
  "content": {
    "failureData": [
      {
        "failureReason": "连接超时",
        "failureCount": 15,
        "errorCode": "408",
        "protocolId": 1,
        "protocolName": "HTTP协议",
        "percentage": 30.0
      },
      {
        "failureReason": "认证失败",
        "failureCount": 12,
        "errorCode": "401",
        "protocolId": 2,
        "protocolName": "HTTPS协议",
        "percentage": 24.0
      },
      {
        "failureReason": "服务器内部错误",
        "failureCount": 8,
        "errorCode": "500",
        "protocolId": 3,
        "protocolName": "FTP协议",
        "percentage": 16.0
      },
      {
        "failureReason": "数据格式错误",
        "failureCount": 7,
        "errorCode": "400",
        "protocolId": 1,
        "protocolName": "HTTP协议",
        "percentage": 14.0
      },
      {
        "failureReason": "网络不可达",
        "failureCount": 8,
        "errorCode": "503",
        "protocolId": 4,
        "protocolName": "TCP协议",
        "percentage": 16.0
      }
    ],
    "summary": {
      "totalFailureCount": 50,
      "topFailureCount": 5,
      "startDate": "2026-04-19",
      "endDate": "2026-04-22"
    },
    "generateTime": "2026-04-21"
  }
}
```

**数据说明**:
- **failureReason**：失败原因描述
- **failureCount**：失败次数
- **errorCode**：错误码（HTTP状态码）
- **protocolId/protocolName**：关联的协议信息
- **percentage**：该失败原因在总失败次数中的占比

### 3.8 获取模板统计摘要
- **接口**: `GET /api/report/template-statistics/summary`
- **权限**: `report:statistics:view` 或 ADMIN角色
- **描述**: 获取模板统计摘要，包含使用频率和效率报告的综合信息
- **查询参数**:
  - `timeRange` (可选, 默认: "30DAYS") - 时间范围：TODAY/7DAYS/30DAYS/CUSTOM
- **响应**:
```json
{
  "reportName": "模板统计摘要",
  "timeRange": "30DAYS",
  "usageReportId": 1776672532387,
  "efficiencyReportId": 1776672532388,
  "reportTime": "2024-04-21T16:30:00"
}
```

### 3.9 生成模板使用图表
- **接口**: `GET /api/report/template-statistics/usage-chart`
- **权限**: `report:statistics:view` 或 ADMIN角色
- **描述**: 生成模板使用频率的图表数据
- **查询参数**:
  - `timeRange` - 时间范围
  - `chartType` - 图表类型

### 3.10 生成模板效率图表
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
- **描述**: 删除指定ID的图表

### 4.4 获取图表列表
- **接口**: `GET /api/report/charts`
- **权限**: `report:chart:view` 或 ADMIN角色
- **描述**: 获取图表列表，支持按类型筛选

### 4.5 获取图表详情
- **接口**: `GET /api/report/charts/{id}`
- **权限**: `report:chart:view` 或 ADMIN角色
- **描述**: 获取图表详细信息

### 4.6 生成图表数据
- **接口**: `GET /api/report/charts/{id}/data`
- **权限**: `report:chart:view` 或 ADMIN角色
- **描述**: 根据图表配置生成数据

---

## 📊 数据源说明

### 模板统计接口支持的数据源

| 数据源 | 描述 | 适用接口 |
|--------|------|----------|
| **JOB_LOG** | 定时任务数据源（template_job_log表） | 所有统计接口 |
| **UNIFIED** | 手动+定时统一数据源（template_execute_log表） | 所有统计接口 |
| **BATCH** | 批量任务数据源（template_job_batch表） | 使用频率、效率、成功率接口 |

### 协议统计接口支持的维度

| 统计维度 | 描述 | 适用接口 |
|----------|------|----------|
| **CATEGORY** | 按协议名称统计 | 协议类型分布统计 |
| **DETAIL** | 按具体协议统计（包含协议编码和名称） | 协议类型分布统计 |
| **TEST_TYPE** | 按测试类型统计（连接测试/数据传输/综合测试） | 协议类型分布统计 |

---

## 🔧 参数说明

### 时间范围参数
- **TODAY**：今天
- **7DAYS**：最近7天
- **30DAYS**：最近30天
- **CUSTOM**：自定义时间范围（需配合startDate和endDate使用）

### 日期格式
- 所有日期参数使用 `YYYY-MM-DD` 格式
- 例如：`2026-04-21`

### 分页参数
- `page`：当前页码，从1开始
- `size`：每页大小，默认10条

---

## 🚀 快速开始

### 获取模板列表（分页）
```bash
GET /api/report/templates/page?page=1&size=10
```
**功能说明**：分页获取报告模板列表，支持按类型、公开状态和名称模糊搜索
**参数说明**：
- `page`：页码，默认1
- `size`：每页大小，默认10
- `templateType`：模板类型筛选（可选）
  - `STATISTICAL`：统计类模板
  - `ANALYTICAL`：分析类模板
  - `ARCHIVAL`：归档类模板
  - `TEMPLATE_EFFICIENCY_STATISTICS`：效率统计模板
  - `一致性`：一致性测试模板
- `isPublic`：是否公开筛选（可选）
- `name`：模板名称模糊搜索（可选）

### 获取协议类型分布统计
```bash
GET /api/report/template-statistics/protocol-distribution?startDate=2026-04-19&endDate=2026-04-22&reportType=CATEGORY
```
**功能说明**：统计协议类型分布情况，支持三种统计维度
**参数说明**：
- `startDate`：开始日期（必填）
- `endDate`：结束日期（必填）
- `reportType`：统计维度（可选，默认CATEGORY）
  - `CATEGORY`：按协议名称统计
  - `DETAIL`：按具体协议统计
  - `TEST_TYPE`：按测试类型统计
- `dataSource`：数据源（可选，默认JOB_LOG）
  - `JOB_LOG`：定时任务数据源（template_job_log表）
  - `UNIFIED`：手动+定时统一数据源（template_execute_log表）
  - `BATCH`：批量任务数据源（template_job_batch表）

### 获取每2小时响应时间统计
```bash
GET /api/report/template-statistics/response-time/hourly?startDate=2026-04-19&endDate=2026-04-22
```
**功能说明**：统计每2小时的平均响应时间，用于性能监控
**参数说明**：
- `startDate`：开始日期（必填）
- `endDate`：结束日期（必填）
- `dataSource`：数据源（可选，默认JOB_LOG）
  - `JOB_LOG`：定时任务数据源（template_job_log表）
  - `UNIFIED`：手动+定时统一数据源（template_execute_log表）
  - `BATCH`：批量任务数据源（template_job_batch表）

### 获取成功率分析
```bash
GET /api/report/template-statistics/success-rate?startDate=2026-04-19&endDate=2026-04-22&dataSource=JOB_LOG
```
**功能说明**：分析成功失败占比，生成饼图数据
**参数说明**：
- `startDate`：开始日期（必填）
- `endDate`：结束日期（必填）
- `dataSource`：数据源（可选，默认JOB_LOG）
  - `JOB_LOG`：定时任务数据源（template_job_log表）
  - `UNIFIED`：手动+定时统一数据源（template_execute_log表）
  - `BATCH`：批量任务数据源（template_job_batch表）

### 获取前5失败原因分析
```bash
GET /api/report/template-statistics/failure-reasons?startDate=2026-04-19&endDate=2026-04-22
```
**功能说明**：分析最常见的5个失败原因，用于故障排查
**参数说明**：
- `startDate`：开始日期（必填）
- `endDate`：结束日期（必填）
- `dataSource`：数据源（可选，默认JOB_LOG）
  - `JOB_LOG`：定时任务数据源（template_job_log表）
  - `UNIFIED`：手动+定时统一数据源（template_execute_log表）
  - `BATCH`：批量任务数据源（template_job_batch表）