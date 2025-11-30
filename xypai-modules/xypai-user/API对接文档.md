# XyPai-User 用户服务 API 对接文档

> **版本**: v1.0.0
>
> **更新日期**: 2025-11-28
>
> **服务端口**: 9401
>
> **接口前缀**: `/api/user/`

---

## 📋 目录

1. [通用说明](#通用说明)
2. [用户资料接口](#用户资料接口)
3. [社交关系接口](#社交关系接口)
4. [技能管理接口](#技能管理接口)
5. [错误码说明](#错误码说明)
6. [集成测试用例](#集成测试用例)

---

## 通用说明

### 基础URL

```
# 开发环境（直连服务）
http://localhost:9401/api/user/

# 生产环境（通过网关）
http://gateway:8080/xypai-user/api/user/
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

技能列表等接口使用 TableDataInfo 格式：

```json
{
  "total": 100,
  "rows": [...],
  "code": 200,
  "msg": "查询成功"
}
```

---

## 用户资料接口

### 1. 获取编辑资料数据

获取当前用户的完整资料，用于编辑页面加载。

**请求**

```http
GET /api/user/profile/edit
Authorization: Bearer <token>
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "userId": 10001,
    "nickname": "小美探店",
    "avatar": "https://cdn.example.com/avatar/10001.jpg",
    "gender": "female",
    "birthday": "1995-06-15",
    "residence": "广东省广州市天河区",
    "height": 165,
    "weight": 50,
    "occupation": "设计师",
    "wechat": "xiaomei666",
    "bio": "热爱生活，分享美好",
    "isOnline": true,
    "stats": {
      "followingCount": 128,
      "fansCount": 1024,
      "likesCount": 5000,
      "momentsCount": 50,
      "postsCount": 30,
      "collectionsCount": 100,
      "skillsCount": 3,
      "ordersCount": 20
    },
    "followStatus": "none",
    "privacy": {
      "showAge": true,
      "showHeight": true,
      "showWeight": false
    },
    "canViewProfile": true,
    "canViewMoments": true,
    "canViewSkills": true
  }
}
```

---

### 2. 获取个人资料头部

获取个人主页头部展示的资料。

**请求**

```http
GET /api/user/profile/header
Authorization: Bearer <token>
```

**响应**: 同上

---

### 3. 获取他人资料

查看其他用户的资料页面。

**请求**

```http
GET /api/user/profile/other/{targetUserId}
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetUserId | long | 是 | 目标用户ID |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "userId": 10002,
    "nickname": "游戏达人",
    "avatar": "https://cdn.example.com/avatar/10002.jpg",
    "gender": "male",
    "bio": "王者荣耀大神",
    "isOnline": true,
    "stats": {
      "followingCount": 50,
      "fansCount": 2000,
      "skillsCount": 5
    },
    "followStatus": "following",
    "canViewProfile": true,
    "canViewMoments": true,
    "canViewSkills": true
  }
}
```

---

### 4. 更新昵称

**请求**

```http
PUT /api/user/profile/nickname
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
| nickname | string | 是 | 昵称，2-20字符 |

**响应示例**

```json
{
  "code": 200,
  "msg": "更新成功",
  "data": null
}
```

---

### 5. 更新性别

**请求**

```http
PUT /api/user/profile/gender
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
| gender | string | 是 | 性别: `male`, `female`, `other` |

**响应示例**

```json
{
  "code": 200,
  "msg": "更新成功",
  "data": null
}
```

---

### 6. 更新生日

**请求**

```http
PUT /api/user/profile/birthday
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
| birthday | string | 是 | 生日，格式: YYYY-MM-DD |

**响应示例**

```json
{
  "code": 200,
  "msg": "更新成功",
  "data": null
}
```

---

### 7. 更新居住地

**请求**

```http
PUT /api/user/profile/residence
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "residence": "广东省广州市天河区"
}
```

**响应示例**

```json
{
  "code": 200,
  "msg": "更新成功",
  "data": null
}
```

---

### 8. 更新身高

**请求**

```http
PUT /api/user/profile/height
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
| height | integer | 是 | 身高，单位cm，范围100-250 |

**响应示例**

```json
{
  "code": 200,
  "msg": "更新成功",
  "data": null
}
```

---

### 9. 更新体重

**请求**

```http
PUT /api/user/profile/weight
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
| weight | integer | 是 | 体重，单位kg，范围30-200 |

---

### 10. 更新职业

**请求**

```http
PUT /api/user/profile/occupation
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "occupation": "软件工程师"
}
```

---

### 11. 更新微信号

**请求**

```http
PUT /api/user/profile/wechat
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "wechat": "wechat_id_123"
}
```

---

### 12. 更新个性签名

**请求**

```http
PUT /api/user/profile/bio
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "bio": "热爱生活，分享美好"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| bio | string | 是 | 个性签名，最多200字符 |

---

### 13. 上传头像

**请求**

```http
POST /api/user/profile/avatar/upload
Content-Type: multipart/form-data
Authorization: Bearer <token>
```

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| avatar | file | 是 | 头像图片文件，支持jpg/png，最大5MB |

**响应示例**

```json
{
  "code": 200,
  "msg": "上传成功",
  "data": "https://cdn.example.com/avatar/10001_new.jpg"
}
```

---

### 14. 获取我的动态列表

**请求**

```http
GET /api/user/profile/posts?page=1&pageSize=20
Authorization: Bearer <token>
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "list": [
      {
        "postId": 1001,
        "content": "今天天气真好",
        "images": ["https://cdn.example.com/post/1.jpg"],
        "likeCount": 128,
        "commentCount": 32,
        "createdAt": "2025-11-27 10:30:00"
      }
    ],
    "total": 50,
    "page": 1,
    "pageSize": 20
  }
}
```

---

### 15. 获取我的收藏列表

**请求**

```http
GET /api/user/profile/favorites?page=1&pageSize=20
Authorization: Bearer <token>
```

---

### 16. 获取我的点赞列表

**请求**

```http
GET /api/user/profile/likes?page=1&pageSize=20
Authorization: Bearer <token>
```

---

## 社交关系接口

### 1. 关注用户

**请求**

```http
POST /api/user/relation/follow/{followingId}
Authorization: Bearer <token>
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| followingId | long | 是 | 要关注的用户ID |

