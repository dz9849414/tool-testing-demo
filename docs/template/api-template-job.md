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

## 7. 异步批量触发

立即返回 `batchId`，后台异步执行。适合任务数量多、不希望接口阻塞的场景。

```http
POST /api/template/job/batch/trigger-async
```

### Request Body

```json
[1, 2, 3]
```

`Long[]` 任务 ID 数组。

### 幂等说明

- 同一批 `ids`（排序后）如果已有任务在 **PENDING / RUNNING** 状态，会抛出异常：
  > `异步批量任务重复提交, 返回已有batchId=xxx`

### 响应 `data`

`String`：`batchId`

### 异步流程

1. 写入 DB 状态为 **PENDING**
2. 线程池中异步执行，状态变为 **RUNNING**
3. 逐个同步调用 `triggerJob(id)`
4. 全部完成后状态变为 **DONE**，结果以 JSON 形式保存
5. 若整体超过 **5 分钟**，状态变为 **FAILED**，结果为 `执行超时`

---

## 8. 查询异步批量触发状态

```http
GET /api/template/job/batch/{batchId}/status
```

### Path 参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `batchId` | String | 异步批次 ID |

### 响应 `data` 结构

```json
{
  "status": "DONE",
  "details": {
    "successIds": [1, 2],
    "failIds": [3],
    "details": {
      "1": { "success": true, ... },
      "3": { "success": false, "message": "..." }
    }
  }
}
```

| `status` 值 | 含义 |
|-------------|------|
| `PENDING` | 等待执行 |
| `RUNNING` | 执行中 |
| `DONE` | 执行完成（`details` 有完整结果） |
| `FAILED` | 执行失败或超时 |
| `NOT_FOUND` | batchId 不存在 |

---

## 9. 手动触发执行（单个）

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

## 10. 分页查询任务执行日志

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

## 11. 查询任务最近日志

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

### 自动调度保护机制

1. **启动清空**：应用启动时先 `cancelAllJobs()`，防止热重启导致重复注册。
2. **单机锁**：`ReentrantLock` 保证同一 `jobId` 在单个 JVM 内不会并发执行。
3. **连续失败自动停用**：自动调度连续失败 **3 次** 后，自动取消调度并将数据库状态改为停用。
4. **子任务超时**：`JobDispatcher` 对每个子项设置毫秒级超时（默认 120 秒），防止子任务永久挂起导致锁不释放。
