package org.dromara.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.dromara.content.domain.entity.Feed;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 动态Mapper
 *
 * @author XiangYuPai
 */
@Mapper
public interface FeedMapper extends BaseMapper<Feed> {

    /**
     * 查询附近的动态(基于经纬度)
     *
     * @param latitude 纬度
     * @param longitude 经度
     * @param radiusKm 半径(公里)
     * @param limit 限制数量
     * @return 动态列表
     */
    @Select("""
        SELECT *,
            ST_Distance_Sphere(
                POINT(longitude, latitude),
                POINT(#{longitude}, #{latitude})
            ) / 1000 AS distance
        FROM feed
        WHERE deleted = 0
          AND status = 0
          AND ST_Distance_Sphere(
                POINT(longitude, latitude),
                POINT(#{longitude}, #{latitude})
              ) <= #{radiusKm} * 1000
        ORDER BY distance ASC
        LIMIT #{limit}
        """)
    List<Feed> selectNearbyFeeds(@Param("latitude") BigDecimal latitude,
                                   @Param("longitude") BigDecimal longitude,
                                   @Param("radiusKm") Integer radiusKm,
                                   @Param("limit") Integer limit);

    /**
     * 查询用户最新动态
     *
     * @param userId 用户ID
     * @param limit  返回数量
     * @return 动态列表
     */
    @Select("""
        SELECT id, user_id, type, title, content, cover_image,
               like_count, comment_count, created_at, created_timestamp
        FROM feed
        WHERE user_id = #{userId}
          AND deleted = 0
          AND status = 0
          AND visibility = 0
        ORDER BY created_timestamp DESC
        LIMIT #{limit}
        """)
    List<Feed> selectUserLatestFeeds(@Param("userId") Long userId,
                                     @Param("limit") Integer limit);

    /**
     * 批量查询多个用户的最新动态
     *
     * @param userIds 用户ID列表
     * @param limit   每个用户返回的数量
     * @return 动态列表
     */
    @Select("""
        <script>
        SELECT id, user_id, type, title, content, cover_image,
               like_count, comment_count, created_at, created_timestamp
        FROM (
            SELECT *,
                   ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY created_timestamp DESC) as rn
            FROM feed
            WHERE user_id IN
            <foreach item="id" collection="userIds" open="(" separator="," close=")">
                #{id}
            </foreach>
              AND deleted = 0
              AND status = 0
              AND visibility = 0
        ) ranked
        WHERE rn &lt;= #{limit}
        </script>
        """)
    List<Feed> selectUsersLatestFeeds(@Param("userIds") List<Long> userIds,
                                      @Param("limit") Integer limit);

    /**
     * 获取用户动态总数
     *
     * @param userId 用户ID
     * @return 动态总数
     */
    @Select("""
        SELECT COUNT(*)
        FROM feed
        WHERE user_id = #{userId}
          AND deleted = 0
          AND status = 0
          AND visibility = 0
        """)
    Integer countUserFeeds(@Param("userId") Long userId);

}
