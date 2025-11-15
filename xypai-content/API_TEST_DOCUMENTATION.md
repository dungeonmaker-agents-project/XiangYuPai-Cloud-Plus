# API Test Documentation

**Module**: xypai-content
**Version**: 1.0.0
**Date**: 2025-11-14
**Total Endpoints**: 13

---

## Table of Contents

1. [Test Environment Setup](#test-environment-setup)
2. [Authentication](#authentication)
3. [Feed Management Endpoints (5)](#feed-management-endpoints)
4. [Comment Endpoints (3)](#comment-endpoints)
5. [Interaction Endpoints (3)](#interaction-endpoints)
6. [Topic Endpoints (2)](#topic-endpoints)
7. [Report Endpoint (1)](#report-endpoint)
8. [Integration Test Scenarios](#integration-test-scenarios)
9. [Test Data](#test-data)
10. [Postman Collection](#postman-collection)

---

## Test Environment Setup

### Prerequisites

```bash
# 1. Start MySQL
mysql -u root -p

# 2. Initialize database
mysql> source xypai-content/sql/xypai_content.sql

# 3. Start Redis
redis-server

# 4. Start Nacos (Service Registry)
cd nacos/bin
./startup.sh -m standalone

# 5. Start xypai-content service
cd xypai-content
mvn spring-boot:run
```

### Service Configuration

- **Base URL**: `http://localhost:9403`
- **Service Name**: `xypai-content`
- **Port**: 9403
- **Database**: `xypai_content`
- **Redis**: `localhost:6379`

### Environment Variables

```properties
# application.yml
server.port=9403
spring.datasource.url=jdbc:mysql://localhost:3306/xypai_content
spring.redis.host=localhost
spring.redis.port=6379
```

---

## Authentication

All endpoints require Sa-Token authentication except public feed list.

### Get Access Token

```bash
POST http://localhost:9401/api/v1/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123"
}

Response:
{
  "code": 200,
  "msg": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1001
  }
}
```

### Use Token in Requests

```bash
# Add header to all requests:
Satoken: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## Feed Management Endpoints

### 1. Get Feed List

**Endpoint**: `GET /api/v1/content/feed/{tabType}`

**Tab Types**:
- `recommend` - Recommended feeds (default sort by time)
- `follow` - Following users' feeds
- `hot` - Hot feeds (sorted by hot score with time decay)
- `local` - Nearby feeds (requires latitude/longitude)

#### Test Case 1.1: Get Recommended Feeds

```bash
GET http://localhost:9403/api/v1/content/feed/recommend?page=1&pageSize=10

Response:
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1001,
        "userId": 2001,
        "userInfo": {
          "id": 2001,
          "nickname": "张三",
          "avatar": "https://cdn.example.com/avatar1.jpg",
          "gender": "male",
          "age": 25,
          "isRealVerified": true,
          "isGodVerified": false,
          "isVip": true,
          "isFollowed": false,
          "isPopular": true
        },
        "type": 1,
        "typeDesc": "动态",
        "title": "今天天气真好",
        "content": "去公园散步，心情很愉快！",
        "summary": "去公园散步，心情很愉快！",
        "mediaList": [
          {
            "id": 3001,
            "url": "https://cdn.example.com/image1.jpg",
            "type": "image",
            "width": 1080,
            "height": 1920
          }
        ],
        "topicList": [
          {
            "name": "探店日记",
            "isOfficial": true,
            "postCount": 1250
          }
        ],
        "locationName": "朝阳公园",
        "cityId": 110105,
        "likeCount": 120,
        "commentCount": 35,
        "shareCount": 8,
        "collectCount": 42,
        "viewCount": 1580,
        "isLiked": false,
        "isCollected": false,
        "createdAt": "2025-11-14 10:30:00"
      }
    ],
    "total": 150,
    "current": 1,
    "size": 10
  }
}
```

**Assertions**:
- ✅ Response code is 200
- ✅ Data contains `records` array
- ✅ Each record has `typeDesc`, `summary`, `cityId` fields
- ✅ UserInfo has all required fields (gender, age, verification badges)
- ✅ Pagination info is correct

#### Test Case 1.2: Get Hot Feeds

```bash
GET http://localhost:9403/api/v1/content/feed/hot?page=1&pageSize=10

Response: Same structure as recommend, but sorted by hot score

Hot Score Formula:
baseScore = likeCount * 1 + commentCount * 2 + shareCount * 3 + collectCount * 2
timeFactor = Math.pow(0.5, hoursSinceCreated / 24)
hotScore = baseScore * timeFactor
```

**Assertions**:
- ✅ Feeds are sorted by hot score (higher scores first)
- ✅ Recent feeds with high engagement rank higher
- ✅ Old feeds with low engagement rank lower

#### Test Case 1.3: Get Local Feeds

```bash
GET http://localhost:9403/api/v1/content/feed/local?page=1&pageSize=10&latitude=39.9042&longitude=116.4074&radius=5

Query Parameters:
- latitude: 39.9042 (required for local tab)
- longitude: 116.4074 (required for local tab)
- radius: 5 (default 5km, optional)
```

**Assertions**:
- ✅ Returns feeds within specified radius
- ✅ Default radius is 5km if not specified
- ✅ Returns 400 error if latitude/longitude missing

#### Test Case 1.4: Get Following Feeds

```bash
GET http://localhost:9403/api/v1/content/feed/follow?page=1&pageSize=10
Satoken: {token}

Response: Feeds from users that current user follows
```

**Assertions**:
- ✅ Requires authentication
- ✅ Returns only feeds from following users
- ✅ Returns empty list if no followings

---

### 2. Get Feed Detail

**Endpoint**: `GET /api/v1/content/detail/{feedId}`

#### Test Case 2.1: Get Public Feed Detail

```bash
GET http://localhost:9403/api/v1/content/detail/1001

Response:
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1001,
    "userId": 2001,
    "userInfo": {
      "id": 2001,
      "nickname": "张三",
      "avatar": "https://cdn.example.com/avatar1.jpg",
      "gender": "male",
      "age": 25,
      "isRealVerified": true,
      "isGodVerified": false,
      "isVip": true,
      "isFollowed": false
    },
    "type": 1,
    "typeDesc": "动态",
    "title": "今天天气真好",
    "content": "去公园散步，心情很愉快！详细内容...",
    "summary": "去公园散步，心情很愉快！详细内容...",
    "mediaList": [...],
    "topicList": [...],
    "locationName": "朝阳公园",
    "locationAddress": "北京市朝阳区朝阳公园南路1号",
    "distance": 2.5,
    "cityId": 110105,
    "likeCount": 120,
    "commentCount": 35,
    "shareCount": 8,
    "collectCount": 42,
    "viewCount": 1581,
    "isLiked": false,
    "isCollected": false,
    "canEdit": false,
    "canDelete": false,
    "createdAt": "2025-11-14 10:30:00"
  }
}
```

**Assertions**:
- ✅ Response includes all detail fields
- ✅ Has `distance`, `locationAddress` (not in list VO)
- ✅ Has `canEdit`, `canDelete` permissions
- ✅ viewCount increments after each view

#### Test Case 2.2: Get Non-Existent Feed

```bash
GET http://localhost:9403/api/v1/content/detail/999999

Response:
{
  "code": 500,
  "msg": "动态不存在或已删除"
}
```

**Assertions**:
- ✅ Returns 500 error
- ✅ Error message is clear

#### Test Case 2.3: Get Private Feed (Not Owner)

```bash
GET http://localhost:9403/api/v1/content/detail/1002
Satoken: {other_user_token}

Response:
{
  "code": 500,
  "msg": "无权查看此动态"
}
```

**Assertions**:
- ✅ Returns permission error
- ✅ Only owner can view private feeds

---

### 3. Publish Feed

**Endpoint**: `POST /api/v1/content/publish`

#### Test Case 3.1: Publish Text-Only Feed

```bash
POST http://localhost:9403/api/v1/content/publish
Satoken: {token}
Content-Type: application/json

{
  "type": 1,
  "content": "今天去了一家很棒的咖啡店！",
  "visibility": 0
}

Response:
{
  "code": 200,
  "msg": "发布成功",
  "data": {
    "feedId": 1010
  }
}
```

**Assertions**:
- ✅ Returns new feed ID
- ✅ Feed is created with correct data
- ✅ Default visibility is 0 (public)

#### Test Case 3.2: Publish Feed with Media

```bash
POST http://localhost:9403/api/v1/content/publish
Satoken: {token}
Content-Type: application/json

{
  "type": 1,
  "title": "美食分享",
  "content": "这家店的拉面超级好吃！强烈推荐！",
  "mediaIds": [5001, 5002, 5003],
  "topicNames": ["探店日记", "美食推荐"],
  "locationName": "拉面小店",
  "locationAddress": "北京市朝阳区xx街xx号",
  "longitude": 116.4074,
  "latitude": 39.9042,
  "visibility": 0
}

Response:
{
  "code": 200,
  "msg": "发布成功",
  "data": {
    "feedId": 1011
  }
}
```

**Assertions**:
- ✅ Feed created with media associations
- ✅ Topics created or updated (post count incremented)
- ✅ Location data saved correctly

#### Test Case 3.3: Publish with Validation Errors

```bash
POST http://localhost:9403/api/v1/content/publish
Satoken: {token}
Content-Type: application/json

{
  "type": 1,
  "content": ""
}

Response:
{
  "code": 400,
  "msg": "内容不能为空"
}
```

**Assertions**:
- ✅ Validates content length (1-1000 chars)
- ✅ Validates title length (0-50 chars)
- ✅ Validates media count (max 9)
- ✅ Validates topic count (max 5)

---

### 4. Delete Feed

**Endpoint**: `DELETE /api/v1/content/{feedId}`

#### Test Case 4.1: Delete Own Feed

```bash
DELETE http://localhost:9403/api/v1/content/1010
Satoken: {owner_token}

Response:
{
  "code": 200,
  "msg": "删除成功"
}
```

**Assertions**:
- ✅ Feed is soft deleted (deleted=1)
- ✅ Cache is cleared
- ✅ Feed no longer appears in lists

#### Test Case 4.2: Delete Others' Feed

```bash
DELETE http://localhost:9403/api/v1/content/1010
Satoken: {other_user_token}

Response:
{
  "code": 500,
  "msg": "无权删除此动态"
}
```

**Assertions**:
- ✅ Returns permission error
- ✅ Feed is not deleted

---

## Comment Endpoints

### 5. Get Comment List

**Endpoint**: `GET /api/v1/content/comments/{feedId}`

#### Test Case 5.1: Get Comments with Default Sort

```bash
GET http://localhost:9403/api/v1/content/comments/1001?page=1&pageSize=10&sortType=hot

Query Parameters:
- page: 1 (default)
- pageSize: 10 (default 20)
- sortType: hot (options: time/hot/like, default: hot)

Response:
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 4001,
        "feedId": 1001,
        "userId": 2002,
        "userInfo": {
          "id": 2002,
          "nickname": "李四",
          "avatar": "https://cdn.example.com/avatar2.jpg",
          "isFollowed": false
        },
        "content": "说得太对了！",
        "likeCount": 15,
        "isLiked": false,
        "isTop": false,
        "totalReplies": 3,
        "hasMoreReplies": false,
        "replies": [
          {
            "id": 4002,
            "parentId": 4001,
            "userId": 2003,
            "replyToUserId": 2002,
            "userInfo": {...},
            "replyToUserInfo": {...},
            "content": "同意！",
            "likeCount": 5,
            "isLiked": false,
            "createdAt": "2025-11-14 11:15:00"
          }
        ],
        "createdAt": "2025-11-14 11:00:00"
      }
    ],
    "total": 35,
    "current": 1,
    "size": 10
  }
}
```

**Assertions**:
- ✅ Comments sorted by specified type
- ✅ Top comments appear first
- ✅ Each comment has `totalReplies` and `hasMoreReplies`
- ✅ Replies nested correctly (max 3 shown)

#### Test Case 5.2: Sort by Time

```bash
GET http://localhost:9403/api/v1/content/comments/1001?sortType=time
```

**Assertions**:
- ✅ Comments sorted by creation time (newest first)

#### Test Case 5.3: Sort by Like Count

```bash
GET http://localhost:9403/api/v1/content/comments/1001?sortType=like
```

**Assertions**:
- ✅ Comments sorted by like count (highest first)

---

### 6. Post Comment

**Endpoint**: `POST /api/v1/content/comment`

#### Test Case 6.1: Post Top-Level Comment

```bash
POST http://localhost:9403/api/v1/content/comment
Satoken: {token}
Content-Type: application/json

