# 模板模块 API 调试文档

基础 URL: `http://localhost:8080`

---

## 一、模板管理接口 (/api/template)

### 1. 分页查询模板
```
GET /api/template/page
```
**参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| current | Long | 否 | 当前页，默认 1 |
| size | Long | 否 | 每页条数，默认 10 |
| folderId | Long | 否 | 文件夹ID |
| keyword | String | 否 | 关键词搜索 |
| protocolType | String | 否 | 协议类型(HTTP/HTTPS等) |
| status | Integer | 否 | 状态(0-草稿 1-已发布 2-已归档) |

**响应：** `Result<IPage<InterfaceTemplateVO>>`

---

### 2. 获取模板详情
```
GET /api/template/{id}
```
**参数：** Path - id

**响应：** `Result<InterfaceTemplateVO>`

---

### 3. 创建模板
```
POST /api/template
```
**请求体：**
```json
{
  "folderId": 1,
  "name": "测试接口",
  "description": "接口描述",
  "protocolType": "HTTP",
  "method": "POST",
  "baseUrl": "http://localhost:8080",
  "path": "/api/test",
  "authType": "NONE",
  "bodyType": "JSON",
  "bodyContent": "{}",
  "headers": [
    {
      "headerName": "Content-Type",
      "headerValue": "application/json",
      "isEnabled": 1
    }
  ],
  "parameters": [],
  "formDataList": [],
  "assertions": [],
  "preProcessors": [
    {
      "processorName": "生成时间戳",
      "processorType": "TIMESTAMP",
      "targetVariable": "timestamp",
      "isEnabled": 1,
      "sortOrder": 0
    }
  ],
  "postProcessors": [
    {
      "processorName": "提取Token",
      "processorType": "JSON_EXTRACT",
      "extractExpression": "$.data.token",
      "targetVariable": "token",
      "isEnabled": 1,
      "sortOrder": 0
    }
  ],
  "variables": []
}
```

**响应：** `Result<InterfaceTemplateVO>`

---

### 4. 更新模板
```
PUT /api/template/{id}
```
**请求体：** 同创建模板 (InterfaceTemplateDTO)

**响应：** `Result<String>` - "更新成功"

---

### 5. 删除模板
```
DELETE /api/template/{id}
```

**响应：** `Result<String>` - "删除成功"

---

### 6. 复制模板
```
POST /api/template/{id}/copy?newName=新模板名称
```

**响应：** `Result<InterfaceTemplateVO>`

---

### 7. 发布模板
```
PUT /api/template/{id}/publish
```

**响应：** `Result<String>` - "发布成功"

---

### 8. 归档模板
```
PUT /api/template/{id}/archive
```

**响应：** `Result<String>` - "归档成功"

---

### 9. 移动模板
```
PUT /api/template/{id}/move?folderId=目标文件夹ID
```

**响应：** `Result<String>` - "移动成功"

---

## 二、模板执行接口 (/api/template/execute)

### 10. 执行模板
```
POST /api/template/execute/{templateId}
```
**请求体：**
```json
{
  "environmentId": 1,
  "variables": {
    "username": "admin",
    "password": "123456"
  }
}
```

**响应：** `Result<Map<String, Object>>`
```json
{
  "code": 200,
  "message": "执行成功",
  "data": {
    "templateId": 1000,
    "templateName": "测试接口",
    "request": {
      "url": "http://localhost:8080/api/test",
      "method": "POST",
      "headers": {...},
      "body": "{...}"
    },
    "response": {
      "statusCode": 200,
      "statusText": "OK",
      "headers": {...},
      "body": "{...}",
      "responseTime": 123
    },
    "variables": {
      "timestamp": 1640000000,
      "token": "abc123"
    }
  }
}
```

---

### 11. 验证模板
```
GET /api/template/execute/{templateId}/validate
```

**响应：** `Result<Map<String, Object>>`
```json
{
  "valid": true,
  "errors": [],
  "warnings": []
}
```

---

### 12. 预览请求
```
POST /api/template/execute/{templateId}/preview
```
**请求体：** 同执行模板

**响应：** `Result<Map<String, Object>>` - 最终请求内容（不发送）

---

## 三、文件夹管理接口 (/api/template/folder)

### 13. 获取文件夹树
```
GET /api/template/folder/tree?parentId=0
```

**响应：** `Result<List<TemplateFolderVO>>`

---

### 14. 创建文件夹
```
POST /api/template/folder
```
**请求体：**
```json
{
  "parentId": 0,
  "name": "新文件夹",
  "description": "描述",
  "ownerId": 1,
  "visibility": 1
}
```

**响应：** `Result<TemplateFolderVO>`

---

### 15. 更新文件夹
```
PUT /api/template/folder/{id}
```

**响应：** `Result<String>`

---

### 16. 删除文件夹
```
DELETE /api/template/folder/{id}
```

**响应：** `Result<String>`

---

### 17. 移动文件夹
```
PUT /api/template/folder/{id}/move?targetParentId=新父ID
```

**响应：** `Result<String>`

---

### 18. 获取文件夹详情
```
GET /api/template/folder/{id}
```

**响应：** `Result<TemplateFolderVO>`

