package com.xypai.content.service;

import com.xypai.content.domain.dto.CommentAddDTO;
import com.xypai.content.domain.vo.CommentVO;

import java.util.List;

/**
 * 评论服务接口
 * 
 * 支持一级评论和二级回复：
 * - 一级评论：parent_id = null
 * - 二级回复：parent_id != null
 *
 * @author David (内容服务组)
 * @date 2025-01-15
 */
public interface ICommentService {

    /**
     * 发表评论
     *
     * @param commentAddDTO 评论数据
     * @return 评论ID
     */
    Long addComment(CommentAddDTO commentAddDTO);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @return 是否成功
     */
    boolean deleteComment(Long commentId);

    /**
     * 获取内容的评论列表
     *
     * @param contentId 内容ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 评论列表（包含二级回复）
     */
    List<CommentVO> getCommentList(Long contentId, Integer pageNum, Integer pageSize);

    /**
     * 获取评论的所有回复
     *
     * @param parentId 一级评论ID
     * @return 回复列表
     */
    List<CommentVO> getCommentReplies(Long parentId);

    /**
     * 评论点赞/取消点赞
     *
     * @param commentId 评论ID
     * @return 是否成功
     */
    boolean likeComment(Long commentId);

    /**
     * 置顶/取消置顶评论
     *
     * @param commentId 评论ID
     * @param isTop 是否置顶
     * @return 是否成功
     */
    boolean topComment(Long commentId, Boolean isTop);

    /**
     * 统计内容的评论数
     *
     * @param contentId 内容ID
     * @return 评论数量
     */
    Long countComments(Long contentId);
}

