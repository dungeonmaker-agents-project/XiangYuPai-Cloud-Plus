package org.dromara.common.support;

import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;

/**
 * API测试基类
 * <p>
 * 提供API集成测试的通用功能:
 * - RestAssured配置
 * - 测试用户Token生成
 * - 测试数据清理
 * - 通用断言方法
 *
 * @author XiangYuPai Team
 * @since 2025-11-14
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class ApiTestBase {

    @LocalServerPort
    protected int serverPort;

    @Autowired
    protected TestDataBuilder dataBuilder;

    @Autowired
    protected TestDataCleaner dataCleaner;

    /**
     * 测试用户Token
     */
    protected String userToken;

    /**
     * 测试用户ID
     */
    protected Long testUserId = 1001L;

    /**
     * 另一个测试用户ID
     */
    protected Long anotherUserId = 1002L;

    @BeforeEach
    void setUpApiTest() {
        // 配置RestAssured
        RestAssured.port = serverPort;
        RestAssured.basePath = "";
        RestAssured.config = RestAssuredConfig.config()
            .objectMapperConfig(new ObjectMapperConfig().defaultObjectMapperType(ObjectMapperConfig.ObjectMapperType.JACKSON_2));

        // 生成测试用户Token
        userToken = generateTestUserToken(testUserId);

        // 清理测试数据
        dataCleaner.cleanAll();
    }

    /**
     * 生成测试用户Token
     * <p>
     * 这里应该调用实际的登录接口或使用测试专用的Token生成方法
     *
     * @param userId 用户ID
     * @return JWT Token
     */
    protected String generateTestUserToken(Long userId) {
        // TODO: 实际实现中，这里应该调用登录接口或使用测试专用的Token生成
        // 临时实现: 返回模拟Token
        return "test_token_user_" + userId;
    }

    /**
     * 创建带认证的请求
     *
     * @return RequestSpecification
     */
    protected RequestSpecification authenticatedRequest() {
        return given()
            .header("Authorization", "Bearer " + userToken)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON);
    }

    /**
     * 创建带特定用户Token的请求
     *
     * @param token 用户Token
     * @return RequestSpecification
     */
    protected RequestSpecification authenticatedRequest(String token) {
        return given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON);
    }

    /**
     * 创建未认证的请求
     *
     * @return RequestSpecification
     */
    protected RequestSpecification unauthenticatedRequest() {
        return given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON);
    }

    /**
     * 等待一段时间
     * <p>
     * 用于测试异步操作或缓存更新
     *
     * @param milliseconds 毫秒数
     */
    protected void waitFor(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted", e);
        }
    }
}