---

## 四、环境管理接口 (/api/template/environment)

### 19. 获取环境列表
```
GET /api/template/environment/list/{templateId}
```

**响应：** `Result<List<TemplateEnvironmentVO>>`

---

### 20. 获取默认环境
```
GET /api/template/environment/default/{templateId}
```

**响应：** `Result<TemplateEnvironmentVO>`

---

### 21. 创建环境
```
POST /api/template/environment
```
**请求体：**
```json
{
  "templateId": 1000,
  "envName": "测试环境",
  "envCode": "TEST",
  "baseUrl": "http://test.example.com",
  "variables": "{\"apiKey\":\"test123\"}",
  "isDefault": 0
}
```

**响应：** `Result<TemplateEnvironmentVO>`

---

### 22. 更新环境
```
PUT /api/template/environment/{id}
```

**响应：** `Result<String>`

---

### 23. 删除环境
```
DELETE /api/template/environment/{id}
```

**响应：** `Result<String>`

---

### 24. 设置默认环境
```
PUT /api/template/environment/{id}/default?templateId=模板ID
```

**响应：** `Result<String>`

---

### 25. 克隆环境
```
POST /api/template/environment/{id}/clone?newName=新环境名
```

**响应：** `Result<TemplateEnvironmentVO>`

---

## 五、历史版本接口 (/api/template/history)

### 26. 获取历史列表
```
GET /api/template/history/list/{templateId}
```

**响应：** `Result<List<TemplateHistoryVO>>`

---

### 27. 获取历史详情
```
GET /api/template/history/{id}
```

**响应：** `Result<TemplateHistoryVO>`

---

### 28. 回滚版本
```
POST /api/template/history/{id}/rollback
```

**响应：** `Result<String>`

---

### 29. 清理历史
```
DELETE /api/template/history/clean/{templateId}?keepCount=10
```

**响应：** `Result<Integer>` - 删除的数量

---

## 六、收藏关注接口 (/api/template/favorite)

### 30. 收藏模板
```
POST /api/template/favorite/{templateId}?remark=备注
```
**Header:** `userId: 用户ID`

**响应：** `Result<TemplateFavoriteVO>`

---

### 31. 取消收藏
```
DELETE /api/template/favorite/{templateId}
```
**Header:** `userId: 用户ID`

**响应：** `Result<String>`

---

### 32. 关注模板
```
POST /api/template/favorite/follow/{templateId}
```
**Header:** `userId: 用户ID`

**响应：** `Result<TemplateFavoriteVO>`

---

### 33. 取消关注
```
DELETE /api/template/favorite/follow/{templateId}
```
**Header:** `userId: 用户ID`

**响应：** `Result<String>`

---

### 34. 我的收藏列表
```
GET /api/template/favorite/my-favorites
```
**Header:** `userId: 用户ID`

**响应：** `Result<List<TemplateFavoriteVO>>`

---

### 35. 我的关注列表
```
GET /api/template/favorite/my-follows
```
**Header:** `userId: 用户ID`

**响应：** `Result<List<TemplateFavoriteVO>>`

---

### 36. 检查是否已收藏
```
GET /api/template/favorite/check/{templateId}
```
**Header:** `userId: 用户ID`

**响应：** `Result<Boolean>`

---

## 调试建议

### 1. 基础环境准备
```sql
-- 先执行数据库脚本
-- db-migration/2026-04-07/template.sql
```

### 2. 测试顺序
```
1. 创建文件夹 → 2. 创建模板 → 3. 添加环境 → 4. 执行模板
```

### 3. 前置处理器测试模板
```json
{
  "name": "前置处理器测试",
  "protocolType": "HTTP",
  "method": "GET",
  "path": "/api/test?timestamp=${timestamp}&nonce=${nonce}&sign=${sign}",
  "preProcessors": [
    {
      "processorName": "时间戳",
      "processorType": "TIMESTAMP",
      "targetVariable": "timestamp",
      "isEnabled": 1
    },
    {
      "processorName": "随机数",
      "processorType": "RANDOM_STRING",
      "config": "{\"length\": 16}",
      "targetVariable": "nonce",
      "isEnabled": 1
    },
    {
      "processorName": "MD5签名",
      "processorType": "MD5",
      "config": "{\"value\": \"${timestamp}${nonce}secret\"}",
      "targetVariable": "sign",
      "isEnabled": 1
    }
  ]
}
```

### 4. 后置处理器测试模板
```json
{
  "postProcessors": [
    {
      "processorName": "提取Token",
      "processorType": "JSON_EXTRACT",
      "extractExpression": "$.data.token",
      "targetVariable": "authToken",
      "defaultValue": "default_token",
      "isEnabled": 1
    }
  ]
}
```

### 5. 常用 HTTP 测试工具
- **Postman** - 图形化界面
- **IDEA HTTP Client** - `.http` 文件
- **curl** - 命令行

```bash
# curl 示例
curl -X POST http://localhost:8080/api/template \
  -H "Content-Type: application/json" \
  -d '{
    "name": "测试",
    "protocolType": "HTTP",
    "method": "GET",
    "path": "/test"
  }'
```
