package org.dromara.payment.service;

import org.dromara.payment.domain.vo.BalanceVO;

import java.math.BigDecimal;

/**
 * 账户服务接口
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
public interface IAccountService {

    /**
     * 获取用户余额
     *
     * @param userId 用户ID
     * @return 余额信息
     */
    BalanceVO getBalance(Long userId);

    /**
     * 扣减余额
     *
     * @param userId 用户ID
     * @param amount 扣减金额
     * @param reason 扣减原因
     * @param referenceId 关联ID
     * @param referenceType 关联类型
     * @param paymentNo 支付流水号
     * @return true=扣减成功
     */
    boolean deductBalance(Long userId, BigDecimal amount, String reason, String referenceId, String referenceType, String paymentNo);

    /**
     * 增加余额
     *
     * @param userId 用户ID
     * @param amount 增加金额
     * @param reason 增加原因
     * @param referenceId 关联ID
     * @param referenceType 关联类型
     * @param paymentNo 支付流水号
     * @return true=增加成功
     */
    boolean addBalance(Long userId, BigDecimal amount, String reason, String referenceId, String referenceType, String paymentNo);

    /**
     * 验证支付密码
     *
     * @param userId 用户ID
     * @param password 支付密码
     * @return true=正确
     */
    boolean verifyPaymentPassword(Long userId, String password);

    /**
     * 检查是否设置支付密码
     *
     * @param userId 用户ID
     * @return true=已设置
     */
    boolean hasPaymentPassword(Long userId);

    /**
     * 创建或获取用户账户
     *
     * @param userId 用户ID
     * @return 用户账户
     */
    org.dromara.payment.domain.entity.UserAccount getOrCreateAccount(Long userId);
}
