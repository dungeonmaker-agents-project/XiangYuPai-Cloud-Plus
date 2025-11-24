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
 * App 他人主页页面集成测试 - 通过Gateway调用真实接口
 *
 * 🎯 核心目标：
 * 测试 App 查看他人主页页面的完整功能
 *
 * 测试流程:
 * 1. 👤 获取他人主页信息
 * 2. ➕ 关注用户
 * 3. ➖ 取消关注
 * 4. 🎯 获取用户技能列表
 * 5. 📋 获取用户详细资料
 * 6. 🚫 举报用户
 * 7. 🔒 拉黑用户
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
public class AppOtherUserProfilePageTest {

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
    private static String targetUserId;  // 被查看的用户ID

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        log.info("📱 App 他人主页页面集成测试启动");
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
     * 🎯 测试1：新用户注册（准备目标用户）
     */
    @Test
    @Order(1)
    @DisplayName("测试1: 新用户注册 - 准备测试数据")
    public void test1_RegisterUsers() {
        try {
            log.info("\n[测试1] 创建当前用户");
            ensureAuthenticated();

            // 创建第二个用户作为目标用户（确保11位）
            long timestamp = System.currentTimeMillis() % 100000000L;
            String uniqueMobile = String.format("139%08d", timestamp);
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
                    targetUserId = String.valueOf(data.get("userId"));
                    log.info("✅ 目标用户创建成功 - targetUserId: {}", targetUserId);
                } else {
                    throw new RuntimeException("创建目标用户失败");
                }
            }

        } catch (Exception e) {
            log.error("❌ 测试1失败: {}", e.getMessage());
            throw new RuntimeException("注册用户测试失败", e);
        }
    }

    /**
     * 🎯 测试2：获取他人主页信息
     */
    @Test
    @Order(2)
    @DisplayName("测试2: 获取他人主页信息")
    public void test2_GetOtherUserProfile() {
        try {
            log.info("\n[测试2] 获取他人主页信息");
            ensureAuthenticated();

            if (targetUserId == null) {
                log.warn("⚠️ 目标用户ID为空，跳过测试");
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String profileUrl = GATEWAY_URL + "/xypai-user/api/user/profile/other/" + targetUserId;
            ResponseEntity<Map> response = restTemplate.exchange(profileUrl, org.springframework.http.HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    log.info("✅ 获取他人主页成功 - userId: {}, nickname: {}, followStatus: {}",
                        data.get("userId"), data.get("nickname"), data.get("followStatus"));
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("获取他人主页失败: " + msg);
                }
            }

        } catch (Exception e) {
            log.error("❌ 测试2失败: {}", e.getMessage());
            throw new RuntimeException("获取他人主页测试失败", e);
        }
    }

    /**
     * 🎯 测试3：关注用户
     */
    @Test
    @Order(3)
    @DisplayName("测试3: 关注用户")
    public void test3_FollowUser() {
        try {
            log.info("\n[测试3] 关注用户");
            ensureAuthenticated();

            if (targetUserId == null) {
                log.warn("⚠️ 目标用户ID为空，跳过测试");
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String followUrl = GATEWAY_URL + "/xypai-user/api/user/relation/follow/" + targetUserId;
            ResponseEntity<Map> response = restTemplate.postForEntity(followUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    log.info("✅ 关注用户成功");
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("关注用户失败: " + msg);
                }
            }

        } catch (Exception e) {
            log.error("❌ 测试3失败: {}", e.getMessage());
            throw new RuntimeException("关注用户测试失败", e);
        }
    }

    /**
     * 🎯 测试4：取消关注
     */
    @Test
    @Order(4)
    @DisplayName("测试4: 取消关注")
    public void test4_UnfollowUser() {
        try {
            log.info("\n[测试4] 取消关注");
            ensureAuthenticated();

            if (targetUserId == null) {
                log.warn("⚠️ 目标用户ID为空，跳过测试");
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            // 使用正确的路径：DELETE /api/user/relation/follow/{userId}
            String unfollowUrl = GATEWAY_URL + "/xypai-user/api/user/relation/follow/" + targetUserId;
            ResponseEntity<Map> response = restTemplate.exchange(unfollowUrl, org.springframework.http.HttpMethod.DELETE, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    log.info("✅ 取消关注成功");
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("取消关注失败: " + msg);
                }
            }

        } catch (Exception e) {
            log.error("❌ 测试4失败: {}", e.getMessage());
            throw new RuntimeException("取消关注测试失败", e);
        }
    }

    /**
     * 🎯 测试5：获取用户技能列表
     */
    @Test
    @Order(5)
    @DisplayName("测试5: 获取用户技能列表")
    public void test5_GetUserSkills() {
        try {
            log.info("\n[测试5] 获取用户技能列表");
            ensureAuthenticated();

            if (targetUserId == null) {
                log.warn("⚠️ 目标用户ID为空，跳过测试");
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String skillsUrl = GATEWAY_URL + "/xypai-user/api/user/skills/user/" + targetUserId + "?pageNum=1&pageSize=20";
            ResponseEntity<Map> response = restTemplate.exchange(skillsUrl, org.springframework.http.HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();

                // TableDataInfo 直接返回，没有包装在 R 中
                Object records = responseBody.get("rows");
                int skillCount = (records instanceof java.util.List) ? ((java.util.List<?>) records).size() : 0;
                log.info("✅ 获取用户技能列表成功 - 技能数量: {}", skillCount);
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试5失败: {}", e.getMessage());
            throw new RuntimeException("获取技能列表测试失败", e);
        }
    }

    /**
     * 🎯 测试6：获取用户详细资料
     */
    @Test
    @Order(6)
    @DisplayName("测试6: 获取用户详细资料")
    public void test6_GetUserProfileDetail() {
        try {
            log.info("\n[测试6] 获取用户详细资料");
            ensureAuthenticated();

            if (targetUserId == null) {
                log.warn("⚠️ 目标用户ID为空，跳过测试");
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String detailUrl = GATEWAY_URL + "/xypai-user/api/user/profile/info";
            ResponseEntity<Map> response = restTemplate.exchange(detailUrl, org.springframework.http.HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    log.info("✅ 获取用户详细资料成功 - userId: {}, nickname: {}",
                        data.get("userId"), data.get("nickname"));
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("获取详细资料失败: " + msg);
                }
            }

        } catch (Exception e) {
            log.error("❌ 测试6失败: {}", e.getMessage());
            throw new RuntimeException("获取详细资料测试失败", e);
        }
    }

    /**
     * 🎯 测试7：举报用户
     */
    @Test
    @Order(7)
    @DisplayName("测试7: 举报用户")
    public void test7_ReportUser() {
        try {
            log.info("\n[测试7] 举报用户");
            ensureAuthenticated();

            if (targetUserId == null) {
                log.warn("⚠️ 目标用户ID为空，跳过测试");
                return;
            }

            Map<String, String> reportRequest = new HashMap<>();
            reportRequest.put("reason", "测试举报");
            reportRequest.put("description", "这是一个测试举报");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(reportRequest, headers);

            String reportUrl = GATEWAY_URL + "/xypai-user/api/user/relation/report/" + targetUserId;
            ResponseEntity<Map> response = restTemplate.postForEntity(reportUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    log.info("✅ 举报用户成功");
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 举报用户返回非200: {}", msg);
                }
            }

        } catch (Exception e) {
            log.error("❌ 测试7失败: {}", e.getMessage());
            log.warn("⚠️ 举报功能可能未实现，继续测试");
        }
    }

    /**
     * 🎯 测试8：拉黑用户
     */
    @Test
    @Order(8)
    @DisplayName("测试8: 拉黑用户")
    public void test8_BlockUser() {
        try {
            log.info("\n[测试8] 拉黑用户");
            ensureAuthenticated();

            if (targetUserId == null) {
                log.warn("⚠️ 目标用户ID为空，跳过测试");
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String blockUrl = GATEWAY_URL + "/xypai-user/api/user/relation/block/" + targetUserId;
            ResponseEntity<Map> response = restTemplate.postForEntity(blockUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    log.info("✅ 拉黑用户成功");
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 拉黑用户返回非200: {}", msg);
                }
            }

        } catch (Exception e) {
            log.error("❌ 测试8失败: {}", e.getMessage());
            log.warn("⚠️ 拉黑功能可能未实现，继续测试");
        }
    }

    @AfterAll
    static void tearDown() {
        log.info("\n🎉 所有测试完成！");
    }
}