**响应示例**

```json
{
  "code": 200,
  "msg": "关注成功",
  "data": null
}
```

---

### 2. 取消关注

**请求**

```http
DELETE /api/user/relation/follow/{followingId}
Authorization: Bearer <token>
```

**响应示例**

```json
{
  "code": 200,
  "msg": "已取消关注",
  "data": null
}
```

---

### 3. 获取粉丝列表

**请求**

```http
GET /api/user/relation/fans?pageNum=1&pageSize=20&keyword=
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 20 | 每页数量 |
| keyword | string | 否 | - | 搜索关键词（昵称） |

**响应示例**

```json
{
  "total": 1024,
  "rows": [
    {
      "userId": 10002,
      "nickname": "游戏达人",
      "avatar": "https://cdn.example.com/avatar/10002.jpg",
      "gender": "male",
      "bio": "王者荣耀大神",
      "isOnline": true,
      "followStatus": "mutual",
      "fansCount": 2000,
      "isFollowing": true,
      "isMutualFollow": true
    }
  ],
  "code": 200,
  "msg": "查询成功"
}
```

---

### 4. 获取关注列表

**请求**

```http
GET /api/user/relation/following?pageNum=1&pageSize=20&keyword=
Authorization: Bearer <token>
```

**响应格式**: 同粉丝列表

---

### 5. 拉黑用户

**请求**

```http
POST /api/user/relation/block/{blockedUserId}
Authorization: Bearer <token>
```

**响应示例**

```json
{
  "code": 200,
  "msg": "已拉黑",
  "data": null
}
```

---

### 6. 取消拉黑

**请求**

```http
DELETE /api/user/relation/block/{blockedUserId}
Authorization: Bearer <token>
```

---

### 7. 举报用户

**请求**

```http
POST /api/user/relation/report/{reportedUserId}
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "reason": "spam",
  "description": "发布垃圾广告信息",
  "reportImages": [
    "https://cdn.example.com/evidence/1.jpg"
  ]
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reason | string | 是 | 举报原因: `spam`, `abuse`, `inappropriate`, `fraud`, `other` |
| description | string | 否 | 详细描述，最多500字符 |
| reportImages | array | 否 | 证据图片URL列表 |

**响应示例**

```json
{
  "code": 200,
  "msg": "举报已提交",
  "data": null
}
```

---

## 技能管理接口

### 1. 创建线上技能

创建游戏陪玩类技能。

**请求**

