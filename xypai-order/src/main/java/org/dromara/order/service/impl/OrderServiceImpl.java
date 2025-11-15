package org.dromara.order.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.order.domain.dto.*;
import org.dromara.order.domain.entity.Order;
import org.dromara.order.domain.vo.*;
import org.dromara.order.mapper.OrderMapper;
import org.dromara.order.service.IOrderService;
import org.dromara.payment.api.RemotePaymentService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 订单服务实现
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private final OrderMapper orderMapper;

    @DubboReference
    private RemotePaymentService remotePaymentService;

    /**
     * 服务费率（5%）
     */
    private static final BigDecimal SERVICE_FEE_RATE = new BigDecimal("0.05");

    /**
     * 订单缓存key前缀
     */
    private static final String ORDER_CACHE_KEY = "order:detail:";

    /**
     * 订单缓存时间（10分钟）
     */
    private static final Duration ORDER_CACHE_TTL = Duration.ofMinutes(10);

    /**
     * 自动取消时间（10分钟）
     */
    private static final Duration AUTO_CANCEL_DURATION = Duration.ofMinutes(10);

    @Override
    public OrderPreviewVO preview(OrderPreviewDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 默认数量为1
        Integer quantity = dto.getQuantity() != null ? dto.getQuantity() : 1;

        // TODO: 调用ContentService获取服务信息
        // 这里使用模拟数据
        Long providerId = 10001L;
        BigDecimal unitPrice = new BigDecimal("50.00");

        // 计算价格
        BigDecimal subtotal = unitPrice.multiply(new BigDecimal(quantity));
        BigDecimal serviceFee = subtotal.multiply(SERVICE_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(serviceFee);

        // 获取用户余额
        BigDecimal userBalance = remotePaymentService.getBalance(userId);

        // 构建返回数据
        return OrderPreviewVO.builder()
            .provider(buildProviderInfo(providerId))
            .service(buildServiceInfo(dto.getServiceId()))
            .price(buildPriceInfo(unitPrice))
            .quantityOptions(buildQuantityOptions())
            .preview(OrderPreviewVO.PreviewInfo.builder()
                .quantity(quantity)
                .subtotal(subtotal)
                .serviceFee(serviceFee)
                .total(total)
                .build())
            .userBalance(userBalance)
            .build();
    }

    @Override
    public OrderPreviewVO.PreviewInfo updatePreview(UpdateOrderPreviewDTO dto) {
        // TODO: 调用ContentService获取服务信息
        BigDecimal unitPrice = new BigDecimal("50.00");

        // 验证数量范围
        if (dto.getQuantity() < 1 || dto.getQuantity() > 100) {
            throw new ServiceException("数量必须在1-100之间");
        }

        // 计算价格
        BigDecimal subtotal = unitPrice.multiply(new BigDecimal(dto.getQuantity()));
        BigDecimal serviceFee = subtotal.multiply(SERVICE_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(serviceFee);

        return OrderPreviewVO.PreviewInfo.builder()
            .quantity(dto.getQuantity())
            .subtotal(subtotal)
            .serviceFee(serviceFee)
            .total(total)
            .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateResultVO createOrder(CreateOrderDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();

        // TODO: 调用ContentService获取服务信息
        Long providerId = 10001L;
        BigDecimal unitPrice = new BigDecimal("50.00");

        // 计算价格
        BigDecimal subtotal = unitPrice.multiply(new BigDecimal(dto.getQuantity()));
        BigDecimal serviceFee = subtotal.multiply(SERVICE_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(serviceFee);

        // 验证金额
        if (total.compareTo(dto.getTotalAmount()) != 0) {
            throw new ServiceException("订单金额验证失败");
        }

        // 生成订单号
        String orderNo = generateOrderNo();

        // 计算自动取消时间
        LocalDateTime autoCancelTime = LocalDateTime.now().plus(AUTO_CANCEL_DURATION);

        // 创建订单
        Order order = Order.builder()
            .orderNo(orderNo)
            .userId(userId)
            .providerId(providerId)
            .serviceId(dto.getServiceId())
            .orderType("service")
            .quantity(dto.getQuantity())
            .unitPrice(unitPrice)
            .subtotal(subtotal)
            .serviceFee(serviceFee)
            .totalAmount(total)
            .status("pending")
            .paymentStatus("pending")
            .autoCancelTime(autoCancelTime)
            .build();

        orderMapper.insert(order);

        // 获取用户余额
        BigDecimal userBalance = remotePaymentService.getBalance(userId);
        boolean sufficientBalance = userBalance.compareTo(total) >= 0;

        // 返回结果
        return OrderCreateResultVO.builder()
            .orderId(order.getId().toString())
            .orderNo(orderNo)
            .amount(total)
            .needPayment(true)
            .paymentInfo(OrderCreateResultVO.PaymentInfo.builder()
                .amount(total)
                .currency("coin")
                .userBalance(userBalance)
                .sufficientBalance(sufficientBalance)
                .build())
            .build();
    }

    @Override
    public OrderDetailVO getOrderDetail(String orderId) {
        // 先从缓存获取
        String cacheKey = ORDER_CACHE_KEY + orderId;
        OrderDetailVO cached = RedisUtils.getCacheObject(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 查询数据库
        Order order = orderMapper.selectById(Long.valueOf(orderId));
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        // 验证权限
        Long userId = StpUtil.getLoginIdAsLong();
        if (!order.getUserId().equals(userId) && !order.getProviderId().equals(userId)) {
            throw new ServiceException("无权查看此订单");
        }

        // 构建返回数据
        OrderDetailVO result = OrderDetailVO.builder()
            .orderId(order.getId().toString())
            .orderNo(order.getOrderNo())
            .status(order.getStatus())
            .amount(order.getTotalAmount())
            .createdAt(order.getCreatedAt())
            .autoCancelTime(order.getAutoCancelTime())
            .provider(buildProviderInfoForDetail(order.getProviderId()))
            .service(buildServiceInfoForDetail(order.getServiceId(), order.getQuantity()))
            .build();

        // 缓存结果
        RedisUtils.setCacheObject(cacheKey, result, ORDER_CACHE_TTL);

        return result;
    }

    @Override
    public OrderStatusVO getOrderStatus(String orderId) {
        Order order = orderMapper.selectById(Long.valueOf(orderId));
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        // 验证权限
        Long userId = StpUtil.getLoginIdAsLong();
        if (!order.getUserId().equals(userId) && !order.getProviderId().equals(userId)) {
            throw new ServiceException("无权查看此订单");
        }

        // 计算自动取消剩余时间
        OrderStatusVO.AutoCancelInfo autoCancelInfo = null;
        if ("pending".equals(order.getStatus()) && order.getAutoCancelTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            long remainingSeconds = Duration.between(now, order.getAutoCancelTime()).getSeconds();
            autoCancelInfo = OrderStatusVO.AutoCancelInfo.builder()
                .enabled(true)
                .cancelAt(order.getAutoCancelTime())
                .remainingSeconds(Math.max(0, remainingSeconds))
                .build();
        }

        // 构建可用操作
        List<OrderStatusVO.ActionInfo> actions = buildActions(order.getStatus());

        return OrderStatusVO.builder()
            .orderId(order.getId().toString())
            .orderNo(order.getOrderNo())
            .status(order.getStatus())
            .statusLabel(getStatusLabel(order.getStatus()))
            .provider(buildProviderInfoForStatus(order.getProviderId()))
            .service(buildServiceInfoForStatus(order.getServiceId(), order.getQuantity(), order.getUnitPrice()))
            .amount(order.getTotalAmount())
            .createdAt(order.getCreatedAt())
            .acceptedAt(order.getAcceptedTime())
            .completedAt(order.getCompletedTime())
            .cancelledAt(order.getCancelledTime())
            .autoCancel(autoCancelInfo)
            .actions(actions)
            .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCancelResultVO cancelOrder(CancelOrderDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();

        Order order = orderMapper.selectById(Long.valueOf(dto.getOrderId()));
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        // 验证权限
        if (!order.getUserId().equals(userId)) {
            throw new ServiceException("无权取消此订单");
        }

        // 只能取消pending状态的订单
        if (!"pending".equals(order.getStatus())) {
            throw new ServiceException("当前订单状态不允许取消");
        }

        // 更新订单状态
        order.setStatus("cancelled");
        order.setCancelledTime(LocalDateTime.now());
        order.setCancelReason(dto.getReason());

        // 如果已支付，需要退款
        BigDecimal refundAmount = BigDecimal.ZERO;
        if ("success".equals(order.getPaymentStatus())) {
            refundAmount = order.getTotalAmount();
            order.setRefundAmount(refundAmount);
            order.setRefundTime(LocalDateTime.now());

            // TODO: 调用支付服务退款
            // remotePaymentService.refundBalance(order.getOrderNo(), refundAmount, "订单取消");
        }

        orderMapper.updateById(order);

        // 清除缓存
        RedisUtils.deleteObject(ORDER_CACHE_KEY + dto.getOrderId());

        // 获取用户当前余额
        BigDecimal balance = remotePaymentService.getBalance(userId);

        return OrderCancelResultVO.builder()
            .orderId(order.getId().toString())
            .status("cancelled")
            .refundAmount(refundAmount)
            .refundTime(order.getRefundTime())
            .balance(balance)
            .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderStatus(Long orderId, String orderNo, String status, String paymentStatus, String paymentMethod) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        order.setStatus(status);
        order.setPaymentStatus(paymentStatus);
        order.setPaymentMethod(paymentMethod);

        if ("success".equals(paymentStatus)) {
            order.setPaymentTime(LocalDateTime.now());
        }

        int result = orderMapper.updateById(order);

        // 清除缓存
        RedisUtils.deleteObject(ORDER_CACHE_KEY + orderId);

        return result > 0;
    }

    @Override
    public Long getOrderCount(Long userId, Long providerId, String status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            wrapper.eq(Order::getUserId, userId);
        }
        if (providerId != null) {
            wrapper.eq(Order::getProviderId, providerId);
        }
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }

        return orderMapper.selectCount(wrapper);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        int random = new Random().nextInt(9000) + 1000;
        return timestamp + random;
    }

    /**
     * 构建服务提供者信息
     */
    private OrderPreviewVO.ProviderInfo buildProviderInfo(Long providerId) {
        // TODO: 调用UserService获取用户信息
        return OrderPreviewVO.ProviderInfo.builder()
            .userId(providerId)
            .avatar("https://example.com/avatar.jpg")
            .nickname("测试用户")
            .gender("male")
            .age(25)
            .tags(Arrays.asList("专业", "靠谱"))
            .skillInfo(OrderPreviewVO.SkillInfo.builder()
                .gameArea("峡谷之巅")
                .rank("王者")
                .rankDisplay("王者100星")
                .build())
            .build();
    }

    /**
     * 构建服务信息
     */
    private OrderPreviewVO.ServiceInfo buildServiceInfo(Long serviceId) {
        // TODO: 调用ContentService获取服务信息
        return OrderPreviewVO.ServiceInfo.builder()
            .serviceId(serviceId)
            .name("王者荣耀陪玩")
            .icon("https://example.com/service-icon.jpg")
            .build();
    }

    /**
     * 构建价格信息
     */
    private OrderPreviewVO.PriceInfo buildPriceInfo(BigDecimal unitPrice) {
        return OrderPreviewVO.PriceInfo.builder()
            .unitPrice(unitPrice)
            .unit("局")
            .displayText(unitPrice + "向娱币/局")
            .build();
    }

    /**
     * 构建数量选项
     */
    private OrderPreviewVO.QuantityOptions buildQuantityOptions() {
        return OrderPreviewVO.QuantityOptions.builder()
            .min(1)
            .max(100)
            .defaultValue(1)
            .build();
    }

    /**
     * 构建服务提供者信息（详情页）
     */
    private OrderDetailVO.ProviderInfo buildProviderInfoForDetail(Long providerId) {
        // TODO: 调用UserService获取用户信息
        return OrderDetailVO.ProviderInfo.builder()
            .userId(providerId)
            .nickname("测试用户")
            .avatar("https://example.com/avatar.jpg")
            .build();
    }

    /**
     * 构建服务信息（详情页）
     */
    private OrderDetailVO.ServiceInfo buildServiceInfoForDetail(Long serviceId, Integer quantity) {
        // TODO: 调用ContentService获取服务信息
        return OrderDetailVO.ServiceInfo.builder()
            .name("王者荣耀陪玩")
            .quantity(quantity)
            .build();
    }

    /**
     * 构建服务提供者信息（状态页）
     */
    private OrderStatusVO.ProviderInfo buildProviderInfoForStatus(Long providerId) {
        // TODO: 调用UserService获取用户信息
        return OrderStatusVO.ProviderInfo.builder()
            .userId(providerId)
            .nickname("测试用户")
            .avatar("https://example.com/avatar.jpg")
            .isOnline(true)
            .build();
    }

    /**
     * 构建服务信息（状态页）
     */
    private OrderStatusVO.ServiceInfo buildServiceInfoForStatus(Long serviceId, Integer quantity, BigDecimal unitPrice) {
        // TODO: 调用ContentService获取服务信息
        return OrderStatusVO.ServiceInfo.builder()
            .name("王者荣耀陪玩")
            .quantity(quantity)
            .unitPrice(unitPrice)
            .build();
    }

    /**
     * 获取状态标签
     */
    private String getStatusLabel(String status) {
        return switch (status) {
            case "pending" -> "待接单";
            case "accepted" -> "已接单";
            case "in_progress" -> "进行中";
            case "completed" -> "已完成";
            case "cancelled" -> "已取消";
            case "refunded" -> "已退款";
            default -> "未知";
        };
    }

    /**
     * 构建可用操作
     */
    private List<OrderStatusVO.ActionInfo> buildActions(String status) {
        List<OrderStatusVO.ActionInfo> actions = new ArrayList<>();

        if ("pending".equals(status)) {
            actions.add(OrderStatusVO.ActionInfo.builder()
                .action("cancel")
                .label("取消订单")
                .enabled(true)
                .build());
        }

        actions.add(OrderStatusVO.ActionInfo.builder()
            .action("contact")
            .label("联系TA")
            .enabled(true)
            .build());

        if ("completed".equals(status)) {
            actions.add(OrderStatusVO.ActionInfo.builder()
                .action("rate")
                .label("评价")
                .enabled(true)
                .build());
        }

        return actions;
    }
}
