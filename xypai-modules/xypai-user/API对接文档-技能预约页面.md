# XyPai-User 技能预约页面 API 对接文档

> **版本**: v1.1.0
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
4. [技能管理接口](#技能管理接口)
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

分页接口统一返回以下格式：

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
| 页面路由 | /skill/booking |
| 页面名称 | 技能预约 |
| 用户角色 | 登录用户 |
| 页面类型 | 详情+预约页面 |

### 涉及的后端服务

| 服务 | 端口 | 功能 |
|------|------|------|
| xypai-auth | 9211 | 用户认证 |
| xypai-user | 9401 | 技能管理、详情查询 |
| xypai-trade | (另见) | 订单创建、支付 |

### 功能说明

本页面主要展示技能预约相关信息：
- 技能详情（包含技能信息、价格、游戏信息等）
- 用户技能列表（我的技能）

> **注意**:
> - 订单创建、支付等功能属于 `xypai-trade` 模块，不在本文档范围内。
> - 技能评价功能需要 `skill_reviews` 表，当前版本暂未实现。

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

## 技能管理接口

### 1. 创建线上技能

创建一个线上技能（如游戏陪玩）。

**请求**

```http
POST /xypai-user/api/user/skills/online
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "gameName": "王者荣耀",
  "gameRank": "王者",
  "skillName": "王者荣耀陪玩",
  "description": "专业王者荣耀陪玩，段位王者，有丰富的游戏经验，可以带飞上分！",
  "price": 50,
  "serviceHours": 1
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| gameName | string | 是 | 游戏名称 |
| gameRank | string | 是 | 游戏段位 |
| skillName | string | 是 | 技能名称 |
| description | string | 是 | 技能描述 |
| price | integer | 是 | 价格（金币/小时） |
| serviceHours | integer | 是 | 服务时长（小时） |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": 1001
}
```

> **说明**: `data` 返回新创建的技能ID（Long类型）

---

### 2. 获取技能详情

获取指定技能的详细信息。

**请求**

```http
GET /xypai-user/api/user/skills/{skillId}
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| skillId | long | 是 | 技能ID |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "skillId": 1001,
    "userId": 10001,
    "skillName": "王者荣耀陪玩",
    "skillType": "online",
    "description": "专业王者荣耀陪玩，段位王者，有丰富的游戏经验...",
    "price": 50,
    "gameName": "王者荣耀",
    "gameRank": "王者",
    "status": 1,
    "rating": 0.0,
    "reviewCount": 0,
    "orderCount": 0,
    "createdAt": "2025-11-29 10:00:00"
  }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| skillId | long | 技能ID |
| userId | long | 用户ID |
| skillName | string | 技能名称 |
| skillType | string | 技能类型: online/offline |
| description | string | 技能描述 |
| price | integer | 价格（金币/小时） |
| gameName | string | 游戏名称 |
| gameRank | string | 游戏段位 |
| status | integer | 状态: 0-下架, 1-上架 |
| rating | decimal | 评分 |
| reviewCount | integer | 评价数量 |
| orderCount | integer | 订单数量 |
| createdAt | string | 创建时间 |

---

### 3. 获取我的技能列表

获取当前登录用户的技能列表。

**请求**

```http
GET /xypai-user/api/user/skills/my
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 20 | 每页数量 |

**请求示例**

```http
GET /xypai-user/api/user/skills/my?pageNum=1&pageSize=20
```

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
      "status": 1,
      "rating": 4.8,
      "orderCount": 128
    }
  ],
  "total": 1
}
```

---

### 4. 删除技能

删除指定技能。

**请求**

```http
DELETE /xypai-user/api/user/skills/{skillId}
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| skillId | long | 是 | 技能ID |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

---

### 5. 获取用户技能列表

获取指定用户的技能列表（公开接口）。

**请求**

```http
GET /xypai-user/api/user/skills/user/{userId}
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 用户ID |

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
      "status": 1,
      "rating": 4.8,
      "orderCount": 128
    }
  ],
  "total": 1
}
```

---

### 6. 获取附近技能

获取附近的技能列表（基于地理位置）。

**请求**

```http
GET /xypai-user/api/user/skills/nearby
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| longitude | decimal | 是 | - | 经度 |
| latitude | decimal | 是 | - | 纬度 |
| distance | integer | 否 | 5000 | 距离范围（米） |
| pageNum | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 20 | 每页数量 |

**请求示例**

```http
GET /xypai-user/api/user/skills/nearby?longitude=114.0579&latitude=22.5431&distance=5000
```

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权，请先登录 |
| 404 | 技能不存在 |
| 500 | 服务器内部错误 |

### 常见业务错误

| 错误信息 | 说明 |
|----------|------|
| 技能不存在 | skillId 对应的技能不存在 |
| 技能已下架 | 技能已被服务提供者下架 |
| 无权操作 | 当前用户无权操作该技能 |

---

## 集成测试用例

### 测试环境配置

```
Gateway:    http://localhost:8080
xypai-auth: http://localhost:9211 (认证服务)
xypai-user: http://localhost:9401 (用户技能服务)
```

**依赖服务**: Nacos, Redis, MySQL

---

### 测试场景: 技能预约页面 (AppSkillBookingPageTest)

测试技能预约页面的UserService相关功能，包括技能创建、详情查询、列表获取等。

#### 测试1: 用户登录/注册

```java
// 接口: POST /xypai-auth/api/auth/login/sms
Map<String, String> loginRequest = new HashMap<>();
loginRequest.put("countryCode", "+86");
loginRequest.put("mobile", "13800000001");  // 动态生成唯一手机号
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
- userId != null
```

#### 测试2: 创建测试技能

```java
// 接口: POST /xypai-user/api/user/skills/online
// 请求头: Authorization: Bearer {token}

