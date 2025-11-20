package org.dromara.xypai.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.appuser.api.RemoteAppUserService;
import org.dromara.xypai.auth.domain.dto.ResetPasswordDto;
import org.dromara.xypai.auth.domain.dto.VerifyCodeDto;
import org.dromara.common.core.constant.GlobalConstants;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.redis.utils.RedisUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * 忘记密码控制器
 *
 * <p>完整流程：</p>
 * <ol>
 *     <li>用户输入手机号 → 调用 {@code POST /auth/sms/send} 发送验证码（复用SmsController）</li>
 *     <li>用户输入验证码 → 调用 {@code POST /auth/password/reset/verify} 验证验证码</li>
 *     <li>用户设置新密码 → 调用 {@code POST /auth/password/reset/confirm} 重置密码</li>
 * </ol>
 *
 * @author XyPai Team
 * @date 2025-11-13
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth/password")
@Tag(name = "忘记密码", description = "App忘记密码流程（3步）")
public class ForgotPasswordController {

    @DubboReference
    private final RemoteAppUserService remoteAppUserService;

    /**
     * Redis Key 前缀
     */
    private static final String SMS_CODE_KEY = GlobalConstants.CAPTCHA_CODE_KEY;
    private static final String VERIFIED_CODE_KEY = "verified_code:";

    /**
     * 步骤1：发送短信验证码
     *
     * <p>⚠️ 注意：此步骤复用 {@link SmsController#sendCode(org.dromara.xypai.auth.controller.SmsController.SendSmsRequest)}</p>
     * <p>请求示例：</p>
     * <pre>
     * POST /auth/sms/send
     * {
     *   "mobile": "13800138000",
     *   "countryCode": "+86",
     *   "type": "reset"  // 固定值：reset（表示重置密码）
     * }
     * </pre>
     */

    /**
     * 步骤2：验证短信验证码
     *
     * <p>接口：POST /auth/password/reset/verify</p>
     * <p>触发时机：用户输入完第6位验证码后自动触发</p>
     *
     * @param request 请求体
     * @return 验证结果
     */
    @PostMapping("/reset/verify")
    @Operation(summary = "验证验证码", description = "忘记密码流程 - 步骤2：验证短信验证码")
    public R<Void> verifyCode(@RequestBody @Valid VerifyCodeDto request) {
        String mobile = request.getMobile();
        String countryCode = request.getCountryCode();
        String code = request.getVerificationCode();

        log.info("验证验证码：mobile={}, countryCode={}", mobile, countryCode);

        // 1. 检查用户是否存在
        boolean exists = remoteAppUserService.existsByMobile(mobile, countryCode);
        if (!exists) {
            log.warn("用户不存在：mobile={}", mobile);
            return R.fail("该手机号未注册");
        }

        // 2. 从 Redis 获取验证码
        String smsCodeKey = SMS_CODE_KEY + mobile;
        String cachedCode = RedisUtils.getCacheObject(smsCodeKey);

        if (StringUtils.isEmpty(cachedCode)) {
            log.warn("验证码已过期：mobile={}", mobile);
            return R.fail("验证码已过期，请重新获取");
        }

        // 3. 验证验证码
        if (!StringUtils.equals(code, cachedCode)) {
            log.warn("验证码错误：mobile={}, inputCode={}, cachedCode={}", mobile, code, cachedCode);
            return R.fail("验证码错误，请重新输入");
        }

        // 4. 验证成功，标记为已验证（保存10分钟，用于步骤3验证）
        String verifiedKey = VERIFIED_CODE_KEY + mobile;
        RedisUtils.setCacheObject(verifiedKey, code, Duration.ofMinutes(10));

        // 5. 删除原始验证码（一次性使用）
        RedisUtils.deleteObject(smsCodeKey);

        log.info("验证码验证成功：mobile={}", mobile);
        return R.ok("验证成功");
    }

    /**
     * 步骤3：设置新密码（重置密码）
     *
     * <p>接口：POST /auth/password/reset/confirm</p>
     * <p>触发时机：用户点击"确认"按钮</p>
     *
     * @param request 请求体
     * @return 重置结果
     */
    @PostMapping("/reset/confirm")
    @Operation(summary = "重置密码", description = "忘记密码流程 - 步骤3：设置新密码")
    public R<Void> resetPassword(@RequestBody @Valid ResetPasswordDto request) {
        String mobile = request.getMobile();
        String countryCode = request.getCountryCode();
        String code = request.getVerificationCode();
        String newPassword = request.getNewPassword();

        log.info("重置密码：mobile={}, countryCode={}", mobile, countryCode);

        // 1. 验证验证码是否已在步骤2中验证过
        String verifiedKey = VERIFIED_CODE_KEY + mobile;
        String verifiedCode = RedisUtils.getCacheObject(verifiedKey);

        if (StringUtils.isEmpty(verifiedCode)) {
            log.warn("验证码未验证或已过期：mobile={}", mobile);
            return R.fail("验证码已过期，请重新获取");
        }

        if (!StringUtils.equals(code, verifiedCode)) {
            log.warn("验证码不匹配：mobile={}, inputCode={}, verifiedCode={}", mobile, code, verifiedCode);
            return R.fail("验证码错误");
        }

        // 2. 校验新密码格式
        if (!isValidPassword(newPassword)) {
            log.warn("密码格式不正确：mobile={}", mobile);
            return R.fail("密码格式不正确（6-20位，不可纯数字）");
        }

        // 3. 重置密码
        try {
            boolean success = remoteAppUserService.resetPassword(mobile, countryCode, newPassword);

            if (success) {
                // 4. 清除已验证标记
                RedisUtils.deleteObject(verifiedKey);

                log.info("密码重置成功：mobile={}", mobile);
                return R.ok("密码重置成功");
            } else {
                log.error("密码重置失败：mobile={}", mobile);
                return R.fail("密码重置失败");
            }
        } catch (Exception e) {
            log.error("密码重置异常：mobile={}, error={}", mobile, e.getMessage(), e);
            return R.fail("密码重置失败：" + e.getMessage());
        }
    }

    /**
     * 校验密码格式
     *
     * @param password 密码
     * @return true=格式正确，false=格式不正确
     */
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 6 || password.length() > 20) {
            return false;
        }

        // 不可纯数字
        return !password.matches("^\\d+$");
    }
}
