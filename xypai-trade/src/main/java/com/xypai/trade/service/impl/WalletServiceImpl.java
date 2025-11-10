package com.xypai.trade.service.impl;

import org.dromara.common.core.exception.ServiceException;
import com.xypai.trade.domain.entity.UserWallet;
import com.xypai.trade.mapper.UserWalletMapper;
import com.xypai.trade.service.IWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 钱包服务实现类（乐观锁核心实现）
 *
 * @author xypai (Frank)
 * @date 2025-01-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements IWalletService {

    private final UserWalletMapper userWalletMapper;
    // private final ITransactionService transactionService;  // TODO: 注入交易流水服务

    /**
     * 乐观锁重试次数
     */
    private static final int MAX_RETRY_TIMES = 3;

    @Override
    public UserWallet getOrCreateWallet(Long userId) {
        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        UserWallet wallet = userWalletMapper.selectById(userId);
        if (wallet == null) {
            // 创建新钱包
            wallet = UserWallet.builder()
                    .userId(userId)
                    .balance(0L)
                    .frozen(0L)
                    .coinBalance(100L)  // 新用户赠送100金币
                    .totalIncome(0L)
                    .totalExpense(0L)
                    .version(0)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            userWalletMapper.insert(wallet);
            log.info("✅ 创建新钱包成功，用户ID：{}，赠送金币：100", userId);
        }

        return wallet;
    }

    @Override
    public UserWallet getWallet(Long userId) {
        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        UserWallet wallet = userWalletMapper.selectById(userId);
        if (wallet == null) {
            throw new ServiceException("钱包不存在，请先创建");
        }

        return wallet;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductBalance(Long userId, Long amount, String refType, Long refId, String description) {
        if (amount == null || amount <= 0) {
            throw new ServiceException("扣款金额必须大于0");
        }

        // 核心：乐观锁重试机制
        for (int i = 0; i < MAX_RETRY_TIMES; i++) {
            try {
                // 1. 查询钱包（获取version）
                UserWallet wallet = getOrCreateWallet(userId);

                // 2. 校验余额是否足够
                if (!wallet.hasEnoughBalance(amount)) {
                    throw new ServiceException("余额不足，当前余额：" + wallet.getFormattedBalance() + 
                                             "，需要：¥" + (amount / 100.0));
                }

                // 3. 乐观锁扣款（UPDATE ... WHERE version = ?）
                int rows = userWalletMapper.deductBalance(userId, amount, wallet.getVersion());
                
                if (rows == 0) {
                    // 并发冲突，version已变化
                    log.warn("⚠️ 乐观锁冲突（第{}次），用户：{}，金额：{}分，version：{}", 
                            i + 1, userId, amount, wallet.getVersion());
                    
                    if (i == MAX_RETRY_TIMES - 1) {
                        throw new ServiceException("余额扣减失败，系统繁忙，请稍后重试");
                    }
                    
                    // 短暂休眠后重试
                    Thread.sleep(50 * (i + 1));
                    continue;
                }

                // 4. 扣款成功，创建交易流水
                // TODO: transactionService.createTransaction(userId, -amount, "consume", refType, refId, description);

                log.info("✅ 扣款成功，用户：{}，金额：{}元，类型：{}，业务ID：{}", 
                        userId, amount / 100.0, refType, refId);
                
                return true;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ServiceException("扣款被中断");
            }
        }

        throw new ServiceException("余额扣减失败，超过最大重试次数");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rechargeBalance(Long userId, Long amount, String refType, Long refId, String description) {
        if (amount == null || amount <= 0) {
            throw new ServiceException("充值金额必须大于0");
        }

        // 乐观锁重试
        for (int i = 0; i < MAX_RETRY_TIMES; i++) {
            try {
                UserWallet wallet = getOrCreateWallet(userId);

                int rows = userWalletMapper.rechargeBalance(userId, amount, wallet.getVersion());
                
                if (rows == 0) {
                    if (i == MAX_RETRY_TIMES - 1) {
                        throw new ServiceException("充值失败，系统繁忙");
                    }
                    Thread.sleep(50 * (i + 1));
                    continue;
                }

                // 创建交易流水
                // TODO: transactionService.createTransaction(userId, amount, "recharge", refType, refId, description);

                log.info("✅充值成功，用户：{}，金额：{}元", userId, amount / 100.0);
                return true;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ServiceException("充值被中断");
            }
        }

        throw new ServiceException("充值失败，超过最大重试次数");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean freezeBalance(Long userId, Long amount) {
        if (amount == null || amount <= 0) {
            throw new ServiceException("冻结金额必须大于0");
        }

        for (int i = 0; i < MAX_RETRY_TIMES; i++) {
            try {
                UserWallet wallet = getOrCreateWallet(userId);

                if (!wallet.hasEnoughAvailableBalance(amount)) {
                    throw new ServiceException("可用余额不足，当前余额：" + wallet.getFormattedBalance());
                }

                int rows = userWalletMapper.freezeBalance(userId, amount, wallet.getVersion());
                
                if (rows == 0) {
                    if (i == MAX_RETRY_TIMES - 1) {
                        throw new ServiceException("冻结余额失败");
                    }
                    Thread.sleep(50 * (i + 1));
                    continue;
                }

                log.info("✅冻结余额成功，用户：{}，金额：{}元", userId, amount / 100.0);
                return true;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ServiceException("冻结操作被中断");
            }
        }

        throw new ServiceException("冻结余额失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfreezeBalance(Long userId, Long amount) {
        if (amount == null || amount <= 0) {
            throw new ServiceException("解冻金额必须大于0");
        }

        for (int i = 0; i < MAX_RETRY_TIMES; i++) {
            try {
                UserWallet wallet = getWallet(userId);

                if (wallet.getFrozen() < amount) {
                    throw new ServiceException("冻结金额不足");
                }

                int rows = userWalletMapper.unfreezeBalance(userId, amount, wallet.getVersion());
                
                if (rows == 0) {
                    if (i == MAX_RETRY_TIMES - 1) {
                        throw new ServiceException("解冻余额失败");
                    }
                    Thread.sleep(50 * (i + 1));
                    continue;
                }

                log.info("✅解冻余额成功，用户：{}，金额：{}元", userId, amount / 100.0);
                return true;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ServiceException("解冻操作被中断");
            }
        }

        throw new ServiceException("解冻余额失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductFrozen(Long userId, Long amount, String refType, Long refId, String description) {
        if (amount == null || amount <= 0) {
            throw new ServiceException("扣减金额必须大于0");
        }

        for (int i = 0; i < MAX_RETRY_TIMES; i++) {
            try {
                UserWallet wallet = getWallet(userId);

                if (wallet.getFrozen() < amount) {
                    throw new ServiceException("冻结金额不足");
                }

                int rows = userWalletMapper.deductFrozen(userId, amount, wallet.getVersion());
                
                if (rows == 0) {
                    if (i == MAX_RETRY_TIMES - 1) {
                        throw new ServiceException("扣减冻结金额失败");
                    }
                    Thread.sleep(50 * (i + 1));
                    continue;
                }

                // 创建交易流水
                // TODO: transactionService.createTransaction(userId, -amount, "consume", refType, refId, description);

                log.info("✅扣减冻结金额成功，用户：{}，金额：{}元", userId, amount / 100.0);
                return true;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ServiceException("扣减操作被中断");
            }
        }

        throw new ServiceException("扣减冻结金额失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addIncome(Long userId, Long amount, String refType, Long refId, String description) {
        if (amount == null || amount <= 0) {
            throw new ServiceException("收入金额必须大于0");
        }

        for (int i = 0; i < MAX_RETRY_TIMES; i++) {
            try {
                UserWallet wallet = getOrCreateWallet(userId);

                int rows = userWalletMapper.addIncome(userId, amount, wallet.getVersion());
                
                if (rows == 0) {
                    if (i == MAX_RETRY_TIMES - 1) {
                        throw new ServiceException("增加收入失败");
                    }
                    Thread.sleep(50 * (i + 1));
                    continue;
                }

                // 创建交易流水
                // TODO: transactionService.createTransaction(userId, amount, "income", refType, refId, description);

                log.info("✅增加收入成功，用户：{}，金额：{}元", userId, amount / 100.0);
                return true;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ServiceException("收入操作被中断");
            }
        }

        throw new ServiceException("增加收入失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean transfer(Long fromUserId, Long toUserId, Long amount, String description) {
        if (fromUserId == null || toUserId == null) {
            throw new ServiceException("用户ID不能为空");
        }
        if (fromUserId.equals(toUserId)) {
            throw new ServiceException("不能转账给自己");
        }
        if (amount == null || amount <= 0) {
            throw new ServiceException("转账金额必须大于0");
        }

        // 1. 扣减转出用户余额
        boolean deductSuccess = deductBalance(fromUserId, amount, "transfer", toUserId, "转账给用户" + toUserId);
        if (!deductSuccess) {
            throw new ServiceException("转账失败：扣款失败");
        }

        // 2. 增加转入用户余额
        boolean addSuccess = addIncome(toUserId, amount, "transfer", fromUserId, "收到用户" + fromUserId + "的转账");
        if (!addSuccess) {
            // 回滚：将转出金额退回
            rechargeBalance(fromUserId, amount, "refund", toUserId, "转账失败退款");
            throw new ServiceException("转账失败：收款失败");
        }

        log.info("✓ 转账成功，从用户{}转账{}元给用户{}", fromUserId, amount / 100.0, toUserId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addCoins(Long userId, Long coins) {
        if (coins == null || coins <= 0) {
            throw new ServiceException("金币数量必须大于0");
        }

        for (int i = 0; i < MAX_RETRY_TIMES; i++) {
            try {
                UserWallet wallet = getOrCreateWallet(userId);

                int rows = userWalletMapper.addCoins(userId, coins, wallet.getVersion());
                
                if (rows == 0) {
                    if (i == MAX_RETRY_TIMES - 1) {
                        throw new ServiceException("增加金币失败");
                    }
                    Thread.sleep(50 * (i + 1));
                    continue;
                }

                log.info("✅增加金币成功，用户：{}，金币：{}", userId, coins);
                return true;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ServiceException("金币操作被中断");
            }
        }

        throw new ServiceException("增加金币失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductCoins(Long userId, Long coins) {
        if (coins == null || coins <= 0) {
            throw new ServiceException("金币数量必须大于0");
        }

        for (int i = 0; i < MAX_RETRY_TIMES; i++) {
            try {
                UserWallet wallet = getWallet(userId);

                if (wallet.getCoinBalance() < coins) {
                    throw new ServiceException("金币余额不足，当前金币：" + wallet.getCoinBalance());
                }

                int rows = userWalletMapper.deductCoins(userId, coins, wallet.getVersion());
                
                if (rows == 0) {
                    if (i == MAX_RETRY_TIMES - 1) {
                        throw new ServiceException("扣减金币失败");
                    }
                    Thread.sleep(50 * (i + 1));
                    continue;
                }

                log.info("✅扣减金币成功，用户：{}，金币：{}", userId, coins);
                return true;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ServiceException("金币操作被中断");
            }
        }

        throw new ServiceException("扣减金币失败");
    }

    @Override
    public Map<String, Object> getWalletStats() {
        return userWalletMapper.selectWalletOverview();
    }

    @Override
    public boolean hasEnoughBalance(Long userId, Long amount) {
        if (userId == null || amount == null) {
            return false;
        }

        try {
            UserWallet wallet = getWallet(userId);
            return wallet.hasEnoughBalance(amount);
        } catch (Exception e) {
            log.error("查询余额失败，用户：{}", userId, e);
            return false;
        }
    }
}

