# XyPai-User 他人主页页面 API 对接文档

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
4. [他人主页接口](#他人主页接口)
5. [用户关系接口](#用户关系接口)
6. [错误码说明](#错误码说明)
7. [集成测试用例](#集成测试用例)

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

---

## 页面信息

| 属性 | 值 |
|------|------|
| 页面路由 | /user/{userId} |
| 页面名称 | 他人主页 |
| 用户角色 | 登录用户 |
| 页面类型 | 详情页面 |

### 涉及的后端服务

| 服务 | 端口 | 功能 |
|------|------|------|
| xypai-auth | 9211 | 用户认证 |
| xypai-user | 9401 | 他人主页、用户关系 |

### 功能说明

本页面主要展示他人主页相关功能：
- 获取他人主页信息
- 关注/取消关注
- 获取用户技能列表
- 举报用户
- 拉黑用户

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

## 他人主页接口

### 1. 获取他人主页信息

获取指定用户的主页信息。

**请求**

```http
GET /xypai-user/api/user/profile/other/{userId}
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 目标用户ID |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "userId": 10002,
    "nickname": "他人昵称",
    "avatar": "https://cdn.example.com/avatar/10002.jpg",
    "gender": "female",
    "age": 25,
    "bio": "这是他人的个性签名",
    "followStatus": "none",
    "isVerified": true,
    "stats": {
      "followingCount": 100,
      "fansCount": 500,
      "likesCount": 1200,
      "postsCount": 50
    },
    "tags": ["游戏达人", "陪玩高手"]
  }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | long | 用户ID |
| nickname | string | 昵称 |
| avatar | string | 头像URL |
| gender | string | 性别: male/female |
| age | integer | 年龄 |
| bio | string | 个性签名 |
| followStatus | string | 关注状态: none/following/follower/mutual |
| isVerified | boolean | 是否认证 |
| stats | object | 统计数据 |
| tags | array | 用户标签 |

**关注状态说明**

| 值 | 说明 |
|------|------|
| none | 未关注 |
| following | 我已关注对方 |
| follower | 对方关注我 |
| mutual | 互相关注 |

---

### 2. 获取用户技能列表

获取指定用户的技能列表。

**请求**

```http
GET /xypai-user/api/user/skills/user/{userId}
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 目标用户ID |

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 20 | 每页数量 |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "rows": [
    {
      "skillId": 1001,
      "skillName": "王者荣耀陪玩",
      "skillType": "online",
      "price": 50,
      "gameName": "王者荣耀",
      "gameRank": "王者",
      "rating": 4.8,
      "orderCount": 128
    }
  ],
  "total": 1
}
```

---

### 3. 获取用户详细资料

获取当前登录用户的详细资料。

**请求**

```http
GET /xypai-user/api/user/profile/info
Authorization: Bearer <token>
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "userId": 10001,
    "nickname": "用户昵称",
    "avatar": "https://cdn.example.com/avatar/10001.jpg",
    "gender": "male",
    "birthday": "1995-06-15",
    "residence": "广东省广州市",
    "height": 175,
    "weight": 65,
    "occupation": "软件工程师",
    "bio": "这是我的个性签名"
  }
}
```

---

## 用户关系接口

### 1. 关注用户

关注指定用户。

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

### 2. 取消关注

取消关注指定用户。

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

### 3. 举报用户

举报指定用户。

**请求**

```http
POST /xypai-user/api/user/relation/report/{userId}
Content-Type: application/json
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 要举报的用户ID |

**请求体**

```json
{
  "reason": "违规行为",
  "description": "详细描述举报原因"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reason | string | 是 | 举报原因 |
| description | string | 否 | 详细描述 |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

---

### 4. 拉黑用户

拉黑指定用户。

**请求**

```http
POST /xypai-user/api/user/relation/block/{userId}
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 要拉黑的用户ID |

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
| 不能拉黑自己 | 尝试拉黑自己 |
| 已经拉黑该用户 | 重复拉黑 |
| 已经举报过该用户 | 重复举报 |

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

### 测试场景: 他人主页页面 (AppOtherUserProfilePageTest)

测试他人主页页面的完整功能。

#### 测试1: 新用户注册 - 准备测试数据

```java
// 1. 创建当前用户
// 接口: POST /xypai-auth/api/auth/login/sms

// 2. 创建目标用户
// 接口: POST /xypai-auth/api/auth/login/sms (不同手机号)

// 断言
- 当前用户创建成功
- 目标用户创建成功
```

#### 测试2: 获取他人主页信息

```java
// 接口: GET /xypai-user/api/user/profile/other/{targetUserId}
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "data": {
    "userId": 10002,
    "nickname": "...",
    "followStatus": "none"
  }
}

// 断言
- userId != null
- nickname != null
```

#### 测试3: 关注用户

```java
// 接口: POST /xypai-user/api/user/relation/follow/{targetUserId}
// 请求头: Authorization: Bearer {token}

// 断言
- code == 200
```

#### 测试4: 取消关注

```java
// 接口: DELETE /xypai-user/api/user/relation/follow/{targetUserId}
// 请求头: Authorization: Bearer {token}

// 断言
- code == 200
```

#### 测试5: 获取用户技能列表

```java
// 接口: GET /xypai-user/api/user/skills/user/{targetUserId}?pageNum=1&pageSize=20
// 请求头: Authorization: Bearer {token}

// 断言
- rows != null
```

#### 测试6: 获取用户详细资料

```java
// 接口: GET /xypai-user/api/user/profile/info
// 请求头: Authorization: Bearer {token}

// 断言
- userId != null
- nickname != null
```

#### 测试7: 举报用户

```java
// 接口: POST /xypai-user/api/user/relation/report/{targetUserId}
// 请求头: Authorization: Bearer {token}
// 请求体: {"reason": "测试举报", "description": "测试"}

// 断言
- code == 200 或功能未实现
```

#### 测试8: 拉黑用户

```java
// 接口: POST /xypai-user/api/user/relation/block/{targetUserId}
// 请求头: Authorization: Bearer {token}

// 断言
- code == 200 或功能未实现
```

---

### 运行测试

```bash
# 进入用户服务目录
cd xypai-modules/xypai-user

# 运行他人主页页面测试
mvn test -Dtest=AppOtherUserProfilePageTest

# 运行所有测试
mvn test
```

---

### 测试流程图

```
┌─────────────────────────────────────────────────────────────┐
│                    他人主页页面测试流程                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. 准备测试数据                                             │
│     ├── 创建当前用户                                         │
│     └── 创建目标用户                                         │
│                                                             │
│  2. 获取他人主页信息                                         │
│     GET /xypai-user/api/user/profile/other/{userId}         │
│     └── 验证用户信息、关注状态                               │
│                                                             │
│  3. 关注/取消关注                                            │
│     ├── POST /api/user/relation/follow/{userId}             │
│     └── DELETE /api/user/relation/follow/{userId}           │
│                                                             │
│  4. 获取用户技能列表                                         │
│     GET /xypai-user/api/user/skills/user/{userId}           │
│     └── 验证技能数量                                         │
│                                                             │
│  5. 举报/拉黑用户                                            │
│     ├── POST /api/user/relation/report/{userId}             │
│     └── POST /api/user/relation/block/{userId}              │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

### 接口实现状态

| 接口 | 状态 | 说明 |
|------|------|------|
| GET /api/user/profile/other/{userId} | ✅ 已实现 | 获取他人主页信息 |
| GET /api/user/profile/info | ✅ 已实现 | 获取当前用户详细资料 |
| GET /api/user/skills/user/{userId} | ✅ 已实现 | 获取用户技能列表 |
| POST /api/user/relation/follow/{userId} | ✅ 已实现 | 关注用户 |
| DELETE /api/user/relation/follow/{userId} | ✅ 已实现 | 取消关注 |
| POST /api/user/relation/report/{userId} | ⏳ 待确认 | 举报用户 |
| POST /api/user/relation/block/{userId} | ⏳ 待确认 | 拉黑用户 |

---

**文档版本**: v1.0.0

**最后更新**: 2025-11-29

**测试文件**: `xypai-modules/xypai-user/src/test/java/org/dromara/user/AppOtherUserProfilePageTest.java`
