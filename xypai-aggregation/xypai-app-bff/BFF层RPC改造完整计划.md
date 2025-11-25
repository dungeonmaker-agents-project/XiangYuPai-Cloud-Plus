# XyPai App BFF 层 RPC 改造完整计划

## 📋 规划概述

基于《向娱拍平台架构总览》，本计划旨在将 `xypai-app-bff` (9400) 聚合服务层的所有 **Mock 数据替换为真实 RPC 调用**，实现生产级代码质量。

---

## 🎯 目标

### 核心目标
- **消除 Mock 数据**: 所有 BFF 接口必须调用真实领域服务
- **RPC 调用**: 通过 Dubbo RPC 调用 6 个领域服务（xypai-user, xypai-content, xypai-chat, xypai-order, xypai-payment, xypai-common）
- **生产级质量**: 代码经过完整测试，可直接部署到生产环境
- **边测边改**: 每完成一个功能立即测试验证，发现问题立即修复

### 质量标准
1. **无 Mock 数据**: BFF 层不允许任何硬编码 Mock 数据
2. **真实数据库**: 所有数据来自领域服务的真实数据库查询
3. **性能优化**: 使用批量 RPC 调用、并行调用、缓存等优化手段
4. **错误处理**: 完善的 RPC 超时、降级、容错机制
5. **测试覆盖**: 每个接口有对应的集成测试用例

---

## 📊 当前状态分析

### 总体进度

| 页面功能 | 接口数量 | Mock数量 | RPC数量 | 直接调用 | 完成度 |
|---------|---------|---------|---------|---------|--------|
| **Page 02 - 筛选** | 2 | 2 | 0 | 0 | 0% |
| **Page 02 - 发布动态** | 4 | 0 | 0 | 4 | 100% ✅ |
| **Page 03 - 动态详情** | 8 | 0 | 0 | 8 | 100% ✅ |
| **Page 04 - 搜索** | 9 | 9 | 0 | 0 | 0% |
| **Page 05 - 限时专享** | 1 | 0 | 1 | 0 | 100% ✅ |
| **Page 01 - 首页Feed** | 1 | 1 | 0 | 0 | 0% |
| **合计** | **25** | **12** | **1** | **12** | **52%** |

### 需要改造的功能

#### ⚠️ 高优先级（必须）

1. **Page 02 - 筛选功能** (2 个接口)
   - `GET /api/home/filter/config` - 获取筛选配置
   - `POST /api/home/filter/apply` - 应用筛选条件

2. **Page 04 - 搜索功能** (9 个接口)
   - 搜索初始化：热门搜索、搜索历史、推荐标签 (5个)
   - 搜索结果：综合搜索、用户搜索、接单搜索、话题搜索 (4个)

3. **Page 01 - 首页 Feed 流** (1 个接口)
   - `GET /api/home/feed` - 首页用户推荐

#### ✅ 已完成（无需改造）

4. **Page 05 - 限时专享** (1 个接口) - **RPC 真实数据** ✅
5. **Page 02 - 发布动态** (4 个接口) - **直接调用 xypai-content** ✅
6. **Page 03 - 动态详情** (8 个接口) - **直接调用 xypai-content** ✅

---

## 🗓️ 实施计划

### Phase 0: 准备工作 (已完成 ✅)

**目标**: 统一代码结构，修复包结构问题

**任务清单**:
- [x] 统一包结构为 `org.dromara.appbff`
- [x] 删除旧包 `org.dromara.aggregation`
- [x] 更新启动类位置
- [x] 编译验证通过
- [x] 创建状态分析文档

**成果**:
- ✅ 包结构统一完成
- ✅ 编译成功
- ✅ 文档完善
- ⏳ **待重启服务验证**

---

### Phase 1: Page 05 限时专享验证 (0.5 天)

**目标**: 验证 RPC 真实数据调用是否正常

**依赖服务**:
- xypai-user (9401) - 提供用户+技能+统计数据

**任务清单**:
1. [x] 重启 xypai-app-bff 服务（使用新包结构）
2. [ ] 执行 SQL 测试数据脚本 `xypai_user_test_data.sql`
3. [ ] 运行 `Page05_LimitedTimeTest` 测试
4. [ ] 验证返回真实数据
5. [ ] 检查 RPC 调用日志
6. [ ] 性能测试（响应时间 < 200ms）

