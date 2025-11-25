package org.dromara.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dromara.appuser.api.domain.vo.FilterUserVo;
import org.dromara.appuser.api.domain.vo.LimitedTimeUserVo;
import org.dromara.user.domain.entity.User;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户Mapper
 * User Mapper
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 通过手机号查询用户
     *
     * @param mobile      手机号
     * @param countryCode 国家区号
     * @return 用户信息
     */
    @Select("SELECT * FROM users WHERE mobile = #{mobile} AND country_code = #{countryCode} AND deleted = 0 LIMIT 1")
    User selectByMobile(@Param("mobile") String mobile, @Param("countryCode") String countryCode);

    /**
     * 查找附近的用户（空间索引查询）
     *
     * @param latitude     纬度
     * @param longitude    经度
     * @param radiusMeters 半径（米）
     * @param limit        返回数量
     * @return 附近用户列表
     */
    @Select("""
        SELECT *,
          ST_Distance_Sphere(
            location,
            ST_GeomFromText(CONCAT('POINT(', #{longitude}, ' ', #{latitude}, ')'), 4326)
          ) / 1000 AS distance_km
        FROM users
        WHERE ST_Distance_Sphere(
            location,
            ST_GeomFromText(CONCAT('POINT(', #{longitude}, ' ', #{latitude}, ')'), 4326)
          ) <= #{radiusMeters}
          AND deleted = 0
        ORDER BY distance_km
        LIMIT #{limit}
        """)
    List<User> findNearbyUsers(
        @Param("latitude") BigDecimal latitude,
        @Param("longitude") BigDecimal longitude,
        @Param("radiusMeters") int radiusMeters,
        @Param("limit") int limit
    );

    /**
     * 批量查询用户信息
     *
     * @param userIds 用户ID列表
     * @return 用户列表
     */
    @Select("<script>"
        + "SELECT * FROM users WHERE user_id IN "
        + "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>"
        + "#{id}"
        + "</foreach>"
        + " AND deleted = 0"
        + "</script>")
    List<User> selectBatchByIds(@Param("userIds") List<Long> userIds);

    /**
     * 更新用户在线状态
     *
     * @param userId   用户ID
     * @param isOnline 是否在线
     * @return 更新行数
     */
    @Update("UPDATE users SET is_online = #{isOnline}, updated_at = NOW() WHERE user_id = #{userId} AND deleted = 0")
    int updateOnlineStatus(@Param("userId") Long userId, @Param("isOnline") Boolean isOnline);

    /**
     * 更新最后登录信息
     *
     * @param userId  用户ID
     * @param loginIp 登录IP
     * @return 更新行数
     */
    @Update("UPDATE users SET last_login_at = NOW(), last_login_ip = #{loginIp}, updated_at = NOW() WHERE user_id = #{userId} AND deleted = 0")
    int updateLastLoginInfo(@Param("userId") Long userId, @Param("loginIp") String loginIp);

    /**
     * 查询限时专享用户列表（带技能和价格信息）
     *
     * JOIN users + skills + user_stats
     * 只返回有上架技能的用户
     */
    @Select("<script>"
        + "SELECT "
        + "  u.user_id AS userId, "
        + "  u.nickname, "
        + "  u.avatar, "
        + "  u.gender, "
        + "  YEAR(CURDATE()) - YEAR(u.birthday) AS age, "
        + "  u.is_online AS isOnline, "
        + "  u.bio, "
        + "  <if test='latitude != null and longitude != null'>"
        + "  CAST(ST_Distance_Sphere("
        + "    u.location, "
        + "    ST_GeomFromText(CONCAT('POINT(', #{longitude}, ' ', #{latitude}, ')'), 4326)"
        + "  ) AS SIGNED) AS distance, "
        + "  </if>"
        + "  <if test='latitude == null or longitude == null'>"
        + "  0 AS distance, "
        + "  </if>"
        + "  s.skill_id AS skillId, "
        + "  s.skill_name AS skillName, "
        + "  s.price, "
        + "  s.price_unit AS priceUnit, "
        + "  s.game_rank AS skillLevel, "
        + "  s.rating, "
        + "  s.order_count AS orderCount, "
        + "  us.fans_count AS fansCount, "
        + "  us.likes_count AS likesCount "
        + "FROM users u "
        + "INNER JOIN skills s ON u.user_id = s.user_id AND s.is_online = 1 AND s.deleted = 0 "
        + "LEFT JOIN user_stats us ON u.user_id = us.user_id AND us.deleted = 0 "
        + "WHERE u.deleted = 0 "
        + "<if test='gender != null and gender != \"all\"'>"
        + "  AND u.gender = #{gender} "
        + "</if>"
        + "<if test='cityCode != null'>"
        + "  AND u.residence LIKE CONCAT('%', #{cityCode}, '%') "
        + "</if>"
        + "<if test='districtCode != null'>"
        + "  AND u.residence LIKE CONCAT('%', #{districtCode}, '%') "
        + "</if>"
        + "GROUP BY u.user_id "
        + "ORDER BY u.is_online DESC, u.user_id DESC "
        + "LIMIT #{offset}, #{pageSize}"
        + "</script>")
    List<LimitedTimeUserVo> queryLimitedTimeUsers(
        @Param("gender") String gender,
        @Param("cityCode") String cityCode,
        @Param("districtCode") String districtCode,
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude,
        @Param("offset") Integer offset,
        @Param("pageSize") Integer pageSize
    );

    /**
     * 统计限时专享用户总数
     */
    @Select("<script>"
        + "SELECT COUNT(DISTINCT u.user_id) "
        + "FROM users u "
        + "INNER JOIN skills s ON u.user_id = s.user_id AND s.is_online = 1 AND s.deleted = 0 "
        + "WHERE u.deleted = 0 "
        + "<if test='gender != null and gender != \"all\"'>"
        + "  AND u.gender = #{gender} "
        + "</if>"
        + "<if test='cityCode != null'>"
        + "  AND u.residence LIKE CONCAT('%', #{cityCode}, '%') "
        + "</if>"
        + "<if test='districtCode != null'>"
        + "  AND u.residence LIKE CONCAT('%', #{districtCode}, '%') "
        + "</if>"
        + "</script>")
    Integer countLimitedTimeUsers(
        @Param("gender") String gender,
        @Param("cityCode") String cityCode,
        @Param("districtCode") String districtCode
    );

    // ==================== 筛选功能相关 ====================

    /**
     * 查询不同游戏名称列表 (线上技能)
     */
    @Select("SELECT DISTINCT game_name FROM skills WHERE skill_type = 'online' AND is_online = 1 AND deleted = 0 AND game_name IS NOT NULL ORDER BY game_name")
    List<String> selectDistinctGameNames();

    /**
     * 查询不同段位列表 (线上技能)
     */
    @Select("SELECT DISTINCT game_rank FROM skills WHERE skill_type = 'online' AND is_online = 1 AND deleted = 0 AND game_rank IS NOT NULL ORDER BY game_rank")
    List<String> selectDistinctGameRanks();

    /**
     * 查询不同服务类型列表 (线下技能)
     */
    @Select("SELECT DISTINCT service_type FROM skills WHERE skill_type = 'offline' AND is_online = 1 AND deleted = 0 AND service_type IS NOT NULL ORDER BY service_type")
    List<String> selectDistinctServiceTypes();

    /**
     * 查询价格范围
     */
    @Select("<script>"
        + "SELECT MIN(price) AS minPrice, MAX(price) AS maxPrice "
        + "FROM skills "
        + "WHERE is_online = 1 AND deleted = 0 "
        + "<if test='type == \"online\"'>"
        + "  AND skill_type = 'online' "
        + "</if>"
        + "<if test='type == \"offline\"'>"
        + "  AND skill_type = 'offline' "
        + "</if>"
        + "</script>")
    java.util.Map<String, Object> selectPriceRange(@Param("type") String type);

    /**
     * 查询技能选项 (按游戏名称/服务类型分组，统计用户数)
     */
    @Select("<script>"
        + "SELECT "
        + "  <if test='type == \"online\"'>"
        + "    s.game_rank AS value, "
        + "    s.game_rank AS label, "
        + "    s.game_name AS category, "
        + "  </if>"
        + "  <if test='type == \"offline\"'>"
        + "    s.service_type AS value, "
        + "    s.service_type AS label, "
        + "    s.service_type AS category, "
        + "  </if>"
        + "  COUNT(DISTINCT s.user_id) AS count "
        + "FROM skills s "
        + "WHERE s.is_online = 1 AND s.deleted = 0 "
        + "<if test='type == \"online\"'>"
        + "  AND s.skill_type = 'online' AND s.game_rank IS NOT NULL "
        + "</if>"
        + "<if test='type == \"offline\"'>"
        + "  AND s.skill_type = 'offline' AND s.service_type IS NOT NULL "
        + "</if>"
        + "GROUP BY "
        + "  <if test='type == \"online\"'>s.game_name, s.game_rank</if>"
        + "  <if test='type == \"offline\"'>s.service_type</if>"
        + " ORDER BY count DESC"
        + "</script>")
    List<java.util.Map<String, Object>> selectSkillOptions(@Param("type") String type);

    /**
     * 根据筛选条件查询用户列表
     * JOIN users + skills + user_stats
     */
    @Select("<script>"
        + "SELECT "
        + "  u.user_id AS userId, "
        + "  u.nickname, "
        + "  u.avatar, "
        + "  u.gender, "
        + "  TIMESTAMPDIFF(YEAR, u.birthday, CURDATE()) AS age, "
        + "  u.is_online AS isOnline, "
        + "  u.bio, "
        + "  u.residence, "
        + "  u.last_login_at AS lastActiveAt, "
        + "  <if test='latitude != null and longitude != null'>"
        + "  CAST(ST_Distance_Sphere("
        + "    u.location, "
        + "    ST_GeomFromText(CONCAT('POINT(', #{longitude}, ' ', #{latitude}, ')'), 4326)"
        + "  ) AS SIGNED) AS distance, "
        + "  </if>"
        + "  <if test='latitude == null or longitude == null'>"
        + "  0 AS distance, "
        + "  </if>"
        + "  s.skill_id AS skillId, "
        + "  s.skill_name AS skillName, "
        + "  s.skill_type AS skillType, "
        + "  s.game_name AS gameName, "
        + "  s.price, "
        + "  s.price_unit AS priceUnit, "
        + "  s.game_rank AS skillLevel, "
        + "  s.rating, "
        + "  s.order_count AS orderCount, "
        + "  us.fans_count AS fansCount, "
        + "  us.likes_count AS likesCount, "
        + "  us.posts_count AS postsCount "
        + "FROM users u "
        + "INNER JOIN skills s ON u.user_id = s.user_id AND s.is_online = 1 AND s.deleted = 0 "
        + "LEFT JOIN user_stats us ON u.user_id = us.user_id AND us.deleted = 0 "
        + "WHERE u.deleted = 0 "
        // 技能类型筛选
        + "<if test='type == \"online\"'>"
        + "  AND s.skill_type = 'online' "
        + "</if>"
        + "<if test='type == \"offline\"'>"
        + "  AND s.skill_type = 'offline' "
        + "</if>"
        // 性别筛选
        + "<if test='gender != null and gender != \"all\"'>"
        + "  AND u.gender = #{gender} "
        + "</if>"
        // 年龄筛选
        + "<if test='ageMin != null'>"
        + "  AND TIMESTAMPDIFF(YEAR, u.birthday, CURDATE()) &gt;= #{ageMin} "
        + "</if>"
        + "<if test='ageMax != null'>"
        + "  AND TIMESTAMPDIFF(YEAR, u.birthday, CURDATE()) &lt;= #{ageMax} "
        + "</if>"
        // 状态筛选
        + "<if test='status == \"online\"'>"
        + "  AND u.is_online = 1 "
        + "</if>"
        + "<if test='status == \"active_3d\"'>"
        + "  AND u.last_login_at &gt;= DATE_SUB(NOW(), INTERVAL 3 DAY) "
        + "</if>"
        + "<if test='status == \"active_7d\"'>"
        + "  AND u.last_login_at &gt;= DATE_SUB(NOW(), INTERVAL 7 DAY) "
        + "</if>"
        // 技能/段位筛选 (线上)
        + "<if test='skills != null and skills.size() > 0'>"
        + "  AND (s.game_rank IN "
        + "    <foreach collection='skills' item='skill' open='(' separator=',' close=')'>"
        + "      #{skill}"
        + "    </foreach>"
        + "    OR s.service_type IN "
        + "    <foreach collection='skills' item='skill' open='(' separator=',' close=')'>"
        + "      #{skill}"
        + "    </foreach>"
        + "  ) "
        + "</if>"
        // 价格范围筛选
        + "<if test='priceMin != null'>"
        + "  AND s.price &gt;= #{priceMin} "
        + "</if>"
        + "<if test='priceMax != null'>"
        + "  AND s.price &lt;= #{priceMax} "
        + "</if>"
        + "GROUP BY u.user_id "
        + "ORDER BY u.is_online DESC, s.order_count DESC, u.user_id DESC "
        + "LIMIT #{offset}, #{pageSize}"
        + "</script>")
    List<FilterUserVo> queryFilteredUsers(
        @Param("type") String type,
        @Param("gender") String gender,
        @Param("ageMin") Integer ageMin,
        @Param("ageMax") Integer ageMax,
        @Param("status") String status,
        @Param("skills") List<String> skills,
        @Param("priceMin") Integer priceMin,
        @Param("priceMax") Integer priceMax,
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude,
        @Param("offset") Integer offset,
        @Param("pageSize") Integer pageSize
    );

    /**
     * 统计筛选用户总数
     */
    @Select("<script>"
        + "SELECT COUNT(DISTINCT u.user_id) "
        + "FROM users u "
        + "INNER JOIN skills s ON u.user_id = s.user_id AND s.is_online = 1 AND s.deleted = 0 "
        + "WHERE u.deleted = 0 "
        // 技能类型筛选
        + "<if test='type == \"online\"'>"
        + "  AND s.skill_type = 'online' "
        + "</if>"
        + "<if test='type == \"offline\"'>"
        + "  AND s.skill_type = 'offline' "
        + "</if>"
        // 性别筛选
        + "<if test='gender != null and gender != \"all\"'>"
        + "  AND u.gender = #{gender} "
        + "</if>"
        // 年龄筛选
        + "<if test='ageMin != null'>"
        + "  AND TIMESTAMPDIFF(YEAR, u.birthday, CURDATE()) &gt;= #{ageMin} "
        + "</if>"
        + "<if test='ageMax != null'>"
        + "  AND TIMESTAMPDIFF(YEAR, u.birthday, CURDATE()) &lt;= #{ageMax} "
        + "</if>"
        // 状态筛选
        + "<if test='status == \"online\"'>"
        + "  AND u.is_online = 1 "
        + "</if>"
        + "<if test='status == \"active_3d\"'>"
        + "  AND u.last_login_at &gt;= DATE_SUB(NOW(), INTERVAL 3 DAY) "
        + "</if>"
        + "<if test='status == \"active_7d\"'>"
        + "  AND u.last_login_at &gt;= DATE_SUB(NOW(), INTERVAL 7 DAY) "
        + "</if>"
        // 技能/段位筛选
        + "<if test='skills != null and skills.size() > 0'>"
        + "  AND (s.game_rank IN "
        + "    <foreach collection='skills' item='skill' open='(' separator=',' close=')'>"
        + "      #{skill}"
        + "    </foreach>"
        + "    OR s.service_type IN "
        + "    <foreach collection='skills' item='skill' open='(' separator=',' close=')'>"
        + "      #{skill}"
        + "    </foreach>"
        + "  ) "
        + "</if>"
        // 价格范围筛选
        + "<if test='priceMin != null'>"
        + "  AND s.price &gt;= #{priceMin} "
        + "</if>"
        + "<if test='priceMax != null'>"
        + "  AND s.price &lt;= #{priceMax} "
        + "</if>"
        + "</script>")
    Integer countFilteredUsers(
        @Param("type") String type,
        @Param("gender") String gender,
        @Param("ageMin") Integer ageMin,
        @Param("ageMax") Integer ageMax,
        @Param("status") String status,
        @Param("skills") List<String> skills,
        @Param("priceMin") Integer priceMin,
        @Param("priceMax") Integer priceMax
    );
}
