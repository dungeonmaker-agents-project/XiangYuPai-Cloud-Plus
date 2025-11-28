package org.dromara.appbff.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.appbff.domain.dto.*;
import org.dromara.appbff.domain.vo.*;
import org.dromara.appbff.service.ActivityService;
import org.dromara.common.core.domain.R;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 组局活动控制器
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Slf4j
@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
@Tag(name = "组局活动", description = "组局中心相关接口")
public class ActivityController {

    private final ActivityService activityService;

    // ========== 活动列表 ==========

    @GetMapping("/list")
    @Operation(summary = "获取活动列表", description = "支持分页、排序、筛选")
    public R<ActivityListResultVO> getActivityList(@Valid ActivityListQueryDTO queryDTO) {
        Long userId = getCurrentUserId();
        ActivityListResultVO result = activityService.queryActivityList(queryDTO, userId);
        return R.ok(result);
    }

    // ========== 活动详情 ==========

    @GetMapping("/detail/{activityId}")
    @Operation(summary = "获取活动详情")
    public R<ActivityDetailVO> getActivityDetail(
        @Parameter(description = "活动ID") @PathVariable Long activityId) {
        Long userId = getCurrentUserId();
        ActivityDetailVO result = activityService.getActivityDetail(activityId, userId);
        if (result == null) {
            return R.fail("活动不存在");
        }
        return R.ok(result);
    }

    // ========== 活动报名 ==========

    @PostMapping("/register")
    @Operation(summary = "报名参加活动")
    public R<ActivityRegisterResultVO> registerActivity(@Valid @RequestBody ActivityRegisterDTO registerDTO) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }
        ActivityRegisterResultVO result = activityService.registerActivity(registerDTO, userId);
        if (!result.getSuccess()) {
            return R.fail(result.getStatusMessage());
        }
        return R.ok(result);
    }

    @PostMapping("/register/cancel")
    @Operation(summary = "取消活动报名")
    public R<Void> cancelRegistration(@Valid @RequestBody ActivityRegisterCancelDTO cancelDTO) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }
        Boolean success = activityService.cancelActivityRegistration(cancelDTO, userId);
        if (!success) {
            return R.fail("取消报名失败");
        }
        return R.ok();
    }

    // ========== 发布组局 ==========

    @GetMapping("/publish/config")
    @Operation(summary = "获取发布组局配置", description = "获取活动类型、费用配置等")
    public R<ActivityPublishConfigVO> getPublishConfig() {
        Long userId = getCurrentUserId();
        ActivityPublishConfigVO config = activityService.getPublishConfig(userId);
        return R.ok(config);
    }

    @PostMapping("/publish")
    @Operation(summary = "发布组局活动")
    public R<ActivityPublishResultVO> publishActivity(@Valid @RequestBody ActivityPublishDTO publishDTO) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }
        ActivityPublishResultVO result = activityService.publishActivity(publishDTO, userId);
        if (!result.getSuccess()) {
            return R.fail("发布失败");
        }
        return R.ok(result);
    }

    // ========== 支付相关 ==========

    @PostMapping("/pay")
    @Operation(summary = "支付活动费用", description = "支付报名费或发布费")
    public R<ActivityPayResultVO> payActivity(@Valid @RequestBody ActivityPayDTO payDTO) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }
        ActivityPayResultVO result = activityService.payActivity(payDTO, userId);
        if (!result.getSuccess()) {
            return R.fail(result.getStatusMessage());
        }
        return R.ok(result);
    }

    // ========== 发起人操作 ==========

    @PostMapping("/approve")
    @Operation(summary = "审核报名申请", description = "仅活动发起人可操作")
    public R<Void> approveRegistration(
        @Parameter(description = "活动ID") @RequestParam Long activityId,
        @Parameter(description = "报名用户ID") @RequestParam Long participantId,
        @Parameter(description = "是否通过") @RequestParam Boolean approved) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }
        Boolean success = activityService.approveRegistration(activityId, participantId, approved, userId);
        if (!success) {
            return R.fail("操作失败");
        }
        return R.ok();
    }

    @PostMapping("/cancel/{activityId}")
    @Operation(summary = "取消活动", description = "仅活动发起人可操作")
    public R<Void> cancelActivity(
        @Parameter(description = "活动ID") @PathVariable Long activityId,
        @Parameter(description = "取消原因") @RequestParam(required = false) String reason) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return R.fail("请先登录");
        }
        Boolean success = activityService.cancelActivity(activityId, reason, userId);
        if (!success) {
            return R.fail("取消活动失败");
        }
        return R.ok();
    }

    /**
     * 获取当前登录用户ID
     * 优先从Gateway传递的X-User-Id请求头获取
     * 如果未登录返回null（某些接口允许未登录访问）
     */
    private Long getCurrentUserId() {
        // 优先从Gateway传递的请求头获取用户ID
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String userIdHeader = request.getHeader("X-User-Id");
                if (userIdHeader != null && !userIdHeader.isEmpty()) {
                    log.debug("从请求头X-User-Id获取到用户ID: {}", userIdHeader);
                    return Long.parseLong(userIdHeader);
                }
            }
        } catch (Exception e) {
            log.debug("从请求头获取用户ID失败: {}", e.getMessage());
        }

        // 回退：尝试从Sa-Token获取
        try {
            Long userId = LoginHelper.getUserId();
            if (userId != null) {
                log.debug("从Sa-Token获取到用户ID: {}", userId);
                return userId;
            }
        } catch (Exception e) {
            log.debug("从Sa-Token获取用户ID失败: {}", e.getMessage());
        }

        log.debug("未能获取到用户ID，用户可能未登录");
        return null;
    }
}
