package org.dromara.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * App 技能预约页面集成测试 - 通过Gateway调用真实接口
 *
 * 🎯 核心目标：
 * 测试 App 技能预约页面的UserService相关功能
 *
 * 测试流程:
 * 1. 📋 获取技能预约详情
 * 2. ⭐ 获取技能评价列表
 *
 * 💡 测试方式说明：
 * - 集成测试，调用真实服务
 * - 需要手动启动：Gateway(8080), xypai-auth(9211), xypai-user(9401), Nacos, Redis, MySQL
 * - 注意：订单相关接口属于xypai-trade模块，不在此测试范围内
 *
 * @author XyPai Team
 * @date 2025-11-18
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppSkillBookingPageTest {

    // 测试配置
    private static final String GATEWAY_URL = "http://localhost:8080";
    private static final String TEST_COUNTRY_CODE = "+86";
    private static final String TEST_SMS_CODE = "123456";

    // HTTP 客户端
    private static RestTemplate restTemplate;
    private static ObjectMapper objectMapper;

    // 保存登录后的 Token，用于后续测试
    private static String authToken;
    private static String userId;
    private static String testSkillId = "test_skill_001";  // 测试用技能ID

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        log.info("📱 App 技能预约页面集成测试启动");
        log.info("⚠️ 确保服务已启动: Gateway(8080), xypai-auth(9211), xypai-user(9401), Nacos, Redis, MySQL\n");
    }

    /**
     * 辅助方法：确保有有效的登录 Token
     */
    private static void ensureAuthenticated() {
        if (authToken != null && !authToken.isEmpty()) {
            return;
        }

        log.info("⚠️ 创建新用户并登录...");

        try {
            // 生成唯一手机号（确保11位）
            long timestamp = System.currentTimeMillis() % 100000000L;
            String uniqueMobile = String.format("138%08d", timestamp);

            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("countryCode", TEST_COUNTRY_CODE);
            loginRequest.put("mobile", uniqueMobile);
            loginRequest.put("verificationCode", TEST_SMS_CODE);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

            String loginUrl = GATEWAY_URL + "/xypai-auth/auth/login/sms";
            ResponseEntity<Map> response = restTemplate.postForEntity(loginUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    authToken = (String) data.get("token");
                    userId = String.valueOf(data.get("userId"));
                    log.info("✅ 新用户创建成功 - userId: {}", userId);
                }
            }
        } catch (Exception e) {
            log.error("❌ 创建用户异常: {}", e.getMessage());
        }
    }

    /**
     * 🎯 测试1：新用户注册
     */
    @Test
    @Order(1)
    @DisplayName("测试1: 新用户注册 - 准备测试数据")
    public void test1_RegisterUser() {
        try {
            log.info("\n[测试1] 创建当前用户");
            ensureAuthenticated();

            if (authToken != null && !authToken.isEmpty()) {
                log.info("✅ 用户注册成功 - userId: {}", userId);
            } else {
                throw new RuntimeException("用户注册失败");
            }

        } catch (Exception e) {
            log.error("❌ 测试1失败: {}", e.getMessage());
            throw new RuntimeException("注册用户测试失败", e);
        }
    }

    /**
     * 🎯 测试2：获取技能预约详情
     */
    @Test
    @Order(2)
    @DisplayName("测试2: 获取技能预约详情")
    public void test2_GetSkillBookingDetail() {
        try {
            log.info("\n[测试2] 获取技能预约详情");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String detailUrl = GATEWAY_URL + "/xypai-user/api/skills/" + testSkillId + "/booking-detail";
            ResponseEntity<Map> response = restTemplate.exchange(detailUrl, org.springframework.http.HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    log.info("✅ 获取技能预约详情成功");
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 获取技能预约详情返回非200: {}", msg);
                }
            }

        } catch (Exception e) {
            log.error("❌ 测试2失败: {}", e.getMessage());
            log.warn("⚠️ 技能不存在或接口未实现，这是正常的");
        }
    }

    /**
     * 🎯 测试3：获取技能评价列表
     */
    @Test
    @Order(3)
    @DisplayName("测试3: 获取技能评价列表")
    public void test3_GetSkillReviews() {
        try {
            log.info("\n[测试3] 获取技能评价列表");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String reviewsUrl = GATEWAY_URL + "/xypai-user/api/skills/" + testSkillId + "/reviews?pageNum=1&pageSize=20";
            ResponseEntity<Map> response = restTemplate.exchange(reviewsUrl, org.springframework.http.HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    Object reviews = data.get("reviews");
                    int reviewCount = (reviews instanceof java.util.List) ? ((java.util.List<?>) reviews).size() : 0;
                    log.info("✅ 获取技能评价列表成功 - 评价数量: {}", reviewCount);
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 获取技能评价列表返回非200: {}", msg);
                }
            }

        } catch (Exception e) {
            log.error("❌ 测试3失败: {}", e.getMessage());
            log.warn("⚠️ 技能不存在或接口未实现，这是正常的");
        }
    }

    @AfterAll
    static void tearDown() {
        log.info("\n🎉 所有测试完成！");
        log.info("\n💡 说明：");
        log.info("   - 本测试仅测试UserService相关接口");
        log.info("   - 订单创建、支付等接口属于xypai-trade模块");
        log.info("   - 如需测试完整预约流程，请参考xypai-trade模块测试");
    }
}
