# XyPai-App-BFF 搜索结果页面 API 对接文档

> **版本**: v1.0.0
>
> **更新日期**: 2025-11-29
>
> **服务端口**: 9400 (BFF) / 9211 (Auth)
>
> **接口前缀**: `/api/`

---

## 目录

1. [通用说明](#通用说明)
2. [页面信息](#页面信息)
3. [用户认证接口](#用户认证接口)
4. [综合搜索接口](#综合搜索接口)
5. [分Tab搜索接口](#分tab搜索接口)
6. [错误码说明](#错误码说明)
7. [集成测试用例](#集成测试用例)

---

## 通用说明

### 基础URL

```
# 开发环境（通过网关）
http://localhost:8080

# 直连服务
xypai-auth:    http://localhost:9211
xypai-app-bff: http://localhost:9400
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
| 文档路径 | XiangYuPai-Doc/Action-API/模块化架构/03-content模块/Frontend/07-搜索结果页面.md |
| 页面路由 | /search/results |
| 页面名称 | 搜索结果 |
| 用户角色 | 所有用户 |
| 页面类型 | Tab列表页面 |

### 涉及的后端服务

| 服务 | 端口 | 功能 |
|------|------|------|
| xypai-auth | 9211 | 用户认证 |
| xypai-app-bff | 9400 | 搜索结果聚合 |

### 页面Tab结构

| Tab | 类型标识 | 说明 |
|------|------|------|
| 全部 | all | 综合搜索结果 |
| 用户 | users | 用户账号搜索 |
| 下单 | orders | 服务提供者搜索（可下单） |
| 话题 | topics | 话题/标签搜索 |

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
  "mobile": "13900000001",
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
    "userId": 10001
  }
}
```

---

## 综合搜索接口

### 1. 执行综合搜索

提交搜索关键词，获取综合搜索结果及各Tab统计。

**请求**

```http
POST /xypai-app-bff/api/search/search
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "keyword": "王者",
  "type": "all",
  "pageNum": 1,
  "pageSize": 10
}
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| keyword | string | 是 | - | 搜索关键词 |
| type | string | 否 | all | 搜索类型: all/users/orders/topics |
| pageNum | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 10 | 每页数量 |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "keyword": "王者",
    "total": 45,
    "hasMore": true,
    "tabs": [
      {
        "type": "all",
        "label": "全部",
        "count": 45
      },
      {
        "type": "users",
        "label": "用户",
        "count": 12
      },
      {
        "type": "orders",
        "label": "下单",
        "count": 18
      },
      {
        "type": "topics",
        "label": "话题",
        "count": 15
      }
    ],
    "results": [
      {
        "itemType": "post",
        "post": {
          "postId": 1001,
          "title": "王者荣耀五排开黑",
          "content": "今晚八点王者荣耀五排...",
          "coverImage": "https://cdn.example.com/post/1001.jpg",
          "likeCount": 128,
          "commentCount": 32
        }
      },
      {
        "itemType": "user",
        "user": {
          "userId": 2001,
          "nickname": "王者大神",
          "avatar": "https://cdn.example.com/avatar/2001.jpg",
          "gender": "male",
          "relationStatus": "none"
        }
      }
    ]
  }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| keyword | string | 搜索关键词 |
| total | integer | 总结果数 |
| hasMore | boolean | 是否有更多数据 |
| tabs | array | Tab列表及各Tab结果统计 |
| tabs[].type | string | Tab类型标识 |
| tabs[].label | string | Tab显示名称 |
| tabs[].count | integer | 该Tab结果数量 |
| results | array | 搜索结果列表（混合类型） |
| results[].itemType | string | 结果项类型: post/user/order/topic |

---

## 分Tab搜索接口

### 1. 获取全部Tab结果

获取"全部"Tab下的混合搜索结果。

**请求**

```http
GET /xypai-app-bff/api/search/all
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| keyword | string | 是 | - | 搜索关键词 |
| pageNum | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 10 | 每页数量 |

**请求示例**

```http
GET /xypai-app-bff/api/search/all?keyword=王者&pageNum=1&pageSize=5
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "list": [
      {
        "itemType": "post",
        "post": {
          "postId": 1001,
          "title": "王者荣耀五排开黑",
          "content": "今晚八点王者荣耀五排...",
          "coverImage": "https://cdn.example.com/post/1001.jpg"
        }
      },
      {
        "itemType": "user",
        "user": {
          "userId": 2001,
          "nickname": "王者大神",
          "avatar": "https://cdn.example.com/avatar/2001.jpg"
        }
      }
    ],
    "total": 45,
    "hasMore": true
  }
}
```

---

### 2. 获取用户Tab结果

获取"用户"Tab下的用户搜索结果。

**请求**

```http
GET /xypai-app-bff/api/search/users
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| keyword | string | 是 | - | 搜索关键词 |
| pageNum | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 10 | 每页数量 |

**请求示例**

```http
GET /xypai-app-bff/api/search/users?keyword=游戏&pageNum=1&pageSize=10
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "list": [
      {
        "userId": 2001,
        "nickname": "游戏达人",
        "avatar": "https://cdn.example.com/avatar/2001.jpg",
        "gender": "male",
        "age": 25,
        "bio": "热爱游戏，专业陪玩",
        "relationStatus": "none",
        "tags": ["王者荣耀", "英雄联盟"],
        "isVerified": true,
        "isOnline": true
      },
      {
        "userId": 2002,
        "nickname": "游戏小姐姐",
        "avatar": "https://cdn.example.com/avatar/2002.jpg",
        "gender": "female",
        "age": 22,
        "bio": "声音甜美，游戏超棒",
        "relationStatus": "following",
        "tags": ["和平精英", "原神"],
        "isVerified": false,
        "isOnline": false
      }
    ],
    "total": 12,
    "hasMore": true
  }
}
```

**用户对象字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | long | 用户ID |
| nickname | string | 昵称 |
| avatar | string | 头像URL |
| gender | string | 性别: male/female |
| age | integer | 年龄 |
| bio | string | 个人简介 |
| relationStatus | string | 关系状态: none/following/follower/mutual |
| tags | array | 标签列表 |
| isVerified | boolean | 是否认证 |
| isOnline | boolean | 是否在线 |

---

### 3. 获取下单Tab结果

获取"下单"Tab下的服务提供者列表（可直接下单）。

**请求**

```http
GET /xypai-app-bff/api/search/orders
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| keyword | string | 是 | - | 搜索关键词 |
| pageNum | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 10 | 每页数量 |

**请求示例**

```http
GET /xypai-app-bff/api/search/orders?keyword=陪练&pageNum=1&pageSize=10
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "list": [
      {
        "userId": 2001,
        "nickname": "专业陪练",
        "avatar": "https://cdn.example.com/avatar/2001.jpg",
        "gender": "male",
        "distanceText": "1.5km",
        "distance": 1500,
        "price": {
          "amount": 50,
          "unit": "金币/小时",
          "displayText": "50金币/小时"
        },
        "tags": [
          {"name": "王者荣耀", "type": "game"},
          {"name": "王者段位", "type": "rank"}
        ],
        "rating": 4.8,
        "orderCount": 128,
        "isOnline": true,
        "skills": [
          {
            "skillId": 5001,
            "skillName": "王者荣耀陪玩",
            "description": "专业王者陪玩..."
          }
        ]
      }
    ],
    "total": 18,
    "hasMore": true
  }
}
```

**服务提供者字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | long | 用户ID |
| nickname | string | 昵称 |
| avatar | string | 头像URL |
| gender | string | 性别 |
| distanceText | string | 距离显示文本 |
| distance | integer | 距离（米） |
| price | object | 价格信息 |
| price.amount | integer | 价格金额 |
| price.unit | string | 价格单位 |
| price.displayText | string | 价格显示文本 |
| tags | array | 标签列表 |
| rating | decimal | 评分 |
| orderCount | integer | 接单数 |
| isOnline | boolean | 是否在线 |
| skills | array | 技能列表 |

---

### 4. 获取话题Tab结果

获取"话题"Tab下的话题搜索结果。

**请求**

```http
GET /xypai-app-bff/api/search/topics
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| keyword | string | 是 | - | 搜索关键词 |
| pageNum | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 10 | 每页数量 |

**请求示例**

```http
GET /xypai-app-bff/api/search/topics?keyword=游戏&pageNum=1&pageSize=10
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "list": [
      {
        "topicId": 6001,
        "topicName": "游戏日常",
        "icon": "🎮",
        "description": "分享游戏日常",
        "isHot": true,
        "hotLabel": "热门",
        "stats": {
          "posts": 50000,
          "views": 1200000,
          "participants": 15000
        },
        "coverImage": "https://cdn.example.com/topic/6001.jpg"
      },
      {
        "topicId": 6002,
        "topicName": "游戏攻略",
        "icon": "📖",
        "description": "各类游戏攻略分享",
        "isHot": false,
        "hotLabel": null,
        "stats": {
          "posts": 30000,
          "views": 800000,
          "participants": 10000
        },
        "coverImage": "https://cdn.example.com/topic/6002.jpg"
      }
    ],
    "total": 15,
    "hasMore": true
  }
}
```

**话题对象字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| topicId | long | 话题ID |
| topicName | string | 话题名称 |
| icon | string | 话题图标（emoji） |
| description | string | 话题描述 |
| isHot | boolean | 是否热门 |
| hotLabel | string | 热门标签（如"热门"、"新"） |
| stats | object | 统计信息 |
| stats.posts | integer | 动态数量 |
| stats.views | integer | 浏览量 |
| stats.participants | integer | 参与人数 |
| coverImage | string | 封面图URL |

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
Gateway:       http://localhost:8080
xypai-auth:    http://localhost:9211 (认证服务)
xypai-app-bff: http://localhost:9400 (BFF聚合服务)
```

**依赖服务**: Nacos, Redis, MySQL

---

### 测试场景: 搜索结果页面 (Page07_SearchResultsTest)

测试搜索结果页面的所有功能，包括综合搜索、分Tab搜索、分页等。

#### 测试1: 用户登录

```java
// 接口: POST /xypai-auth/api/auth/login/sms
Map<String, String> loginRequest = new HashMap<>();
loginRequest.put("countryCode", "+86");
loginRequest.put("mobile", "13900000001");
loginRequest.put("verificationCode", "123456");

// 响应
{
  "code": 200,
  "data": {
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "userId": 10001
  }
}
```

#### 测试2: 执行综合搜索（type=all）

```java
// 接口: POST /xypai-app-bff/api/search/search
// 请求头: Authorization: Bearer {token}
Map<String, Object> searchRequest = new HashMap<>();
searchRequest.put("keyword", "王者");
searchRequest.put("type", "all");
searchRequest.put("pageNum", 1);
searchRequest.put("pageSize", 10);

// 响应
{
  "code": 200,
  "data": {
    "keyword": "王者",
    "total": 45,
    "hasMore": true,
    "tabs": [
      {"type": "all", "label": "全部", "count": 45},
      {"type": "users", "label": "用户", "count": 12},
      {"type": "orders", "label": "下单", "count": 18},
      {"type": "topics", "label": "话题", "count": 15}
    ],
    "results": [...]
  }
}

// 断言
- keyword == "王者"
- tabs.size() == 4
- results != null
```

#### 测试3: 获取全部Tab结果

```java
// 接口: GET /xypai-app-bff/api/search/all?keyword=王者&pageNum=1&pageSize=5
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "data": {
    "list": [
      {"itemType": "post", "post": {...}},
      {"itemType": "user", "user": {...}}
    ],
    "total": 45,
    "hasMore": true
  }
}

// 断言
- list != null
- 可以包含不同itemType的结果
```

#### 测试4: 获取用户Tab结果

```java
// 接口: GET /xypai-app-bff/api/search/users?keyword=游戏&pageNum=1&pageSize=10
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "data": {
    "list": [
      {
        "userId": 2001,
        "nickname": "游戏达人",
        "gender": "male",
        "relationStatus": "none"
      }
    ],
    "total": 12
  }
}

// 断言
- list != null
- 每个用户都有userId、nickname、gender字段
```

#### 测试5: 获取下单Tab结果

```java
// 接口: GET /xypai-app-bff/api/search/orders?keyword=陪练&pageNum=1&pageSize=10
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "data": {
    "list": [
      {
        "userId": 2001,
        "nickname": "专业陪练",
        "distanceText": "1.5km",
        "price": {"amount": 50, "displayText": "50金币/小时"},
        "tags": [...],
        "isOnline": true
      }
    ],
    "total": 18
  }
}

// 断言
- list != null
- 每个服务提供者都有price、distanceText字段
```

#### 测试6: 获取话题Tab结果

```java
// 接口: GET /xypai-app-bff/api/search/topics?keyword=游戏&pageNum=1&pageSize=10
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "data": {
    "list": [
      {
        "topicId": 6001,
        "topicName": "游戏日常",
        "icon": "🎮",
        "isHot": true,
        "hotLabel": "热门",
        "stats": {
          "posts": 50000,
          "views": 1200000
        }
      }
    ],
    "total": 15
  }
}

// 断言
- list != null
- 每个话题都有topicName、stats字段
```

#### 测试7: 搜索特定关键词（台球）

```java
// 接口: POST /xypai-app-bff/api/search/search
Map<String, Object> searchRequest = new HashMap<>();
searchRequest.put("keyword", "台球");
searchRequest.put("type", "all");
searchRequest.put("pageNum", 1);
searchRequest.put("pageSize", 10);

// 响应
{
  "code": 200,
  "data": {
    "keyword": "台球",
    "tabs": [...]
  }
}

// 断言
- keyword == "台球"
```

#### 测试8: 搜索无结果关键词

```java
// 接口: GET /xypai-app-bff/api/search/all?keyword=不存在的关键词XYZABC&pageNum=1&pageSize=10

// 响应
{
  "code": 200,
  "data": {
    "list": [],
    "total": 0,
    "hasMore": false
  }
}

// 断言
- list.size() == 0 或 list == null
```

#### 测试9: 测试分页功能

```java
// 第一页
// 接口: GET /xypai-app-bff/api/search/all?keyword=王者&pageNum=1&pageSize=2
{
  "code": 200,
  "data": {
    "list": [...],  // 2条
    "hasMore": true
  }
}

// 第二页
// 接口: GET /xypai-app-bff/api/search/all?keyword=王者&pageNum=2&pageSize=2
{
  "code": 200,
  "data": {
    "list": [...],  // 2条
    "hasMore": true或false
  }
}

// 断言
- 第一页list != null
- 两页数据不重复
```

#### 测试10: 验证Tab统计信息一致性

```java
// 接口: POST /xypai-app-bff/api/search/search
Map<String, Object> searchRequest = new HashMap<>();
searchRequest.put("keyword", "游戏");
searchRequest.put("type", "all");
searchRequest.put("pageNum", 1);
searchRequest.put("pageSize", 100);

// 响应
{
  "code": 200,
  "data": {
    "tabs": [
      {"type": "all", "label": "全部", "count": 45},
      {"type": "users", "label": "用户", "count": 12},
      {"type": "orders", "label": "下单", "count": 18},
      {"type": "topics", "label": "话题", "count": 15}
    ]
  }
}

// 断言
- tabs.size() == 4
- 每个tab都有type、label、count字段
- count >= 0
```

---

### 运行测试

```bash
# 进入聚合服务目录
cd xypai-aggregation/xypai-app-bff

# 运行搜索结果页面测试
mvn test -Dtest=Page07_SearchResultsTest

# 运行所有页面测试
mvn test -Dtest=Page*Test
```

**测试前置条件**:
1. 确保 Nacos、Redis、MySQL 已启动
2. 确保 xypai-auth (9211) 服务已启动
3. 确保 xypai-app-bff (9400) 服务已启动
4. 确保 Gateway (8080) 已启动

---

### 测试流程图

```
┌─────────────────────────────────────────────────────────────┐
│                    搜索结果页面测试流程                       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. 用户登录                                                 │
│     POST /xypai-auth/api/auth/login/sms                     │
│     └── 获取 Token                                          │
│                                                             │
│  2. 执行综合搜索                                             │
│     POST /xypai-app-bff/api/search/search                   │
│     ├── 返回各Tab统计                                        │
│     └── 返回综合结果                                         │
│                                                             │
│  3. 分Tab查询                                                │
│     ├── GET /api/search/all     → 全部Tab                   │
│     ├── GET /api/search/users   → 用户Tab                   │
│     ├── GET /api/search/orders  → 下单Tab                   │
│     └── GET /api/search/topics  → 话题Tab                   │
│                                                             │
│  4. 特定场景测试                                             │
│     ├── 搜索特定关键词（台球）                               │
│     ├── 搜索无结果关键词                                     │
│     └── 分页功能测试                                         │
│                                                             │
│  5. 验证Tab统计信息一致性                                    │
│     └── 验证各Tab的count字段                                 │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

**文档版本**: v1.0.0

**最后更新**: 2025-11-29

**测试文件**: `xypai-aggregation/xypai-app-bff/src/test/java/org/dromara/appbff/pages/Page07_SearchResultsTest.java`
