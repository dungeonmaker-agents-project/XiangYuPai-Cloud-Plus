package org.dromara.payment.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.payment.domain.entity.AccountTransaction;
import org.dromara.payment.domain.entity.UserAccount;
import org.dromara.payment.domain.vo.BalanceVO;
import org.dromara.payment.mapper.AccountTransactionMapper;
import org.dromara.payment.mapper.UserAccountMapper;
import org.dromara.payment.service.IAccountService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 账户服务实现
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements IAccountService {

    private final UserAccountMapper userAccountMapper;
    private final AccountTransactionMapper accountTransactionMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 余额缓存key前缀
     */
    private static final String BALANCE_CACHE_KEY = "payment:balance:";

    /**
     * 余额缓存时间（5分钟）
     */
    private static final Duration BALANCE_CACHE_TTL = Duration.ofMinutes(5);

    /**
     * 密码错误次数缓存key前缀
     */
    private static final String PWD_ERROR_KEY = "payment:pwd:error:";

    /**
     * 密码错误次数锁定时间（30分钟）
     */
    private static final Duration PWD_LOCK_DURATION = Duration.ofMinutes(30);

    /**
     * 最大密码错误次数
     */
    private static final int MAX_PWD_ERROR_COUNT = 5;

    @Override
    public BalanceVO getBalance(Long userId) {
        // 先从缓存获取
        String cacheKey = BALANCE_CACHE_KEY + userId;
        BalanceVO cached = RedisUtils.getCacheObject(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 查询数据库
        UserAccount account = getOrCreateAccount(userId);

        BigDecimal availableBalance = account.getBalance().subtract(account.getFrozenBalance());

        BalanceVO result = BalanceVO.builder()
            .balance(account.getBalance())
            .frozenBalance(account.getFrozenBalance())
            .availableBalance(availableBalance)
            .build();

        // 缓存结果
        RedisUtils.setCacheObject(cacheKey, result, BALANCE_CACHE_TTL);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductBalance(Long userId, BigDecimal amount, String reason, String referenceId, String referenceType, String paymentNo) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("扣减金额必须大于0");
        }

        UserAccount account = getOrCreateAccount(userId);

        // 检查余额是否充足
        if (account.getBalance().compareTo(amount) < 0) {
            throw new ServiceException("余额不足");
        }

        // 记录交易前余额
        BigDecimal balanceBefore = account.getBalance();

        // 扣减余额
        account.setBalance(account.getBalance().subtract(amount));
        account.setTotalExpense(account.getTotalExpense().add(amount));

        // 使用乐观锁更新
        int updated = userAccountMapper.updateById(account);
        if (updated == 0) {
            throw new ServiceException("余额扣减失败，请重试");
        }

        // 记录流水
        String transactionNo = generateTransactionNo();
        AccountTransaction transaction = AccountTransaction.builder()
            .transactionNo(transactionNo)
            .userId(userId)
            .transactionType("expense")
            .amount(amount)
            .balanceBefore(balanceBefore)
            .balanceAfter(account.getBalance())
            .paymentNo(paymentNo)
            .referenceId(referenceId)
            .referenceType(referenceType)
            .remark(reason)
            .build();
        accountTransactionMapper.insert(transaction);

        // 清除缓存
        RedisUtils.deleteObject(BALANCE_CACHE_KEY + userId);

        log.info("扣减余额成功: userId={}, amount={}, reason={}", userId, amount, reason);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addBalance(Long userId, BigDecimal amount, String reason, String referenceId, String referenceType, String paymentNo) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("增加金额必须大于0");
        }

        UserAccount account = getOrCreateAccount(userId);

        // 记录交易前余额
        BigDecimal balanceBefore = account.getBalance();

        // 增加余额
        account.setBalance(account.getBalance().add(amount));
        account.setTotalIncome(account.getTotalIncome().add(amount));

        // 使用乐观锁更新
        int updated = userAccountMapper.updateById(account);
        if (updated == 0) {
            throw new ServiceException("余额增加失败，请重试");
        }

        // 记录流水
        String transactionNo = generateTransactionNo();
        AccountTransaction transaction = AccountTransaction.builder()
            .transactionNo(transactionNo)
            .userId(userId)
            .transactionType("income")
            .amount(amount)
            .balanceBefore(balanceBefore)
            .balanceAfter(account.getBalance())
            .paymentNo(paymentNo)
            .referenceId(referenceId)
            .referenceType(referenceType)
            .remark(reason)
            .build();
        accountTransactionMapper.insert(transaction);

        // 清除缓存
        RedisUtils.deleteObject(BALANCE_CACHE_KEY + userId);

        log.info("增加余额成功: userId={}, amount={}, reason={}", userId, amount, reason);
        return true;
    }

    @Override
    public boolean verifyPaymentPassword(Long userId, String password) {
        UserAccount account = getOrCreateAccount(userId);

        // 检查是否锁定
        if (account.getPasswordLockedUntil() != null &&
            account.getPasswordLockedUntil().isAfter(LocalDateTime.now())) {
            throw new ServiceException("密码已锁定，请稍后再试");
        }

        // 检查是否设置密码
        if (account.getPaymentPasswordHash() == null) {
            throw new ServiceException("未设置支付密码");
        }

        // 验证密码
        boolean matches = passwordEncoder.matches(password, account.getPaymentPasswordHash());

        if (!matches) {
            // 密码错误，增加错误计数
            incrementPasswordErrorCount(userId, account);
            throw new ServiceException("支付密码错误");
        }

        // 密码正确，重置错误计数
        resetPasswordErrorCount(userId, account);

        return true;
    }

    @Override
    public boolean hasPaymentPassword(Long userId) {
        UserAccount account = getOrCreateAccount(userId);
        return account.getPaymentPasswordHash() != null;
    }

    @Override
    public UserAccount getOrCreateAccount(Long userId) {
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAccount::getUserId, userId);

        UserAccount account = userAccountMapper.selectOne(wrapper);

        if (account == null) {
            // 创建新账户
            account = UserAccount.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .frozenBalance(BigDecimal.ZERO)
                .totalIncome(BigDecimal.ZERO)
                .totalExpense(BigDecimal.ZERO)
                .passwordErrorCount(0)
                .build();
            userAccountMapper.insert(account);
            log.info("创建用户账户: userId={}", userId);
        }

        return account;
    }

    /**
     * 增加密码错误次数
     */
    private void incrementPasswordErrorCount(Long userId, UserAccount account) {
        String errorKey = PWD_ERROR_KEY + userId;
        Integer errorCount = RedisUtils.getCacheObject(errorKey);
        errorCount = errorCount == null ? 1 : errorCount + 1;

        RedisUtils.setCacheObject(errorKey, errorCount, PWD_LOCK_DURATION);

        if (errorCount >= MAX_PWD_ERROR_COUNT) {
            // 锁定账户
            account.setPasswordLockedUntil(LocalDateTime.now().plus(PWD_LOCK_DURATION));
            userAccountMapper.updateById(account);
            log.warn("支付密码错误次数过多，账户已锁定: userId={}", userId);
        }
    }

    /**
     * 重置密码错误次数
     */
    private void resetPasswordErrorCount(Long userId, UserAccount account) {
        String errorKey = PWD_ERROR_KEY + userId;
        RedisUtils.deleteObject(errorKey);

        if (account.getPasswordLockedUntil() != null) {
            account.setPasswordLockedUntil(null);
            userAccountMapper.updateById(account);
        }
    }

    /**
     * 生成交易流水号
     */
    private String generateTransactionNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String timestamp = LocalDateTime.now().format(formatter);
        int random = new Random().nextInt(9000) + 1000;
        return "TXN" + timestamp + random;
    }
}
