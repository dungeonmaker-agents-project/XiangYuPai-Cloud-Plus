package com.xypai.user.controller.auth;

import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import com.xypai.user.domain.dto.AuthUserQueryDTO;
import com.xypai.user.domain.dto.AutoRegisterDTO;
import com.xypai.user.domain.dto.UserValidateDTO;
import com.xypai.user.domain.vo.AuthUserVO;
import com.xypai.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证用户控制�?内部调用)
 *
 * @author xypai
 * @date 2025-01-01
 */
@Slf4j
@Tag(name = "认证用户管理", description = "供认证服务内部调用的用户API")
@RestController
@RequestMapping("/api/v1/users/auth")
@RequiredArgsConstructor
public class AuthUserController extends BaseController {

    private final IUserService userService;

    /**
     * 根据用户名获取用户信�?认证服务专用)
     */
    @Operation(summary = "根据用户名获取用户信息", description = "认证服务专用接口")
    @GetMapping("/username/{username}")
    public R<AuthUserVO> getUserByUsername(@PathVariable("username") String username) {
        AuthUserVO userVO = userService.selectAuthUserByUsername(username);
        return userVO != null ? R.ok(userVO) : R.fail("用户不存在");
    }

    /**
     * 根据手机号获取用户信�?认证服务专用)
     */
    @Operation(summary = "根据手机号获取用户信息", description = "认证服务专用接口")
    @GetMapping("/mobile/{mobile}")
    public R<AuthUserVO> getUserByMobile(@PathVariable("mobile") String mobile) {
        try {
            log.debug("📞 收到手机号查询请求: mobile={}", mobile);
            AuthUserVO userVO = userService.selectAuthUserByMobile(mobile);
            if (userVO != null) {
                log.info("✅ 查询成功: mobile={}, userId={}", mobile, userVO.getId());
                return R.ok(userVO);
            } else {
                log.warn("⚠️ 用户不存在: mobile={}", mobile);
                return R.fail("用户不存在");
            }
        } catch (Exception e) {
            log.error("❌ 查询用户异常: mobile={}, error={}", mobile, e.getMessage(), e);
            return R.fail("查询用户失败: " + e.getMessage());
        }
    }

    /**
     * 验证用户密码(认证服务专用)
     */
    @Operation(summary = "验证用户密码", description = "认证服务专用接口")
    @PostMapping("/validate-password")
    public R<Boolean> validatePassword(@RequestBody UserValidateDTO validateDTO) {
        boolean valid = userService.validateUserPassword(validateDTO);
        return R.ok(valid);
    }

    /**
     * 更新用户最后登录时�?认证服务专用)
     */
    @Operation(summary = "更新用户最后登录时间", description = "认证服务专用接口")
    @PostMapping("/update-login-time/{userId}")
    public R<Void> updateLastLoginTime(@PathVariable("userId") Long userId) {
        boolean success = userService.updateLastLoginTime(userId);
        return success ? R.ok() : R.fail("更新失败");
    }

    /**
     * 短信登录时自动注册用�?认证服务专用)
     */
    @Operation(summary = "短信登录自动注册", description = "认证服务专用接口，短信验证成功后自动创建用户")
    @PostMapping("/auto-register")
    public R<AuthUserVO> autoRegisterUser(@RequestBody AutoRegisterDTO autoRegisterDTO) {
        AuthUserVO userVO = userService.autoRegisterUser(autoRegisterDTO);
        return userVO != null ? R.ok(userVO) : R.fail("自动注册失败");
    }
}
