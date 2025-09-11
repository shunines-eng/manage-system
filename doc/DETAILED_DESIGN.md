# 个人信息管理系统详细设计文档

## 1. 模块详细设计

### 1.1 类图设计

```plaintext
+----------------+          +----------------------+          +------------------------+
|   User         |          |   UserService        |          |   CustomUserDetails    |
|----------------|          |----------------------|          |------------------------|
| - Long id      |          | + registerUser()     |          | + loadUserByUsername() |
| - String username |       | + findByUsername()   |          +------------------------+
| - String password |       | + existsByUsername() |                     ^
| - String email    |       | + existsByEmail()    |                     |
| - String phone    |       | + authenticate()     |          +------------------------+
| - String fullName |       | + updatePassword()   |          |   UserDetails          |
| - String role     |       | + ...                |          |------------------------|
| - Boolean enabled |       +----------------------+          | + getAuthorities()     |
| - Date createTime |                  |                      | + getPassword()        |
| - Date updateTime |                  |                      | + getUsername()        |
| - Integer loginAttempts |            |                      | + isAccountNonExpired()|
| - Date lockTime    |                 |                      +------------------------+
| - String verificationToken |         | implements
+----------------+            +----------------------+          +-----------------------+
        |                     |   UserServiceImpl    |          |   JwtTokenProvider    |
        |                     |----------------------|          |-----------------------|
        |                     | - UserRepository     |          | + generateToken()     |
        |                     | - PasswordEncoder    |          | + getUsernameFromJWT()|
        |                     +----------------------+          | + validateToken()     |
        |                                                       +-----------------------+
        |                                                               ^
        |                                                               |
        |                                                     +-----------------------+
        |                                                     |   JwtAuthentication  |
        |                                                     |   Filter             |
        |                                                     +-----------------------+
        | uses
+-----------------+
|   UserRepository|
|-----------------|
| + findByUsername() |
| + existsByUsername()|
| + existsByEmail()  |
| + save()        |
+-----------------+
```

### 1.2 核心接口说明

**UserService 接口**:
- `User registerUser(User user)`: 注册新用户
- `Optional<User> findByUsername(String username)`: 根据用户名查找用户
- `boolean existsByUsername(String username)`: 检查用户名是否存在
- `boolean existsByEmail(String email)`: 检查邮箱是否存在
- `User authenticate(String username, String password)`: 用户认证
- `void updatePassword(Long userId, String newPassword)`: 更新密码

**AdminOperationLogService 接口**:
- `void logOperation()`: 记录操作日志
- `Page<AdminOperationLog> findLogs()`: 查询日志
- `Map<String, Long> getLogStatistics()`: 获取日志统计

## 2. 数据库设计

### 2.1 ER 图

```plaintext
users
+---------------------+---------+-------------------+
| 字段名               | 类型     | 说明             |
+---------------------+---------+-------------------+
| id                  | BIGINT  | 主键             |
| username            | VARCHAR | 用户名，唯一     |
| password            | VARCHAR | 加密密码         |
| email               | VARCHAR | 邮箱，唯一       |
| phone               | VARCHAR | 手机号           |
| full_name           | VARCHAR | 全名             |
| role                | VARCHAR | 角色             |
| enabled             | BOOLEAN | 是否启用         |
| create_time         | TIMESTAMP| 创建时间         |
| update_time         | TIMESTAMP| 更新时间         |
| last_login_time     | TIMESTAMP| 最后登录时间     |
| login_attempts      | INTEGER | 登录尝试次数     |
| lock_time           | TIMESTAMP| 锁定时间         |
| verification_token  | VARCHAR | 验证令牌         |
+---------------------+---------+-------------------+

admin_operation_logs
+---------------------+---------+-------------------+
| 字段名               | 类型     | 说明             |
+---------------------+---------+-------------------+
| id                  | BIGINT  | 主键             |
| admin_username      | VARCHAR | 管理员用户名     |
| operation_type      | VARCHAR | 操作类型         |
| object_type         | VARCHAR | 操作对象类型     |
| object_id           | BIGINT  | 操作对象ID       |
| object_name         | VARCHAR | 操作对象名称     |
| operation_time      | TIMESTAMP| 操作时间         |
| success             | BOOLEAN | 是否成功         |
| ip_address          | VARCHAR | IP地址           |
| user_agent          | VARCHAR | 用户代理         |
+---------------------+---------+-------------------+
```

