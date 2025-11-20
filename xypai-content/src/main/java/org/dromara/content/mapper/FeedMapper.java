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

}
