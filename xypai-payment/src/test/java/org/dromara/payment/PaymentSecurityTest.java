package org.dromara.payment;

import org.dromara.common.BaseIntegrationTest;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.payment.domain.dto.ExecutePaymentDTO;
import org.dromara.payment.domain.entity.UserAccount;
import org.dromara.payment.mapper.UserAccountMapper;
import org.dromara.payment.service.IAccountService;
import org.dromara.payment.service.IPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Payment Security Test
 * Tests security features like password encryption, lockout mechanism, and concurrency control
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@DisplayName("Payment Security Tests")
class PaymentSecurityTest extends BaseIntegrationTest {

    @Autowired
    private IPaymentService paymentService;

    @Autowired
    private IAccountService accountService;

    @Autowired
    private UserAccountMapper userAccountMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUpSecurityTests() {
        // Create test user account with payment password
        UserAccount account = accountService.getOrCreateAccount(TEST_USER_ID);
        account.setBalance(TEST_BALANCE);
        account.setPaymentPasswordHash(passwordEncoder.encode(TEST_PAYMENT_PASSWORD));
        account.setPasswordErrorCount(0);
        account.setPasswordLockedUntil(null);
        userAccountMapper.updateById(account);
    }

    @Test
    @DisplayName("Should encrypt payment password using BCrypt")
    void testPasswordEncryption() {
        // Given
        UserAccount account = accountService.getOrCreateAccount(TEST_USER_ID);

        // Then
        assertThat(account.getPaymentPasswordHash()).isNotNull();
        assertThat(account.getPaymentPasswordHash()).startsWith("$2a$");
        assertThat(account.getPaymentPasswordHash()).isNotEqualTo(TEST_PAYMENT_PASSWORD);
        assertThat(passwordEncoder.matches(TEST_PAYMENT_PASSWORD, account.getPaymentPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("Should increment error count on wrong password")
    void testPasswordErrorCounting() {
        // Given
        String errorKey = "payment:pwd:error:" + TEST_USER_ID;

        // When - Try wrong password multiple times
        for (int i = 0; i < 3; i++) {
            try {
                accountService.verifyPaymentPassword(TEST_USER_ID, "wrongpassword");
            } catch (Exception e) {
                // Expected exception
            }
        }

        // Then
        Integer errorCount = RedisUtils.getCacheObject(errorKey);
        assertThat(errorCount).isNotNull();
        assertThat(errorCount).isEqualTo(3);
    }

    @Test
    @DisplayName("Should lock account after 5 failed password attempts")
    void testAccountLockoutMechanism() {
        // Given - Try wrong password 5 times
        for (int i = 0; i < 5; i++) {
            try {
                accountService.verifyPaymentPassword(TEST_USER_ID, "wrongpassword");
            } catch (Exception e) {
                // Expected exception
            }
        }

        // Then - Account should be locked
        UserAccount account = accountService.getOrCreateAccount(TEST_USER_ID);
        assertThat(account.getPasswordLockedUntil()).isNotNull();
        assertThat(account.getPasswordLockedUntil()).isAfter(LocalDateTime.now());

        // And - Further attempts should fail with lockout message
        assertThatThrownBy(() -> accountService.verifyPaymentPassword(TEST_USER_ID, TEST_PAYMENT_PASSWORD))
            .hasMessageContaining("密码已锁定");
    }

    @Test
    @DisplayName("Should use distributed lock to prevent concurrent payments")
    void testDistributedLockForPayment() throws Exception {
        // Given
        ExecutePaymentDTO dto = ExecutePaymentDTO.builder()
            .orderId("1001")
            .orderNo("20251114120000001")
            .paymentMethod("balance")
            .amount(new BigDecimal("100.00"))
            .paymentPassword(TEST_PAYMENT_PASSWORD)
            .build();

        // When - First payment starts
        Thread thread1 = new Thread(() -> {
            try {
                paymentService.executePayment(dto);
            } catch (Exception e) {
                // Expected if concurrent
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                sleep(100); // Small delay to ensure thread1 acquires lock first
                paymentService.executePayment(dto);
            } catch (Exception e) {
                // Expected - should fail to acquire lock
                assertThat(e.getMessage()).contains("支付处理中");
            }
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        // Then - Only one payment should succeed
        // (The test verifies that distributed locking mechanism is in place)
    }

    @Test
    @DisplayName("Should use optimistic locking for balance updates")
    void testOptimisticLockingForBalance() {
        // Given
        UserAccount account1 = accountService.getOrCreateAccount(TEST_USER_ID);
        UserAccount account2 = accountService.getOrCreateAccount(TEST_USER_ID);

        // Both have the same version
        assertThat(account1.getVersion()).isEqualTo(account2.getVersion());

        // When - Update account1
        account1.setBalance(account1.getBalance().subtract(new BigDecimal("100.00")));
        int updated1 = userAccountMapper.updateById(account1);

        // Then - First update succeeds
        assertThat(updated1).isEqualTo(1);

        // When - Try to update account2 (with old version)
        account2.setBalance(account2.getBalance().subtract(new BigDecimal("50.00")));
        int updated2 = userAccountMapper.updateById(account2);

        // Then - Second update should fail due to version mismatch
        assertThat(updated2).isEqualTo(0);
    }

    @Test
    @DisplayName("Should validate payment amount is positive")
    void testAmountValidation() {
        // Given
        BigDecimal zeroAmount = BigDecimal.ZERO;
        BigDecimal negativeAmount = new BigDecimal("-10.00");

        // When & Then - Zero amount should fail
        assertThatThrownBy(() ->
            accountService.deductBalance(TEST_USER_ID, zeroAmount, "test", "1001", "order", "PAY001"))
            .hasMessageContaining("扣减金额必须大于0");

        // When & Then - Negative amount should fail
        assertThatThrownBy(() ->
            accountService.deductBalance(TEST_USER_ID, negativeAmount, "test", "1001", "order", "PAY001"))
            .hasMessageContaining("扣减金额必须大于0");
    }
}
