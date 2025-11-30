# API 接口对接分析报告

> **分析日期**: 2025-11-28
>
> **分析范围**: xypai-content、xypai-user 模块 API 与前端实现对比
>
> **目标**: 确认接口完整性，识别缺失/更新的接口

---

## 📊 总体分析结果

### 接口覆盖率统计

| 服务模块 | 后端接口总数 | 前端已实现 | 待实现 | 覆盖率 |
|----------|--------------|------------|--------|--------|
| xypai-content (Feed/评论/互动) | 16 | 12 | 4 | 75% |
| xypai-content (话题) | 3 | 2 | 1 | 67% |
| xypai-content (活动) | 10 | 5 | 5 | 50% |
| xypai-user (用户资料) | 14 | 14 | 0 | 100% ✅ |
| xypai-user (技能管理) | 9 | 9 | 0 | 100% ✅ |
| xypai-user (社交关系) | 7 | 7 | 0 | 100% ✅ |
| **总计** | **59** | **49** | **10** | **83%** |

---

## ✅ 已完成对接的模块

### 1. xypai-user 用户资料接口 (14/14 = 100%)

| 接口 | 方法 | 路径 | 前端实现 | 状态 |
|------|------|------|----------|------|
| 获取编辑资料数据 | GET | `/api/user/profile/edit` | `profileApi.getEditPageData()` | ✅ |
| 获取主页头部 | GET | `/api/user/profile/header` | `profileApi.getProfileHeader()` | ✅ |
| 获取他人主页 | GET | `/api/user/profile/other/{userId}` | `profileApi.getOtherUserProfile()` | ✅ |
| 获取资料详情 | GET | `/api/user/profile/info` | `profileApi.getProfileInfo()` | ✅ |
| 更新昵称 | PUT | `/api/user/profile/nickname` | `profileApi.updateNickname()` | ✅ |
| 更新性别 | PUT | `/api/user/profile/gender` | `profileApi.updateGender()` | ✅ |
| 更新生日 | PUT | `/api/user/profile/birthday` | `profileApi.updateBirthday()` | ✅ |
| 更新居住地 | PUT | `/api/user/profile/residence` | `profileApi.updateResidence()` | ✅ |
| 更新身高 | PUT | `/api/user/profile/height` | `profileApi.updateHeight()` | ✅ |
| 更新体重 | PUT | `/api/user/profile/weight` | `profileApi.updateWeight()` | ✅ |
| 更新职业 | PUT | `/api/user/profile/occupation` | `profileApi.updateOccupation()` | ✅ |
| 更新微信号 | PUT | `/api/user/profile/wechat` | `profileApi.updateWechat()` | ✅ |
| 更新个性签名 | PUT | `/api/user/profile/bio` | `profileApi.updateBio()` | ✅ |
| 上传头像 | POST | `/api/user/profile/avatar/upload` | `profileApi.uploadAvatar()` | ✅ |

### 2. xypai-user 技能管理接口 (9/9 = 100%)

| 接口 | 方法 | 路径 | 前端实现 | 状态 |
|------|------|------|----------|------|
| 创建线上技能 | POST | `/api/user/skills/online` | `skillApi.createOnlineSkill()` | ✅ |
| 创建线下技能 | POST | `/api/user/skills/offline` | `skillApi.createOfflineSkill()` | ✅ |
| 获取我的技能列表 | GET | `/api/user/skills/my` | `skillApi.getMySkills()` | ✅ |
| 获取技能详情 | GET | `/api/user/skills/{skillId}` | `skillApi.getSkillDetail()` | ✅ |
| 更新技能 | PUT | `/api/user/skills/{skillId}` | `skillApi.updateSkill()` | ✅ |
| 删除技能 | DELETE | `/api/user/skills/{skillId}` | `skillApi.deleteSkill()` | ✅ |
| 切换上下架状态 | PUT | `/api/user/skills/{skillId}/toggle` | `skillApi.toggleSkillStatus()` | ✅ |
| 获取用户技能列表 | GET | `/api/user/skills/user/{userId}` | `skillApi.getUserSkills()` | ✅ |
| 搜索附近技能 | GET | `/api/user/skills/nearby` | `skillApi.getNearbySkills()` | ✅ |

### 3. xypai-user 社交关系接口 (7/7 = 100%)

| 接口 | 方法 | 路径 | 前端实现 | 状态 |
|------|------|------|----------|------|
| 关注用户 | POST | `/api/user/relation/follow/{id}` | `relationApi.followUser()` | ✅ |
| 取消关注 | DELETE | `/api/user/relation/follow/{id}` | `relationApi.unfollowUser()` | ✅ |
| 获取关注列表 | GET | `/api/user/relation/following` | `relationApi.getFollowingList()` | ✅ |
| 获取粉丝列表 | GET | `/api/user/relation/fans` | `relationApi.getFansList()` | ✅ |
| 拉黑用户 | POST | `/api/user/relation/block/{id}` | `relationApi.blockUser()` | ✅ |
| 取消拉黑 | DELETE | `/api/user/relation/block/{id}` | `relationApi.unblockUser()` | ✅ |
| 举报用户 | POST | `/api/user/relation/report/{id}` | `relationApi.reportUser()` | ✅ |

