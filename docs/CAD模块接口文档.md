# CAD 模块接口文档

## 1. 模块概述

CAD 模块用于模拟第三方 CAD 软件接口，并提供数据字段转换、文件格式转换、统一执行和测试日志能力。

当前模块入口为：

```text
/api/pdm/cad/mock
```

核心能力：

- 维护 CAD 模拟接口配置。
- 维护模拟响应字段到业务字段的映射关系。
- 维护 CAD 文件格式转换配置。
- 创建并查询文件转换任务。
- 按 CAD 类型和业务流程统一执行数据转换与文件转换。
- 记录模拟接口测试结果。

## 2. 技术与代码位置

| 类型 | 位置 |
| --- | --- |
| Controller | `src/main/java/com/example/tooltestingdemo/controller/cad/CadMockInterfaceController.java` |
| Service | `src/main/java/com/example/tooltestingdemo/service/cad/CadMockInterfaceService.java` |
| Service 实现 | `src/main/java/com/example/tooltestingdemo/service/cad/impl/CadMockInterfaceServiceImpl.java` |
| DTO | `src/main/java/com/example/tooltestingdemo/dto/cad` |
| VO | `src/main/java/com/example/tooltestingdemo/vo/cad` |
| Entity | `src/main/java/com/example/tooltestingdemo/entity/cad` |
| Mapper | `src/main/java/com/example/tooltestingdemo/mapper/cad` |
| 枚举 | `src/main/java/com/example/tooltestingdemo/enums/cad` |
| 数据库脚本 | `db-migration/2026-04-29/cad_mock_tables.sql` |

## 3. 通用响应格式

所有接口统一返回 `Result<T>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1710000000000,
  "traceId": "trace-id"
}
```

分页接口返回 MyBatis-Plus `IPage<T>`，默认分页参数：

| 参数 | 说明 | 默认值 |
| --- | --- | --- |
| current | 当前页 | 1 |
| size | 每页条数 | 10 |

## 4. 枚举说明

### 4.1 CAD 类型 `cadType`

| 值 | 说明 |
| --- | --- |
| NX | NX 软件 |
| CATIA | CATIA 软件 |
| ZWCAD | 中望 CAD 软件 |
| CUSTOM | 自定义 CAD 软件 |

### 4.2 转换类型 `convertType`

| 值 | 说明 |
| --- | --- |
| DATA | 仅数据字段转换 |
| FILE | 仅文件格式转换 |
| BOTH | 数据字段转换 + 文件格式转换 |

### 4.3 文件转换任务状态 `taskStatus`

| 值 | 说明 |
| --- | --- |
| 10 | 待转换 |
| 20 | 转换中 |
| 30 | 转换成功 |
| 40 | 转换失败 |
| 50 | 已取消 |

### 4.4 认证方式 `authType`

| 值 | 说明 | authConfig 示例 |
| --- | --- | --- |
| NONE | 无认证 | - |
| BASIC | Basic 认证 | `{"username":"user","password":"pwd"}` |
| BEARER | Bearer Token 认证 | `{"token":"access-token"}` |
| API_KEY | API Key 认证 | `{"name":"X-API-Key","value":"key","location":"HEADER"}` |
| CUSTOM_HEADERS | 自定义请求头认证 | `{"X-Sign":"sign","X-Tenant":"tenant"}` |

## 5. 权限点

| 接口能力 | 权限表达式 |
| --- | --- |
| 查询模拟接口 | `cad:mock:query` |
| 创建/更新模拟接口 | `cad:mock:create` |
| 编辑数据映射 | `cad:mock:data:edit` |
| 查询数据映射 | `cad:mock:data:query` |
| 编辑文件转换配置 | `cad:mock:file:edit` |
| 查询文件转换配置/任务 | `cad:mock:file:query` |
| 执行文件转换 | `cad:mock:file:execute` |
| 统一执行 | `cad:mock:execute` |
| 接口测试 | `cad:mock:test` |
| 编辑认证方式 | `cad:mock:auth:edit` |
| 连通性测试 | `cad:mock:connectivity:test` |

管理员角色 `ADMIN` 可访问全部接口。

## 6. 接口说明

### 6.1 分页查询模拟接口

```http
GET /api/pdm/cad/mock/page
```

请求参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| current | Long | 否 | 当前页 |
| size | Long | 否 | 每页条数 |
| cadType | String | 否 | CAD 类型 |
| convertType | String | 否 | 转换类型 |
| interfaceName | String | 否 | 模拟接口名称，模糊查询 |
| applyFlow | String | 否 | 适用流程 |
| status | Integer | 否 | 状态：0 停用，1 启用 |