**验收标准**:
- ✅ 测试全部通过
- ✅ 返回 25 条真实用户数据
- ✅ 筛选、排序功能正常
- ✅ RPC 调用日志正常

---

### Phase 2: Page 02 筛选功能 RPC 改造 (2-3 天)

**目标**: 将筛选功能从 Mock 数据改为 RPC 调用

**涉及接口**:
1. `GET /api/home/filter/config` - 获取筛选配置
2. `POST /api/home/filter/apply` - 应用筛选条件

#### 2.1 定义 RPC 接口 (0.5 天)

**位置**: `ruoyi-api/xypai-api-appuser/src/main/java/org/dromara/appuser/api/`

**新增接口**:

```java
// RemoteAppUserService.java (已存在，新增方法)
public interface RemoteAppUserService {

    // ========== 筛选功能 ==========

    /**
     * 根据筛选条件查询用户列表
     *
     * @param query 筛选条件
     * @return 用户分页结果
     */
    PageResult<UserProfileDTO> filterUsers(UserFilterQueryDTO query);

    /**
     * 获取所有用户标签
     *
     * @return 标签列表
     */
    List<TagDTO> getAllUserTags();

    /**
     * 根据类别获取技能列表
     *
     * @param category 类别 (online/offline)
     * @return 技能列表
     */
    List<SkillCategoryDTO> getSkillsByCategory(String category);
}
```

**新增 DTO**:
- `UserFilterQueryDTO` - 筛选条件 DTO
- `UserProfileDTO` - 用户资料 DTO
- `TagDTO` - 标签 DTO
- `SkillCategoryDTO` - 技能分类 DTO
- `PageResult<T>` - 分页结果通用类

#### 2.2 实现 RPC Provider (1 天)

**位置**: `xypai-modules/xypai-user/`

**实现步骤**:

1. **Mapper 层**: 实现复杂筛选 SQL
```java
// UserMapper.java
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据筛选条件查询用户
     */
    @Select("<script>"
        + "SELECT u.*, us.fans_count, us.likes_count "
        + "FROM users u "
        + "LEFT JOIN user_stats us ON u.user_id = us.user_id "
        + "WHERE u.deleted = 0 "
        + "<if test='gender != null and gender != \"all\"'>"
        + "  AND u.gender = #{gender} "
        + "</if>"
        + "<if test='minAge != null'>"
        + "  AND YEAR(CURDATE()) - YEAR(u.birthday) >= #{minAge} "
        + "</if>"
        + "<if test='maxAge != null'>"
        + "  AND YEAR(CURDATE()) - YEAR(u.birthday) <= #{maxAge} "
        + "</if>"
        + "<if test='isOnline != null'>"
        + "  AND u.is_online = #{isOnline} "
        + "</if>"
        + "<if test='cityCode != null'>"
        + "  AND u.residence LIKE CONCAT('%', #{cityCode}, '%') "
        + "</if>"
        + "ORDER BY u.is_online DESC, u.user_id DESC "
        + "LIMIT #{offset}, #{pageSize}"
        + "</script>")
    List<User> filterUsers(@Param("gender") String gender,
                          @Param("minAge") Integer minAge,
                          @Param("maxAge") Integer maxAge,
                          @Param("isOnline") Boolean isOnline,
                          @Param("cityCode") String cityCode,
                          @Param("offset") Integer offset,
                          @Param("pageSize") Integer pageSize);
}

// SkillMapper.java
@Mapper
public interface SkillMapper extends BaseMapper<Skill> {

    /**
     * 按类别统计技能
     */
    @Select("SELECT skill_name AS name, COUNT(*) AS count "
        + "FROM skills "
        + "WHERE deleted = 0 AND is_online = 1 "
        + "GROUP BY skill_name "
        + "ORDER BY count DESC")
    List<SkillCategoryDTO> getSkillCategories();
}

// TagMapper.java (新建)
@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    @Select("SELECT * FROM user_tags WHERE deleted = 0 ORDER BY hot_count DESC LIMIT 20")
    List<Tag> getAllTags();
}
```

