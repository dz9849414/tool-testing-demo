# 模板定时任务接口文档

> **Base URL**: `/api/template/job`
>
> 本文档对应 `TemplateJobController`，涵盖模板定时任务的 CRUD、手动/自动/异步触发、日志查询等全部接口。

---

## 统一响应格式

所有接口均返回 `Result<T>` 封装：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1713330000000
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | Integer | `200` 成功，`500` 或其他为失败 |
| `message` | String | 提示信息 |
| `data` | T | 业务数据，类型随接口变化 |
| `timestamp` | Long | 响应时间戳 |

---

## 1. 分页查询任务列表（带最近执行状态）

**推荐管理页面使用**，可直接展示最近一次执行结果。

```http
GET /api/template/job/page-with-last-log
```

### Query 参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `current` | Long | 否 | 1 | 当前页码 |
| `size` | Long | 否 | 10 | 每页条数 |
| `keyword` | String | 否 | - | 按任务名称模糊搜索 |
| `status` | Integer | 否 | - | 任务状态：`0` 停用，`1` 启用 |

### 响应 `data` 结构

`IPage<TemplateJobListVO>`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 任务 ID |
| `jobName` | String | 任务名称 |
| `cronExpression` | String | Cron 表达式 |
| `status` | Integer | 状态：`0` 停用，`1` 启用 |
| `description` | String | 任务描述 |
| `lastExecuteTime` | String | 上次执行时间（yyyy-MM-dd HH:mm:ss） |
| `lastExecuteSuccess` | Integer | 最近是否成功：`0` 否，`1` 是，`null` 未执行 |
| `lastExecuteDurationMs` | Long | 最近执行耗时（ms） |
| `lastExecuteSummary` | String | 结果摘要，如 `2个成功, 1个失败` |
| `createTime` | String | 创建时间（yyyy-MM-dd HH:mm:ss） |

---

## 2. 获取任务详情

```http
GET /api/template/job/{id}
```

### Path 参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 任务 ID |

### 响应 `data` 结构

`TemplateJob`，包含 `items` 子项列表。

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 任务 ID |
| `jobName` | String | 任务名称 |
| `cronExpression` | String | Cron 表达式 |
| `status` | Integer | 状态：`0` 停用，`1` 启用 |
| `description` | String | 描述 |
| `lastExecuteTime` | String | 上次执行时间 |
| `createId` / `createName` | - | 创建人信息 |
| `createTime` / `updateTime` | String | 创建/更新时间 |
| `isDeleted` | Integer | 是否删除：`0` 否，`1` 是 |
| `items` | List<TemplateJobItem> | 任务子项（模板配置） |

### `TemplateJobItem` 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 子项 ID |
| `jobId` | Long | 所属任务 ID |
| `templateId` | Long | 关联模板 ID |
| `environmentId` | Long | 关联环境 ID |
| `variables` | String | 执行变量（JSON 格式） |
| `sortOrder` | Integer | 执行顺序 |
| `status` | Integer | 子项状态：`0` 停用，`1` 启用 |

---

## 3. 创建任务

```http
POST /api/template/job
```

### Request Body

`TemplateJob`（必须包含 `items` 列表，至少 1 个子项）。

```json
{
  "jobName": "每日接口巡检",
  "cronExpression": "0 0 9 * * ?",
  "status": 1,
  "description": "每天早上9点执行",
  "items": [
    {
      "templateId": 1,
      "environmentId": 2,
      "variables": "{\"token\":\"xxx\"}",
      "status": 1
    },
    {
      "templateId": 3,
      "environmentId": 2,
      "status": 1
    }
  ]
}
```

### 说明

- `status` 不传时，默认启用（`1`）。
- `items` 为空会抛出业务异常：`任务模板列表不能为空`。
- 创建成功后，若状态为启用，会**立即注册 Cron 调度**。

### 响应 `data`

创建后的 `TemplateJob`（含详情和子项）。

---

## 4. 更新任务

```http
PUT /api/template/job/{id}
```

### Path 参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 任务 ID |

### Request Body

同创建任务，需传完整 `TemplateJob`（包含全部 `items`）。

> ⚠️ **注意**：更新时会先删除旧 `items`，再重新插入传入的 `items`。如果传入空列表，事务会回滚并报错。

### 说明

- 更新成功后，若状态仍为启用，会**重新注册 Cron 调度**（先取消旧调度）。
- 若状态改为停用，则只取消调度，不再注册。

