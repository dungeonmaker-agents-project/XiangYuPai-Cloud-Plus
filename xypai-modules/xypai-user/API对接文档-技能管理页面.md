# XyPai-User 技能管理页面 API 对接文档

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
| 页面路由 | /skills/manage |
| 页面名称 | 技能管理 |
| 用户角色 | 登录用户 |
| 页面类型 | 列表管理页面 |

### 涉及的后端服务

| 服务 | 端口 | 功能 |
|------|------|------|
| xypai-auth | 9211 | 用户认证 |
| xypai-user | 9401 | 技能管理 |

### 功能说明

本页面主要展示技能管理相关功能：
- 获取我的技能列表
- 按类型筛选技能（线上/线下）
- 切换技能上下架状态
- 删除技能

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

## 技能管理接口

### 1. 获取我的技能列表

获取当前用户的技能列表。

**请求**

```http
GET /xypai-user/api/user/skills/my
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 10 | 每页数量 |
| skillType | string | 否 | - | 技能类型: online/offline |

**请求示例**

```http
GET /xypai-user/api/user/skills/my?pageNum=1&pageSize=10
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
      "description": "专业王者荣耀陪玩",
      "price": 50,
      "gameName": "王者荣耀",
      "gameRank": "王者",
      "status": 1,
      "rating": 4.8,
      "orderCount": 128,
      "createdAt": "2025-11-01 10:00:00"
    }
  ],
  "total": 1
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| skillId | long | 技能ID |
| skillName | string | 技能名称 |
| skillType | string | 技能类型: online/offline |
| description | string | 技能描述 |
| price | integer | 价格（金币/小时） |
| gameName | string | 游戏名称 |
| gameRank | string | 游戏段位 |
| status | integer | 状态: 0-下架, 1-上架 |
| rating | decimal | 评分 |
| orderCount | integer | 订单数量 |
| createdAt | string | 创建时间 |

---

### 2. 获取线上技能列表

获取线上类型的技能列表。

**请求**

```http
GET /xypai-user/api/user/skills/my?skillType=online
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| skillType | string | 是 | 固定值: online |
| pageNum | integer | 否 | 页码 |
| pageSize | integer | 否 | 每页数量 |

**请求示例**

```http
GET /xypai-user/api/user/skills/my?skillType=online&pageNum=1&pageSize=10
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
      "price": 50
    }
  ],
  "total": 1
}
```

---

### 3. 获取线下技能列表

获取线下类型的技能列表。

**请求**

```http
GET /xypai-user/api/user/skills/my?skillType=offline
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| skillType | string | 是 | 固定值: offline |
| pageNum | integer | 否 | 页码 |
| pageSize | integer | 否 | 每页数量 |

**请求示例**

```http
GET /xypai-user/api/user/skills/my?skillType=offline&pageNum=1&pageSize=10
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "rows": [
    {
      "skillId": 2001,
      "skillName": "台球陪玩",
      "skillType": "offline",
      "price": 100
    }
  ],
  "total": 1
}
```

---

### 4. 切换技能上下架状态

切换指定技能的上下架状态。

**请求**

```http
PUT /xypai-user/api/user/skills/{skillId}/toggle
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
    "status": 0,
    "statusText": "已下架"
  }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| skillId | long | 技能ID |
| status | integer | 新状态: 0-下架, 1-上架 |
| statusText | string | 状态文本 |

---

### 5. 删除技能

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

### 6. 创建线上技能

创建一个线上技能。

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
  "description": "专业王者荣耀陪玩",
  "price": 50,
  "serviceHours": 1
}
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": 1001
}
```

---

### 7. 创建线下技能

创建一个线下技能。

**请求**

