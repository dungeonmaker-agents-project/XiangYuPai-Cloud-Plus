# XyPai-App-BFF 组局中心列表页面 API 对接文档

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
4. [活动列表接口](#活动列表接口)
5. [筛选参数说明](#筛选参数说明)
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
| 文档路径 | XiangYuPai-Doc/Action-API/模块化架构/03-content模块/Frontend/08-组局中心列表页面.md |
| 页面路由 | /activity/list |
| 页面名称 | 组局中心 |
| 用户角色 | 登录用户 |
| 页面类型 | 列表页面 |

### 涉及的后端服务

| 服务 | 端口 | 功能 |
|------|------|------|
| xypai-auth | 9211 | 用户认证 |
| xypai-app-bff | 9400 | 活动列表聚合 |

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

## 活动列表接口

### 1. 获取活动列表

获取组局活动列表，支持多种排序和筛选条件。

**请求**

```http
GET /xypai-app-bff/api/activity/list
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | integer | 否 | 1 | 页码，最小1 |
| pageSize | integer | 否 | 10 | 每页数量，1-100 |
| sortBy | string | 否 | smart_recommend | 排序方式 |
| gender | string | 否 | - | 性别筛选 |
| memberCount | string | 否 | - | 人数范围筛选 |
| activityType | string | 否 | - | 活动类型筛选 |

**排序方式 (sortBy)**

| 值 | 说明 |
|------|------|
| smart_recommend | 智能推荐（默认） |
| latest | 最新发布 |
| distance_asc | 距离最近 |

**性别筛选 (gender)**

| 值 | 说明 |
|------|------|
| all | 不限 |
| male | 男 |
| female | 女 |

**人数范围筛选 (memberCount)**

| 值 | 说明 |
|------|------|
| all | 不限 |
| 2-4 | 2-4人 |
| 5-10 | 5-10人 |
| 10+ | 10人以上 |

**活动类型筛选 (activityType)**

| 值 | 说明 |
|------|------|
| billiards | 台球 |
| basketball | 篮球 |
| badminton | 羽毛球 |
| dinner | 聚餐 |
| ktv | KTV |
| board_game | 桌游 |

**请求示例**

```http
GET /xypai-app-bff/api/activity/list?pageNum=1&pageSize=10&sortBy=smart_recommend&gender=female
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "list": [
      {
        "activityId": 8001,
        "status": "recruiting",
        "organizer": {
          "userId": 10001,
          "nickname": "台球达人",
          "avatar": "https://cdn.example.com/avatar/10001.jpg",
          "gender": "male",
          "isVerified": true,
          "tags": ["台球爱好者", "活跃组局人"]
        },
        "activityType": {
          "value": "billiards",
          "label": "台球",
          "icon": "🎱"
        },
        "description": "周末一起来打台球，新手老手都欢迎！",
        "schedule": {
          "startTime": "2025-11-30 14:00:00",
          "endTime": "2025-11-30 17:00:00",
          "displayText": "11月30日 周六 14:00-17:00"
        },
        "location": {
          "name": "星球台球俱乐部",
          "address": "深圳市南山区科技园南路88号",
          "city": "深圳",
          "district": "南山区",
          "distance": 1500,
          "distanceText": "1.5km"
        },
        "price": {
          "isPaid": true,
          "amount": 30,
          "unit": "元/人",
          "displayText": "30元/人"
        },
        "participants": {
          "current": 3,
          "max": 6,
          "displayText": "3/6人",
          "avatars": [
            "https://cdn.example.com/avatar/10002.jpg",
            "https://cdn.example.com/avatar/10003.jpg"
          ]
        },
        "tags": ["周末活动", "新手友好"],
        "createdAt": "2025-11-28 10:00:00"
      }
    ],
    "total": 50,
    "pageNum": 1,
    "pageSize": 10,
    "hasMore": true,
    "filters": {
      "sortOptions": [
        {"value": "smart_recommend", "label": "智能推荐", "selected": true},
        {"value": "latest", "label": "最新发布", "selected": false},
        {"value": "distance_asc", "label": "距离最近", "selected": false}
      ],
      "genderOptions": [
        {"value": "all", "label": "不限", "selected": true},
        {"value": "male", "label": "男", "selected": false},
        {"value": "female", "label": "女", "selected": false}
      ],
      "memberOptions": [
        {"value": "all", "label": "不限", "selected": true},
        {"value": "2-4", "label": "2-4人", "selected": false},
        {"value": "5-10", "label": "5-10人", "selected": false},
        {"value": "10+", "label": "10人以上", "selected": false}
      ],
      "activityTypes": [
        {"value": "billiards", "label": "台球", "icon": "🎱"},
        {"value": "basketball", "label": "篮球", "icon": "🏀"},
        {"value": "badminton", "label": "羽毛球", "icon": "🏸"},
        {"value": "dinner", "label": "聚餐", "icon": "🍽️"},
        {"value": "ktv", "label": "KTV", "icon": "🎤"},
        {"value": "board_game", "label": "桌游", "icon": "🎲"}
      ]
    }
  }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| list | array | 活动列表 |
| total | integer | 总记录数 |
| pageNum | integer | 当前页码 |
| pageSize | integer | 每页数量 |
| hasMore | boolean | 是否有更多数据 |
| filters | object | 筛选选项配置 |

**活动对象字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| activityId | long | 活动ID |
| status | string | 活动状态: recruiting/full/ended/cancelled |
| organizer | object | 组织者信息 |
| organizer.userId | long | 组织者用户ID |
| organizer.nickname | string | 昵称 |
| organizer.avatar | string | 头像URL |
| organizer.isVerified | boolean | 是否认证 |
| organizer.tags | array | 组织者标签 |
| activityType | object | 活动类型 |
| activityType.value | string | 类型标识 |
| activityType.label | string | 类型名称 |
| activityType.icon | string | 类型图标 |
| description | string | 活动描述 |
| schedule | object | 时间安排 |
| schedule.startTime | string | 开始时间 |
| schedule.endTime | string | 结束时间 |
| schedule.displayText | string | 时间显示文本 |
| location | object | 地点信息 |
| location.name | string | 地点名称 |
| location.address | string | 详细地址 |
| location.distance | integer | 距离（米） |
| location.distanceText | string | 距离显示文本 |
| price | object | 费用信息 |
| price.isPaid | boolean | 是否收费 |
| price.amount | integer | 费用金额 |
| price.displayText | string | 费用显示文本 |
| participants | object | 参与者信息 |
| participants.current | integer | 当前人数 |
| participants.max | integer | 最大人数 |
| participants.displayText | string | 人数显示文本 |
| participants.avatars | array | 参与者头像列表 |
| tags | array | 活动标签 |
| createdAt | string | 创建时间 |

---

## 筛选参数说明

### 排序选项

系统支持以下排序方式：

| 排序 | 说明 | 适用场景 |
|------|------|------|
| smart_recommend | 智能推荐 | 综合考虑用户偏好、距离、热度 |
| latest | 最新发布 | 查看最新发起的活动 |
| distance_asc | 距离最近 | 寻找附近的活动 |

### 性别筛选

| 选项 | 说明 |
|------|------|
| all | 显示所有性别的组织者 |
| male | 仅显示男性组织者 |
| female | 仅显示女性组织者 |

### 人数范围筛选

| 选项 | 说明 |
|------|------|
| all | 不限制人数 |
| 2-4 | 小型活动（2-4人） |
| 5-10 | 中型活动（5-10人） |
| 10+ | 大型活动（10人以上） |

### 活动类型筛选

| 选项 | 图标 | 说明 |
|------|------|------|
| billiards | 🎱 | 台球活动 |
| basketball | 🏀 | 篮球活动 |
| badminton | 🏸 | 羽毛球活动 |
| dinner | 🍽️ | 聚餐活动 |
| ktv | 🎤 | KTV活动 |
| board_game | 🎲 | 桌游活动 |

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

### 测试场景: 组局中心列表页面 (Page08_ActivityListTest)

测试组局中心列表页面的所有功能，包括列表查询、排序、筛选、分页等。

#### 测试1: 用户登录

```java
// 接口: POST /xypai-auth/api/auth/login/sms
Map<String, String> loginRequest = new HashMap<>();
loginRequest.put("countryCode", "+86");
loginRequest.put("mobile", "13900000001");  // 动态生成
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

#### 测试2: 获取活动列表（首页加载）

```java
// 接口: GET /xypai-app-bff/api/activity/list?pageNum=1&pageSize=10
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "data": {
    "list": [...],
    "total": 50,
    "hasMore": true
  }
}

