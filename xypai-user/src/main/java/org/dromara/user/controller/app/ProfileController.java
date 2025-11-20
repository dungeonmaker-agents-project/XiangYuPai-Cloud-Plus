package org.dromara.user.controller.app;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.user.domain.dto.*;
import org.dromara.user.domain.vo.*;
import org.dromara.user.service.IUserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户资料控制器
 * User Profile Controller
 *
 * @author XiangYuPai
 * @since 2025-11-14
 */
@Slf4j
@Tag(name = "User Profile API", description = "用户资料接口")
@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final IUserService userService;

    /**
     * 获取当前用户ID (支持从Header fallback)
     * ⭐ 优先从Sa-Token获取，如果失败则从Gateway传递的Header读取
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        // 优先从LoginHelper获取
        Long userId = LoginHelper.getUserId();

        // Fallback: 从Header读取
        if (userId == null) {
            String userIdHeader = request.getHeader("X-User-Id");
            if (userIdHeader != null && !userIdHeader.isEmpty()) {
                try {
                    userId = Long.parseLong(userIdHeader);
                    log.debug("从Header提取userId: {}", userId);
                } catch (NumberFormatException e) {
                    log.error("Header中的userId格式错误: {}", userIdHeader);
                }
            }
        }

        return userId;
    }

    @Operation(summary = "Get user profile header (personal)")
    @GetMapping("/header")
    public R<UserProfileVo> getProfileHeader(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return userService.getUserProfile(userId);
    }

    @Operation(summary = "Get profile edit data")
    @GetMapping("/edit")
    public R<UserProfileVo> getProfileEdit(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return userService.getUserProfile(userId);
    }

    @Operation(summary = "Get other user profile")
    @GetMapping("/other/{targetUserId}")
    public R<UserProfileVo> getOtherUserProfile(@PathVariable Long targetUserId, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return userService.getOtherUserProfile(userId, targetUserId);
    }

    @Operation(summary = "Update nickname")
    @PutMapping("/nickname")
    public R<Void> updateNickname(@RequestBody @Validated UpdateNicknameDto dto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return userService.updateNickname(userId, dto);
    }

    @Operation(summary = "Update gender")
    @PutMapping("/gender")
    public R<Void> updateGender(@RequestBody @Validated UpdateGenderDto dto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return userService.updateGender(userId, dto);
    }

    @Operation(summary = "Update birthday")
    @PutMapping("/birthday")
    public R<Void> updateBirthday(@RequestBody @Validated UpdateBirthdayDto dto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return userService.updateBirthday(userId, dto);
    }

    @Operation(summary = "Update residence")
    @PutMapping("/residence")
    public R<Void> updateResidence(@RequestBody @Validated UserUpdateDto dto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return userService.updateUserProfile(userId, dto);
    }

    @Operation(summary = "Update height")
    @PutMapping("/height")
    public R<Void> updateHeight(@RequestBody @Validated UserUpdateDto dto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return userService.updateUserProfile(userId, dto);
    }

    @Operation(summary = "Update weight")
    @PutMapping("/weight")
    public R<Void> updateWeight(@RequestBody @Validated UserUpdateDto dto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return userService.updateUserProfile(userId, dto);
    }

    @Operation(summary = "Update occupation")
    @PutMapping("/occupation")
    public R<Void> updateOccupation(@RequestBody @Validated UserUpdateDto dto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return userService.updateUserProfile(userId, dto);
    }

    @Operation(summary = "Update WeChat ID")
    @PutMapping("/wechat")
    public R<Void> updateWechat(@RequestBody @Validated UserUpdateDto dto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return userService.updateUserProfile(userId, dto);
    }

    @Operation(summary = "Update bio")
    @PutMapping("/bio")
    public R<Void> updateBio(@RequestBody @Validated UserUpdateDto dto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return userService.updateUserProfile(userId, dto);
    }

    @Operation(summary = "Upload avatar")
    @PostMapping("/avatar/upload")
    public R<String> uploadAvatar(@RequestParam("avatar") MultipartFile file, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return userService.uploadAvatar(userId, file);
    }

    @Operation(summary = "Get user posts list")
    @GetMapping("/posts")
    public R<PostListVo> getUserPosts(
        @RequestParam(required = false, defaultValue = "1") Integer page,
        @RequestParam(required = false, defaultValue = "20") Integer pageSize,
        HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return userService.getUserPosts(userId, page, pageSize);
    }

    @Operation(summary = "Get user favorites list")
    @GetMapping("/favorites")
    public R<FavoriteListVo> getUserFavorites(
        @RequestParam(required = false, defaultValue = "1") Integer page,
        @RequestParam(required = false, defaultValue = "20") Integer pageSize,
        HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return userService.getUserFavorites(userId, page, pageSize);
    }

    @Operation(summary = "Get user likes list")
    @GetMapping("/likes")
    public R<LikeListVo> getUserLikes(
        @RequestParam(required = false, defaultValue = "1") Integer page,
        @RequestParam(required = false, defaultValue = "20") Integer pageSize,
        HttpServletRequest request
    ) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return userService.getUserLikes(userId, page, pageSize);
    }

    @Operation(summary = "Get user profile info (with skills)")
    @GetMapping("/info")
    public R<ProfileInfoVo> getProfileInfo(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return R.fail("无法获取用户信息");
        }
        return userService.getUserProfileInfo(userId);
    }
}
