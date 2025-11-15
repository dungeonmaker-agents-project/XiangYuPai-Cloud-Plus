# Backend Test Organization Plan

**Module**: xypai-content
**Test Framework**: Spring Boot Test + JUnit 5 + MockMvc
**Organization**: By Frontend Page/Flow

---

## Test Structure Overview

### Organized by Frontend Pages (3 Main Flows)

```
src/test/java/org/dromara/content/
├── base/
│   ├── BaseIntegrationTest.java           # Base test class with common setup
│   └── TestDataFactory.java               # Test data factory
│
├── page/
│   ├── DiscoveryMainPageTest.java         # Tests for 01-发现主页页面
│   ├── PublishFeedPageTest.java           # Tests for 02-发布动态页面
│   └── FeedDetailPageTest.java            # Tests for 03-动态详情页面
│
├── flow/
│   ├── CompleteUserFlowTest.java          # End-to-end user journey
│   ├── InteractionFlowTest.java           # Like/Collect/Share flows
│   └── CommentFlowTest.java               # Comment and reply flows
│
├── controller/
│   ├── FeedControllerTest.java            # Unit tests for FeedController
│   ├── CommentControllerTest.java         # Unit tests for CommentController
│   ├── InteractionControllerTest.java     # Unit tests for InteractionController
│   ├── TopicControllerTest.java           # Unit tests for TopicController
│   └── ReportControllerTest.java          # Unit tests for ReportController
│
└── service/
    ├── FeedServiceTest.java               # Unit tests for FeedService
    ├── CommentServiceTest.java            # Unit tests for CommentService
    └── ...                                # Other service tests
```

---

## Test File Mapping

### 1. Discovery Main Page (01-发现主页页面.md)

**File**: `DiscoveryMainPageTest.java`

**Test Scenarios**:
1. Get Recommend Feed List
   - Test pagination
   - Test empty result
   - Test with authentication
   - Test without authentication

2. Get Follow Feed List
   - Test requires authentication
   - Test with following users
   - Test with no following users
   - Test pagination

3. Get Hot Feed List
   - Test hot algorithm sorting
   - Test time decay
   - Test engagement weights
   - Test pagination

4. Get Local Feed List
   - Test spatial query with latitude/longitude
   - Test default 5km radius
   - Test custom radius
   - Test missing location parameters
   - Test distance calculation

5. Like/Unlike Feed
   - Test like toggle behavior
   - Test like count increment/decrement
   - Test optimistic update response
   - Test requires authentication

6. Collect/Uncollect Feed
   - Test collect toggle behavior
   - Test collect count increment/decrement
   - Test requires authentication

7. Share Feed
   - Test share with different channels
   - Test share count increment
   - Test channel validation
   - Test requires authentication
   - Test request body format

**Endpoints Tested**:
- GET /api/v1/content/feed/{tabType}
- POST /api/v1/interaction/like
- POST /api/v1/interaction/collect
- POST /api/v1/interaction/share

---

### 2. Publish Feed Page (02-发布动态页面.md)

**File**: `PublishFeedPageTest.java`

**Test Scenarios**:
1. Publish Text-Only Feed
   - Test minimum required fields
   - Test content validation (1-1000 chars)
   - Test title validation (0-50 chars)
   - Test requires authentication

2. Publish Feed with Media
   - Test with mediaIds array
   - Test max 9 images validation
   - Test max 1 video validation
   - Test mutual exclusivity (image vs video)

3. Publish Feed with Topics
   - Test topic array (max 5)
   - Test topic auto-creation
   - Test topic postCount increment
   - Test existing topic association

4. Publish Feed with Location
   - Test with locationName
   - Test with coordinates
   - Test optional location fields

5. Publish Feed with Privacy Settings
   - Test visibility: 0 (public)
   - Test visibility: 1 (friends only)
   - Test visibility: 2 (private)

6. Get Hot Topics
   - Test hot topics list
   - Test sorting (postCount, participantCount)
   - Test Redis caching
   - Test pagination

7. Search Topics
   - Test keyword search
   - Test LIKE search on name and description
   - Test keyword validation (1-20 chars)
   - Test empty results