2. **Service 层**: 业务逻辑实现
```java
// RemoteAppUserServiceImpl.java
@Service
@DubboService
@RequiredArgsConstructor
public class RemoteAppUserServiceImpl implements RemoteAppUserService {

    private final UserMapper userMapper;
    private final SkillMapper skillMapper;
    private final TagMapper tagMapper;

    @Override
    public PageResult<UserProfileDTO> filterUsers(UserFilterQueryDTO query) {
        // 查询用户列表
        List<User> users = userMapper.filterUsers(
            query.getGender(),
            query.getMinAge(),
            query.getMaxAge(),
            query.getIsOnline(),
            query.getCityCode(),
            (query.getPageNum() - 1) * query.getPageSize(),
            query.getPageSize()
        );

        // 统计总数
        Integer total = userMapper.countFilterUsers(query);

        // 转换为 DTO
        List<UserProfileDTO> dtos = users.stream()
            .map(this::convertToUserProfileDTO)
            .collect(Collectors.toList());

        return new PageResult<>(total, dtos, query.getPageNum() * query.getPageSize() < total);
    }

    @Override
    public List<TagDTO> getAllUserTags() {
        return tagMapper.getAllTags().stream()
            .map(tag -> new TagDTO(tag.getName(), tag.getHotCount()))
            .collect(Collectors.toList());
    }

    @Override
    public List<SkillCategoryDTO> getSkillsByCategory(String category) {
        return skillMapper.getSkillCategories();
    }
}
```

#### 2.3 改造 BFF Service (0.5 天)

**位置**: `xypai-aggregation/xypai-app-bff/src/main/java/org/dromara/appbff/service/impl/`

**实现步骤**:

```java
// HomeFilterServiceImpl.java (重写)
@Service
@RequiredArgsConstructor
public class HomeFilterServiceImpl implements HomeFilterService {

    @DubboReference
    private RemoteAppUserService remoteAppUserService; // ✅ RPC 调用

    @Override
    public FilterConfigVO getFilterConfig(String type) {
        // ✅ RPC 获取技能列表
        List<SkillCategoryDTO> skills = remoteAppUserService.getSkillsByCategory(type);

        // ✅ RPC 获取标签列表
        List<TagDTO> tags = remoteAppUserService.getAllUserTags();

        // 转换为 VO
        return FilterConfigVO.builder()
            .ageRange(FilterConfigVO.AgeRange.builder().min(18).max(null).build())
            .genderOptions(buildGenderOptions())
            .statusOptions(buildStatusOptions())
            .skillOptions(convertToSkillOptions(skills))  // ✅ 真实数据
            .tagOptions(convertToTagOptions(tags))        // ✅ 真实数据
            .build();
    }

    @Override
    public FilterResultVO applyFilter(FilterApplyDTO dto) {
        // 转换 DTO
        UserFilterQueryDTO query = convertToQuery(dto);

        // ✅ RPC 调用筛选用户
        PageResult<UserProfileDTO> result = remoteAppUserService.filterUsers(query);

        // 转换为 VO
        return FilterResultVO.builder()
            .total(result.getTotal())
            .hasMore(result.getHasMore())
            .list(convertToUserCardList(result.getRecords())) // ✅ 真实数据
            .appliedFilters(buildAppliedFilters(dto))
            .build();
    }
}
```

#### 2.4 测试验证 (0.5 天)

**测试步骤**:
1. 编译并安装 `xypai-api-appuser` 模块
2. 重启 `xypai-user` 服务
3. 重启 `xypai-app-bff` 服务
4. 运行 `Page02_FilterTest` 测试
5. 验证筛选功能正常
6. 性能测试

**验收标准**:
- ✅ 所有测试通过
- ✅ 返回真实筛选结果
- ✅ 性别/年龄/技能筛选正常
- ✅ 响应时间 < 300ms

---

### Phase 3: Page 04 搜索功能 RPC 改造 (3-4 天)

**目标**: 将搜索功能从 Mock 数据改为 RPC 调用

**涉及接口**:
1. `GET /api/home/search/init` - 搜索初始化
2. `GET /api/home/search/suggest` - 搜索建议
3. `GET /api/home/search/history` - 搜索历史
4. `DELETE /api/home/search/history` - 删除搜索历史
5. `POST /api/home/search/history/clear` - 清空搜索历史
6. `GET /api/v1/search/all` - 综合搜索
7. `GET /api/v1/search/user` - 用户搜索
8. `GET /api/v1/search/order` - 接单搜索
9. `GET /api/v1/search/topic` - 话题搜索

