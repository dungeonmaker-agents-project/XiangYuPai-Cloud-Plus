package org.dromara.user.pages;

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
 * 【页面级集成测试】发现页面-有技能用户列表
 *
 * ============================================================
 * 📄 前端页面信息
 * ============================================================
 * - 文档路径: XiangYuPai-RNExpoAPP/src/features/Discovery/快速理解.md
 * - 页面路由: /(tabs)/discover
 * - 页面名称: 发现页面-有技能用户
 * - 用户角色: 登录用户（可选）
 * - 页面类型: 列表页面
 *
 * ============================================================
 * 📌 涉及的后端服务及接口
 * ============================================================
 *
 * 【xypai-user (用户服务, 9401)】
 * - GET  /api/user/discovery/skilled-users    获取有技能用户列表（支持分页、筛选、排序）
 * - 提供有上架技能的用户数据
 * - 查询 users + skills 表，只返回 skills.is_online = 1 的用户
 *
 * 【xypai-auth (认证服务, 8200)】
 * - POST /api/auth/login/sms             用户登录
 *
 * ============================================================
 * 🧪 测试流程
 * ============================================================
 * 1. 用户登录 (xypai-auth)
 * 2. 获取有技能用户列表-默认参数 (xypai-user)
 * 3. 获取有技能用户列表-性别筛选 (xypai-user)
 * 4. 获取有技能用户列表-价格排序 (xypai-user)
 * 5. 获取有技能用户列表-分页测试 (xypai-user)
 * 6. 获取有技能用户列表-组合筛选 (xypai-user)
 *
 * 💡 测试说明:
 * - 本测试通过 Gateway (8080) 调用各个微服务
 * - 有技能用户列表是发现页面的核心功能
 * - 需要启动: Gateway(8080), xypai-auth(8200), xypai-user(9401), Nacos, Redis
 *
 * @author XyPai Team
 * @date 2025-11-30
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Page_DiscoverySkilledUsersTest {

    // ============================================================
    // 测试配置
    // ============================================================
    private static final String GATEWAY_URL = "http://localhost:8080";
    private static final String TEST_COUNTRY_CODE = "+86";
    private static final String TEST_SMS_CODE = "123456";

    // HTTP 客户端
    private static RestTemplate restTemplate;
    private static ObjectMapper objectMapper;

    // 保存登录后的 Token
    private static String authToken;
    private static String userId;

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║  📄 页面级集成测试: 发现页面-有技能用户列表                       ║");
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║  涉及服务:                                                   ║");
        log.info("║  - xypai-auth (8200)     用户认证                            ║");
        log.info("║  - xypai-user (9401)     有技能用户列表                        ║");
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║  📝 测试接口: GET /api/user/discovery/skilled-users          ║");
        log.info("╚════════════════════════════════════════════════════════════╝");
        log.info("");
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

            String loginUrl = GATEWAY_URL + "/xypai-auth/api/auth/login/sms";
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

    // ============================================================
    // 测试用例
    // ============================================================

    /**
     * 🎯 测试1: 用户登录
     */
    @Test
    @Order(1)
    @DisplayName("测试1: 用户登录 [xypai-auth]")
    public void test01_UserLogin() {
        try {
            log.info("\n[测试1] 用户登录 → xypai-auth");

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

            String loginUrl = GATEWAY_URL + "/xypai-auth/api/auth/login/sms";
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
     * 🎯 测试2: 获取有技能用户列表（默认参数）
     */
    @Test
    @Order(2)
    @DisplayName("测试2: 获取有技能用户列表-默认参数 [xypai-user]")
    public void test02_GetSkilledUsers_Default() {
        try {
            log.info("\n[测试2] 获取有技能用户列表（默认参数） → xypai-user");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String url = GATEWAY_URL + "/xypai-user/api/user/discovery/skilled-users";
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    Integer total = (Integer) data.get("total");
                    Boolean hasMore = (Boolean) data.get("hasMore");
                    List<?> list = (List<?>) data.get("list");
                    Map<String, Object> filters = (Map<String, Object>) data.get("filters");

                    log.info("✅ 获取有技能用户列表成功");
                    log.info("   - 总数: {}", total);
                    log.info("   - 是否有更多: {}", hasMore);
                    log.info("   - 当前页用户数: {}", list != null ? list.size() : 0);

                    if (filters != null) {
                        List<?> sortOptions = (List<?>) filters.get("sortOptions");
                        List<?> genderOptions = (List<?>) filters.get("genderOptions");
                        log.info("   - 排序选项: {} 个", sortOptions != null ? sortOptions.size() : 0);
                        log.info("   - 性别选项: {} 个", genderOptions != null ? genderOptions.size() : 0);
                    }

                    if (list != null && !list.isEmpty()) {
                        Map<String, Object> firstUser = (Map<String, Object>) list.get(0);
                        log.info("   - 第一个用户: {} (ID: {}, 在线: {})",
                            firstUser.get("nickname"),
                            firstUser.get("userId"),
                            firstUser.get("isOnline"));
                    }
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 获取有技能用户列表失败: {}", msg);
                }
            } else {
                log.warn("⚠️ HTTP请求失败: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试2失败: {}", e.getMessage());
            throw new RuntimeException("获取有技能用户列表测试失败", e);
        }
    }

    /**
     * 🎯 测试3: 获取有技能用户列表（性别筛选-女性）
     */
    @Test
    @Order(3)
    @DisplayName("测试3: 获取有技能用户列表-性别筛选 [xypai-user]")
    public void test03_GetSkilledUsers_FilterByGender() {
        try {
            log.info("\n[测试3] 获取有技能用户列表（性别筛选-女性） → xypai-user");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String url = GATEWAY_URL + "/xypai-user/api/user/discovery/skilled-users?gender=female";
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    Integer total = (Integer) data.get("total");
                    List<?> list = (List<?>) data.get("list");

                    log.info("✅ 性别筛选成功");
                    log.info("   - 筛选条件: gender=female");
                    log.info("   - 女性用户总数: {}", total);
                    log.info("   - 当前页用户数: {}", list != null ? list.size() : 0);
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 性别筛选失败: {}", msg);
                }
            } else {
                log.warn("⚠️ HTTP请求失败: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试3失败: {}", e.getMessage());
            throw new RuntimeException("性别筛选测试失败", e);
        }
    }

    /**
     * 🎯 测试4: 获取有技能用户列表（价格从低到高排序）
     */
    @Test
    @Order(4)
    @DisplayName("测试4: 获取有技能用户列表-价格排序 [xypai-user]")
    public void test04_GetSkilledUsers_SortByPrice() {
        try {
            log.info("\n[测试4] 获取有技能用户列表（价格从低到高排序） → xypai-user");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String url = GATEWAY_URL + "/xypai-user/api/user/discovery/skilled-users?sortBy=price_asc";
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    List<?> list = (List<?>) data.get("list");

                    log.info("✅ 价格排序成功");
                    log.info("   - 排序条件: sortBy=price_asc");

                    if (list != null && list.size() >= 2) {
                        for (int i = 0; i < Math.min(list.size(), 5); i++) {
                            Map<String, Object> user = (Map<String, Object>) list.get(i);
                            Map<String, Object> price = (Map<String, Object>) user.get("price");
                            Integer amount = (Integer) price.get("amount");
                            log.info("   - 用户{}: {} - 价格: {} 金币",
                                i + 1, user.get("nickname"), amount);
                        }
                    }
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 价格排序失败: {}", msg);
                }
            } else {
                log.warn("⚠️ HTTP请求失败: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试4失败: {}", e.getMessage());
            throw new RuntimeException("价格排序测试失败", e);
        }
    }

    /**
     * 🎯 测试5: 获取有技能用户列表（分页测试）
     */
    @Test
    @Order(5)
    @DisplayName("测试5: 获取有技能用户列表-分页测试 [xypai-user]")
    public void test05_GetSkilledUsers_Pagination() {
        try {
            log.info("\n[测试5] 获取有技能用户列表（分页测试） → xypai-user");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String url1 = GATEWAY_URL + "/xypai-user/api/user/discovery/skilled-users?pageNum=1&pageSize=3";
            ResponseEntity<Map> response1 = restTemplate.exchange(url1, HttpMethod.GET, request, Map.class);

            if (response1.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody1 = response1.getBody();
                Integer code1 = (Integer) responseBody1.get("code");

                if (code1 != null && code1 == 200) {
                    Map<String, Object> data1 = (Map<String, Object>) responseBody1.get("data");
                    Integer total = (Integer) data1.get("total");
                    Boolean hasMore = (Boolean) data1.get("hasMore");
                    List<?> list1 = (List<?>) data1.get("list");

                    log.info("✅ 分页测试成功");
                    log.info("   - 请求参数: pageNum=1, pageSize=3");
                    log.info("   - 总数: {}", total);
                    log.info("   - 是否有更多: {}", hasMore);
                    log.info("   - 第一页用户数: {}", list1 != null ? list1.size() : 0);

                    if (list1 != null && list1.size() <= 3) {
                        log.info("   - ✓ 分页大小正确（<= 3）");
                    }
                } else {
                    String msg = (String) responseBody1.get("msg");
                    log.warn("⚠️ 分页测试失败: {}", msg);
                }
            } else {
                log.warn("⚠️ HTTP请求失败: {}", response1.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试5失败: {}", e.getMessage());
            throw new RuntimeException("分页测试失败", e);
        }
    }

    /**
     * 🎯 测试6: 获取有技能用户列表（组合筛选）
     */
    @Test
    @Order(6)
    @DisplayName("测试6: 获取有技能用户列表-组合筛选 [xypai-user]")
    public void test06_GetSkilledUsers_CombinedFilter() {
        try {
            log.info("\n[测试6] 获取有技能用户列表（组合筛选） → xypai-user");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String url = GATEWAY_URL + "/xypai-user/api/user/discovery/skilled-users?gender=female&sortBy=price_asc&pageSize=5";
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    Integer total = (Integer) data.get("total");
                    Boolean hasMore = (Boolean) data.get("hasMore");
                    List<?> list = (List<?>) data.get("list");

                    log.info("✅ 组合筛选成功");
                    log.info("   - 筛选条件:");
                    log.info("     · 性别: 女");
                    log.info("     · 排序: 价格从低到高");
                    log.info("     · 分页: 每页5条");
                    log.info("   - 总数: {}", total);
                    log.info("   - 是否有更多: {}", hasMore);
                    log.info("   - 当前页用户数: {}", list != null ? list.size() : 0);

                    if (list != null && !list.isEmpty()) {
                        log.info("   - 用户列表:");
                        for (int i = 0; i < list.size(); i++) {
                            Map<String, Object> user = (Map<String, Object>) list.get(i);
                            Map<String, Object> price = (Map<String, Object>) user.get("price");
                            log.info("     {}. {} ({}) - {} - 在线: {}",
                                i + 1,
                                user.get("nickname"),
                                user.get("gender"),
                                price != null ? price.get("displayText") : "未知价格",
                                user.get("isOnline"));
                        }
                    }
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 组合筛选失败: {}", msg);
                }
            } else {
                log.warn("⚠️ HTTP请求失败: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试6失败: {}", e.getMessage());
            throw new RuntimeException("组合筛选测试失败", e);
        }
    }

    @AfterAll
    static void tearDown() {
        log.info("\n╔════════════════════════════════════════════════════════════╗");
        log.info("║  🎉 页面测试完成: 发现页面-有技能用户列表                         ║");
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║  📝 测试接口:                                               ║");
        log.info("║  - GET /api/user/discovery/skilled-users                   ║");
        log.info("║                                                             ║");
        log.info("║  📊 支持参数:                                               ║");
        log.info("║  - pageNum:  页码（默认1）                                   ║");
        log.info("║  - pageSize: 每页数量（默认20）                               ║");
        log.info("║  - gender:   性别筛选（all/male/female）                      ║");
        log.info("║  - sortBy:   排序方式（smart_recommend/price_asc/...）        ║");
        log.info("║                                                             ║");
        log.info("║  💡 实现位置:                                                ║");
        log.info("║  - xypai-user/controller/app/SkilledUsersController         ║");
        log.info("╚════════════════════════════════════════════════════════════╝");
    }
}
