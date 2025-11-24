package org.dromara.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dromara.user.domain.entity.UserStats;

import java.util.List;

/**
 * 用户统计Mapper
 * User Stats Mapper
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Mapper
public interface UserStatsMapper extends BaseMapper<UserStats> {

    /**
     * 通过用户ID查询统计信息
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    @Select("SELECT * FROM user_stats WHERE user_id = #{userId} AND deleted = 0 LIMIT 1")
    UserStats selectByUserId(@Param("userId") Long userId);

    /**
     * 批量查询用户统计信息
     *
     * @param userIds 用户ID列表
     * @return 统计信息列表
     */
    @Select("<script>"
        + "SELECT * FROM user_stats WHERE user_id IN "
        + "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>"
        + "#{id}"
        + "</foreach>"
        + " AND deleted = 0"
        + "</script>")
    List<UserStats> selectBatchByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * 增加关注数
     *
     * @param userId 用户ID
     * @return 更新行数
     */
    @Update("UPDATE user_stats SET following_count = following_count + 1, updated_at = NOW() WHERE user_id = #{userId} AND deleted = 0")
    int incrementFollowingCount(@Param("userId") Long userId);

    /**
     * 减少关注数
     *
     * @param userId 用户ID
     * @return 更新行数
     */
    @Update("UPDATE user_stats SET following_count = GREATEST(following_count - 1, 0), updated_at = NOW() WHERE user_id = #{userId} AND deleted = 0")
    int decrementFollowingCount(@Param("userId") Long userId);

    /**
     * 增加粉丝数
     *
     * @param userId 用户ID
     * @return 更新行数
     */
    @Update("UPDATE user_stats SET fans_count = fans_count + 1, updated_at = NOW() WHERE user_id = #{userId} AND deleted = 0")
    int incrementFansCount(@Param("userId") Long userId);

    /**
     * 减少粉丝数
     *
     * @param userId 用户ID
     * @return 更新行数
     */
    @Update("UPDATE user_stats SET fans_count = GREATEST(fans_count - 1, 0), updated_at = NOW() WHERE user_id = #{userId} AND deleted = 0")
    int decrementFansCount(@Param("userId") Long userId);
}
