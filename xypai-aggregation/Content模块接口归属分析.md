# Content 模块接口归属分析

> **更新时间**: 2025-11-24
> **维护者**: XyPai 开发团队

## 📋 概述

本文档详细分析 `03-content模块/Frontend` 中所有前端页面的后端接口归属，明确哪些接口应该在哪个微服务模块实现。

---

## 🎯 微服务模块划分原则

### 1. **xypai-app-bff** (聚合层)
- 业务聚合接口（无数据库）
- 首页推荐流（调用 user + content）
- 筛选功能（调用 user + content）
- 跨领域的复杂查询

### 2. **xypai-content** (内容领域服务)
- 动态发布、查询、删除
- 评论系统
- 话题管理
- 互动功能（点赞、收藏、分享）
- 举报功能

### 3. **xypai-user** (用户领域服务)
- 用户资料
- 关注关系
- 用户技能
- 用户标签

### 4. **xypai-common** (通用服务)
- 媒体上传（图片、视频）
- 位置服务（附近地点、搜索）
- 通知系统
- 举报审核

---

## 📄 页面级接口归属分析

### 01-首页Feed流页面

**文档路径**: `03-content模块/Frontend/01-首页Feed流页面.md`

| 接口 | 归属模块 | 实现状态 | 说明 |
|------|---------|---------|------|
| `GET /api/home/feed` | xypai-app-bff | ✅ 已实现 | 推荐用户列表，聚合 user + content 数据 |
| `POST /api/user/follow` | xypai-user | ⚠️ 待确认 | 关注/取消关注，纯用户领域操作 |

