package org.dromara.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.user.domain.entity.Skill;

import java.math.BigDecimal;
import java.util.List;

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
}
