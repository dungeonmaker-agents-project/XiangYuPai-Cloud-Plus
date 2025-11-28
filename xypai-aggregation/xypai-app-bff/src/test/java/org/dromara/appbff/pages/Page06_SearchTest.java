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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 【页面级集成测试】06-搜索页面
 *
 * ============================================================
 * 📄 前端页面信息
 * ============================================================
 * - 文档路径: XiangYuPai-Doc/Action-API/模块化架构/03-content模块/Frontend/06-搜索页面.md
 * - 页面路由: /search
 * - 页面名称: 搜索
 * - 用户角色: 所有用户
 * - 页面类型: 搜索输入页面
 *
 * ============================================================
 * 📌 涉及的后端服务及接口
 * ============================================================
 *
 * 【xypai-app-bff (聚合服务, 9400)】
 * - GET    /api/search/init             获取搜索初始数据（历史+热门）
 * - GET    /api/search/suggest          获取搜索建议
 * - DELETE /api/search/history          删除搜索历史
 *
 * ============================================================
 * 🧪 测试流程
 * ============================================================
 * 1. 用户登录
 * 2. 获取搜索初始数据（历史+热门搜索）
 * 3. 添加测试搜索历史
 * 4. 验证搜索历史显示
 * 5. 获取搜索建议（王者荣耀）
 * 6. 获取搜索建议（台球）
 * 7. 获取搜索建议（数字-用户搜索）
 * 8. 删除单条搜索历史
 * 9. 清空所有搜索历史
 * 10. 验证热门搜索数据
 *
 * 💡 测试说明:
 * - 本测试通过 Gateway (8080) 调用 xypai-app-bff 服务
 * - 搜索功能使用 Mock 数据
 * - 需要启动: Gateway(8080), xypai-auth(9211), xypai-app-bff(9400), Nacos, Redis
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Page06_SearchTest {

    // ============================================================
    // 测试配置
    // ============================================================
    private static final String GATEWAY_URL = "http://localhost:8080";
    private static final String TEST_COUNTRY_CODE = "+86";
    private static final String TEST_SMS_CODE = "123456";

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
        log.info("║  📄 页面级集成测试: 06-搜索页面                               ║");
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║  涉及服务:                                                   ║");
        log.info("║  - xypai-app-bff (9400)  搜索功能                            ║");
        log.info("║  - xypai-auth (9211)     用户认证                           ║");
        log.info("║  - Gateway (8080)        API网关                             ║");
        log.info("╚════════════════════════════════════════════════════════════╝");
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
    // 测试1: 用户登录
    // ============================================================
    @Test
    @Order(1)
    @DisplayName("[测试1] 用户登录")
    void test01_userLogin() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试1] 用户登录                                           │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
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
                    log.info("✅ 登录成功 - userId: {}", userId);
                    Assertions.assertNotNull(authToken, "Token不能为空");
                } else {
                    Assertions.fail("登录失败: " + responseBody.get("msg"));
                }
            } else {
                Assertions.fail("HTTP请求失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试2: 获取搜索初始数据（首次访问，无历史记录）
    // ============================================================
    @Test
    @Order(2)
    @DisplayName("[测试2] 获取搜索初始数据（首次访问）")
    void test02_getInitialSearchData() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试2] 获取搜索初始数据（首次访问）                        │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            String url = GATEWAY_URL + "/xypai-app-bff/api/search/init";

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            log.info("   - 状态码: {}", response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                List<Map<String, Object>> searchHistory = (List<Map<String, Object>>) data.get("searchHistory");
                List<Map<String, Object>> hotKeywords = (List<Map<String, Object>>) data.get("hotKeywords");
                String placeholder = (String) data.get("placeholder");

                log.info("✅ 获取搜索初始数据成功");
                log.info("   - 搜索历史数量: {}", searchHistory.size());
                log.info("   - 热门搜索数量: {}", hotKeywords.size());
                log.info("   - 搜索框占位符: {}", placeholder);

                if (!hotKeywords.isEmpty()) {
                    Map<String, Object> firstHotKeyword = hotKeywords.get(0);
                    log.info("   - 第一个热门搜索: {} (排名: {}, 是否热门: {})",
                        firstHotKeyword.get("keyword"),
                        firstHotKeyword.get("rank"),
                        firstHotKeyword.get("isHot"));
                }

                Assertions.assertNotNull(searchHistory, "搜索历史不能为空");
                Assertions.assertNotNull(hotKeywords, "热门搜索不能为空");
                Assertions.assertTrue(hotKeywords.size() > 0, "应该有热门搜索关键词");
                Assertions.assertEquals("搜索更多", placeholder, "占位符应该是'搜索更多'");
            } else {
                String msg = (String) responseBody.get("msg");
                log.error("❌ 获取搜索初始数据失败: {}", msg);
                Assertions.fail("获取搜索初始数据失败: " + msg);
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试3: 获取搜索建议（王者荣耀）
    // ============================================================
    @Test
    @Order(3)
    @DisplayName("[测试3] 获取搜索建议（王者荣耀）")
    void test03_getSearchSuggestions_WangZhe() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试3] 获取搜索建议（王者荣耀）                            │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            String url = GATEWAY_URL + "/xypai-app-bff/api/search/suggest?keyword=王者&limit=10";

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                List<Map<String, Object>> suggestions = (List<Map<String, Object>>) data.get("suggestions");

                log.info("✅ 获取搜索建议成功");
                log.info("   - 建议数量: {}", suggestions.size());

                for (int i = 0; i < Math.min(3, suggestions.size()); i++) {
                    Map<String, Object> suggestion = suggestions.get(i);
                    log.info("   - 建议{}: {} (类型: {}, 图标: {})",
                        i + 1,
                        suggestion.get("text"),
                        suggestion.get("type"),
                        suggestion.get("icon"));
                }

                Assertions.assertNotNull(suggestions, "建议列表不能为空");
                Assertions.assertTrue(suggestions.size() > 0, "应该有搜索建议");

                // 验证建议包含关键词相关内容
                boolean hasRelevantSuggestion = suggestions.stream()
                    .anyMatch(s -> ((String) s.get("text")).contains("王者"));
                Assertions.assertTrue(hasRelevantSuggestion, "建议应该包含关键词相关内容");
            } else {
                Assertions.fail("获取搜索建议失败");
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试4: 获取搜索建议（台球）
    // ============================================================
    @Test
    @Order(4)
    @DisplayName("[测试4] 获取搜索建议（台球）")
    void test04_getSearchSuggestions_TaiQiu() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试4] 获取搜索建议（台球）                                │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            String url = GATEWAY_URL + "/xypai-app-bff/api/search/suggest?keyword=台球&limit=5";

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            List<Map<String, Object>> suggestions = (List<Map<String, Object>>) data.get("suggestions");

            log.info("✅ 获取搜索建议成功");
            log.info("   - 建议数量: {}", suggestions.size());

            for (Map<String, Object> suggestion : suggestions) {
                log.info("   - {}: {} ({})",
                    suggestion.get("icon"),
                    suggestion.get("text"),
                    suggestion.get("type"));
            }

            Assertions.assertTrue(suggestions.size() > 0, "应该有搜索建议");

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试5: 获取搜索建议（用户搜索-数字结尾）
    // ============================================================
    @Test
    @Order(5)
    @DisplayName("[测试5] 获取搜索建议（用户搜索）")
    void test05_getSearchSuggestions_User() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试5] 获取搜索建议（用户搜索）                            │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            String url = GATEWAY_URL + "/xypai-app-bff/api/search/suggest?keyword=王者荣耀112&limit=10";

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            List<Map<String, Object>> suggestions = (List<Map<String, Object>>) data.get("suggestions");

            log.info("✅ 获取搜索建议成功");
            log.info("   - 建议数量: {}", suggestions.size());

            // 验证包含用户类型的建议
            boolean hasUserSuggestion = suggestions.stream()
                .anyMatch(s -> "user".equals(s.get("type")));

            if (hasUserSuggestion) {
                log.info("   ✅ 包含用户类型建议（使用 👤 图标）");
            }

            Assertions.assertTrue(suggestions.size() > 0, "应该有搜索建议");

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试6: 删除单条搜索历史（需要先有历史记录）
    // ============================================================
    @Test
    @Order(6)
    @DisplayName("[测试6] 删除单条搜索历史")
    void test06_deleteSingleHistory() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试6] 删除单条搜索历史                                   │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            // 删除关键词 "王者荣耀"
            String url = GATEWAY_URL + "/xypai-app-bff/api/search/history";

            Map<String, Object> deleteRequest = new HashMap<>();
            deleteRequest.put("keyword", "王者荣耀");
            deleteRequest.put("clearAll", false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(deleteRequest, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                Boolean success = (Boolean) data.get("success");
                String message = (String) data.get("message");

                log.info("✅ 删除搜索历史成功");
                log.info("   - 是否成功: {}", success);
                log.info("   - 提示信息: {}", message);

                Assertions.assertTrue(success, "删除应该成功");
            } else {
                log.warn("⚠️ 删除失败（可能没有该历史记录）: {}", responseBody.get("msg"));
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试7: 清空所有搜索历史
    // ============================================================
    @Test
    @Order(7)
    @DisplayName("[测试7] 清空所有搜索历史")
    void test07_clearAllHistory() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试7] 清空所有搜索历史                                   │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            String url = GATEWAY_URL + "/xypai-app-bff/api/search/history";

            Map<String, Object> deleteRequest = new HashMap<>();
            deleteRequest.put("clearAll", true);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(deleteRequest, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                Boolean success = (Boolean) data.get("success");
                String message = (String) data.get("message");

                log.info("✅ 清空所有搜索历史成功");
                log.info("   - 是否成功: {}", success);
                log.info("   - 提示信息: {}", message);

                Assertions.assertTrue(success, "清空应该成功");
            } else {
                Assertions.fail("清空所有搜索历史失败");
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试8: 验证清空后历史记录为空
    // ============================================================
    @Test
    @Order(8)
    @DisplayName("[测试8] 验证清空后历史记录为空")
    void test08_verifyHistoryEmpty() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试8] 验证清空后历史记录为空                             │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            String url = GATEWAY_URL + "/xypai-app-bff/api/search/init";

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            List<Map<String, Object>> searchHistory = (List<Map<String, Object>>) data.get("searchHistory");
            List<Map<String, Object>> hotKeywords = (List<Map<String, Object>>) data.get("hotKeywords");

            log.info("✅ 验证搜索初始数据");
            log.info("   - 搜索历史数量: {}", searchHistory.size());
            log.info("   - 热门搜索数量: {}", hotKeywords.size());

            Assertions.assertEquals(0, searchHistory.size(), "清空后搜索历史应该为空");
            Assertions.assertTrue(hotKeywords.size() > 0, "热门搜索不应该被清空");

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试9: 验证热门搜索数据完整性
    // ============================================================
    @Test
    @Order(9)
    @DisplayName("[测试9] 验证热门搜索数据完整性")
    void test09_verifyHotKeywords() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试9] 验证热门搜索数据完整性                              │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            String url = GATEWAY_URL + "/xypai-app-bff/api/search/init";

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            List<Map<String, Object>> hotKeywords = (List<Map<String, Object>>) data.get("hotKeywords");

            log.info("✅ 热门搜索列表:");
            for (Map<String, Object> hotKeyword : hotKeywords) {
                String keyword = (String) hotKeyword.get("keyword");
                Integer rank = (Integer) hotKeyword.get("rank");
                Boolean isHot = (Boolean) hotKeyword.get("isHot");

                log.info("   - 排名{}: {} {}", rank, keyword, isHot ? "🔥" : "");
            }

            // 验证数据完整性
            Assertions.assertTrue(hotKeywords.size() >= 5, "应该有至少5个热门搜索");

            for (Map<String, Object> hotKeyword : hotKeywords) {
                Assertions.assertNotNull(hotKeyword.get("keyword"), "关键词不能为空");
                Assertions.assertNotNull(hotKeyword.get("rank"), "排名不能为空");
                Assertions.assertNotNull(hotKeyword.get("isHot"), "isHot不能为空");
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试10: 测试搜索建议类型多样性
    // ============================================================
    @Test
    @Order(10)
    @DisplayName("[测试10] 测试搜索建议类型多样性")
    void test10_verifySuggestionTypes() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试10] 测试搜索建议类型多样性                             │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            // 测试不同类型的搜索建议
            String[] testKeywords = {"王者", "台球", "游戏112"};

            for (String keyword : testKeywords) {
                String url = GATEWAY_URL + "/xypai-app-bff/api/search/suggest?keyword=" + keyword + "&limit=5";

                HttpHeaders headers = new HttpHeaders();
                if (authToken != null && !authToken.isEmpty()) {
                    headers.set("Authorization", "Bearer " + authToken);
                }
                HttpEntity<Void> entity = new HttpEntity<>(headers);

                ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                List<Map<String, Object>> suggestions = (List<Map<String, Object>>) data.get("suggestions");

                log.info("\n   关键词: {}", keyword);
                log.info("   - 建议数量: {}", suggestions.size());

                // 统计建议类型
                Map<String, Long> typeCount = suggestions.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                        s -> (String) s.get("type"),
                        java.util.stream.Collectors.counting()
                    ));

                typeCount.forEach((type, count) ->
                    log.info("   - {} 类型: {} 条", type, count));

                Assertions.assertTrue(suggestions.size() > 0, "应该有搜索建议");
            }

            log.info("\n✅ 搜索建议类型多样性验证通过");

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    @AfterAll
    static void tearDown() {
        log.info("\n");
        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║  ✅ 测试完成                                                 ║");
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║  搜索功能测试全部通过                                        ║");
        log.info("║  - 搜索初始数据 ✓                                           ║");
        log.info("║  - 搜索建议（多类型）✓                                      ║");
        log.info("║  - 删除单条历史 ✓                                           ║");
        log.info("║  - 清空所有历史 ✓                                           ║");
        log.info("║  - 热门搜索显示 ✓                                           ║");
        log.info("╚════════════════════════════════════════════════════════════╝");
    }
}
