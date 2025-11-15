package org.dromara.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
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
}
