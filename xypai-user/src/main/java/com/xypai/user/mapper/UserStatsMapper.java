package com.xypai.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.user.domain.entity.UserStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户统计Mapper接口
 *
 * @author Bob
 * @date 2025-01-14
 */
@Mapper
public interface UserStatsMapper extends BaseMapper<UserStats> {

    /**
     * 批量查询用户统计
     */
    @Select("<script>" +
            "SELECT * FROM user_stats WHERE user_id IN " +
            "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<UserStats> selectBatchByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * 增加粉丝数
     */
    @Update("UPDATE user_stats SET follower_count = follower_count + 1, " +
            "updated_at = NOW() WHERE user_id = #{userId}")
    int incrementFollowerCount(@Param("userId") Long userId);

    /**
     * 减少粉丝数
     */
    @Update("UPDATE user_stats SET follower_count = GREATEST(follower_count - 1, 0), " +
            "updated_at = NOW() WHERE user_id = #{userId}")
    int decrementFollowerCount(@Param("userId") Long userId);

    /**
     * 增加关注数
     */
    @Update("UPDATE user_stats SET following_count = following_count + 1, " +
            "updated_at = NOW() WHERE user_id = #{userId}")
    int incrementFollowingCount(@Param("userId") Long userId);

    /**
     * 减少关注数
     */
    @Update("UPDATE user_stats SET following_count = GREATEST(following_count - 1, 0), " +
            "updated_at = NOW() WHERE user_id = #{userId}")
    int decrementFollowingCount(@Param("userId") Long userId);

    /**
     * 增加内容数
     */
    @Update("UPDATE user_stats SET content_count = content_count + 1, " +
            "updated_at = NOW() WHERE user_id = #{userId}")
    int incrementContentCount(@Param("userId") Long userId);

    /**
     * 减少内容数
     */
    @Update("UPDATE user_stats SET content_count = GREATEST(content_count - 1, 0), " +
            "updated_at = NOW() WHERE user_id = #{userId}")
    int decrementContentCount(@Param("userId") Long userId);

    /**
     * 增加点赞数
     */
    @Update("UPDATE user_stats SET total_like_count = total_like_count + #{count}, " +
            "updated_at = NOW() WHERE user_id = #{userId}")
    int incrementLikeCount(@Param("userId") Long userId, @Param("count") Integer count);

    /**
     * 增加收藏数
     */
    @Update("UPDATE user_stats SET total_collect_count = total_collect_count + #{count}, " +
            "updated_at = NOW() WHERE user_id = #{userId}")
    int incrementCollectCount(@Param("userId") Long userId, @Param("count") Integer count);

    /**
     * 查询人气用户（粉丝数倒序）
     */
    @Select("SELECT * FROM user_stats WHERE follower_count > #{minFollowers} " +
            "ORDER BY follower_count DESC LIMIT #{limit}")
    List<UserStats> selectPopularUsers(@Param("minFollowers") Integer minFollowers, 
                                       @Param("limit") Integer limit);

    /**
     * 查询优质组局者
     */
    @Select("SELECT * FROM user_stats WHERE activity_organizer_score > #{minScore} " +
            "AND activity_success_rate > #{minRate} " +
            "ORDER BY activity_organizer_score DESC LIMIT #{limit}")
    List<UserStats> selectQualityOrganizers(@Param("minScore") Double minScore, 
                                            @Param("minRate") Double minRate,
                                            @Param("limit") Integer limit);
}

