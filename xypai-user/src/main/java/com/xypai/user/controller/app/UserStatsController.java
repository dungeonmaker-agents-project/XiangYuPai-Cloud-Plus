package com.xypai.user.controller.app;

import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.satoken.utils.LoginHelper;
import com.xypai.user.domain.vo.UserStatsVO;
import com.xypai.user.service.IUserStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户统计控制�?
 *
 * @author Bob
 * @date 2025-01-14
 */
@Tag(name = "用户统计", description = "用户统计数据管理API")
@RestController
@RequestMapping("/api/v1/users/stats")
@RequiredArgsConstructor
@Validated
public class UserStatsController extends BaseController {

    private final IUserStatsService userStatsService;

    /**
     * 获取用户统计信息
     */
    @Operation(summary = "获取用户统计", description = "查询用户的统计数据（优先从Redis读取）")
    @GetMapping("/{userId}")
    @SaCheckPermission("user:stats:query")
    @Log(title = "用户统计", businessType = BusinessType.OTHER)
    public R<UserStatsVO> getUserStats(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        UserStatsVO stats = userStatsService.getUserStats(userId);
        if (stats == null) {
            return R.fail("用户统计不存在");
        }
        return R.ok(stats);
    }

    /**
     * 获取当前用户统计
     */
    @Operation(summary = "获取当前用户统计", description = "获取当前登录用户的统计数据")
    @GetMapping("/current")
    @SaCheckPermission("user:stats:query")
    public R<UserStatsVO> getCurrentUserStats() {
        Long userId = LoginHelper.getUserId();
        UserStatsVO stats = userStatsService.getUserStats(userId);
        if (stats == null) {
            return R.fail("用户统计不存在");
        }
        return R.ok(stats);
    }

    /**
     * 批量查询用户统计
     */
    @Operation(summary = "批量查询用户统计", description = "批量获取多个用户的统计数据")
    @PostMapping("/batch")
    @SaCheckPermission("user:stats:query")
    public R<List<UserStatsVO>> getBatchUserStats(
            @Parameter(description = "用户ID列表", required = true)
            @RequestBody List<Long> userIds) {
        List<UserStatsVO> statsList = userStatsService.getBatchUserStats(userIds);
        return R.ok(statsList);
    }

    /**
     * 初始化用户统�?
     */
    @Operation(summary = "初始化用户统计", description = "为新用户创建统计记录")
    @PostMapping("/init")
    @SaCheckPermission("user:stats:edit")
    @Log(title = "初始化统计", businessType = BusinessType.INSERT)
    public R<Void> initUserStats(
            @Parameter(description = "用户ID", required = true)
            @RequestParam Long userId) {
        boolean result = userStatsService.initUserStats(userId);
        return result ? R.ok() : R.fail();
    }

    /**
     * 刷新用户统计缓存
     */
    @Operation(summary = "刷新统计缓存", description = "从MySQL重新加载统计数据到Redis")
    @PostMapping("/{userId}/refresh")
    @SaCheckPermission("user:stats:edit")
    @Log(title = "刷新缓存", businessType = BusinessType.UPDATE)
    public R<Void> refreshCache(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        boolean result = userStatsService.refreshCache(userId);
        return result ? R.ok("刷新缓存成功") : R.fail("刷新缓存失败");
    }

    /**
     * 查询人气用户排行�?
     */
    @Operation(summary = "人气用户排行", description = "查询粉丝数最多的用户（TOP 10）")
    @GetMapping("/popular")
    @SaCheckPermission("user:stats:query")
    public R<List<UserStatsVO>> getPopularUsers(
            @Parameter(description = "数量限制", required = false)
            @RequestParam(defaultValue = "10") Integer limit) {
        List<UserStatsVO> users = userStatsService.getPopularUsers(limit);
        return R.ok(users);
    }

    /**
     * 查询优质组局者排行榜
     */
    @Operation(summary = "优质组局者排行", description = "查询评分和成功率最高的组局者（TOP 10）")
    @GetMapping("/quality-organizers")
    @SaCheckPermission("user:stats:query")
    public R<List<UserStatsVO>> getQualityOrganizers(
            @Parameter(description = "数量限制", required = false)
            @RequestParam(defaultValue = "10") Integer limit) {
        List<UserStatsVO> organizers = userStatsService.getQualityOrganizers(limit);
        return R.ok(organizers);
    }

    /**
     * 增加粉丝数（内部接口�?
     */
    @Operation(summary = "增加粉丝数", description = "用户被关注时调用（仅内部服务）")
    @PutMapping("/{userId}/follower/increment")
    @SaCheckPermission("user:stats:edit")
    @Log(title = "增加粉丝", businessType = BusinessType.UPDATE)
    public R<Void> incrementFollowerCount(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        boolean result = userStatsService.incrementFollowerCount(userId);
        return result ? R.ok() : R.fail();
    }

    /**
     * 减少粉丝数（内部接口�?
     */
    @Operation(summary = "减少粉丝数", description = "用户被取消关注时调用（仅内部服务）")
    @PutMapping("/{userId}/follower/decrement")
    @SaCheckPermission("user:stats:edit")
    @Log(title = "减少粉丝", businessType = BusinessType.UPDATE)
    public R<Void> decrementFollowerCount(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        boolean result = userStatsService.decrementFollowerCount(userId);
        return result ? R.ok() : R.fail();
    }

    /**
     * 增加内容数（内部接口�?
     */
    @Operation(summary = "增加内容数", description = "用户发布内容时调用（仅内部服务）")
    @PutMapping("/{userId}/content/increment")
    @SaCheckPermission("user:stats:edit")
    @Log(title = "增加内容", businessType = BusinessType.UPDATE)
    public R<Void> incrementContentCount(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        boolean result = userStatsService.incrementContentCount(userId);
        return result ? R.ok() : R.fail();
    }

    /**
     * 增加点赞数（内部接口�?
     */
    @Operation(summary = "增加点赞数", description = "用户内容被点赞时调用（仅内部服务）")
    @PutMapping("/{userId}/like/increment")
    @SaCheckPermission("user:stats:edit")
    @Log(title = "增加点赞", businessType = BusinessType.UPDATE)
    public R<Void> incrementLikeCount(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "增加数量", required = false)
            @RequestParam(defaultValue = "1") Integer count) {
        boolean result = userStatsService.incrementLikeCount(userId, count);
        return result ? R.ok() : R.fail();
    }
}