#### 3.1 定义 RPC 接口 (1 天)

**新增服务**: `RemoteSearchService`

**位置**: `ruoyi-api/xypai-api-appuser/` (或独立的 `xypai-api-search`)

```java
// RemoteSearchService.java
public interface RemoteSearchService {

    // ========== 搜索历史管理 ==========

    /**
     * 获取热门搜索关键词
     */
    List<String> getHotSearchKeywords(int limit);

    /**
     * 获取用户搜索历史
     */
    List<String> getUserSearchHistory(Long userId, int limit);

    /**
     * 保存搜索历史
     */
    void saveSearchHistory(Long userId, String keyword);

    /**
     * 删除单条搜索历史
     */
    void deleteSearchHistory(Long userId, String keyword);

    /**
     * 清空用户搜索历史
     */
    void clearSearchHistory(Long userId);

    /**
     * 获取推荐搜索标签
     */
    List<String> getRecommendTags(Long userId);

    // ========== 搜索功能 ==========

    /**
     * 搜索用户
     */
    List<UserDTO> searchUsers(String keyword, int limit);

    /**
     * 搜索技能/接单
     */
    List<SkillDTO> searchSkills(String keyword, int limit);

    /**
     * 搜索话题 (调用 xypai-content)
     */
    List<TopicDTO> searchTopics(String keyword, int limit);
}
```

#### 3.2 实现 RPC Provider (1.5 天)

**选项 1**: 在 `xypai-user` 服务中实现搜索功能
**选项 2**: 创建独立的 `xypai-search` 服务

**推荐**: 先在 `xypai-user` 中实现，后续可拆分

**数据库设计**:

```sql
-- 搜索历史表
CREATE TABLE search_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    keyword VARCHAR(100) NOT NULL COMMENT '搜索关键词',
    search_type VARCHAR(20) DEFAULT 'all' COMMENT '搜索类型',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_keyword (keyword),
    INDEX idx_created_at (created_at)
) COMMENT '搜索历史表';

-- 热门搜索表 (可选，可用 Redis 替代)
CREATE TABLE hot_search_keywords (
    id INT PRIMARY KEY AUTO_INCREMENT,
    keyword VARCHAR(100) NOT NULL UNIQUE,
    search_count INT DEFAULT 0 COMMENT '搜索次数',
    hot_score DECIMAL(10,2) DEFAULT 0 COMMENT '热度分数',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_hot_score (hot_score)
) COMMENT '热门搜索关键词';
```

**Mapper 实现**:

```java
// SearchHistoryMapper.java
@Mapper
public interface SearchHistoryMapper extends BaseMapper<SearchHistory> {

    @Select("SELECT DISTINCT keyword FROM search_history "
        + "WHERE user_id = #{userId} AND deleted = 0 "
        + "ORDER BY created_at DESC LIMIT #{limit}")
    List<String> getUserSearchHistory(@Param("userId") Long userId,
                                      @Param("limit") int limit);

    @Delete("DELETE FROM search_history WHERE user_id = #{userId} AND keyword = #{keyword}")
    void deleteSearchHistory(@Param("userId") Long userId,
                            @Param("keyword") String keyword);

    @Delete("DELETE FROM search_history WHERE user_id = #{userId}")
    void clearSearchHistory(@Param("userId") Long userId);
}

// HotSearchMapper.java
@Mapper
public interface HotSearchMapper extends BaseMapper<HotSearchKeyword> {

    @Select("SELECT keyword FROM hot_search_keywords "
        + "ORDER BY hot_score DESC LIMIT #{limit}")
    List<String> getHotKeywords(@Param("limit") int limit);

    @Update("UPDATE hot_search_keywords SET search_count = search_count + 1 "
        + "WHERE keyword = #{keyword}")
    void incrementSearchCount(@Param("keyword") String keyword);
}

// UserMapper (新增搜索方法)
@Select("SELECT * FROM users WHERE deleted = 0 "
    + "AND (nickname LIKE CONCAT('%', #{keyword}, '%') "
    + "     OR bio LIKE CONCAT('%', #{keyword}, '%')) "
    + "LIMIT #{limit}")
List<User> searchUsers(@Param("keyword") String keyword,
                      @Param("limit") int limit);

// SkillMapper (新增搜索方法)
@Select("SELECT * FROM skills WHERE deleted = 0 AND is_online = 1 "
    + "AND (skill_name LIKE CONCAT('%', #{keyword}, '%') "
    + "     OR game_name LIKE CONCAT('%', #{keyword}, '%')) "
    + "LIMIT #{limit}")
List<Skill> searchSkills(@Param("keyword") String keyword,
                        @Param("limit") int limit);
```

