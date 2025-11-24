package org.dromara.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Base Integration Test Class
 * Provides common test setup and utilities for all integration tests
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Test user ID
     */
    protected static final Long TEST_USER_ID = 100001L;

    /**
     * Test provider ID
     */
    protected static final Long TEST_PROVIDER_ID = 10001L;

    /**
     * Test service ID
     */
    protected static final Long TEST_SERVICE_ID = 101L;

    /**
     * Test token for authentication
     */
    protected String testToken;

    /**
     * Test payment password
     */
    protected static final String TEST_PAYMENT_PASSWORD = "123456";

    /**
     * Test balance amount
     */
    protected static final BigDecimal TEST_BALANCE = new BigDecimal("1000.00");

    @BeforeEach
    public void setUp() {
        // Generate test token (mock Sa-Token authentication)
        testToken = "Bearer test-token-" + System.currentTimeMillis();
    }

    /**
     * Convert object to JSON string
     */
    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * Convert JSON string to object
     */
    protected <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }

    /**
     * Sleep for a short time (for async operations)
     */
    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