```http
POST /api/user/skills/online
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "gameId": "wzry",
  "gameName": "王者荣耀",
  "gameRank": "王者",
  "skillName": "王者荣耀陪玩",
  "description": "王者50星巅峰赛选手，可带上分或娱乐陪玩，声音好听，性格温柔。",
  "price": 30.00,
  "serviceHours": 1,
  "coverImage": "https://cdn.example.com/skill/wzry.jpg",
  "images": [
    "https://cdn.example.com/skill/wzry_1.jpg",
    "https://cdn.example.com/skill/wzry_2.jpg"
  ],
  "promises": [
    "不骂人",
    "准时上线",
    "包上分"
  ],
  "isOnline": true
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| gameId | string | 否 | 游戏ID |
| gameName | string | 是 | 游戏名称 |
| gameRank | string | 是 | 游戏段位 |
| skillName | string | 是 | 技能名称，2-50字符 |
| description | string | 是 | 技能介绍，10-500字符 |
| price | decimal | 是 | 价格，必须>0 |
| serviceHours | decimal | 是 | 每局/每次服务时长（小时） |
| coverImage | string | 否 | 封面图URL |
| images | array | 否 | 技能图片URL列表 |
| promises | array | 否 | 服务承诺列表 |
| isOnline | boolean | 否 | 是否上架，默认false |

**响应示例**

```json
{
  "code": 200,
  "msg": "创建成功",
  "data": 5001
}
```

---

### 2. 创建线下技能

创建本地服务类技能。

**请求**

```http
POST /api/user/skills/offline
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "serviceType": "photography",
  "serviceTypeName": "摄影服务",
  "skillName": "专业人像摄影",
  "description": "专业摄影师，擅长人像、写真、商业摄影。提供修图服务，出片快。",
  "price": 200.00,
  "coverImage": "https://cdn.example.com/skill/photo.jpg",
  "images": [
    "https://cdn.example.com/skill/photo_1.jpg",
    "https://cdn.example.com/skill/photo_2.jpg"
  ],
  "location": {
    "address": "广东省深圳市南山区科技园",
    "latitude": 22.5431,
    "longitude": 114.0579
  },
  "availableTimes": [
    {
      "dayOfWeek": 6,
      "startTime": "09:00",
      "endTime": "18:00"
    },
    {
      "dayOfWeek": 7,
      "startTime": "09:00",
      "endTime": "18:00"
    }
  ],
  "promises": [
    "准时到达",
    "精修10张"
  ],
  "isOnline": true
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| serviceType | string | 是 | 服务类型 |
| skillName | string | 是 | 技能名称，2-50字符 |
| description | string | 是 | 技能介绍，10-500字符 |
| price | decimal | 是 | 价格，必须>0 |
| location | object | 是 | 服务地点 |
| location.address | string | 是 | 详细地址 |
| location.latitude | decimal | 是 | 纬度 |
| location.longitude | decimal | 是 | 经度 |
| availableTimes | array | 是 | 可用时间段，至少1个 |
| availableTimes[].dayOfWeek | integer | 是 | 星期几，1-7 |
| availableTimes[].startTime | string | 是 | 开始时间，HH:mm |
| availableTimes[].endTime | string | 是 | 结束时间，HH:mm |

**响应示例**

```json
{
  "code": 200,
  "msg": "创建成功",
  "data": 5002
}
```

---

### 3. 获取我的技能列表

**请求**

```http
GET /api/user/skills/my?pageNum=1&pageSize=10
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 10 | 每页数量 |
| skillType | string | 否 | - | 技能类型: `online`, `offline` |

**响应示例**

```json
{
  "total": 3,
  "rows": [
    {
      "skillId": 5001,
      "skillName": "王者荣耀陪玩",
      "skillType": "online",
      "coverImage": "https://cdn.example.com/skill/wzry.jpg",
      "price": 30.00,
      "priceUnit": "局",
      "isOnline": true,
      "rating": 4.8,
      "reviewCount": 128,
      "orderCount": 200,
      "gameName": "王者荣耀",
      "gameRank": "王者"
    },
    {
      "skillId": 5002,
      "skillName": "专业人像摄影",
      "skillType": "offline",
      "coverImage": "https://cdn.example.com/skill/photo.jpg",
      "price": 200.00,
      "priceUnit": "次",
      "isOnline": true,
      "rating": 5.0,
      "reviewCount": 50,
      "orderCount": 80,
      "serviceType": "photography",
      "serviceLocation": "深圳市南山区"
    }
  ],
  "code": 200,
  "msg": "查询成功"
}
```

---

### 4. 获取技能详情

**请求**

```http
GET /api/user/skills/{skillId}
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "skillId": 5001,
    "userId": 10001,
    "skillName": "王者荣耀陪玩",
    "skillType": "online",
    "gameName": "王者荣耀",
    "gameRank": "王者",
    "description": "王者50星巅峰赛选手，可带上分或娱乐陪玩",
    "price": 30.00,
    "priceUnit": "局",
    "serviceHours": 1,
    "coverImage": "https://cdn.example.com/skill/wzry.jpg",
    "images": [
      "https://cdn.example.com/skill/wzry_1.jpg"
    ],
    "promises": [
      "不骂人",
      "准时上线"
    ],
    "isOnline": true,
    "rating": 4.8,
    "reviewCount": 128,
    "orderCount": 200,
    "userInfo": {
      "userId": 10001,
      "nickname": "游戏达人",
      "avatar": "https://cdn.example.com/avatar/10001.jpg",
      "isOnline": true
    },
    "createdAt": "2025-11-01 10:00:00"
  }
}
```

---

### 5. 更新技能

**请求**

```http
PUT /api/user/skills/{skillId}
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "skillName": "王者荣耀陪玩-新赛季",
  "description": "更新后的描述",
  "price": 35.00
}
```

**响应示例**

```json
{
  "code": 200,
  "msg": "更新成功",
  "data": null
}
```

---

### 6. 删除技能

**请求**

```http
DELETE /api/user/skills/{skillId}
Authorization: Bearer <token>
```

**响应示例**

```json
{
  "code": 200,
  "msg": "删除成功",
  "data": null
}
```

---

### 7. 切换技能上下架状态

**请求**

```http
PUT /api/user/skills/{skillId}/toggle?isOnline=true
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| isOnline | boolean | 是 | true=上架, false=下架 |

