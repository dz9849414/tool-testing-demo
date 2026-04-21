# 系统管理使用说明

## 概述

系统管理模块提供完整的用户认证、权限控制、系统配置和数据字典管理功能，确保系统的安全性和可配置性。

## 1. 登录和角色管理

### 1.1 用户登录

#### API接口
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

#### 响应示例
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "role": "ADMIN",
      "permissions": ["user:view", "user:edit", "system:config"]
    }
  }
}
```

#### 功能说明
- 支持用户名密码登录
- 返回JWT token用于后续请求认证
- 包含用户基本信息和权限列表

### 1.2 角色管理

#### 角色类型
- **ADMIN** - 系统管理员：拥有所有权限
- **USER** - 普通用户：基础操作权限
- **OPERATOR** - 操作员：特定业务操作权限

#### 角色权限关联
- 角色通过权限表关联具体权限
- 支持动态权限分配
- 权限变更实时生效

## 2. 权限管理

### 2.1 权限控制机制

#### 权限注解使用
```java
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasPermission('user:create')")
@PreAuthorize("@securityService.hasPermission('report:view')")
```

#### 权限层级结构
```
系统管理
├── 用户管理 (user:*)
│   ├── 查看用户 (user:view)
│   ├── 创建用户 (user:create)
│   ├── 编辑用户 (user:edit)
│   └── 删除用户 (user:delete)
├── 角色管理 (role:*)
│   ├── 查看角色 (role:view)
│   ├── 分配权限 (role:assign)
│   └── 管理角色 (role:manage)
└── 权限管理 (permission:*)
    ├── 查看权限 (permission:view)
    └── 编辑权限 (permission:edit)
```

### 2.2 权限API接口

#### 获取用户权限列表
```http
GET /api/auth/permissions
Authorization: Bearer {token}
```

#### 权限分配接口
```http
POST /api/roles/{roleId}/permissions
Authorization: Bearer {token}
Content-Type: application/json

["user:view", "user:create", "report:view"]
```

## 3. 系统配置管理

### 3.1 配置类型

#### 系统级配置
- 应用名称、版本信息
- 日志级别设置
- 文件上传限制
- 会话超时时间

#### 业务配置
- 报告生成频率
- 数据保留期限
- 通知设置
- 模板默认参数

### 3.2 配置管理接口

#### 获取系统配置
```http
GET /api/system/configs
Authorization: Bearer {token}
```

#### 更新配置
```http
PUT /api/system/configs/{key}
Authorization: Bearer {token}
Content-Type: application/json

{
  "configValue": "新值",
  "description": "配置说明"
}
```

#### 配置示例
```java
@RestController
@RequestMapping("/api/system")
public class SysConfigController {
    
    @GetMapping("/configs")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<SysConfigDTO>> getConfigs() {
        // 获取所有系统配置
    }
    
    @PutMapping("/configs/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> updateConfig(@PathVariable String key, @RequestBody SysConfigDTO config) {
        // 更新指定配置
    }
}
```

## 4. 数据字典管理

### 4.1 字典结构

#### 字典类型定义
```sql
CREATE TABLE sys_dictionary (
    id BIGINT PRIMARY KEY,
    dict_type VARCHAR(50) NOT NULL COMMENT '字典类型',
    dict_code VARCHAR(100) NOT NULL COMMENT '字典编码',
    dict_value VARCHAR(200) NOT NULL COMMENT '字典值',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status VARCHAR(20) DEFAULT 'ENABLED' COMMENT '状态',
    description VARCHAR(500) COMMENT '描述'
);
```

### 4.2 常用字典类型

#### 用户状态字典
| 编码 | 值 | 描述 |
|------|----|------|
| ENABLED | 启用 | 用户正常状态 |
| DISABLED | 禁用 | 用户被禁用 |
| LOCKED | 锁定 | 用户被锁定 |

#### 报告状态字典
| 编码 | 值 | 描述 |
|------|----|------|
| DRAFT | 草稿 | 报告草稿状态 |
| PUBLISHED | 已发布 | 报告已发布 |
| ARCHIVED | 已归档 | 报告已归档 |

#### 任务执行状态字典
| 编码 | 值 | 描述 |
|------|----|------|
| PENDING | 待执行 | 任务等待执行 |
| RUNNING | 执行中 | 任务正在执行 |
| SUCCESS | 成功 | 任务执行成功 |
| FAILED | 失败 | 任务执行失败 |

### 4.3 字典管理接口

#### 获取字典列表
```http
GET /api/dict/types
Authorization: Bearer {token}
```

#### 根据类型获取字典项
```http
GET /api/dict/items?type=USER_STATUS
Authorization: Bearer {token}
```

#### 添加字典项
```http
POST /api/dict/items
Authorization: Bearer {token}
Content-Type: application/json