#### 3.3 改造 BFF Service (1 天)

```java
// HomeSearchServiceImpl.java
@Service
@RequiredArgsConstructor
public class HomeSearchServiceImpl implements HomeSearchService {

    @DubboReference
    private RemoteSearchService remoteSearchService; // ✅ RPC

    @Override
    public SearchInitVO getSearchInit() {
        Long userId = StpUtil.getLoginIdAsLong();

        // ✅ RPC 获取热门搜索
        List<String> hotSearches = remoteSearchService.getHotSearchKeywords(10);

        // ✅ RPC 获取搜索历史
        List<String> history = remoteSearchService.getUserSearchHistory(userId, 10);

        // ✅ RPC 获取推荐标签
        List<String> tags = remoteSearchService.getRecommendTags(userId);

        return SearchInitVO.builder()
            .hotSearches(hotSearches)   // ✅ 真实数据
            .searchHistory(history)     // ✅ 真实数据
            .recommendTags(tags)        // ✅ 真实数据
            .build();
    }
}

// HomeSearchResultServiceImpl.java
@Service
@RequiredArgsConstructor
public class HomeSearchResultServiceImpl implements HomeSearchResultService {

    @DubboReference
    private RemoteSearchService remoteSearchService;

    @DubboReference
    private RemoteContentService remoteContentService;

    @Override
    public SearchAllResultVO searchAll(SearchQueryDTO queryDTO) {
        String keyword = queryDTO.getKeyword();

        // ✅ 保存搜索历史
        Long userId = StpUtil.getLoginIdAsLong();
        remoteSearchService.saveSearchHistory(userId, keyword);

        // ✅ 并行 RPC 调用 (性能优化)
        CompletableFuture<List<UserDTO>> userFuture =
            CompletableFuture.supplyAsync(() ->
                remoteSearchService.searchUsers(keyword, 3));

        CompletableFuture<List<SkillDTO>> skillFuture =
            CompletableFuture.supplyAsync(() ->
                remoteSearchService.searchSkills(keyword, 3));

        CompletableFuture<List<TopicDTO>> topicFuture =
            CompletableFuture.supplyAsync(() ->
                remoteContentService.searchTopics(keyword, 3));

        // 等待所有结果
        CompletableFuture.allOf(userFuture, skillFuture, topicFuture).join();

        return SearchAllResultVO.builder()
            .userResults(convertToUserResults(userFuture.join()))   // ✅ 真实数据
            .orderResults(convertToOrderResults(skillFuture.join())) // ✅ 真实数据
            .topicResults(convertToTopicResults(topicFuture.join())) // ✅ 真实数据
            .build();
    }
}
```

#### 3.4 测试验证 (0.5 天)

**测试步骤**:
1. 执行数据库脚本创建搜索表
2. 重启相关服务
3. 运行 `Page06_SearchTest` 测试
4. 运行 `Page07_SearchResultsTest` 测试
5. 验证搜索功能正常
6. 性能测试

**验收标准**:
- ✅ 所有测试通过
- ✅ 热门搜索返回真实数据
- ✅ 搜索历史保存正常
- ✅ 综合搜索返回正确结果
- ✅ 响应时间 < 500ms (并行调用优化后)

---

### Phase 4: Page 01 首页 Feed 流 RPC 改造 (3-4 天)

**目标**: 实现首页推荐算法，聚合多服务数据

**涉及接口**:
1. `GET /api/home/feed` - 首页用户推荐

#### 4.1 定义 RPC 接口 (0.5 天)

**扩展现有接口**: `RemoteAppUserService`, `RemoteContentService`

