package org.dromara.payment.api;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.payment.api.domain.*;

/**
 * 支付远程服务接口
 *
 * <p>用途：其他服务通过Dubbo调用此接口进行支付相关操作</p>
 * <p>实现：xypai-payment模块实现此接口</p>
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
public interface RemotePaymentService {

    /**
     * 创建支付记录
     *
     * <p>调用方：OrderService, ActivityService</p>
     * <p>场景：创建订单或活动时创建支付记录</p>
     *
     * @param request 创建支付请求
     * @return 支付结果
     * @throws ServiceException 创建失败
     */
    PaymentResultDTO createPayment(CreatePaymentRequest request) throws ServiceException;

    /**
     * 扣减用户余额
     *
     * <p>调用方：所有需要扣款的服务</p>
     * <p>场景：支付、扣费等操作</p>
     *
     * @param request 扣减余额请求
     * @return true=扣减成功，false=余额不足
     * @throws ServiceException 用户不存在或其他错误
     */
    boolean deductBalance(DeductBalanceRequest request) throws ServiceException;

    /**
     * 增加用户余额
     *
     * <p>调用方：所有需要加款的服务</p>
     * <p>场景：充值、退款、收入等操作</p>
     *
     * @param request 增加余额请求
     * @return true=增加成功
     * @throws ServiceException 用户不存在或其他错误
     */
    boolean addBalance(AddBalanceRequest request) throws ServiceException;

    /**
     * 退款
     *
     * <p>调用方：OrderService, ActivityService</p>
     * <p>场景：订单取消、活动取消等</p>
     *
     * @param paymentNo 支付流水号
     * @param refundAmount 退款金额
     * @param reason 退款原因
     * @return true=退款成功
     * @throws ServiceException 支付记录不存在或退款失败
     */
    boolean refundBalance(String paymentNo, java.math.BigDecimal refundAmount, String reason) throws ServiceException;

    /**
     * 获取用户余额
     *
     * <p>调用方：所有需要查询余额的服务</p>
     * <p>场景：支付前检查余额等</p>
     *
     * @param userId 用户ID
     * @return 用户余额
     */
    java.math.BigDecimal getBalance(Long userId);

    /**
     * 验证支付密码
     *
     * <p>调用方：xypai-app-bff (订单确认支付)</p>
     * <p>场景：用户金币支付前验证6位支付密码</p>
     * <p>安全机制：错误3次锁定30分钟</p>
     *
     * @param userId 用户ID
     * @param password 6位支付密码（明文）
     * @return true=验证通过，false=密码错误
     * @throws ServiceException 账户不存在/已锁定/未设置密码
     */
    boolean verifyPaymentPassword(Long userId, String password) throws ServiceException;

    /**
     * 检查用户是否已设置支付密码
     *
     * <p>调用方：xypai-app-bff (订单确认页面)</p>
     * <p>场景：判断是否需要弹出设置密码引导</p>
     *
     * @param userId 用户ID
     * @return true=已设置，false=未设置
     */
    boolean hasPaymentPassword(Long userId);
}
