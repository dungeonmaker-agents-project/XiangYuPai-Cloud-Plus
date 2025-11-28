package org.dromara.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.content.domain.entity.Activity;

import java.math.BigDecimal;
import java.util.List;

/**
 * 活动Mapper
 *
 * @author XiangYuPai
 */
@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {

    /**
     * 查询附近的活动(基于经纬度)
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @param radiusKm  半径(公里)
     * @param limit     限制数量
     * @return 活动列表
     */
    @Select("""
        SELECT *,
            ST_Distance_Sphere(
                POINT(longitude, latitude),
                POINT(#{longitude}, #{latitude})
            ) / 1000 AS distance
        FROM activity
        WHERE deleted = 0
          AND status IN ('recruiting', 'full')
          AND start_time > NOW()
          AND ST_Distance_Sphere(
                POINT(longitude, latitude),
                POINT(#{longitude}, #{latitude})
              ) <= #{radiusKm} * 1000
        ORDER BY distance ASC
        LIMIT #{limit}
        """)
    List<Activity> selectNearbyActivities(@Param("latitude") BigDecimal latitude,
                                          @Param("longitude") BigDecimal longitude,
                                          @Param("radiusKm") Integer radiusKm,
                                          @Param("limit") Integer limit);

    /**
     * 查询活动列表(带筛选条件)
     *
     * @param page       分页参数
     * @param typeCode   活动类型编码(可选)
     * @param genderLimit 性别限制(可选)
     * @param status     活动状态(可选)
     * @param city       城市(可选)
     * @param district   区县(可选)
     * @return 活动分页列表
     */
    @Select("""
        <script>
        SELECT * FROM activity
        WHERE deleted = 0
        <if test="typeCode != null and typeCode != ''">
            AND type_code = #{typeCode}
        </if>
        <if test="genderLimit != null and genderLimit != '' and genderLimit != 'all'">
            AND (gender_limit = 'all' OR gender_limit = #{genderLimit})
        </if>
        <if test="status != null and status != ''">
            AND status = #{status}
        </if>
        <if test="city != null and city != ''">
            AND city = #{city}
        </if>
        <if test="district != null and district != ''">
            AND district = #{district}
        </if>
        ORDER BY create_time DESC
        </script>
        """)
    IPage<Activity> selectActivityPage(Page<Activity> page,
                                        @Param("typeCode") String typeCode,
                                        @Param("genderLimit") String genderLimit,
                                        @Param("status") String status,
                                        @Param("city") String city,
                                        @Param("district") String district);
}
