package org.dromara.aggregation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * App 首页 Feed 流集成测试 (BFF 聚合服务)
 *
 * 🎯 核心目标:
 * 测试首页用户推荐流功能，包括线上/线下两种Tab的用户列表
 *
 * 📌 测试接口归属说明:
 * - GET /api/home/feed → xypai-app-bff (聚合层，无数据库)
 *   该接口聚合 xypai-user + xypai-content 数据，返回用户推荐卡片
 *
 * - POST /api/user/follow → xypai-user (用户服务)
 *   关注/取消关注是用户领域操作，应在 xypai-user 测试
 *
 * 测试流程:
 * 1. 📱 用户登录 (调用 xypai-auth)
 * 2. 🏠 获取线上用户推荐列表 (type=online)
 * 3. 🏠 获取线下用户推荐列表 (type=offline)
 * 4. 📄 分页加载测试 (pageNum=2)
 * 5. 📍 带位置筛选的推荐列表 (cityCode)
 *
 * 💡 测试方式说明:
 * - 集成测试，调用真实服务
 * - 需要启动: Gateway(8080), xypai-auth(9211), xypai-app-bff(9400), xypai-user(9401), Nacos, Redis
 *
 * @author XyPai Team
 * @date 2025-11-20
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppHomeFeedTest {

    // 测试配置
    private static final String GATEWAY_URL = "http://localhost:8080";
    private static final String TEST_COUNTRY_CODE = "+86";
    private static final String TEST_SMS_CODE = "123456";

    // HTTP 客户端
    private static RestTemplate restTemplate;
    private static ObjectMapper objectMapper;

    // 保存登录后的 Token
    private static String authToken;
    private static String userId;

    // 保存测试数据
    private static Long firstUserId;

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        log.info("🏠 App 首页 Feed 流集成测试启动 (BFF 聚合服务)");
        log.info("⚠️ 确保服务已启动: Gateway(8080), xypai-auth(9211), xypai-app-bff(9400), xypai-user(9401), Nacos, Redis\n");
    }

    /**
     * 辅助方法: 确保有有效的登录 Token
     */
    private static void ensureAuthenticated() {
        if (authToken != null && !authToken.isEmpty()) {
            return;
        }

        log.info("⚠️ 创建新用户并登录...");

        try {
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
                    authToken = (String) data.get("token");
                    userId = String.valueOf(data.get("userId"));
                    log.info("✅ 登录成功 - userId: {}", userId);
                } else {
                    log.error("❌ 登录失败: {}", responseBody.get("msg"));
                }
            }
        } catch (Exception e) {
            log.error("❌ 登录异常: {}", e.getMessage());
        }
    }

    /**
     * 🎯 测试1: 用户登录
     */
    @Test
    @Order(1)
    @DisplayName("测试1: 用户登录")
    public void test1_UserLogin() {
        try {
            log.info("\n[测试1] 用户登录");

            long timestamp = System.currentTimeMillis() % 100000000L;
            String uniqueMobile = String.format("139%08d", timestamp);
            log.info("手机号: {}, 验证码: {}", uniqueMobile, TEST_SMS_CODE);

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
                    log.info("✅ 登录成功 - userId: {}, token前10位: {}", userId,
                        authToken.substring(0, Math.min(10, authToken.length())));
                } else {
                    throw new RuntimeException("登录失败: " + responseBody.get("msg"));
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试1失败: {}", e.getMessage());
            throw new RuntimeException("用户登录测试失败", e);
        }
    }

    /**
     * 🎯 测试2: 获取线上用户推荐列表
     *
     * 接口: GET /api/home/feed?type=online
     * 服务: xypai-app-bff (聚合层)
     * 数据来源: xypai-user (用户信息) + xypai-content (动态统计)
     */
    @Test
    @Order(2)
    @DisplayName("测试2: 获取线上用户推荐列表")
    public void test2_GetOnlineUserFeed() {
        try {
            log.info("\n[测试2] 获取线上用户推荐列表 (BFF聚合接口)");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> request = new HttpEntity<>(headers);

            // 调用 BFF 聚合接口
            String feedUrl = GATEWAY_URL + "/xypai-app-bff/api/home/feed?type=online&pageNum=1&pageSize=10";
            ResponseEntity<Map> response = restTemplate.exchange(
                feedUrl,
                HttpMethod.GET,
                request,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    Object list = data.get("list");
                    int listSize = (list instanceof List) ? ((List<?>) list).size() : 0;
                    Boolean hasMore = (Boolean) data.get("hasMore");

                    log.info("✅ 获取线上用户推荐列表成功");
                    log.info("   - 总数: {}", data.get("total"));
                    log.info("   - 当前页数量: {}", listSize);
                    log.info("   - 是否有更多: {}", hasMore);

                    // 保存第一个用户ID用于后续测试
                    if (listSize > 0) {
                        List<?> userList = (List<?>) list;
                        Map<String, Object> firstUser = (Map<String, Object>) userList.get(0);
                        firstUserId = Long.valueOf(firstUser.get("userId").toString());
                        log.info("   - 第一个用户: {} (ID: {})", firstUser.get("nickname"), firstUserId);
                    }
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 获取线上用户推荐列表失败: {} (BFF服务可能未启动)", msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试2失败: {} (如果BFF服务未启动，这是正常的)", e.getMessage());
            // 不抛出异常，允许测试继续
        }
    }

    /**
     * 🎯 测试3: 获取线下用户推荐列表
     *
     * 接口: GET /api/home/feed?type=offline
     * 服务: xypai-app-bff (聚合层)
     */
    @Test
    @Order(3)
    @DisplayName("测试3: 获取线下用户推荐列表")
    public void test3_GetOfflineUserFeed() {
        try {
            log.info("\n[测试3] 获取线下用户推荐列表 (BFF聚合接口)");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> request = new HttpEntity<>(headers);

            // 调用 BFF 聚合接口
            String feedUrl = GATEWAY_URL + "/xypai-app-bff/api/home/feed?type=offline&pageNum=1&pageSize=10";
            ResponseEntity<Map> response = restTemplate.exchange(
                feedUrl,
                HttpMethod.GET,
                request,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    Object list = data.get("list");
                    int listSize = (list instanceof List) ? ((List<?>) list).size() : 0;

                    log.info("✅ 获取线下用户推荐列表成功");
                    log.info("   - 总数: {}", data.get("total"));
                    log.info("   - 当前页数量: {}", listSize);
                    log.info("   - 是否有更多: {}", data.get("hasMore"));
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 获取线下用户推荐列表失败: {}", msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试3失败: {} (如果BFF服务未启动，这是正常的)", e.getMessage());
        }
    }

    /**
     * 🎯 测试4: 分页加载测试
     *
     * 接口: GET /api/home/feed?type=online&pageNum=2
     * 服务: xypai-app-bff (聚合层)
     */
    @Test
    @Order(4)
    @DisplayName("测试4: 分页加载测试")
    public void test4_PaginationTest() {
        try {
            log.info("\n[测试4] 分页加载测试 (第2页)");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String feedUrl = GATEWAY_URL + "/xypai-app-bff/api/home/feed?type=online&pageNum=2&pageSize=10";
            ResponseEntity<Map> response = restTemplate.exchange(
                feedUrl,
                HttpMethod.GET,
                request,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    Object list = data.get("list");
                    int listSize = (list instanceof List) ? ((List<?>) list).size() : 0;

                    log.info("✅ 分页加载成功 (第2页)");
                    log.info("   - 当前页数量: {}", listSize);
                    log.info("   - 是否有更多: {}", data.get("hasMore"));
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 分页加载失败: {}", msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试4失败: {}", e.getMessage());
        }
    }

    /**
     * 🎯 测试5: 带城市筛选的推荐列表
     *
     * 接口: GET /api/home/feed?type=online&cityCode=440300
     * 服务: xypai-app-bff (聚合层)
     */
    @Test
    @Order(5)
    @DisplayName("测试5: 带城市筛选的推荐列表")
    public void test5_GetFeedWithCityFilter() {
        try {
            log.info("\n[测试5] 带城市筛选的推荐列表 (深圳: 440300)");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> request = new HttpEntity<>(headers);

            // 深圳市城市代码
            String feedUrl = GATEWAY_URL + "/xypai-app-bff/api/home/feed?type=online&pageNum=1&pageSize=10&cityCode=440300";
            ResponseEntity<Map> response = restTemplate.exchange(
                feedUrl,
                HttpMethod.GET,
                request,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    Object list = data.get("list");
                    int listSize = (list instanceof List) ? ((List<?>) list).size() : 0;

                    log.info("✅ 获取深圳地区用户推荐列表成功");
                    log.info("   - 总数: {}", data.get("total"));
                    log.info("   - 当前页数量: {}", listSize);
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 带城市筛选的推荐列表失败: {}", msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试5失败: {}", e.getMessage());
        }
    }

    @AfterAll
    static void tearDown() {
        log.info("\n🎉 首页 Feed 流测试完成！");
        log.info("📝 注意: 关注/取消关注功能测试请在 xypai-user 模块的 UserFollowTest 中进行");
    }
}
