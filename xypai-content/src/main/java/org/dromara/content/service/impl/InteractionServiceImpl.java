package org.dromara.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.content.domain.dto.InteractionDTO;
import org.dromara.content.domain.entity.*;
import org.dromara.content.domain.vo.InteractionResultVO;
import org.dromara.content.mapper.*;
import org.dromara.content.service.IInteractionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * 互动服务实现
 *
 * @author XiangYuPai
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements IInteractionService {

    private final LikeMapper likeMapper;
    private final CollectionMapper collectionMapper;
    private final ShareMapper shareMapper;
    private final FeedMapper feedMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InteractionResultVO handleLike(InteractionDTO interactionDTO, Long userId) {
        String targetType = interactionDTO.getTargetType();
        Long targetId = interactionDTO.getTargetId();
        String action = interactionDTO.getAction();

        // 1. 查询是否已点赞
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
               .eq(Like::getTargetType, targetType)
               .eq(Like::getTargetId, targetId);
        Like existingLike = likeMapper.selectOne(wrapper);

        boolean isLiked;
        int newCount;

        if ("like".equals(action)) {
            // 点赞
            if (existingLike != null) {
                throw new ServiceException("已经点赞过了");
            }

            Like like = Like.builder()
                .userId(userId)
                .targetType(targetType)
                .targetId(targetId)
                .build();
            likeMapper.insert(like);

            // 更新计数
            newCount = incrementLikeCount(targetType, targetId, 1);
            isLiked = true;

            log.info("用户 {} 点赞 {} {}", userId, targetType, targetId);

        } else if ("unlike".equals(action)) {
            // 取消点赞
            if (existingLike == null) {
                throw new ServiceException("还没有点赞");
            }

            likeMapper.deleteById(existingLike.getId());

            // 更新计数
            newCount = incrementLikeCount(targetType, targetId, -1);
            isLiked = false;

            log.info("用户 {} 取消点赞 {} {}", userId, targetType, targetId);

        } else {
            throw new ServiceException("无效的操作类型");
        }

        return InteractionResultVO.builder()
            .success(true)
            .count(newCount)
            .isActive(isLiked)
            .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InteractionResultVO handleCollect(InteractionDTO interactionDTO, Long userId) {
        String targetType = interactionDTO.getTargetType();
        Long targetId = interactionDTO.getTargetId();
        String action = interactionDTO.getAction();

        // 仅支持收藏动态
        if (!"feed".equals(targetType)) {
            throw new ServiceException("仅支持收藏动态");
        }

        // 1. 查询是否已收藏
        LambdaQueryWrapper<ContentCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContentCollection::getUserId, userId)
               .eq(ContentCollection::getTargetType, targetType)
               .eq(ContentCollection::getTargetId, targetId);
        ContentCollection existingCollection = collectionMapper.selectOne(wrapper);

        boolean isCollected;
        int newCount;

        if ("collect".equals(action)) {
            // 收藏
            if (existingCollection != null) {
                throw new ServiceException("已经收藏过了");
            }

            ContentCollection collection = ContentCollection.builder()
                .userId(userId)
                .targetType(targetType)
                .targetId(targetId)
                .build();
            collectionMapper.insert(collection);

            // 更新计数
            newCount = incrementCollectCount(targetId, 1);
            isCollected = true;

            log.info("用户 {} 收藏动态 {}", userId, targetId);

        } else if ("uncollect".equals(action)) {
            // 取消收藏
            if (existingCollection == null) {
                throw new ServiceException("还没有收藏");
            }

            collectionMapper.deleteById(existingCollection.getId());

            // 更新计数
            newCount = incrementCollectCount(targetId, -1);
            isCollected = false;

            log.info("用户 {} 取消收藏动态 {}", userId, targetId);

        } else {
            throw new ServiceException("无效的操作类型");
        }

        return InteractionResultVO.builder()
            .success(true)
            .count(newCount)
            .isActive(isCollected)
            .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InteractionResultVO handleShare(Long targetId, String shareChannel, Long userId) {
        // 1. 验证动态存在
        Feed feed = feedMapper.selectById(targetId);
        if (feed == null || feed.getDeleted() == 1) {
            throw new ServiceException("动态不存在");
        }

        // 2. 创建分享记录
        Share share = Share.builder()
            .userId(userId)
            .targetType("feed")
            .targetId(targetId)
            .shareChannel(shareChannel)
            .build();
        shareMapper.insert(share);

        // 3. 更新分享数
        feed.setShareCount(feed.getShareCount() + 1);
        feedMapper.updateById(feed);

        // 4. 使用Redis计数器
        String countKey = "feed:share:count:" + targetId;
        RedisUtils.setCacheObject(countKey, feed.getShareCount(), Duration.ofDays(1));

        log.info("用户 {} 分享动态 {} 到 {}", userId, targetId, shareChannel);

        return InteractionResultVO.builder()
            .success(true)
            .count(feed.getShareCount())
            .isActive(true)
            .build();
    }

    /**
     * 增加点赞数
     */
    private int incrementLikeCount(String targetType, Long targetId, int delta) {
        if ("feed".equals(targetType)) {
            Feed feed = feedMapper.selectById(targetId);
            if (feed != null) {
                feed.setLikeCount(Math.max(0, feed.getLikeCount() + delta));
                feedMapper.updateById(feed);
                return feed.getLikeCount();
            }
        } else if ("comment".equals(targetType)) {
            Comment comment = commentMapper.selectById(targetId);
            if (comment != null) {
                comment.setLikeCount(Math.max(0, comment.getLikeCount() + delta));
                commentMapper.updateById(comment);
                return comment.getLikeCount();
            }
        }
        return 0;
    }

    /**
     * 增加收藏数
     */
    private int incrementCollectCount(Long feedId, int delta) {
        Feed feed = feedMapper.selectById(feedId);
        if (feed != null) {
            feed.setCollectCount(Math.max(0, feed.getCollectCount() + delta));
            feedMapper.updateById(feed);
            return feed.getCollectCount();
        }
        return 0;
    }

}
