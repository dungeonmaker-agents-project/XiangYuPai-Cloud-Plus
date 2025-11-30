# XyPai-User 编辑资料页面 API 对接文档

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
4. [资料编辑接口](#资料编辑接口)
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
| 页面路由 | /profile/edit |
| 页面名称 | 编辑资料 |
| 用户角色 | 登录用户 |
| 页面类型 | 表单编辑页面 |

### 涉及的后端服务

| 服务 | 端口 | 功能 |
|------|------|------|
| xypai-auth | 9211 | 用户认证 |
| xypai-user | 9401 | 资料编辑 |

### 功能说明

本页面支持11个字段的实时保存：
- 头像上传
- 昵称、性别、生日
- 居住地、身高、体重
- 职业、微信号、个性签名

> **注意**: 头像上传接口需要 `multipart/form-data` 格式，需单独测试。

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

## 资料编辑接口

### 1. 加载编辑页面数据

获取用户当前资料信息，用于编辑页面初始化。

**请求**

```http
GET /xypai-user/api/user/profile/edit
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
    "residence": "广东省广州市天河区",
    "height": 175,
    "weight": 65,
    "occupation": "软件工程师",
    "wechat": "wechat_10001",
    "bio": "这是我的个性签名"
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
| birthday | string | 生日 (yyyy-MM-dd) |
| residence | string | 居住地 |
| height | integer | 身高 (cm) |
| weight | integer | 体重 (kg) |
| occupation | string | 职业 |
| wechat | string | 微信号 |
| bio | string | 个性签名 |

---

### 2. 更新昵称

**请求**

```http
PUT /xypai-user/api/user/profile/nickname
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "nickname": "新昵称"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nickname | string | 是 | 昵称，1-20个字符 |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

**触发时机**: 昵称输入框失去焦点

---

### 3. 更新性别

**请求**

```http
PUT /xypai-user/api/user/profile/gender
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "gender": "male"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| gender | string | 是 | 性别: male/female |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

**触发时机**: 选择性别后

---

### 4. 更新生日

**请求**

```http
PUT /xypai-user/api/user/profile/birthday
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "birthday": "1995-06-15"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| birthday | string | 是 | 生日，格式: yyyy-MM-dd |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

**触发时机**: 选择生日后

---

### 5. 更新居住地

**请求**

```http
PUT /xypai-user/api/user/profile/residence
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "residence": "广东省广州市天河区"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| residence | string | 是 | 居住地 |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

**触发时机**: 选择居住地后

---

### 6. 更新身高

**请求**

```http
PUT /xypai-user/api/user/profile/height
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "height": 175
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| height | integer | 是 | 身高，单位: cm，范围: 100-250 |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

**触发时机**: 身高输入框失去焦点

---

### 7. 更新体重

**请求**

```http
PUT /xypai-user/api/user/profile/weight
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "weight": 65
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| weight | integer | 是 | 体重，单位: kg，范围: 30-200 |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

**触发时机**: 体重输入框失去焦点

---

### 8. 更新职业

**请求**

```http
PUT /xypai-user/api/user/profile/occupation
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "occupation": "软件工程师"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| occupation | string | 是 | 职业 |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

**触发时机**: 职业输入框失去焦点

---

### 9. 更新微信号

**请求**

```http
PUT /xypai-user/api/user/profile/wechat
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "wechat": "wechat_10001"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| wechat | string | 是 | 微信号 |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

**触发时机**: 微信号输入框失去焦点

---

### 10. 更新个性签名

**请求**

```http
PUT /xypai-user/api/user/profile/bio
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "bio": "这是我的个性签名"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| bio | string | 是 | 个性签名，最大200个字符 |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

**触发时机**: 个性签名输入框失去焦点

---

### 11. 上传头像

**请求**

```http
POST /xypai-user/api/user/profile/avatar/upload
Content-Type: multipart/form-data
Authorization: Bearer <token>
```

**请求体**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 图片文件，支持jpg/png/gif，最大5MB |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "avatarUrl": "https://cdn.example.com/avatar/10001_new.jpg"
  }
}
```

> **注意**: 此接口需要 multipart/form-data 格式，需单独测试。

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权，请先登录 |
| 500 | 服务器内部错误 |

### 常见业务错误

| 错误信息 | 说明 |
|----------|------|
| 昵称不能为空 | nickname 参数缺失 |
| 昵称长度超限 | 昵称超过20个字符 |
| 性别参数错误 | gender 不是 male/female |
| 生日格式错误 | birthday 格式不是 yyyy-MM-dd |

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

### 测试场景: 编辑资料页面 (AppEditProfilePageTest)

测试编辑资料页面的所有字段更新功能。

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

#### 测试2: 加载编辑页面数据

```java
// 接口: GET /xypai-user/api/user/profile/edit
// 请求头: Authorization: Bearer {token}

