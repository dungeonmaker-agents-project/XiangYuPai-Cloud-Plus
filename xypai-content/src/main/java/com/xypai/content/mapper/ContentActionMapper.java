package com.xypai.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.content.domain.entity.ContentAction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 内容行为Mapper接口
 *
 * @author xypai
 * @date 2025-01-01
 */
@Mapper
public interface ContentActionMapper extends BaseMapper<ContentAction> {

    /**
     * 查询内容的行为统计
     *
     * @param contentId 内容ID
     * @return 统计结果 key为行为类型，value为数量
     */
    Map<Integer, Long> selectActionStatsByContentId(@Param("contentId") Long contentId);

    /**
     * 查询用户对内容的行为记录
     *
     * @param contentId 内容ID
     * @param userId 用户ID
     * @return 行为列表
     */
    List<ContentAction> selectUserActionsOnContent(@Param("contentId") Long contentId, 
                                                   @Param("userId") Long userId);

    /**
     * 查询内容的评论列表(包含回复)
     *
     * @param contentId 内容ID
     * @param limit 限制数量
     * @return 评论列表
     */
    List<ContentAction> selectContentComments(@Param("contentId") Long contentId, 
                                            @Param("limit") Integer limit);

    /**
     * 查询评论的回复列表
     *
     * @param commentId 评论ID
     * @return 回复列表
     */
    List<ContentAction> selectCommentReplies(@Param("commentId") Long commentId);

    /**
     * 查询用户的收藏列表
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 收藏的内容行为列表
     */
    List<ContentAction> selectUserCollections(@Param("userId") Long userId, 
                                            @Param("limit") Integer limit);

    /**
     * 查询活动报名列表
     *
     * @param contentId 活动内容ID
     * @return 报名记录列表
     */
    List<ContentAction> selectActivitySignups(@Param("contentId") Long contentId);

    /**
     * 删除用户对内容的特定行为
     *
     * @param contentId 内容ID
     * @param userId 用户ID
     * @param action 行为类型
     * @return 影响行数
     */
    int deleteUserAction(@Param("contentId") Long contentId, 
                        @Param("userId") Long userId, 
                        @Param("action") Integer action);

    /**
     * 查询评论的点赞数
     *
     * @param commentId 评论ID
     * @return 点赞数
     */
    Long selectCommentLikeCount(@Param("commentId") Long commentId);
}
