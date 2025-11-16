package org.dromara.order.base;

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
 * Order Module Test Base Class
 * 订单模块测试基类
 *
 * <p>Provides common test configuration and utilities for order module tests
 * <p>为订单模块测试提供通用配置和工具方法
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // Rollback after each test
public abstract class OrderTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // ==================== Test Constants ====================

    /**
     * Test User IDs
     */
    protected static final Long TEST_USER_ID = 1L;
    protected static final Long TEST_PROVIDER_ID = 2L;
    protected static final Long TEST_OTHER_USER_ID = 3L;

    /**
     * Test Service IDs
     */
    protected static final Long TEST_SERVICE_ID = 101L;
    protected static final Long TEST_INVALID_SERVICE_ID = 999L;

    /**
     * Test Authentication
     */
    protected static final String TEST_TOKEN = "test-token-12345678";
    protected static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Test Price Data (matching frontend docs)
     */
    protected static final BigDecimal UNIT_PRICE = new BigDecimal("10.00");
    protected static final BigDecimal SERVICE_FEE_RATE = new BigDecimal("0.05"); // 5%
    protected static final BigDecimal DEFAULT_SUBTOTAL = new BigDecimal("10.00"); // 10 * 1
    protected static final BigDecimal DEFAULT_SERVICE_FEE = new BigDecimal("0.50"); // 10 * 0.05
    protected static final BigDecimal DEFAULT_TOTAL = new BigDecimal("10.50"); // 10 + 0.50

    /**
     * Test User Balance
     */
    protected static final BigDecimal TEST_USER_BALANCE = new BigDecimal("100.00");
    protected static final BigDecimal TEST_INSUFFICIENT_BALANCE = new BigDecimal("5.00");

    /**
     * Auto-cancel timer (10 minutes in seconds)
     */
    protected static final int AUTO_CANCEL_SECONDS = 600;

    /**
     * Test Payment Password
     */
    protected static final String TEST_PAYMENT_PASSWORD = "123456";
    protected static final String TEST_WRONG_PASSWORD = "wrong123";

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
     * Calculate service fee (5% of subtotal)
     *
     * @param subtotal Subtotal amount
     * @return Service fee
     */
    protected BigDecimal calculateServiceFee(BigDecimal subtotal) {
        return subtotal.multiply(SERVICE_FEE_RATE).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculate total amount (subtotal + service fee)
     *
     * @param subtotal Subtotal amount
     * @return Total amount
     */
    protected BigDecimal calculateTotal(BigDecimal subtotal) {
        BigDecimal serviceFee = calculateServiceFee(subtotal);
        return subtotal.add(serviceFee).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Generate order number format (yyyyMMddHHmmss + 4 random digits)
     * Validation pattern for testing
     *
     * @return Regex pattern for order number
     */
    protected String getOrderNumberPattern() {
        return "\\d{14}\\d{4}"; // 14 digits timestamp + 4 random digits
    }

    /**
     * Sleep for milliseconds (for polling tests)
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
