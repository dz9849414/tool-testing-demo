# API 文档 — 模板 (Template) 与 模板任务 (Template Job)

本文档为前端对接提供接口说明，包含：接口模板（Template）、模板执行（Execute）与模板定时任务（Template Job）相关的 REST API。文中示例基于工程中的 Controller 定义（路径以 /api 开头）。

---

## 约定

- 基础路径示例：http(s)://{host}:{port}
- 所有响应均使用统一封装：Result<T>
  - JSON 结构示例：{ "code": 200, "message": "操作成功", "data": {...}, "timestamp": 1680000000000 }
  - 成功时 `code` 为 200，失败通常返回 500 或其它自定义状态码
- 时间/格式说明：日期时间采用 ISO-like 字符串（若返回为 epoch/ms 请以实际返回为准）
- 鉴权：Controller 源码中没有显式注解声明鉴权策略。若系统在部署中使用鉴权（例如 Authorization: Bearer token），请在请求头中携带 `Authorization`。如果需要明确的权限或角色信息，请与后端确认。

---

## 快速示例

- GET 列表（模板分页）

  curl -X GET "{baseUrl}/api/template/page?current=1&size=10&folderId=1&keyword=test" \
    -H "Accept: application/json"

- 创建模板（POST /api/template）

  curl -X POST "{baseUrl}/api/template" \
    -H "Content-Type: application/json" \
    -d '{ "name": "示例模板", "path": "/api/test", "method": "POST" }'

- 触发模板执行（执行/预览/验证）

  curl -X POST "{baseUrl}/api/template/execute/123" -H "Content-Type: application/json" -d '{"environmentId":1, "variables":{}}'

- 模板任务（创建/触发/查询）

  curl -X POST "{baseUrl}/api/template/job" -H "Content-Type: application/json" -d '{ "jobName":"每日检查", "cronExpression":"0 0 2 * * ?", "items":[{"templateId":1}] }'

---

## 接口详述 — 模板（Interface Template）

Base path: /api/template

1. 分页查询模板列表
   - 方法：GET
   - 路径：/api/template/page
   - 参数（Query）:
     - current (Long, optional, default=1)
     - size (Long, optional, default=10)
     - folderId (Long, optional)
     - keyword (String, optional)
     - protocolType (String, optional)
     - status (Integer, optional)
   - 返回：Result<IPage<InterfaceTemplateVO>>（分页 VO 列表）

2. 获取模板详情
   - 方法：GET
   - 路径：/api/template/{id}
   - 参数：id (path)
   - 返回：Result<InterfaceTemplateVO>

3. 创建模板（保存为草稿）
   - 方法：POST
   - 路径：/api/template
   - 请求体：InterfaceTemplateDTO（JSON）
   - 返回：Result<InterfaceTemplateVO>

   InterfaceTemplateDTO 主要字段（用于创建/更新）示例：
   - id (Long) - 更新时传
   - folderId (Long)
   - name (String)
   - description (String)
   - protocolId (Long)
   - protocolType (String)
   - method (String)
   - baseUrl (String)
   - path (String)
   - authType (String)
   - authConfig (String) - JSON 字符串
   - contentType, charset, bodyType, bodyContent, variables (List)
   - headers (List<TemplateHeaderDTO>), parameters (List<TemplateParameterDTO>) 等关联数据

   简短示例请求体：
   {
     "name":"示例接口",
     "protocolType":"HTTP",
     "method":"POST",
     "baseUrl":"https://api.example.com",
     "path":"/v1/test",
     "contentType":"application/json",
     "bodyContent":"{\"a\":1}",
     "headers":[{"name":"X-Trace","value":"abc"}]
   }

4. 更新模板
   - 方法：PUT
   - 路径：/api/template/{id}
   - 请求体：InterfaceTemplateDTO
   - 返回：Result<String>（"更新成功"）

5. 删除模板
   - 方法：DELETE
   - 路径：/api/template/{id}
   - 返回：Result<String>

6. 批量删除模板
   - 方法：DELETE
   - 路径：/api/template/batch
   - 请求体：Long[]（模板ID数组）
   - 返回：Result<Map<String,Object>> （包含 successIds / failIds / successCount / failCount）

7. 复制模板
   - 方法：POST
   - 路径：/api/template/{id}/copy
   - 参数：newName (query)
   - 返回：Result<InterfaceTemplateVO>

8. 发布 / 归档 / 移动
   - 发布：PUT /api/template/{id}/publish
   - 归档：PUT /api/template/{id}/archive
   - 移动：PUT /api/template/{id}/move?folderId={folderId}
   - 返回：Result<String>

9. 草稿与审核流程
   - 保存草稿（新建）：POST /api/template/draft (InterfaceTemplateDTO)
   - 保存草稿（更新）：PUT /api/template/{id}/draft
   - 提交审核：POST /api/template/{id}/submit (InterfaceTemplateDTO)
   - 审核通过：PUT /api/template/{id}/approve
   - 审核驳回：PUT /api/template/{id}/reject?reason=...

10. 文件附件管理
    - 上传单个文件：POST /api/template/{id}/files (multipart form, param name=file, fileCategory, description)
    - 批量上传：POST /api/template/{id}/files/batch (param files)
    - 获取文件列表：GET /api/template/{id}/files
    - 删除文件：DELETE /api/template/files/{fileId}
    - 下载文件：GET /api/template/files/{fileId}/download

    注意：文件上传使用 multipart/form-data；返回为 TemplateFile 实体（包含 id、文件名、路径等字段）。

---

## 接口详述 — 模板执行（Template Execute）

Base path: /api/template/execute

