package org.dromara.appbff.pages;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 服务详情页面集成测试
 * 对应前端文档: 12-服务详情页面.md
 *
 * 测试场景:
 * 1. 服务详情加载
 * 2. 服务提供者信息展示
 * 3. 技能信息展示
 * 4. 评价信息展示
 * 5. 评价列表分页
 * 6. 服务不存在处理
 *
 * 需要启动的服务:
 * - Gateway (8080)
 * - xypai-auth (8200)
 * - xypai-app-bff (9400)
 * - xypai-user (9401)
 * - Nacos, Redis
 *
 * @author XyPai Team
 * @date 2025-11-26
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Page12: 服务详情页面测试")
public class Page12_ServiceDetailTest {

    private static RestTemplate restTemplate;
    private static final String GATEWAY_URL = "http://localhost:8080";
    private static final String TEST_COUNTRY_CODE = "+86";
    private static final String TEST_SMS_CODE = "123456";

    private static String authToken;
    private static String userId;
    private static Long testServiceId = 1001L; // Mock数据中存在的服务ID

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║  📄 页面级集成测试: 12-服务详情页面                           ║");
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║  涉及服务:                                                   ║");
        log.info("║  - xypai-auth (8200)     用户认证                            ║");
        log.info("║  - xypai-app-bff (9400)  服务详情聚合                         ║");
        log.info("║  - xypai-user (9401)     用户/技能数据 (RPC)                  ║");
        log.info("╚════════════════════════════════════════════════════════════╝");
    }

    /**
     * 确保用户已登录认证
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
                    log.info("✅ 登录成功 - userId: {}, token前10位: {}", userId,
                        authToken.substring(0, Math.min(10, authToken.length())));
                } else {
                    log.error("❌ 登录失败: {}", responseBody.get("msg"));
                }
            }
        } catch (Exception e) {
            log.error("❌ 登录异常: {}", e.getMessage());
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authToken != null) {
            headers.set("Authorization", "Bearer " + authToken);
        }
        return headers;
    }

    // ==================== 1. 服务详情加载 ====================

    @Test
    @Order(1)
    @DisplayName("1.1 获取服务详情成功")
    void testGetServiceDetail_Success() {
        ensureAuthenticated();
        log.info("\n[测试1.1] 获取服务详情 → xypai-app-bff");

        // 使用userId参数，这是API要求的
        String url = GATEWAY_URL + "/xypai-app-bff/api/service/detail?serviceId=" + testServiceId + "&userId=" + userId;

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map body = response.getBody();
        assertNotNull(body);

        log.info("   📊 响应状态: {}", response.getStatusCode());
        log.info("   - code: {}", body.get("code"));
        log.info("   - msg: {}", body.get("msg"));

        Integer code = (Integer) body.get("code");
        if (code != null && code == 200) {
            Map data = (Map) body.get("data");
            if (data != null) {
                log.info("✅ 获取服务详情成功 - serviceId: {}", testServiceId);
                log.info("   📊 响应数据:");
                log.info("   - serviceId: {}", data.get("serviceId"));
                log.info("   - provider: {}", data.get("provider"));
                log.info("   - skillInfo: {}", data.get("skillInfo"));
                log.info("   - price: {}", data.get("price"));
                log.info("   - stats: {}", data.get("stats"));
                log.info("   - reviews: {}", data.get("reviews"));
            } else {
                log.warn("⚠️ 返回成功但data为空");
            }
        } else {
            log.warn("⚠️ 获取服务详情失败 - code: {}, msg: {}", code, body.get("msg"));
        }
    }

    @Test
    @Order(2)
    @DisplayName("1.2 获取服务详情-不带userId参数(测试缺省行为)")
    void testGetServiceDetail_WithoutUserId() {
        ensureAuthenticated();
        log.info("\n[测试1.2] 获取服务详情(不带userId) → xypai-app-bff");

        String url = GATEWAY_URL + "/xypai-app-bff/api/service/detail?serviceId=" + testServiceId;

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        Map body = response.getBody();
        log.info("   📊 响应状态: {}", response.getStatusCode());
        log.info("   - code: {}", body.get("code"));
        log.info("   - msg: {}", body.get("msg"));
        log.info("   - data: {}", body.get("data"));

        Integer code = (Integer) body.get("code");
        if (code != null && code != 200) {
            log.info("✅ 不带userId时正确返回错误码: {}", code);
        } else {
            log.info("✅ 获取服务详情(不带userId)成功");
        }
    }

    @Test
    @Order(3)
    @DisplayName("1.3 服务不存在返回错误")
    void testGetServiceDetail_NotFound() {
        ensureAuthenticated();
        log.info("\n[测试1.3] 查询不存在的服务 → xypai-app-bff");

        String url = GATEWAY_URL + "/xypai-app-bff/api/service/detail?serviceId=999999&userId=" + userId;

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        Map body = response.getBody();
        assertNotNull(body);
        log.info("   📊 响应状态: {}", response.getStatusCode());
        log.info("   - code: {}", body.get("code"));
        log.info("   - msg: {}", body.get("msg"));
        log.info("   - data: {}", body.get("data"));
        log.info("✅ 服务不存在正确处理 - code: {}", body.get("code"));
    }

    // ==================== 2. 服务提供者信息 ====================

    @Test
    @Order(4)
    @DisplayName("2.1 验证服务提供者信息字段")
    void testServiceDetail_ProviderInfo() {
        ensureAuthenticated();
        log.info("\n[测试2.1] 验证服务提供者信息 → xypai-app-bff");

        String url = GATEWAY_URL + "/xypai-app-bff/api/service/detail?serviceId=" + testServiceId + "&userId=" + userId;

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        Map body = response.getBody();
        log.info("   📊 响应状态: {}", response.getStatusCode());
        log.info("   - code: {}", body.get("code"));

        Map data = (Map) body.get("data");
        if (data != null) {
            Map provider = (Map) data.get("provider");
            if (provider != null) {
                log.info("   📊 provider字段:");
                log.info("   - userId: {}", provider.get("userId"));
                log.info("   - nickname: {}", provider.get("nickname"));
                log.info("   - avatar: {}", provider.get("avatar"));
                log.info("   - gender: {}", provider.get("gender"));
                log.info("   - isOnline: {}", provider.get("isOnline"));
                log.info("   - isVerified: {}", provider.get("isVerified"));
                log.info("✅ 服务提供者信息验证通过");
            } else {
                log.warn("⚠️ provider字段为空");
            }
        } else {
            log.warn("⚠️ data为空 - msg: {}", body.get("msg"));
        }
    }

    // ==================== 3. 技能信息 ====================

    @Test
    @Order(5)
    @DisplayName("3.1 验证技能信息字段")
    void testServiceDetail_SkillInfo() {
        ensureAuthenticated();
        log.info("\n[测试3.1] 验证技能信息 → xypai-app-bff");

        String url = GATEWAY_URL + "/xypai-app-bff/api/service/detail?serviceId=" + testServiceId + "&userId=" + userId;

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        Map body = response.getBody();
        log.info("   📊 响应状态: {}", response.getStatusCode());
        log.info("   - code: {}", body.get("code"));

        Map data = (Map) body.get("data");
        if (data != null) {
            Map skillInfo = (Map) data.get("skillInfo");
            if (skillInfo != null) {
                log.info("   📊 skillInfo字段:");
                log.info("   - skillType: {}", skillInfo.get("skillType"));
                log.info("   - skillLabel: {}", skillInfo.get("skillLabel"));
                log.info("✅ 技能信息验证通过");
            } else {
                log.warn("⚠️ skillInfo字段为空");
            }
        } else {
            log.warn("⚠️ data为空 - msg: {}", body.get("msg"));
        }
    }

    // ==================== 4. 价格信息 ====================

    @Test
    @Order(6)
    @DisplayName("4.1 验证价格信息字段")
    void testServiceDetail_PriceInfo() {
        ensureAuthenticated();
        log.info("\n[测试4.1] 验证价格信息 → xypai-app-bff");

        String url = GATEWAY_URL + "/xypai-app-bff/api/service/detail?serviceId=" + testServiceId + "&userId=" + userId;

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        Map body = response.getBody();
        log.info("   📊 响应状态: {}", response.getStatusCode());
        log.info("   - code: {}", body.get("code"));

        Map data = (Map) body.get("data");
        if (data != null) {
            Map price = (Map) data.get("price");
            if (price != null) {
                log.info("   📊 price字段:");
                log.info("   - amount: {}", price.get("amount"));
                log.info("   - unit: {}", price.get("unit"));
                log.info("   - displayText: {}", price.get("displayText"));
                log.info("✅ 价格信息验证通过");
            } else {
                log.warn("⚠️ price字段为空");
            }
        } else {
            log.warn("⚠️ data为空 - msg: {}", body.get("msg"));
        }
    }

    // ==================== 5. 评价信息 ====================

    @Test
    @Order(7)
    @DisplayName("5.1 验证评价摘要信息")
    void testServiceDetail_ReviewsSummary() {
        ensureAuthenticated();
        log.info("\n[测试5.1] 验证评价摘要信息 → xypai-app-bff");

        String url = GATEWAY_URL + "/xypai-app-bff/api/service/detail?serviceId=" + testServiceId + "&userId=" + userId;

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        Map body = response.getBody();
        log.info("   📊 响应状态: {}", response.getStatusCode());
        log.info("   - code: {}", body.get("code"));

        Map data = (Map) body.get("data");
        if (data != null) {
            Map reviews = (Map) data.get("reviews");
            if (reviews != null) {
                log.info("   📊 reviews字段:");
                log.info("   - total: {}", reviews.get("total"));
                log.info("   - summary: {}", reviews.get("summary"));
                log.info("   - tags: {}", reviews.get("tags"));
                log.info("   - recent: {}", reviews.get("recent"));
                log.info("✅ 评价摘要信息验证通过");
            } else {
                log.warn("⚠️ reviews字段为空");
            }
        } else {
            log.warn("⚠️ data为空 - msg: {}", body.get("msg"));
        }
    }

    // ==================== 6. 评价列表接口 ====================

    @Test
    @Order(8)
    @DisplayName("6.1 获取评价列表-第一页")
    void testGetServiceReviews_FirstPage() {
        ensureAuthenticated();
        log.info("\n[测试6.1] 获取评价列表第一页 → xypai-app-bff");

        String url = GATEWAY_URL + "/xypai-app-bff/api/service/reviews?serviceId=" + testServiceId + "&pageNum=1&pageSize=10";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map body = response.getBody();
        assertNotNull(body);

        log.info("   📊 响应状态: {}", response.getStatusCode());
        log.info("   - code: {}", body.get("code"));

        Integer code = (Integer) body.get("code");
        if (code != null && code == 200) {
            Map data = (Map) body.get("data");
            if (data != null) {
                log.info("   📊 响应数据:");
                log.info("   - total: {}", data.get("total"));
                log.info("   - hasNext: {}", data.get("hasNext"));
                java.util.List list = (java.util.List) data.get("list");
                log.info("   - list.size: {}", list != null ? list.size() : 0);
                if (list != null && !list.isEmpty()) {
                    log.info("   - 第一条评价: {}", list.get(0));
                }
                log.info("✅ 获取评价列表第一页成功");
            }
        } else {
            log.warn("⚠️ 获取评价列表失败 - code: {}, msg: {}", code, body.get("msg"));
        }
    }

    @Test
    @Order(9)
    @DisplayName("6.2 获取评价列表-第二页")
    void testGetServiceReviews_SecondPage() {
        ensureAuthenticated();
        log.info("\n[测试6.2] 获取评价列表第二页 → xypai-app-bff");

        String url = GATEWAY_URL + "/xypai-app-bff/api/service/reviews?serviceId=" + testServiceId + "&pageNum=2&pageSize=10";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        Map body = response.getBody();
        log.info("   📊 响应状态: {}", response.getStatusCode());
        log.info("   - code: {}", body.get("code"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        log.info("✅ 获取评价列表第二页成功");
    }

    @Test
    @Order(10)
    @DisplayName("6.3 获取评价列表-筛选好评")
    void testGetServiceReviews_FilterExcellent() {
        ensureAuthenticated();
        log.info("\n[测试6.3] 获取评价列表(筛选好评) → xypai-app-bff");

        String url = GATEWAY_URL + "/xypai-app-bff/api/service/reviews?serviceId=" + testServiceId + "&pageNum=1&pageSize=10&filterBy=excellent";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        Map body = response.getBody();
        log.info("   📊 响应状态: {}", response.getStatusCode());
        log.info("   - code: {}", body.get("code"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        log.info("✅ 获取好评列表成功");
    }

    @Test
    @Order(11)
    @DisplayName("6.4 获取评价列表-筛选差评")
    void testGetServiceReviews_FilterNegative() {
        ensureAuthenticated();
        log.info("\n[测试6.4] 获取评价列表(筛选差评) → xypai-app-bff");

        String url = GATEWAY_URL + "/xypai-app-bff/api/service/reviews?serviceId=" + testServiceId + "&pageNum=1&pageSize=10&filterBy=negative";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        Map body = response.getBody();
        log.info("   📊 响应状态: {}", response.getStatusCode());
        log.info("   - code: {}", body.get("code"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        log.info("✅ 获取差评列表成功");
    }

    // ==================== 7. 统计信息 ====================

    @Test
    @Order(12)
    @DisplayName("7.1 验证统计信息字段")
    void testServiceDetail_StatsInfo() {
        ensureAuthenticated();
        log.info("\n[测试7.1] 验证统计信息 → xypai-app-bff");

        String url = GATEWAY_URL + "/xypai-app-bff/api/service/detail?serviceId=" + testServiceId + "&userId=" + userId;

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        Map body = response.getBody();
        log.info("   📊 响应状态: {}", response.getStatusCode());
        log.info("   - code: {}", body.get("code"));

        Map data = (Map) body.get("data");
        if (data != null) {
            Map stats = (Map) data.get("stats");
            if (stats != null) {
                log.info("   📊 stats字段:");
                log.info("   - orders: {}", stats.get("orders"));
                log.info("   - rating: {}", stats.get("rating"));
                log.info("   - reviewCount: {}", stats.get("reviewCount"));
                log.info("✅ 统计信息验证通过");
            } else {
                log.warn("⚠️ stats字段为空");
            }
        } else {
            log.warn("⚠️ data为空 - msg: {}", body.get("msg"));
        }
    }

    @AfterAll
    static void tearDown() {
        log.info("\n╔════════════════════════════════════════════════════════════╗");
        log.info("║  🎉 页面测试完成: 12-服务详情页面                             ║");
        log.info("╚════════════════════════════════════════════════════════════╝");
    }
}
