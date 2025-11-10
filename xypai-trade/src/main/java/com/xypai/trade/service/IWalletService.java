package com.xypai.trade.service;

import com.xypai.trade.domain.entity.UserWallet;

import java.util.Map;

/**
 * 钱包服务接口
 *
 * @author xypai (Frank)
 * @date 2025-01-14
 */
public interface IWalletService {

    /**
     * 获取或创建用户钱包
     *
     * @param userId 用户ID
     * @return 钱包信息
     */
    UserWallet getOrCreateWallet(Long userId);

    /**
     * 查询钱包余额
     *
     * @param userId 用户ID
     * @return 钱包信息
     */
    UserWallet getWallet(Long userId);

    /**
     * ⭐ 扣减余额（乐观锁，核心方法）
     *
     * @param userId 用户ID
     * @param amount 扣减金额（分）
     * @param refType 关联类型（order/withdraw/transfer等）
     * @param refId 关联业务ID
     * @param description 交易描述
     * @return 是否成功
     */
    boolean deductBalance(Long userId, Long amount, String refType, Long refId, String description);

    /**
     * ⭐ 充值余额（乐观锁）
     *
     * @param userId 用户ID
     * @param amount 充值金额（分）
     * @param refType 关联类型（recharge/refund/income等）
     * @param refId 关联业务ID
     * @param description 交易描述
     * @return 是否成功
     */
    boolean rechargeBalance(Long userId, Long amount, String refType, Long refId, String description);

    /**
     * ⭐ 冻结金额（下单时）
     *
     * @param userId 用户ID
     * @param amount 冻结金额（分）
     * @return 是否成功
     */
    boolean freezeBalance(Long userId, Long amount);

    /**
     * ⭐ 解冻金额（取消订单时）
     *
     * @param userId 用户ID
     * @param amount 解冻金额（分）
     * @return 是否成功
     */
    boolean unfreezeBalance(Long userId, Long amount);

    /**
     * ⭐ 扣减冻结金额（支付成功时）
     *
     * @param userId 用户ID
     * @param amount 扣减金额（分）
     * @param refType 关联类型
     * @param refId 关联业务ID
     * @param description 交易描述
     * @return 是否成功
     */
    boolean deductFrozen(Long userId, Long amount, String refType, Long refId, String description);

    /**
     * ⭐ 增加收入（卖家收款）
     *
     * @param userId 用户ID
     * @param amount 收入金额（分）
     * @param refType 关联类型
     * @param refId 关联业务ID
     * @param description 交易描述
     * @return 是否成功
     */
    boolean addIncome(Long userId, Long amount, String refType, Long refId, String description);

    /**
     * 转账（用户之间）
     *
     * @param fromUserId 转出用户ID
     * @param toUserId 转入用户ID
     * @param amount 转账金额（分）
     * @param description 转账说明
     * @return 是否成功
     */
    boolean transfer(Long fromUserId, Long toUserId, Long amount, String description);

    /**
     * 增加金币
     *
     * @param userId 用户ID
     * @param coins 金币数量
     * @return 是否成功
     */
    boolean addCoins(Long userId, Long coins);

    /**
     * 扣减金币
     *
     * @param userId 用户ID
     * @param coins 金币数量
     * @return 是否成功
     */
    boolean deductCoins(Long userId, Long coins);

    /**
     * 查询钱包统计
     *
     * @return 统计信息
     */
    Map<String, Object> getWalletStats();

    /**
     * 检查余额是否足够
     *
     * @param userId 用户ID
     * @param amount 金额（分）
     * @return 是否足够
     */
    boolean hasEnoughBalance(Long userId, Long amount);
}

