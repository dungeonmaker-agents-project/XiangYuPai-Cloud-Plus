package org.dromara.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.dromara.content.domain.entity.Feed;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
    List<Feed> selectNearbyFeeds(@Param("latitude") BigDecimal latitude,
                                   @Param("longitude") BigDecimal longitude,
                                   @Param("radiusKm") Integer radiusKm,
                                   @Param("limit") Integer limit);

}
