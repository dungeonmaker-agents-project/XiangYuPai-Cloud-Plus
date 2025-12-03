package org.dromara.appbff.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.appbff.domain.dto.UnlockWechatDTO;
import org.dromara.appbff.domain.vo.OtherUserProfileVO;
import org.dromara.appbff.domain.vo.ProfileInfoVO;
import org.dromara.appbff.domain.vo.UnlockWechatResultVO;
import org.dromara.appbff.domain.vo.UserSkillsListVO;
import org.dromara.appbff.service.OtherUserProfileService;
import org.dromara.common.core.domain.R;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 对方主页控制器
 * Other User Profile Controller
 *
 * <p>对应UI文档: 对方主页_结构文档.md</p>
 *
 * @author XyPai Team
 * @date 2025-12-02
 */
@Slf4j
@Tag(name = "Other User Profile API", description = "对方主页接口")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class OtherUserProfileController {

    private final OtherUserProfileService otherUserProfileService;

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        Long userId = LoginHelper.getUserId();
        if (userId == null) {
            String userIdHeader = request.getHeader("X-User-Id");
            if (userIdHeader != null && !userIdHeader.isEmpty()) {
                try {
                    userId = Long.parseLong(userIdHeader);
                } catch (NumberFormatException e) {
                    log.warn("Header中的userId格式错误: {}", userIdHeader);
                }
            }
        }
        return userId;
    }

    @Operation(summary = "获取对方主页数据", description = "获取指定用户的主页展示数据")
    @GetMapping("/{userId}")
    public R<OtherUserProfileVO> getOtherUserProfile(
        @Parameter(description = "目标用户ID") @PathVariable Long userId,
        @Parameter(description = "当前纬度") @RequestParam(required = false) Double latitude,
        @Parameter(description = "当前经度") @RequestParam(required = false) Double longitude,
        HttpServletRequest request
    ) {
        Long currentUserId = getCurrentUserId(request);
        OtherUserProfileVO result = otherUserProfileService.getOtherUserProfile(
            userId, currentUserId, latitude, longitude
        );

        if (result == null) {
            return R.fail("用户不存在");
        }
        return R.ok(result);
    }

    @Operation(summary = "获取用户资料详情", description = "获取用户的详细资料信息")
    @GetMapping("/{userId}/info")
    public R<ProfileInfoVO> getProfileInfo(
        @Parameter(description = "目标用户ID") @PathVariable Long userId,
        HttpServletRequest request
    ) {
        Long currentUserId = getCurrentUserId(request);
        ProfileInfoVO result = otherUserProfileService.getProfileInfo(userId, currentUserId);

        if (result == null) {
            return R.fail("用户资料不存在");
        }
        return R.ok(result);
    }

    @Operation(summary = "获取用户技能列表", description = "获取指定用户的技能服务列表")
    @GetMapping("/{userId}/skills")
    public R<UserSkillsListVO> getUserSkills(
        @Parameter(description = "目标用户ID") @PathVariable Long userId,
        @Parameter(description = "页码") @RequestParam(required = false, defaultValue = "1") Integer pageNum,
        @Parameter(description = "每页数量") @RequestParam(required = false, defaultValue = "10") Integer pageSize,
        HttpServletRequest request
    ) {
        Long currentUserId = getCurrentUserId(request);
        UserSkillsListVO result = otherUserProfileService.getUserSkills(
            userId, currentUserId, pageNum, pageSize
        );
        return R.ok(result);
    }

    @Operation(summary = "解锁微信", description = "使用金币解锁用户的微信号")
    @PostMapping("/unlock-wechat")
    public R<UnlockWechatResultVO> unlockWechat(
        @RequestBody @Validated UnlockWechatDTO dto,
        HttpServletRequest request
    ) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return R.fail("请先登录");
        }

        UnlockWechatResultVO result = otherUserProfileService.unlockWechat(currentUserId, dto);
        if (!result.getSuccess()) {
            return R.fail(result.getFailReason());
        }
        return R.ok(result);
    }

    @Operation(summary = "关注用户", description = "关注指定用户")
    @PostMapping("/{userId}/follow")
    public R<Void> followUser(
        @Parameter(description = "目标用户ID") @PathVariable Long userId,
        HttpServletRequest request
    ) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return R.fail("请先登录");
        }

        if (currentUserId.equals(userId)) {
            return R.fail("不能关注自己");
        }

        boolean success = otherUserProfileService.followUser(currentUserId, userId);
        if (success) {
            return R.ok();
        } else {
            return R.fail("已经关注过了");
        }
    }

    @Operation(summary = "取消关注", description = "取消关注指定用户")
    @DeleteMapping("/{userId}/follow")
    public R<Void> unfollowUser(
        @Parameter(description = "目标用户ID") @PathVariable Long userId,
        HttpServletRequest request
    ) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return R.fail("请先登录");
        }

        boolean success = otherUserProfileService.unfollowUser(currentUserId, userId);
        if (success) {
            return R.ok();
        } else {
            return R.fail("尚未关注该用户");
        }
    }
}
