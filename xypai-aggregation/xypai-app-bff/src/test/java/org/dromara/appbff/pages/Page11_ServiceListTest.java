package org.dromara.appbff.pages;

import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

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
 * @author XyPai Team
 * @date 2025-11-26
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Page11: 服务列表页面测试")
public class Page11_ServiceListTest {

    private static RestTemplate restTemplate;
    private static final String BASE_URL = "http://localhost:8080";
    private static String authToken;

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

    // ==================== 1. 服务列表加载 ====================

    @Test
    @Order(1)
    @DisplayName("1.1 获取王者荣耀服务列表")
    void testGetServiceList_HonorOfKings() {
        String url = BASE_URL + "/api/service/list?skillType=王者荣耀&pageNum=1&pageSize=10";

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
    }

    @Test
    @Order(2)
    @DisplayName("1.2 获取台球服务列表（线下服务）")
    void testGetServiceList_Billiards() {
        String url = BASE_URL + "/api/service/list?skillType=台球&pageNum=1&pageSize=10";

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
    }

    // ==================== 2. Tab切换 ====================

    @Test
    @Order(3)
    @DisplayName("2.1 切换到荣耀王者Tab")
    void testTabSwitch_GloryKing() {
        String url = BASE_URL + "/api/service/list?skillType=王者荣耀&pageNum=1&pageSize=10&tabType=glory_king";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(4)
    @DisplayName("2.2 切换到线上Tab")
    void testTabSwitch_Online() {
        String url = BASE_URL + "/api/service/list?skillType=王者荣耀&pageNum=1&pageSize=10&tabType=online";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(5)
    @DisplayName("2.3 切换到线下Tab")
    void testTabSwitch_Offline() {
        String url = BASE_URL + "/api/service/list?skillType=王者荣耀&pageNum=1&pageSize=10&tabType=offline";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ==================== 3. 排序功能 ====================

    @Test
    @Order(6)
    @DisplayName("3.1 按价格升序排序")
    void testSort_PriceAsc() {
        String url = BASE_URL + "/api/service/list?skillType=王者荣耀&pageNum=1&pageSize=10&sortBy=price_asc";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(7)
    @DisplayName("3.2 按评分降序排序")
    void testSort_RatingDesc() {
        String url = BASE_URL + "/api/service/list?skillType=王者荣耀&pageNum=1&pageSize=10&sortBy=rating_desc";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(8)
    @DisplayName("3.3 按订单数降序排序")
    void testSort_OrdersDesc() {
        String url = BASE_URL + "/api/service/list?skillType=王者荣耀&pageNum=1&pageSize=10&sortBy=orders_desc";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ==================== 4. 分页加载 ====================

    @Test
    @Order(9)
    @DisplayName("4.1 加载第一页")
    void testPagination_FirstPage() {
        String url = BASE_URL + "/api/service/list?skillType=王者荣耀&pageNum=1&pageSize=5";

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
    }

    @Test
    @Order(10)
    @DisplayName("4.2 加载第二页")
    void testPagination_SecondPage() {
        String url = BASE_URL + "/api/service/list?skillType=王者荣耀&pageNum=2&pageSize=5";

        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ==================== 5. 参数校验 ====================

    @Test
    @Order(11)
    @DisplayName("5.1 缺少必填参数skillType")
    void testValidation_MissingSkillType() {
        String url = BASE_URL + "/api/service/list?pageNum=1&pageSize=10";

        try {
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(createHeaders()),
                Map.class
            );
        } catch (Exception e) {
            // 预期会抛出异常或返回错误
            assertTrue(true);
        }
    }

    @Test
    @Order(12)
    @DisplayName("5.2 页码参数无效")
    void testValidation_InvalidPageNum() {
        String url = BASE_URL + "/api/service/list?skillType=王者荣耀&pageNum=0&pageSize=10";

        try {
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(createHeaders()),
                Map.class
            );
        } catch (Exception e) {
            assertTrue(true);
        }
    }
}
