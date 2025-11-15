package org.dromara.payment.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.ratelimiter.annotation.RateLimiter;
import org.dromara.common.ratelimiter.enums.LimitType;
import org.dromara.payment.domain.dto.ExecutePaymentDTO;
import org.dromara.payment.domain.dto.VerifyPasswordDTO;
import org.dromara.payment.domain.vo.BalanceVO;
import org.dromara.payment.domain.vo.PaymentMethodVO;
import org.dromara.payment.domain.vo.PaymentResultVO;
import org.dromara.payment.service.IAccountService;
import org.dromara.payment.service.IPaymentService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import cn.dev33.satoken.stp.StpUtil;

/**
 * 支付控制器
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
@Tag(name = "支付管理", description = "支付执行、密码验证、余额查询")
public class PaymentController {

    private final IPaymentService paymentService;
    private final IAccountService accountService;

    /**
     * 执行支付
     */
    @PostMapping("/pay")
    @SaCheckLogin
    @Operation(summary = "执行支付", description = "执行订单支付（余额/支付宝/微信）")
    @Log(title = "执行支付", businessType = BusinessType.INSERT)
    @RateLimiter(count = 10, time = 60, limitType = LimitType.USER)
    public R<PaymentResultVO> executePayment(@Valid @RequestBody ExecutePaymentDTO dto) {
        return R.ok(paymentService.executePayment(dto));
    }

    /**
     * 验证支付密码
     */
    @PostMapping("/verify")
    @SaCheckLogin
    @Operation(summary = "验证支付密码", description = "验证支付密码并完成支付")
    @Log(title = "验证支付密码", businessType = BusinessType.OTHER)
    @RateLimiter(count = 10, time = 60, limitType = LimitType.USER)
    public R<PaymentResultVO> verifyPassword(@Valid @RequestBody VerifyPasswordDTO dto) {
        return R.ok(paymentService.verifyPassword(dto));
    }

    /**
     * 获取支付方式列表
     */
    @GetMapping("/methods")
    @SaCheckLogin
    @Operation(summary = "获取支付方式", description = "获取可用的支付方式列表")
    @Log(title = "获取支付方式", businessType = BusinessType.OTHER)
    @RateLimiter(count = 30, time = 60, limitType = LimitType.USER)
    public R<PaymentMethodVO> getPaymentMethods() {
        return R.ok(paymentService.getPaymentMethods());
    }

    /**
     * 查询余额
     */
    @GetMapping("/balance")
    @SaCheckLogin
    @Operation(summary = "查询余额", description = "查询用户账户余额")
    @Log(title = "查询余额", businessType = BusinessType.OTHER)
    @RateLimiter(count = 30, time = 60, limitType = LimitType.USER)
    public R<BalanceVO> getBalance() {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(accountService.getBalance(userId));
    }
}
