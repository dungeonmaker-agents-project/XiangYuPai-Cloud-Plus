package com.xypai.trade.service;

import com.xypai.trade.domain.dto.PaymentDTO;
import com.xypai.trade.domain.dto.RefundDTO;
import com.xypai.trade.domain.vo.PaymentResultVO;

import java.util.Map;

/**
 * 支付服务接口
 *
 * @author xypai
 * @date 2025-01-01
 */
public interface IPaymentService {

    /**
     * 创建支付订单
     *
     * @param paymentDTO 支付数据
     * @return 支付结果
     */
    PaymentResultVO createPayment(PaymentDTO paymentDTO);

    /**
     * 钱包支付
     *
     * @param orderId 订单ID
     * @param paymentPassword 支付密码
     * @return 支付结果
     */
    PaymentResultVO walletPay(Long orderId, String paymentPassword);

    /**
     * 微信支付
     *
     * @param orderId 订单ID
     * @param clientIp 客户端IP
     * @param notifyUrl 回调地址
     * @return 支付结果
     */
    PaymentResultVO wechatPay(Long orderId, String clientIp, String notifyUrl);

    /**
     * 支付宝支付
     *
     * @param orderId 订单ID
     * @param clientIp 客户端IP
     * @param notifyUrl 回调地址
     * @param returnUrl 返回地址
     * @return 支付结果
     */
    PaymentResultVO alipayPay(Long orderId, String clientIp, String notifyUrl, String returnUrl);

    /**
     * 查询支付状态
     *
     * @param orderId 订单ID
     * @return 支付状态
     */
    String queryPaymentStatus(Long orderId);

    /**
     * 处理支付回调
     *
     * @param paymentMethod 支付方式
     * @param callbackData 回调数据
     * @return 处理结果
     */
    boolean handlePaymentCallback(String paymentMethod, Map<String, Object> callbackData);

    /**
     * 处理支付成功
     *
     * @param orderId 订单ID
     * @param paymentNo 支付流水号
     * @param thirdPartyNo 第三方交易号
     * @param paymentMethod 支付方式
     * @return 是否成功
     */
    boolean handlePaymentSuccess(Long orderId, String paymentNo, String thirdPartyNo, String paymentMethod);

    /**
     * 处理支付失败
     *
     * @param orderId 订单ID
     * @param errorCode 错误代码
     * @param errorMessage 错误信息
     * @return 是否成功
     */
    boolean handlePaymentFailed(Long orderId, String errorCode, String errorMessage);

    /**
     * 申请退款
     *
     * @param refundDTO 退款数据
     * @return 退款结果
     */
    PaymentResultVO applyRefund(RefundDTO refundDTO);

    /**
     * 处理退款
     *
     * @param orderId 订单ID
     * @param approved 是否同意退款
     * @param processNote 处理说明
     * @return 是否成功
     */
    boolean processRefund(Long orderId, Boolean approved, String processNote);

    /**
     * 查询退款状态
     *
     * @param orderId 订单ID
     * @return 退款状态
     */
    String queryRefundStatus(Long orderId);

    /**
     * 验证支付密码
     *
     * @param userId 用户ID
     * @param paymentPassword 支付密码
     * @return 是否正确
     */
    boolean validatePaymentPassword(Long userId, String paymentPassword);

    /**
     * 获取支付配置
     *
     * @param paymentMethod 支付方式
     * @return 配置信息
     */
    Map<String, Object> getPaymentConfig(String paymentMethod);

    /**
     * 生成支付订单号
     *
     * @param orderId 订单ID
     * @param paymentMethod 支付方式
     * @return 支付订单号
     */
    String generatePaymentNo(Long orderId, String paymentMethod);

    /**
     * 记录支付日志
     *
     * @param orderId 订单ID
     * @param paymentMethod 支付方式
     * @param actionType 操作类型
     * @param actionDesc 操作描述
     * @param result 结果
     * @return 是否成功
     */
    boolean recordPaymentLog(Long orderId, String paymentMethod, String actionType, String actionDesc, String result);
}