示例：

```http
GET /api/pdm/cad/mock/page?current=1&size=10&cadType=NX&convertType=BOTH&status=1
```

### 6.2 创建或更新模拟接口

```http
POST /api/pdm/cad/mock/create
```

请求体：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | Long | 否 | - | 为空时新增，不为空时更新 |
| interfaceName | String | 是 | - | 模拟接口名称 |
| cadType | String | 是 | - | CAD 类型 |
| convertType | String | 是 | - | 转换类型：DATA、FILE、BOTH |
| applyFlow | String | 是 | - | 适用流程 |
| requestMethod | String | 是 | - | 请求方式，如 POST、GET |
| interfaceUrl | String | 是 | - | 模拟接口地址 |
| requestHeaders | String | 否 | - | 请求头 JSON 字符串 |
| requestParams | String | 否 | - | 请求参数 JSON 字符串 |
| requestBodyTemplate | String | 否 | - | 请求体模板 JSON 字符串 |
| responseBody | String | 是 | - | 模拟响应 JSON 字符串 |
| successField | String | 否 | code | 成功标识字段 |
| successValue | String | 否 | 200 | 成功标识值 |
| authType | String | 否 | NONE | 认证方式 |
| authConfig | String | 否 | - | 认证配置 JSON 字符串 |
| status | Integer | 是 | 1 | 状态：0 停用，1 启用 |
| versionNo | String | 否 | V1.0 | 版本号 |
| ownerId | Long | 否 | - | 负责人 ID |
| ownerName | String | 否 | - | 负责人名称 |
| remark | String | 否 | - | 备注 |

示例：

```json
{
  "interfaceName": "NX BOM 同步模拟接口",
  "cadType": "NX",
  "convertType": "BOTH",
  "applyFlow": "BOM_SYNC",
  "requestMethod": "POST",
  "interfaceUrl": "/mock/nx/bom-sync",
  "requestHeaders": "{\"Content-Type\":\"application/json\"}",
  "requestParams": "{\"source\":\"nx\"}",
  "requestBodyTemplate": "{\"partNo\":\"${partNo}\"}",
  "responseBody": "{\"code\":\"200\",\"data\":{\"partNo\":\"P-001\",\"partName\":\"支架\",\"qty\":2}}",
  "successField": "code",
  "successValue": "200",
  "authType": "BEARER",
  "authConfig": "{\"token\":\"access-token\"}",
  "status": 1,
  "versionNo": "V1.0",
  "ownerName": "CAD管理员",
  "remark": "NX BOM 同步模拟"
}
```

注意：

- `cadType` 必须在 CAD 类型枚举内。
- `convertType` 必须在转换类型枚举内。
- `responseBody` 必须是合法 JSON。
- `requestHeaders`、`requestParams`、`requestBodyTemplate` 为空时不校验，非空时必须是合法 JSON。
- `authConfig` 为空时不校验，非空时必须是合法 JSON。

### 6.3 保存接口认证方式

```http
POST /api/pdm/cad/mock/auth/save
```

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| mockInterfaceId | Long | 是 | 模拟接口 ID |
| authType | String | 是 | 认证方式：NONE、BASIC、BEARER、API_KEY、CUSTOM_HEADERS |
| authConfig | String | 否 | 认证配置 JSON 字符串 |

示例：

```json
{
  "mockInterfaceId": 1,
  "authType": "API_KEY",
  "authConfig": "{\"name\":\"X-API-Key\",\"value\":\"cad-api-key\",\"location\":\"HEADER\"}"
}
```

说明：

- `BASIC` 需要 `username`、`password`。
- `BEARER` 需要 `token`。
- `API_KEY` 需要 `name`、`value`，`location` 可选 `HEADER` 或 `QUERY`，默认 `HEADER`。
- `CUSTOM_HEADERS` 会将 `authConfig` 中的所有键值作为请求头。

### 6.4 批量保存数据字段映射

```http
POST /api/pdm/cad/mock/data-convert/save-batch
```

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| mockInterfaceId | Long | 是 | 模拟接口 ID |
| mappings | Array | 是 | 字段映射列表 |

