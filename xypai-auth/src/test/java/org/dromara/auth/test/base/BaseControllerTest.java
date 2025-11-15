package org.dromara.auth.test.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Base Test Class for Controller Tests
 *
 * <p>提供通用测试方法和配置</p>
 *
 * @author XyPai Team
 * @date 2025-11-14
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * 测试用户Token
     */
    protected String testUserToken;

    /**
     * 测试用户ID
     */
    protected Long testUserId = 1001L;

    @BeforeEach
    public void setUp() {
        // 子类可以覆盖此方法进行额外初始化
    }

    /**
     * POST请求辅助方法
     *
     * @param url 请求URL
     * @param requestBody 请求体对象
     * @return ResultActions
     */
    protected ResultActions performPost(String url, Object requestBody) throws Exception {
        return mockMvc.perform(post(url)
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody)));
    }

    /**
     * POST请求辅助方法（携带Token）
     *
     * @param url 请求URL
     * @param requestBody 请求体对象
     * @param token Authorization Token
     * @return ResultActions
     */
    protected ResultActions performPostWithAuth(String url, Object requestBody, String token) throws Exception {
        return mockMvc.perform(post(url)
            .contentType(APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content(objectMapper.writeValueAsString(requestBody)));
    }

    /**
     * GET请求辅助方法
     *
     * @param url 请求URL
     * @return ResultActions
     */
    protected ResultActions performGet(String url) throws Exception {
        return mockMvc.perform(get(url)
            .contentType(APPLICATION_JSON));
    }

    /**
     * GET请求辅助方法（携带Token）
     *
     * @param url 请求URL
     * @param token Authorization Token
     * @return ResultActions
     */
    protected ResultActions performGetWithAuth(String url, String token) throws Exception {
        return mockMvc.perform(get(url)
            .contentType(APPLICATION_JSON)
            .header("Authorization", "Bearer " + token));
    }

    /**
     * 从响应中提取Token
     *
     * @param resultActions MockMvc ResultActions
     * @return Token字符串
     */
    protected String extractToken(ResultActions resultActions) throws Exception {
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        // 简化的Token提取，实际可以使用JsonPath
        return objectMapper.readTree(responseBody)
            .path("data")
            .path("accessToken")
            .asText();
    }

    /**
     * 打印响应内容（调试用）
     *
     * @param resultActions MockMvc ResultActions
     */
    protected void printResponse(ResultActions resultActions) throws Exception {
        String response = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("=== Response ===");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
            objectMapper.readTree(response)
        ));
        System.out.println("================");
    }
}
