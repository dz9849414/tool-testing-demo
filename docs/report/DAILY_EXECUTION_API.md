# DAILY_EXECUTION（日执行量）相关接口文档

## 概述

本文档汇总了所有与 **日执行量统计（DAILY_EXECUTION）** 相关的 API 接口，包括接口路径、请求参数、响应示例等信息。

---

## 接口列表

### 1. 获取日执行量统计报告

**接口路径**: `GET /api/report/template-statistics/daily-execution`

**功能描述**: 获取指定日期范围内的日执行量统计报告（按天统计）

**请求参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| startDate | String | 是 | - | 开始日期，格式：`yyyy-MM-dd` |
| endDate | String | 是 | - | 结束日期，格式：`yyyy-MM-dd` |
| dataSource | String | 否 | EXECUTE_LOG | 数据源类型：`JOB_LOG`/`UNIFIED`/`EXECUTE_LOG` |

**请求示例**:
```
GET /api/report/template-statistics/daily-execution?startDate=2026-05-04&endDate=2026-05-11&dataSource=JOB_LOG
```

**响应示例**:
```json
{
  "code": 200,
  "message": "日执行量统计报告获取成功",
  "data": {
    "id": 1747123456789,
    "name": "日执行量统计报告 - 定时任务 - 2026-05-04 至 2026-05-11",
    "description": "基于定时任务生成的日执行量统计报告（按天统计）",
    "content": [
      {
        "startDate": "2026-05-04",
        "endDate": "2026-05-11",
        "dayData": [
          {"date": "2026-05-04", "dayLabel": "4日", "dayOfWeek": "星期日", "executionCount": 156, "typeDetails": []},
          {"date": "2026-05-05", "dayLabel": "5日", "dayOfWeek": "星期一", "executionCount": 234, "typeDetails": []},
          {"date": "2026-05-06", "dayLabel": "6日", "dayOfWeek": "星期二", "executionCount": 189, "typeDetails": []},
          {"date": "2026-05-07", "dayLabel": "7日", "dayOfWeek": "星期三", "executionCount": 210, "typeDetails": []},
          {"date": "2026-05-08", "dayLabel": "8日", "dayOfWeek": "星期四", "executionCount": 198, "typeDetails": []},
          {"date": "2026-05-09", "dayLabel": "9日", "dayOfWeek": "星期五", "executionCount": 176, "typeDetails": []},
          {"date": "2026-05-10", "dayLabel": "10日", "dayOfWeek": "星期六", "executionCount": 98, "typeDetails": []},
          {"date": "2026-05-11", "dayLabel": "11日", "dayOfWeek": "星期日", "executionCount": 145, "typeDetails": []}
        ],
        "summary": {
          "totalExecutions": 1406,
          "avgExecutionsPerDay": 175.75,
          "days": 8,
          "peakDate": "2026-05-05",
          "peakExecutions": 234,
          "lowestDate": "2026-05-10",
          "lowestExecutions": 98
        }
      }
    ],
    "createTime": "2026-05-11T14:30:00"
  }
}
```

---

### 2. 获取统计对比报告（支持日执行量）

**接口路径**: `POST /api/report/template-statistics/compare`

**功能描述**: 获取两组时间范围的统计数据对比报告，支持日执行量对比

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reportType | String | 是 | 报告类型：`DAILY_EXECUTION` |
| dataSource | String | 否 | 数据源类型：`JOB_LOG`/`UNIFIED`/`EXECUTE_LOG` |
| group1StartDate | String | 是 | 对比组1开始日期 |
| group1EndDate | String | 是 | 对比组1结束日期 |
| group2StartDate | String | 是 | 对比组2开始日期 |
| group2EndDate | String | 是 | 对比组2结束日期 |

**请求示例**:
```json
POST /api/report/template-statistics/compare
{
  "reportType": "DAILY_EXECUTION",
  "dataSource": "JOB_LOG",
  "group1StartDate": "2026-05-01",
  "group1EndDate": "2026-05-07",
  "group2StartDate": "2026-05-08",
  "group2EndDate": "2026-05-14"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "对比报告生成成功",
  "data": {
    "reportType": "DAILY_EXECUTION",
    "group1Label": "2026-05-01 至 2026-05-07",
    "group2Label": "2026-05-08 至 2026-05-14",
    "data1": [
      {"name": "1日", "value": 156},
      {"name": "2日", "value": 234},
      {"name": "3日", "value": 189},
      {"name": "4日", "value": 210},
      {"name": "5日", "value": 198},
      {"name": "6日", "value": 176},
      {"name": "7日", "value": 98}
    ],
    "data2": [
      {"name": "8日", "value": 145},
      {"name": "9日", "value": 201},
      {"name": "10日", "value": 187},
      {"name": "11日", "value": 222},
      {"name": "12日", "value": 193},
      {"name": "13日", "value": 167},
      {"name": "14日", "value": 102}
    ],
    "summary1": {
      "totalExecutions": 1261,
      "avgExecutionsPerDay": 180.14,
      "days": 7
    },
    "summary2": {
      "totalExecutions": 1217,
      "avgExecutionsPerDay": 173.86,
      "days": 7
    }
  }
}
```

---

### 3. 导出报告（支持日执行量报告）

**接口路径**: `GET /api/reports/{id}/export`

**功能描述**: 导出指定报告为 PDF/DOCX/XLSX/HTML/JSON 格式

**请求参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | Long | 是 | - | 报告ID（路径参数） |
| format | String | 否 | pdf | 导出格式：`pdf`/`docx`/`xlsx`/`html`/`json` |
| pageRange | String | 否 | all | 页码范围，如 `1-5` |

