package org.dromara.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.content.domain.entity.Feed;
import org.dromara.content.domain.entity.FeedTopic;
import org.dromara.content.domain.entity.Topic;
import org.dromara.content.domain.vo.FeedListVO;
import org.dromara.content.domain.vo.TopicListVO;
import org.dromara.content.mapper.FeedMapper;
import org.dromara.content.mapper.FeedTopicMapper;
import org.dromara.content.mapper.TopicMapper;
import org.dromara.content.service.ITopicService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 话题服务实现类
 *
 * @author XiangYuPai
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements ITopicService {

    private final TopicMapper topicMapper;
    private final FeedTopicMapper feedTopicMapper;
    private final FeedMapper feedMapper;

    private static final String CACHE_KEY_HOT_TOPICS = "topic:hot:page:";

    @Override
    public Page<TopicListVO> getHotTopics(Integer page, Integer pageSize) {
        // 1. 尝试从缓存读取
        String cacheKey = CACHE_KEY_HOT_TOPICS + page;
        Page<TopicListVO> cachedPage = RedisUtils.getCacheObject(cacheKey);
        if (cachedPage != null) {
            log.debug("从缓存获取热门话题,页码:{}", page);
            return cachedPage;
        }

        // 2. 查询数据库
        Page<Topic> topicPage = topicMapper.selectPage(
            new Page<>(page, pageSize),
            new LambdaQueryWrapper<Topic>()
                .eq(Topic::getIsHot, 1)
                .orderByDesc(Topic::getPostCount)
                .orderByDesc(Topic::getParticipantCount)
        );

        // 3. 转换为VO
        Page<TopicListVO> voPage = convertToVOPage(topicPage);

        // 4. 缓存结果(1小时)
        RedisUtils.setCacheObject(cacheKey, voPage, Duration.ofHours(1));

        return voPage;
    }

    @Override
    public Page<TopicListVO> searchTopics(String keyword, Integer page, Integer pageSize) {
        // 查询数据库(使用LIKE模糊搜索)
        Page<Topic> topicPage = topicMapper.selectPage(
            new Page<>(page, pageSize),
            new LambdaQueryWrapper<Topic>()
                .like(Topic::getName, keyword)
                .or()
                .like(Topic::getDescription, keyword)
                .orderByDesc(Topic::getPostCount)
                .orderByDesc(Topic::getParticipantCount)
        );

        // 转换为VO
        return convertToVOPage(topicPage);
    }

    /**
     * 转换Topic实体分页到TopicListVO分页
     */
    private Page<TopicListVO> convertToVOPage(Page<Topic> topicPage) {
        Page<TopicListVO> voPage = new Page<>(topicPage.getCurrent(), topicPage.getSize(), topicPage.getTotal());

        voPage.setRecords(
            topicPage.getRecords().stream()
                .map(this::convertToVO)
                .toList()
        );

        return voPage;
    }

    /**
     * 转换Topic实体到TopicListVO
     */
    private TopicListVO convertToVO(Topic topic) {
        return TopicListVO.builder()
            .id(topic.getId())
            .name(topic.getName())
            .description(topic.getDescription())
            .coverImage(topic.getCoverImage())
            .participantCount(topic.getParticipantCount())
            .postCount(topic.getPostCount())
            .isOfficial(topic.getIsOfficial() == 1)
            .isHot(topic.getIsHot() == 1)
            .build();
    }

    @Override
    public Page<FeedListVO> getTopicFeeds(Long topicId, Integer page, Integer pageSize, Long userId) {
        // 1. 验证话题存在并获取话题名称
        Topic topic = topicMapper.selectById(topicId);
        if (topic == null) {
            throw new ServiceException("话题不存在");
        }

        // 2. 通过话题名称查询关联的动态ID列表 (FeedTopic使用topicName关联)
        Page<FeedTopic> feedTopicPage = feedTopicMapper.selectPage(
            new Page<>(page, pageSize),
            new LambdaQueryWrapper<FeedTopic>()
                .eq(FeedTopic::getTopicName, topic.getName())
                .orderByDesc(FeedTopic::getCreatedAt)
        );

        if (feedTopicPage.getRecords().isEmpty()) {
            Page<FeedListVO> emptyPage = new Page<>(page, pageSize);
            emptyPage.setRecords(List.of());
            emptyPage.setTotal(0);
            return emptyPage;
        }

        // 3. 查询动态详情
        List<Long> feedIds = feedTopicPage.getRecords().stream()
            .map(FeedTopic::getFeedId)
            .collect(Collectors.toList());

        List<Feed> feeds = feedMapper.selectList(
            new LambdaQueryWrapper<Feed>()
                .in(Feed::getId, feedIds)
                .eq(Feed::getDeleted, 0)
                .orderByDesc(Feed::getCreatedAt)
        );

        // 4. 转换为VO
        List<FeedListVO> voList = feeds.stream()
            .map(feed -> convertFeedToVO(feed, userId))
            .collect(Collectors.toList());

        // 5. 构建返回结果
        Page<FeedListVO> resultPage = new Page<>(page, pageSize);
        resultPage.setRecords(voList);
        resultPage.setTotal(feedTopicPage.getTotal());

        return resultPage;
    }

    /**
     * 转换Feed实体到FeedListVO
     */
    private FeedListVO convertFeedToVO(Feed feed, Long userId) {
        return FeedListVO.builder()
            .id(feed.getId())
            .userId(feed.getUserId())
            .type(feed.getType())
            .typeDesc(getTypeDesc(feed.getType()))
            .title(feed.getTitle())
            .content(feed.getContent())
            .summary(feed.getContent() != null && feed.getContent().length() > 100
                ? feed.getContent().substring(0, 100) + "..."
                : feed.getContent())
            .locationName(feed.getLocationName())
            .cityId(feed.getCityId())
            .likeCount(feed.getLikeCount())
            .commentCount(feed.getCommentCount())
            .shareCount(feed.getShareCount())
            .collectCount(feed.getCollectCount())
            .viewCount(feed.getViewCount())
            .isLiked(false) // TODO: 查询是否已点赞
            .isCollected(false) // TODO: 查询是否已收藏
            .createdAt(feed.getCreatedAt())
            .build();
        // TODO: 填充userInfo, mediaList, topicList
    }

    /**
     * 获取动态类型描述
     */
    private String getTypeDesc(Integer type) {
        if (type == null) return "动态";
        return switch (type) {
            case 1 -> "动态";
            case 2 -> "活动";
            case 3 -> "技能";
            default -> "动态";
        };
    }

}
