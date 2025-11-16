package org.dromara.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.content.domain.dto.CommentListQueryDTO;
import org.dromara.content.domain.dto.CommentPublishDTO;
import org.dromara.content.domain.entity.Comment;
import org.dromara.content.domain.entity.Feed;
import org.dromara.content.domain.vo.CommentListVO;
import org.dromara.content.mapper.CommentMapper;
import org.dromara.content.mapper.FeedMapper;
import org.dromara.content.service.ICommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论服务实现
 *
 * @author XiangYuPai
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements ICommentService {

    private final CommentMapper commentMapper;
    private final FeedMapper feedMapper;

    private static final String CACHE_KEY_COMMENT_LIST = "comment:list:";

    @Override
    public Page<CommentListVO> getCommentList(CommentListQueryDTO queryDTO, Long userId) {
        // 1. 验证动态存在
        Feed feed = feedMapper.selectById(queryDTO.getFeedId());
        if (feed == null || feed.getDeleted() == 1) {
            throw new ServiceException("动态不存在");
        }

        // 2. 查询一级评论
        Page<Comment> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getFeedId, queryDTO.getFeedId())
               .isNull(Comment::getParentId);

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

        // 4. 转换为VO并查询二级回复
        List<CommentListVO> voList = commentPage.getRecords().stream()
            .map(comment -> convertToVO(comment, userId))
            .collect(Collectors.toList());

        // 5. 构建返回结果
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

        // 2. 如果是二级回复,验证父评论存在
        if (publishDTO.getParentId() != null) {
            Comment parentComment = commentMapper.selectById(publishDTO.getParentId());
            if (parentComment == null || parentComment.getDeleted() == 1) {
                throw new ServiceException("父评论不存在");
            }
        }

        // 3. 创建评论
        Comment comment = Comment.builder()
            .feedId(publishDTO.getFeedId())
            .userId(userId)
            .content(publishDTO.getContent())
            .parentId(publishDTO.getParentId())
            .replyToUserId(publishDTO.getReplyToUserId())
            .isTop(0)
            .build();

        commentMapper.insert(comment);

        // 4. 更新动态评论数
        feed.setCommentCount(feed.getCommentCount() + 1);
        feedMapper.updateById(feed);

        // 5. 如果是二级回复,更新父评论回复数
        if (publishDTO.getParentId() != null) {
            Comment parentComment = commentMapper.selectById(publishDTO.getParentId());
            parentComment.setReplyCount(parentComment.getReplyCount() + 1);
            commentMapper.updateById(parentComment);
        }

        // 6. 清除缓存
        RedisUtils.deleteObject(CACHE_KEY_COMMENT_LIST + publishDTO.getFeedId());

        log.info("用户 {} 发布评论: {}", userId, comment.getId());

        // 7. TODO: 异步发送通知

        return convertToVO(comment, userId);
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

        // 5. 如果是一级评论,删除所有二级回复
        if (comment.getParentId() == null) {
            LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Comment::getParentId, commentId);
            List<Comment> replies = commentMapper.selectList(wrapper);

            for (Comment reply : replies) {
                reply.setDeleted(1);
                commentMapper.updateById(reply);
                feed.setCommentCount(Math.max(0, feed.getCommentCount() - 1));
            }
            feedMapper.updateById(feed);
        } else {
            // 更新父评论回复数
            Comment parentComment = commentMapper.selectById(comment.getParentId());
            if (parentComment != null) {
                parentComment.setReplyCount(Math.max(0, parentComment.getReplyCount() - 1));
                commentMapper.updateById(parentComment);
            }
        }

        // 6. 清除缓存
        RedisUtils.deleteObject(CACHE_KEY_COMMENT_LIST + comment.getFeedId());

        log.info("用户 {} 删除评论: {}", userId, commentId);
    }

    /**
     * 转换为VO
     */
    private CommentListVO convertToVO(Comment comment, Long userId) {
        CommentListVO vo = CommentListVO.builder()
            .id(comment.getId())
            .feedId(comment.getFeedId())
            .userId(comment.getUserId())
            .content(comment.getContent())
            .likeCount(comment.getLikeCount())
            .replyCount(comment.getReplyCount())
            .isTop(comment.getIsTop() == 1)
            .isLiked(false)
            .canDelete(comment.getUserId().equals(userId))
            .createdAt(comment.getCreatedAt())
            .build();

        // 查询前3条二级回复
        if (comment.getParentId() == null && comment.getReplyCount() > 0) {
            LambdaQueryWrapper<Comment> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.eq(Comment::getParentId, comment.getId())
                       .orderByDesc(Comment::getCreatedAt)
                       .last("LIMIT 3");
            List<Comment> replies = commentMapper.selectList(replyWrapper);

            List<CommentListVO.ReplyVO> replyVOs = replies.stream()
                .map(this::convertToReplyVO)
                .collect(Collectors.toList());

            vo.setReplies(replyVOs);
            vo.setTotalReplies(comment.getReplyCount());
            vo.setHasMoreReplies(comment.getReplyCount() > 3);
        } else {
            vo.setReplies(new ArrayList<>());
            vo.setTotalReplies(0);
            vo.setHasMoreReplies(false);
        }

        // TODO: 填充userInfo

        return vo;
    }

    /**
     * 转换为回复VO
     */
    private CommentListVO.ReplyVO convertToReplyVO(Comment reply) {
        CommentListVO.ReplyVO vo = CommentListVO.ReplyVO.builder()
            .id(reply.getId())
            .content(reply.getContent())
            .replyToUserNickname("") // TODO: 从UserService获取
            .createdAt(reply.getCreatedAt())
            .build();

        // TODO: 填充userInfo

        return vo;
    }

}
