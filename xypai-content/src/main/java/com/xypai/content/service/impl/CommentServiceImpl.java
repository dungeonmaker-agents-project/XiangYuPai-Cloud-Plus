package com.xypai.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import com.xypai.content.domain.dto.CommentAddDTO;
import com.xypai.content.domain.entity.Comment;
import com.xypai.content.domain.entity.CommentLike;
import com.xypai.content.domain.vo.CommentVO;
import com.xypai.content.mapper.CommentLikeMapper;
import com.xypai.content.mapper.CommentMapper;
import com.xypai.content.service.ICommentService;
import com.xypai.content.service.IContentStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论服务实现�?
 * 
 * 核心功能�?
 * 1. 一级评�?+ 二级回复
 * 2. 评论点赞
 * 3. 评论置顶
 * 4. 敏感词过滤（TODO�?
 *
 * @author David (内容服务�?
 * @date 2025-01-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements ICommentService {

    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final IContentStatsService contentStatsService;

    /**
     * 最多显示二级回复数�?
     */
    private static final int MAX_REPLIES_DISPLAY = 3;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(CommentAddDTO commentAddDTO) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        // 构建评论对象
        Comment comment = Comment.builder()
                .contentId(commentAddDTO.getContentId())
                .userId(currentUserId)
                .parentId(commentAddDTO.getParentId())
                .replyToId(commentAddDTO.getReplyToId())
                .replyToUserId(commentAddDTO.getReplyToUserId())
                .commentText(commentAddDTO.getCommentText())
                .status(Comment.Status.NORMAL.getCode())
                .createdAt(LocalDateTime.now())
                .build();

        // TODO: 敏感词过�?
        // comment.setCommentText(sensitiveWordFilter.filter(comment.getCommentText()));

        // 插入评论
        int result = commentMapper.insert(comment);
        if (result <= 0) {
            throw new ServiceException("发表评论失败");
        }

        // 如果是二级回复，更新一级评论的回复�?
        if (comment.isSecondLevel() && comment.getParentId() != null) {
            commentMapper.incrementReplyCount(comment.getParentId(), 1);
        }

        // 更新内容的评论数（通过ContentStats�?
        contentStatsService.incrementCommentCount(commentAddDTO.getContentId(), 1);

        log.info("发表评论成功, commentId={}, contentId={}, userId={}", 
                comment.getId(), comment.getContentId(), currentUserId);

        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(Long commentId) {
        if (commentId == null) {
            throw new ServiceException("评论ID不能为空");
        }

        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new ServiceException("评论不存在");
        }

        // 权限检查
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null || !currentUserId.equals(comment.getUserId())) {
            throw new ServiceException("无权限删除该评论");
        }

        // 逻辑删除
        Comment updateComment = Comment.builder()
                .id(commentId)
                .status(Comment.Status.DELETED.getCode())
                .updatedAt(LocalDateTime.now())
                .build();

        int result = commentMapper.updateById(updateComment);

        // 如果是二级回复，减少一级评论的回复�?
        if (comment.isSecondLevel() && comment.getParentId() != null) {
            commentMapper.incrementReplyCount(comment.getParentId(), -1);
        }

        // 减少内容的评论数
        contentStatsService.incrementCommentCount(comment.getContentId(), -1);

        log.info("删除评论成功, commentId={}", commentId);
        return result > 0;
    }

    @Override
    public List<CommentVO> getCommentList(Long contentId, Integer pageNum, Integer pageSize) {
        if (contentId == null) {
            throw new ServiceException("内容ID不能为空");
        }

        // 查询一级评�?
        int limit = pageSize != null ? pageSize : 20;
        List<Comment> firstLevelComments = commentMapper.selectFirstLevelComments(contentId, limit);

        if (firstLevelComments.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取当前用户ID
        Long currentUserId = LoginHelper.getUserId();

        // 转换为VO
        List<CommentVO> result = new ArrayList<>();
        for (Comment comment : firstLevelComments) {
            CommentVO vo = convertToVO(comment);

            // 查询二级回复（最多显�?条）
            List<Comment> replies = commentMapper.selectSecondLevelReplies(
                    comment.getId(), MAX_REPLIES_DISPLAY);
            
            if (!replies.isEmpty()) {
                List<CommentVO> replyVOs = replies.stream()
                        .map(this::convertToVO)
                        .collect(Collectors.toList());
                vo.setReplies(replyVOs);
                vo.setTotalReplies(comment.getReplyCount());
                vo.setHasMoreReplies(comment.getReplyCount() > MAX_REPLIES_DISPLAY);
            }

            // 设置当前用户点赞状�?
            if (currentUserId != null) {
                vo.setLiked(checkUserLiked(comment.getId(), currentUserId));
            }

            result.add(vo);
        }

        return result;
    }

    @Override
    public List<CommentVO> getCommentReplies(Long parentId) {
        if (parentId == null) {
            throw new ServiceException("一级评论ID不能为空");
        }

        List<Comment> replies = commentMapper.selectAllRepliesByParentId(parentId);
        
        return replies.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeComment(Long commentId) {
        if (commentId == null) {
            throw new ServiceException("评论ID不能为空");
        }

        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        // 查询是否已点�?
        CommentLike existLike = commentLikeMapper.selectByCommentIdAndUserId(commentId, currentUserId);

        if (existLike == null) {
            // 新增点赞记录
            CommentLike like = CommentLike.builder()
                    .commentId(commentId)
                    .userId(currentUserId)
                    .status(1)
                    .createdAt(LocalDateTime.now())
                    .build();

            commentLikeMapper.insert(like);
            commentMapper.incrementLikeCount(commentId, 1);

            log.info("评论点赞成功, commentId={}, userId={}", commentId, currentUserId);
            return true;

        } else {
            // 切换点赞状�?
            int newStatus = existLike.isLiked() ? 0 : 1;
            int increment = existLike.isLiked() ? -1 : 1;

            CommentLike updateLike = CommentLike.builder()
                    .id(existLike.getId())
                    .status(newStatus)
                    .build();

            commentLikeMapper.updateById(updateLike);
            commentMapper.incrementLikeCount(commentId, increment);

            log.info("切换评论点赞状�? commentId={}, userId={}, newStatus={}", 
                    commentId, currentUserId, newStatus);
            return newStatus == 1;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean topComment(Long commentId, Boolean isTop) {
        if (commentId == null) {
            throw new ServiceException("评论ID不能为空");
        }

        Comment updateComment = Comment.builder()
                .id(commentId)
                .isTop(isTop != null && isTop)
                .updatedAt(LocalDateTime.now())
                .build();

        int result = commentMapper.updateById(updateComment);
        log.info("{}评论, commentId={}", isTop ? "置顶" : "取消置顶", commentId);

        return result > 0;
    }

    @Override
    public Long countComments(Long contentId) {
        if (contentId == null) {
            return 0L;
        }

        return commentMapper.countByContentId(contentId);
    }

    /**
     * 转换为VO
     */
    private CommentVO convertToVO(Comment comment) {
        return CommentVO.builder()
                .id(comment.getId())
                .contentId(comment.getContentId())
                .userId(comment.getUserId())
                // TODO: 查询用户信息（通过Feign调用用户服务�?
                .userNickname("用户" + comment.getUserId())
                .userAvatar(null)
                .parentId(comment.getParentId())
                .replyToId(comment.getReplyToId())
                .replyToUserId(comment.getReplyToUserId())
                // TODO: 查询被回复用户昵�?
                .replyToUserNickname(comment.getReplyToUserId() != null ? 
                        "用户" + comment.getReplyToUserId() : null)
                .commentText(comment.getCommentText())
                .likeCount(comment.getLikeCount())
                .replyCount(comment.getReplyCount())
                .isTop(comment.getIsTop())
                .liked(false)
                .createdAt(comment.getCreatedAt())
                .build();
    }

    /**
     * 检查用户是否已点赞
     */
    private boolean checkUserLiked(Long commentId, Long userId) {
        CommentLike like = commentLikeMapper.selectByCommentIdAndUserId(commentId, userId);
        return like != null && like.isLiked();
    }
}