// 断言
- list != null
- 保存第一个活动ID用于后续测试
```

#### 测试3: 应用排序（智能排序）

```java
// 接口: GET /xypai-app-bff/api/activity/list?pageNum=1&pageSize=10&sortBy=smart_recommend
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "data": {
    "list": [...]
  }
}

// 断言
- data != null
```

#### 测试4: 筛选性别（女性）

```java
// 接口: GET /xypai-app-bff/api/activity/list?pageNum=1&pageSize=10&gender=female
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "data": {
    "list": [...]
  }
}

// 断言
- data != null
- 筛选条件: gender=female
```

#### 测试5: 筛选人数（2-4人）

```java
// 接口: GET /xypai-app-bff/api/activity/list?pageNum=1&pageSize=10&memberCount=2-4
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "data": {
    "list": [...]
  }
}

// 断言
- data != null
- 筛选条件: memberCount=2-4
```

#### 测试6: 组合筛选（性别+人数）

```java
// 接口: GET /xypai-app-bff/api/activity/list?pageNum=1&pageSize=10&gender=male&memberCount=5-10
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "data": {
    "list": [...]
  }
}

// 断言
- data != null
- 筛选条件: gender=male, memberCount=5-10
```

#### 测试7: 分页加载更多

```java
// 第一页
// 接口: GET /xypai-app-bff/api/activity/list?pageNum=1&pageSize=10
{
  "code": 200,
  "data": {
    "list": [...],
    "hasMore": true
  }
}

