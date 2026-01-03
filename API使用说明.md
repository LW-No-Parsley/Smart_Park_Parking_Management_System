# 智能停车场管理系统 - 增强版JWT登录接口使用说明

## 概述

本项目已成功实现基于JWT的增强版登录认证系统，包含以下核心功能：

1. **JWT登录认证** - 基于Token的无状态认证
2. **角色权限管理** - 完整的RBAC（基于角色的访问控制）系统
3. **Token黑名单机制** - 增强Token安全性，支持主动登出
4. **验证码功能** - 防止暴力破解，支持图形验证码
5. **密码重置功能** - 用户自助密码重置服务
6. **登录日志记录** - 完整的登录审计和安全监控

## 技术栈

- **后端框架**: Spring Boot 3.5.8
- **安全认证**: JWT (JSON Web Token) + Token黑名单
- **数据持久层**: MyBatis-Plus 3.5.14
- **数据库**: MySQL
- **密码加密**: MD5 (使用Hutool工具库)
- **验证码生成**: Hutool CaptchaUtil
- **权限管理**: RBAC (基于角色的访问控制)

## 数据库配置

### 1. 创建数据库
```sql
CREATE DATABASE Smart_Park_Parking_Management_System;
```

### 2. 执行初始化脚本
执行 `src/main/resources/sql/init_tables.sql` 文件创建所有表并插入初始化数据。

### 3. 默认用户
系统预置了以下默认用户：
- **管理员用户**: 
  - 用户名：admin，密码：admin123（MD5: e10adc3949ba59abbe56e057f20f883e）
  - 角色：系统管理员（拥有所有权限）
- **普通用户**: 可通过注册接口创建
- **停车场管理员**: 可通过角色管理接口创建

## API接口说明

### 1. 获取验证码

**接口地址**: `GET /api/auth/captcha`

**请求参数**: 无

**响应示例**:
```json
{
  "code": 200,
  "status": true,
  "message": "操作成功",
  "data": {
    "captchaId": "captcha_1234567890",
    "captchaImage": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAA...",
    "expiresIn": 300
  },
  "timestamp": 1732982400000
}
```

### 2. 用户登录（增强版，需要验证码）

**接口地址**: `POST /api/auth/login`

**请求参数**:
```json
{
  "username": "admin",
  "password": "admin123",
  "captchaId": "captcha_1234567890",
  "captchaCode": "AB12"
}
```

**请求示例**:
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=admin123&captchaId=captcha_1234567890&captchaCode=AB12"
```

**响应示例**:
```json
{
  "code": 200,
  "status": true,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "userInfo": {
      "username": "admin",
      "phone": "13800138000",
      "email": "admin@smartpark.com",
      "status": 1
    }
  },
  "timestamp": 1732982400000
}
```

**优化说明**:
1. 移除了`id`和`userId`字段，增强安全性
2. 移除了`roles`、`permissions`和`roleInfo`字段，减少响应数据量
3. 仅保留必要的用户基本信息

### 3. 用户注册（增强版，需要验证码）

**接口地址**: `POST /api/auth/register`

**请求参数**:
```json
{
  "username": "newuser",
  "password": "123456",
  "phone": "13800138003",
  "email": "newuser@smartpark.com",
  "captchaId": "captcha_1234567890",
  "captchaCode": "AB12"
}
```

**请求示例**:
```bash
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "123456",
    "phone": "13800138003",
    "email": "newuser@smartpark.com",
    "captchaId": "captcha_1234567890",
    "captchaCode": "AB12"
  }'
```

### 4. Token刷新

**接口地址**: `POST /api/auth/refresh`

**请求头**:
```
Authorization: Bearer {原token}
```

**请求示例**:
```bash
curl -X POST "http://localhost:8080/api/auth/refresh" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**响应示例**:
```json
{
  "code": 200,
  "status": true,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  },
  "timestamp": 1732982400000
}
```

### 5. 用户登出

**接口地址**: `POST /api/auth/logout`

**请求头**:
```
Authorization: Bearer {token}
```

