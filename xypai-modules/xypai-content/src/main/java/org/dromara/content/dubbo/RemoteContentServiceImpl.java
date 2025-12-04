package org.dromara.content.dubbo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.content.api.RemoteContentService;
import org.dromara.content.api.domain.vo.FeedSimpleVo;
import org.dromara.content.api.domain.vo.RemoteMomentPageResult;
import org.dromara.content.api.domain.vo.RemoteMomentVo;
import org.dromara.content.api.domain.vo.UserFeedsVo;
import org.dromara.content.domain.entity.Feed;
import org.dromara.content.domain.entity.Like;
import org.dromara.content.mapper.FeedMapper;
import org.dromara.content.mapper.LikeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final LikeMapper likeMapper;

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

    // ==================== 对方主页动态列表相关方法实现 ====================

    @Override
    public RemoteMomentPageResult getUserMomentList(Long targetUserId, Long currentUserId, Integer pageNum, Integer pageSize) {
        log.info("RPC获取用户动态列表: targetUserId={}, currentUserId={}, pageNum={}, pageSize={}",
            targetUserId, currentUserId, pageNum, pageSize);

        if (targetUserId == null) {
            return createEmptyMomentPageResult(pageNum, pageSize);
        }

        int page = (pageNum != null && pageNum > 0) ? pageNum : 1;
        int size = (pageSize != null && pageSize > 0) ? pageSize : 10;

        try {
            // 构建查询条件
            LambdaQueryWrapper<Feed> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Feed::getUserId, targetUserId);
            wrapper.eq(Feed::getStatus, 0); // 正常状态
            wrapper.eq(Feed::getDeleted, 0); // 未删除
            wrapper.eq(Feed::getVisibility, 0); // 公开可见
            wrapper.orderByDesc(Feed::getCreatedTimestamp);

            // 分页查询
            Page<Feed> feedPage = new Page<>(page, size);
            Page<Feed> result = feedMapper.selectPage(feedPage, wrapper);

            // 获取当前用户对这些动态的点赞状态
            Set<Long> likedFeedIds = Collections.emptySet();
            if (currentUserId != null && !result.getRecords().isEmpty()) {
                List<Long> feedIds = result.getRecords().stream()
                    .map(Feed::getId)
                    .collect(Collectors.toList());
                likedFeedIds = likeMapper.findLikedTargetIds(currentUserId, "feed", feedIds);
            }

            // 转换为VO
            Set<Long> finalLikedFeedIds = likedFeedIds;
            List<RemoteMomentVo> momentList = result.getRecords().stream()
                .map(feed -> convertToMomentVo(feed, finalLikedFeedIds.contains(feed.getId())))
                .collect(Collectors.toList());

            // 构建返回结果
            boolean hasMore = result.getCurrent() * result.getSize() < result.getTotal();

            return RemoteMomentPageResult.builder()
                .list(momentList)
                .hasMore(hasMore)
                .total(result.getTotal())
                .pageNum(page)
                .pageSize(size)
                .build();

        } catch (Exception e) {
            log.error("获取用户动态列表失败: targetUserId={}", targetUserId, e);
            return createEmptyMomentPageResult(page, size);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeMoment(Long userId, Long momentId) {
        log.info("RPC点赞动态: userId={}, momentId={}", userId, momentId);

        if (userId == null || momentId == null) {
            return false;
        }

        try {
            // 检查是否已点赞
            if (checkMomentLiked(userId, momentId)) {
                log.info("用户已点赞过该动态: userId={}, momentId={}", userId, momentId);
                return false;
            }

            // 检查动态是否存在
            Feed feed = feedMapper.selectById(momentId);
            if (feed == null || feed.getDeleted() == 1) {
                log.warn("动态不存在或已删除: momentId={}", momentId);
                return false;
            }

            // 插入点赞记录
            Like like = Like.builder()
                .userId(userId)
                .targetType("feed")
                .targetId(momentId)
                .build();
            likeMapper.insert(like);

            // 更新动态点赞数
            feed.setLikeCount(feed.getLikeCount() + 1);
            feedMapper.updateById(feed);

            log.info("点赞成功: userId={}, momentId={}", userId, momentId);
            return true;

        } catch (Exception e) {
            log.error("点赞动态失败: userId={}, momentId={}", userId, momentId, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlikeMoment(Long userId, Long momentId) {
        log.info("RPC取消点赞动态: userId={}, momentId={}", userId, momentId);

        if (userId == null || momentId == null) {
            return false;
        }

        try {
            // 查找点赞记录
            LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Like::getUserId, userId);
            wrapper.eq(Like::getTargetType, "feed");
            wrapper.eq(Like::getTargetId, momentId);

            Like like = likeMapper.selectOne(wrapper);
            if (like == null) {
                log.info("用户未点赞过该动态: userId={}, momentId={}", userId, momentId);
                return false;
            }

            // 删除点赞记录
            likeMapper.deleteById(like.getId());

            // 更新动态点赞数
            Feed feed = feedMapper.selectById(momentId);
            if (feed != null && feed.getLikeCount() > 0) {
                feed.setLikeCount(feed.getLikeCount() - 1);
                feedMapper.updateById(feed);
            }

            log.info("取消点赞成功: userId={}, momentId={}", userId, momentId);
            return true;

        } catch (Exception e) {
            log.error("取消点赞动态失败: userId={}, momentId={}", userId, momentId, e);
            throw e;
        }
    }

    @Override
    public boolean checkMomentLiked(Long userId, Long momentId) {
        if (userId == null || momentId == null) {
            return false;
        }

        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId);
        wrapper.eq(Like::getTargetType, "feed");
        wrapper.eq(Like::getTargetId, momentId);

        return likeMapper.selectCount(wrapper) > 0;
    }

    /**
     * 转换为动态VO
     */
    private RemoteMomentVo convertToMomentVo(Feed feed, boolean isLiked) {
        // 确定媒体类型
        String mediaType = feed.getMediaType() != null ? feed.getMediaType() : "image";

        // 计算宽高比，默认4:3
        BigDecimal aspectRatio = feed.getAspectRatio() != null
            ? feed.getAspectRatio()
            : new BigDecimal("1.333");

        return RemoteMomentVo.builder()
            .id(feed.getId())
            .type(mediaType)
            .coverUrl(feed.getCoverImage())
            .aspectRatio(aspectRatio)
            .duration(feed.getDuration())
            .title(feed.getTitle())
            .authorId(feed.getUserId())
            .authorAvatar(null) // 由BFF层填充
            .authorNickname(null) // 由BFF层填充
            .likeCount(feed.getLikeCount())
            .isLiked(isLiked)
            .createdTimestamp(feed.getCreatedTimestamp())
            .build();
    }

    /**
     * 创建空的动态分页结果
     */
    private RemoteMomentPageResult createEmptyMomentPageResult(Integer pageNum, Integer pageSize) {
        return RemoteMomentPageResult.builder()
            .list(Collections.emptyList())
            .hasMore(false)
            .total(0L)
            .pageNum(pageNum != null ? pageNum : 1)
            .pageSize(pageSize != null ? pageSize : 10)
            .build();
    }
}
