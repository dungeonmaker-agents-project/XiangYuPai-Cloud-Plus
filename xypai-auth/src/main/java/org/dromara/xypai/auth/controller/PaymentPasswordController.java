package org.dromara.xypai.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.appuser.api.RemoteAppUserService;
import org.dromara.xypai.auth.domain.dto.SetPaymentPasswordDto;
import org.dromara.xypai.auth.domain.dto.UpdatePaymentPasswordDto;
import org.dromara.xypai.auth.domain.dto.VerifyPaymentPasswordDto;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 支付密码管理控制器
 *
 * <p>功能：</p>
 * <ul>
 *     <li>设置支付密码（首次）</li>
 *     <li>修改支付密码</li>
 *     <li>验证支付密码（支付时使用）</li>
 * </ul>
 *
 * <p>注意：</p>
 * <ul>
 *     <li>所有接口需要登录认证（携带Token）</li>
 *     <li>支付密码必须为6位数字</li>
 *     <li>错误5次后锁定30分钟（由user服务实现）</li>
 * </ul>
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth/payment-password")
@Tag(name = "支付密码管理", description = "支付密码设置、修改、验证")
public class PaymentPasswordController {

    @DubboReference
    private final RemoteAppUserService remoteAppUserService;

    /**
     * 设置支付密码（首次设置）
     *
     * <p>请求示例：</p>
     * <pre>
     * POST /auth/payment-password/set
     * Headers: Authorization: Bearer <token>
     * {
     *   "paymentPassword": "123456",
     *   "confirmPassword": "123456"
     * }
     * </pre>
     *
     * <p>响应示例：</p>
     * <pre>
     * {
     *   "code": 200,
     *   "message": "支付密码设置成功"
     * }
     * </pre>
     *
     * @param dto 设置请求
     * @return 结果
     */
    @PostMapping("/set")
    @Operation(summary = "设置支付密码", description = "首次设置支付密码（需登录）")
    public R<Void> setPaymentPassword(@RequestBody @Valid SetPaymentPasswordDto dto) {
        // 1. 获取当前登录用户ID
        Long userId = LoginHelper.getUserId();
        log.info("设置支付密码：userId={}", userId);

        // 2. 校验两次密码是否一致
        if (!StringUtils.equals(dto.getPaymentPassword(), dto.getConfirmPassword())) {
            log.warn("两次密码不一致：userId={}", userId);
            return R.fail("两次输入的密码不一致");
        }

        // 3. 调用远程服务设置支付密码
        try {
            boolean success = remoteAppUserService.setPaymentPassword(userId, dto.getPaymentPassword());

            if (success) {
                log.info("支付密码设置成功：userId={}", userId);
                return R.ok("支付密码设置成功");
            } else {
                log.error("支付密码设置失败：userId={}", userId);
                return R.fail("支付密码设置失败");
            }
        } catch (UserException e) {
            log.error("设置支付密码异常：userId={}, error={}", userId, e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * 修改支付密码
     *
     * <p>请求示例：</p>
     * <pre>
     * POST /auth/payment-password/update
     * Headers: Authorization: Bearer <token>
     * {
     *   "oldPaymentPassword": "123456",
     *   "newPaymentPassword": "654321",
     *   "confirmPassword": "654321"
     * }
     * </pre>
     *
     * @param dto 修改请求
     * @return 结果
     */
    @PostMapping("/update")
    @Operation(summary = "修改支付密码", description = "修改已有支付密码（需登录）")
    public R<Void> updatePaymentPassword(@RequestBody @Valid UpdatePaymentPasswordDto dto) {
        // 1. 获取当前登录用户ID
        Long userId = LoginHelper.getUserId();
        log.info("修改支付密码：userId={}", userId);

        // 2. 校验新密码和确认密码是否一致
        if (!StringUtils.equals(dto.getNewPaymentPassword(), dto.getConfirmPassword())) {
            log.warn("两次新密码不一致：userId={}", userId);
            return R.fail("两次输入的新密码不一致");
        }

        // 3. 校验新密码不能与旧密码相同
        if (StringUtils.equals(dto.getOldPaymentPassword(), dto.getNewPaymentPassword())) {
            log.warn("新密码与旧密码相同：userId={}", userId);
            return R.fail("新密码不能与旧密码相同");
        }

        // 4. 调用远程服务修改支付密码
        try {
            boolean success = remoteAppUserService.updatePaymentPassword(
                userId,
                dto.getOldPaymentPassword(),
                dto.getNewPaymentPassword()
            );

            if (success) {
                log.info("支付密码修改成功：userId={}", userId);
                return R.ok("支付密码修改成功");
            } else {
                log.error("支付密码修改失败：userId={}", userId);
                return R.fail("支付密码修改失败");
            }
        } catch (UserException e) {
            log.error("修改支付密码异常：userId={}, error={}", userId, e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * 验证支付密码
     *
     * <p>用途：支付时验证支付密码是否正确</p>
     *
     * <p>请求示例：</p>
     * <pre>
     * POST /auth/payment-password/verify
     * Headers: Authorization: Bearer <token>
     * {
     *   "paymentPassword": "123456"
     * }
     * </pre>
     *
     * <p>响应示例：</p>
     * <pre>
     * {
     *   "code": 200,
     *   "message": "验证成功",
     *   "data": {
     *     "verified": true
     *   }
     * }
     * </pre>
     *
     * @param dto 验证请求
     * @return 验证结果
     */
    @PostMapping("/verify")
    @Operation(summary = "验证支付密码", description = "支付时验证支付密码（需登录）")
    public R<VerifyResult> verifyPaymentPassword(@RequestBody @Valid VerifyPaymentPasswordDto dto) {
        // 1. 获取当前登录用户ID
        Long userId = LoginHelper.getUserId();
        log.info("验证支付密码：userId={}", userId);

        // 2. 调用远程服务验证支付密码
        try {
            boolean verified = remoteAppUserService.verifyPaymentPassword(userId, dto.getPaymentPassword());

            if (verified) {
                log.info("支付密码验证成功：userId={}", userId);
                return R.ok(new VerifyResult(true));
            } else {
                log.warn("支付密码验证失败：userId={}", userId);
                return R.ok("支付密码错误", new VerifyResult(false));
            }
        } catch (UserException e) {
            log.error("验证支付密码异常：userId={}, error={}", userId, e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * 验证结果
     */
    public record VerifyResult(Boolean verified) {
    }
}
