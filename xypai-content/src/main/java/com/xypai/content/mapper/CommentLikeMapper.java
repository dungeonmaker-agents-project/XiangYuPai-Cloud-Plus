package com.xypai.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.content.domain.entity.CommentLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评论点赞Mapper接口
 *
 * @author David (内容服务组)
 * @date 2025-01-15
 */
@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {

    /**
     * 查询用户是否已点赞评论
     *
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 点赞记录，如果未点赞则返回null
     */
    CommentLike selectByCommentIdAndUserId(@Param("commentId") Long commentId, 
                                           @Param("userId") Long userId);

    /**
     * 批量查询用户的点赞状态
     *
     * @param commentIds 评论ID列表
     * @param userId 用户ID
     * @return 点赞记录列表
     */
    List<CommentLike> selectBatchByCommentIdsAndUserId(@Param("commentIds") List<Long> commentIds, 
                                                       @Param("userId") Long userId);
}

