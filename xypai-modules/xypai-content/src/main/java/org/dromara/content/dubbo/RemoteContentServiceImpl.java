package org.dromara.content.dubbo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.content.api.RemoteContentService;
import org.dromara.content.api.domain.vo.FeedSimpleVo;
import org.dromara.content.api.domain.vo.UserFeedsVo;
import org.dromara.content.domain.entity.Feed;
import org.dromara.content.mapper.FeedMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 内容远程服务实现类
 *
 * <p>提供动态内容相关的RPC服务</p>
 *
 * @author XyPai Team
 * @date 2025-11-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DubboService
public class RemoteContentServiceImpl implements RemoteContentService {

    private final FeedMapper feedMapper;

    @Override
    public Map<Long, UserFeedsVo> batchGetUserFeeds(List<Long> userIds, Integer limit) {
        log.info("RPC批量获取用户动态: userIds={}, limit={}", userIds, limit);

        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        int feedLimit = (limit != null && limit > 0) ? limit : 3;

        try {
            // 批量查询所有用户的动态
            List<Feed> allFeeds = feedMapper.selectUsersLatestFeeds(userIds, feedLimit);

            // 按用户ID分组
            Map<Long, List<Feed>> feedsByUser = allFeeds.stream()
                .collect(Collectors.groupingBy(Feed::getUserId));

            // 构建结果
            Map<Long, UserFeedsVo> result = new HashMap<>();
            for (Long userId : userIds) {
                List<Feed> userFeeds = feedsByUser.getOrDefault(userId, Collections.emptyList());
                UserFeedsVo vo = UserFeedsVo.builder()
                    .userId(userId)
                    .feeds(userFeeds.stream()
                        .map(this::convertToSimpleVo)
                        .collect(Collectors.toList()))
                    .totalCount(userFeeds.size())
                    .build();
                result.put(userId, vo);
            }

            log.info("批量获取用户动态完成，共 {} 个用户，{} 条动态",
                userIds.size(), allFeeds.size());

            return result;
        } catch (Exception e) {
            log.error("批量获取用户动态失败", e);
            return Collections.emptyMap();
        }
    }

    @Override
    public UserFeedsVo getUserFeeds(Long userId, Integer limit) {
        log.info("RPC获取用户动态: userId={}, limit={}", userId, limit);

        if (userId == null) {
            return null;
        }

        int feedLimit = (limit != null && limit > 0) ? limit : 3;

        try {
            List<Feed> feeds = feedMapper.selectUserLatestFeeds(userId, feedLimit);
            Integer totalCount = feedMapper.countUserFeeds(userId);

            return UserFeedsVo.builder()
                .userId(userId)
                .feeds(feeds.stream()
                    .map(this::convertToSimpleVo)
                    .collect(Collectors.toList()))
                .totalCount(totalCount)
                .build();
        } catch (Exception e) {
            log.error("获取用户动态失败: userId={}", userId, e);
            return UserFeedsVo.builder()
                .userId(userId)
                .feeds(Collections.emptyList())
                .totalCount(0)
                .build();
        }
    }

    @Override
    public Integer getUserFeedCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        try {
            return feedMapper.countUserFeeds(userId);
        } catch (Exception e) {
            log.error("获取用户动态数量失败: userId={}", userId, e);
            return 0;
        }
    }

    @Override
    public Map<Long, Integer> batchGetUserFeedCounts(List<Long> userIds) {
        log.info("RPC批量获取用户动态数量: userIds={}", userIds);

        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Integer> result = new HashMap<>();
        for (Long userId : userIds) {
            result.put(userId, getUserFeedCount(userId));
        }
        return result;
    }

    /**
     * 转换为简单VO
     */
    private FeedSimpleVo convertToSimpleVo(Feed feed) {
        return FeedSimpleVo.builder()
            .feedId(feed.getId())
            .coverImage(feed.getCoverImage())
            .content(truncateContent(feed.getContent(), 50))
            .type(feed.getType())
            .likeCount(feed.getLikeCount())
            .commentCount(feed.getCommentCount())
            .createdTimestamp(feed.getCreatedTimestamp())
            .build();
    }

    /**
     * 截取内容
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) {
            return null;
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }
}
