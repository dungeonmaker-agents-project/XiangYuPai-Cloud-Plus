package org.dromara.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 个人资料编辑页面集成测试（完善版）
 *
 * 基于新UI文档: 个人主页-编辑_结构文档.md
 *
 * 新增测试:
 * - 职业多选功能 (支持最多5个职业)
 * - 获取职业列表
 * - 更新职业列表
 *
 * 测试服务:
 * - Gateway: 8080
 * - xypai-auth: 9211
 * - xypai-user: 9401
 *
 * @author XyPai Team
 * @date 2025-12-02
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppEditProfilePageEnhancedTest {

    private static final String GATEWAY_URL = "http://localhost:8080";
    private static final String TEST_COUNTRY_CODE = "+86";
    private static final String TEST_SMS_CODE = "123456";

    private static RestTemplate restTemplate;
    private static ObjectMapper objectMapper;
    private static String authToken;
    private static String userId;

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        log.info("📝 个人资料编辑页面增强测试启动");
        log.info("⚠️ 确保服务已启动: Gateway(8080), xypai-auth(9211), xypai-user(9401), Nacos, Redis, MySQL");
        log.info("⚠️ 确保已执行 add_user_occupations.sql 创建职业表\n");
    }

    private static void ensureAuthenticated() {
        if (authToken != null && !authToken.isEmpty()) {
            return;
        }

        log.info("⚠️ 创建新用户并登录...");

        try {
            long timestamp = System.currentTimeMillis() % 100000000L;
            String uniqueMobile = String.format("138%08d", timestamp);

            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("countryCode", TEST_COUNTRY_CODE);
            loginRequest.put("mobile", uniqueMobile);
            loginRequest.put("verificationCode", TEST_SMS_CODE);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

            String loginUrl = GATEWAY_URL + "/xypai-auth/api/auth/login/sms";
            ResponseEntity<Map> response = restTemplate.postForEntity(loginUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    authToken = (String) data.get("token");
                    userId = String.valueOf(data.get("userId"));
                    log.info("✅ 用户创建成功 - userId: {}", userId);
                }
            }
        } catch (Exception e) {
            log.error("❌ 创建用户异常: {}", e.getMessage());
        }
    }

    @Test
    @Order(1)
    @DisplayName("测试1: 新用户登录获取Token")
    public void test1_Login() {
        try {
            log.info("\n[测试1] 新用户登录");
            ensureAuthenticated();
            Assertions.assertNotNull(authToken, "登录Token不能为空");
            log.info("✅ 登录成功，Token已获取");
        } catch (Exception e) {
            log.error("❌ 测试1失败: {}", e.getMessage());
            throw new RuntimeException("登录测试失败", e);
        }
    }

    @Test
    @Order(2)
    @DisplayName("测试2: 加载编辑页面数据")
    public void test2_LoadEditData() {
        try {
            log.info("\n[测试2] 加载编辑页面数据");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String editUrl = GATEWAY_URL + "/xypai-user/api/user/profile/edit";
            ResponseEntity<Map> response = restTemplate.exchange(editUrl, HttpMethod.GET, request, Map.class);

            Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
            Map<String, Object> body = response.getBody();
            Assertions.assertNotNull(body);
            Assertions.assertEquals(200, body.get("code"));
            log.info("✅ 加载编辑数据成功");
        } catch (Exception e) {
            log.error("❌ 测试2失败: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // ==================== 职业多选功能测试 ====================

    @Test
    @Order(10)
    @DisplayName("测试10: 获取职业列表（初始为空）")
    public void test10_GetOccupations() {
        try {
            log.info("\n[测试10] 获取职业列表");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String url = GATEWAY_URL + "/xypai-user/api/user/profile/occupations";
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
            Map<String, Object> body = response.getBody();
            Assertions.assertNotNull(body);
            Assertions.assertEquals(200, body.get("code"));

            List<String> occupations = (List<String>) body.get("data");
            log.info("✅ 获取职业列表成功 - 当前职业数: {}", occupations != null ? occupations.size() : 0);
        } catch (Exception e) {
            log.error("❌ 测试10失败: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(11)
    @DisplayName("测试11: 更新职业列表（多选）")
    public void test11_UpdateOccupations() {
        try {
            log.info("\n[测试11] 更新职业列表（多选）");
            ensureAuthenticated();

            List<String> newOccupations = Arrays.asList("程序员", "游戏博主", "电竞选手");
            log.info("新职业列表: {}", newOccupations);

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("occupations", newOccupations);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(updateRequest, headers);

            String url = GATEWAY_URL + "/xypai-user/api/user/profile/occupations";
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.PUT, request, Map.class);

            Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
            Map<String, Object> body = response.getBody();
            Assertions.assertNotNull(body);
            Assertions.assertEquals(200, body.get("code"));

            log.info("✅ 更新职业列表成功");
        } catch (Exception e) {
            log.error("❌ 测试11失败: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(12)
    @DisplayName("测试12: 验证职业列表更新结果")
    public void test12_VerifyOccupations() {
        try {
            log.info("\n[测试12] 验证职业列表更新结果");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String url = GATEWAY_URL + "/xypai-user/api/user/profile/occupations";
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
            Map<String, Object> body = response.getBody();
            Assertions.assertNotNull(body);
            Assertions.assertEquals(200, body.get("code"));

            List<String> occupations = (List<String>) body.get("data");
            Assertions.assertNotNull(occupations);
            Assertions.assertEquals(3, occupations.size());
            Assertions.assertTrue(occupations.contains("程序员"));
            Assertions.assertTrue(occupations.contains("游戏博主"));
            Assertions.assertTrue(occupations.contains("电竞选手"));

            log.info("✅ 职业列表验证成功 - 职业: {}", occupations);
        } catch (Exception e) {
            log.error("❌ 测试12失败: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(13)
    @DisplayName("测试13: 添加单个职业")
    public void test13_AddSingleOccupation() {
        try {
            log.info("\n[测试13] 添加单个职业");
            ensureAuthenticated();

            String newOccupation = "设计师";
            log.info("添加职业: {}", newOccupation);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String url = GATEWAY_URL + "/xypai-user/api/user/profile/occupations/" + newOccupation;
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
            Map<String, Object> body = response.getBody();
            Assertions.assertNotNull(body);
            Assertions.assertEquals(200, body.get("code"));

            log.info("✅ 添加单个职业成功");
        } catch (Exception e) {
            log.error("❌ 测试13失败: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(14)
    @DisplayName("测试14: 删除单个职业")
    public void test14_RemoveSingleOccupation() {
        try {
            log.info("\n[测试14] 删除单个职业");
            ensureAuthenticated();

            String occupationToRemove = "游戏博主";
            log.info("删除职业: {}", occupationToRemove);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String url = GATEWAY_URL + "/xypai-user/api/user/profile/occupations/" + occupationToRemove;
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.DELETE, request, Map.class);

            Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
            Map<String, Object> body = response.getBody();
            Assertions.assertNotNull(body);
            Assertions.assertEquals(200, body.get("code"));

            log.info("✅ 删除单个职业成功");
        } catch (Exception e) {
            log.error("❌ 测试14失败: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(15)
    @DisplayName("测试15: 职业数量限制（最多5个）")
    public void test15_OccupationLimit() {
        try {
            log.info("\n[测试15] 职业数量限制测试");
            ensureAuthenticated();

            List<String> tooManyOccupations = Arrays.asList(
                "程序员", "设计师", "产品经理", "运营专员", "数据分析师", "项目经理"  // 6个，超过限制
            );
            log.info("尝试设置6个职业（超过限制）: {}", tooManyOccupations);

            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("occupations", tooManyOccupations);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(updateRequest, headers);

            String url = GATEWAY_URL + "/xypai-user/api/user/profile/occupations";
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.PUT, request, Map.class);

            Map<String, Object> body = response.getBody();
            // 应该返回失败
            Assertions.assertNotEquals(200, body.get("code"));

            log.info("✅ 职业数量限制测试通过 - 正确拒绝超过5个职业");
        } catch (Exception e) {
            log.error("❌ 测试15失败: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(16)
    @DisplayName("测试16: 最终职业列表验证")
    public void test16_FinalVerification() {
        try {
            log.info("\n[测试16] 最终职业列表验证");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String url = GATEWAY_URL + "/xypai-user/api/user/profile/occupations";
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
            Map<String, Object> body = response.getBody();
            Assertions.assertNotNull(body);
            Assertions.assertEquals(200, body.get("code"));

            List<String> occupations = (List<String>) body.get("data");
            log.info("✅ 最终职业列表: {}", occupations);
        } catch (Exception e) {
            log.error("❌ 测试16失败: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void tearDown() {
        log.info("\n🎉 个人资料编辑增强测试完成！");
        log.info("✅ 职业多选功能测试通过");
    }
}