{
  "feedId": 1001,
  "content": "这个地方我也去过，确实不错！"
}

Response:
{
  "code": 200,
  "msg": "评论成功",
  "data": {
    "commentId": 4010
  }
}
```

**Assertions**:
- ✅ Comment created successfully
- ✅ Feed's commentCount incremented
- ✅ Returns new comment ID

#### Test Case 6.2: Post Reply

```bash
POST http://localhost:9403/api/v1/content/comment
Satoken: {token}
Content-Type: application/json

{
  "feedId": 1001,
  "content": "我也这么觉得！",
  "parentId": 4001,
  "replyToUserId": 2002
}

Response:
{
  "code": 200,
  "msg": "回复成功",
  "data": {
    "commentId": 4011
  }
}
```

**Assertions**:
- ✅ Reply created with correct parent
- ✅ Parent comment's replyCount incremented
- ✅ Notification sent to parent comment author

#### Test Case 6.3: Comment with Invalid Content

```bash
POST http://localhost:9403/api/v1/content/comment
Satoken: {token}
Content-Type: application/json

{
  "feedId": 1001,
  "content": ""
}

Response:
{
  "code": 400,
  "msg": "评论内容不能为空"
}
```

**Assertions**:
- ✅ Validates content length (1-500 chars)
- ✅ Returns validation error

---

### 7. Delete Comment

**Endpoint**: `DELETE /api/v1/content/comment`

#### Test Case 7.1: Delete Own Comment

```bash
DELETE http://localhost:9403/api/v1/content/comment?commentId=4010
Satoken: {owner_token}