**响应示例**

```json
{
  "code": 200,
  "msg": "已上架",
  "data": null
}
```

---

### 8. 获取用户技能列表

获取指定用户的技能列表（公开）。

**请求**

```http
GET /api/user/skills/user/{userId}?pageNum=1&pageSize=10
```

---

### 9. 搜索附近技能

基于地理位置搜索附近的线下技能。

**请求**

```http
GET /api/user/skills/nearby?latitude=22.5431&longitude=114.0579&radiusMeters=10000&pageNum=1&pageSize=10
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| latitude | decimal | 是 | - | 用户纬度 |
| longitude | decimal | 是 | - | 用户经度 |
| radiusMeters | integer | 否 | 10000 | 搜索半径（米） |
| pageNum | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 10 | 每页数量 |

**响应示例**

```json
{
  "total": 20,
  "rows": [
    {
      "skillId": 5002,
      "skillName": "专业人像摄影",
      "skillType": "offline",
      "coverImage": "https://cdn.example.com/skill/photo.jpg",
      "price": 200.00,
      "priceUnit": "次",
      "isOnline": true,
      "rating": 5.0,
      "reviewCount": 50,
      "serviceType": "photography",
      "serviceLocation": "深圳市南山区",
      "distance": 1.5
    }
  ],
  "code": 200,
  "msg": "查询成功"
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
xypai-user:    http://localhost:9401 (用户服务)
```

**依赖服务**: Nacos, Redis, MySQL

---

### 测试场景1: 编辑资料页面 (AppEditProfilePageTest)

测试编辑资料页面的所有字段更新功能，支持11个字段的实时保存。

#### 1.1 用户SMS注册

```java
// 接口: POST /xypai-auth/api/auth/login/sms
Map<String, String> loginRequest = new HashMap<>();
loginRequest.put("countryCode", "+86");
loginRequest.put("mobile", "13800000001");
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

#### 1.2 加载编辑页面数据

```java
// 接口: GET /xypai-user/api/user/profile/edit
// 请求头: Authorization: Bearer {token}
String editUrl = GATEWAY_URL + "/xypai-user/api/user/profile/edit";

// 响应
{
  "code": 200,
  "data": {
    "userId": 10001,
    "nickname": "用户10001",
    "gender": null,
    "birthday": null,
    ...
  }
}
```

#### 1.3 更新昵称

```java
// 接口: PUT /xypai-user/api/user/profile/nickname
Map<String, String> updateRequest = new HashMap<>();
updateRequest.put("nickname", "测试昵称_1234");

// 响应
{
  "code": 200,
  "msg": "更新成功"
}
```

#### 1.4 更新性别

```java
// 接口: PUT /xypai-user/api/user/profile/gender
Map<String, String> updateRequest = new HashMap<>();
updateRequest.put("gender", "male");

// 响应
{
  "code": 200,
  "msg": "更新成功"
}
```

#### 1.5 更新生日

```java
// 接口: PUT /xypai-user/api/user/profile/birthday
Map<String, String> updateRequest = new HashMap<>();
updateRequest.put("birthday", "1995-06-15");

// 响应
{
  "code": 200,
  "msg": "更新成功"
}
```

#### 1.6 更新居住地

```java
// 接口: PUT /xypai-user/api/user/profile/residence
Map<String, String> updateRequest = new HashMap<>();
updateRequest.put("residence", "广东省广州市天河区");

// 响应
{
  "code": 200,
  "msg": "更新成功"
}
```

#### 1.7 更新身高

```java
// 接口: PUT /xypai-user/api/user/profile/height
Map<String, Object> updateRequest = new HashMap<>();
updateRequest.put("height", 175);

// 响应
{
  "code": 200,
  "msg": "更新成功"
}
```

#### 1.8 更新体重

```java
// 接口: PUT /xypai-user/api/user/profile/weight
Map<String, Object> updateRequest = new HashMap<>();
updateRequest.put("weight", 65);

// 响应
{
  "code": 200,
  "msg": "更新成功"
}
```

#### 1.9 更新职业

```java
// 接口: PUT /xypai-user/api/user/profile/occupation
Map<String, String> updateRequest = new HashMap<>();
updateRequest.put("occupation", "软件工程师");

// 响应
{
  "code": 200,
  "msg": "更新成功"
}
```

#### 1.10 更新微信号

```java
// 接口: PUT /xypai-user/api/user/profile/wechat
Map<String, String> updateRequest = new HashMap<>();
updateRequest.put("wechat", "wechat_1234");

// 响应
{
  "code": 200,
  "msg": "更新成功"
}
```

#### 1.11 更新个性签名

```java
// 接口: PUT /xypai-user/api/user/profile/bio
Map<String, String> updateRequest = new HashMap<>();
updateRequest.put("bio", "这是我的个性签名");

// 响应
{
  "code": 200,
  "msg": "更新成功"
}
```

---

### 测试场景2: 技能管理页面 (AppSkillManagementPageTest)

测试技能管理页面的完整功能。

#### 2.1 获取我的技能列表

```java
// 接口: GET /xypai-user/api/user/skills/my?pageNum=1&pageSize=10
String skillsUrl = GATEWAY_URL + "/xypai-user/api/user/skills/my?pageNum=1&pageSize=10";

// 响应 (TableDataInfo 格式)
{
  "total": 3,
  "rows": [...],
  "code": 200,
  "msg": "查询成功"
}
```

#### 2.2 获取线上技能列表

```java
// 接口: GET /xypai-user/api/user/skills/my?skillType=online&pageNum=1&pageSize=10
String skillsUrl = GATEWAY_URL + "/xypai-user/api/user/skills/my?skillType=online&pageNum=1&pageSize=10";

// 响应
{
  "total": 2,
  "rows": [
    {
      "skillId": 5001,
      "skillName": "王者荣耀陪玩",
      "skillType": "online",
      "gameName": "王者荣耀",
      "gameRank": "王者",
      "isOnline": true
    }
  ]
}
```

#### 2.3 获取线下技能列表

```java
// 接口: GET /xypai-user/api/user/skills/my?skillType=offline&pageNum=1&pageSize=10
String skillsUrl = GATEWAY_URL + "/xypai-user/api/user/skills/my?skillType=offline&pageNum=1&pageSize=10";
```

#### 2.4 切换技能上下架状态

```java
// 接口: PUT /xypai-user/api/user/skills/{skillId}/toggle?isOnline=true
// 需要先创建技能
```

#### 2.5 删除技能

```java
// 接口: DELETE /xypai-user/api/user/skills/{skillId}
// 需要先创建技能
```

---

### 测试场景3: 新用户注册流程 (AppSmsRegistrationTest)

测试新用户通过 SMS 验证码注册并使用核心功能的完整流程。

#### 3.1 新用户 SMS 注册

```java
// 接口: POST /xypai-auth/api/auth/login/sms
// 使用时间戳生成唯一手机号，确保是新用户
long timestamp = System.currentTimeMillis() % 100000000L;
String uniqueMobile = String.format("138%08d", timestamp);

Map<String, String> loginRequest = new HashMap<>();
loginRequest.put("countryCode", "+86");
loginRequest.put("mobile", uniqueMobile);
loginRequest.put("verificationCode", "123456");  // 测试环境固定验证码

// 响应
{
  "code": 200,
  "data": {
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "userId": 10001,
    "isNewUser": true,
    "nickname": "用户10001"
  }
}
// → isNewUser=true 时前端跳转到完善资料页
```

#### 3.2 获取我的资料

```java
// 接口: GET /xypai-user/api/user/profile/header
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "data": {
    "userId": 10001,
    "nickname": "用户10001",
    "stats": {
      "followingCount": 0,
      "fansCount": 0,
      "likesCount": 0
    }
  }
}
```

#### 3.3 更新昵称

```java
// 接口: PUT /xypai-user/api/user/profile/nickname
Map<String, String> updateRequest = new HashMap<>();
updateRequest.put("nickname", "测试用户_" + System.currentTimeMillis());

// 响应
{
  "code": 200,
  "msg": "更新成功"
}
```

#### 3.4 获取粉丝列表

```java
// 接口: GET /xypai-user/api/user/relation/fans?pageNum=1&pageSize=10
// 请求头: Authorization: Bearer {token}

// 响应 (TableDataInfo 格式)
{
  "total": 0,
  "rows": [],
  "code": 200,
  "msg": "查询成功"
}
```

#### 3.5 获取我的技能列表

```java
// 接口: GET /xypai-user/api/user/skills/my?pageNum=1&pageSize=10
// 请求头: Authorization: Bearer {token}

// 响应 (新用户技能为空)
{
  "total": 0,
  "rows": [],
  "code": 200,
  "msg": "查询成功"
}
```

---

### 测试场景4: 个人主页页面 (AppProfilePageTest)

测试个人主页页面的完整功能，包括头部信息、动态、收藏、点赞列表。

#### 4.1 获取个人主页头部信息

```java
// 接口: GET /xypai-user/api/user/profile/header
// 请求头: Authorization: Bearer {token}

// 响应
{
  "code": 200,
  "data": {
    "userId": 10001,
    "nickname": "小美探店",
    "avatar": "https://cdn.example.com/avatar/10001.jpg",
    "stats": {
      "followingCount": 128,
      "fansCount": 1024,
      "likesCount": 5000
    }
  }
}
```

#### 4.2 获取动态列表

```java
// 接口: GET /xypai-user/api/user/profile/posts?page=1&pageSize=10
// 请求头: Authorization: Bearer {token}
// 触发时机: 点击"动态"Tab

// 响应
{
  "code": 200,
  "data": {
    "posts": [...],
    "total": 50,
    "hasMore": true
  }
}
```

#### 4.3 获取收藏列表

```java
// 接口: GET /xypai-user/api/user/profile/favorites?page=1&pageSize=10
// 请求头: Authorization: Bearer {token}
// 触发时机: 点击"收藏"Tab

// 响应
{
  "code": 200,
  "data": {
    "favorites": [...],
    "total": 100,
    "hasMore": true
  }
}
```

#### 4.4 获取点赞列表

```java
// 接口: GET /xypai-user/api/user/profile/likes?page=1&pageSize=10
// 请求头: Authorization: Bearer {token}
// 触发时机: 点击"点赞"Tab

// 响应
{
  "code": 200,
  "data": {
    "likes": [...],
    "total": 200,
    "hasMore": true
  }
}
```

#### 4.5 获取个人资料信息

```java
// 接口: GET /xypai-user/api/user/profile/info
// 请求头: Authorization: Bearer {token}
// 触发时机: 点击"资料"Tab

// 响应
{
  "code": 200,
  "data": {
    "userId": 10001,
    "nickname": "小美探店",
    "gender": "female",
    "skills": [...]
  }
}
```

---

### 测试场景5: 他人主页页面 (AppOtherUserProfilePageTest)

测试查看他人主页页面的完整功能，包括关注、拉黑、举报。

#### 5.1 准备测试数据 - 创建目标用户

```java
// 创建第二个用户作为目标用户
long timestamp = System.currentTimeMillis() % 100000000L;
String uniqueMobile = String.format("139%08d", timestamp);

Map<String, String> loginRequest = new HashMap<>();
loginRequest.put("countryCode", "+86");
loginRequest.put("mobile", uniqueMobile);
loginRequest.put("verificationCode", "123456");

// 响应
{
  "code": 200,
  "data": {
    "token": "...",
    "userId": 10002  // targetUserId
  }
}
```

#### 5.2 获取他人主页信息

```java
// 接口: GET /xypai-user/api/user/profile/other/{targetUserId}
// 请求头: Authorization: Bearer {token}
String profileUrl = GATEWAY_URL + "/xypai-user/api/user/profile/other/" + targetUserId;

// 响应
{
  "code": 200,
  "data": {
    "userId": 10002,
    "nickname": "游戏达人",
    "avatar": "https://cdn.example.com/avatar/10002.jpg",
    "followStatus": "none",
    "stats": {
      "followingCount": 50,
      "fansCount": 2000
    }
  }
}
```

#### 5.3 关注用户

```java
// 接口: POST /xypai-user/api/user/relation/follow/{targetUserId}
// 请求头: Authorization: Bearer {token}
String followUrl = GATEWAY_URL + "/xypai-user/api/user/relation/follow/" + targetUserId;

// 响应
{
  "code": 200,
  "msg": "关注成功"
}
```

#### 5.4 取消关注

```java
// 接口: DELETE /xypai-user/api/user/relation/follow/{targetUserId}
// 请求头: Authorization: Bearer {token}
String unfollowUrl = GATEWAY_URL + "/xypai-user/api/user/relation/follow/" + targetUserId;

// 响应
{
  "code": 200,
  "msg": "已取消关注"
}
```

#### 5.5 获取用户技能列表

```java
// 接口: GET /xypai-user/api/user/skills/user/{targetUserId}?pageNum=1&pageSize=20
// 请求头: Authorization: Bearer {token}
String skillsUrl = GATEWAY_URL + "/xypai-user/api/user/skills/user/" + targetUserId + "?pageNum=1&pageSize=20";

// 响应 (TableDataInfo 格式)
{
  "total": 5,
  "rows": [...],
  "code": 200,
  "msg": "查询成功"
}
```

#### 5.6 举报用户

```java
// 接口: POST /xypai-user/api/user/relation/report/{targetUserId}
// 请求头: Authorization: Bearer {token}
Map<String, String> reportRequest = new HashMap<>();
reportRequest.put("reason", "spam");
reportRequest.put("description", "发布垃圾广告信息");

// 响应
{
  "code": 200,
  "msg": "举报已提交"
}
```

#### 5.7 拉黑用户

```java
// 接口: POST /xypai-user/api/user/relation/block/{targetUserId}
// 请求头: Authorization: Bearer {token}
String blockUrl = GATEWAY_URL + "/xypai-user/api/user/relation/block/" + targetUserId;

// 响应
{
  "code": 200,
  "msg": "已拉黑"
}
```

---

### 测试场景6: 粉丝列表页面 (AppFansListPageTest)

测试粉丝列表页面的完整功能，包括获取粉丝、搜索、回关。

#### 6.1 准备测试数据 - 创建粉丝用户

```java
// 创建粉丝用户
long timestamp = System.currentTimeMillis() % 100000000L;
String uniqueMobile = String.format("139%08d", timestamp);

// 登录获取 fanToken 和 fanUserId
// 然后让粉丝用户关注当前用户

// 接口: POST /xypai-user/api/user/relation/follow/{currentUserId}
// 请求头: Authorization: Bearer {fanToken}
String followUrl = GATEWAY_URL + "/xypai-user/api/user/relation/follow/" + userId;
```

#### 6.2 获取粉丝列表

```java
// 接口: GET /xypai-user/api/user/relation/fans?pageNum=1&pageSize=20
// 请求头: Authorization: Bearer {token}
String fansUrl = GATEWAY_URL + "/xypai-user/api/user/relation/fans?pageNum=1&pageSize=20";

// 响应 (TableDataInfo 格式)
{
  "total": 1024,
  "rows": [
    {
      "userId": 10002,
      "nickname": "游戏达人",
      "avatar": "https://cdn.example.com/avatar/10002.jpg",
      "isOnline": true,
      "followStatus": "none",
      "isFollowing": false,
      "isMutualFollow": false
    }
  ],
  "code": 200,
  "msg": "查询成功"
}
```

#### 6.3 搜索粉丝

```java
// 接口: GET /xypai-user/api/user/relation/fans?keyword=User&pageNum=1&pageSize=20
// 请求头: Authorization: Bearer {token}
String searchUrl = GATEWAY_URL + "/xypai-user/api/user/relation/fans?keyword=User&pageNum=1&pageSize=20";

// 响应
{
  "total": 5,
  "rows": [...],
  "code": 200,
  "msg": "查询成功"
}
```

#### 6.4 回关粉丝

```java
// 接口: POST /xypai-user/api/user/relation/follow/{fanUserId}
// 请求头: Authorization: Bearer {token}
String followUrl = GATEWAY_URL + "/xypai-user/api/user/relation/follow/" + fanUserId;

// 响应
{
  "code": 200,
  "msg": "关注成功"
}
// → 此时双方互相关注，变成互粉关系
```

#### 6.5 取消关注粉丝

```java
// 接口: DELETE /xypai-user/api/user/relation/follow/{fanUserId}
// 请求头: Authorization: Bearer {token}
String unfollowUrl = GATEWAY_URL + "/xypai-user/api/user/relation/follow/" + fanUserId;

// 响应
{
  "code": 200,
  "msg": "已取消关注"
}
```

---

### 测试场景7: 关注列表页面 (AppFollowingListPageTest)

测试关注列表页面的完整功能，包括获取关注列表、搜索、取消关注。

#### 7.1 准备测试数据 - 关注用户

```java
// 创建被关注的用户
long timestamp = System.currentTimeMillis() % 100000000L;
String uniqueMobile = String.format("139%08d", timestamp);

// 登录获取 followingUserId
// 然后当前用户关注这个用户

// 接口: POST /xypai-user/api/user/relation/follow/{followingUserId}
// 请求头: Authorization: Bearer {token}
String followUrl = GATEWAY_URL + "/xypai-user/api/user/relation/follow/" + followingUserId;
```

#### 7.2 获取关注列表

```java
// 接口: GET /xypai-user/api/user/relation/following?pageNum=1&pageSize=20
// 请求头: Authorization: Bearer {token}
String followingUrl = GATEWAY_URL + "/xypai-user/api/user/relation/following?pageNum=1&pageSize=20";

// 响应 (TableDataInfo 格式)
{
  "total": 128,
  "rows": [
    {
      "userId": 10002,
      "nickname": "游戏达人",
      "avatar": "https://cdn.example.com/avatar/10002.jpg",
      "isOnline": true,
      "followStatus": "following",
      "isFollowing": true,
      "isMutualFollow": false
    }
  ],
  "code": 200,
  "msg": "查询成功"
}
```

#### 7.3 搜索关注列表

```java
// 接口: GET /xypai-user/api/user/relation/following?keyword=User&pageNum=1&pageSize=20
// 请求头: Authorization: Bearer {token}
String searchUrl = GATEWAY_URL + "/xypai-user/api/user/relation/following?keyword=User&pageNum=1&pageSize=20";

// 响应
{
  "total": 3,
  "rows": [...],
  "code": 200,
  "msg": "查询成功"
}
```

#### 7.4 取消关注

```java
// 接口: DELETE /xypai-user/api/user/relation/follow/{followingUserId}
// 请求头: Authorization: Bearer {token}
String unfollowUrl = GATEWAY_URL + "/xypai-user/api/user/relation/follow/" + followingUserId;

// 响应
{
  "code": 200,
  "msg": "已取消关注"
}
```

---

### 测试场景8: 技能预约页面 (AppSkillBookingPageTest)

测试技能预约页面的 UserService 相关功能。

#### 8.1 获取技能预约详情

```java
// 接口: GET /xypai-user/api/skills/{skillId}/booking-detail
String detailUrl = GATEWAY_URL + "/xypai-user/api/skills/" + skillId + "/booking-detail";

// 响应
{
  "code": 200,
  "data": {
    "skillId": 5001,
    "skillName": "王者荣耀陪玩",
    "price": 30.00,
    "userInfo": {...}
  }
}
```

#### 8.2 获取技能评价列表

```java
// 接口: GET /xypai-user/api/skills/{skillId}/reviews?pageNum=1&pageSize=20
String reviewsUrl = GATEWAY_URL + "/xypai-user/api/skills/" + skillId + "/reviews?pageNum=1&pageSize=20";

// 响应
{
  "code": 200,
  "data": {
    "reviews": [...],
    "total": 50
  }
}
```

**注意**: 订单相关接口属于 xypai-order 模块，不在此测试范围内。

---

### 测试关系图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    共享认证模式 (xypai-auth)                              │
│                                                                         │
│   所有测试使用: POST /xypai-auth/api/auth/login/sms                      │
│   → 返回: token + userId                                                │
│   → Token 用于所有后续 API 调用                                          │
└────────────────────────────────────────────────────────────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        ▼                           ▼                           ▼
┌─────────────────┐    ┌────────────────────┐    ┌─────────────────────┐
│ AppSms          │    │ AppProfilePage     │    │ AppOtherUserProfile │
│ RegistrationTest│───▶│ Test               │    │ PageTest            │
│                 │    │                    │    │                     │
│ • SMS 登录      │    │ • 主页头部         │    │ • 查看他人主页      │
│ • 获取资料      │    │ • 动态列表         │    │ • 关注/取消关注     │
│ • 更新昵称      │    │ • 收藏列表         │    │ • 获取用户技能      │
│ • 获取粉丝      │    │ • 点赞列表         │    │ • 举报用户          │
│ • 获取技能      │    │ • 资料信息         │    │ • 拉黑用户          │
└─────────────────┘    └────────────────────┘    └─────────────────────┘
        │                                                   │
        │                                                   │
        ▼                                                   ▼
┌─────────────────┐                            ┌─────────────────────┐
│ AppFansListPage │                            │ AppFollowingListPage│
│ Test            │◀───────────────────────────│ Test                │
│                 │        (互粉关系)           │                     │
│ • 创建粉丝      │                            │ • 创建关注          │
│ • 获取粉丝列表  │                            │ • 获取关注列表      │
│ • 搜索粉丝      │                            │ • 搜索关注          │
│ • 回关粉丝      │                            │ • 取消关注          │
│ • 取消关注      │                            │                     │
└─────────────────┘                            └─────────────────────┘
```

---

### 运行测试

```bash
# 进入用户服务目录
cd xypai-modules/xypai-user

# 运行编辑资料测试
mvn test -Dtest=AppEditProfilePageTest

# 运行技能管理测试
mvn test -Dtest=AppSkillManagementPageTest

# 运行技能预约测试
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

**文档版本**: v1.0.0

**最后更新**: 2025-11-28
