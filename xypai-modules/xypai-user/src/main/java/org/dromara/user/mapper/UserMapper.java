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
import java.util.Map;

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

    // ==================== 以下方法使用 XML Mapper 实现 (UserMapper.xml) ====================

    /**
     * 查询限时专享用户列表（带技能和价格信息）
     * <p>
     * JOIN users + skills + user_stats
     * 只返回有上架技能的用户
     *
     * @param gender       性别筛选
     * @param cityCode     城市代码
     * @param districtCode 区域代码
     * @param latitude     纬度（用于计算距离）
     * @param longitude    经度（用于计算距离）
     * @param offset       分页偏移
     * @param pageSize     每页数量
     * @return 限时专享用户列表
     */
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
     *
     * @param gender       性别筛选
     * @param cityCode     城市代码
     * @param districtCode 区域代码
     * @return 用户总数
     */
    Integer countLimitedTimeUsers(
        @Param("gender") String gender,
        @Param("cityCode") String cityCode,
        @Param("districtCode") String districtCode
    );

    /**
     * 根据筛选条件查询用户列表
     * <p>
     * JOIN users + skills + user_stats
     *
     * @param type      技能类型: online/offline
     * @param gender    性别筛选
     * @param ageMin    最小年龄
     * @param ageMax    最大年龄
     * @param status    状态筛选: online/active_3d/active_7d
     * @param skills    技能/段位筛选列表
     * @param priceMin  最低价格
     * @param priceMax  最高价格
     * @param latitude  纬度（用于计算距离）
     * @param longitude 经度（用于计算距离）
     * @param offset    分页偏移
     * @param pageSize  每页数量
     * @return 筛选用户列表
     */
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
     *
     * @param type     技能类型: online/offline
     * @param gender   性别筛选
     * @param ageMin   最小年龄
     * @param ageMax   最大年龄
     * @param status   状态筛选: online/active_3d/active_7d
     * @param skills   技能/段位筛选列表
     * @param priceMin 最低价格
     * @param priceMax 最高价格
     * @return 用户总数
     */
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
     *
     * @param type 技能类型: online/offline
     * @return 包含 minPrice 和 maxPrice 的 Map
     */
    Map<String, Object> selectPriceRange(@Param("type") String type);

    /**
     * 查询技能选项 (按游戏名称/服务类型分组，统计用户数)
     *
     * @param type 技能类型: online/offline
     * @return 技能选项列表
     */
    List<Map<String, Object>> selectSkillOptions(@Param("type") String type);
}
