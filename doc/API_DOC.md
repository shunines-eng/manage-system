# API 文档

## 1. API 概览

本文档描述了个人信息管理系统(PIM)的RESTful API接口。所有API端点均以`/api`或`/auth`为前缀，使用JSON格式进行数据交换。

**基础URL**: `http://localhost:8080`

**认证方式**: Bearer Token (JWT)

## 2. 端点列表

### 认证相关
- `POST /auth/register` - 用户注册
- `POST /auth/login` - 用户登录

### 验证码相关
- `GET /api/captcha/generate` - 生成验证码

### 用户管理 (需要管理员权限)
- `GET /api/admin/users` - 获取用户列表
- `GET /api/admin/users/{id}` - 获取指定用户信息
- `PUT /api/admin/users/{id}` - 更新用户信息
- `DELETE /api/admin/users/{id}` - 删除用户

### 日志管理 (需要管理员权限)
- `GET /api/admin/logs` - 获取操作日志列表
- `GET /api/admin/logs/statistics` - 获取日志统计信息

## 3. 详细端点说明

### 用户注册

**方法**: POST  
**路径**: `/auth/register`  
**认证**: 不需要

**请求体**:
```json
{
  "username": "string",
  "password": "string",
  "email": "string",
  "phone": "string",
  "fullName": "string"
}
```

**响应**:
- 201 Created: 注册成功
- 400 Bad Request: 参数验证失败或用户已存在

**示例**:
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com",
    "phone": "13800138000",
    "fullName": "Test User"
  }'
```

### 用户登录

**方法**: POST  
**路径**: `/auth/login`  
**认证**: 不需要

**请求体**:
```json
{
  "username": "string",
  "password": "string",
  "captcha": "string"
}
```

**响应**:
- 200 OK: 登录成功，返回JWT令牌和用户信息
- 401 Unauthorized: 用户名或密码错误
- 400 Bad Request: 验证码错误

**成功响应体**:
```json
{
  "token": "jwt_token_string",
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "role": "ROLE_USER"
  }
}
```

**示例**:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "captcha": "ABCD"
  }'
```

### 生成验证码

**方法**: GET  
**路径**: `/api/captcha/generate`  
**认证**: 不需要

**响应**:
- 200 OK: 返回Base64编码的验证码图片和验证码ID（存储在session中）

**响应体**:
```json
{
  "captchaId": "session_id",
  "imageBase64": "data:image/png;base64,..."
}
```

**示例**:
```bash
curl -X GET http://localhost:8080/api/captcha/generate
```

### 获取用户列表 (管理员)

**方法**: GET  
**路径**: `/api/admin/users`  
**认证**: 需要Bearer Token (管理员角色)

**查询参数**:
- `page` (可选): 页码，默认0
- `size` (可选): 每页大小，默认10
- `keyword` (可选): 搜索关键字

**响应**:
- 200 OK: 返回分页用户列表
- 403 Forbidden: 权限不足

**响应体**:
```json
{
  "content": [
    {
      "id": 1,
      "username": "admin",
      "email": "admin@example.com",
      "role": "ROLE_ADMIN"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

**示例**:
```bash
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer {jwt_token}"
```

## 4. 状态码定义

| 状态码 | 说明 |
|--------|------|
| 200 OK | 请求成功 |
| 201 Created | 资源创建成功 |
| 400 Bad Request | 请求参数错误 |
| 401 Unauthorized | 未认证或认证失败 |
| 403 Forbidden | 权限不足 |
| 404 Not Found | 资源不存在 |
| 500 Internal Server Error | 服务器内部错误 |

## 5. 速率限制

API设有速率限制以防止滥用：
- 认证端点：每分钟10次请求
- 普通API端点：每分钟100次请求
- 管理员API端点：每分钟50次请求

超过限制将返回 `429 Too Many Requests` 状态码。

## 6. SDK 使用示例

### JavaScript/Node.js

```javascript
const axios = require('axios');

class PIMClient {
  constructor(baseURL = 'http://localhost:8080') {
    this.baseURL = baseURL;
    this.token = null;
  }

  async login(username, password, captcha) {
    const response = await axios.post(`${this.baseURL}/auth/login`, {
      username,
      password,
      captcha
    });
    this.token = response.data.token;
    return response.data;
  }

  async getUsers(page = 0, size = 10) {
    const response = await axios.get(`${this.baseURL}/api/admin/users`, {
      params: { page, size },
      headers: { 'Authorization': `Bearer ${this.token}` }
    });
    return response.data;
  }
}

// 使用示例
const client = new PIMClient();
await client.login('admin', 'password', 'captcha');
const users = await client.getUsers(0, 10);
console.log(users);
```

### Java

```java
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class PIMClient {
    private String baseUrl = "http://localhost:8080";
    private String token;
    
    public LoginResponse login(String username, String password, String captcha) {
        RestTemplate restTemplate = new RestTemplate();
        LoginRequest request = new LoginRequest(username, password, captcha);
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
            baseUrl + "/auth/login", request, LoginResponse.class);
        this.token = response.getBody().getToken();
        return response.getBody();
    }
    
    public UserList getUsers(int page, int size) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        String url = baseUrl + "/api/admin/users?page=" + page + "&size=" + size;
        ResponseEntity<UserList> response = restTemplate.exchange(
            url, HttpMethod.GET, entity, UserList.class);
        return response.getBody();
    }
}
```

## 7. 更新日志

### v1.0.0 (2024-09-12)
- 初始版本发布
- 包含用户认证和管理功能
- 包含操作日志功能

### v1.1.0 (计划中)
- 添加用户个人资料管理
- 添加密码重置功能
- 增强日志查询功能
