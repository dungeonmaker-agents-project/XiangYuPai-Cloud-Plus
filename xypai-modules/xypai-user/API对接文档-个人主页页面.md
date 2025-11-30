# XyPai-User 个人主页页面 API 对接文档

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
4. [个人主页接口](#个人主页接口)
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

---

## 页面信息

| 属性 | 值 |
|------|------|
| 页面路由 | /profile |
| 页面名称 | 个人主页 |
| 用户角色 | 登录用户 |
| 页面类型 | Tab页面 |

### 涉及的后端服务

| 服务 | 端口 | 功能 |
|------|------|------|
| xypai-auth | 9211 | 用户认证 |
| xypai-user | 9401 | 个人主页数据 |

### 页面Tab结构

| Tab | 说明 |
|------|------|
| 动态 | 用户发布的动态列表 |
| 收藏 | 用户收藏的内容列表 |
| 点赞 | 用户点赞的内容列表 |

### 功能说明

本页面主要展示个人主页相关功能：
- 获取个人主页头部信息（头像、昵称、统计数据等）
- 获取动态列表
- 获取收藏列表
- 获取点赞列表
- 获取个人资料信息

> **注意**: 点赞/收藏动态的接口属于 ContentService（内容服务），不在本模块中。

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

## 个人主页接口

### 1. 获取个人主页头部信息

获取个人主页的头部信息，包括头像、昵称、统计数据等。

**请求**

```http
GET /xypai-user/api/user/profile/header
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
    "bio": "这是我的个性签名",
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
| bio | string | 个性签名 |
| isVerified | boolean | 是否认证 |
| stats | object | 统计数据 |
| tags | array | 用户标签 |

**统计数据 (stats)**

| 字段 | 类型 | 说明 |
|------|------|------|
| followingCount | integer | 关注数 |
| fansCount | integer | 粉丝数 |
| likesCount | integer | 获赞数 |
| postsCount | integer | 动态数 |

---

### 2. 获取动态列表

获取当前用户发布的动态列表。

**请求**

```http
GET /xypai-user/api/user/profile/posts
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 10 | 每页数量 |

**请求示例**

```http
GET /xypai-user/api/user/profile/posts?page=1&pageSize=10
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "posts": [
      {
        "postId": 1001,
        "content": "今天的游戏状态超好！",
        "images": ["https://cdn.example.com/post/1001_1.jpg"],
        "likeCount": 50,
        "commentCount": 10,
        "createdAt": "2025-11-28 10:00:00"
      }
    ],
    "total": 50,
    "hasMore": true
  }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| posts | array | 动态列表 |
| total | integer | 总数 |
| hasMore | boolean | 是否有更多 |

---

### 3. 获取收藏列表

获取当前用户收藏的内容列表。

**请求**

```http
GET /xypai-user/api/user/profile/favorites
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 10 | 每页数量 |

**请求示例**

```http
GET /xypai-user/api/user/profile/favorites?page=1&pageSize=10
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "favorites": [
      {
        "postId": 2001,
        "content": "这篇攻略太棒了！",
        "author": {
          "userId": 10002,
          "nickname": "攻略达人",
          "avatar": "https://cdn.example.com/avatar/10002.jpg"
        },
        "likeCount": 100,
        "createdAt": "2025-11-27 15:00:00",
        "favoriteTime": "2025-11-28 10:00:00"
      }
    ],
    "total": 20,
    "hasMore": true
  }
}
```

---

### 4. 获取点赞列表

获取当前用户点赞的内容列表。

**请求**

```http
GET /xypai-user/api/user/profile/likes
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 10 | 每页数量 |

**请求示例**

```http
GET /xypai-user/api/user/profile/likes?page=1&pageSize=10
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "likes": [
      {
        "postId": 3001,
        "content": "分享今天的游戏战绩",
        "author": {
          "userId": 10003,
          "nickname": "游戏高手",
          "avatar": "https://cdn.example.com/avatar/10003.jpg"
        },
        "likeCount": 200,
        "createdAt": "2025-11-26 20:00:00",
        "likeTime": "2025-11-28 09:00:00"
      }
    ],
    "total": 30,
    "hasMore": true
  }
}
```

---

### 5. 获取个人资料信息

获取当前用户的详细资料信息。

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
    "age": 29,
    "residence": "广东省广州市",
    "height": 175,
    "weight": 65,
    "occupation": "软件工程师",
    "bio": "这是我的个性签名",
    "skills": [
      {
        "skillId": 1001,
        "skillName": "王者荣耀陪玩",
        "price": 50
      }
    ]
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
| birthday | string | 生日 |
| age | integer | 年龄 |
| residence | string | 居住地 |
| height | integer | 身高 (cm) |
| weight | integer | 体重 (kg) |
| occupation | string | 职业 |
| bio | string | 个性签名 |
| skills | array | 技能列表 |

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权，请先登录 |
| 500 | 服务器内部错误 |

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

### 测试场景: 个人主页页面 (AppProfilePageTest)

测试个人主页页面的所有功能。

#### 测试1: 新用户SMS注册

```java
// 接口: POST /xypai-auth/api/auth/login/sms
Map<String, String> loginRequest = new HashMap<>();
loginRequest.put("countryCode", "+86");
loginRequest.put("mobile", "13800000001");
loginRequest.put("verificationCode", "123456");