Map<String, Object> skillRequest = new HashMap<>();
skillRequest.put("gameName", "王者荣耀");
skillRequest.put("gameRank", "王者");
skillRequest.put("skillName", "王者荣耀陪玩");
skillRequest.put("description", "专业王者荣耀陪玩...");
skillRequest.put("price", 50);
skillRequest.put("serviceHours", 1);

// 响应
{
  "code": 200,
  "data": 1001  // 返回技能ID
}

// 断言
- code == 200
- data (skillId) != null
```

#### 测试3: 获取技能详情

```java
// 接口: GET /xypai-user/api/user/skills/{skillId}
// 请求头: Authorization: Bearer {token}

String detailUrl = GATEWAY_URL + "/xypai-user/api/user/skills/" + testSkillId;

// 响应
{
  "code": 200,
  "data": {
    "skillId": 1001,
    "skillName": "王者荣耀陪玩",
    "skillType": "online",
    "price": 50,
    "gameName": "王者荣耀",
    "gameRank": "王者"
  }
}

// 断言
- skillId != null
- skillName != null
- price != null
```

#### 测试4: 获取我的技能列表

```java
// 接口: GET /xypai-user/api/user/skills/my?pageNum=1&pageSize=20
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "rows": [
    {
      "skillId": 1001,
      "skillName": "王者荣耀陪玩",
      ...
    }
  ],
  "total": 1
}

// 断言
- rows != null
- 技能数量 >= 1
```

#### 测试5: 删除测试技能（清理数据）

```java
// 接口: DELETE /xypai-user/api/user/skills/{skillId}
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

# 运行技能预约页面测试
mvn test -Dtest=AppSkillBookingPageTest

# 运行所有测试
mvn test
```

**测试前置条件**:
1. 确保 Nacos、Redis、MySQL 已启动
2. 确保 xypai-auth (9211) 服务已启动
3. 确保 xypai-user (9401) 服务已启动
4. 确保 Gateway (8080) 已启动

---

### 测试流程图

```
┌─────────────────────────────────────────────────────────────┐
│                    技能预约页面测试流程                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. 用户登录/注册                                            │
│     POST /xypai-auth/api/auth/login/sms                     │
│     └── 获取 Token                                          │
│                                                             │
│  2. 创建测试技能                                             │
│     POST /xypai-user/api/user/skills/online                 │
│     └── 获取 skillId                                        │
│                                                             │
│  3. 获取技能详情                                             │
│     GET /xypai-user/api/user/skills/{skillId}               │
│     ├── 技能基本信息                                         │
│     ├── 价格信息                                             │
│     └── 游戏信息                                             │
│                                                             │
│  4. 获取我的技能列表                                         │
│     GET /xypai-user/api/user/skills/my                      │
│     └── 验证技能数量                                         │
│                                                             │
│  5. 删除测试技能（清理数据）                                  │
│     DELETE /xypai-user/api/user/skills/{skillId}            │
│     └── 确保测试数据清理                                     │
│                                                             │
│  💡 注意：                                                   │
│  - 订单创建、支付等属于 xypai-trade 模块                     │
│  - 技能评价功能需要 skill_reviews 表（暂未实现）             │
│  - 完整预约流程需配合 xypai-trade 模块                       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

### 接口实现状态

| 接口 | 状态 | 说明 |
|------|------|------|
| POST /api/user/skills/online | ✅ 已实现 | 创建线上技能 |
| POST /api/user/skills/offline | ✅ 已实现 | 创建线下技能 |
| GET /api/user/skills/{skillId} | ✅ 已实现 | 获取技能详情 |
| GET /api/user/skills/my | ✅ 已实现 | 获取我的技能列表 |
| GET /api/user/skills/user/{userId} | ✅ 已实现 | 获取用户技能列表 |
| GET /api/user/skills/nearby | ✅ 已实现 | 获取附近技能 |
| PUT /api/user/skills/{skillId} | ✅ 已实现 | 更新技能信息 |
| DELETE /api/user/skills/{skillId} | ✅ 已实现 | 删除技能 |
| PUT /api/user/skills/{skillId}/toggle | ✅ 已实现 | 切换技能上下架状态 |

> **注意**: 技能评价接口（`/api/skills/{skillId}/reviews`）需要 `skill_reviews` 表，当前版本暂未实现。

---

**文档版本**: v1.1.0

**最后更新**: 2025-11-29

**测试文件**: `xypai-modules/xypai-user/src/test/java/org/dromara/user/AppSkillBookingPageTest.java`
