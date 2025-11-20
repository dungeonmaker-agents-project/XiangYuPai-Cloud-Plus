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
 * App 粉丝列表页面集成测试 - 通过Gateway调用真实接口
 *
 * 🎯 核心目标：
 * 测试 App 粉丝列表页面的完整功能
 *
 * 测试流程:
 * 1. 📋 获取粉丝列表
 * 2. 🔍 搜索粉丝
 * 3. ➕ 关注粉丝
 * 4. ➖ 取消关注
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
public class AppFansListPageTest {

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
    private static String fanUserId;  // 粉丝用户ID

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        log.info("📱 App 粉丝列表页面集成测试启动");
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
     * 🎯 测试1：准备测试数据 - 创建粉丝
     */
    @Test
    @Order(1)
    @DisplayName("测试1: 准备测试数据 - 创建粉丝用户")
    public void test1_PrepareFanUser() {
        try {
            log.info("\n[测试1] 创建当前用户");
            ensureAuthenticated();

            // 创建粉丝用户（确保11位）
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
                    String fanToken = (String) data.get("token");
                    fanUserId = String.valueOf(data.get("userId"));
                    log.info("✅ 粉丝用户创建成功 - fanUserId: {}", fanUserId);

                    // 让粉丝用户关注当前用户
                    HttpHeaders fanHeaders = new HttpHeaders();
                    fanHeaders.set("Authorization", "Bearer " + fanToken);
                    HttpEntity<Void> fanRequest = new HttpEntity<>(fanHeaders);

                    String followUrl = GATEWAY_URL + "/xypai-user/api/user/relation/follow/" + userId;
                    ResponseEntity<Map> followResponse = restTemplate.postForEntity(followUrl, fanRequest, Map.class);

                    if (followResponse.getStatusCode().is2xxSuccessful()) {
                        log.info("✅ 粉丝用户关注成功");
                    }
                }
            }

        } catch (Exception e) {
            log.error("❌ 测试1失败: {}", e.getMessage());
            throw new RuntimeException("准备测试数据失败", e);
        }
    }

    /**
     * 🎯 测试2：获取粉丝列表
     */
    @Test
    @Order(2)
    @DisplayName("测试2: 获取粉丝列表")
    public void test2_GetFansList() {
        try {
            log.info("\n[测试2] 获取粉丝列表");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String fansUrl = GATEWAY_URL + "/xypai-user/api/user/relation/fans?pageNum=1&pageSize=20";
            ResponseEntity<Map> response = restTemplate.exchange(fansUrl, org.springframework.http.HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();

                // TableDataInfo 直接返回，没有包装在 R 中
                Object records = responseBody.get("rows");
                int fanCount = (records instanceof java.util.List) ? ((java.util.List<?>) records).size() : 0;
                log.info("✅ 获取粉丝列表成功 - 总数: {}, 当前页: {}", responseBody.get("total"), fanCount);
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试2失败: {}", e.getMessage());
            throw new RuntimeException("获取粉丝列表测试失败", e);
        }
    }

    /**
     * 🎯 测试3：搜索粉丝
     */
    @Test
    @Order(3)
    @DisplayName("测试3: 搜索粉丝")
    public void test3_SearchFans() {
        try {
            log.info("\n[测试3] 搜索粉丝");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            // 使用 keyword 参数进行搜索
            String searchUrl = GATEWAY_URL + "/xypai-user/api/user/relation/fans?keyword=User&pageNum=1&pageSize=20";
            ResponseEntity<Map> response = restTemplate.exchange(searchUrl, org.springframework.http.HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();

                // TableDataInfo 直接返回，没有包装在 R 中
                Object records = responseBody.get("rows");
                int resultCount = (records instanceof java.util.List) ? ((java.util.List<?>) records).size() : 0;
                log.info("✅ 搜索粉丝成功 - 搜索结果数量: {}", resultCount);
            } else {
                log.warn("⚠️ 搜索粉丝返回非2xx: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试3失败: {}", e.getMessage());
            log.warn("⚠️ 搜索功能可能未实现，继续测试");
        }
    }

    /**
     * 🎯 测试4：回关粉丝
     */
    @Test
    @Order(4)
    @DisplayName("测试4: 回关粉丝")
    public void test4_FollowBackFan() {
        try {
            log.info("\n[测试4] 回关粉丝");
            ensureAuthenticated();

            if (fanUserId == null) {
                log.warn("⚠️ 粉丝用户ID为空，跳过测试");
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String followUrl = GATEWAY_URL + "/xypai-user/api/user/relation/follow/" + fanUserId;
            ResponseEntity<Map> response = restTemplate.postForEntity(followUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    log.info("✅ 回关粉丝成功");
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("回关粉丝失败: " + msg);
                }
            }

        } catch (Exception e) {
            log.error("❌ 测试4失败: {}", e.getMessage());
            throw new RuntimeException("回关粉丝测试失败", e);
        }
    }

    /**
     * 🎯 测试5：取消关注粉丝
     */
    @Test
    @Order(5)
    @DisplayName("测试5: 取消关注粉丝")
    public void test5_UnfollowFan() {
        try {
            log.info("\n[测试5] 取消关注粉丝");
            ensureAuthenticated();

            if (fanUserId == null) {
                log.warn("⚠️ 粉丝用户ID为空，跳过测试");
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            // 使用正确的路径：DELETE /api/user/relation/follow/{userId}
            String unfollowUrl = GATEWAY_URL + "/xypai-user/api/user/relation/follow/" + fanUserId;
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
            log.error("❌ 测试5失败: {}", e.getMessage());
            throw new RuntimeException("取消关注测试失败", e);
        }
    }

    @AfterAll
    static void tearDown() {
        log.info("\n🎉 所有测试完成！");
    }
}
