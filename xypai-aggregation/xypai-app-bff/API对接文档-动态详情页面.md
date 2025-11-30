# XyPai-App-BFF 动态详情页面 API 对接文档

> **版本**: v1.0.0
>
> **更新日期**: 2025-11-29
>
> **服务端口**: 9403 (Content) / 8200 (Auth)
>
> **接口前缀**: `/api/`

---

## 目录

1. [通用说明](#通用说明)
2. [页面信息](#页面信息)
3. [用户认证接口](#用户认证接口)
4. [动态详情接口](#动态详情接口)
5. [评论接口](#评论接口)
6. [互动接口](#互动接口)
7. [错误码说明](#错误码说明)
8. [集成测试用例](#集成测试用例)

---

## 通用说明

### 基础URL

```
# 开发环境（通过网关）
http://localhost:8080

# 直连服务
xypai-auth:    http://localhost:8200
xypai-content: http://localhost:9403
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

**错误响应示例**:

```json
{
  "code": 500,
  "msg": "动态不存在",
  "data": null
}
```

### 分页格式

分页接口统一返回以下格式：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [...],
    "total": 100,
    "size": 20,
    "current": 1,
    "pages": 5
  }
}
```

---

## 页面信息

| 属性 | 值 |
|------|------|
| 文档路径 | XiangYuPai-Doc/Action-API/模块化架构/03-content模块/Frontend/03-动态详情页面.md |
| 页面路由 | /feed/detail/:feedId |
| 页面名称 | 动态详情页 |
| 用户角色 | 已登录用户/游客(部分功能限制) |
| 页面类型 | 详情页 |

### 涉及的后端服务

| 服务 | 端口 | 功能 |
|------|------|------|
| xypai-auth | 8200 | 用户认证 |
| xypai-content | 9403 | 动态详情、评论、互动 |

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
  "mobile": "13800000002",
  "verificationCode": "123456"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| countryCode | string | 是 | 国家区号，如 "+86" |
| mobile | string | 是 | 手机号码 |
| verificationCode | string | 是 | 短信验证码（测试环境固定：123456） |

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

## 动态详情接口

### 1. 获取动态详情

获取单个动态的完整信息。

**请求**

```http
GET /xypai-content/api/v1/content/detail/{feedId}
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| feedId | long | 是 | 动态ID |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 1001,
    "userId": 2001,
    "type": 1,
    "typeDesc": "动态",
    "title": "今天的探店日记",
    "content": "发现了一家超棒的咖啡馆...(完整内容)",
    "summary": "发现了一家超棒的咖啡馆...",
    "userInfo": {
      "id": 2001,
      "nickname": "小美探店",
      "avatar": "https://cdn.example.com/avatar/2001.jpg",
      "gender": "female",
      "age": 25,
      "isFollowed": false,
      "isRealVerified": true,
      "isGodVerified": false,
      "isVip": true
    },
    "mediaList": [
      {
        "mediaId": 3001,
        "mediaType": "image",
        "url": "https://cdn.example.com/feed/3001.jpg",
        "thumbnailUrl": "https://cdn.example.com/feed/3001_thumb.jpg",
        "width": 1920,
        "height": 1080,
        "duration": null
      }
    ],
    "topicList": [
      {
        "name": "探店日记",
        "description": "分享你的探店体验",
        "participantCount": 10000,
        "postCount": 50000
      }
    ],
    "locationName": "深圳市南山区",
    "locationAddress": "科技园南路88号",
    "distance": 1.5,
    "cityId": 440300,
    "likeCount": 128,
    "commentCount": 32,
    "shareCount": 15,
    "collectCount": 45,
    "viewCount": 1024,
    "isLiked": false,
    "isCollected": true,
    "canEdit": true,
    "canDelete": true,
    "createdAt": "2025-11-29 10:30:00"
  }
}
```

**字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 动态ID |
| userId | long | 发布者用户ID |
| type | integer | 动态类型: 1=动态, 2=活动, 3=技能 |
| typeDesc | string | 类型描述 |
| content | string | 动态内容 |
| userInfo | object | 发布者信息 |
| mediaList | array | 媒体列表 |
| topicList | array | 关联话题列表 |
| likeCount | integer | 点赞数 |
| commentCount | integer | 评论数 |
| collectCount | integer | 收藏数 |
| isLiked | boolean | 当前用户是否已点赞 |
| isCollected | boolean | 当前用户是否已收藏 |
| canDelete | boolean | 当前用户是否可删除 |

---

### 2. 删除动态

删除自己发布的动态。

**请求**

```http
DELETE /xypai-content/api/v1/content/{feedId}
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| feedId | long | 是 | 动态ID |

**响应示例**

```json
{
  "code": 200,
  "msg": "删除成功",
  "data": null
}
```

---

## 评论接口

### 1. 获取评论列表

获取动态的评论列表，包含二级回复。

**请求**

```http
GET /xypai-content/api/v1/content/comments/{feedId}
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| feedId | long | 是 | 动态ID |

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | integer | 是 | 1 | 页码 |
| pageSize | integer | 是 | 20 | 每页数量，1-100 |
| sortType | string | 否 | time | 排序: `time`(时间), `hot`(热度), `like`(点赞) |

**请求示例**

```http
GET /xypai-content/api/v1/content/comments/1001?pageNum=1&pageSize=10&sortType=hot
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [
      {
        "id": 4001,
        "feedId": 1001,
        "userId": 2002,
        "content": "写得真好！推荐大家去试试",
        "userInfo": {
          "id": 2002,
          "nickname": "咖啡爱好者",
          "avatar": "https://cdn.example.com/avatar/2002.jpg"
        },
        "likeCount": 25,
        "replyCount": 3,
        "isTop": true,
        "isLiked": false,
        "replies": [
          {
            "id": 4002,
            "content": "已收藏，下周去！",
            "userInfo": {
              "id": 2003,
              "nickname": "美食达人",
              "avatar": "https://cdn.example.com/avatar/2003.jpg"
            },
            "replyToUserNickname": "咖啡爱好者",
            "createdAt": "2025-11-29 11:00:00"
          }
        ],
        "totalReplies": 3,
        "hasMoreReplies": false,
        "canDelete": false,
        "createdAt": "2025-11-29 10:45:00"
      }
    ],
    "total": 32,
    "size": 10,
    "current": 1,
    "pages": 4
  }
}
```

---

### 2. 发表评论/回复

发表一级评论或二级回复。

**请求**

```http
POST /xypai-content/api/v1/content/comment
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体（一级评论）**

```json
{
  "feedId": 1001,
  "content": "这是一条测试评论，测试评论功能是否正常！"
}
```

**请求体（二级回复）**

```json
{
  "feedId": 1001,
  "content": "这是对评论的回复，测试二级评论功能！",
  "parentId": 4001,
  "replyToUserId": 2001
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| feedId | long | 是 | 动态ID |
| content | string | 是 | 评论内容，1-500字符 |
| parentId | long | 否 | 父评论ID（回复时传入） |
| replyToUserId | long | 否 | 回复的用户ID（回复时传入） |

**响应示例**

```json
{
  "code": 200,
  "msg": "评论成功",
  "data": {
    "id": 4003,
    "feedId": 1001,
    "userId": 2001,
    "content": "这是一条测试评论，测试评论功能是否正常！",
    "userInfo": {
      "id": 2001,
      "nickname": "小美探店",
      "avatar": "https://cdn.example.com/avatar/2001.jpg"
    },
    "likeCount": 0,
    "replyCount": 0,
    "isTop": false,
    "isLiked": false,
    "replies": [],
    "totalReplies": 0,
    "hasMoreReplies": false,
    "canDelete": true,
    "createdAt": "2025-11-29 12:00:00"
  }
}
```

---

### 3. 删除评论

删除自己的评论。

**请求**

```http
DELETE /xypai-content/api/v1/content/comment/{commentId}
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| commentId | long | 是 | 评论ID |

**响应示例**

```json
{
  "code": 200,
  "msg": "删除成功",
  "data": null
}
```

---

## 互动接口

### 1. 点赞/取消点赞

对动态或评论进行点赞/取消点赞操作。

**请求**

```http
POST /xypai-content/api/v1/interaction/like
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体（点赞动态）**

```json
{
  "targetType": "feed",
  "targetId": 1001,
  "action": "like"
}
```

**请求体（取消点赞）**

```json
{
  "targetType": "feed",
  "targetId": 1001,
  "action": "unlike"
}
```

**请求体（点赞评论）**

```json
{
  "targetType": "comment",
  "targetId": 4001,
  "action": "like"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetType | string | 是 | 目标类型: `feed`(动态), `comment`(评论) |
| targetId | long | 是 | 目标ID |
| action | string | 是 | 操作: `like`(点赞), `unlike`(取消点赞) |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "success": true,
    "count": 129,
    "isActive": true
  }
}
```

**字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| success | boolean | 操作是否成功 |
| count | integer | 当前点赞总数 |
| isActive | boolean | 当前状态: true=已点赞, false=未点赞 |

---

### 2. 收藏/取消收藏

对动态进行收藏/取消收藏操作。

**请求**

```http
POST /xypai-content/api/v1/interaction/collect
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体（收藏）**

```json
{
  "targetType": "feed",
  "targetId": 1001,
  "action": "collect"
}
```

**请求体（取消收藏）**

```json
{
  "targetType": "feed",
  "targetId": 1001,
  "action": "uncollect"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetType | string | 是 | 目标类型: `feed`(仅支持动态) |
| targetId | long | 是 | 目标ID |
| action | string | 是 | 操作: `collect`(收藏), `uncollect`(取消收藏) |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "success": true,
    "count": 46,
    "isActive": true
  }
}
```

---

### 3. 分享动态

分享动态到不同渠道。

**请求**

```http
POST /xypai-content/api/v1/interaction/share
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "targetType": "feed",
  "targetId": 1001,
  "shareChannel": "wechat"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetType | string | 是 | 目标类型: `feed` |
| targetId | long | 是 | 动态ID |
| shareChannel | string | 是 | 分享渠道: `wechat`, `moments`, `qq`, `qzone`, `weibo`, `copy_link` |

**响应示例**

```json
{
  "code": 200,
  "msg": "分享成功",
  "data": {
    "success": true,
    "count": 16,
    "isActive": true
  }
}
```

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权，请先登录 |
| 403 | 无权限操作 |
| 404 | 资源不存在 |
| 429 | 请求过于频繁，请稍后再试 |
| 500 | 服务器内部错误 |

### 常见业务错误

| 错误信息 | 说明 |
|----------|------|
| 动态不存在 | feedId 对应的动态已删除或不存在 |
| 评论不存在 | commentId 对应的评论已删除或不存在 |
| 无权删除 | 尝试删除他人的动态或评论 |
| 评论内容不能为空 | 评论 content 字段为空 |

---

## 集成测试用例

### 测试环境配置

```
Gateway:       http://localhost:8080
xypai-auth:    http://localhost:8200 (认证服务)
xypai-content: http://localhost:9403 (内容服务)
```

**依赖服务**: Nacos, Redis, MySQL

---

### 测试场景: 动态详情页面 (Page03_FeedDetailTest)

测试动态详情页面的所有功能，包括动态详情、评论、互动、删除等。

#### 测试1: 用户A登录

```java
// 接口: POST /xypai-auth/api/auth/login/sms
Map<String, String> loginRequest = new HashMap<>();
loginRequest.put("countryCode", "+86");
loginRequest.put("mobile", "13800000002");
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

#### 测试2: 用户A发布动态

```java
// 接口: POST /xypai-content/api/v1/content/publish
// 请求头: Authorization: Bearer {token}
Map<String, Object> publishRequest = new HashMap<>();
publishRequest.put("type", 1);
publishRequest.put("content", "这是一条测试动态，用于动态详情页测试。#动态测试 #集成测试");
publishRequest.put("visibility", 0);

// 响应
{
  "code": 200,
  "msg": "发布成功",
  "data": 1001  // feedId
}
```

#### 测试3: 用户A获取动态详情

```java
// 接口: GET /xypai-content/api/v1/content/detail/{feedId}
// 请求头: Authorization: Bearer {token}
String detailUrl = GATEWAY_URL + "/xypai-content/api/v1/content/detail/" + testFeedId;

// 响应
{
  "code": 200,
  "data": {
    "id": 1001,
    "content": "这是一条测试动态...",
    "likeCount": 0,
    "commentCount": 0,
    "collectCount": 0,
    "isLiked": false,
    "isCollected": false,
    "userInfo": {
      "nickname": "用户A",
      "avatar": "..."
    }
  }
}
```

#### 测试4: 用户A点赞动态

```java
// 接口: POST /xypai-content/api/v1/interaction/like
Map<String, Object> likeRequest = new HashMap<>();
likeRequest.put("targetType", "feed");
likeRequest.put("targetId", testFeedId);
likeRequest.put("action", "like");

// 响应
{
  "code": 200,
  "data": {
    "success": true,
    "count": 1,
    "isActive": true
  }
}
```

#### 测试5: 用户A取消点赞

```java
// 接口: POST /xypai-content/api/v1/interaction/like
Map<String, Object> unlikeRequest = new HashMap<>();
unlikeRequest.put("targetType", "feed");
unlikeRequest.put("targetId", testFeedId);
unlikeRequest.put("action", "unlike");

// 响应
{
  "code": 200,
  "data": {
    "success": true,
    "count": 0,
    "isActive": false
  }
}
```

#### 测试6: 用户A收藏动态

```java
// 接口: POST /xypai-content/api/v1/interaction/collect
Map<String, Object> collectRequest = new HashMap<>();
collectRequest.put("targetType", "feed");
collectRequest.put("targetId", testFeedId);
collectRequest.put("action", "collect");

// 响应
{
  "code": 200,
  "data": {
    "success": true,
    "count": 1,
    "isActive": true
  }
}
```

#### 测试7: 用户A发布一级评论

```java
// 接口: POST /xypai-content/api/v1/content/comment
Map<String, Object> commentRequest = new HashMap<>();
commentRequest.put("feedId", testFeedId);
commentRequest.put("content", "这是一条测试评论，测试评论功能是否正常！");

// 响应
{
  "code": 200,
  "msg": "评论成功",
  "data": {
    "id": 4001,  // commentId
    "content": "这是一条测试评论...",
    "likeCount": 0,
    "replyCount": 0
  }
}
```

#### 测试8: 用户A获取评论列表

```java
// 接口: GET /xypai-content/api/v1/content/comments/{feedId}
String commentsUrl = GATEWAY_URL + "/xypai-content/api/v1/content/comments/" + testFeedId
    + "?pageNum=1&pageSize=10&sortType=hot";

// 响应
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 4001,
        "content": "这是一条测试评论...",
        "userInfo": {...}
      }
    ],
    "total": 1
  }
}
```

#### 测试9: 用户B登录

```java
// 接口: POST /xypai-auth/api/auth/login/sms
Map<String, String> loginRequest = new HashMap<>();
loginRequest.put("countryCode", "+86");
loginRequest.put("mobile", "13800000003");
loginRequest.put("verificationCode", "123456");

// 响应
{
  "code": 200,
  "data": {
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "userId": 10002
  }
}
```

#### 测试10: 用户B发布二级回复

```java
// 接口: POST /xypai-content/api/v1/content/comment
Map<String, Object> replyRequest = new HashMap<>();
replyRequest.put("feedId", testFeedId);
replyRequest.put("content", "这是对评论的回复，测试二级评论功能！");
replyRequest.put("parentId", testCommentId);
replyRequest.put("replyToUserId", userIdA);

// 响应
{
  "code": 200,
  "data": {
    "id": 4002,  // replyId
    "content": "这是对评论的回复..."
  }
}
```

#### 测试11: 用户B点赞评论

```java
// 接口: POST /xypai-content/api/v1/interaction/like
Map<String, Object> likeCommentRequest = new HashMap<>();
likeCommentRequest.put("targetType", "comment");
likeCommentRequest.put("targetId", testCommentId);
likeCommentRequest.put("action", "like");

// 响应
{
  "code": 200,
  "data": {
    "success": true,
    "count": 1,
    "isActive": true
  }
}
```

#### 测试12: 用户B分享动态

```java
// 接口: POST /xypai-content/api/v1/interaction/share
Map<String, Object> shareRequest = new HashMap<>();
shareRequest.put("targetType", "feed");
shareRequest.put("targetId", testFeedId);
shareRequest.put("shareChannel", "wechat");

// 响应
{
  "code": 200,
  "data": {
    "success": true
  }
}
```

#### 测试13: 用户A删除自己的评论

```java
// 接口: DELETE /xypai-content/api/v1/content/comment/{commentId}
// 请求头: Authorization: Bearer {tokenUserA}

// 响应
{
  "code": 200,
  "msg": "删除成功"
}
```

#### 测试14: 用户A删除自己的动态

```java
// 接口: DELETE /xypai-content/api/v1/content/{feedId}
// 请求头: Authorization: Bearer {tokenUserA}

// 响应
{
  "code": 200,
  "msg": "删除成功"
}
```

---

### 运行测试

```bash
# 进入聚合服务目录
cd xypai-aggregation/xypai-app-bff

# 运行动态详情页面测试
mvn test -Dtest=Page03_FeedDetailTest

# 运行所有页面测试
mvn test -Dtest=Page*Test
```

**测试前置条件**:
1. 确保 Nacos、Redis、MySQL 已启动
2. 确保 xypai-auth (8200) 服务已启动
3. 确保 xypai-content (9403) 服务已启动
4. 确保 Gateway (8080) 已启动

---

### 测试流程图

```
┌─────────────────────────────────────────────────────────────┐
│                    动态详情页面测试流程                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. 用户A登录                                                │
│     POST /xypai-auth/api/auth/login/sms                     │
│     └── 获取 TokenA                                         │
│                                                             │
│  2. 用户A发布动态                                            │
│     POST /xypai-content/api/v1/content/publish              │
│     └── 获取 feedId                                         │
│                                                             │
│  3. 用户A获取动态详情                                        │
│     GET /xypai-content/api/v1/content/detail/{feedId}       │
│                                                             │
│  4. 用户A互动操作                                            │
│     ├── 点赞动态 (like)                                     │
│     ├── 取消点赞 (unlike)                                   │
│     └── 收藏动态 (collect)                                  │
│                                                             │
│  5. 用户A发布评论                                            │
│     POST /xypai-content/api/v1/content/comment              │
│     └── 获取 commentId                                      │
│                                                             │
│  6. 用户A获取评论列表                                        │
│     GET /xypai-content/api/v1/content/comments/{feedId}     │
│                                                             │
│  7. 用户B登录                                                │
│     POST /xypai-auth/api/auth/login/sms                     │
│     └── 获取 TokenB                                         │
│                                                             │
│  8. 用户B发布二级回复                                        │
│     POST /xypai-content/api/v1/content/comment              │
│     └── parentId = commentId                                │
│                                                             │
│  9. 用户B互动操作                                            │
│     ├── 点赞评论                                            │
│     └── 分享动态                                            │
│                                                             │
│  10. 清理测试数据                                            │
│      ├── 用户A删除评论                                       │
│      └── 用户A删除动态                                       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

**文档版本**: v1.0.0

**最后更新**: 2025-11-29

**测试文件**: `xypai-aggregation/xypai-app-bff/src/test/java/org/dromara/appbff/pages/Page03_FeedDetailTest.java`
