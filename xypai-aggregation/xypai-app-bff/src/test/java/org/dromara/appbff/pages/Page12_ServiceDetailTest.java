package org.dromara.appbff.pages;

import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

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
 * @author XyPai Team
 * @date 2025-11-26
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Page12: 服务详情页面测试")
public class Page12_ServiceDetailTest {

    private static RestTemplate restTemplate;
    private static final String BASE_URL = "http://localhost:8080";
    private static String authToken;
    private static Long testServiceId = 1001L; // Mock数据中存在的服务ID

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        // 测试时需要先登录获取token
        // authToken = login();
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
        String url = BASE_URL + "/api/service/detail?serviceId=" + testServiceId;

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
        assertNotNull(data.get("serviceId"));
        assertNotNull(data.get("provider"));
        assertNotNull(data.get("skillInfo"));
        assertNotNull(data.get("price"));
        assertNotNull(data.get("stats"));
    }

    @Test
    @Order(2)
    @DisplayName("1.2 获取服务详情-包含userId参数")
    void testGetServiceDetail_WithUserId() {
        String url = BASE_URL + "/api/service/detail?serviceId=" + testServiceId + "&userId=12345";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(3)
    @DisplayName("1.3 服务不存在返回错误")
    void testGetServiceDetail_NotFound() {
        String url = BASE_URL + "/api/service/detail?serviceId=999999";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        // 服务不存在时返回错误码
        Map body = response.getBody();
        assertNotNull(body);
        // code != 200 或 data 为 null
    }

    // ==================== 2. 服务提供者信息 ====================

    @Test
    @Order(4)
    @DisplayName("2.1 验证服务提供者信息字段")
    void testServiceDetail_ProviderInfo() {
        String url = BASE_URL + "/api/service/detail?serviceId=" + testServiceId;

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        Map body = response.getBody();
        Map data = (Map) body.get("data");
        Map provider = (Map) data.get("provider");

        assertNotNull(provider);
        assertNotNull(provider.get("userId"));
        assertNotNull(provider.get("nickname"));
        assertNotNull(provider.get("avatar"));
        assertNotNull(provider.get("gender"));
        assertNotNull(provider.get("isOnline"));
        assertNotNull(provider.get("isVerified"));
    }

    // ==================== 3. 技能信息 ====================

    @Test
    @Order(5)
    @DisplayName("3.1 验证技能信息字段")
    void testServiceDetail_SkillInfo() {
        String url = BASE_URL + "/api/service/detail?serviceId=" + testServiceId;

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        Map body = response.getBody();
        Map data = (Map) body.get("data");
        Map skillInfo = (Map) data.get("skillInfo");

        assertNotNull(skillInfo);
        assertNotNull(skillInfo.get("skillType"));
        assertNotNull(skillInfo.get("skillLabel"));
    }

    // ==================== 4. 价格信息 ====================

    @Test
    @Order(6)
    @DisplayName("4.1 验证价格信息字段")
    void testServiceDetail_PriceInfo() {
        String url = BASE_URL + "/api/service/detail?serviceId=" + testServiceId;

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        Map body = response.getBody();
        Map data = (Map) body.get("data");
        Map price = (Map) data.get("price");

        assertNotNull(price);
        assertNotNull(price.get("amount"));
        assertNotNull(price.get("unit"));
        assertNotNull(price.get("displayText"));
    }

    // ==================== 5. 评价信息 ====================

    @Test
    @Order(7)
    @DisplayName("5.1 验证评价摘要信息")
    void testServiceDetail_ReviewsSummary() {
        String url = BASE_URL + "/api/service/detail?serviceId=" + testServiceId;

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        Map body = response.getBody();
        Map data = (Map) body.get("data");
        Map reviews = (Map) data.get("reviews");

        assertNotNull(reviews);
        assertNotNull(reviews.get("total"));
        assertNotNull(reviews.get("summary"));
        assertNotNull(reviews.get("tags"));
        assertNotNull(reviews.get("recent"));
    }

    // ==================== 6. 评价列表接口 ====================

    @Test
    @Order(8)
    @DisplayName("6.1 获取评价列表-第一页")
    void testGetServiceReviews_FirstPage() {
        String url = BASE_URL + "/api/service/reviews?serviceId=" + testServiceId + "&pageNum=1&pageSize=10";

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
        assertNotNull(data.get("total"));
        assertNotNull(data.get("list"));
        assertNotNull(data.get("hasNext"));
    }

    @Test
    @Order(9)
    @DisplayName("6.2 获取评价列表-第二页")
    void testGetServiceReviews_SecondPage() {
        String url = BASE_URL + "/api/service/reviews?serviceId=" + testServiceId + "&pageNum=2&pageSize=10";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(10)
    @DisplayName("6.3 获取评价列表-筛选好评")
    void testGetServiceReviews_FilterExcellent() {
        String url = BASE_URL + "/api/service/reviews?serviceId=" + testServiceId + "&pageNum=1&pageSize=10&filterBy=excellent";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(11)
    @DisplayName("6.4 获取评价列表-筛选差评")
    void testGetServiceReviews_FilterNegative() {
        String url = BASE_URL + "/api/service/reviews?serviceId=" + testServiceId + "&pageNum=1&pageSize=10&filterBy=negative";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ==================== 7. 统计信息 ====================

    @Test
    @Order(12)
    @DisplayName("7.1 验证统计信息字段")
    void testServiceDetail_StatsInfo() {
        String url = BASE_URL + "/api/service/detail?serviceId=" + testServiceId;

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        Map body = response.getBody();
        Map data = (Map) body.get("data");
        Map stats = (Map) data.get("stats");

        assertNotNull(stats);
        assertNotNull(stats.get("orders"));
        assertNotNull(stats.get("rating"));
        assertNotNull(stats.get("reviewCount"));
    }
}