---

## 5. 删除任务

```http
DELETE /api/template/job/{id}
```

### Path 参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 任务 ID |

### 说明

- 软删除（`is_deleted` 置为 `1`）。
- 同时**取消该任务的 Cron 调度**。
- 删除成功后，关联的 `items` 也会被物理删除。

### 响应示例

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 6. 批量停止任务

停用任务并取消 Cron 调度。

```http
POST /api/template/job/batch/stop
```

### Request Body

```json
[1, 2, 3]
```

`Long[]` 任务 ID 数组。

### 响应 `data` 结构

```json
{
  "successIds": [1],
  "failIds": [2],
  "details": {
    "1": "stopped",
    "2": "update_failed"
  }
}
```

---

## 7. 异步执行模型总览

页面中的以下按钮，统一建议走“异步批次执行”模型：

| 页面功能 | 推荐接口 |
|------|------|
| 单条执行 | `POST /api/template/job/batch/trigger-async`，请求体传 `[jobId]` |
| 批量执行 | `POST /api/template/job/batch/trigger-async`，请求体传 `[jobId1, jobId2, ...]` |
| 执行进度显示 | `GET /api/template/job/batch/{batchId}/progress` |
| 批量暂停 / 单条暂停 | `POST /api/template/job/batch/{batchId}/pause` |
| 批量恢复 / 单条恢复 | `POST /api/template/job/batch/{batchId}/resume` |
| 批量取消 / 单条取消 | `POST /api/template/job/batch/{batchId}/cancel` |
| 批量重试失败项 | `POST /api/template/job/batch/{batchId}/retry-failed` |
| 单条重试 | 失败后可重新调用 `trigger-async`，请求体 `[jobId]`；或对单任务批次调用 `retry-failed` |

前端关键点：

1. 执行时先拿到 `batchId`
2. 将 `jobId -> batchId` 暂存在页面状态或本地缓存
3. 轮询 `/progress`
4. 暂停、恢复、取消、失败重试都基于 `batchId`

---

## 8. 异步批量触发

立即返回 `batchId`，后台异步执行。适合单条执行和批量执行两种场景。

```http
POST /api/template/job/batch/trigger-async
```

### Request Body

```json
[1, 2, 3]
```

`Long[]` 任务 ID 数组。  
如果是单条执行，直接传：

```json
[6]
```

### 幂等说明

- 同一批 `ids`（排序后）如果已有任务在执行中，会抛出异常：
  > `异步批量任务重复提交, 返回已有batchId=xxx`

### 响应 `data`

```json
"e8c31a0d-ff7f-498d-8f82-0fe6f7b6b234"
```

即：`batchId`

---

## 9. 查询异步批量执行进度

```http
GET /api/template/job/batch/{batchId}/progress
```

### Path 参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `batchId` | String | 异步批次 ID |

### 响应 `data` 结构

