# XyPai-App-BFF 限时专享页面 API 对接文档

> **版本**: v1.0.0
>
> **更新日期**: 2025-11-29
>
> **服务端口**: 9400 (BFF) / 9211 (Auth) / 9201 (User)
>
> **接口前缀**: `/api/`

---

## 目录

1. [通用说明](#通用说明)
2. [页面信息](#页面信息)
3. [用户认证接口](#用户认证接口)
4. [限时专享列表接口](#限时专享列表接口)
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
xypai-user:    http://localhost:9201
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
  "code": 500,
  "msg": "服务器内部错误",
  "data": null
}
```

---

## 页面信息

| 属性 | 值 |
|------|------|
| 文档路径 | XiangYuPai-Doc/Action-API/模块化架构/03-content模块/Frontend/05-限时专享页面.md |
| 页面路由 | /home/limited-time |
| 页面名称 | 限时专享 |
| 用户角色 | 所有用户 |
| 页面类型 | 列表页面 |

### 涉及的后端服务

| 服务 | 端口 | 功能 |
|------|------|------|
| xypai-auth | 9211 | 用户认证 |
| xypai-user | 9201 | 用户技能服务 |
| xypai-app-bff | 9400 | 限时专享列表聚合 |

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

## 限时专享列表接口

### 1. 获取限时专享列表

获取限时专享用户列表，支持多种排序和筛选条件。

**请求**

```http
GET /xypai-app-bff/api/home/limited-time/list
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | integer | 否 | 1 | 页码，最小1 |
| pageSize | integer | 否 | 10 | 每页数量，1-100 |
| sortBy | string | 否 | smart_recommend | 排序方式 |
| gender | string | 否 | - | 性别筛选 |
| language | string | 否 | - | 语言筛选 |

**排序方式 (sortBy)**

| 值 | 说明 |
|------|------|
| smart_recommend | 智能推荐（默认） |
| price_asc | 价格从低到高 |
| price_desc | 价格从高到低 |
| distance_asc | 距离最近 |

**性别筛选 (gender)**

| 值 | 说明 |
|------|------|
| all | 不限 |
| male | 男 |
| female | 女 |

**语言筛选 (language)**

| 值 | 说明 |
|------|------|
| all | 不限 |
| mandarin | 普通话 |
| cantonese | 粤语 |
| english | 英语 |

**请求示例**

```http
GET /xypai-app-bff/api/home/limited-time/list?pageNum=1&pageSize=10&sortBy=price_asc&gender=female
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "list": [
      {
        "userId": 10001,
        "nickname": "小美陪玩",
        "avatar": "https://cdn.example.com/avatar/10001.jpg",
        "gender": "female",
        "age": 22,
        "tags": ["王者荣耀", "陪玩"],
        "promotionTag": "限时8折",
        "price": {
          "amount": 80,
          "unit": "金币/小时",
          "displayText": "80金币/小时",
          "originalPrice": 100,
          "discount": "8折"
        },
        "distance": 1500,
        "distanceText": "1.5km",
        "isOnline": true,
        "rating": 4.8,
        "orderCount": 128,
        "skill": {
          "skillId": 5001,
          "skillName": "王者荣耀陪玩",
          "gameName": "王者荣耀",
          "gameRank": "王者",
          "description": "专业王者荣耀陪玩，段位王者..."
        }
      }
    ],
    "total": 50,
    "pageNum": 1,
    "pageSize": 10,
    "hasMore": true,
    "filters": {
      "sortOptions": [
        {"value": "smart_recommend", "label": "智能推荐", "selected": false},
        {"value": "price_asc", "label": "价格从低到高", "selected": true},
        {"value": "price_desc", "label": "价格从高到低", "selected": false},
        {"value": "distance_asc", "label": "距离最近", "selected": false}
      ],
      "genderOptions": [
        {"value": "all", "label": "不限", "selected": false},
        {"value": "male", "label": "男", "selected": false},
        {"value": "female", "label": "女", "selected": true}
      ],
      "languageOptions": [
        {"value": "all", "label": "不限", "selected": true},
        {"value": "mandarin", "label": "普通话", "selected": false},
        {"value": "cantonese", "label": "粤语", "selected": false},
        {"value": "english", "label": "英语", "selected": false}
      ]
    }
  }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| list | array | 用户列表 |
| total | integer | 总记录数 |
| pageNum | integer | 当前页码 |
| pageSize | integer | 每页数量 |
| hasMore | boolean | 是否有更多数据 |
| filters | object | 筛选选项配置 |

**用户对象字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | long | 用户ID |
| nickname | string | 昵称 |
| avatar | string | 头像URL |
| gender | string | 性别: male/female |
| age | integer | 年龄 |
| tags | array | 标签列表 |
| promotionTag | string | 促销标签，如 "限时8折" |
| price | object | 价格信息 |
| price.amount | integer | 价格金额（金币） |
| price.unit | string | 价格单位 |
| price.displayText | string | 显示文本 |
| price.originalPrice | integer | 原价 |
| price.discount | string | 折扣信息 |
| distance | integer | 距离（米） |
| distanceText | string | 距离显示文本 |
| isOnline | boolean | 是否在线 |
| rating | decimal | 评分 |
| orderCount | integer | 接单数 |
| skill | object | 技能信息 |

---

## 筛选参数说明

### 排序选项

系统支持以下4种排序方式：

| 排序 | 说明 | 适用场景 |
|------|------|------|
| smart_recommend | 智能推荐 | 综合考虑用户偏好、热度、匹配度 |
| price_asc | 价格升序 | 寻找性价比高的服务 |
| price_desc | 价格降序 | 寻找高端服务 |
| distance_asc | 距离升序 | 寻找附近的服务提供者 |

### 性别筛选

| 选项 | 说明 |
|------|------|
| all | 显示所有性别 |
| male | 仅显示男性 |
| female | 仅显示女性 |

### 语言筛选

| 选项 | 说明 |
|------|------|
| all | 不限制语言 |
| mandarin | 普通话 |
| cantonese | 粤语 |
| english | 英语 |

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

---

## 集成测试用例

### 测试环境配置

```
Gateway:       http://localhost:8080
xypai-auth:    http://localhost:9211 (认证服务)
xypai-user:    http://localhost:9201 (用户技能服务)
xypai-app-bff: http://localhost:9400 (BFF聚合服务)
```

**依赖服务**: Nacos, Redis, MySQL

---

### 测试场景: 限时专享页面 (Page05_LimitedTimeTest)

测试限时专享页面的所有功能，包括列表查询、排序、筛选、分页等。

#### 测试数据初始化

测试开始前，通过 API 创建多个用户和技能数据：

```java
// 创建6个测试用户，每个用户有不同的性别和技能
String[] genders = {"male", "female", "male", "female", "male", "female"};
String[] games = {"王者荣耀", "英雄联盟", "和平精英", "原神", "永劫无间", "CSGO"};
String[] ranks = {"王者", "钻石", "大师", "铂金", "黄金", "青铜"};
int[] prices = {50, 80, 60, 100, 70, 30};

// 1. 为每个用户登录获取Token
// 2. 更新用户性别
// 3. 创建上架的技能
```

#### 测试1: 获取限时专享列表（默认参数）

```java
// 接口: GET /xypai-app-bff/api/home/limited-time/list
// 请求头: Authorization: Bearer {token}
String url = GATEWAY_URL + "/xypai-app-bff/api/home/limited-time/list?pageNum=1&pageSize=10";

// 响应
{
  "code": 200,
  "data": {
    "list": [...],
    "total": 6,
    "hasMore": false,
    "filters": {
      "sortOptions": [...],    // 4个排序选项
      "genderOptions": [...],  // 3个性别选项
      "languageOptions": [...]  // 4个语言选项
    }
  }
}

// 断言
- list 不为空
- sortOptions 数量为 4
- genderOptions 数量为 3
- languageOptions 数量为 4
```

#### 测试2: 价格从低到高排序

```java
// 接口: GET /xypai-app-bff/api/home/limited-time/list?sortBy=price_asc
String url = GATEWAY_URL + "/xypai-app-bff/api/home/limited-time/list?pageNum=1&pageSize=5&sortBy=price_asc";

// 响应
{
  "code": 200,
  "data": {
    "list": [
      {"nickname": "用户6", "price": {"amount": 30}},  // 最低
      {"nickname": "用户1", "price": {"amount": 50}},
      {"nickname": "用户3", "price": {"amount": 60}},
      ...
    ]
  }
}

// 断言
- 价格按升序排列
- 后一个用户价格 >= 前一个用户价格
```

#### 测试3: 价格从高到低排序

```java
// 接口: GET /xypai-app-bff/api/home/limited-time/list?sortBy=price_desc
String url = GATEWAY_URL + "/xypai-app-bff/api/home/limited-time/list?pageNum=1&pageSize=5&sortBy=price_desc";

// 响应
{
  "code": 200,
  "data": {
    "list": [
      {"nickname": "用户4", "price": {"amount": 100}},  // 最高
      {"nickname": "用户2", "price": {"amount": 80}},
      {"nickname": "用户5", "price": {"amount": 70}},
      ...
    ]
  }
}

// 断言
- 价格按降序排列
- 后一个用户价格 <= 前一个用户价格
```

#### 测试4: 距离最近排序

```java
// 接口: GET /xypai-app-bff/api/home/limited-time/list?sortBy=distance_asc
String url = GATEWAY_URL + "/xypai-app-bff/api/home/limited-time/list?pageNum=1&pageSize=5&sortBy=distance_asc";

// 响应
{
  "code": 200,
  "data": {
    "list": [
      {"nickname": "用户A", "distance": 500, "distanceText": "500m"},
      {"nickname": "用户B", "distance": 1000, "distanceText": "1km"},
      {"nickname": "用户C", "distance": 1500, "distanceText": "1.5km"},
      ...
    ]
  }
}

// 断言
- 距离按升序排列
- 后一个用户距离 >= 前一个用户距离
```

#### 测试5: 性别筛选-男

```java
// 接口: GET /xypai-app-bff/api/home/limited-time/list?gender=male
String url = GATEWAY_URL + "/xypai-app-bff/api/home/limited-time/list?pageNum=1&pageSize=10&gender=male";

// 响应
{
  "code": 200,
  "data": {
    "list": [
      {"nickname": "用户1", "gender": "male"},
      {"nickname": "用户3", "gender": "male"},
      {"nickname": "用户5", "gender": "male"}
    ]
  }
}

// 断言
- 所有用户的 gender 字段都是 "male"
```

#### 测试6: 性别筛选-女

```java
// 接口: GET /xypai-app-bff/api/home/limited-time/list?gender=female
String url = GATEWAY_URL + "/xypai-app-bff/api/home/limited-time/list?pageNum=1&pageSize=10&gender=female";

// 响应
{
  "code": 200,
  "data": {
    "list": [
      {"nickname": "用户2", "gender": "female"},
      {"nickname": "用户4", "gender": "female"},
      {"nickname": "用户6", "gender": "female"}
    ]
  }
}

// 断言
- 所有用户的 gender 字段都是 "female"
```

#### 测试7: 组合筛选（女性+价格从低到高）

```java
// 接口: GET /xypai-app-bff/api/home/limited-time/list?gender=female&sortBy=price_asc
String url = GATEWAY_URL + "/xypai-app-bff/api/home/limited-time/list?pageNum=1&pageSize=5&gender=female&sortBy=price_asc";

// 响应
{
  "code": 200,
  "data": {
    "list": [
      {"nickname": "用户6", "gender": "female", "price": {"amount": 30}},
      {"nickname": "用户2", "gender": "female", "price": {"amount": 80}},
      {"nickname": "用户4", "gender": "female", "price": {"amount": 100}}
    ]
  }
}

// 断言
- 所有用户都是女性
- 价格按升序排列
```

#### 测试8: 分页功能

```java
// 第一页
String url1 = GATEWAY_URL + "/xypai-app-bff/api/home/limited-time/list?pageNum=1&pageSize=5";
// 响应
{
  "code": 200,
  "data": {
    "list": [...],  // 5条数据
    "hasMore": true
  }
}

// 第二页
String url2 = GATEWAY_URL + "/xypai-app-bff/api/home/limited-time/list?pageNum=2&pageSize=5";
// 响应
{
  "code": 200,
  "data": {
    "list": [...],  // 剩余数据
    "hasMore": false
  }
}

// 断言
- 第一页 hasMore 为 true（假设总数据超过5条）
- 第二页数据与第一页不重复
- 两页的第一个用户ID不相同
```

---

### 运行测试

```bash
# 进入聚合服务目录
cd xypai-aggregation/xypai-app-bff

# 运行限时专享页面测试
mvn test -Dtest=Page05_LimitedTimeTest

# 运行所有页面测试
mvn test -Dtest=Page*Test
```

**测试前置条件**:
1. 确保 Nacos、Redis、MySQL 已启动
2. 确保 xypai-auth (9211) 服务已启动
3. 确保 xypai-user (9201) 服务已启动
4. 确保 xypai-app-bff (9400) 服务已启动
5. 确保 Gateway (8080) 已启动

---

### 测试流程图

```
┌─────────────────────────────────────────────────────────────┐
│                    限时专享页面测试流程                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  【测试数据初始化】                                          │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ 循环6次:                                               │ │
│  │   1. SMS登录 → 获取Token                              │ │
│  │   2. 更新用户性别 (male/female交替)                    │ │
│  │   3. 创建上架技能 (不同游戏、段位、价格)               │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
│  【测试用例执行】                                            │
│                                                             │
│  1. 默认参数查询                                            │
│     GET /api/home/limited-time/list                        │
│     └── 验证: 列表数据、筛选选项配置                        │
│                                                             │
│  2. 价格排序测试                                            │
│     ├── sortBy=price_asc  → 验证升序                       │
│     └── sortBy=price_desc → 验证降序                       │
│                                                             │
│  3. 距离排序测试                                            │
│     └── sortBy=distance_asc → 验证升序                     │
│                                                             │
│  4. 性别筛选测试                                            │
│     ├── gender=male   → 验证全部为男                       │
│     └── gender=female → 验证全部为女                       │
│                                                             │
│  5. 组合筛选测试                                            │
│     └── gender=female&sortBy=price_asc                     │
│         → 验证女性 + 价格升序                              │
│                                                             │
│  6. 分页测试                                                │
│     ├── pageNum=1 → 验证 hasMore                          │
│     └── pageNum=2 → 验证数据不重复                        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

### 辅助接口（测试数据初始化用）

#### 更新用户性别

```java
// 接口: PUT /xypai-user/api/user/profile
// 请求头: Authorization: Bearer {token}
Map<String, Object> updateRequest = new HashMap<>();
updateRequest.put("gender", "male");  // 或 "female"

// 响应
{
  "code": 200,
  "msg": "更新成功"
}
```

#### 创建线上技能

```java
// 接口: POST /xypai-user/api/user/skills/online
// 请求头: Authorization: Bearer {token}
Map<String, Object> skillRequest = new HashMap<>();
skillRequest.put("gameName", "王者荣耀");
skillRequest.put("gameRank", "王者");
skillRequest.put("skillName", "王者荣耀陪玩");
skillRequest.put("description", "专业王者荣耀陪玩，段位王者，有丰富的游戏经验...");
skillRequest.put("price", 50);
skillRequest.put("serviceHours", 1);
skillRequest.put("isOnline", true);  // 直接上架

// 响应
{
  "code": 200,
  "msg": "创建成功",
  "data": 5001  // skillId
}
```

---

**文档版本**: v1.0.0

**最后更新**: 2025-11-29

**测试文件**: `xypai-aggregation/xypai-app-bff/src/test/java/org/dromara/appbff/pages/Page05_LimitedTimeTest.java`