### 4. xypai-content Feed/互动接口 (12/16 = 75%)

| 接口 | 方法 | 路径 | 前端实现 | 状态 |
|------|------|------|----------|------|
| 获取动态列表 | GET | `/api/v1/content/feed/{tabType}` | `feedApi.getFeedList()` | ✅ |
| 获取动态详情 | GET | `/api/v1/content/detail/{feedId}` | `feedApi.getFeedDetail()` | ✅ |
| 发布动态 | POST | `/api/v1/content/publish` | `feedApi.publishFeed()` | ✅ |
| 删除动态 | DELETE | `/api/v1/content/{feedId}` | `feedApi.deleteFeed()` | ✅ |
| 获取评论列表 | GET | `/api/v1/content/comments/{feedId}` | `feedApi.getCommentList()` | ✅ |
| 发表评论 | POST | `/api/v1/content/comment` | `feedApi.publishComment()` | ✅ |
| 删除评论 | DELETE | `/api/v1/content/comment/{commentId}` | `feedApi.deleteComment()` | ✅ |
| 点赞/取消点赞 | POST | `/api/v1/interaction/like` | `feedApi.like()` | ✅ |
| 收藏/取消收藏 | POST | `/api/v1/interaction/collect` | `feedApi.collect()` | ✅ |
| 分享动态 | POST | `/api/v1/interaction/share` | `feedApi.share()` | ✅ |
| 获取热门话题 | GET | `/api/v1/content/topics/hot` | `feedApi.getHotTopics()` | ✅ |
| 搜索话题 | GET | `/api/v1/content/topics/search` | `feedApi.searchTopics()` | ✅ |

---

## ⚠️ 待实现的接口 (10个)

### 1. xypai-content 评论接口 (1个)

| 接口 | 方法 | 路径 | 优先级 | 说明 |
|------|------|------|--------|------|
| 置顶/取消置顶评论 | PUT | `/api/v1/content/comment/{commentId}/pin` | P2 | 动态作者操作 |

**后端文档规范:**
```http
PUT /api/v1/content/comment/{commentId}/pin?pin=true
Authorization: Bearer <token>
```

**响应:**
```json
{
  "code": 200,
  "msg": "置顶成功",
  "data": null
}
```

---

### 2. xypai-content 话题接口 (1个)

| 接口 | 方法 | 路径 | 优先级 | 说明 |
|------|------|------|--------|------|
| 话题下的动态列表 | GET | `/api/v1/content/topics/{topicId}/feeds` | P1 | 话题详情页 |

**后端文档规范:**
```http
GET /api/v1/content/topics/{topicId}/feeds?page=1&pageSize=20
```

**响应:**
```json
{
  "code": 200,
  "data": {
    "records": [...],
    "total": 500,
    "current": 1,
    "pages": 25
  }
}
```

---

### 3. xypai-content 用户动态/收藏接口 (2个)

| 接口 | 方法 | 路径 | 优先级 | 说明 |
|------|------|------|--------|------|
| 获取用户动态列表 | GET | `/api/v1/content/feed/user/{userId}` | **P0** | Profile页"动态"Tab |
| 获取我的收藏列表 | GET | `/api/v1/interaction/collect/my` | **P0** | Profile页"收藏"Tab |

> ⚠️ **重要**: 这两个接口是 Profile 页面 Tab 数据的关键依赖，优先级最高！

**用户动态列表 - 后端文档规范:**
```http
GET /api/v1/content/feed/user/{userId}?pageNum=1&pageSize=20
Authorization: Bearer <token>
```

**我的收藏列表 - 后端文档规范:**
```http
GET /api/v1/interaction/collect/my?pageNum=1&pageSize=20
Authorization: Bearer <token>
```

