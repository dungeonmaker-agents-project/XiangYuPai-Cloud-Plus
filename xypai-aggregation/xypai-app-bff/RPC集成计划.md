# XyPai-App-BFF RPC 集成计划

> 创建日期：2025-11-26
> 版本：v1.0.0

## 📋 当前状态总览

### 已完成模块（Mock 实现）

| 模块 | 页面 | 接口数 | Mock状态 | RPC集成状态 |
|------|------|--------|----------|-------------|
| 首页 Feed 流 | Page01 | 3 | ✅ | 部分RPC |
| 筛选功能 | Page02 | 2 | ✅ | ✅ RPC已集成 |
| 限时专享 | Page05 | 1 | - | ✅ RPC已集成 |
| 搜索功能 | Page06-07 | 4 | ✅ | ⏳ 待集成 |
| 组局中心 | Page08-10 | 9 | ✅ | ⏳ 待集成 |
| 技能服务 | Page11-12 | 3 | ✅ | ⏳ 待集成 |

**总计**: 22 个接口，Mock实现完成，待RPC集成

---

## 🔗 模块依赖分析

### 1. 技能服务模块（Page11-12）

**BFF 接口**:
- `GET /api/service/list` - 服务列表
- `GET /api/service/detail` - 服务详情
- `GET /api/service/reviews` - 评价列表

**需要的 RPC 接口** (xypai-user 服务):

```java
// RemoteAppUserService 需要新增的方法
public interface RemoteAppUserService {
    // 已有接口
    // ...

    // 新增：技能服务相关接口
    /**
     * 查询技能服务列表
     * @param queryDto 包含 skillType, tabType, sortBy, filters, 分页参数
     * @return 技能服务分页结果
     */
    SkillServicePageResult querySkillServiceList(SkillServiceQueryDto queryDto);

    /**
     * 获取技能服务详情
     * @param serviceId 服务ID
     * @param userId 当前用户ID（可选，用于计算距离等）
     * @return 技能服务详情
     */
    SkillServiceDetailVo getSkillServiceDetail(Long serviceId, Long userId);

    /**
     * 获取技能服务评价列表
     * @param serviceId 服务ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param filterBy 筛选类型 (all/excellent/positive/negative)
     * @return 评价分页结果
     */
    SkillServiceReviewPageResult getSkillServiceReviews(Long serviceId, Integer pageNum, Integer pageSize, String filterBy);
}
```

**依赖的数据库表** (xypai_user):
- `skill` - 技能主表
- `skill_image` - 技能图片
- `skill_promise` - 技能承诺
- `skill_available_time` - 可用时间
- `xy_user` - 用户信息
- `user_stats` - 用户统计

**集成优先级**: ⭐⭐⭐⭐⭐ (最高)
- 核心业务功能
- 数据表已存在
- 只需扩展现有 RemoteAppUserService

---

### 2. 组局中心模块（Page08-10）

**BFF 接口**:
- `GET /api/activity/list` - 活动列表
- `GET /api/activity/detail/{id}` - 活动详情
- `POST /api/activity/register` - 报名活动
- `POST /api/activity/register/cancel` - 取消报名
- `GET /api/activity/publish/config` - 发布配置
- `POST /api/activity/publish` - 发布活动
- `POST /api/activity/pay` - 支付活动费用
- `POST /api/activity/approve` - 审核报名
- `POST /api/activity/cancel/{id}` - 取消活动

**方案选择**:

#### 方案A：扩展 xypai-content 服务（推荐）

将组局活动作为内容的一种类型，扩展 xypai-content 服务：

```
xypai_content 数据库新增表:
├── activity              # 组局活动主表
├── activity_participant  # 活动参与者表
├── activity_type         # 活动类型配置表
└── activity_payment      # 活动费用记录表
```

**优点**:
- 复用现有内容服务架构
- 活动与 Feed 内容有天然关联（发布活动可同时生成 Feed）
- 减少新服务部署成本

#### 方案B：新建 xypai-activity 服务

独立服务管理组局活动：

```
新服务: xypai-activity (9408)
新数据库: xypai_activity
├── activity              # 组局活动主表
├── activity_participant  # 活动参与者表
├── activity_type         # 活动类型配置表
└── activity_payment      # 活动费用记录表
```

**优点**:
- 业务隔离清晰
- 独立扩展和部署

**需要的 RPC 接口**:

```java
// 新接口定义 (ruoyi-api 新增 xypai-api-activity 或扩展 xypai-api-content)
public interface RemoteActivityService {
    // 列表查询
    ActivityPageResult queryActivityList(ActivityQueryDto queryDto);

    // 详情
    ActivityDetailVo getActivityDetail(Long activityId, Long userId);

    // 发布配置
    ActivityPublishConfigVo getPublishConfig(Long userId);

    // 发布活动
    ActivityPublishResultVo publishActivity(ActivityPublishDto dto, Long userId);

    // 报名
    ActivityRegisterResultVo registerActivity(Long activityId, Long userId, String message);

    // 取消报名
    Boolean cancelRegistration(Long activityId, Long userId, String reason);

    // 审核报名
    Boolean approveRegistration(Long activityId, Long participantId, Boolean approved, Long userId);

    // 取消活动
    Boolean cancelActivity(Long activityId, String reason, Long userId);
}
```

