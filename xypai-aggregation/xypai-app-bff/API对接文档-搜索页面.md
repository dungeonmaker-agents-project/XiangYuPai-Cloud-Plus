# XyPai-App-BFF 搜索页面 API 对接文档

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
4. [搜索初始化接口](#搜索初始化接口)
5. [搜索建议接口](#搜索建议接口)
6. [搜索历史接口](#搜索历史接口)
7. [错误码说明](#错误码说明)
8. [集成测试用例](#集成测试用例)

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
| 文档路径 | XiangYuPai-Doc/Action-API/模块化架构/03-content模块/Frontend/06-搜索页面.md |
| 页面路由 | /search |
| 页面名称 | 搜索 |
| 用户角色 | 所有用户 |
| 页面类型 | 搜索输入页面 |

### 涉及的后端服务

| 服务 | 端口 | 功能 |
|------|------|------|
| xypai-auth | 9211 | 用户认证 |
| xypai-app-bff | 9400 | 搜索功能 |

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

## 搜索初始化接口

### 1. 获取搜索初始数据

进入搜索页面时调用，获取搜索历史和热门搜索关键词。

**请求**

```http
GET /xypai-app-bff/api/search/init
Authorization: Bearer <token>
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "searchHistory": [
      {
        "keyword": "王者荣耀",
        "searchTime": "2025-11-29 10:30:00"
      },
      {
        "keyword": "台球",
        "searchTime": "2025-11-29 09:15:00"
      }
    ],
    "hotKeywords": [
      {
        "keyword": "王者荣耀陪玩",
        "rank": 1,
        "isHot": true
      },
      {
        "keyword": "英雄联盟",
        "rank": 2,
        "isHot": true
      },
      {
        "keyword": "台球约局",
        "rank": 3,
        "isHot": false
      },
      {
        "keyword": "唱歌陪练",
        "rank": 4,
        "isHot": false
      },
      {
        "keyword": "桌游约局",
        "rank": 5,
        "isHot": false
      }
    ],
    "placeholder": "搜索更多"
  }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| searchHistory | array | 用户搜索历史列表 |
| searchHistory[].keyword | string | 搜索关键词 |
| searchHistory[].searchTime | string | 搜索时间 |
| hotKeywords | array | 热门搜索关键词列表 |
| hotKeywords[].keyword | string | 关键词 |
| hotKeywords[].rank | integer | 排名（1开始） |
| hotKeywords[].isHot | boolean | 是否为热门（显示🔥图标） |
| placeholder | string | 搜索框占位符文本 |

---

## 搜索建议接口

### 1. 获取搜索建议

用户输入关键词时实时获取搜索建议。

**请求**

```http
GET /xypai-app-bff/api/search/suggest
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| keyword | string | 是 | - | 搜索关键词 |
| limit | integer | 否 | 10 | 返回建议数量上限 |

**请求示例**

```http
GET /xypai-app-bff/api/search/suggest?keyword=王者&limit=10
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "suggestions": [
      {
        "text": "王者荣耀",
        "type": "game",
        "icon": "🎮"
      },
      {
        "text": "王者荣耀陪玩",
        "type": "skill",
        "icon": "🎯"
      },
      {
        "text": "王者荣耀大神",
        "type": "user",
        "icon": "👤"
      },
      {
        "text": "王者荣耀代练",
        "type": "service",
        "icon": "💼"
      }
    ]
  }
}
```

**响应字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| suggestions | array | 搜索建议列表 |
| suggestions[].text | string | 建议文本 |
| suggestions[].type | string | 建议类型: game/skill/user/service/activity |
| suggestions[].icon | string | 图标（emoji或图片URL） |

**建议类型说明**

| 类型 | 图标 | 说明 |
|------|------|------|
| game | 🎮 | 游戏相关 |
| skill | 🎯 | 技能服务 |
| user | 👤 | 用户账号 |
| service | 💼 | 服务类型 |
| activity | 🎉 | 活动相关 |

---

## 搜索历史接口

### 1. 删除搜索历史

删除单条搜索历史或清空所有历史。

**请求**

```http
DELETE /xypai-app-bff/api/search/history
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体（删除单条）**

```json
{
  "keyword": "王者荣耀",
  "clearAll": false
}
```

**请求体（清空所有）**

```json
{
  "clearAll": true
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 否 | 要删除的关键词（clearAll=false时必填） |
| clearAll | boolean | 是 | 是否清空所有历史 |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "success": true,
    "message": "删除成功"
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

### 测试场景: 搜索页面 (Page06_SearchTest)

测试搜索页面的所有功能，包括搜索初始化、搜索建议、搜索历史管理等。

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

#### 测试2: 获取搜索初始数据（首次访问）

```java
// 接口: GET /xypai-app-bff/api/search/init
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "data": {
    "searchHistory": [],  // 新用户无历史
    "hotKeywords": [
      {"keyword": "王者荣耀陪玩", "rank": 1, "isHot": true},
      {"keyword": "英雄联盟", "rank": 2, "isHot": true},
      ...
    ],
    "placeholder": "搜索更多"
  }
}

// 断言
- searchHistory 不为null
- hotKeywords.size() > 0
- placeholder == "搜索更多"
```

#### 测试3: 获取搜索建议（王者荣耀）

```java
// 接口: GET /xypai-app-bff/api/search/suggest?keyword=王者&limit=10
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "data": {
    "suggestions": [
      {"text": "王者荣耀", "type": "game", "icon": "🎮"},
      {"text": "王者荣耀陪玩", "type": "skill", "icon": "🎯"},
      ...
    ]
  }
}

// 断言
- suggestions.size() > 0
- 至少有一条建议包含"王者"关键词
```

#### 测试4: 获取搜索建议（台球）

```java
// 接口: GET /xypai-app-bff/api/search/suggest?keyword=台球&limit=5
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "data": {
    "suggestions": [
      {"text": "台球约局", "type": "activity", "icon": "🎉"},
      {"text": "台球陪玩", "type": "skill", "icon": "🎯"},
      ...
    ]
  }
}

// 断言
- suggestions.size() > 0
```

#### 测试5: 获取搜索建议（用户搜索）

```java
// 接口: GET /xypai-app-bff/api/search/suggest?keyword=王者荣耀112&limit=10
// 请求头: Authorization: Bearer {token}
// 说明: 带数字的关键词可能触发用户搜索建议

// 响应
{
  "code": 200,
  "data": {
    "suggestions": [
      {"text": "王者荣耀112", "type": "user", "icon": "👤"},
      ...
    ]
  }
}

// 断言
- suggestions.size() > 0
- 可能包含type="user"的建议
```

#### 测试6: 删除单条搜索历史

```java
// 接口: DELETE /xypai-app-bff/api/search/history
// 请求头: Authorization: Bearer {token}
Map<String, Object> deleteRequest = new HashMap<>();
deleteRequest.put("keyword", "王者荣耀");
deleteRequest.put("clearAll", false);

// 响应
{
  "code": 200,
  "data": {
    "success": true,
    "message": "删除成功"
  }
}

// 断言
- success == true
```

#### 测试7: 清空所有搜索历史

```java
// 接口: DELETE /xypai-app-bff/api/search/history
// 请求头: Authorization: Bearer {token}
Map<String, Object> deleteRequest = new HashMap<>();
deleteRequest.put("clearAll", true);

// 响应
{
  "code": 200,
  "data": {
    "success": true,
    "message": "全部清空成功"
  }
}

// 断言
- success == true
```

#### 测试8: 验证清空后历史记录为空

```java
// 接口: GET /xypai-app-bff/api/search/init
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "data": {
    "searchHistory": [],  // 已清空
    "hotKeywords": [...],  // 热门搜索不受影响
    "placeholder": "搜索更多"
  }
}

// 断言
- searchHistory.size() == 0
- hotKeywords.size() > 0
```

#### 测试9: 验证热门搜索数据完整性

```java
// 接口: GET /xypai-app-bff/api/search/init

// 响应数据验证
{
  "code": 200,
  "data": {
    "hotKeywords": [
      {"keyword": "王者荣耀陪玩", "rank": 1, "isHot": true},
      {"keyword": "英雄联盟", "rank": 2, "isHot": true},
      {"keyword": "台球约局", "rank": 3, "isHot": false},
      {"keyword": "唱歌陪练", "rank": 4, "isHot": false},
      {"keyword": "桌游约局", "rank": 5, "isHot": false}
    ]
  }
}

// 断言
- hotKeywords.size() >= 5
- 每个热门搜索都有keyword、rank、isHot字段
```

#### 测试10: 测试搜索建议类型多样性

```java
// 测试多个关键词的搜索建议
String[] testKeywords = {"王者", "台球", "游戏112"};

for (String keyword : testKeywords) {
    // 接口: GET /xypai-app-bff/api/search/suggest?keyword={keyword}&limit=5

    // 统计返回的建议类型分布
    // type可能包含: game, skill, user, service, activity
}

// 断言
- 每个关键词都应返回suggestions.size() > 0
```

---

### 运行测试

```bash
# 进入聚合服务目录
cd xypai-aggregation/xypai-app-bff

# 运行搜索页面测试
mvn test -Dtest=Page06_SearchTest

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
│                    搜索页面测试流程                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. 用户登录                                                 │
│     POST /xypai-auth/api/auth/login/sms                     │
│     └── 获取 Token                                          │
│                                                             │
│  2. 获取搜索初始数据                                         │
│     GET /xypai-app-bff/api/search/init                      │
│     ├── 搜索历史列表                                         │
│     ├── 热门搜索关键词                                       │
│     └── 搜索框占位符                                         │
│                                                             │
│  3. 获取搜索建议                                             │
│     GET /xypai-app-bff/api/search/suggest                   │
│     ├── 游戏关键词（王者荣耀）                               │
│     ├── 活动关键词（台球）                                   │
│     └── 用户关键词（带数字）                                 │
│                                                             │
│  4. 管理搜索历史                                             │
│     DELETE /xypai-app-bff/api/search/history                │
│     ├── 删除单条历史                                         │
│     └── 清空所有历史                                         │
│                                                             │
│  5. 验证数据                                                 │
│     ├── 验证历史已清空                                       │
│     ├── 验证热门搜索完整性                                   │
│     └── 验证建议类型多样性                                   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

**文档版本**: v1.0.0

**最后更新**: 2025-11-29

**测试文件**: `xypai-aggregation/xypai-app-bff/src/test/java/org/dromara/appbff/pages/Page06_SearchTest.java`
