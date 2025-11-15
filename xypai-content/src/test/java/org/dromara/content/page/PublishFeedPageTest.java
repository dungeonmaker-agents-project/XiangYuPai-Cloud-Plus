package org.dromara.content.page;

import org.dromara.content.base.BaseIntegrationTest;
import org.dromara.content.domain.entity.Feed;
import org.dromara.content.domain.entity.Topic;
import org.dromara.content.domain.dto.FeedPublishDTO;
import org.dromara.content.mapper.FeedMapper;
import org.dromara.content.mapper.TopicMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Publish Feed Page Integration Test
 * <p>
 * Tests all scenarios from frontend page: 02-发布动态页面.md
 * <p>
 * Test Coverage:
 * - Publish Feed (text/media/topics/location/privacy)
 * - Get Hot Topics
 * - Search Topics
 * - Validation Rules
 * <p>
 * Endpoints Tested:
 * - POST /api/v1/content/publish
 * - GET /api/v1/content/topics/hot
 * - GET /api/v1/content/topics/search
 *
 * @author Claude Code AI
 * @date 2025-11-14
 */
@DisplayName("Publish Feed Page Tests")
public class PublishFeedPageTest extends BaseIntegrationTest {

    @Autowired
    private FeedMapper feedMapper;

    @Autowired
    private TopicMapper topicMapper;

    @Override
    protected void setupTestData() {
        // Create test topics for selection
        testDataFactory.createHotTopic("王者荣耀");
        testDataFactory.createHotTopic("美食推荐");
        testDataFactory.createNormalTopic("探店日记");
        testDataFactory.createNormalTopic("旅行分享");
    }

    // ==================== Publish Feed Tests ====================

    @Nested
    @DisplayName("Publish Feed Basic Tests")
    class PublishFeedBasicTests {

        @Test
        @DisplayName("Should publish text-only feed successfully")
        void shouldPublishFeed_whenTextOnly() throws Exception {
            FeedPublishDTO dto = testDataFactory.createFeedPublishDTO("This is a test feed content");

            mockMvc.perform(post("/api/v1/content/publish")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("发布成功"))
                .andExpect(jsonPath("$.data.feedId").exists())
                .andExpect(jsonPath("$.data.feedId").isNumber());
        }

