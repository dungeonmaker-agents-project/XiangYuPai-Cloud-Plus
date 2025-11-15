package org.dromara.content.base;

import org.dromara.content.domain.Comment;
import org.dromara.content.domain.Feed;
import org.dromara.content.domain.Topic;
import org.dromara.content.domain.dto.FeedPublishDTO;
import org.dromara.content.domain.dto.CommentDTO;
import org.dromara.content.domain.dto.ReportDTO;
import org.dromara.content.mapper.CommentMapper;
import org.dromara.content.mapper.FeedMapper;
import org.dromara.content.mapper.TopicMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Test Data Factory
 * <p>
 * Creates test data for integration tests.
 * Provides builders for common test scenarios.
 *
 * @author Claude Code AI
 * @date 2025-11-14
 */
@Component
public class TestDataFactory {

    @Autowired
    private FeedMapper feedMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private TopicMapper topicMapper;

    // ==================== Feed Builders ====================

    /**
     * Create a public feed for testing
     */
    public Feed createPublicFeed(Long userId, String content) {
        Feed feed = Feed.builder()
            .userId(userId)
            .type(1) // 动态
            .title("Test Feed Title")
            .content(content)
            .visibility(0) // Public
            .status(0)
            .likeCount(0)
            .commentCount(0)
            .shareCount(0)
            .collectCount(0)
            .viewCount(0)
            .deleted(0)
            .createdTimestamp(System.currentTimeMillis())
            .build();

        feedMapper.insert(feed);
        return feed;
    }

    /**
     * Create a private feed for testing
     */
    public Feed createPrivateFeed(Long userId, String content) {
        Feed feed = Feed.builder()
            .userId(userId)
            .type(1)
            .content(content)
            .visibility(2) // Private
            .status(0)
            .likeCount(0)
            .commentCount(0)
            .shareCount(0)
            .collectCount(0)
            .viewCount(0)
            .deleted(0)
            .createdTimestamp(System.currentTimeMillis())
            .build();

        feedMapper.insert(feed);
        return feed;
    }

    /**
     * Create a hot feed with high engagement
     */
    public Feed createHotFeed(Long userId) {
        Feed feed = Feed.builder()
            .userId(userId)
            .type(1)
            .content("Hot feed content with high engagement")
            .visibility(0)
            .status(0)
            .likeCount(100)
            .commentCount(50)
            .shareCount(10)
            .collectCount(20)
            .viewCount(1000)
            .deleted(0)
            .createdTimestamp(System.currentTimeMillis())
            .build();

        feedMapper.insert(feed);
        return feed;
    }

    /**
     * Create a feed with location
     */
    public Feed createFeedWithLocation(Long userId, Double latitude, Double longitude) {
        Feed feed = Feed.builder()
            .userId(userId)
            .type(1)
            .content("Feed with location")
            .visibility(0)
            .status(0)
            .locationName("Test Location")
            .locationAddress("Test Address")
            .longitude(longitude)
            .latitude(latitude)
            .likeCount(0)
            .commentCount(0)
            .shareCount(0)
            .collectCount(0)
            .viewCount(0)
            .deleted(0)
            .createdTimestamp(System.currentTimeMillis())
            .build();

        feedMapper.insert(feed);
        return feed;
    }

    // ==================== Comment Builders ====================

    /**
     * Create a top-level comment
     */
    public Comment createComment(Long feedId, Long userId, String content) {
        Comment comment = Comment.builder()
            .feedId(feedId)
            .userId(userId)
            .content(content)
            .parentId(null)
            .replyToUserId(null)
            .likeCount(0)
            .replyCount(0)
            .isTop(0)
            .deleted(0)
            .build();

        commentMapper.insert(comment);
        return comment;
    }

    /**
     * Create a reply to a comment
     */
    public Comment createReply(Long feedId, Long userId, Long parentId, Long replyToUserId, String content) {
        Comment comment = Comment.builder()
            .feedId(feedId)
            .userId(userId)
            .content(content)
            .parentId(parentId)
            .replyToUserId(replyToUserId)
            .likeCount(0)
            .replyCount(0)
            .isTop(0)
            .deleted(0)
            .build();

        commentMapper.insert(comment);
        return comment;
    }

    // ==================== Topic Builders ====================