**实现位置**:
- [xypai-app-bff/controller/HomeFeedController.java](../xypai-app-bff/src/main/java/org/dromara/aggregation/controller/HomeFeedController.java#L75)

**测试文件**:
- [AppHomeFeedTest.java](../xypai-app-bff/src/test/java/org/dromara/aggregation/AppHomeFeedTest.java)

---

### 02-筛选页面

**文档路径**: `03-content模块/Frontend/02-筛选页面.md`

| 接口 | 归属模块 | 实现状态 | 说明 |
|------|---------|---------|------|
| `GET /api/home/filter/config` | xypai-app-bff | 🔜 待实现 | 获取筛选配置（年龄、性别、技能等选项） |
| `POST /api/home/filter/apply` | xypai-app-bff | 🔜 待实现 | 应用筛选条件，返回筛选后的用户列表 |

**实现建议**:
```java
// xypai-app-bff/controller/HomeFilterController.java
@RestController
@RequestMapping("/api/home/filter")
public class HomeFilterController {

    @GetMapping("/config")
    public R<FilterConfigVO> getFilterConfig(@RequestParam String type) {
        // 返回筛选配置：年龄范围、性别选项、技能列表、标签列表等
    }

    @PostMapping("/apply")
    public R<Map<String, Object>> applyFilter(@RequestBody FilterApplyDTO filterDTO) {
        // 应用筛选条件，调用 user 服务获取符合条件的用户
        // 返回用户列表 + 筛选统计信息
    }
}
```

---

### 02-发布动态页面

**文档路径**: `03-content模块/Frontend/02-发布动态页面.md`

| 接口 | 归属模块 | 实现状态 | 说明 |
|------|---------|---------|------|
| `POST /api/v1/content/publish` | xypai-content | ✅ 已实现 | 发布动态 |
| `GET /api/v1/content/topics/hot` | xypai-content | ✅ 已实现 | 获取热门话题 |
| `GET /api/v1/content/topics/search` | xypai-content | ✅ 已实现 | 搜索话题 |
| `POST /api/v1/media/upload` | xypai-common | ✅ 已实现 | 上传图片/视频 |
| `GET /api/v1/location/nearby` | xypai-common | ✅ 已实现 | 获取附近地点 |
| `GET /api/v1/location/search` | xypai-common | ✅ 已实现 | 搜索地点 |

**实现位置**:
- Content: [xypai-content/controller/FeedController.java](../../xypai-modules/xypai-content/src/main/java/org/dromara/content/controller/FeedController.java)
- Content: [xypai-content/controller/TopicController.java](../../xypai-modules/xypai-content/src/main/java/org/dromara/content/controller/TopicController.java)
- Media: [xypai-common/media/controller/app/MediaController.java](../../xypai-common/src/main/java/org/dromara/common/media/controller/app/MediaController.java)
- Location: [xypai-common/location/controller/app/LocationController.java](../../xypai-common/src/main/java/org/dromara/common/location/controller/app/LocationController.java)

**测试文件**:
- [ContentPublishTest.java](../../xypai-modules/xypai-content/src/test/java/org/dromara/content/ContentPublishTest.java)

---

### 03-动态详情页面

**文档路径**: `03-content模块/Frontend/03-动态详情页面.md`

| 接口 | 归属模块 | 实现状态 | 说明 |
|------|---------|---------|------|
| `GET /api/v1/content/detail/{feedId}` | xypai-content | ✅ 已实现 | 获取动态详情 |
| `POST /api/v1/content/like/{feedId}` | xypai-content | ✅ 已实现 | 点赞动态 |
| `POST /api/v1/content/collect/{feedId}` | xypai-content | ✅ 已实现 | 收藏动态 |
| `POST /api/v1/content/share/{feedId}` | xypai-content | ✅ 已实现 | 分享动态 |
| `POST /api/v1/content/comment` | xypai-content | ✅ 已实现 | 发表评论 |
| `GET /api/v1/content/comment/{feedId}` | xypai-content | ✅ 已实现 | 获取评论列表 |
| `DELETE /api/v1/content/comment/{id}` | xypai-content | ✅ 已实现 | 删除评论 |
| `PUT /api/v1/content/comment/{id}/pin` | xypai-content | ✅ 已实现 | 置顶评论 |

**实现位置**:
- [xypai-content/controller/FeedController.java](../../xypai-modules/xypai-content/src/main/java/org/dromara/content/controller/FeedController.java)
- [xypai-content/controller/CommentController.java](../../xypai-modules/xypai-content/src/main/java/org/dromara/content/controller/CommentController.java)
- [xypai-content/controller/InteractionController.java](../../xypai-modules/xypai-content/src/main/java/org/dromara/content/controller/InteractionController.java)

**测试文件**: 🔜 待创建 `FeedDetailTest.java`

---

### 01-发现主页页面

**文档路径**: `03-content模块/Frontend/01-发现主页页面.md`

| 接口 | 归属模块 | 实现状态 | 说明 |
|------|---------|---------|------|
| `GET /api/v1/content/feed/following` | xypai-content | ✅ 已实现 | 关注Tab - 时间倒序 |
| `GET /api/v1/content/feed/hot` | xypai-content | ✅ 已实现 | 热门Tab - 热度排序 |
| `GET /api/v1/content/feed/nearby` | xypai-content | ✅ 已实现 | 同城Tab - 空间查询 |
| `DELETE /api/v1/content/{feedId}` | xypai-content | ✅ 已实现 | 删除动态 |

**实现位置**:
- [xypai-content/controller/FeedController.java](../../xypai-modules/xypai-content/src/main/java/org/dromara/content/controller/FeedController.java)

**测试文件**: 🔜 待创建 `DiscoveryFeedTest.java`

---

### 05-限时专享页面

**文档路径**: `03-content模块/Frontend/05-限时专享页面.md`

| 接口 | 归属模块 | 实现状态 | 说明 |
|------|---------|---------|------|
| `GET /api/promotion/flash-deals` | xypai-content | 🔜 待实现 | 获取限时优惠列表 |
| `GET /api/promotion/flash-deals/{id}` | xypai-content | 🔜 待实现 | 获取优惠详情 |

**实现建议**: 创建 `PromotionController` 或归入 xypai-trade 模块

---

### 06-搜索页面 & 07-搜索结果页面

**文档路径**:
- `03-content模块/Frontend/06-搜索页面.md`
- `03-content模块/Frontend/07-搜索结果页面.md`

| 接口 | 归属模块 | 实现状态 | 说明 |
|------|---------|---------|------|
| `GET /api/search/hot` | xypai-content | 🔜 待实现 | 获取热搜词 |
| `GET /api/search/history` | xypai-content | 🔜 待实现 | 获取搜索历史 |
| `POST /api/search/history` | xypai-content | 🔜 待实现 | 添加搜索历史 |
| `DELETE /api/search/history` | xypai-content | 🔜 待实现 | 清空搜索历史 |
| `GET /api/search/suggest` | xypai-content | 🔜 待实现 | 搜索建议 |
| `GET /api/search/result` | xypai-content | 🔜 待实现 | 综合搜索（用户+动态+话题） |
| `GET /api/search/users` | xypai-user | 🔜 待实现 | 搜索用户 |
| `GET /api/search/feeds` | xypai-content | 🔜 待实现 | 搜索动态 |
| `GET /api/search/topics` | xypai-content | ✅ 已实现 | 搜索话题（已有） |

**实现建议**:
- 搜索历史、热搜词 → xypai-content
- 综合搜索 → xypai-app-bff (聚合多个来源)
- 分类搜索 → 各自领域服务

---

### 08-组局中心列表页面 & 09-组局详情页面 & 10-发布组局页面

**文档路径**:
- `03-content模块/Frontend/08-组局中心列表页面.md`
- `03-content模块/Frontend/09-组局详情页面.md`
- `03-content模块/Frontend/10-发布组局页面.md`

| 接口 | 归属模块 | 实现状态 | 说明 |
|------|---------|---------|------|
| `GET /api/activities/list` | xypai-content | 🔜 待实现 | 组局列表 |
| `GET /api/activities/{id}` | xypai-content | 🔜 待实现 | 组局详情 |
| `POST /api/activities/create` | xypai-content | 🔜 待实现 | 创建组局 |
| `POST /api/activities/{id}/join` | xypai-content | 🔜 待实现 | 参加组局 |
| `POST /api/activities/{id}/leave` | xypai-content | 🔜 待实现 | 退出组局 |
| `GET /api/activities/{id}/participants` | xypai-content | 🔜 待实现 | 参与者列表 |

**实现建议**:
- 组局功能属于内容领域，归 xypai-content
- 需要创建 `ActivityController`

---

### 11-服务列表页面 & 12-服务详情页面

**文档路径**:
- `03-content模块/Frontend/11-服务列表页面.md`
- `03-content模块/Frontend/12-服务详情页面.md`

| 接口 | 归属模块 | 实现状态 | 说明 |
|------|---------|---------|------|
| `GET /api/services/list` | xypai-user | 🔜 待实现 | 服务列表（技能服务） |
| `GET /api/services/{id}` | xypai-user | 🔜 待实现 | 服务详情 |
| `GET /api/services/{id}/reviews` | xypai-content | 🔜 待实现 | 服务评价 |
| `POST /api/services/{id}/book` | xypai-trade | 🔜 待实现 | 预约服务 |

**实现建议**:
- 服务（技能）属于用户领域 → xypai-user
- 评价属于内容领域 → xypai-content
- 预约/交易 → xypai-trade

---

## 📊 模块接口统计

### xypai-app-bff (聚合层)

**已实现**:
- ✅ `GET /api/home/feed` - 首页推荐流

**待实现**:
- 🔜 `GET /api/home/filter/config` - 筛选配置
- 🔜 `POST /api/home/filter/apply` - 应用筛选
- 🔜 `GET /api/search/result` - 综合搜索（可选）

**实现优先级**: 🔥 高（筛选功能是首页核心）

---

### xypai-content (内容领域服务)

**已实现**:
- ✅ 发布动态
- ✅ 动态详情、列表、删除
- ✅ 评论系统（一级、二级）
- ✅ 互动功能（点赞、收藏、分享）
- ✅ 话题管理（热门、搜索）

**待实现**:
- 🔜 搜索功能（热搜词、历史、建议、动态搜索）
- 🔜 组局功能（列表、详情、创建、参与）
- 🔜 限时专享（可选，或归入 trade）
- 🔜 服务评价

**实现优先级**:
- 🔥 高: 搜索功能（用户核心需求）
- 📦 中: 组局功能
- 📦 低: 限时专享、服务评价

---

### xypai-user (用户领域服务)

**已实现**:
- ⚠️ 待确认: 关注/取消关注

**待实现**:
- 🔜 用户搜索
- 🔜 服务列表（技能服务）
- 🔜 服务详情

**实现优先级**:
- 🔥 高: 关注功能（首页必需）
- 📦 中: 用户搜索
- 📦 低: 服务功能

---

### xypai-common (通用服务)

**已实现**:
- ✅ 媒体上传（图片、视频）
- ✅ 位置服务（附近地点、搜索）

**待实现**:
- 无（当前需求已满足）

---

## 🎯 下一步实现建议

### 阶段1: 完善首页核心功能 (优先级: 🔥 高)

1. **xypai-app-bff**:
   - 实现 `HomeFilterController` (筛选配置 + 应用筛选)
   - 创建测试: `HomeFilterTest.java`

2. **xypai-user**:
   - 确认/实现 `UserFollowController` (关注/取消关注)
   - 创建测试: `UserFollowTest.java`

### 阶段2: 实现搜索功能 (优先级: 🔥 高)

1. **xypai-content**:
   - 创建 `SearchController`
   - 实现热搜词、搜索历史、搜索建议
   - 实现动态搜索、话题搜索
   - 创建测试: `SearchTest.java`

2. **xypai-user**:
   - 创建 `UserSearchController`
   - 实现用户搜索
   - 创建测试: `UserSearchTest.java`

3. **xypai-app-bff** (可选):
   - 创建 `SearchAggregationController`
   - 实现综合搜索（聚合用户+动态+话题）

### 阶段3: 实现组局功能 (优先级: 📦 中)

1. **xypai-content**:
   - 创建 `ActivityController`
   - 实现组局 CRUD、参与、退出
   - 创建测试: `ActivityTest.java`

### 阶段4: 实现服务功能 (优先级: 📦 低)

1. **xypai-user**:
   - 创建 `ServiceController` (技能服务)
   - 实现服务列表、详情

2. **xypai-content**:
   - 扩展评论系统支持服务评价

3. **xypai-trade**:
   - 创建 `BookingController` (预约服务)

---

## 📝 接口路径规范

### 当前路径差异

**前端期望**:
- `/api/v1/content/*` - Content 服务
- `/api/v1/media/*` - Media 服务
- `/api/v1/location/*` - Location 服务

**后端实现**:
- `/api/v1/content/*` - Content 服务 ✅
- `/api/media/*` - Media 服务 ⚠️ (缺少 /v1)
- `/api/location/*` - Location 服务 ⚠️ (缺少 /v1)

### 解决方案

**选项1: 修改后端路径（推荐）**
```java
// xypai-common/media/controller/app/MediaController.java
@RequestMapping("/api/v1/media")  // 添加 /v1

// xypai-common/location/controller/app/LocationController.java
@RequestMapping("/api/v1/location")  // 添加 /v1
```

**选项2: Gateway 路由重写**
```yaml
# ruoyi-gateway-routes-xypai.yml
- id: xypai-common-media
  uri: lb://xypai-common
  predicates:
    - Path=/api/v1/media/**
  filters:
    - RewritePath=/api/v1/media/(?<segment>.*), /api/media/$\{segment}
```

**选项3: 前端适配**
- 修改前端 API 路径去掉 `/v1`

**建议**: 使用选项1，保持接口版本化规范

---

## 🔗 相关文档

- [xypai-app-bff 实现进度](../xypai-app-bff/实现进度.md)
- [xypai-content 实现进度](../../xypai-modules/xypai-content/实现进度.md)
- [xypai-common 快速理解](../../xypai-common/快速理解.md)
- [向娱拍平台架构总览](../../向娱拍平台架构总览.md)

---

**最后更新**: 2025-11-24
**更新内容**: 完成 Content 模块所有前端页面的接口归属分析
