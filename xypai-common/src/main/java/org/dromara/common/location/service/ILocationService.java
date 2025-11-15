package org.dromara.common.location.service;

import org.dromara.common.location.domain.bo.NearbyLocationQueryBo;
import org.dromara.common.location.domain.vo.LocationListVo;
import org.dromara.common.mybatis.core.page.TableDataInfo;

import java.math.BigDecimal;

/**
 * 位置服务接口
 * Location Service Interface
 *
 * @author XiangYuPai Team
 */
public interface ILocationService {

    /**
     * 查询附近地点
     *
     * @param query 查询参数
     * @return 附近地点列表
     */
    TableDataInfo<LocationListVo> queryNearbyLocations(NearbyLocationQueryBo query);

    /**
     * 计算两点之间的距离 (Haversine公式)
     *
     * @param lat1 起点纬度
     * @param lon1 起点经度
     * @param lat2 终点纬度
     * @param lon2 终点经度
     * @return 距离 (单位: km)
     */
    BigDecimal calculateDistance(BigDecimal lat1, BigDecimal lon1,
                                 BigDecimal lat2, BigDecimal lon2);

    /**
     * 格式化距离为可读文本
     *
     * @param distance 距离 (单位: km)
     * @return 格式化文本 (如: "1.2km", "500m", ">100km")
     */
    String formatDistance(BigDecimal distance);

    /**
     * 验证坐标有效性
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 是否有效
     */
    boolean validateCoordinates(BigDecimal latitude, BigDecimal longitude);
}
