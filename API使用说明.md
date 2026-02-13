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

### 1. 认证相关接口

#### 1.1 获取验证码

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

#### 1.2 用户登录（增强版，需要验证码）

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
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "captchaId": "captcha_1234567890",
    "captchaCode": "AB12"
  }'
```

**响应示例**:
```json
{
  "code": 200,
  "status": true,
  "message": "操作成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "username": "admin",
      "phone": "13800138000",
      "email": "admin@smartpark.com",
      "status": 1
    }
  },
  "timestamp": 1732982400000
}
```

#### 1.3 用户注册（增强版，需要验证码）

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

#### 1.4 Token刷新

**接口地址**: `POST /api/auth/refresh`

**请求头**:
```
Authorization: Bearer {refreshToken}
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
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  },
  "timestamp": 1732982400000
}
```

#### 1.5 用户登出

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

#### 1.6 验证Token

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

#### 1.7 获取登录日志

**接口地址**: `GET /api/auth/login-logs`

**请求参数**:
- `userId` (可选): 用户ID，不传则查询所有用户
- `limit` (可选): 限制条数，默认10

**请求示例**:
```bash
curl -X GET "http://localhost:8080/api/auth/login-logs?userId=1&limit=10"
```

### 2. 园区管理接口

#### 2.1 获取所有园区列表
**接口地址**: `GET /api/park-area/list`

#### 2.2 根据ID获取园区详情
**接口地址**: `GET /api/park-area/{id}`

#### 2.3 创建园区
**接口地址**: `POST /api/park-area`
```json
{
  "name": "园区A",
  "address": "北京市朝阳区",
  "totalSpaces": 100,
  "latitude": 39.9042,
  "longitude": 116.4074,
  "status": 1
}
```

#### 2.4 更新园区信息
**接口地址**: `PUT /api/park-area/{id}`

#### 2.5 删除园区
**接口地址**: `DELETE /api/park-area/{id}`

#### 2.6 根据状态获取园区列表
**接口地址**: `GET /api/park-area/status/{status}`

#### 2.7 搜索园区
**接口地址**: `GET /api/park-area/search?keyword={keyword}`

### 3. 车位分区管理接口

#### 3.1 获取所有车位分区列表
**接口地址**: `GET /api/parking-zone/list`

**请求参数**: 无

**响应示例**:
```json
{
  "code": 200,
  "status": true,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "parkAreaId": 1,
      "parkAreaName": "园区A",
      "zoneName": "A区",
      "description": "A区车位",
      "sortOrder": 1,
      "status": 1,
      "createTime": "2024-01-01T10:00:00",
      "updateTime": "2024-01-01T10:00:00"
    }
  ],
  "timestamp": 1732982400000
}
```

#### 3.2 根据ID获取车位分区详情
**接口地址**: `GET /api/parking-zone/{id}`

**请求参数**:
- `id`: 分区ID（路径参数）

#### 3.3 创建车位分区
**接口地址**: `POST /api/parking-zone`

**请求参数**:
```json
{
  "parkAreaId": 1,
  "zoneName": "B区",
  "description": "B区车位",
  "sortOrder": 2,
  "status": 1
}
```

**注意事项**:
- 同一个园区内分区名称必须唯一
- 如果园区已存在相同名称的分区，会返回错误（RC1301）

#### 3.4 更新车位分区信息
**接口地址**: `PUT /api/parking-zone/{id}`

#### 3.5 删除车位分区
**接口地址**: `DELETE /api/parking-zone/{id}`

#### 3.6 根据园区ID获取车位分区列表
**接口地址**: `GET /api/parking-zone/park-area/{parkAreaId}`

#### 3.7 根据状态获取车位分区列表
**接口地址**: `GET /api/parking-zone/status/{status}`

#### 3.8 搜索车位分区（按分区名称）
**接口地址**: `GET /api/parking-zone/search?keyword={keyword}`

#### 3.9 批量更新分区状态
**接口地址**: `PUT /api/parking-zone/batch/status?ids=1,2,3&status=1`

**请求参数**:
- `ids`: 分区ID列表，用逗号分隔
- `status`: 状态（0-禁用，1-启用）

#### 3.10 批量删除分区
**接口地址**: `DELETE /api/parking-zone/batch?ids=1,2,3`

**请求参数**:
- `ids`: 分区ID列表，用逗号分隔

### 4. 车位管理接口

#### 4.1 获取所有车位列表
**接口地址**: `GET /api/parking-space/list`

#### 4.2 根据ID获取车位详情
**接口地址**: `GET /api/parking-space/{id}`

#### 4.3 创建车位
**接口地址**: `POST /api/parking-space`
```json
{
  "parkAreaId": 1,
  "zoneId": 1,
  "spaceNumber": "A-101",
  "spaceType": 2,
  "status": 1,
  "latitude": 39.9042,
  "longitude": 116.4074
}
```

#### 4.4 更新车位
**接口地址**: `PUT /api/parking-space/{id}`

#### 4.5 删除车位
**接口地址**: `DELETE /api/parking-space/{id}`

#### 4.6 根据园区ID获取车位列表
**接口地址**: `GET /api/parking-space/park-area/{parkAreaId}`

#### 4.7 根据分区ID获取车位列表
**接口地址**: `GET /api/parking-space/zone/{zoneId}`

#### 4.8 根据车位状态获取车位列表
**接口地址**: `GET /api/parking-space/status/{status}`

#### 4.9 根据车位类型获取车位列表
**接口地址**: `GET /api/parking-space/type/{spaceType}`

#### 4.10 获取空闲车位列表
**接口地址**: `GET /api/parking-space/available`

#### 4.11 批量更新车位状态
**接口地址**: `PUT /api/parking-space/batch-update-status?ids=1,2,3&status=1`

### 5. 车辆管理接口

#### 5.1 获取所有车辆列表
**接口地址**: `GET /api/vehicle/list`

#### 5.2 根据ID获取车辆详情
**接口地址**: `GET /api/vehicle/{id}`

#### 5.3 创建车辆
**接口地址**: `POST /api/vehicle`
```json
{
  "userId": 1,
  "plateNumber": "京A12345",
  "isDefault": 1,
  "vehicleType": 1,
  "brand": "特斯拉",
  "color": "黑色",
  "status": 1
}
```

#### 5.4 更新车辆
**接口地址**: `PUT /api/vehicle/{id}`

#### 5.5 删除车辆
**接口地址**: `DELETE /api/vehicle/{id}`

#### 5.6 根据用户ID获取车辆列表
**接口地址**: `GET /api/vehicle/user/{userId}`

#### 5.7 根据车牌号获取车辆
**接口地址**: `GET /api/vehicle/plate-number/{plateNumber}`

#### 5.8 获取用户的默认车辆
**接口地址**: `GET /api/vehicle/user/{userId}/default`

#### 5.9 设置默认车辆
**接口地址**: `PUT /api/vehicle/user/{userId}/set-default/{vehicleId}`

#### 5.10 批量更新车辆状态
**接口地址**: `PUT /api/vehicle/batch-update-status?ids=1,2,3&status=1`

### 6. 预约管理接口

#### 6.1 获取所有预约列表
**接口地址**: `GET /api/reservation/list`

#### 6.2 根据ID获取预约详情
**接口地址**: `GET /api/reservation/{id}`

#### 6.3 创建预约
**接口地址**: `POST /api/reservation`
```json
{
  "userId": 1,
  "vehicleId": 1,
  "spaceId": 1,
  "reservationType": 1,
  "startTime": "2024-01-01T10:00:00",
  "endTime": "2024-01-01T18:00:00",
  "status": 1
}
```

#### 6.4 更新预约
**接口地址**: `PUT /api/reservation/{id}`

#### 6.5 删除预约
**接口地址**: `DELETE /api/reservation/{id}`

#### 6.6 根据用户ID获取预约列表
**接口地址**: `GET /api/reservation/user/{userId}`

#### 6.7 根据车辆ID获取预约列表
**接口地址**: `GET /api/reservation/vehicle/{vehicleId}`

#### 6.8 根据车位ID获取预约列表
**接口地址**: `GET /api/reservation/space/{spaceId}`

#### 6.9 审批预约
**接口地址**: `PUT /api/reservation/{id}/approve?approvedBy=1`

#### 6.10 拒绝预约
**接口地址**: `PUT /api/reservation/{id}/reject?approvedBy=1&rejectReason=车位已满`

#### 6.11 更新预约状态
**接口地址**: `PUT /api/reservation/{id}/status?status=2`

#### 6.12 记录到达时间
**接口地址**: `PUT /api/reservation/{id}/arrive?arriveTime=2024-01-01T10:05:00`

#### 6.13 记录离开时间
**接口地址**: `PUT /api/reservation/{id}/leave?leaveTime=2024-01-01T17:55:00&totalFee=50.00`

#### 6.14 检查车位可用性
**接口地址**: `GET /api/reservation/check-availability?spaceId=1&startTime=2024-01-01T10:00:00&endTime=2024-01-01T18:00:00`

#### 6.15 获取待审批预约列表
**接口地址**: `GET /api/reservation/pending-approval`

#### 6.16 获取用户当前有效预约
**接口地址**: `GET /api/reservation/user/{userId}/current-valid`

### 7. 其他业务接口

#### 7.1 进出记录管理
- `GET /api/access-log/list` - 获取进出记录列表
- `GET /api/access-log/{id}` - 根据ID获取进出记录详情
- `POST /api/access-log` - 创建进出记录
- `GET /api/access-log/plate/{plateNumber}` - 根据车牌号查询进出记录

#### 7.2 黑名单管理
- `GET /api/blacklist/list` - 获取黑名单列表
- `POST /api/blacklist` - 添加黑名单
- `DELETE /api/blacklist/{id}` - 移除黑名单

#### 7.3 支付记录管理
- `GET /api/payment-record/list` - 获取支付记录列表
- `GET /api/payment-record/reservation/{reservationId}` - 根据预约ID获取支付记录

#### 7.4 异常上报管理
- `GET /api/exception-report/list` - 获取异常上报列表
- `POST /api/exception-report` - 提交异常上报
- `PUT /api/exception-report/{id}/handle` - 处理异常上报

#### 7.5 道闸设备管理
- `GET /api/gate-device/list` - 获取道闸设备列表
- `POST /api/gate-device` - 添加道闸设备
- `PUT /api/gate-device/{id}/status` - 更新设备状态

#### 7.6 操作日志管理
- `GET /api/operation-log/list` - 获取操作日志列表
- `GET /api/operation-log/user/{userId}` - 根据用户ID获取操作日志

### 8. 角色权限管理接口

#### 8.1 获取用户角色信息
**接口地址**: `GET /api/role/user/{userId}`

#### 8.2 获取所有角色
**接口地址**: `GET /api/role/list`

#### 8.3 创建角色
**接口地址**: `POST /api/role/create`

#### 8.4 分配用户角色
**接口地址**: `POST /api/role/assign`

#### 8.5 获取角色权限
**接口地址**: `GET /api/role/permissions/{roleId}`

#### 8.6 分配角色权限
**接口地址**: `POST /api/role/assign-permissions`

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
- `1300`: 数据不存在

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
mysql -u root -p Smart_Park_Parking_Management_System < src/main/resources/sql/smart_park_parking_management_system.sql
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
3. Token黑名单会随Token过期自动清理
4. 所有敏感操作都需要验证码保护
5. 建议在生产环境中启用HTTPS

## 快速开始

### 1. 环境准备
- JDK 17+
- MySQL 5.7+/8.0+
- Maven 3.6+

### 2. 数据库配置
修改 `src/main/resources/application.properties` 中的数据库连接信息：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/Smart_Park?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=123456
```

### 3. 启动应用
```bash
mvn clean install
mvn spring-boot:run
```

### 4. 测试API
使用Postman或curl测试API：
```bash
# 获取验证码
curl -X GET "http://localhost:8080/api/auth/captcha"

# 用户登录
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "captchaId": "captcha_1234567890",
    "captchaCode": "AB12"
  }'
```

### 5. 访问Swagger文档
项目集成了Swagger API文档，启动后访问：
```
http://localhost:8080/swagger-ui.html
```

## 常见问题

### Q1: 验证码获取失败
A: 检查验证码服务是否正常启动，验证码表是否创建。

### Q2: 登录返回401错误
A: 检查用户名密码是否正确，验证码是否有效。

### Q3: Token验证失败
A: 检查Token是否过期或被加入黑名单。

### Q4: 数据库连接失败
A: 检查application.properties中的数据库配置是否正确。

### Q5: 权限不足
A: 检查用户角色和权限配置。

## 版本更新

### v1.0.0 (2024-11-30)
- 初始版本发布
- 实现JWT认证系统
- 完成RBAC权限管理
- 集成验证码功能
- 添加登录日志审计

### v1.1.0 (计划中)
- 集成微信小程序登录
- 添加支付功能
- 优化性能监控
- 增强安全防护
