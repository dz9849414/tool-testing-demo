# CAD 模块前端联调说明

## 1. 基础信息

后端接口统一前缀：

```text
/api/pdm/cad/mock
```

统一响应结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1710000000000,
  "traceId": "trace-id"
}
```

前端判断请求是否成功：

- 优先判断 `code === 200`。
- 业务数据都从 `data` 读取。

## 2. 页面按钮与接口对应关系

| 前端按钮 | 接口 | 方法 | 说明 |
| --- | --- | --- | --- |
| 配置 CAD 软件 STEP 数据交换格式 | `/file-convert/save` | POST | 保存 `.step` 相关文件转换配置 |
| 配置 CAD 软件 IGES 数据交换格式 | `/file-convert/save` | POST | 保存 `.iges` 或 `.igs` 相关文件转换配置 |
| 配置 CAD 软件自定义数据交换格式 | `/data-convert/save-batch` | POST | 保存字段映射；如涉及文件格式，也调用 `/file-convert/save` |
| CAD 接口连通性测试 | `/connectivity-test` | POST | 真实发起 HTTP/HTTPS 请求测试连通性 |
| 配置 CAD 接口认证方式 | `/auth/save` | POST | 保存认证方式和认证参数 |
| 表格查询 | `/page` | GET | 分页查询模拟接口 |
| 新增/编辑模拟接口 | `/create` | POST | 保存模拟接口主体配置 |
| 操作列-测试 | `/test` | POST | 使用模拟响应测试字段转换和文件转换 |
| 操作列-统一执行 | `/execute` | POST | 统一执行数据转换和文件转换 |

## 3. 枚举值

### 3.1 CAD 类型 `cadType`

| 值 | 展示文案 |
| --- | --- |
| NX | NX 软件 |
| CATIA | CATIA 软件 |
| ZWCAD | 中望 CAD 软件 |
| CUSTOM | 自定义 CAD 软件 |

### 3.2 配置类型 `convertType`

前端表格中的“配置类型”建议直接映射后端 `convertType`：

| 值 | 展示文案 |
| --- | --- |
| DATA | 数据转换 |
| FILE | 文件转换 |
| BOTH | 数据 + 文件转换 |

### 3.3 状态 `status`

| 值 | 展示文案 |
| --- | --- |
| 0 | 停用 |
| 1 | 启用 |

### 3.4 认证方式 `authType`

| 值 | 展示文案 | authConfig |
| --- | --- | --- |
| NONE | 无认证 | 可为空 |
| BASIC | Basic Auth | `{"username":"user","password":"pwd"}` |
| BEARER | Bearer Token | `{"token":"access-token"}` |
| API_KEY | API Key | `{"name":"X-API-Key","value":"key","location":"HEADER"}` |
| CUSTOM_HEADERS | 自定义请求头 | `{"X-Sign":"sign","X-Tenant":"tenant"}` |

`API_KEY.location` 支持：

| 值 | 说明 |
| --- | --- |
| HEADER | 放入请求头，默认值 |
| QUERY | 放入 URL 查询参数 |

## 4. 表格查询

```http
GET /api/pdm/cad/mock/page
```

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| current | number | 否 | 当前页，默认 1 |
| size | number | 否 | 每页条数，默认 10 |
| cadType | string | 否 | CAD 类型 |
| convertType | string | 否 | 配置类型 |
| interfaceName | string | 否 | 模拟接口名称，模糊查询 |
| applyFlow | string | 否 | 适用流程 |
| status | number | 否 | 状态 |

示例：

```http
GET /api/pdm/cad/mock/page?current=1&size=10&cadType=NX&status=1
```

表格字段建议：

| 表格列 | 后端字段 |
| --- | --- |
| 模拟接口ID | `id` |
| 模拟接口名称 | `interfaceName` |
| 适用流程 | `applyFlow` |
| 配置类型 | `convertType` |
| 状态 | `status` |
| 版本号 | `versionNo` |
| 负责人 | `ownerName` |
| 创建时间 | `createTime` |
| 创建人 | `createName` |
| 关联功能/关联配置 | 前端可通过 `id` 查询字段映射和文件转换配置后自行聚合 |

## 5. 新增/编辑模拟接口

```http
POST /api/pdm/cad/mock/create
```

新增时不传 `id`，编辑时传 `id`。

请求示例：

```json
{
  "id": 1,
  "interfaceName": "NX BOM 同步模拟接口",
  "cadType": "NX",
  "convertType": "BOTH",
  "applyFlow": "BOM_SYNC",
  "requestMethod": "POST",
  "interfaceUrl": "/mock/nx/bom-sync",
  "requestHeaders": "{\"Content-Type\":\"application/json\"}",
  "requestParams": "{\"source\":\"nx\"}",
  "requestBodyTemplate": "{\"partNo\":\"${partNo}\"}",
  "responseBody": "{\"code\":\"200\",\"data\":{\"partNo\":\"P-001\",\"qty\":2}}",
  "successField": "code",
  "successValue": "200",
  "authType": "NONE",
  "authConfig": null,
  "status": 1,
  "versionNo": "V1.0",
  "ownerId": 1001,
  "ownerName": "张三",
  "remark": "前端联调示例"
}
```

前端注意：

- `requestHeaders`、`requestParams`、`requestBodyTemplate`、`responseBody`、`authConfig` 都是 JSON 字符串，不是对象。
- `responseBody` 必填且必须是合法 JSON 字符串。
- 如果用户在表单里用 JSON 编辑器，提交前需要 `JSON.stringify`。

## 6. 配置认证方式

```http
POST /api/pdm/cad/mock/auth/save
```

请求示例：

```json
{
  "mockInterfaceId": 1,
  "authType": "BEARER",
  "authConfig": "{\"token\":\"access-token\"}"
}
```

不同认证方式的表单建议：

| authType | 前端输入项 |
| --- | --- |
| NONE | 无 |
| BASIC | 用户名、密码 |
| BEARER | Token |
| API_KEY | 参数名、参数值、位置 HEADER/QUERY |
| CUSTOM_HEADERS | JSON 编辑器 |

## 7. 配置 STEP/IGES 文件转换

```http
POST /api/pdm/cad/mock/file-convert/save
```

STEP 示例：

```json
{
  "mockInterfaceId": 1,
  "cadType": "NX",
  "applyFlow": "BOM_SYNC",
  "sourceFormat": ".prt",
  "targetFormat": ".step",
  "convertUrl": "/mock/nx/file-convert",
  "asyncConvert": 1,
  "timeoutSeconds": 60,
  "generatePreview": 1,
  "keepSourceFile": 1,
  "overwriteTargetFile": 0,
  "requestTemplate": "{\"sourceFileId\":\"${sourceFileId}\"}",
  "responseTemplate": "{\"targetFileId\":\"${targetFileId}\"}",
  "status": 1,
  "remark": "NX PRT 转 STEP"
}
```

IGES 示例：

```json
{
  "mockInterfaceId": 1,
  "cadType": "NX",
  "applyFlow": "BOM_SYNC",
  "sourceFormat": ".prt",
  "targetFormat": ".iges",
  "convertUrl": "/mock/nx/file-convert",
  "asyncConvert": 1,
  "timeoutSeconds": 60,
  "status": 1,
  "remark": "NX PRT 转 IGES"
}
```

前端注意：

- 后端会自动把 `prt` 规范为 `.prt`，但前端建议统一传带点格式。
- 同一 `cadType + applyFlow + sourceFormat + targetFormat` 只能有一条启用配置。

## 8. 查询文件转换配置

```http
GET /api/pdm/cad/mock/file-convert/list
```

请求参数：

| 字段 | 类型 | 必填 |
| --- | --- | --- |
| cadType | string | 否 |
| applyFlow | string | 否 |
| sourceFormat | string | 否 |
| targetFormat | string | 否 |
| status | number | 否 |

可用于编辑弹窗回显已配置的 STEP/IGES/自定义格式。

## 9. 配置自定义数据交换格式

```http
POST /api/pdm/cad/mock/data-convert/save-batch
```

请求示例：

```json
{
  "mockInterfaceId": 1,
  "mappings": [
    {
      "sourceField": "data.partNo",
      "sourceFieldName": "CAD物料号",
      "targetModule": "ITEM",
      "targetField": "itemCode",
      "targetFieldName": "物料编码",
      "fieldType": "STRING",
      "required": 1,
      "transformRule": "TRIM,UPPERCASE",
      "sortNo": 1,
      "status": 1,
      "convertDirection": "RESPONSE"
    },
    {
      "sourceField": "data.qty",
      "sourceFieldName": "数量",
      "targetModule": "BOM",
      "targetField": "quantity",
      "targetFieldName": "用量",
      "fieldType": "NUMBER",
      "required": 0,
      "defaultValue": "1",
      "sortNo": 2,
      "status": 1,
      "convertDirection": "RESPONSE"
    }
  ]
}
```

字段说明：

| 字段 | 说明 |
| --- | --- |
| sourceField | 源字段路径，使用点号路径，如 `data.partNo` |
| targetModule | 目标模块 |
| targetField | 目标字段 |
| fieldType | `STRING`、`NUMBER`、`BOOLEAN`、`JSON` |
| required | 0 否，1 是 |
| transformRule | 支持 `TRIM`、`UPPERCASE`、`LOWERCASE`，多个用英文逗号分隔 |

前端注意：

- 保存时会覆盖该模拟接口下的旧映射。
- 同一接口下 `targetModule + targetField` 不能重复。

## 10. 查询数据字段映射

```http
GET /api/pdm/cad/mock/data-convert/list/{mockInterfaceId}
```

用于自定义数据交换格式弹窗回显。

## 11. CAD 接口连通性测试

```http
POST /api/pdm/cad/mock/connectivity-test
```

最简调用，使用已保存的模拟接口配置：

```json
{
  "mockInterfaceId": 1,
  "baseUrl": "http://127.0.0.1:8080",
  "timeoutSeconds": 10
}
```

完整调用，不依赖已保存配置：

```json
{
  "requestMethod": "POST",
  "interfaceUrl": "https://cad.example.com/api/ping",
  "requestHeaders": "{\"Content-Type\":\"application/json\"}",
  "requestParams": "{\"source\":\"nx\"}",
  "requestBody": "{\"ping\":true}",
  "authType": "API_KEY",
  "authConfig": "{\"name\":\"X-API-Key\",\"value\":\"cad-api-key\",\"location\":\"HEADER\"}",
  "timeoutSeconds": 10
}
```

响应示例：

```json
{
  "success": true,
  "requestMethod": "POST",
  "requestUrl": "https://cad.example.com/api/ping?source=nx",
  "statusCode": 200,
  "responseHeaders": {
    "content-type": ["application/json"]
  },
  "responseBody": "{\"ok\":true}",
  "costTimeMs": 88,
  "errorMessage": null
}
```

前端展示建议：

| 字段 | 展示 |
| --- | --- |
| success | 成功/失败标签 |
| statusCode | HTTP 状态码 |
| costTimeMs | 耗时 |
| requestUrl | 实际请求地址 |
| responseBody | 响应内容，可用 JSON 查看器 |
| errorMessage | 失败原因 |

注意：

- `interfaceUrl` 是相对路径时必须传 `baseUrl`。
- 只有 HTTP 状态码 `200-299` 会返回 `success = true`。
- 连通性测试是真实请求，不使用 `responseBody` 模拟响应。

## 12. 模拟接口测试

```http
POST /api/pdm/cad/mock/test
```

请求示例：

```json
{
  "mockInterfaceId": 1,
  "sourceFileId": 10001,
  "sourceFileName": "bracket.prt",
  "sourceFormat": ".prt",
  "targetFormat": ".step",
  "requestData": {
    "partNo": "P-001"
  }
}
```

响应 `data` 示例：

```json
{
  "mockInterfaceId": 1,
  "success": true,
  "rawResponse": {
    "code": "200",
    "data": {
      "partNo": "P-001",
      "qty": 2
    }
  },
  "convertedData": {
    "itemCode": "P-001",
    "quantity": 2
  },
  "fileConvert": {
    "taskNo": "CAD_CONVERT_202604291530001234",
    "taskStatus": 30,
    "targetFileId": 123456,
    "targetFileName": "bracket.step",
    "targetFormat": ".step",
    "errorMessage": null
  },
  "errorMessage": null
}
```

说明：

- 这个接口用于测试模拟响应、字段映射、模拟文件转换。
- 它不是连通性测试，不会真实请求 CAD 接口。

## 13. 执行文件转换

```http
POST /api/pdm/cad/mock/file-convert/execute
```

请求示例：

```json
{
  "cadType": "NX",
  "applyFlow": "BOM_SYNC",
  "sourceFileId": 10001,
  "sourceFileName": "bracket.prt",
  "sourceFormat": ".prt",
  "targetFormat": ".step"
}
```

响应 `data` 示例：

```json
{
  "taskNo": "CAD_CONVERT_202604291530001234",
  "taskStatus": 10,
  "targetFileId": null,
  "targetFileName": null,
  "targetFormat": ".step",
  "errorMessage": null
}
```

## 14. 查询文件转换任务

```http
GET /api/pdm/cad/mock/file-convert/task/{taskNo}
```

响应 `data` 示例：

```json
{
  "taskNo": "CAD_CONVERT_202604291530001234",
  "taskStatus": 30,
  "targetFileId": 123456,
  "targetFileName": "bracket.step",
  "targetFormat": ".step",
  "errorMessage": null
}
```

任务状态：

| taskStatus | 说明 |
| --- | --- |
| 10 | 待转换 |
| 20 | 转换中 |
| 30 | 转换成功 |
| 40 | 转换失败 |
| 50 | 已取消 |

## 15. 推荐联调流程

1. 调用 `/create` 新建模拟接口。
2. 如接口需要认证，调用 `/auth/save` 保存认证方式。
3. 如果是 STEP/IGES/自定义文件格式，调用 `/file-convert/save` 保存文件转换配置。
4. 如果是数据交换格式，调用 `/data-convert/save-batch` 保存字段映射。
5. 调用 `/connectivity-test` 验证真实 CAD 接口是否可访问。
6. 调用 `/test` 验证模拟响应、字段映射和文件转换。
7. 调用 `/page` 刷新列表。

## 16. 常见错误提示

| 场景 | 后端提示 |
| --- | --- |
| JSON 字符串格式错误 | 必须是合法JSON |
| CAD 类型错误 | 不支持的CAD类型 |
| 转换类型错误 | 不支持的转换类型 |
| 认证方式错误 | 不支持的认证方式 |
| 相对地址未传 baseUrl | 相对接口地址需要提供baseUrl |
| 文件名无扩展名 | 源文件缺少扩展名 |
| 文件后缀和 sourceFormat 不一致 | 文件后缀与源格式不匹配 |
| 未找到文件转换配置 | 未找到匹配的文件转换配置 |
| 未找到模拟接口 | 模拟接口不存在 |
