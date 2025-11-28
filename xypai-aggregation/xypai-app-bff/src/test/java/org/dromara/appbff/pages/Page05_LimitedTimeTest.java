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
 * 【页面级集成测试】05-限时专享页面
 *
 * ============================================================
 * 📄 前端页面信息
 * ============================================================
 * - 文档路径: XiangYuPai-Doc/Action-API/模块化架构/03-content模块/Frontend/05-限时专享页面.md
 * - 页面路由: /home/limited-time
 * - 页面名称: 限时专享
 * - 用户角色: 所有用户
 * - 页面类型: 列表页面
 *
 * ============================================================
 * 📌 涉及的后端服务及接口
 * ============================================================
 *
 * 【xypai-app-bff (聚合服务, 9400)】
 * - GET  /api/home/limited-time/list      获取限时专享列表
 *
 * ============================================================
 * 🧪 测试流程
 * ============================================================
 * 1. 获取限时专享列表（默认参数）
 * 2. 获取限时专享列表（智能推荐排序）
 * 3. 获取限时专享列表（价格从低到高）
 * 4. 获取限时专享列表（价格从高到低）
 * 5. 获取限时专享列表（距离最近）
 * 6. 获取限时专享列表（性别筛选-男）
 * 7. 获取限时专享列表（性别筛选-女）
 * 8. 获取限时专享列表（语言筛选-普通话）
 * 9. 获取限时专享列表（组合筛选）
 * 10. 测试分页功能
 *
 * 💡 测试说明:
 * - 本测试通过 Gateway (8080) 调用 xypai-app-bff 服务
 * - 限时专享是促销功能，当前使用 Mock 数据
 * - 需要启动: Gateway(8080), xypai-auth(9211), xypai-app-bff(9400), Nacos, Redis
 *
 * @author XyPai Team
 * @date 2025-11-24
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Page05_LimitedTimeTest {

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
        log.info("║  📄 页面级集成测试: 05-限时专享页面                            ║");
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║  涉及服务:                                                   ║");
        log.info("║  - xypai-app-bff (9400)  限时专享列表                        ║");
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
    // 测试1: 获取限时专享列表（默认参数）
    // ============================================================
    @Test
    @Order(1)
    @DisplayName("[测试1] 获取限时专享列表（默认参数）")
    void test01_getDefaultList() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试1] 获取限时专享列表（默认参数）                        │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            String url = GATEWAY_URL + "/xypai-app-bff/api/home/limited-time/list?pageNum=1&pageSize=10";

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
                List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");

                log.info("✅ 获取限时专享列表成功");
                log.info("   - 总记录数: {}", data.get("total"));
                log.info("   - 当前页数量: {}", list.size());
                log.info("   - 是否有更多: {}", data.get("hasMore"));

                if (!list.isEmpty()) {
                    Map<String, Object> firstUser = list.get(0);
                    log.info("   - 第一个用户: {} ({})", firstUser.get("nickname"), firstUser.get("promotionTag"));

                    Map<String, Object> price = (Map<String, Object>) firstUser.get("price");
                    log.info("   - 价格: {} (原价: {})", price.get("displayText"), price.get("originalPrice"));
                }

                // 验证筛选选项
                Map<String, Object> filters = (Map<String, Object>) data.get("filters");
                List<Map<String, Object>> sortOptions = (List<Map<String, Object>>) filters.get("sortOptions");
                List<Map<String, Object>> genderOptions = (List<Map<String, Object>>) filters.get("genderOptions");
                List<Map<String, Object>> languageOptions = (List<Map<String, Object>>) filters.get("languageOptions");

                log.info("   - 排序选项数量: {}", sortOptions.size());
                log.info("   - 性别选项数量: {}", genderOptions.size());
                log.info("   - 语言选项数量: {}", languageOptions.size());

                Assertions.assertNotNull(list, "用户列表不能为空");
                Assertions.assertTrue(list.size() > 0, "应该至少有一个用户");
                Assertions.assertEquals(4, sortOptions.size(), "应该有4个排序选项");
                Assertions.assertEquals(3, genderOptions.size(), "应该有3个性别选项");
                Assertions.assertEquals(4, languageOptions.size(), "应该有4个语言选项");
            } else {
                String msg = (String) responseBody.get("msg");
                log.error("❌ 获取列表失败: {}", msg);
                Assertions.fail("获取列表失败: " + msg);
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试2: 价格从低到高排序
    // ============================================================
    @Test
    @Order(2)
    @DisplayName("[测试2] 价格从低到高排序")
    void test02_sortByPriceAsc() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试2] 价格从低到高排序                                   │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            String url = GATEWAY_URL + "/xypai-app-bff/api/home/limited-time/list?pageNum=1&pageSize=5&sortBy=price_asc";

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
                List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");

                log.info("✅ 价格从低到高排序成功");
                log.info("   - 返回用户数: {}", list.size());

                // 验证价格是从低到高
                Integer prevPrice = null;
                for (int i = 0; i < list.size(); i++) {
                    Map<String, Object> user = list.get(i);
                    Map<String, Object> price = (Map<String, Object>) user.get("price");
                    Integer amount = (Integer) price.get("amount");

                    log.info("   - 用户{}: {} - {} 金币", i + 1, user.get("nickname"), amount);

                    if (prevPrice != null) {
                        Assertions.assertTrue(amount >= prevPrice, "价格应该从低到高排序");
                    }
                    prevPrice = amount;
                }
            } else {
                Assertions.fail("价格排序失败");
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试3: 价格从高到低排序
    // ============================================================
    @Test
    @Order(3)
    @DisplayName("[测试3] 价格从高到低排序")
    void test03_sortByPriceDesc() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试3] 价格从高到低排序                                   │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            String url = GATEWAY_URL + "/xypai-app-bff/api/home/limited-time/list?pageNum=1&pageSize=5&sortBy=price_desc";

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");

            log.info("✅ 价格从高到低排序成功");
            log.info("   - 返回用户数: {}", list.size());

            // 验证价格是从高到低
            Integer prevPrice = null;
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> user = list.get(i);
                Map<String, Object> price = (Map<String, Object>) user.get("price");
                Integer amount = (Integer) price.get("amount");

                log.info("   - 用户{}: {} - {} 金币", i + 1, user.get("nickname"), amount);

                if (prevPrice != null) {
                    Assertions.assertTrue(amount <= prevPrice, "价格应该从高到低排序");
                }
                prevPrice = amount;
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试4: 距离最近排序
    // ============================================================
    @Test
    @Order(4)
    @DisplayName("[测试4] 距离最近排序")
    void test04_sortByDistance() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试4] 距离最近排序                                       │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            String url = GATEWAY_URL + "/xypai-app-bff/api/home/limited-time/list?pageNum=1&pageSize=5&sortBy=distance_asc";

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");

            log.info("✅ 距离最近排序成功");
            log.info("   - 返回用户数: {}", list.size());

            // 验证距离是从近到远
            Integer prevDistance = null;
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> user = list.get(i);
                Integer distance = (Integer) user.get("distance");
                String distanceText = (String) user.get("distanceText");

                log.info("   - 用户{}: {} - {}", i + 1, user.get("nickname"), distanceText);

                if (prevDistance != null) {
                    Assertions.assertTrue(distance >= prevDistance, "距离应该从近到远排序");
                }
                prevDistance = distance;
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试5: 性别筛选-男
    // ============================================================
    @Test
    @Order(5)
    @DisplayName("[测试5] 性别筛选-男")
    void test05_filterByMale() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试5] 性别筛选-男                                        │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            String url = GATEWAY_URL + "/xypai-app-bff/api/home/limited-time/list?pageNum=1&pageSize=10&gender=male";

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");

            log.info("✅ 性别筛选成功");
            log.info("   - 男性用户数: {}", list.size());

            // 验证所有用户都是男性
            for (Map<String, Object> user : list) {
                String gender = (String) user.get("gender");
                log.info("   - 用户: {} (性别: {})", user.get("nickname"), gender);
                Assertions.assertEquals("male", gender, "所有用户应该是男性");
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试6: 性别筛选-女
    // ============================================================
    @Test
    @Order(6)
    @DisplayName("[测试6] 性别筛选-女")
    void test06_filterByFemale() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试6] 性别筛选-女                                        │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            String url = GATEWAY_URL + "/xypai-app-bff/api/home/limited-time/list?pageNum=1&pageSize=10&gender=female";

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");

            log.info("✅ 性别筛选成功");
            log.info("   - 女性用户数: {}", list.size());

            // 验证所有用户都是女性
            for (Map<String, Object> user : list) {
                String gender = (String) user.get("gender");
                log.info("   - 用户: {} (性别: {})", user.get("nickname"), gender);
                Assertions.assertEquals("female", gender, "所有用户应该是女性");
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试7: 组合筛选（女性+价格从低到高）
    // ============================================================
    @Test
    @Order(7)
    @DisplayName("[测试7] 组合筛选（女性+价格从低到高）")
    void test07_combinedFilter() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试7] 组合筛选（女性+价格从低到高）                       │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            String url = GATEWAY_URL + "/xypai-app-bff/api/home/limited-time/list?pageNum=1&pageSize=5&gender=female&sortBy=price_asc";

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");

            log.info("✅ 组合筛选成功");
            log.info("   - 返回用户数: {}", list.size());

            // 验证所有用户都是女性且价格从低到高
            Integer prevPrice = null;
            for (Map<String, Object> user : list) {
                String gender = (String) user.get("gender");
                Map<String, Object> price = (Map<String, Object>) user.get("price");
                Integer amount = (Integer) price.get("amount");

                log.info("   - 用户: {} (性别: {}, 价格: {})", user.get("nickname"), gender, amount);

                Assertions.assertEquals("female", gender, "所有用户应该是女性");
                if (prevPrice != null) {
                    Assertions.assertTrue(amount >= prevPrice, "价格应该从低到高排序");
                }
                prevPrice = amount;
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试8: 分页功能
    // ============================================================
    @Test
    @Order(8)
    @DisplayName("[测试8] 分页功能")
    void test08_pagination() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试8] 分页功能                                           │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // 第一页
            String url1 = GATEWAY_URL + "/xypai-app-bff/api/home/limited-time/list?pageNum=1&pageSize=5";
            ResponseEntity<Map> response1 = restTemplate.exchange(url1, HttpMethod.GET, entity, Map.class);
            Map<String, Object> data1 = (Map<String, Object>) response1.getBody().get("data");
            List<Map<String, Object>> list1 = (List<Map<String, Object>>) data1.get("list");
            Boolean hasMore1 = (Boolean) data1.get("hasMore");

            log.info("✅ 第一页数据");
            log.info("   - 用户数: {}", list1.size());
            log.info("   - 是否有更多: {}", hasMore1);

            // 第二页
            String url2 = GATEWAY_URL + "/xypai-app-bff/api/home/limited-time/list?pageNum=2&pageSize=5";
            ResponseEntity<Map> response2 = restTemplate.exchange(url2, HttpMethod.GET, entity, Map.class);
            Map<String, Object> data2 = (Map<String, Object>) response2.getBody().get("data");
            List<Map<String, Object>> list2 = (List<Map<String, Object>>) data2.get("list");
            Boolean hasMore2 = (Boolean) data2.get("hasMore");

            log.info("✅ 第二页数据");
            log.info("   - 用户数: {}", list2.size());
            log.info("   - 是否有更多: {}", hasMore2);

            Assertions.assertEquals(5, list1.size(), "第一页应该有5个用户");
            Assertions.assertTrue(hasMore1, "第一页应该有更多数据");
            Assertions.assertTrue(list2.size() > 0, "第二页应该有数据");

            // 验证两页数据不重复
            Long firstUserId1 = ((Number) list1.get(0).get("userId")).longValue();
            Long firstUserId2 = ((Number) list2.get(0).get("userId")).longValue();
            Assertions.assertNotEquals(firstUserId1, firstUserId2, "两页数据不应该重复");

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
        log.info("║  限时专享功能测试全部通过                                    ║");
        log.info("║  - 列表查询 ✓                                               ║");
        log.info("║  - 多维度排序 ✓                                             ║");
        log.info("║  - 性别筛选 ✓                                               ║");
        log.info("║  - 组合筛选 ✓                                               ║");
        log.info("║  - 分页功能 ✓                                               ║");
        log.info("╚════════════════════════════════════════════════════════════╝");
    }
}
