# XyPai-Chat 聊天服务 API 对接文档

> **版本**: v1.0.0
>
> **更新日期**: 2025-11-28
>
> **服务端口**: 9404
>
> **接口前缀**: `/api/message/`

---

## 目录

1. [通用说明](#通用说明)
2. [消息主页接口](#消息主页接口)
3. [聊天会话接口](#聊天会话接口)
4. [消息管理接口](#消息管理接口)
5. [文件上传接口](#文件上传接口)
6. [WebSocket 实时通讯](#websocket-实时通讯)
7. [错误码说明](#错误码说明)
8. [集成测试用例](#集成测试用例)

---

## 通用说明

### 基础URL

```
# 开发环境（直连服务）
http://localhost:9404/api/message/

# 生产环境（通过网关）
http://gateway:8080/xypai-chat/api/message/
```

### 认证方式

所有接口必须在请求头中携带 Token：

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

### 消息状态码

| 状态值 | 说明 |
|--------|------|
| 0 | 发送中 |
| 1 | 已送达 |
| 2 | 已读 |
| 3 | 发送失败 |

### 消息类型

| 类型 | 说明 |
|------|------|
| text | 文字消息 |
| image | 图片消息 |
| voice | 语音消息 |
| video | 视频消息 |

---

## 消息主页接口

### 1. 获取未读消息数

获取各类型未读消息统计（聊天+点赞+评论+粉丝+系统通知）。

**请求**

```http
GET /api/message/unread-count
Authorization: Bearer <token>
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "likes": 5,
    "comments": 3,
    "followers": 2,
    "system": 1,
    "total": 11
  }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| likes | integer | 赞和收藏未读数 |
| comments | integer | 评论未读数 |
| followers | integer | 粉丝未读数 |
| system | integer | 系统通知未读数 |
| total | integer | 总未读数 |

---

### 2. 获取会话列表

获取当前用户的私信会话列表，按最后消息时间排序。

**请求**

```http
GET /api/message/conversations?page=1&pageSize=20
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | integer | 否 | 1 | 页码，最小1 |
| pageSize | integer | 否 | 20 | 每页数量，最小10 |
| lastMessageId | long | 否 | - | 最后一条消息ID（用于游标分页） |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [
      {
        "conversationId": 1001,
        "userId": 2001,
        "nickname": "小美",
        "avatar": "https://cdn.example.com/avatar/2001.jpg",
        "lastMessage": "好的，明天见！",
        "lastMessageTime": "2025-11-28T10:30:00.000Z",
        "unreadCount": 3,
        "isOnline": true
      }
    ],
    "total": 10,
    "size": 20,
    "current": 1,
    "pages": 1
  }
}
```

**会话字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| conversationId | long | 会话ID |
| userId | long | 对方用户ID |
| nickname | string | 对方昵称 |
| avatar | string | 对方头像URL |
| lastMessage | string | 最后一条消息内容 |
| lastMessageTime | datetime | 最后消息时间（ISO 8601格式） |
| unreadCount | integer | 未读消息数 |
| isOnline | boolean | 对方是否在线 |

---

### 3. 删除会话

删除指定会话（软删除，不影响对方）。

**请求**

```http
DELETE /api/message/conversation/{conversationId}
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| conversationId | long | 是 | 会话ID |

**响应示例**

```json
{
  "code": 200,
  "msg": "删除成功",
  "data": null
}
```

---

### 4. 清除所有消息

删除当前用户的所有会话（软删除）。

**请求**

```http
POST /api/message/clear-all
Authorization: Bearer <token>
```

**响应示例**

```json
{
  "code": 200,
  "msg": "清除成功",
  "data": null
}
```

---

## 聊天会话接口

### 1. 获取聊天记录

获取指定会话的聊天记录，按时间倒序排列。

**请求**

```http
GET /api/message/chat/{conversationId}?page=1&pageSize=20
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| conversationId | long | 是 | 会话ID |

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | integer | 否 | 1 | 页码，最小1 |
| pageSize | integer | 否 | 20 | 每页数量，最小10 |
| lastMessageId | long | 否 | - | 最后一条消息ID（用于游标分页） |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [
      {
        "messageId": 5001,
        "conversationId": 1001,
        "senderId": 2001,
        "receiverId": 2002,
        "messageType": "text",
        "content": "你好！",
        "mediaUrl": null,
        "thumbnailUrl": null,
        "duration": null,
        "status": 2,
        "isRecalled": false,
        "createdAt": "2025-11-28T10:30:00.000Z"
      },
      {
        "messageId": 5002,
        "conversationId": 1001,
        "senderId": 2002,
        "receiverId": 2001,
        "messageType": "image",
        "content": null,
        "mediaUrl": "https://cdn.example.com/chat/5002.jpg",
        "thumbnailUrl": null,
        "duration": null,
        "status": 1,
        "isRecalled": false,
        "createdAt": "2025-11-28T10:31:00.000Z"
      }
    ],
    "total": 50,
    "size": 20,
    "current": 1,
    "pages": 3
  }
}
```

**消息字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| messageId | long | 消息ID |
| conversationId | long | 会话ID |
| senderId | long | 发送者ID |
| receiverId | long | 接收者ID |
| messageType | string | 消息类型: text/image/voice/video |
| content | string | 文字消息内容 |
| mediaUrl | string | 媒体文件URL（图片/语音/视频） |
| thumbnailUrl | string | 缩略图URL（视频消息） |
| duration | integer | 时长（语音/视频，单位：秒） |
| status | integer | 消息状态: 0=发送中, 1=已送达, 2=已读, 3=失败 |
| isRecalled | boolean | 是否已撤回 |
| createdAt | datetime | 发送时间（ISO 8601格式） |

---

### 2. 标记消息已读

将指定会话的所有未读消息标记为已读。

**请求**

```http
PUT /api/message/read/{conversationId}
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| conversationId | long | 是 | 会话ID |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": 5
}
```

**响应说明**

`data` 返回被标记为已读的消息数量。

---

## 消息管理接口

### 1. 发送消息

发送消息（支持文字、图片、语音、视频）。

**请求**

```http
POST /api/message/send
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体 - 文字消息**

```json
{
  "conversationId": 1001,
  "receiverId": 2002,
  "messageType": "text",
  "content": "你好！这是一条测试消息"
}
```

**请求体 - 图片消息**

```json
{
  "conversationId": 1001,
  "receiverId": 2002,
  "messageType": "image",
  "mediaUrl": "https://cdn.example.com/chat/image.jpg"
}
```

**请求体 - 语音消息**

```json
{
  "conversationId": 1001,
  "receiverId": 2002,
  "messageType": "voice",
  "mediaUrl": "https://cdn.example.com/chat/voice.mp3",
  "duration": 30
}
```

**请求体 - 视频消息**

```json
{
  "conversationId": 1001,
  "receiverId": 2002,
  "messageType": "video",
  "mediaUrl": "https://cdn.example.com/chat/video.mp4",
  "thumbnailUrl": "https://cdn.example.com/chat/video_thumb.jpg",
  "duration": 60
}
```

**请求参数说明**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| conversationId | long | 否 | 会话ID（新会话可不传） |
| receiverId | long | 是 | 接收者ID |
| messageType | string | 是 | 消息类型: text/image/voice/video |
| content | string | 条件 | 文字消息内容，最多500字符（text类型必填） |
| mediaUrl | string | 条件 | 媒体文件URL（image/voice/video类型必填） |
| thumbnailUrl | string | 条件 | 缩略图URL（video类型必填） |
| duration | integer | 条件 | 时长，单位秒（voice/video类型必填，voice最大60秒） |

**响应示例**

```json
{
  "code": 200,
  "msg": "发送成功",
  "data": {
    "messageId": 5003,
    "conversationId": 1001,
    "senderId": 2001,
    "receiverId": 2002,
    "messageType": "text",
    "content": "你好！这是一条测试消息",
    "mediaUrl": null,
    "thumbnailUrl": null,
    "duration": null,
    "status": 1,
    "isRecalled": false,
    "createdAt": "2025-11-28T10:35:00.000Z"
  }
}
```

---

### 2. 撤回消息

撤回已发送的消息（仅限2分钟内）。

**请求**

```http
POST /api/message/recall/{messageId}
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| messageId | long | 是 | 消息ID |

**响应示例**

```json
{
  "code": 200,
  "msg": "撤回成功",
  "data": null
}
```

**错误响应**

```json
{
  "code": 400,
  "msg": "消息已超过2分钟，无法撤回",
  "data": null
}
```

---

### 3. 删除消息

删除消息（软删除，仅对自己不可见）。

**请求**

```http
DELETE /api/message/{messageId}
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| messageId | long | 是 | 消息ID |

**响应示例**

```json
{
  "code": 200,
  "msg": "删除成功",
  "data": null
}
```

---

## 文件上传接口

### 上传媒体文件

上传聊天所需的媒体文件（图片/语音/视频）。

**请求**

```http
POST /api/message/upload
Content-Type: multipart/form-data
Authorization: Bearer <token>
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 媒体文件 |
| fileType | string | 是 | 文件类型: image/voice/video |

**响应示例 - 图片**

```json
{
  "code": 200,
  "msg": "上传成功",
  "data": {
    "mediaUrl": "https://cdn.example.com/chat/image_123.jpg",
    "fileType": "image"
  }
}
```

**响应示例 - 语音**

```json
{
  "code": 200,
  "msg": "上传成功",
  "data": {
    "mediaUrl": "https://cdn.example.com/chat/voice_123.mp3",
    "duration": 30,
    "fileType": "voice"
  }
}
```

**响应示例 - 视频**

```json
{
  "code": 200,
  "msg": "上传成功",
  "data": {
    "mediaUrl": "https://cdn.example.com/chat/video_123.mp4",
    "thumbnailUrl": "https://cdn.example.com/chat/video_123_thumb.jpg",
    "duration": 45,
    "fileType": "video"
  }
}
```

**文件格式限制**

| 类型 | 支持格式 | 最大大小 |
|------|----------|----------|
| image | jpg, png, gif, webp | 10MB |
| voice | mp3, m4a, wav, aac | 5MB |
| video | mp4, mov | 50MB |

---

## WebSocket 实时通讯

### 连接地址

```
ws://localhost:9404/ws/chat
```

### 连接认证

连接时需在 URL 或 Header 中携带 Sa-Token：

```
ws://localhost:9404/ws/chat?token=<access_token>
```

### 心跳机制

- 客户端每 30 秒发送一次 `ping`
- 服务端回复 `pong`
- 超过 90 秒无心跳自动断开

### 消息推送格式

**新消息推送**

```json
{
  "type": "new_message",
  "data": {
    "messageId": 5003,
    "conversationId": 1001,
    "senderId": 2001,
    "receiverId": 2002,
    "messageType": "text",
    "content": "你好！",
    "status": 1,
    "createdAt": "2025-11-28T10:35:00.000Z"
  }
}
```

**消息撤回通知**

```json
{
  "type": "message_recalled",
  "data": {
    "messageId": 5003,
    "conversationId": 1001
  }
}
```

**已读回执**

```json
{
  "type": "messages_read",
  "data": {
    "conversationId": 1001,
    "readCount": 5
  }
}
```

**在线状态变更**

```json
{
  "type": "online_status",
  "data": {
    "userId": 2001,
    "isOnline": true
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
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |

---

## 集成测试用例

### 测试环境配置

```
Gateway:       http://localhost:8080
xypai-auth:    http://localhost:9211 (认证服务)
xypai-chat:    http://localhost:9404 (聊天服务)
```

**依赖服务**: Nacos, Redis, MySQL

---

### 测试场景1: 消息主页 (MessageHomePageTest)

测试消息主页的核心功能。

#### 1.1 获取未读消息数

```java
// 接口: GET /xypai-chat/api/message/unread-count
// 请求头: Authorization: Bearer {token}
String unreadUrl = GATEWAY_URL + "/xypai-chat/api/message/unread-count";

// 响应
{
  "code": 200,
  "data": {
    "likes": 5,
    "comments": 3,
    "followers": 2,
    "system": 1,
    "total": 11
  }
}
```

#### 1.2 获取会话列表

```java
// 接口: GET /xypai-chat/api/message/conversations
String conversationsUrl = GATEWAY_URL + "/xypai-chat/api/message/conversations?page=1&pageSize=20";

// 响应
{
  "code": 200,
  "data": {
    "records": [
      {
        "conversationId": 1001,
        "userId": 2001,
        "nickname": "小美",
        "avatar": "https://cdn.example.com/avatar/2001.jpg",
        "lastMessage": "好的，明天见！",
        "lastMessageTime": "2025-11-28T10:30:00.000Z",
        "unreadCount": 3,
        "isOnline": true
      }
    ],
    "total": 10
  }
}
```

#### 1.3 删除会话

```java
// 接口: DELETE /xypai-chat/api/message/conversation/{conversationId}
String deleteUrl = GATEWAY_URL + "/xypai-chat/api/message/conversation/1001";

// 响应
{
  "code": 200,
  "msg": "删除成功"
}
```

#### 1.4 清除所有消息

```java
// 接口: POST /xypai-chat/api/message/clear-all
String clearUrl = GATEWAY_URL + "/xypai-chat/api/message/clear-all";

// 响应
{
  "code": 200,
  "msg": "清除成功"
}
```

---

### 测试场景2: 聊天页面 (ChatPageTest)

测试聊天页面的完整功能。

#### 2.1 获取聊天记录

```java
// 接口: GET /xypai-chat/api/message/chat/{conversationId}
String chatUrl = GATEWAY_URL + "/xypai-chat/api/message/chat/1001?page=1&pageSize=20";

// 响应
{
  "code": 200,
  "data": {
    "records": [
      {
        "messageId": 5001,
        "conversationId": 1001,
        "senderId": 2001,
        "receiverId": 2002,
        "messageType": "text",
        "content": "你好！",
        "status": 2,
        "isRecalled": false,
        "createdAt": "2025-11-28T10:30:00.000Z"
      }
    ],
    "total": 50
  }
}
```

#### 2.2 发送文字消息

```java
// 接口: POST /xypai-chat/api/message/send
Map<String, Object> sendRequest = new HashMap<>();
sendRequest.put("receiverId", 2002);
sendRequest.put("messageType", "text");
sendRequest.put("content", "Hello, this is a test message!");

// 响应
{
  "code": 200,
  "data": {
    "messageId": 5002,
    "conversationId": 1001,
    "senderId": 2001,
    "receiverId": 2002,
    "messageType": "text",
    "content": "Hello, this is a test message!",
    "status": 1,
    "createdAt": "2025-11-28T10:35:00.000Z"
  }
}
```

#### 2.3 发送图片消息

```java
// 接口: POST /xypai-chat/api/message/send
Map<String, Object> sendRequest = new HashMap<>();
sendRequest.put("receiverId", 2002);
sendRequest.put("messageType", "image");
sendRequest.put("mediaUrl", "https://cdn.example.com/chat/image.jpg");

// 响应
{
  "code": 200,
  "data": {
    "messageId": 5003,
    "messageType": "image",
    "mediaUrl": "https://cdn.example.com/chat/image.jpg"
  }
}
```

#### 2.4 发送语音消息

```java
// 接口: POST /xypai-chat/api/message/send
Map<String, Object> sendRequest = new HashMap<>();
sendRequest.put("receiverId", 2002);
sendRequest.put("messageType", "voice");
sendRequest.put("mediaUrl", "https://cdn.example.com/chat/voice.mp3");
sendRequest.put("duration", 30);

// 响应
{
  "code": 200,
  "data": {
    "messageId": 5004,
    "messageType": "voice",
    "mediaUrl": "https://cdn.example.com/chat/voice.mp3",
    "duration": 30
  }
}
```

#### 2.5 发送视频消息

```java
// 接口: POST /xypai-chat/api/message/send
Map<String, Object> sendRequest = new HashMap<>();
sendRequest.put("receiverId", 2002);
sendRequest.put("messageType", "video");
sendRequest.put("mediaUrl", "https://cdn.example.com/chat/video.mp4");
sendRequest.put("thumbnailUrl", "https://cdn.example.com/chat/video_thumb.jpg");
sendRequest.put("duration", 45);

// 响应
{
  "code": 200,
  "data": {
    "messageId": 5005,
    "messageType": "video",
    "mediaUrl": "https://cdn.example.com/chat/video.mp4",
    "thumbnailUrl": "https://cdn.example.com/chat/video_thumb.jpg",
    "duration": 45
  }
}
```

#### 2.6 标记消息已读

```java
// 接口: PUT /xypai-chat/api/message/read/{conversationId}
String readUrl = GATEWAY_URL + "/xypai-chat/api/message/read/1001";

// 响应
{
  "code": 200,
  "data": 5
}
```

#### 2.7 撤回消息（2分钟内）

```java
// 接口: POST /xypai-chat/api/message/recall/{messageId}
String recallUrl = GATEWAY_URL + "/xypai-chat/api/message/recall/5001";

// 响应
{
  "code": 200,
  "msg": "撤回成功"
}
```

#### 2.8 撤回消息（超过2分钟 - 预期失败）

```java
// 接口: POST /xypai-chat/api/message/recall/{messageId}
// 消息发送超过2分钟

// 响应 (预期错误)
{
  "code": 400,
  "msg": "消息已超过2分钟，无法撤回"
}
```

#### 2.9 删除消息

```java
// 接口: DELETE /xypai-chat/api/message/{messageId}
String deleteUrl = GATEWAY_URL + "/xypai-chat/api/message/5001";

// 响应
{
  "code": 200,
  "msg": "删除成功"
}
```

---

### 测试场景3: 消息流程集成测试 (MessageFlowIntegrationTest)

测试完整的消息流程。

#### 3.1 新会话流程

```java
// 1. 用户1检查会话列表（应为空）
// GET /api/message/conversations

// 2. 用户1发送第一条消息给用户2
// POST /api/message/send
{
  "receiverId": 2002,
  "messageType": "text",
  "content": "Hello User 2! This is our first message."
}

// 3. 验证双方会话列表都有该会话
// GET /api/message/conversations (用户1)
// GET /api/message/conversations (用户2)
```

#### 3.2 消息撤回流程

```java
// 1. 用户1发送消息
// POST /api/message/send

// 2. 用户1立即撤回消息（2分钟内）
// POST /api/message/recall/{messageId}

// 3. 验证消息已撤回
// GET /api/message/chat/{conversationId}
// 确认 isRecalled = true
```

#### 3.3 已读回执流程

```java
// 1. 用户2发送消息给用户1
// POST /api/message/send

// 2. 用户1打开聊天查看消息
// GET /api/message/chat/{conversationId}

// 3. 用户1标记已读
// PUT /api/message/read/{conversationId}

// 4. 验证消息状态变为已读 (status = 2)
// GET /api/message/chat/{conversationId}
```

---

### 运行测试

```bash
# 进入聊天服务目录
cd xypai-modules/xypai-chat

# 运行消息主页测试
mvn test -Dtest=MessageHomePageTest

# 运行聊天页面测试
mvn test -Dtest=ChatPageTest

# 运行消息流程集成测试
mvn test -Dtest=MessageFlowIntegrationTest

# 运行所有测试
mvn test
```

**测试前置条件**:
1. 确保 Nacos、Redis、MySQL 已启动
2. 确保 xypai-auth (9211) 服务已启动
3. 确保 xypai-chat (9404) 服务已启动
4. 确保 Gateway (8080) 已启动

---

**文档版本**: v1.0.0

**最后更新**: 2025-11-28
