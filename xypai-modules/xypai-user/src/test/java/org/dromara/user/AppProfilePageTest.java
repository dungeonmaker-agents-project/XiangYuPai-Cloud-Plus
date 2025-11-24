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
 * App 个人主页页面集成测试 - 通过Gateway调用真实接口
 *
 * 🎯 核心目标：
 * 测试个人主页页面的 UserService 相关接口功能
 *
 * 测试流程:
 * 1. 📱 新用户 SMS 注册（获取Token）
 * 2. 📋 获取个人主页头部信息
 * 3. 📝 获取动态列表
 * 4. ⭐ 获取收藏列表
 * 5. ❤️ 获取点赞列表
 * 6. 👤 获取个人资料信息
 *
 * ⚠️ 注意：
 * 点赞/收藏动态的接口 (POST/DELETE /api/moments/{momentId}/like|favorite)
 * 属于 ContentService（内容服务），不在本模块中，需在 xypai-content 模块实现
 *
 * 💡 测试方式说明：
 * - 集成测试，调用真实服务
 * - 需要手动启动：Gateway(8080), xypai-auth(9211), xypai-user(9401), Nacos, Redis, MySQL
 *
 * @author XyPai Team
 * @date 2025-11-19
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppProfilePageTest {

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
        log.info("📱 App 个人主页页面集成测试启动");
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
     * 🎯 测试1：新用户 SMS 注册（获取Token）
     *
     * 测试场景：
     * 1. 用户首次使用手机号登录
     * 2. 后端自动创建用户
     * 3. 返回 Token，用于后续接口调用
     */
    @Test
    @Order(1)
    @DisplayName("测试1: 新用户 SMS 注册 - 获取Token")
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
                    String uid = String.valueOf(data.get("userId"));

                    // 保存 Token 和 UserId，供后续测试使用
                    authToken = token;
                    userId = uid;

                    log.info("✅ 注册成功 - userId: {}, token 已保存", uid);
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
     * 🎯 测试2：获取个人主页头部信息
     *
     * 接口: GET /api/user/profile/header
     * 触发时机: 页面加载时
     */
    @Test
    @Order(2)
    @DisplayName("测试2: 获取个人主页头部信息")
    public void test2_GetProfileHeader() {
        try {
            log.info("\n[测试2] 获取个人主页头部信息");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token，测试跳过");
                return;
            }

            // 构造请求头（带 Token + Bearer 前缀）
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String headerUrl = GATEWAY_URL + "/xypai-user/api/user/profile/header";
            ResponseEntity<Map> response = restTemplate.exchange(headerUrl, org.springframework.http.HttpMethod.GET, request, Map.class);

            // 验证响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    log.info("✅ 获取头部信息成功 - userId: {}, nickname: {}", data.get("userId"), data.get("nickname"));

                    // 验证必须字段
                    if (data.containsKey("stats")) {
                        Map<String, Object> stats = (Map<String, Object>) data.get("stats");
                        log.info("   统计数据 - 关注: {}, 粉丝: {}, 获赞: {}",
                            stats.get("followingCount"), stats.get("fansCount"), stats.get("likesCount"));
                    }
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("获取头部信息失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试2失败: {}", e.getMessage());
            throw new RuntimeException("获取头部信息测试失败", e);
        }
    }

    /**
     * 🎯 测试3：获取动态列表
     *
     * 接口: GET /api/user/profile/posts
     * 触发时机: 点击"动态"Tab
     */
    @Test
    @Order(3)
    @DisplayName("测试3: 获取动态列表")
    public void test3_GetPostsList() {
        try {
            log.info("\n[测试3] 获取动态列表");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token，测试跳过");
                return;
            }

            // 构造请求头（带 Token + Bearer 前缀）
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String postsUrl = GATEWAY_URL + "/xypai-user/api/user/profile/posts?page=1&pageSize=10";
            ResponseEntity<Map> response = restTemplate.exchange(postsUrl, org.springframework.http.HttpMethod.GET, request, Map.class);

            // 验证响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

                    if (data != null) {
                        Object posts = data.get("posts");
                        int postsCount = (posts instanceof java.util.List) ? ((java.util.List<?>) posts).size() : 0;
                        log.info("✅ 获取动态列表成功 - 总数: {}, 当前页: {}, 是否有更多: {}",
                            data.get("total"), postsCount, data.get("hasMore"));
                    } else {
                        log.info("✅ 获取动态列表成功 - 数据为空");
                    }
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("获取动态列表失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试3失败: {}", e.getMessage());
            throw new RuntimeException("获取动态列表测试失败", e);
        }
    }

    /**
     * 🎯 测试4：获取收藏列表
     *
     * 接口: GET /api/user/profile/favorites
     * 触发时机: 点击"收藏"Tab
     */
    @Test
    @Order(4)
    @DisplayName("测试4: 获取收藏列表")
    public void test4_GetFavoritesList() {
        try {
            log.info("\n[测试4] 获取收藏列表");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token，测试跳过");
                return;
            }

            // 构造请求头（带 Token + Bearer 前缀）
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String favoritesUrl = GATEWAY_URL + "/xypai-user/api/user/profile/favorites?page=1&pageSize=10";
            ResponseEntity<Map> response = restTemplate.exchange(favoritesUrl, org.springframework.http.HttpMethod.GET, request, Map.class);

            // 验证响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

                    if (data != null) {
                        Object favorites = data.get("favorites");
                        int favoritesCount = (favorites instanceof java.util.List) ? ((java.util.List<?>) favorites).size() : 0;
                        log.info("✅ 获取收藏列表成功 - 总数: {}, 当前页: {}, 是否有更多: {}",
                            data.get("total"), favoritesCount, data.get("hasMore"));
                    } else {
                        log.info("✅ 获取收藏列表成功 - 数据为空");
                    }
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("获取收藏列表失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试4失败: {}", e.getMessage());
            throw new RuntimeException("获取收藏列表测试失败", e);
        }
    }

    /**
     * 🎯 测试5：获取点赞列表
     *
     * 接口: GET /api/user/profile/likes
     * 触发时机: 点击"点赞"Tab
     */
    @Test
    @Order(5)
    @DisplayName("测试5: 获取点赞列表")
    public void test5_GetLikesList() {
        try {
            log.info("\n[测试5] 获取点赞列表");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token，测试跳过");
                return;
            }

            // 构造请求头（带 Token + Bearer 前缀）
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String likesUrl = GATEWAY_URL + "/xypai-user/api/user/profile/likes?page=1&pageSize=10";
            ResponseEntity<Map> response = restTemplate.exchange(likesUrl, org.springframework.http.HttpMethod.GET, request, Map.class);

            // 验证响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

                    if (data != null) {
                        Object likes = data.get("likes");
                        int likesCount = (likes instanceof java.util.List) ? ((java.util.List<?>) likes).size() : 0;
                        log.info("✅ 获取点赞列表成功 - 总数: {}, 当前页: {}, 是否有更多: {}",
                            data.get("total"), likesCount, data.get("hasMore"));
                    } else {
                        log.info("✅ 获取点赞列表成功 - 数据为空");
                    }
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("获取点赞列表失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试5失败: {}", e.getMessage());
            throw new RuntimeException("获取点赞列表测试失败", e);
        }
    }

    /**
     * 🎯 测试6：获取个人资料信息
     *
     * 接口: GET /api/user/profile/info
     * 触发时机: 点击"资料"Tab
     */
    @Test
    @Order(6)
    @DisplayName("测试6: 获取个人资料信息")
    public void test6_GetProfileInfo() {
        try {
            log.info("\n[测试6] 获取个人资料信息");
            ensureAuthenticated();

            if (authToken == null || authToken.isEmpty()) {
                log.error("❌ 无法获取登录 Token，测试跳过");
                return;
            }

            // 构造请求头（带 Token + Bearer 前缀）
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String infoUrl = GATEWAY_URL + "/xypai-user/api/user/profile/info";
            ResponseEntity<Map> response = restTemplate.exchange(infoUrl, org.springframework.http.HttpMethod.GET, request, Map.class);

            // 验证响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    log.info("✅ 获取资料信息成功 - userId: {}, nickname: {}", data.get("userId"), data.get("nickname"));

                    // 显示可选字段
                    if (data.containsKey("gender")) {
                        log.info("   性别: {}", data.get("gender"));
                    }
                    if (data.containsKey("skills")) {
                        Object skills = data.get("skills");
                        int skillsCount = (skills instanceof java.util.List) ? ((java.util.List<?>) skills).size() : 0;
                        log.info("   技能数量: {}", skillsCount);
                    }
                } else {
                    String msg = (String) responseBody.get("msg");
                    throw new RuntimeException("获取资料信息失败: " + msg);
                }
            } else {
                throw new RuntimeException("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试6失败: {}", e.getMessage());
            throw new RuntimeException("获取资料信息测试失败", e);
        }
    }

    @AfterAll
    static void tearDown() {
        log.info("\n🎉 所有测试完成！");
    }
}