### 2.2 关系说明
- users 表存储用户基本信息
- admin_operation_logs 表存储管理员操作日志
- 两表之间通过 admin_username 关联（非外键关系）

## 3. 算法设计

### 3.1 JWT 令牌生成算法

**伪代码**:
```
函数 generateToken(authentication):
    输入: authentication - 认证信息
    输出: JWT 令牌字符串
    
    userDetails ← authentication.getPrincipal()
    now ← 当前时间
    expiryDate ← now + jwtExpirationInMs
    
    token ← Jwts.builder()
        .setSubject(userDetails.getUsername())
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(SignatureAlgorithm.HS512, jwtSecret)
        .compact()
    
    返回 token
```

### 3.2 密码加密算法

**伪代码**:
```
函数 encode(rawPassword):
    输入: rawPassword - 原始密码
    输出: 加密后的密码字符串
    
    md ← MessageDigest.getInstance("MD5")
    array ← md.digest(rawPassword.toString().getBytes())
    sb ← new StringBuffer()
    
    对于 array 中的每个字节:
        hex ← Integer.toHexString((byte & 0xFF) | 0x100).substring(1, 3)
        sb.append(hex)
    
    返回 sb.toString()
```

## 4. 接口规范

### 4.1 REST API 设计原则

- 使用 HTTP 状态码表示操作结果
- 使用 JSON 作为数据交换格式
- 资源命名使用复数形式
- 认证使用 JWT Token

### 4.2 主要接口列表

| 方法   | 路径                    | 说明             | 权限要求   |
|--------|-------------------------|------------------|------------|
| POST   | /auth/login             | 用户登录         | 无         |
| POST   | /auth/register          | 用户注册         | 无         |
| GET    | /api/users/profile      | 获取用户资料     | ROLE_USER  |
| PUT    | /api/users/profile      | 更新用户资料     | ROLE_USER  |
| GET    | /api/admin/users        | 获取用户列表     | ROLE_ADMIN |
| PUT    | /api/admin/users/{id}   | 更新用户信息     | ROLE_ADMIN |
| DELETE | /api/admin/users/{id}   | 删除用户         | ROLE_ADMIN |
| GET    | /api/admin/logs         | 获取操作日志     | ROLE_ADMIN |

### 4.3 请求/响应示例

**登录请求**:
```json
{
  "username": "testuser",
  "password": "password123",
  "captcha": "ABCD"
}
```

**登录成功响应**:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "role": "ROLE_USER"
  }
}
```

## 5. 错误处理

### 5.1 错误码规范

| HTTP 状态码 | 错误码          | 说明                 |
|-------------|-----------------|----------------------|
| 400         | BAD_REQUEST     | 请求参数错误         |
| 401         | UNAUTHORIZED    | 未授权               |
| 403         | FORBIDDEN       | 禁止访问             |
| 404         | NOT_FOUND       | 资源不存在           |
| 500         | INTERNAL_ERROR  | 服务器内部错误       |

### 5.2 异常处理机制

- 使用 Spring 的 @ControllerAdvice 进行全局异常处理
- 自定义异常类区分业务异常和系统异常
- 记录异常日志便于排查问题

### 5.3 错误响应格式

```json
{
  "timestamp": "2023-10-01T12:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "用户名已存在",
  "path": "/auth/register"
}
```

## 设计验证与约束说明

### 设计验证
1. **功能验证**: 通过单元测试覆盖核心业务逻辑
2. **安全验证**: 使用安全扫描工具检查漏洞
3. **性能验证**: 进行压力测试确保系统性能

### 约束说明
1. **技术约束**: 
   - 使用 Spring Boot 2.x 框架
   - 数据库使用 H2（开发环境）
   - 前端使用 Vue.js
2. **安全约束**:
   - 密码使用 MD5 加密存储
   - API 访问需要 JWT 认证
   - 管理员操作需要记录日志
3. **性能约束**:
   - 页面响应时间 < 2 秒
   - 支持并发用户数 ≥ 100
4. **可用性约束**:
   - 系统可用性 ≥ 99.9%
   - 支持主流浏览器访问

### 部署约束
- 需要 Java 11+ 运行环境
- 需要配置 SMTP 服务器发送邮件
- 需要配置正确的跨域策略

---
*文档版本: 1.0*
*最后更新: 2023-10-01*
