package org.dromara.appbff.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.appbff.domain.dto.SubmitOrderDTO;
import org.dromara.appbff.domain.vo.OrderConfirmPreviewVO;
import org.dromara.appbff.domain.vo.OrderSubmitResultVO;
import org.dromara.appbff.domain.vo.UserBalanceVO;
import org.dromara.appbff.service.OrderConfirmService;
import org.dromara.appuser.api.RemoteAppUserService;
import org.dromara.appuser.api.domain.vo.SkillServiceDetailVo;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.order.api.RemoteOrderService;
import org.dromara.order.api.domain.CreateOrderRequest;
import org.dromara.order.api.domain.CreateOrderResult;
import org.dromara.payment.api.RemotePaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * 订单确认服务实现
 *
 * <p>聚合调用：xypai-user(服务详情) + xypai-payment(余额/密码) + xypai-order(创建订单)</p>
 *
 * @author XyPai Team
 * @date 2025-12-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderConfirmServiceImpl implements OrderConfirmService {

    @DubboReference
    private RemoteAppUserService remoteAppUserService;

    @DubboReference
    private RemotePaymentService remotePaymentService;

    @DubboReference
    private RemoteOrderService remoteOrderService;

    @Override
    public OrderConfirmPreviewVO getOrderConfirmPreview(Long serviceId, Integer quantity, Long userId) {
        log.info("获取订单确认预览: serviceId={}, quantity={}, userId={}", serviceId, quantity, userId);

        try {
            // 1. 获取服务详情
            SkillServiceDetailVo serviceDetail = remoteAppUserService.getSkillServiceDetail(serviceId, userId);
            if (serviceDetail == null) {
                log.warn("服务不存在: serviceId={}", serviceId);
                return null;
            }

            // 2. 获取用户余额和支付密码状态
            BigDecimal userBalance = remotePaymentService.getBalance(userId);
            boolean hasPaymentPassword = remotePaymentService.hasPaymentPassword(userId);

            // 3. 计算价格预览
            int qty = quantity != null && quantity > 0 ? quantity : 1;
            BigDecimal unitPrice = serviceDetail.getPrice() != null ? serviceDetail.getPrice().getAmount() : BigDecimal.ZERO;
            BigDecimal total = unitPrice.multiply(new BigDecimal(qty));

            // 4. 构建响应
            return OrderConfirmPreviewVO.builder()
                .provider(buildProviderInfo(serviceDetail))
                .service(buildServiceInfo(serviceDetail))
                .price(OrderConfirmPreviewVO.PriceInfo.builder()
                    .unitPrice(unitPrice)
                    .unit(serviceDetail.getPrice() != null ? serviceDetail.getPrice().getUnit() : "局")
                    .displayText(unitPrice + "向娱币/" + (serviceDetail.getPrice() != null ? serviceDetail.getPrice().getUnit() : "局"))
                    .build())
                .quantityOptions(OrderConfirmPreviewVO.QuantityOptions.builder()
                    .min(1)
                    .max(99)
                    .defaultValue(1)
                    .build())
                .preview(OrderConfirmPreviewVO.PricePreview.builder()
                    .quantity(qty)
                    .subtotal(total)
                    .total(total)
                    .build())
                .userBalance(userBalance)
                .hasPaymentPassword(hasPaymentPassword)
                .build();

        } catch (Exception e) {
            log.error("获取订单确认预览失败: serviceId={}, error={}", serviceId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public OrderConfirmPreviewVO.PricePreview updatePricePreview(Long serviceId, Integer quantity) {
        log.info("更新价格预览: serviceId={}, quantity={}", serviceId, quantity);

        try {
            // 获取服务详情（只取价格）
            SkillServiceDetailVo serviceDetail = remoteAppUserService.getSkillServiceDetail(serviceId, null);
            if (serviceDetail == null) {
                return null;
            }

            BigDecimal unitPrice = serviceDetail.getPrice() != null ? serviceDetail.getPrice().getAmount() : BigDecimal.ZERO;
            int qty = quantity != null && quantity > 0 ? quantity : 1;
            BigDecimal total = unitPrice.multiply(new BigDecimal(qty));

            return OrderConfirmPreviewVO.PricePreview.builder()
                .quantity(qty)
                .subtotal(total)
                .total(total)
                .build();

        } catch (Exception e) {
            log.error("更新价格预览失败: serviceId={}, error={}", serviceId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public OrderSubmitResultVO submitOrder(SubmitOrderDTO dto, Long userId) {
        log.info("提交订单: serviceId={}, quantity={}, userId={}", dto.getServiceId(), dto.getQuantity(), userId);

        try {
            // 1. 验证支付密码
            boolean passwordValid = remotePaymentService.verifyPaymentPassword(userId, dto.getPaymentPassword());
            if (!passwordValid) {
                log.warn("支付密码错误: userId={}", userId);
                return OrderSubmitResultVO.fail("PASSWORD_ERROR", "支付密码错误");
            }

            // 2. 获取服务详情（验证价格）
            SkillServiceDetailVo serviceDetail = remoteAppUserService.getSkillServiceDetail(dto.getServiceId(), userId);
            if (serviceDetail == null) {
                return OrderSubmitResultVO.fail("SERVICE_NOT_FOUND", "服务不存在");
            }

            // 3. 验证金额
            BigDecimal unitPrice = serviceDetail.getPrice() != null ? serviceDetail.getPrice().getAmount() : BigDecimal.ZERO;
            BigDecimal expectedTotal = unitPrice.multiply(new BigDecimal(dto.getQuantity()));
            if (expectedTotal.compareTo(dto.getTotalAmount()) != 0) {
                log.warn("金额验证失败: expected={}, actual={}", expectedTotal, dto.getTotalAmount());
                return OrderSubmitResultVO.fail("AMOUNT_MISMATCH", "金额验证失败，请刷新页面重试");
            }

            // 4. 检查余额
            BigDecimal userBalance = remotePaymentService.getBalance(userId);
            if (userBalance.compareTo(dto.getTotalAmount()) < 0) {
                return OrderSubmitResultVO.fail("INSUFFICIENT_BALANCE", "余额不足");
            }

            // 5. 创建订单并支付
            Long providerId = serviceDetail.getProvider() != null ? serviceDetail.getProvider().getUserId() : null;
            CreateOrderRequest orderRequest = CreateOrderRequest.builder()
                .userId(userId)
                .serviceId(dto.getServiceId())
                .providerId(providerId)
                .quantity(dto.getQuantity())
                .unitPrice(unitPrice)
                .totalAmount(dto.getTotalAmount())
                .paymentMethod("balance")
                .remark(dto.getRemark())
                .build();

            CreateOrderResult orderResult = remoteOrderService.createOrder(orderRequest);

            // 6. 处理结果
            if (orderResult == null || !Boolean.TRUE.equals(orderResult.getSuccess())) {
                String errorCode = orderResult != null ? orderResult.getErrorCode() : "ORDER_FAILED";
                String errorMsg = orderResult != null ? orderResult.getErrorMessage() : "创建订单失败";
                return OrderSubmitResultVO.fail(errorCode, errorMsg);
            }

            // 7. 获取剩余余额
            BigDecimal remainingBalance = remotePaymentService.getBalance(userId);

            log.info("订单提交成功: orderId={}, orderNo={}", orderResult.getOrderId(), orderResult.getOrderNo());
            return OrderSubmitResultVO.success(
                orderResult.getOrderId(),
                orderResult.getOrderNo(),
                orderResult.getAmount(),
                remainingBalance
            );

        } catch (ServiceException e) {
            log.error("提交订单失败(业务异常): userId={}, error={}", userId, e.getMessage());
            return OrderSubmitResultVO.fail("BIZ_ERROR", e.getMessage());
        } catch (Exception e) {
            log.error("提交订单失败(系统异常): userId={}", userId, e);
            return OrderSubmitResultVO.fail("SYSTEM_ERROR", "系统繁忙，请稍后重试");
        }
    }

    @Override
    public UserBalanceVO getUserBalance(Long userId) {
        log.info("获取用户余额: userId={}", userId);

        try {
            BigDecimal balance = remotePaymentService.getBalance(userId);
            boolean hasPassword = remotePaymentService.hasPaymentPassword(userId);

            return UserBalanceVO.builder()
                .availableBalance(balance)
                .hasPaymentPassword(hasPassword)
                .build();
        } catch (Exception e) {
            log.error("获取用户余额失败: userId={}, error={}", userId, e.getMessage(), e);
            return UserBalanceVO.builder()
                .availableBalance(BigDecimal.ZERO)
                .hasPaymentPassword(false)
                .build();
        }
    }

    // ========== 辅助方法 ==========

    private OrderConfirmPreviewVO.ProviderInfo buildProviderInfo(SkillServiceDetailVo detail) {
        if (detail.getProvider() == null) {
            return null;
        }

        var provider = detail.getProvider();
        OrderConfirmPreviewVO.SkillInfo skillInfo = null;

        if (detail.getSkillInfo() != null) {
            skillInfo = OrderConfirmPreviewVO.SkillInfo.builder()
                .gameArea(detail.getSkillInfo().getGameArea())
                .rank(detail.getSkillInfo().getRank())
                .rankDisplay(detail.getSkillInfo().getRankDisplay())
                .build();
        }

        return OrderConfirmPreviewVO.ProviderInfo.builder()
            .userId(provider.getUserId())
            .nickname(provider.getNickname())
            .avatar(provider.getAvatar())
            .gender(provider.getGender())
            .age(provider.getAge())
            .isOnline(provider.getIsOnline())
            .isVerified(provider.getIsVerified())
            .tags(provider.getCertifications())
            .skillInfo(skillInfo)
            .build();
    }

    private OrderConfirmPreviewVO.ServiceInfo buildServiceInfo(SkillServiceDetailVo detail) {
        String skillType = detail.getSkillInfo() != null ? detail.getSkillInfo().getSkillType() : "";
        String skillLabel = detail.getSkillInfo() != null ? detail.getSkillInfo().getSkillLabel() : "";

        return OrderConfirmPreviewVO.ServiceInfo.builder()
            .serviceId(detail.getSkillId())
            .name(skillLabel)
            .icon(detail.getBannerImage())
            .skillType(skillType)
            .build();
    }
}