// 断言
- token != null
- userId != null
```

#### 测试2: 获取个人主页头部信息

```java
// 接口: GET /xypai-user/api/user/profile/header
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "data": {
    "userId": 10001,
    "nickname": "...",
    "stats": {
      "followingCount": 0,
      "fansCount": 0,
      "likesCount": 0
    }
  }
}

// 断言
- userId != null
- nickname != null
- stats != null
```

#### 测试3: 获取动态列表

```java
// 接口: GET /xypai-user/api/user/profile/posts?page=1&pageSize=10
// 请求头: Authorization: Bearer {token}

// 断言
- data != null
```

#### 测试4: 获取收藏列表

```java
// 接口: GET /xypai-user/api/user/profile/favorites?page=1&pageSize=10
// 请求头: Authorization: Bearer {token}

// 断言
- data != null
```

#### 测试5: 获取点赞列表

```java
// 接口: GET /xypai-user/api/user/profile/likes?page=1&pageSize=10
// 请求头: Authorization: Bearer {token}

// 断言
- data != null
```

#### 测试6: 获取个人资料信息

```java
// 接口: GET /xypai-user/api/user/profile/info
// 请求头: Authorization: Bearer {token}

// 断言
- userId != null
- nickname != null
```

---

### 运行测试

```bash
# 进入用户服务目录
cd xypai-modules/xypai-user

# 运行个人主页页面测试
mvn test -Dtest=AppProfilePageTest

# 运行所有测试
mvn test
```

---

### 测试流程图

```
┌─────────────────────────────────────────────────────────────┐
│                    个人主页页面测试流程                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. 用户登录                                                 │
│     POST /xypai-auth/api/auth/login/sms                     │
│     └── 获取 Token                                          │
│                                                             │
│  2. 获取个人主页头部信息                                      │
│     GET /xypai-user/api/user/profile/header                 │
│     ├── 头像、昵称                                           │
│     └── 统计数据（关注、粉丝、获赞）                          │
│                                                             │
│  3. 获取动态列表                                             │
│     GET /xypai-user/api/user/profile/posts                  │
│     └── 分页获取用户发布的动态                               │
│                                                             │
│  4. 获取收藏列表                                             │
│     GET /xypai-user/api/user/profile/favorites              │
│     └── 分页获取用户收藏的内容                               │
│                                                             │
│  5. 获取点赞列表                                             │
│     GET /xypai-user/api/user/profile/likes                  │
│     └── 分页获取用户点赞的内容                               │
│                                                             │
│  6. 获取个人资料信息                                         │
│     GET /xypai-user/api/user/profile/info                   │
│     └── 详细资料、技能列表                                   │
│                                                             │
│  💡 注意：                                                   │
│  - 点赞/收藏动态的接口属于 ContentService                    │
│  - 需在 xypai-content 模块实现                              │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

### 接口实现状态

| 接口 | 状态 | 说明 |
|------|------|------|
| GET /api/user/profile/header | ✅ 已实现 | 获取个人主页头部信息 |
| GET /api/user/profile/posts | ✅ 已实现 | 获取动态列表 |
| GET /api/user/profile/favorites | ✅ 已实现 | 获取收藏列表 |
| GET /api/user/profile/likes | ✅ 已实现 | 获取点赞列表 |
| GET /api/user/profile/info | ✅ 已实现 | 获取个人资料信息 |

---

**文档版本**: v1.0.0

**最后更新**: 2025-11-29

**测试文件**: `xypai-modules/xypai-user/src/test/java/org/dromara/user/AppProfilePageTest.java`