```java
// RemoteAppUserService.java (新增方法)
/**
 * 批量获取用户资料
 */
List<UserProfileDTO> batchGetUserProfiles(List<Long> userIds);

/**
 * 批量获取用户技能
 */
Map<Long, List<SkillDTO>> batchGetUserSkills(List<Long> userIds);

/**
 * 批量查询关注状态
 */
Map<Long, Boolean> batchGetFollowStatus(Long currentUserId, List<Long> targetUserIds);

/**
 * 获取推荐用户列表 (基础推荐)
 */
List<Long> getRecommendedUserIds(Long currentUserId, String type, int limit);

// RemoteContentService.java (新建)
/**
 * 批量获取用户动态数量
 */
Map<Long, Integer> batchGetUserFeedCount(List<Long> userIds);
```

#### 4.2 实现推荐算法 (1.5 天)

**推荐策略**:
1. **距离优先**: 附近的用户优先推荐
2. **在线优先**: 在线用户优先展示
3. **活跃度**: 有技能、有动态的用户优先
4. **多样性**: 不同技能类别混合推荐

**实现位置**: `xypai-modules/xypai-user/service/impl/RecommendationServiceImpl.java`

```java
@Service
public class RecommendationServiceImpl {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SkillMapper skillMapper;

    public List<Long> getRecommendedUserIds(Long currentUserId, String type, int limit) {
        // 1. 获取当前用户位置
        User currentUser = userMapper.selectById(currentUserId);

        // 2. 查询附近有技能的用户
        List<User> nearbyUsers = userMapper.findNearbyUsersWithSkills(
            currentUser.getLatitude(),
            currentUser.getLongitude(),
            type,
            limit * 3 // 多查询一些，用于排序筛选
        );

        // 3. 综合评分排序
        List<UserScore> scores = nearbyUsers.stream()
            .map(user -> calculateUserScore(user, currentUser))
            .sorted(Comparator.comparingDouble(UserScore::getScore).reversed())
            .limit(limit)
            .collect(Collectors.toList());

        return scores.stream()
            .map(UserScore::getUserId)
            .collect(Collectors.toList());
    }

    private UserScore calculateUserScore(User user, User currentUser) {
        double score = 0;

        // 在线状态 (权重: 1000)
        if (user.getIsOnline()) {
            score += 1000;
        }

        // 距离评分 (权重: 500, 距离越近分数越高)
        double distance = calculateDistance(user, currentUser);
        score += Math.max(0, 500 - distance / 100);

        // 有技能 (权重: 300)
        if (user.getSkillCount() > 0) {
            score += 300;
        }

        // 粉丝数 (权重: 200)
        score += Math.min(200, user.getFansCount() / 10);

        // 动态数 (权重: 100)
        score += Math.min(100, user.getFeedCount() / 5);

        return new UserScore(user.getUserId(), score);
    }
}
```

#### 4.3 改造 BFF Service (1 天)

```java
// HomeFeedServiceImpl.java (新建)
@Service
@RequiredArgsConstructor
public class HomeFeedServiceImpl implements HomeFeedService {

    @DubboReference
    private RemoteAppUserService remoteAppUserService;

    @DubboReference
    private RemoteContentService remoteContentService;

    @Override
    public HomeFeedResultVO getHomeFeed(HomeFeedQueryDTO queryDTO) {
        Long currentUserId = StpUtil.getLoginIdAsLong();

        // 1. ✅ RPC 获取推荐用户 ID 列表
        List<Long> userIds = remoteAppUserService.getRecommendedUserIds(
            currentUserId,
            queryDTO.getType(),
            queryDTO.getPageSize()
        );

        if (userIds.isEmpty()) {
            return buildEmptyResult();
        }

        // 2. ✅ 批量 RPC 调用 (并行优化)
        CompletableFuture<List<UserProfileDTO>> profilesFuture =
            CompletableFuture.supplyAsync(() ->
                remoteAppUserService.batchGetUserProfiles(userIds));

        CompletableFuture<Map<Long, List<SkillDTO>>> skillsFuture =
            CompletableFuture.supplyAsync(() ->
                remoteAppUserService.batchGetUserSkills(userIds));

        CompletableFuture<Map<Long, Integer>> feedCountFuture =
            CompletableFuture.supplyAsync(() ->
                remoteContentService.batchGetUserFeedCount(userIds));

        CompletableFuture<Map<Long, Boolean>> followFuture =
            CompletableFuture.supplyAsync(() ->
                remoteAppUserService.batchGetFollowStatus(currentUserId, userIds));

        // 3. 等待所有 RPC 完成
        CompletableFuture.allOf(profilesFuture, skillsFuture, feedCountFuture, followFuture).join();

        // 4. 聚合数据
        List<UserCardVO> userCards = buildUserCards(
            profilesFuture.join(),
            skillsFuture.join(),
            feedCountFuture.join(),
            followFuture.join()
        );

        return HomeFeedResultVO.builder()
            .total((long) userCards.size())
            .hasMore(userCards.size() >= queryDTO.getPageSize())
            .list(userCards) // ✅ 真实聚合数据
            .build();
    }
}
```

