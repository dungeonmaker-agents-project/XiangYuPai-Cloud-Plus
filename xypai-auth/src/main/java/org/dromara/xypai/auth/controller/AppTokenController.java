package org.dromara.xypai.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * App Token管理控制器
 *
 * <p>功能：</p>
 * <ul>
 *     <li>刷新Token</li>
 *     <li>登出（Token失效）</li>
 * </ul>
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
@Tag(name = "App Token管理", description = "Token刷新和登出")
public class AppTokenController {

    /**
     * 刷新Token
     *
     * <p>使用场景：当Access Token即将过期时，使用Refresh Token获取新的Access Token</p>
     *
     * <p>请求示例：</p>
     * <pre>
     * POST /auth/token/refresh
     * {
     *   "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * }
     * </pre>
     *
     * <p>响应示例：</p>
     * <pre>
     * {
     *   "code": 200,
     *   "message": "Token刷新成功",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",  // 新的Access Token
     *     "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",  // 新的Refresh Token
     *     "expireIn": 7200  // 过期时间（秒）
     *   }
     * }
     * </pre>
     *
     * @param request 刷新请求
     * @return 新Token
     */
    @PostMapping("/token/refresh")
    @Operation(summary = "刷新Token", description = "使用Refresh Token获取新的Access Token")
    public R<RefreshTokenResult> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        log.info("刷新Token请求：refreshToken={}", refreshToken);

        try {
            // 1. 验证Refresh Token是否有效
            Object loginId = StpUtil.getLoginIdByToken(refreshToken);
            if (loginId == null) {
                log.warn("Refresh Token无效或已过期");
                return R.fail("Refresh Token无效或已过期");
            }

            // 2. 生成新的Token
            StpUtil.login(loginId);
            String newAccessToken = StpUtil.getTokenValue();
            long expireIn = StpUtil.getTokenTimeout();

            log.info("Token刷新成功：loginId={}, newToken={}", loginId, newAccessToken);

            // 3. 返回新Token
            RefreshTokenResult result = new RefreshTokenResult(
                newAccessToken,
                refreshToken,  // Refresh Token保持不变（也可以生成新的）
                expireIn
            );

            return R.ok("Token刷新成功", result);
        } catch (Exception e) {
            log.error("Token刷新失败：error={}", e.getMessage(), e);
            return R.fail("Token刷新失败：" + e.getMessage());
        }
    }

    /**
     * 登出
     *
     * <p>功能：使当前Token失效，用户需要重新登录</p>
     *
     * <p>请求示例：</p>
     * <pre>
     * POST /auth/logout
     * Headers: Authorization: Bearer <token>
     * </pre>
     *
     * <p>响应示例：</p>
     * <pre>
     * {
     *   "code": 200,
     *   "message": "登出成功"
     * }
     * </pre>
     *
     * @return 结果
     */
    @PostMapping("/logout")
    @Operation(summary = "登出", description = "退出登录，Token失效")
    public R<Void> logout() {
        try {
            Long userId = LoginHelper.getUserId();
            log.info("用户登出：userId={}", userId);

            // 登出（Sa-Token会将Token加入黑名单）
            StpUtil.logout();

            log.info("用户登出成功：userId={}", userId);
            return R.ok("登出成功");
        } catch (Exception e) {
            log.error("登出失败：error={}", e.getMessage(), e);
            return R.fail("登出失败：" + e.getMessage());
        }
    }

    /**
     * 刷新Token请求
     */
    public static class RefreshTokenRequest implements Serializable {

        @NotBlank(message = "Refresh Token不能为空")
        private String refreshToken;

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }

    /**
     * 刷新Token结果
     */
    public record RefreshTokenResult(
        String token,
        String refreshToken,
        Long expireIn
    ) {
    }
}
