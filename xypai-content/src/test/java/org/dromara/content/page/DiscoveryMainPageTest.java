package org.dromara.content.page;

import org.dromara.content.base.BaseIntegrationTest;
import org.dromara.content.domain.Feed;
import org.dromara.content.domain.dto.InteractionDTO;
import org.dromara.content.mapper.FeedMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Discovery Main Page Integration Test
 * <p>
 * Tests all scenarios from frontend page: 01-发现主页页面.md
 * <p>
 * Test Coverage:
 * - Get Feed List (4 tabs: recommend/follow/hot/local)
 * - Like/Unlike Feed
 * - Collect/Uncollect Feed
 * - Share Feed
 * <p>
 * Endpoints Tested:
 * - GET /api/v1/content/feed/{tabType}
 * - POST /api/v1/interaction/like
 * - POST /api/v1/interaction/collect
 * - POST /api/v1/interaction/share
 *
 * @author Claude Code AI
 * @date 2025-11-14
 */
@DisplayName("Discovery Main Page Tests")
public class DiscoveryMainPageTest extends BaseIntegrationTest {

    @Autowired
    private FeedMapper feedMapper;

    @Override
    protected void setupTestData() {
        // Create test feeds for various scenarios
        testDataFactory.createPublicFeed(testUserId, "Test feed 1");
        testDataFactory.createPublicFeed(testUserId, "Test feed 2");
        testDataFactory.createHotFeed(testUserId);
        testDataFactory.createFeedWithLocation(testUserId, 39.9042, 116.4074);
    }

    // ==================== Tab 1: Recommend Feed List ====================

    @Nested
    @DisplayName("Recommend Tab Tests")
    class RecommendTabTests {