Response:
{
  "code": 200,
  "msg": "删除成功"
}
```

**Assertions**:
- ✅ Comment soft deleted (deleted=1)
- ✅ Feed's commentCount decremented
- ✅ If has replies, they remain visible

#### Test Case 7.2: Delete Others' Comment

```bash
DELETE http://localhost:9403/api/v1/content/comment?commentId=4010
Satoken: {other_user_token}

Response:
{
  "code": 500,
  "msg": "无权删除此评论"
}
```

**Assertions**:
- ✅ Returns permission error
- ✅ Comment not deleted

---

## Interaction Endpoints

### 8. Like

**Endpoint**: `POST /api/v1/interaction/like`

#### Test Case 8.1: Like Feed

```bash
POST http://localhost:9403/api/v1/interaction/like
Satoken: {token}
Content-Type: application/json

{
  "targetType": "feed",
  "targetId": 1001
}

Response:
{
  "code": 200,
  "msg": "点赞成功",
  "data": {
    "isLiked": true,
    "likeCount": 121
  }
}
```

**Assertions**:
- ✅ Like record created in database
- ✅ Feed's likeCount incremented
- ✅ Returns updated like status

#### Test Case 8.2: Unlike Feed

```bash
POST http://localhost:9403/api/v1/interaction/like
Satoken: {token}
Content-Type: application/json

