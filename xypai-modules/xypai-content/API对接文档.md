# XyPai-Content 内容服务 API 对接文档

> **版本**: v1.0.0
>
> **更新日期**: 2025-11-27
>
> **服务端口**: 9403
>
> **接口前缀**: `/api/v1/`

---

## 📋 目录

1. [通用说明](#通用说明)
2. [Feed 动态接口](#feed-动态接口)
3. [评论接口](#评论接口)
4. [互动接口](#互动接口)
5. [话题接口](#话题接口)
6. [活动接口](#活动接口)
7. [错误码说明](#错误码说明)
8. [集成测试用例](#集成测试用例)

---

## 通用说明

### 基础URL

```
# 开发环境（直连服务）
http://localhost:9403/api/v1/

# 生产环境（通过网关）
http://gateway:8080/xypai-content/api/v1/
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
  "msg": "动态不存在",
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

## Feed 动态接口

### 1. 获取动态列表

获取 Feed 流列表，支持关注/热门/同城三种 Tab。

**请求**

```http
GET /api/v1/content/feed/{tabType}
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| tabType | string | 是 | Tab类型: `follow`(关注), `hot`(热门), `local`(同城) |

**查询参数**

```json
{
  "pageNum": 1,
  "pageSize": 20,
  "latitude": 22.5431,
  "longitude": 114.0579,
  "radius": 5
}
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | integer | 是 | 1 | 页码，最小1 |
| pageSize | integer | 是 | 20 | 每页数量，1-100 |
| latitude | decimal | 否 | - | 用户纬度（同城Tab必传） |
| longitude | decimal | 否 | - | 用户经度（同城Tab必传） |
| radius | integer | 否 | 5 | 搜索半径（km） |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [
      {
        "id": 1001,
        "userId": 2001,
        "type": 1,
        "typeDesc": "动态",
        "title": "今天的探店日记",
        "content": "发现了一家超棒的咖啡馆，环境很好，咖啡也很香...",
        "summary": "发现了一家超棒的咖啡馆，环境很好，咖啡也很香...",
        "userInfo": {
          "id": 2001,
          "nickname": "小美探店",
          "avatar": "https://cdn.example.com/avatar/2001.jpg",
          "gender": "female",
          "age": 25,
          "isFollowed": false,
          "isRealVerified": true,
          "isGodVerified": false,
          "isVip": true,
          "isPopular": true
        },
        "mediaList": [
          {
            "mediaId": 3001,
            "mediaType": "image",
            "url": "https://cdn.example.com/feed/3001.jpg",
            "thumbnailUrl": "https://cdn.example.com/feed/3001_thumb.jpg"
          }
        ],
        "topicList": [
          {
            "name": "探店日记",
            "isHot": true
          }
        ],
        "locationName": "深圳市南山区",
        "distance": 1.5,
        "cityId": 440300,
        "likeCount": 128,
        "commentCount": 32,
        "shareCount": 15,
        "collectCount": 45,
        "viewCount": 1024,
        "isLiked": false,
        "isCollected": true,
        "createdAt": "2025-11-27 10:30:00"
      }
    ],
    "total": 100,
    "size": 20,
    "current": 1,
    "pages": 5
  }
}
```

---

### 2. 获取动态详情

获取单个动态的完整信息。

**请求**

```http
GET /api/v1/content/detail/{feedId}
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| feedId | long | 是 | 动态ID |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 1001,
    "userId": 2001,
    "type": 1,
    "typeDesc": "动态",
    "title": "今天的探店日记",
    "content": "发现了一家超棒的咖啡馆...(完整内容)",
    "summary": "发现了一家超棒的咖啡馆...",
    "userInfo": {
      "id": 2001,
      "nickname": "小美探店",
      "avatar": "https://cdn.example.com/avatar/2001.jpg",
      "gender": "female",
      "age": 25,
      "isFollowed": false,
      "isRealVerified": true,
      "isGodVerified": false,
      "isVip": true
    },
    "mediaList": [
      {
        "mediaId": 3001,
        "mediaType": "image",
        "url": "https://cdn.example.com/feed/3001.jpg",
        "thumbnailUrl": "https://cdn.example.com/feed/3001_thumb.jpg",
        "width": 1920,
        "height": 1080,
        "duration": null
      },
      {
        "mediaId": 3002,
        "mediaType": "video",
        "url": "https://cdn.example.com/feed/3002.mp4",
        "thumbnailUrl": "https://cdn.example.com/feed/3002_cover.jpg",
        "width": 1920,
        "height": 1080,
        "duration": 30
      }
    ],
    "topicList": [
      {
        "name": "探店日记",
        "description": "分享你的探店体验",
        "participantCount": 10000,
        "postCount": 50000
      }
    ],
    "locationName": "深圳市南山区",
    "locationAddress": "科技园南路88号",
    "distance": 1.5,
    "cityId": 440300,
    "likeCount": 128,
    "commentCount": 32,
    "shareCount": 15,
    "collectCount": 45,
    "viewCount": 1024,
    "isLiked": false,
    "isCollected": true,
    "canEdit": true,
    "canDelete": true,
    "createdAt": "2025-11-27 10:30:00"
  }
}
```

---

### 3. 发布动态

发布新动态，支持文字、图片、视频、话题、地点。

**请求**

```http
POST /api/v1/content/publish
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

---

### 4. 删除动态

删除自己发布的动态。

**请求**

```http
DELETE /api/v1/content/{feedId}
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

### 5. 获取用户动态列表

获取指定用户发布的动态列表。支持查看他人公开动态或自己的所有动态。

**请求**

```http
GET /api/v1/content/feed/user/{userId}
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 目标用户ID |

**查询参数**

```json
{
  "pageNum": 1,
  "pageSize": 20
}
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | integer | 否 | 1 | 页码，最小1 |
| pageSize | integer | 否 | 20 | 每页数量，1-100 |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [
      {
        "id": 1001,
        "userId": 2001,
        "type": 1,
        "typeDesc": "动态",
        "title": "今天的探店日记",
        "content": "发现了一家超棒的咖啡馆，环境很好，咖啡也很香...",
        "summary": "发现了一家超棒的咖啡馆，环境很好，咖啡也很香...",
        "userInfo": {
          "id": 2001,
          "nickname": "小美探店",
          "avatar": "https://cdn.example.com/avatar/2001.jpg",
          "gender": "female",
          "age": 25,
          "isFollowed": false,
          "isRealVerified": true,
          "isGodVerified": false,
          "isVip": true,
          "isPopular": true
        },
        "mediaList": [
          {
            "mediaId": 3001,
            "mediaType": "image",
            "url": "https://cdn.example.com/feed/3001.jpg",
            "thumbnailUrl": "https://cdn.example.com/feed/3001_thumb.jpg"
          }
        ],
        "topicList": [
          {
            "name": "探店日记",
            "isHot": true
          }
        ],
        "locationName": "深圳市南山区",
        "cityId": 440300,
        "likeCount": 128,
        "commentCount": 32,
        "shareCount": 15,
        "collectCount": 45,
        "viewCount": 1024,
        "isLiked": false,
        "isCollected": true,
        "createdAt": "2025-11-27 10:30:00"
      }
    ],
    "total": 50,
    "size": 20,
    "current": 1,
    "pages": 3
  }
}
```

**权限说明**

- 查看他人动态：仅返回 `visibility=0`（公开）的动态
- 查看自己动态：返回所有动态（包括仅自己可见的）

---

## 评论接口

### 1. 获取评论列表

获取动态的评论列表，包含二级回复。

**请求**

```http
GET /api/v1/content/comments/{feedId}
```

**查询参数**

```json
{
  "pageNum": 1,
  "pageSize": 20,
  "sortType": "time"
}
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | integer | 是 | 1 | 页码 |
| pageSize | integer | 是 | 20 | 每页数量，1-100 |
| sortType | string | 否 | time | 排序: `time`(时间), `hot`(热度), `like`(点赞) |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [
      {
        "id": 4001,
        "feedId": 1001,
        "userId": 2002,
        "content": "写得真好！推荐大家去试试",
        "userInfo": {
          "id": 2002,
          "nickname": "咖啡爱好者",
          "avatar": "https://cdn.example.com/avatar/2002.jpg"
        },
        "likeCount": 25,
        "replyCount": 3,
        "isTop": true,
        "isLiked": false,
        "replies": [
          {
            "id": 4002,
            "content": "已收藏，下周去！",
            "userInfo": {
              "id": 2003,
              "nickname": "美食达人",
              "avatar": "https://cdn.example.com/avatar/2003.jpg"
            },
            "replyToUserNickname": "咖啡爱好者",
            "createdAt": "2025-11-27 11:00:00"
          }
        ],
        "totalReplies": 3,
        "hasMoreReplies": false,
        "canDelete": false,
        "createdAt": "2025-11-27 10:45:00"
      }
    ],
    "total": 32,
    "size": 20,
    "current": 1,
    "pages": 2
  }
}
```

---

### 2. 发表评论/回复

发表一级评论或二级回复。

**请求**

```http
POST /api/v1/content/comment
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "feedId": 1001,
  "content": "写得真好！",
  "parentId": null,
  "replyToUserId": null
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| feedId | long | 是 | 动态ID |
| content | string | 是 | 评论内容，1-500字符 |
| parentId | long | 否 | 父评论ID（回复时传入） |
| replyToUserId | long | 否 | 回复的用户ID（回复时传入） |

**响应示例**

```json
{
  "code": 200,
  "msg": "评论成功",
  "data": {
    "id": 4003,
    "feedId": 1001,
    "userId": 2001,
    "content": "写得真好！",
    "userInfo": {
      "id": 2001,
      "nickname": "小美探店",
      "avatar": "https://cdn.example.com/avatar/2001.jpg"
    },
    "likeCount": 0,
    "replyCount": 0,
    "isTop": false,
    "isLiked": false,
    "replies": [],
    "totalReplies": 0,
    "hasMoreReplies": false,
    "canDelete": true,
    "createdAt": "2025-11-27 12:00:00"
  }
}
```

---

### 3. 删除评论

删除自己的评论。

**请求**

```http
DELETE /api/v1/content/comment/{commentId}
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

### 4. 置顶/取消置顶评论

动态作者可以置顶或取消置顶评论。

**请求**

```http
PUT /api/v1/content/comment/{commentId}/pin?pin=true
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pin | boolean | 否 | true | 是否置顶 |

**响应示例**

```json
{
  "code": 200,
  "msg": "置顶成功",
  "data": null
}
```

---

## 互动接口

### 1. 点赞/取消点赞

对动态或评论进行点赞/取消点赞操作。

**请求**

```http
POST /api/v1/interaction/like
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "targetType": "feed",
  "targetId": 1001,
  "action": "like"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetType | string | 是 | 目标类型: `feed`(动态), `comment`(评论) |
| targetId | long | 是 | 目标ID |
| action | string | 是 | 操作: `like`(点赞), `unlike`(取消点赞) |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "success": true,
    "count": 129,
    "isActive": true
  }
}
```

---

### 2. 收藏/取消收藏

对动态进行收藏/取消收藏操作。

**请求**

```http
POST /api/v1/interaction/collect
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "targetType": "feed",
  "targetId": 1001,
  "action": "collect"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetType | string | 是 | 目标类型: `feed`(仅支持动态) |
| targetId | long | 是 | 目标ID |
| action | string | 是 | 操作: `collect`(收藏), `uncollect`(取消收藏) |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "success": true,
    "count": 46,
    "isActive": true
  }
}
```

---

### 3. 分享动态

分享动态到不同渠道。

**请求**

```http
POST /api/v1/interaction/share
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "targetType": "feed",
  "targetId": 1001,
  "shareChannel": "wechat"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetType | string | 是 | 目标类型: `feed` |
| targetId | long | 是 | 动态ID |
| shareChannel | string | 是 | 分享渠道: `wechat`, `moments`, `qq`, `qzone`, `weibo`, `copy_link` |

**响应示例**

```json
{
  "code": 200,
  "msg": "分享成功",
  "data": {
    "success": true,
    "count": 16,
    "isActive": true
  }
}
```

---

### 4. 我的点赞列表

获取当前用户的点赞记录。

**请求**

```http
GET /api/v1/interaction/like/my
Authorization: Bearer <token>
```

**查询参数**

```json
{
  "pageNum": 1,
  "pageSize": 20,
  "type": "feed"
}
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 20 | 每页数量，1-100 |
| type | string | 否 | feed | 点赞类型: `feed`(动态), `comment`(评论), `all`(全部) |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [
      {
        "id": 5001,
        "targetType": "feed",
        "targetId": 1001,
        "targetContent": "发现了一家超棒的咖啡馆，环境很好...",
        "targetCover": "https://cdn.example.com/feed/3001_thumb.jpg",
        "author": {
          "userId": 2001,
          "nickname": "小美探店",
          "avatar": "https://cdn.example.com/avatar/2001.jpg"
        },
        "likeTime": "2025-11-27 10:00:00"
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

### 5. 我的收藏列表

获取当前用户的收藏记录。

**请求**

```http
GET /api/v1/interaction/collect/my
Authorization: Bearer <token>
```

**查询参数**

```json
{
  "pageNum": 1,
  "pageSize": 20
}
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | integer | 否 | 1 | 页码 |
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
        "targetType": "feed",
        "targetId": 1001,
        "targetContent": "发现了一家超棒的咖啡馆，环境很好...",
        "targetCover": "https://cdn.example.com/feed/3001_thumb.jpg",
        "author": {
          "userId": 2001,
          "nickname": "小美探店",
          "avatar": "https://cdn.example.com/avatar/2001.jpg"
        },
        "collectTime": "2025-11-27 10:00:00"
      }
    ],
    "total": 30,
    "size": 20,
    "current": 1,
    "pages": 2
  }
}
```

---

## 话题接口

### 1. 热门话题列表

获取热门话题，按帖子数和参与人数排序。

**请求**

```http
GET /api/v1/content/topics/hot
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
GET /api/v1/content/topics/search?keyword=探店
```

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 是 | 搜索关键词，1-20字符 |
| page | integer | 否 | 页码，默认1 |
| pageSize | integer | 否 | 每页数量，默认20 |

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

### 3. 话题下的动态列表

获取指定话题关联的所有动态。

**请求**

```http
GET /api/v1/content/topics/{topicId}/feeds
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| topicId | long | 是 | 话题ID |

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
        "id": 1001,
        "userId": 2001,
        "type": 1,
        "typeDesc": "动态",
        "title": "今天的探店日记",
        "content": "发现了一家超棒的咖啡馆...",
        "summary": "发现了一家超棒的咖啡馆...",
        "locationName": "深圳市南山区",
        "cityId": 440300,
        "likeCount": 128,
        "commentCount": 32,
        "shareCount": 15,
        "collectCount": 45,
        "viewCount": 1024,
        "isLiked": false,
        "isCollected": false,
        "createdAt": "2025-11-27 10:30:00"
      }
    ],
    "total": 500,
    "size": 20,
    "current": 1,
    "pages": 25
  }
}
```

---

## 活动接口

### 1. 活动列表

获取组局活动列表，支持分页、筛选、排序。

**请求**

```http
GET /api/v1/activity/list
```

**查询参数**

```json
{
  "pageNum": 1,
  "pageSize": 10,
  "sortBy": "smart_recommend",
  "typeCode": "billiards",
  "gender": "all",
  "memberCount": "2-4",
  "city": "深圳市",
  "district": "南山区",
  "latitude": 22.5431,
  "longitude": 114.0579
}
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | integer | 否 | 1 | 页码 |
| pageSize | integer | 否 | 10 | 每页数量，1-100 |
| sortBy | string | 否 | - | 排序: `smart_recommend`, `newest`, `distance_asc`, `start_time_asc` |
| typeCode | string | 否 | - | 活动类型编码 |
| gender | string | 否 | - | 性别筛选: `all`, `male`, `female` |
| memberCount | string | 否 | - | 人数范围: `2-4` |
| city | string | 否 | - | 城市 |
| district | string | 否 | - | 区县 |
| latitude | decimal | 否 | - | 用户纬度（距离排序时使用） |
| longitude | decimal | 否 | - | 用户经度（距离排序时使用） |

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [
      {
        "activityId": 7001,
        "typeCode": "billiards",
        "typeName": "台球",
        "title": "周末台球局",
        "coverImageUrl": "https://cdn.example.com/activity/7001.jpg",
        "startTime": "2025-12-01T14:00:00",
        "endTime": "2025-12-01T18:00:00",
        "timeDisplay": "12月1日 周六 14:00-18:00",
        "locationName": "星球台球俱乐部",
        "city": "深圳市",
        "district": "南山区",
        "distance": 1500,
        "distanceDisplay": "1.5km",
        "currentMembers": 3,
        "maxMembers": 6,
        "membersDisplay": "3/6人",
        "genderLimit": "all",
        "genderLimitDisplay": "不限",
        "isPaid": true,
        "fee": 30.00,
        "feeDisplay": "¥30/人",
        "status": "recruiting",
        "statusDisplay": "招募中",
        "tags": ["新手友好", "周末局"],
        "organizer": {
          "userId": 2001,
          "nickname": "台球达人",
          "avatarUrl": "https://cdn.example.com/avatar/2001.jpg",
          "isVerified": true
        },
        "participantAvatars": [
          "https://cdn.example.com/avatar/2001.jpg",
          "https://cdn.example.com/avatar/2002.jpg",
          "https://cdn.example.com/avatar/2003.jpg"
        ]
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  }
}
```

---

### 2. 活动详情

获取单个活动的完整信息。

**请求**

```http
GET /api/v1/activity/detail/{activityId}
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "activityId": 7001,
    "typeCode": "billiards",
    "typeName": "台球",
    "title": "周末台球局",
    "description": "欢迎大家来参加台球活动，新手老手都欢迎！",
    "coverImageUrl": "https://cdn.example.com/activity/7001.jpg",
    "imageUrls": [
      "https://cdn.example.com/activity/7001_1.jpg",
      "https://cdn.example.com/activity/7001_2.jpg"
    ],
    "startTime": "2025-12-01T14:00:00",
    "endTime": "2025-12-01T18:00:00",
    "timeDisplay": "12月1日 周六 14:00-18:00",
    "registrationDeadline": "2025-12-01T12:00:00",
    "registrationDeadlineDisplay": "报名截止: 12月1日 12:00",
    "createTime": "2025-11-27T10:00:00",
    "locationName": "星球台球俱乐部",
    "locationAddress": "深圳市南山区科技园南路88号",
    "city": "深圳市",
    "district": "南山区",
    "longitude": 113.9430,
    "latitude": 22.5440,
    "distance": 1500,
    "distanceDisplay": "1.5km",
    "minMembers": 2,
    "maxMembers": 6,
    "currentMembers": 3,
    "membersDisplay": "3/6人",
    "genderLimit": "all",
    "genderLimitDisplay": "不限",
    "isPaid": true,
    "fee": 30.00,
    "feeDescription": "包含场地费和饮料",
    "feeDisplay": "¥30/人",
    "status": "recruiting",
    "statusDisplay": "招募中",
    "needApproval": false,
    "contactInfo": "微信: taiqiu666",
    "viewCount": 256,
    "shareCount": 12,
    "tags": ["新手友好", "周末局"],
    "organizer": {
      "userId": 2001,
      "nickname": "台球达人",
      "avatarUrl": "https://cdn.example.com/avatar/2001.jpg",
      "isVerified": true,
      "verifyType": "god",
      "level": 5,
      "organizedCount": 20,
      "goodRateDisplay": "98%好评",
      "bio": "热爱台球，喜欢交友"
    },
    "participants": [
      {
        "userId": 2002,
        "nickname": "小王",
        "avatarUrl": "https://cdn.example.com/avatar/2002.jpg",
        "gender": "male",
        "status": "approved",
        "registerTime": "2025-11-27T11:00:00",
        "message": "期待参加！"
      }
    ],
    "pendingCount": 2,
    "isOrganizer": false,
    "currentUserStatus": "none",
    "canRegister": true,
    "canCancel": false,
    "cannotRegisterReason": null
  }
}
```

---

### 3. 发布活动

发布新的组局活动。

**请求**

```http
POST /api/v1/activity/publish
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "typeCode": "billiards",
  "title": "周末台球局",
  "description": "欢迎大家来参加台球活动",
  "coverImageUrl": "https://cdn.example.com/activity/7001.jpg",
  "imageUrls": ["https://cdn.example.com/activity/7001_1.jpg"],
  "startTime": "2025-12-01T14:00:00",
  "endTime": "2025-12-01T18:00:00",
  "registrationDeadline": "2025-12-01T12:00:00",
  "locationName": "星球台球俱乐部",
  "locationAddress": "深圳市南山区科技园南路88号",
  "city": "深圳市",
  "district": "南山区",
  "longitude": 113.9430,
  "latitude": 22.5440,
  "minMembers": 2,
  "maxMembers": 6,
  "genderLimit": "all",
  "isPaid": true,
  "fee": 30.00,
  "feeDescription": "包含场地费和饮料",
  "needApproval": false,
  "contactInfo": "微信: taiqiu666",
  "tags": ["新手友好", "周末局"]
}
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| typeCode | string | 是 | - | 活动类型编码 |
| title | string | 是 | - | 活动标题 |
| description | string | 否 | - | 活动描述 |
| coverImageUrl | string | 否 | - | 封面图URL |
| imageUrls | array[string] | 否 | - | 活动图片URL列表 |
| startTime | datetime | 是 | - | 活动开始时间 |
| endTime | datetime | 否 | - | 活动结束时间 |
| registrationDeadline | datetime | 否 | - | 报名截止时间 |
| locationName | string | 是 | - | 地点名称 |
| locationAddress | string | 否 | - | 详细地址 |
| city | string | 否 | - | 城市 |
| district | string | 否 | - | 区县 |
| longitude | decimal | 否 | - | 经度 |
| latitude | decimal | 否 | - | 纬度 |
| minMembers | integer | 否 | 2 | 最少人数 |
| maxMembers | integer | 是 | - | 最多人数 |
| genderLimit | string | 否 | all | 性别限制: `all`, `male`, `female` |
| isPaid | boolean | 否 | false | 是否收费 |
| fee | decimal | 否 | - | 费用金额（元/人） |
| feeDescription | string | 否 | - | 费用说明 |
| needApproval | boolean | 否 | false | 是否需要审核 |
| contactInfo | string | 否 | - | 联系方式 |
| tags | array[string] | 否 | - | 标签列表 |

**响应示例**

```json
{
  "code": 200,
  "msg": "发布成功",
  "data": 7001
}
```

---

### 4. 报名活动

报名参加组局活动。

**请求**

```http
POST /api/v1/activity/register
Content-Type: application/json
Authorization: Bearer <token>
```

**请求体**

```json
{
  "activityId": 7001,
  "message": "期待参加！"
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
  "msg": "报名成功",
  "data": 8001
}
```

---

### 5. 取消报名

取消活动报名。

**请求**

```http
POST /api/v1/activity/cancel-registration?activityId=7001&reason=有事无法参加
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| activityId | long | 是 | 活动ID |
| reason | string | 否 | 取消原因 |

**响应示例**

```json
{
  "code": 200,
  "msg": "取消成功",
  "data": null
}
```

---

### 6. 审核报名

发起人审核报名申请。

**请求**

```http
POST /api/v1/activity/approve?activityId=7001&participantId=8001&approved=true
Authorization: Bearer <token>
```

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| activityId | long | 是 | 活动ID |
| participantId | long | 是 | 参与者ID |
| approved | boolean | 是 | 是否通过 |

**响应示例**

```json
{
  "code": 200,
  "msg": "审核通过",
  "data": null
}
```

---

### 7. 取消活动

发起人取消活动。

**请求**

```http
POST /api/v1/activity/cancel/{activityId}?reason=天气原因取消
Authorization: Bearer <token>
```

**响应示例**

```json
{
  "code": 200,
  "msg": "活动已取消",
  "data": null
}
```

---

### 8. 活动类型列表

获取所有活动类型配置。

**请求**

```http
GET /api/v1/activity/types
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "code": "billiards",
      "name": "台球",
      "icon": "https://cdn.example.com/icon/billiards.png",
      "sortOrder": 1,
      "status": 1
    },
    {
      "id": 2,
      "code": "boardgame",
      "name": "桌游",
      "icon": "https://cdn.example.com/icon/boardgame.png",
      "sortOrder": 2,
      "status": 1
    }
  ]
}
```

---

### 9. 热门活动类型

获取热门活动类型列表。

**请求**

```http
GET /api/v1/activity/types/hot
```

**响应示例**

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "code": "billiards",
      "name": "台球",
      "icon": "https://cdn.example.com/icon/billiards.png",
      "sortOrder": 1,
      "status": 1
    }
  ]
}
```

---

### 10. 分享活动

记录活动分享。

**请求**

```http
POST /api/v1/activity/share/{activityId}
```

**响应示例**

```json
{
  "code": 200,
  "msg": "分享成功",
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

---

## 集成测试用例

### 测试环境配置

```
Gateway:       http://localhost:8080
xypai-auth:    http://localhost:9211 (认证服务)
xypai-content: http://localhost:9403 (内容服务)
```

**依赖服务**: Nacos, Redis, MySQL

---

### 测试场景1: 发现主页 (AppContentDiscoveryTest)

测试发现主页的核心功能，包括三种Feed流和互动功能。

#### 1.1 用户SMS登录

```java
// 接口: POST /xypai-auth/auth/login/sms
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

#### 1.2 获取热门动态列表

```java
// 接口: GET /xypai-content/api/v1/content/feed/hot
// 请求头: Authorization: Bearer {token}
String feedUrl = GATEWAY_URL + "/xypai-content/api/v1/content/feed/hot?pageNum=1&pageSize=20";

// 响应
{
  "code": 200,
  "data": {
    "records": [...],
    "total": 100,
    "size": 20,
    "current": 1
  }
}
```

#### 1.3 获取关注动态列表

```java
// 接口: GET /xypai-content/api/v1/content/feed/follow
// 请求头: Authorization: Bearer {token} (必须)
String feedUrl = GATEWAY_URL + "/xypai-content/api/v1/content/feed/follow?pageNum=1&pageSize=20";

// 响应 (新用户通常为空)
{
  "code": 200,
  "data": {
    "records": [],
    "total": 0
  }
}
```

#### 1.4 获取同城动态列表

```java
// 接口: GET /xypai-content/api/v1/content/feed/local
// 请求头: Authorization: Bearer {token}
// 参数: latitude, longitude, radius (必传)
String feedUrl = GATEWAY_URL + "/xypai-content/api/v1/content/feed/local" +
    "?pageNum=1&pageSize=20&latitude=22.5431&longitude=114.0579&radius=5";

// 响应
{
  "code": 200,
  "data": {
    "records": [...],
    "total": 50
  }
}
```

#### 1.5 点赞动态

```java
// 接口: POST /xypai-content/api/v1/interaction/like
// 请求头: Authorization: Bearer {token}
Map<String, Object> likeRequest = new HashMap<>();
likeRequest.put("targetType", "feed");
likeRequest.put("targetId", 1001);
likeRequest.put("action", "like");

// 响应
{
  "code": 200,
  "data": {
    "success": true,
    "count": 129,
    "isActive": true
  }
}
```

#### 1.6 收藏动态

```java
// 接口: POST /xypai-content/api/v1/interaction/collect
Map<String, Object> collectRequest = new HashMap<>();
collectRequest.put("targetType", "feed");
collectRequest.put("targetId", 1001);
collectRequest.put("action", "collect");

// 响应
{
  "code": 200,
  "data": {
    "success": true,
    "count": 46,
    "isActive": true
  }
}
```

#### 1.7 分享动态

```java
// 接口: POST /xypai-content/api/v1/interaction/share
Map<String, Object> shareRequest = new HashMap<>();
shareRequest.put("targetType", "feed");
shareRequest.put("targetId", 1001);
shareRequest.put("shareChannel", "wechat");

// 响应
{
  "code": 200,
  "data": {
    "success": true,
    "count": 16
  }
}
```

#### 1.8 取消点赞

```java
// 接口: POST /xypai-content/api/v1/interaction/like
Map<String, Object> unlikeRequest = new HashMap<>();
unlikeRequest.put("targetType", "feed");
unlikeRequest.put("targetId", 1001);
unlikeRequest.put("action", "unlike");

// 响应
{
  "code": 200,
  "data": {
    "success": true,
    "count": 128,
    "isActive": false
  }
}
```

#### 1.9 取消收藏

```java
// 接口: POST /xypai-content/api/v1/interaction/collect
Map<String, Object> uncollectRequest = new HashMap<>();
uncollectRequest.put("targetType", "feed");
uncollectRequest.put("targetId", 1001);
uncollectRequest.put("action", "uncollect");

// 响应
{
  "code": 200,
  "data": {
    "success": true,
    "count": 45,
    "isActive": false
  }
}
```

---

### 测试场景2: 内容发布 (ContentPublishTest)

测试发布动态页面的所有功能，包括发布动态、话题查询、话题搜索。

#### 2.1 发布纯文字动态

```java
// 接口: POST /xypai-content/api/v1/content/publish
// 请求头: Authorization: Bearer {token}
Map<String, Object> publishRequest = new HashMap<>();
publishRequest.put("type", 1);  // 1=动态
publishRequest.put("content", "这是一条测试动态，来自集成测试。今天天气真好！ 😊");
publishRequest.put("visibility", 0);  // 0=公开

// 响应
{
  "code": 200,
  "msg": "发布成功",
  "data": 1001  // feedId
}
```

#### 2.2 发布带标题的动态

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

#### 2.3 发布带话题的动态

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

#### 2.4 发布带地点的动态

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

#### 2.5 获取热门话题列表

```java
// 接口: GET /xypai-content/api/v1/content/topics/hot
String topicsUrl = GATEWAY_URL + "/xypai-content/api/v1/content/topics/hot?page=1&pageSize=20";

// 响应
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 6001,
        "name": "探店日记",
        "description": "分享你的探店体验",
        "participantCount": 10000,
        "postCount": 50000,
        "isOfficial": true,
        "isHot": true
      }
    ],
    "total": 50
  }
}
```

#### 2.6 搜索话题

```java
// 接口: GET /xypai-content/api/v1/content/topics/search
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

#### 2.7 内容验证 - 空内容 (预期失败)

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

#### 2.8 内容验证 - 超长内容 (预期失败)

```java
// 接口: POST /xypai-content/api/v1/content/publish
// content 超过 1000 字符

// 响应 (预期错误)
{
  "code": 400,
  "msg": "内容长度超过限制"
}
```

#### 2.9 话题验证 - 超过5个话题 (预期失败)

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
# 进入内容服务目录
cd xypai-modules/xypai-content

# 运行发现主页测试
mvn test -Dtest=AppContentDiscoveryTest

# 运行内容发布测试
mvn test -Dtest=ContentPublishTest

# 运行所有测试
mvn test
```

**测试前置条件**:
1. 确保 Nacos、Redis、MySQL 已启动
2. 确保 xypai-auth (9211) 服务已启动
3. 确保 xypai-content (9403) 服务已启动
4. 确保 Gateway (8080) 已启动

---

**文档版本**: v1.0.0

**最后更新**: 2025-11-28
