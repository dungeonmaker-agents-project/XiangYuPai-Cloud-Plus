package org.dromara.common.location.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.dromara.common.location.domain.bo.NearbyLocationQueryBo;
import org.dromara.common.location.domain.entity.Location;
import org.dromara.common.location.domain.vo.LocationListVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * 地点数据访问层
 * Location Mapper
 *
 * @author XiangYuPai Team
 */
public interface LocationMapper extends BaseMapperPlus<Location, LocationListVo> {

    /**
     * 分页查询附近地点 (使用空间索引优化)
     *
     * @param page  分页对象
     * @param wrapper 查询条件
     * @param query 查询参数
     * @return 分页结果
     */
    Page<Location> selectNearbyPage(@Param("page") Page<Location> page,
                                     @Param("ew") Wrapper<Location> wrapper,
                                     @Param("query") NearbyLocationQueryBo query);
}