{
  "targetType": "feed",
  "targetId": 1001
}

Response:
{
  "code": 200,
  "msg": "取消点赞成功",
  "data": {
    "isLiked": false,
    "likeCount": 120
  }
}
```

**Assertions**:
- ✅ Like record deleted
- ✅ Feed's likeCount decremented
- ✅ Idempotent operation

#### Test Case 8.3: Like Comment

```bash
POST http://localhost:9403/api/v1/interaction/like
Satoken: {token}
Content-Type: application/json

{
  "targetType": "comment",
  "targetId": 4001
}

Response:
{
  "code": 200,
  "msg": "点赞成功",
  "data": {
    "isLiked": true,
    "likeCount": 16
  }
}
```

**Assertions**:
- ✅ Comment's likeCount incremented
- ✅ Works for both feed and comment

---

### 9. Collect

**Endpoint**: `POST /api/v1/interaction/collect`

#### Test Case 9.1: Collect Feed

```bash
POST http://localhost:9403/api/v1/interaction/collect
Satoken: {token}
Content-Type: application/json

{
  "targetType": "feed",
  "targetId": 1001
}

Response:
{
  "code": 200,
  "msg": "收藏成功",
  "data": {
    "isCollected": true,
    "collectCount": 43
  }
}
```

**Assertions**:
- ✅ Collection record created
- ✅ Feed's collectCount incremented
- ✅ Returns updated collection status

#### Test Case 9.2: Uncollect Feed

```bash
POST http://localhost:9403/api/v1/interaction/collect
Satoken: {token}
Content-Type: application/json

{
  "targetType": "feed",
  "targetId": 1001
}

Response:
{
  "code": 200,
  "msg": "取消收藏成功",
  "data": {
    "isCollected": false,
    "collectCount": 42
  }
}
```

**Assertions**:
- ✅ Collection record deleted
- ✅ Feed's collectCount decremented
- ✅ Idempotent operation

---

### 10. Share

**Endpoint**: `POST /api/v1/interaction/share`

#### Test Case 10.1: Share to WeChat

```bash
POST http://localhost:9403/api/v1/interaction/share
Satoken: {token}
Content-Type: application/json

{
  "targetType": "feed",
  "targetId": 1001,
  "shareChannel": "wechat"
}

Response:
{
  "code": 200,
  "msg": "分享成功",
  "data": {
    "shareCount": 9
  }
}
```

**Share Channels**:
- `wechat` - WeChat
- `moments` - WeChat Moments
- `qq` - QQ
- `qzone` - QQ Zone
- `weibo` - Weibo
- `copy_link` - Copy Link

**Assertions**:
- ✅ Share record created with channel
- ✅ Feed's shareCount incremented
- ✅ Uses request body (not query params)
- ✅ Validates share channel enum

#### Test Case 10.2: Share with Invalid Channel

```bash
POST http://localhost:9403/api/v1/interaction/share
Satoken: {token}
Content-Type: application/json

{
  "targetType": "feed",
  "targetId": 1001,
  "shareChannel": "invalid"
}

Response:
{
  "code": 400,
  "msg": "分享渠道无效"
}
```

**Assertions**:
- ✅ Validates share channel
- ✅ Returns validation error

---

## Topic Endpoints

### 11. Get Hot Topics

**Endpoint**: `GET /api/v1/content/topics/hot`

#### Test Case 11.1: Get Hot Topics List

```bash
GET http://localhost:9403/api/v1/content/topics/hot?page=1&pageSize=20

Response:
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 101,
        "name": "王者荣耀",
        "description": "王者荣耀相关内容",
        "coverImage": "https://cdn.example.com/topic1.jpg",
        "participantCount": 15230,
        "postCount": 5678,
        "isOfficial": true,
        "isHot": true
      },
      {
        "id": 102,
        "name": "美食推荐",
        "description": "发现身边的美食",
        "coverImage": "https://cdn.example.com/topic2.jpg",
        "participantCount": 12450,
        "postCount": 2340,
        "isOfficial": true,
        "isHot": true
      }
    ],
    "total": 50,
    "current": 1,
    "size": 20
  }
}
```

**Assertions**:
- ✅ Returns only hot topics (isHot=1)
- ✅ Sorted by postCount DESC, participantCount DESC
- ✅ Results cached in Redis (1 hour TTL)
- ✅ Pagination works correctly

#### Test Case 11.2: Cache Validation

```bash
# First request - cache miss
GET http://localhost:9403/api/v1/content/topics/hot?page=1&pageSize=20
# Check logs: "Cache miss, querying database"

