package com.xypai.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import com.xypai.user.domain.dto.TransactionQueryDTO;
import com.xypai.user.domain.dto.WalletRechargeDTO;
import com.xypai.user.domain.dto.WalletTransferDTO;
import com.xypai.user.domain.entity.Transaction;
import com.xypai.user.domain.entity.UserWallet;
import com.xypai.user.domain.vo.TransactionVO;
import com.xypai.user.domain.vo.UserWalletVO;
import com.xypai.user.mapper.TransactionMapper;
import com.xypai.user.mapper.UserWalletMapper;
import com.xypai.user.service.IUserWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户钱包服务实现�?
 *
 * @author xypai
 * @date 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserWalletServiceImpl implements IUserWalletService {

    private final UserWalletMapper userWalletMapper;
    private final TransactionMapper transactionMapper;

    @Override
    public UserWalletVO getUserWallet() {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }
        return getUserWalletByUserId(currentUserId);
    }

    @Override
    public UserWalletVO getUserWalletByUserId(Long userId) {
        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        UserWallet wallet = userWalletMapper.selectById(userId);
        if (wallet == null) {
            throw new ServiceException("用户钱包不存在");
        }

        return convertToWalletVO(wallet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createRechargeOrder(WalletRechargeDTO rechargeDTO) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        // 生成充值订单号
        String orderNo = generateOrderNo("RO");
        
        // 这里应该调用支付服务创建支付订单
        // 暂时返回订单号，实际项目中需要集成支付平�?
        log.info("创建充值订单成功，用户ID：{}，订单号：{}，金额：{}", 
                currentUserId, orderNo, rechargeDTO.getAmount());
        
        return orderNo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean transferMoney(WalletTransferDTO transferDTO) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        if (currentUserId.equals(transferDTO.getToUserId())) {
            throw new ServiceException("不能向自己转账");
        }

        Long amountFen = transferDTO.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

        // 检查余额是否足�?
        if (!checkBalance(currentUserId, amountFen)) {
            throw new ServiceException("余额不足");
        }

        // 检查接收方用户是否存在
        UserWallet toWallet = userWalletMapper.selectById(transferDTO.getToUserId());
        if (toWallet == null) {
            throw new ServiceException("接收方用户钱包不存在");
        }

        String refId = generateOrderNo("TR");

        // 转出方扣�?
        boolean deductResult = updateBalance(currentUserId, -amountFen, 
                Transaction.Type.TRANSFER_OUT.getCode(), refId);
        if (!deductResult) {
            throw new ServiceException("转账失败");
        }

        // 接收方加�?
        boolean addResult = updateBalance(transferDTO.getToUserId(), amountFen, 
                Transaction.Type.TRANSFER_IN.getCode(), refId);
        if (!addResult) {
            throw new ServiceException("转账失败");
        }

        log.info("转账成功，转出用户：{}，接收用户：{}，金额：{}", 
                currentUserId, transferDTO.getToUserId(), transferDTO.getAmount());
        
        return true;
    }

    @Override
    public TableDataInfo<TransactionVO> getUserTransactions(TransactionQueryDTO query, PageQuery pageQuery) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        LambdaQueryWrapper<Transaction> queryWrapper = Wrappers.lambdaQuery(Transaction.class)
                .eq(Transaction::getUserId, query.getUserId() != null ? query.getUserId() : currentUserId)
                .eq(StringUtils.isNotBlank(query.getType()), Transaction::getType, query.getType())
                .eq(StringUtils.isNotBlank(query.getRefId()), Transaction::getRefId, query.getRefId())
                .between(StringUtils.isNotBlank(query.getBeginTime()) && StringUtils.isNotBlank(query.getEndTime()),
                        Transaction::getCreatedAt, query.getBeginTime(), query.getEndTime())
                .orderByDesc(Transaction::getCreatedAt);

        Page<Transaction> page = transactionMapper.selectPage(pageQuery.build(), queryWrapper);
        
        // Convert to VO
        Page<TransactionVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<TransactionVO> voList = new ArrayList<>();
        for (Transaction transaction : page.getRecords()) {
            voList.add(convertToTransactionVO(transaction));
        }
        voPage.setRecords(voList);
        
        return TableDataInfo.build(voPage);
    }

    @Override
    public TransactionVO getTransactionById(Long transactionId) {
        if (transactionId == null) {
            throw new ServiceException("交易ID不能为空");
        }

        Transaction transaction = transactionMapper.selectById(transactionId);
        if (transaction == null) {
            throw new ServiceException("交易记录不存在");
        }

        // 检查是否有权限查看该交易记�?
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId != null && !currentUserId.equals(transaction.getUserId())) {
            throw new ServiceException("无权限查看该交易记录");
        }

        return convertToTransactionVO(transaction);
    }

    @Override
    public Map<String, Object> getWalletStatistics(String startDate, String endDate) {
        Long currentUserId = LoginHelper.getUserId();
        if (currentUserId == null) {
            throw new ServiceException("未获取到当前用户信息");
        }

        Map<String, Object> statistics = new HashMap<>();
        
        // 获取钱包信息
        UserWallet wallet = userWalletMapper.selectById(currentUserId);
        statistics.put("currentBalance", wallet != null ? wallet.getFormattedBalance() : "¥0.00");

        // 查询交易统计
        LambdaQueryWrapper<Transaction> queryWrapper = Wrappers.lambdaQuery(Transaction.class)
                .eq(Transaction::getUserId, currentUserId)
                .between(StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate),
                        Transaction::getCreatedAt, startDate, endDate);

        List<Transaction> transactions = transactionMapper.selectList(queryWrapper);
        
        long totalIncome = 0L;
        long totalExpense = 0L;
        int transactionCount = transactions.size();
        
        for (Transaction transaction : transactions) {
            if (transaction.isIncome()) {
                totalIncome += transaction.getAmount();
            } else {
                totalExpense += Math.abs(transaction.getAmount());
            }
        }

        statistics.put("totalIncome", formatAmount(totalIncome));
        statistics.put("totalExpense", formatAmount(totalExpense));
        statistics.put("transactionCount", transactionCount);
        
        return statistics;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean freezeWallet(Long userId, String reason) {
        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        // TODO: 实现钱包冻结逻辑
        log.info("冻结用户钱包，用户ID：{}，原因：{}", userId, reason);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfreezeWallet(Long userId) {
        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        // TODO: 实现钱包解冻逻辑
        log.info("解冻用户钱包，用户ID：{}", userId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBalance(Long userId, Long amount, String type, String refId) {
        if (userId == null || amount == null || amount == 0) {
            throw new ServiceException("参数错误");
        }

        // 获取钱包信息
        UserWallet wallet = userWalletMapper.selectById(userId);
        if (wallet == null) {
            throw new ServiceException("用户钱包不存在");
        }

        // 如果是扣款，检查余额是否足�?
        if (amount < 0 && !wallet.hasEnoughBalance(Math.abs(amount))) {
            throw new ServiceException("余额不足");
        }

        // 更新钱包余额（使用乐观锁�?
        UserWallet updateWallet = UserWallet.builder()
                .userId(userId)
                .balance(wallet.getBalance() + amount)
                .version(wallet.getVersion())
                .build();

        int updateResult = userWalletMapper.updateById(updateWallet);
        if (updateResult <= 0) {
            throw new ServiceException("余额更新失败，请重试");
        }

        // 记录交易流水
        Transaction transaction = Transaction.builder()
                .userId(userId)
                .amount(amount)
                .type(type)
                .refId(refId)
                .createdAt(LocalDateTime.now())
                .build();

        int insertResult = transactionMapper.insert(transaction);
        if (insertResult <= 0) {
            throw new ServiceException("交易记录创建失败");
        }

        log.info("用户余额变动成功，用户ID：{}，变动金额：{}分，类型：{}", userId, amount, type);
        return true;
    }

    @Override
    public boolean checkBalance(Long userId, Long amount) {
        if (userId == null || amount == null || amount <= 0) {
            return false;
        }

        UserWallet wallet = userWalletMapper.selectById(userId);
        return wallet != null && wallet.hasEnoughBalance(amount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rechargeSuccess(String orderNo, Long amount) {
        // TODO: 根据订单号查询用户信息
        // 这里简化处理，实际项目中需要从订单表查询
        log.info("充值成功回调，订单号：{}，金额：{}", orderNo, amount);
        
        // 示例：假设从订单号中提取用户ID（实际应该查询订单表�?
        // Long userId = extractUserIdFromOrderNo(orderNo);
        // return updateBalance(userId, amount, Transaction.Type.RECHARGE.getCode(), orderNo);
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean consumeBalance(Long userId, Long amount, String type, String refId) {
        return updateBalance(userId, -amount, type, refId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refundBalance(Long userId, Long amount, String type, String refId) {
        return updateBalance(userId, amount, type, refId);
    }

    /**
     * 转换为钱包VO
     */
    private UserWalletVO convertToWalletVO(UserWallet wallet) {
        return UserWalletVO.builder()
                .userId(wallet.getUserId())
                .balance(wallet.getFormattedBalance())
                .balanceFen(wallet.getBalance())
                .available(true) // TODO: 根据钱包状态判�?
                .version(wallet.getVersion())
                .build();
    }

    /**
     * 转换为交易VO
     */
    private TransactionVO convertToTransactionVO(Transaction transaction) {
        return TransactionVO.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .amount(transaction.getAmountYuan().toString())
                .amountFen(transaction.getAmount())
                .type(transaction.getType())
                .typeDesc(transaction.getTypeDesc())
                .refId(transaction.getRefId())
                .createdAt(transaction.getCreatedAt())
                .isIncome(transaction.isIncome())
                .formattedAmount(transaction.getFormattedAmount())
                .build();
    }

    /**
     * 生成订单�?
     */
    private String generateOrderNo(String prefix) {
        return prefix + System.currentTimeMillis() + 
               String.format("%03d", new Random().nextInt(1000));
    }

    /**
     * 格式化金额显�?
     */
    private String formatAmount(Long amountFen) {
        return "¥" + BigDecimal.valueOf(amountFen)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .toString();
    }
}
