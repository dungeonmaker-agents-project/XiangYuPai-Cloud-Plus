# Content Module Implementation Strategy

> **Project**: XiangYuPai Content Modules
> **Date**: 2025-11-14
> **Tech Lead**: Backend Architecture Team
> **Target**: Production-Ready Backend Services

---

## 📋 Executive Summary

This document outlines the comprehensive implementation strategy for **5 backend microservices** based on the API documentation specifications:

1. **xypai-content** (ContentService) - Feed流、评论、话题、互动
2. **xypai-search** (SearchService) - 搜索引擎、建议、历史
3. **xypai-service** (ServiceService) - 技能服务、评价
4. **xypai-activity** (ActivityService) - 组局中心、报名管理
5. **xypai-home** (HomeService) - 首页Feed流、筛选、推荐

**Total Deliverables**:
- 5 Microservices (Spring Boot 3.2.0 + Spring Cloud)
- 42+ REST API Endpoints
- 15+ RPC (Dubbo) Interfaces
- 12+ Database Tables
- Complete Redis Caching Layer
- Elasticsearch Integration (Search)
- Comprehensive Unit Tests

---

## 🏗️ Architecture Overview

### Service Topology

```
                          ┌─────────────────┐
                          │  API Gateway    │
                          │   (Port 8080)   │
                          └────────┬────────┘
                                   │
                 ┌─────────────────┼─────────────────┐
                 │                 │                 │
         ┌───────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐
         │ xypai-home   │  │xypai-content│  │xypai-search │
         │  (Port 9402) │  │ (Port 9403) │  │ (Port 9407) │
         └──────┬───────┘  └──────┬──────┘  └──────┬──────┘
                │                 │                 │
         ┌──────▼──────┐  ┌──────▼──────┐          │
         │xypai-service│  │xypai-activity│          │
         │  (Port 9409)│  │ (Port 9408) │          │
         └──────┬──────┘  └──────┬──────┘          │
                │                 │                 │
                └────────┬────────┴─────────────────┘
                         │
              ┌──────────▼──────────┐
              │   Infrastructure    │
              ├─────────────────────┤
              │ • MySQL 8.0+        │
              │ • Redis 7.0+        │
              │ • Nacos Registry    │
              │ • Elasticsearch 8.x │
              └─────────────────────┘
```

### Technology Matrix

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| **Language** | Java | 21 (LTS) | Latest stable JDK |
| **Framework** | Spring Boot | 3.2.0 | Microservice foundation |
| **Cloud** | Spring Cloud | 2023.0.3 | Cloud-native patterns |
| **Registry** | Nacos | 2.x | Service discovery + config |
| **RPC** | Apache Dubbo | 3.x | High-performance RPC |
| **Database** | MySQL | 8.0+ | Persistent storage |
| **ORM** | MyBatis Plus | 3.5.7 | Database operations |
| **Cache** | Redis | 7.0+ | Hot data caching |
| **Search** | Elasticsearch | 8.x | Full-text search |
| **Auth** | Sa-Token | Latest | JWT + permissions |
| **API Doc** | Knife4j | Latest | Swagger UI enhanced |

---

## 📦 Module Structure

### 1. xypai-content (ContentService)

**Purpose**: 核心内容管理服务 - 动态、评论、话题、互动

**Port**: 9403
**Database**: xypai_content