#### 4.4 测试验证 (1 天)

**测试步骤**:
1. 准备测试数据 (用户、技能、动态)
2. 重启相关服务
3. 创建 `Page01_HomeFeedTest` 测试
4. 验证推荐算法准确性
5. 性能测试

**验收标准**:
- ✅ 测试通过
- ✅ 推荐结果符合算法逻辑
- ✅ 在线用户优先展示
- ✅ 附近用户优先展示
- ✅ 响应时间 < 500ms

---

## 📋 总体时间表

| Phase | 任务 | 时间 | 依赖服务 | 优先级 |
|-------|------|------|---------|--------|
| **Phase 0** | 准备工作 | **已完成** ✅ | - | - |
| **Phase 1** | Page 05 验证 | **0.5 天** | xypai-user | 🔥 最高 |
| **Phase 2** | Page 02 筛选 RPC | **2-3 天** | xypai-user | 🔥 高 |
| **Phase 3** | Page 04 搜索 RPC | **3-4 天** | xypai-user, xypai-content | 🔥 高 |
| **Phase 4** | Page 01 首页 Feed | **3-4 天** | xypai-user, xypai-content | 🔥 高 |
| **总计** | | **9-12 天** | | |

---

## ✅ 验收标准

### 代码质量
- [ ] 所有 Mock 数据已移除
- [ ] 所有接口使用 RPC 调用
- [ ] 代码遵循阿里巴巴 Java 规范
- [ ] 没有硬编码和魔法值

### 功能完整性
- [ ] 所有页面测试通过
- [ ] 筛选功能完全正常
- [ ] 搜索功能完全正常
- [ ] 首页推荐算法准确

### 性能指标
- [ ] 单接口响应时间 < 500ms (P99)
- [ ] 批量 RPC 调用使用并行优化
- [ ] 热点数据使用 Redis 缓存

### 可靠性
- [ ] RPC 调用有超时控制 (3秒)
- [ ] RPC 失败有降级策略
- [ ] 日志记录完整

### 文档完善
- [ ] 接口文档更新 (Swagger)
- [ ] RPC 接口文档完善
- [ ] 快速理解文档更新

---

## 🎯 关键成功因素

### 技术要点
1. **批量 RPC 调用**: 减少网络往返 (n → 1)
2. **并行调用**: 使用 `CompletableFuture` 并行调用多个 RPC
3. **缓存优化**: 热门搜索、筛选配置使用 Redis 缓存
4. **VO 转换**: RPC VO → BFF VO 转换逻辑清晰

### 开发流程
1. **API 先行**: 先定义 RPC 接口，再实现
2. **边测边改**: 每完成一个功能立即测试
3. **小步快跑**: 每个 Phase 独立交付
4. **代码审查**: 每个 Phase 完成后进行代码审查

### 风险控制
1. **服务依赖**: 确保依赖服务稳定运行
2. **数据准备**: 提前准备足够的测试数据
3. **性能瓶颈**: 及时发现和优化性能问题
4. **回滚方案**: 保留 Mock 代码作为备份

---

## 📝 下一步行动

### 立即执行 (今天)
1. ✅ 重启 xypai-app-bff 服务 (修复包结构)
2. [ ] 执行 Page 05 SQL 测试数据
3. [ ] 运行 Page 05 测试验证

### 本周目标
4. [ ] 完成 Phase 2 (筛选功能 RPC 改造)
5. [ ] 开始 Phase 3 (搜索功能 RPC 改造)

### 下周目标
6. [ ] 完成 Phase 3 (搜索功能)
7. [ ] 完成 Phase 4 (首页 Feed)
8. [ ] 整体测试验证

---

**文档版本**: v1.0
**创建时间**: 2025-11-24
**负责人**: Claude Code + 开发团队
**状态**: ✅ 计划完成，等待执行

**最后更新**: 2025-11-24 21:30