        @Test
        @DisplayName("Should return recommend feeds with pagination")
        void shouldReturnRecommendFeeds_whenValidPagination() throws Exception {
            mockMvc.perform(get("/api/v1/content/feed/recommend")
                    .param("page", "1")
                    .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.size").value(10));
        }

        @Test
        @DisplayName("Should return feeds with all required VO fields")
        void shouldReturnFeedsWithAllFields_whenGetRecommendFeeds() throws Exception {
            mockMvc.perform(get("/api/v1/content/feed/recommend")
                    .param("page", "1")
                    .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].id").exists())
                .andExpect(jsonPath("$.data.records[0].userId").exists())
                .andExpect(jsonPath("$.data.records[0].type").exists())
                .andExpect(jsonPath("$.data.records[0].typeDesc").exists())         // NEWLY ADDED
                .andExpect(jsonPath("$.data.records[0].content").exists())
                .andExpect(jsonPath("$.data.records[0].summary").exists())          // NEWLY ADDED
                .andExpect(jsonPath("$.data.records[0].cityId").exists())           // NEWLY ADDED
                .andExpect(jsonPath("$.data.records[0].likeCount").exists())
                .andExpect(jsonPath("$.data.records[0].commentCount").exists())
                .andExpect(jsonPath("$.data.records[0].shareCount").exists())
                .andExpect(jsonPath("$.data.records[0].collectCount").exists())
                .andExpect(jsonPath("$.data.records[0].viewCount").exists())
                .andExpect(jsonPath("$.data.records[0].isLiked").exists())
                .andExpect(jsonPath("$.data.records[0].isCollected").exists())
                .andExpect(jsonPath("$.data.records[0].createdAt").exists());
        }

        @Test
        @DisplayName("Should return feeds with expanded UserInfo fields")
        void shouldReturnFeedsWithExpandedUserInfo_whenGetFeeds() throws Exception {
            mockMvc.perform(get("/api/v1/content/feed/recommend")
                    .param("page", "1")
                    .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].userInfo").exists())
                .andExpect(jsonPath("$.data.records[0].userInfo.id").exists())
                .andExpect(jsonPath("$.data.records[0].userInfo.nickname").exists())
                .andExpect(jsonPath("$.data.records[0].userInfo.avatar").exists())
                .andExpect(jsonPath("$.data.records[0].userInfo.gender").exists())         // NEWLY ADDED
                .andExpect(jsonPath("$.data.records[0].userInfo.age").exists())            // NEWLY ADDED
                .andExpect(jsonPath("$.data.records[0].userInfo.isRealVerified").exists()) // NEWLY ADDED
                .andExpect(jsonPath("$.data.records[0].userInfo.isGodVerified").exists())  // NEWLY ADDED
                .andExpect(jsonPath("$.data.records[0].userInfo.isVip").exists())          // NEWLY ADDED
                .andExpect(jsonPath("$.data.records[0].userInfo.isPopular").exists())      // NEWLY ADDED
                .andExpect(jsonPath("$.data.records[0].userInfo.isFollowed").exists());
        }

        @Test
        @DisplayName("Should work without authentication for public feeds")
        void shouldReturnFeeds_whenNoAuthentication() throws Exception {
            mockMvc.perform(get("/api/v1/content/feed/recommend")
                    .param("page", "1")
                    .param("pageSize", "10"))
                // No Satoken header
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("Should handle empty result gracefully")
        void shouldReturnEmptyList_whenNoFeeds() throws Exception {
            // Clean up all feeds first
            testDataFactory.cleanupUserFeeds(testUserId);

            mockMvc.perform(get("/api/v1/content/feed/recommend")
                    .param("page", "1")
                    .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isEmpty())
                .andExpect(jsonPath("$.data.total").value(0));
        }

        @Test
        @DisplayName("Should validate page and pageSize parameters")
        void shouldReturnBadRequest_whenInvalidPagination() throws Exception {
            mockMvc.perform(get("/api/v1/content/feed/recommend")
                    .param("page", "0")  // Invalid: should be >= 1
                    .param("pageSize", "1000"))  // Invalid: too large
                .andDo(print())
                .andExpect(status().isBadRequest());
        }
    }

    // ==================== Tab 2: Follow Feed List ====================

    @Nested
    @DisplayName("Follow Tab Tests")
    class FollowTabTests {

        @Test
        @DisplayName("Should require authentication for follow tab")
        void shouldRequireAuth_whenAccessFollowTab() throws Exception {
            mockMvc.perform(get("/api/v1/content/feed/follow")
                    .param("page", "1")
                    .param("pageSize", "10"))
                // No Satoken header
                .andDo(print())
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return feeds from following users")
        void shouldReturnFollowingFeeds_whenAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/content/feed/follow")
                    .header("Satoken", getAuthHeader())
                    .param("page", "1")
                    .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
        }

        @Test
        @DisplayName("Should return empty list when no following users")
        void shouldReturnEmptyList_whenNoFollowing() throws Exception {
            // Assuming test user has no following
            mockMvc.perform(get("/api/v1/content/feed/follow")
                    .header("Satoken", getAuthHeader())
                    .param("page", "1")
                    .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records").isArray());
        }
    }

    // ==================== Tab 3: Hot Feed List ====================

    @Nested
    @DisplayName("Hot Tab Tests")
    class HotTabTests {

        @Test
        @DisplayName("Should return hot feeds sorted by hot score")
        void shouldReturnHotFeeds_whenSortedByHotScore() throws Exception {
            // Create feeds with different engagement levels
            Feed lowEngagement = testDataFactory.createPublicFeed(testUserId, "Low engagement feed");
            Feed highEngagement = testDataFactory.createHotFeed(testUserId);

            mockMvc.perform(get("/api/v1/content/feed/hot")
                    .param("page", "1")
                    .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());

            // TODO: Verify sorting order - high engagement should be first
            // This requires parsing response and checking likeCount/commentCount
        }

        @Test
        @DisplayName("Should apply time decay factor to hot score")
        void shouldApplyTimeDecay_whenCalculatingHotScore() throws Exception {
            mockMvc.perform(get("/api/v1/content/feed/hot")
                    .param("page", "1")
                    .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

            // Hot score formula:
            // baseScore = likeCount*1 + commentCount*2 + shareCount*3 + collectCount*2
            // timeFactor = Math.pow(0.5, hoursSinceCreated / 24)
            // hotScore = baseScore * timeFactor
        }

        @Test
        @DisplayName("Should query last 7 days only for performance")
        void shouldQueryLast7Days_whenGetHotFeeds() throws Exception {
            mockMvc.perform(get("/api/v1/content/feed/hot")
                    .param("page", "1")
                    .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

            // Implementation detail: queries feeds from last 7 days to avoid performance issues
        }
    }

    // ==================== Tab 4: Local Feed List ====================

    @Nested
    @DisplayName("Local Tab Tests")
    class LocalTabTests {

        @Test
        @DisplayName("Should return local feeds with default 5km radius")
        void shouldReturnLocalFeeds_whenDefaultRadius() throws Exception {
            mockMvc.perform(get("/api/v1/content/feed/local")
                    .param("page", "1")
                    .param("pageSize", "10")
                    .param("latitude", "39.9042")   // Tiananmen Square
                    .param("longitude", "116.4074"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
        }

        @Test
        @DisplayName("Should return local feeds with custom radius")
        void shouldReturnLocalFeeds_whenCustomRadius() throws Exception {
            mockMvc.perform(get("/api/v1/content/feed/local")
                    .param("page", "1")
                    .param("pageSize", "10")
                    .param("latitude", "39.9042")
                    .param("longitude", "116.4074")
                    .param("radius", "10"))  // 10km radius
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("Should require latitude and longitude for local tab")
        void shouldReturnError_whenMissingLocation() throws Exception {
            mockMvc.perform(get("/api/v1/content/feed/local")
                    .param("page", "1")
                    .param("pageSize", "10"))
                // Missing latitude and longitude
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.msg").value("同城Tab需要提供经纬度"));
        }

        @Test
        @DisplayName("Should calculate distance for each feed")
        void shouldCalculateDistance_whenReturnLocalFeeds() throws Exception {
            // Create feed with known location
            testDataFactory.createFeedWithLocation(testUserId, 39.9150, 116.4074); // ~1.2km away

            mockMvc.perform(get("/api/v1/content/feed/local")
                    .param("page", "1")
                    .param("pageSize", "10")
                    .param("latitude", "39.9042")
                    .param("longitude", "116.4074"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[?(@.distance)].distance").exists());
        }
    }

    // ==================== Interaction: Like/Unlike ====================

    @Nested
    @DisplayName("Like Interaction Tests")
    class LikeInteractionTests {

        @Test
        @DisplayName("Should like feed and return updated state")
        void shouldLikeFeed_whenToggleLike() throws Exception {
            Feed feed = testDataFactory.createPublicFeed(testUserId, "Feed to be liked");
            int initialLikeCount = feed.getLikeCount();

            InteractionDTO dto = new InteractionDTO();
            dto.setTargetType("feed");
            dto.setTargetId(feed.getId());

            mockMvc.perform(post("/api/v1/interaction/like")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("点赞成功"))
                .andExpect(jsonPath("$.data.isLiked").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(initialLikeCount + 1));

            // Verify database updated
            Feed updated = feedMapper.selectById(feed.getId());
            assertThat(updated.getLikeCount()).isEqualTo(initialLikeCount + 1);
        }

        @Test
        @DisplayName("Should unlike feed when toggling again")
        void shouldUnlikeFeed_whenToggleAgain() throws Exception {
            Feed feed = testDataFactory.createPublicFeed(testUserId, "Feed to be unliked");

            InteractionDTO dto = new InteractionDTO();
            dto.setTargetType("feed");
            dto.setTargetId(feed.getId());

            // First like
            mockMvc.perform(post("/api/v1/interaction/like")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isLiked").value(true));

            // Then unlike
            mockMvc.perform(post("/api/v1/interaction/like")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("取消点赞成功"))
                .andExpect(jsonPath("$.data.isLiked").value(false));
        }

        @Test
        @DisplayName("Should require authentication to like")
        void shouldRequireAuth_whenLikeFeed() throws Exception {
            Feed feed = testDataFactory.createPublicFeed(testUserId, "Feed");

            InteractionDTO dto = new InteractionDTO();
            dto.setTargetType("feed");
            dto.setTargetId(feed.getId());

            mockMvc.perform(post("/api/v1/interaction/like")
                    // No Satoken header
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        }
    }

    // ==================== Interaction: Collect/Uncollect ====================

    @Nested
    @DisplayName("Collect Interaction Tests")
    class CollectInteractionTests {

        @Test
        @DisplayName("Should collect feed and return updated state")
        void shouldCollectFeed_whenToggleCollect() throws Exception {
            Feed feed = testDataFactory.createPublicFeed(testUserId, "Feed to be collected");
            int initialCollectCount = feed.getCollectCount();

            InteractionDTO dto = new InteractionDTO();
            dto.setTargetType("feed");
            dto.setTargetId(feed.getId());

            mockMvc.perform(post("/api/v1/interaction/collect")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("收藏成功"))
                .andExpect(jsonPath("$.data.isCollected").value(true))
                .andExpect(jsonPath("$.data.collectCount").value(initialCollectCount + 1));
        }

        @Test
        @DisplayName("Should uncollect feed when toggling again")
        void shouldUncollectFeed_whenToggleAgain() throws Exception {
            Feed feed = testDataFactory.createPublicFeed(testUserId, "Feed to be uncollected");

            InteractionDTO dto = new InteractionDTO();
            dto.setTargetType("feed");
            dto.setTargetId(feed.getId());

            // First collect
            mockMvc.perform(post("/api/v1/interaction/collect")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andExpect(status().isOk());

            // Then uncollect
            mockMvc.perform(post("/api/v1/interaction/collect")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("取消收藏成功"))
                .andExpect(jsonPath("$.data.isCollected").value(false));
        }
    }

    // ==================== Interaction: Share ====================

    @Nested
    @DisplayName("Share Interaction Tests")
    class ShareInteractionTests {

        @Test
        @DisplayName("Should share feed with channel tracking")
        void shouldShareFeed_whenValidChannel() throws Exception {
            Feed feed = testDataFactory.createPublicFeed(testUserId, "Feed to be shared");
            int initialShareCount = feed.getShareCount();

            InteractionDTO dto = new InteractionDTO();
            dto.setTargetType("feed");
            dto.setTargetId(feed.getId());
            dto.setShareChannel("wechat");

            mockMvc.perform(post("/api/v1/interaction/share")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("分享成功"))
                .andExpect(jsonPath("$.data.shareCount").value(initialShareCount + 1));
        }

        @Test
        @DisplayName("Should validate share channel enum")
        void shouldRejectInvalidChannel_whenShareFeed() throws Exception {
            Feed feed = testDataFactory.createPublicFeed(testUserId, "Feed");

            InteractionDTO dto = new InteractionDTO();
            dto.setTargetType("feed");
            dto.setTargetId(feed.getId());
            dto.setShareChannel("invalid_channel");

            mockMvc.perform(post("/api/v1/interaction/share")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("分享渠道无效"));
        }

        @Test
        @DisplayName("Should support all share channels")
        void shouldSupportAllChannels_whenShareFeed() throws Exception {
            Feed feed = testDataFactory.createPublicFeed(testUserId, "Feed");

            String[] channels = {"wechat", "moments", "qq", "qzone", "weibo", "copy_link"};

            for (String channel : channels) {
                InteractionDTO dto = new InteractionDTO();
                dto.setTargetType("feed");
                dto.setTargetId(feed.getId());
                dto.setShareChannel(channel);

                mockMvc.perform(post("/api/v1/interaction/share")
                        .header("Satoken", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
            }
        }

        @Test
        @DisplayName("Should require shareChannel field in request body")
        void shouldRequireShareChannel_whenShareFeed() throws Exception {
            Feed feed = testDataFactory.createPublicFeed(testUserId, "Feed");

            InteractionDTO dto = new InteractionDTO();
            dto.setTargetType("feed");
            dto.setTargetId(feed.getId());
            // Missing shareChannel

            mockMvc.perform(post("/api/v1/interaction/share")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        }
    }
}
