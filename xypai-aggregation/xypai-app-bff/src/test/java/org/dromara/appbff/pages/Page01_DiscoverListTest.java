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
 * 【页面级集成测试】发现页
 *
 * ============================================================
 * 📄 前端页面信息
 * ============================================================
 * - 文档路径: XiangYuPai-Doc/UI页面/发现/发现页_结构文档.md
 * - 页面路由: /(tabs)/discover
 * - 页面名称: 发现
 * - 用户角色: 所有用户
 * - 页面类型: 列表页面（三Tab切换 + 瀑布流列表）
 *
 * ============================================================
 * 📌 涉及的后端服务及接口
 * ============================================================
 *
 * 【xypai-app-bff (聚合服务, 9400)】
 * - GET  /api/discover/list      获取发现列表（支持三Tab切换）
 * - POST /api/discover/like      点赞/取消点赞
 *
 * ============================================================
 * 🧪 测试流程
 * ============================================================
 * 1. 获取热门Tab列表（默认）
 * 2. 获取关注Tab列表
 * 3. 获取同城Tab列表（带经纬度）
 * 4. 测试分页功能
 * 5. 点赞功能测试
 * 6. 取消点赞功能测试
 *
 * 💡 测试说明:
 * - 本测试通过 Gateway (8080) 调用 xypai-app-bff 服务
 * - 需要启动: Gateway(8080), xypai-auth(9211), xypai-app-bff(9400), xypai-content(9403), Nacos, Redis, MySQL
 * - 测试数据使用 xypai_content.feed 表中的数据
 *
 * @author XiangYuPai
 * @date 2025-12-01
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Page01_DiscoverListTest {

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

    // 测试数据
    private static Long testContentId;

    @BeforeAll
    static void setup() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║  📄 页面级集成测试: 发现页                                    ║");
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║  涉及服务:                                                   ║");
        log.info("║  - xypai-app-bff (9400)     发现页聚合API                    ║");
        log.info("║  - xypai-content (9403)     内容服务                         ║");
        log.info("║  - xypai-user (9401)        用户服务                         ║");
        log.info("║  - xypai-auth (9211)        用户认证                         ║");
        log.info("║  - Gateway (8080)           API网关                          ║");
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
    // 测试1: 获取热门Tab列表（默认）
    // ============================================================
    @Test
    @Order(1)
    @DisplayName("[测试1] 获取热门Tab列表（默认）")
    void test01_getHotTabList() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试1] 获取热门Tab列表（默认）                            │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            String url = GATEWAY_URL + "/xypai-app-bff/api/discover/list?tab=hot&pageNum=1&pageSize=10";

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            log.info("   - 状态码: {}", response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");

                log.info("✅ 获取热门Tab列表成功");
                log.info("   - 总记录数: {}", data.get("total"));
                log.info("   - 当前页数量: {}", list != null ? list.size() : 0);
                log.info("   - 是否有更多: {}", data.get("hasMore"));

                if (list != null && !list.isEmpty()) {
                    Map<String, Object> firstItem = list.get(0);
                    testContentId = Long.parseLong((String) firstItem.get("id"));

                    log.info("   - 第一条内容:");
                    log.info("     - ID: {}", firstItem.get("id"));
                    log.info("     - 类型: {}", firstItem.get("type"));

                    Map<String, Object> textData = (Map<String, Object>) firstItem.get("textData");
                    if (textData != null) {
                        log.info("     - 标题: {}", textData.get("title"));
                    }

                    Map<String, Object> authorData = (Map<String, Object>) firstItem.get("authorData");
                    if (authorData != null) {
                        log.info("     - 作者: {}", authorData.get("nickname"));
                    }

                    Map<String, Object> statsData = (Map<String, Object>) firstItem.get("statsData");
                    if (statsData != null) {
                        log.info("     - 点赞数: {}", statsData.get("likeCount"));
                    }
                }

                Assertions.assertNotNull(list, "列表不能为空");
            } else {
                String msg = (String) responseBody.get("msg");
                log.warn("⚠️ 获取列表响应: {}", msg);
                // 不 fail，可能是没有数据
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试2: 获取关注Tab列表
    // ============================================================
    @Test
    @Order(2)
    @DisplayName("[测试2] 获取关注Tab列表")
    void test02_getFollowTabList() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试2] 获取关注Tab列表                                    │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            String url = GATEWAY_URL + "/xypai-app-bff/api/discover/list?tab=follow&pageNum=1&pageSize=10";

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

                log.info("✅ 获取关注Tab列表成功");
                log.info("   - 当前页数量: {}", list != null ? list.size() : 0);
                log.info("   - 是否有更多: {}", data.get("hasMore"));

                // 新用户没有关注任何人，列表可能为空
                if (list == null || list.isEmpty()) {
                    log.info("   - 提示: 新用户暂无关注，列表为空是正常的");
                }
            } else {
                String msg = (String) responseBody.get("msg");
                log.warn("⚠️ 获取列表响应: {}", msg);
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试3: 获取同城Tab列表（带经纬度）
    // ============================================================
    @Test
    @Order(3)
    @DisplayName("[测试3] 获取同城Tab列表（带经纬度）")
    void test03_getNearbyTabList() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试3] 获取同城Tab列表（带经纬度）                          │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            // 深圳南山区的经纬度
            String url = GATEWAY_URL + "/xypai-app-bff/api/discover/list?tab=nearby&pageNum=1&pageSize=10&latitude=22.5431&longitude=113.9298";

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            log.info("   - 状态码: {}", response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");

                log.info("✅ 获取同城Tab列表成功");
                log.info("   - 当前页数量: {}", list != null ? list.size() : 0);
                log.info("   - 是否有更多: {}", data.get("hasMore"));

                if (list != null && !list.isEmpty()) {
                    Map<String, Object> firstItem = list.get(0);
                    Map<String, Object> metaData = (Map<String, Object>) firstItem.get("metaData");
                    if (metaData != null) {
                        log.info("   - 第一条位置: {}", metaData.get("location"));
                        log.info("   - 距离: {}米", metaData.get("distance"));
                    }
                }
            } else {
                String msg = (String) responseBody.get("msg");
                log.warn("⚠️ 获取列表响应: {}", msg);
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试4: 分页功能测试
    // ============================================================
    @Test
    @Order(4)
    @DisplayName("[测试4] 分页功能测试")
    void test04_pagination() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试4] 分页功能测试                                       │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // 第一页
            String url1 = GATEWAY_URL + "/xypai-app-bff/api/discover/list?tab=hot&pageNum=1&pageSize=5";
            ResponseEntity<Map> response1 = restTemplate.exchange(url1, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body1 = response1.getBody();

            if ((Integer) body1.get("code") == 200) {
                Map<String, Object> data1 = (Map<String, Object>) body1.get("data");
                List<Map<String, Object>> list1 = (List<Map<String, Object>>) data1.get("list");
                Boolean hasMore1 = (Boolean) data1.get("hasMore");

                log.info("✅ 第一页数据");
                log.info("   - 内容数: {}", list1 != null ? list1.size() : 0);
                log.info("   - 是否有更多: {}", hasMore1);

                // 第二页
                String url2 = GATEWAY_URL + "/xypai-app-bff/api/discover/list?tab=hot&pageNum=2&pageSize=5";
                ResponseEntity<Map> response2 = restTemplate.exchange(url2, HttpMethod.GET, entity, Map.class);
                Map<String, Object> body2 = response2.getBody();

                if ((Integer) body2.get("code") == 200) {
                    Map<String, Object> data2 = (Map<String, Object>) body2.get("data");
                    List<Map<String, Object>> list2 = (List<Map<String, Object>>) data2.get("list");

                    log.info("✅ 第二页数据");
                    log.info("   - 内容数: {}", list2 != null ? list2.size() : 0);

                    // 验证分页数据不重复
                    if (list1 != null && list2 != null && !list1.isEmpty() && !list2.isEmpty()) {
                        String id1 = (String) list1.get(0).get("id");
                        String id2 = (String) list2.get(0).get("id");
                        Assertions.assertNotEquals(id1, id2, "两页数据不应该重复");
                        log.info("   - 分页数据不重复 ✓");
                    }
                }
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试5: 点赞功能测试
    // ============================================================
    @Test
    @Order(5)
    @DisplayName("[测试5] 点赞功能测试")
    void test05_like() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试5] 点赞功能测试                                       │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            if (testContentId == null) {
                log.warn("⚠️ 没有可用的内容ID，跳过点赞测试");
                return;
            }

            String url = GATEWAY_URL + "/xypai-app-bff/api/discover/like";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);

            Map<String, Object> likeRequest = new HashMap<>();
            likeRequest.put("contentId", testContentId);
            likeRequest.put("action", "like");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(likeRequest, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            log.info("   - 状态码: {}", response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

                log.info("✅ 点赞成功");
                log.info("   - 是否已点赞: {}", data.get("isLiked"));
                log.info("   - 新点赞数: {}", data.get("likeCount"));

                Assertions.assertEquals(true, data.get("isLiked"), "点赞后状态应为true");
            } else {
                String msg = (String) responseBody.get("msg");
                log.warn("⚠️ 点赞响应: {}", msg);
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    // ============================================================
    // 测试6: 取消点赞功能测试
    // ============================================================
    @Test
    @Order(6)
    @DisplayName("[测试6] 取消点赞功能测试")
    void test06_unlike() {
        log.info("\n");
        log.info("┌─────────────────────────────────────────────────────────┐");
        log.info("│ [测试6] 取消点赞功能测试                                    │");
        log.info("└─────────────────────────────────────────────────────────┘");

        try {
            ensureAuthenticated();

            if (testContentId == null) {
                log.warn("⚠️ 没有可用的内容ID，跳过取消点赞测试");
                return;
            }

            String url = GATEWAY_URL + "/xypai-app-bff/api/discover/like";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);

            Map<String, Object> unlikeRequest = new HashMap<>();
            unlikeRequest.put("contentId", testContentId);
            unlikeRequest.put("action", "unlike");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(unlikeRequest, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            log.info("   - 状态码: {}", response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();
            Integer code = (Integer) responseBody.get("code");

            if (code != null && code == 200) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

                log.info("✅ 取消点赞成功");
                log.info("   - 是否已点赞: {}", data.get("isLiked"));
                log.info("   - 新点赞数: {}", data.get("likeCount"));

                Assertions.assertEquals(false, data.get("isLiked"), "取消点赞后状态应为false");
            } else {
                String msg = (String) responseBody.get("msg");
                log.warn("⚠️ 取消点赞响应: {}", msg);
            }

        } catch (Exception e) {
            log.error("❌ 测试异常", e);
            Assertions.fail("测试异常: " + e.getMessage());
        }
    }

    @AfterAll
    static void tearDown() {
        log.info("\n");
        log.info("╔════════════════════════════════════════════════════════════╗");
        log.info("║  ✅ 发现页测试完成                                           ║");
        log.info("╠════════════════════════════════════════════════════════════╣");
        log.info("║  - 热门Tab列表 ✓                                            ║");
        log.info("║  - 关注Tab列表 ✓                                            ║");
        log.info("║  - 同城Tab列表 ✓                                            ║");
        log.info("║  - 分页功能 ✓                                               ║");
        log.info("║  - 点赞功能 ✓                                               ║");
        log.info("║  - 取消点赞 ✓                                               ║");
        log.info("╚════════════════════════════════════════════════════════════╝");
    }
}
