package com.xypai.user.service;

import com.xypai.user.domain.dto.TransactionQueryDTO;
import com.xypai.user.domain.dto.WalletRechargeDTO;
import com.xypai.user.domain.dto.WalletTransferDTO;
import com.xypai.user.domain.vo.TransactionVO;
import com.xypai.user.domain.vo.UserWalletVO;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

import java.util.Map;

/**
 * 用户钱包服务接口
 *
 * @author xypai
 * @date 2025-01-01
 */
public interface IUserWalletService {

    /**
     * 获取用户钱包信息
     */
    UserWalletVO getUserWallet();

    /**
     * 根据用户ID获取钱包信息
     */
    UserWalletVO getUserWalletByUserId(Long userId);

    /**
     * 创建充值订单
     */
    String createRechargeOrder(WalletRechargeDTO rechargeDTO);

    /**
     * 钱包转账
     */
    boolean transferMoney(WalletTransferDTO transferDTO);

    /**
     * 获取用户交易记录
     */
    TableDataInfo<TransactionVO> getUserTransactions(TransactionQueryDTO query, PageQuery pageQuery);

    /**
     * 根据交易ID获取交易详情
     */
    TransactionVO getTransactionById(Long transactionId);

    /**
     * 获取钱包统计信息
     */
    Map<String, Object> getWalletStatistics(String startDate, String endDate);

    /**
     * 冻结钱包
     */
    boolean freezeWallet(Long userId, String reason);

    /**
     * 解冻钱包
     */
    boolean unfreezeWallet(Long userId);

    /**
     * 钱包余额变动（内部接口）
     */
    boolean updateBalance(Long userId, Long amount, String type, String refId);

    /**
     * 检查钱包余额是否足够
     */
    boolean checkBalance(Long userId, Long amount);

    /**
     * 钱包充值完成回调
     */
    boolean rechargeSuccess(String orderNo, Long amount);

    /**
     * 钱包消费
     */
    boolean consumeBalance(Long userId, Long amount, String type, String refId);

    /**
     * 钱包退款
     */
    boolean refundBalance(Long userId, Long amount, String type, String refId);
}
