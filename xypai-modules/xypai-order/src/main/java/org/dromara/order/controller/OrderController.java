package org.dromara.order.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.ratelimiter.annotation.RateLimiter;
import org.dromara.common.ratelimiter.enums.LimitType;
import org.dromara.order.domain.dto.*;
import org.dromara.order.domain.vo.*;
import org.dromara.order.service.IOrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
@Tag(name = "订单管理", description = "订单预览、创建、查询、取消")
public class OrderController {

    private final IOrderService orderService;

    /**
     * 订单预览
     */
    @GetMapping("/preview")
    @SaCheckLogin
    @Operation(summary = "订单预览", description = "获取订单预览信息，包含价格计算和用户余额")
    @Log(title = "订单预览", businessType = BusinessType.OTHER)
    @RateLimiter(count = 20, time = 60, limitType = LimitType.USER)
    public R<OrderPreviewVO> preview(
        @Parameter(description = "服务ID", required = true) @RequestParam Long serviceId,
        @Parameter(description = "数量，默认为1") @RequestParam(required = false) Integer quantity
    ) {
        OrderPreviewDTO dto = OrderPreviewDTO.builder()
            .serviceId(serviceId)
            .quantity(quantity)
            .build();
        return R.ok(orderService.preview(dto));
    }

    /**
     * 更新订单预览
     */
    @PostMapping("/preview/update")
    @SaCheckLogin
    @Operation(summary = "更新订单预览", description = "数量变化时重新计算价格")
    @Log(title = "更新订单预览", businessType = BusinessType.OTHER)
    @RateLimiter(count = 30, time = 60, limitType = LimitType.USER)
    public R<OrderPreviewVO.PreviewInfo> updatePreview(@Valid @RequestBody UpdateOrderPreviewDTO dto) {
        return R.ok(orderService.updatePreview(dto));
    }

    /**
     * 创建订单
     */
    @PostMapping("/create")
    @SaCheckLogin
    @Operation(summary = "创建订单", description = "创建订单并返回支付信息")
    @Log(title = "创建订单", businessType = BusinessType.INSERT)
    @RateLimiter(count = 10, time = 60, limitType = LimitType.USER)
    public R<OrderCreateResultVO> createOrder(@Valid @RequestBody CreateOrderDTO dto) {
        return R.ok(orderService.createOrder(dto));
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/detail")
    @SaCheckLogin
    @Operation(summary = "获取订单详情", description = "查询订单详细信息")
    @Log(title = "查询订单详情", businessType = BusinessType.OTHER)
    @RateLimiter(count = 30, time = 60, limitType = LimitType.USER)
    public R<OrderDetailVO> getOrderDetail(
        @Parameter(description = "订单ID", required = true) @RequestParam String orderId
    ) {
        return R.ok(orderService.getOrderDetail(orderId));
    }

    /**
     * 获取订单状态
     */
    @GetMapping("/status")
    @SaCheckLogin
    @Operation(summary = "获取订单状态", description = "查询订单状态和可用操作")
    @Log(title = "查询订单状态", businessType = BusinessType.OTHER)
    @RateLimiter(count = 30, time = 60, limitType = LimitType.USER)
    public R<OrderStatusVO> getOrderStatus(
        @Parameter(description = "订单ID", required = true) @RequestParam String orderId
    ) {
        return R.ok(orderService.getOrderStatus(orderId));
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel")
    @SaCheckLogin
    @Operation(summary = "取消订单", description = "取消订单并退款")
    @Log(title = "取消订单", businessType = BusinessType.UPDATE)
    @RateLimiter(count = 10, time = 60, limitType = LimitType.USER)
    public R<OrderCancelResultVO> cancelOrder(@Valid @RequestBody CancelOrderDTO dto) {
        return R.ok(orderService.cancelOrder(dto));
    }
}
