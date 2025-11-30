# XyPai-App-BFF 组局详情页面 API 对接文档

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
4. [活动详情接口](#活动详情接口)
5. [活动报名接口](#活动报名接口)
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

游客可以查看活动详情（不带Token），但无法报名参加。

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
| 文档路径 | XiangYuPai-Doc/Action-API/模块化架构/03-content模块/Frontend/09-组局详情页面.md |
| 页面路由 | /activity/detail |
| 页面名称 | 组局详情 |
| 用户角色 | 所有用户 |
| 页面类型 | 详情页面 |

### 涉及的后端服务

| 服务 | 端口 | 功能 |
|------|------|------|
| xypai-auth | 9211 | 用户认证 |
| xypai-app-bff | 9400 | 活动详情/报名 |

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
  "mobile": "13800000010",
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

## 活动详情接口

### 1. 获取活动详情

获取单个活动的完整信息，包括组织者、参与者、时间地点等。

**请求**

```http
GET /xypai-app-bff/api/activity/detail/{activityId}
Authorization: Bearer <token>  (可选，游客可不带)
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| activityId | long | 是 | 活动ID |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "activityId": 8001,
    "status": "recruiting",
    "statusText": "招募中",
    "description": "周末一起来打台球，新手老手都欢迎！环境优雅，设备专业。",

    "organizer": {
      "userId": 10001,
      "nickname": "台球达人",
      "avatar": "https://cdn.example.com/avatar/10001.jpg",
      "gender": "male",
      "age": 28,
      "isVerified": true,
      "tags": ["台球爱好者", "活跃组局人"],
      "bio": "热爱台球，每周必打"
    },

    "activityType": "billiards",
    "activityTypeName": "台球",
    "activityTypeIcon": "🎱",

    "startTime": "2025-11-30 14:00:00",
    "endTime": "2025-11-30 17:00:00",
    "timeDisplay": "11月30日 周六 14:00-17:00",

    "locationName": "星球台球俱乐部",
    "locationAddress": "深圳市南山区科技园南路88号",
    "city": "深圳",
    "district": "南山区",
    "longitude": 114.0579,
    "latitude": 22.5431,

    "isPaid": true,
    "fee": 30,
    "feeDisplay": "30元/人",
    "feeDescription": "包含场地费和饮料",

    "registrationDeadline": "2025-11-30 12:00:00",
    "registrationDeadlineDisplay": "报名截止: 11月30日 12:00",

    "currentMembers": 3,
    "maxMembers": 6,
    "membersDisplay": "3/6人",
    "pendingCount": 2,

    "participants": [
      {
        "userId": 10002,
        "nickname": "小明",
        "avatar": "https://cdn.example.com/avatar/10002.jpg",
        "gender": "male",
        "status": "confirmed",
        "statusText": "已确认",
        "joinTime": "2025-11-28 15:00:00"
      },
      {
        "userId": 10003,
        "nickname": "小红",
        "avatar": "https://cdn.example.com/avatar/10003.jpg",
        "gender": "female",
        "status": "pending",
        "statusText": "待确认",
        "joinTime": "2025-11-29 10:00:00"
      }
    ],

    "isOrganizer": false,
    "currentUserStatus": "none",
    "canRegister": true,
    "cannotRegisterReason": null,
    "canCancel": false,

    "tags": ["周末活动", "新手友好", "提供饮料"],
    "images": [
      "https://cdn.example.com/activity/8001_1.jpg",
      "https://cdn.example.com/activity/8001_2.jpg"
    ],

    "viewCount": 128,
    "shareCount": 15,
    "createdAt": "2025-11-28 10:00:00"
  }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| activityId | long | 活动ID |
| status | string | 活动状态: recruiting/full/ended/cancelled |
| statusText | string | 状态显示文本 |
| description | string | 活动描述 |

**组织者信息 (organizer)**

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | long | 组织者用户ID |
| nickname | string | 昵称 |
| avatar | string | 头像URL |
| gender | string | 性别: male/female |
| age | integer | 年龄 |
| isVerified | boolean | 是否认证 |
| tags | array | 组织者标签 |
| bio | string | 个人简介 |

**活动类型信息**

| 字段 | 类型 | 说明 |
|------|------|------|
| activityType | string | 活动类型标识 |
| activityTypeName | string | 活动类型名称 |
| activityTypeIcon | string | 活动类型图标 |

**时间信息**

| 字段 | 类型 | 说明 |
|------|------|------|
| startTime | string | 开始时间 |
| endTime | string | 结束时间 |
| timeDisplay | string | 时间显示文本 |
| registrationDeadline | string | 报名截止时间 |
| registrationDeadlineDisplay | string | 报名截止显示文本 |

**地点信息**

| 字段 | 类型 | 说明 |
|------|------|------|
| locationName | string | 地点名称 |
| locationAddress | string | 详细地址 |
| city | string | 城市 |
| district | string | 区域 |
| longitude | decimal | 经度 |
| latitude | decimal | 纬度 |

**费用信息**

| 字段 | 类型 | 说明 |
|------|------|------|
| isPaid | boolean | 是否收费 |
| fee | integer | 费用金额 |
| feeDisplay | string | 费用显示文本 |
| feeDescription | string | 费用说明 |

**参与者信息**

| 字段 | 类型 | 说明 |
|------|------|------|
| currentMembers | integer | 当前人数 |
| maxMembers | integer | 最大人数 |
| membersDisplay | string | 人数显示文本 |
| pendingCount | integer | 待确认人数 |
| participants | array | 参与者列表 |

**参与者对象**

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | long | 用户ID |
| nickname | string | 昵称 |
| avatar | string | 头像URL |
| gender | string | 性别 |
| status | string | 状态: pending/confirmed/rejected |
| statusText | string | 状态显示文本 |
| joinTime | string | 报名时间 |

**当前用户状态**

| 字段 | 类型 | 说明 |
|------|------|------|
| isOrganizer | boolean | 是否是组织者 |
| currentUserStatus | string | 用户状态: none/pending/confirmed/rejected |
| canRegister | boolean | 是否可以报名 |
| cannotRegisterReason | string | 不能报名的原因 |
| canCancel | boolean | 是否可以取消报名 |

---

## 活动报名接口

### 1. 报名参加活动

用户报名参加活动。

**请求**

```http
POST /xypai-app-bff/api/activity/register
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "activityId": 8001,
  "message": "我想参加这个活动！"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| activityId | long | 是 | 活动ID |
| message | string | 否 | 报名留言 |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "success": true,
    "status": "pending",
    "statusMessage": "报名成功，等待组织者确认",
    "needPay": true,
    "payAmount": 30,
    "currentMembers": 4,
    "maxMembers": 6
  }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| success | boolean | 报名是否成功 |
| status | string | 报名状态: pending/confirmed |
| statusMessage | string | 状态说明消息 |
| needPay | boolean | 是否需要支付 |
| payAmount | integer | 支付金额 |
| currentMembers | integer | 当前参与人数 |
| maxMembers | integer | 最大人数 |

---

### 2. 取消报名

用户取消已报名的活动。

**请求**

```http
POST /xypai-app-bff/api/activity/register/cancel
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "activityId": 8001,
  "registrationId": 9001
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| activityId | long | 是 | 活动ID |
| registrationId | long | 否 | 报名记录ID（可选） |

**响应示例**

```json
{
  "code": 200,
  "msg": "取消成功",
  "data": {
    "success": true,
    "refundInfo": {
      "hasRefund": true,
      "refundAmount": 30,
      "refundStatus": "processing",
      "refundMessage": "退款处理中，预计1-3个工作日到账"
    }
  }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| success | boolean | 取消是否成功 |
| refundInfo | object | 退款信息（如有） |
| refundInfo.hasRefund | boolean | 是否有退款 |
| refundInfo.refundAmount | integer | 退款金额 |
| refundInfo.refundStatus | string | 退款状态 |
| refundInfo.refundMessage | string | 退款说明 |

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权，请先登录 |
| 403 | 无权限操作 |
| 404 | 活动不存在 |
| 500 | 服务器内部错误 |

### 常见业务错误

| 错误信息 | 说明 |
|----------|------|
| 活动不存在 | activityId 对应的活动不存在 |
| 活动已结束 | 活动已结束，无法报名 |
| 活动已满员 | 活动人数已满，无法报名 |
| 报名已截止 | 已超过报名截止时间 |
| 已报名该活动 | 用户已报名，不能重复报名 |
| 未报名该活动 | 用户未报名，无法取消 |
| 组织者不能报名 | 组织者不能报名自己的活动 |

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

### 测试场景: 组局详情页面 (Page09_ActivityDetailTest)

测试组局详情页面的所有功能，包括活动详情、组织者信息、参与者列表、报名、取消报名等。

#### 测试1: 用户A登录

```java
// 接口: POST /xypai-auth/api/auth/login/sms
Map<String, String> loginRequest = new HashMap<>();
loginRequest.put("countryCode", "+86");
loginRequest.put("mobile", "13800000010");
loginRequest.put("verificationCode", "123456");

// 响应
{
  "code": 200,
  "data": {
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "userId": 10001
  }
}

// 断言
- token != null
- 从活动列表获取真实活动ID
```

#### 测试2: 获取活动详情

```java
// 接口: GET /xypai-app-bff/api/activity/detail/{activityId}
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "data": {
    "activityId": 8001,
    "status": "recruiting",
    "description": "周末一起来打台球..."
  }
}

// 断言
- activityId != null
- status != null
```

#### 测试3: 验证组织者信息

```java
// 接口: GET /xypai-app-bff/api/activity/detail/{activityId}
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "data": {
    "organizer": {
      "userId": 10001,
      "nickname": "台球达人",
      "avatar": "...",
      "isVerified": true,
      "tags": ["台球爱好者"]
    }
  }
}

// 断言
- organizer.userId != null
- organizer.nickname != null
```

#### 测试4: 验证活动详情数据结构

```java
// 接口: GET /xypai-app-bff/api/activity/detail/{activityId}

// 验证字段
- activityType
- activityTypeName
- startTime / endTime / timeDisplay
- locationName / locationAddress / city / district
- isPaid / fee / feeDisplay
- registrationDeadline / registrationDeadlineDisplay

// 断言
- data != null
```

#### 测试5: 验证参与者列表

```java
// 接口: GET /xypai-app-bff/api/activity/detail/{activityId}

// 响应
{
  "code": 200,
  "data": {
    "currentMembers": 3,
    "maxMembers": 6,
    "membersDisplay": "3/6人",
    "pendingCount": 2,
    "participants": [
      {
        "userId": 10002,
        "nickname": "小明",
        "status": "confirmed"
      }
    ]
  }
}

// 断言
- currentMembers >= 0
- maxMembers > 0
- participants 可以为空列表
```

#### 测试6: 验证用户状态

```java
// 接口: GET /xypai-app-bff/api/activity/detail/{activityId}

// 响应
{
  "code": 200,
  "data": {
    "isOrganizer": false,
    "currentUserStatus": "none",
    "canRegister": true,
    "cannotRegisterReason": null,
    "canCancel": false
  }
}

// 断言
- 验证用户状态字段存在
```

#### 测试7: 用户A报名参加活动

```java
// 接口: POST /xypai-app-bff/api/activity/register
// 请求头: Authorization: Bearer {token}
Map<String, Object> request = new HashMap<>();
request.put("activityId", testActivityId);
request.put("message", "我想参加这个活动！");

// 响应
{
  "code": 200,
  "data": {
    "success": true,
    "status": "pending",
    "statusMessage": "报名成功，等待组织者确认",
    "needPay": true,
    "payAmount": 30,
    "currentMembers": 4,
    "maxMembers": 6
  }
}

// 断言
- success == true 或 status != null
```

#### 测试8: 验证报名后活动详情变化

```java
// 接口: GET /xypai-app-bff/api/activity/detail/{activityId}
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "data": {
    "isOrganizer": false,
    "currentUserStatus": "pending",  // 已变更
    "canRegister": false,
    "cannotRegisterReason": "已报名该活动",
    "canCancel": true  // 可以取消
  }
}

// 断言
- 用户状态已更新
```

#### 测试9: 用户A取消报名

```java
// 接口: POST /xypai-app-bff/api/activity/register/cancel
// 请求头: Authorization: Bearer {token}
Map<String, Object> request = new HashMap<>();
request.put("activityId", testActivityId);
request.put("registrationId", testRegistrationId);

// 响应
{
  "code": 200,
  "data": {
    "refundInfo": {
      "hasRefund": true,
      "refundAmount": 30
    }
  }
}

// 断言
- 取消成功或返回相应提示
```

#### 测试10: 用户B登录

```java
// 接口: POST /xypai-auth/api/auth/login/sms
Map<String, String> loginRequest = new HashMap<>();
loginRequest.put("countryCode", "+86");
loginRequest.put("mobile", "13800000011");
loginRequest.put("verificationCode", "123456");

// 响应
{
  "code": 200,
  "data": {
    "token": "...",
    "userId": 10002
  }
}

// 断言
- token != null
```

#### 测试11: 用户B报名同一活动

```java
// 接口: POST /xypai-app-bff/api/activity/register
// 请求头: Authorization: Bearer {tokenB}
Map<String, Object> request = new HashMap<>();
request.put("activityId", testActivityId);
request.put("message", "用户B也想参加！");

// 响应
{
  "code": 200,
  "data": {
    "success": true,
    "status": "pending"
  }
}

// 断言
- 报名成功或返回相应状态
```

#### 测试12: 游客访问活动详情

```java
// 接口: GET /xypai-app-bff/api/activity/detail/{activityId}
// 不带 Authorization header

// 响应
{
  "code": 200,
  "data": {
    "activityId": 8001,
    "status": "recruiting",
    // 用户状态相关字段可能为null
    "userStatus": null
  }
}

// 断言
- 游客可以查看活动详情
- 用户状态相关字段为null或默认值
```

---

### 运行测试

```bash
# 进入聚合服务目录
cd xypai-aggregation/xypai-app-bff

# 运行组局详情页面测试
mvn test -Dtest=Page09_ActivityDetailTest

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
│                    组局详情页面测试流程                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. 用户A登录                                                │
│     POST /xypai-auth/api/auth/login/sms                     │
│     └── 获取 TokenA                                         │
│                                                             │
│  2. 获取活动详情                                             │
│     GET /xypai-app-bff/api/activity/detail/{activityId}     │
│                                                             │
│  3. 验证详情数据                                             │
│     ├── 验证组织者信息                                       │
│     ├── 验证活动详情数据结构                                  │
│     ├── 验证参与者列表                                       │
│     └── 验证用户状态                                         │
│                                                             │
│  4. 用户A报名流程                                            │
│     ├── POST /api/activity/register → 报名                  │
│     ├── GET /api/activity/detail → 验证状态变化              │
│     └── POST /api/activity/register/cancel → 取消报名       │
│                                                             │
│  5. 用户B登录                                                │
│     POST /xypai-auth/api/auth/login/sms                     │
│     └── 获取 TokenB                                         │
│                                                             │
│  6. 用户B报名同一活动                                        │
│     POST /xypai-app-bff/api/activity/register               │
│     └── 验证多用户报名场景                                   │
│                                                             │
│  7. 游客访问                                                 │
│     GET /api/activity/detail/{activityId} (无Token)         │
│     └── 验证游客可查看详情                                   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

**文档版本**: v1.0.0

**最后更新**: 2025-11-29

**测试文件**: `xypai-aggregation/xypai-app-bff/src/test/java/org/dromara/appbff/pages/Page09_ActivityDetailTest.java`
