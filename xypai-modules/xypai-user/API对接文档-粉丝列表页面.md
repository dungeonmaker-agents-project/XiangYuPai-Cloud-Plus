# XyPai-User 粉丝列表页面 API 对接文档

> **版本**: v1.0.0
>
> **更新日期**: 2025-11-29
>
> **服务端口**: 9401 (User) / 9211 (Auth)
>
> **接口前缀**: `/api/user/`

---

## 目录

1. [通用说明](#通用说明)
2. [页面信息](#页面信息)
3. [用户认证接口](#用户认证接口)
4. [粉丝管理接口](#粉丝管理接口)
5. [错误码说明](#错误码说明)
6. [集成测试用例](#集成测试用例)

---

## 通用说明

### 基础URL

```
# 开发环境（通过网关）
http://localhost:8080

# 直连服务
xypai-auth: http://localhost:9211
xypai-user: http://localhost:9401
```

### 认证方式

需要认证的接口必须在请求头中携带 Token：

```http
Authorization: Bearer <access_token>
```

### 统一响应格式

所有接口返回统一的 JSON 格式：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": { ... }
}
```

### 分页格式

分页接口返回 TableDataInfo 格式：

```json
{
  "code": 200,
  "msg": "操作成功",
  "rows": [...],
  "total": 100
}
```

---

## 页面信息

| 属性 | 值 |
|------|------|
| 页面路由 | /profile/fans |
| 页面名称 | 粉丝列表 |
| 用户角色 | 登录用户 |
| 页面类型 | 列表页面 |

### 涉及的后端服务

| 服务 | 端口 | 功能 |
|------|------|------|
| xypai-auth | 9211 | 用户认证 |
| xypai-user | 9401 | 粉丝管理 |

### 功能说明

本页面主要展示粉丝相关功能：
- 获取粉丝列表
- 搜索粉丝
- 回关/取消关注粉丝

---

## 用户认证接口

### 1. SMS 登录

用户通过短信验证码登录。

**请求**

```http
POST /xypai-auth/api/auth/login/sms
Content-Type: application/json
```

**请求体**

```json
{
  "countryCode": "+86",
  "mobile": "13800000001",
  "verificationCode": "123456"
}
```

**响应示例**

```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "userId": 10001,
    "isNewUser": false
  }
}
```

---

## 粉丝管理接口

### 1. 获取粉丝列表

获取当前用户的粉丝列表。

**请求**

```http
GET /xypai-user/api/user/relation/fans
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 20 | 每页数量 |
| keyword | string | 否 | - | 搜索关键词（昵称） |

**请求示例**

```http
GET /xypai-user/api/user/relation/fans?pageNum=1&pageSize=20
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "rows": [
    {
      "userId": 10002,
      "nickname": "粉丝用户",
      "avatar": "https://cdn.example.com/avatar/10002.jpg",
      "gender": "female",
      "bio": "这是粉丝的简介",
      "isFollowing": false,
      "isMutual": false,
      "followTime": "2025-11-28 10:00:00"
    }
  ],
  "total": 1
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | long | 用户ID |
| nickname | string | 昵称 |
| avatar | string | 头像URL |
| gender | string | 性别: male/female |
| bio | string | 个人简介 |
| isFollowing | boolean | 当前用户是否已关注该粉丝 |
| isMutual | boolean | 是否互相关注 |
| followTime | string | 关注时间 |

---

### 2. 搜索粉丝

根据关键词搜索粉丝。

**请求**

```http
GET /xypai-user/api/user/relation/fans?keyword=xxx
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 是 | 搜索关键词（匹配昵称） |
| pageNum | integer | 否 | 页码 |
| pageSize | integer | 否 | 每页数量 |

**请求示例**

```http
GET /xypai-user/api/user/relation/fans?keyword=User&pageNum=1&pageSize=20
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "rows": [
    {
      "userId": 10002,
      "nickname": "User_12345",
      "avatar": "https://cdn.example.com/avatar/10002.jpg",
      "isFollowing": false
    }
  ],
  "total": 1
}
```

---

### 3. 回关粉丝

关注一个粉丝用户（回关）。

**请求**

```http
POST /xypai-user/api/user/relation/follow/{userId}
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 要关注的用户ID |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

---

### 4. 取消关注

取消关注某个用户。

**请求**

```http
DELETE /xypai-user/api/user/relation/follow/{userId}
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 要取消关注的用户ID |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权，请先登录 |
| 404 | 用户不存在 |
| 500 | 服务器内部错误 |

### 常见业务错误

| 错误信息 | 说明 |
|----------|------|
| 用户不存在 | userId 对应的用户不存在 |
| 不能关注自己 | 尝试关注自己 |
| 已经关注该用户 | 重复关注 |
| 未关注该用户 | 取消关注时未关注过该用户 |

---

## 集成测试用例

### 测试环境配置

```
Gateway:    http://localhost:8080
xypai-auth: http://localhost:9211 (认证服务)
xypai-user: http://localhost:9401 (用户服务)
```

**依赖服务**: Nacos, Redis, MySQL

---

### 测试场景: 粉丝列表页面 (AppFansListPageTest)

测试粉丝列表页面的完整功能。

#### 测试1: 准备测试数据 - 创建粉丝用户

```java
// 1. 创建当前用户
// 接口: POST /xypai-auth/api/auth/login/sms

// 2. 创建粉丝用户
// 接口: POST /xypai-auth/api/auth/login/sms (不同手机号)

// 3. 粉丝用户关注当前用户
// 接口: POST /xypai-user/api/user/relation/follow/{userId}

// 断言
- 粉丝用户创建成功
- 关注操作成功
```

#### 测试2: 获取粉丝列表

```java
// 接口: GET /xypai-user/api/user/relation/fans?pageNum=1&pageSize=20
// 请求头: Authorization: Bearer {token}

// 响应
{
  "rows": [...],
  "total": 1
}

// 断言
- rows != null
- total >= 1
```

#### 测试3: 搜索粉丝

```java
// 接口: GET /xypai-user/api/user/relation/fans?keyword=User&pageNum=1&pageSize=20
// 请求头: Authorization: Bearer {token}

// 断言
- rows != null
```

#### 测试4: 回关粉丝

```java
// 接口: POST /xypai-user/api/user/relation/follow/{fanUserId}
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "msg": "操作成功"
}

// 断言
- code == 200
```

#### 测试5: 取消关注粉丝

```java
// 接口: DELETE /xypai-user/api/user/relation/follow/{fanUserId}
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "msg": "操作成功"
}

// 断言
- code == 200
```

---

### 运行测试

```bash
# 进入用户服务目录
cd xypai-modules/xypai-user

# 运行粉丝列表页面测试
mvn test -Dtest=AppFansListPageTest

# 运行所有测试
mvn test
```

---

### 测试流程图

```
┌─────────────────────────────────────────────────────────────┐
│                    粉丝列表页面测试流程                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. 准备测试数据                                             │
│     ├── 创建当前用户                                         │
│     ├── 创建粉丝用户                                         │
│     └── 粉丝用户关注当前用户                                 │
│                                                             │
│  2. 获取粉丝列表                                             │
│     GET /xypai-user/api/user/relation/fans                  │
│     └── 验证粉丝数量                                         │
│                                                             │
│  3. 搜索粉丝                                                 │
│     GET /api/user/relation/fans?keyword=xxx                 │
│     └── 验证搜索结果                                         │
│                                                             │
│  4. 回关粉丝                                                 │
│     POST /xypai-user/api/user/relation/follow/{userId}      │
│     └── 验证关注成功                                         │
│                                                             │
│  5. 取消关注                                                 │
│     DELETE /xypai-user/api/user/relation/follow/{userId}    │
│     └── 验证取消成功                                         │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

### 接口实现状态

| 接口 | 状态 | 说明 |
|------|------|------|
| GET /api/user/relation/fans | ✅ 已实现 | 获取粉丝列表 |
| GET /api/user/relation/fans?keyword=xxx | ✅ 已实现 | 搜索粉丝 |
| POST /api/user/relation/follow/{userId} | ✅ 已实现 | 关注用户 |
| DELETE /api/user/relation/follow/{userId} | ✅ 已实现 | 取消关注 |

---

**文档版本**: v1.0.0

**最后更新**: 2025-11-29

**测试文件**: `xypai-modules/xypai-user/src/test/java/org/dromara/user/AppFansListPageTest.java`
