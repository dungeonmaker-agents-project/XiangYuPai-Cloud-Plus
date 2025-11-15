package org.dromara.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 测试基类
 * Base Test Class for all tests
 *
 * 提供通用的测试配置和工具方法
 * Provides common test configuration and utility methods
 *
 * @author XiangYuPai
 * @since 2025-11-15
 */
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseTest {

    @Autowired
    protected MockMvc mockMvc;

    /**
     * 测试用户ID
     */
    protected static final Long TEST_USER_ID = 1000L;
    protected static final Long TEST_OTHER_USER_ID = 2000L;
    protected static final Long TEST_SKILL_ID = 3000L;

    /**
     * 测试Token
     */
    protected static final String TEST_TOKEN = "test-token-123456";
    protected static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * 每个测试方法执行前的准备工作
     */
    @BeforeEach
    public void setUp() {
        // 子类可以重写此方法添加自定义初始化逻辑
    }

    /**
     * 获取认证Header
     *
     * @return Authorization header value
     */
    protected String getAuthHeader() {
        return "Bearer " + TEST_TOKEN;
    }

    /**
     * 生成测试用的JSON字符串
     *
     * @param object 对象
     * @return JSON字符串
     */
    protected String toJson(Object object) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }
}