# Second request - cache hit
GET http://localhost:9403/api/v1/content/topics/hot?page=1&pageSize=20
# Check logs: "从缓存获取热门话题"
```

**Assertions**:
- ✅ First request queries database
- ✅ Second request uses cache
- ✅ Cache expires after 1 hour

---

### 12. Search Topics

**Endpoint**: `GET /api/v1/content/topics/search`

#### Test Case 12.1: Search by Keyword

```bash
GET http://localhost:9403/api/v1/content/topics/search?keyword=美食&page=1&pageSize=20

Response:
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 102,
        "name": "美食推荐",
        "description": "发现身边的美食",
        "coverImage": "https://cdn.example.com/topic2.jpg",
        "participantCount": 12450,
        "postCount": 2340,
        "isOfficial": true,
        "isHot": true
      },
      {
        "id": 105,
        "name": "探店美食",
        "description": "分享探店体验和美食",
        "coverImage": null,
        "participantCount": 850,
        "postCount": 230,
        "isOfficial": false,
        "isHot": false
      }
    ],
    "total": 2,
    "current": 1,
    "size": 20
  }
}
```

**Assertions**:
- ✅ Searches in both name and description
- ✅ LIKE search pattern: `%keyword%`
- ✅ Returns empty list if no matches
- ✅ Keyword length validated (1-20 chars)

#### Test Case 12.2: Empty Keyword

```bash
GET http://localhost:9403/api/v1/content/topics/search?keyword=

Response:
{
  "code": 400,
  "msg": "关键词不能为空"
}
```

**Assertions**:
- ✅ Validates keyword presence
- ✅ Validates keyword length

---

## Report Endpoint

### 13. Submit Report

**Endpoint**: `POST /api/v1/content/report`

#### Test Case 13.1: Report Feed

```bash
POST http://localhost:9403/api/v1/content/report
Satoken: {token}
Content-Type: application/json

{
  "targetType": "feed",
  "targetId": 1001,
  "reasonType": "spam",
  "description": "这条动态涉嫌广告推广",
  "evidenceImages": [
    "https://cdn.example.com/evidence1.jpg",
    "https://cdn.example.com/evidence2.jpg"
  ]
}

Response:
{
  "code": 200,
  "msg": "已收到您的举报,我们会尽快处理",
  "data": {
    "reportId": 9001,
    "status": "pending",
    "createdAt": "2025-11-14 14:30:00"
  }
}
```

**Target Types**:
- `feed` - Report feed
- `comment` - Report comment
- `user` - Report user

**Reason Types**:
- `harassment` - Harassment/insult
- `pornography` - Pornography/vulgar content
- `fraud` - Fraud/scam
- `illegal` - Illegal content
- `spam` - Spam/ads
- `other` - Other reasons

**Assertions**:
- ✅ Report created with status "pending"
- ✅ Returns report ID and timestamp
- ✅ Evidence images stored as JSON array
- ✅ Rate limited to 10 reports/minute per user

#### Test Case 13.2: Duplicate Report (Within 24 Hours)

```bash
POST http://localhost:9403/api/v1/content/report
Satoken: {token}
Content-Type: application/json

{
  "targetType": "feed",
  "targetId": 1001,
  "reasonType": "spam",
  "description": "再次举报同一内容"
}

Response:
{
  "code": 500,
  "msg": "24小时内已举报过该内容,请勿重复举报"
}
```

**Assertions**:
- ✅ Prevents duplicate reports within 24 hours
- ✅ Checks by (userId, targetType, targetId, createdAt)
- ✅ Returns clear error message

#### Test Case 13.3: Report with Validation Errors

```bash
POST http://localhost:9403/api/v1/content/report
Satoken: {token}
Content-Type: application/json

{
  "targetType": "invalid",
  "targetId": 1001,
  "reasonType": "spam"
}

Response:
{
  "code": 400,
  "msg": "目标类型无效"
}
```

**Validations**:
- ✅ targetType: feed/comment/user
- ✅ reasonType: harassment/pornography/fraud/illegal/spam/other
- ✅ description: 0-200 chars (optional)
- ✅ evidenceImages: max 3 images (optional)

#### Test Case 13.4: Report Comment

```bash
POST http://localhost:9403/api/v1/content/report
Satoken: {token}
Content-Type: application/json

{
  "targetType": "comment",
  "targetId": 4001,
  "reasonType": "harassment",
  "description": "评论内容含有人身攻击"
}

Response:
{
  "code": 200,
  "msg": "已收到您的举报,我们会尽快处理",
  "data": {
    "reportId": 9002,
    "status": "pending",
    "createdAt": "2025-11-14 14:35:00"
  }
}
```

**Assertions**:
- ✅ Can report comments
- ✅ Same validation rules apply

#### Test Case 13.5: Report User

```bash
POST http://localhost:9403/api/v1/content/report
Satoken: {token}
Content-Type: application/json

{
  "targetType": "user",
  "targetId": 2005,
  "reasonType": "fraud",
  "description": "该用户涉嫌诈骗行为",
  "evidenceImages": [
    "https://cdn.example.com/fraud1.jpg"
  ]
}