**请求示例**:
```bash
curl -X POST "http://localhost:8080/api/auth/logout" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 6. 忘记密码

**接口地址**: `POST /api/auth/forgot-password`

**请求参数**:
```json
{
  "username": "admin",
  "email": "admin@smartpark.com",
  "captchaId": "captcha_1234567890",
  "captchaCode": "AB12"
}
```

**请求示例**:
```bash
curl -X POST "http://localhost:8080/api/auth/forgot-password" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&email=admin@smartpark.com&captchaId=captcha_1234567890&captchaCode=AB12"
```

### 7. 重置密码

**接口地址**: `POST /api/auth/reset-password`

**请求参数**:
```json
{
  "token": "reset_token_123456",
  "newPassword": "newpassword123"
}
```

**请求示例**:
```bash
curl -X POST "http://localhost:8080/api/auth/reset-password" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "token=reset_token_123456&newPassword=newpassword123"
```

### 8. 验证Token

**接口地址**: `POST /api/auth/validate`

**请求头**:
```
Authorization: Bearer {token}
```

**请求示例**:
```bash
curl -X POST "http://localhost:8080/api/auth/validate" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 9. 获取登录日志

**接口地址**: `GET /api/auth/login-logs`

**请求参数**:
- `userId` (可选): 用户ID，不传则查询所有用户
- `page` (可选): 页码，默认1
- `size` (可选): 每页大小，默认10

**请求示例**:
```bash
curl -X GET "http://localhost:8080/api/auth/login-logs?userId=1&page=1&size=10"
```

### 10. 角色权限管理接口

**获取用户角色信息**:
```bash
GET /api/role/user/{userId}
```

**获取所有角色**:
```bash
GET /api/role/list
```

**创建角色**:
```bash
POST /api/role/create
```

**分配用户角色**:
```bash
POST /api/role/assign
```

**获取角色权限**:
```bash
GET /api/role/permissions/{roleId}
```

**分配角色权限**:
```bash
POST /api/role/assign-permissions
```

## 错误码说明

系统使用统一的错误码机制，主要错误码包括：

- `200`: 操作成功
- `400`: 请求参数错误
- `401`: 未授权
- `403`: 禁止访问
- `404`: 资源未找到
- `500`: 服务器内部错误
- `600`: 用户不存在
- `601`: 用户名或密码错误
- `602`: 用户已被禁用
- `603`: 用户已存在
- `604`: 验证码错误或已过期
- `605`: Token已加入黑名单
- `606`: 角色不存在
- `607`: 权限不存在
- `608`: 密码重置token无效或已过期

## 安全特性

### 1. Token黑名单机制
- 支持主动登出，将Token加入黑名单
- 黑名单Token无法通过验证
- 定时清理过期的黑名单Token

### 2. 验证码保护
- 登录、注册、密码重置等敏感操作需要验证码
- 验证码5分钟有效，单次使用
- 防止暴力破解和自动化攻击

### 3. 登录日志审计
- 记录所有登录尝试（成功/失败）
- 记录IP地址、用户代理等信息
- 支持登录行为分析和异常检测

### 4. 角色权限控制
- 完整的RBAC权限管理系统
- 支持菜单权限、按钮权限、接口权限
- 细粒度的权限控制

### 5. 密码安全
- 密码MD5加密存储
- 密码重置需要邮箱验证
- 防止密码泄露和暴力破解

## 生产环境建议

1. 启用Spring Security的完整认证机制
2. 配置HTTPS加密传输
3. 使用更安全的密码加密算法（如BCrypt）
4. 配置API限流和防刷机制
5. 定期清理过期数据和日志

## 启动项目

1. 确保MySQL数据库已启动并创建相应数据库
2. 执行数据库初始化脚本：
```bash
mysql -u root -p Smart_Park_Parking_Management_System < src/main/resources/sql/init_tables.sql
```

3. 运行项目：
```bash
mvn spring-boot:run
```

4. 访问地址：`http://localhost:8080`

## 系统架构

### 核心组件
1. **JwtUtil**: JWT Token生成、验证、黑名单管理
2. **AuthService**: 认证服务，集成验证码、登录日志、Token管理
3. **RoleService**: 角色权限服务，实现RBAC系统
4. **CaptchaService**: 验证码服务，生成和验证图形验证码
5. **LoginLogService**: 登录日志服务，记录和分析登录行为

### 数据库设计
- `sys_user`: 用户表
- `sys_role`: 角色表
- `sys_permission`: 权限表
- `sys_user_role`: 用户角色关联表
- `sys_role_permission`: 角色权限关联表
- `sys_token_blacklist`: Token黑名单表
- `sys_login_log`: 登录日志表
- `sys_captcha`: 验证码表

## 注意事项

1. JWT token默认有效期为24小时
2. 验证码默认有效期为5分钟
3. Token黑名单会随Token
