package org.dromara.common.location.controller.app;

import cn.dev33.satoken.annotation.SaCheckRole;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.location.domain.bo.NearbyLocationQueryBo;
import org.dromara.common.location.domain.vo.LocationListVo;
import org.dromara.common.location.service.ILocationService;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 位置服务C端控制器
 * Location Service App Controller
 *
 * @author XiangYuPai Team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/location")
public class LocationController extends BaseController {

    private final ILocationService locationService;

    /**
     * 获取附近地点
     *
     * @param query 查询参数
     * @return 附近地点列表
     */
    @SaCheckRole("user")
    @GetMapping("/nearby")
    public TableDataInfo<LocationListVo> getNearbyLocations(@Validated NearbyLocationQueryBo query) {
        return locationService.queryNearbyLocations(query);
    }

    /**
     * 搜索地点
     *
     * @param query 查询参数
     * @return 地点列表
     */
    @GetMapping("/search")
    public TableDataInfo<LocationListVo> searchLocations(@Validated NearbyLocationQueryBo query) {
        return locationService.queryNearbyLocations(query);
    }
}
