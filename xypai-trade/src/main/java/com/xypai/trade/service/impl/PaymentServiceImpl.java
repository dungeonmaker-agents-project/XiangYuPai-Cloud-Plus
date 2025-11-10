package com.xypai.trade.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import com.xypai.trade.domain.dto.PaymentDTO;
import com.xypai.trade.domain.dto.RefundDTO;
import com.xypai.trade.domain.entity.ServiceOrder;
import com.xypai.trade.domain.vo.PaymentResultVO;
import com.xypai.trade.mapper.ServiceOrderMapper;
import com.xypai.trade.service.IOrderService;
import com.xypai.trade.service.IPaymentService;
import com.xypai.trade.service.IWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 支付服务实现类
 *
 * @author xypai
 * @date 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements IPaymentService {

    private final ServiceOrderMapper serviceOrderMapper;
    private final IOrderService orderService;
    private final IWalletService walletService;  // 注入钱包服务

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResultVO createPayment(PaymentDTO paymentDTO) {
        // 验证订单
        ServiceOrder order = validatePaymentOrder(paymentDTO.getOrderId());
        
        // 根据支付方式处理
        switch (paymentDTO.getPaymentMethod().toLowerCase()) {
            case "wallet":
                return walletPay(paymentDTO.getOrderId(), paymentDTO.getPaymentPassword());
            case "wechat":
                return wechatPay(paymentDTO.getOrderId(), paymentDTO.getClientIp(), paymentDTO.getNotifyUrl());
            case "alipay":
                return alipayPay(paymentDTO.getOrderId(), paymentDTO.getClientIp(), 
                               paymentDTO.getNotifyUrl(), paymentDTO.getReturnUrl());
            default:
                throw new ServiceException("不支持的支付方式：" + paymentDTO.getPaymentMethod());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResultVO walletPay(Long orderId, String paymentPassword) {
        ServiceOrder order = validatePaymentOrder(orderId);
        Long currentUserId = LoginHelper.getUserId();
        
        if (currentUserId == null || !currentUserId.equals(order.getBuyerId())) {
            throw new ServiceException("无权限支付该订单");
        }

        // 1. 验证支付密码
        if (!validatePaymentPassword(currentUserId, paymentPassword)) {
            recordPaymentLog(orderId, "wallet", "PAY_FAILED", "支付密码错误", "FAILED");
            return PaymentResultVO.builder()
                    .orderId(orderId)
                    .orderNo(order.getOrderNo())
                    .paymentStatus("failed")
                    .paymentMethod("wallet")
                    .errorCode("INVALID_PASSWORD")
                    .errorMessage("支付密码错误")
                    .build();
        }

        // 2. 检查钱包余额
        Long paymentAmount = order.getActualAmount() != null && order.getActualAmount() > 0 ? 
                            order.getActualAmount() : order.getAmount();
        
        if (!walletService.hasEnoughBalance(currentUserId, paymentAmount)) {
            recordPaymentLog(orderId, "wallet", "PAY_FAILED", "余额不足", "FAILED");
            return PaymentResultVO.builder()
                    .orderId(orderId)
                    .orderNo(order.getOrderNo())
                    .paymentStatus("failed")
                    .paymentMethod("wallet")
                    .errorCode("INSUFFICIENT_BALANCE")
                    .errorMessage("钱包余额不足，需要：¥" + (paymentAmount / 100.0))
                    .build();
        }

        // 3. 从钱包扣款（乐观锁，自动重试3次）
        boolean deductSuccess = walletService.deductBalance(
                currentUserId, 
                paymentAmount, 
                "order", 
                orderId, 
                "订单支付：" + order.getServiceName()
        );
        
        if (!deductSuccess) {
            recordPaymentLog(orderId, "wallet", "PAY_FAILED", "扣款失败（并发冲突或余额不足）", "FAILED");
            return PaymentResultVO.builder()
                    .orderId(orderId)
                    .orderNo(order.getOrderNo())
                    .paymentStatus("failed")
                    .paymentMethod("wallet")
                    .errorCode("DEDUCT_FAILED")
                    .errorMessage("扣款失败，请重试")
                    .build();
        }

        // 4. 生成支付流水号
        String paymentNo = generatePaymentNo(orderId, "wallet");
        LocalDateTime paymentTime = LocalDateTime.now();
        
        // 5. 更新订单支付信息
        ServiceOrder updateOrder = ServiceOrder.builder()
                .id(orderId)
                .status(ServiceOrder.Status.PAID.getCode())
                .paymentMethod("wallet")
                .paymentTime(paymentTime)
                .updatedAt(paymentTime)
                .build();

        int result = serviceOrderMapper.updateById(updateOrder);
        if (result <= 0) {
            // 更新失败，退款
            walletService.rechargeBalance(currentUserId, paymentAmount, "refund", orderId, "订单更新失败退款");
            throw new ServiceException("支付处理失败，已退款");
        }

        // 6. 卖家收款（扣除平台服务费）
        Long sellerIncome = paymentAmount - (order.getPlatformFee() != null ? order.getPlatformFee() : 0L);
        if (sellerIncome > 0) {
            walletService.addIncome(
                    order.getSellerId(), 
                    sellerIncome, 
                    "order", 
                    orderId, 
                    "订单收入：" + order.getServiceName()
            );
        }

        // 7. 记录支付日志
        recordPaymentLog(orderId, "wallet", "PAY_SUCCESS", 
                        String.format("钱包支付成功，金额：%.2f元", paymentAmount / 100.0), "SUCCESS");
        
        orderService.recordOrderLog(orderId, "PAYMENT", "支付成功", currentUserId, 
                                   String.format("钱包支付成功，流水号：%s", paymentNo));

        log.info("✓ 钱包支付成功 [乐观锁扣款]，订单号：{}，买家：{}，金额：{}元，流水号：{}", 
                order.getOrderNo(), currentUserId, paymentAmount / 100.0, paymentNo);

        return PaymentResultVO.builder()
                .orderId(orderId)
                .orderNo(order.getOrderNo())
                .paymentStatus("success")
                .paymentMethod("wallet")
                .paymentAmount(BigDecimal.valueOf(paymentAmount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                .paymentNo(paymentNo)
                .paymentTime(paymentTime)
                .build();
    }

    @Override
    public PaymentResultVO wechatPay(Long orderId, String clientIp, String notifyUrl) {
        ServiceOrder order = validatePaymentOrder(orderId);
        
        // 生成支付流水号
        String paymentNo = generatePaymentNo(orderId, "wechat");
        
        // TODO: 集成微信支付API
        // 这里模拟微信支付流程
        
        // 记录支付日志
        recordPaymentLog(orderId, "wechat", "CREATE_ORDER", "创建微信支付订单", "PENDING");
        
        // 模拟返回微信支付二维码
        String qrCode = "weixin://wxpay/bizpayurl?pr=" + UUID.randomUUID().toString().replace("-", "");
        
        return PaymentResultVO.builder()
                .orderId(orderId)
                .orderNo(order.getOrderNo())
                .paymentStatus("pending")
                .paymentMethod("wechat")
                .paymentAmount(order.getAmountYuan())
                .paymentNo(paymentNo)
                .qrCode(qrCode)
                .needRedirect(false)
                .build();
    }

    @Override
    public PaymentResultVO alipayPay(Long orderId, String clientIp, String notifyUrl, String returnUrl) {
        ServiceOrder order = validatePaymentOrder(orderId);
        
        // 生成支付流水号
        String paymentNo = generatePaymentNo(orderId, "alipay");
        
        // TODO: 集成支付宝API
        // 这里模拟支付宝支付流程
        
        // 记录支付日志
        recordPaymentLog(orderId, "alipay", "CREATE_ORDER", "创建支付宝订单", "PENDING");
        
        // 模拟返回支付宝支付链接
        String paymentUrl = "https://openapi.alipay.com/gateway.do?service=create_partner_trade_by_buyer&_input_charset=utf-8&partner=xxx&out_trade_no=" + paymentNo;
        
        return PaymentResultVO.builder()
                .orderId(orderId)
                .orderNo(order.getOrderNo())
                .paymentStatus("pending")
                .paymentMethod("alipay")
                .paymentAmount(order.getAmountYuan())
                .paymentNo(paymentNo)
                .paymentUrl(paymentUrl)
                .needRedirect(true)
                .redirectUrl(paymentUrl)
                .build();
    }

    @Override
    public String queryPaymentStatus(Long orderId) {
        if (orderId == null) {
            throw new ServiceException("订单ID不能为空");
        }

        ServiceOrder order = serviceOrderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        if (order.isPaid()) {
            return "success";
        } else if (order.isPendingPayment()) {
            return "pending";
        } else if (order.isCancelled()) {
            return "cancelled";
        } else {
            return "unknown";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handlePaymentCallback(String paymentMethod, Map<String, Object> callbackData) {
        // TODO: 根据不同支付方式处理回调
        log.info("处理支付回调，支付方式：{}，回调数据：{}", paymentMethod, callbackData);
        
        // 验证回调数据的真实性
        // 解析订单信息
        // 更新订单状态
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handlePaymentSuccess(Long orderId, String paymentNo, String thirdPartyNo, String paymentMethod) {
        if (orderId == null) {
            throw new ServiceException("订单ID不能为空");
        }

        ServiceOrder order = serviceOrderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        if (!order.isPendingPayment()) {
            log.warn("订单状态异常，订单ID：{}，当前状态：{}", orderId, order.getStatus());
            return false;
        }

        // 更新订单状态为已付款
        ServiceOrder updateOrder = ServiceOrder.builder()
                .id(orderId)
                .status(ServiceOrder.Status.PAID.getCode())
                .updatedAt(LocalDateTime.now())
                .build();

        int result = serviceOrderMapper.updateById(updateOrder);
        if (result <= 0) {
            log.error("更新订单支付状态失败，订单ID：{}", orderId);
            return false;
        }

        // 记录订单日志
        orderService.recordOrderLog(orderId, "PAYMENT_SUCCESS", "支付成功", order.getBuyerId(), 
                                   String.format("支付方式：%s，流水号：%s", paymentMethod, paymentNo));

        log.info("处理支付成功，订单ID：{}，支付方式：{}，流水号：{}", orderId, paymentMethod, paymentNo);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handlePaymentFailed(Long orderId, String errorCode, String errorMessage) {
        // 记录支付失败日志
        recordPaymentLog(orderId, "unknown", "PAYMENT_FAILED", 
                        String.format("支付失败，错误码：%s，错误信息：%s", errorCode, errorMessage), "FAILED");
        
        log.warn("处理支付失败，订单ID：{}，错误码：{}，错误信息：{}", orderId, errorCode, errorMessage);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResultVO applyRefund(RefundDTO refundDTO) {
        ServiceOrder order = serviceOrderMapper.selectById(refundDTO.getOrderId());
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        if (!order.canRefund()) {
            throw new ServiceException("订单当前状态不支持退款");
        }

        Long currentUserId = LoginHelper.getUserId();
        
        // 权限验证
        if (currentUserId != null && !orderService.validateOrderPermission(refundDTO.getOrderId(), currentUserId, true, true)) {
            throw new ServiceException("无权限申请退款");
        }

        // 计算退款金额
        BigDecimal refundAmountYuan = refundDTO.getRefundAmount() != null ? 
                                      refundDTO.getRefundAmount() : order.getActualAmountYuan();
        Long refundAmountFen = refundAmountYuan.multiply(BigDecimal.valueOf(100)).longValue();
        
        if (refundAmountFen > order.getActualAmount()) {
            throw new ServiceException("退款金额不能超过实付金额");
        }

        // 从卖家扣款并退款到买家钱包（乐观锁）
        boolean refundSuccess = walletService.rechargeBalance(
                order.getBuyerId(), 
                refundAmountFen, 
                "refund", 
                refundDTO.getOrderId(), 
                "订单退款：" + refundDTO.getRefundReason()
        );
        
        if (!refundSuccess) {
            throw new ServiceException("退款处理失败");
        }

        // 如果卖家已收款，需要扣款
        if (order.getPaymentTime() != null) {
            Long sellerIncome = order.getActualAmount() - (order.getPlatformFee() != null ? order.getPlatformFee() : 0L);
            if (sellerIncome > 0) {
                walletService.deductBalance(
                        order.getSellerId(), 
                        sellerIncome, 
                        "refund", 
                        refundDTO.getOrderId(), 
                        "订单退款扣款"
                );
            }
        }

        LocalDateTime refundTime = LocalDateTime.now();
        
        // 更新订单状态
        ServiceOrder updateOrder = ServiceOrder.builder()
                .id(refundDTO.getOrderId())
                .status(ServiceOrder.Status.REFUNDED.getCode())
                .cancelReason(refundDTO.getRefundReason())
                .cancelTime(refundTime)
                .updatedAt(refundTime)
                .build();

        serviceOrderMapper.updateById(updateOrder);

        // 记录订单日志
        orderService.recordOrderLog(refundDTO.getOrderId(), "REFUND", "申请退款", currentUserId, 
                                   String.format("退款原因：%s，退款金额：%.2f元", refundDTO.getRefundReason(), refundAmountYuan));

        // 记录支付日志
        recordPaymentLog(refundDTO.getOrderId(), "wallet", "REFUND_SUCCESS", 
                        String.format("退款成功，金额：%.2f元", refundAmountYuan), "SUCCESS");

        log.info("✓ 退款成功 [乐观锁退款]，订单号：{}，买家：{}，金额：{}元", 
                order.getOrderNo(), order.getBuyerId(), refundAmountYuan);

        return PaymentResultVO.builder()
                .orderId(refundDTO.getOrderId())
                .orderNo(order.getOrderNo())
                .paymentStatus("success")
                .paymentMethod("refund")
                .paymentAmount(refundAmountYuan)
                .paymentTime(refundTime)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processRefund(Long orderId, Boolean approved, String processNote) {
        // TODO: 实现退款审核处理
        log.info("处理退款审核，订单ID：{}，审核结果：{}，处理说明：{}", orderId, approved, processNote);
        return true;
    }

    @Override
    public String queryRefundStatus(Long orderId) {
        ServiceOrder order = serviceOrderMapper.selectById(orderId);
        if (order == null) {
            return "unknown";
        }

        return order.isRefunded() ? "success" : "pending";
    }

    @Override
    public boolean validatePaymentPassword(Long userId, String paymentPassword) {
        if (userId == null || StringUtils.isBlank(paymentPassword)) {
            return false;
        }

        // TODO: 调用用户服务验证支付密码
        // 这里模拟验证逻辑
        return "123456".equals(paymentPassword); // 简化验证
    }

    // ✓ 已移除：checkWalletBalance、deductWalletAmount、refundWalletAmount、transferToSeller
    // 这些方法已由 WalletService 统一管理（乐观锁机制）

    @Override
    public Map<String, Object> getPaymentConfig(String paymentMethod) {
        Map<String, Object> config = new HashMap<>();
        
        switch (paymentMethod.toLowerCase()) {
            case "wechat":
                config.put("appId", "wx_app_id");
                config.put("mchId", "wx_mch_id");
                config.put("enabled", true);
                break;
            case "alipay":
                config.put("appId", "alipay_app_id");
                config.put("partnerId", "alipay_partner_id");
                config.put("enabled", true);
                break;
            case "wallet":
                config.put("enabled", true);
                config.put("requirePassword", true);
                break;
            default:
                config.put("enabled", false);
                break;
        }
        
        return config;
    }

    @Override
    public String generatePaymentNo(Long orderId, String paymentMethod) {
        String prefix = "";
        switch (paymentMethod.toLowerCase()) {
            case "wechat":
                prefix = "WX";
                break;
            case "alipay":
                prefix = "AL";
                break;
            case "wallet":
                prefix = "WL";
                break;
            default:
                prefix = "UN";
                break;
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return prefix + timestamp + orderId;
    }

    @Override
    public boolean recordPaymentLog(Long orderId, String paymentMethod, String actionType, String actionDesc, String result) {
        // TODO: 实现支付日志记录
        log.info("支付日志 - 订单ID：{}，支付方式：{}，操作：{}，描述：{}，结果：{}", 
                orderId, paymentMethod, actionType, actionDesc, result);
        return true;
    }

    /**
     * 验证支付订单
     */
    private ServiceOrder validatePaymentOrder(Long orderId) {
        if (orderId == null) {
            throw new ServiceException("订单ID不能为空");
        }

        ServiceOrder order = serviceOrderMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        if (!order.isPendingPayment()) {
            throw new ServiceException("订单状态不正确，无法支付");
        }

        return order;
    }
}
