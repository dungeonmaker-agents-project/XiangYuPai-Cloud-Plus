package com.xypai.user.controller.app;

import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.xypai.user.domain.dto.UserAddDTO;
import com.xypai.user.domain.dto.UserQueryDTO;
import com.xypai.user.domain.dto.UserUpdateDTO;
import com.xypai.user.domain.vo.UserDetailVO;
import com.xypai.user.domain.vo.UserListVO;
import com.xypai.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 用户管理控制�?
 *
 * @author xypai
 * @date 2025-01-01
 */
@Tag(name = "用户管理", description = "用户基础信息管理API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController extends BaseController {

    private final IUserService userService;

    /**
     * 查询用户列表
     */
    @Operation(summary = "查询用户列表", description = "分页查询用户列表信息")
    @GetMapping("/list")
    @SaCheckPermission("user:user:list")
    public R<List<UserListVO>> list(UserQueryDTO query) {
        List<UserListVO> list = userService.selectUserList(query);
        return R.ok(list);
    }

    /**
     * 获取用户详细信息
     */
    @Operation(summary = "获取用户详细信息", description = "根据用户ID获取详细信息")
    @GetMapping("/{userId}")
    @SaCheckPermission("user:user:query")
    @Log(title = "用户管理", businessType = BusinessType.OTHER)
    public R<UserDetailVO> getInfo(
            @Parameter(description = "用户ID", required = true) 
            @PathVariable Long userId) {
        return R.ok(userService.selectUserById(userId));
    }

    /**
     * 新增用户
     */
    @Operation(summary = "新增用户", description = "创建新用户")
    @PostMapping
    @SaCheckPermission("user:user:add")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    public R<Void> add(@Validated @RequestBody UserAddDTO userAddDTO) {
        return toAjax(userService.insertUser(userAddDTO));
    }

    /**
     * 修改用户
     */
    @Operation(summary = "修改用户", description = "更新用户信息")
    @PutMapping
    @SaCheckPermission("user:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    public R<Void> edit(@Validated @RequestBody UserUpdateDTO userUpdateDTO) {
        return toAjax(userService.updateUser(userUpdateDTO));
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    @DeleteMapping("/{userIds}")
    @SaCheckPermission("user:user:remove")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    public R<Void> remove(
            @Parameter(description = "用户ID数组", required = true)
            @PathVariable Long[] userIds) {
        return toAjax(userService.deleteUserByIds(Arrays.asList(userIds)));
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/profile")
    @SaCheckPermission("user:user:query")
    public R<UserDetailVO> profile() {
        return R.ok(userService.selectCurrentUser());
    }

    /**
     * 更新当前用户信息
     */
    @Operation(summary = "更新当前用户信息", description = "更新当前登录用户的信息")
    @PutMapping("/profile")
    @SaCheckPermission("user:user:edit")
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    public R<Void> updateProfile(@Validated @RequestBody UserUpdateDTO userUpdateDTO) {
        return toAjax(userService.updateCurrentUser(userUpdateDTO));
    }

    /**
     * 重置用户密码
     */
    @Operation(summary = "重置用户密码", description = "重置指定用户的密码")
    @PutMapping("/{userId}/reset-password")
    @SaCheckPermission("user:user:resetPwd")
    @Log(title = "重置密码", businessType = BusinessType.UPDATE)
    public R<Void> resetPassword(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        return toAjax(userService.resetUserPassword(userId));
    }

    /**
     * 状态修�?
     */
    @Operation(summary = "修改用户状态", description = "启用/禁用用户")
    @PutMapping("/{userId}/status")
    @SaCheckPermission("user:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    public R<Void> changeStatus(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "状态值", required = true)
            @RequestParam Integer status) {
        return toAjax(userService.updateUserStatus(userId, status));
    }

    /**
     * 检查用户名是否唯一
     */
    @Operation(summary = "检查用户名唯一性", description = "检查用户名是否已被使用")
    @GetMapping("/check-username")
    public R<Boolean> checkUsername(
            @Parameter(description = "用户名", required = true)
            @RequestParam String username,
            @Parameter(description = "用户ID(编辑时使用)")
            @RequestParam(required = false) Long userId) {
        return R.ok(userService.checkUsernameUnique(username, userId));
    }

    /**
     * 检查手机号是否唯一
     */
    @Operation(summary = "检查手机号唯一性", description = "检查手机号是否已被使用")
    @GetMapping("/check-mobile")
    public R<Boolean> checkMobile(
            @Parameter(description = "手机号", required = true)
            @RequestParam String mobile,
            @Parameter(description = "用户ID(编辑时使用)")
            @RequestParam(required = false) Long userId) {
        return R.ok(userService.checkMobileUnique(mobile, userId));
    }
}


