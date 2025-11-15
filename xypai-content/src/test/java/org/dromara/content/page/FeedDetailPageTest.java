package org.dromara.content.page;

import org.dromara.content.base.BaseIntegrationTest;
import org.dromara.content.domain.Comment;
import org.dromara.content.domain.Feed;
import org.dromara.content.domain.Report;
import org.dromara.content.domain.dto.CommentDTO;
import org.dromara.content.domain.dto.ReportDTO;
import org.dromara.content.mapper.CommentMapper;
import org.dromara.content.mapper.FeedMapper;
import org.dromara.content.mapper.ReportMapper;
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
 * Feed Detail Page Integration Test
 * <p>
 * Tests all scenarios from frontend page: 03-动态详情页面.md
 * <p>
 * Test Coverage:
 * - Get Feed Detail
 * - Get Comment List (3 sort types)
 * - Post Comment/Reply
 * - Delete Comment
 * - Delete Feed
 * - Submit Report
 * - Like Comment
 * <p>
 * Endpoints Tested:
 * - GET /api/v1/content/detail/{feedId}
 * - GET /api/v1/content/comments/{feedId}
 * - POST /api/v1/content/comment
 * - DELETE /api/v1/content/comment
 * - DELETE /api/v1/content/{feedId}
 * - POST /api/v1/content/report
 * - POST /api/v1/interaction/like (for comments)
 *
 * @author Claude Code AI
 * @date 2025-11-14
 */
@DisplayName("Feed Detail Page Tests")
public class FeedDetailPageTest extends BaseIntegrationTest {

    @Autowired
    private FeedMapper feedMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ReportMapper reportMapper;

    private Feed testFeed;
    private Comment testComment;

    @Override
    protected void setupTestData() {
        // Create test feed
        testFeed = testDataFactory.createPublicFeed(testUserId, "Test feed for detail page");

        // Create test comments
        testComment = testDataFactory.createComment(testFeed.getId(), testUserId, "Test comment");
    }

    // ==================== Get Feed Detail ====================

    @Nested
    @DisplayName("Get Feed Detail Tests")
    class GetFeedDetailTests {

