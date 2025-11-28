package org.dromara.content.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.ratelimiter.annotation.RateLimiter;
import org.dromara.common.ratelimiter.enums.LimitType;
import org.dromara.common.web.core.BaseController;
import org.dromara.content.domain.dto.ActivityListQueryDTO;
import org.dromara.content.domain.dto.ActivityPublishDTO;
import org.dromara.content.domain.dto.ActivityRegisterDTO;
import org.dromara.content.domain.entity.ActivityType;
import org.dromara.content.domain.vo.ActivityDetailVO;
import org.dromara.content.domain.vo.ActivityListVO;
import org.dromara.content.service.IActivityService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动管理控制器
 *
 * @author XiangYuPai
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/activity")
@RequiredArgsConstructor
@Tag(name = "活动管理", description = "组局活动相关接口")
public class ActivityController extends BaseController {

    private final IActivityService activityService;

    /**
     * 获取活动列表
     */
    @Operation(summary = "获取活动列表", description = "支持分页、筛选、排序")
    @GetMapping("/list")
    @RateLimiter(count = 100, time = 60, limitType = LimitType.IP)
    public R<Page<ActivityListVO>> getActivityList(@Valid ActivityListQueryDTO queryDTO) {
        Long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        Page<ActivityListVO> page = activityService.getActivityList(queryDTO, userId);
        return R.ok(page);
    }

    /**
     * 获取活动详情
     */
    @Operation(summary = "获取活动详情", description = "获取单个活动的完整信息")
    @GetMapping("/detail/{activityId}")
    @RateLimiter(count = 200, time = 60, limitType = LimitType.IP)
    public R<ActivityDetailVO> getActivityDetail(
        @Parameter(description = "活动ID", required = true) @PathVariable Long activityId
    ) {
        Long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        ActivityDetailVO detail = activityService.getActivityDetail(activityId, userId);
        if (detail == null) {
            return R.fail("活动不存在");
        }
        // 增加浏览量
        activityService.incrementViewCount(activityId);
        return R.ok(detail);
    }

    /**
     * 发布活动
     */
    @Operation(summary = "发布活动", description = "发布新的组局活动")
    @PostMapping("/publish")
    @RateLimiter(count = 10, time = 60, limitType = LimitType.USER)
    public R<Long> publishActivity(@Valid @RequestBody ActivityPublishDTO publishDTO) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        Long activityId = activityService.publishActivity(publishDTO, userId);
        return R.ok("发布成功", activityId);
    }

    /**
     * 报名活动
     */
    @Operation(summary = "报名活动", description = "报名参加组局活动")
    @PostMapping("/register")
    @RateLimiter(count = 30, time = 60, limitType = LimitType.USER)
    public R<Long> registerActivity(@Valid @RequestBody ActivityRegisterDTO registerDTO) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            Long participantId = activityService.registerActivity(registerDTO, userId);
            return R.ok("报名成功", participantId);
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }

    /**
     * 取消报名
     */
    @Operation(summary = "取消报名", description = "取消活动报名")
    @PostMapping("/cancel-registration")
    public R<Void> cancelRegistration(
        @Parameter(description = "活动ID") @RequestParam Long activityId,
        @Parameter(description = "取消原因") @RequestParam(required = false) String reason
    ) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        boolean success = activityService.cancelRegistration(activityId, userId, reason);
        if (success) {
            return R.ok("取消成功");
        }
        return R.fail("取消失败");
    }

    /**
     * 审核报名
     */
    @Operation(summary = "审核报名", description = "发起人审核报名申请")
    @PostMapping("/approve")
    public R<Void> approveRegistration(
        @Parameter(description = "活动ID") @RequestParam Long activityId,
        @Parameter(description = "参与者ID") @RequestParam Long participantId,
        @Parameter(description = "是否通过") @RequestParam Boolean approved
    ) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            boolean success = activityService.approveRegistration(activityId, participantId, approved, userId);
            if (success) {
                return R.ok(approved ? "审核通过" : "已拒绝");
            }
            return R.fail("操作失败");
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }

    /**
     * 取消活动
     */
    @Operation(summary = "取消活动", description = "发起人取消活动")
    @PostMapping("/cancel/{activityId}")
    public R<Void> cancelActivity(
        @Parameter(description = "活动ID") @PathVariable Long activityId,
        @Parameter(description = "取消原因") @RequestParam(required = false) String reason
    ) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        boolean success = activityService.cancelActivity(activityId, userId, reason);
        if (success) {
            return R.ok("活动已取消");
        }
        return R.fail("取消失败");
    }

    /**
     * 获取活动类型列表
     */
    @Operation(summary = "获取活动类型列表", description = "获取所有活动类型配置")
    @GetMapping("/types")
    public R<List<ActivityType>> getActivityTypes() {
        List<ActivityType> types = activityService.getAllActivityTypes();
        return R.ok(types);
    }

    /**
     * 获取热门活动类型
     */
    @Operation(summary = "获取热门活动类型", description = "获取热门活动类型列表")
    @GetMapping("/types/hot")
    public R<List<ActivityType>> getHotActivityTypes() {
        List<ActivityType> types = activityService.getHotActivityTypes();
        return R.ok(types);
    }

    /**
     * 分享活动
     */
    @Operation(summary = "分享活动", description = "记录活动分享")
    @PostMapping("/share/{activityId}")
    public R<Void> shareActivity(
        @Parameter(description = "活动ID") @PathVariable Long activityId
    ) {
        activityService.incrementShareCount(activityId);
        return R.ok("分享成功");
    }
}
