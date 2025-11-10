package com.xypai.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xypai.trade.domain.entity.UserWallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 用户钱包Mapper接口
 *
 * @author xypai (Frank)
 * @date 2025-01-14
 */
@Mapper
public interface UserWalletMapper extends BaseMapper<UserWallet> {

    /**
     * ⭐ 扣减余额（乐观锁）- 核心方法
     *
     * @param userId 用户ID
     * @param amount 扣减金额（分）
     * @param version 当前版本号
     * @return 影响行数（0=并发冲突）
     */
    int deductBalance(@Param("userId") Long userId, 
                     @Param("amount") Long amount, 
                     @Param("version") Integer version);

    /**
     * ⭐ 充值余额（乐观锁）
     *
     * @param userId 用户ID
     * @param amount 充值金额（分）
     * @param version 当前版本号
     * @return 影响行数
     */
    int rechargeBalance(@Param("userId") Long userId, 
                       @Param("amount") Long amount, 
                       @Param("version") Integer version);

    /**
     * ⭐ 冻结金额（乐观锁）
     *
     * @param userId 用户ID
     * @param amount 冻结金额（分）
     * @param version 当前版本号
     * @return 影响行数
     */
    int freezeBalance(@Param("userId") Long userId, 
                     @Param("amount") Long amount, 
                     @Param("version") Integer version);

    /**
     * ⭐ 解冻金额（乐观锁）
     *
     * @param userId 用户ID
     * @param amount 解冻金额（分）
     * @param version 当前版本号
     * @return 影响行数
     */
    int unfreezeBalance(@Param("userId") Long userId, 
                       @Param("amount") Long amount, 
                       @Param("version") Integer version);

    /**
     * ⭐ 扣减冻结金额（支付成功时）
     *
     * @param userId 用户ID
     * @param amount 扣减金额（分）
     * @param version 当前版本号
     * @return 影响行数
     */
    int deductFrozen(@Param("userId") Long userId, 
                    @Param("amount") Long amount, 
                    @Param("version") Integer version);

    /**
     * 增加收入（卖家收款）
     *
     * @param userId 用户ID
     * @param amount 收入金额（分）
     * @param version 当前版本号
     * @return 影响行数
     */
    int addIncome(@Param("userId") Long userId, 
                 @Param("amount") Long amount, 
                 @Param("version") Integer version);

    /**
     * 增加金币
     *
     * @param userId 用户ID
     * @param coins 金币数量
     * @param version 当前版本号
     * @return 影响行数
     */
    int addCoins(@Param("userId") Long userId, 
                @Param("coins") Long coins, 
                @Param("version") Integer version);

    /**
     * 扣减金币
     *
     * @param userId 用户ID
     * @param coins 金币数量
     * @param version 当前版本号
     * @return 影响行数
     */
    int deductCoins(@Param("userId") Long userId, 
                   @Param("coins") Long coins, 
                   @Param("version") Integer version);

    /**
     * 查询钱包余额分布统计
     *
     * @return 统计信息
     */
    Map<String, Object> selectBalanceDistribution();

    /**
     * 查询钱包总览统计
     *
     * @return 统计信息
     */
    Map<String, Object> selectWalletOverview();

    /**
     * 查询余额不足用户列表
     *
     * @param minBalance 最低余额（分）
     * @return 用户ID列表
     */
    java.util.List<Long> selectLowBalanceUsers(@Param("minBalance") Long minBalance);
}