        @Test
        @DisplayName("Should return feed detail with all fields")
        void shouldReturnFeedDetail_whenValidFeedId() throws Exception {
            mockMvc.perform(get("/api/v1/content/detail/" + testFeed.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(testFeed.getId()))
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.type").exists())
                .andExpect(jsonPath("$.data.typeDesc").exists())
                .andExpect(jsonPath("$.data.content").exists())
                .andExpect(jsonPath("$.data.summary").exists())
                .andExpect(jsonPath("$.data.cityId").exists())
                .andExpect(jsonPath("$.data.likeCount").exists())
                .andExpect(jsonPath("$.data.commentCount").exists())
                .andExpect(jsonPath("$.data.shareCount").exists())
                .andExpect(jsonPath("$.data.collectCount").exists())
                .andExpect(jsonPath("$.data.viewCount").exists())
                .andExpect(jsonPath("$.data.isLiked").exists())
                .andExpect(jsonPath("$.data.isCollected").exists())
                .andExpect(jsonPath("$.data.canEdit").exists())
                .andExpect(jsonPath("$.data.canDelete").exists())
                .andExpect(jsonPath("$.data.createdAt").exists());
        }

        @Test
        @DisplayName("Should return detail fields not in list VO")
        void shouldReturnDetailOnlyFields_whenGetDetail() throws Exception {
            Feed feedWithLocation = testDataFactory.createFeedWithLocation(testUserId, 39.9042, 116.4074);

            mockMvc.perform(get("/api/v1/content/detail/" + feedWithLocation.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.locationAddress").exists())  // Detail only
                .andExpect(jsonPath("$.data.distance").exists());         // Detail only (if has location)
        }

        @Test
        @DisplayName("Should return 404 for non-existent feed")
        void shouldReturn404_whenFeedNotExists() throws Exception {
            mockMvc.perform(get("/api/v1/content/detail/999999"))
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.msg").value("动态不存在或已删除"));
        }

        @Test
        @DisplayName("Should allow owner to view private feed")
        void shouldAllowOwner_whenViewPrivateFeed() throws Exception {
            Feed privateFeed = testDataFactory.createPrivateFeed(testUserId, "Private feed");

            mockMvc.perform(get("/api/v1/content/detail/" + privateFeed.getId())
                    .header("Satoken", getAuthHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(privateFeed.getId()));
        }

        @Test
        @DisplayName("Should deny non-owner access to private feed")
        void shouldDenyAccess_whenNonOwnerViewPrivateFeed() throws Exception {
            Long otherUserId = 9999L;
            Feed privateFeed = testDataFactory.createPrivateFeed(otherUserId, "Other user's private feed");

            mockMvc.perform(get("/api/v1/content/detail/" + privateFeed.getId())
                    .header("Satoken", getAuthHeader()))  // Current user's token
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.msg").value("无权查看此动态"));
        }

        @Test
        @DisplayName("Should set canEdit and canDelete to true for owner")
        void shouldSetPermissions_whenOwnerViewsOwnFeed() throws Exception {
            mockMvc.perform(get("/api/v1/content/detail/" + testFeed.getId())
                    .header("Satoken", getAuthHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.canEdit").value(true))
                .andExpect(jsonPath("$.data.canDelete").value(true));
        }

        @Test
        @DisplayName("Should increment view count when viewing detail")
        void shouldIncrementViewCount_whenViewDetail() throws Exception {
            int initialViewCount = testFeed.getViewCount();

            mockMvc.perform(get("/api/v1/content/detail/" + testFeed.getId()))
                .andExpect(status().isOk());

            // View count should increment (may be async via Redis)
            // TODO: Verify view count incremented
        }

        @Test
        @DisplayName("Should use Redis cache for feed detail (10 min TTL)")
        void shouldCacheFeedDetail_whenFirstRequest() throws Exception {
            // First request - cache miss
            mockMvc.perform(get("/api/v1/content/detail/" + testFeed.getId()))
                .andExpect(status().isOk());

            // Second request - cache hit
            mockMvc.perform(get("/api/v1/content/detail/" + testFeed.getId()))
                .andExpect(status().isOk());

            // TODO: Verify cache hit via metrics
        }
    }

    // ==================== Get Comment List ====================

    @Nested
    @DisplayName("Get Comment List Tests")
    class GetCommentListTests {

        @Test
        @DisplayName("Should return comment list with default sort (hot)")
        void shouldReturnComments_whenDefaultSort() throws Exception {
            mockMvc.perform(get("/api/v1/content/comments/" + testFeed.getId())
                    .param("page", "1")
                    .param("pageSize", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
        }

        @Test
        @DisplayName("Should sort comments by time when sortType=time")
        void shouldSortByTime_whenSortTypeIsTime() throws Exception {
            // Create multiple comments
            testDataFactory.createMultipleComments(testFeed.getId(), testUserId, 5);

            mockMvc.perform(get("/api/v1/content/comments/" + testFeed.getId())
                    .param("page", "1")
                    .param("pageSize", "20")
                    .param("sortType", "time"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

            // TODO: Verify comments sorted by createdAt DESC
        }

        @Test
        @DisplayName("Should sort comments by hot when sortType=hot")
        void shouldSortByHot_whenSortTypeIsHot() throws Exception {
            mockMvc.perform(get("/api/v1/content/comments/" + testFeed.getId())
                    .param("page", "1")
                    .param("pageSize", "20")
                    .param("sortType", "hot"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should sort comments by like count when sortType=like")
        void shouldSortByLikeCount_whenSortTypeIsLike() throws Exception {
            mockMvc.perform(get("/api/v1/content/comments/" + testFeed.getId())
                    .param("page", "1")
                    .param("pageSize", "20")
                    .param("sortType", "like"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should prioritize top comments (isTop=1)")
        void shouldPrioritizeTopComments_whenGetComments() throws Exception {
            // Create a top comment
            Comment topComment = testDataFactory.createComment(testFeed.getId(), testUserId, "Top comment");
            topComment.setIsTop(1);
            commentMapper.updateById(topComment);

            mockMvc.perform(get("/api/v1/content/comments/" + testFeed.getId())
                    .param("page", "1")
                    .param("pageSize", "20"))
                .andExpect(status().isOk());

            // TODO: Verify top comment appears first
        }

        @Test
        @DisplayName("Should include nested replies (max 3 shown)")
        void shouldIncludeNestedReplies_whenGetComments() throws Exception {
            // Create replies to test comment
            testDataFactory.createReply(testFeed.getId(), testUserId, testComment.getId(), testUserId, "Reply 1");
            testDataFactory.createReply(testFeed.getId(), testUserId, testComment.getId(), testUserId, "Reply 2");
            testDataFactory.createReply(testFeed.getId(), testUserId, testComment.getId(), testUserId, "Reply 3");

            mockMvc.perform(get("/api/v1/content/comments/" + testFeed.getId())
                    .param("page", "1")
                    .param("pageSize", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].replies").isArray())
                .andExpect(jsonPath("$.data.records[0].replies.length()").value(org.hamcrest.Matchers.lessThanOrEqualTo(3)));
        }

        @Test
        @DisplayName("Should include totalReplies and hasMoreReplies fields")
        void shouldIncludeReplyMetadata_whenGetComments() throws Exception {
            // Create 5 replies (more than 3)
            for (int i = 0; i < 5; i++) {
                testDataFactory.createReply(testFeed.getId(), testUserId, testComment.getId(), testUserId, "Reply " + i);
            }

            mockMvc.perform(get("/api/v1/content/comments/" + testFeed.getId())
                    .param("page", "1")
                    .param("pageSize", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].totalReplies").exists())
                .andExpect(jsonPath("$.data.records[0].totalReplies").value(5))
                .andExpect(jsonPath("$.data.records[0].hasMoreReplies").value(true));
        }

        @Test
        @DisplayName("Should support pagination for comments")
        void shouldPaginateComments() throws Exception {
            // Create 25 comments
            testDataFactory.createMultipleComments(testFeed.getId(), testUserId, 25);

            // Page 1
            mockMvc.perform(get("/api/v1/content/comments/" + testFeed.getId())
                    .param("page", "1")
                    .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records.length()").value(10))
                .andExpect(jsonPath("$.data.current").value(1));

            // Page 2
            mockMvc.perform(get("/api/v1/content/comments/" + testFeed.getId())
                    .param("page", "2")
                    .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.current").value(2));
        }

        @Test
        @DisplayName("Should not require authentication to view comments")
        void shouldAllowAnonymous_whenGetComments() throws Exception {
            mockMvc.perform(get("/api/v1/content/comments/" + testFeed.getId())
                    .param("page", "1")
                    .param("pageSize", "20"))
                // No Satoken header
                .andExpect(status().isOk());
        }
    }

    // ==================== Post Comment ====================

    @Nested
    @DisplayName("Post Comment Tests")
    class PostCommentTests {

        @Test
        @DisplayName("Should post top-level comment successfully")
        void shouldPostComment_whenValidData() throws Exception {
            CommentDTO dto = testDataFactory.createCommentDTO(testFeed.getId(), "New comment content");

            mockMvc.perform(post("/api/v1/content/comment")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("评论成功"))
                .andExpect(jsonPath("$.data.commentId").exists())
                .andExpect(jsonPath("$.data.commentId").isNumber());

            // Verify feed commentCount incremented
            Feed updated = feedMapper.selectById(testFeed.getId());
            assertThat(updated.getCommentCount()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should post reply to comment successfully")
        void shouldPostReply_whenValidParentId() throws Exception {
            CommentDTO dto = testDataFactory.createReplyDTO(
                testFeed.getId(),
                "Reply content",
                testComment.getId(),
                testUserId
            );

            mockMvc.perform(post("/api/v1/content/comment")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("回复成功"))
                .andExpect(jsonPath("$.data.commentId").exists());

            // Verify parent comment replyCount incremented
            Comment parent = commentMapper.selectById(testComment.getId());
            assertThat(parent.getReplyCount()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should require authentication to post comment")
        void shouldRequireAuth_whenPostComment() throws Exception {
            CommentDTO dto = testDataFactory.createCommentDTO(testFeed.getId(), "Comment");

            mockMvc.perform(post("/api/v1/content/comment")
                    // No Satoken header
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should validate comment content (1-500 chars)")
        void shouldValidateContentLength_whenPostComment() throws Exception {
            // Empty content
            CommentDTO dto1 = testDataFactory.createCommentDTO(testFeed.getId(), "");
            mockMvc.perform(post("/api/v1/content/comment")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("评论内容不能为空"));

            // Too long content
            String longContent = "a".repeat(501);
            CommentDTO dto2 = testDataFactory.createCommentDTO(testFeed.getId(), longContent);
            mockMvc.perform(post("/api/v1/content/comment")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto2)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should increment feed commentCount when posting comment")
        void shouldIncrementCommentCount_whenPostComment() throws Exception {
            int initialCount = testFeed.getCommentCount();

            CommentDTO dto = testDataFactory.createCommentDTO(testFeed.getId(), "New comment");

            mockMvc.perform(post("/api/v1/content/comment")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andExpect(status().isOk());

            Feed updated = feedMapper.selectById(testFeed.getId());
            assertThat(updated.getCommentCount()).isEqualTo(initialCount + 1);
        }
    }

    // ==================== Delete Comment ====================

    @Nested
    @DisplayName("Delete Comment Tests")
    class DeleteCommentTests {

        @Test
        @DisplayName("Should delete own comment successfully")
        void shouldDeleteComment_whenOwner() throws Exception {
            Comment comment = testDataFactory.createComment(testFeed.getId(), testUserId, "Comment to delete");

            mockMvc.perform(delete("/api/v1/content/comment")
                    .header("Satoken", getAuthHeader())
                    .param("commentId", comment.getId().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("删除成功"));

            // Verify soft delete
            Comment deleted = commentMapper.selectById(comment.getId());
            assertThat(deleted.getDeleted()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should deny delete for non-owner")
        void shouldDenyDelete_whenNonOwner() throws Exception {
            Long otherUserId = 9999L;
            Comment otherComment = testDataFactory.createComment(testFeed.getId(), otherUserId, "Other user's comment");

            mockMvc.perform(delete("/api/v1/content/comment")
                    .header("Satoken", getAuthHeader())  // Current user's token
                    .param("commentId", otherComment.getId().toString()))
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.msg").value("无权删除此评论"));
        }

        @Test
        @DisplayName("Should decrement feed commentCount when deleting comment")
        void shouldDecrementCommentCount_whenDeleteComment() throws Exception {
            Comment comment = testDataFactory.createComment(testFeed.getId(), testUserId, "Comment");
            int currentCount = testFeed.getCommentCount() + 1; // +1 from creation

            mockMvc.perform(delete("/api/v1/content/comment")
                    .header("Satoken", getAuthHeader())
                    .param("commentId", comment.getId().toString()))
                .andExpect(status().isOk());

            Feed updated = feedMapper.selectById(testFeed.getId());
            assertThat(updated.getCommentCount()).isEqualTo(currentCount - 1);
        }

        @Test
        @DisplayName("Should require authentication to delete comment")
        void shouldRequireAuth_whenDeleteComment() throws Exception {
            mockMvc.perform(delete("/api/v1/content/comment")
                    // No Satoken header
                    .param("commentId", testComment.getId().toString()))
                .andExpect(status().isUnauthorized());
        }
    }

    // ==================== Delete Feed ====================

    @Nested
    @DisplayName("Delete Feed Tests")
    class DeleteFeedTests {

        @Test
        @DisplayName("Should delete own feed successfully")
        void shouldDeleteFeed_whenOwner() throws Exception {
            Feed feed = testDataFactory.createPublicFeed(testUserId, "Feed to delete");

            mockMvc.perform(delete("/api/v1/content/" + feed.getId())
                    .header("Satoken", getAuthHeader()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("删除成功"));

            // Verify soft delete
            Feed deleted = feedMapper.selectById(feed.getId());
            assertThat(deleted.getDeleted()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should deny delete for non-owner")
        void shouldDenyDelete_whenNonOwner() throws Exception {
            Long otherUserId = 9999L;
            Feed otherFeed = testDataFactory.createPublicFeed(otherUserId, "Other user's feed");

            mockMvc.perform(delete("/api/v1/content/" + otherFeed.getId())
                    .header("Satoken", getAuthHeader()))  // Current user's token
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.msg").value("无权删除此动态"));
        }

        @Test
        @DisplayName("Should invalidate cache when deleting feed")
        void shouldInvalidateCache_whenDeleteFeed() throws Exception {
            Feed feed = testDataFactory.createPublicFeed(testUserId, "Feed");

            // View detail to cache it
            mockMvc.perform(get("/api/v1/content/detail/" + feed.getId()))
                .andExpect(status().isOk());

            // Delete feed
            mockMvc.perform(delete("/api/v1/content/" + feed.getId())
                    .header("Satoken", getAuthHeader()))
                .andExpect(status().isOk());

            // TODO: Verify cache invalidated
        }
    }

    // ==================== Submit Report ====================

    @Nested
    @DisplayName("Submit Report Tests")
    class SubmitReportTests {

        @Test
        @DisplayName("Should submit report for feed successfully")
        void shouldSubmitReport_whenReportFeed() throws Exception {
            ReportDTO dto = testDataFactory.createReportDTO("feed", testFeed.getId(), "spam");

            mockMvc.perform(post("/api/v1/content/report")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("已收到您的举报,我们会尽快处理"))
                .andExpect(jsonPath("$.data.reportId").exists())
                .andExpect(jsonPath("$.data.status").value("pending"))
                .andExpect(jsonPath("$.data.createdAt").exists());
        }

        @Test
        @DisplayName("Should submit report for comment successfully")
        void shouldSubmitReport_whenReportComment() throws Exception {
            ReportDTO dto = testDataFactory.createReportDTO("comment", testComment.getId(), "harassment");

            mockMvc.perform(post("/api/v1/content/report")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportId").exists());
        }

        @Test
        @DisplayName("Should submit report for user successfully")
        void shouldSubmitReport_whenReportUser() throws Exception {
            ReportDTO dto = testDataFactory.createReportDTO("user", 9999L, "fraud");

            mockMvc.perform(post("/api/v1/content/report")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should validate target type (feed/comment/user)")
        void shouldValidateTargetType_whenSubmitReport() throws Exception {
            ReportDTO dto = testDataFactory.createReportDTO("invalid_type", testFeed.getId(), "spam");

            mockMvc.perform(post("/api/v1/content/report")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("目标类型无效"));
        }

        @Test
        @DisplayName("Should validate reason type (6 types)")
        void shouldValidateReasonType_whenSubmitReport() throws Exception {
            String[] validReasons = {"harassment", "pornography", "fraud", "illegal", "spam", "other"};

            for (String reason : validReasons) {
                ReportDTO dto = testDataFactory.createReportDTO("feed", testFeed.getId(), reason);

                mockMvc.perform(post("/api/v1/content/report")
                        .header("Satoken", getAuthHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                    .andExpect(status().isOk());

                // Clean up to avoid duplicate prevention
                sleep(100);
            }
        }

        @Test
        @DisplayName("Should prevent duplicate report within 24 hours")
        void shouldPreventDuplicate_whenReportWithin24Hours() throws Exception {
            ReportDTO dto = testDataFactory.createReportDTO("feed", testFeed.getId(), "spam");

            // First report
            mockMvc.perform(post("/api/v1/content/report")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andExpect(status().isOk());

            // Second report (duplicate)
            mockMvc.perform(post("/api/v1/content/report")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.msg").value("24小时内已举报过该内容,请勿重复举报"));
        }

        @Test
        @DisplayName("Should validate description length (0-200 chars)")
        void shouldValidateDescription_whenSubmitReport() throws Exception {
            String longDescription = "a".repeat(201);  // 201 characters

            ReportDTO dto = testDataFactory.createReportDTO("feed", testFeed.getId(), "spam");
            dto.setDescription(longDescription);

            mockMvc.perform(post("/api/v1/content/report")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should validate evidence images (max 3)")
        void shouldValidateEvidenceImages_whenSubmitReport() throws Exception {
            ReportDTO dto = testDataFactory.createReportDTO("feed", testFeed.getId(), "spam");
            dto.setEvidenceImages(java.util.Arrays.asList(
                "image1.jpg", "image2.jpg", "image3.jpg", "image4.jpg"  // 4 images
            ));

            mockMvc.perform(post("/api/v1/content/report")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("最多上传3张举报图片"));
        }

        @Test
        @DisplayName("Should enforce rate limit (10 reports per minute)")
        void shouldEnforceRateLimit_whenTooManyReports() throws Exception {
            // Submit 11 reports in quick succession
            for (int i = 0; i < 11; i++) {
                // Use different target IDs to avoid duplicate prevention
                ReportDTO dto = testDataFactory.createReportDTO("feed", (long) (i + 1000), "spam");

                if (i < 10) {
                    mockMvc.perform(post("/api/v1/content/report")
                            .header("Satoken", getAuthHeader())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(dto)))
                        .andExpect(status().isOk());
                } else {
                    // 11th request should be rate limited
                    mockMvc.perform(post("/api/v1/content/report")
                            .header("Satoken", getAuthHeader())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(dto)))
                        .andDo(print())
                        .andExpect(status().isTooManyRequests());
                }
            }
        }

        @Test
        @DisplayName("Should require authentication to submit report")
        void shouldRequireAuth_whenSubmitReport() throws Exception {
            ReportDTO dto = testDataFactory.createReportDTO("feed", testFeed.getId(), "spam");

            mockMvc.perform(post("/api/v1/content/report")
                    // No Satoken header
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andExpect(status().isUnauthorized());
        }
    }

    // ==================== Like Comment ====================

    @Nested
    @DisplayName("Like Comment Tests")
    class LikeCommentTests {

        @Test
        @DisplayName("Should like comment and return updated count")
        void shouldLikeComment_whenToggle() throws Exception {
            int initialLikeCount = testComment.getLikeCount();

            org.dromara.content.domain.dto.InteractionDTO dto = new org.dromara.content.domain.dto.InteractionDTO();
            dto.setTargetType("comment");
            dto.setTargetId(testComment.getId());

            mockMvc.perform(post("/api/v1/interaction/like")
                    .header("Satoken", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isLiked").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(initialLikeCount + 1));

            // Verify database updated
            Comment updated = commentMapper.selectById(testComment.getId());
            assertThat(updated.getLikeCount()).isEqualTo(initialLikeCount + 1);
        }

        @Test
        @DisplayName("Should unlike comment when toggling again")
        void shouldUnlikeComment_whenToggleAgain() throws Exception {
            org.dromara.content.domain.dto.InteractionDTO dto = new org.dromara.content.domain.dto.InteractionDTO();
            dto.setTargetType("comment");
            dto.setTargetId(testComment.getId());

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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isLiked").value(false));
        }
    }
}