`mappings` 字段：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | Long | 否 | - | 当前保存逻辑会先删除旧映射再新增，id 不作为更新依据 |
| sourceField | String | 是 | - | 源字段路径，使用点号路径，如 `data.partNo` |
| sourceFieldName | String | 否 | - | 源字段名称 |
| targetModule | String | 是 | - | 目标模块 |
| targetField | String | 是 | - | 目标字段 |
| targetFieldName | String | 否 | - | 目标字段名称 |
| fieldType | String | 否 | STRING | 字段类型：STRING、NUMBER、BOOLEAN、JSON |
| required | Integer | 否 | 0 | 是否必填：0 否，1 是 |
| defaultValue | String | 否 | - | 源值为空时使用的默认值 |
| transformRule | String | 否 | - | 转换规则，支持 `TRIM`、`UPPERCASE`、`LOWERCASE`，多个规则逗号分隔 |
| sortNo | Integer | 否 | 0 | 排序号 |
| status | Integer | 否 | 1 | 状态：0 停用，1 启用 |
| convertDirection | String | 否 | RESPONSE | 转换方向 |

示例：

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
      "status": 1
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
      "status": 1
    }
  ]
}
```

处理规则：

- 保存时会删除该模拟接口下已有映射，再插入本次提交的映射。
- 同一接口下 `targetModule + targetField` 不能重复。
- 执行转换时只使用 `status = 1` 的映射。
- 源字段点号路径会转换为 JSON Pointer 读取响应内容，例如 `data.partNo` 对应 `/data/partNo`。

### 6.5 查询数据字段映射

```http
GET /api/pdm/cad/mock/data-convert/list/{mockInterfaceId}
```

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| mockInterfaceId | Long | 是 | 模拟接口 ID |

返回结果按 `sortNo`、`id` 升序排列。

### 6.6 保存文件转换配置

```http
POST /api/pdm/cad/mock/file-convert/save
```

请求体：

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| id | Long | 否 | - | 为空时新增，不为空时更新 |
| mockInterfaceId | Long | 否 | - | 关联模拟接口 ID |
| cadType | String | 是 | - | CAD 类型 |
| applyFlow | String | 是 | - | 适用流程 |
| sourceFormat | String | 是 | - | 源文件格式，如 `.prt` 或 `prt` |
| targetFormat | String | 是 | - | 目标文件格式，如 `.step` 或 `step` |
| convertUrl | String | 是 | - | 转换服务地址 |
| asyncConvert | Integer | 否 | 1 | 是否异步转换：0 否，1 是 |
| timeoutSeconds | Integer | 否 | 60 | 超时时间 |
| generatePreview | Integer | 否 | 0 | 是否生成预览图 |
| keepSourceFile | Integer | 否 | 1 | 是否保留源文件 |
| overwriteTargetFile | Integer | 否 | 0 | 是否覆盖目标文件 |
| requestTemplate | String | 否 | - | 转换请求模板 JSON 字符串 |
| responseTemplate | String | 否 | - | 转换响应模板 JSON 字符串 |
| status | Integer | 否 | 1 | 状态：0 停用，1 启用 |
| remark | String | 否 | - | 备注 |

示例：

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

注意：

- `sourceFormat` 和 `targetFormat` 会自动规范为带点格式，例如 `prt` 会存为 `.prt`。
- 当配置启用时，同一 `cadType + applyFlow + sourceFormat + targetFormat` 只允许存在一条启用配置。
- `requestTemplate`、`responseTemplate` 为空时不校验，非空时必须是合法 JSON。

### 6.7 查询文件转换配置

```http
GET /api/pdm/cad/mock/file-convert/list
```

请求参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| cadType | String | 否 | CAD 类型 |
| applyFlow | String | 否 | 适用流程 |
| sourceFormat | String | 否 | 源文件格式 |
| targetFormat | String | 否 | 目标文件格式 |
| status | Integer | 否 | 状态：0 停用，1 启用 |

返回结果按 `updateTime`、`id` 倒序排列。

### 6.8 执行文件转换

```http
POST /api/pdm/cad/mock/file-convert/execute
```

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| cadType | String | 是 | CAD 类型 |
| applyFlow | String | 是 | 适用流程 |
| sourceFileId | Long | 是 | 源文件 ID |
| sourceFileName | String | 是 | 源文件名称，必须包含扩展名 |
| sourceFormat | String | 是 | 源文件格式，必须与文件名后缀匹配 |
| targetFormat | String | 是 | 目标文件格式 |

示例：

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

返回 `data` 示例：

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

处理规则：

- 会查找启用的文件转换配置。
- 文件名后缀必须与 `sourceFormat` 匹配。
- 创建任务初始状态为 `10`。
- 如果配置 `asyncConvert = 0`，创建后立即模拟完成并返回成功状态。

### 6.9 查询文件转换任务

```http
GET /api/pdm/cad/mock/file-convert/task/{taskNo}
```

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| taskNo | String | 是 | 文件转换任务编号 |

说明：

- 如果任务状态是 `10`，查询时会模拟完成任务，将状态更新为 `30`。
- 成功时会生成模拟的 `targetFileId` 和 `targetFileName`。

返回 `data` 示例：

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

### 6.10 统一执行

```http
POST /api/pdm/cad/mock/execute
```

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| cadType | String | 是 | CAD 类型 |
| applyFlow | String | 是 | 适用流程 |
| sourceFileId | Long | 否 | 源文件 ID |
| sourceFileName | String | 否 | 源文件名称 |
| sourceFormat | String | 否 | 源文件格式；为空时从文件名后缀提取 |
| targetFormat | String | 否 | 目标文件格式；为空时取匹配到的文件转换配置 |
| requestData | Object | 否 | 请求数据，当前主要用于日志与透传 |

示例：

```json
{
  "cadType": "NX",
  "applyFlow": "BOM_SYNC",
  "sourceFileId": 10001,
  "sourceFileName": "bracket.prt",
  "sourceFormat": ".prt",
  "targetFormat": ".step",
  "requestData": {
    "partNo": "P-001"
  }
}
```

返回 `data` 示例：

```json
{
  "mappedData": {
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
  }
}
```

处理规则：

- 根据 `cadType + applyFlow` 查找启用的模拟接口。
- 若同一条件存在多条启用接口，转换类型优先级为 `BOTH > DATA > FILE`。
- 会解析模拟接口的 `responseBody`。
- 当 `convertType` 为 `DATA` 或 `BOTH` 时执行字段映射；无映射时返回原始响应对象。
- 当 `convertType` 为 `FILE` 或 `BOTH` 且传入 `sourceFileName` 时，尝试执行文件转换。
- 统一执行中的文件转换会立即模拟完成。

### 6.11 测试模拟接口

```http
POST /api/pdm/cad/mock/test
```

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| mockInterfaceId | Long | 否 | 模拟接口 ID；优先使用 |
| cadType | String | 否 | CAD 类型；未传 `mockInterfaceId` 时使用 |
| applyFlow | String | 否 | 适用流程；未传 `mockInterfaceId` 时使用 |
| sourceFileId | Long | 否 | 源文件 ID |
| sourceFileName | String | 否 | 源文件名称 |
| sourceFormat | String | 否 | 源文件格式 |
| targetFormat | String | 否 | 目标文件格式 |
| requestData | Object | 否 | 请求数据 |

示例：

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

返回 `data` 示例：

```json
{
  "mockInterfaceId": 1,
  "success": true,
  "rawResponse": {
    "code": "200",
    "data": {
      "partNo": "P-001",
      "partName": "支架",
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

- 测试接口会写入 `pdm_tool_cad_mock_test_log`。
- 测试失败时 `success = false`，并返回错误信息。

### 6.12 CAD 接口连通性测试

```http
POST /api/pdm/cad/mock/connectivity-test
```

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| mockInterfaceId | Long | 否 | 模拟接口 ID；传入后会默认读取接口地址、请求方式、请求头、请求参数、请求体模板和认证配置 |
| baseUrl | String | 否 | 当接口地址是相对路径时必填，例如 `http://127.0.0.1:8080` |
| requestMethod | String | 否 | 请求方式；不传则取模拟接口配置，仍为空则默认 GET |
| interfaceUrl | String | 否 | 接口地址；不传则取模拟接口配置 |
| requestHeaders | String | 否 | 请求头 JSON 字符串；传入后覆盖模拟接口配置 |
| requestParams | String | 否 | 请求参数 JSON 字符串；传入后覆盖模拟接口配置 |
| requestBody | String | 否 | 请求体；不传则取模拟接口的 `requestBodyTemplate` |
| authType | String | 否 | 认证方式；传入后覆盖模拟接口配置 |
| authConfig | String | 否 | 认证配置 JSON 字符串；传入后覆盖模拟接口配置 |
| timeoutSeconds | Integer | 否 | 超时时间，默认 10 秒，范围 1 到 300 |

示例：

```json
{
  "mockInterfaceId": 1,
  "baseUrl": "http://127.0.0.1:8080",
  "timeoutSeconds": 10
}
```

也可以不依赖已保存配置，直接传入完整请求：

```json
{
  "requestMethod": "POST",
  "interfaceUrl": "https://cad.example.com/api/ping",
  "requestHeaders": "{\"Content-Type\":\"application/json\"}",
  "requestParams": "{\"source\":\"nx\"}",
  "requestBody": "{\"ping\":true}",
  "authType": "BEARER",
  "authConfig": "{\"token\":\"access-token\"}",
  "timeoutSeconds": 10
}
```

返回 `data` 示例：

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

说明：

- 连通性测试会真实发起 HTTP/HTTPS 请求，不使用模拟响应。
- HTTP 状态码在 200 到 299 之间时 `success = true`。
- 相对路径接口必须提供 `baseUrl`。
- 支持 `GET`、`DELETE`、`POST`、`PUT`、`PATCH`。

## 7. 字段转换规则

字段转换入口为模拟接口的 `responseBody`。

示例响应：

```json
{
  "code": "200",
  "data": {
    "partNo": " p-001 ",
    "qty": "2",
    "enabled": "true"
  }
}
```

映射配置：

| sourceField | targetField | fieldType | transformRule | 结果 |
| --- | --- | --- | --- | --- |
| data.partNo | itemCode | STRING | TRIM,UPPERCASE | `P-001` |
| data.qty | quantity | NUMBER | - | `2.0` |
| data.enabled | enabled | BOOLEAN | - | `true` |

支持字段类型：

| 类型 | 处理方式 |
| --- | --- |
| STRING | 转为字符串 |
| NUMBER | 数字保持原样，非数字转为 Double |
| BOOLEAN | 布尔值保持原样，非布尔值按字符串转 Boolean |
| JSON | 保持原对象 |

支持转换规则：

| 规则 | 说明 |
| --- | --- |
| TRIM | 去除首尾空白 |
| UPPERCASE | 转大写 |
| LOWERCASE | 转小写 |

## 8. 文件转换流程

```text
保存文件转换配置
  -> 执行文件转换
  -> 创建 pdm_tool_cad_file_convert_task
  -> 返回 taskNo
  -> 查询任务
  -> 模拟生成目标文件信息
```

任务编号格式：

```text
CAD_CONVERT_yyyyMMddHHmmss + 4位随机数
```

目标文件名生成规则：

```text
源文件基础名 + 目标格式
```

例如：

```text
bracket.prt -> bracket.step
```

## 9. 数据库表

| 表名 | 说明 |
| --- | --- |
| pdm_tool_cad_mock_interface | CAD 软件模拟接口配置表 |
| pdm_tool_cad_data_convert_mapping | CAD 数据字段转换配置表 |
| pdm_tool_cad_file_convert_config | CAD 文件格式转换配置表 |
| pdm_tool_cad_file_convert_task | CAD 文件转换任务表 |
| pdm_tool_cad_mock_test_log | CAD 模拟接口测试日志表 |

### 9.1 关键索引

| 表名 | 索引 | 字段 | 说明 |
| --- | --- | --- | --- |
| pdm_tool_cad_mock_interface | idx_cad_type_flow_status | cad_type, apply_flow, status | 按 CAD 类型、流程、状态查找启用接口 |
| pdm_tool_cad_data_convert_mapping | idx_mock_interface_id | mock_interface_id | 查询接口字段映射 |
| pdm_tool_cad_file_convert_config | idx_lookup | cad_type, apply_flow, source_format, target_format, status | 查找文件转换配置 |
| pdm_tool_cad_file_convert_task | uk_task_no | task_no | 按任务编号唯一查询 |
| pdm_tool_cad_mock_test_log | idx_mock_interface_id | mock_interface_id | 查询接口测试日志 |

## 10. 常见错误

| 场景 | 错误信息 |
| --- | --- |
| CAD 类型不在枚举内 | 不支持的CAD类型 |
| 转换类型不在枚举内 | 不支持的转换类型 |
| JSON 字符串非法 | 请求头/请求参数/请求体模板/模拟响应内容必须是合法JSON |
| 模拟接口不存在 | 模拟接口不存在 |
| 文件转换配置不存在 | 文件转换配置不存在 |
| 未找到启用模拟接口 | 未找到启用的CAD模拟接口配置 |
| 未找到文件转换配置 | 未找到匹配的文件转换配置 |
| 文件名没有扩展名 | 源文件缺少扩展名 |
| 文件后缀与 sourceFormat 不一致 | 文件后缀与源格式不匹配 |
| 必填字段转换后为空 | 必填字段转换后不能为空 |

## 11. 接入建议

- 前端保存 JSON 字符串字段前应先做 JSON 格式校验，减少后端校验失败。
- 文件格式建议统一传带点格式，如 `.prt`、`.step`，虽然后端会自动补点。
- 字段映射的 `sourceField` 使用点号路径，避免直接传 JSON Pointer。
- `BOTH` 类型适合需要同时返回业务字段和文件转换结果的流程。
- 测试接口适合调试模拟响应、字段映射和文件转换配置，正式业务调用建议使用统一执行接口。
