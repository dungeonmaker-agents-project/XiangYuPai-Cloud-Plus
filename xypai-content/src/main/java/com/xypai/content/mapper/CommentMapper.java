package com.xypai.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.content.domain.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评论Mapper接口
 *
 * @author David (内容服务组)
 * @date 2025-01-15
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 查询内容的一级评论列表
     *
     * @param contentId 内容ID
     * @param limit 限制数量
     * @return 评论列表
     */
    List<Comment> selectFirstLevelComments(@Param("contentId") Long contentId, 
                                           @Param("limit") Integer limit);

    /**
     * 查询评论的二级回复列表
     *
     * @param parentId 一级评论ID
     * @param limit 限制数量
     * @return 回复列表
     */
    List<Comment> selectSecondLevelReplies(@Param("parentId") Long parentId, 
                                           @Param("limit") Integer limit);

    /**
     * 查询评论的所有二级回复（分页）
     *
     * @param parentId 一级评论ID
     * @return 回复列表
     */
    List<Comment> selectAllRepliesByParentId(@Param("parentId") Long parentId);

    /**
     * 增加评论回复数
     *
     * @param commentId 评论ID
     * @param increment 增量（1=新增，-1=删除）
     * @return 影响行数
     */
    int incrementReplyCount(@Param("commentId") Long commentId, 
                           @Param("increment") Integer increment);

    /**
     * 增加评论点赞数
     *
     * @param commentId 评论ID
     * @param increment 增量（1=点赞，-1=取消）
     * @return 影响行数
     */
    int incrementLikeCount(@Param("commentId") Long commentId, 
                          @Param("increment") Integer increment);

    /**
     * 查询用户的评论列表
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 评论列表
     */
    List<Comment> selectCommentsByUserId(@Param("userId") Long userId, 
                                         @Param("limit") Integer limit);

    /**
     * 统计内容的评论数
     *
     * @param contentId 内容ID
     * @return 评论数量
     */
    Long countByContentId(@Param("contentId") Long contentId);
}