1. 执行模板
   - 方法：POST
   - 路径：/api/template/execute/{templateId}
   - 请求体（可选）：{ "environmentId": Long, "variables": { ... } }
     - 在代码中内部类为 ExecuteRequest：{ environmentId: Long, variables: Map<String,Object> }
   - 返回：Result<Map<String,Object>>
     - 返回 data 中通常包含执行结果列表、状态信息和每步的 success 字段（具体格式由执行引擎决定）
   - 错误：若执行失败，返回 Result.error("执行失败") 或包含 message

2. 验证模板配置
   - 方法：GET
   - 路径：/api/template/execute/{templateId}/validate
   - 返回：Result<Map<String,Object>>（验证结果）

3. 预览请求
   - 方法：POST
   - 路径：/api/template/execute/{templateId}/preview
   - 请求体（ExecuteRequest 可选）
   - 返回：Result<Map<String,Object>>（生成的请求预览而不实际发送）

示例：

curl -X POST "{baseUrl}/api/template/execute/123" \
  -H "Content-Type: application/json" \
  -d '{ "environmentId": 1, "variables": { "id": 100 } }'

---

## 接口详述 — 模板定时任务（Template Job）

Base path: /api/template/job

1. 分页查询任务（基础）
   - 方法：GET
   - 路径：/api/template/job/page
   - 参数：current, size, keyword, status
   - 返回：Result<IPage<TemplateJob>>

2. 分页查询任务（附带最近一次执行日志摘要）
   - 方法：GET
   - 路径：/api/template/job/page-with-last-log
   - 参数：current, size, keyword, status
   - 返回：Result<IPage<TemplateJobListVO>>
     - VO 包含：id, jobName, cronExpression, status, description, lastExecuteTime, createTime, lastExecuteSuccess, lastExecuteDurationMs, lastExecuteSummary

3. 获取任务详情
   - 方法：GET
   - 路径：/api/template/job/{id}
   - 返回：Result<TemplateJob>（包含 items 子项）

   TemplateJob 字段示例：
   - id, jobName, cronExpression, status(0/1), description, xxlJobId, lastExecuteTime, createId, createName, createTime, updateTime, isDeleted
   - items: [ { id, jobId, templateId, environmentId, variables (JSON string), sortOrder, status } ]

4. 创建任务
   - 方法：POST
   - 路径：/api/template/job
   - 请求体：TemplateJob（JSON，请提供 items 数组）

   示例请求体：
   {
     "jobName": "每日巡检",
     "cronExpression": "0 0 3 * * ?",
     "status": 1,
     "description": "每日 3 点执行",
     "items": [
       { "templateId": 101, "environmentId": 1, "variables": "{\"a\":1}" },
       { "templateId": 102, "environmentId": 2 }
     ]
   }

   - 返回：Result<TemplateJob>（创建后详情）

5. 更新任务
   - 方法：PUT
   - 路径：/api/template/job/{id}
   - 请求体：TemplateJob（完整对象）
   - 返回：Result<TemplateJob>

6. 删除任务
   - 方法：DELETE
   - 路径：/api/template/job/{id}
   - 返回：Result<String>

7. 手动触发执行
   - 方法：POST
   - 路径：/api/template/job/{id}/trigger
   - 返回：Result<Map<String,Object>>
     - 当执行失败时，Controller 返回 Result.error(500, "执行失败", result)

8. 分页查询任务执行日志（结构化）
   - 方法：GET
   - 路径：/api/template/job/logs/page
   - 参数：current, size, jobId (可选), success (可选, 0/1)
   - 返回：Result<IPage<TemplateJobLogVO>>
     - TemplateJobLogVO 包含：id, jobId, templateId, success, durationMs, errorMsg, createTime, results (list of item results)

9. 查询任务最近日志
   - 方法：GET
   - 路径：/api/template/job/{id}/logs?limit=10
   - 返回：Result<List<TemplateJobLog>>（原始日志实体列表，其中 executeResult 为 JSON 字符串）

---

## 失败码与错误说明

- 通用结果：Result.error(message) 返回 code=500
- Controller 层部分方法在内部会包装异常并返回错误消息。如需更准确的 error code 与错误模型，请查看全局异常处理类（如果仓库中存在 `@ControllerAdvice`/`@RestControllerAdvice`）或与后端约定状态码。

---

## 开发与对接建议（给前端）

- 使用 `Result` 约定解析返回：先检查 `code === 200` 再读取 `data`。
- 对于分页接口，`data` 为 MyBatis-Plus 的 `IPage`，常含字段：current, size, total, records；records 为列表。
- 文件上传使用 multipart/form-data；前端需把 `file` 参数名和 Controller 中 `@RequestParam("file")` 对齐。
- 模板执行返回的 `data` 结构依赖执行引擎（包含单步结果、断言与变量），前端应对返回做防守式解析（判空、类型转换安全检查）。
- 创建/更新类接口（使用 DTO）在请求时请根据 DTO 字段补全必要属性（例如 name、path、method 对于接口模板通常是必填项）。

---

## 我可以继续为你做的事情

- 基于源码自动生成更详细的字段列表（逐个 DTO/VO 展开字段说明并把示例 JSON 放入文档）
- 生成 OpenAPI/Swagger 规范（YAML/JSON）以便前端使用工具生成 SDK
- 补充鉴权说明：若你的项目有 Security 配置，我可以扫描并写入鉴权头/权限角色

如果你希望我把这份文档保存到仓库（docs/api-template-and-template-job.md），我已经创建好了该文件并放在 `docs/` 下；如果需要我把 DTO/VO 的字段逐个展开到文档里，请回复："展开 DTO 字段并生成示例"。
