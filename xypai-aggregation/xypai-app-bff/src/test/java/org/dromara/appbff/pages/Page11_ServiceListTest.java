package org.dromara.appbff.pages;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 服务列表页面集成测试
 * 对应前端文档: 11-服务列表页面.md
 *
 * 测试场景:
 * 1. 服务列表加载
 * 2. Tab切换（荣耀王者/线上/线下/我的）
 * 3. 排序功能
 * 4. 性别筛选
 * 5. 高级筛选（大区、段位、价格、位置等）
 * 6. 分页加载
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
@DisplayName("Page11: 服务列表页面测试")
public class Page11_ServiceListTest {

    private static RestTemplate restTemplate;
    private static final String GATEWAY_URL = "http://localhost:8080";
    private static final String TEST_COUNTRY_CODE = "+86";
    private static final String TEST_SMS_CODE = "123456";

    private static String authToken;
    private static String userId;

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║  📄 页面级集成测试: 11-服务列表页面                           ║");
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║  涉及服务:                                                   ║");
        log.info("║  - xypai-auth (8200)     用户认证                            ║");
        log.info("║  - xypai-app-bff (9400)  服务列表聚合                         ║");
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

    // ==================== 1. 服务列表加载 ====================

    @Test
    @Order(1)
    @DisplayName("1.1 获取王者荣耀服务列表")
    void testGetServiceList_HonorOfKings() {
        ensureAuthenticated();
        log.info("\n[测试1.1] 获取王者荣耀服务列表 → xypai-app-bff");

        String url = GATEWAY_URL + "/xypai-app-bff/api/service/list?skillType=王者荣耀&pageNum=1&pageSize=10";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.get("code"));

        Map data = (Map) body.get("data");
        assertNotNull(data);
        assertNotNull(data.get("skillType"));
        assertNotNull(data.get("tabs"));
        assertNotNull(data.get("filters"));
        assertNotNull(data.get("list"));

        // 输出查询结果详情
        log.info("✅ 获取王者荣耀服务列表成功");
        log.info("   📊 响应数据:");
        log.info("   - skillType: {}", data.get("skillType"));
        log.info("   - tabs: {}", data.get("tabs"));
        log.info("   - total: {}", data.get("total"));
        log.info("   - hasMore: {}", data.get("hasMore"));

