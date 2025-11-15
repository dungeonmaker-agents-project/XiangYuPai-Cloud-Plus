package org.dromara.common.api.location;

import org.dromara.common.api.location.domain.CityInfoVo;
import org.dromara.common.api.location.domain.DistanceVo;
import org.dromara.common.api.location.domain.LocationPointDto;
import org.dromara.common.core.domain.R;

import java.math.BigDecimal;
import java.util.List;

/**
 * 位置服务远程调用接口
 * Remote Location Service Interface
 *
 * <p>用途：其他微服务通过Dubbo调用此接口使用位置服务功能</p>
 * <p>实现：xypai-common模块实现此接口</p>
 *
 * @author XiangYuPai Team
 */
public interface RemoteLocationService {

    // ==================== 距离计算 ====================

    /**
     * 计算两点之间的距离
     *
     * @param fromLat 起点纬度
     * @param fromLng 起点经度
     * @param toLat   终点纬度
     * @param toLng   终点经度
     * @return 距离信息（包含格式化文本）
     */
    R<DistanceVo> calculateDistance(BigDecimal fromLat, BigDecimal fromLng,
                                    BigDecimal toLat, BigDecimal toLng);

    /**
     * 批量计算距离（从一个起点到多个目标点）
     *
     * @param fromLat 起点纬度
     * @param fromLng 起点经度
     * @param targets 目标点列表
     * @return 距离列表
     */
    R<List<DistanceVo>> calculateBatchDistance(BigDecimal fromLat, BigDecimal fromLng,
                                                List<LocationPointDto> targets);

    // ==================== 坐标验证 ====================

    /**
     * 验证坐标有效性
     *
     * @param latitude  纬度 (-90 ~ 90)
     * @param longitude 经度 (-180 ~ 180)
     * @return true=有效，false=无效
     */
    R<Boolean> validateCoordinates(BigDecimal latitude, BigDecimal longitude);

    // ==================== 城市查询 ====================

    /**
     * 根据城市代码获取城市信息
     *
     * @param cityCode 城市代码
     * @return 城市信息
     */
    R<CityInfoVo> getCityInfo(String cityCode);

    /**
     * 根据城市名称查询城市代码
     *
     * @param cityName 城市名称
     * @return 城市代码
     */
    R<String> getCityCodeByName(String cityName);
}