**API Endpoints (15)**:

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/content/feed/{tabType}` | 获取动态列表(follow/hot/local) |
| GET | `/api/v1/content/detail/{feedId}` | 获取动态详情 |
| POST | `/api/v1/content/publish` | 发布动态 |
| DELETE | `/api/v1/content/{feedId}` | 删除动态 |
| GET | `/api/v1/content/comments/{feedId}` | 获取评论列表 |
| POST | `/api/v1/content/comment` | 发布评论 |
| DELETE | `/api/v1/content/comment/{commentId}` | 删除评论 |
| GET | `/api/v1/content/topics/hot` | 获取热门话题 |
| GET | `/api/v1/content/topics/search` | 搜索话题 |
| POST | `/api/v1/interaction/like` | 点赞/取消点赞 |
| POST | `/api/v1/interaction/collect` | 收藏/取消收藏 |
| POST | `/api/v1/interaction/share` | 分享 |
| POST | `/api/v1/interaction/distance/batch` | 批量距离计算 |
| POST | `/api/v1/content/report` | 举报内容/用户 |
| GET | `/api/v1/user/reports` | 查看我的举报记录 |

**RPC Interfaces (3)**:
- `getUserFeeds()` - 获取用户动态列表
- `getUserCollections()` - 获取用户收藏列表
- `getUserLikes()` - 获取用户点赞列表

**Database Tables (8)**:
- feed - 动态表
- comment - 评论表
- topic - 话题表
- feed_topic - 动态话题关联表
- feed_media - 动态媒体关联表
- like - 点赞表
- collection - 收藏表
- share - 分享表

---

### 2. xypai-search (SearchService)

**Purpose**: 搜索引擎服务 - 综合搜索、建议、历史

**Port**: 9407
**Database**: xypai_search + Elasticsearch

**API Endpoints (8)**:

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/search/init` | 搜索初始化(历史+热搜) |
| GET | `/api/search/suggest` | 实时搜索建议 |
| POST | `/api/search/search` | 执行综合搜索 |
| GET | `/api/search/all` | 全部Tab结果 |
| GET | `/api/search/users` | 用户Tab结果 |
| GET | `/api/search/orders` | 下单Tab结果 |
| GET | `/api/search/topics` | 话题Tab结果 |
| DELETE | `/api/search/history` | 删除搜索历史 |

**Database Tables (2)**:
- search_history - 搜索历史表
- hot_search - 热搜表

---

### 3. xypai-service (ServiceService)

**Purpose**: 技能服务管理 - 服务列表、详情、评价

**Port**: 9409
**Database**: xypai_service

**API Endpoints (4)**:

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/service/list` | 获取服务列表 |
| GET | `/api/service/detail` | 获取服务详情 |
| GET | `/api/service/reviews` | 获取评价列表 |
| POST | `/api/service/review/submit` | 提交评价 |

**Database Tables (3)**:
- service - 服务表
- service_review - 服务评价表
- service_review_stats - 评价统计表

---

### 4. xypai-activity (ActivityService)

**Purpose**: 组局中心服务 - 活动发布、报名、审核

**Port**: 9408
**Database**: xypai_activity

**API Endpoints (8)**:

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/activity/list` | 获取活动列表 |
| GET | `/api/activity/publish/config` | 获取发布配置 |
| POST | `/api/activity/publish` | 发布活动 |
| GET | `/api/activity/detail` | 获取活动详情 |
| POST | `/api/activity/register` | 报名参加 |
| POST | `/api/activity/registration/approve` | 审核报名 |
| POST | `/api/activity/register/cancel` | 取消报名 |
| POST | `/api/activity/share` | 分享活动 |

**Database Tables (2)**:
- activity - 活动表
- activity_registration - 活动报名表

---

### 5. xypai-home (HomeService)

**Purpose**: 首页Feed流服务 - 推荐、筛选、限时专享

**Port**: 9402
**Database**: xypai_content (共享)

**API Endpoints (8)**:

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/home/init` | 首页初始化 |
| GET | `/api/home/experts` | 明日专家推荐 |
| GET | `/api/home/topic-banner` | 你什么名模块 |
| GET | `/api/home/feed` | 内容Feed流 |
| GET | `/api/home/filter/config` | 获取筛选配置 |
| POST | `/api/home/filter/apply` | 应用筛选条件 |
| GET | `/api/home/filter/results` | 获取筛选结果 |
| GET | `/api/home/limited-time/list` | 限时专享列表 |

**Database Tables (3)**:
- feed_config - Feed流配置表
- filter_config - 筛选配置表
- limited_time_config - 限时专享配置表

---

## 🔧 Implementation Plan

### Phase 1: Foundation Setup (Week 1)

#### Step 1.1: Create Module Structure
```bash
cd /e/Users/Administrator/Documents/GitHub/RuoYi-Cloud-Plus

