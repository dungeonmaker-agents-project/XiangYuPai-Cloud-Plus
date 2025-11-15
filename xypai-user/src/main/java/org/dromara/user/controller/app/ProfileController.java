package org.dromara.user.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.user.domain.dto.*;
import org.dromara.user.domain.vo.UserProfileVo;
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
@Tag(name = "User Profile API", description = "用户资料接口")
@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final IUserService userService;

    @Operation(summary = "Get user profile header (personal)")
    @GetMapping("/header")
    public R<UserProfileVo> getProfileHeader() {
        Long userId = LoginHelper.getUserId();
        return userService.getUserProfile(userId);
    }

    @Operation(summary = "Get profile edit data")
    @GetMapping("/edit")
    public R<UserProfileVo> getProfileEdit() {
        Long userId = LoginHelper.getUserId();
        return userService.getUserProfile(userId);
    }

    @Operation(summary = "Get other user profile")
    @GetMapping("/other/{targetUserId}")
    public R<UserProfileVo> getOtherUserProfile(@PathVariable Long targetUserId) {
        Long userId = LoginHelper.getUserId();
        return userService.getOtherUserProfile(userId, targetUserId);
    }

    @Operation(summary = "Update nickname")
    @PutMapping("/nickname")
    public R<Void> updateNickname(@RequestBody @Validated UpdateNicknameDto dto) {
        Long userId = LoginHelper.getUserId();
        return userService.updateNickname(userId, dto);
    }

    @Operation(summary = "Update gender")
    @PutMapping("/gender")
    public R<Void> updateGender(@RequestBody @Validated UpdateGenderDto dto) {
        Long userId = LoginHelper.getUserId();
        return userService.updateGender(userId, dto);
    }

    @Operation(summary = "Update birthday")
    @PutMapping("/birthday")
    public R<Void> updateBirthday(@RequestBody @Validated UpdateBirthdayDto dto) {
        Long userId = LoginHelper.getUserId();
        return userService.updateBirthday(userId, dto);
    }

    @Operation(summary = "Update residence")
    @PutMapping("/residence")
    public R<Void> updateResidence(@RequestBody @Validated UserUpdateDto dto) {
        Long userId = LoginHelper.getUserId();
        return userService.updateUserProfile(userId, dto);
    }

    @Operation(summary = "Update height")
    @PutMapping("/height")
    public R<Void> updateHeight(@RequestBody @Validated UserUpdateDto dto) {
        Long userId = LoginHelper.getUserId();
        return userService.updateUserProfile(userId, dto);
    }

    @Operation(summary = "Update weight")
    @PutMapping("/weight")
    public R<Void> updateWeight(@RequestBody @Validated UserUpdateDto dto) {
        Long userId = LoginHelper.getUserId();
        return userService.updateUserProfile(userId, dto);
    }

    @Operation(summary = "Update occupation")
    @PutMapping("/occupation")
    public R<Void> updateOccupation(@RequestBody @Validated UserUpdateDto dto) {
        Long userId = LoginHelper.getUserId();
        return userService.updateUserProfile(userId, dto);
    }

    @Operation(summary = "Update WeChat ID")
    @PutMapping("/wechat")
    public R<Void> updateWechat(@RequestBody @Validated UserUpdateDto dto) {
        Long userId = LoginHelper.getUserId();
        return userService.updateUserProfile(userId, dto);
    }

    @Operation(summary = "Update bio")
    @PutMapping("/bio")
    public R<Void> updateBio(@RequestBody @Validated UserUpdateDto dto) {
        Long userId = LoginHelper.getUserId();
        return userService.updateUserProfile(userId, dto);
    }

    @Operation(summary = "Upload avatar")
    @PostMapping("/avatar/upload")
    public R<String> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        Long userId = LoginHelper.getUserId();
        return userService.uploadAvatar(userId, file);
    }
}
