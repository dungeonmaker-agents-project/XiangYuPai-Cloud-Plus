package org.dromara.appbff.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.appbff.domain.dto.SubmitOrderDTO;
import org.dromara.appbff.domain.vo.OrderConfirmPreviewVO;
import org.dromara.appbff.domain.vo.OrderSubmitResultVO;
import org.dromara.appbff.domain.vo.UserBalanceVO;
import org.dromara.appbff.service.OrderConfirmService;
import org.dromara.common.core.domain.R;
import org.springframework.web.bind.annotation.*;

/**
 * 订单确认控制器
 *
 * <p>对应前端：OrderConfirmPage 订单确认页</p>
 * <p>API前缀：/api/app/order</p>
 *
 * @author XyPai Team
 * @date 2025-12-04
 */
@Slf4j
@RestController
@RequestMapping("/api/app/order")
@RequiredArgsConstructor
@Tag(name = "订单确认", description = "订单确认支付相关接口")
public class OrderConfirmController {

    private final OrderConfirmService orderConfirmService;

    /**
     * 获取订单确认预览
     *
     * <p>场景：用户从服务详情页点击"下单"后调用</p>
     */
    @GetMapping("/confirm-preview")
    @Operation(summary = "获取订单确认预览", description = "获取服务详情、价格信息和用户余额")
    public R<OrderConfirmPreviewVO> getConfirmPreview(
        @Parameter(description = "服务ID", required = true) @RequestParam Long serviceId,
        @Parameter(description = "初始数量") @RequestParam(required = false, defaultValue = "1") Integer quantity
    ) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("获取订单确认预览: serviceId={}, quantity={}, userId={}", serviceId, quantity, userId);

        OrderConfirmPreviewVO result = orderConfirmService.getOrderConfirmPreview(serviceId, quantity, userId);
        if (result == null) {
            return R.fail("服务不存在或已下架");
        }
        return R.ok(result);
    }

    /**
     * 更新价格预览
     *
     * <p>场景：用户调整数量后实时更新价格</p>
     */
    @GetMapping("/preview-update")
    @Operation(summary = "更新价格预览", description = "根据数量变化更新总价")
    public R<OrderConfirmPreviewVO.PricePreview> updatePricePreview(
        @Parameter(description = "服务ID", required = true) @RequestParam Long serviceId,
        @Parameter(description = "数量", required = true) @RequestParam Integer quantity
    ) {
        log.info("更新价格预览: serviceId={}, quantity={}", serviceId, quantity);

        OrderConfirmPreviewVO.PricePreview result = orderConfirmService.updatePricePreview(serviceId, quantity);
        if (result == null) {
            return R.fail("服务不存在");
        }
        return R.ok(result);
    }

    /**
     * 提交订单并支付
     *
     * <p>场景：用户确认信息后输入支付密码提交</p>
     * <p>流程：验证密码 → 创建订单 → 扣款 → 返回结果</p>
     */
    @PostMapping("/submit-with-payment")
    @Operation(summary = "提交订单并支付", description = "验证密码后创建订单并完成余额支付")
    public R<OrderSubmitResultVO> submitOrder(@Valid @RequestBody SubmitOrderDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("提交订单: serviceId={}, quantity={}, userId={}", dto.getServiceId(), dto.getQuantity(), userId);

        OrderSubmitResultVO result = orderConfirmService.submitOrder(dto, userId);
        // 无论成功失败都返回 result，前端通过 result.success 判断
        return R.ok(result);
    }

    /**
     * 获取用户余额
     */
    @GetMapping("/balance")
    @Operation(summary = "获取用户余额", description = "获取当前用户的可用余额和支付密码状态")
    public R<UserBalanceVO> getUserBalance() {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("获取用户余额: userId={}", userId);

        UserBalanceVO result = orderConfirmService.getUserBalance(userId);
        return R.ok(result);
    }
}
