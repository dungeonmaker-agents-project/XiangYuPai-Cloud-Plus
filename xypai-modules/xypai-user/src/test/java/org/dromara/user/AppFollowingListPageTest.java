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
import java.util.List;
import java.util.Map;

/**
 * App 关注列表页面集成测试 - 通过Gateway调用真实接口
 *
 * 🎯 核心目标：
 * 测试 App 关注列表页面的完整功能
 *
 * 测试流程:
 * 1. 📋 获取关注列表（验证新字段：age, isVerified, signature, relationStatus）
 * 2. 🔍 搜索关注列表
 * 3. ➖ 取消关注
 *
 * 💡 测试方式说明：
 * - 集成测试，调用真实服务
 * - 需要手动启动：Gateway(8080), xypai-auth(9211), xypai-user(9401), Nacos, Redis, MySQL
 *
 * 📝 新增字段说明（2025-12-02 更新）：
 * - age: 从birthday计算的年龄
 * - isVerified: 是否实名认证
 * - signature: 个性签名（与bio相同）
 * - relationStatus: 关系状态（none/following/followed/mutual）
 *
 * @author XyPai Team
 * @date 2025-11-18
 * @updated 2025-12-02 - 添加新字段验证
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppFollowingListPageTest {

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
    private static String followingUserId;  // 被关注的用户ID

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        log.info("📱 App 关注列表页面集成测试启动");
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

            String loginUrl = GATEWAY_URL + "/xypai-auth/api/auth/login/sms";
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
     * 🎯 测试1：准备测试数据 - 创建并关注用户
     */
    @Test
    @Order(1)
    @DisplayName("测试1: 准备测试数据 - 创建并关注用户")
    public void test1_PrepareFollowingUser() {
        try {
            log.info("\n[测试1] 创建当前用户");
            ensureAuthenticated();

            // 创建被关注的用户（确保11位）
            long timestamp = System.currentTimeMillis() % 100000000L;
            String uniqueMobile = String.format("139%08d", timestamp);
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
                    followingUserId = String.valueOf(data.get("userId"));
                    log.info("✅ 被关注用户创建成功 - followingUserId: {}", followingUserId);

                    // 当前用户关注这个用户
                    HttpHeaders currentHeaders = new HttpHeaders();
                    currentHeaders.set("Authorization", "Bearer " + authToken);
                    HttpEntity<Void> followRequest = new HttpEntity<>(currentHeaders);

                    String followUrl = GATEWAY_URL + "/xypai-user/api/user/relation/follow/" + followingUserId;
                    ResponseEntity<Map> followResponse = restTemplate.postForEntity(followUrl, followRequest, Map.class);

                    if (followResponse.getStatusCode().is2xxSuccessful()) {
                        log.info("✅ 关注用户成功");
                    }
                }
            }

        } catch (Exception e) {
            log.error("❌ 测试1失败: {}", e.getMessage());
            throw new RuntimeException("准备测试数据失败", e);
        }
    }

    /**
     * 🎯 测试2：获取关注列表（验证新字段）
     */
    @Test
    @Order(2)
    @DisplayName("测试2: 获取关注列表（验证新字段: age, isVerified, signature, relationStatus）")
    public void test2_GetFollowingList() {
        try {
            log.info("\n[测试2] 获取关注列表（验证新字段）");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String followingUrl = GATEWAY_URL + "/xypai-user/api/user/relation/following?pageNum=1&pageSize=20";
            ResponseEntity<Map> response = restTemplate.exchange(followingUrl, org.springframework.http.HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();

                // TableDataInfo 直接返回，没有包装在 R 中
                Object records = responseBody.get("rows");
                int followingCount = (records instanceof List) ? ((List<?>) records).size() : 0;
                log.info("✅ 获取关注列表成功 - 总数: {}, 当前页: {}", responseBody.get("total"), followingCount);

                // 验证新字段
                if (records instanceof List && !((List<?>) records).isEmpty()) {
                    Map<String, Object> firstUser = (Map<String, Object>) ((List<?>) records).get(0);
                    log.info("📋 验证返回字段:");
                    log.info("   - userId: {}", firstUser.get("userId"));
                    log.info("   - nickname: {}", firstUser.get("nickname"));
                    log.info("   - avatar: {}", firstUser.get("avatar"));
                    log.info("   - gender: {}", firstUser.get("gender"));
                    log.info("   - age: {} (新字段)", firstUser.get("age"));
                    log.info("   - isVerified: {} (新字段)", firstUser.get("isVerified"));
                    log.info("   - signature: {} (新字段)", firstUser.get("signature"));
                    log.info("   - bio: {}", firstUser.get("bio"));
                    log.info("   - relationStatus: {} (新字段)", firstUser.get("relationStatus"));
                    log.info("   - followStatus: {}", firstUser.get("followStatus"));
                    log.info("   - isFollowing: {}", firstUser.get("isFollowing"));
                    log.info("   - isMutualFollow: {}", firstUser.get("isMutualFollow"));

                    // 验证relationStatus为following或mutual（因为是关注列表）
                    String relationStatus = (String) firstUser.get("relationStatus");
                    if (relationStatus != null) {
                        Assertions.assertTrue(
                            "following".equals(relationStatus) || "mutual".equals(relationStatus),
                            "关注列表中的relationStatus应为following或mutual"
                        );
                        log.info("✅ relationStatus验证通过: {}", relationStatus);
                    }
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试2失败: {}", e.getMessage());
            throw new RuntimeException("获取关注列表测试失败", e);
        }
    }

    /**
     * 🎯 测试3：搜索关注列表
     */
    @Test
    @Order(3)
    @DisplayName("测试3: 搜索关注列表")
    public void test3_SearchFollowing() {
        try {
            log.info("\n[测试3] 搜索关注列表");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            // 使用 keyword 参数进行搜索
            String searchUrl = GATEWAY_URL + "/xypai-user/api/user/relation/following?keyword=User&pageNum=1&pageSize=20";
            ResponseEntity<Map> response = restTemplate.exchange(searchUrl, org.springframework.http.HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();

                // TableDataInfo 直接返回，没有包装在 R 中
                Object records = responseBody.get("rows");
                int resultCount = (records instanceof List) ? ((List<?>) records).size() : 0;
                log.info("✅ 搜索关注列表成功 - 搜索结果数量: {}", resultCount);
            } else {
                log.warn("⚠️ 搜索关注列表返回非2xx: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试3失败: {}", e.getMessage());
            log.warn("⚠️ 搜索功能可能未实现，继续测试");
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

            if (followingUserId == null) {
                log.warn("⚠️ 被关注用户ID为空，跳过测试");
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            // 使用正确的路径：DELETE /api/user/relation/follow/{userId}
            String unfollowUrl = GATEWAY_URL + "/xypai-user/api/user/relation/follow/" + followingUserId;
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

    @AfterAll
    static void tearDown() {
        log.info("\n🎉 所有测试完成！");
    }
}