8. Validation Errors
   - Test empty content
   - Test content too long (>1000 chars)
   - Test title too long (>50 chars)
   - Test too many media (>9)
   - Test too many topics (>5)

**Endpoints Tested**:
- POST /api/v1/content/publish
- GET /api/v1/content/topics/hot
- GET /api/v1/content/topics/search

---

### 3. Feed Detail Page (03-动态详情页面.md)

**File**: `FeedDetailPageTest.java`

**Test Scenarios**:
1. Get Feed Detail
   - Test public feed access
   - Test private feed access (owner only)
   - Test friends-only feed access
   - Test non-existent feed
   - Test canEdit/canDelete permissions
   - Test view count increment
   - Test Redis caching

2. Get Comment List
   - Test sort by time
   - Test sort by hot
   - Test sort by like count
   - Test top comments priority
   - Test nested replies (max 3 shown)
   - Test totalReplies and hasMoreReplies fields
   - Test pagination

3. Post Top-Level Comment
   - Test requires authentication
   - Test content validation (1-500 chars)
   - Test feed commentCount increment
   - Test returns commentId

4. Post Reply Comment
   - Test with parentId
   - Test with replyToUserId
   - Test parent comment replyCount increment
   - Test nested reply structure

5. Delete Comment
   - Test owner can delete
   - Test non-owner cannot delete
   - Test feed commentCount decrement
   - Test parent replyCount decrement (if applicable)
   - Test soft delete (deleted=1)

6. Delete Feed
   - Test owner can delete
   - Test non-owner cannot delete
   - Test soft delete (deleted=1)
   - Test cache invalidation

7. Submit Report
   - Test report feed
   - Test report comment
   - Test report user
   - Test 6 reason types validation
   - Test description validation (0-200 chars)
   - Test evidence images (max 3)
   - Test duplicate prevention (24-hour window)
   - Test rate limiting (10/minute)
   - Test requires authentication

8. Like Comment
   - Test like toggle on comment
   - Test comment likeCount increment/decrement

**Endpoints Tested**:
- GET /api/v1/content/detail/{feedId}
- GET /api/v1/content/comments/{feedId}
- POST /api/v1/content/comment
- DELETE /api/v1/content/comment
- DELETE /api/v1/content/{feedId}
- POST /api/v1/content/report
- POST /api/v1/interaction/like (for comments)

---

## Integration Tests (Complete Flows)

### 4. Complete User Flow Test

**File**: `CompleteUserFlowTest.java`

**Test Scenarios**:
1. New User Publishes First Feed
   - Login → Publish feed → Verify feed created → Check feed list

2. User Browses and Interacts
   - Get feed list → View detail → Like → Collect → Share → Comment

3. User Follows Topic and Posts
   - Search topics → Select topic → Publish with topic → Verify topic postCount

4. User Reports Content
   - View feed → Submit report → Verify report created → Test duplicate prevention

5. Comment Conversation Flow
   - User A comments → User B replies → User A replies back → Verify nesting

6. Privacy Flow
   - Publish private feed → Another user tries to view → Verify access denied → Owner views successfully

**Endpoints Tested**: All 13 endpoints in sequence

---

### 5. Interaction Flow Test

**File**: `InteractionFlowTest.java`

**Test Scenarios**:
1. Complete Like Flow
   - Like feed → Check isLiked=true → Unlike feed → Check isLiked=false

2. Complete Collect Flow
   - Collect feed → Check isCollected=true → Uncollect → Check isCollected=false

3. Complete Share Flow
   - Share to different channels → Verify share count increments

4. Mixed Interactions
   - Like + Collect + Share on same feed → Verify all counts

5. Concurrent Interactions
   - Multiple users interact simultaneously → Verify counts correct

---

### 6. Comment Flow Test

**File**: `CommentFlowTest.java`

**Test Scenarios**:
1. Comment Thread Creation
   - Post top-level comment → Post 3 replies → Verify nesting → Load more replies

2. Comment Deletion Flow
   - Post comment → Delete comment → Verify soft delete → Verify counts updated

3. Comment Pagination
   - Post 25 comments → Load page 1 → Load page 2 → Verify correct pagination

