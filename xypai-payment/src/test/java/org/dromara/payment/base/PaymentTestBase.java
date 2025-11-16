package org.dromara.payment.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Payment Module Test Base Class
 * 支付模块测试基类
 *
 * <p>Provides common test configuration and utilities for payment module tests
 * <p>为支付模块测试提供通用配置和工具方法
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // Rollback after each test
public abstract class PaymentTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // ==================== Test Constants ====================

    /**
     * Test User IDs
     */
    protected static final Long TEST_USER_ID = 1L;
    protected static final Long TEST_OTHER_USER_ID = 2L;

    /**
     * Test Order IDs
     */
    protected static final String TEST_ORDER_ID = "1234567890";
    protected static final String TEST_ORDER_NO = "20251114123456001";

    /**
     * Test Authentication
     */
    protected static final String TEST_TOKEN = "test-token-12345678";
    protected static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Test Payment Amount (matching frontend docs)
     */
    protected static final BigDecimal TEST_PAYMENT_AMOUNT = new BigDecimal("10.50");
    protected static final BigDecimal TEST_LARGE_AMOUNT = new BigDecimal("1000.00");

    /**
     * Test User Balance
     */
    protected static final BigDecimal TEST_INITIAL_BALANCE = new BigDecimal("100.00");
    protected static final BigDecimal TEST_BALANCE_AFTER_PAYMENT = new BigDecimal("89.50"); // 100 - 10.50
    protected static final BigDecimal TEST_INSUFFICIENT_BALANCE = new BigDecimal("5.00");

    /**
     * Test Payment Password
     */
    protected static final String TEST_PAYMENT_PASSWORD = "123456";
    protected static final String TEST_WRONG_PASSWORD = "wrong123";

    /**
     * Password Security Constants
     */
    protected static final int MAX_PASSWORD_ATTEMPTS = 5;
    protected static final int LOCKOUT_DURATION_MINUTES = 30;

    /**
     * Payment Method Constants
     */
    protected static final String PAYMENT_METHOD_BALANCE = "balance";
    protected static final String PAYMENT_METHOD_ALIPAY = "alipay";
    protected static final String PAYMENT_METHOD_WECHAT = "wechat";

    /**
     * Payment Status Constants
     */
    protected static final String STATUS_SUCCESS = "success";
    protected static final String STATUS_FAILED = "failed";
    protected static final String STATUS_PENDING = "pending";
    protected static final String STATUS_REQUIRE_PASSWORD = "require_password";

    // ==================== Setup Methods ====================

    /**
     * Setup before each test
     * 每个测试前的准备工作
     */
    @BeforeEach
    public void setUp() {
        // Subclasses can override to add custom initialization
        // 子类可以重写以添加自定义初始化逻辑
    }

    // ==================== Utility Methods ====================

    /**
     * Get Authorization header value
     *
     * @return Bearer token
     */
    protected String getAuthHeader() {
        return "Bearer " + TEST_TOKEN;
    }

    /**
     * Convert object to JSON string
     *
     * @param object Object to convert
     * @return JSON string
     */
    protected String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    /**
     * Convert JSON string to object
     *
     * @param json JSON string
     * @param clazz Target class
     * @return Converted object
     */
    protected <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }

    /**
     * Calculate expected balance after payment
     *
     * @param initialBalance Initial balance
     * @param paymentAmount Payment amount
     * @return Balance after payment
     */
    protected BigDecimal calculateBalanceAfterPayment(BigDecimal initialBalance, BigDecimal paymentAmount) {
        return initialBalance.subtract(paymentAmount).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Check if balance is sufficient
     *
     * @param balance Current balance
     * @param amount Required amount
     * @return true if sufficient
     */
    protected boolean isSufficientBalance(BigDecimal balance, BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }

    /**
     * Generate expected error message for password attempts
     *
     * @param attemptCount Current attempt count
     * @return Error message
     */
    protected String getPasswordErrorMessage(int attemptCount) {
        int remaining = MAX_PASSWORD_ATTEMPTS - attemptCount;
        if (remaining > 0) {
            return "支付密码错误，还剩" + remaining + "次机会";
        } else {
            return "账户已锁定，请" + LOCKOUT_DURATION_MINUTES + "分钟后再试";
        }
    }

    /**
     * Sleep for milliseconds (for lockout tests)
     *
     * @param millis Milliseconds to sleep
     */
    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