# Create 5 new modules
mkdir xypai-content xypai-search xypai-service xypai-activity xypai-home

# Each module will have:
# ├── src/main/java/org/dromara/{module}/
# │   ├── controller/          # REST APIs
# │   ├── service/             # Business logic
# │   ├── mapper/              # MyBatis mappers
# │   ├── domain/              # Data models
# │   │   ├── entity/          # Database entities
# │   │   ├── dto/             # Request DTOs
# │   │   └── vo/              # Response VOs
# │   └── config/              # Configuration
# ├── src/main/resources/
# │   ├── application.yml
# │   ├── bootstrap.yml
# │   └── mapper/              # MyBatis XML (if needed)
# └── pom.xml
```

#### Step 1.2: Create API Interface Module
```bash
cd ruoyi-api
mkdir xypai-api-content xypai-api-search xypai-api-service xypai-api-activity xypai-api-home

# Each API module contains:
# - Remote service interfaces (Dubbo)
# - Shared DTOs/VOs
# - Constants and enums
```

#### Step 1.3: Database Schema Creation
```sql
-- Create databases
CREATE DATABASE IF NOT EXISTS xypai_content CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS xypai_search CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS xypai_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS xypai_activity CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

### Phase 2: Core Service Implementation (Week 2-3)

#### Priority Order:
1. **xypai-content** (Most Critical) - 核心内容服务
2. **xypai-home** - 首页Feed流
3. **xypai-search** - 搜索服务
4. **xypai-service** - 技能服务
5. **xypai-activity** - 组局服务

#### Implementation Pattern for Each Service:

**Step 2.1: Create Entities**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("feed")
public class Feed implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private Integer type;
    private String title;
    private String content;

    // Spatial data
    private String locationName;
    private Double longitude;
    private Double latitude;

    // Statistics
    private Integer likeCount;
    private Integer commentCount;
    private Integer shareCount;

    // Audit fields
    @TableLogic
    private Integer deleted;
    @Version
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**Step 2.2: Create DTOs and VOs**
```java
// Request DTO
@Data
@ApiModel("发布动态请求")
public class FeedPublishDto {
    @NotNull
    @ApiModelProperty("动态类型")
    private Integer type;

    @NotBlank
    @Length(min = 1, max = 1000)
    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("媒体ID列表")
    private List<String> mediaIds;

    @ApiModelProperty("话题名称列表")
    private List<String> topicNames;
}

// Response VO
@Data
@Builder
@ApiModel("动态详情")
public class FeedDetailVo {
    private Long id;
    private UserBasicVo userInfo;
    private String content;
    private List<MediaVo> mediaList;
    private Integer likeCount;
    private Boolean isLiked;
    private LocalDateTime createdAt;
}
```

**Step 2.3: Create Mapper**
```java
@Mapper
public interface FeedMapper extends BaseMapper<Feed> {

    /**
     * 获取附近动态 (Spatial query)
     */
    @Select("""
        SELECT *,
          ST_Distance_Sphere(
            POINT(longitude, latitude),
            POINT(#{longitude}, #{latitude})
          ) / 1000 AS distance
        FROM feed
        WHERE ST_Distance_Sphere(
          POINT(longitude, latitude),
          POINT(#{longitude}, #{latitude})
        ) <= #{radiusMeters}
          AND deleted = 0
        ORDER BY distance
        LIMIT #{limit}
        """)
    List<Feed> selectNearbyFeeds(
        @Param("longitude") Double longitude,
        @Param("latitude") Double latitude,
        @Param("radiusMeters") Integer radiusMeters,
        @Param("limit") Integer limit
    );
}
```