{
  "dictType": "REPORT_STATUS",
  "dictCode": "DRAFT",
  "dictValue": "草稿",
  "sortOrder": 1,
  "description": "报告草稿状态"
}
```

## 5. 操作日志管理

### 5.1 日志记录内容

#### 操作类型
- 用户登录/登出
- 数据增删改查
- 系统配置变更
- 权限分配操作

#### 日志字段
```java
@Data
public class SysOperationLogDTO {
    private Long id;
    private String module;        // 模块名称
    private String operation;     // 操作类型
    private String description;   // 操作描述
    private String operator;      // 操作人
    private String ipAddress;     // IP地址
    private LocalDateTime operateTime; // 操作时间
    private String params;        // 请求参数
    private String result;        // 操作结果
}
```

### 5.2 日志查询接口

#### 查询操作日志
```http
GET /api/logs/operation?page=1&size=20&module=USER&startTime=2024-01-01&endTime=2024-12-31
Authorization: Bearer {token}
```

## 6. 菜单管理

### 6.1 菜单结构

#### 菜单层级
- 一级菜单：系统管理、报告管理、模板管理等
- 二级菜单：用户管理、角色管理、权限管理等
- 操作按钮：查看、新增、编辑、删除等

#### 菜单权限关联
```sql
CREATE TABLE sys_menu (
    id BIGINT PRIMARY KEY,
    parent_id BIGINT COMMENT '父菜单ID',
    menu_name VARCHAR(100) NOT NULL COMMENT '菜单名称',
    menu_type VARCHAR(20) COMMENT '菜单类型(MENU/BUTTON)',
    permission VARCHAR(100) COMMENT '权限标识',
    path VARCHAR(200) COMMENT '路由路径',
    icon VARCHAR(100) COMMENT '菜单图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    visible BOOLEAN DEFAULT true COMMENT '是否显示'
);
```

### 6.2 菜单接口

#### 获取用户菜单
```http
GET /api/menus/user
Authorization: Bearer {token}
```

#### 管理菜单
```http
GET /api/menus
POST /api/menus
PUT /api/menus/{id}
DELETE /api/menus/{id}
```

## 7. 安全配置

### 7.1 密码策略

#### 密码要求
- 最小长度：8位
- 必须包含字母和数字
- 定期强制修改密码
- 密码历史记录检查

#### 密码加密
```java
// 使用BCrypt加密密码
String encodedPassword = passwordEncoder.encode(rawPassword);
```

### 7.2 会话管理

#### 会话超时设置
- 默认会话超时：30分钟
- 记住我功能：7天
- 并发登录控制

#### 安全头部
- CSRF防护
- XSS防护
- 内容安全策略

## 8. 使用示例

### 8.1 完整的权限控制流程

```java
// 1. 用户登录
AuthController.login()

// 2. 获取用户权限
AuthController.getUserInfo()

// 3. 权限验证
@PreAuthorize("hasPermission('user:view')")
UserController.getUsers()

// 4. 操作记录
SysOperationLogController.logOperation()
```

### 8.2 配置管理示例

```java
// 获取系统配置
@Autowired
private SysConfigService configService;

public String getAppName() {
    return configService.getConfigValue("app.name", "默认应用名");
}

// 更新配置
configService.updateConfig("report.generate.frequency", "DAILY");
```

### 8.3 字典使用示例

```java
// 获取字典项
@Autowired
private SysDictionaryService dictService;

public List<DictItem> getUserStatusOptions() {
    return dictService.getDictItems("USER_STATUS");
}

// 在业务逻辑中使用字典
if (user.getStatus().equals(dictService.getDictCode("USER_STATUS", "ENABLED"))) {
    // 用户启用状态逻辑
}
```

## 9. 最佳实践

### 9.1 权限设计原则
- 最小权限原则：用户只拥有完成工作所需的最小权限
- 职责分离：不同角色负责不同功能模块
- 定期审计：定期检查权限分配情况

### 9.2 配置管理建议
- 敏感配置加密存储
- 配置变更记录审计日志
- 生产环境配置版本控制

### 9.3 字典管理规范
- 字典编码统一规范
- 避免硬编码，使用字典项
- 定期清理无效字典项

## 10. 故障排除

### 10.1 常见问题

#### 权限不足错误
```json
{
  "code": 403,
  "message": "权限不足"
}
```
**解决方案**：检查用户角色和权限分配

#### 登录失败
```json
{
  "code": 401,
  "message": "用户名或密码错误"
}
```
**解决方案**：验证用户名密码，检查用户状态

#### 配置读取失败
**解决方案**：检查配置项是否存在，配置格式是否正确

### 10.2 日志分析

查看系统日志定位问题：
- 认证失败日志
- 权限验证日志
- 配置操作日志
- 字典操作日志

---

*本文档最后更新日期：2024年4月*  
*适用于系统管理模块 v1.0*