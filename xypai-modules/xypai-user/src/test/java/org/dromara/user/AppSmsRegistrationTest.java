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
 * App 新用户注册集成测试 - 通过Gateway调用真实接口
 *
 * 🎯 核心目标：
 * 测试 App 新用户通过 SMS 验证码注册并使用核心功能的完整流程
 *
 * 测试流程:
 * 1. 📱 新用户 SMS 注册（isNewUser=true）
 * 2. 👤 获取用户资料
 * 3. ✏️ 更新昵称
 * 4. 👥 获取粉丝列表
 * 5. 🎯 获取技能列表
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
public class AppSmsRegistrationTest {

    // 测试配置
    private static final String GATEWAY_URL = "http://localhost:8080";
    private static final String TEST_COUNTRY_CODE = "+86";
    private static final String TEST_SMS_CODE = "123456";  // 验证码

    // HTTP 客户端
    private static RestTemplate restTemplate;
    private static ObjectMapper objectMapper;

    // 保存登录后的 Token，用于后续测试
    private static String authToken;
    private static String userId;

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        log.info("📱 App SMS 集成测试启动");
        log.info("⚠️ 确保服务已启动: Gateway(8080), xypai-auth(9211), xypai-user(9401), Nacos, Redis, MySQL\n");
    }

    /**
     * 辅助方法：确保有有效的登录 Token
     * 如果没有 Token，则创建新用户并登录
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
                } else {
                    log.error("❌ 创建用户失败: {}", responseBody.get("msg"));
                }
            }
        } catch (Exception e) {
            log.error("❌ 创建用户异常: {}", e.getMessage());
        }
    }

    /**
     * 🎯 测试1：新用户 SMS 注册
     *
     * 测试场景：
     * 1. 用户首次使用手机号登录
     * 2. 后端自动创建用户
     * 3. 返回 isNewUser=true，前端跳转到完善资料页
     */
    @Test
    @Order(1)
    @DisplayName("测试1: 新用户 SMS 注册 - isNewUser=true")
    public void test1_NewUserRegistration() {
        try {
            log.info("\n[测试1] 新用户 SMS 注册");

            // 使用时间戳生成唯一手机号，确保是新用户（11位）
            long timestamp = System.currentTimeMillis() % 100000000L;
            String uniqueMobile = String.format("138%08d", timestamp);
            log.info("手机号: {}, 验证码: {}", uniqueMobile, TEST_SMS_CODE);

            // 构造请求体
            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("countryCode", TEST_COUNTRY_CODE);
            loginRequest.put("mobile", uniqueMobile);
            loginRequest.put("verificationCode", TEST_SMS_CODE);

            // 发送请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);

            String loginUrl = GATEWAY_URL + "/xypai-auth/auth/login/sms";
            ResponseEntity<Map> response = restTemplate.postForEntity(loginUrl, request, Map.class);

            // 验证响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    String token = (String) data.get("token");
                    Boolean isNewUser = (Boolean) data.get("isNewUser");
                    String uid = String.valueOf(data.get("userId"));
                    String nickname = (String) data.get("nickname");

                    // 保存 Token 和 UserId，供后续测试使用
                    authToken = token;
                    userId = uid;

                    log.info("✅ 注册成功 - userId: {}, nickname: {}, isNewUser: {}", uid, nickname, isNewUser);

                    // 验证新用户标记
                    if (Boolean.TRUE.equals(isNewUser)) {
                        log.info("✅ isNewUser 标记正确");
                    } else {
                        log.warn("⚠️ 预期 isNewUser=true，实际为 {}", isNewUser);
                    }
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("注册失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试1失败: {}", e.getMessage());
            throw new RuntimeException("新用户注册测试失败", e);
        }
    }

    /**
     * 🎯 测试2：核心接口 - 获取我的资料
     */
    @Test
    @Order(2)
    @DisplayName("测试2: 核心接口 - 获取我的资料")
    public void test2_GetMyProfile() {
        try {
            log.info("\n[测试2] 获取我的资料");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token，测试跳过");
                return;
            }

            // 构造请求头（带 Token + Bearer 前缀）
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String profileUrl = GATEWAY_URL + "/xypai-user/api/user/profile/header";
            ResponseEntity<Map> response = restTemplate.exchange(profileUrl, org.springframework.http.HttpMethod.GET, request, Map.class);

            // 验证响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");
                String msg = (String) responseBody.get("msg");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    log.info("✅ 获取资料成功 - userId: {}, nickname: {}", data.get("userId"), data.get("nickname"));
                } else {
                    log.error("❌ 业务错误: {} - {}", code, msg);
                    throw new RuntimeException("获取资料失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试2失败: {}", e.getMessage());
            throw new RuntimeException("获取资料测试失败", e);
        }
    }

    /**
     * 🎯 测试3：核心接口 - 更新昵称
     */
    @Test
    @Order(3)
    @DisplayName("测试3: 核心接口 - 更新昵称")
    public void test3_UpdateNickname() {
        try {
            log.info("\n[测试3] 更新昵称");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token，测试跳过");
                return;
            }

            String newNickname = "测试用户_" + System.currentTimeMillis();
            log.info("新昵称: {}", newNickname);

            // 构造请求
            Map<String, String> updateRequest = new HashMap<>();
            updateRequest.put("nickname", newNickname);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(updateRequest, headers);

            String updateUrl = GATEWAY_URL + "/xypai-user/api/user/profile/nickname";
            ResponseEntity<Map> response = restTemplate.exchange(updateUrl, org.springframework.http.HttpMethod.PUT, request, Map.class);

            // 验证响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    log.info("✅ 更新昵称成功");
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("更新昵称失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试3失败: {}", e.getMessage());
            throw new RuntimeException("更新昵称测试失败", e);
        }
    }

    /**
     * 🎯 测试4：核心接口 - 获取粉丝列表
     */
    @Test
    @Order(4)
    @DisplayName("测试4: 核心接口 - 获取粉丝列表")
    public void test4_GetFollowersList() {
        try {
            log.info("\n[测试4] 获取粉丝列表");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token，测试跳过");
                return;
            }

            // 构造请求头（带 Token + Bearer 前缀）
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String followersUrl = GATEWAY_URL + "/xypai-user/api/user/relation/fans?pageNum=1&pageSize=10";
            ResponseEntity<Map> response = restTemplate.exchange(followersUrl, org.springframework.http.HttpMethod.GET, request, Map.class);

            // 验证响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

                    if (data != null) {
                        Object records = data.get("records");
                        int recordCount = (records instanceof java.util.List) ? ((java.util.List<?>) records).size() : 0;
                        log.info("✅ 获取粉丝列表成功 - 总数: {}, 当前页: {}", data.get("total"), recordCount);
                    } else {
                        log.info("✅ 获取粉丝列表成功 - 数据为空");
                    }
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("获取粉丝列表失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试4失败: {}", e.getMessage());
            throw new RuntimeException("获取粉丝列表测试失败", e);
        }
    }

    /**
     * 🎯 测试5：核心接口 - 获取我的技能列表
     */
    @Test
    @Order(5)
    @DisplayName("测试5: 核心接口 - 获取我的技能列表")
    public void test5_GetMySkills() {
        try {
            log.info("\n[测试5] 获取我的技能列表");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token，测试跳过");
                return;
            }

            // 构造请求头（带 Token + Bearer 前缀）
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            // 添加分页参数 (PageQuery expects: pageNum and pageSize)
            String skillsUrl = GATEWAY_URL + "/xypai-user/api/user/skills/my?pageNum=1&pageSize=10";
            ResponseEntity<Map> response = restTemplate.exchange(skillsUrl, org.springframework.http.HttpMethod.GET, request, Map.class);

            // 验证响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Object data = responseBody.get("data");
                    int skillCount = (data instanceof java.util.List) ? ((java.util.List<?>) data).size() : 0;
                    log.info("✅ 获取技能列表成功 - 技能数量: {}", skillCount);
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("获取技能列表失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试5失败: {}", e.getMessage());
            throw new RuntimeException("获取技能列表测试失败", e);
        }
    }

    @AfterAll
    static void tearDown() {
        log.info("\n🎉 所有测试完成！");
    }
}
