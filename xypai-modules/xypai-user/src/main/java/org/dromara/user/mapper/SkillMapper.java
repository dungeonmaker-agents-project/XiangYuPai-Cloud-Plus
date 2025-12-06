package org.dromara.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.user.domain.entity.Skill;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 技能Mapper
 * Skill Mapper
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Mapper
public interface SkillMapper extends BaseMapper<Skill> {

    /**
     * 查找附近的技能（空间索引查询）
     *
     * @param latitude     纬度
     * @param longitude    经度
     * @param radiusMeters 半径（米）
     * @param limit        返回数量
     * @return 附近技能列表
     */
    @Select("""
        SELECT *,
          ST_Distance_Sphere(
            POINT(longitude, latitude),
            POINT(#{longitude}, #{latitude})
          ) / 1000 AS distance_km
        FROM skills
        WHERE skill_type = 'offline'
          AND is_online = 1
          AND ST_Distance_Sphere(
            POINT(longitude, latitude),
            POINT(#{longitude}, #{latitude})
          ) <= #{radiusMeters}
          AND deleted = 0
        ORDER BY distance_km
        LIMIT #{limit}
        """)
    List<Skill> findNearbySkills(
        @Param("latitude") BigDecimal latitude,
        @Param("longitude") BigDecimal longitude,
        @Param("radiusMeters") int radiusMeters,
        @Param("limit") int limit
    );

    /**
     * 查询用户的所有技能
     *
     * @param userId 用户ID
     * @return 技能列表
     */
    @Select("SELECT * FROM skills WHERE user_id = #{userId} AND deleted = 0 ORDER BY created_at DESC")
    List<Skill> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的上架技能
     *
     * @param userId 用户ID
     * @return 技能列表
     */
    @Select("SELECT * FROM skills WHERE user_id = #{userId} AND is_online = 1 AND deleted = 0 ORDER BY created_at DESC")
    List<Skill> selectOnlineSkillsByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的技能列表 (分页)
     *
     * @param userId 用户ID
     * @param cursor 游标
     * @return 技能列表
     */
    default List<Skill> selectMySkills(Long userId, Object cursor) {
        return selectByUserId(userId);
    }

    /**
     * 查询附近的技能 (别名方法)
     *
     * @param latitude     纬度
     * @param longitude    经度
     * @param radiusMeters 半径（米）
     * @param cursor       游标
     * @return 附近技能列表
     */
    default List<Skill> selectNearbySkills(BigDecimal latitude, BigDecimal longitude, Integer radiusMeters, Object cursor) {
        return findNearbySkills(latitude, longitude, radiusMeters, 50);
    }

    /**
     * 统计用户的技能数量
     *
     * @param userId 用户ID
     * @return 技能数量
     */
    @Select("SELECT COUNT(*) FROM skills WHERE user_id = #{userId} AND deleted = 0")
    long countByUserId(@Param("userId") Long userId);

    /**
     * 统计用户上架技能数量
     *
     * @param userId 用户ID
     * @return 上架技能数量
     */
    @Select("SELECT COUNT(*) FROM skills WHERE user_id = #{userId} AND is_online = 1 AND deleted = 0")
    long countOnlineSkillsByUserId(@Param("userId") Long userId);

    /**
     * 分页查询用户技能（用于对方主页）
     *
     * @param userId   用户ID
     * @param offset   偏移量
     * @param pageSize 每页数量
     * @return 技能列表
     */
    @Select("""
        SELECT * FROM skills
        WHERE user_id = #{userId}
        AND is_online = 1
        AND deleted = 0
        ORDER BY created_at DESC
        LIMIT #{offset}, #{pageSize}
        """)
    List<Skill> selectSkillsByUserId(
        @Param("userId") Long userId,
        @Param("offset") int offset,
        @Param("pageSize") int pageSize
    );

    /**
     * 统计用户技能数量（用于对方主页）
     *
     * @param userId 用户ID
     * @return 技能数量
     */
    default long countSkillsByUserId(Long userId) {
        return countOnlineSkillsByUserId(userId);
    }

    // ==================== 技能服务列表查询 ====================