Response:
{
  "code": 200,
  "msg": "已收到您的举报,我们会尽快处理",
  "data": {
    "reportId": 9003,
    "status": "pending",
    "createdAt": "2025-11-14 14:40:00"
  }
}
```

**Assertions**:
- ✅ Can report users
- ✅ Evidence images required for serious violations

---

## Integration Test Scenarios

### Scenario 1: Complete User Flow - Publish and Interact

```bash
# 1. User logs in
POST /api/v1/auth/login
{
  "username": "testuser",
  "password": "password123"
}
# Save token

# 2. User publishes a feed
POST /api/v1/content/publish
Satoken: {token}
{
  "type": 1,
  "content": "第一次发动态,大家多多关照!",
  "topicNames": ["新人报道"]
}
# Save feedId

# 3. Another user likes the feed
POST /api/v1/interaction/like
Satoken: {other_token}
{
  "targetType": "feed",
  "targetId": {feedId}
}
# Verify likeCount = 1

# 4. Another user comments
POST /api/v1/content/comment
Satoken: {other_token}
{
  "feedId": {feedId},
  "content": "欢迎欢迎!"
}
# Save commentId

# 5. Original user replies
POST /api/v1/content/comment
Satoken: {token}
{
  "feedId": {feedId},
  "content": "谢谢!",
  "parentId": {commentId},
  "replyToUserId": {otherUserId}
}

# 6. Get feed detail to verify
GET /api/v1/content/detail/{feedId}
# Verify: likeCount=1, commentCount=2

# 7. User collects the feed
POST /api/v1/interaction/collect
Satoken: {token}
{
  "targetType": "feed",
  "targetId": {feedId}
}
# Verify collectCount = 1

# 8. User shares the feed
POST /api/v1/interaction/share
Satoken: {token}
{
  "targetType": "feed",
  "targetId": {feedId},
  "shareChannel": "moments"
}
# Verify shareCount = 1
```

**Expected Results**:
- ✅ Feed published successfully
- ✅ Like count incremented
- ✅ Comment count incremented
- ✅ Reply nested under parent
- ✅ Collect count incremented
- ✅ Share count incremented
- ✅ All counts match in detail endpoint

---

### Scenario 2: Hot Feed Algorithm Validation

```bash
# Create test feeds with different engagement
# Feed A: Old but high engagement
POST /api/v1/content/publish
{
  "content": "Feed A - created 48 hours ago"
}
# Manually set createdAt to 48 hours ago
# Add engagement: 100 likes, 50 comments, 10 shares, 20 collects

# Feed B: Recent with moderate engagement
POST /api/v1/content/publish
{
  "content": "Feed B - created 1 hour ago"
}
# Add engagement: 20 likes, 10 comments, 2 shares, 5 collects

# Calculate expected scores:
# Feed A:
#   baseScore = 100*1 + 50*2 + 10*3 + 20*2 = 270
#   timeFactor = Math.pow(0.5, 48/24) = 0.25
#   hotScore = 270 * 0.25 = 67.5

# Feed B:
#   baseScore = 20*1 + 10*2 + 2*3 + 5*2 = 56
#   timeFactor = Math.pow(0.5, 1/24) = 0.971
#   hotScore = 56 * 0.971 = 54.4

# Get hot feeds
GET /api/v1/content/feed/hot?page=1&pageSize=10

# Verify:
# Feed A should rank higher than Feed B (67.5 > 54.4)
```

**Expected Results**:
- ✅ Hot score calculated correctly
- ✅ Feeds sorted by hot score descending
- ✅ Time decay applied properly
- ✅ Recent high-engagement feeds rank higher than old low-engagement

---

### Scenario 3: Spatial Query Validation

```bash
# Create feeds at different locations
# Feed 1: Tiananmen Square (39.9042, 116.4074)
POST /api/v1/content/publish
{
  "content": "天安门广场打卡",
  "latitude": 39.9042,
  "longitude": 116.4074
}

# Feed 2: Wangfujing (39.9150, 116.4074) - ~1.2km away
POST /api/v1/content/publish
{
  "content": "王府井逛街",
  "latitude": 39.9150,
  "longitude": 116.4074
}

# Feed 3: Olympic Park (40.0092, 116.3972) - ~12km away
POST /api/v1/content/publish
{
  "content": "奥林匹克公园",
  "latitude": 40.0092,
  "longitude": 116.3972
}

# Query from Tiananmen with 5km radius
GET /api/v1/content/feed/local?latitude=39.9042&longitude=116.4074&radius=5

# Verify:
# Should return Feed 1 and Feed 2 (within 5km)
# Should NOT return Feed 3 (beyond 5km)
```

**Expected Results**:
- ✅ Returns feeds within radius
- ✅ Excludes feeds outside radius
- ✅ Distance calculated correctly using ST_Distance_Sphere

---

### Scenario 4: Topic Creation and Association

```bash
# Publish feed with new topic
POST /api/v1/content/publish
{
  "content": "分享一个新话题",
  "topicNames": ["新话题测试", "探店日记"]
}

# Verify:
# 1. New topic "新话题测试" created if not exists
# 2. Existing topic "探店日记" postCount incremented
# 3. feed_topic associations created