// 第二页
// 接口: GET /xypai-app-bff/api/activity/list?pageNum=2&pageSize=10
{
  "code": 200,
  "data": {
    "list": [...],
    "hasMore": true或false
  }
}

// 断言
- 第一页 hasMore 为 true（假设总数据超过10条）
- 第二页数据与第一页不重复
```

#### 测试8: 验证活动卡片数据完整性

```java
// 接口: GET /xypai-app-bff/api/activity/list?pageNum=1&pageSize=5
// 请求头: Authorization: Bearer {token}

// 响应数据验证
{
  "code": 200,
  "data": {
    "list": [
      {
        "activityId": 8001,
        "status": "recruiting",
        "organizer": {
          "userId": 10001,
          "nickname": "台球达人",
          "avatar": "..."
        },
        "activityType": {"label": "台球"},
        "schedule": {"displayText": "..."},
        "location": {"address": "..."},
        "price": {"displayText": "..."},
        "participants": {"displayText": "..."}
      }
    ]
  }
}

// 断言
- activityId != null
- status != null
- organizer.userId != null
- organizer.nickname != null
```

#### 测试9: 验证筛选配置选项

```java
// 接口: GET /xypai-app-bff/api/activity/list?pageNum=1&pageSize=1
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "data": {
    "filters": {
      "sortOptions": [...],     // 排序选项
      "genderOptions": [...],   // 性别选项
      "memberOptions": [...],   // 人数选项
      "activityTypes": [...]    // 活动类型
    }
  }
}

// 断言
- filters != null
- sortOptions 数量 >= 1
- genderOptions 数量 >= 1
- activityTypes 数量 >= 1
```

#### 测试10: 按活动类型筛选

```java
// 接口: GET /xypai-app-bff/api/activity/list?pageNum=1&pageSize=10&activityType=billiards
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "data": {
    "list": [...]
  }
}

// 断言
- data != null
- 筛选条件: activityType=billiards (台球)
```

---

### 运行测试

```bash
# 进入聚合服务目录
cd xypai-aggregation/xypai-app-bff

# 运行组局中心列表页面测试
mvn test -Dtest=Page08_ActivityListTest

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
│                    组局中心列表页面测试流程                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. 用户登录                                                 │
│     POST /xypai-auth/api/auth/login/sms                     │
│     └── 获取 Token                                          │
│                                                             │
│  2. 获取活动列表（首页加载）                                  │
│     GET /xypai-app-bff/api/activity/list                    │
│     └── 获取第一个活动ID                                     │
│                                                             │
│  3. 排序测试                                                 │
│     └── sortBy=smart_recommend                              │
│                                                             │
│  4. 筛选测试                                                 │
│     ├── gender=female                                       │
│     ├── memberCount=2-4                                     │
│     ├── gender=male&memberCount=5-10                        │
│     └── activityType=billiards                              │
│                                                             │
│  5. 分页测试                                                 │
│     ├── pageNum=1 → 验证 hasMore                            │
│     └── pageNum=2 → 验证数据不重复                           │
│                                                             │
│  6. 数据验证                                                 │
│     ├── 验证活动卡片数据完整性                                │
│     └── 验证筛选配置选项                                     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

**文档版本**: v1.0.0

**最后更新**: 2025-11-29

**测试文件**: `xypai-aggregation/xypai-app-bff/src/test/java/org/dromara/appbff/pages/Page08_ActivityListTest.java`