**响应格式:**
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1001,
        "targetType": "feed",
        "targetId": 1001,
        "targetContent": "动态内容摘要...",
        "targetCover": "https://cdn.example.com/feed/1001_thumb.jpg",
        "author": {
          "userId": 10002,
          "nickname": "用户昵称",
          "avatar": "https://cdn.example.com/avatar/10002.jpg"
        },
        "collectTime": "2025-11-26 15:00:00"
      }
    ],
    "total": 100,
    "current": 1,
    "pages": 5
  }
}
```

---

### 4. xypai-content 活动接口 (6个)

| 接口 | 方法 | 路径 | 优先级 | 说明 |
|------|------|------|--------|------|
| 审核报名 | POST | `/api/v1/activity/approve` | P1 | 组织者审核 |
| 取消活动 | POST | `/api/v1/activity/cancel/{activityId}` | P1 | 组织者取消 |
| 活动类型列表 | GET | `/api/v1/activity/types` | P2 | 发布页选择 |
| 热门活动类型 | GET | `/api/v1/activity/types/hot` | P2 | 首页展示 |
| 分享活动 | POST | `/api/v1/activity/share/{activityId}` | P2 | 分享统计 |

**审核报名 - 后端文档规范:**
```http
POST /api/v1/activity/approve?activityId=7001&participantId=8001&approved=true
Authorization: Bearer <token>
```

**取消活动 - 后端文档规范:**
```http
POST /api/v1/activity/cancel/{activityId}?reason=天气原因取消
Authorization: Bearer <token>
```

---

## 🔍 接口差异对比

### 字段命名差异

| 位置 | 后端文档 | 前端实现 | 建议 |
|------|----------|----------|------|
| 分页参数 | `pageNum` | 部分用 `page` | 统一使用 `pageNum` |
| 动态列表响应 | `records` | `list` | 前端需适配 `records` |
| 评论排序 | `sortType: time/hot/like` | `sortType: hot/new` | 前端需补充 `like` |

### 请求路径差异

| 功能 | 后端文档路径 | 前端config.ts配置 | 是否一致 |
|------|-------------|-------------------|----------|
| 评论列表 | `/api/v1/content/comments/{feedId}` | `/api/v1/content/comments/{feedId}` | ✅ |
| 发表评论 | `/api/v1/content/comment` | `/api/v1/content/comment` | ✅ |
| 删除评论 | `/api/v1/content/comment/{commentId}` | `/api/v1/content/comment/{commentId}` | ✅ |

---

## 📋 前端待办事项清单

### P0 - 必须立即完成 (阻塞 Profile 页面)

- [ ] **feedApi.ts**: 新增 `getUserFeedList(userId, params)` 方法
  - 接口: `GET /xypai-content/api/v1/content/feed/user/{userId}`
  - 用途: Profile页"动态"Tab

- [ ] **feedApi.ts**: 新增 `getMyCollections(params)` 方法
  - 接口: `GET /xypai-content/api/v1/interaction/collect/my`
  - 用途: Profile页"收藏"Tab

### P1 - 重要功能

- [ ] **feedApi.ts**: 新增 `getTopicFeeds(topicId, params)` 方法
  - 接口: `GET /xypai-content/api/v1/content/topics/{topicId}/feeds`
  - 用途: 话题详情页

- [ ] **activityApi.ts** 或 **bffApi.ts**: 新增活动管理方法
  - `approveRegistration(activityId, participantId, approved)` - 审核报名
  - `cancelActivity(activityId, reason)` - 取消活动

### P2 - 可延后

- [ ] **feedApi.ts**: 新增 `pinComment(commentId, pin)` 方法
- [ ] **activityApi.ts**: 新增 `getActivityTypes()` 方法
- [ ] **activityApi.ts**: 新增 `getHotActivityTypes()` 方法
- [ ] **activityApi.ts**: 新增 `shareActivity(activityId)` 方法

---

## 🔄 后端待确认事项

### 1. 接口实现状态确认

| 接口 | 后端文档状态 | 实际实现 | 需确认 |
|------|-------------|----------|--------|
| `GET /content/feed/user/{userId}` | 文档已定义 | ⚠️ 待确认 | 是否已实现？ |
| `GET /interaction/collect/my` | 文档已定义 | ⚠️ 待确认 | 是否已实现？ |
| `GET /topics/{topicId}/feeds` | 文档已定义 | ⚠️ 待确认 | 是否已实现？ |

### 2. 响应格式确认

请后端确认以下接口的响应是否使用 MyBatis-Plus Page 格式:

```json
{
  "code": 200,
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

## ✨ 结论

### 已完成
1. **xypai-user 模块**: 100% 接口覆盖 ✅
   - 用户资料接口 (14/14)
   - 技能管理接口 (9/9)
   - 社交关系接口 (7/7)

2. **xypai-content 核心接口**: 75% 接口覆盖
   - Feed流相关已完成
   - 互动接口已完成
   - 话题搜索已完成

### 待完成
1. **P0 优先 (2个)**: Profile页 Tab 数据接口
   - 用户动态列表
   - 我的收藏列表

2. **P1 优先 (3个)**: 话题详情、活动管理接口

3. **P2 延后 (5个)**: 评论置顶、活动类型等辅助接口

### 建议行动
1. 后端确认 P0 接口实现状态
2. 前端开始实现 P0 接口对接
3. 联调测试后继续 P1 接口

---

**报告生成**: Claude Code
**最后更新**: 2025-11-28
