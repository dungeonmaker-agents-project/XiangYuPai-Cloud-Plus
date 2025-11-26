package org.dromara.xypai.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.xypai.auth.domain.dto.AppPasswordLoginDto;
import org.dromara.xypai.auth.domain.dto.AppSmsLoginDto;
import org.dromara.xypai.auth.domain.vo.AppLoginVo;
import org.dromara.xypai.auth.service.IAppAuthStrategy;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * App认证控制器（无租户）
 *
 * <p>支持的登录方式：</p>
 * <ul>
 *     <li>SMS登录（支持自动注册）：POST /api/auth/login/sms</li>
 *     <li>密码登录：POST /api/auth/login/password</li>
 * </ul>
 *
 * <p>Gateway路由：</p>
 * <ul>
 *     <li>前端请求: /xypai-auth/api/auth/login/xxx</li>
 *     <li>Gateway StripPrefix=1 后: /api/auth/login/xxx</li>
 * </ul>
 *
 * @author XyPai Team
 * @date 2025-11-13
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/login")
@Tag(name = "App认证", description = "App用户登录（无租户）")
public class AppAuthController {

    /**
     * SMS验证码登录（支持自动注册）
     *
     * <p>流程：</p>
     * <ol>
     *     <li>用户输入手机号 → 调用 /api/auth/sms/send 获取验证码</li>
     *     <li>用户输入6位验证码 → 调用此接口登录</li>
     *     <li>后端验证验证码 → 自动注册（如果未注册） → 返回token</li>
     * </ol>
     *
     * <p>请求示例：</p>
     * <pre>
     * POST /api/auth/login/sms
     * {
     *   "countryCode": "+86",
     *   "mobile": "13800138000",
     *   "verificationCode": "123456"
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

        // 后端自动判断clientId（从User-Agent）
        String clientId = determineClientId(ServletUtils.getRequest());
        loginDto.setClientId(clientId);

        // 后端自动设置grantType
        loginDto.setGrantType("app_sms");

        // 调用策略模式登录
        String body = JsonUtils.toJsonString(loginDto);
        AppLoginVo loginVo = IAppAuthStrategy.login(body, null, "app_sms");

        log.info("App SMS登录成功：userId={}, isNewUser={}, clientId={}",
            loginVo.getUserId(), loginVo.getIsNewUser(), clientId);
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
     * POST /api/auth/login/password
     * {
     *   "countryCode": "+86",
     *   "mobile": "13800138000",
     *   "password": "password123"
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

        // 后端自动判断clientId（从User-Agent）
        String clientId = determineClientId(ServletUtils.getRequest());
        loginDto.setClientId(clientId);

        // 后端自动设置grantType
        loginDto.setGrantType("app_password");

        // 调用策略模式登录
        String body = JsonUtils.toJsonString(loginDto);
        AppLoginVo loginVo = IAppAuthStrategy.login(body, null, "app_password");

        log.info("App密码登录成功：userId={}, clientId={}", loginVo.getUserId(), clientId);
        return R.ok(loginVo);
    }

    /**
     * 根据请求自动判断客户端类型
     * <p>优先级：</p>
     * <ol>
     *     <li>自定义请求头 X-Platform（推荐前端设置）</li>
     *     <li>自定义请求头 X-Client-Type</li>
     *     <li>User-Agent 解析</li>
     * </ol>
     *
     * @param request HTTP请求
     * @return 客户端类型（ios/android/web/app）
     */
    private String determineClientId(HttpServletRequest request) {
        // 1. 优先使用自定义请求头 X-Platform
        String platform = request.getHeader("X-Platform");
        if (platform != null && !platform.isEmpty()) {
            return platform.toLowerCase();
        }

        // 2. 使用 X-Client-Type 请求头
        String clientType = request.getHeader("X-Client-Type");
        if (clientType != null && !clientType.isEmpty()) {
            return clientType.toLowerCase();
        }

        // 3. 从 User-Agent 解析
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            String lowerUA = userAgent.toLowerCase();
            if (lowerUA.contains("iphone") || lowerUA.contains("ipad") || lowerUA.contains("ipod")) {
                return "ios";
            } else if (lowerUA.contains("android")) {
                return "android";
            } else if (lowerUA.contains("windows") || lowerUA.contains("macintosh") || lowerUA.contains("linux")) {
                return "web";
            }
        }

        // 4. 默认值
        return "app";
    }
}
