package org.dromara.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.content.domain.dto.DiscoverListQueryDTO;
import org.dromara.content.domain.entity.ContentCollection;
import org.dromara.content.domain.entity.Feed;
import org.dromara.content.domain.entity.Like;
import org.dromara.content.domain.vo.DiscoverFeedVO;
import org.dromara.content.mapper.CollectionMapper;
import org.dromara.content.mapper.FeedMapper;
import org.dromara.content.mapper.LikeMapper;
import org.dromara.content.service.IDiscoverService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 发现页服务实现
 *
 * @author XiangYuPai
 * @date 2025-12-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscoverServiceImpl implements IDiscoverService {

    private final FeedMapper feedMapper;
    private final LikeMapper likeMapper;
    private final CollectionMapper collectionMapper;

    /**
     * 热度计算权重
     */
    private static final int LIKE_WEIGHT = 1;
    private static final int COMMENT_WEIGHT = 2;
    private static final int SHARE_WEIGHT = 3;

    @Override
    public Page<DiscoverFeedVO> queryDiscoverList(DiscoverListQueryDTO queryDTO, Long userId) {
        log.info("查询发现列表: tab={}, pageNum={}, pageSize={}, userId={}",
            queryDTO.getTab(), queryDTO.getPageNum(), queryDTO.getPageSize(), userId);

        String tab = queryDTO.getTab();
        Page<Feed> feedPage;

        switch (tab) {
            case "follow":
                feedPage = queryFollowFeeds(queryDTO, userId);
                break;
            case "nearby":
                feedPage = queryNearbyFeeds(queryDTO);
                break;
            case "hot":
            default:
                feedPage = queryHotFeeds(queryDTO);
                break;
        }

        // 转换为VO
        Page<DiscoverFeedVO> resultPage = new Page<>(feedPage.getCurrent(), feedPage.getSize(), feedPage.getTotal());
        List<DiscoverFeedVO> voList = convertToVOList(feedPage.getRecords(), userId, queryDTO);
        resultPage.setRecords(voList);

        return resultPage;
    }

    /**
     * 查询关注Tab - 关注用户的动态
     */
    private Page<Feed> queryFollowFeeds(DiscoverListQueryDTO queryDTO, Long userId) {
        Page<Feed> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        if (userId == null) {
            // 未登录，返回空列表
            return page;
        }

        // TODO: 从关系服务获取关注用户列表，然后查询这些用户的动态
        // 这里先返回空，实际需要调用 user 服务获取关注列表
        // 暂时返回热门数据作为fallback
        LambdaQueryWrapper<Feed> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feed::getDeleted, 0)
            .eq(Feed::getStatus, 0)
            .eq(Feed::getVisibility, 0)
            .orderByDesc(Feed::getCreatedTimestamp);

        return feedMapper.selectPage(page, wrapper);
    }

    /**
     * 查询热门Tab - 按热度排序
     */
    private Page<Feed> queryHotFeeds(DiscoverListQueryDTO queryDTO) {
        Page<Feed> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<Feed> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feed::getDeleted, 0)
            .eq(Feed::getStatus, 0)
            .eq(Feed::getVisibility, 0)
            // 按热度排序: likeCount*1 + commentCount*2 + shareCount*3
            // 使用 last() 方法添加自定义SQL排序
            .last("ORDER BY (like_count * " + LIKE_WEIGHT
                + " + comment_count * " + COMMENT_WEIGHT
                + " + share_count * " + SHARE_WEIGHT + ") DESC, created_timestamp DESC");

        return feedMapper.selectPage(page, wrapper);
    }

    /**
     * 查询同城Tab - 按距离排序
     */
    private Page<Feed> queryNearbyFeeds(DiscoverListQueryDTO queryDTO) {
        Page<Feed> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        BigDecimal latitude = queryDTO.getLatitude();
        BigDecimal longitude = queryDTO.getLongitude();

        if (latitude == null || longitude == null) {
            // 没有位置信息，按时间排序
            LambdaQueryWrapper<Feed> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Feed::getDeleted, 0)
                .eq(Feed::getStatus, 0)
                .eq(Feed::getVisibility, 0)
                .isNotNull(Feed::getLatitude)
                .isNotNull(Feed::getLongitude)
                .orderByDesc(Feed::getCreatedTimestamp);
            return feedMapper.selectPage(page, wrapper);
        }

        // 有位置信息，使用空间查询
        List<Feed> nearbyFeeds = feedMapper.selectNearbyFeeds(
            latitude, longitude, 50, queryDTO.getPageSize()
        );

        page.setRecords(nearbyFeeds);
        page.setTotal(nearbyFeeds.size());
        return page;
    }

    /**
     * 转换为VO列表，并填充交互状态
     */
    private List<DiscoverFeedVO> convertToVOList(List<Feed> feeds, Long userId, DiscoverListQueryDTO queryDTO) {
        if (feeds == null || feeds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> feedIds = feeds.stream().map(Feed::getId).collect(Collectors.toList());

        // 批量查询点赞状态
        Map<Long, Boolean> likeStatusMap = userId != null
            ? batchCheckLikeStatus(feedIds, userId)
            : Collections.emptyMap();

        // 批量查询收藏状态
        Map<Long, Boolean> collectStatusMap = userId != null
            ? batchCheckCollectStatus(feedIds, userId)
            : Collections.emptyMap();

        return feeds.stream().map(feed -> {
            DiscoverFeedVO vo = new DiscoverFeedVO();
            vo.setId(feed.getId());
            vo.setType(feed.getMediaType() != null ? feed.getMediaType() : "image");
            vo.setCoverUrl(feed.getCoverImage());
            vo.setAspectRatio(feed.getAspectRatio() != null ? feed.getAspectRatio() : new BigDecimal("1.333"));
            vo.setDuration(feed.getDuration() != null ? feed.getDuration() : 0);
            vo.setWidth(feed.getMediaWidth() != null ? feed.getMediaWidth() : 1080);
            vo.setHeight(feed.getMediaHeight() != null ? feed.getMediaHeight() : 1440);
            vo.setTitle(feed.getTitle());
            vo.setContent(feed.getContent());
            vo.setUserId(feed.getUserId());
            // userAvatar 和 userNickname 需要从user服务获取，在BFF层聚合
            vo.setLikeCount(feed.getLikeCount());
            vo.setIsLiked(likeStatusMap.getOrDefault(feed.getId(), false));
            vo.setCommentCount(feed.getCommentCount());
            vo.setCollectCount(feed.getCollectCount());
            vo.setIsCollected(collectStatusMap.getOrDefault(feed.getId(), false));
            vo.setCreateTime(feed.getCreatedAt());
            vo.setLocation(feed.getLocationName());

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer toggleLike(Long feedId, Long userId, boolean isLike) {
        log.info("切换点赞状态: feedId={}, userId={}, isLike={}", feedId, userId, isLike);

        // 检查当前点赞状态
        LambdaQueryWrapper<Like> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Like::getUserId, userId)
            .eq(Like::getTargetType, "feed")
            .eq(Like::getTargetId, feedId);
        Like existingLike = likeMapper.selectOne(checkWrapper);

        boolean currentlyLiked = existingLike != null;

        if (isLike && !currentlyLiked) {
            // 点赞
            Like like = Like.builder()
                .userId(userId)
                .targetType("feed")
                .targetId(feedId)
                .build();
            likeMapper.insert(like);

            // 更新动态点赞数 +1
            updateFeedLikeCount(feedId, 1);
        } else if (!isLike && currentlyLiked) {
            // 取消点赞
            likeMapper.delete(checkWrapper);

            // 更新动态点赞数 -1
            updateFeedLikeCount(feedId, -1);
        }

        // 返回最新点赞数
        return getLikeCount(feedId);
    }

    /**
     * 更新动态点赞数
     */
    private void updateFeedLikeCount(Long feedId, int delta) {
        LambdaUpdateWrapper<Feed> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Feed::getId, feedId)
            .setSql("like_count = like_count + " + delta);
        feedMapper.update(null, updateWrapper);
    }

    @Override
    public Map<Long, Boolean> batchCheckLikeStatus(List<Long> feedIds, Long userId) {
        if (feedIds == null || feedIds.isEmpty() || userId == null) {
            return Collections.emptyMap();
        }

        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
            .eq(Like::getTargetType, "feed")
            .in(Like::getTargetId, feedIds);

        List<Like> likes = likeMapper.selectList(wrapper);

        Set<Long> likedFeedIds = likes.stream()
            .map(Like::getTargetId)
            .collect(Collectors.toSet());

        Map<Long, Boolean> result = new HashMap<>();
        for (Long feedId : feedIds) {
            result.put(feedId, likedFeedIds.contains(feedId));
        }
        return result;
    }

    /**
     * 批量检查收藏状态
     */
    private Map<Long, Boolean> batchCheckCollectStatus(List<Long> feedIds, Long userId) {
        if (feedIds == null || feedIds.isEmpty() || userId == null) {
            return Collections.emptyMap();
        }

        LambdaQueryWrapper<ContentCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContentCollection::getUserId, userId)
            .eq(ContentCollection::getTargetType, "feed")
            .in(ContentCollection::getTargetId, feedIds);

        List<ContentCollection> collections = collectionMapper.selectList(wrapper);

        Set<Long> collectedFeedIds = collections.stream()
            .map(ContentCollection::getTargetId)
            .collect(Collectors.toSet());

        Map<Long, Boolean> result = new HashMap<>();
        for (Long feedId : feedIds) {
            result.put(feedId, collectedFeedIds.contains(feedId));
        }
        return result;
    }

    @Override
    public Integer getLikeCount(Long feedId) {
        Feed feed = feedMapper.selectById(feedId);
        return feed != null ? feed.getLikeCount() : 0;
    }
}
