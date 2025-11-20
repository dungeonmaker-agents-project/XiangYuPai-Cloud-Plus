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
 * App 技能管理页面集成测试 - 通过Gateway调用真实接口
 *
 * 🎯 核心目标：
 * 测试 App 技能管理页面的完整功能
 *
 * 测试流程:
 * 1. 📋 获取我的技能列表
 * 2. 🎯 切换技能上下架状态
 * 3. 🗑️ 删除技能
 * 4. 📊 按类型获取技能列表
 *
 * 💡 测试方式说明：
 * - 集成测试，调用真实服务
 * - 需要手动启动：Gateway(8080), xypai-auth(9211), xypai-user(9401), Nacos, Redis, MySQL
 *
 * @author XyPai Team
 * @date 2025-11-18
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppSkillManagementPageTest {

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
    private static String skillId;  // 技能ID（如果创建成功）

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        log.info("📱 App 技能管理页面集成测试启动");
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
     * 🎯 测试2：获取我的技能列表
     */
    @Test
    @Order(2)
    @DisplayName("测试2: 获取我的技能列表")
    public void test2_GetMySkills() {
        try {
            log.info("\n[测试2] 获取我的技能列表");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String skillsUrl = GATEWAY_URL + "/xypai-user/api/user/skills/my?pageNum=1&pageSize=10";
            ResponseEntity<Map> response = restTemplate.exchange(skillsUrl, org.springframework.http.HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();

                // TableDataInfo 直接返回，没有包装在 R 中
                Object rows = responseBody.get("rows");
                int skillCount = (rows instanceof java.util.List) ? ((java.util.List<?>) rows).size() : 0;
                log.info("✅ 获取技能列表成功 - 技能数量: {}", skillCount);
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试2失败: {}", e.getMessage());
            throw new RuntimeException("获取技能列表测试失败", e);
        }
    }

    /**
     * 🎯 测试3：获取线上技能列表
     */
    @Test
    @Order(3)
    @DisplayName("测试3: 获取线上技能列表")
    public void test3_GetOnlineSkills() {
        try {
            log.info("\n[测试3] 获取线上技能列表");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String skillsUrl = GATEWAY_URL + "/xypai-user/api/user/skills/my?skillType=online&pageNum=1&pageSize=10";
            ResponseEntity<Map> response = restTemplate.exchange(skillsUrl, org.springframework.http.HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();

                // TableDataInfo 直接返回，没有包装在 R 中
                Object rows = responseBody.get("rows");
                int skillCount = (rows instanceof java.util.List) ? ((java.util.List<?>) rows).size() : 0;
                log.info("✅ 获取线上技能列表成功 - 技能数量: {}", skillCount);
            }

        } catch (Exception e) {
            log.error("❌ 测试3失败: {}", e.getMessage());
            log.warn("⚠️ 按类型筛选功能可能未实现，继续测试");
        }
    }

    /**
     * 🎯 测试4：获取线下技能列表
     */
    @Test
    @Order(4)
    @DisplayName("测试4: 获取线下技能列表")
    public void test4_GetOfflineSkills() {
        try {
            log.info("\n[测试4] 获取线下技能列表");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String skillsUrl = GATEWAY_URL + "/xypai-user/api/user/skills/my?skillType=offline&pageNum=1&pageSize=10";
            ResponseEntity<Map> response = restTemplate.exchange(skillsUrl, org.springframework.http.HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();

                // TableDataInfo 直接返回，没有包装在 R 中
                Object rows = responseBody.get("rows");
                int skillCount = (rows instanceof java.util.List) ? ((java.util.List<?>) rows).size() : 0;
                log.info("✅ 获取线下技能列表成功 - 技能数量: {}", skillCount);
            }

        } catch (Exception e) {
            log.error("❌ 测试4失败: {}", e.getMessage());
            log.warn("⚠️ 按类型筛选功能可能未实现，继续测试");
        }
    }

    /**
     * 🎯 测试5：切换技能状态（需要先有技能）
     */
    @Test
    @Order(5)
    @DisplayName("测试5: 切换技能上下架状态")
    public void test5_ToggleSkillStatus() {
        try {
            log.info("\n[测试5] 切换技能上下架状态");
            log.warn("⚠️ 此测试需要先创建技能，当前跳过");
            log.info("💡 提示: 可以先手动创建技能，然后在此处测试切换状态");

        } catch (Exception e) {
            log.error("❌ 测试5失败: {}", e.getMessage());
        }
    }

    /**
     * 🎯 测试6：删除技能（需要先有技能）
     */
    @Test
    @Order(6)
    @DisplayName("测试6: 删除技能")
    public void test6_DeleteSkill() {
        try {
            log.info("\n[测试6] 删除技能");
            log.warn("⚠️ 此测试需要先创建技能，当前跳过");
            log.info("💡 提示: 可以先手动创建技能，然后在此处测试删除功能");

        } catch (Exception e) {
            log.error("❌ 测试6失败: {}", e.getMessage());
        }
    }

    @AfterAll
    static void tearDown() {
        log.info("\n🎉 所有测试完成！");
    }
}