**请求示例**:
```
GET /api/reports/18/export?format=pdf&pageRange=all
```

**响应**: 文件下载流

---

### 4. 获取报告统计（支持日执行量）

**接口路径**: `GET /api/reports/statistics`

**功能描述**: 获取指定类型报告的统计信息

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startTime | String | 否 | 开始时间 |
| endTime | String | 否 | 结束时间 |
| reportType | String | 是 | 报告类型：`DAILY_EXECUTION` |

**请求示例**:
```
GET /api/reports/statistics?startTime=2026-05-01&endTime=2026-05-11&reportType=DAILY_EXECUTION
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalReports": 5,
    "totalExports": 12,
    "avgExecutionCount": 175.5,
    "peakDate": "2026-05-05",
    "peakCount": 234
  }
}
```

---

### 5. 自动生成报告（支持日执行量）

**接口路径**: `POST /api/reports/auto-generate`

**功能描述**: 自动生成指定类型的报告

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reportType | String | 是 | 报告类型：`DAILY_EXECUTION` |
| dataSourceIds | String | 否 | 数据源ID列表 |
| reportName | String | 否 | 报告名称 |
| reportDescription | String | 否 | 报告描述 |
| timeRange | String | 否 | 时间范围 |
| templateId | Long | 否 | 模板ID |

**请求示例**:
```json
POST /api/reports/auto-generate
{
  "reportType": "DAILY_EXECUTION",
  "dataSourceIds": "1,2,3",
  "reportName": "2026年5月日执行量报告",
  "reportDescription": "2026年5月每日执行量统计分析",
  "timeRange": "2026-05-01~2026-05-31"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "报告生成成功",
  "data": 123
}
```

---
### 6. 图表数据分析接口（已支持 DAILY_EXECUTION）

**接口路径**: `GET /api/report/charts/{id}/analyze`

**功能描述**: 图表数据分析接口，**已支持** `DAILY_EXECUTION` 类型数据

**说明**:
- 当 `id=0` 时，会查询所有报告进行统计，并**自动调用日执行量统计接口**获取 `DAILY_EXECUTION` 数据
- 返回的数据包含：`WEEKLY_EXECUTION`、`SUCCESS_RATE`、`RESPONSE_TIME`、`FAILURE_REASONS`、`DAILY_EXECUTION` 等类型

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 图表ID（当`id=0`时按时间范围查询并包含DAILY_EXECUTION数据） |
| startTime | String | 否 | 开始时间，格式：`yyyy-MM-dd HH:mm:ss` |
| endTime | String | 否 | 结束时间，格式：`yyyy-MM-dd HH:mm:ss` |
| timeRange | String | 否 | 时间范围：`TODAY`/`YESTERDAY`/`THIS_WEEK`/`THIS_MONTH` 等 |

**调用示例**:
```
GET /api/report/charts/0/analyze?startTime=2026-05-10%2000:00:00&endTime=2026-05-16%2023:59:59&timeRange=THIS_WEEK
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "chartId": 0,
    "analysisResult": "时间范围数据分析完成",
    "chartData": {
      "xAxis": ["WEEKLY_EXECUTION", "SUCCESS_RATE", "DAILY_EXECUTION"],
      "data": [
        {"name": "WEEKLY_EXECUTION", "value": 5, "color": "#5470c6"},
        {"name": "SUCCESS_RATE", "value": 3, "color": "#91cc75"},
        {"name": "DAILY_EXECUTION", "value": 1, "color": "#fac858", "totalCount": 1406}
      ],
      "title": "时间范围分析 - 2026-05-10 至 2026-05-16",
      "type": "BAR",
      "reportCount": 9,
      "reportTypes": 3
    },
    "message": "时间范围数据分析完成",
    "timePeriod": "2026-05-10 至 2026-05-16",
    "dataSourceType": "TIME_RANGE",
    "mode": "time_range_only"
  }
}
```

---

### 7. 获取报告列表（支持 DAILY_EXECUTION 筛选）

**接口路径**: `GET /api/reports`

**功能描述**: 获取报告列表，支持按类型筛选（包括 `DAILY_EXECUTION`）

**请求参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |
| reportType | String | 否 | - | 报告类型：`DAILY_EXECUTION`/`WEEKLY_EXECUTION`/`SUCCESS_RATE` 等 |
| status | String | 否 | - | 报告状态：`DRAFT`/`PUBLISHED`/`ARCHIVED` |

**调用示例**:
```
GET /api/reports?pageNum=1&pageSize=10&reportType=DAILY_EXECUTION
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 18,
        "name": "日执行量统计报告 - 2026-05-04 至 2026-05-11",
        "description": "基于EXECUTE_LOG生成的日执行量统计报告",
        "reportType": "DAILY_EXECUTION",
        "status": "PUBLISHED",
        "createTime": "2026-05-11T14:30:00",
        "updateTime": "2026-05-11T14:30:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10,
    "totalPages": 1
  }
}
```

---

## 数据源类型说明

| 数据源类型 | 说明 |
|------------|------|
| `JOB_LOG` | 仅统计定时任务执行日志 |
| `UNIFIED` | 统计手动执行 + 定时任务执行日志 |
| `EXECUTE_LOG` | 统计所有执行日志（默认） |

---

## 报告模板信息

日执行量报告对应的系统模板信息：

| 模板ID | 模板名称 | 模板类型 | 状态 |
|--------|----------|----------|------|
| 18 | 日执行量标准模板 | DAILY_EXECUTION | 启用 |

---

---

**文档版本**: v1.0  
**生成日期**: 2026-05-11