```json
{
  "batchId": "e8c31a0d-ff7f-498d-8f82-0fe6f7b6b234",
  "sourceBatchId": null,
  "status": "RUNNING",
  "totalCount": 3,
  "completedCount": 1,
  "successCount": 1,
  "failCount": 0,
  "pendingCount": 2,
  "progressPercent": 33,
  "nextIndex": 1,
  "currentJobId": 6,
  "currentJobName": "CAD获取报文",
  "jobIds": [7, 6, 3],
  "successIds": [7],
  "failIds": [],
  "details": {
    "7": {
      "success": true,
      "message": "全部成功"
    }
  },
  "message": "批次执行中",
  "canPause": true,
  "canResume": false,
  "canCancel": true,
  "canRetryFailed": false
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `batchId` | String | 当前批次 ID |
| `sourceBatchId` | String/null | 若是失败重试批次，这里是原始批次 ID |
| `status` | String | 批次状态 |
| `totalCount` | Integer | 总任务数 |
| `completedCount` | Integer | 已完成数 |
| `successCount` | Integer | 成功数 |
| `failCount` | Integer | 失败数 |
| `pendingCount` | Integer | 未完成数 |
| `progressPercent` | Integer | 进度百分比，0-100 |
| `nextIndex` | Integer | 下一个待执行索引，从 0 开始 |
| `currentJobId` | Long/null | 当前正在执行的任务 ID |
| `currentJobName` | String/null | 当前正在执行的任务名称 |
| `jobIds` | List<Long> | 本批次任务 ID 列表 |
| `successIds` | List<Long> | 已成功任务 ID 列表 |
| `failIds` | List<Long> | 已失败任务 ID 列表 |
| `details` | Map | 每个任务的执行结果明细 |
| `message` | String | 状态提示语 |
| `canPause` | Boolean | 前端是否展示“暂停”按钮 |
| `canResume` | Boolean | 前端是否展示“恢复”按钮 |
| `canCancel` | Boolean | 前端是否展示“取消”按钮 |
| `canRetryFailed` | Boolean | 前端是否展示“失败重试”按钮 |

### `status` 枚举值

| 值 | 含义 |
|------|------|
| `PENDING` | 已提交，待执行 |
| `RUNNING` | 执行中 |
| `PAUSED` | 已暂停 |
| `CANCELED` | 已取消 |
| `DONE` | 已完成 |
| `FAILED` | 执行异常 |
| `NOT_FOUND` | 批次不存在 |

### 兼容说明

老接口仍可用：

```http
GET /api/template/job/batch/{batchId}/status
```

当前已兼容返回与 `/progress` 基本一致的数据结构，前端新接入建议统一使用 `/progress`。

---

## 10. 暂停异步批量执行

```http
POST /api/template/job/batch/{batchId}/pause
```

### 说明

- 温和暂停，不会强杀当前已开始执行的任务
- 当前任务执行完后，批次状态变为 `PAUSED`

### 响应示例

```json
{
  "code": 200,
  "message": "暂停请求已提交",
  "data": {
    "batchId": "e8c31a0d-ff7f-498d-8f82-0fe6f7b6b234",
    "status": "RUNNING",
    "message": "暂停请求已记录，将在当前任务完成后暂停"
  }
}
```

---

## 11. 取消异步批量执行

```http
POST /api/template/job/batch/{batchId}/cancel
```

### 说明

- 如果批次处于 `RUNNING`，则会在当前任务完成后取消
- 如果批次处于 `PAUSED`，会直接转为 `CANCELED`

---

## 12. 恢复已暂停的异步批量执行

```http
POST /api/template/job/batch/{batchId}/resume
```

### 说明

- 仅 `PAUSED` 状态可恢复
- 会从 `nextIndex` 对应的未执行任务继续往下执行

### 响应示例

```json
{
  "code": 200,
  "message": "恢复请求已提交",
  "data": {
    "batchId": "e8c31a0d-ff7f-498d-8f82-0fe6f7b6b234",
    "status": "RUNNING",
    "message": "批次恢复执行已提交"
  }
}
```

---

## 13. 重试失败的批量执行项

```http
POST /api/template/job/batch/{batchId}/retry-failed
```

### 说明

- 仅重试原批次中失败的任务 ID
- 会生成一个新的 `retryBatchId`
- 新批次的 `sourceBatchId` 指向原始批次

### 响应示例

```json
{
  "code": 200,
  "message": "重试任务已提交",
  "data": {
    "batchId": "0d6c4632-5e56-49a4-8446-27e6b7012c85",
    "sourceBatchId": "e8c31a0d-ff7f-498d-8f82-0fe6f7b6b234",
    "status": "PENDING",
    "message": "失败项重试批次已提交"
  }
}
```

---

## 14. 手动触发执行（单个，同步）

```http
POST /api/template/job/{id}/trigger
```

### Path 参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 任务 ID |

### 说明

- 立即执行指定任务，不走 Cron 调度。
- 若任务已停用，会抛出异常。
- **单机锁保护**：同一任务如果在自动调度中正在执行，手动触发会返回 `"任务正在执行"`。

### 响应 `data` 结构

```json
{
  "success": true,
  "results": [
    { "success": true, "templateId": 1, ... },
    { "success": false, "templateId": 3, "message": "..." }
  ],
  "message": "全部成功"
}
```

- 执行成功时 `code = 200`
- 执行失败时 `code = 500`，但 `data` 里仍有详细结果

---

## 15. 分页查询任务执行日志

```http
GET /api/template/job/logs/page
```

### Query 参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `current` | Long | 否 | 1 | 页码 |
| `size` | Long | 否 | 10 | 每页条数 |
| `jobId` | Long | 否 | - | 按任务 ID 过滤 |
| `success` | Integer | 否 | - | 按是否成功过滤：`0` 失败，`1` 成功 |

### 响应 `data` 结构

`IPage<TemplateJobLogVO>`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 日志 ID |
| `jobId` | Long | 任务 ID |
| `jobName` | String | 任务名称（关联填充） |
| `templateId` | Long | 首个模板 ID |
| `success` | Integer | 是否成功：`0` 否，`1` 是 |
| `durationMs` | Long | 耗时（ms） |
| `errorMsg` | String | 错误信息 |
| `resultSummary` | String | 结果摘要，如 `2个成功, 1个失败` |
| `results` | List<TemplateJobLogItemVO> | 各子项详细结果 |
| `createTime` | String | 执行时间（yyyy-MM-dd HH:mm:ss） |

### `TemplateJobLogItemVO` 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `templateId` | Long | 模板 ID |
| `templateName` | String | 模板名称 |
| `success` | Boolean | 是否成功 |
| `statusCode` | Integer | HTTP 状态码 |
| `durationMs` | Long | 单模板耗时 |
| `message` | String | 结果消息 |
| `request` | Object | 请求信息（url、method、headers、body） |
| `response` | Object | 响应信息（statusCode、headers、body 等） |
| `assertions` | List<Object> | 断言结果列表 |
| `variables` | Object | 最终变量 |

---

## 16. 查询任务最近日志

```http
GET /api/template/job/{id}/logs
```

### Path 参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 任务 ID |

### Query 参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `limit` | Integer | 否 | 10 | 返回最近 N 条 |

### 响应 `data`

`List<TemplateJobLog>`，原始日志实体列表。

---

## 附录：核心状态码与业务规则

### 任务状态 (`status`)

| 值 | 含义 |
|----|------|
| `0` | 停用（不触发调度） |
| `1` | 启用（按 Cron 表达式自动调度） |

### 子项状态 (`status`)

| 值 | 含义 |
|----|------|
| `0` | 停用（执行时跳过） |
| `1` | 启用（正常执行） |

### 批次执行状态 (`batch.status`)

| 值 | 含义 |
|----|------|
| `PENDING` | 已提交待执行 |
| `RUNNING` | 执行中 |
| `PAUSED` | 已暂停 |
| `CANCELED` | 已取消 |
| `DONE` | 已完成 |
| `FAILED` | 执行异常 |

### 前端接入建议

#### 一、列表页“执行”按钮

1. 调用 `POST /api/template/job/batch/trigger-async`
2. 请求体传 `[jobId]`
3. 保存返回的 `batchId`
4. 立即弹出执行进度窗口，并轮询 `/progress`

#### 二、列表页“执行进度显示”

依赖前端已保存的 `batchId` 查询：

```http
GET /api/template/job/batch/{batchId}/progress
```

#### 三、列表页“暂停 / 恢复 / 取消”

统一基于 `batchId` 调用：

- 暂停：`POST /batch/{batchId}/pause`
- 恢复：`POST /batch/{batchId}/resume`
- 取消：`POST /batch/{batchId}/cancel`

#### 四、列表页“重试”

有两种方式：

- 简单重试：重新调 `POST /batch/trigger-async`，请求体 `[jobId]`
- 失败项重试：调 `POST /batch/{batchId}/retry-failed`

#### 五、顶部“批量执行 / 批量暂停 / 批量恢复 / 批量停止”

- 批量执行：`POST /batch/trigger-async`
- 批量暂停：对当前选中批次的 `batchId` 逐个调用 `/pause`
- 批量恢复：对当前选中批次的 `batchId` 逐个调用 `/resume`
- 批量停止：已有接口 `POST /batch/stop`，这是“停用任务配置”，不是“取消当前执行”

这两个动作不要混淆：

- `batch/stop`：修改任务配置状态为停用，后续不会自动调度
- `batch/{batchId}/cancel`：取消当前这一轮执行，不会改任务配置的启用/停用状态

### 自动调度保护机制

1. **启动清空**：应用启动时先 `cancelAllJobs()`，防止热重启导致重复注册。
2. **单机锁**：`ReentrantLock` 保证同一 `jobId` 在单个 JVM 内不会并发执行。
3. **连续失败自动停用**：自动调度连续失败 **3 次** 后，自动取消调度并将数据库状态改为停用。
4. **子任务超时**：`JobDispatcher` 对每个子项设置毫秒级超时（默认 120 秒），防止子任务永久挂起导致锁不释放。
