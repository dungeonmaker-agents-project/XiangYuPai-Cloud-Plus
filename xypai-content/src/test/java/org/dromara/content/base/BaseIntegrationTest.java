package org.dromara.content.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dromara.content.XyPaiContentApplication;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base Integration Test Class
 * <p>
 * Provides common setup for all integration tests:
 * - Spring Boot context loading
 * - MockMvc for HTTP testing
 * - Transaction rollback after each test
 * - Common test utilities
 * <p>
 * All page and flow tests should extend this class.
 *
 * @author Claude Code AI
 * @date 2025-11-14
 */
@SpringBootTest(
    classes = XyPaiContentApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // Rollback after each test
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected TestDataFactory testDataFactory;

    /**
     * Mock authentication token for test user
     * In real tests, this should be obtained from AuthService
     */
    protected String testUserToken;
    protected Long testUserId = 1001L;

    /**
     * Setup before each test
     * - Initialize test data
     * - Setup mock authentication
     */
    @BeforeEach
    void setUp() {
        // TODO: Call AuthService to get real token
        // For now, use mock token
        testUserToken = "mock-test-token-" + testUserId;

        // Setup test data if needed
        setupTestData();
    }

    /**
     * Override this method in subclasses to setup specific test data
     */
    protected void setupTestData() {
        // Default: no additional setup
    }

    /**
     * Helper: Convert object to JSON string
     */
    protected String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * Helper: Parse JSON string to object
     */
    protected <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }

    /**
     * Helper: Get auth header with token
     */
    protected String getAuthHeader() {
        return testUserToken;
    }

    /**
     * Helper: Sleep for rate limiting tests
     */
    protected void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
