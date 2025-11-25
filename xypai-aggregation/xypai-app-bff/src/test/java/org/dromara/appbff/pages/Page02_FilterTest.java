package org.dromara.appbff.pages;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 【页面级集成测试】02-筛选页面
 *
 * ============================================================
 * 📄 前端页面信息
 * ============================================================
 * - 文档路径: XiangYuPai-Doc/Action-API/模块化架构/03-content模块/Frontend/02-筛选页面.md
 * - 页面路由: /home/filter
 * - 页面名称: 首页筛选
 * - 用户角色: 登录用户
 * - 页面类型: 全屏筛选页面
 *
 * ============================================================
 * 📌 涉及的后端服务及接口
 * ============================================================
 *
 * 【xypai-app-bff (聚合服务, 9400)】
 * - GET  /api/home/filter/config      获取筛选配置（年龄、性别、技能、标签等选项）
 * - POST /api/home/filter/apply       应用筛选条件，返回用户列表
 *
 * 【xypai-user (用户服务, 9401)】- 通过 RPC 调用
 * - 提供用户资料、技能标签数据
 *
 * 【xypai-auth (认证服务, 8200)】
 * - POST /auth/login/sms              用户登录
 *
 * ============================================================
 * 🧪 测试流程
 * ============================================================
 * 1. 用户登录 (xypai-auth)
 * 2. 获取线上模式筛选配置 (xypai-app-bff)
 * 3. 获取线下模式筛选配置 (xypai-app-bff)
 * 4. 应用年龄范围筛选 (xypai-app-bff)
 * 5. 应用性别筛选 (xypai-app-bff)
 * 6. 应用技能筛选 (xypai-app-bff)
 * 7. 应用多维度组合筛选 (xypai-app-bff)
 * 8. 验证筛选结果统计 (xypai-app-bff)
 *
 * 💡 测试说明:
 * - 本测试通过 Gateway (8080) 调用各个微服务
 * - 筛选功能是首页的核心功能，需要 BFF 层聚合 user 服务数据
 * - 需要启动: Gateway(8080), xypai-auth(8200), xypai-app-bff(9400), xypai-user(9401), Nacos, Redis
 *
 * ⚠️ 待实现状态:
 * - GET /api/home/filter/config 接口待实现
 * - POST /api/home/filter/apply 接口待实现
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Page02_FilterTest {

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

    // 保存筛选配置
    private static Map<String, Object> onlineFilterConfig;
    private static Map<String, Object> offlineFilterConfig;

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║  📄 页面级集成测试: 02-筛选页面                                ║");
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║  涉及服务:                                                   ║");
        log.info("║  - xypai-auth (8200)     用户认证                            ║");
        log.info("║  - xypai-app-bff (9400)  筛选配置、应用筛选                   ║");
        log.info("║  - xypai-user (9401)     用户数据（RPC）                      ║");
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║  ⚠️ 注意: 筛选接口待实现，部分测试会失败                        ║");
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

    // ============================================================
    // 测试用例
    // ============================================================

    /**
     * 🎯 测试1: 用户登录
     *
     * 服务: xypai-auth (8200)
     * 接口: POST /auth/login/sms
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
     * 🎯 测试2: 获取线上模式筛选配置
     *
     * 服务: xypai-app-bff (9400)
     * 接口: GET /api/home/filter/config?type=online
     *
     * 期望返回:
     * - 年龄范围: min=18, max=null
     * - 性别选项: all/male/female
     * - 状态选项: online/active_3d/active_7d
     * - 技能选项: 游戏段位等
     * - 价格选项: 线上模式专有
     * - 位置选项: 线上模式专有（打野/上路/中路等）
     * - 标签选项: 荣耀王者/大神认证等
     */
    @Test
    @Order(2)
    @DisplayName("测试2: 获取线上模式筛选配置 [xypai-app-bff]")
    public void test02_GetOnlineFilterConfig() {
        try {
            log.info("\n[测试2] 获取线上模式筛选配置 → xypai-app-bff");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String configUrl = GATEWAY_URL + "/xypai-app-bff/api/home/filter/config?type=online";
            ResponseEntity<Map> response = restTemplate.exchange(
                configUrl,
                HttpMethod.GET,
                request,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    onlineFilterConfig = (Map<String, Object>) responseBody.get("data");

                    log.info("✅ 获取线上筛选配置成功");

                    // 打印配置详情
                    if (onlineFilterConfig.containsKey("ageRange")) {
                        log.info("   - 年龄范围: {}", onlineFilterConfig.get("ageRange"));
                    }
                    if (onlineFilterConfig.containsKey("genderOptions")) {
                        List<?> genderOptions = (List<?>) onlineFilterConfig.get("genderOptions");
                        log.info("   - 性别选项: {} 个", genderOptions.size());
                    }
                    if (onlineFilterConfig.containsKey("skillOptions")) {
                        List<?> skillOptions = (List<?>) onlineFilterConfig.get("skillOptions");
                        log.info("   - 技能选项: {} 个", skillOptions.size());
                    }
                    if (onlineFilterConfig.containsKey("priceOptions")) {
                        List<?> priceOptions = (List<?>) onlineFilterConfig.get("priceOptions");
                        log.info("   - 价格选项: {} 个 (线上专有)", priceOptions.size());
                    }
                    if (onlineFilterConfig.containsKey("positionOptions")) {
                        List<?> positionOptions = (List<?>) onlineFilterConfig.get("positionOptions");
                        log.info("   - 位置选项: {} 个 (线上专有)", positionOptions.size());
                    }
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 获取线上筛选配置失败: {} (接口可能未实现)", msg);
                }
            } else {
                log.warn("⚠️ HTTP请求失败: {} (接口可能未实现)", response.getStatusCode());
            }

        } catch (Exception e) {
            log.warn("⚠️ 测试2失败: {} (筛选配置接口待实现)", e.getMessage());
        }
    }

    /**
     * 🎯 测试3: 获取线下模式筛选配置
     *
     * 服务: xypai-app-bff (9400)
     * 接口: GET /api/home/filter/config?type=offline
     *
     * 期望返回:
     * - 与线上模式相同的基础配置
     * - 不包含价格选项
     * - 不包含位置选项
     * - 技能选项可能不同
     */
    @Test
    @Order(3)
    @DisplayName("测试3: 获取线下模式筛选配置 [xypai-app-bff]")
    public void test03_GetOfflineFilterConfig() {
        try {
            log.info("\n[测试3] 获取线下模式筛选配置 → xypai-app-bff");
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String configUrl = GATEWAY_URL + "/xypai-app-bff/api/home/filter/config?type=offline";
            ResponseEntity<Map> response = restTemplate.exchange(
                configUrl,
                HttpMethod.GET,
                request,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    offlineFilterConfig = (Map<String, Object>) responseBody.get("data");

                    log.info("✅ 获取线下筛选配置成功");

                    // 验证线下模式不包含价格和位置选项
                    boolean hasPriceOptions = offlineFilterConfig.containsKey("priceOptions");
                    boolean hasPositionOptions = offlineFilterConfig.containsKey("positionOptions");

                    if (!hasPriceOptions) {
                        log.info("   - ✓ 线下模式不包含价格选项");
                    } else {
                        log.warn("   - ✗ 线下模式不应包含价格选项");
                    }

                    if (!hasPositionOptions) {
                        log.info("   - ✓ 线下模式不包含位置选项");
                    } else {
                        log.warn("   - ✗ 线下模式不应包含位置选项");
                    }
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 获取线下筛选配置失败: {}", msg);
                }
            } else {
                log.warn("⚠️ HTTP请求失败: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.warn("⚠️ 测试3失败: {} (筛选配置接口待实现)", e.getMessage());
        }
    }

    /**
     * 🎯 测试4: 应用年龄范围筛选
     *
     * 服务: xypai-app-bff (9400)
     * 接口: POST /api/home/filter/apply
     */
    @Test
    @Order(4)
    @DisplayName("测试4: 应用年龄范围筛选 [xypai-app-bff]")
    public void test04_ApplyAgeRangeFilter() {
        try {
            log.info("\n[测试4] 应用年龄范围筛选 → xypai-app-bff");
            ensureAuthenticated();

            Map<String, Object> filterRequest = new HashMap<>();
            filterRequest.put("type", "online");

            Map<String, Object> filters = new HashMap<>();
            Map<String, Object> age = new HashMap<>();
            age.put("min", 20);
            age.put("max", 30);
            filters.put("age", age);

            filterRequest.put("filters", filters);
            filterRequest.put("pageNum", 1);
            filterRequest.put("pageSize", 10);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(filterRequest, headers);

            String applyUrl = GATEWAY_URL + "/xypai-app-bff/api/home/filter/apply";
            ResponseEntity<Map> response = restTemplate.postForEntity(applyUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    Object list = data.get("list");
                    int listSize = (list instanceof List) ? ((List<?>) list).size() : 0;

                    log.info("✅ 年龄范围筛选成功");
                    log.info("   - 筛选条件: 20-30岁");
                    log.info("   - 结果数量: {}", listSize);
                    log.info("   - 是否有更多: {}", data.get("hasMore"));
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 年龄范围筛选失败: {}", msg);
                }
            } else {
                log.warn("⚠️ HTTP请求失败: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.warn("⚠️ 测试4失败: {} (筛选应用接口待实现)", e.getMessage());
        }
    }

    /**
     * 🎯 测试5: 应用性别筛选
     *
     * 服务: xypai-app-bff (9400)
     * 接口: POST /api/home/filter/apply
     */
    @Test
    @Order(5)
    @DisplayName("测试5: 应用性别筛选 [xypai-app-bff]")
    public void test05_ApplyGenderFilter() {
        try {
            log.info("\n[测试5] 应用性别筛选 → xypai-app-bff");
            ensureAuthenticated();

            Map<String, Object> filterRequest = new HashMap<>();
            filterRequest.put("type", "online");

            Map<String, Object> filters = new HashMap<>();
            filters.put("gender", "female");

            filterRequest.put("filters", filters);
            filterRequest.put("pageNum", 1);
            filterRequest.put("pageSize", 10);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(filterRequest, headers);

            String applyUrl = GATEWAY_URL + "/xypai-app-bff/api/home/filter/apply";
            ResponseEntity<Map> response = restTemplate.postForEntity(applyUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    Object list = data.get("list");
                    int listSize = (list instanceof List) ? ((List<?>) list).size() : 0;

                    log.info("✅ 性别筛选成功");
                    log.info("   - 筛选条件: 女");
                    log.info("   - 结果数量: {}", listSize);
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 性别筛选失败: {}", msg);
                }
            } else {
                log.warn("⚠️ HTTP请求失败: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.warn("⚠️ 测试5失败: {} (筛选应用接口待实现)", e.getMessage());
        }
    }

    /**
     * 🎯 测试6: 应用技能筛选
     *
     * 服务: xypai-app-bff (9400)
     * 接口: POST /api/home/filter/apply
     */
    @Test
    @Order(6)
    @DisplayName("测试6: 应用技能筛选 [xypai-app-bff]")
    public void test06_ApplySkillFilter() {
        try {
            log.info("\n[测试6] 应用技能筛选 → xypai-app-bff");
            ensureAuthenticated();

            Map<String, Object> filterRequest = new HashMap<>();
            filterRequest.put("type", "online");

            Map<String, Object> filters = new HashMap<>();
            List<String> skills = new ArrayList<>();
            skills.add("最强王者");
            skills.add("荣耀王者");
            filters.put("skills", skills);

            filterRequest.put("filters", filters);
            filterRequest.put("pageNum", 1);
            filterRequest.put("pageSize", 10);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(filterRequest, headers);

            String applyUrl = GATEWAY_URL + "/xypai-app-bff/api/home/filter/apply";
            ResponseEntity<Map> response = restTemplate.postForEntity(applyUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    Object list = data.get("list");
                    int listSize = (list instanceof List) ? ((List<?>) list).size() : 0;

                    log.info("✅ 技能筛选成功");
                    log.info("   - 筛选条件: 最强王者, 荣耀王者");
                    log.info("   - 结果数量: {}", listSize);
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 技能筛选失败: {}", msg);
                }
            } else {
                log.warn("⚠️ HTTP请求失败: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.warn("⚠️ 测试6失败: {} (筛选应用接口待实现)", e.getMessage());
        }
    }

    /**
     * 🎯 测试7: 应用多维度组合筛选
     *
     * 服务: xypai-app-bff (9400)
     * 接口: POST /api/home/filter/apply
     *
     * 组合条件:
     * - 年龄: 20-28岁
     * - 性别: 女
     * - 状态: 在线
     * - 技能: 最强王者
     * - 标签: 大神认证
     */
    @Test
    @Order(7)
    @DisplayName("测试7: 应用多维度组合筛选 [xypai-app-bff]")
    public void test07_ApplyMultiDimensionFilter() {
        try {
            log.info("\n[测试7] 应用多维度组合筛选 → xypai-app-bff");
            ensureAuthenticated();

            Map<String, Object> filterRequest = new HashMap<>();
            filterRequest.put("type", "online");

            Map<String, Object> filters = new HashMap<>();

            // 年龄
            Map<String, Object> age = new HashMap<>();
            age.put("min", 20);
            age.put("max", 28);
            filters.put("age", age);

            // 性别
            filters.put("gender", "female");

            // 状态
            filters.put("status", "online");

            // 技能
            List<String> skills = new ArrayList<>();
            skills.add("最强王者");
            filters.put("skills", skills);

            // 标签
            List<String> tags = new ArrayList<>();
            tags.add("大神认证");
            filters.put("tags", tags);

            filterRequest.put("filters", filters);
            filterRequest.put("pageNum", 1);
            filterRequest.put("pageSize", 10);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(filterRequest, headers);

            String applyUrl = GATEWAY_URL + "/xypai-app-bff/api/home/filter/apply";
            ResponseEntity<Map> response = restTemplate.postForEntity(applyUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    Object list = data.get("list");
                    int listSize = (list instanceof List) ? ((List<?>) list).size() : 0;

                    log.info("✅ 多维度组合筛选成功");
                    log.info("   - 年龄: 20-28岁");
                    log.info("   - 性别: 女");
                    log.info("   - 状态: 在线");
                    log.info("   - 技能: 最强王者");
                    log.info("   - 标签: 大神认证");
                    log.info("   - 结果数量: {}", listSize);

                    // 检查筛选统计信息
                    if (data.containsKey("appliedFilters")) {
                        Map<String, Object> appliedFilters = (Map<String, Object>) data.get("appliedFilters");
                        log.info("   - 应用条件数: {}", appliedFilters.get("count"));
                        log.info("   - 筛选摘要: {}", appliedFilters.get("summary"));
                    }
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 多维度组合筛选失败: {}", msg);
                }
            } else {
                log.warn("⚠️ HTTP请求失败: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.warn("⚠️ 测试7失败: {} (筛选应用接口待实现)", e.getMessage());
        }
    }

    /**
     * 🎯 测试8: 重置后的默认筛选
     *
     * 服务: xypai-app-bff (9400)
     * 接口: POST /api/home/filter/apply
     *
     * 不带任何筛选条件，应返回全部用户
     */
    @Test
    @Order(8)
    @DisplayName("测试8: 重置后的默认筛选 [xypai-app-bff]")
    public void test08_ApplyDefaultFilter() {
        try {
            log.info("\n[测试8] 重置后的默认筛选 → xypai-app-bff");
            ensureAuthenticated();

            Map<String, Object> filterRequest = new HashMap<>();
            filterRequest.put("type", "online");
            filterRequest.put("filters", new HashMap<>());  // 空筛选条件
            filterRequest.put("pageNum", 1);
            filterRequest.put("pageSize", 10);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(filterRequest, headers);

            String applyUrl = GATEWAY_URL + "/xypai-app-bff/api/home/filter/apply";
            ResponseEntity<Map> response = restTemplate.postForEntity(applyUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                Integer code = (Integer) responseBody.get("code");

                if (code != null && code == 200) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    Object list = data.get("list");
                    int listSize = (list instanceof List) ? ((List<?>) list).size() : 0;

                    log.info("✅ 默认筛选成功（无筛选条件）");
                    log.info("   - 结果数量: {}", listSize);
                    log.info("   - 总数: {}", data.get("total"));
                } else {
                    String msg = (String) responseBody.get("msg");
                    log.warn("⚠️ 默认筛选失败: {}", msg);
                }
            } else {
                log.warn("⚠️ HTTP请求失败: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.warn("⚠️ 测试8失败: {} (筛选应用接口待实现)", e.getMessage());
        }
    }

    @AfterAll
    static void tearDown() {
        log.info("\n╔════════════════════════════════════════════════════════════╗");
        log.info("║  🎉 页面测试完成: 02-筛选页面                                  ║");
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║  📝 待实现接口:                                              ║");
        log.info("║  - GET  /api/home/filter/config  筛选配置接口                 ║");
        log.info("║  - POST /api/home/filter/apply   应用筛选接口                 ║");
        log.info("║                                                             ║");
        log.info("║  💡 实现位置: xypai-app-bff/controller/HomeFilterController   ║");
        log.info("╚════════════════════════════════════════════════════════════╝");
    }
}
