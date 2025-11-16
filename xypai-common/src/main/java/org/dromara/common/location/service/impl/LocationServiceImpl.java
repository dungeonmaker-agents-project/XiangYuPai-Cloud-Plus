package org.dromara.common.location.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.location.domain.bo.NearbyLocationQueryBo;
import org.dromara.common.location.domain.entity.Location;
import org.dromara.common.location.domain.vo.LocationListVo;
import org.dromara.common.location.mapper.LocationMapper;
import org.dromara.common.location.service.ILocationService;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

/**
 * 位置服务实现
 * Location Service Implementation
 *
 * @author XiangYuPai Team
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class LocationServiceImpl implements ILocationService {

    private final LocationMapper baseMapper;

    /**
     * 地球半径 (单位: km)
     */
    private static final double EARTH_RADIUS_KM = 6371.0;

    @Override
    public TableDataInfo<LocationListVo> queryNearbyLocations(NearbyLocationQueryBo query) {
        log.info("查询附近地点 - 位置: ({}, {}), 半径: {}km",
                 query.getLatitude(), query.getLongitude(), query.getRadius());

        // 构建查询条件
        LambdaQueryWrapper<Location> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Location::getStatus, 0)  // 0=正常
               .eq(Location::getDeleted, 0); // 未删除

        // 分类筛选
        if (query.getCategory() != null && !query.getCategory().isEmpty()) {
            wrapper.eq(Location::getCategory, query.getCategory());
        }

        // 城市筛选
        if (query.getCityCode() != null && !query.getCityCode().isEmpty()) {
            wrapper.eq(Location::getCityCode, query.getCityCode());
        }

        // 关键词搜索
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(Location::getName, query.getKeyword())
                              .or()
                              .like(Location::getAddress, query.getKeyword()));
        }

        // 查询所有符合条件的地点
        Page<Location> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Location> result = baseMapper.selectPage(page, wrapper);

        // 计算距离并过滤
        List<LocationListVo> voList = result.getRecords().stream()
            .map(location -> {
                LocationListVo vo = BeanUtil.toBean(location, LocationListVo.class);

                // 计算距离
                BigDecimal distance = calculateDistance(
                    query.getLatitude(), query.getLongitude(),
                    location.getLatitude(), location.getLongitude()
                );

                vo.setDistance(distance);
                vo.setDistanceText(formatDistance(distance));
                return vo;
            })
            .filter(vo -> vo.getDistance().doubleValue() <= query.getRadius())  // 过滤半径外的地点
            .sorted(Comparator.comparing(LocationListVo::getDistance))  // 按距离排序
            .toList();

        log.info("找到 {} 个附近地点", voList.size());

        return TableDataInfo.build(voList);
    }

    @Override
    public BigDecimal calculateDistance(BigDecimal lat1, BigDecimal lon1,
                                       BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return BigDecimal.ZERO;
        }

        // Haversine公式计算两点间距离
        double dLat = Math.toRadians(lat2.subtract(lat1).doubleValue());
        double dLon = Math.toRadians(lon2.subtract(lon1).doubleValue());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1.doubleValue())) *
                   Math.cos(Math.toRadians(lat2.doubleValue())) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_KM * c;

        return BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String formatDistance(BigDecimal distance) {
        if (distance == null) {
            return "未知";
        }

        double dist = distance.doubleValue();

        if (dist > 100) {
            return ">100km";
        }

        if (dist < 1) {
            // 转换为米
            int meters = distance.multiply(BigDecimal.valueOf(1000)).intValue();
            return meters + "m";
        }

        return distance.toPlainString() + "km";
    }

    @Override
    public boolean validateCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            return false;
        }

        double lat = latitude.doubleValue();
        double lon = longitude.doubleValue();

        return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
    }
}