    /**
     * 查询技能服务列表（带筛选和排序，联合用户表）
     * 调用场景：王者荣耀陪玩列表查询
     * 核心逻辑：JOIN users表获取用户信息，支持多种筛选条件和排序方式
     *
     * @param gameName  游戏名称（如"王者荣耀"）
     * @param tabType   Tab类型（glory_king/online/offline）
     * @param sortBy    排序方式（smart/newest/recent/popular/price_asc/price_desc）
     * @param gender    性别筛选（male/female）
     * @param isOnline  在线状态（1-在线）
     * @param server    游戏大区（如"微信区"）
     * @param ranks     段位列表
     * @param priceMin  最低价格
     * @param priceMax  最高价格
     * @param offset    分页偏移
     * @param pageSize  每页数量
     * @return 技能+用户信息Map列表
     */
    @Select("""
        <script>
        SELECT
            s.skill_id, s.user_id, s.skill_name, s.skill_type, s.cover_image, s.description,
            s.price, s.price_unit, s.is_online AS skill_is_online, s.rating, s.review_count, s.order_count,
            s.game_name, s.game_rank, s.peak_score, s.server, s.service_hours, s.created_at,
            u.nickname, u.avatar, u.gender, u.birthday, u.is_online AS user_is_online,
            u.is_real_verified, u.is_god_verified, u.is_vip, u.last_login_at
        FROM skills s
        INNER JOIN users u ON s.user_id = u.user_id
        WHERE s.deleted = 0 AND u.deleted = 0 AND s.is_online = 1
            AND s.game_name = #{gameName}
            <if test="gender != null and gender != '' and gender != 'all'">
                AND u.gender = #{gender}
            </if>
            <if test="isOnline != null">
                AND u.is_online = #{isOnline}
            </if>
            <if test="server != null and server != ''">
                AND s.server = #{server}
            </if>
            <if test="ranks != null and ranks.size() > 0">
                AND s.game_rank IN
                <foreach collection="ranks" item="rank" open="(" separator="," close=")">
                    #{rank}
                </foreach>
            </if>
            <if test="priceMin != null">
                AND s.price &gt;= #{priceMin}
            </if>
            <if test="priceMax != null">
                AND s.price &lt;= #{priceMax}
            </if>
        <choose>
            <when test="sortBy == 'newest'">
                ORDER BY s.created_at DESC
            </when>
            <when test="sortBy == 'recent'">
                ORDER BY u.last_login_at DESC
            </when>
            <when test="sortBy == 'popular'">
                ORDER BY s.order_count DESC, s.rating DESC
            </when>
            <when test="sortBy == 'price_asc'">
                ORDER BY s.price ASC
            </when>
            <when test="sortBy == 'price_desc'">
                ORDER BY s.price DESC
            </when>
            <otherwise>
                ORDER BY u.is_online DESC, s.order_count DESC, s.rating DESC
            </otherwise>
        </choose>
        LIMIT #{offset}, #{pageSize}
        </script>
        """)
    List<Map<String, Object>> querySkillServiceListWithUser(
        @Param("gameName") String gameName,
        @Param("tabType") String tabType,
        @Param("sortBy") String sortBy,
        @Param("gender") String gender,
        @Param("isOnline") Integer isOnline,
        @Param("server") String server,
        @Param("ranks") List<String> ranks,
        @Param("priceMin") BigDecimal priceMin,
        @Param("priceMax") BigDecimal priceMax,
        @Param("offset") int offset,
        @Param("pageSize") int pageSize
    );

    /**
     * 统计技能服务列表数量
     * 调用场景：分页查询总数统计
     * 核心逻辑：与querySkillServiceListWithUser保持一致的筛选条件
     */
    @Select("""
        <script>
        SELECT COUNT(*)
        FROM skills s
        INNER JOIN users u ON s.user_id = u.user_id
        WHERE s.deleted = 0 AND u.deleted = 0 AND s.is_online = 1
            AND s.game_name = #{gameName}
            <if test="gender != null and gender != '' and gender != 'all'">
                AND u.gender = #{gender}
            </if>
            <if test="isOnline != null">
                AND u.is_online = #{isOnline}
            </if>
            <if test="server != null and server != ''">
                AND s.server = #{server}
            </if>
            <if test="ranks != null and ranks.size() > 0">
                AND s.game_rank IN
                <foreach collection="ranks" item="rank" open="(" separator="," close=")">
                    #{rank}
                </foreach>
            </if>
            <if test="priceMin != null">
                AND s.price &gt;= #{priceMin}
            </if>
            <if test="priceMax != null">
                AND s.price &lt;= #{priceMax}
            </if>
        </script>
        """)
    Long countSkillServiceList(
        @Param("gameName") String gameName,
        @Param("gender") String gender,
        @Param("isOnline") Integer isOnline,
        @Param("server") String server,
        @Param("ranks") List<String> ranks,
        @Param("priceMin") BigDecimal priceMin,
        @Param("priceMax") BigDecimal priceMax
    );

    /**
     * 统计各Tab数量（按游戏名称）
     * 调用场景：构建Tab统计信息
     */
    @Select("""
        SELECT COUNT(*) FROM skills s
        INNER JOIN users u ON s.user_id = u.user_id
        WHERE s.deleted = 0 AND u.deleted = 0 AND s.is_online = 1
            AND s.game_name = #{gameName}
        """)
    Long countByGameName(@Param("gameName") String gameName);

    /**
     * 统计在线用户的技能数量
     */
    @Select("""
        SELECT COUNT(*) FROM skills s
        INNER JOIN users u ON s.user_id = u.user_id
        WHERE s.deleted = 0 AND u.deleted = 0 AND s.is_online = 1
            AND s.game_name = #{gameName} AND u.is_online = 1
        """)
    Long countOnlineByGameName(@Param("gameName") String gameName);

    /**
     * 查询技能详情（带用户信息）
     * Invocation: getSkillServiceDetail in RemoteAppUserServiceImpl
     * Logic: JOIN users table to get provider info along with skill detail
     *
     * @param skillId 技能ID
     * @return 技能+用户信息Map
     */
    @Select("""
        SELECT
            s.skill_id, s.user_id, s.skill_name, s.skill_type, s.cover_image, s.description,
            s.price, s.price_unit, s.is_online AS skill_is_online, s.rating, s.review_count, s.order_count,
            s.game_name, s.game_rank, s.peak_score, s.server, s.service_hours, s.created_at,
            s.service_type, s.service_location, s.latitude, s.longitude,
            u.nickname, u.avatar, u.gender, u.birthday, u.is_online AS user_is_online,
            u.is_real_verified, u.is_god_verified, u.is_vip, u.level, u.residence
        FROM skills s
        INNER JOIN users u ON s.user_id = u.user_id
        WHERE s.skill_id = #{skillId} AND s.deleted = 0 AND u.deleted = 0
        """)
    Map<String, Object> selectSkillDetailWithUser(@Param("skillId") Long skillId);
}
