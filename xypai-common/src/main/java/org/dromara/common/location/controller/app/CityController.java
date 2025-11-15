package org.dromara.common.location.controller.app;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.location.domain.vo.CityListResultVo;
import org.dromara.common.location.service.ICityService;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 城市服务C端控制器
 * City Service App Controller
 *
 * @author XiangYuPai Team
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/city")
public class CityController extends BaseController {

    private final ICityService cityService;

    /**
     * 获取城市列表
     *
     * @return 城市列表 (含热门城市和全部城市分组)
     */
    @GetMapping("/list")
    public R<CityListResultVo> getCityList() {
        // 尝试获取当前登录用户ID (未登录则为null)
        Long userId = null;
        try {
            userId = LoginHelper.getUserId();
        } catch (Exception ignored) {
            // 未登录用户也可以访问城市列表
        }

        return cityService.getCityList(userId);
    }
}
