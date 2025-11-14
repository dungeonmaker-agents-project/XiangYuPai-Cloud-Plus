package org.dromara.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.auth.domain.dto.AppPasswordLoginDto;
import org.dromara.auth.domain.dto.AppSmsLoginDto;
import org.dromara.auth.domain.vo.AppLoginVo;
import org.dromara.auth.service.IAuthStrategy;
import org.dromara.common.core.domain.R;
import org.dromara.common.json.utils.JsonUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * App认证控制器（无租户）
 *
 * <p>支持的登录方式：</p>
 * <ul>
 *     <li>SMS登录（支持自动注册）：POST /auth/login/sms</li>
 *     <li>密码登录：POST /auth/login/password</li>
 * </ul>
 *
 * @author XyPai Team
 * @date 2025-11-13
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth/login")
@Tag(name = "App认证", description = "App用户登录（无租户）")
public class AppAuthController {

    /**
     * SMS验证码登录（支持自动注册）
     *
     * <p>流程：</p>
     * <ol>
     *     <li>用户输入手机号 → 调用 /auth/sms/send 获取验证码</li>
     *     <li>用户输入6位验证码 → 调用此接口登录</li>
     *     <li>后端验证验证码 → 自动注册（如果未注册） → 返回token</li>
     * </ol>
     *
     * <p>请求示例：</p>
     * <pre>
     * POST /auth/login/sms
     * {
     *   "countryCode": "+86",
     *   "mobile": "13800138000",
     *   "verificationCode": "123456",
     *   "agreeToTerms": true,
     *   "clientId": "app",
     *   "grantType": "app_sms"
     * }
     * </pre>
     *
     * <p>响应示例：</p>
     * <pre>
     * {
     *   "code": 200,
     *   "message": "成功",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *     "expireIn": 7200,
     *     "userId": "1001",
     *     "nickname": "138****8000",
     *     "avatar": null,
     *     "isNewUser": true  // ⭐ 前端根据此字段决定跳转：true→完善资料页，false→主页
     *   }
     * }
     * </pre>
     *
     * @param loginDto 登录请求
     * @return 登录结果（包含token和用户信息）
     */
    @PostMapping("/sms")
    @Operation(summary = "SMS验证码登录", description = "支持自动注册，返回isNewUser标记")
    public R<AppLoginVo> smsLogin(@RequestBody @Valid AppSmsLoginDto loginDto) {
        log.info("App SMS登录请求：mobile={}, countryCode={}", loginDto.getMobile(), loginDto.getCountryCode());

        // 设置默认值
        if (loginDto.getClientId() == null) {
            loginDto.setClientId("app");
        }
        if (loginDto.getGrantType() == null) {
            loginDto.setGrantType("app_sms");
        }

        // 调用策略模式登录
        String body = JsonUtils.toJsonString(loginDto);
        AppLoginVo loginVo = IAuthStrategy.login(body, null, "app_sms");

        log.info("App SMS登录成功：userId={}, isNewUser={}", loginVo.getUserId(), loginVo.getIsNewUser());
        return R.ok(loginVo);
    }

    /**
     * 密码登录
     *
     * <p>前提条件：</p>
     * <ul>
     *     <li>用户已设置密码（首次SMS登录后在个人中心设置）</li>
     * </ul>
     *
     * <p>请求示例：</p>
     * <pre>
     * POST /auth/login/password
     * {
     *   "countryCode": "+86",
     *   "mobile": "13800138000",
     *   "password": "password123",
     *   "agreeToTerms": true,
     *   "clientId": "app",
     *   "grantType": "app_password"
     * }
     * </pre>
     *
     * @param loginDto 登录请求
     * @return 登录结果
     */
    @PostMapping("/password")
    @Operation(summary = "密码登录", description = "手机号+密码登录（需先设置密码）")
    public R<AppLoginVo> passwordLogin(@RequestBody @Valid AppPasswordLoginDto loginDto) {
        log.info("App密码登录请求：mobile={}, countryCode={}", loginDto.getMobile(), loginDto.getCountryCode());

        // 设置默认值
        if (loginDto.getClientId() == null) {
            loginDto.setClientId("app");
        }
        if (loginDto.getGrantType() == null) {
            loginDto.setGrantType("app_password");
        }

        // 调用策略模式登录
        String body = JsonUtils.toJsonString(loginDto);
        AppLoginVo loginVo = IAuthStrategy.login(body, null, "app_password");

        log.info("App密码登录成功：userId={}", loginVo.getUserId());
        return R.ok(loginVo);
    }
}
