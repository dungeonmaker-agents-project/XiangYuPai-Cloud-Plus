package com.xypai.user.controller.app;

import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.web.core.BaseController;
import com.xypai.user.domain.dto.UserProfileUpdateDTO;
import com.xypai.user.domain.vo.ProfileCompletenessVO;
import com.xypai.user.domain.vo.UserProfileVO;
import com.xypai.user.service.IUserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户资料控制器（v7.1完整版）
 *
 * @author Bob
 * @date 2025-01-14
 */
@Tag(name = "用户资料", description = "用户资料管理API（42个字段完整支持）")
@RestController
@RequestMapping("/api/v1/user/profile")
@RequiredArgsConstructor
@Validated
public class UserProfileController extends BaseController {

    private final IUserProfileService userProfileService;

    /**
     * 获取用户资料详情
     */
    @Operation(summary = "获取用户资料", description = "查询用户的完整资料信息（42个字段）")
    @GetMapping("/{userId}")
    // ⚠️ 已移除权限检查 - 允许已登录用户查看任何人的公开资料
    // @SaCheckPermission("user:profile:query")
    @Log(title = "查询资料", businessType = BusinessType.OTHER)
    public R<UserProfileVO> getUserProfile(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        UserProfileVO profile = userProfileService.getUserProfile(userId);
        return R.ok(profile);
    }

    /**
     * 获取当前用户资料
     */
    @Operation(summary = "获取当前用户资料", description = "查询当前登录用户的完整资料")
    @GetMapping("/current")
    // ⚠️ 已移除权限检查 - 已登录用户可以查看自己的资料
    // @SaCheckPermission("user:profile:query")
    public R<UserProfileVO> getCurrentUserProfile() {
        Long userId = LoginHelper.getUserId();
        UserProfileVO profile = userProfileService.getUserProfile(userId);
        return R.ok(profile);
    }

    /**
     * 更新用户资料
     */
    @Operation(summary = "更新用户资料", description = "更新用户的资料信息（支持分步骤编辑）")
    @PutMapping("/{userId}")
    @SaCheckPermission("user:profile:edit")
    @Log(title = "更新资料", businessType = BusinessType.UPDATE)
    public R<Void> updateUserProfile(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Validated @RequestBody UserProfileUpdateDTO updateDTO) {
        boolean result = userProfileService.updateUserProfile(userId, updateDTO);
        return result ? R.ok("更新资料成功") : R.fail("更新资料失败");
    }

    /**
     * 更新当前用户资料
     */
    @Operation(summary = "更新当前用户资料", description = "更新当前登录用户的资料")
    @PutMapping("/current")
    @SaCheckPermission("user:profile:edit")
    @Log(title = "更新个人资料", businessType = BusinessType.UPDATE)
    public R<Void> updateCurrentUserProfile(
            @Validated @RequestBody UserProfileUpdateDTO updateDTO) {
        Long userId = LoginHelper.getUserId();
        boolean result = userProfileService.updateUserProfile(userId, updateDTO);
        return result ? R.ok("更新资料成功") : R.fail("更新资料失败");
    }

    /**
     * 获取资料完整度
     */
    @Operation(summary = "获取资料完整度", description = "查询用户的资料完整度信息（0-100分）")
    @GetMapping("/{userId}/completeness")
    // ⚠️ 已移除权限检查 - 允许已登录用户查看资料完整度
    // @SaCheckPermission("user:profile:completeness")
    public R<ProfileCompletenessVO> getProfileCompleteness(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        ProfileCompletenessVO completeness = userProfileService.getProfileCompleteness(userId);
        return R.ok(completeness);
    }

    /**
     * 获取当前用户资料完整度
     */
    @Operation(summary = "获取当前用户资料完整度", description = "查询当前用户的资料完整度和完善建议")
    @GetMapping("/current/completeness")
    // ⚠️ 已移除权限检查 - 已登录用户可以查看自己的完整度
    // @SaCheckPermission("user:profile:completeness")
    public R<ProfileCompletenessVO> getCurrentUserCompleteness() {
        Long userId = LoginHelper.getUserId();
        ProfileCompletenessVO completeness = userProfileService.getProfileCompleteness(userId);
        return R.ok(completeness);
    }

    /**
     * 更新在线状态
     */
    @Operation(summary = "更新在线状态", description = "更新用户的在线状态（在线/离线/忙碌/隐身）")
    @PutMapping("/{userId}/online-status")
    @SaCheckPermission("user:profile:edit")
    @Log(title = "更新在线状态", businessType = BusinessType.UPDATE)
    public R<Void> updateOnlineStatus(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "在线状态", required = true)
            @RequestParam Integer onlineStatus) {
        boolean result = userProfileService.updateOnlineStatus(userId, onlineStatus);
        return toAjax(result);
    }

    /**
     * 用户上线
     */
    @Operation(summary = "用户上线", description = "标记用户为在线状态")
    @PutMapping("/current/go-online")
    @SaCheckPermission("user:profile:edit")
    public R<Void> goOnline() {
        Long userId = LoginHelper.getUserId();
        boolean result = userProfileService.goOnline(userId);
        return toAjax(result);
    }

    /**
     * 用户离线
     */
    @Operation(summary = "用户离线", description = "标记用户为离线状态")
    @PutMapping("/current/go-offline")
    @SaCheckPermission("user:profile:edit")
    public R<Void> goOffline() {
        Long userId = LoginHelper.getUserId();
        boolean result = userProfileService.goOffline(userId);
        return toAjax(result);
    }

    /**
     * 用户隐身
     */
    @Operation(summary = "用户隐身", description = "标记用户为隐身状态")
    @PutMapping("/current/go-invisible")
    @SaCheckPermission("user:profile:edit")
    public R<Void> goInvisible() {
        Long userId = LoginHelper.getUserId();
        boolean result = userProfileService.goInvisible(userId);
        return toAjax(result);
    }

    /**
     * 检查用户是否在线
     */
    @Operation(summary = "检查用户是否在线", description = "检查用户是否在线（5分钟内活跃）")
    @GetMapping("/{userId}/is-online")
    public R<Boolean> isUserOnline(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        boolean isOnline = userProfileService.isUserOnline(userId);
        return R.ok(isOnline);
    }
}

