package org.dromara.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.appuser.api.RemoteAppUserService;
import org.dromara.appuser.api.domain.vo.RemoteAppUserVo;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.content.domain.dto.CommentListQueryDTO;
import org.dromara.content.domain.dto.CommentPublishDTO;
import org.dromara.content.domain.entity.Comment;
import org.dromara.content.domain.entity.Feed;
import org.dromara.content.domain.vo.CommentListVO;
import org.dromara.content.mapper.CommentMapper;
import org.dromara.content.mapper.FeedMapper;
import org.dromara.content.mapper.LikeMapper;
import org.dromara.content.service.ICommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论服务实现（仅支持一级评论）
 *
 * @author XiangYuPai
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements ICommentService {

    private final CommentMapper commentMapper;
    private final FeedMapper feedMapper;
    private final LikeMapper likeMapper;

    @DubboReference
    private RemoteAppUserService remoteAppUserService;

    private static final String CACHE_KEY_COMMENT_LIST = "comment:list:";

    @Override
    public Page<CommentListVO> getCommentList(CommentListQueryDTO queryDTO, Long userId) {
        // 1. 验证动态存在
        Feed feed = feedMapper.selectById(queryDTO.getFeedId());
        if (feed == null || feed.getDeleted() == 1) {
            throw new ServiceException("动态不存在");
        }

        // 2. 查询评论（只有一级评论）
        // 注意: @TableLogic 注解会自动添加 deleted = 0 条件
        Page<Comment> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getFeedId, queryDTO.getFeedId());

        // 3. 排序
        String sortType = queryDTO.getSortType() != null ? queryDTO.getSortType() : "time";
        switch (sortType) {
            case "hot":
            case "like":
                wrapper.orderByDesc(Comment::getLikeCount);
                break;
            default:
                wrapper.orderByDesc(Comment::getIsTop)
                       .orderByDesc(Comment::getCreatedAt);
        }

        Page<Comment> commentPage = commentMapper.selectPage(page, wrapper);

        // 4. 收集所有评论者的用户ID
        Set<Long> userIds = commentPage.getRecords().stream()
            .map(Comment::getUserId)
            .collect(Collectors.toSet());

        // 5. 批量查询用户信息
        Map<Long, RemoteAppUserVo> userInfoMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            try {
                userInfoMap = remoteAppUserService.batchGetUserBasicInfo(
                    new ArrayList<>(userIds), userId);
                if (userInfoMap == null) {
                    userInfoMap = new HashMap<>();
                }
            } catch (Exception e) {
                log.warn("批量获取用户信息失败: {}", e.getMessage());
            }
        }

        // 6. 查询当前用户对评论的点赞状态
        Set<Long> likedCommentIds = new HashSet<>();
        if (userId != null) {
            List<Long> commentIds = commentPage.getRecords().stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

            if (!commentIds.isEmpty()) {
                Set<Long> result = likeMapper.findLikedTargetIds(userId, "comment", commentIds);
                if (result != null) {
                    likedCommentIds = result;
                }
            }
        }

        // 7. 转换为VO
        final Set<Long> finalLikedCommentIds = likedCommentIds;
        final Map<Long, RemoteAppUserVo> finalUserInfoMap = userInfoMap;
        List<CommentListVO> voList = commentPage.getRecords().stream()
            .map(comment -> convertToVO(comment, userId, finalUserInfoMap, finalLikedCommentIds))
            .collect(Collectors.toList());

        // 8. 构建返回结果
        Page<CommentListVO> resultPage = new Page<>(commentPage.getCurrent(), commentPage.getSize());
        resultPage.setRecords(voList);
        resultPage.setTotal(commentPage.getTotal());

        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentListVO publishComment(CommentPublishDTO publishDTO, Long userId) {
        // 1. 验证动态存在
        Feed feed = feedMapper.selectById(publishDTO.getFeedId());
        if (feed == null || feed.getDeleted() == 1) {
            throw new ServiceException("动态不存在");
        }

        // 2. 创建评论（只支持一级评论）
        Comment comment = Comment.builder()
            .feedId(publishDTO.getFeedId())
            .userId(userId)
            .content(publishDTO.getContent())
            .isTop(0)
            .build();

        commentMapper.insert(comment);

        // 3. 更新动态评论数
        feed.setCommentCount(feed.getCommentCount() + 1);
        feedMapper.updateById(feed);

        // 4. 清除缓存
        RedisUtils.deleteObject(CACHE_KEY_COMMENT_LIST + publishDTO.getFeedId());

        log.info("用户 {} 发布评论: {}", userId, comment.getId());

        // 5. 构建返回的VO，包含用户信息
        Map<Long, RemoteAppUserVo> userInfoMap = new HashMap<>();
        try {
            userInfoMap = remoteAppUserService.batchGetUserBasicInfo(
                Collections.singletonList(userId), userId);
            if (userInfoMap == null) {
                userInfoMap = new HashMap<>();
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败: {}", e.getMessage());
        }

        return convertToVO(comment, userId, userInfoMap, new HashSet<>());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long userId) {
        // 1. 查询评论
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new ServiceException("评论不存在");
        }

        // 2. 验证权限
        if (!comment.getUserId().equals(userId)) {
            throw new ServiceException("无权删除此评论");
        }

        // 3. 软删除
        comment.setDeleted(1);
        commentMapper.updateById(comment);

        // 4. 更新动态评论数
        Feed feed = feedMapper.selectById(comment.getFeedId());
        feed.setCommentCount(Math.max(0, feed.getCommentCount() - 1));
        feedMapper.updateById(feed);

        // 5. 清除缓存
        RedisUtils.deleteObject(CACHE_KEY_COMMENT_LIST + comment.getFeedId());

        log.info("用户 {} 删除评论: {}", userId, commentId);
    }

    /**
     * 转换为VO
     */
    private CommentListVO convertToVO(Comment comment, Long userId,
                                       Map<Long, RemoteAppUserVo> userInfoMap,
                                       Set<Long> likedCommentIds) {
        CommentListVO vo = CommentListVO.builder()
            .id(comment.getId())
            .feedId(comment.getFeedId())
            .userId(comment.getUserId())
            .content(comment.getContent())
            .likeCount(comment.getLikeCount())
            .isTop(comment.getIsTop() == 1)
            .isLiked(likedCommentIds.contains(comment.getId()))
            .canDelete(userId != null && comment.getUserId().equals(userId))
            .createdAt(comment.getCreatedAt())
            .build();

        // 填充用户信息
        RemoteAppUserVo userVo = userInfoMap.get(comment.getUserId());
        if (userVo != null) {
            vo.setUserInfo(CommentListVO.UserInfoVO.builder()
                .id(userVo.getUserId())
                .nickname(userVo.getNickname())
                .avatar(userVo.getAvatar())
                .level(userVo.getLevel())
                .levelName(userVo.getLevelName())
                .build());
        } else {
            vo.setUserInfo(CommentListVO.UserInfoVO.builder()
                .id(comment.getUserId())
                .nickname("用户" + comment.getUserId())
                .avatar("")
                .level(1)
                .levelName("青铜")
                .build());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean pinComment(Long commentId, Boolean pin, Long userId) {
        // 1. 查询评论
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getDeleted() == 1) {
            throw new ServiceException("评论不存在");
        }

        // 2. 查询动态,验证是否是动态作者
        Feed feed = feedMapper.selectById(comment.getFeedId());
        if (feed == null || feed.getDeleted() == 1) {
            throw new ServiceException("动态不存在");
        }

        if (!feed.getUserId().equals(userId)) {
            log.warn("用户 {} 尝试置顶动态 {} 的评论 {}, 但不是动态作者", userId, feed.getId(), commentId);
            return false;
        }

        // 3. 更新置顶状态
        comment.setIsTop(pin ? 1 : 0);
        commentMapper.updateById(comment);

        // 4. 清除缓存
        RedisUtils.deleteObject(CACHE_KEY_COMMENT_LIST + comment.getFeedId());

        log.info("用户 {} {}评论: {}", userId, pin ? "置顶" : "取消置顶", commentId);

        return true;
    }

}