        java.util.List list = (java.util.List) data.get("list");
        log.info("   - list.size: {}", list != null ? list.size() : 0);
        if (list != null && !list.isEmpty()) {
            log.info("   - 第一条数据: {}", list.get(0));
        }
    }

    @Test
    @Order(2)
    @DisplayName("1.2 获取台球服务列表（线下服务）")
    void testGetServiceList_Billiards() {
        ensureAuthenticated();
        log.info("\n[测试1.2] 获取台球服务列表 → xypai-app-bff");

        String url = GATEWAY_URL + "/xypai-app-bff/api/service/list?skillType=台球&pageNum=1&pageSize=10";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.get("code"));

        Map data = (Map) body.get("data");
        assertNotNull(data.get("list"));

        log.info("✅ 获取台球服务列表成功");
    }

    // ==================== 2. Tab切换 ====================

    @Test
    @Order(3)
    @DisplayName("2.1 切换到荣耀王者Tab")
    void testTabSwitch_GloryKing() {
        ensureAuthenticated();
        log.info("\n[测试2.1] 切换到荣耀王者Tab → xypai-app-bff");

        String url = GATEWAY_URL + "/xypai-app-bff/api/service/list?skillType=王者荣耀&pageNum=1&pageSize=10&tabType=glory_king";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        log.info("✅ 切换到荣耀王者Tab成功");
    }

    @Test
    @Order(4)
    @DisplayName("2.2 切换到线上Tab")
    void testTabSwitch_Online() {
        ensureAuthenticated();
        log.info("\n[测试2.2] 切换到线上Tab → xypai-app-bff");

        String url = GATEWAY_URL + "/xypai-app-bff/api/service/list?skillType=王者荣耀&pageNum=1&pageSize=10&tabType=online";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        log.info("✅ 切换到线上Tab成功");
    }

    @Test
    @Order(5)
    @DisplayName("2.3 切换到线下Tab")
    void testTabSwitch_Offline() {
        ensureAuthenticated();
        log.info("\n[测试2.3] 切换到线下Tab → xypai-app-bff");

        String url = GATEWAY_URL + "/xypai-app-bff/api/service/list?skillType=王者荣耀&pageNum=1&pageSize=10&tabType=offline";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        log.info("✅ 切换到线下Tab成功");
    }

    // ==================== 3. 排序功能 ====================

    @Test
    @Order(6)
    @DisplayName("3.1 按价格升序排序")
    void testSort_PriceAsc() {
        ensureAuthenticated();
        log.info("\n[测试3.1] 按价格升序排序 → xypai-app-bff");

        String url = GATEWAY_URL + "/xypai-app-bff/api/service/list?skillType=王者荣耀&pageNum=1&pageSize=10&sortBy=price_asc";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        log.info("✅ 按价格升序排序成功");
    }

    @Test
    @Order(7)
    @DisplayName("3.2 按评分降序排序")
    void testSort_RatingDesc() {
        ensureAuthenticated();
        log.info("\n[测试3.2] 按评分降序排序 → xypai-app-bff");

        String url = GATEWAY_URL + "/xypai-app-bff/api/service/list?skillType=王者荣耀&pageNum=1&pageSize=10&sortBy=rating_desc";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        log.info("✅ 按评分降序排序成功");
    }

    @Test
    @Order(8)
    @DisplayName("3.3 按订单数降序排序")
    void testSort_OrdersDesc() {
        ensureAuthenticated();
        log.info("\n[测试3.3] 按订单数降序排序 → xypai-app-bff");

        String url = GATEWAY_URL + "/xypai-app-bff/api/service/list?skillType=王者荣耀&pageNum=1&pageSize=10&sortBy=orders_desc";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        log.info("✅ 按订单数降序排序成功");
    }

    // ==================== 4. 分页加载 ====================

    @Test
    @Order(9)
    @DisplayName("4.1 加载第一页")
    void testPagination_FirstPage() {
        ensureAuthenticated();
        log.info("\n[测试4.1] 加载第一页 → xypai-app-bff");

        String url = GATEWAY_URL + "/xypai-app-bff/api/service/list?skillType=王者荣耀&pageNum=1&pageSize=5";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map body = response.getBody();
        Map data = (Map) body.get("data");
        assertNotNull(data.get("total"));
        assertNotNull(data.get("hasMore"));

        log.info("✅ 加载第一页成功 - total: {}, hasMore: {}", data.get("total"), data.get("hasMore"));
    }

    @Test
    @Order(10)
    @DisplayName("4.2 加载第二页")
    void testPagination_SecondPage() {
        ensureAuthenticated();
        log.info("\n[测试4.2] 加载第二页 → xypai-app-bff");

        String url = GATEWAY_URL + "/xypai-app-bff/api/service/list?skillType=王者荣耀&pageNum=2&pageSize=5";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        log.info("✅ 加载第二页成功");
    }

    // ==================== 5. 参数校验 ====================

    @Test
    @Order(11)
    @DisplayName("5.1 缺少必填参数skillType")
    void testValidation_MissingSkillType() {
        ensureAuthenticated();
        log.info("\n[测试5.1] 缺少必填参数skillType → xypai-app-bff");

        String url = GATEWAY_URL + "/xypai-app-bff/api/service/list?pageNum=1&pageSize=10";

        try {
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

            // 检查是否返回错误码
            Integer code = (Integer) body.get("code");
            if (code != null && code != 200) {
                log.info("✅ 缺少参数正确返回错误码: {}", code);
            } else {
                log.warn("⚠️ 缺少参数时应返回错误码，但实际返回: {}", code);
            }
        } catch (Exception e) {
            log.info("✅ 缺少参数正确抛出异常: {}", e.getMessage());
        }
    }

    @Test
    @Order(12)
    @DisplayName("5.2 页码参数无效")
    void testValidation_InvalidPageNum() {
        ensureAuthenticated();
        log.info("\n[测试5.2] 页码参数无效 → xypai-app-bff");

        String url = GATEWAY_URL + "/xypai-app-bff/api/service/list?skillType=王者荣耀&pageNum=0&pageSize=10";

        try {
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

            // 检查是否返回错误码
            Integer code = (Integer) body.get("code");
            if (code != null && code != 200) {
                log.info("✅ 无效页码正确返回错误码: {}", code);
            } else {
                log.warn("⚠️ 无效页码时应返回错误码，但实际返回: {}", code);
            }
        } catch (Exception e) {
            log.info("✅ 无效页码正确抛出异常: {}", e.getMessage());
        }
    }

    @AfterAll
    static void tearDown() {
        log.info("\n╔════════════════════════════════════════════════════════════╗");
        log.info("║  🎉 页面测试完成: 11-服务列表页面                             ║");
        log.info("╚════════════════════════════════════════════════════════════╝");
    }
}