---

## Test Data Requirements

### Test Users
```java
// Pre-created test users in database
User testUser1 = { id: 1001, username: "testuser1", nickname: "测试用户1" }
User testUser2 = { id: 1002, username: "testuser2", nickname: "测试用户2" }
User testUser3 = { id: 1003, username: "testuser3", nickname: "测试用户3" }
```

### Test Feeds
```java
// Pre-created test feeds with different types
Feed publicFeed = { id: 2001, userId: 1001, visibility: 0, content: "Public feed" }
Feed privateFeed = { id: 2002, userId: 1001, visibility: 2, content: "Private feed" }
Feed hotFeed = { id: 2003, userId: 1002, likeCount: 100, commentCount: 50 }
```

### Test Topics
```java
// Pre-created topics
Topic hotTopic = { id: 3001, name: "王者荣耀", isHot: 1, postCount: 5678 }
Topic normalTopic = { id: 3002, name: "美食推荐", isHot: 0, postCount: 2340 }
```

---

## Test Execution Strategy

### Unit Tests (Fast)
- Run on every code change
- Mock external dependencies
- Test individual methods
- ~500ms execution time

### Integration Tests (Medium)
- Run before commit
- Use test database
- Test HTTP endpoints with MockMvc
- ~5 seconds execution time

### Flow Tests (Slow)
- Run before merge to main
- Full end-to-end scenarios
- Test complete user journeys
- ~30 seconds execution time

---

## Test Coverage Goals

- **Line Coverage**: ≥80%
- **Branch Coverage**: ≥70%
- **Controller Coverage**: 100% (all endpoints)
- **Service Coverage**: ≥85%
- **Critical Paths**: 100%

---

## Test Naming Convention

```java
// Pattern: should{ExpectedBehavior}_when{StateUnderTest}_given{Context}

@Test
void shouldReturnHotFeeds_whenTabTypeIsHot_givenValidPagination()

@Test
void shouldThrowException_whenPublishFeed_givenEmptyContent()

@Test
void shouldIncrementLikeCount_whenLikeFeed_givenNotLikedBefore()

@Test
void shouldPreventDuplicateReport_whenSubmitReport_givenReportedWithin24Hours()
```

---

## Test Assertions Strategy

### HTTP Response Assertions
```java
// Status code
resultActions.andExpect(status().isOk());

// Response structure
resultActions.andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.msg").value("success"))
            .andExpect(jsonPath("$.data").exists());

// Data fields
resultActions.andExpect(jsonPath("$.data.records").isArray())
            .andExpect(jsonPath("$.data.total").isNumber())
            .andExpect(jsonPath("$.data.current").value(1));

// Business logic
resultActions.andExpect(jsonPath("$.data.records[0].isLiked").value(true))
            .andExpect(jsonPath("$.data.records[0].likeCount").value(121));
```

### Database Assertions
```java
// Verify record created
Feed feed = feedMapper.selectById(feedId);
assertThat(feed).isNotNull();
assertThat(feed.getContent()).isEqualTo("Test content");

// Verify count incremented
assertThat(feed.getLikeCount()).isEqualTo(1);
assertThat(feed.getCommentCount()).isEqualTo(1);

// Verify soft delete
assertThat(feed.getDeleted()).isEqualTo(1);
```

---

## Summary

**Total Test Files**: 13
- Base/Utility: 2 files
- Page Tests: 3 files
- Flow Tests: 3 files
- Controller Tests: 5 files
- Service Tests: (as needed)

**Total Test Scenarios**: 50+
- Discovery Page: 15 scenarios
- Publish Page: 12 scenarios
- Detail Page: 15 scenarios
- Integration Flows: 10+ scenarios

**Endpoints Covered**: 13/13 (100%)
- All REST endpoints fully tested
- All validation rules verified
- All business logic tested
- All error scenarios covered

---

**Next Steps**:
1. Create BaseIntegrationTest.java
2. Create TestDataFactory.java
3. Implement DiscoveryMainPageTest.java
4. Implement PublishFeedPageTest.java
5. Implement FeedDetailPageTest.java
6. Implement flow tests
7. Run all tests and verify coverage