**Step 2.4: Create Service**
```java
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements IFeedService {

    private final FeedMapper feedMapper;
    private final RedisService redisService;

    @Override
    public Page<FeedListVo> getFeedList(FeedQueryDto dto) {
        // 1. Build query
        Page<Feed> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        // 2. Try cache first
        String cacheKey = buildCacheKey(dto);
        Page<FeedListVo> cached = redisService.getCacheObject(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 3. Query database
        LambdaQueryWrapper<Feed> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getType() != null, Feed::getType, dto.getType())
               .orderByDesc(Feed::getCreatedAt);

        Page<Feed> result = feedMapper.selectPage(page, wrapper);

        // 4. Convert to VOs
        List<FeedListVo> voList = result.getRecords().stream()
            .map(this::convertToListVo)
            .toList();

        // 5. Cache result
        Page<FeedListVo> voPage = new Page<>();
        voPage.setRecords(voList);
        voPage.setTotal(result.getTotal());

        redisService.setCacheObject(cacheKey, voPage, Duration.ofMinutes(5));

        return voPage;
    }
}
```

**Step 2.5: Create Controller**
```java
@RestController
@RequestMapping("/api/v1/content")
@Tag(name = "内容管理")
@RequiredArgsConstructor
public class FeedController {

    private final IFeedService feedService;

    @GetMapping("/feed/{tabType}")
    @Operation(summary = "获取动态列表")
    public R<Page<FeedListVo>> getFeedList(
        @PathVariable String tabType,
        @Validated FeedQueryDto dto
    ) {
        return R.ok(feedService.getFeedList(dto));
    }

    @PostMapping("/publish")
    @Operation(summary = "发布动态")
    @SaCheckLogin
    public R<FeedPublishVo> publishFeed(@Validated @RequestBody FeedPublishDto dto) {
        return R.ok(feedService.publishFeed(dto));
    }
}
```

---

### Phase 3: Integration & Testing (Week 4)

#### Step 3.1: Dubbo RPC Integration
```java
// Provider (Content Service)
@DubboService
@Service
@RequiredArgsConstructor
public class RemoteContentServiceImpl implements RemoteContentService {

    private final IFeedService feedService;

    @Override
    public R<List<FeedListVo>> getUserFeeds(Long userId, Integer limit) {
        return R.ok(feedService.getUserFeeds(userId, limit));
    }
}

// Consumer (User Service)
@Service
@RequiredArgsConstructor
public class UserProfileService {

    @DubboReference
    private RemoteContentService remoteContentService;

    public UserProfileVo getProfile(Long userId) {
        // Get user's feeds via RPC
        R<List<FeedListVo>> feedResult = remoteContentService.getUserFeeds(userId, 10);
        // Build profile...
    }
}
```

#### Step 3.2: Redis Caching Strategy
```java
@Component
@RequiredArgsConstructor
public class FeedCacheService {

    private final RedisService redisService;

    // Cache keys
    private static final String FEED_DETAIL = "feed:detail:";
    private static final String FEED_LIST = "feed:list:";
    private static final String HOT_TOPICS = "topic:hot";

    public void cacheFeedDetail(Long feedId, FeedDetailVo vo) {
        redisService.setCacheObject(
            FEED_DETAIL + feedId,
            vo,
            Duration.ofMinutes(10)
        );
    }

    public void invalidateFeedCache(Long feedId) {
        redisService.deleteObject(FEED_DETAIL + feedId);
        redisService.deleteKeys(FEED_LIST + "*");
    }
}
```

