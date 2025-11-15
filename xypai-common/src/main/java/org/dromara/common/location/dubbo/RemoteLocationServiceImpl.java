package org.dromara.common.location.dubbo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.api.location.RemoteLocationService;
import org.dromara.common.api.location.domain.CityInfoVo;
import org.dromara.common.api.location.domain.DistanceVo;
import org.dromara.common.api.location.domain.LocationPointDto;
import org.dromara.common.core.domain.R;
import org.dromara.common.location.service.ICityService;
import org.dromara.common.location.service.ILocationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 位置服务远程调用实现
 * Remote Location Service Implementation (Dubbo Provider)
 *
 * <p>用途: 为其他微服务提供位置相关的RPC接口</p>
 * <p>调用方: xypai-user, xypai-content, xypai-activity等</p>
 *
 * @author XiangYuPai Team
 */
@Slf4j
@Service
@DubboService
@RequiredArgsConstructor
public class RemoteLocationServiceImpl implements RemoteLocationService {

    private final ILocationService locationService;
    private final ICityService cityService;

    @Override
    public R<DistanceVo> calculateDistance(BigDecimal fromLat, BigDecimal fromLng,
                                          BigDecimal toLat, BigDecimal toLng) {
        log.info("RPC调用 - 计算距离: ({},{}) -> ({},{})",
                fromLat, fromLng, toLat, toLng);

        try {
            // 参数验证
            if (!locationService.validateCoordinates(fromLat, fromLng)) {
                return R.fail("起点坐标无效");
            }
            if (!locationService.validateCoordinates(toLat, toLng)) {
                return R.fail("终点坐标无效");
            }

            // 调用业务Service计算距离
            BigDecimal distance = locationService.calculateDistance(
                fromLat, fromLng, toLat, toLng
            );

            // 构建返回VO
            DistanceVo vo = new DistanceVo();
            vo.setDistance(distance);
            vo.setUnit("km");
            vo.setDisplayText(locationService.formatDistance(distance));

            return R.ok(vo);
        } catch (Exception e) {
            log.error("计算距离失败", e);
            return R.fail("计算距离失败: " + e.getMessage());
        }
    }

    @Override
    public R<List<DistanceVo>> calculateBatchDistance(BigDecimal fromLat, BigDecimal fromLng,
                                                      List<LocationPointDto> targets) {
        log.info("RPC调用 - 批量计算距离: 起点({},{}), 目标数量: {}",
                fromLat, fromLng, targets == null ? 0 : targets.size());

        try {
            // 参数验证
            if (targets == null || targets.isEmpty()) {
                return R.ok(List.of());
            }

            if (!locationService.validateCoordinates(fromLat, fromLng)) {
                return R.fail("起点坐标无效");
            }

            // 批量计算距离
            List<DistanceVo> distances = targets.stream()
                .map(target -> {
                    try {
                        BigDecimal distance = locationService.calculateDistance(
                            fromLat, fromLng,
                            target.getLatitude(), target.getLongitude()
                        );

                        DistanceVo vo = new DistanceVo();
                        vo.setId(target.getId());
                        vo.setDistance(distance);
                        vo.setUnit("km");
                        vo.setDisplayText(locationService.formatDistance(distance));
                        return vo;
                    } catch (Exception e) {
                        log.warn("计算单个距离失败: targetId={}", target.getId(), e);
                        DistanceVo vo = new DistanceVo();
                        vo.setId(target.getId());
                        vo.setDistance(BigDecimal.ZERO);
                        vo.setDisplayText("未知");
                        return vo;
                    }
                })
                .collect(Collectors.toList());

            return R.ok(distances);
        } catch (Exception e) {
            log.error("批量计算距离失败", e);
            return R.fail("批量计算距离失败: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> validateCoordinates(BigDecimal latitude, BigDecimal longitude) {
        log.debug("RPC调用 - 验证坐标: ({},{})", latitude, longitude);

        try {
            boolean valid = locationService.validateCoordinates(latitude, longitude);
            return R.ok(valid);
        } catch (Exception e) {
            log.error("验证坐标失败", e);
            return R.fail("验证坐标失败: " + e.getMessage());
        }
    }

    @Override
    public R<CityInfoVo> getCityInfo(String cityCode) {
        log.info("RPC调用 - 获取城市信息: {}", cityCode);

        try {
            org.dromara.common.location.domain.vo.CityInfoVo cityInfo =
                cityService.getByCityCode(cityCode);

            if (cityInfo == null) {
                return R.fail("城市不存在: " + cityCode);
            }

            // 转换为API层的CityInfoVo
            CityInfoVo vo = new CityInfoVo();
            vo.setId(cityInfo.getId());
            vo.setCityCode(cityInfo.getCityCode());
            vo.setCityName(cityInfo.getCityName());
            vo.setProvince(cityInfo.getProvince());
            vo.setPinyin(cityInfo.getPinyin());
            vo.setFirstLetter(cityInfo.getFirstLetter());
            vo.setCenterLat(cityInfo.getCenterLat());
            vo.setCenterLng(cityInfo.getCenterLng());
            vo.setIsHot(cityInfo.getIsHot());

            return R.ok(vo);
        } catch (Exception e) {
            log.error("获取城市信息失败: cityCode={}", cityCode, e);
            return R.fail("获取城市信息失败: " + e.getMessage());
        }
    }

    @Override
    public R<String> getCityCodeByName(String cityName) {
        log.info("RPC调用 - 根据城市名查询代码: {}", cityName);

        try {
            // TODO: 实现根据名称查询城市代码的逻辑
            // 需要在ICityService中添加相应方法
            return R.fail("功能未实现");
        } catch (Exception e) {
            log.error("查询城市代码失败: cityName={}", cityName, e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }
}