# Search for new topic
GET /api/v1/content/topics/search?keyword=新话题

# Verify:
# Topic found with postCount = 1
```

**Expected Results**:
- ✅ New topics created automatically
- ✅ Existing topics updated
- ✅ Associations created correctly
- ✅ Topics searchable immediately

---

### Scenario 5: Permission Validation

```bash
# User A publishes private feed
POST /api/v1/content/publish
Satoken: {tokenA}
{
  "content": "私密动态",
  "visibility": 2
}
# Save feedId

# User B tries to view
GET /api/v1/content/detail/{feedId}
Satoken: {tokenB}

# Verify:
# Returns error: "无权查看此动态"

# User A views own private feed
GET /api/v1/content/detail/{feedId}
Satoken: {tokenA}

# Verify:
# Returns feed details successfully

# User B tries to delete User A's feed
DELETE /api/v1/content/{feedId}
Satoken: {tokenB}

# Verify:
# Returns error: "无权删除此动态"
```

**Expected Results**:
- ✅ Private feeds only visible to owner
- ✅ Users cannot delete others' content
- ✅ Permission errors clear and consistent

---

## Test Data

### Sample Users

```sql
-- Insert test users (requires xypai-user service)
INSERT INTO user (id, username, nickname, avatar, gender, age, phone, is_real_verified, is_god_verified, is_vip)
VALUES
  (2001, 'zhangsan', '张三', 'https://cdn.example.com/avatar1.jpg', 'male', 25, '13800138001', 1, 0, 1),
  (2002, 'lisi', '李四', 'https://cdn.example.com/avatar2.jpg', 'female', 23, '13800138002', 1, 1, 0),
  (2003, 'wangwu', '王五', 'https://cdn.example.com/avatar3.jpg', 'male', 28, '13800138003', 0, 0, 0);
```

### Sample Feeds

```sql
-- Insert test feeds
INSERT INTO feed (id, user_id, type, title, content, location_name, latitude, longitude, city_id, like_count, comment_count, share_count, collect_count, view_count, created_at, created_timestamp)
VALUES
  (1001, 2001, 1, '今天天气真好', '去公园散步，心情很愉快！', '朝阳公园', 39.9042, 116.4074, 110105, 120, 35, 8, 42, 1580, '2025-11-14 10:30:00', UNIX_TIMESTAMP('2025-11-14 10:30:00') * 1000),
  (1002, 2002, 2, '周末爬山活动', '周末一起去香山爬山！', '香山公园', 39.9950, 116.1883, 110108, 85, 22, 15, 30, 980, '2025-11-13 15:20:00', UNIX_TIMESTAMP('2025-11-13 15:20:00') * 1000),
  (1003, 2003, 1, '美食分享', '这家店的拉面超级好吃！', '拉面小店', 39.9150, 116.4074, 110105, 200, 68, 25, 95, 3200, '2025-11-12 18:45:00', UNIX_TIMESTAMP('2025-11-12 18:45:00') * 1000);
```

### Sample Topics

```sql
-- Topics already inserted in xypai_content.sql
-- Additional topics for testing
INSERT INTO topic (name, description, is_official, is_hot, post_count, participant_count)
VALUES
  ('新人报道', '新用户第一次发动态', 0, 0, 120, 120),
  ('日常分享', '分享日常生活点滴', 1, 1, 3500, 2800);
```

### Sample Comments

```sql
-- Insert test comments
INSERT INTO comment (id, feed_id, user_id, content, parent_id, like_count, reply_count, created_at)
VALUES
  (4001, 1001, 2002, '说得太对了！', NULL, 15, 3, '2025-11-14 11:00:00'),
  (4002, 1001, 2003, '同意！', 4001, 5, 0, '2025-11-14 11:15:00'),
  (4003, 1001, 2001, '谢谢支持', 4001, 2, 0, '2025-11-14 11:20:00');
```

---

## Postman Collection

### Collection Structure

```
xypai-content API Tests
├── Authentication
│   └── Login
├── Feed Management
│   ├── Get Recommend Feeds
│   ├── Get Hot Feeds
│   ├── Get Local Feeds
│   ├── Get Following Feeds
│   ├── Get Feed Detail
│   ├── Publish Feed
│   └── Delete Feed
├── Comment Management
│   ├── Get Comments (Hot)
│   ├── Get Comments (Time)
│   ├── Get Comments (Like)
│   ├── Post Comment
│   ├── Post Reply
│   └── Delete Comment
├── Interactions
│   ├── Like Feed
│   ├── Unlike Feed
│   ├── Like Comment
│   ├── Collect Feed
│   ├── Uncollect Feed
│   ├── Share to WeChat
│   └── Share to Moments
├── Topics
│   ├── Get Hot Topics
│   └── Search Topics
└── Report
    ├── Report Feed (Spam)
    ├── Report Comment (Harassment)
    ├── Report User (Fraud)
    └── Duplicate Report (Error)