#### Step 3.3: Unit Tests
```java
@SpringBootTest
@Transactional
class FeedServiceTest {

    @Autowired
    private IFeedService feedService;

    @Test
    void testPublishFeed() {
        FeedPublishDto dto = new FeedPublishDto();
        dto.setType(1);
        dto.setContent("Test content");

        FeedPublishVo result = feedService.publishFeed(dto);

        assertNotNull(result.getFeedId());
        assertTrue(result.getCreatedAt() > 0);
    }

    @Test
    void testGetFeedList() {
        FeedQueryDto dto = new FeedQueryDto();
        dto.setPageNum(1);
        dto.setPageSize(10);

        Page<FeedListVo> result = feedService.getFeedList(dto);

        assertNotNull(result);
        assertTrue(result.getTotal() >= 0);
    }
}
```

---

## 📚 Code Quality Standards

### 1. Naming Conventions
- **Entity**: `Feed`, `Comment`, `Topic`
- **DTO**: `FeedPublishDto`, `FeedQueryDto`
- **VO**: `FeedDetailVo`, `FeedListVo`
- **Service**: `IFeedService`, `FeedServiceImpl`
- **Controller**: `FeedController`
- **Mapper**: `FeedMapper`

### 2. Documentation Requirements
- All public methods must have JavaDoc
- All API endpoints must have Swagger annotations
- All DTOs must have validation annotations
- All complex logic must have inline comments

### 3. Error Handling
```java
// Use ServiceException for business errors
if (feed == null) {
    throw new ServiceException("动态不存在");
}

// Use custom error codes
public class ContentErrorCode {
    public static final int FEED_NOT_FOUND = 40001;
    public static final int COMMENT_TOO_LONG = 40002;
    public static final int ALREADY_LIKED = 40003;
}
```

### 4. Validation
```java
@Data
@Validated
public class FeedPublishDto {
    @NotBlank(message = "内容不能为空")
    @Length(min = 1, max = 1000, message = "内容长度必须在1-1000字符之间")
    private String content;

    @Size(max = 9, message = "最多上传9张图片")
    private List<String> mediaIds;

    @Size(max = 5, message = "最多选择5个话题")
    private List<String> topicNames;
}
```

---

## 🚀 Deployment Strategy

### 1. Environment Configuration (Nacos)

**xypai-content-dev.yml**:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xypai_content?useSSL=false
    username: root
    password: ${MYSQL_PASSWORD:password}

server:
  port: 9403

dubbo:
  protocol:
    port: 20883

xypai:
  content:
    max-feed-length: 1000
    max-images: 9
```

### 2. Docker Deployment
```dockerfile
FROM openjdk:21-slim
WORKDIR /app
COPY target/xypai-content.jar app.jar
EXPOSE 9403 20883
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 3. Kubernetes Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: xypai-content
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: xypai-content
        image: xypai/content:latest
        ports:
        - containerPort: 9403
        - containerPort: 20883
        env:
        - name: NACOS_SERVER
          value: "nacos-service:8848"
```

---

## 📊 Success Metrics

### Performance Targets
- API Response Time: P95 < 200ms, P99 < 500ms
- Database Query: P95 < 100ms
- Cache Hit Rate: > 80%
- RPC Call Latency: < 50ms
- QPS Support: > 1000 req/s per service

### Quality Targets
- Code Coverage: > 80%
- Critical Path Coverage: 100%
- Zero Critical Bugs in Production
- 99.9% Service Availability

---

## 📝 Next Steps

### Immediate Actions:
1. ✅ Review and approve this strategy document
2. ✅ Setup development environment
3. ✅ Create database schemas
4. ✅ Start Phase 1 implementation

### Weekly Milestones:
- **Week 1**: Foundation setup + Database schemas
- **Week 2**: xypai-content + xypai-home implementation
- **Week 3**: xypai-search + xypai-service + xypai-activity
- **Week 4**: Integration testing + Deployment

---

**Document Owner**: Backend Architecture Team
**Last Updated**: 2025-11-14
**Status**: Ready for Implementation ✅