```http
POST /xypai-user/api/user/skills/offline
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "skillName": "台球陪玩",
  "description": "专业台球陪玩",
  "price": 100,
  "serviceHours": 2,
  "location": "广州市天河区"
}
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": 2001
}
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
| 无权操作 | 当前用户无权操作该技能 |
| 技能名称已存在 | 技能名称重复 |

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

### 测试场景: 技能管理页面 (AppSkillManagementPageTest)

测试技能管理页面的完整功能。

#### 测试1: 新用户注册

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

#### 测试2: 获取我的技能列表

```java
// 接口: GET /xypai-user/api/user/skills/my?pageNum=1&pageSize=10
// 请求头: Authorization: Bearer {token}

// 响应
{
  "rows": [...],
  "total": 0
}

// 断言
- rows != null
```

#### 测试3: 获取线上技能列表

```java
// 接口: GET /xypai-user/api/user/skills/my?skillType=online&pageNum=1&pageSize=10
// 请求头: Authorization: Bearer {token}

// 断言
- rows != null
```

#### 测试4: 获取线下技能列表

```java
// 接口: GET /xypai-user/api/user/skills/my?skillType=offline&pageNum=1&pageSize=10
// 请求头: Authorization: Bearer {token}

// 断言
- rows != null
```

#### 测试5: 切换技能上下架状态

```java
// 前提: 需要先创建技能
// 接口: PUT /xypai-user/api/user/skills/{skillId}/toggle
// 请求头: Authorization: Bearer {token}

// 断言
- code == 200
```

#### 测试6: 删除技能

```java
// 前提: 需要先创建技能
// 接口: DELETE /xypai-user/api/user/skills/{skillId}
// 请求头: Authorization: Bearer {token}

// 断言
- code == 200
```

---

### 运行测试

```bash
# 进入用户服务目录
cd xypai-modules/xypai-user

# 运行技能管理页面测试
mvn test -Dtest=AppSkillManagementPageTest

# 运行所有测试
mvn test
```

---

### 测试流程图

```
┌─────────────────────────────────────────────────────────────┐
│                    技能管理页面测试流程                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. 用户登录                                                 │
│     POST /xypai-auth/api/auth/login/sms                     │
│     └── 获取 Token                                          │
│                                                             │
│  2. 获取我的技能列表                                         │
│     GET /xypai-user/api/user/skills/my                      │
│     └── 验证技能数量                                         │
│                                                             │
│  3. 按类型筛选                                               │
│     ├── GET /api/user/skills/my?skillType=online            │
│     │   └── 获取线上技能                                     │
│     └── GET /api/user/skills/my?skillType=offline           │
│         └── 获取线下技能                                     │
│                                                             │
│  4. 切换技能状态（需先创建技能）                             │
│     PUT /xypai-user/api/user/skills/{skillId}/toggle        │
│     └── 验证状态变更                                         │
│                                                             │
│  5. 删除技能（需先创建技能）                                 │
│     DELETE /xypai-user/api/user/skills/{skillId}            │
│     └── 验证删除成功                                         │
│                                                             │
│  💡 提示：                                                   │
│  - 测试5和测试6需要先手动创建技能                            │
│  - 可使用 POST /api/user/skills/online 创建技能             │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

### 接口实现状态

| 接口 | 状态 | 说明 |
|------|------|------|
| GET /api/user/skills/my | ✅ 已实现 | 获取我的技能列表 |
| GET /api/user/skills/my?skillType=online | ✅ 已实现 | 获取线上技能列表 |
| GET /api/user/skills/my?skillType=offline | ✅ 已实现 | 获取线下技能列表 |
| PUT /api/user/skills/{skillId}/toggle | ✅ 已实现 | 切换技能上下架状态 |
| DELETE /api/user/skills/{skillId} | ✅ 已实现 | 删除技能 |
| POST /api/user/skills/online | ✅ 已实现 | 创建线上技能 |
| POST /api/user/skills/offline | ✅ 已实现 | 创建线下技能 |

---

**文档版本**: v1.0.0

**最后更新**: 2025-11-29

**测试文件**: `xypai-modules/xypai-user/src/test/java/org/dromara/user/AppSkillManagementPageTest.java`