    /**
     * Create a hot topic
     */
    public Topic createHotTopic(String name) {
        Topic topic = Topic.builder()
            .name(name)
            .description("Test hot topic: " + name)
            .participantCount(1000)
            .postCount(500)
            .isOfficial(1)
            .isHot(1)
            .build();

        topicMapper.insert(topic);
        return topic;
    }

    /**
     * Create a normal topic
     */
    public Topic createNormalTopic(String name) {
        Topic topic = Topic.builder()
            .name(name)
            .description("Test topic: " + name)
            .participantCount(50)
            .postCount(20)
            .isOfficial(0)
            .isHot(0)
            .build();

        topicMapper.insert(topic);
        return topic;
    }

    // ==================== DTO Builders ====================

    /**
     * Create FeedPublishDTO for testing
     */
    public FeedPublishDTO createFeedPublishDTO(String content) {
        FeedPublishDTO dto = new FeedPublishDTO();
        dto.setType(1); // 动态
        dto.setContent(content);
        dto.setVisibility(0); // Public
        return dto;
    }

    /**
     * Create FeedPublishDTO with topics
     */
    public FeedPublishDTO createFeedPublishDTOWithTopics(String content, String... topicNames) {
        FeedPublishDTO dto = createFeedPublishDTO(content);
        dto.setTopicNames(Arrays.asList(topicNames));
        return dto;
    }

    /**
     * Create FeedPublishDTO with location
     */
    public FeedPublishDTO createFeedPublishDTOWithLocation(String content, Double lat, Double lon) {
        FeedPublishDTO dto = createFeedPublishDTO(content);
        dto.setLocationName("Test Location");
        dto.setLocationAddress("Test Address");
        dto.setLatitude(lat);
        dto.setLongitude(lon);
        return dto;
    }

    /**
     * Create CommentDTO for testing
     */
    public CommentDTO createCommentDTO(Long feedId, String content) {
        CommentDTO dto = new CommentDTO();
        dto.setFeedId(feedId);
        dto.setContent(content);
        return dto;
    }

    /**
     * Create CommentDTO for reply
     */
    public CommentDTO createReplyDTO(Long feedId, String content, Long parentId, Long replyToUserId) {
        CommentDTO dto = createCommentDTO(feedId, content);
        dto.setParentId(parentId);
        dto.setReplyToUserId(replyToUserId);
        return dto;
    }

    /**
     * Create ReportDTO for testing
     */
    public ReportDTO createReportDTO(String targetType, Long targetId, String reasonType) {
        ReportDTO dto = new ReportDTO();
        dto.setTargetType(targetType);
        dto.setTargetId(targetId);
        dto.setReasonType(reasonType);
        dto.setDescription("Test report description");
        return dto;
    }

    // ==================== Batch Data Creation ====================

    /**
     * Create multiple feeds for pagination testing
     */
    public List<Feed> createMultipleFeeds(Long userId, int count) {
        return java.util.stream.IntStream.range(0, count)
            .mapToObj(i -> createPublicFeed(userId, "Test feed content " + i))
            .toList();
    }

    /**
     * Create multiple comments for pagination testing
     */
    public List<Comment> createMultipleComments(Long feedId, Long userId, int count) {
        return java.util.stream.IntStream.range(0, count)
            .mapToObj(i -> createComment(feedId, userId, "Test comment " + i))
            .toList();
    }

    /**
     * Create multiple topics for search testing
     */
    public List<Topic> createMultipleTopics(int count) {
        return java.util.stream.IntStream.range(0, count)
            .mapToObj(i -> createNormalTopic("测试话题" + i))
            .toList();
    }

    // ==================== Cleanup Helpers ====================

    /**
     * Delete all test feeds created by a user
     */
    public void cleanupUserFeeds(Long userId) {
        // Soft delete all feeds by user
        feedMapper.selectList(null).stream()
            .filter(feed -> feed.getUserId().equals(userId))
            .forEach(feed -> {
                feed.setDeleted(1);
                feedMapper.updateById(feed);
            });
    }

    /**
     * Delete all test comments for a feed
     */
    public void cleanupFeedComments(Long feedId) {
        commentMapper.selectList(null).stream()
            .filter(comment -> comment.getFeedId().equals(feedId))
            .forEach(comment -> {
                comment.setDeleted(1);
                commentMapper.updateById(comment);
            });
    }
}