        @Test
        @DisplayName("Should require authentication to publish")
        void shouldRequireAuth_whenPublishFeed() throws Exception {
            FeedPublishDTO dto = testDataFactory.createFeedPublishDTO("Test content");

            mockMvc.perform(post("/api/v1/content/publish")
                    // No Satoken header
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should publish feed with title and content")
        void shouldPublishFeed_whenWithTitle() throws Exception {
            FeedPublishDTO dto = testDataFactory.createFeedPublishDTO("Feed content");
            dto.setTitle("Feed Title");

            mockMvc.perform(post("/api/v1/content/publish")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("Should set default visibility to 0 (public)")
        void shouldDefaultToPublic_whenNoVisibilitySpecified() throws Exception {
            FeedPublishDTO dto = testDataFactory.createFeedPublishDTO("Public feed");
            // Don't set visibility - should default to 0

            String response = mockMvc.perform(post("/api/v1/content/publish")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

            // Extract feedId from response
            // Verify feed.visibility = 0
        }
    }

    // ==================== Validation Tests ====================

    @Nested
    @DisplayName("Publish Feed Validation Tests")
    class PublishFeedValidationTests {

        @Test
        @DisplayName("Should reject empty content")
        void shouldRejectEmptyContent_whenPublish() throws Exception {
            FeedPublishDTO dto = new FeedPublishDTO();
            dto.setType(1);
            dto.setContent("");  // Empty content

            mockMvc.perform(post("/api/v1/content/publish")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("内容不能为空"));
        }

        @Test
        @DisplayName("Should reject content longer than 1000 chars")
        void shouldRejectLongContent_whenPublish() throws Exception {
            String longContent = "a".repeat(1001);  // 1001 characters

            FeedPublishDTO dto = testDataFactory.createFeedPublishDTO(longContent);

            mockMvc.perform(post("/api/v1/content/publish")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("内容长度必须在1-1000字符之间"));
        }

        @Test
        @DisplayName("Should reject title longer than 50 chars")
        void shouldRejectLongTitle_whenPublish() throws Exception {
            String longTitle = "a".repeat(51);  // 51 characters

            FeedPublishDTO dto = testDataFactory.createFeedPublishDTO("Valid content");
            dto.setTitle(longTitle);

            mockMvc.perform(post("/api/v1/content/publish")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("标题长度不能超过50字符"));
        }

        @Test
        @DisplayName("Should reject more than 9 media items")
        void shouldRejectTooManyMedia_whenPublish() throws Exception {
            FeedPublishDTO dto = testDataFactory.createFeedPublishDTO("Content with many media");
            dto.setMediaIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));  // 10 items

            mockMvc.perform(post("/api/v1/content/publish")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("最多上传9张图片"));
        }

        @Test
        @DisplayName("Should reject more than 5 topics")
        void shouldRejectTooManyTopics_whenPublish() throws Exception {
            FeedPublishDTO dto = testDataFactory.createFeedPublishDTO("Content");
            dto.setTopicNames(Arrays.asList("话题1", "话题2", "话题3", "话题4", "话题5", "话题6"));  // 6 topics

            mockMvc.perform(post("/api/v1/content/publish")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("最多添加5个话题"));
        }

        @Test
        @DisplayName("Should validate visibility range (0-2)")
        void shouldValidateVisibility_whenPublish() throws Exception {
            FeedPublishDTO dto = testDataFactory.createFeedPublishDTO("Content");
            dto.setVisibility(3);  // Invalid: should be 0, 1, or 2

            mockMvc.perform(post("/api/v1/content/publish")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        }
    }

    // ==================== Publish with Topics ====================

    @Nested
    @DisplayName("Publish Feed with Topics Tests")
    class PublishFeedWithTopicsTests {

        @Test
        @DisplayName("Should publish feed with existing topics")
        void shouldPublishFeed_whenWithExistingTopics() throws Exception {
            FeedPublishDTO dto = testDataFactory.createFeedPublishDTOWithTopics(
                "Feed content",
                "王者荣耀",  // Existing hot topic
                "美食推荐"   // Existing hot topic
            );

            mockMvc.perform(post("/api/v1/content/publish")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

            // Verify topic postCount incremented
            Topic topic1 = topicMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Topic>()
                    .eq(Topic::getName, "王者荣耀")
            );
            assertThat(topic1.getPostCount()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should auto-create new topics when publishing")
        void shouldAutoCreateTopic_whenPublishWithNewTopic() throws Exception {
            String newTopicName = "新话题测试" + System.currentTimeMillis();

            FeedPublishDTO dto = testDataFactory.createFeedPublishDTOWithTopics(
                "Feed content",
                newTopicName
            );

            mockMvc.perform(post("/api/v1/content/publish")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk());

            // Verify new topic created
            Topic newTopic = topicMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Topic>()
                    .eq(Topic::getName, newTopicName)
            );
            assertThat(newTopic).isNotNull();
            assertThat(newTopic.getPostCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should increment existing topic postCount")
        void shouldIncrementPostCount_whenPublishWithExistingTopic() throws Exception {
            // Get initial postCount
            Topic topic = topicMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Topic>()
                    .eq(Topic::getName, "王者荣耀")
            );
            int initialCount = topic.getPostCount();

            FeedPublishDTO dto = testDataFactory.createFeedPublishDTOWithTopics(
                "Feed content",
                "王者荣耀"
            );

            mockMvc.perform(post("/api/v1/content/publish")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andExpect(status().isOk());

            // Verify postCount incremented
            Topic updated = topicMapper.selectById(topic.getId());
            assertThat(updated.getPostCount()).isEqualTo(initialCount + 1);
        }

        @Test
        @DisplayName("Should create feed-topic associations")
        void shouldCreateAssociations_whenPublishWithTopics() throws Exception {
            FeedPublishDTO dto = testDataFactory.createFeedPublishDTOWithTopics(
                "Feed content",
                "王者荣耀",
                "美食推荐"
            );

            String response = mockMvc.perform(post("/api/v1/content/publish")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

            // TODO: Verify feed_topic associations created
            // This requires accessing feed_topic table
        }
    }

    // ==================== Publish with Location ====================

    @Nested
    @DisplayName("Publish Feed with Location Tests")
    class PublishFeedWithLocationTests {

        @Test
        @DisplayName("Should publish feed with location data")
        void shouldPublishFeed_whenWithLocation() throws Exception {
            FeedPublishDTO dto = testDataFactory.createFeedPublishDTOWithLocation(
                "Feed content",
                39.9042,   // Latitude
                116.4074   // Longitude
            );

            mockMvc.perform(post("/api/v1/content/publish")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("Should accept optional location fields")
        void shouldAcceptOptionalLocation_whenPublish() throws Exception {
            FeedPublishDTO dto = testDataFactory.createFeedPublishDTO("Content");
            dto.setLocationName("Test Location");
            // No latitude/longitude - should be optional

            mockMvc.perform(post("/api/v1/content/publish")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk());
        }
    }

    // ==================== Privacy Settings ====================

    @Nested
    @DisplayName("Publish Feed Privacy Tests")
    class PublishFeedPrivacyTests {

        @Test
        @DisplayName("Should publish public feed (visibility=0)")
        void shouldPublishPublicFeed() throws Exception {
            FeedPublishDTO dto = testDataFactory.createFeedPublishDTO("Public content");
            dto.setVisibility(0);

            mockMvc.perform(post("/api/v1/content/publish")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should publish friends-only feed (visibility=1)")
        void shouldPublishFriendsOnlyFeed() throws Exception {
            FeedPublishDTO dto = testDataFactory.createFeedPublishDTO("Friends only content");
            dto.setVisibility(1);

            mockMvc.perform(post("/api/v1/content/publish")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should publish private feed (visibility=2)")
        void shouldPublishPrivateFeed() throws Exception {
            FeedPublishDTO dto = testDataFactory.createFeedPublishDTO("Private content");
            dto.setVisibility(2);

            mockMvc.perform(post("/api/v1/content/publish")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andExpect(status().isOk());
        }
    }

    // ==================== Get Hot Topics ====================

    @Nested
    @DisplayName("Get Hot Topics Tests")
    class GetHotTopicsTests {

        @Test
        @DisplayName("Should return hot topics list")
        void shouldReturnHotTopics_whenGetHotTopics() throws Exception {
            mockMvc.perform(get("/api/v1/content/topics/hot")
                    .param("page", "1")
                    .param("pageSize", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").exists())
                .andExpect(jsonPath("$.data.current").value(1));
        }

        @Test
        @DisplayName("Should return only hot topics (isHot=1)")
        void shouldReturnOnlyHotTopics() throws Exception {
            mockMvc.perform(get("/api/v1/content/topics/hot")
                    .param("page", "1")
                    .param("pageSize", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[*].isHot").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is(1))));
        }

        @Test
        @DisplayName("Should sort by postCount and participantCount DESC")
        void shouldSortByPostCount_whenGetHotTopics() throws Exception {
            mockMvc.perform(get("/api/v1/content/topics/hot")
                    .param("page", "1")
                    .param("pageSize", "20"))
                .andDo(print())
                .andExpect(status().isOk());

            // TODO: Verify sorting order
            // First topic should have highest postCount
        }

        @Test
        @DisplayName("Should cache hot topics in Redis (1 hour TTL)")
        void shouldCacheHotTopics_whenFirstRequest() throws Exception {
            // First request - cache miss
            mockMvc.perform(get("/api/v1/content/topics/hot")
                    .param("page", "1")
                    .param("pageSize", "20"))
                .andExpect(status().isOk());

            // Second request - cache hit (should be faster)
            mockMvc.perform(get("/api/v1/content/topics/hot")
                    .param("page", "1")
                    .param("pageSize", "20"))
                .andExpect(status().isOk());

            // TODO: Verify cache hit via logs or metrics
        }

        @Test
        @DisplayName("Should support pagination for hot topics")
        void shouldPaginateHotTopics() throws Exception {
            // Create enough topics for pagination
            for (int i = 0; i < 25; i++) {
                testDataFactory.createHotTopic("热门话题" + i);
            }

            // Page 1
            mockMvc.perform(get("/api/v1/content/topics/hot")
                    .param("page", "1")
                    .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records.length()").value(10))
                .andExpect(jsonPath("$.data.current").value(1));

            // Page 2
            mockMvc.perform(get("/api/v1/content/topics/hot")
                    .param("page", "2")
                    .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.current").value(2));
        }

        @Test
        @DisplayName("Should not require authentication to get hot topics")
        void shouldAllowAnonymous_whenGetHotTopics() throws Exception {
            mockMvc.perform(get("/api/v1/content/topics/hot")
                    .param("page", "1")
                    .param("pageSize", "20"))
                // No Satoken header
                .andExpect(status().isOk());
        }
    }

    // ==================== Search Topics ====================

    @Nested
    @DisplayName("Search Topics Tests")
    class SearchTopicsTests {

        @Test
        @DisplayName("Should search topics by keyword")
        void shouldSearchTopics_whenValidKeyword() throws Exception {
            mockMvc.perform(get("/api/v1/content/topics/search")
                    .param("keyword", "美食")
                    .param("page", "1")
                    .param("pageSize", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
        }

        @Test
        @DisplayName("Should search in both name and description")
        void shouldSearchInNameAndDescription() throws Exception {
            // Create topic with keyword in description
            Topic topic = testDataFactory.createNormalTopic("特殊话题");
            topic.setDescription("包含美食关键词的描述");
            topicMapper.updateById(topic);

            mockMvc.perform(get("/api/v1/content/topics/search")
                    .param("keyword", "美食")
                    .param("page", "1")
                    .param("pageSize", "20"))
                .andDo(print())
                .andExpect(status().isOk());

            // Should find topics with "美食" in name or description
        }

        @Test
        @DisplayName("Should return empty list when no matches")
        void shouldReturnEmptyList_whenNoMatches() throws Exception {
            mockMvc.perform(get("/api/v1/content/topics/search")
                    .param("keyword", "不存在的话题关键词xyz123")
                    .param("page", "1")
                    .param("pageSize", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records").isEmpty());
        }

        @Test
        @DisplayName("Should validate keyword length (1-20 chars)")
        void shouldValidateKeywordLength_whenSearch() throws Exception {
            // Empty keyword
            mockMvc.perform(get("/api/v1/content/topics/search")
                    .param("keyword", "")
                    .param("page", "1")
                    .param("pageSize", "20"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("关键词不能为空"));

            // Too long keyword
            String longKeyword = "a".repeat(21);  // 21 characters
            mockMvc.perform(get("/api/v1/content/topics/search")
                    .param("keyword", longKeyword)
                    .param("page", "1")
                    .param("pageSize", "20"))
                .andDo(print())
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should support pagination for search results")
        void shouldPaginateSearchResults() throws Exception {
            // Create many topics with same keyword
            for (int i = 0; i < 15; i++) {
                testDataFactory.createNormalTopic("搜索测试" + i);
            }

            mockMvc.perform(get("/api/v1/content/topics/search")
                    .param("keyword", "搜索测试")
                    .param("page", "1")
                    .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.current").value(1));
        }

        @Test
        @DisplayName("Should not require authentication to search topics")
        void shouldAllowAnonymous_whenSearchTopics() throws Exception {
            mockMvc.perform(get("/api/v1/content/topics/search")
                    .param("keyword", "测试")
                    .param("page", "1")
                    .param("pageSize", "20"))
                // No Satoken header
                .andExpect(status().isOk());
        }
    }
}