```

### Environment Variables

```json
{
  "baseUrl": "http://localhost:9403",
  "authBaseUrl": "http://localhost:9401",
  "token": "",
  "userId": "",
  "feedId": "",
  "commentId": ""
}
```

### Pre-request Script (Global)

```javascript
// Auto-refresh token if expired
if (pm.environment.get("tokenExpiry")) {
  const expiry = new Date(pm.environment.get("tokenExpiry"));
  const now = new Date();

  if (now >= expiry) {
    // Token expired, re-login
    pm.sendRequest({
      url: pm.environment.get("authBaseUrl") + "/api/v1/auth/login",
      method: "POST",
      header: {
        "Content-Type": "application/json"
      },
      body: {
        mode: "raw",
        raw: JSON.stringify({
          username: "testuser",
          password: "password123"
        })
      }
    }, function(err, res) {
      if (!err && res.code == 200) {
        pm.environment.set("token", res.json().data.token);
        pm.environment.set("userId", res.json().data.userId);

        // Set expiry to 24 hours from now
        const newExpiry = new Date();
        newExpiry.setHours(newExpiry.getHours() + 24);
        pm.environment.set("tokenExpiry", newExpiry.toISOString());
      }
    });
  }
}
```

### Test Script Template

```javascript
// Test: Response code is 200
pm.test("Status code is 200", function() {
  pm.response.to.have.status(200);
});

// Test: Response has success code
pm.test("Response code is 200", function() {
  const jsonData = pm.response.json();
  pm.expect(jsonData.code).to.eql(200);
});

// Test: Response has data
pm.test("Response has data", function() {
  const jsonData = pm.response.json();
  pm.expect(jsonData.data).to.exist;
});

// Save variables for next requests
const jsonData = pm.response.json();
if (jsonData.data.feedId) {
  pm.environment.set("feedId", jsonData.data.feedId);
}
```

---

## Performance Testing

### Load Test Scenarios

#### 1. Feed List Endpoint

```bash
# Using Apache Bench
ab -n 1000 -c 10 http://localhost:9403/api/v1/content/feed/recommend?page=1&pageSize=10

# Expected:
# - 99% requests < 200ms
# - 0% errors
# - Throughput > 50 req/sec
```

#### 2. Hot Topics Cache

```bash
# First request (cache miss)
curl -w "@curl-format.txt" http://localhost:9403/api/v1/content/topics/hot

# Second request (cache hit)
curl -w "@curl-format.txt" http://localhost:9403/api/v1/content/topics/hot

# Expected:
# - Cache hit < 20ms
# - Cache miss < 100ms
```

#### 3. Spatial Query Performance

```bash
# Query with 5km radius
ab -n 100 -c 5 "http://localhost:9403/api/v1/content/feed/local?latitude=39.9042&longitude=116.4074&radius=5"

# Expected:
# - Average < 300ms
# - Uses spatial index
```

---

## Error Scenarios

### 1. Authentication Errors

```bash
# No token
GET http://localhost:9403/api/v1/content/publish
# Expected: 401 Unauthorized

# Invalid token
GET http://localhost:9403/api/v1/content/publish
Satoken: invalid_token
# Expected: 401 Invalid token

# Expired token
GET http://localhost:9403/api/v1/content/publish
Satoken: {expired_token}
# Expected: 401 Token expired
```

### 2. Validation Errors

```bash
# Empty content
POST /api/v1/content/publish
{
  "type": 1,
  "content": ""
}
# Expected: 400 内容不能为空

# Content too long
POST /api/v1/content/publish
{
  "type": 1,
  "content": "..." (1001 chars)
}
# Expected: 400 内容长度不能超过1000字符

# Too many media
POST /api/v1/content/publish
{
  "type": 1,
  "content": "test",
  "mediaIds": [1,2,3,4,5,6,7,8,9,10]
}
# Expected: 400 最多上传9张图片
```

### 3. Permission Errors

```bash
# Delete others' feed
DELETE /api/v1/content/1001
Satoken: {other_user_token}
# Expected: 500 无权删除此动态

# View private feed
GET /api/v1/content/detail/1002
Satoken: {other_user_token}
# Expected: 500 无权查看此动态
```

### 4. Rate Limit Errors

```bash
# Submit 11 reports in 1 minute
for i in {1..11}; do
  curl -X POST http://localhost:9403/api/v1/content/report \
    -H "Satoken: {token}" \
    -H "Content-Type: application/json" \
    -d '{"targetType":"feed","targetId":1001,"reasonType":"spam"}'
done

# Expected:
# First 10: Success
# 11th: 429 Too Many Requests
```

---

## Conclusion

This test documentation provides comprehensive coverage of all 13 endpoints in the xypai-content module. Each test case includes:

- **Request format** - Exact HTTP method, URL, headers, and body
- **Response format** - Expected JSON structure
- **Assertions** - Validation checklist for each test
- **Error scenarios** - Edge cases and validation errors

**Test Coverage**:
- ✅ All 13 endpoints covered
- ✅ Happy path scenarios
- ✅ Error scenarios
- ✅ Permission validation
- ✅ Integration flows
- ✅ Performance benchmarks

**Next Steps**:
1. Import Postman collection
2. Configure environment variables
3. Run all tests
4. Verify 100% pass rate
5. Generate test report

**Status**: Ready for frontend integration testing
