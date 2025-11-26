package org.dromara.appbff.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.appbff.domain.dto.ServiceListQueryDTO;
import org.dromara.appbff.domain.dto.ServiceReviewQueryDTO;
import org.dromara.appbff.domain.vo.ServiceDetailVO;
import org.dromara.appbff.domain.vo.ServiceListResultVO;
import org.dromara.appbff.domain.vo.ServiceReviewListVO;
import org.dromara.appbff.service.SkillServiceService;
import org.dromara.common.core.domain.R;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.web.bind.annotation.*;

/**
 * 技能服务控制器
 * 对应前端: 11-服务列表页面, 12-服务详情页面
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Slf4j
@RestController
@RequestMapping("/api/service")
@RequiredArgsConstructor
@Tag(name = "技能服务", description = "技能服务列表和详情相关接口")
public class SkillServiceController {

    private final SkillServiceService skillServiceService;

    /**
     * 获取服务列表
     * 对应前端: 11-服务列表页面
     */
    @GetMapping("/list")
    @Operation(summary = "获取服务列表", description = "支持分页、Tab切换、排序、多维度筛选")
    public R<ServiceListResultVO> getServiceList(@Valid ServiceListQueryDTO queryDTO) {
        Long userId = getCurrentUserId();
        ServiceListResultVO result = skillServiceService.queryServiceList(queryDTO, userId);
        return R.ok(result);
    }

    /**
     * 获取服务详情
     * 对应前端: 12-服务详情页面
     */
    @GetMapping("/detail")
    @Operation(summary = "获取服务详情", description = "获取服务提供者的完整信息、技能信息、评价等")
    public R<ServiceDetailVO> getServiceDetail(
        @Parameter(description = "服务ID") @RequestParam Long serviceId,
        @Parameter(description = "用户ID（可选）") @RequestParam(required = false) Long userId) {
        Long currentUserId = userId != null ? userId : getCurrentUserId();
        ServiceDetailVO result = skillServiceService.getServiceDetail(serviceId, currentUserId);
        if (result == null) {
            return R.fail("服务不存在");
        }
        return R.ok(result);
    }

    /**
     * 获取服务评价列表
     * 对应前端: 12-服务详情页面 -> 查看全部评价
     */
    @GetMapping("/reviews")
    @Operation(summary = "获取服务评价列表", description = "分页获取服务的全部评价，支持按类型筛选")
    public R<ServiceReviewListVO> getServiceReviews(@Valid ServiceReviewQueryDTO queryDTO) {
        ServiceReviewListVO result = skillServiceService.getServiceReviews(queryDTO);
        if (result == null) {
            return R.fail("服务不存在");
        }
        return R.ok(result);
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        try {
            return LoginHelper.getUserId();
        } catch (Exception e) {
            log.debug("用户未登录或获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }
}
