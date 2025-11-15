package org.dromara.content.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.content.domain.dto.FeedListQueryDTO;
import org.dromara.content.domain.dto.FeedPublishDTO;
import org.dromara.content.domain.entity.*;
import org.dromara.content.domain.vo.FeedDetailVO;
import org.dromara.content.domain.vo.FeedListVO;
import org.dromara.content.mapper.*;
import org.dromara.content.service.IFeedService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 动态服务实现
 *
 * @author XiangYuPai
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements IFeedService {

    private final FeedMapper feedMapper;
    private final FeedTopicMapper feedTopicMapper;
    private final FeedMediaMapper feedMediaMapper;
    private final TopicMapper topicMapper;
    private final LikeMapper likeMapper;
    private final CollectionMapper collectionMapper;

    private static final String CACHE_KEY_FEED_DETAIL = "feed:detail:";
    private static final String CACHE_KEY_FEED_LIST = "feed:list:";

    @Override
    public Page<FeedListVO> getFeedList(FeedListQueryDTO queryDTO) {
        // 1. 构建查询条件
        Page<Feed> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<Feed> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feed::getStatus, 0); // 正常状态

        // 2. 根据tabType查询不同数据
        if ("follow".equals(queryDTO.getTabType())) {
            // TODO: 调用UserService RPC获取关注列表
            // 暂时返回空列表
            List<Long> followingIds = new ArrayList<>();
            if (followingIds.isEmpty()) {
                return new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
            }
            wrapper.in(Feed::getUserId, followingIds);
            wrapper.orderByDesc(Feed::getCreatedTimestamp);

        } else if ("hot".equals(queryDTO.getTabType())) {
            // 热门排序: 使用热度算法
            // 热度分 = 点赞数 * 1 + 评论数 * 2 + 分享数 * 3 + 收藏数 * 2
            // 时间衰减: score * Math.pow(0.5, hoursSinceCreated / 24)

            // 查询最近7天的动态(避免数据量过大)
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            wrapper.ge(Feed::getCreatedAt, sevenDaysAgo);
            wrapper.orderByDesc(Feed::getCreatedTimestamp);

            // 查询所有记录(不分页,后面排序后再分页)
            Page<Feed> allFeeds = new Page<>(1, 1000); // 限制最多1000条
            Page<Feed> feedPage = feedMapper.selectPage(allFeeds, wrapper);

            // 计算热度分并排序
            List<Feed> sortedFeeds = feedPage.getRecords().stream()
                .sorted((f1, f2) -> Double.compare(
                    calculateHotScore(f2), // 降序排序
                    calculateHotScore(f1)
                ))
                .collect(Collectors.toList());

            // 手动分页
            int start = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
            int end = Math.min(start + queryDTO.getPageSize(), sortedFeeds.size());
            List<Feed> pagedFeeds = sortedFeeds.subList(
                Math.min(start, sortedFeeds.size()),
                Math.min(end, sortedFeeds.size())
            );

            // 转换为VO
            List<FeedListVO> voList = pagedFeeds.stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

            Page<FeedListVO> resultPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
            resultPage.setRecords(voList);
            resultPage.setTotal(sortedFeeds.size());
            return resultPage;

        } else if ("local".equals(queryDTO.getTabType())) {
            // 同城: 基于地理位置
            if (queryDTO.getLatitude() == null || queryDTO.getLongitude() == null) {
                throw new ServiceException("同城Tab需要提供经纬度");
            }

            // 使用空间查询
            Integer radius = queryDTO.getRadius() != null ? queryDTO.getRadius() : 5;
            List<Feed> nearbyFeeds = feedMapper.selectNearbyFeeds(
                queryDTO.getLatitude(),
                queryDTO.getLongitude(),
                radius,
                queryDTO.getPageSize()
            );

            // 转换为VO
            List<FeedListVO> voList = nearbyFeeds.stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

            Page<FeedListVO> resultPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
            resultPage.setRecords(voList);
            resultPage.setTotal(voList.size());
            return resultPage;
        }

        // 3. 执行查询
        Page<Feed> feedPage = feedMapper.selectPage(page, wrapper);

        // 4. 转换为VO
        List<FeedListVO> voList = feedPage.getRecords().stream()
            .map(this::convertToListVO)
            .collect(Collectors.toList());

        // 5. 构建返回结果
        Page<FeedListVO> resultPage = new Page<>(feedPage.getCurrent(), feedPage.getSize());
        resultPage.setRecords(voList);
        resultPage.setTotal(feedPage.getTotal());

        return resultPage;
    }

    @Override
    public FeedDetailVO getFeedDetail(Long feedId, Long userId) {
        // 1. 尝试从缓存获取
        String cacheKey = CACHE_KEY_FEED_DETAIL + feedId;
        FeedDetailVO cached = RedisUtils.getCacheObject(cacheKey);
        if (cached != null) {
            log.debug("从缓存获取动态详情: {}", feedId);
            return cached;
        }

        // 2. 查询数据库
        Feed feed = feedMapper.selectById(feedId);
        if (feed == null || feed.getDeleted() == 1) {
            throw new ServiceException("动态不存在或已删除");
        }

        // 3. 检查可见性
        if (feed.getVisibility() == 1) {
            // TODO: 检查是否为好友关系
        } else if (feed.getVisibility() == 2) {
            // 仅自己可见
            if (!feed.getUserId().equals(userId)) {
                throw new ServiceException("无权查看此动态");
            }
        }

        // 4. 转换为VO
        FeedDetailVO vo = convertToDetailVO(feed, userId);

        // 5. 异步增加浏览数
        incrementViewCount(feedId);

        // 6. 缓存结果(10分钟)
        RedisUtils.setCacheObject(cacheKey, vo, Duration.ofMinutes(10));

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishFeed(FeedPublishDTO publishDTO, Long userId) {
        // 1. 创建Feed记录
        Feed feed = Feed.builder()
            .userId(userId)
            .type(publishDTO.getType())
            .title(publishDTO.getTitle())
            .content(publishDTO.getContent())
            .locationName(publishDTO.getLocationName())
            .locationAddress(publishDTO.getLocationAddress())
            .longitude(publishDTO.getLongitude())
            .latitude(publishDTO.getLatitude())
            .visibility(publishDTO.getVisibility() != null ? publishDTO.getVisibility() : 0)
            .status(0)
            .createdTimestamp(System.currentTimeMillis())
            .build();

        feedMapper.insert(feed);

        // 2. 关联媒体资源
        if (publishDTO.getMediaIds() != null && !publishDTO.getMediaIds().isEmpty()) {
            for (int i = 0; i < publishDTO.getMediaIds().size(); i++) {
                FeedMedia feedMedia = FeedMedia.builder()
                    .feedId(feed.getId())
                    .mediaId(publishDTO.getMediaIds().get(i))
                    .mediaType("image") // TODO: 从MediaService获取实际类型
                    .sortOrder(i + 1)
                    .build();
                feedMediaMapper.insert(feedMedia);
            }
        }

        // 3. 处理话题标签
        if (publishDTO.getTopicNames() != null && !publishDTO.getTopicNames().isEmpty()) {
            for (String topicName : publishDTO.getTopicNames()) {
                // 查询或创建话题
                LambdaQueryWrapper<Topic> topicWrapper = new LambdaQueryWrapper<>();
                topicWrapper.eq(Topic::getName, topicName);
                Topic topic = topicMapper.selectOne(topicWrapper);

                if (topic == null) {
                    // 创建新话题
                    topic = Topic.builder()
                        .name(topicName)
                        .isOfficial(0)
                        .isHot(0)
                        .build();
                    topicMapper.insert(topic);
                } else {
                    // 更新话题帖子数
                    topic.setPostCount(topic.getPostCount() + 1);
                    topicMapper.updateById(topic);
                }

                // 创建关联
                FeedTopic feedTopic = FeedTopic.builder()
                    .feedId(feed.getId())
                    .topicName(topicName)
                    .build();
                feedTopicMapper.insert(feedTopic);
            }
        }

        log.info("用户 {} 发布动态成功: {}", userId, feed.getId());
        return feed.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFeed(Long feedId, Long userId) {
        // 1. 查询动态
        Feed feed = feedMapper.selectById(feedId);
        if (feed == null) {
            throw new ServiceException("动态不存在");
        }

        // 2. 验证权限
        if (!feed.getUserId().equals(userId)) {
            throw new ServiceException("无权删除此动态");
        }

        // 3. 软删除
        feed.setDeleted(1);
        feedMapper.updateById(feed);

        // 4. 清除缓存
        RedisUtils.deleteObject(CACHE_KEY_FEED_DETAIL + feedId);

        log.info("用户 {} 删除动态: {}", userId, feedId);
    }

    /**
     * 转换为列表VO
     */
    private FeedListVO convertToListVO(Feed feed) {
        // 生成类型描述
        String typeDesc = getTypeDesc(feed.getType());

        // 生成内容摘要(截取前100字符)
        String summary = feed.getContent() != null && feed.getContent().length() > 100
            ? feed.getContent().substring(0, 100) + "..."
            : feed.getContent();

        FeedListVO vo = FeedListVO.builder()
            .id(feed.getId())
            .userId(feed.getUserId())
            .type(feed.getType())
            .typeDesc(typeDesc)
            .title(feed.getTitle())
            .content(feed.getContent())
            .summary(summary)
            .locationName(feed.getLocationName())
            .cityId(feed.getCityId())
            .likeCount(feed.getLikeCount())
            .commentCount(feed.getCommentCount())
            .shareCount(feed.getShareCount())
            .collectCount(feed.getCollectCount())
            .viewCount(feed.getViewCount())
            .isLiked(false)
            .isCollected(false)
            .createdAt(feed.getCreatedAt())
            .build();

        // TODO: 填充userInfo, mediaList, topicList

        return vo;
    }

    /**
     * 转换为详情VO
     */
    private FeedDetailVO convertToDetailVO(Feed feed, Long userId) {
        // 生成类型描述
        String typeDesc = getTypeDesc(feed.getType());

        // 生成内容摘要(截取前100字符)
        String summary = feed.getContent() != null && feed.getContent().length() > 100
            ? feed.getContent().substring(0, 100) + "..."
            : feed.getContent();

        FeedDetailVO vo = FeedDetailVO.builder()
            .id(feed.getId())
            .userId(feed.getUserId())
            .type(feed.getType())
            .typeDesc(typeDesc)
            .title(feed.getTitle())
            .content(feed.getContent())
            .summary(summary)
            .locationName(feed.getLocationName())
            .locationAddress(feed.getLocationAddress())
            .cityId(feed.getCityId())
            .likeCount(feed.getLikeCount())
            .commentCount(feed.getCommentCount())
            .shareCount(feed.getShareCount())
            .collectCount(feed.getCollectCount())
            .viewCount(feed.getViewCount())
            .isLiked(false)
            .isCollected(false)
            .canEdit(feed.getUserId().equals(userId))
            .canDelete(feed.getUserId().equals(userId))
            .createdAt(feed.getCreatedAt())
            .build();

        // TODO: 填充userInfo, mediaList, topicList

        return vo;
    }

    /**
     * 增加浏览数
     */
    private void incrementViewCount(Long feedId) {
        // 使用Redis计数器
        String countKey = "feed:view:count:" + feedId;
        Long count = RedisUtils.getCacheObject(countKey);
        if (count == null) {
            count = 0L;
        }
        count++;
        RedisUtils.setCacheObject(countKey, count, Duration.ofDays(1));

        // TODO: 定时任务同步到MySQL
    }

    /**
     * 获取动态类型描述
     *
     * @param type 类型编号
     * @return 类型描述
     */
    private String getTypeDesc(Integer type) {
        return switch (type) {
            case 1 -> "动态";
            case 2 -> "活动";
            case 3 -> "技能";
            default -> "未知";
        };
    }

    /**
     * 计算热度分数(带时间衰减)
     *
     * 算法说明:
     * 1. 基础分 = 点赞数 * 1 + 评论数 * 2 + 分享数 * 3 + 收藏数 * 2
     * 2. 时间衰减 = Math.pow(0.5, hoursSinceCreated / 24)
     * 3. 最终热度分 = 基础分 * 时间衰减
     *
     * @param feed 动态实体
     * @return 热度分数
     */
    private double calculateHotScore(Feed feed) {
        // 1. 计算基础分
        double baseScore = feed.getLikeCount() * 1.0
            + feed.getCommentCount() * 2.0
            + feed.getShareCount() * 3.0
            + feed.getCollectCount() * 2.0;

        // 2. 计算时间衰减因子
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = feed.getCreatedAt();

        // 计算小时差
        long hoursSinceCreated = java.time.Duration.between(createdAt, now).toHours();

        // 时间衰减: 每24小时衰减50%
        double timeFactor = Math.pow(0.5, hoursSinceCreated / 24.0);

        // 3. 最终热度分 = 基础分 * 时间衰减
        double hotScore = baseScore * timeFactor;

        return hotScore;
    }

}
