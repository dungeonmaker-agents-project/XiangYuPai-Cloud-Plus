package org.dromara.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.user.domain.entity.UserRelation;

import java.util.List;

/**
 * 用户关系Mapper
 * User Relation Mapper
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Mapper
public interface UserRelationMapper extends BaseMapper<UserRelation> {

    /**
     * 查询关注状态
     *
     * @param followerId  关注者ID
     * @param followingId 被关注者ID
     * @return 关注关系
     */
    @Select("SELECT * FROM user_relations WHERE follower_id = #{followerId} AND following_id = #{followingId} AND deleted = 0 LIMIT 1")
    UserRelation selectRelation(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    /**
     * 查询是否互相关注
     *
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @return 是否互相关注
     */
    @Select("""
        SELECT COUNT(*) = 2 FROM user_relations
        WHERE ((follower_id = #{userId1} AND following_id = #{userId2})
           OR (follower_id = #{userId2} AND following_id = #{userId1}))
        AND deleted = 0
        """)
    boolean isMutualFollow(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * 查询粉丝列表
     *
     * @param userId 用户ID
     * @return 粉丝用户ID列表
     */
    @Select("SELECT follower_id FROM user_relations WHERE following_id = #{userId} AND deleted = 0")
    List<Long> selectFans(@Param("userId") Long userId);

    /**
     * 查询关注列表
     *
     * @param userId 用户ID
     * @return 关注的用户ID列表
     */
    @Select("SELECT following_id FROM user_relations WHERE follower_id = #{userId} AND deleted = 0")
    List<Long> selectFollowing(@Param("userId") Long userId);

    /**
     * 批量查询关注状态
     *
     * @param followerId   关注者ID
     * @param followingIds 被关注者ID列表
     * @return 关注关系列表
     */
    @Select("<script>"
        + "SELECT * FROM user_relations WHERE follower_id = #{followerId} "
        + "AND following_id IN "
        + "<foreach collection='followingIds' item='id' open='(' separator=',' close=')'>"
        + "#{id}"
        + "</foreach>"
        + " AND deleted = 0"
        + "</script>")
    List<UserRelation> selectBatchRelations(@Param("followerId") Long followerId, @Param("followingIds") List<Long> followingIds);

    /**
     * 查询关注关系 (别名方法)
     *
     * @param followerId  关注者ID
     * @param followingId 被关注者ID
     * @return 关注关系
     */
    default UserRelation selectByFollowerAndFollowing(Long followerId, Long followingId) {
        return selectRelation(followerId, followingId);
    }

    /**
     * 查询粉丝列表 (别名方法)
     *
     * @param userId 用户ID
     * @param cursor 游标
     * @param limit  限制数量
     * @return 粉丝ID列表
     */
    default List<Long> selectFansList(Long userId, Object cursor, Object limit) {
        return selectFans(userId);
    }

    /**
     * 查询关注列表 (别名方法)
     *
     * @param userId 用户ID
     * @param cursor 游标
     * @param limit  限制数量
     * @return 关注的用户ID列表
     */
    default List<Long> selectFollowingList(Long userId, Object cursor, Object limit) {
        return selectFollowing(userId);
    }

    /**
     * 统计用户关注数量
     *
     * @param userId 用户ID
     * @return 关注数量
     */
    @Select("SELECT COUNT(*) FROM user_relations WHERE follower_id = #{userId} AND deleted = 0")
    long countFollowingByUserId(@Param("userId") Long userId);
}