**集成优先级**: ⭐⭐⭐⭐ (高)
- 需要新建数据库表
- 涉及支付流程（需与 xypai-payment 联动）

---

### 3. 搜索功能模块（Page06-07）

**BFF 接口**:
- `GET /api/home/search/init` - 搜索初始化
- `GET /api/home/search/suggest` - 搜索联想
- `GET /api/v1/search/all` - 综合搜索
- `GET /api/v1/search/user` - 用户搜索

**需要的 RPC 接口**:

```java
// RemoteAppUserService 扩展
public interface RemoteAppUserService {
    // 搜索用户
    UserSearchPageResult searchUsers(String keyword, Integer pageNum, Integer pageSize);

    // 获取热门搜索词
    List<String> getHotSearchKeywords(Integer limit);
}

// RemoteContentService (xypai-content 新增)
public interface RemoteContentService {
    // 搜索动态内容
    FeedSearchPageResult searchFeeds(String keyword, Integer pageNum, Integer pageSize);

    // 搜索话题
    TopicSearchPageResult searchTopics(String keyword, Integer pageNum, Integer pageSize);
}
```

**集成优先级**: ⭐⭐⭐ (中)
- 可考虑接入 Elasticsearch 提升搜索性能
- 需要 xypai-user 和 xypai-content 两个服务配合

---

### 4. 首页 Feed 流模块（Page01）

**BFF 接口**:
- `GET /api/home/feed` - 首页推荐流

**需要的 RPC 接口**:

```java
// 已有 RemoteAppUserService
- queryLimitedTimeUsers()  // ✅ 已集成
- getFilterConfig()        // ✅ 已集成
- queryFilteredUsers()     // ✅ 已集成

// 新增需要
- batchGetUsersByIds(List<Long> userIds)  // 批量获取用户信息

// RemoteChatService
- batchCheckOnlineStatus(List<Long> userIds)  // 批量获取在线状态

// RemoteLocationService
- calculateBatchDistance(Location from, List<Location> to)  // 批量计算距离
```

**集成优先级**: ⭐⭐⭐ (中)
- 基础框架已完成
- 需要推荐算法优化

---

## 📊 RPC 接口汇总

### 需要新增的接口统计

| 服务模块 | 新增方法数 | 复杂度 | 依赖 |
|----------|-----------|--------|------|
| RemoteAppUserService | 5 | 中 | xypai_user 数据库 |
| RemoteActivityService | 8 | 高 | 新建表 + xypai_payment |
| RemoteContentService | 2 | 低 | xypai_content 数据库 |
| RemoteChatService | 1 | 低 | xypai_chat 数据库 |
| RemoteLocationService | 1 | 低 | xypai_common 数据库 |

**总计**: 17 个新 RPC 方法

---

## 🚀 集成优先级排序

### Phase 1: 技能服务 RPC 集成（建议优先）

1. **扩展 RemoteAppUserService**
   - 新增 `querySkillServiceList()`
   - 新增 `getSkillServiceDetail()`
   - 新增 `getSkillServiceReviews()`

2. **修改 SkillServiceServiceImpl**
   - 注入 `@DubboReference RemoteAppUserService`
   - 替换 Mock 数据为 RPC 调用

**预计工时**: 2-3 天

### Phase 2: 组局中心 RPC 集成

1. **方案决策**: 扩展 xypai-content 还是新建 xypai-activity
2. **数据库设计**: 创建 activity 相关表
3. **RPC 接口定义**: 新建 RemoteActivityService
4. **服务实现**: 在领域服务中实现接口
5. **BFF 集成**: 替换 Mock 数据

**预计工时**: 5-7 天

### Phase 3: 搜索功能 RPC 集成

1. **扩展 RemoteAppUserService**: 用户搜索
2. **新建 RemoteContentService**: 内容搜索
3. **可选**: Elasticsearch 集成

**预计工时**: 3-4 天

### Phase 4: 首页 Feed 流完善

1. **批量查询优化**
2. **推荐算法实现**
3. **Redis 缓存层**

**预计工时**: 3-4 天

---

## 📝 下一步行动

### 立即可做（无需额外开发）

1. ✅ 技能服务模块测试用例完善
2. ✅ 组局中心模块测试用例完善
3. ⏳ 与领域服务团队确认 RPC 接口设计

### 需要协调的工作

1. **xypai-user 服务**: 新增技能查询 RPC 接口
2. **架构决策**: 组局中心归属（content 还是新服务）
3. **数据库**: activity 相关表结构设计

---

## 🔄 更新日志

| 日期 | 版本 | 更新内容 |
|------|------|----------|
| 2025-11-26 | v1.0.0 | 初始版本，完成模块依赖分析和集成计划 |