// 断言
- userId != null
- nickname != null
```

#### 测试3-11: 更新各字段

| 测试 | 接口 | 字段 | 示例值 |
|------|------|------|--------|
| 测试3 | PUT /profile/nickname | nickname | 测试昵称_1234 |
| 测试4 | PUT /profile/gender | gender | male |
| 测试5 | PUT /profile/birthday | birthday | 1995-06-15 |
| 测试6 | PUT /profile/residence | residence | 广东省广州市天河区 |
| 测试7 | PUT /profile/height | height | 175 |
| 测试8 | PUT /profile/weight | weight | 65 |
| 测试9 | PUT /profile/occupation | occupation | 软件工程师 |
| 测试10 | PUT /profile/wechat | wechat | wechat_1234 |
| 测试11 | PUT /profile/bio | bio | 这是我的个性签名 |

---

### 运行测试

```bash
# 进入用户服务目录
cd xypai-modules/xypai-user

# 运行编辑资料页面测试
mvn test -Dtest=AppEditProfilePageTest

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
│                    编辑资料页面测试流程                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. 用户登录                                                 │
│     POST /xypai-auth/api/auth/login/sms                     │
│     └── 获取 Token                                          │
│                                                             │
│  2. 加载编辑页面数据                                         │
│     GET /xypai-user/api/user/profile/edit                   │
│     └── 获取当前资料                                         │
│                                                             │
│  3. 逐个更新字段                                             │
│     ├── PUT /profile/nickname   → 更新昵称                  │
│     ├── PUT /profile/gender     → 更新性别                  │
│     ├── PUT /profile/birthday   → 更新生日                  │
│     ├── PUT /profile/residence  → 更新居住地                │
│     ├── PUT /profile/height     → 更新身高                  │
│     ├── PUT /profile/weight     → 更新体重                  │
│     ├── PUT /profile/occupation → 更新职业                  │
│     ├── PUT /profile/wechat     → 更新微信号                │
│     └── PUT /profile/bio        → 更新个性签名              │
│                                                             │
│  💡 注意：                                                   │
│  - 每个字段独立保存，实时生效                                │
│  - 头像上传需要 multipart/form-data 格式                    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

### 接口实现状态

| 接口 | 状态 | 说明 |
|------|------|------|
| GET /api/user/profile/edit | ✅ 已实现 | 加载编辑页面数据 |
| PUT /api/user/profile/nickname | ✅ 已实现 | 更新昵称 |
| PUT /api/user/profile/gender | ✅ 已实现 | 更新性别 |
| PUT /api/user/profile/birthday | ✅ 已实现 | 更新生日 |
| PUT /api/user/profile/residence | ✅ 已实现 | 更新居住地 |
| PUT /api/user/profile/height | ✅ 已实现 | 更新身高 |
| PUT /api/user/profile/weight | ✅ 已实现 | 更新体重 |
| PUT /api/user/profile/occupation | ✅ 已实现 | 更新职业 |
| PUT /api/user/profile/wechat | ✅ 已实现 | 更新微信号 |
| PUT /api/user/profile/bio | ✅ 已实现 | 更新个性签名 |
| POST /api/user/profile/avatar/upload | ⏳ 待测试 | 上传头像（需multipart） |

---

**文档版本**: v1.0.0

**最后更新**: 2025-11-29

**测试文件**: `xypai-modules/xypai-user/src/test/java/org/dromara/user/AppEditProfilePageTest.java`
