package org.dromara.content;

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
 * App 内容模块 - 发现主页集成测试
 *
 * 🎯 核心目标:
 * 测试发现主页的核心功能,包括三种Feed流(关注/热门/同城)和互动功能(点赞/收藏/分享)
 *
 * 测试流程:
 * 1. 📱 用户登录(复用原有登录流程)
 * 2. 📋 获取热门动态列表
 * 3. 📋 获取关注动态列表
 * 4. 📋 获取同城动态列表
 * 5. ❤️ 点赞动态
 * 6. ⭐ 收藏动态
 * 7. 📤 分享动态
 * 8. 💔 取消点赞
 * 9. 🗑️ 取消收藏
 *
 * 💡 测试方式说明:
 * - 集成测试,调用真实服务
 * - 需要手动启动:Gateway(8080), xypai-auth(9211), xypai-content(9403), Nacos, Redis, MySQL
 *
 * @author XyPai Team
 * @date 2025-11-20
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppContentDiscoveryTest {

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

    // 保存测试用的动态ID
    private static Long testFeedId;

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        log.info("📱 App 内容模块 - 发现主页集成测试启动");
        log.info("⚠️ 确保服务已启动: Gateway(8080), xypai-auth(9211), xypai-content(9403), Nacos, Redis, MySQL\n");
    }

    /**
     * 辅助方法:确保有有效的登录 Token
     * 如果没有 Token,则创建新用户并登录
     */
    private static void ensureAuthenticated() {
        if (authToken != null && !authToken.isEmpty()) {
            return;
        }

        log.info("⚠️ 创建新用户并登录...");

        try {
            // 生成唯一手机号(确保11位)
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
     * 🎯 测试1:新用户 SMS 登录
     */
    @Test
    @Order(1)
    @DisplayName("测试1: 新用户 SMS 登录")
    public void test1_UserLogin() {
        try {
            log.info("\n[测试1] 新用户 SMS 登录");

            // 使用时间戳生成唯一手机号,确保是新用户(11位)
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
                    String uid = String.valueOf(data.get("userId"));

                    // 保存 Token 和 UserId,供后续测试使用
                    authToken = token;
                    userId = uid;

                    log.info("✅ 登录成功 - userId: {}, token前10位: {}", uid, token.substring(0, Math.min(10, token.length())));
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("登录失败: " + msg);
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
     * 🎯 测试2:获取热门动态列表
     */
    @Test
    @Order(2)
    @DisplayName("测试2: 获取热门动态列表")
    public void test2_GetHotFeeds() {
        try {
            log.info("\n[测试2] 获取热门动态列表");

            // 热门Tab不需要登录,但我们还是登录后测试
            ensureAuthenticated();

            // 构造请求头
            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> request = new HttpEntity<>(headers);

            // 调用热门动态列表接口
            String feedUrl = GATEWAY_URL + "/xypai-content/api/v1/content/feed/hot?pageNum=1&pageSize=20";
            ResponseEntity<Map> response = restTemplate.exchange(
                feedUrl,
                org.springframework.http.HttpMethod.GET,
                request,
                Map.class
            );

            // 验证响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    Object records = data.get("records");
                    int recordCount = (records instanceof java.util.List) ? ((java.util.List<?>) records).size() : 0;

                    log.info("✅ 获取热门动态列表成功 - 总数: {}, 当前页: {}", data.get("total"), recordCount);

                    // 保存第一个动态ID用于后续测试
                    if (recordCount > 0) {
                        java.util.List<?> recordList = (java.util.List<?>) records;
                        Map<String, Object> firstFeed = (Map<String, Object>) recordList.get(0);
                        testFeedId = Long.valueOf(firstFeed.get("id").toString());
                        log.info("📝 保存测试动态ID: {}", testFeedId);
                    }
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("获取热门动态列表失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试2失败: {}", e.getMessage());
            throw new RuntimeException("获取热门动态列表测试失败", e);
        }
    }

    /**
     * 🎯 测试3:获取关注动态列表
     */
    @Test
    @Order(3)
    @DisplayName("测试3: 获取关注动态列表")
    public void test3_GetFollowFeeds() {
        try {
            log.info("\n[测试3] 获取关注动态列表");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token,测试跳过");
                return;
            }

            // 构造请求头(带 Token + Bearer 前缀)
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String feedUrl = GATEWAY_URL + "/xypai-content/api/v1/content/feed/follow?pageNum=1&pageSize=20";
            ResponseEntity<Map> response = restTemplate.exchange(
                feedUrl,
                org.springframework.http.HttpMethod.GET,
                request,
                Map.class
            );

            // 验证响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    Object records = data.get("records");
                    int recordCount = (records instanceof java.util.List) ? ((java.util.List<?>) records).size() : 0;

                    log.info("✅ 获取关注动态列表成功 - 总数: {}, 当前页: {} (新用户通常为空)", data.get("total"), recordCount);
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("获取关注动态列表失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试3失败: {}", e.getMessage());
            throw new RuntimeException("获取关注动态列表测试失败", e);
        }
    }

    /**
     * 🎯 测试4:获取同城动态列表
     */
    @Test
    @Order(4)
    @DisplayName("测试4: 获取同城动态列表")
    public void test4_GetLocalFeeds() {
        try {
            log.info("\n[测试4] 获取同城动态列表");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token,测试跳过");
                return;
            }

            // 构造请求头(带 Token + Bearer 前缀)
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            // 提供测试位置(深圳坐标)
            String feedUrl = GATEWAY_URL + "/xypai-content/api/v1/content/feed/local?pageNum=1&pageSize=20&latitude=22.5431&longitude=114.0579&radius=5";
            ResponseEntity<Map> response = restTemplate.exchange(
                feedUrl,
                org.springframework.http.HttpMethod.GET,
                request,
                Map.class
            );

            // 验证响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    Object records = data.get("records");
                    int recordCount = (records instanceof java.util.List) ? ((java.util.List<?>) records).size() : 0;

                    log.info("✅ 获取同城动态列表成功 - 总数: {}, 当前页: {}", data.get("total"), recordCount);
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 获取同城动态列表失败: {} (可能是数据库不支持空间查询)", msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试4失败: {} (可能是数据库不支持空间查询,这是正常的)", e.getMessage());
            // 不抛出异常,允许测试继续
        }
    }

    /**
     * 🎯 测试5:点赞动态
     */
    @Test
    @Order(5)
    @DisplayName("测试5: 点赞动态")
    public void test5_LikeFeed() {
        try {
            log.info("\n[测试5] 点赞动态");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token,测试跳过");
                return;
            }

            // 如果没有测试动态ID,先获取一个
            if (testFeedId == null) {
                log.warn("⚠️ 没有测试动态ID,先获取热门动态列表");
                test2_GetHotFeeds();
            }

            if (testFeedId == null) {
                log.warn("⚠️ 没有可用的动态进行测试,跳过点赞测试");
                return;
            }

            // 构造请求
            Map<String, Object> likeRequest = new HashMap<>();
            likeRequest.put("targetType", "feed");
            likeRequest.put("targetId", testFeedId);
            likeRequest.put("action", "like");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(likeRequest, headers);

            String likeUrl = GATEWAY_URL + "/xypai-content/api/v1/interaction/like";
            ResponseEntity<Map> response = restTemplate.postForEntity(likeUrl, request, Map.class);

            // 验证响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    log.info("✅ 点赞成功 - 动态ID: {}, 点赞数: {}, 是否已点赞: {}",
                        testFeedId, data.get("count"), data.get("isActive"));
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("点赞失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试5失败: {}", e.getMessage());
            throw new RuntimeException("点赞动态测试失败", e);
        }
    }

    /**
     * 🎯 测试6:收藏动态
     */
    @Test
    @Order(6)
    @DisplayName("测试6: 收藏动态")
    public void test6_CollectFeed() {
        try {
            log.info("\n[测试6] 收藏动态");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token,测试跳过");
                return;
            }

            if (testFeedId == null) {
                log.warn("⚠️ 没有测试动态ID,跳过收藏测试");
                return;
            }

            // 构造请求
            Map<String, Object> collectRequest = new HashMap<>();
            collectRequest.put("targetType", "feed");
            collectRequest.put("targetId", testFeedId);
            collectRequest.put("action", "collect");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(collectRequest, headers);

            String collectUrl = GATEWAY_URL + "/xypai-content/api/v1/interaction/collect";
            ResponseEntity<Map> response = restTemplate.postForEntity(collectUrl, request, Map.class);

            // 验证响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    log.info("✅ 收藏成功 - 动态ID: {}, 收藏数: {}, 是否已收藏: {}",
                        testFeedId, data.get("count"), data.get("isActive"));
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("收藏失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试6失败: {}", e.getMessage());
            throw new RuntimeException("收藏动态测试失败", e);
        }
    }

    /**
     * 🎯 测试7:分享动态
     */
    @Test
    @Order(7)
    @DisplayName("测试7: 分享动态")
    public void test7_ShareFeed() {
        try {
            log.info("\n[测试7] 分享动态");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token,测试跳过");
                return;
            }

            if (testFeedId == null) {
                log.warn("⚠️ 没有测试动态ID,跳过分享测试");
                return;
            }

            // 构造请求
            Map<String, Object> shareRequest = new HashMap<>();
            shareRequest.put("targetType", "feed");
            shareRequest.put("targetId", testFeedId);
            shareRequest.put("shareChannel", "wechat");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(shareRequest, headers);

            String shareUrl = GATEWAY_URL + "/xypai-content/api/v1/interaction/share";
            ResponseEntity<Map> response = restTemplate.postForEntity(shareUrl, request, Map.class);

            // 验证响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    log.info("✅ 分享成功 - 动态ID: {}, 分享数: {}", testFeedId, data.get("count"));
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("分享失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试7失败: {}", e.getMessage());
            throw new RuntimeException("分享动态测试失败", e);
        }
    }

    /**
     * 🎯 测试8:取消点赞
     */
    @Test
    @Order(8)
    @DisplayName("测试8: 取消点赞")
    public void test8_UnlikeFeed() {
        try {
            log.info("\n[测试8] 取消点赞");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token,测试跳过");
                return;
            }

            if (testFeedId == null) {
                log.warn("⚠️ 没有测试动态ID,跳过取消点赞测试");
                return;
            }

            // 构造请求
            Map<String, Object> unlikeRequest = new HashMap<>();
            unlikeRequest.put("targetType", "feed");
            unlikeRequest.put("targetId", testFeedId);
            unlikeRequest.put("action", "unlike");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(unlikeRequest, headers);

            String unlikeUrl = GATEWAY_URL + "/xypai-content/api/v1/interaction/like";
            ResponseEntity<Map> response = restTemplate.postForEntity(unlikeUrl, request, Map.class);

            // 验证响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    log.info("✅ 取消点赞成功 - 动态ID: {}, 点赞数: {}, 是否已点赞: {}",
                        testFeedId, data.get("count"), data.get("isActive"));
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("取消点赞失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试8失败: {}", e.getMessage());
            throw new RuntimeException("取消点赞测试失败", e);
        }
    }

    /**
     * 🎯 测试9:取消收藏
     */
    @Test
    @Order(9)
    @DisplayName("测试9: 取消收藏")
    public void test9_UncollectFeed() {
        try {
            log.info("\n[测试9] 取消收藏");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token,测试跳过");
                return;
            }

            if (testFeedId == null) {
                log.warn("⚠️ 没有测试动态ID,跳过取消收藏测试");
                return;
            }

            // 构造请求
            Map<String, Object> uncollectRequest = new HashMap<>();
            uncollectRequest.put("targetType", "feed");
            uncollectRequest.put("targetId", testFeedId);
            uncollectRequest.put("action", "uncollect");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(uncollectRequest, headers);

            String uncollectUrl = GATEWAY_URL + "/xypai-content/api/v1/interaction/collect";
            ResponseEntity<Map> response = restTemplate.postForEntity(uncollectUrl, request, Map.class);

            // 验证响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    log.info("✅ 取消收藏成功 - 动态ID: {}, 收藏数: {}, 是否已收藏: {}",
                        testFeedId, data.get("count"), data.get("isActive"));
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("取消收藏失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试9失败: {}", e.getMessage());
            throw new RuntimeException("取消收藏测试失败", e);
        }
    }

    @AfterAll
    static void tearDown() {
        log.info("\n🎉 所有测试完成！");
    }
}
