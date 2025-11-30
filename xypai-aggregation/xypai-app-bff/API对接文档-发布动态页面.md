# XyPai-App-BFF 发布动态页面 API 对接文档

> **版本**: v1.0.0
>
> **更新日期**: 2025-11-29
>
> **服务端口**: 9400 (BFF) / 9403 (Content) / 8200 (Auth)
>
> **接口前缀**: `/api/`

---

## 目录

1. [通用说明](#通用说明)
2. [页面信息](#页面信息)
3. [用户认证接口](#用户认证接口)
4. [话题接口](#话题接口)
5. [动态发布接口](#动态发布接口)
6. [错误码说明](#错误码说明)
7. [集成测试用例](#集成测试用例)

---

## 通用说明

### 基础URL

```
# 开发环境（通过网关）
http://localhost:8080

# 直连服务
xypai-auth:    http://localhost:8200
xypai-content: http://localhost:9403
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

**错误响应示例**:

```json
{
  "code": 400,
  "msg": "内容不能为空",
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
| 文档路径 | XiangYuPai-Doc/Action-API/模块化架构/03-content模块/Frontend/02-发布动态页面.md |
| 页面路由 | /publish |
| 页面名称 | 发布动态 |
| 用户角色 | 登录用户 |
| 页面类型 | 全屏表单页面 |

### 涉及的后端服务

| 服务 | 端口 | 功能 |
|------|------|------|
| xypai-auth | 8200 | 用户认证 |
| xypai-content | 9403 | 动态发布、话题管理 |
| xypai-common | 9407 | 媒体上传、位置服务 |

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

## 话题接口

### 1. 获取热门话题列表

获取热门话题，按帖子数和参与人数排序。

**请求**

```http
GET /xypai-content/api/v1/content/topics/hot
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 20 | 每页数量，1-100 |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [
      {
        "id": 6001,
        "name": "探店日记",
        "description": "分享你的探店体验",
        "coverImage": "https://cdn.example.com/topic/6001.jpg",
        "participantCount": 10000,
        "postCount": 50000,
        "isOfficial": true,
        "isHot": true
      },
      {
        "id": 6002,
        "name": "美食推荐",
        "description": "推荐好吃的餐厅",
        "coverImage": "https://cdn.example.com/topic/6002.jpg",
        "participantCount": 8000,
        "postCount": 40000,
        "isOfficial": true,
        "isHot": true
      }
    ],
    "total": 50,
    "size": 20,
    "current": 1,
    "pages": 3
  }
}
```

---

### 2. 搜索话题

根据关键词搜索话题名称和描述。

**请求**

```http
GET /xypai-content/api/v1/content/topics/search
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 是 | 搜索关键词，1-20字符 |
| page | integer | 否 | 页码，默认1 |
| pageSize | integer | 否 | 每页数量，默认20 |

**请求示例**

```http
GET /xypai-content/api/v1/content/topics/search?keyword=探店&page=1&pageSize=20
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [
      {
        "id": 6001,
        "name": "探店日记",
        "description": "分享你的探店体验",
        "coverImage": "https://cdn.example.com/topic/6001.jpg",
        "participantCount": 10000,
        "postCount": 50000,
        "isOfficial": true,
        "isHot": true
      }
    ],
    "total": 5,
    "size": 20,
    "current": 1,
    "pages": 1
  }
}
```

---

## 动态发布接口

### 1. 发布动态

发布新动态，支持文字、图片、视频、话题、地点。

**请求**

```http
POST /xypai-content/api/v1/content/publish
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "type": 1,
  "title": "今天的探店日记",
  "content": "发现了一家超棒的咖啡馆，环境很好，咖啡也很香...",
  "mediaIds": [3001, 3002, 3003],
  "topicNames": ["探店日记", "咖啡控"],
  "locationId": 5001,
  "locationName": "深圳市南山区",
  "locationAddress": "科技园南路88号",
  "longitude": 114.0579,
  "latitude": 22.5431,
  "visibility": 0
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | integer | 是 | 动态类型: 1=动态, 2=活动, 3=技能 |
| title | string | 否 | 标题，0-50字符 |
| content | string | 是 | 内容，1-1000字符 |
| mediaIds | array[long] | 否 | 媒体ID列表，最多9张图或1个视频 |
| topicNames | array[string] | 否 | 话题名称列表，最多5个 |
| locationId | long | 否 | 地点ID |
| locationName | string | 否 | 地点名称 |
| locationAddress | string | 否 | 详细地址 |
| longitude | decimal | 否 | 经度 |
| latitude | decimal | 否 | 纬度 |
| visibility | integer | 否 | 可见范围: 0=公开, 1=仅好友, 2=仅自己 |

**响应示例**

```json
{
  "code": 200,
  "msg": "发布成功",
  "data": 1001
}
```

**字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| data | long | 发布成功的动态ID |

---

### 2. 发布场景示例

#### 2.1 发布纯文字动态

```json
{
  "type": 1,
  "content": "这是一条测试动态，来自页面级测试。今天天气真好！",
  "visibility": 0
}
```

#### 2.2 发布带标题的动态

```json
{
  "type": 1,
  "title": "今天的美食分享",
  "content": "今天去了一家很棒的餐厅，菜品精致，服务也很好。推荐给大家！",
  "visibility": 0
}
```

#### 2.3 发布带话题的动态

```json
{
  "type": 1,
  "content": "发现了一家宝藏店铺，环境优雅，服务贴心！强烈推荐！",
  "topicNames": ["探店日记", "美食推荐"],
  "visibility": 0
}
```

#### 2.4 发布带地点的动态

```json
{
  "type": 1,
  "content": "在深圳湾公园散步，天气很好！推荐大家周末来这里放松。",
  "locationName": "深圳湾公园",
  "locationAddress": "广东省深圳市南山区深圳湾",
  "longitude": 113.9577,
  "latitude": 22.5189,
  "visibility": 0
}
```

---

### 3. 内容验证规则

#### 3.1 空内容验证

**请求体（预期失败）**

```json
{
  "type": 1,
  "content": "",
  "visibility": 0
}
```

**响应（预期错误）**

```json
{
  "code": 400,
  "msg": "内容不能为空",
  "data": null
}
```

#### 3.2 超长内容验证

**请求体（预期失败）**

```json
{
  "type": 1,
  "content": "超过1000字符的内容...",
  "visibility": 0
}
```

**响应（预期错误）**

```json
{
  "code": 400,
  "msg": "内容长度超过限制",
  "data": null
}
```

#### 3.3 话题数量验证

**请求体（预期失败）**

```json
{
  "type": 1,
  "content": "测试超过5个话题的验证",
  "topicNames": ["话题1", "话题2", "话题3", "话题4", "话题5", "话题6"],
  "visibility": 0
}
```

**响应（预期错误）**

```json
{
  "code": 400,
  "msg": "最多关联5个话题",
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
| 403 | 无权限操作 |
| 404 | 资源不存在 |
| 429 | 请求过于频繁，请稍后再试 |
| 500 | 服务器内部错误 |

### 常见业务错误

| 错误信息 | 说明 |
|----------|------|
| 内容不能为空 | 发布动态时 content 字段为空 |
| 内容长度超过限制 | content 超过 1000 字符 |
| 最多关联5个话题 | topicNames 数组超过 5 个 |
| 标题长度超过限制 | title 超过 50 字符 |

---

## 集成测试用例

### 测试环境配置

```
Gateway:       http://localhost:8080
xypai-auth:    http://localhost:8200 (认证服务)
xypai-content: http://localhost:9403 (内容服务)
xypai-common:  http://localhost:9407 (通用服务)
```

**依赖服务**: Nacos, Redis, MySQL

---

### 测试场景: 发布动态页面 (Page02_PublishFeedTest)

测试发布动态页面的所有功能，包括发布动态、话题查询、话题搜索。

#### 测试1: 用户登录

```java
// 接口: POST /xypai-auth/api/auth/login/sms
Map<String, String> loginRequest = new HashMap<>();
loginRequest.put("countryCode", "+86");
loginRequest.put("mobile", "13800000001");     // 动态生成
loginRequest.put("verificationCode", "123456"); // 测试环境固定验证码

// 响应
{
  "code": 200,
  "data": {
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "userId": 10001
  }
}
```

#### 测试2: 获取热门话题列表

```java
// 接口: GET /xypai-content/api/v1/content/topics/hot
// 请求头: Authorization: Bearer {token}
String topicsUrl = GATEWAY_URL + "/xypai-content/api/v1/content/topics/hot?page=1&pageSize=20";

// 响应
{
  "code": 200,
  "data": {
    "records": [...],
    "total": 50,
    "size": 20,
    "current": 1
  }
}
```

#### 测试3: 搜索话题

```java
// 接口: GET /xypai-content/api/v1/content/topics/search
// 请求头: Authorization: Bearer {token}
String searchUrl = GATEWAY_URL + "/xypai-content/api/v1/content/topics/search?keyword=探店&page=1&pageSize=20";

// 响应
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 6001,
        "name": "探店日记",
        "isHot": true
      }
    ],
    "total": 5
  }
}
```

#### 测试4: 发布纯文字动态

```java
// 接口: POST /xypai-content/api/v1/content/publish
// 请求头: Authorization: Bearer {token}
Map<String, Object> publishRequest = new HashMap<>();
publishRequest.put("type", 1);  // 1=动态
publishRequest.put("content", "这是一条页面级测试动态，来自 Page02_PublishFeedTest。今天天气真好！");
publishRequest.put("visibility", 0);  // 0=公开

// 响应
{
  "code": 200,
  "msg": "发布成功",
  "data": 1001  // feedId
}
```

#### 测试5: 发布带标题的动态

```java
// 接口: POST /xypai-content/api/v1/content/publish
Map<String, Object> publishRequest = new HashMap<>();
publishRequest.put("type", 1);
publishRequest.put("title", "今天的美食分享");
publishRequest.put("content", "今天去了一家很棒的餐厅，菜品精致，服务也很好。推荐给大家！");
publishRequest.put("visibility", 0);

// 响应
{
  "code": 200,
  "msg": "发布成功",
  "data": 1002
}
```

#### 测试6: 发布带话题的动态

```java
// 接口: POST /xypai-content/api/v1/content/publish
List<String> topics = new ArrayList<>();
topics.add("探店日记");
topics.add("美食推荐");

Map<String, Object> publishRequest = new HashMap<>();
publishRequest.put("type", 1);
publishRequest.put("content", "发现了一家宝藏店铺，环境优雅，服务贴心！强烈推荐！");
publishRequest.put("topicNames", topics);
publishRequest.put("visibility", 0);

// 响应
{
  "code": 200,
  "msg": "发布成功",
  "data": 1003
}
```

#### 测试7: 发布带地点的动态

```java
// 接口: POST /xypai-content/api/v1/content/publish
Map<String, Object> publishRequest = new HashMap<>();
publishRequest.put("type", 1);
publishRequest.put("content", "在深圳湾公园散步，天气很好！推荐大家周末来这里放松。");
publishRequest.put("locationName", "深圳湾公园");
publishRequest.put("locationAddress", "广东省深圳市南山区深圳湾");
publishRequest.put("longitude", 113.9577);
publishRequest.put("latitude", 22.5189);
publishRequest.put("visibility", 0);

// 响应
{
  "code": 200,
  "msg": "发布成功",
  "data": 1004
}
```

#### 测试8: 空内容验证（预期失败）

```java
// 接口: POST /xypai-content/api/v1/content/publish
Map<String, Object> publishRequest = new HashMap<>();
publishRequest.put("type", 1);
publishRequest.put("content", "");  // 空内容
publishRequest.put("visibility", 0);

// 响应 (预期错误)
{
  "code": 400,
  "msg": "内容不能为空"
}
```

#### 测试9: 超长内容验证（预期失败）

```java
// 接口: POST /xypai-content/api/v1/content/publish
// content 超过 1000 字符

// 响应 (预期错误)
{
  "code": 400,
  "msg": "内容长度超过限制"
}
```

#### 测试10: 话题数量验证（预期失败）

```java
// 接口: POST /xypai-content/api/v1/content/publish
List<String> topics = Arrays.asList("话题1", "话题2", "话题3", "话题4", "话题5", "话题6");

Map<String, Object> publishRequest = new HashMap<>();
publishRequest.put("type", 1);
publishRequest.put("content", "测试超过5个话题的验证");
publishRequest.put("topicNames", topics);

// 响应 (预期错误)
{
  "code": 400,
  "msg": "最多关联5个话题"
}
```

---

### 运行测试

```bash
# 进入聚合服务目录
cd xypai-aggregation/xypai-app-bff

# 运行发布动态页面测试
mvn test -Dtest=Page02_PublishFeedTest

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
│                    发布动态页面测试流程                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. 用户登录                                                 │
│     POST /xypai-auth/api/auth/login/sms                     │
│     └── 获取 Token                                          │
│                                                             │
│  2. 获取热门话题                                             │
│     GET /xypai-content/api/v1/content/topics/hot            │
│     └── 展示推荐话题                                         │
│                                                             │
│  3. 搜索话题                                                 │
│     GET /xypai-content/api/v1/content/topics/search         │
│     └── 用户输入关键词搜索                                   │
│                                                             │
│  4. 发布动态                                                 │
│     POST /xypai-content/api/v1/content/publish              │
│     ├── 纯文字动态                                          │
│     ├── 带标题动态                                          │
│     ├── 带话题动态                                          │
│     └── 带地点动态                                          │
│                                                             │
│  5. 验证规则                                                 │
│     ├── 空内容拒绝                                          │
│     ├── 超长内容拒绝                                         │
│     └── 超过5个话题拒绝                                      │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

**文档版本**: v1.0.0

**最后更新**: 2025-11-29

**测试文件**: `xypai-aggregation/xypai-app-bff/src/test/java/org/dromara/appbff/pages/Page02_PublishFeedTest.java`
